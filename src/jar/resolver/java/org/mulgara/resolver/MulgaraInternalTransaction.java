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
 * Work deriving from MulgaraTransaction Copyright (c) 2007 Topaz Foundation
 * under contract by Andrae Muys (mailto:andrae@netymon.com).
 */
package org.mulgara.resolver;

// Java 2 enterprise packages
import java.util.HashSet;
import java.util.Set;
import javax.transaction.Transaction;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.EnlistableResource;
import org.mulgara.util.StackTrace;

import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.query.TuplesException;

/**
 * Responsible for the javax.transaction.Transaction object.
 * Responsibilities
 * Ensuring every begin or resume is followed by either a suspend or an end.
 * Ensuring every suspend or end is preceeded by either a begin or a resume.
 * In conjunction with TransactionalAnswer ensuring that
 * all calls to operations on SubqueryAnswer are preceeded by a successful resume.
 * all calls to operations on SubqueryAnswer conclude with a suspend as the last call prior to returning to the user.
 * Collaborates with DatabaseTransactionManager to determine when to end the transaction.
 *
 * @created 2006-10-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
 *
 * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *
 * @licence Open Software License v3.0
 */
public class MulgaraInternalTransaction implements MulgaraTransaction {
  /**
   * This is the state machine switch matching these states.
      switch (state) {
        case CONSTRUCTEDREF:
        case CONSTRUCTEDUNREF:
        case ACTUNREF:
        case ACTREF:
        case DEACTREF:
        case FINISHED:
        case FAILED:
      }
   */
  private enum State { CONSTRUCTEDREF, CONSTRUCTEDUNREF, ACTUNREF, ACTREF, DEACTREF, FINISHED, FAILED };

  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(MulgaraInternalTransaction.class.getName());

  private MulgaraInternalTransactionFactory factory;
  private DatabaseOperationContext context;
  private Set<EnlistableResource> enlisted;

  private Transaction transaction;
  private Thread currentThread;

  private volatile long deactivateTime;
  private boolean inXACompletion;

  private State state;
  private int inuse;
  private int using;

  private Throwable rollbackCause;

  public MulgaraInternalTransaction(MulgaraInternalTransactionFactory factory, DatabaseOperationContext context)
      throws IllegalArgumentException {
    debugReport("Creating Transaction");

    try {
      if (factory == null) {
        throw new IllegalArgumentException("Manager null in MulgaraTransaction");
      } else if (context == null) {
        throw new IllegalArgumentException("OperationContext null in MulgaraTransaction");
      }
      this.factory = factory;
      this.context = context;
      this.enlisted = new HashSet<EnlistableResource>();
      this.currentThread = null;

      inuse = 0;
      using = 0;
      state = State.CONSTRUCTEDUNREF;
      rollbackCause = null;
      deactivateTime = 0;
    } finally {
      debugReport("Finished Creating Transaction");
    }
  }


  private void activate() throws MulgaraTransactionException {
    debugReport("Activating Transaction");

    try {
      if (currentThread != null && !currentThread.equals(Thread.currentThread())) {
        throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
      }

      deactivateTime = -1;
      switch (state) {
        case CONSTRUCTEDUNREF:
          startTransaction();
          inuse = 1;
          state = State.ACTUNREF;
          try {
            context.initiate(this);
          } catch (Throwable th) {
            throw implicitRollback(th);
          }
          break;
        case CONSTRUCTEDREF:
          startTransaction();
          inuse = 1;
          using = 1;
          state = State.ACTREF;
          try {
            context.initiate(this);
          } catch (Throwable th) {
            throw implicitRollback(th);
          }
          break;
        case DEACTREF:
          resumeTransaction();
          inuse = 1;
          state = State.ACTREF;
          break;
        case ACTREF:
        case ACTUNREF:
          inuse++;
          break;
        case FINISHED:
          throw new MulgaraTransactionException("Attempt to activate terminated transaction");
        case FAILED:
          throw new MulgaraTransactionException("Attempt to activate failed transaction", rollbackCause);
      }

      try {
        checkActivated();
      } catch (MulgaraTransactionException em) {
        throw abortTransaction("Activate failed post-condition check", em);
      }
    } catch (MulgaraTransactionException em) {
      throw em;
    } catch (Throwable th) {
      throw abortTransaction("Error activating transaction", th);
    } finally {
//      debugReport("Leaving Activate transaction");
    }
  }


