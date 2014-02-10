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
 * An SPObject that represents yearly periodic Gregorian calendar months.
 * Format: --MM
 *
 * @created 2004-10-06
 * @author Mark Ludlow
 * @author Paul Gearon
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPGMonthImpl extends AbstractSPGDateTime {

  private final static Logger logger = Logger.getLogger(SPGMonthImpl.class);

  static final int TYPE_ID = 11; // Unique ID

  /** URI for our gmonth representation */
  static final URI TYPE_URI = XSD.GMONTH_URI;

  /** The number of dashes that appear without a timezone */
  static private final int STD_DASHES = 2;


  /**
   * Constructs a new GMonth representation using a calendar object representation.
   * @param monthDate The gMonth object represented as an integer
   */
  SPGMonthImpl(Date monthDate) {
    super(TYPE_ID, TYPE_URI, monthDate);
  }

  /**
   * Constructs a new GMonth representation using a calendar object representation.
   * @param monthCalendar The gMonth object represented as a calendar
   */
  SPGMonthImpl(Calendar monthCalendar, Timezone tz) {
    super(TYPE_ID, TYPE_URI, monthCalendar, tz);
  }

  /**
   * Constructs a gMonth object which reads the month value from a byte buffer as an integer.
   * @param data The byte buffer storing the month as an integer
   */
  SPGMonthImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI, data);
  }

  /**
   * Creates a new gMonth representation using a long value of the month
   * and creating a Date object from it.
   * @param monthLong The month as a long
   */
  SPGMonthImpl(long monthLong) {
    super(TYPE_ID, TYPE_URI, monthLong);
  }

  /**
   * Constructs a new GMonth object given the lexical form of a date.
   * @param lexicalForm The lexical form of the GMonth object
   * @return A new SPGMonth instance
   */
  static SPGMonthImpl newInstance(String lexicalForm) {
    if (lexicalForm.length() < 4 || !lexicalForm.startsWith("--")) {
      throw new IllegalArgumentException("Invalid gDay lexical format: " + lexicalForm);
    }

    String dateValue = lexicalForm.substring(2, 4);
    int dateInt = Integer.parseInt(dateValue);

    // Check that the value is valid
    if (dateInt <= 0 || dateInt >= 13) {
      throw new IllegalArgumentException("gMonth value [" + lexicalForm + "] is not a valid month number.");
    }

    // Create a timezone for this object
    Timezone tz = scanForTimezone(lexicalForm, STD_DASHES);

    Calendar calendar = createCalendar(tz);
    calendar.set(Calendar.MONTH, dateInt - 1);

    if (logger.isDebugEnabled()) logger.debug("Month value before calendar is: " + dateValue);

    return new SPGMonthImpl(calendar, tz);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.AbstractSPGDateTime#getFormatString()
   */
  protected String getFormatString() {
    return "'--'MM";
  }

}
