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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for looking at SPARQL query strings.
 *
 * @created Oct 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class SparqlUtil {

  /** A pattern for extracting the first word from a query */
  static Pattern firstWordPattern = Pattern.compile("^\\s*([^\\{\\s\\*\\?\\$]+)");

  /** A pattern for finding the first WHERE expression */
  static Pattern firstWherePattern = Pattern.compile("\\s+where\\s*", Pattern.CASE_INSENSITIVE);

  /** A list of keywords known to absolutely indicate SPARQL */
  static Set<String> absKeywords = new HashSet<String>();

  /** A token for the SELECT keyword */
  static final String SELECT = "select";

  static {
    absKeywords.add("base");
    absKeywords.add("prefix");
    absKeywords.add("construct");
    absKeywords.add("describe");
    absKeywords.add("ask");
  }

  /**
   * Guesses the type of the query language.
   * @param query The text to be parsed by the language interpreter
   * @return <code>true</code> if the query appears to be SPARQL
   */
  public static boolean looksLikeSparql(String query) {
    // get the first word
    Matcher m = firstWordPattern.matcher(query);
    if (!m.find()) return false;
    String firstWord = m.group(1).trim().toLowerCase();

    // if the first word is only legal SPARQL then this is SPARQL
    if (absKeywords.contains(firstWord)) return true;

    // if not "select" then it cannot b SPARQL
    if (!SELECT.equals(firstWord)) return false;

    // look for the "WHERE" clause
    m = firstWherePattern.matcher(query);

    // SPARQL allows WHERE to be skipped
    // there should be a {. If not, then either interpreter will give an error
    if (!m.find()) return true;

    // WHERE exists, look for the first {
    int firstBrace = query.indexOf('{', SELECT.length() + 1);

    // make sure the brace exists, and is either part of this WHERE or was the first in the string
    return firstBrace > 0 && firstBrace <= m.end();
  }
}
