/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.krule;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.itql.VariableFactoryImpl;
import org.mulgara.query.ConstantValue;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintHaving;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphResource;
import org.mulgara.query.GraphUnion;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.SelectElement;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.VariableFactory;
import org.mulgara.query.rdf.Krule;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * A structure to describe an iTQL query.
 *
 * @created 2005-5-27
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.2 $
 * @modified $Date: 2005/07/03 12:56:44 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class QueryStruct implements Serializable {

  static final long serialVersionUID = -6472138136362435835L;
  
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(QueryStruct.class.getName());

  /** List of elements which are variables, or ConstantValues. */
  private List<SelectElement> variables = null;

  /** The graph expresison for the query. */
  private GraphExpression graphs = null;

  /** The where clause of the query. */
  private ConstraintExpression where = null;

  /** The having clause for the query. */
  private ConstraintHaving having = null;


  /**
   * Constructor.  Converts string descriptions of the values and variables into the constraint elements.
   *
   * @param valueSelection The element nodes.
   * @param selTypes The types of the elements, defined in the krule namespace.
   * @param uriReferences A map of all krule:ref_* objects to the appropriate {@link org.jrdf.graph.URIReference}s. 
   * @param varReferences A map of all krule:var_* objects to the appropriate name.
   * @throws IllegalArgumentException If the types are incorrect, the elements are not named as expected,
   *         or the references are not found in the references map.
   */
  public QueryStruct(
      List<URIReference> valueSelection, List<URIReference> selTypes,
      Map<URIReference,URIReference> uriReferences, Map<URIReference,Variable> varReferences,
      Map<Node,Literal> litReferences
  ) {

    if (valueSelection.size() <= 0 || selTypes.size() <= 0 || valueSelection.size() != selTypes.size()) {
      throw new IllegalArgumentException("Wrong number of elements for a rule query");
    }

    URIReference[] vs = valueSelection.toArray(new URIReference[valueSelection.size()]);
    URIReference[] types = selTypes.toArray(new URIReference[selTypes.size()]);

    // If there is a non-multiple of 3 in the selection variables, then this is a check rule
    // and we can only select variables in check rules
    boolean varsOnly = vs.length % 3 != 0;

    VariableFactory variableFactory = new VariableFactoryImpl();

    // set up a list of variables
    variables = new ArrayList<SelectElement>();

    // convert the parameters to usable objects
    for (int i = 0; i < vs.length; i++) {
      ConstraintElement select = null;
      URIReference element = vs[i];
      // check the type
      if (types[i].equals(Krule.URI_REF)) {

        // check that this didn't have a non-multiple of 3 in the selection values
        if (varsOnly) throw new IllegalArgumentException("Wrong number of elements for a rule query: " + vs.length);

        // get the referred value from the map
        select = (URIReferenceImpl)uriReferences.get(element);
        // assume that literals do not have the "Value" type inferred
        variables.add(new ConstantValue(variableFactory.newVariable(), (URIReferenceImpl)select));

      } else if (types[i].equals(Krule.VARIABLE)) {

        // get the variable
        select = (Variable)varReferences.get(element);
        variables.add((Variable)select);

      } else if (types[i].equals(Krule.LITERAL)) {
        
        if (i % 3 != 2) {
          throw new IllegalArgumentException("Selection literal in illegal position in query");
        }

        // check that this didn't have a non-multiple of 3 in the selection values
        if (varsOnly) throw new IllegalArgumentException("Wrong number of elements for a rule query: " + vs.length);

        // get the literal
        select = (LiteralImpl)litReferences.get(element);
        variables.add(new ConstantValue(variableFactory.newVariable(), (LiteralImpl)select));
      } else {
        throw new IllegalArgumentException("Unknown selection type in rule query.");
      }

      if (select == null) {
        throw new IllegalArgumentException("Unable to resolve a reference for: " + element);
      }
    }
  }
  
  /**
   * Construct a query structure with a known list of select elements.
   * @param vars The select elements for this query.
   */
  public QueryStruct(List<SelectElement> vars) {
    if (vars.size() <= 0) throw new IllegalArgumentException("Wrong number of elements for a rule query");
    this.variables = new ArrayList<SelectElement>(vars);
    
    // If there is a non-multiple of 3 in the selection variables, then this is a check rule
    // and we can only select variables in check rules
    boolean varsOnly = variables.size() % 3 != 0;
    
    // Validate the select elements.
    for (int i = 0; i < variables.size(); i++) {
      SelectElement var = variables.get(i);
      if (varsOnly && !(var instanceof Variable)) {
        throw new IllegalArgumentException("Wrong number of elements for a rule query: " + variables.size());
      }
      if ((var instanceof ConstantValue) && (((ConstantValue)var).getValue() instanceof Literal) && i % 3 != 2) {
        throw new IllegalArgumentException("Selection literal in illegal position in query");
      }
    }
  }


  /**
   * Returns the number of elements to be returned from this query.
   * @return The number of selection elements from the query.
   */
  public int elementCount() {
    return variables.size();
  }


  /**
   * Sets the where clause for the query.
   *
   * @param constraints The constraint expression defining the where clause.
   */
  public void setWhereClause(ConstraintExpression constraints) {
    where = constraints;
  }


  /**
   * Sets the having clause for the query.
   *
   * @param having The having constraint expression defining the having clause.
   */
  public void setHavingClause(ConstraintHaving having) {
    this.having = having;
  }


  /**
   * Sets the default graph expression for the query.
   *
   * @param expr The graph expression for the query.
   */
  public void setGraphExpression(GraphExpression expr) {
    this.graphs = expr;
  }


  /**
   * Sets the default graph expression for the query.
   *
   * @param expr The base graph expression for the query.
   * @param secondGraphUri The secondary graph URI for the query.
   */
  public void setGraphExpression(GraphExpression expr, URI secondGraphUri) {
    if (GraphResource.sameAs(expr, secondGraphUri)) {
      setGraphExpression(expr);
    } else {
      setGraphExpression(new GraphUnion(expr, new GraphResource(secondGraphUri)));
    }
  }


  /**
   * Constructs a new query based on the current data.
   *
   * @return a new {@link org.mulgara.query.Query}
   */
  @SuppressWarnings("unchecked")
  public Query extractQuery() {
    logger.debug("Extracting query");
    return new Query(variables, graphs, where, having, (List<Order>)Collections.EMPTY_LIST, null, 0, true, new UnconstrainedAnswer());
  }

}
