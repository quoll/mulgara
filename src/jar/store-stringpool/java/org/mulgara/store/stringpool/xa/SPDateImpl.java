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

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;


/**
 * An SPObject that represents dates.
 *
 * @created 2002-03-07
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPDateImpl extends AbstractSPTypedLiteral {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDateImpl.class);

  static final int TYPE_ID = 5; // Unique ID

  static final URI TYPE_URI = XSD.DATE_URI;

  static final int MAX_LEN_NO_TIMEZONE = "-YYYY-MM-dd".length();

  static final int TIMEZONE_LEN = "+00:00".length();

  static final int MILLIS_IN_MINUTE = 60000;

  static final DateTimeFormatter utcParser;

  static final DateTimeFormatter tzParser;

  static final DateTimeFormatter outputFormat;

  private Date date;

  private int timezoneOffset = 0;

  static {
    DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
    b.appendYear(4, 4).appendLiteral('-').appendMonthOfYear(2).appendLiteral('-').appendDayOfMonth(2);
    b.appendOptional(new DateTimeFormatterBuilder().appendTimeZoneOffset("", true, 2, 2).toParser());
    utcParser = b.toFormatter().withZone(DateTimeZone.UTC);
    b = new DateTimeFormatterBuilder();
    tzParser = b.appendTimeZoneOffset("", true, 2, 2).toFormatter().withZone(DateTimeZone.UTC);
    b = new DateTimeFormatterBuilder();
    b.appendYear(4, 4).appendLiteral('-').appendMonthOfYear(2).appendLiteral('-').appendDayOfMonth(2).appendTimeZoneOffset("+00:00", true, 2, 2);
    outputFormat = b.toFormatter();
  }

  private SPDateImpl(Date date, int timezoneOffset) {
    super(TYPE_ID, TYPE_URI);

    if (date == null) {
      throw new IllegalArgumentException("Null \"date\" parameter");
    }

    this.date = date;
    this.timezoneOffset = timezoneOffset;
  }


  SPDateImpl(ByteBuffer data) {
    this(data.getLong(), (data.limit() > Constants.SIZEOF_LONG) ? data.getInt() : calcTZOffset(data.getLong(0)));
  }

  /** Guess the timezone offset the given time (date) was stored under */
  private static int calcTZOffset(long time) {
    int minutes = new DateTime(time, DateTimeZone.UTC).getMinuteOfDay();
    return (minutes < 12*60) ? -minutes : (minutes > 12*60) ? 24*60 - minutes :
           (DateTimeZone.getDefault().getOffset(time) > 0) ? -minutes : minutes;
  }


  SPDateImpl(long l) {
    this(new Date(l), 0);
  }

  SPDateImpl(long l, int timezoneMinutes) {
    this(new Date(l), timezoneMinutes);
  }

  SPDateImpl(long l, int timezoneHours, int timezoneMinutes) {
    this(new Date(l), timezoneHours * 60 + timezoneMinutes);
  }

  static SPDateImpl newInstance(String lexicalForm) {
    int minuteOffset = 0;
    if (lexicalForm.length() > MAX_LEN_NO_TIMEZONE) {
      int timezoneStart = lexicalForm.length() - TIMEZONE_LEN;
      String timezone = lexicalForm.substring(timezoneStart);
      minuteOffset = -(int)(tzParser.parseDateTime(timezone).getMillis() / MILLIS_IN_MINUTE);
    } else {
      minuteOffset = Integer.MIN_VALUE;
    }
    Date date = new Date(utcParser.parseDateTime(lexicalForm).getMillis());
    return new SPDateImpl(date, minuteOffset);
  }


  /* from SPObject interface. */

  public ByteBuffer getData() {
    ByteBuffer data = ByteBuffer.allocate(Constants.SIZEOF_LONG + Constants.SIZEOF_INT);
    data.putLong(date.getTime());
    data.putInt(timezoneOffset);
    data.flip();
    return data;
  }


  public SPComparator getSPComparator() {
    return SPDateComparator.getInstance();
  }


  public String getLexicalForm() {
    if (timezoneOffset == Integer.MIN_VALUE) {
      // no timezone information
      return ISODateTimeFormat.date().withZone(DateTimeZone.UTC).print(date.getTime());
    }
    DateTimeZone zone = DateTimeZone.forOffsetMillis(timezoneOffset * MILLIS_IN_MINUTE);
    return outputFormat.withZone(zone).print(date.getTime());
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the Dates.
    return date.compareTo(((SPDateImpl)o).date);
  }


  /**
   * Used for caching, so it cannot compare by value.
   * @return The hashcode for this date with timezone.
   */
  public int hashCode() {
    return date.hashCode() ^ timezoneOffset;
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   * Used for caching, so it cannot compare by value.
   */
  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      SPDateImpl oDate = (SPDateImpl)obj;
      return date.equals(oDate.date) && timezoneOffset == oDate.timezoneOffset;
    } catch (ClassCastException ex) {
      // obj was not an SPDateImpl.
      return false;
    }
  }


  /** Compares the binary representations of two SPDateImpl objects. */
  public static class SPDateComparator implements SPComparator {

    private static final SPDateComparator INSTANCE = new SPDateComparator();

    public static SPDateComparator getInstance() {
      return INSTANCE;
    }

    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
      int c = AbstractSPObject.compare(d1.getLong(), d2.getLong());
      // if the times are the same, then differentiate by timezone
      if (c == 0) c = AbstractSPObject.compare(d1.getInt(), d2.getInt());
      return c;
    }

  }

}
