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
 * by Netymon Pty Ltd are Copyright (c) 2007 Netymon Pty Ltd.
 * All Rights Reserved.
 *
 * Migration to interface Copyright (c) 2007 Topaz Foundation
 * under contract by Andrae Muys (mailto:andrae@netymon.com).
 */
package org.mulgara.resolver;

// Java 2 enterprise packages

// Third party packages

// Local packages
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.EnlistableResource;

import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.query.TuplesException;

/**
 * @created 2007-11-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @version $Revision: $
 *
 * @modified $Date: $
 *
 * @maintenanceAuthor $Author: $
 *
 * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
 *
 * @copyright &copy;2007 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *
 * @licence Open Software License v3.0
 */
public interface MulgaraTransaction {
  void reference() throws MulgaraTransactionException;
  void dereference() throws MulgaraTransactionException;

  /**
   * Calls through to {@link #abortTransaction(String, Throwable)} to force the transaction
   * to be abandoned, using the same message as is present in the cause.
   */
  MulgaraTransactionException abortTransaction(Throwable cause) throws MulgaraTransactionException;

  /**
   * Forces the transaction to be abandoned, including bypassing JTA to directly
   * rollback/abort the underlying store-phases if required.
   * This this transaction is externally managed this amounts to a heuristic
   * rollback decision and should be treated as such.
   *
   * @return an exception constructed with the provided error-message and cause.
   * @throws MulgaraTransactionException if a further error is encounted while
   * attempting to abort.
   */
  MulgaraTransactionException abortTransaction(String errorMessage, Throwable cause) throws MulgaraTransactionException;

  /**
   * Perform a heuristic rollback on the transaction.
   *
   * @param cause the reason for the rollback
   * @throws MulgaraTransactionException if an error is encounted while attempting to roll back;
   *                                     the transaction will still have been terminated, though.
   */
  void heuristicRollback(String cause) throws MulgaraTransactionException;

  /**
   * Execute the specified operation.
   */
  void execute(Operation operation,
               DatabaseMetadata metadata) throws MulgaraTransactionException;

  /**
   * Execute the specified operation.
   * Used by TransactionalAnswer to ensure transactional guarantees are met when
   * using a result whose transaction may be in a suspended state.
   */
  AnswerOperationResult execute(AnswerOperation ao) throws TuplesException;

  /**
   * Used by the TransactionCoordinator/Manager.
   */
  void execute(TransactionOperation to) throws MulgaraTransactionException;

  /**
   * enlist an XAResource in this transaction - includes an extra method
   * abort().
   */
  public void enlist(EnlistableResource enlistable) throws MulgaraTransactionException;

  /**
   * Returns the currentTimeMillis when the transaction last used;
   *   -1 if it is currently in use; or
   *    0 if it has never been used.
   */
  public long lastActive();
}
