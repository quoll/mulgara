/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.distributed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.log4j.Logger;

import org.mulgara.resolver.spi.AbstractXAResource;
import org.mulgara.resolver.spi.AbstractXAResource.RMInfo;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.AbstractXAResource.TxInfo;

/**
 * This is an implementation of {@link XAResource} that presents a collection of xa-resources as
 * a single xa-resource. In doing so, this implements parts of what a transaction-manager must
 * provide in order to present a unified view. For example, a one-phase commit may need to be
 * turned into a two-phase commit over the underlying resources, and all underlying resources must
 * be aborted if an RMFAIL exception occurs.
 *
 * <p>XAResource's may be enlisted via {@link #enlistResource}; the enlistement is only valid for
 * the duration of the transaction.
 *
 * <p>This class is partially thread-safe, as required by JTA. Specifically, {@link #prepare},
 * {@link #commit}, and {@link #rollback} may be invoked concurrently with {@link #start} and
 * {@link #end} so long as these do not involve the same xid. Also, there is no requirement that
 * the same thread be used for any two operations on a given xid. However, {@link #start} and
 * {@link #end} may not be nested, nor may any two methods be invoked concurrently with the same
 * xid.
 *
 * <p>Limitations:
 * <ul>
 *   <li>Transaction-id's (Xid's) are not remembered persistently, so {@link #recover} and
 *       {@link #forget} across a restart won't work</li>
 *   <li>{@link #isSameRM} cannot be properly implemented.</li>
 * </ul>
 *
 * @created 2008-02-16
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Project</a>
 * @licence Apache License v2.0
 */
