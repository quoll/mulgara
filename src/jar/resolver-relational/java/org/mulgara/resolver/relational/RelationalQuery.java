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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;


/**
 * Represents a relational query.
 */

// Notes on possible gaps:
//   We do not handle repeated object variables properly ie.  $s <p1> $o and $s <p2> $o.
//   This should map to a single column entry + a restriction.
//
//   

public class RelationalQuery {
  /** Logger */
  private static final Logger logger = Logger.getLogger(RelationalQuery.class);

  private Set<String> tableSet;
  private List<String> columnList;
  private Set<String> restrictionSet;
  private Map<Variable,VariableDesc> variableMap;
  private Set<Variable> variableSet;
  private boolean distinct = false;

  private Map<LiteralDesc,List<UnionCase>> unionCases;

  public RelationalQuery() {
    tableSet = new HashSet<String>();
    columnList = new ArrayList<String>();
    restrictionSet = new HashSet<String>();
    variableMap = new HashMap<Variable,VariableDesc>();
    unionCases = new HashMap<LiteralDesc,List<UnionCase>>();
    variableSet = new HashSet<Variable>();
  }

  public void addTable(String table) {
    tableSet.add(table);
  }

  public void addTables(Set<String> tables) {
    tableSet.addAll(tables);
  }

  public int addColumn(String column) {
    int index = columnList.indexOf(column);
    if (index == -1) {
      columnList.add(column);
      index = columnList.size() - 1;
    }

    return index;
  }

  public void addVariable(Variable v, VariableDesc desc) {
    Object old = variableMap.put(v, desc);
    if (old != null) {
      if (logger.isInfoEnabled()) {
        logger.info("Multiple descriptions for variable $" + v + " old: " + old + " new: " + desc);
      }
    }
    variableSet.add(v);
  }


  public void addUnionCase(LiteralDesc predVariable, UnionCase unionCase) {
    // !!FIXME: We need to consider what happens if multiple constraints use the same
    // variable predicate.  This would result in multiple restrictions/variables associated
    // with each union case.  To handle that we probably would need to move the map generation
    // out into the Resolution.
    List<UnionCase> unionCaseList = unionCases.get(predVariable);
    if (unionCaseList == null) {
      unionCaseList = new ArrayList<UnionCase>();
      unionCases.put(predVariable, unionCaseList);
    }

    unionCaseList.add(unionCase);

    // Allow for the existence of variables in the object position that do not participate
    // elsewhere in the constraint.
    if (unionCase.obj instanceof Variable) {
      variableSet.add((Variable)unionCase.obj);
    }
  }


  public void addRestriction(String restriction) {
    restrictionSet.add(restriction);
  }

  public void makeDistinct() {
    this.distinct = true;
  }
  
  public List<String> getQuery() {
    if (unionCases.size() == 0) {
      String distinctStr = distinct ? "DISTINCT " : "";
      String sql = "SELECT " + distinctStr + toList(columnList, ", ") + " FROM " + toList(tableSet, ", ");
      if (restrictionSet.size() > 0) {
        sql += " WHERE " + toList(restrictionSet, " AND ");
      }

      return Collections.singletonList(sql);
    } else {
      // Too many side-effects here.  There must be a better way of doing this.
      List<LiteralDesc> indexList = assignIndicies();
      List<String> selectList = generateSelectQueries(new ArrayList<String>(), new SubQuery(), indexList, 0);

      if (!variableSet.equals(variableMap.keySet())) {
        throw new IllegalStateException("Post query generated variableMap must match variableSet. map=" + variableMap + ", set=" + variableSet);
      }

      return selectList;
    }
  }

  private List<LiteralDesc> assignIndicies() {
    List<LiteralDesc> indexList = new ArrayList<LiteralDesc>();
    for (LiteralDesc desc: unionCases.keySet()) {
      indexList.add(desc);
    }

    return indexList;
  }

  private class SubQuery implements Cloneable {
    public ArrayList<String> columnList = new ArrayList<String>();
    public ArrayList<String> objColumnList = new ArrayList<String>();
    public HashSet<String> restrictionSet = new HashSet<String>();
    public HashSet<String> tableSet = new HashSet<String>();

    @SuppressWarnings("unchecked")
    public Object clone() {
      try {
        SubQuery c = (SubQuery)super.clone();
        c.columnList = (ArrayList<String>)columnList.clone();
        c.objColumnList = (ArrayList<String>)objColumnList.clone();
        c.restrictionSet = (HashSet<String>)restrictionSet.clone();
        c.tableSet = (HashSet<String>)tableSet.clone();

        return c;
      } catch (CloneNotSupportedException ec) {
        throw new IllegalStateException("Clone not supported on Cloneable");
      }
    }

    public String toString() {
      return "sq:(" + 
        "columnList=" + columnList + ", " +
        "objColumnList=" + objColumnList + ", " +
        "restrictionSet=" + restrictionSet + ", " +
        "tableSet=" + tableSet + ")";
    }
  }


