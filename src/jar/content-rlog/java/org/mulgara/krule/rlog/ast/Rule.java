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

package org.mulgara.krule.rlog.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.TypeException;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;
import org.mulgara.util.functional.C;

/**
 * Represents a rule statement.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Rule extends Statement {

  /** The predicate found in the head of the rule. */
  public final Predicate head;

  /** The list of predicates found in the body of the rule. */
  public final List<Predicate> body;

  /** The list of predicates to be subtracted from the body of the rule. */
  public final List<Predicate> bodySubtractions;

  /** The collection of rules that this rule can trigger for execution. */
  public final Collection<Rule> triggers;
  
  /** The name of this rule. Used in RDF. */
  private String name;

  protected Rule(List<Predicate> body,  ParseContext context) {
    this(NullPredicate.NULL, body, context);
  }

  /**
   * This constructor takes a head and body for a statement. It normalizes the structure,
   * and checks for soundness.
   * @param head A Predicate containing a subset of variables from the body.
   * @param body A set of Predicates for the body.
   * @param context The context passed in from the parser.
   */
  public Rule(Predicate head, List<Predicate> body, ParseContext context) {
    super(context);
    this.head = head;
    this.body = body;
    this.bodySubtractions = new LinkedList<Predicate>();
    this.head.setParent(this);
    for (Predicate p: this.body) p.setParent(this);
    // shuffle the inversions out of the body and into the body subtractions
    normalizeBody(this.body, this.bodySubtractions);
    triggers = new HashSet<Rule>();
    try {
      checkVariables();
    } catch (IllegalArgumentException e) {
      System.err.println("head = '" + head + "'");
      System.err.println("body = '" + this.body + "'");
      throw e;
    }
  }

  /**
   * Get all variables referred to by this rule.
   * @return All variables from the body, since the variables in the head must be a subset.
   */
  public Collection<Var> getVariables() {
    Collection<Var> vars = new HashSet<Var>();
    for (Predicate p: body) vars.addAll(p.getVariables());
    for (Predicate p: bodySubtractions) vars.addAll(p.getVariables());
    return vars;
  }

  /**
   * Adds a rule into the set of rules to be triggered by this rule.
   * @param target The rule to be triggered.
   */
  public void addTrigger(Rule target) {
    triggers.add(target);
  }

  /**
   * Sets the name for this rule.
   * @param name The new name for the rule.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return The name of this rule.
   */
  public String getName() {
    return name;
  }

  // inheritdoc
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  // inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "Rule {");
    System.out.println(sp(indent + 1) + "head:");
    head.print(indent + 2);
    System.out.println(sp(indent + 1) + "body [length=" + body.size() + "]:");
    for (Predicate p: body) p.print(indent + 2);
    if (!bodySubtractions.isEmpty()) {
      System.out.print(sp(indent + 1) + "minus [length=" + bodySubtractions.size() + "]:");
      for (Predicate p: bodySubtractions) p.print(indent + 2);
    }
    System.out.println(sp(indent) + "}");
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (!(o instanceof Rule)) return false;
    Rule r = (Rule)o;
    return head.equals(r.head) && body.equals(r.body);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return head.hashCode() * 31 + body.hashCode();
  }

  /**
   * Tests if this rule is triggered by the given rule.
   * @param trigger The potential triggering rule.
   * @return <code>false</code> if this rule <em>cannot</em> be triggered by the trigger.
   * @throws TypeException If the product of the rule is an illegal type.
   * @throws URIParseException If the predicates in the rule result in illegal URIs.
   */
  public boolean triggeredBy(Rule trigger) throws TypeException, URIParseException {
    Predicate triggerProduct = trigger.head;
    if (triggerProduct instanceof InvertedPredicate) throw new TypeException("Unexpected inversion in a rule head.");
    if (triggerProduct == NullPredicate.NULL) return false;
    RDFNode subject = triggerProduct.getSubject();
    RDFNode predicate = triggerProduct.getPredicate();
    RDFNode object = triggerProduct.getObject();
    
    for (Predicate p: body) if (p.matches(subject, predicate, object)) return true;
    return false;
  }

  /**
   * Get all the URI references used in this rule.
   * @return All the references used in this rule.
   */
  public Set<URIReference> getReferences() throws URIParseException {
    Set<URIReference> refs = new LinkedHashSet<URIReference>();
    refs.addAll(head.getReferences());
    for (Predicate p: body) refs.addAll(p.getReferences());
    return refs;
  }

  /**
   * @return the head
   */
  public Predicate getHead() {
    return head;
  }

  /**
   * @return the body
   */
  public List<Predicate> getBody() {
    return Collections.unmodifiableList(body);
  }

  /**
   * @return the bodySubtractions
   */
  public List<Predicate> getBodySubtractions() {
    return Collections.unmodifiableList(bodySubtractions);
  }

  /**
   * @return the triggers
   */
  public Collection<Rule> getTriggers() {
    return triggers;
  }

  @Override
  public CanonicalStatement getCanonical() {
    List<CanonicalPredicate> list = new ArrayList<CanonicalPredicate>(body.size() + 1);
    // reorder the predicates
    for (Predicate p: body) C.ascendingInsert(list, p.getCanonical());
    return new CanonicalStatement(head.getCanonical(), list);
  }


  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuilder sb = new StringBuilder(head.toString());
    sb.append(" :- ");
    for (int b = 0; b < body.size(); b++) {
      if (b != 0) sb.append(", ");
      sb.append(body.get(b));
    }
    sb.append(".");
    return sb.toString();
  }


  /**
   * Checks that all variables in the head are found in the body, and that every subtracted predicate
   * contains at least one variable that appears in the standard matching predicates.
   * @throws IllegalArgumentException if there are any variables unaccounted for.
   */
  @SuppressWarnings("unchecked")
  private void checkVariables() {
    Collection<Var> headVars = head.getVariables();
    HashSet<Var> bodyVars = new HashSet<Var>();
    for (Predicate p: body) bodyVars.addAll(p.getVariables());
    headVars.removeAll(bodyVars);
    if (!headVars.isEmpty()) throw new IllegalArgumentException("Head of rule must contain variables from the body.");

    for (Predicate p: bodySubtractions) {
      HashSet<Var> vars = (HashSet<Var>)bodyVars.clone();
      vars.retainAll(p.getVariables());
      if (vars.isEmpty()) throw new IllegalArgumentException("Subtractions must match at least one variable in the original predicates.");
    }
  }

  /**
   * Remove all subtractions from the body, and place them in the bodySubtractions.
   */
  private void normalizeBody(List<Predicate> bdy, List<Predicate> subs) {
    Iterator<Predicate> preds = bdy.iterator();
    boolean changes = false;
    while (preds.hasNext()) {
      Predicate current = preds.next();
      if (current instanceof InvertedPredicate) {
        subs.add(((InvertedPredicate)current).getInvertPredicate());
        preds.remove();
        changes = true;
      }
    }
    // normalize for any nested inversions
    if (changes) normalizeBody(subs, bdy);
  }

}

