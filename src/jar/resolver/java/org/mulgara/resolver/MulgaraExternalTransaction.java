/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com) under contract to
 * Topaz Foundation. Portions created under this contract are
 * Copyright (c) 2007 Topaz Foundation
 * All Rights Reserved.
 */
package org.mulgara.resolver;

// Java 2 enterprise packages
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.EnlistableResource;

import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.QueryException;

/**
 * @created 2007-11-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
 *
 * @copyright &copy;2007 <a href="http://www.topazproject.org/">Topaz Foundation</a>
 *
 * @licence Open Software License v3.0
 */
public class MulgaraExternalTransaction implements MulgaraTransaction {
  private static final Logger logger =
    Logger.getLogger(MulgaraExternalTransaction.class.getName());
  private static enum ResourceState { IDLE, ACTIVE, SUSPENDED, FINISHED };

  private Xid xid;

  private Set<EnlistableResource> enlisted;
  private Set<EnlistableResource> started;
  private Set<EnlistableResource> needRollback;
  private Set<EnlistableResource> prepared;
  private Set<EnlistableResource> committed;
  private Set<EnlistableResource> rollbacked;

  private Map<EnlistableResource, XAResource> xaResources;
  private ResourceState xaResState;
  private int inuse;

  private MulgaraExternalTransactionFactory factory;
  private DatabaseOperationContext context;

  private boolean inXACompletion;

  private boolean hRollback;
  private int heurCode;
  private boolean rollback;
  private String rollbackCause;
  private boolean completed;
  private volatile long lastActive;

  MulgaraExternalTransaction(MulgaraExternalTransactionFactory factory, Xid xid, DatabaseOperationContext context)
      throws QueryException {
    this.factory = factory;
    this.context = context;
    this.xid = xid;

    this.enlisted = new HashSet<EnlistableResource>();
    this.started = new HashSet<EnlistableResource>();
    this.needRollback = new HashSet<EnlistableResource>();
    this.prepared = new HashSet<EnlistableResource>();
    this.committed = new HashSet<EnlistableResource>();
    this.rollbacked = new HashSet<EnlistableResource>();

    this.xaResources = new HashMap<EnlistableResource, XAResource>();
    this.xaResState = ResourceState.IDLE;
    this.inuse = 0;

    this.inXACompletion = false;

    this.hRollback = false;
    this.heurCode = 0;
    this.rollback = false;
    this.completed = false;
    this.lastActive = System.currentTimeMillis();

    this.context.initiate(this);
  }

  // We ignore reference counting in external transactions
  public void reference() throws MulgaraTransactionException {}
  public void dereference() throws MulgaraTransactionException {}

  /**
   * Calls through to {@link #abortTransaction(String,Throwable)} passing the message in
   * the cause as the message for the transaction abort.
   * @param cause The state triggering the abort.
   * @return The exception for aborting.
   * @throws MulgaraTransactionException Indicated failure to cleanly abort.
   */
  public MulgaraTransactionException abortTransaction(Throwable cause) throws MulgaraTransactionException {
    return abortTransaction(cause.getMessage(), cause);
  }

  public MulgaraTransactionException abortTransaction(String errorMessage, Throwable cause)
      throws MulgaraTransactionException {
    report("abortTransaction");

    // we should actually already have the mutex, but let's make sure
    acquireMutex(0L, true, MulgaraTransactionException.class);
    try {
      if (rollbackCause == null)
        rollbackCause = errorMessage;

      try {
        for (EnlistableResource resource : enlisted) {
          try {
            resource.abort();
          } catch (Throwable throw_away) {
            logger.warn("Difficulty aborting enlisted resource while aborting transaction", throw_away);
          }
        }
        for (EnlistableResource resource : prepared) {
          try {
            resource.abort();
          } catch (Throwable throw_away) {
            logger.warn("Difficulty aborting prepared resource while aborting transaction", throw_away);
          }
        }

        return new MulgaraTransactionException(errorMessage, cause);
      } finally {
        completed = true;
        factory.transactionComplete(this, rollbackCause);
      }
    } finally {
      releaseMutex();
    }
  }

  public void heuristicRollback(String cause) throws MulgaraTransactionException {
    report("heuristicRollback: " + cause);

    synchronized (factory.getMutexLock()) {
      if (factory.getMutexHolder() != null && factory.getMutexHolder() != Thread.currentThread()) {
        if (inXACompletion) {
          return;       // this txn is already being cleaned up, so let it go
        }
      }

      factory.acquireMutexWithInterrupt(0L, MulgaraTransactionException.class);
      inXACompletion = true;
    }

    try {
      if (hRollback)
        return;
      hRollback = true;

      if (rollbackCause == null)
        rollbackCause = cause;

      try {
        rollback(xid);
      } catch (XAException xa) {
        throw new MulgaraTransactionException("Failed heuristic rollback", xa);
      } finally {
        heurCode = heurCode == 0 ? XAException.XA_HEURRB : heurCode;
      }
    } finally {
      releaseMutex();
    }
  }