  private String generateSelectQuery(SubQuery sq) {
    String sql = "SELECT " + toList(columnList, ", ");
    if (sq.columnList.size() > 0) {
      sql += ", " + toList(sq.columnList, ", ");
    }
    if (sq.objColumnList.size() > 0) {
      sql += ", " + toList(sq.objColumnList, ", ");
    }

    
    sql += " FROM " + toList(tableSet, ", ");
    // Remove duplicates.
    sq.tableSet.removeAll(tableSet);
    if (sq.tableSet.size() > 0) {
      sql += ", " + toList(sq.tableSet, ", ");
    }

    if (restrictionSet.size() > 0 || sq.restrictionSet.size() > 0) {
      sql += " WHERE ";
    }
    if (restrictionSet.size() > 0) {
      sql += toList(restrictionSet, " AND ");
    }
    if (sq.restrictionSet.size() > 0) {
      sql += toList(sq.restrictionSet, " AND ");
    }

    return sql;
  }


  /**
   * @return The accumulator is returned having accumulated the required select queries.
   */
  private List<String> generateSelectQueries(List<String> accum, SubQuery subQuery, List<LiteralDesc> indexList, int index) {
    if (index >= indexList.size()) {
      accum.add(generateSelectQuery(subQuery));
    } else {
      LiteralDesc desc = indexList.get(index);
      List<UnionCase> cases = unionCases.get(desc);
      for (UnionCase cse: cases) {
        SubQuery sq = (SubQuery)subQuery.clone();

        sq.columnList.add("'" + cse.pred + "'");
        // We need to do the assignment here (which will be highly redundant) because
        // this is the only place we can compensate for the possible insertion of object variables
        // into the query.
        desc.assignColumnIndex(null, columnList.size() + sq.columnList.size() - 1);

        if (cse.obj instanceof Variable) { // Variable object.
          // Obtain a redirect descriptor for variable.
          VariableDesc od = variableMap.get(cse.obj);
          RedirectDesc rdesc;
          if (od == null) {
            rdesc = new RedirectDesc(desc);
            // We don't subtract 1 here because we have not yet added rdesc to sq.columnList
            int redirectIndex = columnList.size() + sq.columnList.size();
            rdesc.assignColumnIndex(null, redirectIndex);
          } else if (od instanceof RedirectDesc) {
            rdesc = (RedirectDesc)od;
          } else {
            throw new IllegalStateException("Pre-existing variable descriptor for variable object with variable predicate not supported: v=" + cse.obj + " desc=" + od);
          }

          // Handle the Variable - uses redirect to handle different types returning from union.
          this.addVariable((Variable)cse.obj, rdesc);

          // Handle Tables.  Note that duplicates with rq.tableSet are removed in generateSelectQuery
          sq.tableSet.addAll(cse.desc.getTables());
          
          // Handle conditions and joins.  cut-n-paste from includePropertyBridge - refactor required
          for (String join: cse.desc.getJoin()) {
            sq.tableSet.addAll(RelationalResolver.extractTablesFromJoin(join));
            sq.restrictionSet.add(join);
          }
          sq.restrictionSet.addAll(cse.desc.getCondition());

          // Register case descriptor with redirect and insert redirect index as literal into query
          sq.columnList.add(Integer.toString(rdesc.addVariableDesc(cse.desc)));

          // Handle Columns
          for (String c: cse.desc.getColumns()) {
            int newIndex = columnList.indexOf(c);
            if (newIndex == -1) {
              newIndex = sq.columnList.indexOf(c);
              if (newIndex == -1) {
                sq.columnList.add(c);
                // Allow for the fact that we just increased the size of sq.columnList by 1.
                newIndex = columnList.size() + sq.columnList.size() - 1;
              } else {
                newIndex += columnList.size();
              }
            }
            cse.desc.assignColumnIndex(c, newIndex);
          }
        } else { // Literal object
          // Handle Tables. See note above regarding duplicates
          sq.tableSet.addAll(cse.desc.getTables());

          // Handle conditions and joins.  cut-n-paste from includePropertyBridge - refactor required
          for (String join: cse.desc.getJoin()) {
            sq.tableSet.addAll(RelationalResolver.extractTablesFromJoin(join));
            sq.restrictionSet.add(join);
          }
          sq.restrictionSet.addAll(cse.desc.getCondition());

          // Handle Restriction
          if (cse.obj instanceof URIReference) {
            sq.restrictionSet.add(cse.desc.restrict(cse.obj.toString()));
          } else if (cse.obj instanceof Literal) {
            sq.restrictionSet.add(cse.desc.restrict(((Literal)cse.obj).getLexicalForm()));
          } else {
            throw new IllegalArgumentException("Unsupported object type: " + cse.obj);
          }
        }

        // Recurse on indexList.
        // By recursing inside the for-loop we expand the combinations of union cases.
        generateSelectQueries(accum, sq, indexList, index + 1);
      }
    }

    return accum;
  }

  public List<Variable> getVariables() {
    return new ArrayList<Variable>(variableSet);
  }

  public VariableDesc getVariableDesc(Variable var) throws TuplesException {
    VariableDesc desc = variableMap.get(var);
    if (desc == null) {
      throw new TuplesException("Variable not found: " + var);
    }

    return desc;
  }

  public static String toList(Collection<String> strings, String delim) {
    StringBuffer result;
    Iterator<String> i = strings.iterator();
    if (!i.hasNext()) {
      return "";
    } else {
      String s = i.next();
      result = new StringBuffer(s);
    }
    while (i.hasNext()) result.append(delim + i.next().toString());
    return result.toString();
  }

  public String toString() {
    return "RelationalQuery (sql = " + getQuery() + ", varMap = " + variableMap + ")";
  }
}
