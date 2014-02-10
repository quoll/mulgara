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
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

// Third party packages
import org.apache.log4j.*;

/**
 * Returns {@link javax.xml.transform.Transformer}s for stylesheets, returning
 * cached versions where possible. <p>
 *
 * This should probably be implemented as a subclass of
 * <code>javax.xml.transform.TransformerFactory</code>. </p>
 *
 * @created 2001-11-13
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:09 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class CachedTransformerFactory {

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
    Logger.getLogger(CachedTransformerFactory.class.getName());

  //
  // Members
  //

  /**
   * the map of XSLT transformers
   */
  private static HashMap<URL,Transformer> transformers = new HashMap<URL,Transformer>();

  //
  // Public API
  //

  /**
   * Returns a transformer for the given <code>stylesheet</code>.
   *
   * @param stylesheet PARAMETER TO DO
   * @return a transformer for the given <code>stylesheet</code>
   * @throws TransformerConfigurationException if the transformed cannot be
   *      configured using the <code>masterStyleSheet</code>
   * @throws IOException if a stream cannot be obtained to the <code>styleSheet</code>
   */
  public static Transformer getTransformer(URL stylesheet)
    throws TransformerConfigurationException, IOException {

    // log what we're doing
    log.debug("Finding transformer for " + stylesheet.toString());

    // create the transformer variable
    Transformer transformer = null;

    // find out if we already have a transformer for this stylesheet
    if (CachedTransformerFactory.transformers.containsKey(stylesheet)) {

      // log that we've had a cache hit
      log.debug("Found transformer for stylesheet in cache");

      // return the transformer for this stylesheet
      transformer = CachedTransformerFactory.transformers.get(stylesheet);
    }
    else {

      // log that we need to create a new transformer
      log.debug("Creating new transformer for " + stylesheet);

      // create a new transformer for this stylesheet
      StreamSource stylesheetSource = new StreamSource(stylesheet.openStream());
      transformer =
        TransformerFactory.newInstance().newTransformer(stylesheetSource);

      // add it to the map
      CachedTransformerFactory.transformers.put(stylesheet, transformer);
    }

    // end if
    // return the transformer
    return transformer;
  }

  // getTransformer()
}


// CachedTransformerFactory
