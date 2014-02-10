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

package org.mulgara.demo.mp3;

// Java 2 standard packages

// JRDF
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.QueryException;

import java.util.*;
import org.jrdf.util.*;

/**
 * Generic methods for Graph management.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Model {

  /**
   * Initializes the Graph.
   *
   * @param context ModelContext
   * @throws Exception
   */
  public void init(ModelContext context) throws Exception;

  /**
   * Sets the Resource URI that is being represented.
   * @param model URIReference
   */
  public void setResource(URIReference model);

  /**
   * Returns the Resource URI that is being represented.
   * @return URIReference
   */
  public URIReference getResource();

  /**
   * Sets the Graph's type.
   * @param model URIReference
   */
  public void setType(URIReference type);

  /**
   * Returns the Resource URI that represents the Graph's type.
   * @return URIReference
   */
  public URIReference getType();

  /**
   * Returns a ClosableIterator of Triples that are returned from the model
   * using the property-value pairs as constraints. Any null values are
   * replaced by the relevant variable name.
   *
   * @param properties Iterator containing PredicateNodes
   * @param values Iterator containing ObjectNodes (non-Blank)
   * @throws QueryException
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> find(Iterator<PredicateNode> properties,
      Iterator<ObjectNode> values) throws QueryException;

  /**
   * Executes the query and returns the results as a ClosableIterator. Query
   * must return three columns that represent subject, predicate and object.
   *
   * @param query String
   * @throws Exception
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> query(String query) throws Exception;

  /**
   * Creates the Graph with the type set by setType(), or the default Graph
   * type if 'type' null (or not set).
   * @throws QueryException
   */
  public void create() throws QueryException;

  /**
   * Deletes any persistant resources.
   *
   * @throws QueryException
   */
  public void delete() throws QueryException;

  /**
   * Deletes all statements in the Graph.
   *
   * @throws QueryException
   */
  public void clear() throws QueryException;

  /**
   * Frees any resources allocated.
   */
  public void close();

  /**
   * Returns true if the Graph has been initialized.
   * @return boolean
   */
  public boolean hasInitialized();
}
