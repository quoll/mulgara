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

// standard java packages
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Validates attributes and creates scripting variables for the {@link
 * AlldocsTag} tag.
 *
 * @created 2001-10-15
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
public class AlldocsTEI extends TagExtraInfo {

  /**
   * Returns information about the scripting variable containing the all
   * documents message.
   *
   * @param data run-time attributes of the alldocs tag
   * @return an array containing a single element, detailing the scripting
   *      variable
   */
  public VariableInfo[] getVariableInfo(TagData data) {

    // declare the array we'll be returning
    VariableInfo[] info = null;

    // get the name of the scripting variable
    Object varName = data.getAttribute(AlldocsTag.ATTR_ID);

    // proceed only if an id was defined!
    if ((varName != null) && (varName != TagData.REQUEST_TIME_VALUE)) {

      // decalare the type of the scripting variable
      String varType = "org.apache.soap.rpc.Response";

      // create the array containing the variable information
      VariableInfo idVarInfo =
        new VariableInfo((String) varName, varType, true, VariableInfo.AT_BEGIN);
      info = new VariableInfo[] { idVarInfo };
    }

    // end if
    // return the information on the variables
    return info;
  }

  // getVariableInfo()

  /**
   * Validates the run-time attribute values of the alldocs tag.
   *
   * @param data run-time attributes of the alldocs tag
   * @return The Valid value
   */
  public boolean isValid(TagData data) {

    // assume the tag is valid
    boolean valid = true;

    // validate the model attribute
    Object model = data.getAttribute(AlldocsTag.ATTR_MODEL);

    if ((model != null) && (model != TagData.REQUEST_TIME_VALUE)) {

      try {

        // try to convert it to a URL
        new URI((String) model);
      }
       catch (URISyntaxException e) {

        valid = false;
      }

      // try-catch
    }

    // end if
    // only continue if the model parameter validated
    if (valid) {

      // validate the server attribute
      Object server = data.getAttribute(AlldocsTag.ATTR_SERVER);

      if ((server != null) && (server != TagData.REQUEST_TIME_VALUE)) {

        try {

          // try to convert it to a URL
          new URL((String) server);
        }
         catch (MalformedURLException e) {

          valid = false;
        }

        // try-catch
      }

      // end if
    }

    // end if
    // return the validity of tha params
    return valid;
  }

  // isValid()
}


// AlldocsTEI
