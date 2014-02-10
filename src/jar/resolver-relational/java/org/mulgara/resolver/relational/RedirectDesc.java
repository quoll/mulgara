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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.jrdf.graph.Node;

import org.mulgara.query.TuplesException;

public class RedirectDesc extends VariableDesc {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RedirectDesc.class);

  private List<VariableDesc> descs;
  private int index;

  public RedirectDesc(VariableDesc desc) {
    super(desc.defn);
    this.descs = new ArrayList<VariableDesc>();
  }

  /**
   * @return index of desc for inclusion in query
   */
  public int addVariableDesc(VariableDesc desc) {
    descs.add(desc);

    return descs.size() - 1;
  }

  public Node getNode(ResultSet result) throws SQLException, TuplesException {
    // Do translation.
    int descIndex = result.getInt(index + 1);

    if (descIndex >= descs.size()) {
      throw new TuplesException("Attempt to index redirect out of bounds descIndex=" + descIndex + " list=" + descs);
    }

    VariableDesc desc = (VariableDesc)descs.get(descIndex);

    return desc.getNode(result);
  }


  public String restrict(String rdfValue) {
    throw new IllegalStateException("Can't restrict using a RedirectDesc");
  }

  public Set<String> getTables() {
    throw new IllegalStateException("RedirectDesc has no tables");
  }

  public Set<String> getColumns() {
    throw new IllegalStateException("RedirectDesc has no columns");
  }

  public String getTable() {
    throw new IllegalStateException("RedirectDesc has no table");
  }

  public String getColumn() {
    throw new IllegalStateException("RedirectDesc has no column");
  }

  public void assignColumnIndex(String column, int index) {
    if (column != null) {
      throw new IllegalArgumentException("Column provided to RedirectDesc::assignColumnIndex, should be null: " + column);
    }

    this.index = index;
  }


  public String toString() {
    return "RedirectDesc:(" +
        "index=" + index + ", " +
        "descs=" + descs + ")";
  }
}
