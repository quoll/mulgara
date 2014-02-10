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
import java.sql.*;
import java.util.*;

// Servlet packages
import javax.servlet.*;
import javax.servlet.http.*;

// Third party packages
import org.apache.log4j.*;

// Debugging writer
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.QueryException;

/**
 * Deploys descriptors.
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
public class DeployServlet extends HttpServlet {

  /** Serialization ID */
  private static final long serialVersionUID = 8322549888804327301L;

  /**
   * the logging category to log to
   */
  private final static Logger log = Logger.getLogger(DeployServlet.class.getName());

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * hostname of this machine
   */
  private static String hostname = null;

  /**
   * Array of deployed descriptors
   */
  private static URL[] descriptorsArray = {};


  /**
   * Maintain the DescriptorFactory for the application
   */
  private static DescriptorFactory factory = null;

  /**
   * Deploys descriptors
   *
   * @param req The HTTP request
   * @param res The HTTP response
   * @throws ServletException An error in running the servlet
   * @throws IOException An error reading from the request, or writing a response
   */
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("text/plain");

    // Extract the parameters
    Enumeration<String> enumeration = (Enumeration<String>)req.getParameterNames();

    // commands
    boolean deployLocalDescriptors = false;
    boolean clearLocalDescriptors = false;

    try {

      // check if we have any params, if not show usage screen
      if (enumeration.hasMoreElements()) {

        // analyse parameters and set variables
        while (enumeration.hasMoreElements()) {

          String name = (String) enumeration.nextElement();
          String[] values = req.getParameterValues(name);

          if (values != null) {
            for (int i = 0; i < values.length; i++) {
              if (log.isDebugEnabled()) {
                log.debug("name :" + name + " (" + i + ") : " + values[i]);
              }
            }
          }

          if (name.equals("deployLocalDescriptors") && values[0].equals("true")) {
            deployLocalDescriptors = true;
          } else if (name.equals("clearLocalDescriptors") &&
            values[0].equals("true")) {
            clearLocalDescriptors = true;
          }
        }

        // for our HTML result
        StringBuffer result = new StringBuffer();

        result.append("Generating Descriptor Deploy Statements... " + eol);

        // deploy local descriptors
        if (deployLocalDescriptors) {

          // build the deploy query;
          StringBuffer query = buildDeployQuery(clearLocalDescriptors);

          // write result to response
          res.getWriter().println(result);

          try {
            if (log.isDebugEnabled()) log.debug("about to execute query: " + query.toString());


            // send statements to the database
            ItqlInterpreterBean bean = null;
            StringBuffer beanResponseBuffer = null;
            // get new bean
            try {
              bean = new ItqlInterpreterBean();
              beanResponseBuffer = new StringBuffer(bean.executeQueryToString(query.toString()));
            } finally {
              // close the bean
              bean.close();
            }

            // HACK to fix brain dead IE on windows
            String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            int xmlStart = beanResponseBuffer.indexOf(XML_HEADER);
            if (xmlStart > -1) {
              int xmlEnd = xmlStart + XML_HEADER.length();
              beanResponseBuffer.delete(xmlStart, xmlEnd);
            }
            String beanResponse = beanResponseBuffer.toString();

            // Create Descriptor Factory
            if (factory == null) factory = DescriptorFactory.getInstance();

            // clear the cache
            factory.clearDescriptorCache();

            // write result to response
            res.getWriter().println("Response from server: " + beanResponse);
          } catch (QueryException qe) {
            throw new ServletException("Query error", qe);
          } catch (SQLException se) {
            throw new ServletException("SQL error", se);
          } catch (Exception ge) {
            throw new ServletException("General Exception", ge);
          }
        } else {
          // redirect to the error.jsp page with some needed params
          res.sendRedirect("error.jsp&message=NO_VALID_PARAMS");
        }
      } else {
        // No parameters
        // redirect to the error.jsp page with some needed params
        res.sendRedirect("error.jsp&message=NO_PARAMS");
      }
    } catch (DescriptorException de) {
     throw new ServletException("Descriptor Problem", de);
    }
  }


  /**
   * Initatiates the servlet
   *
   * @param config Servlet Config
   * @throws ServletException EXCEPTION TO DO
   */
  public void init(ServletConfig config) throws ServletException {

    super.init(config);
    log.info("initializing the descriptor servlet");

    // get the hostname from the descriptor Servlet
    if (hostname == null) {
      hostname = org.mulgara.server.ServerInfo.getBoundHostname();
      if (log.isDebugEnabled()) log.debug("Hostname is set from ServerInfo to " + hostname);
    }

    // Auto deploy the descriptors
    // write result to response
    /* DOESNT WORK, JETTY doesn't seem to have expanded the WAR file yet...
    try {

      // build the deploy query;
      StringBuffer query = buildDeployQuery(true);

      if (log.isDebugEnabled()) log.debug("about to execute query: " + query.toString());

      // send statements to the database
      String beanResponse = bean.executeQueryToString(query.toString());

      // Create Descriptor Factory
      if (factory == null) factory = DescriptorFactory.getInstance();

      // clear the cache
      factory.clearDescriptorCache();
    } catch (Exception e){
      throw new ServletException("Could not auto deploy descriptors", e);
    }
    */
  }


  /**
   * only accepts XSL files public static class XSLFileFilter implements
   * FileFilter { boolean accept(File pathname) { return
   * (pathname.getName().toLowerCase().endsWith("xsl")); } }
   */
  /**
   * Returns an array of Local Descriptor URLs
   *
   * @param resources PARAMETER TO DO
   * @return The LocalDescriptorURLs value
   * @throws DescriptorException EXCEPTION TO DO
   */
  private URL[] getLocalDescriptorURLs(List<String> resources) throws DescriptorException {

    Vector<URL> descriptors = new Vector<URL>();

    // Figure out our URL
    String URL2Here = Descriptor.DEFAULT_DESCRIPTOR_PROTOCOL +
        org.mulgara.server.ServerInfo.getBoundHostname() + 
        ":" + org.mulgara.server.ServerInfo.getHttpPort() + "/webservices/";

    for (String currResource: resources) {

      // get the descriptor resource from this context
      try {
        URL defaultDir = getServletContext().getResource(currResource);
        log.info("default dir is " + defaultDir);

        //if (defaultDir != null) {

          File dfile = null;
          try {
            // Now make a file out of it..
            // Use the URI constructor to assist with Windows 2000/NT
            // issues.
            dfile = new File(new URI(defaultDir.toString()));
          } catch (java.net.URISyntaxException use) {
            throw new DescriptorException("Unable to construct URI to default" +
                 " Descriptors at directory location "+ defaultDir );
          }

          // check if its a directory
          if (dfile.isDirectory()) {
            log.debug("Entering " + defaultDir);

            // go thru all files in dir, TODO descend into dirs if necessary
            File[] dfiles = dfile.listFiles();

            for (int i = 0; i < dfiles.length; i++) {
              log.debug("Testing " + dfiles[i].getName());
              // test if this file is a file
              if (dfiles[i].isFile()) {
                //String urlString = URL2Here + "descriptors/default/" + dfiles[i].getName();
                String urlString =
                    URL2Here + currResource.substring(1, currResource.length()) +
                    dfiles[i].getName();

                log.debug("Attempting to make URL from " + urlString);

                URL url = new URL(urlString);

                log.debug("Testing URL " + url);

                // add this file to list
                descriptors.add(url);
              }
            }
          } else {
            throw new DescriptorException("Path to default descriptors (" + defaultDir + ") is NOT a directory!");
          }
        //} // if defaultDir 
      } catch (java.net.MalformedURLException mue) {
        throw new DescriptorException("Unable to construct URL to default Descriptors!");
      }
    }

    // return as an array
    return descriptors.toArray(descriptorsArray);
  }


  /**
   * Figure out URL up to this servlet
   *
   * @param clearLocalDescriptors flag to clear local descriptors to start of query.
   * @return The URL2Here value
   */
  private StringBuffer buildDeployQuery(boolean clearLocalDescriptors) throws DescriptorException {

    // create query string...
    StringBuffer query = new StringBuffer();

    // get model
    String modelURIString = Descriptor.getModelURIString();

    // log it
    if (log.isDebugEnabled()) {
      log.debug("Descriptor model URI is " + modelURIString);
    }

    // end if
    // get server name
    String serverURIString = modelURIString.substring(0, modelURIString.indexOf('#'));

    // log it
    if (log.isDebugEnabled()) log.debug("Server URI is " + serverURIString);

    // end if

    // present security credentials
    query.append("su <" + serverURIString + "> " +
        DescriptorServlet.DESCRIPTOR_USERNAME + " " +
        DescriptorServlet.DESCRIPTOR_PASSWORD + " ;" + eol);

    // start the transaction
    query.append("set autocommit on;" + eol);

    // when clearing, create,drop,create to avoid exceptions
    if (clearLocalDescriptors) {

      query.append("# create,drop,create avoids Graph not found exceptions" + eol);
      query.append("create <" + modelURIString + ">;" + eol);
      query.append("drop <" + modelURIString + ">;" + eol);
      query.append("create <" + modelURIString + ">;" + eol);
    }

    // create load statements
    List<String> resources = new Vector<String>();
    resources.add("/descriptor/descriptors/default/");
    resources.add("/descriptor/descriptors/vcard/");
    resources.add("/descriptor/descriptors/test/");

    URL[] descriptors = getLocalDescriptorURLs(resources);
    query.append("# Deploy Internal Descriptors " + eol);

    for (int i = 0; i < descriptors.length; i++) {
      log.debug("Descriptor URL: " + descriptors[i].toString());
      query.append("load <");
      query.append(descriptors[i].toString());
      query.append("> into <");
      query.append(modelURIString);
      query.append(">; " + eol);
    }

    // end transaction
    query.append("set autocommit on;" + eol);
    /*
    // send to response
    result.append(eol + query + eol);
    result.append("\n Sending Statements to Mulgara Database..." + eol);
    */

    return query;
  }
}
