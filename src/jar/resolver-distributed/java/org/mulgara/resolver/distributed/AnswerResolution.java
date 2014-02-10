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

package org.mulgara.resolver.distributed;

import org.apache.log4j.Logger;  // Apache Log4J

import org.jrdf.graph.Node;
import org.mulgara.query.Answer;
import org.mulgara.query.Constraint;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.BlankNodeImpl;

import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.LocalizedTuples;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Resolution;

import java.net.URI;

/**
 * A {@link Resolution} which extends a LocalizedTuples, which in turn wraps an Answer.
 *
 * @created 2007-03-23
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
class AnswerResolution extends LocalizedTuples implements Resolution {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(AnswerResolution.class.getName());

  /** The constraint. */
  private final Constraint constraint;
  
  /** The server URI being accessed. */
  private final URI serverUri;


  /**
   * Construct an AnswerResolution, passing most of the work off to the LocalizedTuples.
   * @param session The current session.
   * @param answer The answer to be wrapped by the parent class.
   * @param constraint the constraint.
   * @throws IllegalArgumentException if <var>constraint<var> is <code>null</code>
   */
  AnswerResolution(URI serverUri, ResolverSession session, Answer answer, Constraint constraint) throws TuplesException {
    super(session, answer);
    logger.debug("Constructed AnswerResolution for distributed resolver");
    if (constraint == null) throw new IllegalArgumentException("Null constraint parameter");
    this.constraint = constraint;
    this.serverUri = serverUri;
    logger.debug("Created resolution on server: " + serverUri + " for: " + constraint);
  }


  /**
   * Get the constraint leading to this resolution.
   * @return The constraint for the resolution.
   */
  public Constraint getConstraint() {
    return constraint;
  }


  /**
   * {@inheritDoc}
   */
  public boolean isComplete() {
    return true;
  }

  /**
   * Get the bound value for the column, converting Blank Nodes to a
   * remote representation when needed.
   * @param column The column of the bound value.
   * @return the Localized long integer for the bound value.
   * @throws TuplesException Indicates an error getting the value from the string pool.
   */
  public long getColumnValue(int column) throws TuplesException {
    try {
      Object obj = answer.getObject(column);
      assert obj instanceof Node;

      Node node = obj instanceof BlankNodeImpl ? new ForeignBlankNode(serverUri, (BlankNodeImpl)obj) : (Node)obj;
      return session.localize(node);
    } catch (LocalizeException e) {
      throw new TuplesException("Couldn't localize column " + column, e);
    }
  }
}
