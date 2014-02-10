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

package org.mulgara.resolver.spi;

// Third party packages
import java.net.URI;


/**
 * Generates {@link Resolver} instances.
 *
 * Resolver factories represent a particular way of storing models.  They can
 * broadly be differentiated into models stored internally to the database, and
 * models stored external to the database.  Models internal to the database have
 * a model type identified by a URI, registered by the
 * {@link ResolverFactoryInitializer#addModelType} method.  Models external to
 * the database are associated with a particular URL protocol,
 * registered by {@link ResolverFactoryInitializer#addProtocol}.
 *
 * @created 2004-03-28
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ResolverFactory
{
  /**
   * Obtain and configure a resolver factory.
   *
   * This must be overridden by subclasses.  The default implementation throws
   * a {@link ResolverException}.
   *
   * @param initializer  the database within which to find or create the various
   *   resources
   * @throws IllegalArgumentException if <var>initializer</var> is
   *   </code>null</code>
   * @throws InitializerException if the resolver couldn't be initialized
   */
//  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
//    throws InitializerException
//  {
//    throw new InitializerException(
//      "ResolverFactory must override newInstance");
//  }

  /**
   * Gets any default graphs established by this factory.
   * @return An array of graphs provided by default by this resolver.
   *         May be empty or <code>null</code>.
   */
  public Graph[] getDefaultGraphs();

  /**
   * Flush any persistent resources associated with this factory so that
   * they can be recovered after the database closes and later restarts.
   *
   * @throws ResolverFactoryException if the resources associated with this
   *   resolver couldn't be removed
   */
  public abstract void close() throws ResolverFactoryException;

  /**
   * Remove any persistent resources associated with this factory.
   *
   * This will discard any stored information.
   *
   * @throws ResolverFactoryException if the resources associated with this
   *   resolver couldn't be removed
   */
  public abstract void delete() throws ResolverFactoryException;

  /**
   * Obtain a resolver for a particular session.
   *
   * @param resolverSession  a session within which to perform resolution,
   *   never <code>null</code>
   * @param canWrite  whether the obtained resolver must support modification;
   *   if <code>false</code> is passed, it's permitted to return a read-only
   *   resolver
   * @return a resolver instance for the <var>resolverSession</var>
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   </code>null</code>
   * @throws ResolverFactoryException if the resolver couldn't be created
   */
  public abstract Resolver newResolver(boolean canWrite,
      ResolverSession resolverSession, Resolver systemResolver) throws ResolverFactoryException;
  
  /**
   * Answers whether graphs associated with this factory (via a registered graph
   * type URI for internal graphs or URL protocol scheme for external graphs) support
   * the export operation.  If this method returns <code>true</code>, then the
   * Resolver returned by the {@link #newResolver(boolean, ResolverSession, Resolver)} method
   * MUST be able to resolve the constraint ($s $p $o &lt;graph&gt;).
   * 
   * @return <code>true</code> if graphs associated with this factory support exporting
   *         all of their contents.
   */
  public abstract boolean supportsExport();

  /**
   * A class to manage pairs of URIs for graph identifiers and types.
   */
  public static class Graph {
    URI graph;
    URI type;
    public Graph(URI graph, URI type) { this.graph = graph; this.type = type; }
    public URI getGraph() { return graph; }
    public URI getType() { return type; }
  }
}
