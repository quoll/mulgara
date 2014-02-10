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

package org.mulgara.resolver.nodetype;

// Java 2 standard packages
import java.net.*;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.vocabulary.*;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * Factory for a resolver that gets type information from the string pool
 *
 * @created 2004-10-27
 * @author <a href="mailto:pag@tucanatech.com">Paul Gearon</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:49 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class NodeTypeResolverFactory implements ResolverFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(NodeTypeResolverFactory.class.getName());

  /** The preallocated local node representing the <code>rdf:type</code> property.  */
  private long rdfType;

  /** The preallocated local node representing models representing all non-blank nodes.  */
  private long modelType;
  
  /** The preallocated local node representing the RDFS literal type.  */
  private long rdfsLiteral;
  
  /** The preallocated local node representing the URI reference type.  */
  private long mulgaraUriReference;


  /** The URI for the modelType.  */
  private static final URI graphTypeURI = URI.create(Mulgara.NAMESPACE + "TypeGraph");

  /** The URI for the default graph. */
  private static final URI DEFAULT_GRAPH = URI.create(Mulgara.NODE_TYPE_GRAPH);

  /** The URI for the internal URI reference type. */
  private static final URI mulgaraUriReferenceURI = URI.create(Mulgara.NAMESPACE + "UriReference");


  /**
   * The preallocated local node representing the system model (<code>#</code>).
   */
  private long systemModel;


  //
  // Constructors
  //

  /**
   * Instantiate a {@link NodeTypeResolverFactory}.
   */
  private NodeTypeResolverFactory(
    ResolverFactoryInitializer initializer
  ) throws InitializerException {

    // Validate "resolverFactoryInitializer" parameter
    if (initializer == null) {
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" parameter");
    }

    // intialize the fields
    rdfType = initializer.preallocate(new URIReferenceImpl(RDF.TYPE));
    modelType = initializer.preallocate(new URIReferenceImpl(graphTypeURI));
    rdfsLiteral = initializer.preallocate(new URIReferenceImpl(RDFS.LITERAL));
    mulgaraUriReference = initializer.preallocate(new URIReferenceImpl(mulgaraUriReferenceURI));
    systemModel = initializer.getSystemModel();

    // No need to claim the type supported by the resolver as this is detected in the default graph
  }

  //
  // Methods implementing ResolverFactory
  //

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void close() {
    // null implementation
  }

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void delete() {
    // null implementation
  }

  /**
   * @return The default graph for this resolver.
   */
  public Graph[] getDefaultGraphs() {
    return new Graph[] { new Graph(DEFAULT_GRAPH, graphTypeURI) };
  }
  
  /**
   * {@inheritDoc}
   * @return <code>false</code> - this graph does not support exports.
   */
  public boolean supportsExport() {
    return false;
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
  ) throws InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Creating new node type resolver factory");
    return new NodeTypeResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtain a Node Type resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored, as these models are read only
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code> or canWrite is <code>true</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(
      boolean canWrite, ResolverSession resolverSession, Resolver systemResolver
  ) throws ResolverFactoryException {

    if (logger.isDebugEnabled()) logger.debug("Creating new node type resolver");
    return new NodeTypeResolver(
        resolverSession, systemResolver, rdfType, systemModel,
        rdfsLiteral, mulgaraUriReference, modelType, graphTypeURI
    );
  }
}
