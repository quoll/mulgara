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
 * Contributor(s):
 *   ConstraintLocalization contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.File;
import java.net.URI;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J
import org.jrdf.graph.*;         // JRDF
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.query.Constraint;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.*;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.util.NVPair;

// Local packages

/**
 * Initialiser for {@link ResolverFactory} instances.
 *
 * @created 2004-04-26
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DatabaseResolverFactoryInitializer extends DatabaseFactoryInitializer implements ResolverFactoryInitializer {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(DatabaseResolverFactoryInitializer.class.getName());

  private final Set<ResolverFactory> cachedResolverFactorySet;
  private final Database database;
  private final DatabaseMetadata metadata;
  private final ContentHandlerManager contentHandlerManager;
  private final ResolverFactory systemResolverFactory;
  private final SessionFactory restrictedSessionFactory;

  /**
   * Sole constructor.
   *
   * @throws IllegalArgumentException if the <var>cachedResolveFactorySet</var>,
   *   <var>database</var>, <var>metadata</var>, or
   *   <var>contentHandlerManager</var> arguments are <code>null</code>
   */
  public DatabaseResolverFactoryInitializer(
           Set<ResolverFactory>  cachedResolverFactorySet,
           final Database        database,
           DatabaseMetadata      metadata,
           File                  persistenceDirectory,
           ContentHandlerManager contentHandlerManager,
           ResolverFactory       systemResolverFactory
  ) throws InitializerException {

    super(metadata.getURI(), metadata.getHostnameAliases(), persistenceDirectory);

    // Validate parameters
    if (cachedResolverFactorySet == null) throw new IllegalArgumentException("Null \"cachedResolverFactorySet\" parameter");
    if (database == null) throw new IllegalArgumentException("database null");
    if (contentHandlerManager == null) throw new IllegalArgumentException("contentHandlerManager null");

    // Initialize fields
    this.cachedResolverFactorySet = cachedResolverFactorySet;
    this.database                 = database;
    this.metadata                 = metadata;
    this.contentHandlerManager    = contentHandlerManager;
    this.systemResolverFactory    = systemResolverFactory;

    this.restrictedSessionFactory = new SessionFactory() {
      public URI getSecurityDomain() throws QueryException  { return database.getSecurityDomain(); }
      public Session newSession() throws QueryException     { return database.newSession(); }
      public Session newJRDFSession() throws QueryException { return database.newJRDFSession(); }
      public void close() throws QueryException  { throw new UnsupportedOperationException(); }
      public void delete() throws QueryException { throw new UnsupportedOperationException(); }
    };
  }


  //
  // Methods implementing ResolverFactoryInitializer
  //

  public void addModelType(URI modelType, ResolverFactory resolverFactory) throws InitializerException {
    database.addModelType(modelType, resolverFactory);
  }

  public boolean addDefaultGraph(ResolverFactory resolverFactory) throws InitializerException {
    ResolverFactory.Graph[] defaultGraphs = resolverFactory.getDefaultGraphs();
    if (defaultGraphs == null) return false;

    // initialize the types to be handled by the resolver
    for (ResolverFactory.Graph graph: defaultGraphs) {
      database.addModelType(graph.getType(), resolverFactory);
    }
    boolean result = false;
    DatabaseSession session = null;
    try {
      session = (DatabaseSession)database.newSession();
      result = session.createDefaultGraphs(defaultGraphs);
      session.close();
    } catch (QueryException e) {
      try {
        if (session != null) session.close();
      } catch (QueryException e2) { /* report first exception */ }
      throw new InitializerException("Failed to create a resolver default graph", e);
    }
    return result;
  }

  public void addProtocol(String protocol, ResolverFactory resolverFactory) throws InitializerException {
    database.addProtocol(protocol, resolverFactory);
  }

  public void addSymbolicTransformation(SymbolicTransformation symbolicTransformation) throws InitializerException {
    database.addSymbolicTransformation(symbolicTransformation);
  }

  public void cacheModelAccess(ResolverFactory resolverFactory) throws InitializerException {
    if (resolverFactory == null) throw new IllegalArgumentException("Null \"resolverFactory\" parameter");
    cachedResolverFactorySet.add(resolverFactory);
  }

  public ContentHandlerManager getContentHandlers() {
    return contentHandlerManager;
  }

  public ResolverFactory getSystemResolverFactory() throws NoSystemResolverFactoryException {
    if (systemResolverFactory == null) throw new NoSystemResolverFactoryException();
    return systemResolverFactory;
  }

  public long getRdfType() {
    return metadata.getRdfTypeNode();
  }

  public long getSystemModel() {
    checkState();
    return metadata.getSystemModelNode();
  }

  public long getSystemModelType() throws NoSystemResolverFactoryException {
    checkState();
    return metadata.getSystemModelTypeNode();
  }

  public long preallocate(Node node) throws InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Preallocating " + node);
    checkState();
    try {
      //!!FIXME: Can't add preallocate to Session until we switch over.
      DatabaseSession session = (DatabaseSession)database.newSession();
      long result = session.preallocate(node);
      session.close();
      return result;
    } catch (QueryException eq) {
      throw new InitializerException("Failed to preallocate node", eq);
    }
  }


  @SuppressWarnings("unchecked")
  public void registerNewConstraint(ConstraintDescriptor descriptor) throws InitializerException {
    Class<? extends Constraint> constraintClass = descriptor.getConstraintClass();
    if (!ConstraintOperations.constraintRegistered(constraintClass)) {
      // FIXME: This needs refactoring.  With the constraint registration in place, ConstraintOperations can be simplifed.
      ConstraintOperations.addConstraintResolutionHandlers(new NVPair[] { new NVPair<Class<? extends Constraint>,ConstraintDescriptor>(constraintClass, descriptor), });
      ConstraintOperations.addConstraintModelRewrites(new NVPair[] { new NVPair<Class<? extends Constraint>,ConstraintDescriptor>(constraintClass, descriptor) });
      if (descriptor instanceof ConstraintLocalization) {
        ConstraintOperations.addConstraintLocalizations(new NVPair[] { new NVPair<Class<? extends Constraint>,ConstraintDescriptor>(constraintClass, descriptor) });
      }
      if (descriptor instanceof ConstraintBindingHandler) {
        ConstraintOperations.addConstraintBindingHandlers(new NVPair[] { new NVPair<Class<? extends Constraint>,ConstraintDescriptor>(constraintClass, descriptor) });
      }
    } else {
      // FIXME: We need to eliminate the use of static variables (as opposed to constants).
      // FIXME: This will allow multiple database instances within the same JVM
      logger.warn("Attempted to register " + constraintClass + " twice");
    }
  }

  public SessionFactory getSessionFactory() {
    return restrictedSessionFactory;
  }
}
