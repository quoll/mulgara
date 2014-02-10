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

// 3rd party package
import net.sourceforge.jxunit.*;

// Locally created package.
import org.mulgara.itql.ItqlInterpreterBean;

/**
 * Test frame for restoring a database
 *
 * @created 2001-12-18
 *
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
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
public class RestoreDataJX implements JXTestStep {

  /**
   * Parameter name of the server name
   */
  public final static String SERVER = "server";

  /**
   * Parameter name of the file name
   */
  public final static String FILE = "file";

  /**
   * Parameter name of the time flag
   */
  public final static String TIME = "time";

  /**
   * Supplied a valid server name and a file this method will restore a Mulgara
   * database.
   *
   * @param testCase PARAMETER TO DO
   * @throws Exception EXCEPTION TO DO
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();

    String server = (String) props.get(SERVER);
    String file = (String) props.get(FILE);

    boolean time = false;

    if (props.getString(TIME) != null) {

      time = Boolean.valueOf(props.getString(TIME)).booleanValue();
    }

    ItqlInterpreterBean iTQL = new ItqlInterpreterBean();

    System.out.println("Restoring Mulgara database from " + file +
      " please wait...");

    long start = System.currentTimeMillis();
    try {
      iTQL.executeQueryToString(
          "restore <" + server + "> from <" + file + ">;"
      );
    } finally {
      iTQL.close();
    }

    if (time) {

      System.out.println("Time to restore " + file + " was " +
        ((System.currentTimeMillis() - start) / 1000L) + " seconds");
    }

    // clear out the parameters for additional restore
    props.remove(SERVER);
    props.remove(FILE);
  }
}
