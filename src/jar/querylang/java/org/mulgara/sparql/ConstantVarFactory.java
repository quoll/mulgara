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

package org.mulgara.sparql;

import org.mulgara.query.Variable;

/**
 * Creates variables to use for constants.
 *
 * @created Jun 30, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ConstantVarFactory {

  /** A label to use for all "constant" variables. */
  private static final String PREFIX = "c";

  /** The internal incrementing counter for identifying variables. */
  private int id;

  /** Creates a new factory, with a fresh counter. */
  ConstantVarFactory() {
    id = 0;
  }

  /**
   * Allocate a new variable.
   * @return The new variable.
   */
  public Variable newVar() {
    return new Variable(PREFIX + id++);
  }
}
