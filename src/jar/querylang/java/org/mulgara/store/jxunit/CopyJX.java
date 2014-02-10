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

package org.mulgara.store.jxunit;

// Java 2 standard packages
import java.io.*;
import java.net.URI;

// 3rd party package
import net.sourceforge.jxunit.*;

/**
 * Test frame for file copying.
 *
 * @created 2004-11-15
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:16 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class CopyJX implements JXTestStep {

  /**
   * Parameter name of the source file
   */
  public final static String SOURCE = "sourceURI";

  /**
   * Parameter name of the destination file
   */
  public final static String DESTINATION = "destinationURI";

  /**
   * Execute this object.
   *
   * Clears the test model from the graph, then retrieve a
   * filename from the testCase properties and load it as RDF. Results of each
   * stage are stored back in the properties object.
   *
   * @param testCase The map object containing the properties.
   * @throws Exception EXCEPTION TO DO
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();
    URI sourceURI      = new URI((String) props.get(SOURCE));
    URI destinationURI = new URI((String) props.get(DESTINATION));

    InputStream in = sourceURI.toURL().openStream();
    OutputStream out =
      new FileOutputStream(new File(destinationURI.toURL().getFile()));

    try {
      // Copy from source to destination in 1K chunks
      byte buffer[] = new byte[1024];
      int length;
      while ((length = in.read(buffer, 0, buffer.length)) != -1) {
        out.write(buffer, 0, length);
      }
      assert length == -1;
    } finally {
      out.close();
      in.close();
    }

    // clear out the parameters for subsequent copies
    props.remove(SOURCE);
    props.remove(DESTINATION);
  }
}
