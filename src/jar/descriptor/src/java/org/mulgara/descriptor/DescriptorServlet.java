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


// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

// Servlet packages
import javax.servlet.*;
import javax.servlet.http.*;

// Third party packages
import org.apache.log4j.*;

/**
 *
 * @axis.service name="DescriptorService"
 *
 * @created 2002-03-15
 *
 * @author Keith Ahern
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/06/14 09:39:28 $
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
public class DescriptorServlet extends HttpServlet {

  /** Serialization ID */
  private static final long serialVersionUID = -8766212391201433314L;

  /**
   * descriptor username for Mulgara access
   */
  final static String DESCRIPTOR_USERNAME = "descriptor";

  /**
   * descriptor password for Mulgara access
   */
  final static String DESCRIPTOR_PASSWORD = "ZZpj4i6p7r";

  /**
   * logging class
   */
  private final static Logger log =
      Logger.getLogger(DescriptorServlet.class.getName());

  /**
   * Maintain the Descriptor for the application
   */
  private static DescriptorFactory factory = null;

  /**
   * magic name/value to override Mime type
   */
  private final static String MIME_TYPE = "_mimeType";

  /**
   * unknown mimeType
   */
  private final static String UNKNOWN_MIME_TYPE = "unknown/unknown";


  /**
   * Invokes a descriptor return the result as a string.
   *
   * @param params 
   * @return see description.
   * @throws Exception thrown when the descriptor has an internal exception.
  public String invokeToString(Map params) throws Exception {

    // if this gets set to thru reset the factory descriptors
    boolean clearDescriptorCache = false;

    String result;
    URL descURL = null;
    Param[] parameters = new Param[params.size()];
    String sourceURLString = null;

    // may be set if the url is relative,
    // the source should have the full URL
    // of the calling descriptor e.g.
    // in a HTML
    String url = null;
    int i = 0;

    for (Iterator pi = params.keySet().iterator(); pi.hasNext(); ) {

      String key = (String) pi.next();
      String value = (String) params.get(key);

      parameters[i++] = new Param(key, value);

      //Check for the descriptor url
      if (key.equalsIgnoreCase(Descriptor.CLEAR_DESCRIPTOR_CACHE)) {

        clearDescriptorCache = true;
      }
      else if (key.equalsIgnoreCase(Descriptor.DESCRIPTOR_SELF)) {

        url = value;
      }
      else if (key.equalsIgnoreCase(Descriptor.DESCRIPTOR_SOURCE)) {

        // we've got a _source param - above url must be relative
        sourceURLString = value;
      }
    }

    if (url == null) {

      throw new Exception(Descriptor.DESCRIPTOR_SELF +
                          " not set as a name value pair");
    }

    // Create Descriptor Factory
    if (factory == null) {

      factory = DescriptorFactory.getInstance();
    }

    try {

      // received a clear descriptor cache command
      if (clearDescriptorCache) {

        factory.clearDescriptorCache();
      }

      try {

        descURL = new URL(url);

        // we have a source attribute
      }
      catch (MalformedURLException mue) {

        descURL = Descriptor.resolveRelativeURL(url, sourceURLString);
      }

      // get a Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // activate the descriptor...
      result = des.processToString(parameters);

      // return the descriptor
      factory.releaseDescriptor(des);
    }
    catch (DescriptorException de) {

      throw new Exception("Descriptor Problem", de);
    }

    // string
    return result;
  }
   */


  /**
   * Invokes a descriptor returning an Element.
   *
   * @param eparams an Element containing parameters for the descriptor.
   *      <params>
   *
   *      <_url>http://....</_url> <model>rmi://</model>
   * @return Element the result of the descriptor
   * @throws Exception thrown when the descriptor has an internal exception.
  public Element invokeDescriptor(Element eparams) throws Exception {

    if (log.isDebugEnabled()) {

      log.debug("SOAP SUBMITTED PARAMS:" + DOM2Writer.nodeToString(eparams));
    }

    // Document to store result in.
    Document result;
    URL descURL = null;
    String sourceURLString = null;

    // may be set if the url is relative,
    // the source should have the full URL
    // of the calling descriptor e.g.
    // in a HTML
    // Break down parameters to name value pairs in an array
    NodeList cl = eparams.getChildNodes();

    // array of the parameters
    Param[] parameters = new Param[cl.getLength()];

    // url of the descriptor to use
    String url = null;

    // if this gets set to thru reset the factory descriptors
    boolean clearDescriptorCache = false;

    // loop thru children
    for (int i = 0; i < cl.getLength(); i++) {

      Element param = (Element) cl.item(i);

      // name value pairs
      String name = param.getTagName();
      String value = (String) param.getFirstChild().getNodeValue();

      if (log.isDebugEnabled()) {

        log.debug("Descriptor PARAM: name " + name + " value " + value);
      }

      parameters[i] = new Param(name, value);

      //Check for the descriptor url
      if (name.equalsIgnoreCase(Descriptor.CLEAR_DESCRIPTOR_CACHE)) {

        clearDescriptorCache = true;
      }
      else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SELF)) {

        url = value;
      }
      else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SOURCE)) {

        // we've got a _source param - above url must be relative
        sourceURLString = value;
      }
    }

    if (url == null) {

      throw new Exception(Descriptor.DESCRIPTOR_SELF +
                          " not set as a name value pair");
    }

    // Create Descriptor Factory
    if (factory == null) {

      factory = DescriptorFactory.getInstance();
    }

    try {

      // received a clear descriptor cache command
      if (clearDescriptorCache) {

        factory.clearDescriptorCache();
      }

      try {

        descURL = new URL(url);

        // we have a source attribute
      }
      catch (MalformedURLException mue) {

        descURL = Descriptor.resolveRelativeURL(url, sourceURLString);
      }

      // get a Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // activate the descriptor...
      result = des.processToDocument(parameters);

      // return the descriptor
      factory.releaseDescriptor(des);
    }
    catch (DescriptorException de) {

      throw new Exception("Descriptor Problem", de);
    }

    return result.getDocumentElement();
  }
   */

  /**
   * Performs the descriptor execute command
   *
   * @param req PARAMETER TO DO
   * @param res PARAMETER TO DO
   * @throws ServletException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   */
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws
      ServletException, IOException {

    String url = null;
    String sourceURLString = null;

    // may be set if the url is relative,
    // the source should have the full URL
    // of the calling descriptor e.g.
    // in a HTML
    URL descURL = null;
    String mimeType = null;

    // if this gets set to thru reset the factory descriptors
    @SuppressWarnings("unused")
    boolean clearDescriptorCache = false;

    // get descriptor URL first.
    url = req.getParameter(Descriptor.DESCRIPTOR_SELF);

    // sanity check url
    if (url == null) {
      throw new ServletException("Insifficient parameters supplied - no URL " +
                                 Descriptor.DESCRIPTOR_SELF + " parameter");
    }

    try {
      // see if we have a source URL string
      sourceURLString = req.getParameter(Descriptor.DESCRIPTOR_SOURCE);

      // try and make a URL first
      try {
        if (log.isDebugEnabled()) {
          log.debug("Attempting to construct descriptor URL from " + url);
        }
        descURL = new URL(url);
      } catch (MalformedURLException mue) {
        // if we have a source attribute attempt to make a full URL
        if (sourceURLString != null) {
          if (log.isDebugEnabled()) {
            log.debug(
                "Attempting to absolute construct descriptor URL from relative URL '" +
                url + "' and source URL '" + sourceURLString + "'");
          }
          descURL = Descriptor.resolveRelativeURL(url, sourceURLString);
        } else {
          throw new DescriptorException("Partial URL: '" + url +
                                        "' can not be used to find " +
              " descriptor, use full URL or supply full URL of descriptor located relative " +
              " to this descriptor as a " + Descriptor.DESCRIPTOR_SOURCE +
              " parameter.");
        }
      }

      // Get a Descriptor Factory instance
      if (factory == null) {
        factory = DescriptorFactory.getInstance();
      }

      // check if we need to clear the cache
      if (req.getParameter(Descriptor.CLEAR_DESCRIPTOR_CACHE) != null) {
        factory.clearDescriptorCache();
      }

      // get a Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // get the params for the descriptor,
      List<Param> params = new Vector<Param>();

      // fill paramsList with modifiable copy of descriptor list
      // Collections.copy(paramsList, des.getParams(descURL));
      // Extract the remaining parameters
      Enumeration<String> enumeration = (Enumeration<String>)req.getParameterNames();

      // check if we have any params, if not show usage screen
      if (enumeration.hasMoreElements()) {

        //Output the parameters to the debug logger
        while (enumeration.hasMoreElements()) {

          String name = enumeration.nextElement();
          String[] values = req.getParameterValues(name);

          // if param is required then value can NOT be null or empty string
          // otherwise remove name value pair
          if (values != null) {

            int i = 0;
            for (String value: values) {

              if (log.isDebugEnabled()) {
                log.debug("HTTP param name :" + name + " (" + i++ + ") : '" + value + "'");
              }

              //Check for the descriptor url
              if (name.equalsIgnoreCase(Descriptor.CLEAR_DESCRIPTOR_CACHE)) {
                clearDescriptorCache = true;
              } else if (name.equalsIgnoreCase(MIME_TYPE)) {
                mimeType = value;
              } else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SELF)) {
                // IGNORE we have it already.
              } else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SOURCE)) {
                // IGNORE we already have it.
              } else if ( (value != null) && (value.length() > 0)) {
                params.add(new Param(name, value));
              } else if (log.isDebugEnabled()) {

                // ignoring empty value
                log.debug("ignoring parameter " + name + " value is " +
                          ( (value == null) ? "null" : "zero length"));
              }
            }
          }
        }

        // now that we have the proper URL for the descriptor add it as _SELF
        params.add(new Param(Descriptor.DESCRIPTOR_SELF, descURL.toString()));

        //Grab the parameters
        Param[] parameters = (Param[]) params.toArray(new Param[params.size()]);

        // set mime type if known and not over ridden
        if (mimeType == null) {
          // not set as HTTP param - use configured mimetype
          mimeType = des.getMimeType().toString();
        }

        // set it unless unknown type
        if (!mimeType.equals(UNKNOWN_MIME_TYPE)) {
          res.setContentType(mimeType);
        }

        // activate the descriptor...
        String result = des.processToString(parameters);

        // write result to response
        res.getWriter().println(result);

        // return the descriptor
        factory.releaseDescriptor(des);
      } else {

        // No parameters
        log.debug("NO parameters");

        // redirect to the index.jsp page with some needed params
        res.sendRedirect("index.jsp");
      }
    } catch (MalformedURLException mue) {
      throw new ServletException("Bad URL for Descriptor", mue);
    } catch (DescriptorException de) {
      throw new ServletException("Descriptor Problem", de);
    }
  }

  /**
   * Initatiates the servlet
   *
   * @param config Servlet Config
   * @throws ServletException Error initializing the servlet framework
   */
  public void init(ServletConfig config) throws ServletException {

    super.init(config);
    log.info("initializing the descriptor servlet");

    // get a descriptor factory
    factory = DescriptorFactory.getInstance();
  }
}
