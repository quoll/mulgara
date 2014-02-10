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

// Java2 packages
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.server.ResourceManagerInstanceAdaptor;
import org.mulgara.util.Assoc1toNMap;

/**
 * Provides an external JTA-compliant TransactionManager with the ability to
 * control Mulgara Transactions.
 *
 * @created 2007-11-07
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2006 <a href="http://www.topazproject.org/">Topaz Project</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class MulgaraXAResourceContext {
  private static final Logger logger =
    Logger.getLogger(MulgaraXAResourceContext.class.getName());
  /**
   * Map from keyed from the {@link Integer} value of the various flags
   * defined in {@link XAResource} and mapping to the formatted name for that
   * flag.
   */
  private final static Map<Integer, String> flagMap = new HashMap<Integer, String>();

  static {
    flagMap.put(XAResource.TMENDRSCAN,   "TMENDRSCAN");
    flagMap.put(XAResource.TMFAIL,       "TMFAIL");
    flagMap.put(XAResource.TMJOIN,       "TMJOIN");
    flagMap.put(XAResource.TMONEPHASE,   "TMONEPHASE");
    flagMap.put(XAResource.TMRESUME,     "TMRESUME");
    flagMap.put(XAResource.TMSTARTRSCAN, "TMSTARTRSCAN");
    flagMap.put(XAResource.TMSUCCESS,    "TMSUCCESS");
    flagMap.put(XAResource.TMSUSPEND,    "TMSUSPEND");
  }

  private final MulgaraExternalTransactionFactory factory;

  protected final DatabaseSession session;

  private final Assoc1toNMap<MulgaraExternalTransaction, Xid> xa2xid;

  private final UUID uniqueId;

  MulgaraXAResourceContext(MulgaraExternalTransactionFactory factory, DatabaseSession session) {
    if (logger.isDebugEnabled()) logger.debug("Creating MulgaraXAResource");
    this.factory = factory;
    this.session = session;
    this.xa2xid = new Assoc1toNMap<MulgaraExternalTransaction, Xid>();
    this.uniqueId = UUID.randomUUID();
  }

  public XAResource getResource(boolean writing) {
    return new MulgaraXAResource(writing);
  }

  private class MulgaraXAResource implements XAResource,
      ResourceManagerInstanceAdaptor {
    private final boolean writing;

    public MulgaraXAResource(boolean writing) {
      this.writing = writing;
    }

    /**
     * Commit transaction identified by xid.
     *
     * Transaction must be Idle, Prepared, or Heuristically-Completed.
     * If transaction not Heuristically-Completed we are required to finish it,
     * clean up, and forget it.
     * If transaction is Heuristically-Completed we throw an exception and wait
     * for a call to forget().
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing commit: " + parseXid(xid));
        MulgaraExternalTransaction xa = xa2xid.get1(xid);
        if (xa == null) {
          throw new XAException(XAException.XAER_NOTA);
        } else if (xa.isHeuristicallyRollbacked()) {
          // HEURRB causes difficulties with JOTM - so throw the less precise
          // but still correct RBROLLBACK.
          // Note: Found the problem here - The J2EE Connector Architecture
          // 7.6.2.2 requires an XA_RB* exception in the case of 1PC and 7.6.2.5
          // implies that HEURRB is not permitted during 2PC - this seems broken
          // to me, but that's the spec.
//          throw newXAException(XAException.XA_HEURRB, xa.getRollbackCause());
          throw newXAException(XAException.XA_RBROLLBACK, xa.getRollbackCause());
        } else if (xa.isHeuristicallyCommitted()) {
          throw new XAException(XAException.XA_HEURCOM);
        }

        if (onePhase) {
          try {
            xa.prepare(xid);
          } catch (XAException ex) {
            if (ex.errorCode != XAException.XA_RDONLY) {
              doRollback(xa, xid);
            }
            // Note: XA spec requires us to forget about transactions that fail
            // during commit.  doRollback throws exception under Heuristic
            // Completion - when we do want to remember transaction.
            xa2xid.remove1(xa);
            throw ex;
          }
        }

        try {
          xa.commit(xid);
          xa2xid.remove1(xa);
        } catch (XAException ex) {
          // We are not allowed to forget this transaction if we completed
          // heuristically.
          switch (ex.errorCode) { 
            case XAException.XA_HEURHAZ:
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURRB:
            case XAException.XA_HEURMIX:
              throw ex;
            default:
              xa2xid.remove1(xa);
              throw ex;
          }
        }
      } finally {
        factory.releaseMutex();
      }
    }

    /**
     * Deactivate a transaction.
     *
     * TMSUCCESS: Move to Idle and await call to rollback, prepare, or commit.
     * TMFAIL: Move to RollbackOnly; await call to rollback.
     * TMSUSPEND: Move to Idle and await start(TMRESUME) or end(TMSUCCESS|FAIL)
     *
     * In all cases disassociate from current session.
     */
    public void end(Xid xid, int flags) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing end(" + formatFlags(flags) + "): " + parseXid(xid));
        MulgaraExternalTransaction xa = xa2xid.get1(xid);
        if (xa == null) {
          throw new XAException(XAException.XAER_NOTA);
        }
        switch (flags) {
          case TMFAIL:
            doRollback(xa, xid);
            break;
          case TMSUCCESS:
            if (xa.isHeuristicallyRollbacked()) {
              throw newXAException(XAException.XA_RBPROTO, xa.getRollbackCause());
            }
            break;
          case TMSUSPEND: // Should I be tracking the xid's state to ensure
                          // conformance with the X/Open state diagrams?
            break;
          default:
            logger.error("Invalid flag passed to end() : " + flags);
            throw new XAException(XAException.XAER_INVAL);
        }

        try {
          // If XA is currently associated with session, disassociate it.
          factory.disassociateTransaction(xa);
        } catch (MulgaraTransactionException em) {
          logger.error("Error disassociating transaction from session", em);
          throw new XAException(XAException.XAER_PROTO);
        }
      } finally {
        factory.releaseMutex();
      }
    }

    public void forget(Xid xid) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing forget: " + parseXid(xid));
        MulgaraExternalTransaction xa = xa2xid.get1(xid);
        if (xa == null) {
          throw new XAException(XAException.XAER_NOTA);
        }
        try {
          if (!xa.isHeuristicallyRollbacked()) {
            try {
              xa.abortTransaction(new MulgaraTransactionException("External XA Manager specified 'forget'"));
            } catch (MulgaraTransactionException em) {
              logger.error("Failed to abort transaction in forget", em);
              throw new XAException(XAException.XAER_RMERR);
            }
          }
        } finally {
          xa2xid.remove1(xa);
        }
      } finally {
        factory.releaseMutex();
      }
    }

    public int getTransactionTimeout() throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        if (logger.isDebugEnabled()) logger.debug("Performing getTransactionTimeout");
        return (int) (session.getTransactionTimeout() / 1000);
      } finally {
        factory.releaseMutex();
      }
    }

    public boolean isSameRM(XAResource xares) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        if (logger.isDebugEnabled()) logger.debug("Performing isSameRM");
        if (xares.getClass() != MulgaraXAResource.class) {
          return false;
        } else {
          // Based on X/Open-XA-TP section 3.2 I believe a 'Resource Manager
          // Instance' corresponds to a session, as each session 'supports
          // independent transaction completion'.
          return session == ((MulgaraXAResource)xares).getSession();
        }
      } finally {
        factory.releaseMutex();
      }
    }


    public int prepare(Xid xid) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing prepare: " + parseXid(xid));
        MulgaraExternalTransaction xa = xa2xid.get1(xid);
        if (xa == null) {
          throw new XAException(XAException.XAER_NOTA);
        } else if (xa.isRollbacked()) {
          throw new XAException(XAException.XA_RBROLLBACK);
        }

        xa.prepare(xid);

        return XA_OK;
      } finally {
        factory.releaseMutex();
      }
    }

    /**
     * We don't currently support recover.
     * FIXME: We should at least handle the case where we are asked to recover
     * when we haven't crashed.
     */
    public Xid[] recover(int flag) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        if (logger.isDebugEnabled()) logger.debug("Performing recover");
        return new Xid[] {};
      } finally {
        factory.releaseMutex();
      }
    }


    public void rollback(Xid xid) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing rollback: " + parseXid(xid));
        MulgaraExternalTransaction xa = xa2xid.get1(xid);
        if (xa == null) {
          throw new XAException(XAException.XAER_NOTA);
        }

        doRollback(xa, xid);
        // If we don't throw a Heuristic Exception we need to forget this
        // transaction.  doRollback only throws Heuristic Exceptions.
        xa2xid.remove1(xa);
      } finally {
        factory.releaseMutex();
      }
    }


    /**
     * Performs rollback.  Only throws exception if transaction is subject to
     * Heuristic Completion.
     */
    private void doRollback(MulgaraExternalTransaction xa, Xid xid) throws XAException {
      if (xa.isHeuristicallyRollbacked()) {
        logger.warn("Attempted to rollback heuristically rollbacked transaction: xa-code=" +
                    xa.getHeuristicCode() + ", reason-string='" + xa.getRollbackCause() + "'");
        throw newXAException(xa.getHeuristicCode(), xa.getRollbackCause());
      } else if (!xa.isRollbacked()) {
        xa.rollback(xid);
      }
    }


    public boolean setTransactionTimeout(int seconds) throws XAException {
      if (seconds < 0)
        throw new XAException(XAException.XAER_INVAL);

      factory.acquireMutex(0, XAException.class);
      try {
        if (logger.isDebugEnabled()) logger.debug("Performing setTransactionTimeout");
        session.setTransactionTimeout(seconds * 1000L);
        return true;
      } finally {
        factory.releaseMutex();
      }
    }


    public void start(Xid xid, int flags) throws XAException {
      factory.acquireMutex(0, XAException.class);
      try {
        xid = convertXid(xid);
        if (logger.isDebugEnabled()) logger.debug("Performing start(" + formatFlags(flags) + "): " + parseXid(xid));
        switch (flags) {
          case TMNOFLAGS:
            if (xa2xid.containsN(xid)) {
              throw new XAException(XAException.XAER_DUPID);
            } else if (factory.hasAssociatedTransaction()) {
              throw new XAException(XAException.XA_RBDEADLOCK);
            } else {
              // FIXME: Need to consider read-only transactions here.
              try {
                MulgaraExternalTransaction xa = factory.createTransaction(xid, writing);
                xa2xid.put(xa, xid);
              } catch (MulgaraTransactionException em) {
                logger.error("Failed to create transaction", em);
                throw new XAException(XAException.XAER_RMFAIL);
              }
            }
            break;
          case TMJOIN:
            if (!factory.hasAssociatedTransaction()) {
              throw new XAException(XAException.XAER_NOTA);
            } else if (!factory.getAssociatedTransaction().getXid().equals(xid)) {
              throw new XAException(XAException.XAER_OUTSIDE);
            }
            break;
          case TMRESUME:
            MulgaraExternalTransaction xa = xa2xid.get1(xid);
            if (xa == null) {
              throw new XAException(XAException.XAER_NOTA);
            } else if (xa.isRollbacked()) {
              throw new XAException(XAException.XA_RBROLLBACK);
            } else {
              if (!factory.associateTransaction(xa)) {
                // session already associated with a transaction.
                throw new XAException(XAException.XAER_PROTO);
              }
            }
            break;
        }
      } finally {
        factory.releaseMutex();
      }

    }

    public Serializable getRMId() {
      return uniqueId;
    }

    /**
     * Required only because Java has trouble with accessing fields from
     * inner-classes.
     */
    private DatabaseSession getSession() { return session; }
  }

  private static XAException newXAException(int errorCode, String reason) {
    XAException xae = new XAException(reason);
    xae.errorCode = errorCode;
    return xae;
  }

  public static String parseXid(Xid xid) {
    return xid.toString();
  }

  private static InternalXid convertXid(Xid xid) {
    return new InternalXid(xid);
  }

  /**
   * Provides an Xid that compares equal by value.
   */
  private static class InternalXid implements Xid {
    private byte[] bq;
    private int fi;
    private byte[] gtid;

    public InternalXid(Xid xid) {
      byte[] tbq = xid.getBranchQualifier();
      byte[] tgtid = xid.getGlobalTransactionId();
      this.bq = new byte[tbq.length];
      this.fi = xid.getFormatId();
      this.gtid = new byte[tgtid.length];
      System.arraycopy(tbq, 0, this.bq, 0, tbq.length);
      System.arraycopy(tgtid, 0, this.gtid, 0, tgtid.length);
    }

    public byte[] getBranchQualifier() {
      return bq;
    }

    public int getFormatId() {
      return fi;
    }

    public byte[] getGlobalTransactionId() {
      return gtid;
    }

    public int hashCode() {
      return Arrays.hashCode(bq) ^ fi ^ Arrays.hashCode(gtid);
    }

    public boolean equals(Object rhs) {
      if (!(rhs instanceof InternalXid)) {
        return false;
      } else {
        InternalXid rhx = (InternalXid)rhs;
        return this.fi == rhx.fi &&
            Arrays.equals(this.bq, rhx.bq) &&
            Arrays.equals(this.gtid, rhx.gtid);
      }
    }

    public String toString() {
      return ":" + fi + ":" + Arrays.hashCode(gtid) + ":" + Arrays.hashCode(bq) + ":";
    }
  }

  /**
   * Format bitmasks defined by {@link XAResource}.
   *
   * @param flags  a bitmask composed from the constants defined in
   *   {@link XAResource}
   * @return a formatted representation of the <var>flags</var>
   */
  private static String formatFlags(int flags)
  {
    // Short-circuit evaluation if we've been explicitly passed no flags
    if (flags == XAResource.TMNOFLAGS) {
      return "TMNOFLAGS";
    }

    StringBuffer buffer = new StringBuffer();

    // Add any flags that are present
    for (Map.Entry<Integer, String> entry : flagMap.entrySet()) {
      int flag = entry.getKey();

      // If this flag is present, add it to the formatted output and remove
      // from the bitmask
      if ((flag & flags) == flag) {
        if (buffer.length() > 0) {
          buffer.append("|");
        }
        buffer.append(entry.getValue());
        flags &= ~flag;
      }
    }

    // We would expect to have removed all flags by this point
    // If there's some unknown flag we've missed, format it as hexadecimal
    if (flags != 0) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }
      buffer.append("0x").append(Integer.toHexString(flags));
    }

    return buffer.toString();
  }
}
