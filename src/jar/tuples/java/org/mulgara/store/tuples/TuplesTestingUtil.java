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
 * The Initial Developer of the Original Code is Andrew Newman.
 *  Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.tuples;

// Third party packages
import junit.framework.*;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Common utilities for testing tuples.
 *
 * @created 2005-03-08
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/07 19:42:40 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class TuplesTestingUtil {

  public static void closeTuples(Tuples[] tuples) throws TuplesException {
    for (int i = 0; i < tuples.length; i++) {
      tuples[i].close();
    }
  }

  public static void testTuplesRow(Tuples joined, Variable[] variables)
      throws TuplesException {
    for (int i = 0; i < variables.length; i++) {
      Assert.assertEquals(i, joined.getColumnIndex(variables[i]));
    }
  }

  public static void testTuplesRow(Tuples actual, long[] values) throws TuplesException {
    Assert.assertTrue(actual.next());
    for (int i = 0; i < values.length; i++) {
      Assert.assertEquals("Index: " + i, values[i], actual.getColumnValue(i));
    }
  }

  public static void testVariables(Variable[] expected, Variable[] actual) {
    Assert.assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(expected[i], actual[i]);
    }
  }

  public static void checkBeforeFirst(Tuples joined) {
    try {
      joined.getColumnValue(0);
      Assert.fail("Shouldn't be able to read values before the first row");
    }
    catch (TuplesException e) {

      // correct behavior
    }
  }
}
