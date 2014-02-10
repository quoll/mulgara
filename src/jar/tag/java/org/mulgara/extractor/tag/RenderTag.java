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

package org.mulgara.extractor.tag;

// Java 2 enterprise packages
import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

// third party packages
import org.apache.log4j.*;
import org.mulgara.util.*;

/**
 * Renders the output of an alldocs or metadata tag using the rendering handlers
 * for each schema class. <p>
 *
 * The default rendering handlers (XSL stylesheets) can be overriden by
 * including a handler tag in the body of this tag. </p>
 *
 * @created 2001-10-17
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:08 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RenderTag extends BodyTagSupport {

  private static final long serialVersionUID = 5508736603982578506L;

  private static final boolean DEBUG = false;
  
  //
  // TMex classes
  //

  /**
   * the class
   */
  protected final static String TMEX_CLASS_DOCUMENT =
    "http://mulgara.org/mulgara/Document#Document";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_DATE =
    "http://mulgara.org/mulgara/tool/DateExtractor#Date";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_GENERICFEATURE =
    "http://mulgara.org/mulgara/tool/FeatureExtractor#GenericFeature";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_PERSON =
    "http://mulgara.org/mulgara/tool/FeatureExtractor#Person";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_COMPANY =
    "http://mulgara.org/mulgara/tool/FeatureExtractor#Company";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_GEOGRAPHIC =
    "http://mulgara.org/mulgara/tool/FeatureExtractor#Geographic";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_HTML =
    "http://mulgara.org/mulgara/tool/HtmlExtractor#Html";

  /**
   * the class
   */
  protected final static String TMEX_CLASS_SUMMARY =
    "http://mulgara.org/mulgara/tool/SummaryExtractor#Summary";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
    Logger.getLogger(RenderTag.class.getName());

  //
  // Members
  //

  /**
   * the stylesheet to use to render the tag body
   */
  private String stylesheet = null;

  /**
   * the map of TMex class XSL stylesheet handlers
   */
  private HashMap<String,URL> handlerMap = null;

  // release()
  //
  // Public API
  //

  /**
   * Sets the stylesheet to use to render the tag body.
   *
   * @param stylesheet the stylesheet to use to render the tag body
   */
  public void setStylesheet(String stylesheet) {

    this.stylesheet = stylesheet;
  }

  // setStylesheet()

  /**
   * Returns the stylesheet to use to render the tag body.
   *
   * @return the stylesheet to use to render the tag body
   */
  public String getStylesheet() {

    return this.stylesheet;
  }

  //
  // Methods overriding BodyTagSupport
  //

  /**
   * Initialises the map of TMex class rendering handlers.
   *
   * @throws JspTagException if a TMex class handler URL is invalid
   */
  public void doInitBody() throws JspTagException {

    // create a new handler map
    this.handlerMap = new HashMap<String,URL>();

    try {

      // add the default handlers
      this.addHandler(TMEX_CLASS_DOCUMENT, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_DATE, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_GENERICFEATURE, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_PERSON, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_COMPANY, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_GEOGRAPHIC, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_HTML, new URL("http://foo.com/"));
      this.addHandler(TMEX_CLASS_SUMMARY, new URL("http://foo.com/"));
    }
     catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid URL for TMex class handler stylesheet");

      // wrap it and re-throw!
      throw new JspTagException("render: Invalid URL for TMex class handler " +
        "stylesheet");
    }

    // try-catch
  }

  // doInitBody()

  /**
   * Renders the tag body content using rendering handlers (XSL stylesheets).
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspTagException if an the content cannot be rendered
   */
  public int doAfterBody() throws JspTagException {

    try {

      // get the stylesheet to render the content
      String stylesheet = this.getStylesheet();

      if ((stylesheet == null) || stylesheet.equals("")) {

        throw new JspTagException("render: stylesheet attribute cannot be null");
      }

      // end if
      // log that we've found a stylsheet to use
      log.debug("Rendering using stylesheet - " + stylesheet);

      // get a URL to the actual location
      URL masterStylesheet =
        pageContext.getServletContext().getResource(stylesheet);

      // log that we've resolved the URL of the stylesheet
      log.debug("Resolved stylesheet URL - " + masterStylesheet);

      // get a reader to the contents of the tag body
      BodyContent content = this.getBodyContent();
      Reader contentReader = content.getReader();

      // log that we're about to render the content
      log.debug("Rendering tag body contents");

      // render the content
      try {
        this.render(masterStylesheet, contentReader, this.getPreviousOut());
      } finally {
        contentReader.close();
      }
    }
     catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid URL for master stylesheet", mue);
      throw new JspTagException("render: Could not locate XSL stylesheet");
    }
     catch (TransformerConfigurationException tce) {

      // log the error
      log.error("Unable to configure transformer", tce);
      throw new JspTagException("render: Unable to configure XSL transformer");
    }
     catch (TransformerException te) {

      // log the error
      log.error("Unable to transform content", te);
      throw new JspTagException("render: ");
    }
     catch (IOException ioe) {

      // log the error
      log.error("Unable to obtain stream from stylesheet URL", ioe);
      throw new JspTagException("render: Error reading from XSL stylesheet");
    }

    // try-catch
    // don't process the body again
    return SKIP_BODY;
  }

  // doAfterBody()

  /**
   * Resets the tag to its default state.
   */
  public void release() {

    this.stylesheet = null;
    this.handlerMap = null;
  }

  // getStylesheet()

  /**
   * Adds a handler to the map of TMex class XSL stylesheet handlers.
   *
   * @param tmexClass the TMex class to use this
   * @param stylesheet the stylesheet to use to render this class
   */
  public void addHandler(String tmexClass, URL stylesheet) {

    this.handlerMap.put(tmexClass, stylesheet);
  }

  // addHandler()
  //
  // Internal methods
  //

  /**
   * Renders the <code>input</code> using the given XSL stylesheet.
   *
   * @param masterStylesheet PARAMETER TO DO
   * @param input the content to render
   * @param output the rendered content
   * @throws TransformerConfigurationException if the transformed cannot be
   *      configured using the <code>masterStyleSheet</code>
   * @throws TransformerException if the <code>input</code> cannot be
   *      transformed
   * @throws IOException if a stream cannot be obtained to the <code>masterStyleSheet</code>
   */
  private void render(URL masterStylesheet, Reader input, Writer output)
    throws TransformerConfigurationException,
      TransformerException,
      IOException {

    // DEBUG *************
    //if (masterStylesheet.toString().endsWith("metadata.xsl")) {
    if (DEBUG) {

      PrintWriter out = new PrintWriter(new BufferedWriter(output));
      out.println("<p>SOAP content is:</p>\n<pre>");

      BufferedReader in = new BufferedReader(input);
      String line = in.readLine();

      while (line != null) {

        out.println(line);
        line = in.readLine();
      }

      // end while
      out.println("</pre>");
      out.flush();
    }
    else {

      // DEBUG *************
      // create the streams
      StreamSource contentSource = new StreamSource(input);
      StreamResult transformedContent = new StreamResult(output);

      log.debug("Transforming tag body contents using " + masterStylesheet);

      // DEBUG *************
      //long transformerTime = System.currentTimeMillis();
      // DEBUG *************
      // get a transformer to perform the transformation
      Transformer transformer =
        CachedTransformerFactory.getTransformer(masterStylesheet);

      // DEBUG *************
      /*
      long timeMillis = System.currentTimeMillis() - transformerTime;
      double timeSeconds = ((double) timeMillis) / ((double) 1000L);
      System.out.println("\n\nTransformer creation time = " + timeSeconds + "\n\n");
      long transformationTime = System.currentTimeMillis();
      */
      // DEBUG *************
      // transform the document
      transformer.transform(contentSource, transformedContent);

      // DEBUG *************

      /*
      timeMillis = System.currentTimeMillis() - transformationTime;
      timeSeconds = ((double) timeMillis) / ((double) 1000L);
      System.out.println("\n\nTransformation time = " + timeSeconds + "\n\n");
      */

      // DEBUG *************
      // DEBUG *************
    }

    // end if
    // DEBUG *************
  }

  // render()
}


// RenderTag
