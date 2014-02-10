/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.krule.rlog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.krule.rlog.parser.NSUtils;


/**
 * Contains contextual information for the current parse.
 *
 * @created Feb 27, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ParseContext {

  /** A mapping of domain prefixes to namespaces */
  private Map<String,String> namespaces = new HashMap<String,String>();

  /** The base namespace for the context */
  private String base = null;

  /**
   * Registers a new namespace as the base.
   * @param namespace The namespace for the base.
   */
  public void setBase(String namespace) {
    base = namespace;
  }

  /**
   * Retrieves the base namespace.
   * @return The namespace of the base.
   */
  public String getBase() {
    return base != null ? base : NSUtils.getDefaultDomain();
  }

  /**
   * Registers a new prefix to a namespace.
   * @param prefix The prefix to map to the namespace.
   * @param namespace The namespace for the domain.
   */
  public void registerDomain(String prefix, String namespace) {
    namespaces.put(prefix, namespace);
  }

  /**
   * Creates a URI within the default domain.
   * @param uriStr The name within the default domain.
   * @return A new URI for the name in the default domain.
   * @throws URISyntaxException The name within the domain doesn't form a valid URI.
   */
  public URI newURI(String uriStr) throws URISyntaxException {
    return new URI(newName(uriStr));
  }

  /**
   * Creates a URI within the default domain.
   * @param uriStr The name within the domain.
   * @param domain The namespace for the URI.
   * @return A new URI for the name in domain.
   * @throws URISyntaxException The name within the domain doesn't form a valid URI.
   */
  public URI newURI(String domain, String uriStr) throws URISyntaxException {
    return new URI(newName(domain, uriStr));
  }

  /**
   * Creates a new URI string within the default domain.
   * @param name The name of the resource in the default domain.
   * @return The complete name for the resource.
   */
  public String newName(String name) {
    if (base == null) return NSUtils.getDefaultDomain() + ":" + name;
    return base + name;
  }

  /**
   * Creates a new URI string within a domain.
   * @param domain The domain for the resource.
   * @param uriStr The name of the resource in the domain.
   * @return The complete name for the resource.
   */
  public String newName(String domain, String uriStr) {
    if (domain == null) return newName(uriStr);
    String namespace = getNamespace(domain);
    return (namespace == null ? domain + ":" : namespace) + uriStr;
  }


  /**
   * Get the namespace registered for a domain. This preferences the user registered
   * domains, and then falls back to the system registered domains if the domain
   * was not found.
   * @param domain The domain to find the namespace for.
   * @return The namespace registered for the domain, or <code>null</code> if not found.
   */
  private String getNamespace(String domain) {
    String ns = namespaces.get(domain);
    if (ns != null) return ns;
    if (domain.length() == 0) return getBase();
    return NSUtils.getRegisteredNamespace(domain);
  }
}
