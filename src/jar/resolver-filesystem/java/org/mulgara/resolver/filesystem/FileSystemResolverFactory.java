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

package org.mulgara.resolver.filesystem;

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
 * Manages the creation and initialisation of a file system resolver.
 *
 * @created 2004-11-17
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:27 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemResolverFactory implements ResolverFactory {

  /** Logger */
  private static final Logger logger =
      Logger.getLogger(FileSystemResolverFactory.class);

  /** The preallocated local node representing the rdf:type property */
  private long rdfType;

  /** Node id for the file system model type */
  private long fileSystemNode;

  /** The inclusion predicate node id */
  private long includeNode;

  /** The exclusion predicate node id */
  private long excludeNode;

  /**
   * The URL associated with the FileSystem type.
   */
  private static final URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "FileSystemModel");

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
  private FileSystemResolverFactory(ResolverFactoryInitializer initializer)
                                   throws InitializerException {

    // Validate parameters
    if (initializer == null) {

      throw new IllegalArgumentException(
                "Null 'resolverFactoryInitializer' parameter");
    }

    // Set the model type
    initializer.getSystemModelType();
    
    // Set the system model and its type
    systemModel = initializer.getSystemModel();
    systemModelType = initializer.getSystemModelType();

    try {

      // Retrieve the rdf type predicate
      rdfType = initializer.preallocate(new URIReferenceImpl(RDF.TYPE));

      // Set the file system type node
      fileSystemNode = initializer.preallocate(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));

      // Define the inclusion node
      includeNode = initializer.preallocate(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));

      // Define the exclusion node
      excludeNode = initializer.preallocate(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (URISyntaxException eu) {

      throw new InitializerException("Attempt to create invalid uri", eu);
    }

    // Claim mulgara:FileSystemModel
    initializer.addModelType(modelTypeURI, this);

    if (logger.isDebugEnabled()) {

      logger.debug("Registered FileSystem resolver to handle " + modelTypeURI);
    }
  }

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
   * @inheritDoc
   * @return <code>true</code> - the graph supports exports.
   */
  public boolean supportsExport() {
    return true;
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
  public static ResolverFactory newInstance(
                                         ResolverFactoryInitializer initializer)
                                         throws InitializerException {

    return new FileSystemResolverFactory(initializer);
  }

  /**
   * Obtain a FileSystem resolver.
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

    return new FileSystemResolver(resolverSession, systemResolver, rdfType,
                                  systemModel, systemModelType, modelTypeURI,
                                  fileSystemNode, includeNode,
                                  excludeNode);
  }
}
