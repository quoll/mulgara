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

package org.mulgara.krule.rlog.rdf;

/**
 * Represents a variable part of a rule.
 *
 * @created May 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class Var implements RDFNode {

  private String name;

  public Var(String name) {
    this.name = name;
  }

  /** This node can represent anything, depending on context. */
  public boolean isVariable() { return true; }

  /** @see org.mulgara.krule.rlog.rdf.RDFNode#isReference() */
  public boolean isReference() { return false; }

  /** The name of the variable */
  public String getName() { return name; }

  /** {@inheritDoc} */
  public String getRdfLabel() {
    return "#var_" + name;
  }

  /** {@inheritDoc} */
  public String toString() {
    return "v(" + name + ")";
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (!(o instanceof Var)) return false;
    return name.equals(((Var)o).name);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return name.hashCode() + 1;
  }
}
