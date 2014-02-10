/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.krule;

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.OperationContext;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.SystemResolver;

/**
 * A rule that generates no data, but instead checks that the data is consistent.
 *
 * @created Mar 18, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ConsistencyCheck extends Rule {

  /** Serialization ID */
  private static final long serialVersionUID = 5514372363770138432L;

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(ConsistencyCheck.class.getName());


  /**
   * Creates the consistency test as a rule.
   * @param name The name of this rule.
   */
  public ConsistencyCheck(String name) {
    super(name);
  }


  /**
   * Adds a target for triggering. These are not legal for consistency checks.
   * @param target The rule to be triggered when this rule is executed.
   */
  public void addTriggerTarget(Rule target) {
    throw new IllegalStateException("Consistency checks cannot trigger other rules.");
  }


  /**
   * Retrieves the list of subordinate rules.
   * @return an immutable set of the subordinate rules.
   */
  public Set<Rule> getTriggerTargets() {
    return Collections.emptySet();
  }


  /**
   * Sets the query for this rule.
   * @param queryStruct The query which retrieves data for this rule.
   */
  public void setQueryStruct(QueryStruct queryStruct) throws KruleStructureException {
    this.query = queryStruct.extractQuery();
  }


  /**
   * Runs this test.
   * TODO: count the size of each individual constraint
   * 
   * @param context The context to query against.
   * @param resolver The resolver to add data with.
   * @param sysResolver The resolver to localize data with.
   */
  public void execute(OperationContext context, Resolver resolver, SystemResolver sysResolver) throws QueryException, TuplesException, ResolverException {
    // Tests the output of this rule
    Answer answer = null;
    logger.debug("Running consistency check: " + name);
    try {
      answer = context.doQuery(query);
    } catch (Exception e) {
      throw new QueryException("Unable to access data in rule.", e);
    }
    try {
      // compare the size of the result data
      long c = answer.getRowCount();
      if (0 != c) {
        if (logger.isDebugEnabled()) {
          logger.debug("Failed consistency check: " + name);
          logOutput(answer);
        }
        throw new QueryException("Consistency check failed for rule \"" + name + "\". Got " + c + " failure results.");
      }
    } finally {
      answer.close();
    }
  }


  /**
   * Send the result of a query to the logger.
   * @param ans The result of the query to log.
   */
  private void logOutput(Answer ans) {
    try {
      ans.beforeFirst();
      Variable[] vars = ans.getVariables();
      StringBuilder line = new StringBuilder();
      for (Variable v: vars) line.append(v).append(" ");
      logger.debug(line.toString());
      line = new StringBuilder();
      while (ans.next()) {
        for (int c = 0; c < vars.length; c++) {
          if (c != 0) line.append(", ");
          line.append(ans.getObject(c));
        }
        line.append("\n");
      }
      logger.debug(line);
    } catch (TuplesException e) {
      logger.error("Error reading failure in consistency check.", e);
    }
  }

}
