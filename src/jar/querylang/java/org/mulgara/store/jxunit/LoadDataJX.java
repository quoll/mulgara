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
 * Test frame for model creation using iTQL.
 *
 * @created 2001-12-18
 * @author <a href="http://staff.pisoftware.com/pag">Paula Gearon</a>
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LoadDataJX implements JXTestStep {

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** Parameter name of the model name */
  public final static String MODEL = "modelName";

  /** Parameter name of the load command */
  public final static String COMMAND = "loadCommand";

  /** Parameter name of the type of model */
  public final static String TYPE = "modelType";

  /** Parameter name of the results of operations performed prior to loading */
  public final static String DROP_RESULT = "preCreateResult";

  /** Parameter name of the results of the model creation */
  public final static String CREATE_RESULT = "createResult";

  /** Parameter name of the results of the load operation */
  public final static String LOAD_RESULT = "loadResult";

  /** Parameter name of the "Clear Graph" command */
  public final static String CLEAR = "clearModel";

  /**
   * Execute this object. Clears the test model from the graph, then retrieve a
   * filename from the testCase properties and load it as RDF. Results of each
   * stage are stored back in the properties object.
   * @param testCase The map object containing the properties.
   * @throws Exception A problex executing the command.
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();
    String model = (String)props.get(MODEL);

    // Performing a load is optional
    String cmd = "";
    if (props.get(COMMAND) != null) cmd = (String)props.get(COMMAND);

    // The type of model is optional
    String type = "";
    if (props.get(TYPE) != null) type = (String)props.get(TYPE);

    String dropResult = null;

    ItqlInterpreterBean iTQL = new ItqlInterpreterBean();
    try {
      boolean clear = true;
      if (props.get(CLEAR) != null) {
        // anything that is not "true" is considered "false"
        clear = ((String) props.get(CLEAR)).equalsIgnoreCase("true");
      }

      if (clear) {
        // Attempt to drop the model - may not be successful if model does not exist
        try {
          dropResult = iTQL.executeQueryToString("drop <" + model + "> ;");
        } catch (Exception ex) {
          System.out.println("Unable to drop model - may have not existed");
        }
      }

      // Create the model - optional model type
      String createResult = "";

      if (type.equals("")) {
        createResult = iTQL.executeQueryToString("create <" + model + "> ;");
      } else {
        createResult = iTQL.executeQueryToString("create <" + model + "> " + "<" + type + "> ;");
      }

      // Execute the command if supplied
      String cmdResult = "";

      if (!cmd.equals("")) cmdResult = iTQL.executeQueryToString(cmd);

      JXUtil.putStringProp(props, DROP_RESULT, dropResult);
      JXUtil.putStringProp(props, CREATE_RESULT, createResult + eol);
      JXUtil.putStringProp(props, LOAD_RESULT, cmdResult + eol);

    } finally {
      iTQL.close();
    }

    // clear out the parameters for additional loads
    props.remove(COMMAND);
    props.remove(TYPE);
    props.remove(MODEL);
  }
}
