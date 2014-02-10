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
 */

package org.mulgara.resolver;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.AbstractAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.MulgaraTransactionException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * A transactional answer.  
 * Wraps all calls to the enclosed answer object, ensuring all calls are made
 * within an activated transactional context.  Also ensures that that context
 * is deactivated upon returning from the outer-call.
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

public class TransactionalAnswer extends AbstractAnswer implements Answer {
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(TransactionalAnswer.class.getName());

  private Answer answer;

  private MulgaraTransaction transaction;

  private boolean closing;

  public TransactionalAnswer(MulgaraTransaction transaction, Answer answer) throws TuplesException {
    try {
      report("Creating Answer");

      if (transaction == null) {
        throw new IllegalArgumentException("Transaction null in TransactionalAnswer");
      } else if (answer == null) {
        throw new IllegalArgumentException("Answer null in TransactionalAnswer");
      }

      this.answer = answer;
      this.closing = false;
      this.transaction = transaction;
      transaction.reference();

      report("Created Answer");
    } catch (MulgaraTransactionException em) {
      throw new TuplesException("Failed to associate with transaction", em);
    }
  }

  public Object getObject(final int column) throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnObject(answer.getObject(column));
        }
      }).getObject();
  }

  public Object getObject(final String columnName) throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnObject(answer.getObject(columnName));
        }
      }).getObject();
  }

  public void beforeFirst() throws TuplesException {
    notClosed();
    transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          answer.beforeFirst();
        }
      });
  }

  public void close() throws TuplesException {
    report("Closing Answer");
    if (closing) {
      report("Deferring close to enclosing call");
      return;
    }
    try {
      notClosed();
      closing = true;
      transaction.execute(new AnswerOperation() {
          public void execute() throws TuplesException {
            answer.close();
            try {
              transaction.dereference();
            } catch (MulgaraTransactionException em) {
              throw new TuplesException("Error dereferencing transaction", em);
            }
          }
        });
    } finally {
      // !!FIXME: Note - We will need to add checks for null to all operations.
      closing = false;
      transaction = null;
      answer = null;    // Note this permits the gc of the answer.
      report("Closed Answer");
    }
  }

  public int getColumnIndex(final Variable column) throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnInt(answer.getColumnIndex(column));
        }
      }).getInt();
  }

  public int getNumberOfVariables() {
    try {
      notClosed();
      return transaction.execute(new AnswerOperation() {
          public void execute() {
            returnInt(answer.getNumberOfVariables());
          }
        }).getInt();
    } catch (TuplesException et) {
      throw new IllegalStateException(et.getMessage(), et);
    }
  }

  public Variable[] getVariables() {
    try {
      notClosed();
      return (Variable[])(transaction.execute(new AnswerOperation() {
          public void execute() {
            returnObject(answer.getVariables());
          }
        }).getObject());
    } catch (TuplesException et) {
      throw new IllegalStateException(et.getMessage(), et);
    }
  }

  public boolean isUnconstrained() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnBoolean(answer.isUnconstrained());
        }
      }).getBoolean();
  }

  public long getRowCount() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnLong(answer.getRowCount());
        }
      }).getLong();
  }

  public long getRowUpperBound() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnLong(answer.getRowUpperBound());
        }
      }).getLong();
  }

  public long getRowExpectedCount() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnLong(answer.getRowExpectedCount());
        }
      }).getLong();
  }

  public int getRowCardinality() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnInt(answer.getRowCardinality());
        }
      }).getInt();
  }


  public boolean isEmpty() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnBoolean(answer.isEmpty());
        }
      }).getBoolean();
  }
  public boolean next() throws TuplesException {
    notClosed();
    return transaction.execute(new AnswerOperation() {
        public void execute() throws TuplesException {
          returnBoolean(answer.next());
        }
      }).getBoolean();
  }

  public Object clone() {
    try {
      TransactionalAnswer c = (TransactionalAnswer)super.clone();
      c.answer = (Answer)this.answer.clone();
      c.transaction.reference();
      report("Cloned Answer, clone=" + System.identityHashCode(c));

      return c;
    } catch (MulgaraTransactionException em) {
      throw new IllegalStateException("Failed to associate with transaction", em);
    }
  }

  public boolean equals(Object o) {
    return super.equals(o);
  }

  public int hashCode() {
    return System.identityHashCode(this);
  }

  private void report(String desc) {
    if (logger.isDebugEnabled()) {
      logger.debug(desc + ": " + System.identityHashCode(this) + ", xa=" + System.identityHashCode(transaction));
    }
  }

  @SuppressWarnings("unused")
  private void warnReport(String desc) {
    logger.warn(desc + ": " + System.identityHashCode(this) + ", xa=" + System.identityHashCode(transaction));
  }

  public void finalize() throws Throwable {
    try {
      report("GC-finalizing");
      if (transaction != null) {
        logger.warn("TransactionalAnswer not closed");
      /*
      try {
        transaction.execute(new AnswerOperation() {
            public void execute() throws TuplesException {
              try {
                answer.close();
              } finally {
                try {
                  transaction.dereference();
                } catch (MulgaraTransactionException em) {
                  throw new TuplesException("Error dereferencing transaction", em);
                }
              }
            }
        });
      } catch (TuplesException et) {
        report("Error dereferencing transaction from finalize");
      } finally {
        transaction = null;
        answer = null;
      }
*/
//      try {
//        sessionClose();
//      } catch (TuplesException et) {
//        logger.warn("Error force-closing TransactionAnswer", et);
//      }
      }
    } finally {
      super.finalize();
    }
  }


  void sessionClose() throws TuplesException {
    if (closing) {
      report("Session close on closing answer");
      return;
    }

    if (answer != null) {
      report("Session forced close");
      closing = true;
      try {
        answer.close();
      } catch (TuplesException e) {
        throw e;
      } catch (Throwable th) {
        throw new TuplesException("Error closing answer", th);
      } finally {
        try {
          transaction.dereference();
        } catch (MulgaraTransactionException em) {
          throw new TuplesException("Error dereferencing transaction", em);
        } finally {
          closing = false;
          answer = null;
          transaction = null;
        }
      }
//      close();
    }
  }

  private void notClosed() throws TuplesException {
    if (transaction == null) {
      throw new TuplesException("TransactionalAnswer closed");
    } else if (answer == null) {
      throw new TuplesException("TransactionAnswer not closed, but Answer null");
    }
  }
}
