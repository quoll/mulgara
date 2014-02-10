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

// Base Java
import java.net.URISyntaxException;

// Jena
import com.hp.hpl.jena.graph.Node;

// JRDF
import org.jrdf.graph.*;

/**
 * Converts Jena objects to JRDF objects.
 *
 * @created 2004-02-17
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/01/07 09:37:25 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface JRDFFactory {

  /**
   * This is used by the JenaFactory telling us about a new anonymous resource.
   *
   * @param node the node to map to the subjectNode.
   * @param subjectNode The subjectNode being stored.
   */
  public void addAnon(Node node, SubjectNode subjectNode);

  /**
   * Add an anonymous JRDF node.
   *
   * @param key the anonymous Jena node.  The string for this node is stored, but
              the node is needed to supply to Jena
   * @param value the JRDF node value to map it to.
   */
  public void addNode(Node key, BlankNode value);

  /**
   * Converts a Jena Graph node to JRDF object node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryException if it failed to create the resource,
   *   literal or blank node.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF object node.
   */
  public ObjectNode convertNodeToObject(Graph databaseGraph,
      Node node) throws GraphElementFactoryException, URISyntaxException;

  /**
   * Converts a Jena Graph node to JRDF predicate node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryException if it failed to create the resource.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF subject node.
   */
  public PredicateNode convertNodeToPredicate(Graph databaseGraph,
      Node node) throws GraphElementFactoryException, URISyntaxException;

  /**
   * Converts a Jena Graph node to JRDF subject node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryExcpetion if it failed to create the resource
   *   or blank node.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF subject node.
   */
  public SubjectNode convertNodeToSubject(Graph databaseGraph,
      Node node) throws GraphElementFactoryException, URISyntaxException;

  /**
   * Retrieve an anonymous JRDF node.
   *
   * @param key String the unique string identifier of the anonymous JRDF node.
   * @throws GraphElementFactoryException if it failed to create the resource,
   *   literal or blank node.
   * @return Node that the JRDF node value is mapped to, or null if it is not
   *   yet mapped.
   */
  public BlankNode getNode(Node key) throws GraphElementFactoryException;

  /**
   * Returns true if the given key has been mapped to a JRDF node.
   *
   * @param key the unique string identifier of an anonymous JRDF node.
   * @return true if the given key has been mapped to a JRDF node.
   */
  public boolean hasNode(Node key);
}
