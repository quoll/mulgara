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

package org.mulgara.resolver.spi;

import java.util.ArrayList;
import java.util.List;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.And;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.SameTerm;
import org.mulgara.query.filter.value.Var;

/**
 * Transforms constraint expressions to remove duplicate variables, and replace the
 * existing constraints with filters that select matching variable values.
 *
 * @created May 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DuplicateVariableTransformer extends AbstractSymbolicTransformer {
  /**
   * All the work of this class is performed in this method. It ignores general constraints,
   * and converts a ConstraintImpls with repeated variables into a conjunction of terms
   * which have non-repeating variables, joined in an equivalent way to the original constraint.
   * @param c The constraint to transform.
   * @return The original constraint, or else a new equivalent conjunction if expr contains
   *         a repeated variable.
   * @throws SymbolicTransformationException If there is an error in the constraint structure.
   */
  @Override
  protected ConstraintExpression transformConstraint(SymbolicTransformationContext context, Constraint c)
      throws SymbolicTransformationException {
    if (!c.isRepeating()) return c;
    ConstraintImpl cnstr = (ConstraintImpl)c;
    VarFreq vf = new VarFreq(cnstr);

    // build the equivalent term
    ConstraintElement[] elements = buildElements(cnstr, vf);
    c = new ConstraintImpl(elements[0], elements[1], elements[2], elements[3]);

    // if there was only a pair then return it as a simple filter
    if (vf.frequency == 2) return new ConstraintFilter(c, createSameTermPair(vf.repeatedVar, 1));

    // build a conjunction of filters
    int matches = vf.frequency - 1;
    Filter[] sameTerms = new Filter[matches];
    for (int f = 0; f < matches; f++) sameTerms[f] = createSameTermPair(vf.repeatedVar, f + 1);
    return new ConstraintFilter(c, new And(sameTerms));
  }

  /**
   * Creates a filter which compares a pair of related variables for equality 
   * @param var The initial variable to compare.
   * @param offset The offset from the initial variable name. This is greater than 0.
   * @return A new filter that checks that both variables are equal.
   */
  private Filter createSameTermPair(Variable var, int offset) {
    assert offset > 0;
    return new SameTerm(new Var(var.getName()), new Var(offsetName(var, offset)));
  }

  /**
   * Creates the elements required for a new ConstraintImpl with no repeating variables.
   * @param constraint The original constraint with repeating variables.
   * @param vfStruct The structure of the variable repetition in the constraint.
   * @return A ConstraintElement array with the variables all made unique, and a cycle starting at offset.
   */
  private ConstraintElement[] buildElements(ConstraintImpl constraint, VarFreq vfStruct) {
    ConstraintElement[] ops = new ConstraintElement[4];
    int offset = 0;
    for (int e = 0; e < 4; e++) {
      ConstraintElement elt = constraint.getElement(e);
      if (!vfStruct.repeatedVar.equals(elt)) ops[e] = elt;
      else {
        ops[e] = createOffsetVar(vfStruct.repeatedVar, offset);
        offset = (offset + 1);
      }
    }
    return ops;
  }

  /**
   * Creates an internal variable. The name contains characters that are illegal in
   * the query language, so they should not cause any conflicts.
   * @param startVar The variable to base the new variable from.
   * @param offset The change from the original variable.
   * @return A variable with name related to the name of startVar by the value of offset,
   *         or the original startVar if offset is 0.
   */
  private Variable createOffsetVar(Variable startVar, int offset) {
    if (offset == 0) return startVar;
    return new Variable(offsetName(startVar, offset));
  }

  /**
   * Creates a new name for an internal variable, based on an initial variable
   * and an offset from that variable.
   * @param v The initial variable.
   * @param offset The offset from the initial variable
   * @return A new variable name.
   */
  private String offsetName(Variable v, int offset) {
    return "*" + v.getName() + offset;
  }

  /**
   * Internal analysis of a ConstraintImpl to find the repeated variables,
   * and the resulting structure.
   */
  private static class VarFreq {
    /** The number of times the repeated variable appears */
    public final int frequency;
    /** The repeating variable */
    public final Variable repeatedVar;

    public VarFreq(ConstraintImpl constraint) throws SymbolicTransformationException {
      int frequency = 0;
      Variable repeatedVar = null;

      // find the repeating variable name, and frequency
      List<Variable> vars = new ArrayList<Variable>(4);
      for (int e = 0; e < 4; e++) {
        ConstraintElement elt = constraint.getElement(e);
        if (elt instanceof Variable) {
          if (vars.contains(elt)) {
            if (repeatedVar != null) {
              if (!repeatedVar.equals(elt)) continue;
            } else {
              repeatedVar = (Variable)elt;
              frequency++; // increment the initial count, since this variable has been seen before
            }
            frequency++;
          } else {
            vars.add((Variable)elt);
          }
        }
      }
      if (frequency < 2) throw new SymbolicTransformationException("No repeats found in a constraint that reported repetition.");
      if (repeatedVar == null) throw new SymbolicTransformationException("No repeating variable found, despite counting a repetition of: " + frequency);
      assert (vars.contains(repeatedVar));
      this.frequency = frequency;
      this.repeatedVar = repeatedVar;
    }
  }
}
