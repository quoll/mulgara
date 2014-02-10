package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.server.Session;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;

/**
 * This class was developed to allow for transitions between nodes in the Jena API
 * and nodes in the Mulgara API.
 * May only be used with the XA1.1 StringPool.
 * <em>Blank node allocations will fail with the XA StringPool.</em>
 * 
 * @created Jun 22, 2009
 * @author Andy Seaborne
 * @copyright &copy; Hewlett-Packard Development Company, LP
 */
public class Jenara {

  // removing skolemizing will require a new Resolver API to return
  // a mapping from requested blank nodes to newly allocated blank nodes.

  // Skolemized Bnodes are URIs of the form "bnode:label"
  static boolean skolemizedBlankNodes = true;
  static final String bNodeScheme = "bnode:";
  static final String LABEL = BlankNodeImpl.LABEL;
  static final int LABEL_LEN = LABEL.length();
    
  // UGLY (and duplicated from GraphMulgara)
  // These maps ensure that Jena allocated nodes are associated with Mulgara nodes
  // Mulgara allocated nodes are converted to Jena labels with appropriate identifiers

  // Jena --> Mulgara
  private static Map<Node,Value> nodesToValues = new HashMap<Node,Value>();
  // Mulgara --> Jena
  private static Map<Long,Node> valuesToNodes = new HashMap<Long,Node>();


  /**
   * Map a Jena graph node to a Mulgara value.
   * @param x The Jena Node to convert.
   * @param session A session to use for blank node persistence.
   * @return A Mulgara Value.
   * @throws URISyntaxException When creating a URIReference that refers to an invalid URI.
   */
  static Value n2v(Node x, Session session) throws URISyntaxException {
    if (x.isURI()) return new URIReferenceImpl(new URI(x.getURI()));

    if (x.isLiteral()) {
      // The return types are Mulgara LiteralImpl
      if (x.getLiteralDatatypeURI() != null) {
        return new LiteralImpl(x.getLiteralLexicalForm(), new URI(x.getLiteralDatatypeURI()));
      }
      if (x.getLiteralLanguage() != null) {
        return new LiteralImpl(x.getLiteralLexicalForm(), x.getLiteralLanguage());
      }
      return new LiteralImpl(x.getLiteralLexicalForm()) ;
    }

    if (x.isBlank()) {
      // is this a previously encountered Jena-allocated node?
      Value bn = nodesToValues.get(x);
      if (bn != null) return bn;

      // May be a Mulgara-allocated bNode (and so we we have seen before)
      String blankLabel = x.getBlankNodeLabel();
      if (blankLabel.startsWith(LABEL)) {
        long id = Long.parseLong(blankLabel.substring(LABEL_LEN));
        return new BlankNodeImpl(BlankNodeImpl.counterToNode(id));
      }

      // It's not - it's a Jena one.
      if (skolemizedBlankNodes) {
        String skol = bNodeScheme+x.getBlankNodeLabel();
        return new URIReferenceImpl(new URI(skol));
      }

      // Not a Mulgara-allocated bNode.  Create a new mapping.
      BlankNodeImpl v = new BlankNodeImpl();

      nodesToValues.put(x, v);
      valuesToNodes.put(v.getNodeId(), x);
      return v;
    }

    throw new RuntimeException("Can't convert from Jena node : " + x) ; 

  }

  /**
   * Convert a Mulgara Value to a Jena graph node.
   * @param obj The Mulgara value to convert.
   * @return A new Jena graph node.
   */
  static Node o2n(org.jrdf.graph.Node obj) {
    if (obj == null) return Node.ANY;

    // testing for org.jrdf.graph.URIReference
    if (obj.isURIReference()) {
      URIReference uRef = (URIReference)obj;
      if (skolemizedBlankNodes) {
        String x = uRef.getURI().toString();
        if (x.startsWith(bNodeScheme)) {
          x = x.substring(bNodeScheme.length());
          Node n = Node.createAnon(new AnonId(x));
          return n;
        }
      }
      return Node.createURI(uRef.getURI().toString());
    }

    // testing for org.jrdf.graph.Literal
    if (obj.isLiteral()) {
      Literal literal = (Literal)obj;
      if (literal.getDatatypeURI() != null) {
        RDFDatatype type = TypeMapper.getInstance().getSafeTypeByName(literal.getDatatypeURI().toString()) ;
        return Node.createLiteral(literal.getLexicalForm(), null, type);
      }

      return Node.createLiteral(literal.getLexicalForm(), literal.getLanguage(), null);
    }
          
    if (obj.isBlankNode()) {
      BlankNodeImpl b = (BlankNodeImpl)obj;
      // check if this was a Jena-allocated node
      Node jenaNode = valuesToNodes.get(b);
      // if not known, then create a Jena node from the Mulgara ID
      return jenaNode != null ? jenaNode : Node.createAnon(new AnonId(b.getID()));
    }
    throw new RuntimeException("Can't convert to Jena Node : "+obj) ; 
  }

  /**
   * Expand a constraint expression to include a variable that is prebound to a value.
   * @param c The constraint expression to expand.
   * @param var The variable to be bound.
   * @param value The value to bind the variable to.
   * @return The new constraint expression with the prebound variable.
   */
  static ConstraintExpression constraint(ConstraintExpression c, Variable var, ConstraintElement value) {
    ConstraintExpression c2 = new ConstraintIs(var, value);
    if (c != null) {
      return new ConstraintConjunction(c, c2);
    } else {
      return c2;
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