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
 * An SPObject that represents monthly Gregorian calendar days.
 * Format: ---DD
 *
 * @created 2004-10-06
 * @author Mark Ludlow
 * @author Paul Gearon
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPGDayImpl extends AbstractSPGDateTime {

  private final static Logger logger = Logger.getLogger(SPGDayImpl.class);

  static final int TYPE_ID = 10; // Unique ID

  /** URI for our gday representation */
  static final URI TYPE_URI = XSD.GDAY_URI;

  /** The number of dashes that appear without a timezone */
  static private final int STD_DASHES = 3;

  /**
   * Constructs a new GDay representation using a calendar object representation.
   * @param dayDate The gDay object represented as a date
   */
  SPGDayImpl(Date dayDate) {
    super(TYPE_ID, TYPE_URI, dayDate);
  }

  /**
   * Constructs a new GDay representation using a calendar object representation.
   * @param dayCalendar The gDay object represented as a calendar
   */
  SPGDayImpl(Calendar dayCalendar, Timezone tz) {
    super(TYPE_ID, TYPE_URI, dayCalendar, tz);
  }

  /**
   *
   * Constructs a gDay object which reads the day value from a byte buffer as an integer.
   * @param data The byte buffer storing the day as an integer
   */
  SPGDayImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI, data);
  }

  /**
   * Creates a new gDay representation using a long value of the day
   * and creating a Date object from it.
   * @param dayLong The day as a long
   */
  SPGDayImpl(long dayLong) {
    super(TYPE_ID, TYPE_URI, dayLong);
  }


  /**
   * Constructs a new GDay object given the lexical form of a date.
   *
   * @param lexicalForm The lexical form of the GDay object
   * @return A new SPGDay instance
   */
  static SPGDayImpl newInstance(String lexicalForm) {

    if (lexicalForm.length() < 5 || !lexicalForm.startsWith("---")) {
      throw new IllegalArgumentException("Invalid gDay lexical format: " + lexicalForm);
    }

    String dateValue = lexicalForm.substring(3, 5);

    // Parse the value to an integer
    int dateInt;
    try {
      dateInt = Integer.parseInt(dateValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid gDay lexical format: " + lexicalForm);
    }

    // Check that the value is valid
    if (dateInt <= 0 || dateInt >= 32) {
      throw new IllegalArgumentException("gDay value [" + lexicalForm + "] is not a valid day number.");
    }

    // Create a timezone for this object
    Timezone tz = scanForTimezone(lexicalForm, STD_DASHES);

    Calendar calendar = createCalendar(tz);

    calendar.set(Calendar.DAY_OF_MONTH, dateInt);

    if (logger.isDebugEnabled()) logger.debug("Day value before calendar is: " + dateInt);

    // Create our object
    return new SPGDayImpl(calendar, tz);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.AbstractSPGDateTime#getFormatString()
   */
  protected String getFormatString() {
    return "'---'dd";
  }

}
