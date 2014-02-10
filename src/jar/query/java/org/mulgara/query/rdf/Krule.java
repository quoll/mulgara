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
package org.mulgara.query.rdf;

import java.net.URI;

/**
 * URI constants for rules.
 * 
 * @created Mar 23, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */

public class Krule {

  /** URI for the Krule namespace. */
  public static final String KRULE = "http://mulgara.org/owl/krule/#";

  /** URI for a constraint subject. */
  private static final String HAS_SUBJECT_STR = KRULE + "hasSubject";

  /** URI for a constraint predicate. */
  private static final String HAS_PREDICATE_STR = KRULE + "hasPredicate";

  /** URI for a constraint object. */
  private static final String HAS_OBJECT_STR = KRULE + "hasObject";

  /** URI for a constraint model. */
  private static final String HAS_GRAPH_STR = KRULE + "hasModel";

  /** URI for a query property on rules. */
  private static final String HAS_QUERY_STR = KRULE + "hasQuery";

  /** URI for an axiom subject. */
  private static final String AXIOM_SUBJECT_STR = KRULE + "subject";

  /** URI for an axiom predicate. */
  private static final String AXIOM_PREDICATE_STR = KRULE + "predicate";

  /** URI for an axiom object. */
  private static final String AXIOM_OBJECT_STR = KRULE + "object";

  /** URI for rule triggering predicate. */
  private static final String TRIGGERS_STR = KRULE + "triggers";

  /** URI for selection variables in a query. */
  private static final String SELECTION_VARS_STR = KRULE + "selectionVariables";

  /** URI for constraints predicate in a query. */
  private static final String HAS_WHERE_CLAUSE_STR = KRULE + "hasWhereClause";

  /** URI for having constraints predicate in a query. */
  private static final String HAS_HAVING_CLAUSE_STR = KRULE + "hasHavingClause";

  /** URI for the argument property. */
  private static final String ARGUMENT_STR = KRULE + "argument";

  /** URI for the minuend property. */
  private static final String MINUEND_STR = KRULE + "minuend";

  /** URI for the subtrahend property. */
  private static final String SUBTRAHEND_STR = KRULE + "subtrahend";

  /** URI for the transitive constraint argument. */
  private static final String TRANSITIVE_ARGUMENT_STR = KRULE + "transitiveArgument";

  /** URI for the transitive constraint anchor argument. */
  private static final String ANCHOR_ARGUMENT_STR = KRULE + "anchorArgument";

  /** URI for the name argument. */
  private static final String NAME_STR = KRULE + "name";

  /** URI for the constraint conjunction type. */
  private static final String CONSTRAINT_CONJUNCTION_STR = KRULE + "ConstraintConjunction";

  /** URI for the constraint disjunction type. */
  private static final String CONSTRAINT_DISJUNCTION_STR = KRULE + "ConstraintDisjunction";

  /** URI for the simple constraint type. */
  private static final String SIMPLE_CONSTRAINT_STR = KRULE + "SimpleConstraint";

  /** URI for the transitive constraint type. */
  private static final String TRANSITIVE_CONSTRAINT_STR = KRULE + "TransitiveConstraint";

  /** URI for difference type. */
  private static final String DIFFERENCE_STR = KRULE + "Difference";

  /** URI for the Value type. */
  private static final String URI_REF_STR = KRULE + "URIReference";

  /** URI for the Variable type. */
  private static final String VARIABLE_STR = KRULE + "Variable";

  /** URI for the Variable type. */
  private static final String LITERAL_STR = KRULE + "Literal";

  /** URI for axiom type. */
  private static final String AXIOM_STR = KRULE + "Axiom";

  /** URI for rule type. */
  private static final String RULE_STR = KRULE + "Rule";

  /** URI for consistency check type. */
  private static final String CHECK_STR = KRULE + "ConsistencyCheck";

  /** URI for query type. */
  private static final String QUERY_STR = KRULE + "Query";

  /** RDF reference for constraint subject. */
  public static final URIReferenceImpl HAS_SUBJECT = new URIReferenceImpl(URI.create(HAS_SUBJECT_STR));

  /** RDF reference for constraint predicate. */
  public static final URIReferenceImpl HAS_PREDICATE = new URIReferenceImpl(URI.create(HAS_PREDICATE_STR));

  /** RDF reference for constraint object. */
  public static final URIReferenceImpl HAS_OBJECT = new URIReferenceImpl(URI.create(HAS_OBJECT_STR));

  /** RDF reference for constraint model. */
  public static final URIReferenceImpl HAS_GRAPH = new URIReferenceImpl(URI.create(HAS_GRAPH_STR));

