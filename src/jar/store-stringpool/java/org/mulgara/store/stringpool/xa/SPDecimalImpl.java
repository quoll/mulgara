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
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;


/**
 * An SPTypedLiteral that represents xsd:decimal literals.
 * Data is encoded as a string or a long. If the data is the length of a long, then
 * it is a long. If it is one more, then it could be a normal string, or a string
 * that was padded to not look like a long.
 *
 * @created 2004-10-05
 * @author David Makepeace
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class SPDecimalImpl extends AbstractSPTypedLiteral implements SPNumber {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDecimalImpl.class);

  /** An ID used for all Decimal types */
  static final int TYPE_ID = 2; // Unique ID

  /** The offset into the byte buffer to find out the encoding used for this data. */
  static final int END_IDX = Constants.SIZEOF_LONG;

  /**
   * Common abstract constructor for all decimal types.
   * @param subtypeId The subtype, this is the ID for either the full or abbreviated URI.
   * @param typeURI The full URI for the data type.
   */
  SPDecimalImpl(int subtypeId, URI typeURI) {
    super(TYPE_ID, subtypeId, typeURI);
  }


  /**
   * Reads a byte buffer and converts to a BigDecimal, regardless of formatting.
   * @param bb The byte buffer to decode.
   * @return A BigDecimal representing the stored number.
   */
  static BigDecimal decode(ByteBuffer bb) {
    ByteBuffer number = bb;
    int limit = bb.limit();
    if (limit == Constants.SIZEOF_LONG) return BigDecimal.valueOf(bb.getLong());
    if (limit == END_IDX + 1) {
      // is this buffer padded?
      byte terminator = bb.get(END_IDX);
      if (terminator == SPDecimalBaseImpl.END_BYTE) {
        // remove the padding
        bb.limit(END_IDX);
        number = bb.slice();
      }
    }
    return new BigDecimal(CHARSET.decode(number).toString());
  }

}


/**
 * This class represents xsd:decimal.
 * The data format is as a string. If the string is exactly the length of a Long
 * (the other format available) then it will be incremented by 1, and a -1 byte appended.
 */
class SPDecimalBaseImpl extends SPDecimalImpl {

  /**
   * The terminating byte, indicating the type of this buffer. Only needed if the buffer
   * is the same length as a long.
   */
  static final byte END_BYTE = -1;

  /** The value of the data. */
  final BigDecimal val;

  /** The string representation of the data. */
  final String lexical;

  /**
   * Creates an xsd:decimal out of a long.
   * @param subtypeId The ID for either the full or abbreviated xsd:decimal.
   * @param typeURI The full or abbreviated URI for xsd:decimal.
   * @param l The long value to store.
   */
  SPDecimalBaseImpl(int subtypeId, URI typeURI, long l) {
    super(subtypeId, typeURI);
    lexical = Long.toString(l);
    val = new BigDecimal(l);
  }


  /**
   * Creates an xsd:decimal out of a BigDecimal.
   * @param subtypeId The ID for either the full or abbreviated xsd:decimal.
   * @param typeURI The full or abbreviated URI for xsd:decimal.
   * @param bd The BigDecimal value to store.
   */
  SPDecimalBaseImpl(int subtypeId, URI typeURI, BigDecimal bd) {
    super(subtypeId, typeURI);
    lexical = bd.toPlainString();
    val = bd;
  }


  /**
   * Creates an xsd:decimal by decoding from a data buffer.
   * @param subtypeId The ID for either the full or abbreviated xsd:decimal.
   * @param typeURI The full or abbreviated URI for xsd:decimal.
   * @param data The data containing the xsd:decimal value.
   */
  SPDecimalBaseImpl(int subtypeId, URI typeURI, ByteBuffer data) {
    super(subtypeId, typeURI);
    if (data.limit() == Constants.SIZEOF_LONG) throw new IllegalArgumentException("Buffer does not hold a decimal.");
    ByteBuffer number = data;
    if (data.limit() == END_IDX + 1) {
      if (data.get(END_IDX) == END_BYTE) {
        data.limit(END_IDX);
        number = data.slice();
      }
    }
    lexical = CHARSET.decode(number).toString();
    val = new BigDecimal(lexical);
  }


  /**
   * Creates an xsd:decimal by decoding from a string.
   * @param subtypeId The ID for either the full or abbreviated xsd:decimal.
   * @param typeURI The full or abbreviated URI for xsd:decimal.
   * @param lexicalForm The string containing the xsd:decimal value.
   */
  SPDecimalBaseImpl(int subtypeId, URI typeURI, String lexicalForm) {
    super(subtypeId, typeURI);
    this.lexical = lexicalForm;
    val = new BigDecimal(lexicalForm);
  }


