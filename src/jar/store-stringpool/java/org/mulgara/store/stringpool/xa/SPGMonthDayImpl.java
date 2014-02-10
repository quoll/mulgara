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
 * An SPObject that represents a specific day in a specific month of Gregorian
 * Calendars.
 * Format: --MM-DD
 *
 * @created 2004-10-06
 *
 * @author Mark Ludlow
 * @author Paul Gearon
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPGMonthDayImpl extends AbstractSPGDateTime {

  private final static Logger logger = Logger.getLogger(SPGMonthDayImpl.class);

  static final int TYPE_ID = 9; // Unique ID

  /** URI for our gmonthDay representation */
  static final URI TYPE_URI = XSD.GMONTHDAY_URI;

  /** The number of dashes that appear without a timezone */
  static private final int STD_DASHES = 3;

  /**
   * Constructs a new GMonthDay representation using a calendar object representation.
   * @param monthDayDate The gMonthDay object represented as an integer
   */
  SPGMonthDayImpl(Date monthDayDate) {
    super(TYPE_ID, TYPE_URI, monthDayDate);
  }

  /**
   * Constructs a new GMonthDay representation using a calendar object representation.
   * @param monthDayCalendar The gMonthDay object represented as a calendar
   */
  SPGMonthDayImpl(Calendar monthDayCalendar, Timezone tz) {
    super(TYPE_ID, TYPE_URI, monthDayCalendar, tz);
  }

  /**
   * Constructs a gMonthDay object which reads the monthDay value from a byte buffer as an integer.
   * @param data The byte buffer storing the monthDay as an integer
   */
  SPGMonthDayImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI, data);
  }

  /**
   * Creates a new gMonthDay representation using a long value of the monthDay
   * and creating a Date object from it.
   * @param monthDayLong The monthDay as a long
   */
  SPGMonthDayImpl(long monthDayLong) {
    super(TYPE_ID, TYPE_URI, monthDayLong);
  }

  /**
   * Constructs a new GMonthDay object given the lexical form of a date.
   * @param lexicalForm The lexical form of the GMonthDay object
   * @return A new SPGMonthDay instance
   */
  static SPGMonthDayImpl newInstance(String lexicalForm) {
    if (lexicalForm.length() < 7 || !lexicalForm.startsWith("--") || lexicalForm.charAt(4) != '-') {
      throw new IllegalArgumentException("Invalid gMonthDay lexical format: " + lexicalForm);
    }

    String monthValue = lexicalForm.substring(2, 4);
    int monthInt = Integer.parseInt(monthValue);

    // Check that the value is valid
    if (monthInt <= 0 || monthInt >= 13) {
      throw new IllegalArgumentException("gMonthDay value [" + lexicalForm +
                                         "] does not have a valid month number.");
    }
    String dayValue = lexicalForm.substring(5, 7);
    int dayInt = Integer.parseInt(dayValue);

    // Check that the value is valid
    if (dayInt <= 0 || dayInt >= 32) {
      throw new IllegalArgumentException("gMonthDay value [" + lexicalForm +
                                         "] does have a valid day number.");
    }

    // Create a timezone for this object
    Timezone tz = scanForTimezone(lexicalForm, STD_DASHES);

    Calendar calendar = createCalendar(tz);

    calendar.set(Calendar.MONTH, monthInt - 1);
    calendar.set(Calendar.DAY_OF_MONTH, dayInt);

    if (logger.isDebugEnabled()) {
      logger.debug("Day value before calendar is: " + dayInt);
      logger.debug("Month value before calendar is: " + monthInt);
    }

    // Create our object
    return new SPGMonthDayImpl(calendar, tz);
  }


  protected String getFormatString() {
    return "'--'MM-dd";
  }

}
