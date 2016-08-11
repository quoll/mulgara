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

import java.util.Collections;
import java.util.List;
import static org.mulgara.util.ObjectUtil.eq;

/**
 * Represents a canonicalized form of a statement. Used for comparing statements
 * to check them for redundancy.
 *
 * @created Mar 12, 2009
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class CanonicalStatement {

  /** A sorted list of predicates */
  private List<CanonicalPredicate> body;

  /** The head of the rule. May be empty. */
  private CanonicalPredicate head;


  /**
   * Creates a canonical statement to represent an axiom.
   * @param h The predicate in the axiom.
   */
  CanonicalStatement(CanonicalPredicate h) {
    this(h, null);
  }


  /**
   * Creates a canonical statement to represent a rule.
   * @param h The predicate in the head of the rule.
   * @param b The <em>sorted</em> predicates in the body of the rule.
   */
  CanonicalStatement(CanonicalPredicate h, List<CanonicalPredicate> b) {
    if (b == null) body = Collections.emptyList();
    else body = b;
    head = h;
    renameVariables();
  }


  /**
   * Test is this statement is the same as another.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (!(o instanceof CanonicalStatement)) return false;
    CanonicalStatement s = (CanonicalStatement)o;
    return eq(head, s.head) && body.equals(s.body);
  }


  /**
   * Generates a hashcode which merges the hashcodes of the body and head.
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int hhc = head.hashCode();
    return (hhc >>> 16 | hhc << 16) ^ body.hashCode();
  }


  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(head);
    s.append(" :- ");
    for (int p = 0; p < body.size(); p++) {
      if (p == 0) s.append(", ");
      s.append(body.get(p));
    }
    s.append(".");
    return s.toString();
  }

  /**
   * Renames the variables into a canonical form.
   */
  private void renameVariables() {
    VariableCanonicalizer vc = new VariableCanonicalizer();
    for (CanonicalPredicate p: body) p.renameVariables(vc);
    head.renameVariables(vc);
  }

}
