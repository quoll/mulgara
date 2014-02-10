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

package org.mulgara.server;

// Jena
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.Triple;

// JRDF
import org.jrdf.graph.*;

// Internal packages
import org.mulgara.query.*;

/**
 * Converts JRDF objects to Jena objects.
 *
 * @created 2004-02-17
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/01/13 11:45:11 $ by $Author: newmana $
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
public interface JenaFactory {

  /**
   * This is used by the JRDFFactory telling us about a new anonymous resource.
   *
   * @param value The value to map to the node.
   * @param node The node being stored.
   */
  public void addAnon(BlankNode value, Node node);

  /**
   * Converts a JRDF literal to a Jena LiteralLabel.
   *
   * @param literal Literal the literal to convert.
   * @return LiteralLabel the converted Jena Literal Label.
   */
  public LiteralLabel convertLiteralToLabel(Literal literal);

  /**
   * Converts a {@link org.jrdf.graph.ObjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param object the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public Node convertObjectToNode(ObjectNode object);

  /**
   * Converts a {@link org.jrdf.graph.PredicateNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param predicate the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public Node convertPredicateToNode(PredicateNode predicate);

  /**
   * Converts a {@link org.jrdf.graph.SubjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param subject the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public Node convertSubjectToNode(SubjectNode subject);

  /**
   * Converts a {@link org.jrdf.graph.Triple} to a
   * {@link com.hp.hpl.jena.graph.Triple}.
   *
   * @param triple the JRDF triple to convert.
   * @return the converted Jena triple object.
   */
  public Triple convertTriple(org.jrdf.graph.Triple triple);

  /**
   * Converts a {@link org.jrdf.graph.ObjectNode} to a
   * {@link com.hp.hpl.jena.graph.Node}.
   *
   * @param value the JRDF subject node to convert.
   * @return the converted Jena node object.
   */
  public Node convertValueToNode(Value value);

  /**
   * Debug method to see the contents of the internal anonymous node mapping.
   */
  public String dumpAnonNodes();

  /**
   * Returns the JRDF factory.
   *
   * @return the JRDF factory.
   */
  public JRDFFactory getJrdfFactory();

  /**
   * Sets the jrdfFactory.  Cannot be done in the constructor as the
   * jrdfFactory also needs to point to this object.
   *
   * @param jrdfFactory The factory to store.
   */
  public void setJrdfFactory(JRDFFactory jrdfFactory);
}
