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

package org.mulgara.resolver.jar;

//Local packages
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.mulgara.content.Content;

/**
 * Wrapper around a {@link URL}to make it satisfy the {@link Content}
 * interface.
 * 
 * @created 2004-09-23
 * @author Tate Jones
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:46 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology </a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana Technology
 *            Inc </a>
 * @licence <a href=" {@docRoot}/../../LICENCE">Mozilla Public License v1.1
 *          </a>
 */
public class JarContent implements Content {

  /** The wrapped URL. */
  private URL url;

  /** The URI version of the URL */
  private URI urlUri;


  public JarContent(URI uri) throws URISyntaxException, MalformedURLException {
    this(uri.toURL());
  }


  /**
   * Constructor.
   *
   * @param url The URL this object will be representing the content of
   */
  public JarContent(URL url) throws URISyntaxException {

    // Validate "url" parameter
    if (url == null) throw new IllegalArgumentException("Null \"url\" parameter");

    // Store the URL this class represents
    this.url = url;

    // Convert the URL to a Uri
    urlUri = new URI(url.toExternalForm());
  }

  public MimeType getContentType() {
    try {
      return new MimeType(url.openConnection().getContentType());
    } catch (IOException e) {
      return null;
    } catch (MimeTypeParseException e) {
      return null;
    }
  }

  /**
   * Retrieves the URI for the actual content.
   *
   * @return The URI for the actual content
   */
  public URI getURI() {
    return urlUri;
  }

  /** The archive can be re-opened as many times as necessary. */
  public boolean isStreaming() {
    return false;
  }


  /**
   * Creates an input stream to the resource whose content we are representing.
   *
   * @return An input stream to the resource whose content we are representing
   *
   * @throws IOException
   */
  public InputStream newInputStream() throws IOException {
    // Create an input stream by opening the URL's input stream
    InputStream inputStream = url.openStream();
    assert inputStream != null;
    return inputStream;
  }

  /**
   * @throws IOException always (not implemented)
   */
  public OutputStream newOutputStream() throws IOException {
    throw new IOException("Output of JAR content not implemented");
  }

  /** @see org.mulgara.content.Content#getURIString() */
  public String getURIString() {
    return urlUri.toString();
  }
}