public class MultiXAResource
    extends AbstractXAResource<RMInfo<MultiXAResource.MultiTxInfo>,MultiXAResource.MultiTxInfo> {

  private static final Logger logger = Logger.getLogger(MultiXAResource.class);

  private final Set<XAResource> ended = new HashSet<XAResource>();

  private volatile MultiTxInfo curTx;


  /**
   * Create a new Multi-XAResource.
   *
   * @param transactionTimeout transaction timeout period, in seconds
   * @param resolverFactory    the resolver-factory we belong to
   */
  public MultiXAResource(int transactionTimeout, ResolverFactory resolverFactory) {
    super(transactionTimeout, resolverFactory);
  }

  protected RMInfo<MultiTxInfo> newResourceManager() {
    return new RMInfo<MultiTxInfo>();
  }

  protected MultiTxInfo newTransactionInfo() {
    return new MultiTxInfo();
  }

  /**
   * Enlist a resource in the current transaction. This may only be invoked while a transaction is
   * in the ACTIVE state (between a {@link #start} and {@link #end}).
   *
   * @param res the resource to enlist
   * @throws XAException if an error occurs
   */
  public void enlistResource(XAResource res) throws XAException {
    if (curTx == null)
      throw new IllegalStateException("No transaction active");

    if (logger.isDebugEnabled()) {
      logger.debug("enlisting resource '" + res + "' in txn '" + formatXid(curTx.xid) + "'");
    }

    int elapsed = (int)((System.currentTimeMillis() - curTx.startTime) / 1000);
    res.setTransactionTimeout(Math.max(transactionTimeout - elapsed, 10));

    for (XAResource r : curTx.resources) {
      if (res.isSameRM(r)) {
        res.start(curTx.xid, TMJOIN);
        return;
      }
    }

    curTx.resources.add(res);
    try {
      res.start(curTx.xid, TMNOFLAGS);
    } catch (Throwable t) {
      if (isCompleted(t)) {
        curTx.resources.remove(res);
        // turn this into a non-rmfail exception so rollback() is called for us
        t = (XAException)new XAException(XAException.XAER_RMERR).initCause(t);
      } else {
        ended.add(res);
      }

      throwExc(t);
    }
  }

  /* flags - One of TMNOFLAGS, TMJOIN, or TMRESUME
   * Possible exceptions are: XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_DUPID, XAER_OUTSIDE, XAER_NOTA,
   * XAER_INVAL, or XAER_PROTO
   */
  protected void doStart(MultiTxInfo txInfo, int flags, boolean isNew) throws XAException {
    // check we're not already between a start and end
    if (curTx != null) throw new XAException(XAException.XAER_PROTO);

    // mark that we're active
    curTx = txInfo;
    txInfo.state = MultiTxInfo.States.ACTIVE;

    // propagate the start
    if (flags == TMJOIN) {
      throw new XAException("Can't handle joins");
    } else if (flags == XAResource.TMRESUME) {
      for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
        XAResource r = iter.next();
        try {
          r.start(txInfo.xid, flags);
        } catch (Throwable t) {
          if (isCompleted(t)) {
            iter.remove();
            // turn this into a non-rmfail exception so rollback() is called for us
            t = (XAException)new XAException(XAException.XAER_RMERR).initCause(t);
          }

          for (Iterator<XAResource> iter2 = txInfo.resources.iterator(); iter2.hasNext(); ) {
            XAResource r2 = iter2.next();
            if (r2 == r) continue;

            try {
              r2.end(txInfo.xid, TMFAIL);
            } catch (Throwable t2) {
              logger.error("Error suspending resource '" + r2 + "' while handling aborted start", t2);
              if (isCompleted(t2)) iter2.remove();
            }
          }

          curTx = null;
          txInfo.state = MultiTxInfo.States.IDLE;

          throwExc(t);
        }
      }
    }
  }

  /* flags - One of TMSUCCESS, TMFAIL, or TMSUSPEND
   * Possible XAException values are: XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, XAER_PROTO, or
   * XA_RB*.
   */
  protected void doEnd(MultiTxInfo txInfo, int flags) throws XAException {
    // check we're between a start and end
    if (curTx != txInfo) throw new XAException(XAException.XAER_PROTO);

    // propagate the end
    Throwable exc = null;

    for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
      XAResource r = iter.next();
      if (ended.contains(r)) continue;

      try {
        r.end(txInfo.xid, flags);
      } catch (Throwable t) {
        if (isCompleted(t)) iter.remove();
        else if (flags == TMSUSPEND) ended.add(r);

        if (exc == null) exc = t;
        else logger.error("2nd or more exception during end; resource = '" + r + "'", t);
      }
    }

    // mark that we're idle
    curTx = null;
    txInfo.state = MultiTxInfo.States.IDLE;

    // clean up if we failed on suspend
    if (flags == TMSUSPEND && exc != null) {
      for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
        XAResource r = iter.next();
        if (ended.contains(r)) continue;

        try {
          r.end(txInfo.xid, TMFAIL);
        } catch (Throwable t) {
          if (isCompleted(t)) iter.remove();
          logger.error("2nd or more exception during end; resource = '" + r + "'", t);
        }
      }
    }

    ended.clear();

    // turn this into a non-rmfail exception so rollback() is called for us
    if (isCompleted(exc)) {
      throw (XAException)new XAException(XAException.XAER_RMERR).initCause(exc);
    }
    if (exc != null) throwExc(exc);
  }

  /* Possible exception values are: XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
   * XAER_PROTO
   */
  protected int doPrepare(MultiTxInfo txInfo) throws XAException {
    // check that this tx isn't in the ACTIVE state
    if (txInfo == curTx) throw new XAException(XAException.XAER_PROTO);

    // tell everyone to prepare and gather their votes
    Throwable exc = null;
    txInfo.state = MultiTxInfo.States.PREPARING;

    for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
      XAResource r = iter.next();
      try {
        if (r.prepare(txInfo.xid) == XA_RDONLY) {
          iter.remove();      // read-only don't participate in commit/rollback
        }
      } catch (Throwable t) {
        if (logger.isDebugEnabled()) {
          logger.debug("prepare vetoed by '" + r + "'", t);
        }
        if (isCompleted(t) || isRollback(t)) iter.remove();

        if (exc == null) exc = t;
      }
    }

    txInfo.state = MultiTxInfo.States.PREPARED;

    // turn this into a non-rmfail/no-rb exception so rollback() is called for us
    if (isCompleted(exc) || isRollback(exc)) {
      throw (XAException)new XAException(XAException.XAER_RMERR).initCause(exc);
    }
    if (exc != null) throwExc(exc);

    //return (txInfo.resources.size() > 0) ? XA_OK : XA_RDONLY;
    return XA_OK;       // JOTM bug
  }

  /* Possible XAExceptions are XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX, XAER_RMERR,
   * XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or XAER_PROTO
   */
  protected void doCommit(MultiTxInfo txInfo) throws XAException {
    // check that this tx isn't in the ACTIVE state
    if (txInfo == curTx) throw new XAException(XAException.XAER_PROTO);

    Throwable exc = null;
    txInfo.state = MultiTxInfo.States.COMMITTING;

    try {
      // ready to do commit
      int numCmt = 0;

      for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
        XAResource r = iter.next();
        try {
          if (numCmt == 0 && exc instanceof XAException &&
              (isRollback(exc) || ((XAException)exc).errorCode == XAException.XA_HEURRB)) {
            r.rollback(txInfo.xid);    // the first commit failed with a RB, so we roll back all
          } else {
            r.commit(txInfo.xid, false);
            numCmt++;
          }
          iter.remove();
        } catch (Throwable t) {
          if (!isHeuristic(t)) iter.remove();

          if (exc == null) {
            exc = t;
          } else if (isHeuristic(t) && isHeuristic(exc) &&
                     ((XAException)t).errorCode != ((XAException)exc).errorCode &&
                     ((XAException)exc).errorCode != XAException.XA_HEURMIX ) {
            exc = (XAException)new XAException(XAException.XA_HEURMIX).initCause(exc);
            logger.error("2nd or more exception during commit; resource = '" + r + "'", t);
          } else {
            logger.error("2nd or more exception during commit; resource = '" + r + "'", t);
          }
        }
      }

      if (exc instanceof XAException) {
        XAException xae = (XAException)exc;
        if (xae.errorCode == XAException.XA_HEURMIX) throw xae;
        if (numCmt == 0 && xae.errorCode == XAException.XA_HEURRB) throw xae;
      }
      if (exc != null) {
        if (numCmt == 0) throw (XAException)new XAException(XAException.XA_HEURRB).initCause(exc);
        else throw (XAException)new XAException(XAException.XA_HEURMIX).initCause(exc);
      }
    } finally {
      txInfo.state = MultiTxInfo.States.FINISHED;
    }
  }

  /* Possible XAExceptions are XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX, XAER_RMERR,
   * XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or XAER_PROTO
   */
  protected void doRollback(MultiTxInfo txInfo) throws XAException {
    // check that this tx isn't in the ACTIVE state
    if (txInfo == curTx) throw new XAException(XAException.XAER_PROTO);

    Throwable exc = null;
    txInfo.state = MultiTxInfo.States.ROLLINGBACK;
    try {
      for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
        XAResource r = iter.next();
        try {
          r.rollback(txInfo.xid);
          iter.remove();
        } catch (Throwable t) {
          if (!isHeuristic(t)) iter.remove();

          if (exc == null) {
            exc = t;
          } else if (isHeuristic(t) && isHeuristic(exc) &&
                     ((XAException)t).errorCode != ((XAException)exc).errorCode &&
                     ((XAException)exc).errorCode != XAException.XA_HEURMIX ) {
            exc = (XAException)new XAException(XAException.XA_HEURMIX).initCause(exc);
            logger.error("2nd or more exception during rollback; resource = '" + r + "'", t);
          } else {
            logger.error("2nd or more exception during rollback; resource = '" + r + "'", t);
          }
        }
      }

      if (exc instanceof XAException) {
        if (((XAException)exc).errorCode == XAException.XA_HEURMIX) throw (XAException)exc;
      }
      if (exc != null) {
        throw (XAException)new XAException(XAException.XA_HEURHAZ).initCause(exc);
      }
    } finally {
      txInfo.state = MultiTxInfo.States.FINISHED;
    }
  }

  // Possible exception values are: XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or XAER_PROTO.
  protected void doForget(MultiTxInfo txInfo) throws XAException {
    Throwable exc = null;

    for (Iterator<XAResource> iter = txInfo.resources.iterator(); iter.hasNext(); ) {
      XAResource r = iter.next();
      try {
        r.forget(txInfo.xid);
        iter.remove();
      } catch (Throwable t) {
        if (isCompleted(t)) {
          logger.debug("transaction " + formatXid(txInfo.xid) + " was not active on resource '" +
                       r + "'", t);
          iter.remove();
          continue;
        }

        if (exc == null) {
          exc = t;
        } else {
          logger.error("2nd or more exception during forget; resource = '" + r + "'", t);
        }
      }
    }

    if (exc != null) throwExc(exc);
  }

  /* flag - One of TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS
   * Possible exception values are: XAER_RMERR, XAER_RMFAIL, XAER_INVAL, and XAER_PROTO
   */
  public Xid[] recover(int flag) throws XAException {
    Set<Xid> res = new HashSet<Xid>();

    if ((flag & TMSTARTRSCAN) != 0) {
      for (MultiTxInfo txInfo : resourceManager.transactions.values()) {
        if (txInfo.state == MultiTxInfo.States.PREPARING ||
            txInfo.state == MultiTxInfo.States.PREPARED ||
            txInfo.state == MultiTxInfo.States.COMMITTING ||
            txInfo.state == MultiTxInfo.States.ROLLINGBACK ||
            txInfo.state == MultiTxInfo.States.FINISHED) {
          res.add(txInfo.xid);
        }
      }
    }

    return res.toArray(new Xid[res.size()]);
  }

  /**
   * Tests whether the exception indicates that the resource has completed its participation in
   * the transaction. See Table 6.4 (page 62) of X/Open for RMFAIL; for NOTA see X/Open pages 37 ff
   * (xa_end), page 62 (Table 6.4), page 15 ("Rollback-Only", last two sentences), page 18
   * ("Unilateral RM Action"),
   *
   * @param t the exception to test
   * @return true if the resource is done with this transaction
   */
  private static boolean isCompleted(Throwable t) {
    return (t instanceof XAException) &&
            (((XAException)t).errorCode == XAException.XAER_RMFAIL ||
             ((XAException)t).errorCode == XAException.XAER_NOTA);
  }

  private static final void throwExc(Throwable t) throws XAException {
    if (t instanceof XAException) throw (XAException)t;
    if (t instanceof RuntimeException) throw (RuntimeException)t;
    throw (Error)t;
  }

  public static class MultiTxInfo extends TxInfo {
    public enum States { IDLE, ACTIVE, PREPARING, PREPARED, COMMITTING, ROLLINGBACK, FINISHED }

    // should be a Set, but easier to test with List
    public final List<XAResource> resources = new ArrayList<XAResource>();
    public States                 state = States.IDLE;
    public long                   startTime = System.currentTimeMillis();
  }
}
