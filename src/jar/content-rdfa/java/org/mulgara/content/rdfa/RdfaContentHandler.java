/*
 * Copyright 2010 Paula Gearon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.content.rdfa;

// Java packages
// Java 2 enterprise packages
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
 * Resolves constraints in models defined by RDFa in HTML and XHTML documents
 *
 * @created 2010-08-09
 * @author Paula Gearon
 */
public class RdfaContentHandler implements ContentHandler {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RdfaContentHandler.class);

  /** The filename extension for XHTML documents */
  static final String XHTML_EXT = ".xhtml";

  /** The filename extension for HTML documents */
  static final String HTML_EXT = ".html";

  /** Supported content types */
  private static final Collection<MimeType> mimeTypes = 
    Collections.unmodifiableList(Arrays.asList(MimeTypes.APPLICATION_XHTML, MimeTypes.TEXT_HTML));

  /** Supported file types */
  private static final Collection<String> fileExts =
    Collections.unmodifiableList(Arrays.asList("xhtml", "html"));
  
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

  /* (non-Javadoc)
   * @see org.mulgara.content.ContentHandler#parse(org.mulgara.content.Content, org.mulgara.resolver.spi.ResolverSession)
   */
  public Statements parse(Content content, ResolverSession resolverSession) throws ContentHandlerException {
    try {
      return new RdfaStatements(resolverSession, content);
    } catch (Exception e) {
      throw new ContentHandlerException(e.getMessage());
    }
  }

  /**
   * Cannot serialize RDF into HTML.
   */
  public void serialize(Statements      statements,
                        Content         content,
                        ResolverSession resolverSession,
                        Map<String,URI> prefixMap)
      throws ContentHandlerException, ModifiedException {
    throw new UnsupportedOperationException();
  }
}