  public void execute(Operation operation, DatabaseMetadata metadata) throws MulgaraTransactionException {
    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      checkActive(MulgaraTransactionException.class);
      try {
        activateXARes(MulgaraTransactionException.class);
        try {
          long la = lastActive;
          lastActive = -1;

          operation.execute(context,
              context.getSystemResolver(),
              metadata);

          lastActive = (la != -1) ? System.currentTimeMillis() : -1;
        } finally {
          deactivateXARes(MulgaraTransactionException.class);
        }
      } catch (Throwable th) {
        try {
          heuristicRollback(th.toString());
        } catch (MulgaraTransactionException ex) {
          logger.error("Error in rollback after operation failure", ex);
        }
        throw new MulgaraTransactionException("Operation failed", th);
      }
    } finally {
      releaseMutex();
    }
  }

  public AnswerOperationResult execute(AnswerOperation ao) throws TuplesException {
    acquireMutex(0, false, TuplesException.class);
    try {
      checkActive(TuplesException.class);
      try {
        activateXARes(TuplesException.class);
        try {
          long la = lastActive;
          lastActive = -1;

          ao.execute();

          lastActive = (la != -1) ? System.currentTimeMillis() : -1;

          return ao.getResult();
        } finally {
          deactivateXARes(TuplesException.class);
        }
      } catch (Throwable th) {
        try {
          logger.warn("Error in answer operation triggered rollback", th);
          heuristicRollback(th.toString());
        } catch (MulgaraTransactionException ex) {
          logger.error("Error in rollback after answer-operation failure", ex);
        }
        throw new TuplesException("Request failed", th);
      }
    } finally {
      releaseMutex();
    }
  }

  // FIXME: See if we can't rearrange things to allow this to be deleted.
  public void execute(TransactionOperation to) throws MulgaraTransactionException {
    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      checkActive(MulgaraTransactionException.class);
      activateXARes(MulgaraTransactionException.class);
      try {
        long la = lastActive;
        lastActive = -1;

        to.execute();

        lastActive = (la != -1) ? System.currentTimeMillis() : -1;
      } finally {
        deactivateXARes(MulgaraTransactionException.class);
      }
    } finally {
      releaseMutex();
    }
  }

  private <T extends Throwable> void checkActive(Class<T> exc) throws T {
    if (hRollback)
      throw MulgaraTransactionFactory.newException(exc, "Transaction was heuristically rolled back. Reason: " + rollbackCause);
    if (rollback)
      throw MulgaraTransactionFactory.newException(exc, "Transaction was rolled back. Reason: " + rollbackCause);
    if (completed)
      throw MulgaraTransactionFactory.newException(exc, "Transaction has been completed");
  }

  private <T extends Throwable> void activateXARes(Class<T> exc) throws T {
    assert xaResState != ResourceState.FINISHED : "Unexpected resource-state: state=" + xaResState;

    if (xaResState == ResourceState.ACTIVE) {
      inuse++;
      return;
    }

    assert inuse == 0 : "Unexpected use-count: state=" + xaResState + ", inuse=" + inuse;
    boolean wasStarted = (xaResState == ResourceState.SUSPENDED);
    xaResState = ResourceState.ACTIVE;

    int flags = wasStarted ? XAResource.TMRESUME : XAResource.TMNOFLAGS;
    for (EnlistableResource eres : wasStarted ? started : enlisted) {
      XAResource res = xaResources.get(eres);
      try {
        res.start(xid, flags);
        if (!wasStarted) {
          started.add(eres);
        }
      } catch (XAException xae) {
        started.remove(eres);
        if (isRollback(xae)) needRollback.add(eres);

        // end started resources so we're in a consistent state
        flags = wasStarted ? XAResource.TMSUSPEND : XAResource.TMFAIL;
        for (EnlistableResource eres2 : wasStarted ? started : enlisted) {
          XAResource res2 = xaResources.get(eres2);
          if (res2 == res) {
            break;
          }

          try {
            res2.end(xid, flags);
          } catch (XAException xae2) {
            logger.error("Error ending resource '" + res2 + "' after start failure", xae2);
          }
        }

        xaResState = wasStarted ? ResourceState.SUSPENDED : ResourceState.FINISHED;

        throw MulgaraTransactionFactory.newExceptionOrCause(exc, "Error starting resource '" + res + "'", xae);
      }
    }

    inuse = 1;
  }

  private <T extends Throwable> void deactivateXARes(Class<T> exc) throws T {
    if (xaResState == ResourceState.FINISHED) {
      return;
    }

    assert xaResState == ResourceState.ACTIVE : "Unexpected resource-state: state=" + xaResState;
    assert inuse > 0 : "Unexpected use-count: state=" + xaResState + ", inuse=" + inuse;

    inuse--;
    if (inuse > 0) {
      return;
    }

    int flags = XAResource.TMSUSPEND;
    T error = null;

    for (Iterator<EnlistableResource> iter = started.iterator(); iter.hasNext(); ) {
      EnlistableResource eres = iter.next();
      XAResource res = xaResources.get(eres);
      try {
        res.end(xid, flags);
      } catch (XAException xae) {
        iter.remove();
        if (isRollback(xae)) needRollback.add(eres);

        if (error == null) {
          error = MulgaraTransactionFactory.newExceptionOrCause(exc, "Error ending resource '" + res + "'", xae);
        } else {
          logger.error("Error ending resource '" + res + "'", xae);
        }
      }
    }

    xaResState = ResourceState.SUSPENDED;

    if (error != null) {
      throw error;
    }
  }

  private void endXARes(boolean success) throws XAException {
    if (xaResState != ResourceState.SUSPENDED && xaResState != ResourceState.ACTIVE) {
      return;
    }

    int flags = success ? XAResource.TMSUCCESS : XAResource.TMFAIL;
    XAException error = null;

    for (Iterator<EnlistableResource> iter = started.iterator(); iter.hasNext(); ) {
      EnlistableResource eres = iter.next();
      XAResource res = xaResources.get(eres);
      try {
        res.end(xid, flags);
      } catch (XAException xae) {
        iter.remove();
        if (isRollback(xae)) needRollback.add(eres);

        if (error == null) {
          error = xae;
        } else {
          logger.error("Error ending resource '" + res + "'", xae);
        }
      }
    }

    xaResState = ResourceState.FINISHED;

    if (error != null) {
      throw error;
    }
  }

  public void enlist(EnlistableResource enlistable) throws MulgaraTransactionException {
    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        XAResource res = enlistable.getXAResource();
        for (EnlistableResource eres : enlisted) {
          if (res.isSameRM(xaResources.get(eres))) {
            return;
          }
        }
        enlisted.add(enlistable);
        xaResources.put(enlistable, res);

        if (xaResState == ResourceState.ACTIVE) {
          res.start(xid, XAResource.TMNOFLAGS);
          started.add(enlistable);
        } else if (xaResState == ResourceState.SUSPENDED) {
          res.start(xid, XAResource.TMNOFLAGS);
          res.end(xid, XAResource.TMSUSPEND);
          started.add(enlistable);
        }
      } catch (XAException ex) {
        if (isRollback(ex)) needRollback.add(enlistable);
        throw new MulgaraTransactionException("Failed to enlist resource", ex);
      }
    } finally {
      releaseMutex();
    }
  }

  public long lastActive() {
    return lastActive;
  }

  //
  // Methods used to manage transaction from XAResource.
  //

  void commit(Xid xid) throws XAException {
    report("commit");

    acquireMutex(0, true, XAException.class);
    try {
      lastActive = -1;

      // FIXME: Consider the possiblity prepare failed, or was incomplete.
      for (EnlistableResource er : prepared) {
        xaResources.get(er).commit(xid, false);
        committed.add(er);
      }
      cleanupTransaction();
    } finally {
      releaseMutex();
    }
  }

  boolean isHeuristicallyRollbacked() {
    return hRollback;
  }

  boolean isHeuristicallyCommitted() {
    return false;
  }

  int getHeuristicCode() {
    return heurCode;
  }

  String getRollbackCause() {
    return rollbackCause;
  }

  boolean isRollbacked() {
    return rollback;
  }

  void prepare(Xid xid) throws XAException {
    report("prepare");

    acquireMutex(0, true, XAException.class);
    try {
      long la = lastActive;
      lastActive = -1;

      endXARes(true);
      for (Iterator<EnlistableResource> iter = started.iterator(); iter.hasNext(); ) {
        EnlistableResource er = iter.next();
        try {
          if (xaResources.get(er).prepare(xid) == XAResource.XA_OK) {
            prepared.add(er);
          } else {
            iter.remove();
          }
        } catch (XAException xae) {
          if (isRollback(xae)) iter.remove();
          throw xae;
        }
      }

      lastActive = (la != -1) ? System.currentTimeMillis() : -1;
    } finally {
      releaseMutex();
    }
  }

  /**
   * Perform rollback.  Only throws exception if transaction is subject to
   * Heuristic Completion.
   */
  void rollback(Xid xid) throws XAException {
    report("rollback");

    acquireMutex(0, true, XAException.class);
    try {
      lastActive = -1;
      try {
        rollback = true;
        Map<EnlistableResource, XAException> rollbackFailed = new HashMap<EnlistableResource, XAException>();

        try {
          endXARes(false);
        } catch (XAException ex) {
          logger.error("Error ending resources - attempting to rollback anyway", ex);
        }

        started.addAll(needRollback);
        for (EnlistableResource er : started) {
          try {
            if (!committed.contains(er)) {
              xaResources.get(er).rollback(xid);
              rollbacked.add(er);
            }
          } catch (XAException ex) {
            logger.error("Attempt to rollback resource failed", ex);
            rollbackFailed.put(er, ex);
          }
        }

        if (rollbackFailed.isEmpty()) {
          if (committed.isEmpty()) {        // Clean failure and rollback
            return; // SUCCESSFUL ROLLBACK - RETURN
          } else {                          // No rollback-failure, but partial commit
            heurCode = XAException.XA_HEURMIX;
            throw new XAException(heurCode);
          }
        } else {
          // Something went wrong - start by assuming if one committed all committed
          heurCode = (committed.isEmpty()) ? 0 : XAException.XA_HEURCOM;
          // Then check every rollback failure code for a contradiction to all committed.
          for (XAException xaex : rollbackFailed.values()) {
            switch (xaex.errorCode) {
              case XAException.XA_HEURHAZ:
              case XAException.XAER_NOTA:
              case XAException.XAER_RMERR:
              case XAException.XAER_RMFAIL:
              case XAException.XAER_INVAL:
              case XAException.XAER_PROTO:
                // All these amount to not knowing the result - so we have a hazard
                // unless we already know we have a mixed result.
                if (heurCode != XAException.XA_HEURMIX) {
                  heurCode = XAException.XA_HEURHAZ;
                }
                break;
              case XAException.XA_HEURCOM:
                if (!rollbacked.isEmpty() || heurCode == XAException.XA_HEURRB) {
                  // We know something else was rollbacked, so we know we have a mixed result.
                  heurCode = XAException.XA_HEURMIX;
                } else if (heurCode == 0) {
                  heurCode = XAException.XA_HEURCOM;
                } // else it's a HEURHAZ or a HEURCOM and stays that way.
                break;
              case XAException.XA_HEURRB:
                if (!committed.isEmpty() || heurCode == XAException.XA_HEURCOM) {
                  heurCode = XAException.XA_HEURMIX;
                } else if (heurCode == 0) {
                  heurCode = XAException.XA_HEURRB;
                } // else it's a HEURHAZ or a HEURRB and stays that way.
                break;
              case XAException.XA_HEURMIX:
                // It can't get worse than, we know we have a mixed result.
                heurCode = XAException.XA_HEURMIX;
                break;
              default:
                // The codes above are the only codes permitted from a rollback() so
                // anything else indicates a serious error in the resource-manager.
                throw new XAException(XAException.XAER_RMERR);
            }
          }

          throw new XAException(heurCode);
        }
      } finally {
        cleanupTransaction();
      }
    } finally {
      releaseMutex();
    }
  }


  Xid getXid() {
    return xid;
  }

  private void cleanupTransaction() throws XAException {
    report("cleanupTransaction");
    try {
      factory.transactionComplete(this, rollbackCause);
    } catch (MulgaraTransactionException em) {
      try {
        logger.error("Failed to cleanup transaction", em);
        abortTransaction("Failure in cleanup", em);
        throw new XAException(XAException.XAER_RMERR);
      } catch (MulgaraTransactionException em2) {
        logger.error("Failed to abort transaction on cleanup failure", em2);
        throw new XAException(XAException.XAER_RMFAIL);
      }
    } finally {
      completed = true;
    }
  }

  private <T extends Throwable> void acquireMutex(long timeout, boolean isXACompletion, Class<T> exc) throws T {
    synchronized (factory.getMutexLock()) {
      factory.acquireMutex(timeout, exc);
      inXACompletion = isXACompletion;
    }
  }

  private void releaseMutex() {
    factory.releaseMutex();
  }

  private static boolean isRollback(XAException xae) {
    return xae.errorCode >= XAException.XA_RBBASE && xae.errorCode <= XAException.XA_RBEND;
  }

  private void report(String desc) {
    if (logger.isInfoEnabled()) {
      logger.info(desc + ": " + System.identityHashCode(this));
    }
  }
}
