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

package org.mulgara.resolver.jrdf;

// Java 2 standard packages
import java.io.*;
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.GraphException;
import org.jrdf.graph.mem.BlankNodeImpl;

// Locally written packages


/**
 * Used to localize and globalize org.jrdf.graph.mem.BlankNodeImpl objects.</p>
 *
 * bi-directional map between org.jrdf.graph.mem.BlankNodeImpl objects
 * and local node id's (long).
 *
 * @created 2004-10-12
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
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
public interface BlankNodeMap {

  /**
   * Looks up the node id and returns any BlankNodeImpl associated with it.
   * Returns null if the node is not mapped.
   *
   * @param id long
   * @throws GraphException
   * @return BlankNodeImpl
   */
  public BlankNodeImpl get(long id) throws GraphException;

  /**
   * Looks up the BlankNodeImpl and returns any node id associated with it.
   * Returns -1 if the node is not mapped.
   *
   * @param node BlankNodeImpl
   * @throws GraphException
   * @return long
   */
  public long get(BlankNodeImpl node) throws GraphException;

  /**
   * Inserts a mapping between a BlankNodeImpl and a node id. Id must be larger
   * than zero.
   *
   * @param node BlankNodeImpl
   * @throws GraphException
   */
  public void put(BlankNodeImpl node, long id) throws GraphException;

  /**
   * Removes the BlankNodeImpl from the Map. Throws an Exception if the Map does
   * not contain the node.
   *
   * @param node BlankNodeImpl
   * @throws GraphException
   */
  public void remove (BlankNodeImpl node) throws GraphException;

  /**
   * Removes the node id from the Map. Throws an Exception if the Map does
   * not contain the node id.
   *
   * @param id long
   * @throws GraphException
   */
  public void remove (long id) throws GraphException;

  /**
   * Releases any resources held and invalidates the Map.
   *
   * @throws GraphException
   */
  public void close() throws GraphException;

}
