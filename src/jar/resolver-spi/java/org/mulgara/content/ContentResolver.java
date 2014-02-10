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

package org.mulgara.content;

// Java 2 standard packages
import java.net.URI;
import java.util.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;

/**
 * Resolves constraints in models defined by parsing a {@link Content} stream.
 *
 * @created 2004-10-04
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/02/22 08:16:17 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana Technology,
 *   Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ContentResolver implements Resolver {
  /**
   * The session that this resolver is associated with.
   */
  protected final ResolverSession resolverSession;

  /**
   * Map from the {@link URIReference} of each document ever parsed by this
   * resolver to the corresponding {@link Content}.
   */
  protected final Map<URIReference,Content> contentMap = new HashMap<URIReference,Content>();

  protected final ContentHandlerManager contentManager;

  /** Current system resolver */
  @SuppressWarnings("unused")
  private final Resolver systemResolver;

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(ContentResolver.class.getName());

  //
  // Constructors
  //

  /**
   * Construct a ContentResolver.
   *
   * @param resolverSession  the session this resolver is associated with - unused for now.
   * @param contentManager  the available {@link ContentHandler}s
   * @throws IllegalArgumentException  if <var>resolverSession</var> is
   *   <code>null</code>
   */
  protected ContentResolver(ResolverSession resolverSession,
      Resolver systemResolver,
      ContentHandlerManager contentManager) {
    // Validate parameters
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null 'resolverSession' parameter");
    }
    if (systemResolver == null) {
      throw new IllegalArgumentException("Null 'systemResolver' parameter");
    }

    if (contentManager == null) {
      throw new IllegalArgumentException("Null 'contentManager' parameter");
    }

    // Initialize fields
    this.resolverSession = resolverSession;
    this.systemResolver = systemResolver;
    this.contentManager = contentManager;
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Create a model by creating empty {@link Content}.
   *
   * @param model  {@inheritDoc}.  In this case it should always be a URL
   *  referencing {@link Content} outside the database.
   * @param modelTypeURI  {@inheritDoc}.  This field is ignored, because the {@link Content} is external.
   */
  public void createModel(long model, URI modelTypeURI) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug("Create content model " + model);
    throw new ResolverException("Creation of external documents not implemented");
  }

  /**
   * @return a {@link DummyXAResource} with a 10 second transaction timeout
   */
  public XAResource getXAResource() {
    return new DummyXAResource(10);   // seconds before transaction timeout
  }

  /**
   * Insert or delete RDF statements from a model stored in a file.
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug("Modify external document model " + model);

    Content content = toContent(model);
    assert content != null;

    try {
      ContentHandler contentHandler = contentManager.getContentHandler(content);
      contentHandler.serialize(statements, content, resolverSession, null);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      URI u = content.getURI();
      if (u != null) throw new ResolverException("Unable to serialize " + content.getURI(), e);
      try {
        throw new ResolverException("Unable to serialize stream with type: " + content.getContentType(), e);
      } catch (NotModifiedException e1) {
        throw new ResolverException("Unable to serialize stream", e);
      }
    }
  }

  /**
   * Remove the file containing the model.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug("Remove model " + model);
    throw new ResolverException("Deletion of external documents not implemented");
  }

  /**
   * Resolve a constraint against an RDF/XML document.
   * Resolution is by filtration of a stream, and thus very slow.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("Resolve " + constraint);

    // Validate parameters
    if (constraint == null) throw new IllegalArgumentException("constraint null");
    else if (!(constraint.getModel() instanceof LocalNode)) {
      throw new QueryException("Constraint model can't be variable");
    }

    // Convert the constraint's model to a URI reference
    URIReference modelURIReference;
    long modelNode;
    try {
      modelNode = ((LocalNode) constraint.getModel()).getValue();
      Node node = resolverSession.globalize(modelNode);

      if (!(node instanceof URIReference)) {
        throw new QueryException("Constraint model " + node +
            " isn't a URI reference");
      }

      modelURIReference = (URIReference) node;
    }
    catch (GlobalizeException e) {
      throw new QueryException("Couldn't globalize model for " + constraint, e);
    }
    assert modelNode != NodePool.NONE;
    assert modelURIReference != null;

    // Find or create the Content instance representing this external document
    Content content = contentMap.get(modelURIReference);
    if (content == null) {
      try {
        content = toContent(modelNode);
      }
      catch (ResolverException e) {
        throw new QueryException("Unable to parse " + modelURIReference, e);
      }
      contentMap.put(modelURIReference, content);
    }
    assert content != null;
    assert contentMap.get(modelURIReference) == content;

    Statements statements = toStatements(content);
    assert statements != null;

    // Generate the resolution
    // TODO: implement switching based on MIME type
    return new StatementsWrapperResolution(
        constraint,
        statements,
        true // a definitive and complete resolution
        );
  }

  //
  // Internal methods
  //

  /**
   * @param content  the stream to parse, never <code>null</code>
   * @return RDF statements parsed from the <var>content</var>
   */
  private Statements toStatements(Content content) throws QueryException {
    // See if any of the content handlers recognize the URI extension
    try {
      ContentHandler contentHandler = contentManager.getContentHandler(content);
      return contentHandler.parse(content, resolverSession);
    } catch (NotModifiedException e) {
      throw new QueryException("Unable to parse " + out(content), e);
    } catch (CorruptContentException e) {
      // This is the right content type, but the content is corrupt
      throw new QueryException("Unable to parse " + out(content), e);
    } catch (ContentHandlerException contentHandlerException) {
      // Continue trying other content handlers
    }

    // See if any of the content handlers can parse the input, despite the
    // unfamiliar extension
    try {
      // Attempt to parse the content of the file into statements
      return contentManager.blindParse(content, resolverSession);
    } catch (NotModifiedException e) {
      throw new QueryException("Unable to parse " + out(content), e);
    } catch (CorruptContentException e) {
      // This is the right content type, but the content is corrupt
      throw new QueryException("Unable to parse " + out(content), e);
    } catch (ContentHandlerException e) {
      // Continue trying other content handlers
    }

    // Couldn't obtain a content handler for this URI
    throw new QueryException("Unable to parse " + out(content));
  }


  public void abort() {
    // I don't believe there is anything to do here.  It is possible that we may
    // need to close file handles or clear caches.
  }


  /**
   * Creates a label for the content.
   * @param content The content to describe.
   * @return A string containing a description of what can be found in the content.
   */
  private String out(Content content) {
    try {
      return content.getURI() == null ? "stream of type: " + content.getContentType() : content.getURI().toString();
    } catch (NotModifiedException e) {
      return "stream of unknown type";
    }
  }


  //
  // SPI methods
  //

  /**
   * Convert a local node representing an external document into
   * {@link Content}.
   *
   * @param modelNode
   * @return the {@link Content} of the external document represented by the
   *   <var>modelNode</var>
   * @throws ResolverException if the conversion cannot be performed
   */
  protected abstract Content toContent(long modelNode) throws ResolverException;
}
