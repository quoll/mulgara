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
import org.mulgara.util.*;

// Locally written packages


/**
 * Scalable disk-based implementation of BlankNodeMap.
 *
 * @created 2004-10-18
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
public class DiskBlankNodeMap implements BlankNodeMap {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger logger =
      Logger.getLogger(DiskBlankNodeMap.class.getName());

  /** Mapping of the BlankNodeImpl to Long */
  private StringToLongMap nodeToLongMap = null;

  /** Mapping of the Long to BlankNodeImpl */
  private LongToStringMap longToNodeMap = null;


  /**
   * Constructor.
   *
   * @throws GraphException
   */
  public DiskBlankNodeMap() throws GraphException {

    //construct "bi-directional Map"
    try {

      this.nodeToLongMap = new StringToLongMap();
      this.longToNodeMap = new LongToStringMap();
    }
    catch (IOException ioException) {

      throw new GraphException("Failed to create map files.", ioException);
    }
  }

  /**
   * Looks up the node id and returns any BlankNodeImpl associated with it.
   * Returns null if the node is not mapped.
   *
   * @param id long
   * @throws GraphException
   * @return BlankNodeImpl
   */
  public BlankNodeImpl get(long id) throws GraphException {

    try {

      String nodeString = longToNodeMap.get(id);

      return (nodeString == null) ? null : (BlankNodeImpl)BlankNodeImpl.valueOf(nodeString);
    }
    catch (IOException ioException) {

      throw new GraphException("Failed to find node.", ioException);
    }
  }

  /**
   * Looks up the BlankNodeImpl and returns any node id associated with it.
   * Returns -1 if the node is not mapped.
   *
   * @param node BlankNodeImpl
   * @throws GraphException
   * @return long
   */
  public long get(BlankNodeImpl node) throws GraphException {

    //null maps to -1
    if (node == null) {
      return -1;
    }

    try {

      String nodeString = node.toString();
      long id = -1;

      if ((id = nodeToLongMap.get(nodeString)) == 0) {
        return -1;
      }

      return id;
    }
    catch (IOException ioException) {

      throw new GraphException("Failed to find node.", ioException);
    }
  }

  /**
   * Inserts a mapping between a BlankNodeImpl and a node id.
   *
   * @param node BlankNodeImpl
   * @param id long
   * @throws GraphException
   */
  public void put(BlankNodeImpl node, long id) throws GraphException {

    String nodeString = node.toString();
    try {

      longToNodeMap.put(nodeString, id);
      nodeToLongMap.put(nodeString, id);
    }
    catch (IOException ioException) {

      throw new GraphException("Failed to add mapping.", ioException);
    }
  }

  /**
   * Not implemented.
   *
   * @param node BlankNodeImpl
   * @throws GraphException
   */
  public void remove (BlankNodeImpl node) throws GraphException {

    throw new UnsupportedOperationException("Remove(BlankNodeImpl node) not " +
                                            "implemented.");
  }

  /**
   * Not implemented.
   *
   * @param id long
   * @throws GraphException
   */
  public void remove (long id) throws GraphException {

    throw new UnsupportedOperationException("Remove(long id) not implemented.");
  }

  /**
   * Releases any resources held and invalidates the Map.
   *
   * @throws GraphException
   */
  public void close() throws GraphException {

    //clear maps
    try {

      longToNodeMap.delete();
      nodeToLongMap.delete();
    }
    catch (IOException ioException) {

      throw new GraphException("Failed to delete map files.",ioException);
    }

    //invalidate
    longToNodeMap = null;
    nodeToLongMap = null;
  }

}
