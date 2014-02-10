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

package org.mulgara.resolver.jar;

// local packages
import org.apache.log4j.Logger;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-11-09
 * @author <a href="http://staff.pisoftware.com/raboczi">Tate Jones</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:46 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JarResolverFactory implements ResolverFactory
{
  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger =
    Logger.getLogger(JarResolverFactory.class.getName());

  
  private final ContentHandlerManager contentHandlers;
  
  //
  // Constructors
  //

  /**
   * Instantiate a {@link JarResolverFactory}.
   */
  private JarResolverFactory(
    ResolverFactoryInitializer resolverFactoryInitializer
  )
    throws InitializerException
  {
    // Validate "resolverFactoryInitializer" parameter
    if (resolverFactoryInitializer == null) {
      throw new IllegalArgumentException(
        "Null \"resolverFactoryInitializer\" parameter"
      );
    }
    // Obtain the content handler list
    contentHandlers = resolverFactoryInitializer.getContentHandlers();
    
    // Claim the protocols supported by java.net.URL
    resolverFactoryInitializer.addProtocol("jar", this);
                                                                                
    // Indicate that content caching is required
    resolverFactoryInitializer.cacheModelAccess(this);
  }

  //
  // Methods implementing ResolverFactory
  //

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void close()
  {
    // null implementation
  }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void delete()
  {
    // null implementation
  }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * {@inheritDoc}
   * @return <code>true</code> - this graph supports exports.
   */
  public boolean supportsExport() {
    return true;
  }

  /**
   * Register this resolver upon database startup.
   *
   * @param resolverFactoryInitializer  the database within which to find or
   *   create the various XML Schema resources
   * @throws InitializerException if the XML Schema resources can't be found or
   *   created
   */
  public static ResolverFactory newInstance(
    ResolverFactoryInitializer resolverFactoryInitializer
  )
    throws InitializerException
  {
    return new JarResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtain a Jar resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored in this implementation
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(boolean canWrite,
      ResolverSession resolverSession, Resolver systemResolver) throws ResolverFactoryException
  {
    return new JarResolver(resolverSession, systemResolver, contentHandlers);
  }
}
