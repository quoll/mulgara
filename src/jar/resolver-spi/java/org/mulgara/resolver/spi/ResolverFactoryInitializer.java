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

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.store.stringpool.StringPool;

/**
 * Interface defining the initialization services a {@link ResolverFactory} must
 * be provided with in order to plug into a database.
 *
 * Initialization services are only provided at initialization.  If a client
 * tries to hold on to a reference to a {@link ResolverFactoryInitializer} after
 * initialization, calling any of the methods of this interface should
 * throw {@link IllegalStateException}.
 *
 * @created 2004-03-26
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/19 08:43:59 $ 
 * @maintenanceAuthor $Author: raboczi $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ResolverFactoryInitializer extends FactoryInitializer {
  /**
   * Register this resolver factory as being able to create and drop models of a particular type.
   * @param graphType a preallocated node identifying models of the type created by the {@link ResolverFactory}.
   * @param resolverFactory  the resolver factory being registered as a handler for the <var>graphType</var>
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the <var>graphType</var> couldn't be registered
   */
  public void addModelType(URI graphType, ResolverFactory resolverFactory) throws InitializerException;

  /**
   * Register this resolver factory as being able to create and drop models external to the database
   * via a specified URI protocol.
   * @param protocol a URL protocol
   * @param resolverFactory the resolver factory being registered as a handler for the <var>protocol</var>
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the <var>protocol</var> couldn't be registered
   */
  public void addProtocol(String protocol, ResolverFactory resolverFactory) throws InitializerException;

  /**
   * Register this resolver factory as handling graphs of a particular type, and creates
   * a default graph of that type in the System Resolver to provide internal functionality.
   * This is the default graph to be used for this resolver factory.
   * @param resolverFactory the resolver factory being registered as a handler for its graph types.
   * @return <code>true</code> if the graph was successfully created,
   *         <code>false</code> if registration occurred, but the graph was not created.
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the <var>graphType</var> couldn't be registered
   */
  public boolean addDefaultGraph(ResolverFactory resolverFactory) throws InitializerException;

  /**
   * Register a symbolic transformation rule.
   * @param symbolicTransformation  the rule to register, never <code>null</code>
   * @throws IllegalStateException is called outside of initialization
   * @throws InitializerException if the <var>symbolicTransformation</var> couldn't be registered
   */
  public void addSymbolicTransformation(SymbolicTransformation symbolicTransformation) throws InitializerException;

  /**
   * Register this resolver factory to have its models cached.
   * @param resolverFactory  the resolver factory being registered to be cached, never <code>null</code>
   * @throws IllegalArgumentException if the <var>resolverFactory</var> is <code>null</code>
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the <var>resolverFactory</var> couldn't be registerered <code>null</code>
   */
  public void cacheModelAccess(ResolverFactory resolverFactory) throws InitializerException;

  /**
   * Obtain the registered {@link ContentHandler}s.
   * This list returned is immutable by the resolvers, but mutable by the
   * database should new content handlers be added.  Resolvers should retain
   * the original reference rather than copying it by value, otherwise they
   * won't see these changes.
   * @return an unmodifiable list of {@link ContentHandler}s, never <code>null</code>
   */
  public ContentHandlerManager getContentHandlers();

  /**
   * Obtain the {@link ResolverFactory} for system models.
   * The system and security models will have been created by this factory.
   * It's appropriate to use this factory for temporary models.
   * @return the system {@link ResolverFactory}, never <code>null</code>
   * @throws IllegalStateException if called outside of initialization
   * @throws NoSystemResolverFactoryException if the system {@link ResolverFactory} hasn't
   *         yet been registered
   */
  public ResolverFactory getSystemResolverFactory() throws NoSystemResolverFactoryException;

  /**
   * Obtain the local coordinate for RDF.TYPE(<code>#</code>).
   */
  public long getRdfType();

  /**
   * Obtain the system model (<code>#</code>).
   */
  public long getSystemModel();

  /**
   * Obtain a model type that can be created from the primary {@link ResolverFactory} returned by the
   * {@link #getSystemResolverFactory} method.
   * It's appropriate to use this model type for temporary models.
   * @return the system model type
   * @throws IllegalStateException if called outside of initialization
   * @throws NoSystemResolverFactoryException if the system {@link ResolverFactory} hasn't yet been registered
   */
  public long getSystemModelType() throws NoSystemResolverFactoryException;

  /**
   * Preallocate an RDF {@link Node} to exist in all sessions.
   * Because they exist in all sessions and can never be reaped, they need not
   * be relocalized for every new {@link Session}.  This savings in speed and
   * hits on the {@link StringPool} is bought at the expense of cluttering up
   * the database with the preallocated nodes.
   * @param node  a global RDF node to preallocate
   * @return a preallocated local node 
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the node can't be allocated
   */
  public long preallocate(Node node) throws InitializerException;

  /**
   * Register new Constraint Type
   * @throws InitializerException if the constraint class specified has already been registered
   */
  public void registerNewConstraint(ConstraintDescriptor descriptor) throws InitializerException;

  /**
   * Obtain the session-factory the resolver-factory is being loaded by.
   *
   * There are a couple restrictions on the returned session-factory. For one, the methods
   * {@link SessionFactory#close close()} and {@link SessionFactory#delete delete()} may not
   * be used and will throw an exception. The second restriction is that operations on different
   * session's may not be nested, which means that if a resolver or resolver-factory creates any
   * sessions using this session-factory then it must use a separate thread for operations on
   * those sessions.
   *
   * @return the owning session-factory
   */
  public SessionFactory getSessionFactory();
}
