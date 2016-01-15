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

// Java 2 standard packages
import static org.mulgara.krule.RuleStructure.UNINITIALIZED;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.query.Answer;
import org.mulgara.query.AnswerImpl;
import org.mulgara.query.Cursor;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.OperationContext;
import org.mulgara.resolver.spi.LocalizedTuples;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.store.tuples.Tuples;

/**
 * Represents a single executable rule.
 *
 * @created 2005-5-16
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @version $Revision: 1.2 $
 * @modified $Date: 2005/06/30 01:12:28 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Rule implements Serializable {

  private static final long serialVersionUID = 3080424724378196982L;

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(Rule.class.getName());

  /** The name of this rule. */
  protected String name;

  /** The rules to be triggered when this rule generates statements.*/
  private Set<Rule> triggerSet;

  /** The query for this rule. This contains the information for the base model. */
  protected Query query;

  /** The graph receiving the inferred data. */
  private long targetGraph = UNINITIALIZED;

  /** The most recent size of the data matching this rule. */
  private long lastCount;

  /** The structure containing this rule */
  protected RuleStructure ruleStruct;

  // TODO: Change this to a map of constraints to longs

  /**
   * Principle constructor.
   *
   * @param name The name of the rule.
   */
  public Rule(String name) {
    triggerSet = new HashSet<Rule>();
    this.name = name;
  }


  /**
   * Gets the name of the rule.
   *
   * @return The rule name.
   */
  public String getName() {
    return name;
  }


  /**
   * Adds a target for triggering.
   *
   * @param target The rule to be triggered when this rule is executed.
   */
  public void addTriggerTarget(Rule target) {
    triggerSet.add(target);
  }


  /**
   * Set the target graph for the rule.
   *
   * @param target The URI of the model to insert inferences into.
   */
  public void setTargetGraph(long target) {
    targetGraph = target;
  }


  /**
   * Sets the query for this rule. Must be a valid query for inserting data, meaning that it
   * returns a multiple of 3 elements.
   *
   * @param queryStruct The query which retrieves data for this rule.
   * @throws KruleStructureException If the query does not return a multiple of 3 elements.
   */
  public void setQueryStruct(QueryStruct queryStruct) throws KruleStructureException {
    int e = queryStruct.elementCount();
    if (e == 0 || e % 3 != 0) throw new KruleStructureException("Rule \"" + name + "\" attempting to generate the wrong number of elements (must be a multiple of 3): " + e);
    this.query = queryStruct.extractQuery();
  }


  /**
   * Retrieves the query from this rule.
   *
   * @return The query which retrieves data for this rule.
   */
  public Query getQuery() {
    return query;
  }


  /**
   * Retrieves the list of subordinate rules.
   *
   * @return an immutable set of the subordinate rules.
   */
  public Set<Rule> getTriggerTargets() {
    return Collections.unmodifiableSet(triggerSet);
  }


  /**
   * Sets the rule system structure containing this rule.
   * 
   * @param ruleStruct The structure for this rule.
   */
  public void setRuleStruct(RuleStructure ruleStruct) {
    this.ruleStruct = ruleStruct;
  }


  /**
   * Runs this rule.
   * TODO: count the size of each individual constraint
   * TODO: Go back to using a Session once they have been properly refactored for transactions
   * 
   * @param context The context to query against.
   * @param resolver The resolver to add data with.
   * @param sysResolver The resolver to localize data with.
   */
  public void execute(OperationContext context, Resolver resolver, SystemResolver sysResolver) throws QueryException, TuplesException, ResolverException {
    if (targetGraph == UNINITIALIZED) throw new IllegalStateException("Target graph has not been set");
    // see if this rule needs to be run
    Answer answer = null;
    try {
      answer = context.doQuery(query);
    } catch (Exception e) {
      throw new QueryException("Unable to access data in rule.", e);
    }
    try {
      // compare the size of the result data
      long newCount = answer.getRowCount();
      if (newCount == lastCount) {
        logger.debug("Rule <" + name + "> is up to date.");
        // this rule does not need to be run
        return;
      }
      logger.debug("Rule <" + name + "> has increased by " + (newCount - lastCount) + " entries");
      logger.debug("Inserting results of: " + query);
      if (answer instanceof AnswerImpl) {
        AnswerImpl a = (AnswerImpl)answer;
        String list = "[ ";
        Variable[] v = a.getVariables();
        for (int i = 0; i < v.length; i++) {
          list += v[i] + " ";
        }
        list += "]";
        logger.debug("query has " + a.getNumberOfVariables() + " variables: " + list);
      }
      // insert the resulting data
      insertData(answer, resolver, sysResolver);
      // update the count
      lastCount = newCount;
      logger.debug("Insertion complete, triggering rules for scheduling.");
    } finally {
      answer.close();
    }
    // trigger subsequent rules
    scheduleTriggeredRules();
  }


  /**
   * Schedule subsequent rules.
   */
  private void scheduleTriggeredRules() {
    Iterator<Rule> it = triggerSet.iterator();
    while (it.hasNext()) ruleStruct.schedule(it.next());
  }


  /**
   * Inserts an Answer into the data store on the current transaction.
   * @param answer The data to be inserted.
   * @param resolver The mechanism for adding data in the current transaction.
   * @param sysResolver Used for localizing the globalized data in the answer parameter.
   *        TODO: use a localized Tuples instead of Answer when src and dest are on the same server.
   * @throws TuplesException There was an error localizing the answer.
   * @throws ResolverException There was an error inserting the data.
   */
  private void insertData(Answer answer, Resolver resolver, SystemResolver sysResolver) throws TuplesException, ResolverException {
    Statements statements = convertToStatements(answer, sysResolver);
    try {
      resolver.modifyModel(targetGraph, statements, true);
    } finally {
      statements.close();
    }
  }


  /**
   * Converts an Answer with a multiple of 3 selection values to a set of statements for insertion.
   * @param answer The answer to convert.
   * @param resolver The resolver used for localizing the results, since Answers are globalized.
   *        TODO: remove this round trip of local->global->local.
   * @return A set of Statements.
   * @throws TuplesException The statements could not be instantiated.
   */
  private Statements convertToStatements(Answer answer, SystemResolver resolver) throws TuplesException {
    assert answer.getVariables().length % 3 == 0;
    return new TuplesStatements(new LocalizedTuples(resolver, answer, true));
  }
  
  /**
   * Wrapper for converting a Tuples to a Statements object.  Unlike the
   * TuplesWrapperStatements class, this class handles Tuples whose row length is
   * an arbitrary multiple of 3.  Subject, predicate, and object are determined
   * by column index in the Tuples, not by a variable mapping as is the case
   * in TuplesWrapperStatements.
   */
  protected static class TuplesStatements implements Statements, Cloneable {
    
    private static List<Variable> variables = 
      Arrays.asList(Statements.SUBJECT, Statements.PREDICATE, Statements.OBJECT);
    
    private static int statementSize = variables.size();
    
    private int wrappedRowLength;
    private int statementsPerRow;
    private int offsetInRow = 0;
    private Tuples tuples;
    
    /**
     * Construct a wrapper around the tuples.
     * @param tuples The Tuples to convert to statements; the number of columns
     * must be a multiple of three.
     */
    public TuplesStatements(Tuples tuples) {
      this.tuples = tuples;
      if (tuples.getNumberOfVariables() % 3 != 0) throw new IllegalArgumentException("Number of variables must be a multiple of 3");
      this.wrappedRowLength = tuples.getNumberOfVariables();
      this.statementsPerRow = wrappedRowLength / statementSize;
    }

    public long getObject() throws TuplesException {
      return tuples.getColumnValue(offsetInRow + 2);
    }

    public long getPredicate() throws TuplesException {
      return tuples.getColumnValue(offsetInRow + 1);
    }

    public long getSubject() throws TuplesException {
      return tuples.getColumnValue(offsetInRow);
    }

    public void beforeFirst() throws TuplesException {
      // Setting the offset past the end of the row forces a call to tuples.next()
      offsetInRow = wrappedRowLength;
      tuples.beforeFirst();
    }

    public Object clone() {
      try {
        TuplesStatements cloned = (TuplesStatements)super.clone();
        cloned.tuples = (Tuples)tuples.clone();
        return cloned;
      } catch (CloneNotSupportedException e) {
        throw new Error("Clone ought to be supported.");
      }
    }

    public void close() throws TuplesException {
      tuples.close();
    }

    public int getColumnIndex(Variable column) throws TuplesException {
      return variables.indexOf(column);
    }

    public int getNumberOfVariables() {
      return variables.size();
    }

    public int getRowCardinality() throws TuplesException {
      int cardinality = tuples.getRowCardinality();
      if (cardinality == Cursor.ONE && statementsPerRow > 1) {
        cardinality = Cursor.MANY;
      }
      return cardinality;
    }

    public boolean isEmpty() throws TuplesException {
      return tuples.isEmpty();
    }

    public long getRowCount() throws TuplesException {
      if (statementsPerRow > 1) {
        BigInteger rowCount = BigInteger.valueOf(tuples.getRowCount());
        rowCount = rowCount.multiply(BigInteger.valueOf(statementsPerRow));
        return rowCount.bitLength() > 63 ? Long.MAX_VALUE : rowCount.longValue();
      }
      return tuples.getRowCount();
    }

    public long getRowUpperBound() throws TuplesException {
      if (statementsPerRow > 1) {
        BigInteger rowBound = BigInteger.valueOf(tuples.getRowUpperBound());
        rowBound = rowBound.multiply(BigInteger.valueOf(statementsPerRow));
        return rowBound.bitLength() > 63 ? Long.MAX_VALUE : rowBound.longValue();
      }
      return tuples.getRowUpperBound();
    }

    public long getRowExpectedCount() throws TuplesException {
      if (statementsPerRow > 1) {
        BigInteger rowExpected = BigInteger.valueOf(tuples.getRowExpectedCount());
        rowExpected = rowExpected.multiply(BigInteger.valueOf(statementsPerRow));
        return rowExpected.bitLength() > 63 ? Long.MAX_VALUE : rowExpected.longValue();
      }
      return tuples.getRowUpperBound();
    }

    public Variable[] getVariables() {
      return variables.toArray(new Variable[statementSize]);
    }

    public boolean isUnconstrained() throws TuplesException {
      return tuples.isUnconstrained();
    }

    public boolean next() throws TuplesException {
      offsetInRow += statementSize;
      if (offsetInRow >= wrappedRowLength) {
        offsetInRow = 0;
        return tuples.next();
      }
      return true;
    }
    
  }
}
