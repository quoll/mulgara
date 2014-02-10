/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

import java.util.*;

// Third party packages
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.AnonId;

// Log4J
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;

// Locally written classes.
import org.mulgara.query.*;
import org.mulgara.server.JRDFFactory;
import org.mulgara.server.JenaFactory;

/**
 * Converts JRDF objects to Jena objects.
 *
 * @created 2004-02-17
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/07 09:37:25 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JenaFactoryImpl implements JenaFactory {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger =
      Logger.getLogger(JenaFactoryImpl.class.getName());

  private Hashtable<BlankNode,com.hp.hpl.jena.graph.Node> anonMap = new Hashtable<BlankNode,com.hp.hpl.jena.graph.Node>();

  /**
   * A link to the JRDFFactory peer.
   */
  private JRDFFactory jrdfFactory = null;

  /**
   * Sets the jrdfFactory.  Cannot be done in the constructor as the
   * jrdfFactory also needs to point to this object.
   *
   * @param jrdfFactory The factory to store.
   */
  public void setJrdfFactory(JRDFFactory jrdfFactory) {
    this.jrdfFactory = jrdfFactory;
  }

  /**
   * Returns the JRDF factory.
   *
   * @return the JRDF factory.
   */
  public JRDFFactory getJrdfFactory() {
    return jrdfFactory;
  }

  /**
   * Converts a {@link org.jrdf.graph.Triple} to a
   * {@link com.hp.hpl.jena.graph.Triple}.
   *
   * @param triple the JRDF triple to convert.
   * @return the converted Jena triple object.
   */
  public com.hp.hpl.jena.graph.Triple convertTriple(
      org.jrdf.graph.Triple triple) {

    com.hp.hpl.jena.graph.Node s = convertSubjectToNode(triple.getSubject());
    com.hp.hpl.jena.graph.Node p = convertPredicateToNode(triple.getPredicate());
    com.hp.hpl.jena.graph.Node o = convertObjectToNode(triple.getObject());

    return new com.hp.hpl.jena.graph.Triple(s, p, o);
  }

  /**
   * Converts a {@link org.jrdf.graph.SubjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param subject the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public com.hp.hpl.jena.graph.Node convertSubjectToNode(SubjectNode subject) {

    com.hp.hpl.jena.graph.Node s;

    if (subject == null) {

      s = Node_Variable.ANY;
    }
    else if (subject instanceof URIReference) {

      s = com.hp.hpl.jena.graph.Node.createURI((
          (URIReference) subject).getURI().toString());
    }
    else {

      s = getAnon((BlankNode) subject);
    }

    return s;
  }

  /**
   * Converts a {@link org.jrdf.graph.PredicateNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param predicate the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public com.hp.hpl.jena.graph.Node convertPredicateToNode(
      PredicateNode predicate) {

    com.hp.hpl.jena.graph.Node p;

    if (predicate == null) {

      p = Node_Variable.ANY;
    }
    else if (predicate instanceof URIReference) {

      p = com.hp.hpl.jena.graph.Node.createURI((
           (URIReference) predicate).getURI().toString());
    }
    else {

      //should not get here!
      throw new IllegalArgumentException("PredicateNode: '" + predicate + "' " +
                                         "is not of type URIReference.");
    }

    return p;
  }

  /**
   * Converts a {@link org.jrdf.graph.ObjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param object the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public com.hp.hpl.jena.graph.Node convertObjectToNode(
      ObjectNode object) {

    com.hp.hpl.jena.graph.Node o;

    if (object == null) {

      o = Node_Variable.ANY;
    }
    else if (object instanceof URIReference) {

      o = com.hp.hpl.jena.graph.Node.createURI((
          (URIReference) object).getURI().toString());
    }
    else if (object instanceof Literal) {

      LiteralLabel label = convertLiteralToLabel((Literal) object);
      o = com.hp.hpl.jena.graph.Node.createLiteral(label);
    }
    else {

      o = getAnon((BlankNode) object);
    }

    return o;
  }

  /**
   * Converts a {@link org.jrdf.graph.ObjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param value the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public com.hp.hpl.jena.graph.Node convertValueToNode(Value value) {

    com.hp.hpl.jena.graph.Node o;

    if (value == null) {

      o = Node_Variable.ANY;
    }
    else if (value instanceof URIReference) {

      o = com.hp.hpl.jena.graph.Node.createURI((
          (URIReference) value).getURI().toString());
    }
    else if (value instanceof Literal) {

      LiteralLabel label = convertLiteralToLabel((Literal) value);
      o = com.hp.hpl.jena.graph.Node.createLiteral(label);
    }
    else {

      o = getAnon((BlankNode) value);
    }

    return o;
  }

  /**
   * Converts a JRDF literal to a Jena LiteralLabel.
   *
   * @param literal Literal the literal to convert.
   * @return LiteralLabel the converted Jena Literal Label.
   */
  public com.hp.hpl.jena.graph.impl.LiteralLabel convertLiteralToLabel(
      Literal literal) {

    if (literal.getDatatypeURI() != null) {

      RDFDatatype type = new BaseDatatype(literal.getDatatypeURI().toString());
      return new LiteralLabel(literal.getLexicalForm(), literal.getLanguage(),
          type);
    }
    else {

      return new LiteralLabel(literal.getLexicalForm(), literal.getLanguage());
    }
  }


  /**
   * Retrieve the anonymous node for a value, creating a new one if none exists
   * for it yet.
   *
   * @param value The value to retrieve the anonymous node for.
   * @return An anonymous node which is unique for the value.
   */
  private com.hp.hpl.jena.graph.Node getAnon(BlankNode value) {

    com.hp.hpl.jena.graph.Node n = anonMap.get(value);

    if (n == null) {
      String nodeId = ((BlankNode)value).toString();
      n = com.hp.hpl.jena.graph.Node.createAnon(new AnonId(nodeId));
      addAnon((BlankNode)value, n);
      // tell the JrdfFactory as well
      jrdfFactory.addAnon(n, (BlankNode) value);
    }
    return n;
  }


  /**
   * This is used by the JRDFFactory telling us about a new anonymous resource.
   *
   * @param value The value to map to the node.
   * @param node The node being stored.
   */
  public void addAnon(BlankNode value, com.hp.hpl.jena.graph.Node node) {
    anonMap.put(value, node);
  }


  /**
   * Debug method to see the contents of the internal anonymous node mapping.
   */
  public String dumpAnonNodes() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<BlankNode,com.hp.hpl.jena.graph.Node> e: anonMap.entrySet()) {
      sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
    }
    return sb.toString();
  }

}
