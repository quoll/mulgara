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

import java.util.ArrayList;


/**
 * Represents a list of expressions, used for arguments to a function call.
 *
 * @created Feb 12, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class ArgList extends ArrayList<Expression> implements Node {

  /** ID for serialization */
  private static final long serialVersionUID = -5665518307457225791L;

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer result = new StringBuffer("(");
    boolean first = true;
    for (Expression e: this) {
      if (!first) result.append(", ");
      else first = false;
      result.append(e.getImage());
    }
    result.append(")");
    return result.toString();
  }

}
