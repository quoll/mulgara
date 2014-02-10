/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

// Java 2 standard packages
import static org.mulgara.util.ObjectUtil.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;

// Third party packages
// import org.apache.log4j.Logger; // Apache Log4J
import org.jrdf.graph.URIReference;

// Local classes
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.QueryEvaluationContext;

// FIXME: Need to work out how to delegate localizations and bindings.
import org.mulgara.resolver.ConstraintOperations;

/**
 * A constraint representing a relational query.
 *
 * @created 2005-05-02
 *
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 *
 * @version $Revision: 1.1.1.1 $
 *
 * @modified $Date: 2005/10/30 19:21:19 $ @maintenanceAuthor $Author: prototypo $
 *
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RelationalConstraint implements Constraint {

  // /** Logger */
  // private static Logger logger = Logger.getLogger(RelationalConstraint.class.getName());

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = -2930975307291404712L;

  private Set<ConstraintExpression> rdfTypeConstraints;

  private Map<ConstraintElement,List<Constraint>> predConstraints;

  private ConstraintElement model;

  private Set<Variable> variables;

  private static final URIReference rdftype;

  static {
    try {
      rdftype = new URIReferenceImpl(new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
    } catch (Exception e) {
      throw new IllegalStateException("Invalid rdf:type uri");
    }
  }

  /**
   * Sole constructor.
   */
  public RelationalConstraint() {
    this.rdfTypeConstraints = new HashSet<ConstraintExpression>();
    this.variables = new HashSet<Variable>();
    this.predConstraints = new HashMap<ConstraintElement,List<Constraint>>();
  }

  public RelationalConstraint(Constraint constraint) {
    this();

    conjoinWith(constraint);
  }

  public void conjoinWith(RelationalConstraint constraint) {
    preliminaries(constraint);

    rdfTypeConstraints.addAll(constraint.rdfTypeConstraints);

    for (ConstraintElement key: constraint.predConstraints.keySet()) {
      List<Constraint> lhs = constraint.predConstraints.get(key);
      List<Constraint> rhs = predConstraints.get(key);
      if (rhs == null) {
        predConstraints.put(key, lhs);
      } else {
        rhs.addAll(lhs);
      }
    }
  }

  public void conjoinWith(Constraint constraint) {
    preliminaries(constraint);

    if (constraint.getElement(1).equals(rdftype)) {
      rdfTypeConstraints.add(constraint);
    } else {
//    } else if (constraint.getElement(0) instanceof Variable) {
      List<Constraint> preds = predConstraints.get(constraint.getElement(0));
      if (preds == null) {
        preds = new ArrayList<Constraint>();
        predConstraints.put((ConstraintElement)constraint.getElement(0), preds);
      }
      preds.add(constraint);
    }
  }

  public void preliminaries(Constraint constraint) {
    if (model == null) {
      model = constraint.getModel();
    } else if (!model.equals(constraint.getModel())) {
      throw new IllegalStateException("Can't combine relational constraints against different models");
    }

    variables.addAll(constraint.getVariables());
  }

  public Set<ConstraintExpression> getRdfTypeConstraints() {
    return rdfTypeConstraints;
  }
  
  public List<Constraint> getConstraintsBySubject(ConstraintElement subj) {
    List<Constraint> list = predConstraints.get(subj);
    return list != null ? list : new ArrayList<Constraint>();
  }

  public ConstraintElement getModel() {
    return model;
  }

  public ConstraintElement getElement(int index) {
    throw new IllegalStateException("Cannot index RelationalConstraint");
  }

  public boolean isRepeating() {
    return false;
  }

  public Set<Variable> getVariables() {
    return variables;
  }

  /**
   * Not a binary operation, so not a binary constraint.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

  static RelationalConstraint localize(QueryEvaluationContext context, RelationalConstraint constraint) throws Exception {
    RelationalConstraint localized = new RelationalConstraint();

    // Aliasing may be a problem, not sure but subqueries might complain.
    // Not sure how it interacts with bindvariables.  If a problem then
    // deep copy.
    // We don't localize because the constraint manipulation is done in global space.
    localized.rdfTypeConstraints = constraint.rdfTypeConstraints;
    localized.predConstraints = constraint.predConstraints;
    localized.model = context.localize(constraint.model);

    localized.variables.addAll(constraint.variables);
    
    return localized;
  }

  static RelationalConstraint bind(Map<Variable,Value> bindings, RelationalConstraint constraint) throws Exception {
    RelationalConstraint bound = new RelationalConstraint();

    for (ConstraintExpression c: constraint.rdfTypeConstraints) {
      bound.rdfTypeConstraints.add(ConstraintOperations.bindVariables(bindings, c));
    }

    for (ConstraintElement key: constraint.predConstraints.keySet()) {
      List<Constraint> entry = constraint.predConstraints.get(key);
      List<Constraint> bentry = new ArrayList<Constraint>();
      for (Constraint jc: entry) {
        bentry.add((Constraint)ConstraintOperations.bindVariables(bindings, jc));
      }
      constraint.predConstraints.put(key, bentry);
    }

    bound.model = constraint.model; // Can't be a variable

    bound.variables.addAll(constraint.variables);
    bound.variables.removeAll(bindings.keySet());
    
    return bound;
  }

  // Not sure if permitting mutation here is a problem.
  // We might have to do a clone and edit in the descriptor.
  void rewriteModel(ConstraintElement newModel) {
    this.model = newModel;
  }


  public String toString() {
    return "RC{" + variables + "} | " + rdfTypeConstraints + " # " + predConstraints;
  }

  public boolean equals(Object o) {
    if (!(o instanceof RelationalConstraint)) return false;
    RelationalConstraint r = (RelationalConstraint)o;
    return eq(model, r.model) && eq(variables, r.variables) &&
           eq(rdfTypeConstraints, r.rdfTypeConstraints) && eq(predConstraints, r.predConstraints);
  }

}
