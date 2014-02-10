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

package org.mulgara.resolver.file;

// Java 2 standard packages
import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.content.ContentResolver;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-04-01
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:26 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileResolver extends ContentResolver
{
  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(FileResolver.class.getName());

  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param resolverSession  the session this resolver is associated with
   * @param contentHandlers  the available {@link ContentHandler}s
   * @throws IllegalArgumentException  if <var>resolverSession</var> is
   *   <code>null</code>
   */
  FileResolver(ResolverSession resolverSession,
               Resolver        systemResolver,
               ContentHandlerManager contentHandlers)
  {
    super(resolverSession, systemResolver, contentHandlers);
  }

  //
  // Methods implementing ContentResolver
  //

  /**
   * Convert a local node number representing a <code>file:</code> model into a
   * {@link File}.
   *
   * @param model  the local node number of a <code>file:</code> URL
   * @throws ResolverException if the <var>model</var> doesn't correspond to a
   *   URL with the <code>file:</code> protocol.
   */
  protected Content toContent(long model) throws ResolverException
  {
    // Globalize the model
    URI modelURI;
    try {
      Node globalModel = resolverSession.globalize(model);
      if (!(globalModel instanceof URIReference)) {
        throw new ResolverException(
          "Graph parameter " + globalModel + " isn't a URI reference");
      }
      modelURI = ((URIReference) globalModel).getURI();
    } catch (GlobalizeException e) {
      throw new ResolverException("Couldn't globalize model", e);
    }
    assert modelURI != null;

    // Validate the URI
    if (!"file".equals(modelURI.getScheme())) {
      throw new ResolverException(modelURI + " doesn't use the file: scheme");
    }
    if (modelURI.getAuthority() != null) {
      throw new ResolverException(
        "Can't access filesystem on " + modelURI.getAuthority());
    }

    // Parse the URI into a File
    return new FileContent(new File(modelURI.getPath()));
  }
}
