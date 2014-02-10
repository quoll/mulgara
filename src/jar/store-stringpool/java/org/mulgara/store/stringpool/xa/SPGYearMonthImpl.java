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
package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Calendar;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.util.Timezone;

/**
 * An SPObject that represents a specific month in a specific year of the
 * Gregorian calendar.
 * Format: YYYY-MM
 *
 * @created 2004-10-06
 * @author Mark Ludlow
 * @author Paul Gearon
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPGYearMonthImpl extends AbstractSPGDateTime {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPGYearMonthImpl.class);

  static final int TYPE_ID = 7; // Unique ID

  static final URI TYPE_URI = XSD.GYEARMONTH_URI;

  /** The number of dashes that appear without a timezone */
  static private final int STD_DASHES = 1;

  /**
   * Constructs a new GYearMonth representation using a calendar object representation.
   * @param yearMonthDate The gYearMonth object represented as an integer
   */
  SPGYearMonthImpl(Date yearMonthDate) {
    super(TYPE_ID, TYPE_URI, yearMonthDate);
  }

  /**
   * Constructs a new GYearMonth representation using a calendar object representation.
   * @param yearMonthCalendar The gYearMonth object represented as a calendar
   */
  SPGYearMonthImpl(Calendar yearMonthCalendar, Timezone tz) {
    super(TYPE_ID, TYPE_URI, yearMonthCalendar, tz);
  }

  /**
   * Constructs a gYearMonth object which reads the yearMonth value from a byte buffer as an integer.
   * @param data The byte buffer storing the yearMonth as an integer
   */
  SPGYearMonthImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI, data);
  }

  /**
   * Creates a new gYearMonth representation using a long value of the yearMonth
   * and creating a Date object from it.
   * @param yearMonth The yearMonth as a long
   */
  SPGYearMonthImpl(long yearMonth) {
    super(TYPE_ID, TYPE_URI, yearMonth);
  }


  /**
   * Constructs a new GYearMonth object given the lexical form of a date.
   * @param lexicalForm The lexical form of the GYearMonth object
   * @return A new SPGYearMonth instance
   */
  static SPGYearMonthImpl newInstance(String lexicalForm) {
    int dashPos = lexicalForm.indexOf('-');
    if (lexicalForm.length() < 7 || dashPos < 4) {
      throw new IllegalArgumentException("Invalid gYearMonth lexical format: " + lexicalForm);
    }

    int lastDashPos = lexicalForm.lastIndexOf('-');
    int end = (lastDashPos > dashPos) ? lastDashPos : lexicalForm.length();
    // chop off the last character if this is a Zulu date
    if (lexicalForm.charAt(end - 1) == 'Z') end--;

    if (end - dashPos != 3) throw new IllegalArgumentException("Invalid month in gYearMonth lexical format: " + lexicalForm);

    String monthValue = lexicalForm.substring(dashPos + 1, end);
    String yearValue = lexicalForm.substring(0, dashPos);

    // Parse the value to an integer (We know it is a valid number)
    int monthInt;
    int yearInt;
    try {
      monthInt = Integer.parseInt(monthValue);

      // Check that the value is valid
      if (monthInt <= 0 || monthInt >= 13) {
        throw new IllegalArgumentException("gYearMonth value [" + lexicalForm +
                                           "] does not have a valid month number.");
      }

      yearInt = Integer.parseInt(yearValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid gYearMonth lexical format: " + lexicalForm);
    }

    // Create a timezone for this object
    Timezone tz = scanForTimezone(lexicalForm, STD_DASHES);

    Calendar calendar = createCalendar(tz);

    calendar.set(Calendar.YEAR, yearInt);
    calendar.set(Calendar.MONTH, monthInt - 1);

    if (logger.isDebugEnabled()) {
      logger.debug("Year value before calendar is: " + yearInt);
      logger.debug("Month value before calendar is: " + monthInt);
    }

    // Create our object
    return new SPGYearMonthImpl(calendar, tz);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.AbstractSPGDateTime#getFormatString()
   */
  protected String getFormatString() {
    return "yyyy-MM";
  }
  
}
