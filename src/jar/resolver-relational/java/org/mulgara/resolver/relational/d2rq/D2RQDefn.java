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
package org.mulgara.resolver.relational.d2rq;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.QueryException;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.Variable;
import org.mulgara.query.LocalNode;
import org.mulgara.query.Value;
import org.mulgara.store.tuples.Tuples;


public abstract class D2RQDefn {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(D2RQDefn.class.getName());

  protected final Resolver resolver;
  protected final ResolverSession session;
  
  // Maps columns to sql datatypes.
  protected Map<String,URIReference> columnTypeMap;

  public D2RQDefn(Resolver resolver, ResolverSession session, D2RQDefn parent) {
    this.resolver = resolver;
    this.session = session;
    this.columnTypeMap = parent.columnTypeMap;
  }

  /**
   * Should only be called by top-level D2RQDefn.
   */
  protected D2RQDefn(Resolver resolver, ResolverSession session) {
    this.resolver = resolver;
    this.session = session;
    this.columnTypeMap = null;
  }

  protected void initColumnTypeMap(Map<String,URIReference> typeMap) {
    if (columnTypeMap != null) {
      throw new IllegalStateException("Only parent node may initialise type map, and only once");
    }
    columnTypeMap = typeMap;
  }

  public Map<String,URIReference> getColumnTypeMap() {
    return columnTypeMap;
  }

  protected String getStringObject(Value subj, Value pred, Value model, boolean optional) throws LocalizeException, TuplesException, GlobalizeException, QueryException {

    LocalNode ln = getLocalNodeObject(subj, pred, model, optional);
    if (ln == null) {
      return null;
    }

    Node obj = session.globalize(ln.getValue());
    if (obj instanceof Literal) {
      return ((Literal)obj).getLexicalForm();
    } if (obj instanceof URIReference) {
      return ((URIReference)obj).getURI().toString();
    } else {
      throw new TuplesException("Invalid node type in statement: " + obj + " :: " + obj.getClass());
    }
  }

  protected List<String> getStringObjects(Value subj, Value pred, Value model) throws LocalizeException, TuplesException, GlobalizeException, QueryException {

    long[] res = getObjects(
        (subj instanceof LocalNode) ? ((LocalNode)subj).getValue() : session.localize((Node)subj),
        (pred instanceof LocalNode) ? ((LocalNode)pred).getValue() : session.localize((Node)pred),
        (model instanceof LocalNode) ? ((LocalNode)model).getValue() : session.localize((Node)model));
    
    List<String> ans = new ArrayList<String>();

    for (int i = 0; i < res.length; i++) {
      Object obj = session.globalize(res[i]);
      if (!(obj instanceof Literal)) {
        logger.info("Ignoring matching node with invalid node type, expected literal: " + obj);
        continue;
      }

      ans.add(((Literal)obj).getLexicalForm());
    }
    
    return ans;
  }

  protected List<LocalNode> getLocalNodeObjects(Value subj, Value pred, Value model) throws LocalizeException, TuplesException, QueryException {

    long[] res = getObjects(
        (subj instanceof LocalNode) ? ((LocalNode)subj).getValue() : session.localize((Node)subj),
        (pred instanceof LocalNode) ? ((LocalNode)pred).getValue() : session.localize((Node)pred),
        (model instanceof LocalNode) ? ((LocalNode)model).getValue() : session.localize((Node)model));

    List<LocalNode> result = new ArrayList<LocalNode>();
    for (int i = 0; i < res.length; i++) {
      result.add(new LocalNode(res[i]));
    }

    return result;
  }

  protected LocalNode getLocalNodeObject(Value subj, Value pred, Value model, boolean optional) throws LocalizeException, TuplesException, QueryException {

    long[] res = getObjects(
        (subj instanceof LocalNode) ? ((LocalNode)subj).getValue() : session.localize((Node)subj),
        (pred instanceof LocalNode) ? ((LocalNode)pred).getValue() : session.localize((Node)pred),
        (model instanceof LocalNode) ? ((LocalNode)model).getValue() : session.localize((Node)model));
    
    if (res.length == 0) {
      if (optional) {
        return null;
      } else {
        throw new TuplesException("No statement found matching: [" + subj + " " + pred + " _ in " + model + "]");
      }
    } else if (res.length > 1) {
      throw new TuplesException("Multiple matching statements not supported: [" + subj + " " + pred + " _ in " + model + "]");
    }

    return new LocalNode(res[0]);
  }

  protected long[] getSubjects(long pred, long obj, long model) throws QueryException, TuplesException {
    Tuples res = resolver.resolve(new ConstraintImpl(
      new Variable("sub"),
      new LocalNode(pred),
      new LocalNode(obj),
      new LocalNode(model)));
    long[] ans = new long[(int)res.getRowCount()];
    int i = 0;
    res.beforeFirst();
    while(res.next()) {
      ans[i] = res.getColumnValue(0);
    }

    return ans;
  }

  protected long[] getObjects(long subj, long pred, long model) throws QueryException, TuplesException {
    Tuples res = resolver.resolve(new ConstraintImpl(
      new LocalNode(subj),
      new LocalNode(pred),
      new Variable("obj"),
      new LocalNode(model)));
    long[] ans = new long[(int)res.getRowCount()];
    int i = 0;
    res.beforeFirst();
    while(res.next()) {
      ans[i] = res.getColumnValue(0);
      i += 1;
    }

    return ans;
  }
}
