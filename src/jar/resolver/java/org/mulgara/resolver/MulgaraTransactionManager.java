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
 * Contributor(s):
 *   Refactoring to focus on write-lock management contributed by Netymon
 *   Pty Ltd on behalf of Topaz Foundation under contract.
 */

package org.mulgara.resolver;

// Java2 packages
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.util.StackTrace;

/**
 * Manages the Write-Lock.
 *
 * Manages tracking the ownership of the write-lock.
 * Provides a facility to trigger a heuristic rollback of any transactions still
 *   valid on session close.
 * Maintains the write-queue
 *
 * @created 2006-10-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @version $Revision: $
 *
 * @modified $Date: $
 *
 * @maintenanceAuthor $Author: $
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class MulgaraTransactionManager {
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(MulgaraTransactionManager.class.getName());

  // Write lock is associated with a session.
  private DatabaseSession sessionHoldingWriteLock;

  // Used to support write-lock reservation.
  private DatabaseSession sessionReservingWriteLock;

  // Used to synchronize access to other fields.
  private final ReentrantLock mutex;
  private final Condition writeLockCondition;

  public MulgaraTransactionManager() {
    this.sessionHoldingWriteLock = null;
    this.sessionReservingWriteLock = null;
    this.mutex = new ReentrantLock();
    this.writeLockCondition = this.mutex.newCondition();
  }


  /** 
   * Obtains the write lock.
   */
  void obtainWriteLock(DatabaseSession session) throws MulgaraTransactionException {
    acquireMutex();
    try {
      if (sessionHoldingWriteLock == session) {
        return;
      }

      while (writeLockHeld() || (writeLockReserved() && !writeLockReserved(session))) {
        try {
          writeLockCondition.await();
        } catch (InterruptedException ei) {
          throw new MulgaraTransactionException("Interrupted while waiting for write lock", ei);
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Obtaining write lock\n" + new StackTrace());
      }
      sessionHoldingWriteLock = session;
    } finally {
      releaseMutex();
    }
  }

  boolean isHoldingWriteLock(DatabaseSession session) {
    acquireMutex();
    try {
      return sessionHoldingWriteLock == session;
    } finally {
      releaseMutex();
    }
  }


  void releaseWriteLock(DatabaseSession session) throws MulgaraTransactionException {
    acquireMutex();
    try {
      if (sessionHoldingWriteLock == null) {
        return;
      }
      if (sessionHoldingWriteLock != session) {
        throw new MulgaraTransactionException("Attempted to release write lock being held by another session");
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Releasing writelock\n" + new StackTrace());
      }
      sessionHoldingWriteLock = null;
      writeLockCondition.signal();
    } finally {
      releaseMutex();
    }
  }


  /**
   * Used to replace the built in monitor to allow it to be properly released
   * during potentially blocking operations.  All potentially blocking
   * operations involve writes, so in these cases the write-lock is reserved
   * allowing the mutex to be safely released and then reobtained after the
   * blocking operation concludes.
   */
  private void acquireMutex() {
    mutex.lock();
  }


  /**
   * Used to reserve the write lock during a commit or rollback.
   * Should only be used by a transaction manager.
   */
  void reserveWriteLock(DatabaseSession session) throws MulgaraTransactionException {
    acquireMutex();
    try {
      if (session != sessionReservingWriteLock && session != sessionHoldingWriteLock) {
        throw new IllegalStateException("Attempt to reserve writelock without holding writelock");
      }
      if (session != sessionReservingWriteLock && sessionReservingWriteLock != null) {
        throw new IllegalStateException("Attempt to reserve writelock when writelock already reserved");
      }

      sessionReservingWriteLock = session;
    } finally {
      releaseMutex();
    }
  }

  boolean writeLockReserved() {
    acquireMutex();
    try {
      return sessionReservingWriteLock != null;
    } finally {
      releaseMutex();
    }
  }

  boolean writeLockReserved(DatabaseSession session) {
    acquireMutex();
    try {
      return session == sessionReservingWriteLock;
    } finally {
      releaseMutex();
    }
  }

  void releaseReserve(DatabaseSession session) {
    acquireMutex();
    try {
      if (!writeLockReserved()) {
        return;
      }
      if (!writeLockReserved(session)) {
        throw new IllegalStateException("Attempt to release reserve without holding reserve");
      }

      sessionReservingWriteLock = null;
      writeLockCondition.signal();
    } finally {
      releaseMutex();
    }
  }

  private boolean writeLockHeld() {
    return sessionHoldingWriteLock != null;
  }

  private void releaseMutex() {
    if (!mutex.isHeldByCurrentThread()) {
      throw new IllegalStateException("Attempt to release mutex without holding mutex");
    }

    mutex.unlock();
  }

  public void closingSession(DatabaseSession session) throws MulgaraTransactionException {
    // This code should not be required, but is there to ensure the manager is
    // reset regardless of errors in the factories.
    acquireMutex();
    try {
      Throwable error = null;

      if (writeLockReserved(session)) {
        try {
          releaseReserve(session);
        } catch (Throwable th) {
          logger.error("Error releasing reserve on force-close", th);
          error = (error == null) ? th : error;
        }
      }

      if (isHoldingWriteLock(session)) {
        try {
          releaseWriteLock(session);
        } catch (Throwable th) {
          logger.error("Error releasing write-lock on force-close", th);
          error = (error == null) ? th : error;
        }
      }

      if (error != null) {
        if (error instanceof MulgaraTransactionException) {
          throw (MulgaraTransactionException)error;
        } else {
          throw new MulgaraTransactionException("Error force releasing write-lock", error);
        }
      }
    } finally {
      releaseMutex();
    }
  }
}
