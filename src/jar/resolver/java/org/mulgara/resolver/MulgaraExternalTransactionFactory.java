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
import java.util.HashSet;
import java.util.Set;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

// Local packages
import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.query.QueryException;

/**
 * Manages external transactions.
 *
 * @created 2007-11-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2007 <a href="http://www.topazproject.org/">Topaz Foundation</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class MulgaraExternalTransactionFactory extends MulgaraTransactionFactory {
  private final Set<MulgaraExternalTransaction> transactions;

  private final MulgaraXAResourceContext xaResource;

  private MulgaraExternalTransaction associatedTransaction;
  private String                     lastRollbackCause;

  public MulgaraExternalTransactionFactory(DatabaseSession session, MulgaraTransactionManager manager) {
    super(session, manager);

    this.associatedTransaction = null;
    this.lastRollbackCause = null;
    this.transactions = new HashSet<MulgaraExternalTransaction>();
    this.xaResource = new MulgaraXAResourceContext(this, session);
  }

  public MulgaraTransaction getTransaction(boolean write) throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (associatedTransaction == null) {
        throw new MulgaraTransactionException(
            "No externally mediated transaction associated with session" +
            (lastRollbackCause != null ? " - last transaction was rolled back with error: " +
                                         lastRollbackCause : ""));
      } else if (write && associatedTransaction != writeTransaction) {
        throw new MulgaraTransactionException("RO-transaction associated with session when requesting write operation");
      }

      return associatedTransaction;
    } finally {
      releaseMutex();
    }
  }

  protected MulgaraExternalTransaction createTransaction(Xid xid, boolean write)
      throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (associatedTransaction != null) {
        throw new MulgaraTransactionException(
            "Attempt to initiate transaction with existing transaction active with session");
      }
      if (write && manager.isHoldingWriteLock(session)) {
        throw new MulgaraTransactionException("Attempt to initiate two write transactions from the same session");
      }

      if (write) {
        manager.obtainWriteLock(session);
        MulgaraExternalTransaction xa = null;
        try {
          xa = new MulgaraExternalTransaction(this, xid, session.newOperationContext(true));
          writeTransaction = xa;
          associatedTransaction = xa;
          lastRollbackCause = null;
          transactions.add(xa);
          transactionCreated(xa);

          return xa;
        } catch (Throwable th) {
          manager.releaseWriteLock(session);
          if (xa != null)
            transactionComplete(xa, th.toString());
          throw new MulgaraTransactionException("Error initiating write transaction", th);
        }
      } else {
        try {
          MulgaraExternalTransaction xa = new MulgaraExternalTransaction(this, xid, session.newOperationContext(false));
          associatedTransaction = xa;
          lastRollbackCause = null;
          transactions.add(xa);
          transactionCreated(xa);

          return xa;
        } catch (QueryException eq) {
          throw new MulgaraTransactionException("Error obtaining new read-only operation-context", eq);
        }
      }
    } finally {
      releaseMutex();
    }
  }

  protected Set<MulgaraExternalTransaction> getTransactions() {
    return transactions;
  }

  public XAResource getXAResource(boolean writing) {
    acquireMutex(0, RuntimeException.class);
    try {
      return xaResource.getResource(writing);
    } finally {
      releaseMutex();
    }
  }

  public void transactionComplete(MulgaraExternalTransaction xa, String rollbackCause)
      throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      super.transactionComplete(xa);

      if (xa == null) {
        throw new IllegalArgumentException("Null transaction indicated completion");
      }
      if (xa == writeTransaction) {
        manager.releaseWriteLock(session);
        writeTransaction = null;
      }
      transactions.remove(xa);
      if (associatedTransaction == xa) {
        associatedTransaction = null;
        lastRollbackCause = rollbackCause;
      }
    } finally {
      releaseMutex();
    }
  }

  public boolean hasAssociatedTransaction() {
    acquireMutex(0, RuntimeException.class);
    try {
      return associatedTransaction != null;
    } finally {
      releaseMutex();
    }
  }

  public boolean associateTransaction(MulgaraExternalTransaction xa) {
    acquireMutex(0, RuntimeException.class);
    try {
      if (associatedTransaction != null) {
        return false;
      } else {
        associatedTransaction = xa;
        return true;
      }
    } finally {
      releaseMutex();
    }
  }

  public MulgaraExternalTransaction getAssociatedTransaction() {
    acquireMutex(0, RuntimeException.class);
    try {
      return associatedTransaction;
    } finally {
      releaseMutex();
    }
  }

  public void disassociateTransaction(MulgaraExternalTransaction xa) 
      throws MulgaraTransactionException {
    acquireMutex(0, MulgaraTransactionException.class);
    try {
      if (associatedTransaction == xa) {
        associatedTransaction = null;
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
}
