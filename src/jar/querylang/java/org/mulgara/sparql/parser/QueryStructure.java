/*
 * Copyright 2008 Fedora Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.sparql.parser;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mulgara.sparql.parser.cst.GroupGraphPattern;
import org.mulgara.sparql.parser.cst.IRIReference;
import org.mulgara.sparql.parser.cst.Node;
import org.mulgara.sparql.parser.cst.Ordering;
import org.mulgara.sparql.parser.cst.TripleList;
import org.mulgara.sparql.parser.cst.Variable;

/**
 * <p>CST data for SPARQL query parsing.</p>
 * <p>This interface serves as the main external interface for this library, and provides
 * access to all the structure of a parsed query. Some of the structural elements include
 * substructures from the {@link org.mulgara.sparql.parser.cst} packace, which need to be
 * interrogated as well.</p>
 * <p>This interface is returned by calling the static {@link SparqlParser#parse(String)}
 * method on {@link SparqlParser}.</p>
 *
 * @created February 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public interface QueryStructure {

  /**
   * <p>Retrieve the base namespace URI for the query.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#QSynIRI">SPARQL Query Language: 4.1.1 Syntax for IRIs</a>
   * @return The base namespace URI.
   */
  public URI getBase();

  /**
   * <p>Gets the list of prefix mappings.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#QSynIRI">SPARQL Query Language: 4.1.1 Syntax for IRIs</a>
   * @return a map of prefix labels to namespace URIs
   */
  public Map<String, URI> getPrefixes();

  /**
   * <p>Gets the type of the query.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#QueryForms">SPARQL Query Language: 10 Query Forms</a>
   * @return The query type, encoded by the {@link QueryType} enumeration.
   */
  public QueryType getType();

  /**
   * <p>Tests if this query should return distinct results.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modDistinct">SPARQL Query Language: 9.3.1 DISTINCT</a>
   * @return <code>true</code> if the query should return distinct results.
   */
  public boolean isDistinct();

  /**
   * <p>Tests if this query should return reduced results.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modReduced">SPARQL Query Language: 9.3.2 REDUCED</a>
   * @return <code>true</code> if the query should return reduced results.
   */
  public boolean isReduced();

  /**
   * <p>Tests if this query returns all its variables, and not a projected subset.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#solutionModifiers">SPARQL Query Language: 9 Solution Sequences and Modifiers</a>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modProjection">9.1 Projection</a>
   * @return <code>true</code> if the query should return all variables.
   */
  public boolean isSelectAll();

  /**
   * <p>Gets the list of {@link org.mulgara.sparql.parser.cst.Variable}s to project
   * the solution to. If {@link #isSelectAll()} returns <code>true</code> then this
   * list will contain all the variables from the query.</p>
   * <p>The returned list contains Node and not Variable to permit implementations
   * to modify the parser to return non-Variables, such as
   * {@link org.mulgara.sparql.parser.cst.IRIReference}s.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modProjection">SPARQL Query Language: 9.1 Projection</a>
   * @return A list of elements to return from the query.
   */
  public List<? extends Node> getSelection();

  /**
   * <p>Gets the template for constructing triples in a CONSTRUCT query.
   * Each triple in the template list can contain IRIReferences, Literals, Blank Nodes
   * and Variables, where all but the variables will be constructed directly, while the
   * variables will be bound from the results coming from the rest of the query. Refer
   * to the {@link org.mulgara.sparql.parser.cst.TripleList} and 
   * {@link org.mulgara.sparql.parser.cst.Triple} classes for details on the structure
   * of the template list.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#construct">SPARQL Query Language: 10.2 CONSTRUCT</a>
   * @return A list of triples containing a template for new triples to be constructed.
   */
  public TripleList getConstructTemplate();

  /**
   * <p>Gets the list of default graphs, as specified by the FROM clauses. If there were no
   * FROM clauses, then this list will be empty, indicating that the system should
   * query the system default graph.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#specifyingDataset">SPARQL Query Language: 8.2 Specifying RDF Datasets</a>
   * @return A list of graph IRIs. This list may be empty, indicating the use of the
   * system default graph.
   */
  public List<IRIReference> getDefaultFroms();

  /**
   * <p>Gets the list of named graphs, as specified by the FROM NAMED clauses. The contents
   * of this list specify which graphs may be referred to (either explicitly, or by
   * variable) by any GRAPH modifiers to patterns in the WHERE clause.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#specifyingDataset">SPARQL Query Language: 8.2 Specifying RDF Datasets</a>
   * <p>For details on how the GRAPH modifier refers to members from this named graph list:</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#queryDataset">8.3 Querying the Dataset</a>
   * @return The list of IRIs in the named graph list.
   */
  public List<IRIReference> getNamedFroms();

  /**
   * <p>Gets the WHERE clause for the query. This is the structure that defines all the
   * processing work required to get the results.</p>
   * <p>The returned structure is a tree of {@link org.mulgara.sparql.parser.cst.GroupGraphPattern}
   * elements, where all the leaves are {@link org.mulgara.sparql.parser.cst.Triple}s. Each
   * node in this tree may optionally have a FILTER applied to it.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#GraphPattern">SPARQL Query Language: 5 Graph Patterns</a>
   * @return The {@link org.mulgara.sparql.parser.cst.GroupGraphPattern} representing the
   *         WHERE clause for this query.
   */
  public GroupGraphPattern getWhereClause();

  /**
   * <p>Gets the list of required orderings. Orderings appear in the list in decreasing
   * ordering priority. An empty list means that the implementation is free to return
   * results in any order. This order should be consistent to allow a paged result
   * when issuing similar consecutive queries using differing OFFSET/LIMIT values, though
   * the specification explicitly disavows the need for this.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">SPARQL Query Language: 9.1 ORDER BY</a>
   * @return A list of {@link org.mulgara.sparql.parser.cst.Ordering} values, in order of priority.
   */
  public List<Ordering> getOrderings();

  /**
   * <p>Gets the numeric offset of the first row to be returned by the query. The default
   * is 0 offset, meaning to start at the beginning.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modResultLimit">SPARQL Query Language: 9.4 OFFSET</a>
   * @return The number of rows to skip in the result. A value of zero does nothing.
   */
  public int getOffset();

  /**
   * <p>Gets the maximum number of results to be returned by the query. A limit of -1 means
   * no limit at all (return all results). A limit of 0 means that no results should
   * be returned.</p>
   * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modResultLimit">SPARQL Query Language: 9.5 LIMIT</a>
   * @return The maximum number of rows to returned by the query.
   */
  public int getLimit();

  /**
   * Retrieve all the variables used anywhere in the query.
   * @return A Collection of variables used in this query.
   */
  public Collection<Variable> getAllVariables();

  /**
   * Emits a string representation of the original query.
   * @see java.lang.Object#toString()
   * @return A string containing an equivalent query to the original. This is unlikely
   *         to be exactly the same string as the original query.
   */
  public String toString();

}