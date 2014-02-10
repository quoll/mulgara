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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Maps from a known hour:minute offset for a timezone into a 6 bit code, and back.
 *
 * @created Jun 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Timezone {

  /** A mask for restricting bit patterns to a byte (removing sign extension) */
  private static final int BYTE_MASK = 0xFF;

  /** The name of the timezone for Zulu time 00:00 */
  private static final String ZULU_NAME = "UTC";

  /** The construct that holds the offset details */
  private HourMinute hm;

  /** The internal code for the timezone offset. */
  private byte internalCode;

  /**
   * Constructs a Timezone using a code.
   * @param code The code for a known timezone.
   * @throws IllegalArgumentException The code does not correspond to a timezone in the database.
   */
  public Timezone(byte code) {
    if ((code & 3) != 0) throw new IllegalArgumentException("Invalid bit pattern in the timezone code");
    internalCode = (byte)((code & BYTE_MASK) >> 2);
    if (internalCode > timezoneList.length || internalCode < 0) throw new IllegalArgumentException("Unknown timezone code: " + code);
    hm = (code == ZULU_CODE) ? ZULU : timezoneList[internalCode];
  }

  /**
   * Constructs a Timezone using an hour:minute offset.
   * @param hour The hour offset of the timezone. This cannot encode ZULU time.
   * @param minute The minute offset of the timezone.
   * @throws IllegalArgumentException The timezone is not in the database of known timezones.
   */
  public Timezone(int hour, int minute) {
    hm = new HourMinute(hour, minute);
    Byte c = tzCodes.get(hm);
    if (c == null) throw new IllegalArgumentException("Timezone is not in official database: " + hm);
    internalCode = c;
  }

  /**
   * A convenience factory method to construct a ZULU timezone.
   * @return a Timezone for ZULU time.
   */
  public static Timezone newZuluTimezone() {
    return new Timezone(ZULU_CODE);
  }

  /** Gets the hour for this timezone. */
  public int getHour() {
    return hm.hour;
  }

  /** Gets the minute for this timezone. */
  public int getMinute() {
    return hm.minute;
  }

  /** Gets the code for this timezone. This is guaranteed to use the top 5 bits, and set the bottom 2 to 0.*/
  public byte getCode() {
    return (byte)(internalCode << 2);
  }

  /** Indicates if this code represents Zulu. */
  public boolean isZulu() {
    return getCode() == ZULU_CODE;
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    return hm == ZULU ? "Z" : hm.toString();
  }

  /** Convert to a {@linkplain java.util.TimeZone} */
  public TimeZone asJavaTimeZone() {
    TimeZone tz = TimeZone.getTimeZone(ZULU_NAME);
    tz.setRawOffset(hm.getOffset());
    return tz;
  }

  /** Gets the code for the ZULU timezone. */
  public static byte getZuluCode() {
    return ZULU_CODE;
  }

  /** The ZULU timezone */
  private static final HourMinute ZULU = new HourMinute(0, 0);

  /** The database of known timezones. @see http://en.wikipedia.org/wiki/List_of_zoneinfo_timezones */
  private static final HourMinute[] timezoneList = new HourMinute[] {
    new HourMinute(-12, 0), new HourMinute(-11, 0), new HourMinute(-10, 0), new HourMinute(-9, 30),
    new HourMinute(-9, 0), new HourMinute(-8, 0), new HourMinute(-7, 0), new HourMinute(-6, 0),
    new HourMinute(-5, 0), new HourMinute(-4, 30), new HourMinute(-4, 0), new HourMinute(-3, 30),
    new HourMinute(-3, 0), new HourMinute(-2, 0), new HourMinute(-1, 0), ZULU,
    new HourMinute(1, 0), new HourMinute(2, 0), new HourMinute(3, 0), new HourMinute(3, 30),
    new HourMinute(4, 0), new HourMinute(4, 30), new HourMinute(5, 0), new HourMinute(5, 30),
    new HourMinute(5, 45), new HourMinute(6, 0), new HourMinute(6, 30), new HourMinute(7, 0),
    new HourMinute(8, 0), new HourMinute(8, 45), new HourMinute(9, 0), new HourMinute(9, 30),
    new HourMinute(10, 0), new HourMinute(10, 30), new HourMinute(11, 0), new HourMinute(11, 30),
    new HourMinute(12, 0), new HourMinute(12, 45), new HourMinute(13, 0), new HourMinute(14, 0)
  };

  /** A map of timezones to their code. */
  private static final Map<HourMinute,Byte> tzCodes;

  /** A special code for ZULU, to distinguish it from 00:00. This is an external code, so the lowest 2 bits must be 0. */
  public static final byte ZULU_CODE = (byte)(timezoneList.length << 2);

  // populates the tzCodes with the timezoneList
  static {
    Map<HourMinute,Byte> writeMap = new HashMap<HourMinute,Byte>();
    for (byte t = 0; t < timezoneList.length; t++) writeMap.put(timezoneList[t], t);
    tzCodes = Collections.unmodifiableMap(writeMap);
  }

  /**
   * A private structure for associating an hour and minute together.
   */
  private static class HourMinute {

    /** Number of milliseconds in a minute */
    private static final int MINUTE_MILLIS = 60 * 1000;

    /** Number of milliseconds in an hour */
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;

    /** The hour value. */
    public final int hour;

    /** The minute value. */
    public final int minute;

    /** Constructs an hour and minute tuple */
    public HourMinute(int h, int m) {
      hour = h;
      minute = m;
    }

    /** Calculates a millisecond offset for this hour/minute pair */
    public int getOffset() {
      return hour * HOUR_MILLIS + minute * MINUTE_MILLIS;
    }

    /** @inheritDoc */
    public int hashCode() {
      return (hour * 4) + (minute / 15);
    }

    /** @inheritDoc */
    public boolean equals(Object o) {
      return (o instanceof HourMinute) && ((HourMinute)o).hour == hour && ((HourMinute)o).minute == minute;
    }

    /** @inheritDoc */
    public String toString() {
      if (hour == 0 && minute == 0) return "+00:00";
      return String.format("%+03d:%02d", hour, minute);
    }
  }

}
