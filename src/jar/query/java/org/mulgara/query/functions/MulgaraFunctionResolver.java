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

package org.mulgara.query.functions;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;


/**
 * Retrieves Mulgara functions.
 *
 * @created Oct 6, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public abstract class MulgaraFunctionResolver implements XPathFunctionResolver {

  /**
   * A mapping of namespace URIs to the map of (name->functions) in that namespace.
   * This is used to look up the requested function object.
   */
  private final Map<String,Map<String,XPathFunction>> functionGroups = new HashMap<String,Map<String,XPathFunction>>();


  /**
   * @see javax.xml.xpath.XPathFunctionResolver#resolveFunction(javax.xml.namespace.QName, int)
   */
  public XPathFunction resolveFunction(QName functionName, int arity) {
    XPathFunction result = null;
    String namespace = functionName.getNamespaceURI();
    if (namespace != null) {
      Map<String,XPathFunction> fnGroupMap = functionGroups.get(namespace);
      if (fnGroupMap != null) {
        result = fnGroupMap.get(functionName.getLocalPart() + "/" + arity);
        // fall back to multiple arity
        if (result == null) result = fnGroupMap.get(functionName.getLocalPart() + "/*");
      }
    }
    return result;
  }


  /**
   * A helper method to create a mapping of function names to their implementing classes,
   * and of namespaces to these mappings.
   * @param fnGroup A group of functions to be added into a single namespace.
   *        This group also provides that namespace.
   */
  protected final void addFunctionGroup(MulgaraFunctionGroup fnGroup) {
    // map the function names to the functions
    Map<String,XPathFunction> functionMap = new HashMap<String,XPathFunction>();
    for (MulgaraFunction fn: fnGroup.getAllFunctions()) functionMap.put(fn.getName(), fn);
    // map the namespace to the name->function map
    functionGroups.put(fnGroup.getNamespace(), functionMap);
  }

}
