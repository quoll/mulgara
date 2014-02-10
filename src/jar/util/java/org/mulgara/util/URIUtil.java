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

package org.mulgara.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Utilities for working with URIs in Mulgara.
 *
 * @created Nov 26, 2008
 * @author Paul Gearon
 */
public class URIUtil {

  /** The parameter name for the graph. */
  private static final String GRAPH = "graph";

  /**
   * Convert a graph to a localized form, if it is defined for localizing.
   * @param uri The URI to convert.
   * @return A reference to a local graph URI. This will be the uriRef if it does not require localizing.
   * @throws QueryException If the local graph has an illegal name.
   */
  public static URI localizeGraphUri(URI uri) throws URISyntaxException {
    QueryParams params = QueryParams.decode(uri);
    String graphName = params.get(GRAPH);
    return (graphName == null) ? uri : new URI(graphName);
  }


  /**
   * Replace an alias in a URI, if one is recognized.
   * @param uriString A string with the initial uri to check for aliases.
   * @param aliasMap The map of known aliases to the associated URIs
   * @return A new URI with the alias replaced, or the original if no alias is found.
   */
  public static URI convertToURI(String uriString, Map<String,URI> aliasMap) {
    try {
      URI uri = new URI(uriString);
      if (uri.isOpaque()) {
        // Attempt qname-to-URI substitution for aliased namespace prefixes
        URI mapping = aliasMap.get(uri.getScheme());
        if (mapping != null) {
          uri = new URI(mapping.toString() + uri.getSchemeSpecificPart() +
                        (uri.getFragment() != null ? "#" + uri.getFragment() : ""));
        }
      }
      return uri;
    } catch (URISyntaxException e) {
      throw new RuntimeException("Bad URI syntax in resource", e);
    }
  }

  /**
   * Converts a URI into a QName, based entirely on parsing.
   * @param u The URI to parse.
   * @return A QName containing the URI as a namespace/localpart combination, or the entire
   *         uri as a localpart if it could not be parsed.
   */
  public static QName parseQName(URI u) {
    String s = u.toString();
    for (int i = s.length() - 1; i > 0; i--) {
      if (!localNameChar(s.charAt(i))) {
        // found the place where a name has to start after
        for (int p = i + 1; p < s.length(); p++) {
          if (localStartNameChar(s.charAt(p))) return new QName(s.substring(0, p), s.substring(p));
        }
        return new QName(s);
      }
    }
    return new QName(s);
  }

  /**
   * Indicates if the char is a valid XML name character, minus the colon character.
   * @see http://www.w3.org/TR/REC-xml/#NT-NameStartChar
   * @param c The character to test.
   * @return <code>true</code> if the character is a valid start to an XML name.
   */
  private static final boolean localNameChar(char c) {
    return localStartNameChar(c) ||
           c == '-' || c == '.' || (c >= '0' && c <= '9') ||
           c == 0xB7 || (c >= 0x0300 && c <= 0x036F) || (c >= 0x203F && c <= 0x2040);
  }

  /**
   * Indicates if the char is a valid start for an XML name character, minus the colon character.
   * @see http://www.w3.org/TR/REC-xml/#NT-NameStartChar
   * @param c The character to test.
   * @return <code>true</code> if the character is a valid start to an XML name.
   */
  private static final boolean localStartNameChar(char c) {
    return (c >= 'A' && c <= 'Z') ||
           (c >= 'a' && c <= 'z') ||
           c == '_' ||
           (c >= 0xC0 && c <= 0xD6) ||
           (c >= 0xD8 && c <= 0xF6) ||
           (c >= 0xF8 && c <= 0x2FF) ||
           (c >= 0x370 && c <= 0x37D) ||
           (c >= 0x37F && c <= 0x1FFF) ||
           (c >= 0x200C && c <= 0x200D) ||
           (c >= 0x2070 && c <= 0x218F) ||
           (c >= 0x2C00 && c <= 0x2FEF) ||
           (c >= 0x3001 && c <= 0xD7FF) ||
           (c >= 0xF900 && c <= 0xFDCF) ||
           (c >= 0xFDF0 && c <= 0xFFFD) ||
           (c >= 0x10000 && c <= 0xEFFFF);
  }
}
