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

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import org.jrdf.graph.Node;

import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.relational.d2rq.D2RQDefn;
import org.mulgara.resolver.relational.d2rq.DB2XSD;
import org.mulgara.util.StackTrace;

public class LiteralDesc extends VariableDesc {
  private static final Logger logger = Logger.getLogger(LiteralDesc.class);

  private int index = -1;
  // p used for debugging purposes.
  private Variable p;

  private StackTrace t;

  public LiteralDesc(D2RQDefn defn, Variable p) {
    super(defn);
    this.p = p;
  }

  public Node getNode(ResultSet result) throws SQLException, TuplesException {
    // We call trim because derby pads literal columns.  I don't know if that's a bug
    // but it does stuff up the URI.
    String value = result.getString(index + 1).trim();

    try {
      return new URIReferenceImpl(new URI(value));
    } catch (Exception eu) {
      URI dataType = DB2XSD.get(result.getMetaData().getColumnType(index + 1));
      return (dataType == null) ? new LiteralImpl(value) :new LiteralImpl(value, dataType) ;
    }
  }

  public String restrict(String rdfValue) {
    throw new IllegalStateException("Can't restrict literal");
  }

  public Set<String> getTables() {
    return Collections.emptySet();
  }

  public Set<String> getColumns() {
    return Collections.emptySet();
  }

  public String getTable() {
    return null;
  }

  public String getColumn() {
    return null;
  }

  public void assignColumnIndex(String column, int index) {
    if (column != null) {
      throw new IllegalArgumentException("Literal descriptions have no column.: " + column + ", p=" + p);
    }
    
    if (this.index != -1 && this.index != index) {
      logger.warn("Reassigning index will fail, was assigned:\n" + this.t);
      logger.warn("Reassigning index will fail, now assigned:\n" + new StackTrace());
      throw new IllegalArgumentException("Index assigned multiple values in union.  Non-union compatible selects formed. Old index = " + this.index + ", New index = " + index + ", p=" + p);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Assigning index to LiteralDesc: " + System.identityHashCode(this) + "=" + index);
    }
    this.index = index;
    this.t = new StackTrace();
  }

  public String toString() {
    return "LiteralDesc@" + System.identityHashCode(this) + "(p=" + p + ")";
  }
}
