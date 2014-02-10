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

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.relational.d2rq.Constants;
import org.mulgara.resolver.relational.d2rq.D2RQDefn;
import org.mulgara.resolver.relational.d2rq.ClassMapElem;
import org.mulgara.resolver.relational.d2rq.PropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.DatabaseElem.DBType;
import static org.mulgara.resolver.relational.d2rq.DatabaseElem.DBType.*;

public abstract class VariableDesc {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(VariableDesc.class.getName());

  protected Map<String,URIReference> columnTypeMap;
  protected D2RQDefn defn;
  protected List<String> join;
  protected List<String> condition;

  protected VariableDesc(ClassMapElem defn) {
    this((D2RQDefn)defn);
    this.addCondition(defn.condition);
  }

  protected VariableDesc(PropertyBridgeElem defn) {
    this((D2RQDefn)defn);
    this.addJoin(defn.join);
    this.addCondition(defn.condition);
  }

  protected VariableDesc(D2RQDefn defn) {
    this.defn = defn;
    this.columnTypeMap = defn.getColumnTypeMap();
    this.join = new ArrayList<String>();
    this.condition = new ArrayList<String>();
  }

  public abstract Node getNode(ResultSet result) throws SQLException, TuplesException;
  public abstract void assignColumnIndex(String column, int index);
  public abstract Set<String> getTables();
  public abstract Set<String> getColumns();

  public void addJoin(List<String> join) {
    this.join.addAll(join);
  }

  public List<String> getJoin() {
    return join;
  }

  public void addCondition(List<String> condition) {
    this.condition.addAll(condition);
  }

  public List<String> getCondition() {
    return condition;
  }

  /**
   * Returns an SQL SELECT query fragment that will effectively constraint the given variable
   * to the specified rdf-space value.
   */
  public abstract String restrict(String rdfValue);


  /**
   * Encodes a value for inclusion in an sql query based on type.
   */
  public String encode(String column, String rdfValue, DBType dbType) {
    Object type = columnTypeMap.get(column);
    if (type == Constants.numericColumn) {
      return rdfValue;
    }
    if (type == Constants.textColumn) {
      return "'" + rdfValue + "'";
    }
    if (type == Constants.dateColumn) {
      if (dbType == oracle) {
        return "TO_CHAR(TO_DATE('" + rdfValue + "'),'YYYY-MM-DD')";
      } else {
        return "date('" + rdfValue + "')";
      }
    }

    logger.warn("Column: " + column + " untyped in definition, unable to guarantee encoding for " + rdfValue);

    return rdfValue;
  }
}
