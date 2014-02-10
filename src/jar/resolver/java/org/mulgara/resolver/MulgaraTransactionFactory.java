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
 * Derivation from MulgaraTransactionManager Copyright (c) 2007 Topaz
 * Foundation under contract by Andrae Muys (mailto:andrae@netymon.com).
 */

package org.mulgara.resolver;

// Java2 packages
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.MulgaraTransactionException;

/**
 * Manages transactions within Mulgara.
 *
 * see http://mulgara.org/confluence/display/dev/Transaction+Architecture
 *
 * Maintains association between Answer's and TransactionContext's.
 * Manages tracking the ownership of the write-lock.
 * Maintains the write-queue and any timeout algorithm desired.
 * Provides new/existing TransactionContext's to DatabaseSession on request.
 *    Note: Returns new context unless Session is currently in a User Demarcated Transaction.
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

public abstract class MulgaraTransactionFactory {
  private static final Logger logger = Logger.getLogger(MulgaraTransactionFactory.class.getName());
  private static final Timer reaperTimer = new Timer("Write-lock Reaper", true);

  protected final MulgaraTransactionManager manager;

  protected final DatabaseSession session;

  private final Map<MulgaraTransaction, XAReaper> timeoutTasks;

  /**
   * Contains a reference the the current writing transaction IFF it is managed
   * by this factory.  If there is no current writing transaction, or if the
   * writing transaction is managed by a different factory then it is null.
   *
   * Modifications of this must be holding the mutexLock.
   */
  protected MulgaraTransaction writeTransaction;

  private Thread mutexHolder;
  private int lockCnt;

  protected MulgaraTransactionFactory(DatabaseSession session, MulgaraTransactionManager manager) {
    this.session = session;
    this.manager = manager;
    this.timeoutTasks = new HashMap<MulgaraTransaction, XAReaper>();
    this.mutexHolder = null;
    this.lockCnt = 0;
    this.writeTransaction = null;
  }

  protected void transactionCreated(MulgaraTransaction transaction) {
    long idleTimeout = session.getIdleTimeout() > 0 ? session.getIdleTimeout() : 15*60*1000L;
    long txnTimeout = session.getTransactionTimeout() > 0 ? session.getTransactionTimeout() : 60*60*1000L;
    long now = System.currentTimeMillis();

    synchronized (getMutexLock()) {
      timeoutTasks.put(transaction, new XAReaper(transaction, now + txnTimeout, idleTimeout, now));
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Timeouts set for transaction " + System.identityHashCode(transaction) +
                   ": idleTimeout=" + idleTimeout + ", txnTimeout=" + txnTimeout);
    }
  }

  protected void transactionComplete(MulgaraTransaction transaction) throws MulgaraTransactionException {
    synchronized (getMutexLock()) {
      XAReaper reaper = timeoutTasks.remove(transaction);
      if (reaper != null) {
        reaper.cancel();
        reaperTimer.purge();
      }
    }
  }

  /**
   * Obtain a transaction context associated with a DatabaseSession.
   *
   * Either returns the existing context if:
   * a) we are currently within a recursive call while under implicit XA control
   * or
   * b) we are currently within an active user demarcated XA.
   * otherwise creates a new transaction context and associates it with the
   * session.
   */
  public abstract MulgaraTransaction getTransaction(boolean write)
      throws MulgaraTransactionException;

  protected abstract Set<? extends MulgaraTransaction> getTransactions();

  /**
   * Rollback, or abort all transactions associated with a DatabaseSession.
   *
   * Will only abort the transaction if the rollback attempt fails.
   */
  public void closingSession() throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      logger.debug("Cleaning up any stale transactions on session close");

      Map<MulgaraTransaction, Throwable> requiresAbort = new HashMap<MulgaraTransaction, Throwable>();
      try {
        Throwable error = null;

        if (manager.isHoldingWriteLock(session)) {
          logger.debug("Session holds write-lock");
          try {
            if (writeTransaction != null) {
              try {
                logger.warn("Terminating session while holding writelock:" + session + ": " + writeTransaction);
                writeTransaction.execute(new TransactionOperation() {
                    public void execute() throws MulgaraTransactionException {
                      writeTransaction.heuristicRollback("Session closed while holding write lock");
                    }
                });
              } catch (Throwable th) {
                if (writeTransaction != null) {
                  requiresAbort.put(writeTransaction, th);
                  error = th;
                }
              } finally {
                writeTransaction = null;
              }
            }
          } finally {
            if (manager.isHoldingWriteLock(session))    // normally this will have been released
              manager.releaseWriteLock(session);
          }
        } else {
          logger.debug("Session does not hold write-lock");
        }

        for (MulgaraTransaction transaction : new HashSet<MulgaraTransaction>(getTransactions())) {
          try {
            // This is final so we can create the closure.
            final MulgaraTransaction xa = transaction;
            transaction.execute(new TransactionOperation() {
                public void execute() throws MulgaraTransactionException {
                  xa.heuristicRollback("Rollback due to session close");
                }
            });
          } catch (Throwable th) {
            requiresAbort.put(transaction, th);
            if (error == null) {
              error = th;
            }
          }
        }

        if (error != null) {
          throw new MulgaraTransactionException("Heuristic rollback failed on session close", error);
        }
      } finally {
        try {
          abortTransactions(requiresAbort);
        } catch (Throwable th) {
          try {
            logger.error("Error aborting transactions after heuristic rollback failure on session close", th);
          } catch (Throwable throw_away) { }
        }

        synchronized (getMutexLock()) {
          for (XAReaper reaper : timeoutTasks.values()) {
            reaper.cancel();
          }
          reaperTimer.purge();
          timeoutTasks.clear();
        }
      }
    } finally {
      releaseMutex();
    }
  }


  /**
   * Abort as many of the transactions as we can.
   */
  protected void abortTransactions(Map<MulgaraTransaction, Throwable> requiresAbort) {
    try {
      if (!requiresAbort.isEmpty()) {
        // At this point the originating exception has been thrown in the caller
        // so we attempt to ensure it doesn't get superseeded by anything that
        // might be thrown here while logging any errors.
        try {
          logger.error("Heuristic Rollback Failed on session close- aborting");
        } catch (Throwable throw_away) { } // Logging difficulties.

        try {
          for (MulgaraTransaction transaction : requiresAbort.keySet()) {
            try {
              transaction.abortTransaction("Heuristic Rollback failed on session close",
                  requiresAbort.get(transaction));
            } catch (Throwable th) {
              try {
                logger.error("Error aborting transaction after heuristic rollback failure on session close", th);
              } catch (Throwable throw_away) { }
            }
          }
        } catch (Throwable th) {
          try {
            logger.error("Loop error while aborting transactions after heuristic rollback failure on session close", th);
          } catch (Throwable throw_away) { }
        }
      }
    } catch (Throwable th) {
      try {
        logger.error("Unidentified error while aborting transactions after heuristic rollback failure on session close", th);
      } catch (Throwable throw_away) { }
    }
  }

  /**
   * Acquire the mutex. The mutex is re-entrant, but {@link #releaseMutex} must be called as many
   * times as this is called.
   *
   * <p>We use our own implementation here (as opposed to, say, java.util.concurrent.Lock) so we
   * can reliably get the current mutex-owner, and we use a lock around the mutex acquisition and
   * release to do atomic tests and settting of additional variables associated with the mutex.
   * 
   * @param timeout how many milliseconds to wait for the mutex, or 0 to wait indefinitely
   * @param exc An exception class that is the type that will be thrown in case of failure.
   * @throws T if the mutex could not be acquired, either due to a timeout or due to an interrupt
   */
  public final <T extends Throwable> void acquireMutex(long timeout, Class<T> exc) throws T {
    synchronized (getMutexLock()) {
      if (mutexHolder == Thread.currentThread()) {
        lockCnt++;
        return;
      }

      long deadline = System.currentTimeMillis() + timeout;

      while (mutexHolder != null) {
        long wait = deadline - System.currentTimeMillis();
        if (timeout == 0) {
          wait = 0;
        } else if (wait <= 0) {
          throw newException(exc, "Timed out waiting to acquire lock");
        }

        try {
          getMutexLock().wait(wait);
        } catch (InterruptedException ie) {
          throw newExceptionOrCause(exc, "Interrupted while waiting to acquire lock", ie);
        }
      }

      mutexHolder = Thread.currentThread();
      lockCnt = 1;
    }
  }

  /** 
   * Acquire the mutex, interrupting the existing holder if there is one. 
   * 
   * @param timeout how many milliseconds to wait for the mutex, or 0 to wait indefinitely
   * @param exc An exception class that is the type that will be thrown in case of failure.
   * @throws T if the mutex could not be acquired, either due to a timeout or due to an interrupt
   * @see #acquireMutex
   */
  public final <T extends Throwable> void acquireMutexWithInterrupt(long timeout, Class<T> exc) throws T {
    synchronized (getMutexLock()) {
      if (mutexHolder != null && mutexHolder != Thread.currentThread()) {
        mutexHolder.interrupt();
      }

      acquireMutex(timeout, exc);
    }
  }

  /** 
   * Release the mutex. 
   *
   * @throws IllegalStateException is the mutex is not held by the current thread
   */
  public final void releaseMutex() {
    synchronized (getMutexLock()) {
      if (Thread.currentThread() != mutexHolder) {
        throw new IllegalStateException("Attempt to release mutex without holding mutex");
      }

      assert lockCnt > 0;
      if (--lockCnt <= 0) {
        mutexHolder = null;
        getMutexLock().notify();
      }
    }
  }

  /**
   * @return the lock used to protect access to and to implement the mutex; must be held as shortly
   *         as possible (no blocking operations)
   */
  public final Object getMutexLock() {
    return session;
  }

  /**
   * @return the current holder of the mutex, or null if none. Must hold the mutex-lock while
   *         calling this.
   */
  public final Thread getMutexHolder() {
    return mutexHolder;
  }

  public static <T extends Throwable> T newException(Class<T> exc, String msg) {
    try {
      return exc.getConstructor(String.class).newInstance(msg);
    } catch (Exception e) {
      throw new Error("Internal error creating " + exc, e);
    }
  }

  public static <T extends Throwable> T newExceptionOrCause(Class<T> exc, String msg, Throwable cause) {
    if (exc.isAssignableFrom(cause.getClass()))
      return exc.cast(cause);
    return exc.cast(newException(exc, msg).initCause(cause));
  }

  private class XAReaper extends TimerTask {
    private final MulgaraTransaction transaction;
    private final long txnDeadline;
    private final long idleTimeout;

    public XAReaper(MulgaraTransaction transaction, long txnDeadline, long idleTimeout, long lastActive) {
      this.transaction = transaction;
      this.txnDeadline = txnDeadline;
      this.idleTimeout = idleTimeout;

      if (lastActive <= 0) lastActive = System.currentTimeMillis();
      long nextWakeup = Math.min(txnDeadline, lastActive + idleTimeout);

      if (logger.isDebugEnabled()) {
        logger.debug("Transaction-reaper created, txn=" + transaction + ", txnDeadline=" + txnDeadline +
                     ", idleTimeout=" + idleTimeout + ", nextWakeup=" + nextWakeup + ": " +
                     System.identityHashCode(getMutexLock()));
      }

      reaperTimer.schedule(this, new Date(nextWakeup));
    }

    public void run() {
      logger.debug("Transaction-reaper running, txn=" + transaction + ": " + System.identityHashCode(getMutexLock()));

      long lastActive = transaction.lastActive();
      long now = System.currentTimeMillis();

      synchronized (getMutexLock()) {
        if (timeoutTasks.remove(transaction) == null) return;  // looks like we got cleaned up

        if (now < txnDeadline && ((lastActive <= 0) || (now < lastActive + idleTimeout))) {
          if (logger.isDebugEnabled()) {
            logger.debug("Transaction still active: " + lastActive + " time: " + now + " idle-timeout: " + idleTimeout + " - rescheduling timer");
          }

          timeoutTasks.put(transaction, new XAReaper(transaction, txnDeadline, idleTimeout, lastActive));
          return;
        }
      }

      final String txnType = (now >= txnDeadline) ? "over-extended" : "inactive";
      final String toType = (now >= txnDeadline) ? "transaction" : "idle";

      logger.warn("Rolling back " + txnType + " transaction");
      new Thread(toType + "-timeout executor") {
        public void run() {
          try {
            transaction.heuristicRollback(toType + "-timeout");
          } catch (MulgaraTransactionException em) {
            logger.warn("Exception thrown while rolling back " + txnType + " transaction");
          }
        }
      }.start();
    }
  }
}
