/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.lucene;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jrdf.graph.URIReference;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.SymbolicTransformationException;
import static org.mulgara.util.ObjectUtil.eq;

/**
 * A constraint representing a lucene query and a score.
 *
 * @created 2008-09-28
 * @author Ronald Tschalär
 * @licence Apache License v2.0
 */
public class LuceneConstraint implements Constraint {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LuceneConstraint.class);
  private static final long serialVersionUID = 1L;

  private ConstraintElement model;
  private ConstraintElement subject;
  private ConstraintElement predicate;
  private ConstraintElement object;
  private Variable binder;
  private Variable score;

  /**
   * Empty constructor. Used internally during constraint rewriting.
   */
  private LuceneConstraint() {
  }

  /**
   * Create a new lucene constraint wrapping a non-lucene constraint.
   *
   * @param constraint the underlying constraint to wrap
   */
  LuceneConstraint(Constraint constraint) {
    this();

    subject = constraint.getElement(0);
    predicate = constraint.getElement(1);
    object = constraint.getElement(2);
    model = constraint.getModel();
  }

  /**
   * Create a new lucene constraint.
   *
   * @param constraint the first raw constraint
   * @param searchPred our predicate indicating a lucene search
   * @param scorePred  our predicate indicating the variable to hold the score
   * @throws SymbolicTransformationException
   */
  LuceneConstraint(Constraint constraint, URIReference searchPred, URIReference scorePred)
      throws SymbolicTransformationException {

    // extract model
    model = constraint.getModel();

    ConstraintElement s = constraint.getElement(0);
    ConstraintElement p = constraint.getElement(1);
    ConstraintElement o = constraint.getElement(2);

    // extract predicate, object, score
    if (p.equals(searchPred)) {
      if (s.equals(o)) {
        throw new SymbolicTransformationException("subject and object of '" + searchPred +
                                                  "' may not be the same: " + s);
      }
      subject = s;
      assignBinder(o);
    } else if (p.equals(scorePred)) {
      if (!(o instanceof Variable)) {
        throw new SymbolicTransformationException("Lucene query score must be a variable: " + o);
      }
      score = (Variable)o;
      assignBinder(s);
    } else {
      subject = s;
      predicate = p;
      object = o;
    }
  }

  private final void assignBinder(ConstraintElement b) throws SymbolicTransformationException {
    if (!(b instanceof Variable)) {
      throw new SymbolicTransformationException("Lucene query binder must be a variable: " + b);
    }

    binder = (Variable)b;
  }

  /**
   * Merge the given constraint into this lucene constraint.
   *
   * @param constraint another raw constraint
   */
  void conjoinWith(LuceneConstraint constraint) throws SymbolicTransformationException {
    model = getNoDup(constraint.model, model, "Can't combine lucene constraints against different models", "model");

    if (binder != null && constraint.binder != null) {
      subject = getNoDup(constraint.subject, subject, "Can't combine lucene constraints with different subjects", "subj");
      assignBinder(getNoDup(constraint.binder, binder, "Mismatched binder variable", "var"));
    } else if (binder != null) {
      assignBinder(getNoDup(constraint.subject, binder, "Mismatched binder variable", "var"));
    } else if (constraint.binder != null) {
      assignBinder(getNoDup(constraint.binder, subject, "Mismatched binder variable", "var"));
      subject = constraint.subject;
    } else {
      subject = getNoDup(constraint.subject, subject, "Can't combine lucene constraints with different subjects", "subj");
    }

    predicate = getNoDup(constraint.predicate, predicate, "Only one predicate supported per search", "pred");
    object = getNoDup(constraint.object, object, "Only one object supported per search", "obj");

    score = getNoDup(constraint.score, score, "Only one score supported per search", "score");
  }

  private static <T extends ConstraintElement> T getNoDup(T elem, T existing, String msg, String elemType)
      throws SymbolicTransformationException {
    if (existing == null) {
      return elem;
    } else if (elem == null) {
      return existing;
    } else if (existing.equals(elem)) {
      return existing;
    } else {
      throw new SymbolicTransformationException(msg + ": " + elemType + "1=" + existing + ", " +
                                                elemType + "2=" + elem);
    }
  }

  /**
   * Do some basic validations on the finished constraint.
   *
   * @throws SymbolicTransformationException if validation fails
   */
  void validate() throws SymbolicTransformationException {
    if (predicate == null)
      throw new SymbolicTransformationException("Missing predicate for lucene constraint: " +
                                                "subject=" + subject + ", binder=" + binder);

    if (subject == null && score != null)
      throw new SymbolicTransformationException("Missing <mulgara:search> for lucene constraint: " +
                                                "binder=" + binder + ", predicate=" + predicate +
                                                ", query=" + object + ", score=" + score);
  }

  public ConstraintElement getModel() {
    return model;
  }

  public ConstraintElement getElement(int index) {
    throw new UnsupportedOperationException("Cannot index LuceneConstraint");
  }

  public boolean isRepeating() {
    return false;
  }

  public Set<Variable> getVariables() {
    Set<Variable> vars = new HashSet<Variable>();
    if (subject instanceof Variable)
      vars.add((Variable)subject);
    if (predicate instanceof Variable)
      vars.add((Variable)predicate);
    if (score != null)
      vars.add(score);
    return vars;
  }

  /** the subject of the search triple */
  ConstraintElement getSubject() {
    return subject;
  }

  /** the predicate of the search triple */
  ConstraintElement getPredicate() {
    return predicate;
  }

  /** the object of the search triple */
  ConstraintElement getObject() {
    return object;
  }

  /** the variable that binds the raw constraints */
  Variable getBindingVar() {
    return binder;
  }

  /** the variable that holds the score, or null */
  Variable getScoreVar() {
    return score;
  }

  static LuceneConstraint localize(QueryEvaluationContext context, LuceneConstraint constraint)
      throws Exception {
    LuceneConstraint localized = new LuceneConstraint();

    localized.subject = (constraint.subject != null) ? context.localize(constraint.subject) : null;
    localized.predicate = context.localize(constraint.predicate);
    localized.object = context.localize(constraint.object);
    localized.model = context.localize(constraint.model);

    localized.binder = constraint.binder;
    localized.score = constraint.score;

    return localized;
  }

  static LuceneConstraint bind(Map<Variable,Value> bindings, LuceneConstraint constraint)
      throws Exception {
    LuceneConstraint bound = new LuceneConstraint();

    Value val = bindings.get(constraint.subject);
    bound.subject = (val != null) ? val : constraint.subject;

    bound.predicate = constraint.predicate;
    bound.object = constraint.object;
    bound.model = constraint.model;

    bound.binder = constraint.binder;

    bound.score = constraint.score;

    return bound;
  }

  public String toString() {
    return "LC{subj=" + subject + ", pred=" + predicate + ", obj=" + object + ", score=" + score +
           ", binder=" + binder + "}";
  }

  public boolean equals(Object o) {
    if (!(o instanceof LuceneConstraint)) return false;
    LuceneConstraint l = (LuceneConstraint)o;
    return eq(model, l.model) && eq(subject, l.subject) && eq(predicate, l.predicate) &&
           eq(object, l.object) && eq(binder, l.binder) && eq(score, l.score);
  }

  /**
   * Not a binary operation, so not associative
   * @return <code>false</code>
   */
  public boolean isAssociative() {
    return false;
  }
}
