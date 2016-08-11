/*
 * Copyright 2008 Fedora Commons, Inc.
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


package org.mulgara.content.rlog;

// Java 2 enterprise packages
import java.io.BufferedWriter;
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
 * Resolves constraints in models defined by RLog documents.
 *
 * @created Feb 24, 2009
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RlogContentHandler implements ContentHandler {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(RlogContentHandler.class);

  /** Supported content types */
  private static final Collection<MimeType> mimeTypes = 
    Collections.unmodifiableList(Arrays.asList(MimeTypes.TEXT_RLOG));

  /** Supported file types */
  private static final Collection<String> fileExts =
    Collections.unmodifiableList(Arrays.asList("dl", "rl", "rlog"));

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
    return new RlogStatements(content, resolverSession);
  }

  /**
   * Writes out the statements in basic RLog format.
   */
  public void serialize(Statements statements, Content content, ResolverSession resolverSession, Map<String,URI> prefixMap)
          throws ContentHandlerException, ModifiedException {
    try {
      if (prefixMap != null) logger.debug("Prefix map will be ignored for RLog writer.");
      Writer out = new BufferedWriter(new OutputStreamWriter(content.newOutputStream(), "utf-8"));
      RlogStructure struct = new RlogStructure(resolverSession);
      struct.load(statements);
      struct.write(out);
      out.close();
    } catch (Exception e) {
      throw new ContentHandlerException("Failed to serialize RLog to " + content.getURIString(), e);
    }
  }
}
