/*
 * Copyright 2008 Fedora Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser;

import org.mulgara.sparql.parser.cst.BlankNode;

/**
 * This class allocated names for anonymous variables used for indicating blank nodes in queries.
 *
 * @created Feb 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class VarNameAllocator {

  /** A marker for variable names that is not valid in any query syntax */
  private static final String ANON_MARKER = "*";

  /** An incrementing value to make names unique */
  private int counter = 0;

  /**
   * Create a unique BlankNode.
   * @return The new blank node.
   */
  public BlankNode allocate() {
    return new BlankNode(ANON_MARKER + counter++);
  }

  /**
   * Get the name for a new blank node.
   * @return A unique name for a blank node.
   */
  public String allocateName() {
    return ANON_MARKER + counter++;
  }
}
