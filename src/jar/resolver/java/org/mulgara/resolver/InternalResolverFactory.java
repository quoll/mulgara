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
 *    Change to field visibility from private to protected
 *    (c) Netymon Pty Ltd 2006 All Rights Reserved.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Local packages
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Wrapper around an external {@link ResolverFactory} that applies caching to
 * its models using a specified internal {@link ResolverFactory}.
 *
 * @created 2004-11-17
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/02 20:07:56 $
 * @maintenanceAuthor $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class InternalResolverFactory implements ResolverFactory {

  /**
   * The preallocated node for <code>rdf:type</code>.
   */
  protected final long rdfType;

  /**
   * The wrapped factory.
   */
  protected final ResolverFactory resolverFactory;

  /**
   * The preallocated node for the system model (<code>#</code>).
   */
  protected final long systemModel;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * Failure to provide a value for <var>rdfType</var> will disable creation
   * and removal of models.
   *
   * @param resolverFactory  the instance to wrap, never <code>null</code>
   * @param rdfType  the preallocated node for <code>rdf:type</code>, or
   *   {@link org.mulgara.store.nodepool.NodePool#NONE} if unavailable
   * @param systemModel  the preallocated node for the system model
   *   (<code>#</code>), or {@link org.mulgara.store.nodepool.NodePool#NONE} if unavailable
   */
  InternalResolverFactory(ResolverFactory resolverFactory,
                          long            rdfType,
                          long            systemModel)
  {
    assert resolverFactory  != null;

    // initialize fields
    this.resolverFactory = resolverFactory;
    this.rdfType         = rdfType;
    this.systemModel     = systemModel;
  }

  //
  // Methods implementing ResolverFactory
  //

  public void close() throws ResolverFactoryException
  {
    // null implementation
  }

  public void delete() throws ResolverFactoryException
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
   * @return resolverFactory.supportsExport() - delegate to the acual resolver.
   */
  public boolean supportsExport() {
    return resolverFactory.supportsExport();
  }

  public Resolver newResolver(boolean         canWrite,
                              ResolverSession resolverSession,
                              Resolver        systemResolver)
    throws ResolverFactoryException
  {
    return new InternalResolver(
      resolverFactory.newResolver(canWrite,
                                  resolverSession,
                                  new InternalResolver(systemResolver,
                                                       rdfType,
                                                       resolverSession,
                                                       systemModel,
                                                       systemResolver)),
      rdfType,
      resolverSession,
      systemModel,
      systemResolver
    );
  }
}
