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

package org.mulgara.descriptor;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.apache.log4j.*;
import org.w3c.dom.*;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.server.ServerInfo;
import org.mulgara.util.TempDir;


/**
 * A Descriptor is therefore an XSL stylesheet that performs a specific, well
 * defined task
 *
 * @created 2002-03-15
 *
 * @author Keith Ahern
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:11 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Descriptor {

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * Description of the Field
   */
  public final static String DESCRIPTOR_NS_STRING =
      "http://mulgara.org/descriptor#";

  /**
   * Description of the Field
   */
  public final static String DESCRIPTOR_TARGET = "_target";

  /**
   * Description of the Field
   */
  public final static String DESCRIPTOR_SELF = "_self";

  /**
   * Description of the Field
   */
  public final static String DESCRIPTOR_SOURCE = "_source";

  /**
   * Description of the Field
   */
  public final static String DESCRIPTOR_CONTEXT = "_context";

  /**
   * magic name/value to get to clear the descriptor cache in the factory
   */
  public final static String CLEAR_DESCRIPTOR_CACHE = "_clearCache";

  /**
   * Description of the Field
   */
  protected static TransformerFactory transformerFactory = null;

  /**
   * Description of the Field
   */
  protected static DocumentBuilderFactory documentBuilderFactory = null;

  /**
   * Description of the Field
   */
  protected static DocumentBuilder documentBuilder = null;

  /**
   * the filename to use for the stack trace output
   */
  final static String DESCRIPTOR_STACKTRACE_OUTPUT =
      "descriptor_stacktrace.log";

  /**
   * Description of the Field
   */
  private static PrintWriter w = null;

  /**
   * Description of the Field
   */
  private static final Logger log = Logger.getLogger(Descriptor.class);

  /**
   * Description of the Field
   */
  private static Document stubDoc = null;

  /**
   * Description of the Field
   */
  private static URI descriptorModelURI = null;

  /**
   * Description of the Field
   */
  private static Map<URL,List<Param>> paramMap = null;

  /**
   * map if our mime types
   */
  private static Map<URL,MimeType> mimeMap = null;

  /**
   * Description of the Field
   */
  protected URL url = null;

  /**
   * Description of the Field
   */
  protected Transformer transformer = null;

  /**
   * holds objects that can be useful from inside descriptors
   */
  private DescriptorContext context = null;

  /**
   * default model for storing descriptors.
   */
  public static final String DEFAULT_DESCRIPTOR_MODEL = "descriptors";

  /**
   * default protocol for accessing descriptors.
   */
  public static final String DEFAULT_DESCRIPTOR_PROTOCOL = "http://";

  /**
   * default relative URL off a host for accessing descriptors.
   */
  public static final String DEFAULT_DESCRIPTOR_REL_URL = "/descriptors";

  /**
   * Constructor for the Descriptor object. Descriptors should be retrieved from
   * the factory.
   *
   * @param url Description of Parameter
   * @throws DescriptorException EXCEPTION TO DO
   */
  Descriptor(URL url) throws DescriptorException {
    this(url, null);
  }

  /**
   * Constructor for the Descriptor object. Descriptors should be retrieved from
   * the factory.
   *
   * @param url Description of Parameter
   * @param bean ItqlInterpreterBean to use
   * @throws DescriptorException EXCEPTION TO DO
   */
  Descriptor(URL url, ItqlInterpreterBean bean) throws DescriptorException {

    if (url == null) {

      throw new IllegalArgumentException(
          "Tried to construct Descriptor with null URL!");
    }

    this.url = url;

    if (log.isDebugEnabled()) {

      log.debug("Descriptor initializing from Object with URL " + url);
    }

    // create our context object
    context = new DescriptorContext();

    // Use the supplied bean if its non null
    if (bean != null) {

      if (log.isDebugEnabled()) {

        log.debug("using supplied Interpreter bean");
      }

      context.setInterpreterBean(bean);
    }
    else {

      if (log.isDebugEnabled()) {

        log.debug("creating own Interpreter bean");
      }

      context.setInterpreterBean(new org.mulgara.itql.ItqlInterpreterBean());
    }

    // configure ourselves
    try {

      if (documentBuilderFactory == null) {

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
      }

      if (documentBuilder == null) {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
      }

      // get our stylesheet from the URL
      initiateTransformer(fetchStyleSheet(url));

      if (descriptorModelURI == null) {

        initiateDescriptorModel();
      }

      // get our metadata
      getSettings();
    }
    catch (IOException ioe) {

      throw new DescriptorException("Could not fetch stylesheet: " + url, ioe);
    }
    catch (TransformerConfigurationException tce) {

      throw new DescriptorException("Transformer Exception: " + url, tce);
    }
    catch (ParserConfigurationException pe) {

      throw new DescriptorException("Transformer failure: ", pe);
    }

    if (log.isDebugEnabled()) {

      log.debug("Descriptor initialized from Object with URL " + url);
    }
  }


  /**
   * Get the descriptor Graph URI
   *
   * @return the Descriptor Graph URI as a string;
   */
   public static String getModelURIString() {
    return ServerInfo.getServerURI() + "#" + DEFAULT_DESCRIPTOR_MODEL;
   }

  /**
   * Writes out the full stack trace details to file and concise summary
   * messages to the screen. If writing to the file doesn't succeed, the stack
   * trace is displayed on the screen.
   *
   * @param e the throwable exception
   * @param exceptionType A displayable string of the exception type, e.g.
   *      configuration or fatal
   */
  static void writeStackTrace(Throwable e, String exceptionType) {

    PrintWriter pw = null;
    FileWriter fw = null;
    Throwable cause = null;

    // write out the stack trace in case it's really needed
    try {

      fw = new FileWriter(DESCRIPTOR_STACKTRACE_OUTPUT);
      pw = new PrintWriter(fw);
      pw.println("Top level exception message is:");
      pw.println(e.getMessage());

      System.err.println("Top level exception message is:");
      System.err.println("-->> " + e.getMessage() + " <<--");

      pw.println(eol + "Stack trace is:");
      e.printStackTrace(pw);
      pw.println(eol + eol + "Although probably not important, the message " +
          "and stack trace is included for all causes back up the chain..." +
          eol);

      cause = e.getCause();

      while (cause != null) {

        String cname = cause.getClass().getName();

        System.err.println("which was caused by:");
        System.err.println("-->> (" + cname + ") " + cause.getMessage() +
            " <<--");

        pw.println("Message is:" + eol + cause.getMessage());
        pw.println("\nStack trace is:");
        cause.printStackTrace(pw);
        pw.println(eol);

        cause = cause.getCause();
      }

      System.err.println(
          "\nThe last message should indicate the base cause of your problem.");
    }
    catch (IOException ioe1) {

      // can't write out the exception details
      System.err.println("\nCan't write out the " + exceptionType +
          " exception details to: " +
          DESCRIPTOR_STACKTRACE_OUTPUT +
          "!\nReason: " + ioe1.getMessage());
      System.err.println("Printing " + exceptionType +
          " exception details now..." + eol);
      e.printStackTrace(System.err);
    }
    finally {

      try {

        if (pw != null) {

          pw.flush();
          pw.close();
        }

        if (fw != null) {

          fw.close();
        }

        System.err.println("\nFull " + exceptionType +
            " exception details successfully written to: " +
            DESCRIPTOR_STACKTRACE_OUTPUT + "!" + eol);
      }
      catch (IOException ioe2) {

        // some problems closing the file.
        System.err.println("\nMay not have successfully written out the " +
            exceptionType + " exception details to: " +
            DESCRIPTOR_STACKTRACE_OUTPUT + "!\nReason: " +
            ioe2.getMessage());
        System.err.println("Printing " + exceptionType +
            " exception details now..." + eol);
        e.printStackTrace(System.err);
      }
    }
  }

  /**
   * Reconstructs a full URL from a partial URL and a full source URL
   *
   * @param relativeURLString such as index.html or images/title.gif
   * @param sourceURLString such as http://www.pisoftware.com/staff
   * @return URL such as http://www.pisoftware.com/staff/index.html
   * @throws DescriptorException EXCEPTION TO DO
   */
  static URL resolveRelativeURL(String relativeURLString,
      String sourceURLString) throws
      DescriptorException {

    if (log.isDebugEnabled()) {

      log.debug("Attempting to make absolute URL from relative URL '" +
          relativeURLString + "' and source URL '" + sourceURLString +
          "'");
    }

    if ( (sourceURLString == null) || (sourceURLString.length() == 0)) {

      throw new DescriptorException(
          "Unable to create absolute URL from null/zero-length source URL (" +
          sourceURLString + ") and relative URL (" + relativeURLString + ")");
    }

    URL descURL = null;

    // lose the last bit of the source URL - after the last slash
    int fslash = sourceURLString.lastIndexOf('/');
    int bslash = sourceURLString.lastIndexOf('\\');
    int slash = (fslash > bslash) ? fslash : bslash;
    String baseURLString = sourceURLString.substring(0, slash + 1);

    // include the slash
    // create the descriptor URL
    try {

      descURL = new URL(baseURLString + relativeURLString);
    }
    catch (java.net.MalformedURLException me) {

      throw new DescriptorException(
          "Unable to create absolute URL from baseURL (" + baseURLString +
          ") and relative URL (" + relativeURLString + ")",
          me);
    }

    return descURL;
  }

  /**
   * Set the bean to use for example a local session bean or an RMI capable
   * bean.
   *
   * @param bean see comment
   */
  public void setInterpreterBean(ItqlInterpreterBean bean) {

    if (log.isDebugEnabled()) {

      log.debug("Setting new Interpreter bean");
    }

    context.setInterpreterBean(bean);
  }

  /**
   * Returns the URL asssociated with this Descriptor
   *
   * @return The URL value
   */
  public URL getURL() {

    return url;
  }

  /**
   * return mime type, defaults to HTML
   *
   * @return The MimeType value
   * @throws DescriptorException EXCEPTION TO DO
   */
  public MimeType getMimeType() throws DescriptorException {

    // we will be in here, or exception would have been thrown earlier
    return mimeMap.get(url);
  }

  /**
   * returns unmodifiable list of Parameter objects
   *
   * @return The Params value
   */
  public List<Param> getParams() {
    return getParams(this.url);
  }

  /**
   * returns unmodifiable list of Parameter objects
   *
   * @param url PARAMETER TO DO
   * @return The Params value
   */
  public List<Param> getParams(URL url) {

    if (paramMap == null) {
      synchronized (Descriptor.class) {
        paramMap = new HashMap<URL,List<Param>>();
      }
    }

    return Collections.unmodifiableList(paramMap.get(url));
  }

  /**
   * Description of the Method
   *
   * @param inParams PARAMETER TO DO
   * @return Description of the Returned Value
   * @exception DescriptorException Description of Exception
   */
  public Document processToDocument(Param[] inParams) throws
      DescriptorException {

    DOMResult result = new DOMResult();
    process(result, inParams);

    return (Document) result.getNode();
  }

  /**
   * Converts to document fragment. NOTE xalan 2.4.1+ seems to have a problem
   * returning document fragments from extensions, use a Node instead.
   *
   * @param inParams PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws DescriptorException EXCEPTION TO DO
   */
  public DocumentFragment processToDocumentFragment(Param[] inParams) throws
      DescriptorException {

    DocumentFragment df = null;
    try {
      DOMResult result = new DOMResult();
      process(result, inParams);

      // convert to document fragment
      Document doc = (Document) result.getNode();

      df = doc.createDocumentFragment();
      df.appendChild(doc.getDocumentElement());
    } catch (DescriptorException e) {
      log.error(e.getCause());
      throw e;
    }

    return df;
  }

  /**
   * Description of the Method
   *
   * @param inParams PARAMETER TO DO
   * @return Description of the Returned Value
   * @exception DescriptorException Description of Exception
   */
  public String processToString(Param[] inParams) throws DescriptorException {

    StringWriter sw = new StringWriter();
    Result result = new StreamResult(sw);
    process(result, inParams);

    return sw.toString();
  }

  /**
   * Wonder method... Processes the source XML using the Descriptor stylesheet
   * for this Descriptor.
   *
   * @param result Description of Parameter
   * @param inParams PARAMETER TO DO
   * @exception DescriptorException Description of Exception
   */
  public void process(Result result, Param[] inParams) throws
      DescriptorException {

    // always clear parameters
    transformer.clearParameters();

    Object param = null;

    // set passed parameters if any
    if (inParams != null) {

      for (int i = 0; i < inParams.length; i++) {

        param = inParams[i].getValue();

        if ( (param == null) ||
            (param instanceof String && ( ( (String) param).length() == 0))) {

          log.debug("Transformer ignoring param: " + inParams[i].getName() +
              ( (param == null) ? " null " : " empty string"));
        }
        else {

          //transformer.setParameter(inParams[i].getName(), inParams[i].getValue());
          transformer.setParameter(inParams[i].getName(), param);

          log.debug("Transformer setting param: " + inParams[i].getName() +
              " to '" + param + "'");
        }
      }
    }

    // always pass our own url as descriptor URL
    transformer.setParameter(DESCRIPTOR_TARGET, url);
    log.debug("Transformer always setting param: '" + DESCRIPTOR_TARGET + "'='" + url + "'");

    // set the bean as a parameter to propagate to other beans
    transformer.setParameter(DESCRIPTOR_CONTEXT, context);
    log.debug("Transformer always setting param: '" + DESCRIPTOR_CONTEXT + "'='" + context + "'");

    if (stubDoc == null) {
      stubDoc = documentBuilder.newDocument();
      stubDoc.appendChild(stubDoc.createElement("ROOT"));
    }

    try {

      // perform the transformation
      transformer.transform(new DOMSource(stubDoc), result);

      log.debug("XLST transformed node result: " + result  + " class: " + result.getClass().getName() );

    }
    catch (TransformerException te) {

      log.error("Transformer failure: " + te.getCause());
      throw new DescriptorException("Transformer failure: ", te);
    }
  }


  /**
   * Wonder method... Processes the source XML using the Descriptor stylesheet
   * for this Descriptor.
   *
   * @param xml Description of Parameter
   * @return Description of the Returned Value
   * @exception DescriptorException Description of Exception
   */
  public Result process(Source xml) throws DescriptorException {

    try {

      DOMResult result = new DOMResult(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
      //USE String result if debugging, then you can dump the result to a log
      //too StringWriter sw = new StringWriter();
      //Result result = new StreamResult(sw);
      //
      transformer.transform(xml, result);
      /*
      if (log.isDebugEnabled()) {
        log.debug("RESULT:" + eol + sw.toString());
      }
      */

      return result;
    } catch (javax.xml.parsers.ParserConfigurationException pce) {
      log.error("XML parser failure: " + pce.getCause());
      throw new DescriptorException("XML parser failure: ", pce);
    }
    catch (TransformerException te) {

      log.error("Transformer failure: " + te.getCause());
      throw new DescriptorException("Transformer failure: ", te);
    }
  }

  /**
   * Resets the descriptor - should be called if the factory is resetting all
   * descriptors with this URL
   *
   */
  protected void resetSettings() {

    // remove ourselves from the param map
    synchronized (paramMap) {

      if (paramMap.containsKey(url)) {

        paramMap.remove(url);
      }
    }

    // remove ourselves from the mime map
    synchronized (mimeMap) {

      if (mimeMap.containsKey(url)) {

        mimeMap.remove(url);
      }
    }

    if (log.isDebugEnabled()) {

      log.debug("Resetting Descriptor: " + url + " instance " + this);
    }
  }

  /**
   * Gets the settings such as parameters from a Mulgara server
   *
   * @throws DescriptorException EXCEPTION TO DO
   */
  private void getSettings() throws DescriptorException {

    // get parameter settings for this URL
    if (paramMap == null) {

      synchronized (Descriptor.class) {

        paramMap = new HashMap<URL,List<Param>>();
      }
    }

    // get mime settings for this URL
    if (mimeMap == null) {

      synchronized (Descriptor.class) {

        mimeMap = new HashMap<URL,MimeType>();
      }
    }

    synchronized (paramMap) {

      if (paramMap.containsKey(url) && mimeMap.containsKey(url)) {

        if (log.isDebugEnabled()) {

          log.debug("Already have settings for: " + url);
        }

        // we have settings just return
        return;
      }

      // var to be put in maps
      List<Param> params = new ArrayList<Param>();
      String mimeMajor = null;
      String mimeMinor = null;

      //String query = "select $param $paramRequired $mime-major $mime-minor from <" + descriptorModelURI + "> where " +
      //  "<" + url + "> <" + DESCRIPTOR_NS_STRING + "hasParam> $id and " +
      //  "$id <" + DESCRIPTOR_NS_STRING + "name> $param and
      //  "$id <" + DESCRIPTOR_NS_STRING + "required> $paramRequired and
      // "<" + url + "> <" + DESCRIPTOR_NS_STRING + "#hasMimetype> $mimeNode and
      // $mimeNode <" + DESCRIPTOR_NS_STRING + "#mime-major and"
      // $mimeNode <" + DESCRIPTOR_NS_STRING + "#mime-minor"
      StringBuffer b = new StringBuffer();
      b.append("select $param $paramRequired $mimeMajor $mimeMinor from <");
      b.append(descriptorModelURI);
      b.append("> where <");

      // param anon node
      b.append(url);
      b.append("> <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("hasParam> $id and ");

      // param name
      b.append("$id <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("name> $param and ");

      // param required
      b.append("$id <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("required> $paramRequired and ");

      // mime type anon node
      b.append("<");
      b.append(url);
      b.append("> <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("hasMimetype> $mimeNode and ");

      // mime type anon node
      // mime major
      b.append("$mimeNode <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("mime-major> $mimeMajor and ");

      // mime minor
      b.append("$mimeNode <");
      b.append(DESCRIPTOR_NS_STRING);
      b.append("mime-minor> $mimeMinor ");

      // close the query
      b.append(";");

      String query = b.toString();

      List<Object> answers = context.getInterpreterBean().executeQueryToList(query);
      Object obj = null;

      try {

        for (Iterator<Object> ai = answers.iterator(); ai.hasNext(); ) {

          obj = ai.next();

          Answer answer = (Answer)obj;

          // get our result
          if (!answer.isUnconstrained()) {

            //reset cursor
            answer.beforeFirst();

            while (answer.next()) {

              // do params
              //params.add(((RDFLiteralImpl)resultSet.getObject("param")).getText());
              params.add(new Param(

                  // param name
                  ( (LiteralImpl) answer.getObject("param")).getLexicalForm(),
                  // param value
                  null,
                  // param required
                  //Boolean.valueOf( ((RDFLiteralImpl)resultSet.getObject("paramRequired")).getText() ).booleanValue()
                  ( ( (LiteralImpl) answer.getObject("paramRequired")).
                  getLexicalForm()).equalsIgnoreCase(
                  "Yes")));

              if (log.isDebugEnabled()) {

                log.debug("retrieved parameter " +
                    ( (LiteralImpl) answer.getObject("param")).getLexicalForm() +
                    " for descriptor " + url);
              }

              // do mime type
              if (mimeMajor == null) {

                mimeMajor = ( ( (LiteralImpl) answer.getObject("mimeMajor")).
                    getLexicalForm());

                if (log.isDebugEnabled()) {

                  log.debug("Mime Major: " + mimeMajor);
                }
              }

              if (mimeMinor == null) {

                mimeMinor = ( ( (LiteralImpl) answer.getObject("mimeMinor")).
                    getLexicalForm());

                if (log.isDebugEnabled()) {

                  log.debug("Mime Minor: " + mimeMinor);
                }
              }
            }
          }
          if (answer != null) answer.close();

          if (log.isDebugEnabled()) {

            log.debug("Set Params for URL: " + url);
          }

          if (params.size() == 0) {

            throw new DescriptorException(
                "No params were found (not even _self) for " + url);
          }

          if (mimeMajor == null) {

            throw new DescriptorException("Mime Major not set for " + url);
          }

          if (mimeMinor == null) {

            throw new DescriptorException("Mime Minor not set for " + url);
          }

          // add to map so subsequent objects may work
          paramMap.put(url, params);
          mimeMap.put(url, new MimeType(mimeMajor, mimeMinor));
        }

        if (log.isDebugEnabled()) {

          log.debug("Retrieved PARAMS!" + params);
        }
      }
      catch (ClassCastException ce) {

        ce.printStackTrace();
        throw new DescriptorException("Mulgara Query error: " + obj,
            (obj instanceof Exception) ?
            (Exception) obj : ce);
      }
      catch (TuplesException qe) {

        throw new DescriptorException("Mulgara Query error", qe);
      }
    }
  }

  /**
   * Get our setting from the Mulgara store
   *
   * @throws DescriptorException EXCEPTION TO DO
   */
  private void initiateDescriptorModel() throws DescriptorException {

    String descriptorModelString = getModelURIString();

    try {

      descriptorModelURI = new URI(descriptorModelString);

      if (log.isDebugEnabled()) {

        log.debug("Using descriptor Graph URI is : " + descriptorModelURI);
      }
    }
    catch (URISyntaxException use) {

      throw new DescriptorException("Descriptor Graph URI: " +
          descriptorModelString +
          " is not a valid URI", use);
    }
  }

  /**
   * Fetchs the stylesheet from the URL for this Descriptor
   *
   * @param url Description of Parameter
   * @return Description of the Returned Value
   * @exception java.io.IOException Description of Exception
   */
  private Source fetchStyleSheet(URL url) throws java.io.IOException {

    InputStream stream = (InputStream) url.openStream();

    if (log.isDebugEnabled()) {

      log.debug("BYTES available: " + stream.available());
    }

    return new StreamSource(stream);
  }

  /**
   * Description of the Method
   *
   * @param xslSource Description of Parameter
   * @exception TransformerConfigurationException Description of Exception
   * @throws DescriptorException EXCEPTION TO DO
   */
  private void initiateTransformer(Source xslSource) throws
      TransformerConfigurationException, DescriptorException {

    if (transformerFactory == null) {

      transformerFactory = TransformerFactory.newInstance();

      //transformerFactory.setAttribute("http://xml.apache.org/xalan/features/optimize", java.lang.Boolean.FALSE);
    }

    //transformer = transformerFactory.newTransformer();
    transformer = transformerFactory.newTransformer(xslSource);

    //transformer.setErrorListener(new org.apache.xml.utils.ListingErrorHandler());
    if (w == null) {

      // construct the file
      File errFile = new File(TempDir.getTempDir(), "descriptor-errors.log");

      try {

        w = new PrintWriter(new FileOutputStream(errFile), true);

        w.write("Descriptor XALAN LOG " + new java.util.Date() + eol);

        transformer.setErrorListener(new PIErrorHandler(w));
      }
      catch (IOException ioe) {

        // could not create file, fall back to default handler
        //transformer.setErrorListener(new org.apache.xml.utils.ListingErrorHandler());
        // NO LONGER FATAL throw new DescriptorException("Unable to write to XALAN report file", ioe);
        log.warn("Unable to write descriptor errors to file: " + errFile +
            " using default error handler");
      }
    }
  }

  /**
   * Inner class for holding Mime Types
   */
  public class MimeType {

    String mimeMajor = null;
    String mimeMinor = null;
    String mimeType = null;

    /**
     * CONSTRUCTOR MimeType TO DO
     *
     * @param mimeMajor PARAMETER TO DO
     * @param mimeMinor PARAMETER TO DO
     */
    public MimeType(String mimeMajor, String mimeMinor) {

      if (mimeMajor == null) {

        throw new IllegalArgumentException(
            "Mime Major type is not allowed to be null!");
      }

      if (mimeMinor == null) {

        throw new IllegalArgumentException(
            "Mime Minor type is not allowed to be null!");
      }

      this.mimeMajor = mimeMajor;
      this.mimeMinor = mimeMinor;
      mimeType = mimeMajor + "/" + mimeMinor;
    }

    public String getMajorType() {

      return mimeMajor;
    }

    public String getMinorType() {

      return mimeMinor;
    }

    public String toString() {

      return mimeType;
    }
  }
}
