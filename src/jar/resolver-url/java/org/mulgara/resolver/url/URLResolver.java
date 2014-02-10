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

package org.mulgara.resolver.url;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;
import javax.transaction.xa.XAResource;

// Third party packages
import com.hp.hpl.jena.rdf.arp.ARP;
import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.tuples.LiteralTuples;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-04-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/02 20:07:59 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class URLResolver implements Resolver {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(URLResolver.class.getName());

  /**
   * The session that this resolver is associated with.
   */
  private final ResolverSession resolverSession;

  /**
   * Map from the {@link URIReference} of each document ever parsed by this
   * resolver to a {@link Map} from {@link ARP}'s {@link String}-valued blank
   * node IDs to {@link BlankNode} instances.
   */
  private final Map<URIReference,Map<String,BlankNode>> documentMap = new HashMap<URIReference,Map<String,BlankNode>>();

  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param resolverSession  the session this resolver is associated with
   * @throws IllegalArgumentException  if <var>resolverSession</var> is
   *   <code>null</code>
   */
  URLResolver(ResolverSession resolverSession, Resolver systemResolver) {
    // Validate parameters
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null 'resolverSession' parameter");
    }
    if (systemResolver == null) {
      throw new IllegalArgumentException("Null 'systemResolver' parameter");
    }

    // Initialize fields
    this.resolverSession = resolverSession;
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Create a model by treating the <var>model</var> as the {@link URL} of an
   * RDF document and downloading it into the database.
   *
   * @param model  {@inheritDoc}.  In this case, it should be the {@link URL} of
   *   an RDF/XML document.
   * @param modelTypeURI  {@inheritDoc}.  This field is ignored, because URL models
   *   are external.
   */
  public void createModel(long model, URI modelTypeURI) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug("Create URL model " + model);

    // Globalize the model
    URIReference modelURIReference;
    try {
      Node globalModel = resolverSession.globalize(model);
      if (!(globalModel instanceof URIReference)) {
        throw new ResolverException(
            "Graph parameter " + globalModel + " isn't a URI reference");
      }
      modelURIReference = (URIReference) globalModel;
    } catch (GlobalizeException e) {
      throw new ResolverException("Couldn't globalize model", e);
    }
    assert modelURIReference != null;

    // Create the new model
    try {
      URL url = modelURIReference.getURI().toURL();

      // Open an output stream to the model URL
      OutputStream outputStream;
      if ("file".equals(url.getProtocol())) {
        // The default file: URL protocol handler doesn't support output, so
        // we bypass it here

        // Make sure it's a file local to the server
        if (url.getAuthority() != null) {
          throw new ResolverException(
              "Can't access filesystem on " + url.getAuthority());
        }

        // Open a stream out to the file
        outputStream = new FileOutputStream(new File(url.getPath()));
      } else {
        // Otherwise, we trust that the Java environment has an appropriate URL
        // protocol handler, and hope furthermore that it supports output
        outputStream = url.openConnection().getOutputStream();
      }
      assert outputStream != null;

      // Write the RDF/XML serialization of an empty model out
      try {
        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(outputStream));
        writer.println("This should be the content of " + modelURIReference);
        writer.flush();
      } finally {
        outputStream.close();
      }
    } catch (IOException e) {
      throw new ResolverException("Can't create RDF document from " + model, e);
    }
  }

  /**
   * @return a {@link DummyXAResource} with a 10 second transaction timeout
   */
  public XAResource getXAResource() {
    return new DummyXAResource(10); // seconds before transaction timeout
  }

  /**
   * Insert or delete RDF statements in a model at a URL.
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug("Modify URL model " + model);
    throw new ResolverException("Modification of URLs not implemented");
    /*
         logger.error("Choosing an arbitrary scratch model node");
         long scratchModel = 47;
         systemResolver.createModel(scratchModel, scratchModelTypeURI);

         try {
      // Load the contents of the destination model into the scratch model
      systemResolver.modifyModel(scratchModel, null, true);

      // Insert/delete the elements of the tripleSet from the scratch model
      systemResolver.modifyModel(scratchModel, null, occurs);

      // Overwrite the destination model with the contents of the scratch model
         } finally {
      // Drop the scratch model
      systemResolver.removeModel(scratchModel);
         }
     */
  }

  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled())  logger.debug("Remove URL model " + model);

    // Globalize the model
    URIReference modelURIReference;
    try {
      Node globalModel = resolverSession.globalize(model);
      if (!(globalModel instanceof URIReference)) {
        throw new ResolverException(
            "Graph parameter " + globalModel + " isn't a URI reference");
      }
      modelURIReference = (URIReference) globalModel;
    } catch (GlobalizeException e) {
      throw new ResolverException("Couldn't globalize model", e);
    }
    assert modelURIReference != null;

    // Remove the model
    try {
      URL url = modelURIReference.getURI().toURL();

      // Java's URL class lacks any facility for removing resources at a URL,
      // so we have to handle removal on a protocol-by-protocol basis
      if ("file".equals(url.getProtocol())) {
        // Make sure it's a file local to the server
        if (url.getAuthority() != null) {
          throw new ResolverException(
              "Can't access filesystem on " + url.getAuthority());
        }

        // Remove the file
        if (!(new File(url.getPath()).delete())) {
          logger.warn("Tried to delete nonexistent " + url + " -- ignoring");
        }
      } else {
        // This is a URL protocol for which we have no specific code
        throw new ResolverException(
            "Can't remove " + url + ": " + url.getProtocol() +
            " protocol not supported");
      }
    } catch (IOException e) {
      throw new ResolverException("Can't remove RDF document from " + modelURIReference, e);
    }
  }

  /**
   * Resolve a constraint against an RDF/XML document.
   *
   * Resolution is by filtration of a URL stream, and thus very slow.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("Resolve " + constraint);

    // Validate parameters
    if (constraint == null) {
      throw new IllegalArgumentException("constraint null");
    } else if (!(constraint.getModel() instanceof LocalNode)) {
      throw new QueryException("Constraint model can't be variable");
    }

    // Convert the constraint's model to a URI reference
    URIReference modelURIReference;
    try {
      Node node = resolverSession.globalize(((LocalNode) constraint.getElement(3)).getValue());

      if (!(node instanceof URIReference)) {
        throw new QueryException("Constraint model " + node + " isn't a URI reference");
      }

      modelURIReference = (URIReference) node;
    } catch (GlobalizeException e) {
      throw new QueryException("Couldn't globalize model for " + constraint, e);
    }
    assert modelURIReference != null;

    // Return the statements in the document at the model URL
    try {
      // Find or create the blank node map for this document
      Map<String,BlankNode> blankNodeMap = documentMap.get(modelURIReference);
      if (blankNodeMap == null) {
        blankNodeMap = new HashMap<String,BlankNode>();
        documentMap.put(modelURIReference, blankNodeMap);
      }
      assert blankNodeMap != null;
      assert documentMap.get(modelURIReference) == blankNodeMap;

      // Generate the resolution
      return new StatementsWrapperResolution(
          constraint,
          new URLStatements(modelURIReference.getURI().toURL(), resolverSession, blankNodeMap),
          true // a definitive and complete resolution
          );
    } catch (MalformedURLException e) {
      // This isn't really a document, so return no statements
      Variable[] variables = new Variable[] {new Variable("subject"),
          new Variable("predicate"),
          new Variable("object")};
      try {
        return new StatementsWrapperResolution(
            constraint,
            new TuplesWrapperStatements(new LiteralTuples(variables),
            variables[0],
            variables[1],
            variables[2]),
            false // not a definitive resolution
            );
      }
      catch (TuplesException e2) {
        throw new QueryException("Couldn't generate empty resolution", e2);
      }
    } catch (TuplesException e) {
      throw new QueryException("Couldn't read URL " + modelURIReference, e);
    }
  }

  public void abort() {}
}
