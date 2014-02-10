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

import java.util.List;

import org.mulgara.krule.rlog.ParseContext;

/**
 * A headless rule, used for checking validity.
 *
 * @created Mar 3, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class CheckRule extends Rule {

  /**
   * @param body The body of the rule.
   * @param context The parsing context for this rule.
   */
  public CheckRule(List<Predicate> body, ParseContext context) {
    super(body, context);
  }


  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuilder sb = new StringBuilder(":- ");
    for (int b = 0; b < body.size(); b++) {
      if (b != 0) sb.append(", ");
      sb.append(body.get(b));
    }
    sb.append(".");
    return sb.toString();
  }

}
