package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.mulgara.query.*;
import org.mulgara.server.Session;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.graph.query.TreeQueryPlan;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Handles Jena query patterns in Mulgara
 *
 * @author Andy Seaborne
 * @copyright &copy; 2008 Hewlett-Packard Development Company, LP
 */
public class PatternMulgara extends SimpleQueryHandler {
  // Scruffy

  private static final Logger logger = Logger.getLogger(PatternMulgara.class);
  
  GraphMulgara graphMulgara;

  /**
   * @param graph
   */
  public PatternMulgara(GraphMulgara graph) {
    super(graph);
    graphMulgara = graph;
  }

  @Override
  final public TreeQueryPlan prepareTree(Graph pattern) {
    throw new RuntimeException("prepareTree - Chris says this will not be called");
  }
  
  @SuppressWarnings("unchecked")
  static List<Order> x = Collections.EMPTY_LIST;
  
  @Override
  public BindingQueryPlan prepareBindings(Query q, Node [] variables) {
    Map<Node, Variable> vars_jm = new HashMap<Node,Variable>();
    List<Variable> proj = new ArrayList<Variable>() ;   // Mirrors Node [] variables
    int idx = 0;

    // Allocate variables.  Projection and pattern
    for (int i = 0; i < variables.length; i++) {
      Node v = variables[i] ;
      idx = allocVar(vars_jm, proj, v, idx) ;
    }
    
    @SuppressWarnings("unchecked")
    List<Triple> pattern = q.getPattern() ;
    for (Triple t: pattern) {
      Node s = t.getSubject();
      Node p = t.getPredicate();
      Node o = t.getObject();
      idx = allocVar(vars_jm, null, s, idx);
      idx = allocVar(vars_jm, null, p, idx);
      idx = allocVar(vars_jm, null, o, idx);
    }

    // Mulgara constraint
    ConstraintExpression constraint = null;
    for (Triple t: pattern) {
        constraint = compileTriplePattern(constraint, vars_jm, 
                                          t.getSubject(), t.getPredicate(), t.getObject());
    }

    // Mulgara query
    org.mulgara.query.Query query = new org.mulgara.query.Query(
         proj,                     // Projection
         new GraphResource(graphMulgara.getModelURI()),        // Model to query
         constraint,               // WHERE
         null,                     // HAVING
         x,                        // ORDER
         null,                     // LIMIT
         0,                        // OFFSET
         true,                     // DISTINCT
         new UnconstrainedAnswer() // Initial Answer
    );

    // Wrap it up into an query execution instance.
    return new BindingQueryPlanMulgara(query, graphMulgara.getSession(), proj);
  }
      
  private static int allocVar(Map<Node, Variable> vars_jm, List<Variable> proj, Node v, int idx) {
    if (!v.isVariable()) return idx ;
    if (!vars_jm.containsKey(v)) {
      String vn = "v"+(idx++);
      Variable vm = new Variable(vn);
      vars_jm.put(v, vm);
      if (proj != null) proj.add(vm);
    }
    return idx ;
  }
  
  private ConstraintExpression compileTriplePattern(ConstraintExpression c,
                                                           Map<Node,Variable> vars_jm,
                                                           Node s, Node p, Node o) {
    try {
      ConstraintElement sElt = compile(vars_jm, s);
      ConstraintElement pElt = compile(vars_jm, p);
      ConstraintElement oElt = compile(vars_jm, o);

      ConstraintExpression c2 = new ConstraintImpl(sElt, pElt, oElt);

      if (c != null) return new org.mulgara.query.ConstraintConjunction(c, c2);
      else return c2;

    } catch (Exception ex) {
      throw new JenaException("Failed to compile: ("+s+" "+p+" "+o+")");
    }
  }

  private ConstraintElement compile(Map<Node, Variable> vars, Node n) throws URISyntaxException {
    if (n.isVariable()) return vars.get(n);
    else return Jenara.n2v(n, graphMulgara.getSession());
  }


  static class BindingQueryPlanMulgara implements BindingQueryPlan {
    org.mulgara.query.Query query;
    AnswerIterator iter;
    private List<Variable> projection;
    private Session session;
    
    public BindingQueryPlanMulgara(org.mulgara.query.Query query,
                                   Session session,
                                   List<Variable> proj) {
      this.query = query ;
      this.session = session ;
      this.projection = proj ;
    }
    
    public ExtendedIterator executeBindings() {
      try {
        Answer answer = session.query(query); 
        // Must reset
        answer.beforeFirst();
        iter = new AnswerIterator(answer, projection);
        return iter;

      } catch (Exception ex) {
        throw new JenaException("Failed to executeBindings");
      }
    }
  }  
  
  // original comment of extending NiceIterator<Domain>
  static class AnswerIterator extends NiceIterator {
    private Answer answer;
    private Domain slot = null;
    private boolean finished = false;
    private List<Variable> proj;

    AnswerIterator(Answer answer, List<Variable> proj) {
      this.answer = answer;
      this.proj = proj;
    }

    @Override
    public boolean hasNext() {
      if (finished) return false;

      if (slot != null) return true;
      try {
        if (! answer.next()) {
            close();
            return false;
        }
        slot = convert();

      } catch (TuplesException ex) {
        logger.warn("Exception iterating Mulgara answer", ex);
        return false;
      }

      return slot != null;
    }

    Domain convert() throws TuplesException {
      Domain d = new Domain(proj.size());
      for (int i = 0; i < proj.size(); i++) {
        Node n = Jenara.o2n((org.jrdf.graph.Node)answer.getObject(i));
        d.setElement(i, n);
      }
      return d ;
    }

    @Override
    public Object next() {
      if (!hasNext()) throw new NoSuchElementException("AnswerIterator.next");
      Domain d = slot;
      slot = null;
      return d;
    }

    @Override
    public void close() {
      if (finished) return;
      finished = true;
      try {
        answer.close();
      } catch (TuplesException ex) {
        logger.warn("Exception closing answer", ex);
      }
    }

    @Override
    public void remove() { throw new UnsupportedOperationException(); } 
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
