/*
 * Copyright 2009 Fedora Commons, Inc.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jrdf.graph.Literal;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.util.StringUtil;

/**
 * Represents a triple to be created or removed. Null values indicate blank nodes.
 *
 * @created Feb 15, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class LocalTriple implements Triple {

  /** Generated serialization ID */
  private static final long serialVersionUID = -2922482409580801899L;

  final URI subject;
  final URI predicate;
  final Object object;

  final boolean hasLiteral;

  public LocalTriple(String s, String p, String o) throws ServletException {
    this(s, p, o, false);
  }


  /**
   * Creates a new triple.
   * @param s The subject.
   * @param p The predicate.
   * @param o The object.
   */
  public LocalTriple(String s, String p, String o, boolean validate) throws ServletException {
    if (validate) {
      if (s == null || s.length() == 0) throw new BadRequestException("Blank subject node not permitted in this operation");
      if (o == null || o.length() == 0) throw new BadRequestException("Blank object node not permitted in this operation");
    }
    try {
      subject = s == null ? null : new URI(s);
    } catch (URISyntaxException e) {
      throw new BadRequestException("Invalid subject URI: " + s);
    }

    if (p == null) throw new BadRequestException("Illegal triple presented. Predicate cannot be blank.");
    try {
      predicate = new URI(p);
    } catch (URISyntaxException e) {
      throw new BadRequestException("Invalid predicate URI: " + p);
    }

    // temporary variables because the compiler doesn't understand code paths when assigning finals
    Object tmpo;
    boolean tmpl;
    try {
      tmpl = false;
      if (o == null || o.length() == 0) {  // no object, so it's blank
        tmpo = null;
      } else {
        char zc = o.charAt(0);
        if (zc == '\'' || zc == '"') {     // starts with quote, so treat it as a literal
          tmpo = new LocalLiteral(o);
          tmpl = true;
        } else {                           // attempt to load as a URI
          tmpo = new URI(o);
        }
      }
    } catch (URISyntaxException e) {
      tmpo = new LocalLiteral(o);
      tmpl = true;
    }
    object = tmpo;
    hasLiteral = tmpl;
  }


  /** @return the subject. <code>null</code> indicates a blank node. */
  public SubjectNode getSubject() {
    return subject == null ? new BlankNodeImpl() : new URIReferenceImpl(subject);
  }


  /** @return the predicate. */
  public PredicateNode getPredicate() {
    return new URIReferenceImpl(predicate);
  }


  /**
   * Get the object. <code>null</code> indicates a blank node.
   * The resulting type is either a URI or a Literal.
   * @return the object.
   */
  public ObjectNode getObject() {
    return object == null ? new BlankNodeImpl() :
      (hasLiteral ? ((LocalLiteral)object).toJRDFLiteral() : new URIReferenceImpl((URI)object, false));
  }


  /**
   * Present this object as a singleton set.
   * @return A set containing only this triple.
   */
  public Set<Triple> toSet() {
    return Collections.singleton((Triple)this);
  }


  /**
   * Represents a literal node found in a request.
   */
  public class LocalLiteral {
    String text;
    URI type;
    String lang;

    LocalLiteral(String s) {
      type = null;
      lang = null;

      String quot = null;
      char c = s.charAt(0);
      if (c == '\'') {
        quot = s.charAt(1) == '\'' ? "'''" : "'";
      } else if (c == '"') {
        quot = s.charAt(1) == '"' ? "\"\"\"" : "\"";
      }
      text = parseQuoted(quot, s);
    }


    /**
     * Construct a JRDF literal for use in the query system.
     * @return A new Literal object with the established parameters.
     */
    public Literal toJRDFLiteral() {
      if (type != null) return new LiteralImpl(text, type);
      if (lang != null) return new LiteralImpl(text, lang);
      return new LiteralImpl(text);
    }


    /**
     * Pull a literal apart into its constituents. If any part is not valid, then the entire
     * string is treated as a single literal. This method sets the lang or uri sections of
     * the literal.
     * @param q The quote character used to wrap the string
     * @param n The string for the literal node.
     * @return The text portion of the literal.
     */
    private String parseQuoted(String q, String n) {
      // unquoted, so return entire string
      if (q == null) return n;
      Pattern p = Pattern.compile(q + "(.*)" + q + "((@([a-zA-Z\\-]+))|(\\^\\^((.+))))?");
      Matcher m = p.matcher(n);
      // no match, so entire thing is literal
      if (!m.find()) return n;
      // partial match, so return entire string
      if (m.end() != n.length()) return n;
      // test for unquoted characters
      String g1 = m.group(1).replaceAll("\\\\\\\\", "");
      if (containsUnquoted(g1, q.substring(0, 1))) return n;
      // get the language
      lang = m.group(4);
      if (lang == null) {
        // no language, so get the type URI
        String t = m.group(6);
        if (t != null) {
          try {
            type = new URI(t);
          } catch (URISyntaxException e) {
            // invalid URI, so return the entire string as a literal
            return n;
          }
        }
      }
      return StringUtil.unescapeJavaString(m.group(1));
    }


    /**
     * Tests the contents of a string for unescaped quote characters.
     * @param str The string to test.
     * @param q The type of quote character to look for.
     * @return <code>true</code> if the string contains an unescaped quote.
     */
    private boolean containsUnquoted(String str, String q) {
      Matcher m = Pattern.compile(q).matcher(str);
      while (m.find()) {
        int s = m.start();
        if (s == 0 || str.charAt(s - 1) != '\\') return true;
      }
      return false;
    }
  }

}
