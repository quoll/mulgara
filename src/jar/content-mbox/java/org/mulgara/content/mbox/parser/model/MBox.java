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

package org.mulgara.content.mbox.parser.model;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

import org.jrdf.graph.Graph;
import org.jrdf.graph.Triple;

import org.mulgara.content.NotModifiedException;
import org.mulgara.content.mbox.parser.model.exception.InvalidMBoxException;
import org.mulgara.content.mbox.parser.model.exception.ModelException;
import org.mulgara.content.mbox.parser.model.exception.VocabularyException;

/**
 * MBox interface which is used to represent an MBox along with its messages and
 * RDF statements.  In addition to being the storage of an MBox, it also allows
 * for navigation of the parsed RDF statements.
 *
 * @created 2004-08-24
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.7 $
 *
 * @modified $Date: 2005/01/05 04:57:42 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface MBox {

  /**
   * Retrieves the mbox file location as an URI.
   *
   * @return The location of the mbox as an URI
   */
  public URI getURI();

  /**
   * Returns the mbox as a stream object.
   *
   * @return The mbox as a stream object
   */
  public InputStream getMBoxStream() throws IOException, NotModifiedException;

  /**
   * Retrieves the graph object that the mbox is using to store its content.
   *
   * @return The graph object that the mbox is using to store its content
   */
  public Graph getGraph();

  /**
   * Sets the graph object the mbox should be using to store its content.
   *
   * @param graph The graph object the mbox should use to store its content
   */
  public void setGraph(Graph graph);

  /**
   * Retrieves the next triple in the mbox.  If there are no triples left to
   * navigate through then null is returned.
   *
   * @return The next triple in the mbox navigation cycle
   *
   * @throws ModelException
   */
  public Triple nextTriple() throws ModelException;

  /**
   * Resets the navigation of the mbox to return to the start of the file and
   * clears the graph, ready for starting again.
   *
   * @throws ModelException
   */
  public void reset() throws ModelException;

  /**
   * Begins the parsing of an mbox and sets up the navigation to be ready for
   * iterating through the statements of the mbox.
   *
   * @throws ModelException
   * @throws InvalidMBoxException
   * @throws VocabularyException
   */
  public void start() throws ModelException, InvalidMBoxException, VocabularyException, NotModifiedException;

  /**
   * Clears all triples from the graph.
   *
   * @throws ModelException
   */
  public void clear() throws ModelException;
}
