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

package org.mulgara.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 * This class extends multipart MIME objects to lookup parameter values.
 *
 * @created Sep 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class MimeMultiNamedPart extends MimeMultipart {

  /** The number of elements in internal data buffers. */
  private static final int BUFFER_SIZE = 1024;

  /**
   * @param src The data source to retrieve the MIME data from
   * @throws MessagingException If the source cannot be parsed as valid MIME data.
   */
  public MimeMultiNamedPart(DataSource arg0) throws MessagingException {
    super(arg0);
  }


  /**
   * Goes through MIME data to look for a parameter.
   * @param paramName The name of the parameter to retrieve
   * @return The value for the parameter.
   * @throws MessageException If the MIME data could not be parsed.
   */
  public Object getParameter(String param) throws MessagingException, IOException {
    BodyPart part = getNamedPart(param);
    return part == null ? null : part.getContent();
  }


  /**
   * Goes through MIME data to look for a string parameter.
   * @param paramName The name of the parameter to retrieve
   * @return The string value for the parameter, converting if needed.
   * @throws MessageException If the MIME data could not be parsed.
   */
  public String getParameterString(String param) throws MessagingException, IOException {
    Object obj = getParameter(param);
    if (obj == null) return null;
    return toString(obj);
  }


  /**
   * Finds a body part that has the requested name.
   * @param paramName The name of the part to get.
   * @return The body part with the requested name, or null if not found.
   * @throws MessagingException If the MIME object could not be scanned.
   */
  public BodyPart getNamedPart(String paramName) throws MessagingException {
    for (int p = 0; p < getCount(); p++) {
      BodyPart bpart = getBodyPart(p);
      if (paramName.equalsIgnoreCase(getPartName(bpart))) return bpart;
    }
    return null;
  }


  /**
   * Look up the name of a part by index.
   * @param partNr The index of the part to look up.
   * @return The name of the part, or null if not available.
   * @throws MessagingException If the MIME object could not be scanned.
   */
  public String getPartName(int partNr) throws MessagingException {
    return getPartName(getBodyPart(partNr));
  }


  /**
   * Gets the name of a body part.
   * @param part The body part to get the name of.
   * @return The name of this part, or <code>null</code> if no name can be found.
   * @throws MessagingException The part could not be accessed.
   */
  public static String getPartName(BodyPart part) throws MessagingException {
    String[] cds = part.getHeader("Content-Disposition");
    if (cds == null) return null;
    // probably only has one Content-Disposition header, but check all anyway
    for (String header: cds) {
      for (String kv: header.split("; ")) {
        int eq = kv.indexOf('=');
        if (eq >= 0) {
          // a key=value element
          String key = kv.substring(0, eq);
          if ("name".equalsIgnoreCase(key)) {
            String value = kv.substring(eq + 1);
            return stripQuotes(value);
          }
        }
      }
    }
    return null;
  }


  /**
   * Removes quote characters from around a string.
   * @param str The string to remove quotes from.
   * @return The part of str that was between quotes, or all of str if there were no quotes.
   */
  private static String stripQuotes(String str) {
    int l = str.length() - 1;
    if (str.charAt(0) == '"' && str.charAt(l) == '"') str = str.substring(1, l);
    return str;
  }


  /**
   * Gets a string from an object. If the object is a stream, then it reads the stream
   * otherwise it gets the string form of the object.
   * @param o The object to convert to a string.
   * @return The string form of the object, or <code>null</code> if the object could not be read.
   */
  private static String toString(Object o) {
    if (o instanceof InputStream) {
      CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
      StringBuilder sb = new StringBuilder();
      InputStreamReader in = new InputStreamReader((InputStream)o);
      try {
        while (in.read(buffer) >= 0) {
          buffer.flip();
          sb.append(buffer);
        }
        o = sb;
      } catch (IOException e) {
        return null;
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          // got our data at this point, so ignore
        }
      }
    }
    return o.toString();
  }
}
