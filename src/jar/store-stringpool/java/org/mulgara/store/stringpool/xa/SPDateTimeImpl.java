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
import java.text.ParseException;
import java.util.Date;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.LexicalDateTime;
import static org.mulgara.util.Constants.SIZEOF_LONG;


/**
 * An SPObject that represents XSD dateTime values.
 *
 * @created 2004-09-27
 *
 * @author David Makepeace
 * @author Edwin Shin
 * @author Paul Gearon
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPDateTimeImpl extends AbstractSPTypedLiteral {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDateTimeImpl.class);

  static final int TYPE_ID = 6; // Unique ID

  static final URI TYPE_URI = XSD.DATE_TIME_URI;

  private LexicalDateTime dateTime;


  /**
   * Creates a dateTime using a LexicalDateTime.
   * @param dateTime A {@link java.util.Date} representing the dateTime.
   */
  SPDateTimeImpl(LexicalDateTime dateTime) {
    super(TYPE_ID, TYPE_URI);
    if (dateTime == null) throw new IllegalArgumentException("Null \"dateTime\" parameter");
    this.dateTime = dateTime;
  }


  /**
   * Creates a dateTime in the default timezone for this system. Not recommended.
   * @param date A {@link java.util.Date} representing the dateTime.
   */
  SPDateTimeImpl(Date date) {
    super(TYPE_ID, TYPE_URI);
    if (date == null) throw new IllegalArgumentException("Null \"date\" parameter");
    this.dateTime = new LexicalDateTime(date.getTime());
  }


  /**
   * Creates a dateTime from encoded data in a buffer.
   * @param data The encoded data.
   */
  SPDateTimeImpl(ByteBuffer data) {
    super(TYPE_ID, TYPE_URI);
    if (data.limit() == SIZEOF_LONG)    // backwards compat with <= 1.x
      this.dateTime = new LexicalDateTime(data.getLong(0));
    else
      this.dateTime = LexicalDateTime.decode(data);
  }


  /**
   * Creates a dateTime in the default timezone for this system. Not recommended.
   * @param l The number of milliseconds since the epoch.
   */
  SPDateTimeImpl(long l) {
    super(TYPE_ID, TYPE_URI);
    this.dateTime = new LexicalDateTime(l);
  }


  static SPDateTimeImpl newInstance(String lexicalForm) {
    try {
      return new SPDateTimeImpl(LexicalDateTime.parseDateTime(lexicalForm));
    } catch (ParseException ex) {
      throw new IllegalArgumentException("Cannot parse date: " + lexicalForm);
    }
  }

  /* from SPObject interface. */

  public ByteBuffer getData() {
    ByteBuffer data = ByteBuffer.allocate(LexicalDateTime.requiredBufferSize());
    dateTime.encode(data);
    return data;
  }

  public SPComparator getSPComparator() {
    return SPDateTimeComparator.getInstance();
  }

  /**
   * Returns the lexical form of the XSD dateTime value, using the original form of the
   * lexical value.
   * @return the lexical form of the XSD dateTime value
   */
  public String getLexicalForm() {
    return dateTime.toString();
  }

  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the Dates.
    long a = dateTime.getMillis();
    long b = (((SPDateTimeImpl)o).dateTime).getMillis();
    return a == b ? 0 : (a < b ? -1 : 1);
  }


  /* from Object. */

  public int hashCode() {
    return dateTime.hashCode();
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      return dateTime.equals(((SPDateTimeImpl)obj).dateTime);
    } catch (ClassCastException ex) {
      // obj was not an SPDateTimeImpl.
      return false;
    }
  }


  /**
   * Compares the binary representations of two SPDateTimeImpl objects.
   * Performs comparisons on the canonical representations, so 2 different
   * values can compare the same, so long as they refer to the same time.
   */
  public static class SPDateTimeComparator implements SPComparator {

    private static final SPDateTimeComparator INSTANCE = new SPDateTimeComparator();

    public static SPDateTimeComparator getInstance() {
      return INSTANCE;
    }

    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
      return AbstractSPObject.compare(d1.getLong(), d2.getLong());
    }

  }

}
