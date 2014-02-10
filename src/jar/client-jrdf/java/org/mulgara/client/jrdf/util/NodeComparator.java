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

package org.mulgara.client.jrdf.util;

// Java 2 standard packages
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;

/**
 * Comparator used to compare two JRDF Nodes. Position in the Triple is ignored.
 * eg. A Subject Node can be equal to a Predicate Node if both nodes are equal.
 * Compares Nodes by their Lexical value.
 *
 * <p>If Nodes are of different types, they are ordered by:
 * <ol>
 *   <li>URIReference</li>
 *   <li>Literal</li>
 *   <li>BlankNode</li>
 * </ol>
 *
 * @created 2004-08-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:37 $
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
public class NodeComparator<T> implements Comparator<T> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(NodeComparator.class.getName());

  /** Value of a URIReference when compared to another type */
  private static final int URI_NODE_VALUE = 3;

  /** Value of a Literal when compared to another type */
  private static final int LITERAL_NODE_VALUE = 2;

  /** Value of a BlankNode when compared to another type */
  private static final int BLANK_NODE_VALUE = 1;

  /**
   * Compares two JRDF nodes. Node must be an URIReference, Literal or
   * BlankNode.
   *
   * @param node1 Object
   * @param node2 Object
   * @return int
   */
  public int compare(T node1, T node2) {

    //validate
    if (! (node1 instanceof Node)) {

      throw new IllegalArgumentException("'node1' is not a JRDF Node. node1: " + node1);
    }

    if (! (node2 instanceof Node)) {

      throw new IllegalArgumentException("'node2' is not a JRDF Node. node2: " + node1);
    }

    return this.nodeCompare( (Node) node1, (Node) node2);
  }

  /**
   * Compares two JRDF Nodes.
   *
   * @param node1 Node
   * @param node2 Node
   * @return int
   */
  protected int nodeCompare(Node node1, Node node2) {

    //value to be returned
    int compare = 0;

    //get the type values
    int nodeValue1 = this.nodeTypeValue(node1);
    int nodeValue2 = this.nodeTypeValue(node2);

    //if they are different types, compare by type
    if (nodeValue1 != nodeValue2) {

      compare = nodeValue1 - nodeValue2;
    } else {

      //determine types and call appropriate method
      if (nodeValue1 == URI_NODE_VALUE) {

        compare = this.nodeCompare( (URIReference) node1, (URIReference) node2);
      } else if (nodeValue1 == LITERAL_NODE_VALUE) {

        compare = this.nodeCompare( (Literal) node1, (Literal) node2);
      } else if (nodeValue1 == BLANK_NODE_VALUE) {

        compare = this.nodeCompare( (BlankNode) node1, (BlankNode) node2);
      } else {

        //should not get here...
        throw new IllegalStateException("Invalid Node type: " +
                                        node1.getClass().getName());
      }
    }

    return compare;
  }

  /**
   * Compares an URI Reference to another URI Reference using the lexical value
   * of it's URI.
   *
   * @param node1 URIReference
   * @param node2 URIReference
   * @return int
   */
  private int nodeCompare(URIReference node1, URIReference node2) {

    //convert to String
    String uri1 = node1.getURI().toString();
    String uri2 = node2.getURI().toString();

    return this.nodeCompare(uri1, uri2);
  }

  /**
   * Compares a BlankNode to another BlankNode using it's String value.
   *
   * @param node1 BlankNode
   * @param node2 BlankNode
   * @return int
   */
  private int nodeCompare(BlankNode node1, BlankNode node2) {

    //convert to String
    String string1 = node1.toString();
    String string2 = node2.toString();

    return this.nodeCompare(string1, string2);
  }

  /**
   * Compares two Literals by their Lexical value.
   *
   * @param node1 Literal
   * @param node2 Literal
   * @return int
   */
  private int nodeCompare(Literal node1, Literal node2) {

    //convert to String
    String string1 = node1.getLexicalForm();
    String string2 = node2.getLexicalForm();

    return this.nodeCompare(string1, string2);
  }

  /**
   * Compares two Strings.
   *
   * @param string1 String
   * @param string2 String
   * @return int
   */
  private int nodeCompare(String string1, String string2) {

    return string1.compareTo(string2);
  }

  /**
   * Returns the ordering value of the Node. Node must be an URIReference,
   * Literal or BlankNode.
   *
   * @param node Node
   * @return int
   */
  private int nodeTypeValue(Node node) {

    //validate
    if (node == null) {

      throw new IllegalArgumentException("Node argument is null.");
    }

    //return type code / type order
    if (node instanceof URIReference) {

      return URI_NODE_VALUE;
    }
    else if (node instanceof Literal) {

      return LITERAL_NODE_VALUE;
    }
    else if (node instanceof BlankNode) {

      return BLANK_NODE_VALUE;
    }
    else {

      throw new IllegalArgumentException("Node must be of type: URIReference, " +
                                         "Literal or BlankNode. Node: " + node);
    }
  }

  /**
   * returns true if this Comparator is equal to another.
   *
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj) {

    return false;
  }
}
