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

package org.mulgara.krule.rlog.ast;

import org.mulgara.krule.rlog.ParseContext;

/**
 * Base class for all nodes in the AST.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class Node {

  /** This contains domain data relevant to the parsing context. */
  protected ParseContext parseContext;

  /**
   * Create this node, with the context it requires.
   * @param context The context passed in from the parser.
   */
  protected Node(ParseContext context) {
    parseContext = context;
  }

  /**
   * Double binding return point for use with the visitor pattern.
   * @param walker The Visitor object.
   */
  public abstract void accept(TreeWalker walker);

  /**
   * Print the details of this statement, over several lines.  Some formatting through indenting, with a starting
   * indent provided by the indent parameter.
   * @param indent The number of spaces to indent the output.
   */
  public abstract void print(int indent);

  /**
   * Creates a string with the required number of spaces for indenting.
   * @param nr The number of indent positions.
   * @return a String continaing nr*2 spaces.
   */
  String sp(int nr) {
    StringBuffer sb = new StringBuffer();
    for (int n = 0; n < nr; n++) sb.append("  ");
    return sb.toString();
  }
}

