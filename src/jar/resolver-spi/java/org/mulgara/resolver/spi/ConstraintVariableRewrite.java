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

// Local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.Variable;

/**
 * Used to update a given variable in a constraint to a new name.
 *
 * @created May 29, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */

public interface ConstraintVariableRewrite {

  /**
   * Rewrite this constraint to use a new variable name
   *
   * @param graphVar the graph variable. This will appear elsewhere in the constraint.
   * @param newVar the new variable to replace the graph variable with outside of the graph position.
   * @param constraint the constraint to update
   */
    public Constraint rewrite(Variable graphVar, Variable newVar, Constraint constraint) throws Exception;
}
