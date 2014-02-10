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

package org.mulgara.util;

// Java 2 enterprise packages
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.mulgara.util.TagSoapClient;

/**
 * This class queries the "#" model in a Mulgara server for permissions.
 *
 * @created 2002-01-14
 *
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/03/12 02:54:04 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2002-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TagSoapClient {

  //
  // Constants
  //
  //XML Header found at the top of each SOAP response

  /**
   * Description of the Field
   */
  protected final static String XMLHEADER =
    "<?xml version='1.0' encoding='UTF-8'?>";

  /**
   * the category to log to
   */
  private final static Logger log =
    Logger.getLogger(TagSoapClient.class.getName());

  /**
   * Extract content from Response
   *
   * @param resp PARAMETER TO DO
   * @return Content of the response returned from the call
   * @throws SOAPException if the query cannot be sent, or the response cannot
   *      be received
   */
  public static String getContent(Response resp) throws SOAPException {

    try {

      // write the output to the output stream
      if (resp.getSOAPContext().getCount() == 1) {

        String result =
          resp.getSOAPContext().getRootPart().getContent().toString();

        // Remove the XML header
        int pos = result.indexOf(XMLHEADER);

        if (pos >= 0) {

          result = result.substring(XMLHEADER.length() + 1);
        }

        if (log.isDebugEnabled()) {

          log.debug("Response posted to output stream :" + result);
        }

        return result;
      }
      else {

        log.error("Multiple SOAP body parts received. Invalid response");
        throw new SOAPException(org.apache.soap.Constants.FAULT_CODE_CLIENT,
          "Multiple SOAP body parts received. Invalid response");
      }
    }
     catch (javax.mail.MessagingException ex) {

      log.error("Invalid SOAP response", ex);

      throw new SOAPException(org.apache.soap.Constants.FAULT_CODE_CLIENT,
        "Invalid SOAP response", ex);
    }
     catch (java.io.IOException ex) {

      log.error("Invalid SOAP response", ex);

      throw new SOAPException(org.apache.soap.Constants.FAULT_CODE_CLIENT,
        "Invalid SOAP response", ex);
    }
  }

  /**
   * Invokes a SOAP call to a server.
   *
   * @param url URL to te SOAP end point
   * @param query the query to send
   * @return the answer to the query
   * @throws SOAPException if the query cannot be sent, or the response cannot
   *      be received
   */
  public static Response invoke(URL url, String query)
    throws SOAPException {

    log.debug("Invoke SOAP Client call");

    //Create the call
    Call call = new Call();
    call.setTargetObjectURI("urn:Query");

    call.setMethodName("execute");
    call.setEncodingStyleURI(org.apache.soap.Constants.NS_URI_LITERAL_XML);

    Vector<Parameter> params = new Vector<Parameter>();
    params.addElement(new Parameter("queryString", String.class, query,
			org.apache.soap.Constants.NS_URI_SOAP_ENC));
    call.setParams(params);

    // make the call: note that the action URI is empty because the
    // XML-SOAP rpc router does not need this. This may change in the
    // future.
    Response resp = call.invoke(  /* router URL */
        url,  /* actionURI */
        "");

    // log that we've recieved the message
    log.debug("Received SOAP response to query");

    // return the answer
    return resp;
  }
}
