/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.xpath;

import org.mulgara.query.functions.MulgaraFunctionResolver;

/**
 * Retrieves functions for SPARQL expressions.
 *
 * @created Oct 5, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class MulgaraXPathFunctionResolver extends MulgaraFunctionResolver {

  /**
   * Initialize the maps of requested parameters to the function object being asked for
   */
  public MulgaraXPathFunctionResolver() {
    addFunctionGroup(new SparqlFunctionGroup());
    addFunctionGroup(new FnFunctionGroup());
    addFunctionGroup(new OpFunctionGroup());
    addFunctionGroup(new AfnFunctionGroup());
    addFunctionGroup(new SmfFunctionGroup());
  }

}
