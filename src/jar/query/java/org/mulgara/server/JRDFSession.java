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

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.*;

/**
 * A JRDF session.
 *
 * @created 2004-10-26
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/07 05:50:42 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface JRDFSession extends Session {

  /**
   * Returns an answer containing a set of statements in the model that match a
   * given triple. A null value for any of the parts of a triple are treated as
   * unconstrained, any values will be returned.
   *
   * @param modelURI URI of the model to be searched
   * @param subject The subject of the triple to find.
   * @param predicate The predicate of the triple to find.
   * @param object The object of the triple to find.
   * @throws GraphException if the model cannot be searched
   * @return Answer containing the triples that match the constraint
   */
  public Answer find(URI modelURI,  SubjectNode subject,
      PredicateNode predicate, ObjectNode object) throws GraphException;

  /**
   * Test the graph for the occurrence of the triple.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param modelURI URI of the model to be searched
   * @param subject The subject of the triple to find.
   * @param predicate The predicate of the triple to find.
   * @param object The object of the triple to find.
   * @return True if the triple is found in the graph, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  public boolean contains(URI modelURI, SubjectNode subject,
      PredicateNode predicate, ObjectNode object) throws GraphException;

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   */
  public long getNumberOfTriples(URI graphURI);
}
