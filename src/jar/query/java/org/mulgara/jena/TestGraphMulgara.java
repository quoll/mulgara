package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import org.mulgara.query.QueryException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Test suite for the Jena bridge
 *
 * @created Jun 22, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 Hewlett-Packard Development Company, LP
 */
public class TestGraphMulgara extends AbstractTestGraph {

  public TestGraphMulgara() {
    super("GraphMulgara") ;
  }

  // Create new session each time.
  // The Mulgara model must be created once (and possibly deleted) because of
  // Mulgara Session.modelExists fails if the model never existed 
  @SuppressWarnings("deprecation")
  @Override
  public Graph getGraph() {
    try {
      JenaMulgara.dropGraph("rmi://localhost/server1", "data:,test");
    } catch (JenaException e) {
      if (!(e.getCause() instanceof QueryException)) throw e;
    }
    return JenaMulgara.createGraph("rmi://localhost/server1", "data:,test") ;
  }
  
  // This test is wrong in AbstractTestGraph for Mulgara.
  @Override public void testHasTransactions() {
    Graph g = getGraph();
    TransactionHandler th = g.getTransactionHandler();
    assertTrue(th.transactionsSupported()) ;
    
    th.begin(); th.abort();
    
    th.begin(); th.commit();
    /* */
    Command cmd = new Command() 
    { public Object execute() { return null; } };
    try { th.executeInTransaction( cmd ); } 
    catch (UnsupportedOperationException x) {}
  }

  @Override public void testContainsNode() {
    Graph g = getGraph();
    graphAdd( g, "a P b; _c P1 _d; a P2 12" );
    QueryHandler qh = g.queryHandler();
    assertTrue( qh.containsNode( node( "a" ) ) );
    assertTrue( qh.containsNode( node( "P" ) ) );
    assertTrue( qh.containsNode( node( "b" ) ) );
    assertTrue( qh.containsNode( node( "_c" ) ) );
    assertTrue( qh.containsNode( node( "_d" ) ) );
    assertTrue( qh.containsNode( node( "P2" ) ) );
    assertTrue( qh.containsNode( node( "12" ) ) );
    /* */
    assertFalse( qh.containsNode( node( "x" ) ) );
    assertFalse( qh.containsNode( node( "_y" ) ) );
    assertFalse( qh.containsNode( node( "99" ) ) );
  }
  
  // Tests that do not apply.
  @Override public void testIsomorphismFile() {}
  @Override public void testContainsFluid() {}

  // These assume remove from an iterator.
  @Override public void testRemoveAllEvent() {}
  @Override public void testRemoveAll() {}
  @Override public void testRemoveSPO() {}
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