  /** @see org.mulgara.store.stringpool.SPObject#getData() */
  public ByteBuffer getData() {
    ByteBuffer data = CHARSET.encode(lexical);
    // if this is the same size as a long, expand it and pad it
    if (data.limit() == END_IDX) {
      ByteBuffer newData = ByteBuffer.allocate(END_IDX + 1);
      newData.put(data);
      newData.put(END_IDX, END_BYTE);
      newData.rewind();
      data = newData;
    }
    return data;
  }


  /** @see org.mulgara.store.stringpool.SPObject#getSPComparator() */
  public SPComparator getSPComparator() {
    return SPDecimalBaseComparator.getInstance();
  }


  /** @see org.mulgara.store.stringpool.SPObject#getLexicalForm() */
  public String getLexicalForm() {
    return lexical;
  }


  /** @see org.mulgara.store.stringpool.AbstractSPTypedLiteral#compareTo(org.mulgara.store.stringpool.SPObject) */
  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the longs.
    if (o instanceof SPDecimalExtImpl) {
      long ol = ((SPDecimalExtImpl)o).l;
      return val.compareTo(BigDecimal.valueOf(ol));
    }
    SPDecimalBaseImpl di = (SPDecimalBaseImpl)o;
    return val.compareTo(di.val);
  }


  /**
   * Indicates if this object is a number.
   * @return <code>true</code> since this object is a number.
   */
  public boolean isNumber() {
    return true;
  }


  /**
   * @see org.mulgara.store.stringpool.AbstractSPObject#numericalCompare(org.mulgara.store.stringpool.SPObject)
   */
  public int numericalCompare(SPObject o) {
    if (!o.isNumber()) return compareTo(o);
    return -((SPNumber)o).numericalCompareTo(val);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(java.math.BigDecimal)
   */
  public int numericalCompareTo(BigDecimal n) {
    return val.compareTo(n);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(double)
   */
  public int numericalCompareTo(double d) {
    return val.compareTo(new BigDecimal(d));
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(long)
   */
  public int numericalCompareTo(long l) {
    return val.compareTo(new BigDecimal(l));
  }


  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return lexical.hashCode();
  }


  /** @see java.lang.Object#equals(java.lang.Object) */
  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      SPDecimalBaseImpl di = (SPDecimalBaseImpl)obj;
      return lexical.equals(di.lexical);
    } catch (ClassCastException ex) {
      // obj was not an SPDecimalImpl.
      return false;
    }
  }

  /** Compares the binary representations of two SPDecimalBaseImpl objects. */
  public static class SPDecimalBaseComparator implements SPComparator {

    /** The singleton instance of this class. */
    private static final SPDecimalBaseComparator INSTANCE = new SPDecimalBaseComparator();

    /**
     * @return The singleton instance of this class.
     */
    public static SPDecimalBaseComparator getInstance() {
      return INSTANCE;
    }

    /**
     * @see org.mulgara.store.stringpool.SPComparator#comparePrefix(java.nio.ByteBuffer, java.nio.ByteBuffer, int)
     * @return Always 0, since this cannot compare on prefixes alone.
     */
    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    /**
     * This comparator WILL compare between xsd:decimal and the extending types
     * @see org.mulgara.store.stringpool.SPComparator#compare(ByteBuffer, int, ByteBuffer, int)
     */
    public int compare(ByteBuffer d1, int subtypeId1, ByteBuffer d2, int subtypeId2) {
      int c = decode(d1).compareTo(decode(d2));
      if (c == 0) c = AbstractSPObject.compare(subtypeId1, subtypeId2);
      return c;
    }

  }

}


/**
 * This class represents extensions of xsd:decimal.
 * The data format is as a long, followed by a byte marker set to 0. This is to
 * distinguish the data format from xsd:decimal, which is stored as a string.
 */
class SPDecimalExtImpl extends SPDecimalImpl {

  /** The long value containing the number. */
  final long l;

  /**
   * Creates an xsd:decimal extension out of a long.
   * @param subtypeId The ID for either the full or abbreviated URI.
   * @param typeURI The full or abbreviated URI.
   * @param l The long value to store.
   */
  SPDecimalExtImpl(int subtypeId, URI typeURI, long l) {
    super(subtypeId, typeURI);
    this.l = l;
  }


  /**
   * Creates an xsd:decimal extension out of a buffer.
   * @param subtypeId The ID for either the full or abbreviated URI.
   * @param typeURI The full or abbreviated URI.
   * @param data The buffer containing the data encoding the value.
   */
  SPDecimalExtImpl(int subtypeId, URI typeURI, ByteBuffer data) {
    super(subtypeId, typeURI);
    assert isLong(data);
    l = data.getLong();
  }


