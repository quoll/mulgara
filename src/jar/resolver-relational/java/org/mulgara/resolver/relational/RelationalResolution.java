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

// Java 2 standard packages
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

// Third party packages
import org.apache.log4j.Logger;      // Apache Log4J
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.EmptyResolution;
import org.mulgara.resolver.spi.LocalizedTuples;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.resolver.relational.d2rq.Definition;

import org.mulgara.resolver.relational.d2rq.AdditionalPropertyElem;
import org.mulgara.resolver.relational.d2rq.ClassMapElem;
import org.mulgara.resolver.relational.d2rq.PropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.ObjectPropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.DatatypePropertyBridgeElem;

/**
 * Resolution of a query against a relational database.
 *
 * @author Andrae Muys
 *
 * @version $Revision: 1.1.1.1 $
 *
 * @modified $Date: 2005/10/30 19:21:14 $ @maintenanceAuthor $Author: prototypo $
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RelationalResolution extends AbstractTuples implements Resolution {

  /** Logger */
  private static final Logger logger = Logger.getLogger(RelationalResolution.class);

  /** The constraint this instance resolves */
  private final RelationalConstraint constraint;

  private Definition defn;
  private Tuples result;
  private Connection conn;
  private Variable[] variables;
  private int[] refCount;
  private ResolverSession resolverSession;
  private int[] columnMapping;
  private long cachedCount = -1;

  /**
   * @param constraint  the constraint to resolver, never <code>null</code>
   * @param result  the result of the resolution.
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>result</var> are <code>null</code>
   */
  RelationalResolution(RelationalConstraint constraint, Definition defn, ResolverSession resolverSession)
      throws TuplesException {
    // Validate parameters
    if (constraint == null) {
      throw new IllegalArgumentException( "Null 'constraint' parameter");
    } else if (defn == null) {
      throw new IllegalArgumentException("Null 'defn' parameter");
    }

    if (logger.isInfoEnabled()) {
      logger.info("Resolving constraint: " + constraint);
    }

    try {
      this.defn = defn;
      this.constraint = constraint;
      this.conn = obtainConnection(defn);
      this.result = null;
      this.variables = (Variable[])constraint.getVariables().toArray(new Variable[] {});
      this.columnMapping = new int[variables.length];
      this.refCount = new int[] { 1 };
      this.resolverSession = resolverSession;
    } catch (SQLException es) {
      throw new TuplesException("Unable to connect to relational database", es);
    }
  }

  public Variable[] getVariables() {
    return variables;
  }

  public long getRowCount() throws TuplesException {
    if (cachedCount == -1) {
      if (result == null) beforeFirst();
      cachedCount = result.getRowCount();
    }
    return cachedCount;
  }

  public long getRowUpperBound() throws TuplesException {
    // !!FIXME
    logger.info("Need to provide better estimate of upperbound for RelationalResolution");

    // However this is a remote SQL query, so extremely expensive to reevalutate, so if we are
    // going to be rebinding variables we want to rebind as many as possible to minimise the amound of
    // data transferred.  OTOH, we also would like to minimise the number of individual queries, I'm
    // not sure how we judge this tradeoff.  On the left of a join, we have exactly one query returning
    // results that will be heavily filtered, on the right multiple queries returning pre-filtered results.
    //
    // My intuition tells me that we want to be as far right as possible, so maybe this is the optimal
    // solution..
    return cachedCount == -1 ? Long.MAX_VALUE : cachedCount;
  }


  public long getRowExpectedCount() throws TuplesException {
    return cachedCount == -1 ? Long.MAX_VALUE : cachedCount;
  }


  public int getRowCardinality() throws TuplesException {
    return Cursor.MANY;
  }


  public int getColumnIndex(Variable variable) throws TuplesException {
    for (int i = 0; i < variables.length; i++) {
      if (variables[i].equals(variable)) {
        return i;
      }
    }
    throw new TuplesException("Variable " + variable + " not found in " + AbstractTuples.toString(variables));
  }


  public boolean isColumnEverUnbound(int column) {
    return false;   // No non-union compatible appends here.
  }


  public boolean isMaterialized() {
    if (result != null) {
      return result.isMaterialized();
    } else {
      return false;
    }
  }

  public boolean isUnconstrained() throws TuplesException {
    if (result != null) {
      return result.isUnconstrained();
    } else {
      return false;
    }
  }

  public boolean hasNoDuplicates() {
    return true;
  }

  public RowComparator getComparator() {
    if (result != null) {
      return result.getComparator();
    } else {
      return null;  // Should I be reporting sorted here, as it will be.
    }
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public void beforeFirst() throws TuplesException {
    beforeFirst(NO_PREFIX, 0);
  }

  public long getColumnValue(int column) throws TuplesException {
    if (result == null) {
      throw new TuplesException("beforeFirst not called in RelationalResolution");
    }
    return result.getColumnValue(columnMapping[column]);
  }


  public void renameVariables(Constraint constraint) {
    throw new IllegalStateException("Don't want to think about this yet");
  }

  
  public Object clone() {
    RelationalResolution r = (RelationalResolution)super.clone();
    r.refCount[0]++;
    if (r.result != null) {
      r.result = (Tuples)result.clone();
    }

    return r;
  }


  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object != null && this.getClass() == object.getClass()) {
      RelationalResolution lhs = (RelationalResolution)object;
      return lhs.defn == this.defn && lhs.constraint == this.constraint;
    }
    return false;
  }

  public int hashCode() {
    return defn.hashCode() * 7;  
  }

  public Constraint getConstraint() {
    return constraint;
  }


  public boolean isComplete() {
    return false;
  }


  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }


  public void close() throws TuplesException {
    if (result != null) {
      result.close();
      result = null;
    }
    refCount[0]--;
    try {
      if (refCount[0] == 0) {
        conn.close();
      }
    } catch (SQLException es) {
      throw new TuplesException("Error closing connection", es);
    }
  }


  public void beforeFirst(long[] prefix, int suffix) throws TuplesException {
    if (suffix != 0) {
      throw new TuplesException("RelationalResolution does not support suffix != 0");
    }

    Iterator<ConstraintExpression> i = constraint.getRdfTypeConstraints().iterator();
    if (!i.hasNext()) {
      this.result = new EmptyResolution(constraint, true);
    } else {
      List<Tuples> result = new ArrayList<Tuples>();
      try {
        while (i.hasNext()) {
          Constraint head = (Constraint)i.next();
          result.add(resolveInstance(head, constraint, conn, defn));
        }

        this.result = null;
        this.result = TuplesOperations.join(result);
        close(result.toArray(new Tuples[result.size()]));
      } catch (TuplesException e) {
        if (this.result == null) {
          // A non-null result means that the exception occured during the close
          try {
            close(result.toArray(new Tuples[result.size()]));
          } catch (TuplesException e2) { /* Already throwing an exception */ }
        }
        throw e;
      }
    }

    // It is possible that the join may have no variables but only if the query returns empty.
    if (!this.result.isEmpty()) {
      Variable[] local = this.getVariables();
      Variable[] join = this.result.getVariables();
      for (int a = 0; a < local.length; a++) {
        this.columnMapping[a] = -1;
        for (int b = 0; b < join.length; b++) {
          if (local[a].equals(join[b])) {
            this.columnMapping[a] = b;
          }
        }
        if (this.columnMapping[a] == -1) {
          throw new TuplesException("Error mapping variables to join");
        }
      }
    }

    this.result.beforeFirst(prefix, suffix);
  }


  public boolean next() throws TuplesException {
    if (this.result == null) {
      throw new TuplesException("beforeFirst() not called on RelationalResolution");
    }

    return this.result.next();
  }


  private Connection obtainConnection(Definition defn) throws SQLException, TuplesException {
    if (defn.databaseDefn.jdbcDriver == null) {
      throw new IllegalArgumentException("defn.jdbcDriver cannot be null");
    } else if (defn.databaseDefn.jdbcDSN == null) {
      throw new IllegalArgumentException("defn.jdbcDSN cannot be null");
    }

    try {
      Class.forName(defn.databaseDefn.jdbcDriver);
    } catch (ClassNotFoundException ec) {
      throw new TuplesException("Couldn't find Driver", ec);
    }

    Properties properties = new Properties();
    if (defn.databaseDefn.username != null) {
      properties.setProperty("user", defn.databaseDefn.username);
    }
    if (defn.databaseDefn.password != null) {
      properties.setProperty("password", defn.databaseDefn.password);
    }

    return DriverManager.getConnection(defn.databaseDefn.jdbcDSN, properties);
  }


  private Tuples resolveInstance(Constraint head, RelationalConstraint constraint, Connection conn, Definition defn) throws TuplesException {

    RelationalQuery query = new RelationalQuery();

    ConstraintElement subj = head.getElement(0);
    ConstraintElement obj = head.getElement(2);
    if (!(obj instanceof URIReference)) {
      throw new TuplesException("rdf:type not URIReference:" + obj + " :: " + obj.getClass());
    }

    // Basic query for instances of class.
    ClassMapElem classMap = (ClassMapElem)defn.classMaps.get(obj.toString());
    if (classMap == null) {
      throw new TuplesException("ClassMap not found for type: " + obj.toString());
    }

    includeInstanceQuery(query, subj, classMap);

    // Include properties
    List<Tuples> additionalProperties = new ArrayList<Tuples>();
    Map<String,? extends PropertyBridgeElem> propBs = defn.objPropBridges.get(classMap.klass);
    Map<String,? extends PropertyBridgeElem> dataBs = defn.dataPropBridges.get(classMap.klass);
    List<Constraint> constraints = constraint.getConstraintsBySubject(subj);
    for (Constraint c: constraints) {
      ConstraintElement pred = c.getElement(1);
      if (pred instanceof Variable) {
        includeVariablePropertyBridge(query, (Variable)pred, c, propBs, dataBs);
      } else {
        includePropertyBridge(query, c, propBs);
        includePropertyBridge(query, c, dataBs);

        // If we find a matching additional property then save it for distribution over the result.
        for (AdditionalPropertyElem ape: classMap.additionalProperties) {

          if (ape.name.equals(c.getElement(1).toString())) {
            if (c.getElement(2) instanceof Variable) {
              additionalProperties.add(TuplesOperations.assign((Variable)c.getElement(2), ape.valueNode));
            } else if (c.getElement(2) instanceof URIReference) {
              if (!ape.value.equals(c.getElement(2).toString())) {
                // Additional Property match failed, so entire constraint will fail.  Short circuit.
                return TuplesOperations.empty();
              }
            } else if (c.getElement(2) instanceof Literal) {
              if (!ape.value.equals(((Literal)c.getElement(2)).getLexicalForm())) {
                return TuplesOperations.empty();
              }
            } else {
              // This shouldn't happen.
              logger.warn("Object in constraint not variable, uri, or literal: " + c);
              continue;
            }
          }
        }
      }
    }

    Answer answer;
    answer = new RelationalAnswer(query, conn);

    Tuples lt = null, st = null;
    try {
      lt = new LocalizedTuples(resolverSession, answer, false);
    } catch (IllegalArgumentException e) {
      try {
        answer.close();
      } catch (TuplesException e2) { /* Already throwing an exception. Ignore. */ }
      throw e;
    }
    answer.close();
    
    try {
      st = TuplesOperations.sort(lt);
    } catch (TuplesException e) {
      try {
        lt.close();
      } catch (TuplesException e2) { /* Already throwing an exception. Ignore. */ }
      throw e;
    }
    lt.close();
    
    // Combine result with additional properties via join.
    additionalProperties.add(st);
    Tuples jt = null;
    try {
      jt = TuplesOperations.join(additionalProperties);
    } catch (TuplesException e) {
      try {
        st.close();
      } catch (TuplesException e2) { /* Already throwing an exception. Ignore. */ }
      throw e;
    }
    st.close();

    return jt;
  }


  private void includeInstanceQuery(RelationalQuery query, ConstraintElement instance, ClassMapElem classMap)
      throws TuplesException {
    if (instance instanceof Variable) {
      VariableDesc desc;
      if (classMap.uriColumn != null) {
        desc = new ColumnDesc(classMap, defn.databaseDefn.dbType);
      } else if (classMap.uriPattern != null) {
        desc = new PatternDesc(classMap, defn.databaseDefn.dbType);
      } else if (classMap.bNodeIdColumns != null) {
        desc = new BNodeDesc(classMap);
      } else {
        throw new TuplesException("Unknown class map definition type (not column or uripattern)");
      }

      if (desc.getTables().size() != 1) {
        throw new TuplesException("Error in PK definition: " + classMap + " - " + desc);
      }
      query.addTables(desc.getTables());
      for (String c: desc.getColumns()) {
        desc.assignColumnIndex(c, query.addColumn(c));
      }
      query.addVariable((Variable)instance, desc);

      for (String c: classMap.condition) {
        query.addRestriction(c);
      }
      
    } else { // subj !instanceof Variable
      VariableDesc desc;
      if (classMap.uriColumn != null) {
        desc = new ColumnDesc(classMap, defn.databaseDefn.dbType);
      } else if (classMap.uriPattern != null) {
        desc = new PatternDesc(classMap, defn.databaseDefn.dbType);
      } else {
        throw new TuplesException("Unknown class map definition type (not column or uripattern)");
      }

      if (desc.getTables().size() != 1) {
        throw new TuplesException("Error in PK definition: " + classMap + " - " + desc);
      }
      query.addTables(desc.getTables());
      query.addRestriction(desc.restrict(instance.toString()));

      for (String r: classMap.condition) {
        query.addRestriction(r);
      }
    }
    
    if(classMap.containsDuplicates) {
      query.makeDistinct();
    }
  }

  private void includePropertyBridge(RelationalQuery query, Constraint c, Map<String,? extends PropertyBridgeElem> propBs)
      throws TuplesException {
    ConstraintElement pred = c.getElement(1);
    PropertyBridgeElem propB = propBs.get(pred.toString());
    if (propB != null) {
      if (propB instanceof ObjectPropertyBridgeElem) {
        includeObjectPropertyBridge(query, c.getElement(2), (ObjectPropertyBridgeElem)propB);
      } else if (propB instanceof DatatypePropertyBridgeElem) {
        includeDatatypePropertyBridge(query, c.getElement(2), (DatatypePropertyBridgeElem)propB);
      } else {
        throw new TuplesException("Unknown propertybridge type");
      }

      for (String r: propB.condition) {
        query.addRestriction(r);
      }

      for (String join: propB.join) {
        query.addTables(RelationalResolver.extractTablesFromJoin(join));
        query.addRestriction(join);
      }
    }
  }


  private void includeVariablePropertyBridge(RelationalQuery query, Variable p, Constraint c,
        Map<String,? extends PropertyBridgeElem> propBs,
        Map<String,? extends PropertyBridgeElem> dataBs) throws TuplesException {

    PropertyBridgeElem defn = propBs.values().iterator().next();
    LiteralDesc predDesc = new LiteralDesc(defn, p);
    query.addVariable(p, predDesc);

    ConstraintElement obj = c.getElement(2);
    for (Map.Entry<String,? extends PropertyBridgeElem> entry: propBs.entrySet()) {
      String predicate = entry.getKey();
      PropertyBridgeElem propB = entry.getValue();
      VariableDesc vdesc = obtainDescForVariableProperty(propB);
      if (vdesc != null) {
        query.addUnionCase(predDesc, new UnionCase(predDesc, predicate, vdesc, obj));
      }
    }

    for (Map.Entry<String,? extends PropertyBridgeElem> entry: dataBs.entrySet()) {
      String predicate = entry.getKey();
      PropertyBridgeElem pb = entry.getValue();
      VariableDesc vdesc = obtainDescForVariableProperty(pb);
      if (vdesc != null) {
        query.addUnionCase(predDesc, new UnionCase(predDesc, predicate, vdesc, obj));
      }
    }
  }

  private VariableDesc obtainDescForVariableProperty(PropertyBridgeElem propB) throws TuplesException {
    if (propB instanceof ObjectPropertyBridgeElem) {
      ObjectPropertyBridgeElem ob = (ObjectPropertyBridgeElem)propB;
      if (ob.column != null) {
        return new ColumnDesc(ob, defn.databaseDefn.dbType);
      } else if (ob.refersToClassMap != null) {
        VariableDesc cdesc;
        if (ob.refersToClassMap.uriColumn != null) {
          cdesc = new ColumnDesc(ob.refersToClassMap, defn.databaseDefn.dbType);
        } else if (ob.refersToClassMap.uriPattern != null) {
          cdesc = new PatternDesc(ob.refersToClassMap, defn.databaseDefn.dbType);
        } else if (ob.refersToClassMap.bNodeIdColumns != null) {
          return null;  // ignore blanknodes until we figure out their interraction with restrict.
        } else {
          throw new TuplesException("Unknown class map definition type (not column or uripattern)");
        }
        cdesc.addJoin(propB.join);
        cdesc.addCondition(propB.condition);

        return cdesc;
      } else if (ob.pattern != null) {
        return new PatternDesc(ob, defn.databaseDefn.dbType);
      } else {
        throw new TuplesException("Unknown object property bridge type: " + propB);
      }
    } else if (propB instanceof DatatypePropertyBridgeElem) {
      DatatypePropertyBridgeElem db = (DatatypePropertyBridgeElem)propB;
      if (db.column != null) {
        return new ColumnDesc(db, defn.databaseDefn.dbType);
      } else if (db.pattern != null) {
        return new PatternDesc(db, defn.databaseDefn.dbType);
      } else {
        throw new TuplesException("Unknown datatype property bridge type: " + propB);
      }
    } else {
      throw new TuplesException("Unknown propertybridge type");
    }
  }

  private void includeObjectPropertyBridge(RelationalQuery query, ConstraintElement obj, ObjectPropertyBridgeElem propB)
      throws TuplesException {
    if (propB != null) {
      if (propB.column != null) {
        includeColumnBasedProperty(query, new ColumnDesc(propB, defn.databaseDefn.dbType), obj);
      } else if (propB.refersToClassMap != null) {
        includeInstanceQuery(query, obj, propB.refersToClassMap);
      } else if (propB.pattern != null) {
        includePatternBasedProperty(query, new PatternDesc(propB, defn.databaseDefn.dbType), obj);
      } else {
        throw new TuplesException("Unknown object property bridge type: " + propB);
      }
    }
  }

  private void includeDatatypePropertyBridge(RelationalQuery query, ConstraintElement obj, DatatypePropertyBridgeElem propB)
      throws TuplesException {
    if (propB != null) {
      if (propB.column != null) {
        includeColumnBasedProperty(query, new ColumnDesc(propB, defn.databaseDefn.dbType), obj);
      } else if (propB.pattern != null) {
        includePatternBasedProperty(query, new PatternDesc(propB, defn.databaseDefn.dbType), obj);
      } else {
        throw new TuplesException("Unknown datatype property bridge type: " + propB);
      }
    }
  }

  private void includeColumnBasedProperty(RelationalQuery query, ColumnDesc desc, ConstraintElement obj) throws TuplesException {
    query.addTable(desc.getTable());
    if (obj instanceof Variable) {          // *
      desc.assignColumnIndex(desc.getColumn(), query.addColumn(desc.getColumn()));
      query.addVariable((Variable)obj, desc);
    } else if (obj instanceof URIReference) {
      query.addRestriction(desc.restrict(obj.toString()));
    } else if (obj instanceof Literal) {
      // FIXME: handle datatypes by abstracting anticipatedClass, this can eliminate the above test for uri's as well.
      query.addRestriction(desc.restrict(((Literal)obj).getLexicalForm()));
    } else {
      throw new TuplesException("Unsupported object type: " + obj);
    }
  }


  private void includePatternBasedProperty(RelationalQuery query, PatternDesc desc, ConstraintElement obj) throws TuplesException {
    if (obj instanceof Variable) {
      Variable v = (Variable)obj;
      if (desc.getTables().size() != 1) {
        throw new TuplesException("Error in property definition: " + desc);
      }
      query.addTables(desc.getTables());
      for (String c: desc.getColumns()) {
        desc.assignColumnIndex(c, query.addColumn(c));
      }
      query.addVariable(v, desc);
    } else if (obj instanceof URIReference) {
      query.addRestriction(desc.restrict(obj.toString()));
    } else if (obj instanceof Literal) {
      query.addRestriction(desc.restrict(((Literal)obj).getLexicalForm()));
    } else {
      throw new TuplesException("Unsupported object type: " + obj);
    }
  }


}
