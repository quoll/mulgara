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
 * Various contributions copyright:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 *  getModel() contributed by Netymon Pty Ltd on behalf of
 *  The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Java 2 standard packages
import java.net.URI;
import java.net.URISyntaxException;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.vocabulary.RDF;

// Locally written packages
import org.mulgara.query.LocalNode;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * Resolves XML Schema constraints from the Mulgara string pool.
 *
 * @created 2004-10-28
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Mark Ludlow
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XSDResolverFactory implements ResolverFactory {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(XSDResolverFactory.class.getName());

  /** The URI for the default graph. */
  private static final URI DEFAULT_GRAPH = URI.create(Mulgara.XSD_GRAPH);

  private static final URI graphTypeURI = URI.create(Mulgara.NAMESPACE + "XMLSchemaModel");

  /** The preallocated local node representing the system model */
  private long systemModel;

  /** The preallocated local node representing the <code>rdf:type</code> property. */
  private long rdfType;

  /** The preallocated local node representing models stored on the Java heap. */
  @SuppressWarnings("unused")
  private long modelType;

  /**
   * Construct a local query.
   *
   * @param resolverFactoryInitializer  {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   * @throws ResolverException {@inheritDoc}
   */
  private XSDResolverFactory(ResolverFactoryInitializer resolverFactoryInitializer)
        throws ResolverException, InitializerException {

    // Validate "resolverFactoryInitializer" parameter
    if (resolverFactoryInitializer == null) {
      throw new IllegalArgumentException("Null \"resolverInitializer\" parameter");
    }

    // Initialize fields
    rdfType = resolverFactoryInitializer.preallocate(new URIReferenceImpl(RDF.TYPE));
    modelType = resolverFactoryInitializer.preallocate(new URIReferenceImpl(graphTypeURI));
    systemModel = resolverFactoryInitializer.getSystemModel();

    // No need to Claim mulgara:MemoryModel, as this is done by the default graphs
    // resolverFactoryInitializer.addModelType(graphTypeURI, this);

    try {
      // Create the after node ID
      XSDResolver.MULGARA_AFTER = resolverFactoryInitializer.preallocate(
            new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "after")));

      // Create the before node ID
      XSDResolver.MULGARA_BEFORE = resolverFactoryInitializer.preallocate(
            new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "before")));

      // Create the less than node ID
      XSDResolver.MULGARA_LT = resolverFactoryInitializer.preallocate(
            new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "lt")));

      // Create the greater than node ID
      XSDResolver.MULGARA_GT = resolverFactoryInitializer.preallocate(
            new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "gt")));

    } catch (URISyntaxException e) {
      throw new ResolverException("Generated bad XML schema URI", e);
    } catch (InitializerException initializerException) {
      throw new ResolverException("Failed to preallocate xsd node values.", initializerException);
    }

    // Register XSD-specific constraint types
    try {
      resolverFactoryInitializer.registerNewConstraint(new IntervalConstraintDescriptor());
    } catch (InitializerException initializerException) {
      throw new ResolverException("Failed to register new constraint", initializerException);
    }

    // Register the rule that generates XSD-specific constraints
    try {
      resolverFactoryInitializer.addSymbolicTransformation(
        new IntervalTransformation(graphTypeURI, 
          new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "lt")),
          new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "gt"))
        )
      );
    } catch (InitializerException initializerException) {
      throw new ResolverException("Failed to register new transformation", initializerException);
    } catch (URISyntaxException e) {
      throw new Error("Bad hardcoded constant", e);
    }

    // Store the after opposite
    XSDResolver.oppositePropertyMap.put(new LocalNode(XSDResolver.MULGARA_AFTER),
                                        new LocalNode(XSDResolver.MULGARA_BEFORE));

    // Store the before opposite
    XSDResolver.oppositePropertyMap.put(new LocalNode(XSDResolver.MULGARA_BEFORE),
                                        new LocalNode(XSDResolver.MULGARA_AFTER));

    // Store the greater than opposite
    XSDResolver.oppositePropertyMap.put(new LocalNode(XSDResolver.MULGARA_GT),
                                        new LocalNode(XSDResolver.MULGARA_LT));

    // Store the less than opposite
    XSDResolver.oppositePropertyMap.put(new LocalNode(XSDResolver.MULGARA_LT),
                                        new LocalNode(XSDResolver.MULGARA_GT));
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
   * @return {sys:xsd, mulgara:XMLSchemaModel}
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
   *                                    create the various XML Schema resources
   * @throws ResolverException If the XML Schema resources can't be found or created
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer resolverFactoryInitializer)
        throws ResolverException, InitializerException {
    return new XSDResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtaining an XML Schema
   *
   * @param resolverSession  the session which this query is local to
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code>
   */
  public Resolver newResolver(boolean canWrite,
                              ResolverSession resolverSession,
                              Resolver systemResolver) throws
                              ResolverFactoryException {
    return new XSDResolver(resolverSession, systemResolver, rdfType, systemModel);
  }
}
