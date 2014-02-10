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
 * 
 *
 * @created Feb 11, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class BooleanLiteral implements PrimaryExpression, LogicExpression {

  /** Constant value for <code>true</code> */
  public static final BooleanLiteral TRUE = new BooleanLiteral(true);

  /** Constant value for <code>false</code> */
  public static final BooleanLiteral FALSE = new BooleanLiteral(false);

  /** The internal value. */
  private boolean value;
  
  /**
   * Private constructor.
   * @param value
   */
  private BooleanLiteral(boolean value) {
    this.value = value;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return Boolean.toString(value);
  }

}
