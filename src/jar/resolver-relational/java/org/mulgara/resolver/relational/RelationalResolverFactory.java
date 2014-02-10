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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.vocabulary.RDF;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * Represents the time invariant concept of a relationally backed rdf-graph.
 *
 * @created 2006-04-30
 *
 * @author Andrae Muys
 *
 * @version $Revision: 1.1.1.1 $
 *
 * @modified $Date: 2005/10/30 19:21:14 $ @maintenanceAuthor $Author: prototypo $
 *
 * @company <a href="mailto:info@netymon.com">Netymon Pty Ltd</a>
 *
 * @copyright &copy;2006 Australian Commonwealth Government, Department of Defense.
 *      All rights reserved.
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RelationalResolverFactory implements ResolverFactory {

  private static final Logger logger = Logger.getLogger(RelationalResolverFactory.class);

  /** The preallocated local node representing the rdf:type property */
  private long rdfType;

  /** Node id for the relational model type */
  private long relationalModelTypeNode;

  /** The URL associated with the Relational Graph type.  */
  private static final URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "RelationalModel");


  /**
   * The preallocated local node representing the system model and it's type(<code>#</code>).
   */
  private long systemModel;
  private long systemModelType;

  /**
   * Constructor
   *
   * @param initializer The initialisation object to allow us access to
   *                    resolver initialisation parameters
   *
   * @throws InitializerException
   */
  private RelationalResolverFactory(ResolverFactoryInitializer initializer)
                                   throws InitializerException {
    // Validate parameters
    if (initializer == null) {
      throw new IllegalArgumentException("Null 'initializer' parameter");
    }

    // Set the system model and its type
    systemModel = initializer.getSystemModel();
    systemModelType = initializer.getSystemModelType();

    // Retrieve the rdf type predicate
    rdfType = initializer.preallocate(new URIReferenceImpl(RDF.TYPE));

    // Set the relational type node
    relationalModelTypeNode = initializer.preallocate(new URIReferenceImpl(modelTypeURI));

    // Claim mulgara:RelationalModel
    initializer.addModelType(modelTypeURI, this);

    if (logger.isDebugEnabled()) {
      logger.debug("Registered Relational resolver to handle " + modelTypeURI);
    }

    // Register RelationalConstraint
    initializer.registerNewConstraint(new RelationalConstraintDescriptor());

    // Register the RelationalConstraint's transformation
      initializer.addSymbolicTransformation(new RelationalTransformer(modelTypeURI));
  }

  /**
   * {@inheritDoc ResolverFactory}
   * noop - unless we maintain a connection pool of some description or similar trans-transactional resource.
   */
  public void close() { }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * noop - read-only access to relational database precludes delete.
   */
  public void delete() { }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * {@inheritDoc}
   * @return <code>false</code> - this graph can resolve ($s $p $o) but the resolution is
   * always empty, so exporting this type graph has no meaning.
   */
  public boolean supportsExport() {
    return false;
  }

  /**
   * Register this resolver upon database startup.
   *
   * @param initializer  the database within which to find or
   *                     create the various XML Schema resources
   *
   * @throws InitializerException if the XML Schema resources can't be found or
   *   created
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
                                         throws InitializerException {

    return new RelationalResolverFactory(initializer);
  }

  /**
   * Obtain a Relational resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored by this implementation
   * @param systemResolver The resolver being used for the system model
   *
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
                              Resolver systemResolver)
                              throws ResolverFactoryException {
    return new RelationalResolver(resolverSession, systemResolver, rdfType,
                                  systemModel, systemModelType, modelTypeURI,
                                  relationalModelTypeNode);
  }
}
