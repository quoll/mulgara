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

package org.mulgara.krule.rlog.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mulgara.krule.rlog.rdf.RDF;

/**
 * Records namespace information.
 *
 * @created May 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class NSUtils {

  /** The default domain to use when no other is set. */
  private static String defaultDomain = RDF.KRULE_NS;

  private static String defaultPrefix = RDF.KRULE_PREFIX;

  /** A mapping of prefixes to namespaces. */
  private static Map<String,String> registeredDomains = new HashMap<String,String>();
  
  static {
    registeredDomains.put(RDF.PREFIX, RDF.NS);
    registeredDomains.put(RDF.RDFS_PREFIX, RDF.RDFS_NS);
    registeredDomains.put(RDF.OWL_PREFIX, RDF.OWL_NS);
    registeredDomains.put(RDF.XSD_PREFIX, RDF.XSD_NS);
    registeredDomains.put(RDF.MULGARA_PREFIX, RDF.MULGARA_NS);
    registeredDomains.put(RDF.KRULE_PREFIX, RDF.KRULE_NS);
    registeredDomains.put(RDF.FOAF_PREFIX, RDF.FOAF_NS);
    registeredDomains.put(RDF.SKOS_PREFIX, RDF.SKOS_NS);
    registeredDomains.put(RDF.DC_PREFIX, RDF.DC_NS);
  }

  /** Sets the default domain to use when no namespace is given. */
  public static void setDefaultDomain(String prefix, String domain) {
    defaultPrefix = prefix;
    defaultDomain = domain;
    registeredDomains.put(prefix, domain);
  }

  /** Gets the default domain used when no namespace is given. */
  public static String getDefaultDomain() {
    return defaultDomain;
  }

  /** Gets the default prefix used when no namespace is given. */
  public static String getDefaultPrefix() {
    return defaultPrefix;
  }

  /**
   * Registers a new prefix to a namespace.
   * @param prefix The prefix to map to the namespace.
   * @param namespace The namespace for the domain.
   */
  public static void registerDomain(String prefix, String namespace) {
    registeredDomains.put(prefix, namespace);
  }

  /**
   * Creates a URI within the default domain.
   * @param uriStr The name within the default domain.
   * @return A new URI for the name in the default domain.
   * @throws URISyntaxException The name within the domain doesn't form a valid URI.
   */
  public static URI newURI(String uriStr) throws URISyntaxException {
    return new URI(newName(uriStr));
  }

  /**
   * Creates a URI within the default domain.
   * @param uriStr The name within the domain.
   * @param domain The namespace for the URI.
   * @return A new URI for the name in domain.
   * @throws URISyntaxException The name within the domain doesn't form a valid URI.
   */
  public static URI newURI(String domain, String uriStr) throws URISyntaxException {
    return new URI(newName(domain, uriStr));
  }

  /**
   * Creates a new URI string within the default domain.
   * @param name The name of the resource in the default domain.
   * @return The complete name for the resource.
   */
  public static String newName(String name) {
    return defaultDomain + ":" + name;
  }

  /**
   * Creates a new URI string within a domain.
   * @param domain The domain for the resource.
   * @param uriStr The name of the resource in the domain.
   * @return The complete name for the resource.
   */
  public static String newName(String domain, String uriStr) {
    if (domain == null) return newName(uriStr);
    String namespace = registeredDomains.get(domain);
    return (namespace == null ? domain + ":" : namespace) + uriStr;
  }

  /**
   * Retrieves the namespace registered for a domain.
   * @param domain The registered domain.
   * @return The namespace associated with the domain, or <code>null</code> if the domain
   *         was not registered.
   */
  public static String getRegisteredNamespace(String domain) {
    return registeredDomains.get(domain);
  }


  /**
   * Returns a list of entries in the registered domains.
   * @return A Set of Map.Entries which map domain abbreviations to full namespaces.
   */
  public static Set<Map.Entry<String,String>> getRegisteredDomains() {
    return Collections.unmodifiableSet(registeredDomains.entrySet());
  }
}
