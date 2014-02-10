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
 * An SPObject that represents non-periodic Gregorian calendar years.
 * Format: YYYY (no left truncation allowed)
 *
 * @created 2004-10-04
 * @author Mark Ludlow
 * @author Paul Gearon
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPGYearImpl extends AbstractSPGDateTime {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPGYearImpl.class);

  static final int TYPE_ID = 8; // Unique ID

  static final URI TYPE_URI = XSD.GYEAR_URI;

  /** The number of dashes that appear without a timezone */
  static private final int STD_DASHES = 0;

  /**
   * Constructs a new GYear representation using a calendar object representation.
   * @param yearDate The gYear object represented as an integer
   */
  SPGYearImpl(Date yearDate) {
    super(TYPE_ID, TYPE_URI, yearDate);
  }

  /**
   * Constructs a new GYear representation using a calendar object representation.
   * @param yearCalendar The gYear object represented as a calendar
   */
  SPGYearImpl(Calendar yearCalendar, Timezone tz) {
    super(TYPE_ID, TYPE_URI, yearCalendar, tz);
  }

  /**
   * Constructs a gYear object which reads the year value from a byte buffer as an integer.
   * @param data The byte buffer storing the year as an integer
   */
  SPGYearImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI, data);
  }

  /**
   * Creates a new gYear representation using a long value of the year
   * and creating a Date object from it.
   * @param year The year as a long
   */
  SPGYearImpl(long year) {
    super(TYPE_ID, TYPE_URI, year);
  }

  /**
   * Constructs a new GYear object given the lexical form of a date.
   * @param lexicalForm The lexical form of the GYear object
   * @return A new SPGYear instance
   */
  static SPGYearImpl newInstance(String lexicalForm) {
    if (lexicalForm.length() < 4) {
      throw new IllegalArgumentException("Invalid gYear lexical format: " + lexicalForm);
    }

    String yearValue = lexicalForm.substring(0, yearEnd(lexicalForm));

    // Parse the value to an integer
    int yearInt;
    try {
      yearInt = Integer.parseInt(yearValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid gYear lexical format: " + lexicalForm);
    }

    // Create a timezone for this object
    Timezone tz = scanForTimezone(lexicalForm, STD_DASHES);

    Calendar calendar = createCalendar(tz);

    calendar.set(Calendar.YEAR, yearInt);

    if (logger.isDebugEnabled()) logger.debug("Year value before calendar is: " + yearInt);

    // Create our object
    return new SPGYearImpl(calendar, tz);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.AbstractSPGDateTime#getFormatString()
   */
  protected String getFormatString() {
    return "yyyy";
  }


  /**
   * Finds the end of the year text. This is marked either by the end of the string,
   * or the start of the timezone.
   * @param lexical The lexical representation of the gYear.
   * @return The position of the first character that is not a part of the year.
   */
  private static int yearEnd(String lexical) {
    int end = lexical.length();
    int tzCharPos = lexical.indexOf('+');
    if (tzCharPos > 0) {
      if (lexical.indexOf('-') > 0) throw new IllegalArgumentException("Invalid gYear lexical format: " + lexical);
      end = tzCharPos;
    } else {
      tzCharPos = lexical.indexOf('-');
      if (tzCharPos > 0) end = tzCharPos;
      else if (lexical.charAt(end - 1) == 'Z') end--;
    }
    return end;
  }

}
