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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.filesystem;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Node;
import gnu.trove.TLongObjectHashMap;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.tuples.Tuples;

/**
 * Resolves constraints against file system data specified by the included and
 * excluded directories within the model.
 *
 * @created 2004-11-17
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/02/22 08:16:13 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemResolver implements Resolver {

  /** Logger */
  private static final Logger log = Logger.getLogger(FileSystemResolver.class);

  /** System model's type URI */
  private URI systemModelTypeURI;

  /** URI representing the file system model's type */
  private URI modelTypeURI;

  /** Node id for the file system model */
  @SuppressWarnings("unused")
  private long fileSystemNode;

  /** Node ID for the rdf:type node */
  @SuppressWarnings("unused")
  private long rdfType;

  /** Node ID for the inclusion predicate */
  private long includeNode;

  /** Node ID for the exclusion predicate */
  private long excludeNode;

  /** The system model's resolver */
  private Resolver systemResolver;

  /** Cache for the file system */
  private TLongObjectHashMap<Resolution> fileSystemCache;

  /** Node id for the system model */
  @SuppressWarnings("unused")
  private long systemModel;

  /** Resolver session for node resolution */
  private ResolverSession resolverSession;

  /**
   * Constructor.
   *
   * @param resolverSession The session in which the resolver exists
   * @param systemResolver The resolver used for the system model
   * @param rdfType The rdf:type predicate
   * @param systemModel The system model node id
   * @param systemModelType The system model's type node id
   * @param modelTypeURI The URI representing the file system's model type
   * @param fileSystemNode The node id for the file system model
   * @param includeNode The inclusion predicate node id
   * @param excludeNode The exclusion predicate node id
   *
   * @throws ResolverFactoryException
   */
  FileSystemResolver(ResolverSession resolverSession, Resolver systemResolver,
                     long rdfType, long systemModel, long systemModelType,
                     URI modelTypeURI, long fileSystemNode, long includeNode,
                     long excludeNode) throws ResolverFactoryException {

    // Validate "modelType" parameter
    if (modelTypeURI == null) {

      throw new IllegalArgumentException("Graph type can't be null");
    }

    // Store the global variable values
    this.systemResolver = systemResolver;
    this.modelTypeURI = modelTypeURI;
    this.fileSystemNode = fileSystemNode;
    this.rdfType = rdfType;
    this.includeNode = includeNode;
    this.excludeNode = excludeNode;
    this.systemModel = systemModel;
    this.resolverSession = resolverSession;

    // Initialise the cache
    this.fileSystemCache = new TLongObjectHashMap<Resolution>();

    // Container for our system model's type node
    Node systemModelTypeURIRef = null;

    try {

      // Create an actual node for the system model type
      systemModelTypeURIRef = resolverSession.globalize(systemModelType);
    } catch (GlobalizeException globaliseException) {

      throw new ResolverFactoryException("Failed to globalize SystemModel " +
                                         "Type for file system model",
                                         globaliseException);
    }

    if (systemModelTypeURIRef instanceof URIReference) {

      // If we have a uri reference then store the system type's URI
      this.systemModelTypeURI = ((URIReference) systemModelTypeURIRef).getURI();
    } else {

      // We cannot handle literals for the system model
      throw new ResolverFactoryException("systemModelType not a URIRef " +
                                         systemModelTypeURIRef);
    }

    /**
     * Retrieves a transaction manager to help sequence resolution transactions.
     *
     * @return The transaction manager
     */
  }

  public XAResource getXAResource() {

    return new DummyXAResource(10);
  }

  /**
   * Create a model by treating the <var>model</var> as the {@link URL} of an
   * RDF document and downloading it into the database.
   *
   * @param model  {@inheritDoc}.  In this case, it should be the {@link URL} of
   *               a file system type model
   * @param modelTypeURI  {@inheritDoc}.  The type must match the value of
   *                      {@link #modelTypeURI}
   *
   * @throws ResolverException
   * @throws LocalizeException
   */
  public void createModel(long model, URI modelTypeURI) throws
      ResolverException, LocalizeException {

    if (log.isDebugEnabled()) {

      log.debug("Create FileSystem model " + model + " of type " +
                modelTypeURI);
    }

    // Validate the "modelTypeURI" parameter
    if (!modelTypeURI.equals(this.modelTypeURI)) {

      throw new ResolverException(
          "Can't create " + model + " of type " + modelTypeURI +
          ", which was never registered by " + getClass());
    }

    // Obtain the node representing the ?def URI of the model
    URIReference defNode = new URIReferenceImpl(definitionURI(model));

    // Container for our definition model
    long defModel = 0;

    try {

      // Localise the definition node
      defModel = resolverSession.localizePersistent(defNode);
    } catch (LocalizeException localiseException) {

      throw new ResolverException("Error localizing uri when creating " +
                                  "file system definition model",
                                  localiseException);
    }

    // Create the definition model
    systemResolver.createModel(defModel, systemModelTypeURI);

    // Add the definition model to the system model
    //addModelToSystemModel(model, modelTypeURI);
  }

  /**
   * @throws ResolverException always -- not yet implemented
   */
  public void writeStringPool(Writer w) throws IOException, ResolverException {

    throw new ResolverException("Backup of string pool not implemented");
  }

  /**
   * Remove the cached model containing the contents of a URL.
   *
   * @param model The node id of the model we are removing
   *
   * @throws ResolverException
   */
  public void removeModel(long model) throws ResolverException {

    if (log.isDebugEnabled()) {

      log.debug("Remove FileSystem model " + model);
    }

    // Container for the definition model node id
    long defModel = 0;

    try {

      // Obtain the node id of the definition model
      defModel = resolverSession.lookupPersistent(new URIReferenceImpl(
          definitionURI(model)));
    } catch (LocalizeException localiseException) {

      throw new ResolverException("Error localizing uri when removing " +
                                  "file system definition model",
                                  localiseException);
    }

    // Remove the definition model
    systemResolver.removeModel(defModel);
  }

  /**
   * Insert or delete RDF statements in a model at a URL.
   *
   * @param model The model we are modifying
   * @param statements The statements we are adding or deleting
   * @param occurs Whether the statements should occur or not occur in the model
   *
   * @throws ResolverException
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws
      ResolverException {

    if (log.isDebugEnabled()) {

      if (occurs) {

        log.debug("Asserting " + statements + " in " + model);
      } else {

        log.debug("Denying " + statements + " in " + model);
      }
    }

    // Validate "statements" parameter
    if (statements == null) {

      throw new IllegalArgumentException("Null \"statements\" parameter");
    }

    // Container for our definition model node id
    long defModel = 0;

    try {

      // Obtain the node id of our definition model
      defModel = resolverSession.lookupPersistent(new URIReferenceImpl(
          definitionURI(model)));
    } catch (LocalizeException localiseException) {

      throw new ResolverException("Error localizing uri when modifying file " +
                                  "system definition model", localiseException);
    }

    // Modify the definition model
    systemResolver.modifyModel(defModel, statements, occurs);
  }

  /**
   * Resolve a constraint against a model on the Java heap.
   *
   * @param constraint The constraint to resolve against the model
   *
   * @return The resolution of our constraint against the model
   *
   * @throws QueryException
   */
  public Resolution resolve(Constraint constraint) throws QueryException {

    if (log.isDebugEnabled()) {

      log.debug("!! Resolve " + constraint);
    }

    // Obtain the model of the constraint
    ConstraintElement modelElem = constraint.getModel();

    // Verify our model node is a local node
    if (!(modelElem instanceof LocalNode)) {
      if (log.isDebugEnabled()) log.debug("Ignoring solutions for " + constraint);
      return new EmptyResolution(constraint, false);
    }

    // Get the model id for the node
    long model = ((LocalNode) modelElem).getValue();

    // Container for our definition model node id
    long defModel = 0;

    try {

      // Obtain the definition mode node id
      defModel = resolverSession.lookupPersistent(new URIReferenceImpl(
          definitionURI(model)));
    } catch (LocalizeException localiseException) {

      throw new QueryException("Graph " + model +
                               " did not exist in FileSystem" +
                               " expansion", localiseException);
    } catch (ResolverException resolverException) {

      throw new QueryException("Failed to find FileSystem definition node id",
                               resolverException.getCause());
    }

    if (!fileSystemCache.containsKey(defModel)) {

      // Cache the results of the resolution against the model
      fileSystemCache.put(defModel,
                          fetchFileSystemResolution(defModel, constraint));
    }

    // Retrieve the definition for our file system
    return fetchFileSystemResolution(defModel, constraint);
  }

  /**
   * Fetches the resolution object produced by constraining the tuples produced
   * by the included and excluded file systems.
   *
   * @param defModel The definition model's node id
   * @param constraint  The constraint we are constraining our results against
   *
   * @return The files matching the constraints in the included file systems
   *         exluding those that are excluded
   *
   * @throws QueryException
   */
  private Resolution fetchFileSystemResolution(long defModel,
                                               Constraint constraint) throws
      QueryException {

    // Obtain all included file systems in the model
    Tuples includeTuples = systemResolver.resolve(new ConstraintImpl(
        new Variable("fileSystemModel"),
        new LocalNode(includeNode),
        new Variable("fileSystemRef"),
        new LocalNode(defModel)));

    // Obtain all excluded file systems in the model
    Tuples excludeTuples = systemResolver.resolve(new ConstraintImpl(
        new Variable("fileSystemModel"),
        new LocalNode(excludeNode),
        new Variable("fileSystemRef"),
        new LocalNode(defModel)));

    if (log.isDebugEnabled()) {

      log.debug("-- Including the following file systems: " + includeTuples);
      log.debug("-- Excluding the following file systems: " + excludeTuples);
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Obtain the statements about the file systems included in the model
      statements = new FileSystemStatements(includeTuples,
                                            excludeTuples,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      throw new QueryException(
          "Unable to create statements from file system data",
          tuplesException);
    }

    return new StatementsWrapperResolution(constraint, statements, true);
  }

  private URI definitionURI(long fileSystemNode) throws ResolverException {

    try {

      Node modelNode = resolverSession.globalize(fileSystemNode);

      if (modelNode instanceof URIReference) {

        return definitionURI(((URIReference) modelNode).getURI());
      } else {

        throw new ResolverException("FileSystemNode not URI");
      }
    } catch (GlobalizeException eg) {

      throw new ResolverException("Failed to globalize FileSystem", eg);
    }
  }

  private URI definitionURI(URI fileSystemURI) throws ResolverException {

    try {

      URI defURI = new URI(fileSystemURI.getScheme(),
                           fileSystemURI.getAuthority(),
                           fileSystemURI.getPath(),
                           "def",
                           fileSystemURI.getFragment());

      if (log.isInfoEnabled()) {

        log.info("Creating defModel with uri = " + defURI);
      }

      return defURI;
    } catch (URISyntaxException eu) {

      throw new ResolverException("Invalid URI", eu);
    }
  }

  public void abort() {}
}
