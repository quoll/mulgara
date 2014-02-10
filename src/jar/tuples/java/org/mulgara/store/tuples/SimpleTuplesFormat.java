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

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.text.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Format a {@link Tuples} as a {@link String}.
 *
 * @created 2004-03-02
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class SimpleTuplesFormat {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(SimpleTuplesFormat.class.getName());

  /**
   * Line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * Format for local node numbers.
   */
  private static final ThreadLocal<NumberFormat> formatter = new ThreadLocal<NumberFormat>() {
    @Override
    protected NumberFormat initialValue() {
      return new DecimalFormat("000000");
    }
  };

  /**
   * The maximum number of rows to display before abbreviating with an ellipsis.
   */
  private static final long MAX_ROWS = 20;

  /**
   * Format a {@link Tuples} as a {@link String}.
   *
   * @param tuples  the instance to serialize
   * @return the serialized representation of the <var>tuples</var>
   */
  public static String format(Tuples tuples) {
    Tuples cloned = (Tuples) tuples.clone();

    try {
      StringBuffer buffer = new StringBuffer(eol + "{");

      // Display column headings
      Variable[] variables = cloned.getVariables();
      for (int i = 0; i < variables.length; i++) {
        buffer.append(variables[i]);
        for (int j = 0; j < (6 - variables[i].toString().length()); j++) {
          buffer.append(" ");
        }
        buffer.append("  ");
      }

      // Indicate whether this instance is material or virtual
      if (cloned.isMaterialized()) {
        buffer.append("(");
        buffer.append(cloned.getRowCount());
        buffer.append(" rows)" + eol);
      } else {
        buffer.append("(unevaluated, ");
        buffer.append(cloned.getRowUpperBound());
        buffer.append(" rows max, ");
        buffer.append(cloned.getRowExpectedCount());
        buffer.append(" rows expected)").append(eol);
      }

      // Display rows
      cloned.beforeFirst();
      long rowNo = 0;
      while (cloned.next()) {
        if (++rowNo > MAX_ROWS) {
          buffer.append("..." + eol);
          break;
        }

        buffer.append("[");
        for (int i = 0; i < variables.length; i++) {
          buffer.append(formatter.get().format(cloned.getColumnValue(i)));
          buffer.append("  ");
        }
        buffer.append("]" + eol);
      }

      buffer.append("}");

      return buffer.toString();
    }
    catch (TuplesException e) {
      return e.toString();
    }
    finally {
      try {
        cloned.close();
      }
      catch (Exception e) {
        logger.warn("Failed to close tuples after serializing", e);
      }
    }
  }
}
