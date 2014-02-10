/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.xpath;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathFunctionException;

import org.jrdf.graph.BlankNode;
import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;
import org.mulgara.util.URIUtil;

/**
 * Container for functions in the afn domain.
 *
 * @created Dec 14, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class AfnFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the afn: namespace */
  static final String PREFIX = "afn";

  /** The afn: namespace */
  static final String NAMESPACE = "http://jena.hpl.hp.com/ARQ/function#";
  // static final String NAMESPACE = "http://openjena.org/ARQ/function#";

  /**
   * Get the prefix used for the namespace of these operations.
   * @return The short string used for a prefix in a QName.
   */
  public String getPrefix() {
    return PREFIX;
  }

  /**
   * Get the namespace of these operations.
   * @return The string of the namespace URI.
   */
  public String getNamespace() {
    return NAMESPACE;
  }

  /**
   * Get the set of SPARQL functions.
   * @return A set of MulgaraFunction for this entire group.
   */
  public Set<MulgaraFunction> getAllFunctions() {
    Set<MulgaraFunction> functions = new HashSet<MulgaraFunction>();
    functions.add(new Substring2());
    functions.add(new Substring3());
    functions.add(new Substr2());
    functions.add(new Substr3());
    functions.add(new Strjoin());
    functions.add(new Sha1sum());
    functions.add(new Now());
    functions.add(new Bnode());
    functions.add(new Localname());
    functions.add(new Namespace());
    functions.add(new Min());
    functions.add(new Max());
    functions.add(new Pi());
    functions.add(new E());
    return functions;
  }

  /**
   * As for {@link org.mulgara.query.xpath.FnFunctionGroup.Substring2}, but with Java semantics.
   * @see http://jena.hpl.hp.com/ARQ/function#substr
   */
  static private class Substring2 extends MulgaraFunction {
    public int getArity() { return 2; }
    public String getName() { return "substring/2"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String source = args.get(0).toString();
      int start = ((Number)args.get(1)).intValue();
      // perform boundary checking
      int len = source.length();
      if (start < 0) start = 0;
      if (start > len) start = len;
      return source.substring(start);
    }
  }

  /**
   * As for {@link org.mulgara.query.xpath.FnFunctionGroup.Substring3} but with Java semantics.
   * @see http://jena.hpl.hp.com/ARQ/function#substr
   */
  static private class Substring3 extends MulgaraFunction {
    public int getArity() { return 3; }
    public String getName() { return "substring/3"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String source = args.get(0).toString();
      int start = ((Number)args.get(1)).intValue();
      int end = ((Number)args.get(2)).intValue();
      // perform boundary checking
      if (start < 0) start = 0;
      int len = source.length();
      if (start > len) {
        start = len;
        end = len;
      }
      if (end > len) end = len;
      if (end < start) end = start;
      
      return source.substring(start, end);
    }
  }

  /**
   * Synonym for afn:substring
   */
  static private class Substr2 extends Substring2 {
    public String getName() { return "substr/2"; }
  }

  /**
   * Synonym for afn:substring
   */
  static private class Substr3 extends Substring3 {
    public String getName() { return "substr/3"; }
  }

  /**
   * Join all the arguments using a separator found in the first parameter.
   * afn:strjoin(separator, sequence...)
   * @see http://jena.hpl.hp.com/ARQ/function#strjoin
   */
  static private class Strjoin extends MulgaraFunction {
    public String getName() { return "strjoin/*"; }
    public int getArity() { return -1; }
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder s = new StringBuilder();
      int lastIndex = args.size() - 1;
      String separator = (String)args.get(0);
      for (int i = 1; i < lastIndex; i++) {
        if (i != 1) s.append(separator);
        s.append(args.get(i));
      }
      return s.toString();
    }
  }

  /**
   * Calculate the SHA1 checkum of a literal or URI
   * afn:sha1sum(resource)
   * @see http://jena.hpl.hp.com/ARQ/function#sha1sum
   */
  static private class Sha1sum extends MulgaraFunction {
    public String getName() { return "sha1sum/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String resource = args.get(0).toString();
      try {
        byte[] bytes = resource.getBytes("UTF8");
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.update(bytes, 0, bytes.length);
        
        return toHexString(digest.digest());
      } catch (UnsupportedEncodingException e) {
        throw new XPathFunctionException("Unable to handle data as UTF-8: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
        throw new XPathFunctionException("SHA1 algorithm is not available: " + e.getMessage());
      }
    }
    static final String toHexString(byte[] data) {
      StringBuilder s = new StringBuilder();
      for (byte b: data) {
        s.append(Integer.toHexString((b & 0xF0) >> 4));
        s.append(Integer.toHexString(b & 0x0F));
      }
      return s.toString();
    }
  }

  /**
   * Returns the time this function is called. This will increase throughout a query if the
   * query takes any measurable time.
   * afn:now()
   * @see http://jena.hpl.hp.com/ARQ/function#now
   */
  static private class Now extends MulgaraFunction {
    public int getArity() { return 0; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return new Date();
    }
  }
  
  /**
   * Return the blank node label if ?x is a blank node.
   * afn:bnode(node)
   * @see http://jena.hpl.hp.com/ARQ/function#bnode
   */
  static private class Bnode extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Object resource = args.get(0);
      return (resource instanceof BlankNode) ? ((BlankNode)resource).toString() : "";
    }
  }

  /**
   * The local name of u if an IRI.
   * Based on splitting the IRI, not on any prefixes in the query or dataset
   * afn:localname(u)
   * @see http://jena.hpl.hp.com/ARQ/function#localname
   */
  static private class Localname extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Object resource = args.get(0);
      return (resource instanceof URI) ? localName((URI)resource) : "";
    }
    static String localName(URI u) {
      return URIUtil.parseQName(u).getLocalPart();
    }
  }
  
  /**
   * The namespace of an IRI.
   * Based on splitting the IRI, not on any prefixes in the query or dataset
   * afn:namespace(u)
   * @see http://jena.hpl.hp.com/ARQ/function#namespace
   */
  static private class Namespace extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Object resource = args.get(0);
      return (resource instanceof URI) ? namespace((URI)resource) : "";
    }
    static String namespace(URI u) {
      return URIUtil.parseQName(u).getNamespaceURI();
    }
  }
  
  /**
   * Return the minimum of two expressions evaluating to numbers.
   * afn:min(x,y)
   * @see http://jena.hpl.hp.com/ARQ/function#min
   */
  static private class Min extends MulgaraFunction {
    public int getArity() { return 2; }
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      Number y = (Number)args.get(1);
      return x.doubleValue() < y.doubleValue() ? x : y;
    }
  }

  /**
   * Return the maximum of two expressions evaluating to numbers.
   * afn:max(x,y)
   * @see http://jena.hpl.hp.com/ARQ/function#max
   */
  static private class Max extends MulgaraFunction {
    public int getArity() { return 2; }
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      Number y = (Number)args.get(1);
      return x.doubleValue() > y.doubleValue() ? x : y;
    }
  }

  /**
   * The value of pi.
   * afn:pi()
   * @see http://jena.hpl.hp.com/ARQ/function#pi
   */
  static private class Pi extends MulgaraFunction {
    public int getArity() { return 0; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return Math.PI;
    }
  }

  /**
   * The value of e.
   * afn:e()
   * @see http://jena.hpl.hp.com/ARQ/function#e
   */
  static private class E extends MulgaraFunction {
    public int getArity() { return 0; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return Math.E;
    }
  }

}
