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
 * Marker class to represent top level nodes in the AST.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class Statement extends Node {

  /**
   * Create this statement, with the context it requires.
   * @param context The context passed in from the parser.
   */
  protected Statement(ParseContext context) {
    super(context);
  }

  /**
   * Get a list of canonicalized predicates to represent the statement. The body of the
   * statement must be sorted, but if a head exists then it must be at the end.
   * @return A list containing the statement in canonical form.
   */
  public abstract CanonicalStatement getCanonical();
}