  private void deactivate() throws MulgaraTransactionException {
    debugReport("Deactivating transaction");

    try {
      if (currentThread == null) {
        throw new MulgaraTransactionException("Transaction not associated with thread");
      } else if (!currentThread.equals(Thread.currentThread())) {
        throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
      }

      deactivateTime = System.currentTimeMillis();
      switch (state) {
        case ACTUNREF:
          if (inuse == 1) {
            commitTransaction();
          }
          inuse--;
          break;
        case ACTREF:
          if (inuse == 1) {
            suspendTransaction();
          }
          inuse--;
          break;
        case CONSTRUCTEDREF:
          throw new MulgaraTransactionException("Attempt to deactivate uninitiated refed transaction");
        case CONSTRUCTEDUNREF:
          throw new MulgaraTransactionException("Attempt to deactivate uninitiated transaction");
        case DEACTREF:
          throw new IllegalStateException("Attempt to deactivate unactivated transaction");
        case FINISHED:
          if (inuse < 0) {
            errorReport("Activation count failure - too many deacts - in finished transaction", null);
          } else {
            inuse--;
          }
          break;
        case FAILED:
          // Nothing to do here.
          break;
      }
    } catch (MulgaraTransactionException em) {
      throw em;
    } catch (Throwable th) {
      throw abortTransaction("Error deactivating transaction", th);
    } finally {
//      debugReport("Leaving Deactivate Transaction");
    }
  }

