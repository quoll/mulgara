/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import static org.mulgara.util.ObjectUtil.eq;


/**
 * A simple Context used for testing purposes.
 * This context returns <code>null</code> for unbound values, since it is globalizing.
 * @created Mar 31, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class TestContext implements Context {
  
  /** The current internal row for the data */
  private int rowNumber = -1;

  /** The names of the columns */
  private List<String> columnNames;

  /** The rows of data. Now row can exceed the size of columnNames */
  private Node[][] rows;

  /** The map for converting IDs into Nodes stored against them */
  private Map<Long,Node> globalizer = new HashMap<Long,Node>();

  /** The map for converting Nodes into the IDs stored against them */
  private Map<Node,Long> localizer = new HashMap<Node,Long>();

  /** The pseudo node-pool counter */
  private long lastNode = 1;

  /**
   * Empty constructor used for tests which have no data.
   */
  public TestContext() {
    columnNames = Collections.emptyList();
    rows = new Node[][] { new Node[] {} };
  }

  /**
   * Creates a test context.
   * @param columnNames The names of the columns for the virtual tuples.
   * @param rows An array of rows, where each row is Node[]. All rows should be
   *   the same width, which is equal to columnNames.length.
   */
  public TestContext(String[] columnNames, Node[][] rows) {
    this.columnNames = Arrays.asList(columnNames);
    this.rows = rows;
    mapGlobalizer(rows);
  }

  /**
   * Reset the position to the start.
   */
  public void beforeFirst() {
    rowNumber = -1;
  }

  /**
   * Move to the next row of data.
   * @return <code>true</code> if the new row contains data, or <code>false</code>
   *   if this object has moved past the last row.
   */
  public boolean next() {
    return ++rowNumber < rows.length;
  }

  /**@see org.mulgara.query.filter.Context#getColumnValue(int) */
  public long getColumnValue(int columnNumber) throws QueryException {
    if (columnNumber >= columnNames.size()) throw new QueryException("Unexpected column: " + columnNumber);
    Node v = rows[rowNumber][columnNumber];
    if (v == null) throw new QueryException("Unbound column: " + columnNumber);
    return localizer.get(v);
  }

  /** @see org.mulgara.query.filter.Context#getInternalColumnIndex(java.lang.String) */
  public int getInternalColumnIndex(String name) {
    return columnNames.contains(name) ? columnNames.indexOf(name) : NOT_BOUND;
  }

  /** @see org.mulgara.query.filter.Context#globalize(long) */
  public Node globalize(long node) throws QueryException {
    Node n = globalizer.get(node);
    if (n == null) throw new QueryException("Unable to globalize id <" + node + ">");
    return n != Null.NULL ? n : null;
  }

  /** @see org.mulgara.query.filter.Context#localize(org.jrdf.graph.Node) */
  public long localize(Node node) throws QueryException {
    Long l = localizer.get(node);
    if (l == null) throw new QueryException("Unable to localize id <" + node + ">");
    return l.longValue();
  }

  /** @see org.mulgara.query.filter.Context#isBound(int) */
  public boolean isBound(int columnNumber) throws QueryException {
    if (columnNumber >= columnNames.size()) throw new QueryException("Unexpected column: " + columnNumber);
    if (rowNumber < 0) throw new QueryException("beforeFirst() called on Context without next()");
    if (rowNumber >= rows.length) throw new QueryException("called next() on Context too often");
    return rows[rowNumber][columnNumber] != null;
  }

  /** @see org.mulgara.query.filter.Context#getUnboundVal() */
  public long getUnboundVal() {
    return 0;
  }

  public boolean equals(Object o) {
    if (!(o instanceof TestContext)) return false;
    TestContext c = (TestContext)o;
    return c == this ||
           eq(columnNames, c.columnNames) &&
           Arrays.deepEquals(rows, c.rows) &&
           eq(globalizer, c.globalizer);
  }

  /**
   * Gets a previously unused node ID.
   * @return a new Node ID.
   */
  private long newNodeId() {
    return lastNode++;
  }

  /**
   * Map node IDs to the nodes, and nodes back to their IDs.
   * @param rows An array of node arrays.
   */
  private void mapGlobalizer(Node[][] rows) {
    for (Node[] row: rows) {
      assert row.length == columnNames.size();
      for (Node v: row) {
        if (v == null) v = Null.NULL;
        Long storedId = localizer.get(v);
        if (storedId == null) {
          storedId = newNodeId();
          globalizer.put(storedId, v);
          localizer.put(v, storedId);
        } else {
          assert globalizer.get(storedId).equals(v) : "Bidirectional mapping for nodes<->ID failed";
        }
      }
    }
  }

  /** Testing class used for storing a symbol for <code>null</code> that is disambiguated from a missing value */
  @SuppressWarnings("serial")
  private static class Null implements Node {
    public static final Null NULL = new Null();
    public int hashCode() { return -1; }
    public String stringValue() { return "null"; }
    public boolean isBlankNode() { return false; }
    public boolean isLiteral() { return false; }
    public boolean isURIReference() { return false; }
  }

}
