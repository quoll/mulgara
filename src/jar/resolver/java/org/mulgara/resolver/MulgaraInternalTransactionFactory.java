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
 * (http://www.netymon.com, mailto:mail@netymon.com). Portions created
 * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
 * All Rights Reserved.
 *
 * Work deriving from MulgaraTransactionManager Copyright (c) 2007 Topaz
 * Foundation under contract by Andrae Muys (mailto:andrae@netymon.com).
 */

package org.mulgara.resolver;

// Java2 packages
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.transaction.TransactionManagerFactory;
import org.mulgara.util.StackTrace;

/**
 * Implements the internal transaction controls offered by Session.
 *
 * @created 2006-10-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class MulgaraInternalTransactionFactory extends MulgaraTransactionFactory {
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(MulgaraInternalTransactionFactory.class.getName());

  /** Flag indicating current explicit-transaction has been rolledback. */
  private boolean isFailed;

  /** The reason for the failure if {@link #isFailed} is true. */
  private Throwable failureCause;

  /** Map of threads to active transactions. */
  private final Map<Thread, MulgaraTransaction> activeTransactions;

  /** Are we in auto-commit mode. */
  public boolean autoCommit;

  /** All uncompleted transactions (may be more than 1 because of unclosed answers) */
  public final Set<MulgaraTransaction> transactions;

  /** Currently associated explicit transaction */
  public MulgaraInternalTransaction explicitXA;

  private final TransactionManager transactionManager;

  public MulgaraInternalTransactionFactory(DatabaseSession session, MulgaraTransactionManager manager,
                                           TransactionManagerFactory transactionManagerFactory) {
    super(session, manager);

    this.isFailed = false;
    this.failureCause = null;
    this.activeTransactions = new HashMap<Thread, MulgaraTransaction>();
    this.autoCommit = true;
    this.transactions = new HashSet<MulgaraTransaction>();
    this.explicitXA = null;

    this.transactionManager = transactionManagerFactory.newTransactionManager();
    try {
      /* "disable" the TM's timeout because we implement timeouts ourselves; we also don't set
       * it to our timeout because that can interfere with txn cleanup if the TM's timeout has
       * fired by causing rollback or commit to fail.
       */
      this.transactionManager.setTransactionTimeout(Integer.MAX_VALUE);
    } catch (SystemException es) {
      logger.warn("Unable to disable transaction timeout on jta tm", es);
    }
  }

  public MulgaraTransaction getTransaction(boolean write) throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (explicitXA != null) {
        return explicitXA;
      }

      try {
        MulgaraInternalTransaction transaction;
        if (write) {
          manager.obtainWriteLock(session);
          try {
            assert writeTransaction == null;
            writeTransaction = transaction = 
              new MulgaraInternalTransaction(this, session.newOperationContext(true));
          } catch (Throwable th) {
            manager.releaseWriteLock(session);
            throw new MulgaraTransactionException("Error creating write transaction", th);
          }
        } else {
          transaction = new MulgaraInternalTransaction(this, session.newOperationContext(false));
        }

        transactions.add(transaction);
        transactionCreated(transaction);

        return transaction;
      } catch (MulgaraTransactionException em) {
        throw em;
      } catch (Exception e) {
        throw new MulgaraTransactionException("Error creating transaction", e);
      }
    } finally {
      releaseMutex();
    }
  }

  public Set<MulgaraTransaction> getTransactions() {
    return transactions;
  }


  public void commit() throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (isFailed) {
        if (failureCause != null) throw new MulgaraTransactionException("Attempting to commit failed session", failureCause);
        else throw new MulgaraTransactionException("Attempting to commit failed session");
      } else if (!manager.isHoldingWriteLock(session)) {
        throw new MulgaraTransactionException(
            "Attempting to commit while not the current writing transaction");
      }

      manager.reserveWriteLock(session);
      try {
        setAutoCommit(true);
        setAutoCommit(false);
      } finally {
        manager.releaseReserve(session);
      }
    } finally {
      releaseMutex();
    }
  }


  /**
   * This is an explicit, user-specified rollback.
   * 
   * This needs to be distinguished from an implicit rollback triggered by failure.
   */
  public void rollback() throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (manager.isHoldingWriteLock(session)) {
        manager.reserveWriteLock(session);
        try {
          try {
            writeTransaction.execute(new TransactionOperation() {
              public void execute() throws MulgaraTransactionException {
                writeTransaction.dereference();
                ((MulgaraInternalTransaction)writeTransaction).explicitRollback();
              }
            });
            // FIXME: Should be checking status here, not writelock.
            if (manager.isHoldingWriteLock(session)) {
              // transaction referenced by something - need to explicitly end it.
              writeTransaction.abortTransaction("Rollback failed",
                  new MulgaraTransactionException("Rollback failed to terminate write transaction"));
            }
          } finally {
            explicitXA = null;
            setAutoCommit(false);
          }
        } finally {
          manager.releaseReserve(session);
        }
      } else if (isFailed) {
        explicitXA = null;
        isFailed = false;
        failureCause = null;
        setAutoCommit(false);
      } else {
        throw new MulgaraTransactionException(
            "Attempt to rollback while not in the current writing transaction");
      }
    } finally {
      releaseMutex();
    }
  }

  public void setAutoCommit(boolean autoCommit) throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (manager.isHoldingWriteLock(session) && isFailed) {
        writeTransaction.abortTransaction("Session failed and still holding writeLock",
                              new MulgaraTransactionException("Failed Session in setAutoCommit"));
      }

      if (manager.isHoldingWriteLock(session) || isFailed) {
        if (autoCommit) {
          this.autoCommit = true;
          this.explicitXA = null;

          // AutoCommit off -> on === branch on current state of transaction.
          if (manager.isHoldingWriteLock(session)) {
            // Within active transaction - commit and finalise.
            try {
              writeTransaction.execute(new TransactionOperation() {
                public void execute() throws MulgaraTransactionException {
                  writeTransaction.dereference();
                  ((MulgaraInternalTransaction)writeTransaction).commitTransaction();
                }
              });
            } finally {
              // This should have been cleaned up by the commit above, but if it
              // hasn't then if we don't release here we could deadlock the
              // transaction manager
              if (manager.isHoldingWriteLock(session)) {
                manager.releaseWriteLock(session);
              }
            }
          } else if (isFailed) {
            // Within failed transaction - cleanup.
            isFailed = false;
            failureCause = null;
          }
        } else {
          if (!manager.isHoldingWriteLock(session)) {
            throw new MulgaraTransactionException("Attempting set auto-commit false in failed session");
          } else {
            // AutoCommit off -> off === no-op. Log info.
            if (logger.isDebugEnabled()) {
              logger.debug("Attempt to set autocommit false twice\n" + new StackTrace());
            }
          }
        }
      } else {
        explicitXA = null;
        if (autoCommit) {
          // AutoCommit on -> on === no-op.  Log info.
          if (logger.isDebugEnabled()) logger.debug("Attempting to set autocommit true without setting it false");
        } else {
          // AutoCommit on -> off == Start new transaction.
          getTransaction(true); // Set's writeTransaction.
          writeTransaction.reference();
          explicitXA = (MulgaraInternalTransaction) writeTransaction;
          this.autoCommit = false;
        }
      }
    } finally {
      releaseMutex();
    }
  }

  //
  // Transaction livecycle callbacks.
  //

  public Transaction transactionStart(MulgaraTransaction transaction) throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      try {
        if (logger.isDebugEnabled()) logger.debug("Beginning Transaction");
        if (activeTransactions.get(Thread.currentThread()) != null) {
          throw new MulgaraTransactionException(
              "Attempt to start transaction in thread with exiting active transaction.");
        } else if (activeTransactions.containsValue(transaction)) {
          throw new MulgaraTransactionException("Attempt to start transaction twice");
        }

        transactionManager.begin();
        Transaction jtaTrans = transactionManager.getTransaction();

        activeTransactions.put(Thread.currentThread(), transaction);

        return jtaTrans;
      } catch (Exception e) {
        throw new MulgaraTransactionException("Transaction Begin Failed", e);
      }
    } finally {
      releaseMutex();
    }
  }

  public void transactionResumed(MulgaraTransaction transaction, Transaction jtaXA) 
      throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (activeTransactions.get(Thread.currentThread()) != null) {
        throw new MulgaraTransactionException(
            "Attempt to resume transaction in already activated thread");
      } else if (activeTransactions.containsValue(transaction)) {
        throw new MulgaraTransactionException("Attempt to resume active transaction");
      }

      try {
        transactionManager.resume(jtaXA);
        activeTransactions.put(Thread.currentThread(), transaction);
      } catch (Exception e) {
        throw new MulgaraTransactionException("Resume Failed", e);
      }
    } finally {
      releaseMutex();
    }
  }

  public Transaction transactionSuspended(MulgaraTransaction transaction)
      throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      try {
        if (transaction != activeTransactions.get(Thread.currentThread())) {
          throw new MulgaraTransactionException(
              "Attempt to suspend transaction from outside thread");
        }

        if (autoCommit && transaction == writeTransaction) {
          logger.error("Attempt to suspend write transaction without setting AutoCommit Off");
          throw new MulgaraTransactionException(
              "Attempt to suspend write transaction without setting AutoCommit Off");
        }

        Transaction xa = transactionManager.suspend();
        activeTransactions.remove(Thread.currentThread());

        return xa;
      } catch (Throwable th) {
        logger.error("Attempt to suspend failed", th);
        try {
          transactionManager.setRollbackOnly();
        } catch (Throwable t) {
          logger.error("Attempt to setRollbackOnly() failed", t);
        }
        throw new MulgaraTransactionException("Suspend failed", th);
      }
    } finally {
      releaseMutex();
    }
  }

  public void closingSession() throws MulgaraTransactionException {
    acquireMutexWithInterrupt(0, MulgaraTransactionException.class);
    try {
      try {
        super.closingSession();
      } finally {
        transactions.clear();
      }
    } finally {
      releaseMutex();
    }
  }

  public void transactionComplete(MulgaraTransaction transaction) throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      super.transactionComplete(transaction);

      logger.debug("Transaction Complete");

      if (transaction == writeTransaction) {
        if (manager.isHoldingWriteLock(session)) {
          manager.releaseWriteLock(session);
          writeTransaction = null;
        }
      }

      transactions.remove(transaction);
      activeTransactions.remove(Thread.currentThread());
    } finally {
      releaseMutex();
    }
  }

  public void transactionAborted(MulgaraTransaction transaction, Throwable cause) {
    acquireMutex(0, RuntimeException.class);
    try {
      try {
        // Make sure this cleans up the transaction metadata - this transaction is DEAD!
        if (!autoCommit && transaction == writeTransaction) {
          isFailed = true;
          failureCause = cause;
        }
        transactionComplete(transaction);
      } catch (Throwable th) {
        // FIXME: This should probably abort the entire server after logging the error!
        logger.error("Error managing transaction abort", th);
      }
    } finally {
      releaseMutex();
    }
  }
}
