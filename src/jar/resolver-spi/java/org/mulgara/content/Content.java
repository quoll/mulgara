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

package org.mulgara.content;

// Java 2 standard packages
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.activation.MimeType;

/**
 * Content is a sequence of bytes, along with optional metadata describing
 * the stream.  This can include a URI indentifying its source; or a MimeType
 * (ie. one obtained via an HTTP CONTENT-TYPE header, or os-level info such
 * as a VFS or an OSX resource-fork);
 *
 * @created 2004-09-15
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Content {

  /**
   * The MIME type of the content, if any.
   *
   * @return the MIME type of the content, or <code>null</code> if the content
   *   doesn't know its type
   * @throws NotModifiedException if the cache should be consulted instead
   */
  public MimeType getContentType() throws NotModifiedException;

  /**
   * The URI of the source of the content, if any.
   *
   * This is used to resolve any relative URI references contained within it.
   *
   * @return the source URI, or <code>null</code> if the source has no known
   *   URI
   */
  public URI getURI();

  /**
   * Determines whether this content is streaming. If the content is streaming, then the
   * {@link #newInputStream()} method may be called only once; if not, then the client may
   * create and consume multiple input streams.
   * @return <tt>true</tt> if the content is streaming, else <tt>false</tt>.
   */
  public boolean isStreaming();
  
  /**
   * @return a content stream
   * @throws NotModifiedException if the cache validates against the source
   *   server and should be used instead of opening a new stream
   * @throws IOException if the stream can't be obtained
   */
  public InputStream newInputStream() throws IOException, NotModifiedException;

  /**
   * @return a content stream
   * @throws ModifiedException if the cache doesn't validate against the source
   *   server; this avoids lost updates if someone else has modified the
   *   content
   * @throws IOException if the stream can't be obtained
   */
  public OutputStream newOutputStream() throws IOException, ModifiedException;

  /**
   * Gets a string representation of the URI, or something suitable if no URI is available.
   * @return A string containing a URI or a description of an alternative.
   */
  public String getURIString();
}