  /**
   * Creates an xsd:decimal extension out of a string.
   * @param subtypeId The ID for either the full or abbreviated URI.
   * @param typeURI The full or abbreviated URI.
   * @param lexicalForm The string containing the value.
   */
  SPDecimalExtImpl(int subtypeId, URI typeURI, String lexicalForm) {
    super(subtypeId, typeURI);
    if (lexicalForm.startsWith("+")) lexicalForm = lexicalForm.substring(1);
    l = Long.valueOf(lexicalForm);
  }


  /** @see org.mulgara.store.stringpool.SPObject#getData() */
  public ByteBuffer getData() {
    ByteBuffer data = ByteBuffer.allocate(Constants.SIZEOF_LONG);
    data.putLong(l);
    data.flip();
    return data;
  }


  /** @see org.mulgara.store.stringpool.SPObject#getSPComparator() */
  public SPComparator getSPComparator() {
    return SPDecimalExtComparator.getInstance();
  }


  /** @see org.mulgara.store.stringpool.SPObject#getLexicalForm() */
  public String getLexicalForm() {
    return Long.toString(l);
  }


  /** @see org.mulgara.store.stringpool.AbstractSPTypedLiteral#compareTo(org.mulgara.store.stringpool.SPObject) */
  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // The super will have returned a value already, but just in case we need to compare
    // values between different types, we convert.
    if (o instanceof SPDecimalBaseImpl) {
      return BigDecimal.valueOf(l).compareTo(((SPDecimalBaseImpl)o).val);
    }
    // if this is an unparseable value, then hard code it to the bottom of the type ordering
    if (o instanceof UnknownSPTypedLiteralImpl) return -1;

    // Compare the longs.
    SPDecimalExtImpl di = (SPDecimalExtImpl)o;
    return compare(l, di.l);
  }


  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return (int)(l * 7) | (int)(l >> 32) ^ subtypeId;
  }


  /** @see java.lang.Object#equals(java.lang.Object) */
  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      SPDecimalExtImpl di = (SPDecimalExtImpl)obj;
      return l == di.l && subtypeId == di.subtypeId;
    } catch (ClassCastException ex) {
      // obj was not an SPDecimalExtImpl.
      return false;
    }
  }


  /**
   * Tests a buffer to see if if contains a long value. It must be the correct length
   * and it must have the correct byte in the last place.
   * @param d The buffer to test.
   * @return <code>true</code> if the buffer contains a long value.
   */
  static final boolean isLong(ByteBuffer d) {
    return d.limit() == Constants.SIZEOF_LONG;
  }

  /** Compares the binary representations of two SPDecimalExtImpl objects. */
  public static class SPDecimalExtComparator implements SPComparator {

    /** The singleton instance of this object. */
    private static final SPDecimalExtComparator INSTANCE = new SPDecimalExtComparator();

    /** @return The singleton instance of this object. */
    public static SPDecimalExtComparator getInstance() {
      return INSTANCE;
    }

    /**
     * @return Always 0, since all data is needed for comparing on this class.
     * @see org.mulgara.store.stringpool.SPComparator#comparePrefix(java.nio.ByteBuffer, java.nio.ByteBuffer, int)
     */
    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    /**
     * This comparator WILL compare between xsd:decimal and the extending types
     * @see org.mulgara.store.stringpool.SPComparator#compare(ByteBuffer, int, ByteBuffer, int)
     */
    public int compare(ByteBuffer d1, int subtypeId1, ByteBuffer d2, int subtypeId2) {
      int c;
      if (isLong(d1) && isLong(d2)) {
        c = SPDecimalExtImpl.compare(d1.getLong(), d2.getLong());
      } else {
        c = decode(d1).compareTo(decode(d2));
      }
      if (c == 0) c = AbstractSPObject.compare(subtypeId1, subtypeId2);
      return c;
    }

  }


  /**
   * Indicates if this object is a number.
   * @return <code>true</code> since this object is a number.
   */
  public boolean isNumber() {
    return true;
  }


  /**
   * @see org.mulgara.store.stringpool.AbstractSPObject#numericalCompare(org.mulgara.store.stringpool.SPObject)
   */
  public int numericalCompare(SPObject o) {
    if (!o.isNumber()) return compareTo(o);
    return -((SPNumber)o).numericalCompareTo(l);
  }


  public int numericalCompareTo(BigDecimal n) {
    return new BigDecimal(l).compareTo(n);
  }


  public int numericalCompareTo(double d) {
    return Double.compare((double)l, d);
  }


  public int numericalCompareTo(long l) {
    return this.l < l ? -1 : (this.l > l ? 1 : 0);
  }

}
