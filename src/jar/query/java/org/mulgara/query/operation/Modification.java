/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.operation;

import java.net.URI;
import java.util.Set;

import org.jrdf.graph.Triple;
import org.mulgara.query.Query;

public abstract class Modification extends SessionCommand {

  /** The graph to insert into. */
  protected final URI graph;
  /** A SELECT query where the results are to be inserted into the graph. */
  protected final Query selectQuery;
  /** A set of statements to be inserted into the graph. */
  protected final Set<Triple> statements;

  /**
   * Create a modification command for modifying a set of statements in a graph.
   * @param graph The graph to modify.
   * @param statements The data to be modified.
   */
  public Modification(URI graph, Set<Triple> statements){
    super(graph);
    this.graph = graph;
    this.statements = statements;
    this.selectQuery = null;
  }

  /**
   * Create a modification command for modifying the results of a query in a graph.
   * @param graph The graph to modify.
   * @param selectQuery The query to get data for modification.
   */
  public Modification(URI graph, Query selectQuery){
    super(graph);
    this.graph = graph;
    this.selectQuery = selectQuery;
    this.statements = null;
  }

  /**
   * Test is this insertion is based on a SELECT query.
   * @return <code>true</code> if the data to be inserted comes from a SELECT query.
   *         <code>false</code> otherwise.
   */
  public boolean isSelectBased() {
    assert selectQuery == null ^ statements == null;
    return selectQuery != null;
  }

  /**
   * @return the graph
   */
  public URI getGraph() {
    return graph;
  }

  /**
   * @return the selectQuery
   */
  public Query getSelectQuery() {
    return selectQuery;
  }

  /**
   * @return the statements
   */
  public Set<Triple> getStatements() {
    return statements;
  }

}
