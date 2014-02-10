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

package org.mulgara.sparql;

import static org.jrdf.vocabulary.RDF.TYPE;
import static org.jrdf.vocabulary.RDFS.LITERAL;
import static org.mulgara.query.rdf.Mulgara.NODE_TYPE_GRAPH;
import static org.mulgara.query.rdf.Mulgara.DEFAULT_GRAPH;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mulgara.parser.Interpreter;
import org.mulgara.parser.MulgaraLexerException;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.AskQuery;
import org.mulgara.query.ConstantValue;
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.ConstructQuery;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphResource;
import org.mulgara.query.GraphUnion;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.SelectElement;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.operation.Command;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.sparql.parser.ParseException;
import org.mulgara.sparql.parser.QueryStructure;
import org.mulgara.sparql.parser.QueryType;
import org.mulgara.sparql.parser.SparqlParser;
import org.mulgara.sparql.parser.cst.BlankNode;
import org.mulgara.sparql.parser.cst.Expression;
import org.mulgara.sparql.parser.cst.GroupGraphPattern;
import org.mulgara.sparql.parser.cst.IRIReference;
import org.mulgara.sparql.parser.cst.Node;
import org.mulgara.sparql.parser.cst.Ordering;
import org.mulgara.sparql.parser.cst.RDFLiteral;
import org.mulgara.sparql.parser.cst.Triple;
import org.mulgara.util.ServerInfoRef;
import org.mulgara.util.functional.C;
import org.mulgara.util.functional.Fn1;

