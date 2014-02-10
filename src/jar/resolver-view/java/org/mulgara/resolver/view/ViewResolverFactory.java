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

package org.mulgara.resolver.view;

// Java 2 standard packages
import java.net.*;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.vocabulary.RDF;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * Resolves constraints in models stored on the Java heap.
 *
 * @created 2004-04-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:57 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ViewResolverFactory implements ResolverFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(ViewResolverFactory.class.getName());

  /**
   * The preallocated local node representing the <code>rdf:type</code>
   * property.
   */
  private long rdfType;

  private long viewNode;
  private long typeNode;
  private long exprNode;
  private long modelNode;
  private long unionNode;
  private long intersectNode;

  /** 
   * The URL associated with the view type.
   */
  private static final URI modelTypeURI = URI.create(Mulgara.NAMESPACE+"ViewModel");


  /**
   * The preallocated local node representing the system model and it's type(<code>#</code>).
   */
  private long systemModel;
  private long systemModelType;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link ViewResolverFactory}.
   */
  private ViewResolverFactory(ResolverFactoryInitializer initializer)
      throws InitializerException {
    // Validate parameters
    if (initializer == null) {
      throw new IllegalArgumentException(
        "Null 'resolverFactoryInitializer' parameter");
    }

    systemModel = initializer.getSystemModel();
    systemModelType = initializer.getSystemModelType();

    try {
      rdfType = initializer.preallocate(new URIReferenceImpl(RDF.TYPE));
      viewNode = initializer.preallocate(new URIReferenceImpl(new URI("http://mulgara.org/mulgara/view")));;
      typeNode = initializer.preallocate(new URIReferenceImpl(new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
      exprNode = initializer.preallocate(new URIReferenceImpl(new URI("http://mulgara.org/mulgara/view#expr")));
      modelNode = initializer.preallocate(new URIReferenceImpl(new URI("http://mulgara.org/mulgara/view#model")));
      unionNode = initializer.preallocate(new URIReferenceImpl(new URI("http://mulgara.org/mulgara/view#Union")));
      intersectNode = initializer.preallocate(new URIReferenceImpl(new URI("http://mulgara.org/mulgara/view#Intersection")));
    } catch (URISyntaxException eu) {
      throw new InitializerException("Attempt to create invalid uri", eu);
    }

    // Claim mulgara:ViewModel
    initializer.addModelType(modelTypeURI, this);
    if (logger.isDebugEnabled()) {
      logger.debug("Registered view resolver to handle " + modelTypeURI);
    }
  }

  //
  // Methods implementing ResolverFactory
  //

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because there are no persistent
   * resources.
   */
  public void close() {
    // null implementation
  }
                                                                                
  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because there are no persistent
   * resources.
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
   * {@inheritDoc}
   * @return <code>true</code> - this graph supports exports.
   */
  public boolean supportsExport() {
    return true;
  }


  /**
   * Register this resolver upon database startup.
   *
   * @param initializer  the database within which to find or
   *   create the various XML Schema resources
   * @throws InitializerException if the XML Schema resources can't be found or
   *   created
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
      throws InitializerException {
    return new ViewResolverFactory(initializer);
  }

  /**
   * Obtain a view resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored by this implementation
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(
      boolean canWrite, ResolverSession resolverSession, Resolver systemResolver
  ) throws ResolverFactoryException {
    return new ViewResolver(resolverSession,
                            systemResolver,
                            rdfType,
                            systemModel,
                            systemModelType,
                            modelTypeURI,
                            viewNode,
                            typeNode,
                            exprNode,
                            modelNode,
                            unionNode,
                            intersectNode);
  }
}