  /** RDF reference for query property on rules. */
  public static final URIReferenceImpl HAS_QUERY = new URIReferenceImpl(URI.create(HAS_QUERY_STR));

  /** RDF reference for axiom subject. */
  public static final URIReferenceImpl AXIOM_SUBJECT = new URIReferenceImpl(URI.create(AXIOM_SUBJECT_STR));

  /** RDF reference for axiom predicate. */
  public static final URIReferenceImpl AXIOM_PREDICATE = new URIReferenceImpl(URI.create(AXIOM_PREDICATE_STR));

  /** RDF reference for axiom object. */
  public static final URIReferenceImpl AXIOM_OBJECT = new URIReferenceImpl(URI.create(AXIOM_OBJECT_STR));

  /** RDF reference for rule triggering predicate. */
  public static final URIReferenceImpl TRIGGERS = new URIReferenceImpl(URI.create(TRIGGERS_STR));

  /** RDF reference for selection variables predicate. */
  public static final URIReferenceImpl SELECTION_VARS = new URIReferenceImpl(URI.create(SELECTION_VARS_STR));

  /** RDF reference for hasWhereClause predicate. */
  public static final URIReferenceImpl HAS_WHERE_CLAUSE = new URIReferenceImpl(URI.create(HAS_WHERE_CLAUSE_STR));

  /** RDF reference for hasHavingClause predicate. */
  public static final URIReferenceImpl HAS_HAVING_CLAUSE = new URIReferenceImpl(URI.create(HAS_HAVING_CLAUSE_STR));

  /** RDF reference for the argument property. */
  public static final URIReferenceImpl ARGUMENT = new URIReferenceImpl(URI.create(ARGUMENT_STR));

  /** RDF reference for the minuend property. */
  public static final URIReferenceImpl MINUEND = new URIReferenceImpl(URI.create(MINUEND_STR));

  /** RDF reference for the subtrahend property. */
  public static final URIReferenceImpl SUBTRAHEND = new URIReferenceImpl(URI.create(SUBTRAHEND_STR));

  /** RDF reference for the transitive constraint argument. */
  public static final URIReferenceImpl TRANSITIVE_ARGUMENT = new URIReferenceImpl(URI.create(TRANSITIVE_ARGUMENT_STR));

  /** RDF reference for the transitive constraint anchor argument. */
  public static final URIReferenceImpl ANCHOR_ARGUMENT = new URIReferenceImpl(URI.create(ANCHOR_ARGUMENT_STR));

  /** RDF reference for the name argument. */
  public static final URIReferenceImpl NAME = new URIReferenceImpl(URI.create(NAME_STR));

  /** RDF reference for constraint conjunction class. */
  public static final URIReferenceImpl CONSTRAINT_CONJUNCTION = new URIReferenceImpl(URI.create(CONSTRAINT_CONJUNCTION_STR));

  /** RDF reference for constraint disjunction class. */
  public static final URIReferenceImpl CONSTRAINT_DISJUNCTION = new URIReferenceImpl(URI.create(CONSTRAINT_DISJUNCTION_STR));

  /** RDF reference for the simple constraint type. */
  public static final URIReferenceImpl SIMPLE_CONSTRAINT = new URIReferenceImpl(URI.create(SIMPLE_CONSTRAINT_STR));

  /** RDF reference for the transitive constraint type. */
  public static final URIReferenceImpl TRANSITIVE_CONSTRAINT = new URIReferenceImpl(URI.create(TRANSITIVE_CONSTRAINT_STR));

  /** RDF reference for the Difference type. */
  public static final URIReferenceImpl DIFFERENCE = new URIReferenceImpl(URI.create(DIFFERENCE_STR));

  /** RDF reference for the Value type. */
  public static final URIReferenceImpl URI_REF = new URIReferenceImpl(URI.create(URI_REF_STR));

  /** RDF reference for the Variable type. */
  public static final URIReferenceImpl VARIABLE = new URIReferenceImpl(URI.create(VARIABLE_STR));

  /** RDF reference for the Literal type. */
  public static final URIReferenceImpl LITERAL = new URIReferenceImpl(URI.create(LITERAL_STR));

  /** RDF reference for the Axiom type. */
  public static final URIReferenceImpl AXIOM = new URIReferenceImpl(URI.create(AXIOM_STR));

  /** RDF reference for the Rule type. */
  public static final URIReferenceImpl RULE = new URIReferenceImpl(URI.create(RULE_STR));

  /** RDF reference for the Consistency Check type. */
  public static final URIReferenceImpl CHECK = new URIReferenceImpl(URI.create(CHECK_STR));

  /** RDF reference for the Query type. */
  public static final URIReferenceImpl QUERY = new URIReferenceImpl(URI.create(QUERY_STR));

}
