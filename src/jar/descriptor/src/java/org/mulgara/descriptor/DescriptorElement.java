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

// Java 2
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

// Log4j
import org.apache.log4j.*;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.util.StringUtil;

// W3C DOM
import org.w3c.dom.*;

/**
 * Represents an Descriptor as an Element in an XSL stylesheet
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
public class DescriptorElement {

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * doc factory
   */
  protected static DocumentBuilderFactory documentBuilderFactory =
      DocumentBuilderFactory.newInstance();

  /**
   * descriptor factory
   */
  protected static DescriptorFactory factory = DescriptorFactory.getInstance();

  /**
   * log4j
   */
  private static final Logger log = Logger.getLogger(DescriptorElement.class);

  /**
   * used in Vector.toArray(p)
   *
   */
  private static Param[] p = new Param[] {};

  /**
   * delimiter to look for in query strings
   *
   */
  private final static String DELIM = "@@";

  /**
   * calls another descriptor... TODO get context
   *
   * @param context PARAMETER TO DO
   * @param extElem PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws DescriptorException EXCEPTION TO DO
   */
  public static Node descriptor(
      org.apache.xalan.extensions.XSLProcessorContext context,
      org.apache.xalan.templates.ElemExtensionCall extElem) throws
      DescriptorException {

    if (log.isDebugEnabled()) {

      log.debug("Descriptor Element XSL context: " + context);
    }

    // the URL of the descriptor as a String
    String descURLString = null;

    // Now call the descriptor
    try {

      // _target must always be there....
      descURLString =
          extElem.getAttribute(Descriptor.DESCRIPTOR_TARGET,
                               context.getContextNode(), context.getTransformer());

      // or throw an error
      if (descURLString == null) {

        String errorString = Descriptor.DESCRIPTOR_TARGET +
                                      " was not specified as an " +
            " attribute - can not invoke Descriptor - good bye";

        log.error(errorString);
        throw new DescriptorException(errorString);
      }

      // try and make a URL from this - if not its relative, try and make it
      // from the source URL
      URL descURL;

      // URL of descriptor to invoke
      String sourceURLString = null;

      try {

        // create the descriptor URL
        descURL = new URL(descURLString);
      } catch (MalformedURLException use) {

        log.debug("Unable to use URL from '" + descURLString + 
            "' as full URL attempting to make full URL");

        // if the descURLString is a  partial URL such as allCompanies.xsl then
        // we must construct it as a proper URL from the _source URL which
        // will be fully qualified e.g. http://pike.pisoftware.com:8080/descriptors/default/desc.xsl
        // allCompanies.xsl -> scheme://schemespecificpart/path/allCompanies.xsl
        sourceURLString =
            extElem.getAttribute(Descriptor.DESCRIPTOR_SOURCE,
                                 context.getContextNode(),
                                 context.getTransformer());

        // or throw an error
        if ( (sourceURLString == null) || (sourceURLString.length() == 0)) {

          String errorString = Descriptor.DESCRIPTOR_SOURCE +
                                        " was not specified as an attribute - " +
                                        ( (sourceURLString == null) ?
                                         " null string " : " empty string ") +
                                        "can not invoke Descriptor - good bye";

          log.error(errorString);
          throw new DescriptorException(errorString);

        }

        if (log.isDebugEnabled()) {

          log.debug("Unable to make complete URL from (" + descURLString +
                    ") attempting " +
                    " to make complete URL using source URL (" +
                    sourceURLString + ")");
        }

        descURL = Descriptor.resolveRelativeURL(descURLString, sourceURLString);
      }

      // get the Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // get the interpreter bean to use
      ItqlInterpreterBean bean = null;

      // see if a bean as passed as a parameter, if so use it - otherwise create one.
      DescriptorContext descContext =
          (DescriptorContext) context.getTransformer().getParameter(Descriptor.
          DESCRIPTOR_CONTEXT);

      if (descContext == null) {

        if (log.isDebugEnabled()) {

          log.debug("Element creating own bean");
        }

        bean = new ItqlInterpreterBean();
      } else {

        if (log.isDebugEnabled()) {

          log.debug("Element using bean passed as parameter: " + bean);
        }

        bean = descContext.getInterpreterBean();
      }

      // set the passed in bean on the descriptor
      des.setInterpreterBean(bean);

      // get descriptor list of parameters to search for
      List<Param> paramsList = des.getParams(descURL);

      // get original params for this
      // List origParams = des.getParams(descURL);
      // get the params for the descriptor,
      // List paramsList = new Vector(origParams);
      // fill paramsList with modifiable copy of descriptor list
      // Collections.copy(paramsList, origParams);
      // sanity check list of params - should always have one - the implicit
      // _self parameter.  If we didn't get one then the metadata for the Descriptor
      // is probably not loaded
      if ( (paramsList == null) || (paramsList.size() == 0)) {

        String errorString = "Could not find parameters for (" +
                                      descURL +
            ") RDF is probably not loaded into the Descriptor Graph";
        log.error(errorString);
        throw new DescriptorException(errorString);
      }

      // populate params
      String paramName = null;
      Object paramValue = null;
      Vector<Param> paramVec = new Vector<Param>();

      // iterate thru the parameters this desciptor has
      for (Param param: paramsList) {

        paramName = param.getName();

        // we have to map some internal names to their proper values
        // map _self to _target to get the value
        if (paramName.equalsIgnoreCase(Descriptor.DESCRIPTOR_TARGET)) {

          paramValue = descURL.toString();

          log.debug("adding special param " + paramValue + " as " +
                    Descriptor.DESCRIPTOR_TARGET);
        } else {

          paramValue =
              extElem.getAttribute(paramName, context.getContextNode(),
                                   context.getTransformer());

          log.debug("adding param " + paramName + " '" + paramName + "'");
        }

        paramVec.add(new Param(paramName, paramValue));

        //param.setValue(paramValue);
      }

      // add target as _self
      paramVec.add(new Param(Descriptor.DESCRIPTOR_SELF, descURL.toString()));

      Param[] params = (Param[]) paramVec.toArray(p);

      // parameters
      if (log.isDebugEnabled()) {

        log.debug("Calling descriptor : " + descURL + " with params " +
                  Param.toString(params));
      }

      // get the data as a document fragment
      //DocumentFragment df = des.processToDocumentFragment(params);
      Document doc = des.processToDocument(params);

      // return descriptor to factory
      factory.releaseDescriptor(des);

      return doc;
    } catch (TransformerException te) {

      String errorString="Transformer problems (" + descURLString + ")";
      log.error(errorString + " cause: " + te.getCause() );
      throw new DescriptorException(errorString, te);
    } catch (Exception e) {

      String errorString="General Exception (" + descURLString + ")";
      log.error(errorString + " cause: " + e.getCause());
      Descriptor.writeStackTrace(e, errorString);
      throw new DescriptorException(errorString, e);
    }
  }


  /**
   * Executes a query on a Mulgara store and returns the result as a Node
   *
   * @param context Description of Parameter
   * @param extElem Description of Parameter
   * @return RETURNED VALUE TO DO
   * @throws DescriptorException EXCEPTION TO DO
   */
  public static Node query(
      org.apache.xalan.extensions.XSLProcessorContext context,
      org.apache.xalan.templates.ElemExtensionCall extElem) throws
      DescriptorException {

    // LOG
    if (log.isDebugEnabled()) {

      log.debug("JAVA ELEMENT QUERY!");
    }

    // bean to use
    ItqlInterpreterBean bean = null;

    // see if a bean as passed as a parameter, if so use it - otherwise create one.
    DescriptorContext descContext =
        (DescriptorContext) context.getTransformer().getParameter(Descriptor.
        DESCRIPTOR_CONTEXT);

    if (descContext == null) {

      if (log.isDebugEnabled()) {

        log.debug("Element creating own bean");
      }

      bean = new ItqlInterpreterBean();
    } else {

      if (log.isDebugEnabled()) {

        log.debug("Element using bean passed as parameter: " + bean);
      }

      bean = descContext.getInterpreterBean();
    }

    // grab the query string
    String queryString = extElem.getFirstChild().getNodeValue();

    // execute using bean
    try {

      // populate params
      String paramName = null;
      Object paramValue = null;
      Vector<Param> paramVec = new Vector<Param>();

      // replace @@XXX@@
      int start;
      int end;
      int queryStringLen = queryString.length();
      start = queryString.indexOf(DELIM);

      while (start > 0) {

        if (start != queryStringLen) {

          end = queryString.indexOf(DELIM, start + 1);

          // extract var name
          if (end > 0) {

            paramName = queryString.substring(start + 2, end);

            if (log.isDebugEnabled()) {

              log.debug("Replacing " + paramName);
            }

            paramValue =
                extElem.getAttribute(paramName, context.getContextNode(),
                                     context.getTransformer());

            // no value - then the XSL:Param was not set
            if ( (paramValue == null) ||
                (paramValue instanceof String &&
                 ( (String) paramValue).equals(""))) {

              if (log.isDebugEnabled()) {

                log.debug("query attribute:  '" + paramName +
                    "' is not set for this Descriptor query - please set it");
              }

              throw new DescriptorException("query attribute: '" + paramName +
                  "' is not set for this Descriptor query - please set it");
            }

            // add param to the vector
            paramVec.add(new Param(paramName, paramValue));

            //queryString = queryString.replaceAll(DELIM + paramName + DELIM,
            //                                    paramValue.toString());
            queryString =
                StringUtil.replaceStringWithString(queryString,
                DELIM + paramName + DELIM, paramValue.toString());
          } else {

            throw new DescriptorException("Could not find closing " + DELIM + " good bye");
          }
        }

        // get next one
        start = queryString.indexOf(DELIM);
      }

      // lose the carraige returns
      queryString = queryString.replaceAll(eol, "");

      // show the query string
      if (log.isDebugEnabled()) {

        log.debug("executing query using bean: " + queryString);
      }

      Element result = bean.execute(queryString);

      return result;
    } catch (TransformerException te) {

      throw new DescriptorException("Transformer probs - query string: " +
                                    queryString, te);
    } catch (java.lang.Exception e) {

      throw new DescriptorException("Mulgara probs - query String:" +
                                    queryString, e);
    }
  }

  /**
   * METHOD TO DO
   *
   * @param context PARAMETER TO DO
   * @param extElem PARAMETER TO DO
   * @throws DescriptorException EXCEPTION TO DO
   */
  public static void copyURL2File(
      org.apache.xalan.extensions.XSLProcessorContext context,
      org.apache.xalan.templates.ElemExtensionCall extElem) throws
      DescriptorException {

    String src = null;
    String dest = null;

    try {

      // src file
      src =
          extElem.getAttribute("src", context.getContextNode(),
                               context.getTransformer());

      // dest file
      dest =
          extElem.getAttribute("dest", context.getContextNode(),
                               context.getTransformer());

      URL url = new URL(src);
      File srcFile = new File(url.getFile());
      File destFile = new File(dest);

      copy(srcFile, destFile);
    } catch (javax.xml.transform.TransformerException te) {

      throw new DescriptorException("Unable to find attributes !", te);
    } catch (MalformedURLException mue) {

      throw new DescriptorException("Bad File URL for source", mue);
    } catch (IOException ie) {

      throw new DescriptorException("Unable to copy file:" + src + "  to  " +
                                    dest, ie);
    }
  }

  /**
   * Copies a source file to a dest file creating directories if necessary
   *
   * @param src PARAMETER TO DO
   * @param dst PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   */
  public static void copy(File src, File dst) throws IOException {
    File parentDir = dst.getParentFile();
    if (parentDir != null) parentDir.mkdirs();

    InputStream is = new FileInputStream(src);
    OutputStream os = null;

    try {
      os = new FileOutputStream(dst);
      int n;
      try {
        byte[] buf = new byte[4096];
        while ( (n = is.read(buf)) != -1) os.write(buf, 0, n);
      } finally {
        os.close();
      }
    } finally {
      is.close();
    }
  }

  /**
   * Writes a Debug message to the output stream
   *
   * @param context PARAMETER TO DO
   * @param extElem PARAMETER TO DO
   * @throws DescriptorException EXCEPTION TO DO
   */
  public static void debug(
      org.apache.xalan.extensions.XSLProcessorContext context,
      org.apache.xalan.templates.ElemExtensionCall extElem) throws
      DescriptorException {

    // grab the query string
    String queryString = extElem.getFirstChild().getNodeValue();
    queryString += extElem.getAttribute("msg");

    if (log.isDebugEnabled()) log.debug("DEBUG:" + queryString);
  }
}
