package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Represents a Jena graph as instantiated by Mulgara.
 *
 * @author Andy Seaborne
 * @copyright &copy; 2008 Hewlett-Packard Development Company, LP
 */
public class GraphMulgara extends GraphBase {
  // TODO Bnodes (bless 'em!) - currently skolemized, change to label skolemization.
  // TODO Batched updates

  private Session session;
  private URI modelURI;
  private QueryHandler queryHandler;
  private TransactionMulgara transactionHandler = null;
    
  public GraphMulgara(Session session, URI modelURI) {
    this.session = session;
    this.modelURI = modelURI;
    transactionHandler = new TransactionMulgara(this, session);
    queryHandler = new PatternMulgara(this);
  }
    
  public Session getSession() {
    return session;
  }

  public URI getModelURI() {
    return modelURI;
  }

  @Override
  public TransactionHandler getTransactionHandler() {
    return transactionHandler;
  }

  @Override 
  public QueryHandler queryHandler() {
    return queryHandler;
  }
    
  @Override
  public void performAdd(Triple t) {
    // if (transactionHandler.inExplicitTransaction()) { }
        
    try {
      Set<org.jrdf.graph.Triple> acc = prepare(t);
      session.insert(modelURI, acc);
    } catch (QueryException ex) {
      throw new JenaException(ex);
    }
  }

  @Override
  public void performDelete(Triple t) { 
    try {
      Set<org.jrdf.graph.Triple> acc = prepare(t) ;
      session.delete(modelURI, acc) ;
    } catch (QueryException ex) {
      throw new JenaException(ex) ;
    }
  }

  private final Set<org.jrdf.graph.Triple> prepare(Triple t) {
    Node s = t.getSubject();
    Node p = t.getPredicate();
    Node o = t.getObject();
    return prepare(s,p,o);
  }
    
  private final Set<org.jrdf.graph.Triple> prepare(Node s, Node p, Node o) {
    try {
//      if (s.isBlank() || o.isBlank()) throw new JenaException("GraphMulgara : blank nodes not supported yet") ; 
      SubjectNode sNode = (SubjectNode)Jenara.n2v(s, session);
      PredicateNode pNode = (PredicateNode)Jenara.n2v(p, session);
      ObjectNode oNode = (ObjectNode)Jenara.n2v(o, session);
      org.jrdf.graph.Triple mt = new org.mulgara.query.rdf.TripleImpl(sNode, pNode, oNode);
      Set<org.jrdf.graph.Triple> acc = new HashSet<org.jrdf.graph.Triple>();
      acc.add(mt);
      return acc;
    } catch (URISyntaxException ex) {
      throw new JenaException(ex);
    }
  }

  @Override
  protected ExtendedIterator graphBaseFind(TripleMatch m) {
    int length = 0;
    Node s = m.getMatchSubject();
    Node p = m.getMatchPredicate();
    Node o = m.getMatchObject();

    if (s == null || s.equals(Node.ANY)) {
      s = Node.createVariable("s");
      length++;
    }
    if (p == null || p.equals(Node.ANY)) {
      p = Node.createVariable("p");
      length++;
    }
    if (o == null || o.equals(Node.ANY)) {
      o = Node.createVariable("o");
      length++;
    }

    // Check not already a variable.
        
    Node[] variables = new Node[length];
    int i = 0;
        
    if (s.isVariable()) variables[i++] = s;  
    if (p.isVariable()) variables[i++] = p;
    if (o.isVariable()) variables[i++] = o;
        
    Triple t = new Triple(s,p,o) ;
    com.hp.hpl.jena.graph.query.Query graphQuery = new com.hp.hpl.jena.graph.query.Query();
    graphQuery.addMatch(t);
    BindingQueryPlan plan = queryHandler().prepareBindings(graphQuery, variables);
    ExtendedIterator iter = plan.executeBindings(); // Of domains
    return new AnswerBindingIterator(iter, variables, t);
  }
    
  static class AnswerBindingIterator extends NiceIterator {
    private ExtendedIterator iter ;
    private final Triple triple ;
    private Triple slot ;
    private boolean finished ;
        
    public AnswerBindingIterator(ExtendedIterator iter, Node[] variables, Triple t) {
      this.iter = iter ;
      this.triple = t ;
      finished = false ;
    }


    @Override
    public boolean hasNext() {
      if (finished) return false;

      if (slot != null) return true;
            
      if (!iter.hasNext()) {
        NiceIterator.close(iter);
        finished = true;
        return false;
      }
            
      Domain d = (Domain)iter.next();
      Node s = triple.getSubject();
      Node p = triple.getPredicate();
      Node o = triple.getObject();
      int i = 0;
      if (s.isVariable()) s = d.getElement(i++);
      if (p.isVariable()) p = d.getElement(i++);
      if (o.isVariable()) o = d.getElement(i++);
      slot = new Triple(s,p,o);
      return true ;
    }

    // This form is common.
    @Override
    public Object next() {
      if (!hasNext()) throw new NoSuchElementException("AnswerIterator.next");
      Triple t = slot;
      slot = null;
      return t;
    }

    @Override
    public void remove() { throw new UnsupportedOperationException(); } 
  }

  //private Node bNodeProperty = Node.createURI("http://jena.hpl.hp.com/JenaMulgara#bNodeLabel") ; 
    
    
  // Callbacks from the tranaction handler.
  void transactionStart() {}
    
  void transactionCommit() {
    removeBNodeLabels() ;
    flushBNodeMapping() ;
  }
    
  void transactionAbort() { 
    // No need - the transaction rollback will do that.
    //removeBNodeLabels() ;
    flushBNodeMapping() ;
  }
    
  void flushBNodeMapping() {
    //nodesToValues.clear();
    //valuesToNodes.clear() ;
  }

  void removeBNodeLabels() { }

    
  @Override
  public void close() {
    super.close();
  }

  @Override
  public Capabilities getCapabilities() {
    if (capabilities == null) capabilities = new Capabilities() {
        public boolean sizeAccurate()                   { return true; }
        public boolean addAllowed()                     { return true ; }
        public boolean addAllowed( boolean every )      { return true; } 
        public boolean deleteAllowed()                  { return true ; }
        public boolean deleteAllowed( boolean every )   { return true; } 
        public boolean canBeEmpty()                     { return true; }
        public boolean iteratorRemoveAllowed()          { return false; } /* ** */
        public boolean findContractSafe()               { return true; }
        public boolean handlesLiteralTyping()           { return false; } /* ** */
    }; 
    
    return super.getCapabilities() ;
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
