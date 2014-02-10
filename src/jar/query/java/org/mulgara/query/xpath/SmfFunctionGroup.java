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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathFunctionException;

import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;

/**
 * Container for functions in the smf domain.
 *
 * @created Dec 15, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class SmfFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the afn: namespace */
  static final String PREFIX = "smf";

  /** The afn: namespace */
  static final String NAMESPACE = "http://www.topquadrant.com/sparqlmotion/smf.html#smf:";

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
    functions.add(new IndexOf());
    functions.add(new LastIndexOf());
    functions.add(new TitleCase());
    functions.add(new Trim());
    return functions;
  }

  // can't do cast, as we don't control RDF types here

  /**
   * Gets the index of the first occurrence of a certain substring in a given search string.
   * Returns null if the substring is not found.
   * smf:indexOf(text, substr)
   * @see http://www.topquadrant.com/sparqlmotion/smf.html#smf:indexOf
   */
  static private class IndexOf extends MulgaraFunction {
    public int getArity() { return 2; }
    public String getName() { return "indexOf/2"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String text = args.get(0).toString();
      String substr = args.get(1).toString();
      int i = text.indexOf(substr);
      return i >= 0 ? i : null;
    }
  }

  /**
   * Gets the last index of the first occurrence of a certain substring in a given search string.
   * Returns null if the substring is not found
   * smf:lastIndexOf(text, substr)
   * @see http://www.topquadrant.com/sparqlmotion/smf.html#smf:lastIndexOf
   */
  static private class LastIndexOf extends MulgaraFunction {
    public int getArity() { return 2; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String text = args.get(0).toString();
      String substr = args.get(1).toString();
      int i = text.lastIndexOf(substr);
      return i >= 0 ? i : null;
    }
  }

  // can't do setLanguage at this level

  /**
   * Converts an input string to title case. For example, germany becomes Germany.
   * smf:titleCase(text)
   * @see http://www.topquadrant.com/sparqlmotion/smf.html#smf:titleCase
   */
  static private class TitleCase extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder text = new StringBuilder(args.get(0).toString());
      int textLen = text.length();
      if (textLen == 0) return "";
      char c = text.charAt(0);
      if (Character.isLowerCase(c)) text.setCharAt(0, Character.toUpperCase(c));
      for (int i = 0; i < textLen; i++) {
        if (text.charAt(i) == ' ' && i < textLen - 1) {
          c = text.charAt(i + 1);
          if (Character.isLowerCase(c)) text.setCharAt(i + 1, Character.toUpperCase(c));
        }
      }
      return text.toString();
    }
  }

  /**
   * Creates a new string value by trimming an input string.
   * Leading and trailing whitespaces are deleted
   * smf:trim
   * @see http://www.topquadrant.com/sparqlmotion/smf.html#smf:trim
   */
  static private class Trim extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      return args.get(0).toString().trim();
    }
  }

  
}
