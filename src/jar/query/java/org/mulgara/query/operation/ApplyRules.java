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
import java.rmi.RemoteException;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphResource;
import org.mulgara.query.QueryException;
import org.mulgara.rules.InitializerException;
import org.mulgara.rules.RulesRef;
import org.mulgara.server.Session;

/**
 * Represents a command to apply rules to a set of data.
 *
 * @created Aug 10, 2007
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ApplyRules extends ServerCommand {

  /** The graph containing the rules to be run. */
  private final URI ruleGraph;
  
  /** The graph containing the data to apply the rules to. */
  private final GraphExpression baseGraph;
  
  /** The graph to put the rule productions into. */
  private final URI destGraph;
  
  /**
   * Create a new rules command.
   * @param ruleGraph The graph containing the rules to be run.
   * @param baseGraph The graph containing the data to apply the rules to.
   * @param destGraph The graph to put the rule productions into.
   */
  public ApplyRules(URI ruleGraph, URI baseGraph, URI destGraph) {
    this(ruleGraph, new GraphResource(baseGraph), destGraph);
  }
  
  /**
   * Create a new rules command.
   * @param ruleGraph The graph containing the rules to be run.
   * @param baseGraph The graph expression containing the data to apply the rules to.
   * @param destGraph The graph to put the rule productions into.
   */
  public ApplyRules(URI ruleGraph, GraphExpression baseGraph, URI destGraph) {
    super(destGraph);
    this.ruleGraph = ruleGraph;
    this.baseGraph = baseGraph;
    this.destGraph = destGraph;
  }
  
  /**
   * @return the ruleGraph
   */
  public URI getRuleGraph() {
    return ruleGraph;
  }

  /**
   * @return the baseGraph
   */
  public GraphExpression getBaseGraph() {
    return baseGraph;
  }

  /**
   * @return the destGraph
   */
  public URI getDestGraph() {
    return destGraph;
  }

  /**
   * Apply rules using the given connection.
   * @param conn The connection to make the rule application on.
   * @return A string containing the result message.
   * @throws InitializerException The rules were not structured correctly.
   * @throws QueryException Unable to read the rules.
   * @throws RemoteException There was a connectivity problem with the server.
   */
  public Object execute(Connection conn) throws QueryException, InitializerException {
    return execute(conn, conn);
  }

  /**
   * Apply rules using separate connections for getting rules and applying them.
   * @param conn The connection to apply the rules with.
   * @param ruleConn The connection to retrieve rules over.
   * @return A string containing the result message.
   * @throws InitializerException The rules were not structured correctly.
   * @throws QueryException Unable to read the rules.
   * @throws RemoteException There was a connectivity problem with the server.
   */
  public Object execute(Connection conn, Connection ruleConn) throws QueryException, InitializerException {
    if (conn == null) throw new IllegalArgumentException("Connection may not be null");
    if (ruleConn == null) throw new IllegalArgumentException("Rule Connection may not be null");
    // get the structure from the rule model
    final RulesRef rules;
    try {
      rules = ruleConn.execute(new SessionOp<RulesRef,Exception>() {
        public RulesRef fn(Session session) throws Exception {
          return session.buildRules(ruleGraph, baseGraph, destGraph);
        }
      });
    } catch (QueryException qe) {
      throw qe;
    } catch (InitializerException ie) {
      throw ie;
    } catch (Exception e) { // shouldn't happen.
      throw new QueryException("Unexpected exception building rules", e);
    }
    // create apply the rules to the model
    conn.execute(new SessionOp<Object,QueryException>() {
      public Object fn(Session session) throws QueryException {
        session.applyRules(rules);
        return null;
      }
    });
    return setResultMessage("Successfully applied " + ruleGraph + " to " + baseGraph + (GraphResource.sameAs(baseGraph, destGraph) ? "" : " => " + destGraph));
  }

}
