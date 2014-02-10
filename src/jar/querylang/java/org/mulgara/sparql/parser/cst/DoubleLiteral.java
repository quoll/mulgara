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
 * Represents a Double precision literal number.
 *
 * @created Feb 11, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class DoubleLiteral implements NumericLiteral {

  /** The value of this literal. */
  private double value;

  /**
   * Constructs the literal from a string image.
   * @param s The string image of the value for this literal
   */
  public DoubleLiteral(String s) {
    this.value = Double.parseDouble(s);
  }

  /**
   * Constructs the literal.
   * @param value The double value for this literal
   */
  public DoubleLiteral(double value) {
    this.value = value;
  }
  
  /**
   * Retrieve the value as a generic Number.
   * @return A Number object containing the value.
   */
  public Number getValue() {
    return new Double(value);
  }

  /**
   * Retrieve the value as a raw type.
   * @return The internal value.
   */
  public double getDouble() {
    return value;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return Double.toString(value);
  }

}