  // Note: The transaction is often not activated when these are called.
  //       This occurs when setting autocommit, as this creates and
  //       references a transaction object that won't be started/activated
  //       until it is first used.
  public void reference() throws MulgaraTransactionException {
    debugReport("Referencing Transaction");

    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        if (currentThread != null && !currentThread.equals(Thread.currentThread())) {
          throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
        }

        switch (state) {
          case CONSTRUCTEDUNREF:
            state = State.CONSTRUCTEDREF;
            break;
          case ACTREF:
          case ACTUNREF:
            using++;
            state = State.ACTREF;
            break;
          case DEACTREF:
            using++;
            break;
          case CONSTRUCTEDREF:
            throw new MulgaraTransactionException("Attempt to reference uninitated transaction twice");
          case FINISHED:
            throw new MulgaraTransactionException("Attempt to reference terminated transaction");
          case FAILED:
            throw new MulgaraTransactionException("Attempt to reference failed transaction", rollbackCause);
        }
      } catch (MulgaraTransactionException em) {
        throw em;
      } catch (Throwable th) {
        report("Error referencing transaction");
        throw implicitRollback(th);
      } finally {
        debugReport("Leaving Reference Transaction");
      }
    } finally {
      releaseMutex();
    }
  }

  public void dereference() throws MulgaraTransactionException {
    debugReport("Dereferencing Transaction");

    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        if (currentThread != null && !currentThread.equals(Thread.currentThread())) {
          throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
        }

        switch (state) {
          case ACTREF:
            if (using == 1) {
              state = State.ACTUNREF;
            }
            using--;
            break;
          case CONSTRUCTEDREF:
            state = State.CONSTRUCTEDUNREF;
            break;
          case FINISHED:
          case FAILED:
            if (using < 1) {
              errorReport("Reference count failure - too many derefs - in finished transaction", null);
            } else {
              using--;
            }
            break;
          case ACTUNREF:
            throw new IllegalStateException("Attempt to dereference unreferenced transaction");
          case CONSTRUCTEDUNREF:
            throw new MulgaraTransactionException("Attempt to dereference uninitated transaction");
          case DEACTREF:
            throw new IllegalStateException("Attempt to dereference deactivated transaction");
        }
      } catch (MulgaraTransactionException em) {
        throw em;
      } catch (Throwable th) {
        throw implicitRollback(th);
      } finally {
        debugReport("Dereferenced Transaction");
      }
    } finally {
      releaseMutex();
    }
  }

  private void startTransaction() throws MulgaraTransactionException {
    debugReport("Initiating transaction");
    try {
      transaction = factory.transactionStart(this);
      currentThread = Thread.currentThread();
    } catch (Throwable th) {
      throw abortTransaction("Failed to start transaction", th);
    }
  }

  private void resumeTransaction() throws MulgaraTransactionException {
//    debugReport("Resuming transaction");
    try {
      factory.transactionResumed(this, transaction);
      currentThread = Thread.currentThread();
    } catch (Throwable th) {
      throw abortTransaction("Failed to resume transaction", th);
    }
  }

  private void suspendTransaction() throws MulgaraTransactionException {
//    debugReport("Suspending Transaction");
    try {
      if (using < 1) {
        throw implicitRollback(
            new MulgaraTransactionException("Attempt to suspend unreferenced transaction"));
      }
      transaction = factory.transactionSuspended(this);
      currentThread = null;
      state = State.DEACTREF;
    } catch (Throwable th) {
      throw implicitRollback(th);
    } finally {
//      debugReport("Finished suspending transaction");
    }
  }

  public void commitTransaction() throws MulgaraTransactionException {
    debugReport("Committing Transaction");
    acquireMutex(0, true, MulgaraTransactionException.class);
    try {
      try {
        transaction.commit();
      } catch (Throwable th) {
        throw implicitRollback(th);
      }
      try {
        try {
          transaction = null;
        } finally { try {
          state = State.FINISHED;
        } finally { try {
          context.clear();
        } finally { try {
          enlisted.clear();
        } finally { try {
          factory.transactionComplete(this);
        } finally {
          debugReport("Committed transaction");
        } } } } }
      } catch (Throwable th) {
        errorReport("Error cleaning up transaction post-commit", th);
        throw new MulgaraTransactionException("Error cleaning up transaction post-commit", th);
      }
    } finally {
      releaseMutex();
    }
  }

  public void heuristicRollback(String cause) throws MulgaraTransactionException {
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
      switch (state) {
        case DEACTREF:
          activate();
          try {
            implicitRollback(new MulgaraTransactionException(cause));
          } finally {
            currentThread = null;
          }
          break;

        case ACTUNREF:
        case ACTREF:
          implicitRollback(new MulgaraTransactionException(cause));
          break;

        case CONSTRUCTEDREF:
        case CONSTRUCTEDUNREF:
          // no point in starting and then rolling back immediately, so just clean up
          abortTransaction(cause, new Throwable());

        case FINISHED:
        case FAILED:
          // Nothing to do here.
          return;
      }
    } finally {
      releaseMutex();
    }
  }

  /**
   * Rollback the transaction.
   * We don't throw an exception here when transaction fails - this is expected,
   * after all we requested it.
   */
  public void explicitRollback() throws MulgaraTransactionException {
    acquireMutex(0, true, MulgaraTransactionException.class);
    try {
      if (currentThread == null) {
        throw new MulgaraTransactionException("Transaction failed activation check");
      } else if (!currentThread.equals(Thread.currentThread())) {
        throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
      }

      try {
        switch (state) {
          case ACTUNREF:
          case ACTREF:
            transaction.rollback();
            context.clear();
            enlisted.clear();
            factory.transactionComplete(this);
            transaction = null;
            state = State.FINISHED;
            break;
          case DEACTREF:
            throw new IllegalStateException("Attempt to rollback unactivated transaction");
          case CONSTRUCTEDREF:
            throw new MulgaraTransactionException("Attempt to rollback uninitiated ref'd transaction");
          case CONSTRUCTEDUNREF:
            throw new MulgaraTransactionException("Attempt to rollback uninitiated unref'd transaction");
          case FINISHED:
            throw new MulgaraTransactionException("Attempt to rollback finished transaction");
          case FAILED:
            throw new MulgaraTransactionException("Attempt to rollback failed transaction");
        }
      } catch (MulgaraTransactionException em) {
        throw em;
      } catch (Throwable th) {
        throw implicitRollback(th);
      }
    } finally {
      releaseMutex();
    }
  }

  /**
   * This will endevour to terminate the transaction via a rollback - if this
   * fails it will abort the transaction.
   * If the rollback succeeds then this method will return a suitable
   * MulgaraTransactionException to be thrown by the caller.
   * If the rollback fails then this method will throw the resulting exception
   * from abortTransaction().
   * Post-condition: The transaction is terminated and cleaned up.
   */
  @SuppressWarnings("finally")
  private MulgaraTransactionException implicitRollback(Throwable cause) throws MulgaraTransactionException {
    debugReport("Implicit Rollback triggered");

    synchronized (factory.getMutexLock()) {
      inXACompletion = true;
    }

    try {
      if (rollbackCause != null) {
        errorReport("Cascading error, transaction already rolled back", cause);
        errorReport("Cascade error, expected initial cause", rollbackCause);

        return new MulgaraTransactionException("Transaction already in rollback", cause);
      }

      switch (state) {
        case ACTUNREF:
        case ACTREF:
            rollbackCause = cause;
            transaction.rollback();
            transaction = null;
            context.clear();
            enlisted.clear();
            state = State.FAILED;
            factory.transactionAborted(this, rollbackCause);
            return new MulgaraTransactionException("Transaction rollback triggered", cause);
        case DEACTREF:
          throw new IllegalStateException("Attempt to rollback deactivated transaction");
        case CONSTRUCTEDREF:
          throw new MulgaraTransactionException("Attempt to rollback uninitiated ref'd transaction");
        case CONSTRUCTEDUNREF:
          throw new MulgaraTransactionException("Attempt to rollback uninitiated unref'd transaction");
        case FINISHED:
          throw new MulgaraTransactionException("Attempt to rollback finished transaction");
        case FAILED:
          throw new MulgaraTransactionException("Attempt to rollback failed transaction");
        default:
          throw new MulgaraTransactionException("Unknown state");
      }
    } catch (Throwable th) {
      try {
        errorReport("Attempt to rollback failed; initiating cause: ", cause);
      } finally {
        throw abortTransaction("Failed to rollback normally - see log for inititing cause", th);
      }
    } finally {
      debugReport("Leaving implicitRollback");
    }
  }

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

  /**
   * Forces the transaction to be abandoned, including bypassing JTA to directly
   * rollback/abort the underlying store-phases if required.
   * Heavilly nested try{}finally{} should guarentee that even JVM errors should
   * not prevent this function from cleaning up anything that can be cleaned up.
   * We have to delegate to the OperationContext the abort() on the resolvers as
   * only it has full knowledge of which resolvers are associated with this
   * transaction.
   */
  public MulgaraTransactionException abortTransaction(String errorMessage, Throwable cause)
      throws MulgaraTransactionException {
    // we should actually already have the mutex, but let's make sure
    acquireMutex(0L, true, MulgaraTransactionException.class);
    try {
      // We need to notify the factory here - this is serious, we
      // need to rollback this transaction, but if we have reached here
      // we have failed to obtain a valid transaction to rollback!
      try {
        if (rollbackCause == null) rollbackCause = cause;

        try {
          errorReport(errorMessage + " - Aborting", cause);
        } finally { try {
          if (transaction != null) {
            transaction.rollback();
          }
        } finally { try {
          factory.transactionAborted(this, cause);
        } finally { try {
          abortEnlistedResources();
        } finally { try {
          context.clear();
        } finally { try {
          enlisted.clear();
        } finally { try {
          transaction = null;
        } finally {
          state = State.FAILED;
        } } } } } } }
        return new MulgaraTransactionException(errorMessage + " - Aborting", cause);
      } catch (Throwable th) {
        throw new MulgaraTransactionException(errorMessage + " - Failed to abort cleanly", th);
      } finally {
        debugReport("Leaving abortTransaction");
      }
    } finally {
      releaseMutex();
    }
  }

  /**
   * Used to bypass JTA and explicitly abort resources behind the scenes.
   */
  private void abortEnlistedResources() {
    for (EnlistableResource e : enlisted) {
      try {
        e.abort();
      } catch (Throwable th) {
        try {
          errorReport("Error aborting enlistable resource", th);
        } catch (Throwable ignore) { /* Unable to log. Would normally log, so ignore. */ }
      }
    }
  }

  public void execute(Operation operation, DatabaseMetadata metadata) throws MulgaraTransactionException {
    debugReport("Executing Operation");

    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        activate();
        try {
          operation.execute(context,
                            context.getSystemResolver(),
                            metadata);
        } catch (Throwable th) {
          throw implicitRollback(th);
        } finally {
          deactivate();
        }
      } finally {
        debugReport("Executed Operation");
      }
    } finally {
      releaseMutex();
    }
  }

  public AnswerOperationResult execute(AnswerOperation ao) throws TuplesException {
    debugReport("Executing AnswerOperation");

    acquireMutex(0, false, TuplesException.class);
    try {
      try {
        activate();
        try {
          ao.execute();
          return ao.getResult();
        } catch (Throwable th) {
          throw implicitRollback(th);
        } finally {
          deactivate();
        }
      } catch (MulgaraTransactionException em) {
        throw new TuplesException("Transaction error", em);
      } finally {
        debugReport("Executed AnswerOperation");
      }
    } finally {
      releaseMutex();
    }
  }


  public void execute(TransactionOperation to) throws MulgaraTransactionException {
    debugReport("Executing TransactionOperation");

    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        activate();
        try {
          to.execute();
        } catch (Throwable th) {
          throw implicitRollback(th);
        } finally {
          deactivate();
        }
      } finally {
        debugReport("Executed TransactionOperation");
      }
    } finally {
      releaseMutex();
    }
  }

  public void enlist(EnlistableResource enlistable) throws MulgaraTransactionException {
    acquireMutex(0, false, MulgaraTransactionException.class);
    try {
      try {
        if (currentThread == null) {
          throw new MulgaraTransactionException("Transaction not associated with thread");
        } else if (!currentThread.equals(Thread.currentThread())) {
          throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
        }

        if (enlisted.contains(enlistable)) {
          return;
        }

        switch (state) {
          case ACTUNREF:
          case ACTREF:
            transaction.enlistResource(enlistable.getXAResource());
            enlisted.add(enlistable);
            break;
          case CONSTRUCTEDREF:
            throw new MulgaraTransactionException("Attempt to enlist resource in uninitated ref'd transaction");
          case CONSTRUCTEDUNREF:
            throw new MulgaraTransactionException("Attempt to enlist resource in uninitated unref'd transaction");
          case DEACTREF:
            throw new MulgaraTransactionException("Attempt to enlist resource in unactivated transaction");
          case FINISHED:
            throw new MulgaraTransactionException("Attempt to enlist resource in finished transaction");
          case FAILED:
            throw new MulgaraTransactionException("Attempt to enlist resource in failed transaction");
        }
      } catch (Throwable th) {
        throw implicitRollback(th);
      }
    } finally {
      releaseMutex();
    }
  }

  public long lastActive() {
    return deactivateTime;
  }

  //
  // Used internally
  //

  private void checkActivated() throws MulgaraTransactionException {
    if (currentThread == null) {
      throw new MulgaraTransactionException("Transaction not associated with thread");
    } else if (!currentThread.equals(Thread.currentThread())) {
      throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
    }

    switch (state) {
      case ACTUNREF:
      case ACTREF:
        if (inuse < 0 || using < 0) {
          throw new MulgaraTransactionException("Reference Failure, using: " + using + ", inuse: " + inuse);
        }
        return;
      case CONSTRUCTEDREF:
        throw new MulgaraTransactionException("Transaction (ref) uninitiated");
      case CONSTRUCTEDUNREF:
        throw new MulgaraTransactionException("Transaction (unref) uninitiated");
      case DEACTREF:
        throw new MulgaraTransactionException("Transaction deactivated");
      case FINISHED:
        throw new MulgaraTransactionException("Transaction is terminated");
      case FAILED:
        throw new MulgaraTransactionException("Transaction is failed", rollbackCause);
    }
  }

  private <T extends Throwable> void acquireMutex(long timeout, boolean isXACompletion, Class<T> exc) throws T {
    synchronized (factory.getMutexLock()) {
      factory.acquireMutex(timeout, exc);
      inXACompletion |= isXACompletion;
    }
  }

  private void releaseMutex() {
    factory.releaseMutex();
  }

  protected void finalize() throws Throwable {
    try {
      debugReport("GC-finalize");
      if (state != State.FINISHED && state != State.FAILED) {
        errorReport("Finalizing incomplete transaction - aborting...", null);
        try {
          abortTransaction(new MulgaraTransactionException("Transaction finalized while still valid"));
        } catch (Throwable th) {
          errorReport("Attempt to abort transaction from finalize failed", th);
        }
      }
  
      if (state != State.FAILED && (inuse != 0 || using != 0)) {
        errorReport("Reference counting error in transaction", null);
      }
  
      if (transaction != null) {
        errorReport("Transaction not terminated properly", null);
      }
    } finally {
      super.finalize();
    }
  }

  private final void report(String desc) {
    if (logger.isInfoEnabled()) {
      logger.info(desc + ": " + System.identityHashCode(this) + ", state=" + state +
          ", inuse=" + inuse + ", using=" + using);
    }
  }

  private final void debugReport(String desc) {
    if (logger.isDebugEnabled()) {
      logger.debug(desc + ": " + System.identityHashCode(this) + ", state=" + state +
          ", inuse=" + inuse + ", using=" + using);
    }
  }

  private final void errorReport(String desc, Throwable cause) {
    if (cause != null) {
      logger.error(desc + ": " + System.identityHashCode(this) + ", state=" + state +
          ", inuse=" + inuse + ", using=" + using, cause);
    } else {
      logger.error(desc + ": " + System.identityHashCode(this) + ", state=" + state +
          ", inuse=" + inuse + ", using=" + using + "\n" + new StackTrace());
    }
  }
}
