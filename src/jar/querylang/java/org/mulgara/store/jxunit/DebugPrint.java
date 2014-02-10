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
// third party packages
// Plugged In packages
// third party packages
import net.sourceforge.jxunit.*;  // Plugged In packages

/**
 * Debug object for printing values in JXUnit tests.
 *
 * @created 2001-12-18
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:16 $
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
public class DebugPrint implements JXTestStep {

  /**
   * Name of the print parameter
   */
  public final static String PRINT = "print";

  /**
   * Name of the label parameter
   */
  public final static String LABEL = "label";

  /**
   * Name of the debug paramter
   */
  public final static String DEBUG = "debug";

  /**
   * boolean value for the debug parameter
   */
  public final static String TRUE = "true";

  /**
   * Execute this object. Retrieves a string from the testCase properties and
   * prints it to stdout.
   *
   * @param testCase The map object containing the properties.
   * @throws Exception EXCEPTION TO DO
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();
    String debug = (String) props.get(DEBUG);

    if ((debug != null) && debug.equals(TRUE)) {

      String print = (String) props.get(PRINT);
      String label = (String) props.get(LABEL);

      System.out.println(label + ": " + print);
    }
  }
}
