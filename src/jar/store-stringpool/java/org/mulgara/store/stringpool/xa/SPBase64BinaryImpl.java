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

//Java 2 standard packages
import java.nio.ByteBuffer;
import java.net.URI;

//apache packages
import org.apache.log4j.*;

//mulgara packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.AbstractSPTypedLiteral;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPTypedLiteral;


/**
 * Class that represents a XSD base64Binary primitive datatype.
 *
 * @created 2004-10-04
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPBase64BinaryImpl
    extends AbstractSPTypedLiteral
    implements SPTypedLiteral {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SPBase64BinaryImpl.class);

  /** Type code that identifies this type */
  static final int TYPE_ID = 13;

  /** the xsd:base64Binary URI */
  static final URI TYPE_URI = XSD.BASE64_BINARY_URI;

  /** The base64 binary data as bytes */
  private ByteBuffer data = null;

  /**
   * Constructor.
   *
   * @param lexicalForm String must contain values [0-9a-zA-Z+/=]
   */
  public SPBase64BinaryImpl(String lexicalForm) {

    super(TYPE_ID, TYPE_URI);

    //validate
    if (lexicalForm == null) {

      throw new IllegalArgumentException("'lexicalForm' cannot be null.");
    }

    //create a buffer and copy contents of the lexical string
    data = ByteBuffer.allocate(lexicalForm.length());
    char current = 0;
    byte value = 0;

    //convert each character to a base64 byte value
    //2 bits are wasted for each byte, but it is faster without the compression
    for (int i = 0; i < lexicalForm.length(); i++) {

      current = lexicalForm.charAt(i);

      //ignore whitespace characters
      if (!Character.isWhitespace(current)) {
        value = (byte) (base64Value(current) & 0xFF);
        data.put(value);
      }
    }
    data.flip();
  }

  /**
   * Constructor.
   *
   * @param buffer ByteBuffer
   */
  public SPBase64BinaryImpl(ByteBuffer buffer) {

    super(TYPE_ID, TYPE_URI);

    data = buffer;
  }

  /**
   * Converts a base64 character [0-9a-zA-Z+/=] to an int.
   *
   * @param base64Char char
   * @return int
   */
  public int base64Value(char base64Char) {

    //invalid default value
    int value = -1;

    if (Character.isDigit(base64Char)) {
      //[0-9] (0 == 52, 9 == 61)
      value = base64Char - '0' + 52;
    }
    else if ( (base64Char >= 'A')
             && (base64Char <= 'Z')) {
      //[A-Z] (A == 0, Z == 25)
      value = base64Char - 'A';
    }
    else if ( (base64Char >= 'a')
             && (base64Char <= 'z')) {
      //[a-z] (a == 26, z == 51)
      value = base64Char - 'a' + 26;
    }
    else if (base64Char == '+') {
      //(+ == 62)
      value = 62;
    }
    else if (base64Char == '/') {
      //(/ == 63)
      value = 63;
    }
    else if (base64Char == '=') {
      //(= == 64)
      value = 64;
    }

    //validate
    if ( (value < 0)
        || (value > 64)) {

      throw new IllegalArgumentException("'base64Char' (" + base64Char +
                                         ") is not a valid base 64 character " +
                                         "([0-9a-zA-Z+/=]).");
    }

    return value;
  }

  /**
   * Converts the value into a base64 character. Value must be between 0 and 64
   *
   * @param value int
   * @return char
   */
  public char base64Char(int value) {

    char base64Char = '0';

    //validate
    if ( (value < 0)
        || (value > 64)) {

      throw new IllegalArgumentException("'value' (" + value + ") must be " +
                                         "between 0 and 64.");
    }

    if (value <= 25) {
      //[0-25] (A == 0, Z == 25)
      base64Char = (char) ('A' + value);
    }
    else if (value <= 51) {
      //[26-51] (a == 26, z == 51)
      base64Char = (char) ('a' + (value - 26));
    }
    else if (value <= 61) {
      //[52-61] (0 == 52, 9 == 61)
      base64Char = (char) ('0' + (value - 52));
    }
    else if (value == 62) {
      //(+ == 62)
      base64Char = '+';
    }
    else if (value == 63) {
      //(/ == 63)
      base64Char = '/';
    }
    else if (value == 64) {
      //(= == 64)
      base64Char = '=';
    }

    return base64Char;
  }

  /**
   * Returns a read-only ByteBuffer containing the base64Binary data
   *
   * @return ByteBuffer
   */
  public ByteBuffer getData() {
    return data.asReadOnlyBuffer();
  }

  /**
   * Converts the data bytes into a String.
   *
   * @return String
   */
  public String getLexicalForm() {

    StringBuffer lexical = new StringBuffer();
    ByteBuffer buffer = getData();
    buffer.position(0);

    //convert each byte value to it's base64 character and add
    byte current = 0;
    char character = 0;
    int charCount = 0;

    while (buffer.hasRemaining()) {

      current = buffer.get();
      character = base64Char( (current & 0xFF));
      charCount++;

      //lines cannot be longer than 76 characters
      if ((charCount % 76) == 0) {

        charCount = 0;
        lexical.append(System.getProperty("line.separator"));
      }

      lexical.append(character);
    }

    return lexical.toString();
  }

  /**
   * Compare's this SPBoolean to another object.
   *
   * @param obj Object
   * @return int
   */
  public int compareTo(SPObject obj) {

    int compare = super.compareTo(obj);

    if (compare == 0) {

      //compare by lexical value
//      compare = getLexicalForm().compareTo( ( (SPTypedLiteral) obj).
//                                           getLexicalForm());
//
      //compare buffers
      ByteBuffer objData = ( (SPTypedLiteral) obj).getData();
      ByteBuffer data = getData();
      SPComparator comparator = getSPComparator();

      //reset buffers
      data.position(0);
      objData.position(0);

      //compare prefixes first
      compare = comparator.comparePrefix(data, objData, data.limit());
      if (compare == 0) {

        compare = comparator.compare(data, 0, objData, 0);
      }
    }

    return compare;
  }

  /**
   * Returns the SPBinaryComparator
   *
   * @return SPComparator
   */
  public SPComparator getSPComparator() {
    return SPBinaryComparator.getInstance();
  }

  public boolean equals(Object o) {
    if (!(o instanceof SPBase64BinaryImpl)) return false;
    SPBase64BinaryImpl b = (SPBase64BinaryImpl)o;
    return data.equals(b.data);
  }

  public int hashCode() {
    return data.hashCode();
  }

}
