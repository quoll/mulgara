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

package org.mulgara.query;

import org.mulgara.util.Reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathFunctionResolver;

/**
 * A singleton registry for XPathFunctionResolvers.
 *
 * @created Sep 27, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class FunctionResolverRegistry implements Iterable<XPathFunctionResolver> {

  /** The singleton of this object */
  static private FunctionResolverRegistry registry = new FunctionResolverRegistry();

  /** The list of registered resolvers */
  private List<XPathFunctionResolver> registered;

  /**
   * Constructs the singleton resolver.
   */
  private FunctionResolverRegistry() {
    registered = new ArrayList<XPathFunctionResolver>();
  }

  /**
   * Gets the singleton registry.
   * @return The registry for resolvers.
   */
  public static FunctionResolverRegistry getFunctionResolverRegistry() {
    return registry;
  }

  /**
   * Registered a resolver instance.
   * @param resolver The resolver instance to register.
   */
  public void register(XPathFunctionResolver resolver) {
    registered.add(resolver);
  }

  /**
   * Registered a resolver by name.
   * @param resolver The name of a resolver type to register.
   * @throws ClassNotFoundException Unable to find the specified class.
   * @throws RuntimeException If the class could not be instantiated for any reason.
   */
  @SuppressWarnings("unchecked")
  public void register(String resolverName) throws ClassNotFoundException {
    Class<XPathFunctionResolver> resolverType = (Class<XPathFunctionResolver>)Class.forName(resolverName);
    registered.add(Reflect.newInstance(resolverType));
  }

  /**
   * Get an iterator for all the registered resolvers.
   * @see java.lang.Iterable#iterator()
   * @return an Iterator for the registry.
   */
  public Iterator<XPathFunctionResolver> iterator() {
    return Collections.unmodifiableList(registered).iterator();
  }
}
