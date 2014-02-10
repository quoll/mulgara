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

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.activation.MimeType;

import org.mulgara.content.Content;

/**
 * Wrapper around a {@link InputStream} or {@link OutputStream} to make it satisfy the {@link Content}
 * interface. 
 *
 * @created 2004-10-20
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class StreamContent implements Content {

  /** The wrapped uri to assist with InputStream content detection. */
  private final URI uri;

  /** The wrapped inputStream containing the source content. */
  private final InputStream inputStream;
  
  private final OutputStream outputStream;

  /** The content type of the stream, if provided. */
  private final MimeType contentType;

  //
  // Constructor
  //

  /**
   * Wrap a {@link InputStream} as {@link Content}.
   * Either a URI or a conten type must be supplied to help determine the content
   * of the inputstream.  
   */
  StreamContent(InputStream inputStream, URI uri, MimeType contentType) {
    // Validate "file" parameter
    if (inputStream == null) {
      throw new IllegalArgumentException("Null \"inputStream\" parameter");
    }
    if (uri == null && contentType == null) {
      throw new IllegalArgumentException("Must provide at least one of URI and contentType");
    }
    // Initialize fields
    this.uri = uri;
    this.contentType = contentType;
    this.inputStream = inputStream;
    this.outputStream = null;
  }


  /**
   * Wrap a {@link OutputStream} as {@link Content}.
   * Either a URI or a conten type must be supplied to help determine the content
   * of the inputstream.  
   */
  StreamContent(OutputStream outputStream, URI uri, MimeType contentType) {
    // Validate "outputStream" parameter
    if (outputStream == null) {
      throw new IllegalArgumentException("Null \"outputStream\" parameter");
    }
    // Initialize fields
    this.uri = uri;
    this.contentType = contentType;
    this.inputStream = null;
    this.outputStream = outputStream;
  }
  
  //
  // Methods implementing Content
  //

  /**
   * @return {@inheritDoc}; always returns <code>null</code> because Java
   *   {@link java.io.File}s don't have any inherent MIME type
   */
  public MimeType getContentType() {
    return contentType;
  }

  /**
   * This URI will help determine the contents of the inputStream.
   * @see org.mulgara.content.Content#getURI()
   */
  public URI getURI() {
    return uri;
  }

  /** We wrap an input stream, which may only be consumed once. */
  public boolean isStreaming() {
    return true;
  }

  /**
   * Returns the inputstream supplied by the client  
   * 
   * @see org.mulgara.content.Content#newInputStream()
   */
  public InputStream newInputStream() throws IOException {
    if (inputStream == null) throw new IOException("No input provided for stream content.");
    return inputStream;
  }

  /**
   * {@inheritDoc}
   *
   * Because {@link StreamContent} is only a wrapper around an
   * {@link InputStream}, it's never going to be able to perform output.
   *
   * @throws IOException always
   */
  public OutputStream newOutputStream() throws IOException {
    if (outputStream == null) throw new IOException("No output provided for stream content.");
    return outputStream;
  }

  /** @see org.mulgara.content.Content#getURIString() */
  public String getURIString() {
    return uri == null ? "<<stream>>" : uri.toString();
  }
}
