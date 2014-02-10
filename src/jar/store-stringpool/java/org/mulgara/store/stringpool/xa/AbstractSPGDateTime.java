/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.store.stringpool.xa;

import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;
import org.mulgara.util.Timezone;

/**
 * Holds constants used by the SPG dateTime types.
 *
 * @created Sep 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractSPGDateTime extends AbstractSPTypedLiteral {

  private final static Logger logger = Logger.getLogger(AbstractSPGDateTime.class);

  /** The arbitrary default year to use for a type that has no year. */
  public static final int D_YEAR = 2000;

  /** The arbitrary default month to use for a type that has no month. */
  public static final int D_MONTH = 0;

  /** The arbitrary default day to use for a type that has no day. */
  public static final int D_DAY = 1;

  /** The arbitrary default hour to use for a types with no hour. */
  public static final int D_HOUR_OF_DAY = 0;

  /** The arbitrary default minute to use for a type that has no minutes. */
  public static final int D_MINUTE = 0;

  /** The arbitrary default seconds to use for a type that has no seconds. */
  public static final int D_SECOND = 0;

  /** The arbitrary default milliseconds to use for a type that has no milliseconds. */
  public static final int D_MILLISECOND = 0;

  /** Bit 0 indicates a timezone. */
  static final byte NO_TZ_BIT = 1;

  /** The date representation for the data. */
  protected Calendar gDate;

  /** The timezone info. */
  protected Timezone tz = null;


  /**
   * Constructs a new GDay representation using a calendar object representation.
   *
   * @param dayDate The gDay object represented as a date
   */
  AbstractSPGDateTime(int typeID, URI typeURI, Date dayDate) {
    super(typeID, typeURI);

    // Initialise the calendar object
    gDate = Calendar.getInstance();
    gDate.setTime(dayDate);
  }


  /**
   * Constructs a new GDay representation using a calendar object representation.
   *
   * @param dayCalendar The gDay object represented as a calendar
   */
  AbstractSPGDateTime(int typeID, URI typeURI, Calendar dayCalendar, Timezone tz) {
    super(typeID, typeURI);

    // Store the day date as a calendar
    gDate = dayCalendar;
    this.tz = tz;
  }


  /**
   * Creates a new gDay representation using a long value of the day
   * and creating a Date object from it.
   *
   * @param dayLong The day as a long
   */
  AbstractSPGDateTime(int typeID, URI typeURI, long dayLong) {
    // Use the date constructor to create a new instance
    this(typeID, typeURI, new Date(dayLong));
  }


  /**
   *
   * Constructs a gDay object which reads the day value from a byte buffer as
   * an integer.
   *
   * @param data The byte buffer storing the day as an integer
   */
  AbstractSPGDateTime(int typeID, URI typeURI, ByteBuffer data) {
    // Call the constructor using a long for the date
    this(typeID, typeURI, data.getLong());
    // update the timezone
    setTzByte(data.get());
  }


  /**
   * Retrieve the size of the buffer needed for this object. This is the size of the buffer
   * returned by {@link #getData()} or used by {@link #AbstractSPGDateTime(int, URI, ByteBuffer)}.
   * @return A buffer size in bytes.
   */
  public static int getBufferSize() {
    return Constants.SIZEOF_LONG + 1;
  }


  /**
   * Converts this gDate/Time object to a buffer of byte data.
   * @return The byte representation of this gDate/Time object
   */
  public ByteBuffer getData() {

    // Create a new byte buffer that can hold a long object
    ByteBuffer data = ByteBuffer.allocate(getBufferSize());

    // Store the date as a long value
    data.putLong(gDate.getTimeInMillis());
    data.put(getTzByte());

    // Prepare the buffer for reading
    data.flip();

    return data;
  }


  /**
   * Compares this gDay representation to another object to see if they are
   * the same values.  First the typing is checked and then the value.
   * @param object The object we are comparing against
   * @return Whether the gDay value is greater than (&gt; 0), less than (&lt; 0), or equal to (0) this value
   */
  public int compareTo(SPObject object) {
    // Compare types.
    int comparison = super.compareTo(object);
    if (comparison != 0) return comparison;
    // Compare the dates lexically
    return getLexicalForm().compareTo(((AbstractSPGDateTime)object).getLexicalForm());
  }


  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return gDate.hashCode();
  }


  /**
   * Determines whether the object is equal to the one passed in, in both type
   * and value.  This is different to the compareTo(Object) method in the
   * respect that it does a direct comparison, not a ranking comparison.
   * @param object The object to compare this one to
   * @return Whether the object is the same as this one
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    return object.getClass().isInstance(this) && ((AbstractSPGDateTime)object).getLexicalForm().equals(getLexicalForm());
  }


  /**
   * Create a new comparator for comparison operations.
   * @return The comparator to be used for comparisons
   */
  public SPComparator getSPComparator() {
    return SPGDateTimeComparator.getInstance();
  }


  /**
   * Convert the gDay representation to a lexical string as defined by XSD datatypes.
   * @return The lexical form of the gDay object
   */
  public String getLexicalForm() {
    // Create a formatter to output the date
    SimpleDateFormat formatter = new SimpleDateFormat(getFormatString());
    if (tz != null) formatter.setTimeZone(tz.asJavaTimeZone());

    // Apply the formatting
    String lexical = formatter.format(gDate.getTime());

    // append the timezone if needed
    if (tz != null) lexical += tz.toString();
    return lexical;
  }


  /**
   * Get the format string used for lexical form.
   * @return A format string used by {@linkplain java.text.SimpleDateFormat}.
   */
  abstract protected String getFormatString();


  /**
   * Set up a calendar object for the "arbitrary" year/month/time used by this type.
   * @param cal The calendar to configure
   */
  protected static void configureCalendar(Calendar cal) {
    cal.set(Calendar.YEAR, D_YEAR);
    cal.set(Calendar.MONTH, D_MONTH);
    cal.set(Calendar.DAY_OF_MONTH, D_DAY);
    cal.set(Calendar.HOUR_OF_DAY, D_HOUR_OF_DAY);
    cal.set(Calendar.MINUTE, D_MINUTE);
    cal.set(Calendar.SECOND, D_SECOND);
    cal.set(Calendar.MILLISECOND, D_MILLISECOND);
  }


  /**
   * Scan a lexical form for a timezone, and return an appropriate timezone.
   * @param str The lexical form of the day value.
   * @param maxDashes The number of dashes used when no timezone is present.
   * @return a new Timezone built from the string, or null if none found.
   */
  protected static Timezone scanForTimezone(String str, int maxDashes) {
    if (str.indexOf('Z') > 1) return Timezone.newZuluTimezone();

    // look for indicators of a negative or a positive timezone,
    // and set the start of the timezone string
    int startPos = str.indexOf('+') + 1;

    if (startPos == 0) {
      int splitLen = str.split("-").length;
      // Add a dash to check for negative timezones, and one more for the number of fields.
      if (splitLen > maxDashes + 2) throw new IllegalArgumentException("Bad timezone. Too many dashes in: " + str);
      if (splitLen == maxDashes + 2) startPos = str.lastIndexOf("-");
    } else {
      // positive offset, so there should be no dashes after this point
      if (str.lastIndexOf('-') >= startPos) throw new IllegalArgumentException("Bad timezone characters in: " + str);
    }

    Timezone result = null;
    // If the timezone string is there, then pull it out and set the object
    if (startPos > 1) {
      String[] zone = str.substring(startPos).split(":");
      if (zone.length < 2) throw new IllegalArgumentException("Bad timezone characters in: " + str.substring(startPos));
      result = buildTimezone(zone[0], zone[1]);
    }
    return result;
  }


  /**
   * Creates a "Standard" calendar object, with a default date and time, in the given timezone.
   * @param tz The timezone for the calendar.
   * @return A new default calendar in the given timezone.
   */
  protected static Calendar createCalendar(Timezone tz) {
    // Get the date/time object as a calendar
    Calendar calendar = tz == null ? new GregorianCalendar() : new GregorianCalendar(tz.asJavaTimeZone());
    configureCalendar(calendar);
    if (logger.isDebugEnabled()) {
      logger.debug("Calendar lexical string is: " +
                   calendar.get(Calendar.YEAR) + "-" +
                   calendar.get(Calendar.MONTH) + "-" +
                   calendar.get(Calendar.DAY_OF_MONTH));
      logger.debug("TimeZone of calendar is: " + calendar.getTimeZone());
      logger.debug("Calendar as date: " + calendar.getTime());
    }
    return calendar;
  }

  /**
   * Creates a Timezone for an object
   * @param hr The hour offset of the timezone as a string. May be negative.
   * @param min The minute offset of the timezone as a string.
   */
  private static Timezone buildTimezone(String hr, String min) {
    return new Timezone(Integer.parseInt(hr), Integer.parseInt(min));
  }


  /**
   * Calculate an encoding byte for the timezone.
   * @return A code for the presence of a timezone.
   */
  private byte getTzByte() {
    return (tz == null) ? NO_TZ_BIT : tz.getCode();
  }


  /**
   * Calculate an encoding byte for the timezone.
   * Sets a code for the presence of a timezone.
   */
  private void setTzByte(byte tzData) {
    tz = null;
    if ((tzData & NO_TZ_BIT) == 0) tz = new Timezone(tzData);
  }


  /**
   * Implementation of an SPComparator which compares the binary representations of GDateTime objects.
   */
  public static class SPGDateTimeComparator implements SPComparator {

    /** Singleton instance of the comparator */
    private static final SPGDateTimeComparator INSTANCE = new SPGDateTimeComparator();

    /**
     * Retrieves the singleton instance of this comparator.
     *
     * @return The comparator singleton instance
     */
    public static SPGDateTimeComparator getInstance() {
      return INSTANCE;
    }

    /**
     * Gives the comparator an opportunity to return an ordering where only the
     * prefix of the binary representation of one or both SPObjects is available.
     * If the comparator does not support this method or if an ordering can not
     * be determined from the available data then zero (0) should be returned.
     *
     * @param d1 The first gDay's byte buffer
     * @param d2 The second gDay's byte buffer
     * @param d2Size The number of bytes to compare
     *
     * @return Whether the first prefix is greater than (&gt; 0), less than (&lt; 0),
     *         or equal to (0) the other
     */
    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    /**
     * Compares the content of a byte buffer to the other and determines whether
     * they are equal or not.
     *
     * @param d1 The first byte buffer
     * @param d2 The second byte buffer
     * @return Whether the first buffer's content is greater than (&gt; 0), less
     *         than (&lt; 0), or equal to (0) the other
     */
    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
      int c = AbstractSPObject.compare(d1.getLong(), d2.getLong());
      if (c != 0) return c;
      // if equal then compare the timezone bytes
      return AbstractSPObject.compare(d1.get(), d2.get());
    }

  }

}
