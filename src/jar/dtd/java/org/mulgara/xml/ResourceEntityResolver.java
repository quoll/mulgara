/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.xml;

// Java 2 standard packages
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An {@link EntityResolver} that knows about the public identifiers of the
 * XHTML 1.0 DTD. The following documents can be fetched directly from the
 * classloader:
 * <dl>
 *   <dt> PUBLIC <a href="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
 *   -//W3C//DTD XHTML 1.0 Strict//EN</a> </dt>
 *   <dd> The XML document type declaration for the Extensible Hypertext Markup
 *   Language (XHTML), version 1.0 </dd>
 *   <dt> PUBLIC <a href="http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent">
 *   -//W3C//ENTITIES Latin 1 for XHTML//EN</a> </dt>
 *   <dd> ISO-8859-1 character entities used by XHTML 1.0 </dd>
 *   <dt> PUBLIC <a href="http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent">
 *   -//W3C//ENTITIES Symbols for XHTML//EN</a> </dt>
 *   <dd> Symbol character entities used by XHTML 1.0 </dd>
 *   <dt> PUBLIC <a href="http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent">
 *   -//W3C//ENTITIES Special for XHTML//EN</a> </dt>
 *   <dd> Special entities used by XHTML 1.0 </dd>
 *   <dt> SYSTEM <a href="http://jakarta.apache.org/log4j/docs/api/org/apache/log4j/xml/doc-files/log4j.dtd">
 *   http://jakarta.apache.org/log4j/docs/api/org/apache/log4j/xml/doc-files/log4j.dtd
 *   </a></dt>
 *   <dd> The XML document type declaration for the Log4J logging package's
 *   configuration files </dd>
 *   <dt> PUBLIC <a href="http://jetty.mortbay.org/configure_1_1.dtd">-//Mort
 *   Bay Consulting//DTD Configure 1.0//EN</a> </dt>
 *   <dd> The XML document type declaration for the configuration files of the
 *   <a href="http://jetty.mortbay.org/jetty/index.html">Jetty</a> servlet
 *   container </dd>
 *   <dt> PUBLIC <a href="http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd">
 *   -//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN</a> </dt>
 *   <dd> The JSP tag library descriptor </dd>
 *   <dt> PUBLIC <a href="http://java.sun.com/j2ee/dtds/web-app_2_2.dtd">-//Sun
 *   Microsystems, Inc.//DTD Web Application 2.2//EN</a> </dt>
 *   <dd> The web application descriptor </dd>
 * </dl>
 *
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ResourceEntityResolver implements EntityResolver {

  private static final Logger logger = Logger.getLogger(ResourceEntityResolver.class);
  
  /**
   * Cached entities. The map is keyed on the public ID {@link String} and
   * contains the paths to the corresponding resources within the classpath,
   * also as {@link String}s.
   */
  private static Map<String,String> publicEntities =
      Collections.synchronizedMap(new HashMap<String,String>());

  /**
   * Cached entities. The map is keyed on the system ID {@link String} and
   * contains the paths to the corresponding resources within the classpath,
   * also as {@link String}s.
   */
  private static Map<String,String> systemEntities =
      Collections.synchronizedMap(new HashMap<String,String>());

  /**
   * Initialize entity map. TODO: rewrite this to use an SGML catalog instead of
   * hardcoding the cache See http://validator.w3.org/sgml-lib/catalog and
   * http://www.w3.org/TR/html4/sgml/intro.html#catalog
   */
  static {

    // Initialize the map of public IDs
    publicEntities.put("-//W3C//DTD XHTML 1.0 Strict//EN",
        "/DTD/xhtml1-strict.dtd");
    publicEntities.put("-//W3C//ENTITIES Latin 1 for XHTML//EN",
        "/DTD/xhtml-lat1.ent");
    publicEntities.put("-//W3C//ENTITIES Symbols for XHTML//EN",
        "/DTD/xhtml-symbol.ent");
    publicEntities.put("-//W3C//ENTITIES Special for XHTML//EN",
        "/DTD/xhtml-special.ent");
    publicEntities.put("-//Mort Bay Consulting//DTD Configure 1.0//EN",
        "/DTD/configure_1_1.dtd");
    publicEntities.put("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN",
        "/DTD/web-jsptaglibrary_1_1.dtd");
    publicEntities.put("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
        "/DTD/web-app_2_2.dtd");

    // Initialize the map of system IDs
    systemEntities.put("http://jakarta.apache.org/log4j/docs/api/org/apache/log4j/xml/doc-files/log4j.dtd",
        "/DTD/log4j.dtd");
  }

  /**
   * Chained entity resolver, to be used if this one fails.
   */
  private EntityResolver entityResolver;

  //
  // Constructors
  //

  /**
   * Default constructor.
   */
  public ResourceEntityResolver() {
    this(null);
  }

  /**
   * Constructor with a fallback resolver.
   *
   * @param entityResolver another {@link EntityResolver} to be used as a
   *      fallback if the entity isn't one this class has in its cache.
   */
  public ResourceEntityResolver(EntityResolver entityResolver) {

    this.entityResolver = entityResolver;
  }

  //
  // Method implementing the EntityResolver interface
  //

  /**
   * METHOD TO DO
   *
   * @param publicId PARAMETER TO DO
   * @param systemId PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws SAXException EXCEPTION TO DO
   */
  public InputSource resolveEntity(String publicId,
      String systemId) throws IOException, SAXException {

    // Compose entity name
    String entityName =
        (publicId == null) ? ("SYSTEM " + systemId)
        : ("PUBLIC " + publicId + " SYSTEM " + systemId);

    // Try the cache by public ID
    String s = (String) publicEntities.get(publicId);

    // Try the cache by system ID if unsuccessful
    if (s == null) {

      s = (String) systemEntities.get(systemId);
    }

    // Return the cached DTD, if we found something
    InputSource source;

    if (s != null) {

      logger.info("Resolving " + entityName + " from classpath...");
      source = new InputSource(getClass().getResourceAsStream(s));
      source.setPublicId(publicId);
      source.setSystemId(systemId);
    }
    else {

      // Fallback to either the system, or the chained resolver.
      logger.info("Resolving " + entityName + " from system...");
      source =
          (entityResolver == null) ? null
          : entityResolver.resolveEntity(publicId,
          systemId);
    }

    logger.info( (source == null) ? "failed." : "resolved.");

    return source;
  }
}
