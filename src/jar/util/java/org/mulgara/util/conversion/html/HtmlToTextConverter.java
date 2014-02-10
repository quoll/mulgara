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

package org.mulgara.util.conversion.html;

// Java 2 standard packages
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Converts HTML text into unicode text. <p>
 *
 * The method {@link #convert(String)} can be used to replace non-breaking
 * spaces with normal spaces if the unicode value is not acceptable. </p> <p>
 *
 * The end of line characters can be set using the System property <code>line.separator</code>
 * . The tags for putting end of lines in at can be set using {@link
 * #getEndlineSet()} and {@link #setEndlineSet(Set)}. </p> <p>
 *
 * Whether or not titles and image alt tags are included in the output can be
 * set using {@link #setIncludeTitle(boolean)} and {@link
 * #setIncludeImageAlts(boolean)}. </p>
 *
 * @created 2002-08-01
 *
 * @author Ben Warren
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:30 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class HtmlToTextConverter {
  
  private static final Logger logger = Logger.getLogger(HtmlToTextConverter.class);

  /**
   * Private constructor to stop instanciation.
   */
  private HtmlToTextConverter() {

  }

  /**
   * Set whether or not normal spaces will replace non-breaking space entities.
   *
   * @param value If true normal spaces will replace non-braking spaces
   *      otherwise non-breaking spaces will be used.
   */
  public static void setUseNormalSpace(boolean value) {

    // Normal space
    if (value) {

      Entities.add("&nbsp", 32);
    }

    // Non-breaking
    else {

      Entities.add("&nbsp", 160);
    }
  }

  /**
   * Set the set of tags to put an end of line in for. The tag text excludes the
   * closing angle bracket. eg &lt;br or &lt;/h1
   *
   * @param set The end of line tag set.
   */
  public static void setEndlineSet(Set<String> set) {

    HTMLParser.setEndlineSet(set);
  }

  /**
   * Set if the title should be included in the text output.
   *
   * @param include The title will be included if true. Defaults to true.
   */
  public static void setIncludeTitle(boolean include) {

    HTMLParser.setIncludeTitle(include);
  }

  /**
   * Set if image alt tags should be included in the text output.
   *
   * @param include The image alt tags will be included if true. Defaults to
   *      true.
   */
  public static void setIncludeImageAlts(boolean include) {

    HTMLParser.setIncludeImageAlts(include);
  }

  /**
   * Get the set of tags to put an end of line in for. The tag text excludes the
   * closing angle bracket. eg &lt;br or &lt;/h1
   *
   * @return The end of line tag set.
   */
  public static Set<String> getEndlineSet() {

    return HTMLParser.getEndlineSet();
  }

  /**
   * Convert a string of HTML to a text string.
   *
   * @param html The string of HTML.
   * @return The converted string or null if <code>html</code> was null.
   * @throws ParseException If there is a problem parsing the HTML.
   * @throws IOException If an IO error occurs.
   */
  public static String convert(String html) throws ParseException, IOException {

    String text = null;

    if (html != null) {

      HTMLParser parser = new HTMLParser(new StringReader(html));
      StringWriter writer = new StringWriter();

      // Create the text string
      Reader reader = parser.getReader();

      try {
        char[] buffer = new char[1024];
        int numRead = reader.read(buffer);

        while (numRead != -1) {

          writer.write(buffer, 0, numRead);
          numRead = reader.read(buffer);
        }
      } finally {
        try {
          reader.close();
        } catch (IOException e) {
          logger.warn("Error closing reader", e);
        }
      }

      writer.flush();
      text = writer.toString();
    }

    return text;
  }

  /**
   * Convert a reader with a HTML stream into a reader with a text stream.
   *
   * @param html The HTML reader.
   * @return A reader that reads the text conversion stream or null if <code>html</code>
   *      was null.
   * @throws ParseException If there is a problem parsing the HTML.
   * @throws IOException If an IO error occurs.
   */
  public static Reader convert(Reader html) throws ParseException, IOException {

    if (html != null) {

      HTMLParser parser = new HTMLParser(html);

      return parser.getReader();
    }
    else {

      return null;
    }
  }

  /**
   * Runs this converter over a whole directory of HTML files or a single HTML
   * file and prints the text conversion to standard out. <p>
   *
   * The args are either:
   * <ol>
   *   <li> -dir directory_name (convert entire directory) or</li>
   *   <li> file_name (convert a single file)</li>
   * </ol>
   * </p>
   *
   * @param args The command line args.
   * @throws Exception on error.
   */
  public static void main(String[] args) throws Exception {

    // Directory
    if ("-dir".equals(args[0])) {

      String[] files = new File(args[1]).list();
      java.util.Arrays.sort(files);

      for (int i = 0; i < files.length; i++) {

        System.err.println(files[i]);

        File file = new File(args[1], files[i]);
        parse(file);
      }
    }

    // One file
    else {

      parse(new File(args[0]));
    }
  }

  /**
   * Convert a HTML file to text and write it to standard out.
   *
   * @param file The file to convert.
   * @throws Exception on error.
   */
  private static void parse(File file) throws Exception {

    HTMLParser parser = new HTMLParser(file);
    BufferedReader reader = new BufferedReader(parser.getReader());

    try {
      for (String l = reader.readLine(); l != null; l = reader.readLine()) {

        System.out.println(l);
      }
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        System.err.println("Error closing reader");
        e.printStackTrace(System.err);
      }
    }
  }
}
