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

// Java 2 standard packages
import java.lang.ref.SoftReference;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;    // Apache Log4J
import org.jrdf.graph.Literal;       // JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.query.Order;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;

/**
 * Row comparator for implementing the iTQL <code>ORDER BY</code> clause.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2003-02-21
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2003-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class OrderByRowComparator implements RowComparator {

  protected static class Cache extends LinkedHashMap {
    public static final int MAXSIZE = 100000;
    public static final float LOAD_FACTOR = 0.75F;
    public static final int INITIAL_SIZE = 133334; // ceil(MAXSIZE / LOAD_FACTOR + 1)

    public Cache() {
      super(INITIAL_SIZE, LOAD_FACTOR, true);
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
      return size() > MAXSIZE;
    }

    public void put(long key, Object value) {
      super.put(new Long(key), value);
    }

    public Object get(long key) {
      return super.get(new Long(key));
    }
  }

  /** Logger. */
  private static final Logger logger =
      Logger.getLogger(OrderByRowComparator.class);

  /**
   * The index corresponds to positions in the <code>ORDER BY</code> clause; the
   * indexed value corresponds to column position in the {@link Tuples} being
   * ordered.
   */
  private int[] columnMap;

  /**
   * The index corresponds to positions in the <code>ORDER BY</code> clause; the
   * indexed value is +1 for ascending order and -1 for descending.
   */
  private int[] directionMap;

  /** Description of the Field */
  private List orderList;

  /** Description of the Field */
  private ResolverSession session;

  private Cache globalNodeCache;

  //
  // Constructor
  //

  /**
   * Construct a {@link RowComparator} for a {@link Tuples} that will satisfy an
   * <code>ORDER BY</code> clause.
   *
   * @param tuples PARAMETER TO DO
   * @param orderList the <code>ORDER BY</code> clause
   * @param session the session used to globalize values for comparison;
   * @throws QueryException EXCEPTION TO DO
   */
  OrderByRowComparator(Tuples tuples, List orderList, ResolverSession session)
      throws QueryException
  {
    this.orderList = orderList;
    this.session = session;

    columnMap = new int[orderList.size()];
    directionMap = new int[orderList.size()];
    globalNodeCache = new Cache();

    // Populate the columnMap array
    int n = 0;

    for (Iterator i = orderList.iterator(); i.hasNext(); n++) {
      Order order = (Order) i.next();
      try {
        columnMap[n] = tuples.getColumnIndex(order.getVariable());
        directionMap[n] = order.isAscending() ? ( -1) : ( +1);
      } catch (TuplesException e) {
        throw new QueryException("Can't order by " + order.getVariable(), e);
      }
    }
  }

  //
  // Methods implementing the RowComparator interface
  //

  public boolean equals(Object o) {
    return (o instanceof RowComparator) && equals((RowComparator)o);
  }

  /**
   * Test if this is equal to another RowComparator.
   *
   * @param r The other comparator
   * @return true if both comparators are the same
   */
  public boolean equals(RowComparator r)
  {
    // Require matching class
    if (r.getClass() != OrderByRowComparator.class) {
      return false;
    }

    // Require matching field values
    OrderByRowComparator obrc = (OrderByRowComparator) r;

    return orderList.equals(obrc.orderList) && (session == obrc.session) &&
        Arrays.equals(columnMap, obrc.columnMap);
  }

  public int hashCode() {
    return columnMap.hashCode() + orderList.hashCode() * 5 + session.hashCode() * 7;
  }

  /**
   * METHOD TO DO
   *
   * @param first PARAMETER TO DO
   * @param second PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(Tuples first, Tuples second) throws TuplesException
  {
    for (int j = 0; j < columnMap.length; j++) {
      int comparison = directionMap[j] * columnCompare(first.getColumnValue(columnMap[j]),
          second.getColumnValue(columnMap[j]));
      if (comparison != 0) {
        return comparison;
      }
    }

    return 0;
  }

  /**
   * METHOD TO DO
   *
   * @param array PARAMETER TO DO
   * @param tuples PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(long[] array, Tuples tuples) throws TuplesException
  {
    for (int j = 0; j < columnMap.length; j++) {
      int comparison = directionMap[j] * columnCompare(array[columnMap[j]],
          tuples.getColumnValue(columnMap[j]));
      if (comparison != 0) {
        return comparison;
      }
    }

    return 0;
  }

  /**
   * METHOD TO DO
   *
   * @param first PARAMETER TO DO
   * @param second PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(long[] first, long[] second) throws TuplesException
  {
    for (int j = 0; j < columnMap.length; j++) {
      int comparison = directionMap[j] * columnCompare(first[columnMap[j]],
          second[columnMap[j]]);
      if (comparison != 0) {
        return comparison;
      }
    }

    return 0;
  }

  //
  // Internal methods
  //

  /**
   * METHOD TO DO
   *
   * @param lhs PARAMETER TO DO
   * @param rhs PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws StoreException if the nodes to be compared can't be globalized
   */
  private int columnCompare(long lhs, long rhs) throws TuplesException
  {
    int result;

    if (lhs == Tuples.UNBOUND && rhs == Tuples.UNBOUND) {
      return 0;
    } else if (lhs == Tuples.UNBOUND) {
      return -1;
    } else if (rhs == Tuples.UNBOUND) {
      return +1;
    }

    Comparable lhsComparable;
    try {
      lhsComparable = valueToComparable(globalize(lhs));
    } catch (GlobalizeException e) {
      throw new TuplesException("Couldn't globalize local LHS node "+lhs, e);
    }

    Comparable rhsComparable;
    try {
      rhsComparable = valueToComparable(globalize(rhs));
    } catch (GlobalizeException e) {
      throw new TuplesException("Couldn't globalize local RHS node "+rhs, e);
    }

    if (rhsComparable != null) {
      if (lhsComparable != null) {
        Class lhsClass = lhsComparable.getClass();
        Class rhsClass = rhsComparable.getClass();

        result = (lhsClass == rhsClass)
                  ? ( -lhsComparable.compareTo(rhsComparable))
                  : ( - lhsClass.getName().compareTo(rhsClass.getName()));
      } else {
        result = -1;
      }
    } else {
      result = (lhsComparable == null) ? 0 : 1;
    }

    return result;

    /* The following code uses the SPObject collation order
         try {
      SPObject lhsSPObject = session.getStringPool().findSPObject(lhs);
      SPObject rhsSPObject = session.getStringPool().findSPObject(rhs);
      int comparison = lhsSPObject.compareTo(rhsSPObject);
      logger.debug("Comparing node "+lhs+" ("+lhsSPObject+") to node "+rhs+
                  " ("+rhsSPObject+"): "+comparison);
      return comparison;
         }
         catch (StringPoolException e) {
      logger.warn("Couldn't compare node "+lhs+" to node "+rhs);
      return 0;
         }
     */
  }


  private Node globalize(long localNode) throws GlobalizeException
  {
    // log what we're doing
    if (logger.isDebugEnabled()) {
      logger.debug("Finding literal for node " + localNode);
    }

    Node value = null;
    SoftReference ref = (SoftReference)globalNodeCache.get(localNode);
    if (ref != null) {
      value = (Node)ref.get();
    }
    if (value == null) {
      value = session.globalize(localNode);
      globalNodeCache.put(localNode, new SoftReference(value));
    }

    return value;
  }


  /**
  * @param value a local node
  * @return a {@link Comparable} usable for row ordering, or <code>null</code>
  *      if a no such value could be obtained
  */
  private static Comparable valueToComparable(Node value)
  {
    if (value == null) {
      return null;
    }

    Node rdfNode = value;

    if (rdfNode instanceof Literal) {
      String text = ((Literal) rdfNode).getLexicalForm();

      if (logger.isDebugEnabled()) logger.debug("Checking if " + text + " is a number");

      if (text.length() > 0) {
        char ch = text.charAt(0);

        // if it smells like a numeric (and not a DateTime)
        if (((ch >= '0' && ch <= '9') || ch == '.' || ch == '+' || ch == '-') && text.indexOf('T') < 0) {
          // The floating parser accepts numbers ending in d or f, but we don't
          String num = text.trim();
          ch = num.charAt(num.length() - 1);
          if (ch != 'f' && ch != 'F' && ch != 'd' && ch != 'D') {
            try {
              // try to return as a number, using autoboxing
              return Float.parseFloat(num);
            } catch (NumberFormatException ex) { /* not a number - fall through */ }
          }
        }
      }

      return text;
    } else if (rdfNode instanceof URIReference) {
      return ((URIReference) rdfNode).getURI().toString();
    } else if (rdfNode instanceof BlankNode) {
      return ((BlankNode) rdfNode).toString();
    } else {
      throw new Error("Unknown global type for " + rdfNode + " (" + rdfNode.getClass() + ")");
    }
  }
}