/**
 * Converts a parsed SPARQL query into a Command for execution.
 *
 * @created Apr 18, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class SparqlInterpreter implements Interpreter {

  /** The default graph to use if none has been set. */
  private static final List<URI> INTERNAL_DEFAULT_GRAPH_URIS = Collections.singletonList(URI.create(DEFAULT_GRAPH));

  /** The column variables used to build a graph. */
  private static final Variable[] GRAPH_VARS = GraphAnswer.getGraphVariables();

  /** A constraint for referring to an entire constructed graph. */
  private static final Constraint GRAPH_CONSTRAINT = new ConstraintImpl(GRAPH_VARS[0], GRAPH_VARS[1], GRAPH_VARS[2]);

  /** Reference for URI references. */
  private static final URIReferenceImpl URI_REF = new URIReferenceImpl(URI.create(Mulgara.NAMESPACE + "UriReference"));

  /** Reference for rdf:type. */
  private static final URIReferenceImpl TYPE_REF = new URIReferenceImpl(TYPE);

  /** Reference for rdfs:Literal. */
  private static final URIReferenceImpl LITERAL_REF = new URIReferenceImpl(LITERAL);

  /** Reference for the graph of sys:type. */
  private static final URIReferenceImpl NODE_TYPE_GRAPH_REF = new URIReferenceImpl(URI.create(NODE_TYPE_GRAPH));

  /** A constraint for limiting the graph predicate variable to literals. */
  private static final ConstraintImpl LITERAL_ONLY_CONSTRAINT = new ConstraintImpl(GRAPH_VARS[2], TYPE_REF, LITERAL_REF, NODE_TYPE_GRAPH_REF);

  /** A constraint for limiting the graph predicate variable to URIs. */
  private static final ConstraintImpl URI_ONLY_CONSTRAINT = new ConstraintImpl(GRAPH_VARS[2], TYPE_REF, URI_REF, NODE_TYPE_GRAPH_REF);


  /** The default graphs to use as set by the protocol. */
  private List<IRIReference> defaultGraphIris = Collections.emptyList();

  /** The named graphs to use as set by the protocol. */
  private List<IRIReference> namedGraphIris = Collections.emptyList();

  /**
   * Sets the graphs to use in parsed queries, overriding what is found in the query.
   * @param graphUris The graph URIs to use as the default graphs. A <code>null</code> value
   *        is treated the same as an empty list, and is used to unset this override.
   */
  public SparqlInterpreter setDefaultGraphUris(List<URI> graphUris) {
    defaultGraphIris = toIRIs(graphUris);
    return this;
  }

  /**
   * Sets the single default graph to use in parsed queries, overriding what is found in the query.
   * @param graphUri The graph URI to use as the default graph, or <code>null</code> if the
   *        override graph is to be unset.
   */
  public SparqlInterpreter setDefaultGraphUri(URI graphUri) {
    if (graphUri == null) defaultGraphIris = Collections.emptyList();
    else defaultGraphIris = Collections.singletonList(new IRIReference(graphUri));
    return this;
  }

  /**
   * Sets the single default graph to use in parsed queries.
   * @param graph The graph URI to use as the default graph, or <code>null</code> if the
   *        default graph is to be unset.
   * @throws URISyntaxException The graph was not a valid URI.
   */
  public SparqlInterpreter setDefaultGraphUri(String graph) throws URISyntaxException {
    if (graph == null || graph.trim().length() == 0) return setDefaultGraphUri((URI)null);
    return setDefaultGraphUri(new URI(graph));
  }

  /**
   * Sets the graphs to use as named graphs in parsed queries, overriding what is found in the query.
   * @param graphUris The graph URIs to use as the named graphs. A <code>null</code> value
   *        is treated the same as an empty list, and is used to unset this override.
   */
  public SparqlInterpreter setNamedGraphUris(List<URI> graphUris) {
    namedGraphIris = toIRIs(graphUris);
    return this;
  }

  /**
   * Gets the default graph to use when none has been parsed from the query.
   * @return The graph that parsed queries will default to when no FROM graph is supplied.
   */
  public List<URI> getDefaultGraphUris() {
    return defaultGraphIris.isEmpty() ? INTERNAL_DEFAULT_GRAPH_URIS : toURIs(defaultGraphIris);
  }

  /**
   * Gets the default graph to use when none has been parsed from the query.
   * @return The graph that parsed queries will default to when no FROM graph is supplied.
   */
  public List<IRIReference> getDefaultGraphIris() {
    return defaultGraphIris.isEmpty() ? toIRIs(INTERNAL_DEFAULT_GRAPH_URIS) : defaultGraphIris;
  }

  /**
   * Gets the default graph to use when none has been parsed from the query.
   * @return The graph that parsed queries will default to when no FROM graph is supplied.
   */
  public List<IRIReference> getNamedGraphIris() {
    return namedGraphIris;
  }

  /**
   * @see org.mulgara.parser.Interpreter#parseCommand(java.lang.String)
   * The only commands that SPARQL current handles are queries.
   */
  public Command parseCommand(String command) throws MulgaraParserException, MulgaraLexerException, IllegalArgumentException, IOException {
    return parseQuery(command);
  }

  /**
   * @see org.mulgara.parser.Interpreter#parseCommands(java.lang.String)
   * Since SPARQL has no separator character, there can only be one command per string.
   */
  public List<Command> parseCommands(String command) throws MulgaraParserException, MulgaraLexerException, IOException, IllegalArgumentException {
    return Collections.singletonList(parseCommand(command));
  }

  /**
   * @see org.mulgara.parser.Interpreter#parseQuery(java.lang.String)
   */
  public Query parseQuery(String queryString) throws IOException, MulgaraLexerException, MulgaraParserException {
    QueryStructure struct;
    try {
      struct = SparqlParser.parse(queryString);
    } catch (ParseException pe) {
      throw new MulgaraParserException(pe);
    }
    Query result = null;
    switch (struct.getType()) {
      case select:
        result = buildSelectQuery(struct);
        break;
      case construct:
        result = buildConstructQuery(struct);
        break;
      case describe:
        result = buildDescribeQuery(struct);
        break;
      case ask:
        result = buildAskQuery(struct);
        break;
      default:
        throw new MulgaraParserException("Unknown query type: " + struct.getType().name());
    }
    result.setText(queryString);
    return result;
  }

  /**
   * Respond to an unhandled query type.
   * @param struct The structure representing the query
   * @return Nothing. An exception is always thrown.
   * @throws UnsupportedOperationException An exception explaining that this query type is not handled.
   */
  @SuppressWarnings("unused")
  private Query unhandledType(QueryStructure struct) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Query type not yet supported: " + struct.getType().name());
  }

  /**
   * Converts the elements of a {@link QueryStructure} into a Mulgara {@link Query}.
   * @param queryStruct The structure to analyze and convert.
   * @return A new query that can be run as a {@link org.mulgara.query.operation.Command} on a connection.
   * @throws MulgaraParserException If the query structure contains elements that are not supported by Mulgara.
   */
  Query buildSelectQuery(QueryStructure queryStruct) throws MulgaraParserException {
    List<? extends SelectElement> selection = getSelection(queryStruct);
    GraphExpression defaultGraphs = getFrom(queryStruct);
    ConstraintExpression whereClause = getWhere(queryStruct);
    if (whereClause == null) throw new MulgaraParserException("SELECT query must have a WHERE clause");
    List<Order> orderBy = getOrdering(queryStruct);
    Integer limit = getLimit(queryStruct);
    int offset = queryStruct.getOffset();
    boolean distinct = queryStruct.isDistinct();
    // null having, unconstrained answer
    return new Query(selection, defaultGraphs, whereClause, null, orderBy, limit, offset, distinct, new UnconstrainedAnswer());
  }

  /**
   * Converts the elements of a {@link QueryStructure} into a Mulgara {@link AskQuery}.
   * @param queryStruct The structure to analyze and convert.
   * @return A new query that can be run as a {@link org.mulgara.query.operation.Command} on a connection.
   * @throws MulgaraParserException If the query structure contains elements that are not supported by Mulgara.
   */
  AskQuery buildAskQuery(QueryStructure queryStruct) throws MulgaraParserException {
    List<Variable> selection = new ArrayList<Variable>();
    Collection<org.mulgara.sparql.parser.cst.Variable> allVars = queryStruct.getAllVariables();
    for (org.mulgara.sparql.parser.cst.Variable v: allVars) selection.add(new Variable(v.getName()));
    GraphExpression defaultGraphs = getFrom(queryStruct);
    ConstraintExpression whereClause = getWhere(queryStruct);
    if (whereClause == null) throw new MulgaraParserException("ASK query must have a WHERE clause");
    return new AskQuery(selection, defaultGraphs, whereClause);
  }

  /**
   * Converts the elements of a {@link QueryStructure} into a Mulgara {@link ConstructQuery}.
   * @param queryStruct The structure to analyze and convert.
   * @return A new query that can be run as a {@link org.mulgara.query.operation.Command} on a connection.
   * @throws MulgaraParserException If the query structure contains elements that are not supported by Mulgara.
   */
  ConstructQuery buildConstructQuery(QueryStructure queryStruct) throws MulgaraParserException {
    List<? extends SelectElement> selection = getConstructTemplate(queryStruct);
    if (selection.size() % 3 != 0) {
      throw new MulgaraParserException("CONSTRUCT queries require a multiple of 3 nodes in the template.");
    }
    GraphExpression defaultGraphs = getFrom(queryStruct);
    ConstraintExpression whereClause = getWhere(queryStruct);
    if (whereClause == null) throw new MulgaraParserException("CONSTRUCT query must have a WHERE clause");
    List<Order> orderBy = getOrdering(queryStruct);
    Integer limit = getLimit(queryStruct);
    int offset = queryStruct.getOffset();
    // null having, unconstrained answer
    return new ConstructQuery(selection, defaultGraphs, whereClause, orderBy, limit, offset);
  }

  /**
   * Converts the elements of a {@link QueryStructure} into a Mulgara {@link ConstructQuery}
   * appropriate for a DESCRIBE query.
   * @param queryStruct The structure to analyze and convert.
   * @return A new query that can be run as a {@link org.mulgara.query.operation.Command} on a connection.
   * @throws MulgaraParserException If the query structure contains elements that are not supported by Mulgara.
   */
  ConstructQuery buildDescribeQuery(QueryStructure queryStruct) throws MulgaraParserException {
    List<? extends SelectElement> described = getSelection(queryStruct);
    ConstraintExpression whereClause = distributeIntoWhereClause(described, getWhere(queryStruct));
    whereClause = constraintToNonBlank(whereClause);
    GraphExpression defaultGraphs = getFrom(queryStruct);
    // Ignore the order since its behavior is unspecified for DESCRIBE.
    //List<Order> orderBy = getOrdering(queryStruct);
    List<Order> orderBy = new ArrayList<Order>(0);
    Integer limit = getLimit(queryStruct);
    int offset = queryStruct.getOffset();
    return new ConstructQuery(graphVariables(), defaultGraphs, whereClause, orderBy, limit, offset);
  }

  /**
   * Extract the requested variables from this query into a list.
   * @param queryStruct The query to get the selected variables from.
   * @return A new list containing Mulgara {@link Variable}s.
   * @throws MulgaraParserException If and selected elements are not variables.
   */
  List<? extends SelectElement> getSelection(QueryStructure queryStruct) throws MulgaraParserException {
    List<SelectElement> result = new ArrayList<SelectElement>();
    if (queryStruct.isSelectAll()) {
      Collection<org.mulgara.sparql.parser.cst.Variable> allVars = queryStruct.getAllVariables();
      for (org.mulgara.sparql.parser.cst.Variable v: allVars) result.add(new Variable(v.getName()));
    } else {
      ConstantVarFactory constantVars = new ConstantVarFactory();
      List<? extends Node> selection = queryStruct.getSelection();
      for (Node n: selection) {
        if (n instanceof org.mulgara.sparql.parser.cst.Variable) {
          org.mulgara.sparql.parser.cst.Variable cv = (org.mulgara.sparql.parser.cst.Variable)n;
          result.add(new Variable(cv.getName()));
        } else if (n instanceof IRIReference) {
          // SPARQL SELECT only permits variables; DESCRIBE permits IRI's
          if (queryStruct.getType() == QueryType.select) throw new MulgaraParserException("Unexpected non-variable in the SELECT clause");
          result.add(new ConstantValue(constantVars.newVar(), new URIReferenceImpl(((IRIReference)n).getUri(), false)));
        } else {
          throw new MulgaraParserException("Unexpected select expression");
        }
      }
    }
    return result;
  }

  /**
   * Extract the requested elements from this construction template into a List.
   * @param queryStruct The query to get the selected variables from.
   * @return A new list containing Mulgara {@link Variable}s.
   * @throws MulgaraParserException If and selected elements are not variables.
   */
  @SuppressWarnings("unchecked")
  List<? extends SelectElement> getConstructTemplate(QueryStructure queryStruct) throws MulgaraParserException {
    List<SelectElement> result = new ArrayList<SelectElement>();
    List<Triple> template = (List<Triple>)queryStruct.getConstructTemplate().getElements();
    ConstantVarFactory factory = new ConstantVarFactory();
    for (Triple triple: template) {
      result.add(convertForTemplate(triple.getSubject(), factory));
      result.add(convertForTemplate(triple.getPredicate(), factory));
      result.add(convertForTemplate(triple.getObject(), factory));
    }
    return result;
  }

  /**
   * Converts a node to a "selectable" element for use in a CONSTRUCT template.
   * @param node The query node to convert.
   * @param constantVars A variable factory for labelling constants.
   * @return A new element that can be used in a CONSTRUCT template.
   * @throws MulgaraParserException The template element was not valid.
   */
  private SelectElement convertForTemplate(Node node, ConstantVarFactory constantVars) throws MulgaraParserException {
    // variables are simply converted
    if (node instanceof org.mulgara.sparql.parser.cst.Variable) {
      return new Variable(((org.mulgara.sparql.parser.cst.Variable)node).getName());
    }
    // blank nodes are converted to unused variables
    if (node instanceof BlankNode) {
      // blank node labels are not used elsewhere as variables as they are not parsed as valid variables
      return new Variable(((BlankNode)node).getLabel());
    }
    // RDFLiterals are converted to a literal, and wrapped in a ConstantValue
    if (node instanceof RDFLiteral) {
      RDFLiteral rl = (RDFLiteral)node;
      LiteralImpl literal;
      if (rl.isLanguageCoded()) literal = new LiteralImpl(rl.getValue(), rl.getLanguage());
      else if (rl.isTyped()) literal = new LiteralImpl(rl.getValue(), rl.getDatatype().getUri());
      else literal = new LiteralImpl(rl.getValue());
      return new ConstantValue(constantVars.newVar(), literal);
    }
    // IRIReferences are converted to a URI and wrapped in a ConstantValue
    if (node instanceof IRIReference) {
      return new ConstantValue(constantVars.newVar(), new URIReferenceImpl(((IRIReference)node).getUri(), false));
    }
    // nothing else is valid. We probably have a compound expression.
    throw new MulgaraParserException("Unexpected element in CONSTRUCT template: " + node); 
  }

  /**
   * Gets the graph expression ({@link GraphExpression}) the represents the FROM clause, or the default
   * graph if none was provided.
   * @param queryStruct The structure to query for the FROM clause.
   * @return A GraphExpression containing all the required graphs as a union. TODO: this should be a merge.
   */
  GraphExpression getFrom(QueryStructure queryStruct) {
    // get the overriding defaults
    List<IRIReference> graphs = defaultGraphIris;
    if (graphs == null || graphs.isEmpty()) {
      // no override set, so get the data from the query
      graphs = queryStruct.getDefaultFroms();
      // nothing in the query, so fall back to the system default
      if (graphs == null || graphs.isEmpty()) graphs = getSystemDefaultGraph();
    }
    // accumulate the graphs as a union
    return graphUnion(graphs);
  }

  /**
   * Convert a list of IRIs into a model resource union of minimal depth. This recurses through construction
   * of a tree of binary unions, rather than creating a linear linked list of unions.
   * @param iris The list to convert.
   * @return A GraphExpression which is a union of all the elements in the list,
   *   or a {@link GraphResource} if the list contains only one element.
   */
  private GraphExpression graphUnion(List<IRIReference> iris) {
    int listSize = iris.size();
    // terminate on singleton lists
    if (listSize == 1) return new GraphResource(iris.get(0).getUri());
    // short circuit for 2 element lists - optimization
    if (listSize == 2) return new GraphUnion(new GraphResource(iris.get(0).getUri()), new GraphResource(iris.get(1).getUri()));
    // general case
    return new GraphUnion(graphUnion(iris.subList(0, listSize / 2)), graphUnion(iris.subList(listSize / 2, listSize)));
  }

  /**
   * Creates a list of the ordering to apply to the results. While SPARQL permits ordering by complex
   * expressions, this is not supported.
   * @param queryStruct The query structure.
   * @return A list of {@link Order}, which are each ordered ascending or descending by variable.
   * @throws MulgaraParserException If the ORDER BY expression was more complex than a simple variable.
   */
  List<Order> getOrdering(QueryStructure queryStruct) throws MulgaraParserException {
    List<Ordering> orderings = queryStruct.getOrderings();
    List<Order> result = new ArrayList<Order>(orderings.size());
    for (Ordering order: orderings) {
      Expression v = order.getExpr();
      if (!(v instanceof org.mulgara.sparql.parser.cst.Variable)) throw new MulgaraParserException("Unable to support arbitrarily complex ORDER BY clauses.");
      org.mulgara.sparql.parser.cst.Variable var = (org.mulgara.sparql.parser.cst.Variable)v;
      result.add(new Order(new Variable(var.getName()), order.isAscending()));
    }
    return result;
  }

  /**
   * Get the limit described by the query.
   * @param queryStruct The structure of the query.
   * @return A {@link java.lang.Integer} containing the limit, or <code>null</code> if there is no limit.
   */
  Integer getLimit(QueryStructure queryStruct) {
    int limit = queryStruct.getLimit();
    return limit == -1 ? null : limit;
  }

  /**
   * Build a WHERE clause for a Mulgara query out of a SPARQL WHERE clause.
   * @param queryStruct The SPARQL query structure to analyze for the WHERE clause.
   * @return A Mulgara WHERE clause, as a {@link ConstraintExpression}.
   * @throws MulgaraParserException The structure of the pattern was incorrect.
   */
  ConstraintExpression getWhere(QueryStructure queryStruct) throws MulgaraParserException {
    // get the basic pattern
    GroupGraphPattern pattern = queryStruct.getWhereClause();
    
    // short-circuit, since DESCRIBE may omit the WHERE clause
    if (pattern == null) return null;
    
    PatternMapper patternMapper = new PatternMapper(pattern);
    ConstraintExpression result = patternMapper.mapToConstraints();
    // apply the FROM NAMED expression
    // TODO: This needs to become a Constraint that wraps LiteralTuples.
    List<IRIReference> namedFroms = getNamedFroms(queryStruct);
    if (!namedFroms.isEmpty()) result = addNamedFroms(result, namedFroms, patternMapper.getGraphVars());
    // possible to ask for non-variables that were employed in GRAPH statements as a parser check.

    // grammar-level optimizations
    result = PatternTransformer.transform(result);
    return result;
  }


  /**
   * Get the NAMED FROM clauses to be used in this query.
   * @param queryStruct the parse structure for the query.
   * @return The NAMED FROM IRIs to be used, using the protocol values if these are set.
   */
  List<IRIReference> getNamedFroms(QueryStructure queryStruct) {
    if (namedGraphIris != null && !namedGraphIris.isEmpty()) return namedGraphIris;
    return queryStruct.getNamedFroms();
  }


  /**
   * Add in the FROM NAMED values to provide a binding list for each variable used in GRAPH statements.
   * @param expr The total expression to be modified. This is the WHERE clause.
   * @param graphs The list of graphs given in the FROM NAMED clauses.
   * @param graphVars The variables that are used in GRAPH statements.
   * @return A modified form of expr, with all the graph variables pre-bound.
   */
  ConstraintExpression addNamedFroms(ConstraintExpression expr, List<IRIReference> graphs, Set<Variable> graphVars) {
    List<ConstraintExpression> params = new ArrayList<ConstraintExpression>(graphVars.size() + 1);
    params.add(expr);
    for (Variable var: graphVars) params.add(newListConstraint(var, graphs));
    return new ConstraintConjunction(params);
  }

  /**
   * Construct a constraint expression that binds a variable to a list of values.
   * TODO: This needs to be represented by a new Constraint that gets converted to a LiteralTuples.
   * @param var The variable to bind.
   * @param bindingList The list of values to bind the variable to.
   * @return A new {@link org.mulgara.query.Constraint} that represents the variable binding.
   */
  ConstraintExpression newListConstraint(Variable var, List<IRIReference> bindingList) {
    List<ConstraintExpression> isConstraints = new ArrayList<ConstraintExpression>(bindingList.size());
    for (IRIReference iri: bindingList) {
      // does this need a graph node that isn't variable?
      isConstraints.add(new ConstraintIs(var, new URIReferenceImpl(iri.getUri(), false)));
    }
    return new ConstraintDisjunction(isConstraints);
  }

  /**
   * Converts a WHERE clause into using "subject" instead of the variables from described.
   * Then creates a union of this WHERE clause converted for each element in described.
   * If the base WHERE clause is empty, then ($subject $predicate $object) is presumed. 
   * @param described The elements to distribute into the unioned WHERE clause.
   * @param baseWhere The WHERE clause to convert and duplicate
   * @return The UNIONed WHERE clauses with the variables adjusted.
   */
  ConstraintExpression distributeIntoWhereClause(List<? extends SelectElement> described, ConstraintExpression baseWhere) throws MulgaraParserException {
    if (described == null || described.size() == 0) throw new MulgaraParserException("DESCRIBE clause must have at least one entry");
    baseWhere = (baseWhere == null) ? GRAPH_CONSTRAINT : new ConstraintConjunction(baseWhere, GRAPH_CONSTRAINT);
    List<ConstraintExpression> constraints = new LinkedList<ConstraintExpression>();
    for (SelectElement e: described) {
      ConstraintExpression expr;
      if (e instanceof ConstantValue) {
        expr = new ConstraintConjunction(GRAPH_CONSTRAINT, new ConstraintIs(GRAPH_VARS[0], ((ConstantValue)e).getValue()));
      } else if (e instanceof Variable) {
        expr = mapVariable(baseWhere, (Variable)e, GRAPH_VARS[0]);
      } else throw new MulgaraParserException("Illegal type in DESCRIBE clause: " + e);
      constraints.add(expr);
    }
    assert constraints.size() == described.size();
    return constraints.size() > 1 ? new ConstraintDisjunction(constraints) : constraints.get(0);
  }

  /**
   * Maps a ConstraintExpression to a new ConstraintExpression with all instance of one
   * {@link Variable} changed to another Variable.
   * @param base The ConstraintExpression to map.
   * @param from The Variable to find and replace.
   * @param to The new Variable to replace <var>from</var> with.
   * @return A new ConstraintExpression with all instances of <var>from</var> replaced
   *         with <var>to</var>.
   * @throws MulgaraParserException There was an error with the transformation.
   */
  ConstraintExpression mapVariable(ConstraintExpression base, Variable from, Variable to) throws MulgaraParserException {
    VariableRenameTransformer tx = new VariableRenameTransformer(from, to);
    try {
      return tx.transform(base);
    } catch (SymbolicTransformationException e) {
      throw new MulgaraParserException("Unable to convert WHERE clause for use in query.", e);
    }
  }


  ConstraintExpression constraintToNonBlank(ConstraintExpression expr) {
    // TODO: Consider using ConstraintConjunction(expr, ConstraintDisjunction(LITERAL_ONLY, URI_ONLY) when disjunction performance is fixed.
    ConstraintExpression literals = new ConstraintConjunction(expr, LITERAL_ONLY_CONSTRAINT);
    ConstraintExpression uris = new ConstraintConjunction(expr, URI_ONLY_CONSTRAINT);
    return new ConstraintDisjunction(literals, uris);
  }


  /**
   * Get the default graph to use in this system.
   * Without configuration, this value will be <code>sys:default</code>.
   * @return A list of the graphs to be used by default when no graphs are specified.
   */
  List<IRIReference> getSystemDefaultGraph() {
    URI d = ServerInfoRef.getDefaultURI();
    if (d != null) return Collections.singletonList(new IRIReference(d));
    // can't find a configured default, use the hard-coded default
    return toIRIs(INTERNAL_DEFAULT_GRAPH_URIS);
  }


  /**
   * Get the column names used in a Graph.
   * @return A list of Variables used when defining a graph.
   */
  static List<? extends SelectElement> graphVariables() {
    return Arrays.asList(GRAPH_VARS);
  }


  /**
   * Converts a collection of URIs to a collection of IRIReferences. If IRIReference becomes
   * more compliant then this may throw an exception.
   * @param uris The URI collection. If <code>null</code> then convert to an empty list.
   * @return A new List of IRIReferences.
   */
  static List<IRIReference> toIRIs(Collection<URI> uris) {
    if (uris == null) return Collections.emptyList();
    return C.map(uris, new Fn1<URI,IRIReference>(){public IRIReference fn(URI u) {return new IRIReference(u);}});
  }


  /**
   * Converts a collection of IRIReferences to a collections of URIs.
   * @param iris The IRIReference collection. If <code>null</code> then convert to an empty list.
   * @return A new List of URIs. These are extracted from the IRIs.
   */
  static List<URI> toURIs(Collection<IRIReference> iris) {
    if (iris == null) return Collections.emptyList();
    return C.map(iris, new Fn1<IRIReference,URI>(){public URI fn(IRIReference i) {return i.getUri();}});
  }
}
