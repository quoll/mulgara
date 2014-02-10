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

package org.mulgara.krule.rlog.ast;

/**
 * Represents a canonicalization of a predicate.
 *
 * @created Mar 4, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class CanonicalPredicate implements Comparable<CanonicalPredicate> {

  /** The number of elements for a binary predicate. */
  private static final int BINARY_LENGTH = 3;

  /** The number of elements for a unary predicate. */
  private static final int UNARY_LENGTH = 2;

  /** The number of elements for a null predicate. */
  private static final int NULL_LENGTH = 0;

  /** The different in ID between inverted predicates and the original predicate. */
  private static final int INVERT_DIFF = 8;

  /** The elements of the predicate */
  private PredicateParam[] elements;

  /** A flag to indicate that this predicate is inverted. */
  private boolean invertFlag = false;

  /** An ID used internally for comparisons between differing types. */
  private int typeId;

  /**
   * Create a new canonicalized form for a null predicate.
   */
  CanonicalPredicate() {
    elements = new PredicateParam[NULL_LENGTH];
    typeId = elements.length;
  }


  /**
   * Create a new canonicalized form for a binary predicate.
   * @param s The subject of the predicate.
   * @param p The value of the predicate.
   * @param o The object of the predicate.
   */
  CanonicalPredicate(PredicateParam s, PredicateParam p, PredicateParam o) {
    elements = new PredicateParam[BINARY_LENGTH];
    elements[0] = s;
    elements[1] = p;
    elements[2] = o;
    typeId = elements.length;
  }


  /**
   * Create a new canonicalized form for a unary predicate.
   * @param t The type of the predicate. 
   * @param v The value of the predicate.
   */
  CanonicalPredicate(PredicateParam t, PredicateParam v) {
    elements = new PredicateParam[UNARY_LENGTH];
    elements[0] = t;
    elements[1] = v;
    typeId = elements.length;
  }


  /**
   * Changes this predicate to an inverted one.
   */
  public CanonicalPredicate invert() {
    invertFlag = !invertFlag;
    typeId += invertFlag ? INVERT_DIFF : -INVERT_DIFF;
    return this;
  }


  /**
   * Changes this predicate to an inverted one.
   */
  public boolean isInverted() {
    return invertFlag;
  }


  /**
   * Updates variables to a canonical form
   * @param con The object with the update state for the variables.
   */
  public void renameVariables(VariableCanonicalizer con) {
    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof Variable) {
        elements[i] = con.get((Variable)elements[i]);
      }
    }
  }


  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder s = new StringBuilder();
    if (invertFlag) s.append("~");
    switch (elements.length) {
    case NULL_LENGTH:
      s.append("<<null>>");
      break;
    case UNARY_LENGTH:
      s.append(elements[0]);
      s.append("(").append(elements[1]).append(")");
      break;
    case BINARY_LENGTH:
      s.append(elements[1]);
      s.append("(").append(elements[0]).append(", ");
      s.append(elements[2]).append(")");
      break;
    default:
      throw new IllegalStateException("Illegal predicate structure. Length = " + elements.length);
    }
    return s.toString();
  }


  /**
   * Tests if this predicate equals another.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (!(o instanceof CanonicalPredicate)) return false;
    CanonicalPredicate cp = (CanonicalPredicate)o;
    if (elements.length != cp.elements.length) return false;
    for (int i = 0; i < elements.length; i++) {
      if (!elements[i].equals(cp.elements[i])) return false;
    }
    return true;
  }


  /**
   * Generate a repeatable hashcode for this predicate.
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    final int[] seed = new int[] { 7, 13, 17, 19 };
    int result = 5;
    for (int i = 0; i < elements.length; i++) result += seed[i] * elements[i].hashCode();
    return result;
  }


  /**
   * Compare this predicate to another.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(CanonicalPredicate cpred) {
    if (typeId != cpred.typeId) return typeId - cpred.typeId;
    return compareOnElt(0, cpred);
  }


  /**
   * Compare this predicate, first on equivalent types, and then on type.
   * Order by PredicateLiteral, StringLiteral, IntegerLiteral, Var.
   * @param i The element being compared at this stage.
   * @param cpred The other CanonicalPredicate to compare against.
   * @return &gt;0 if this object occurs after cpred, &lt;0 if this object is before cpred,
   *         and 0 if this object is equal to cpred.
   */
  private int compareOnElt(int i, CanonicalPredicate cpred) {
    assert i >= 0 && i < elements.length;
    int typeDiff = elements[i].orderId() - cpred.elements[i].orderId();
    // if the types are the same then compare
    if (typeDiff == 0) {
      int r = elements[i].compareTo(cpred.elements[i]);
      // if the elements are equal, then move to the next element
      return r == 0 && ++i < elements.length ? compareOnElt(i, cpred) : r;
    }
    return typeDiff;
  }

}
