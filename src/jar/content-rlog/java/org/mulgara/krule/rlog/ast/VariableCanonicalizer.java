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

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains state for updating variables to a canonical form.
 *
 * @created Mar 4, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class VariableCanonicalizer {

  /** Maintains the state of the variables that have already been mapped to new names. */
  private Map<Variable,Variable> varMap = new HashMap<Variable,Variable>();

  /** The index used to name variables. */
  private int varIndex = 1;

  /**
   * Get the canonical variable that a variable is supposed to map to.
   * @param old The old variable to be replaced.
   * @return The new variable that the old variable is mapped to.
   */
  public Variable get(Variable old) {
    Variable newVar = varMap.get(old);
    if (newVar == null) {
      newVar = new Variable("V" + varIndex++);
      varMap.put(old, newVar);
    }
    return newVar;
  }
}
