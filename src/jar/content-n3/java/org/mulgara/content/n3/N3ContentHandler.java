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

package org.mulgara.content.n3;

// Java packages
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ModifiedException;
import org.mulgara.query.rdf.MimeTypes;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints in models defined by static N3 documents.
 *
 * @created 2004-09-10
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:02 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class N3ContentHandler implements ContentHandler {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(N3ContentHandler.class);

  private static final Collection<MimeType> mimeTypes = 
    Collections.unmodifiableList(Arrays.asList(MimeTypes.APPLICATION_N3, MimeTypes.TEXT_RDF_N3, MimeTypes.TEXT_TURTLE));

  private static final Collection<String> fileExts =
    Collections.unmodifiableList(Arrays.asList("n3", "nt", "ttl"));

  //
  // Methods implementing ContentHandler
  //

  /* (non-Javadoc)
   * @see org.mulgara.content.ContentHandler#getContentTypes()
   */
  public Collection<MimeType> getContentTypes() {
    return mimeTypes;
  }

  /* (non-Javadoc)
   * @see org.mulgara.content.ContentHandler#getFileExtensions()
   */
  public Collection<String> getFileExtensions() {
    return fileExts;
  }

  public Statements parse(Content content, ResolverSession resolverSession) throws ContentHandlerException {
    return new N3Statements(content, resolverSession);
  }

  /**
   * Writes out the statements in basic NTriples format.
   */
  public void serialize(Statements      statements,
                        Content         content,
                        ResolverSession resolverSession,
                        Map<String,URI> prefixMap)
      throws ContentHandlerException, ModifiedException {
    try {
      if (prefixMap != null) logger.debug("Prefix map will be ignored for N3 output.");
      Writer out = new BufferedWriter(new OutputStreamWriter(content.newOutputStream(), "utf-8"));
      new N3Writer().write(statements, resolverSession, out);
      out.close();
    } catch (IOException e) {
      throw new ContentHandlerException("Failed to serialize N3 to " + content.getURIString(), e);
    }
  }
}
