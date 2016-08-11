/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;


/**
 * Represents a named variable.
 *
 * @created Feb 12, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class Variable implements Node, PrimaryExpression, NumericExpression, LogicExpression, Verb {

  /** The name of this variable */
  private String name;

  /** The modifier for this variable */
  private Modifier mod = Modifier.none;

  /** Constructs a named variable */
  public Variable(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return "?" + name;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getImage();
  }

  public void setModifier(Modifier m) {
    mod = m;
  }
  
  public Modifier getModifier() {
    return mod;
  }
}
