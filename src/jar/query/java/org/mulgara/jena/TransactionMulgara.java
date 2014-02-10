package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import org.apache.log4j.Logger;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

import com.hp.hpl.jena.graph.impl.TransactionHandlerBase;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Manages Jena transactions using the Mulgara transaction framework.
 *
 * @author Andy Seaborne
 * @copyright &copy; 2008 Hewlett-Packard Development Company, LP
 */
class TransactionMulgara extends TransactionHandlerBase {

  private static final Logger log = Logger.getLogger(TransactionMulgara.class.getName()) ;

  GraphMulgara mGraph ;
  Session session  ;
  boolean transactionActive = false ;

  boolean inExplicitTransaction() {
    return transactionActive;
  }
  
  TransactionMulgara(GraphMulgara mGraph, Session session) {
    this.session = session;
    this.mGraph = mGraph;
  }

  private static void fatal(Exception ex) {
    throw new JenaException(ex);
  }
  
  private static void fatal(String msg) {
    throw new JenaException(msg);
  }
  
  public void abort() {
    try {
      if (!transactionActive) fatal("abort: Not in an explicit transaction");
        session.rollback();
        transaction(false);
      } catch (QueryException ex) {
        log.warn("Error aborting transaction", ex);
      }
  }

  public void begin() {
    if (transactionActive) fatal("begin: Transaction already started");
    transaction(true);
  }
  
  public void commit() {
    try {
      if (!transactionActive) fatal("commit: Not in an explicit transaction") ;
      session.commit() ;
      transaction(false) ;
    } catch (QueryException ex) {
      log.warn("Error committing transaction", ex);
    }
  }

  public boolean transactionsSupported() {
    return true ;
  }

  private void transaction(boolean transactionOn) {
    try {
      session.setAutoCommit(!transactionOn) ;
      transactionActive = transactionOn ;
    } catch (QueryException ex) {
      fatal(ex);
    }
  }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
