/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.query.rdf.Mulgara;
import org.mulgara.sparql.parser.ParseException;

import javax.xml.namespace.QName;

/**
 * Represents IRI references in SPARQL. This is a superset of URI references.
 * For the moment, just wrap a URI, even though this doesn't meet the strict
 * definition of an IRI.
 *
 * @created Feb 8, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class IRIReference implements Node, PrimaryExpression, Verb, Cloneable {
  
  /** The RDF namespace */
  private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /** Constant IRI for RDF_TYPE */
  public static final IRIReference RDF_TYPE = new IRIReference(URI.create(RDF_NS + "type"));

  /** Constant IRI for RDF_FIRST */
  public static final IRIReference RDF_FIRST = new IRIReference(URI.create(RDF_NS + "first"));

  /** Constant IRI for RDF_REST */
  public static final IRIReference RDF_REST = new IRIReference(URI.create(RDF_NS + "rest"));

  /** Constant IRI for RDF_NIL */
  public static final IRIReference RDF_NIL = new IRIReference(URI.create(RDF_NS + "nil"));

  /** The made-up namespace for op: XPath functions */
  public static final String OP_NS = "op";

  /** The "op" prefix for XPath functions */
  public static final String OP_PREFIX = OP_NS + ":";

  /** The prefix for the sparql: namespace */
  static final String SPARQL_PREFIX = "sparql";

  /** The sparql: namespace */
  static final URI SPARQL_NS;

  /** The prefix for the fn: namespace */
  static final String FN_PREFIX = "fn";

  /** The fn: namespace */
  static final URI FN_NS;

  /** The internal URI value */
  private URI uri;

  /** The original text of the URI */
  private String text;

  /** The qname form, if available */
  private QName qname;

  /** A modifier when this reference is used as a predicate */
  private Modifier mod = Modifier.none;

  static {
    SPARQL_NS = URI.create("http://www.w3.org/2006/sparql-functions#");
    FN_NS = URI.create("http://www.w3.org/2005/xpath-functions/#");
  }

  /**
   * Create an IRI reference from a URI.
   * @param uri The URI referred to.
   */
  public IRIReference(URI uri) {
    this.uri = uri;
    text = "<" + uri.toString() + ">";
    qname = parseQName(uri.toString());
  }

  /**
   * Create an IRI reference from a URI.
   * @param uri The URI referred to.
   * @param namespaces The environment's map of prefix to namespace URIs.
   */
  public IRIReference(URI uri, Map<String,URI> namespaces) {
    this.uri = uri;
    text = "<" + uri.toString() + ">";
    qname = parseQName(uri.toString(), namespaces);
  }

  /**
   * Create an IRI reference from a URI.
   * @param uri The URI referred to.
   * @param namespaces The environment's map of prefix to namespace URIs.
   */
  public IRIReference(String uri, Map<String,URI> namespaces) throws ParseException {
    try {
      this.uri = new URI(uri);
    } catch (URISyntaxException e) {
      // Mulgara hack for handling the system graph
      if (!uri.endsWith("##")) throw new ParseException("Unable to create a URI from: " + uri);
      this.uri = URI.create("#");
    }
    text = "<" + uri + ">";
    qname = parseQName(uri.toString(), namespaces);
  }

  /**
   * Create an IRI reference from a URI with an abbreviated namespace, with a known namespace.
   * @param uri The URI referred to.
   * @param text The abbreviated form.
   */
  public IRIReference(URI namespace, String prefix, String localPart) throws ParseException {
    try {
      this.uri = new URI(namespace + localPart);
    } catch (URISyntaxException e) {
      throw new ParseException("Unable to create URI: " + namespace + localPart);
    }
    if (prefix == null) {
      text = "<:" + localPart + ">";
      qname = new QName(namespace.toString(), localPart);
    } else {
      text = prefix + ":" + localPart;
      qname = new QName(namespace.toString(), localPart, prefix);
    }
  }

  /**
   * @return the IRI value
   */
  public URI getUri() {
    return uri;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return text;
  }

  /**
   * Retrieves the qname for this URI, if one is known.
   * @return The known QName, or <code>null</code> if none available.
   */
  public QName getQName() {
    return qname;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "<" + uri.toString() + ">";
  }

  /**
   * Parse a URI, looking for a valid QName structure.
   * @param uri The URI to parse.
   * @return a new QName if one could be parsed, or <code>null</code> if one couldn't be found.
   * @param namespaces The environment's map of prefix to namespace URIs.
   */
  private QName parseQName(String uri) {
    Map<String,URI> e = Collections.emptyMap();
    return parseQName(uri, e);
  }

  /**
   * Parse a URI, looking for a valid QName structure.
   * @param uri The URI to parse.
   * @return a new QName if one could be parsed, or <code>null</code> if one couldn't be found.
   * @param namespaces The environment's map of prefix to namespace URIs.
   */
  private QName parseQName(String uri, Map<String,URI> namespaces) {
    // the "op" prefix is special, in that it has no namespace
    // check if the URI starts with "op:" and is followed by a valid local name character
    if (uri.startsWith(OP_PREFIX) && localStartNameChar(uri.charAt(OP_PREFIX.length()))) {
      return new QName(OP_NS, uri.substring(OP_PREFIX.length()), OP_PREFIX);
    }

    // expand the namespaces to include some defaults, if missing
    namespaces = expandedNamespaces(namespaces);

    // search for a namespace we know about
    for (Map.Entry<String,URI> e: namespaces.entrySet()) {
      String namespaceUri = e.getValue().toString();
      if (uri.startsWith(namespaceUri)) {
        return new QName(namespaceUri, uri.substring(namespaceUri.length()), e.getKey());
      }
    }

    return null;
  }

  /**
   * Expand on a namespace map to include some useful defaults.
   * @param original The original namespace map of prefixes to namespace URIs.
   * @return A new namespace map with the added entries.
   */
  private static Map<String,URI> expandedNamespaces(Map<String,URI> original) {
    Map<String,URI> result = new HashMap<String,URI>(original);
    if (!result.containsKey(SPARQL_PREFIX)) result.put(SPARQL_PREFIX, URI.create("http://www.w3.org/2006/sparql-functions#"));
    if (!result.containsKey(FN_PREFIX)) result.put(FN_PREFIX, URI.create("http://www.w3.org/2005/xpath-functions/#"));
    if (!result.containsKey(Mulgara.NS_PREFIX)) result.put(Mulgara.NS_PREFIX, Mulgara.NS_URI);
    return result;
  }

  /**
   * Indicates if the char is a valid XML name character, minus the colon character.
   * @see http://www.w3.org/TR/REC-xml/#NT-NameStartChar
   * @param c The character to test.
   * @return <code>true</code> if the character is a valid start to an XML name.
   */
  @SuppressWarnings("unused")
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

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Unable to copy an IRIReference");
    }
  }

  public void setModifier(Modifier m) {
    mod = m;
  }
  
  public Modifier getModifier() {
    return mod;
  }
}
