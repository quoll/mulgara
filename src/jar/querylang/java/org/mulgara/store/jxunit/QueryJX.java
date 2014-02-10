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
 * Test frame for iTQL queries.
 *
 * @created 2001-12-18
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class QueryJX implements JXTestStep {

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** Name of the query command parameter */
  public final static String QUERY = "query";

  /** Parameter name of the results of the command */
  public final static String RESULT = "queryResult";

  /** Parameter name of the time flag */
  public final static String TIME = "time";

  /** Name of the queryName command parameter */
  public final static String QUERY_NAME = "queryName";

  /**
   * Execute this object. Retrieves a string from the testCase properties and
   * executes it as an iTQL command, returning the results in the testCase
   * object.
   *

   * @param testCase The map object containing the properties.
   * @throws Exception EXCEPTION TO DO
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();
    String query = (String) props.get(QUERY);

    boolean time = false;
    if (props.getString(TIME) != null) time = Boolean.valueOf(props.getString(TIME)).booleanValue();

    String queryName = query + eol;
    if (props.getString(QUERY_NAME) != null) queryName = props.getString(QUERY_NAME);

    ItqlInterpreterBean iTQL = new ItqlInterpreterBean();

    long start = System.currentTimeMillis();
    String queryResult = "";

    try {
      queryResult = iTQL.executeQueryToString(query);
    } catch (Exception ex) {
      queryResult = ex.getMessage();
    } finally {
      iTQL.close();
    }

    if (time) {
      System.out.println(eol + "<" + queryName + "> query has taken " +
        (System.currentTimeMillis() - start) + " milliseconds to execute." + eol);
    }

    JXUtil.putStringProp(props, RESULT, queryResult + eol);
    props.remove(QUERY_NAME);
  }
}
