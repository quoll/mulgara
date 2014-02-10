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

package org.mulgara.protocol;

import java.net.URI;

/**
 * Represents an Answer that can be emitted as XML.
 *
 * @created Jul 9, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface XMLAnswer {

  /**
   * Explicitly adds a namespace to be used in the document.
   * @param name The name of the namespace to use.
   * @param nsValue The URI of the namespace.
   */
  public abstract void addNamespace(String name, URI nsValue);

  /**
   * Remove all previously added namespaces.
   */
  public abstract void clearNamespaces();

  /**
   * Sets whether or not to used pretty printing when creating the XML. On by default.
   * @param prettyPrint <code>true</code> to turn pretty printing on. <code>false</code> to turn it off.
   */
  public abstract void setPrettyPrint(boolean prettyPrint);

}
