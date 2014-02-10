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
import java.net.*;
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Debugging writer
import org.apache.axis.utils.DOM2Writer;

// DOM
import org.w3c.dom.*;

/**
 * Facade to provide old apache SOAP access through AXIS.
 *
 * @created 2004-04-06
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
public class DescriptorUtil {

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
      Logger.getLogger(DescriptorUtil.class.getName());

  /**
   * Maintain the Descriptor for the application
   */
  private static DescriptorFactory factory = null;


  /**
   * Initatiates the servlet
   *
   * @param config Servlet Config
   * @throws ServletException EXCEPTION TO DO
   */
  public DescriptorUtil(){

    log.info("initializing the descriptor Util class");

    // get a descriptor factory
    factory = DescriptorFactory.getInstance();
  }

  /**
   * Invokes a descriptor return the result as a string.
   *
   * @param params HashMap is used instead of Map because of an Axis bug
   * @return see description.
   * @throws Exception thrown when the descriptor has an internal exception.
   */
  public String invokeToString(HashMap<String,String> params) throws Exception {

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

    for (String key: params.keySet()) {

      String value = (String) params.get(key);

      parameters[i++] = new Param(key, value);

      //Check for the descriptor url
      if (key.equalsIgnoreCase(Descriptor.CLEAR_DESCRIPTOR_CACHE)) {
        clearDescriptorCache = true;
      } else if (key.equalsIgnoreCase(Descriptor.DESCRIPTOR_SELF)) {
        url = value;
      } else if (key.equalsIgnoreCase(Descriptor.DESCRIPTOR_SOURCE)) {
        // we've got a _source param - above url must be relative
        sourceURLString = value;
      }
    }

    if (url == null) {
      throw new Exception(Descriptor.DESCRIPTOR_SELF + " not set as a name value pair");
    }

    // Create Descriptor Factory
    if (factory == null) factory = DescriptorFactory.getInstance();

    try {

      // received a clear descriptor cache command
      if (clearDescriptorCache) factory.clearDescriptorCache();

      try {
        descURL = new URL(url);
        // we have a source attribute
      } catch (MalformedURLException mue) {
        descURL = Descriptor.resolveRelativeURL(url, sourceURLString);
      }

      // get a Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // activate the descriptor...
      result = des.processToString(parameters);

      // return the descriptor
      factory.releaseDescriptor(des);
    } catch (DescriptorException de) {
      throw new Exception("Descriptor Problem", de);
    }

    // string
    return result;
  }


  /**
   * Invokes a descriptor returning an Element.
   *
   * @param eparams an Element containing parameters for the descriptor.
   *      <params>
   *
   *      <_url>http://....</_url> <model>rmi://</model>
   * @return Element the result of the descriptor
   * @throws Exception thrown when the descriptor has an internal exception.
   */
  public Element invokeDescriptor(Element eparams) throws Exception {

    if (log.isDebugEnabled()) {
      log.debug("SOAP SUBMITTED PARAMS:" + DOM2Writer.nodeToString((Node)eparams, true));
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
      } else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SELF)) {
        url = value;
      } else if (name.equalsIgnoreCase(Descriptor.DESCRIPTOR_SOURCE)) {
        // we've got a _source param - above url must be relative
        sourceURLString = value;
      }
    }

    if (url == null) {
      throw new Exception(Descriptor.DESCRIPTOR_SELF + " not set as a name value pair");
    }

    // Create Descriptor Factory
    if (factory == null) factory = DescriptorFactory.getInstance();

    try {
      // received a clear descriptor cache command
      if (clearDescriptorCache) {
        factory.clearDescriptorCache();
      }

      try {
        descURL = new URL(url);
        // we have a source attribute
      } catch (MalformedURLException mue) {
        descURL = Descriptor.resolveRelativeURL(url, sourceURLString);
      }

      // get a Descriptor from the Factory
      Descriptor des = factory.getDescriptor(descURL);

      // activate the descriptor...
      result = des.processToDocument(parameters);

      // return the descriptor
      factory.releaseDescriptor(des);
    } catch (DescriptorException de) {
      log.warn("Error invoking descriptor", de);
      throw new Exception("Descriptor Problem: " + de.getCause(), de);
    }

    return result.getDocumentElement();
  }
}
