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

package org.mulgara.query.rdf;

// Java 2 standard packages
import java.io.Serializable;

// JRDF
import org.jrdf.graph.*;

//Local packages
import org.mulgara.query.Value;

/**
 * RDF blank node.
 *
 * @created 2002-05-22
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2002-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class BlankNodeImpl
    extends AbstractBlankNode
    implements Comparable<Node>, BlankNode, Value, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 8304792420513868988L;

  /** The unique node id for the blank node. */
  private long nodeId;

  private String stringValue;

  /** The mask that can remove the BLANK_NODE_BIT on nodes that use it. */
  static final private long COUNTER_MASK = 0x3FFFFFFFFFFFFFFFL;

  /** The bit that indicates a blank node on nodes that use it. */
  static final long BLANK_NODE_BIT = 0x4000000000000000L;

  /** The label used for printing blank nodes. */
  static final public String LABEL = "node";

  /** The label used for printing blank nodes. */
  static final private String _LABEL = "_node";

  /**
   * Create an empty blank node.  Just a place holder.
   */
  public BlankNodeImpl() {
    // Do nothing
    this(0);
    stringValue = LABEL + "0";
  }

  /**
   * Create an RDF blank node
   *
   * @param newNodeId the unique node id.
   */
  public BlankNodeImpl(long newNodeId) {
    nodeId = newNodeId;
    stringValue = LABEL + printable(nodeId);
  }


  /**
   * Returns the internal node id of the blank node.
   *
   * @return node id.
   */
  public long getNodeId() {
    return nodeId;
  }


  /**
   * Associates this blank node with an internal node id.
   */
  public void setNodeId(long nodeId) {
    this.nodeId = nodeId;
    stringValue = LABEL + printable(nodeId);
  }


  /**
   * Gives Literals, URIReference and BlankNodes an order.
   *
   * @param object the RDF object to compare.
   * @return -1 if Literal or URIReference otherwise performance comparTo.
   */
  public int compareTo(Node object) {
    if (object instanceof Literal) {
      return -1;
    } else if (object instanceof URIReference) {
      return -1;
    } else if (object instanceof BlankNode) {
      // FIXME: this is ugly and I sincerely hope we can figure out a better
      //        way to implement comparisons by Java reference value
      return toString().compareTo(((BlankNode)object).toString());
    } else {
      throw new ClassCastException("Not an RDF node");
    }
  }

  /**
   * Compare node for equality.
   *
   * @param obj The object to compare against.
   * @return True if the object evaluates as an equivalent blank node.
   */
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    } else if (obj == this) {
      return true;
    } else if (obj instanceof BlankNodeImpl) {
      return this.nodeId != 0 && this.nodeId == ((BlankNodeImpl)obj).nodeId;
    } else {
      return super.equals(obj);
    }
  }

  /** @see org.jrdf.graph.AbstractBlankNode#hashCode() */
  public int hashCode() {
    return ((int)(nodeId >> 32) * 23) ^ ((int)(0xFFFF & nodeId));
  }

  /**
   * Provide a legible representation of the blank node.  Returns "node" and
   * the node id.
   *
   * @return the string value of the uri and node id.
   */
  public String toString() {
    return _LABEL + printable(nodeId);
  }

  public String getID() {
    return stringValue;
  }

  /** Strips off the blank node bit for blank nodes that use it. */
  private long printable(long l) {
    return l > 0 ? COUNTER_MASK & l : l;
  }

  /**
   * Duplicate of the BlankNodeAllocator utility to convert a blank node code to a counter value.
   * @param counter The blank node value, with the blank node bit turned off.
   * @return A value with the blank node bit turned on.
   */
  public static final long counterToNode(long counter) {
    return counter | BLANK_NODE_BIT;
  }
}
