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

package org.mulgara.resolver.gis;

// Third party packages
import org.apache.log4j.Logger;

//JRDF
import org.jrdf.vocabulary.*;

// Locally written packages
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;


/**
 * Resolves constraints in models defined by HTTP URLs.
 *
 * @created 2004-09-23
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.6 $
 *
 * @modified $Date: 2005/01/05 04:58:29 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GISResolverFactory implements ResolverFactory {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(GISResolverFactory.class.getName());

  /** Used by resolver instances */
  private final ContentHandlerManager contentManager;

  /** Used to create System resolvers */
  private ResolverFactory systemResolverFactory = null;

  /** Used to instantiate Resolvers */
  long rdfType = -1;

  /** Used to instantiate Resolvers */
  long systemModel = -1;

  /**
   * Constructor. Registers the instance with the intializer for the "gis"
   * protocol.
   *
   * @param resolverFactoryInitializer ResolverFactoryInitializer
   * @throws InitializerException
   */
  private GISResolverFactory(ResolverFactoryInitializer
      resolverFactoryInitializer) throws InitializerException {

    // Validate "resolverFactoryInitializer" parameter
    if (resolverFactoryInitializer == null) {
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" " +
          "parameter");
    }

    // initialize feilds
    contentManager = resolverFactoryInitializer.getContentHandlers();
    systemResolverFactory = resolverFactoryInitializer.getSystemResolverFactory();
    if (systemResolverFactory == null) {
      throw new InitializerException("ResolverFactoryInitializer returned a " +
          "null Resolverfactory.");
    }

    // Claim the "mulgara:GISModel" type
    resolverFactoryInitializer.addModelType(ReadOnlyGISResolver.MODEL_TYPE, this);
    resolverFactoryInitializer.cacheModelAccess(this);

    //get pre-defined node ID's
    rdfType = resolverFactoryInitializer.preallocate(new URIReferenceImpl(RDF.
        TYPE));
    systemModel = resolverFactoryInitializer.getSystemModel();
  }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void close() {
    // null implementation
  }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void delete() {
    // null implementation
  }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * @inheritDoc
   * @return <code>true</code> - this graph supports exports.
   */
  public boolean supportsExport() {
    return true;
  }

  /**
   * Register this resolver upon database startup.
   *
   * @param resolverFactoryInitializer the database within which to find
   *   initialization information
   * @throws InitializerException if an instance cannot be created
   * @return ResolverFactory
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer
      resolverFactoryInitializer) throws InitializerException {
    return new GISResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtain a gis resolver.
   *
   * @param canWrite {@inheritDoc}; ignored in this implementation
   * @param resolverSession the session which this query is local to
   * @param systemResolver Resolver
   * @throws ResolverFactoryException {@inheritDoc}
   * @return Resolver
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
      Resolver systemResolver) throws ResolverFactoryException {

    Resolver resolver = this.systemResolverFactory.newResolver(canWrite,
        resolverSession, systemResolver);

    //return either a read-only or writable resolver depending on the flag
    return (canWrite) ? new WritableGISResolver(resolverSession, systemResolver,
          resolver, contentManager)
          : new ReadOnlyGISResolver(resolverSession, systemResolver, resolver,
          contentManager);
    }
}
