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
 * @created Feb 12, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class FunctionCall implements PrimaryExpression {

  /** The name of this function */
  private IRIReference name;

  /** The arguments used in this invokation */
  private ArgList args;
  
  /**
   * Constructs the function representation, with its name and arguments.
   * @param name The function name
   * @param args The function arguments
   */
  public FunctionCall(IRIReference name, ArgList args) {
    this.name = name;
    this.args = args;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return null;
  }

  
  /**
   * @return the name
   */
  public IRIReference getName() {
    return name;
  }

  
  /**
   * @return the args
   */
  public ArgList getArgs() {
    return args;
  }

}
