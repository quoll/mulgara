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
import java.net.URI;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.OperationContext;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.resolver.spi.TripleSetWrapperStatements;
import org.mulgara.rules.InitializerException;
import org.mulgara.rules.Rules;
import org.mulgara.rules.RulesException;
import org.mulgara.server.Session;

/**
 * Represents a structure of rules.
 *
 * @created 2005-5-16
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @version $Revision: 1.3 $
 * @modified $Date: 2005/07/03 12:53:41 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RuleStructure implements Rules, Serializable {

  private static final long serialVersionUID = 7891222973611830607L;

  /** Used to indicate that a gNode is not configured */
  static final long UNINITIALIZED = -1;

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(RuleStructure.class.getName());

  /** The rules in the framework. */
  private Set<Rule> rules;

  /** Map of rule names to the rule. */
  private Map<String,Rule> ruleMap;

  /** The URI of the target graph to contain the entailments. */
  private URI targetGraphURI;

  /** The target graph to contain the entailments. */
  private long targetGraph = UNINITIALIZED;

  /** The current list of rules that have to be run. */
  private LinkedHashSet<Rule> runQueue;

  /** The set of axioms pertinent to these rules. */
  private Set<org.jrdf.graph.Triple> axioms;


  /**
   * Principle constructor.
   */
  public RuleStructure() {
    rules = new HashSet<Rule>();
    ruleMap = new HashMap<String,Rule>();
    runQueue = new LinkedHashSet<Rule>();
    axioms = null;
  }


  /**
   * Adds a new rule to the rule set.
   *
   * @param rule The rule to be added.
   */
  public void add(Rule rule) {
    rules.add(rule);
    ruleMap.put(rule.getName(), rule);
    rule.setRuleStruct(this);
  }


  /**
   * Links a pair of rules for triggering.
   *
   * @param src The name of the trigger generating rule.
   * @param dest The name of the rule which is triggered.
   */
  public void setTrigger(String src, String dest) throws InitializerException {
    // get the rules
    Rule srcRule = ruleMap.get(src);
    Rule destRule = ruleMap.get(dest);
    // check that the rules exist
    if (srcRule == null || destRule == null) {
      throw new InitializerException("Nonexistent rule: " + srcRule == null ? src : dest);
    }

    // set the link
    srcRule.addTriggerTarget(destRule);
  }


  /**
   * Sets a set of axioms for these rules.
   *
   * @param axioms A {@link java.util.Set} of {@link org.jrdf.graph.Triple}s
   *   comprising axiomatic statements.
   */
  public void setAxioms(Set<org.jrdf.graph.Triple> axioms) {
    this.axioms = axioms;
  }
  
  
  /**
   * Adds a single axiom to these rules.
   * 
   * @param axiom A triple comprising an axiomatic statement.
   */
  public void addAxiom(org.jrdf.graph.Triple axiom) {
    if (axioms == null) axioms = new HashSet<org.jrdf.graph.Triple>();
    axioms.add(axiom);
  }


  /**
   * Gets the number of axioms for these rules.
   *
   * @return The number of axiomatic statements.
   */
  public int getAxiomCount() {
    return axioms == null ? 0 : axioms.size();
  }


  /**
   * Returns the number of rules.
   *
   * @return The number of rules.
   */
  public int getRuleCount() {
    return rules.size();
  }


  /**
   * Returns an iterator for the rules.
   *
   * @return An iterator for the rules.
   */
  public Iterator<Rule> getRuleIterator() {
    return rules.iterator();
  }


  /**
   * Debug method to view the contents of a rule structure.
   * 
   * @return A string representation of this structure.
   */
  public String toString() {
    String result = "Rules = {\n";
    for (Rule r: rules) result += r.getName() + "\n";
    result += "}";
    return result;
  }


  /**
   * Set the target model for the rules. This will get localized when the rules are run.
   *
   * @param target The URI of the target graph to insert inferences into.
   */
  public void setTargetModel(URI target) {
    targetGraphURI = target;
  }


  /**
   * Starts the rules engine.  This is a breadth first evaluation engine.
   * This means that any triggered rules are scheduled for evaluation,
   * and are not run immediately.
   *
   * @param params An array containing the transactionally controlled {@link OperationContext}
   *        and {@link SystemResolver} to use. This is not a structured parameter as it will
   *        eventually drop back to a single {@link Session} parameter like it used to be. 
   */
  public void run(Object params) throws RulesException {
    logger.debug("Run called");
    validateParams(params);
    // set up the operating parameters
    OperationContext context = (OperationContext)((Object[])params)[0];
    SystemResolver systemResolver = (SystemResolver)((Object[])params)[1];

    // determine the graph to insert into, and the resolver for that graph
    localizeRuleTarget(context, systemResolver);
    Resolver resolver = extractTargetResolver(context);

    // set up the run queue
    runQueue = new LinkedHashSet<Rule>(rules);
    // fill the run queue
    runQueue.addAll(rules);
    Rule currentRule = null;
    try {
      // start by inserting the axioms
      insertAxioms(resolver, systemResolver);
      // process the queue
      while (runQueue.size() > 0) {
        // get the first rule from the queue
        currentRule = popRunQueue();
        logger.debug("Executing rule: " + currentRule);
        // execute the rule
        currentRule.execute(context, resolver, systemResolver);
      }
    } catch (TuplesException te) {
      logger.error("Error getting data within rule: " + currentRule);
      throw new RulesException("Error getting data within rule: " + currentRule, te);
    } catch (ResolverException re) {
      logger.error("Error inserting data from rule: " + currentRule);
      throw new RulesException("Error inserting data from rule: " + currentRule, re);
    } catch (QueryException qe) {
      logger.error("Error executing rule: " + currentRule, qe);
      throw new RulesException("Error executing rule: " + currentRule, qe);
    }
    logger.debug("All rules complete");
  }


  /**
   * Schedules a rule to be run.
   * 
   * @param rule The rule to schedule.
   */
  public void schedule(Rule rule) {
    logger.debug("Scheduling: " + rule.getName());
    // re-insertions do NOT affect the order in the queue
    runQueue.add(rule);
  }


  /**
   * Remove the head of the queue.
   *
   * @return The first item in the queue.
   */
  private Rule popRunQueue() {
    // get an iterator for the queue
    Iterator<Rule> iterator = runQueue.iterator();
    // this queue must have data in it
    assert iterator.hasNext();
    Rule head = iterator.next();
    iterator.remove();
    return head;
  }


  /**
   * Inserts all axioms into the output model in the current session.
   *
   * @param resolver The resolver to use for writing.
   */
  private void insertAxioms(Resolver resolver, ResolverSession resolverSession) throws QueryException {
    logger.debug("Inserting axioms");
    // check if axioms were provided
    if (axioms == null) {
      logger.debug("No axioms provided");
      return;
    }
    try {
      // Create the statements
      Statements stmts = new TripleSetWrapperStatements(axioms, resolverSession, TripleSetWrapperStatements.PERSIST);
      // insert the statements
      resolver.modifyModel(targetGraph, stmts, true);
    } catch (TuplesException te) {
      throw new QueryException("Unable to convert axioms for storage", te);
    } catch (ResolverException re) {
      throw new QueryException("Unable to store axioms", re);
    }
  }


  /**
   * Calculate the localized gNode for the target graph, and update all rules with this info.
   * @param context The context to query for the canonical form of the graph.
   * @param sysResolver The resolver to localize the graph info on.
   * @throws RulesException If the graph URI could not be localized.
   */
  private void localizeRuleTarget(OperationContext context, SystemResolver sysResolver) throws RulesException {
    try {
      targetGraph = sysResolver.localize(new URIReferenceImpl(targetGraphURI));
    } catch (LocalizeException e) {
      throw new RulesException("Unable to make a determination on the destination graph: " + targetGraphURI, e);
    }
    targetGraph = context.getCanonicalModel(targetGraph);
    // tell the rules which graph they need to modify
    for (Rule rule: rules) rule.setTargetGraph(targetGraph);
  }


  /**
   * Determine the resolver to use for modifications to the target graph.
   * @param context The context to extract the resolver from.
   * @return A resolver associated with the current transactional context.
   * @throws RulesException If a resolver could not be obtained.
   */
  private Resolver extractTargetResolver(OperationContext context) throws RulesException {
    if (targetGraph == UNINITIALIZED) throw new IllegalStateException("Target graph has not been resolved");
    try {
      return context.obtainResolver(context.findModelResolverFactory(targetGraph));
    } catch (QueryException e) {
      throw new RulesException("Unable to get access to modify the target graph: " + targetGraphURI);
    }
  }


  /**
   * Confirm that the parameters for {@link #run(Object)} are the expected type, since
   * they are not subject to static type checking. The complexity of this object will be
   * reduced when it is replaced by a {@link Session} again.
   * @param params The parameters to {@link #run(Object)}. For the moment this is a 2 element
   *        array of Object, with elements of {@link OperationContext} and {@link SystemResolver}.
   * @throws IllegalArgumentException If the parameters are not of the expected form.
   */
  private void validateParams(Object params) {
    if (!(params instanceof Object[])) {
      throw new IllegalArgumentException("Rules must be run with parameters of OperationContext/SystemResolver");
    }
    if (!(((Object[])params)[0] instanceof OperationContext)) {
      throw new IllegalArgumentException("Rules must be run with an OperationContext");
    }
    if (!(((Object[])params)[1] instanceof SystemResolver)) {
      throw new IllegalArgumentException("Rules must be run with a SystemResolver");
    }
  }
}
