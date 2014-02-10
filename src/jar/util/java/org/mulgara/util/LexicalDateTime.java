/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.util;

import java.text.ParseException;
import java.nio.ByteBuffer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static org.joda.time.DateTimeZone.UTC;
import static org.mulgara.util.Constants.SIZEOF_LONG;

/**
 * This class represents a dateTime value, preserving its lexical representation exactly.
 * It stores the value of the dateTime in the canonical form, but also contains values which
 * allow the preservation of the non-canonical format.
 *
 * @created Jun 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class LexicalDateTime {

  /** The character for separating date elements */
  private static final char DATE_SEPARATOR = '-';

  /** The character for separating the date part from the time part */
  private static final char DATE_TIME_SEPARATOR = 'T';

  /** The character for separating time elements */
  private static final char TIME_SEPARATOR = ':';

  /** The character for separating the milliseconds from the seconds */
  private static final char MILLI_SEPARATOR = '.';

  /** The string form for the character separating the milliseconds from the seconds */
  private static final String MILLI_SEPARATOR_STR = ".";

  /** The character for indicating the UTC timezone (Zulu time). */
  private static final char ZULU = 'Z';

  /** The string form for the character indicating the UTC timezone (Zulu time). */
  private static final String ZULU_STR = "Z";

  /** The character for indicating a positive timezone offset */
  private static final char POS_TZ = '+';

  /** The character for indicating a negative timezone offset */
  private static final char NEG_TZ = '-';

  /** The hour value for midnight */
  private static final int MIDNIGHT = 24;

  /** The string representation of midnight when the midnight flag is set */
  private static final String MIDNIGHT_STR = "24:00:00";

  /** Standard start of parsing error messages */
  private static final String BAD_FORMAT = "Bad format in ";

  /** Output format for the dateTime */
  private static final String LEXICAL_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

  /** Output format for the date portion of the dateTime */
  private static final String SHORT_PATTERN = "yyyy-MM-dd'T'";

  /** The formatter used for converting the dateTime into a lexical form */
  private static final DateTimeFormatter MAIN_FORMATTER = DateTimeFormat.forPattern(LEXICAL_PATTERN);

  /** A supplemantary formatter for outputting the date, when the time has to be represented in non-canonical form */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(SHORT_PATTERN);

  /** The number of milliseconds in a second */
  private static final int MILLIS = 1000;

  /** The number of milliseconds in a minute */
  private static final int MILLIS_IN_MINUTE = MILLIS * 60;

  /** The number of milliseconds in an hour */
  private static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;

  /** The bit used to encode the localFlag */
  private static final byte LOCAL_BIT = 0x02;

  /** The bit used to encode the midnightFlag */
  private static final byte MIDNIGHT_BIT = 0x01;

  /** The mask for the timezone bits */
  private static final byte TZ_MASK = (byte)0xFC;

  /** The offset of the timezone data in an encoded buffer */
  private static final int TZ_OFFSET = SIZEOF_LONG;

  /** The offset of the fractional seconds decimal places in an encoded buffer */
  private static final int PLACES_OFFSET = TZ_OFFSET + 1;

  /** The milliseconds since the epoch */
  private final long millis;

  /** The hours offset for the time */
  private final int tzHours;

  /** The minutes offset for the timezone. A multiple of 15. */
  private final int tzMinutes;

  /** Indicates that the time was supplied as 24:00:00. */
  private final boolean midnight;

  /** The number of decimal places used to represent the milliseconds. No greater than 3. */
  private final byte milliPlaces;

  /** Indicates no supplied timezone. This defaults to the local timezone. */
  private final boolean localFlag;

  /** Indicates ZULU time, which is equivalent to +00:00. */
  private final boolean zuluFlag;

  /** A DateTime corresponding to this object. Only created if needed. */
  private DateTime cachedDateTime = null;

  /**
   * This constructor is used to set each field explicitly, when all such information is available.
   * No checking is performed on the consistency of the millisecond value, though some minimal testing is done on flags.
   * Whether tested or not, the following should hold:
   * <ul>
   * <li>if <code>isMidnight</code> is set, then <code>millis</code> must be a multiple of 24 hours.</li>
   * <li>if <code>isLocalTz</code> is set, then <code>tzHours</code> and <code>tzMinutes</code> should be 0,
   *     and <code>isZulu</code> must be false.</li>
   * <li>if <code>isZulu</code> is set, then <code>tzHours</code> and <code>tzMinutes</code> must be 0.</li>
   * <li><code>millis</code> % 10^(6 - milliPlaces) == 0</li>
   * </ul>
   * @param millis The milliseconds since the epoch.
   * @param tzHours The hour offset for the timezone.
   * @param tzMinutes The minute offset for the timezone.
   * @param isMidnight If the non-canonical form for midnight is used. "24:00:00"
   * @param milliPlaces The number of decimal places used for representing millisecds as fractions of a second.
   * @param isLocalTz Indicates no timezone information, so use the local default.
   * @param isZulu Indicates that the timezone is "Zulu". This is equivalent to 00:00 and is represented as "Z".
   * @throws IllegalArgumentException if the <code>isZulu</code> flag conflicts with the timezone values or the offsets.
   */
  public LexicalDateTime(long millis, int tzHours, int tzMinutes, boolean isMidnight, byte milliPlaces, boolean isLocalTz, boolean isZulu) {
    this.millis = millis;
    this.tzHours = tzHours;
    this.tzMinutes = tzMinutes;
    this.midnight = isMidnight;
    this.milliPlaces = milliPlaces;
    this.localFlag = isLocalTz;
    this.zuluFlag = isZulu;
    testTimezoneConsistency();
  }

  /**
   * Internal constructor used with a dateTime object, and parsed fields.
   * No checking is performed on the consistency of the millisecond value, though some minimal testing is done on flags.
   * Whether tested or not, the following should hold:
   * <ul>
   * <li>if <code>isMidnight</code> is set, then <code>millis</code> plus the timezone offset must be a multiple
   *     of 24 hours.</li>
   * <li>if <code>isLocalTz</code> is set, then <code>tzHours</code> and <code>tzMinutes</code> should be 0,
   *     and <code>isZulu</code> must be false.</li>
   * <li>if <code>isZulu</code> is set, then <code>tzHours</code> and <code>tzMinutes</code> must be 0.</li>
   * <li><code>millis</code> % 10^(3 - milliPlaces) == 0</li>
   * <li>The <code>tzHours</code> and <code>tzMinutes</code> values should correspond to the values in the
   *     <code>dateTime</code> field.</li>
   * </ul>
   * @param dateTime The dateTime object representing the time.
   * @param tzHours The hour offset for the timezone.
   * @param tzMinutes The minute offset for the timezone.
   * @param isMidnight If the non-canonical form for midnight is used. "24:00:00"
   * @param milliPlaces The number of decimal places used for representing millisecds as fractions of a second.
   * @param isLocalTz Indicates no timezone information, so use the local default.
   * @param isZulu Indicates that the timezone is "Zulu". This is equivalent to 00:00 and is represented as "Z".
   * @throws IllegalArgumentException if the <code>isZulu</code> flag conflicts with the timezone values or the offsets.
   */
  private LexicalDateTime(DateTime dateTime, int tzHours, int tzMinutes, boolean isMidnight, byte milliPlaces, boolean isLocalTz, boolean isZulu) {
    this.millis = dateTime.getMillis();
    this.tzHours = tzHours;
    this.tzMinutes = tzMinutes;
    this.midnight = isMidnight;
    this.cachedDateTime = dateTime;
    this.milliPlaces = milliPlaces;
    this.localFlag = isLocalTz;
    this.zuluFlag = isZulu;
    testTimezoneConsistency();
  }

  /**
   * Convenience constructor which allows easy construction of a LexicalDateTime using the milliseconds since the epoch.
   * @param millis Milliseconds since the epoch.
   */
  public LexicalDateTime(long millis) {
    this.millis = millis;
    long offset = DateTimeZone.getDefault().getOffset(0);
    tzHours = (int)(offset / MILLIS_IN_HOUR);
    tzMinutes = (int)(offset % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE;
    midnight = false;
    cachedDateTime = null;
    localFlag = true;
    zuluFlag = false;
    milliPlaces = minimumPlaces(millis);
  }

  /** Gets the number of milliseconds since the epoch. */
  public long getMillis() {
    return millis;
  }

  /** The the hour part of the offset for the timezone. */
  public int getTZHour() {
    return tzHours;
  }

  /** The the minute part of the offset for the timezone. */
  public long getTZMinute() {
    return tzMinutes;
  }

  /** Gets the flag that indicates that this time is a non-canonical form of midnight. */
  public boolean isMidnight() {
    return midnight;
  }

  /** Gets the flag that indicates no timezone is present, and the local default should be used. */
  public boolean isLocal() {
    return localFlag;
  }

  /** Gets the flag that indicates the Zulu timezone (UTC) and representation. */
  public boolean isZulu() {
    return zuluFlag;
  }

  /** Gets the number of decimal places to represent the fraction of a second. */
  public byte getDecimalPlaces() {
    return milliPlaces;
  }

  /** Get the size of buffer in bytes required to store this object */
  public static int requiredBufferSize() {
    return PLACES_OFFSET + 1;
  }

  /**
   * Fills in a ByteBuffer with the data required to encode this object.
   * @param bb The {@link java.nio.ByteBuffer} to populate.
   * @return The populated ByteBuffer.
   */
  public ByteBuffer encode(ByteBuffer bb) {
    assert bb.limit() > PLACES_OFFSET;
    bb.putLong(0, millis);
    bb.put(TZ_OFFSET, encodeTimezoneState());
    bb.put(PLACES_OFFSET, milliPlaces);
    return bb;
  }

  /**
   * Creates a byte code for the timezone and flags of this dateTime.
   * <table>
   * <tr><td>bits 7-2</td><td>timezone code</td></tr>
   * <tr><td >bit 1</td><td>local flag</td></tr>
   * <tr><td>bit 0</td><td>midnight flag</td></tr>
   * </table>
   * @return a byte containing the timezone data.
   */
  public byte encodeTimezoneState() {
    byte result = 0;
    if (zuluFlag) result = Timezone.getZuluCode();
    else result = new Timezone(tzHours, tzMinutes).getCode();
    if (localFlag) result |= LOCAL_BIT;
    if (midnight) result |= MIDNIGHT_BIT;
    return result;
  }

  /**
   * Decodes a {@link ByteBuffer} into a LexicalDateTime.
   * @param bb The ByteBuffer to decode.
   * @return a new LexicalDateTime structure.
   */
  public static LexicalDateTime decode(ByteBuffer bb) {
    assert bb.limit() > PLACES_OFFSET;
    return decode(bb.getLong(0), bb.get(TZ_OFFSET), bb.get(PLACES_OFFSET));
  }

  /**
   * Decodes a millisecond value and an encoded byte into a timezone and flags.
   * @param millis The milliseconds since the epoch.
   * @param timezoneState The encoded data representing the timezone.
   * @param places The number of decimal places for the seconds representation.
   * @return a new LexicalDateTime structure.
   */
  public static LexicalDateTime decode(long millis, byte timezoneState, byte places) {
    boolean local = (timezoneState & LOCAL_BIT) != 0;
    boolean midnight = (timezoneState & MIDNIGHT_BIT) != 0;
    byte tzCode = (byte)(timezoneState & TZ_MASK);
    boolean zulu = (tzCode == Timezone.getZuluCode());
    Timezone tz = new Timezone(tzCode);
    return new LexicalDateTime(millis, tz.getHour(), tz.getMinute(), midnight, places, local, zulu);
  }

  /** Return a lexical representation of this dateTime. */
  public String toString() {
    if (cachedDateTime == null) {
      DateTimeZone dtz;
      dtz = (localFlag) ? null : DateTimeZone.forOffsetHoursMinutes(tzHours, tzMinutes);
      cachedDateTime = new DateTime(millis, dtz);
    }
    StringBuilder result;
    if (!midnight) {
      result = new StringBuilder(MAIN_FORMATTER.print(cachedDateTime));
      if (milliPlaces > 0) {
        result.append(MILLI_SEPARATOR_STR);
        int place = MILLIS;
        long fraction = millis;
        if (fraction < 0) fraction = fraction % place + place;
        for (int m = 0; m < milliPlaces; m++) {
          fraction = fraction % place;
          place /= 10;
          result.append(fraction / place);
        }
      }
    } else {
      result = new StringBuilder(DATE_FORMATTER.print(cachedDateTime.plusDays(-1)));
      result.append(MIDNIGHT_STR);
      if (milliPlaces > 0) {
        result.append(MILLI_SEPARATOR_STR);
        for (int i = 0; i < milliPlaces; i++) result.append("0");
      }
    }
    if (!localFlag) {
      if (zuluFlag) result.append(ZULU_STR);
      else result.append(String.format("%+03d:%02d", tzHours, tzMinutes));
    }
    return result.toString();
  }

  /**
   * Parse a dateTime string. It <strong>must</strong> be of the form:
   * ('-')? yyyy '-' MM '-' dd 'T' hh ':' mm ':' ss ( '.' s+ )? ( ( ('+'|'-')? hh ':' mm ) | 'Z' )?
   * @param dt The dateTime string to parse.
   * @return a new LexcalDateTime value.
   * @throws ParseException If a character that doesn't match the above pattern is discovered.
   */
  public static LexicalDateTime parseDateTime(String dt) throws ParseException {
    int pos = 0;
    try {
      boolean negative = dt.charAt(pos) == '-';
      if (negative) pos++;
      int year = d(dt, pos++) * 1000 + d(dt, pos++) * 100 + d(dt, pos++) * 10 + d(dt, pos++);
      while (dt.charAt(pos) != DATE_SEPARATOR) year = year * 10 + d(dt, pos++);
      if (negative) year = -year;
      if (dt.charAt(pos++) != DATE_SEPARATOR) throw new ParseException(BAD_FORMAT + "date: " + dt, pos - 1);
      int month = d(dt, pos++) * 10 + d(dt, pos++);
      if (dt.charAt(pos++) != DATE_SEPARATOR) throw new ParseException(BAD_FORMAT + "date: " + dt, pos - 1);
      int day = d(dt, pos++) * 10 + d(dt, pos++);
  
      if (dt.charAt(pos++) != DATE_TIME_SEPARATOR) throw new ParseException(BAD_FORMAT + "date/time: " + dt, pos - 1);
  
      int hour = d(dt, pos++) * 10 + d(dt, pos++);
      if (dt.charAt(pos++) != TIME_SEPARATOR) throw new ParseException(BAD_FORMAT + "time: " + dt, pos - 1);
      int minute = d(dt, pos++) * 10 + d(dt, pos++);
      if (dt.charAt(pos++) != TIME_SEPARATOR) throw new ParseException(BAD_FORMAT + "time: " + dt, pos - 1);
      int second = d(dt, pos++) * 10 + d(dt, pos++);
  
      int millisecs = 0;
      byte milliPlaces = 0;
      int lastPos = dt.length() - 1;
      if (pos < lastPos) {
        if (dt.charAt(pos) == MILLI_SEPARATOR) {
          int place = MILLIS / 10;
          int digit;
          while (isDecimal((digit = dt.charAt(++pos) - '0'))) {
            millisecs += digit * place;
            if (milliPlaces++ > 3) throw new ParseException(BAD_FORMAT + "milliseconds: " + dt, pos);
            place /= 10;
            if (pos == lastPos) {
              pos++;
              break;
            }
          }
        }
      }
  
      boolean midnightFlag = false;
      if (hour == MIDNIGHT) {
        midnightFlag = true;
        hour = 0;
      }
      if (midnightFlag && (minute > 0 || second > 0 || millisecs > 0)) throw new ParseException(BAD_FORMAT + "time: " + dt, pos);
  
      boolean local = false;
      int tzHour = 0;
      int tzMinute = 0;
      boolean zuluFlag = false;
      DateTimeZone timezone = null;
      if (pos <= lastPos) {
        char tz = dt.charAt(pos++);
        if (tz == ZULU) {
          if (pos != lastPos + 1) throw new ParseException(BAD_FORMAT + "timezone: " + dt, pos);
          timezone = UTC;
          zuluFlag = true;
        } else {
          if (pos != lastPos - 4 || (tz != NEG_TZ && tz != POS_TZ)) throw new ParseException(BAD_FORMAT + "timezone: " + dt, pos);
          tzHour = d(dt, pos++) * 10 + d(dt, pos++);
          if (dt.charAt(pos++) != TIME_SEPARATOR) throw new ParseException(BAD_FORMAT + "timezone: " + dt, pos - 1);
          tzMinute = d(dt, pos++) * 10 + d(dt, pos++);
          if (tz == NEG_TZ) tzHour = -tzHour;
          timezone = DateTimeZone.forOffsetHoursMinutes(tzHour, tzMinute);
        }
      } else {
        local = true;
      }
  
      DateTime dateTime = new DateTime(year, month, day, hour, minute, second, millisecs, timezone);
      if (midnightFlag) dateTime = dateTime.plusDays(1);
      return new LexicalDateTime(dateTime, tzHour, tzMinute, midnightFlag, milliPlaces, local, zuluFlag);
    } catch (StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException(BAD_FORMAT + "date: " + dt);
    }
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (!(o instanceof LexicalDateTime)) return false;
    LexicalDateTime other = (LexicalDateTime)o;
    return millis == other.millis && tzHours == other.tzHours && tzMinutes == other.tzMinutes
        && milliPlaces == other.milliPlaces && localFlag == other.localFlag
        && zuluFlag == other.zuluFlag && midnight == other.midnight;
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return Long.valueOf(millis).hashCode() + encodeTimezoneState() * 13;
  }

  /**
   * Check that the timezone flags are consistent with one another.
   * @throws IllegalArgumentException if the is an inconsistency in the timezone values.
   */
  private void testTimezoneConsistency() {
    if (zuluFlag) {
      if (localFlag) throw new IllegalArgumentException("Cannot have Zulu time and a \"default\" timezone");
      if (tzHours != 0 || tzMinutes != 0) throw new IllegalArgumentException("Cannot have Zulu time and a timezone offset");
    }
    assert (millis % (int)Math.pow(10, 3 - milliPlaces)) == 0;
  }

  /**
   * Extract a single decimal digit from a string.
   * @param str The string to get the digit from.
   * @param i The location in the string to extract the digit from
   * @return The extracted digit.
   * @throws ParseException If the character to be extracted is not a decimal digit.
   */
  private static int d(String str, int i) throws ParseException {
    int d = str.charAt(i) - '0';
    if (d >= 10 || d < 0) throw new ParseException("Unexpected character: " + Character.toString(str.charAt(i)) + ". Expected numeric digit.", i);
    return d;
  }

  /**
   * Tests if a number represents a single decimal digit.
   * @param i The number to test.
   * @return <code>true</code> if the number represents a single decimal digit.
   */
  private static boolean isDecimal(int i) {
    return i < 10 && i >= 0;
  }

  /**
   * Determine the minimum number of decimal places required to represent
   * a millisecond value in seconds.
   * @param mSec The number of milliseconds to represent.
   * @return The minimum number of decimal places needed when representing mSec in seconds.
   *         This result is always in the range 0-3.
   */
  private static byte minimumPlaces(long mSec) {
    byte p = 3;
    int precision = 1;
    for (; p > 0; p--) {
      precision *= 10;
      if (mSec % precision != 0) break;
    }
    return p;
  }
}
