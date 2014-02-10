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
 * Class that represents a XSD hexBinary primitive datatype.
 *
 * @created 2004-10-04
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPHexBinaryImpl extends AbstractSPTypedLiteral implements SPTypedLiteral {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SPHexBinaryImpl.class);

  /** Type code that identifies this type */
  static final int TYPE_ID = 14;

  /** the xsd:hexBinary URI */
  static final URI TYPE_URI = XSD.HEX_BINARY_URI;

  /** The hex binary data as bytes */
  private final ByteBuffer data;


  /**
   * Constructor.
   * @param lexicalForm String must contain valid hex values [a-f][A-F][0-9]
   */
  public SPHexBinaryImpl(String lexicalForm) {
    super(TYPE_ID, TYPE_URI);
    if (lexicalForm == null) throw new IllegalArgumentException("'lexicalForm' cannot be null.");

    // ignore case
    String lexical = lexicalForm.toLowerCase();

    // ensure that lexical falls on a boundary of 2 chars
    if ((lexical.length() % 2) != 0) lexical = "0" + lexical;

    //pack two chars into each byte (hex digits are 4 bit)
    int capacity = lexical.length() / 2;
    data = ByteBuffer.allocate(capacity);
    byte currentByte = 0;
    int lowChar = 0;
    int hiChar = 0;

    int offset = 0;
    for (int i = lexical.length() - 1; i > 0; i -= 2) {

      //there will always be an even number of chars
      lowChar = hexValue(lexical.charAt(i));
      hiChar = hexValue(lexical.charAt(i - 1));

      //join the hex values
      currentByte = (byte) ((hiChar << 4) | lowChar);

      data.put(offset++, currentByte);
    }
    data.rewind();
    data.limit(offset);
  }


  /**
   * Constructor.
   * @param buffer ByteBuffer
   */
  public SPHexBinaryImpl(ByteBuffer buffer) {
    super(TYPE_ID, TYPE_URI);
    data = buffer;
  }


  /**
   * Converts a hex character [0-9][a-f][A-F] to an int.
   * @param hexChar char
   * @return int
   */
  public int hexValue(char hexChar) {
    return Integer.parseInt("" + hexChar, 16);
  }


  /**
   * Converts the value into a hax character. Value must be between 0 and 15
   * @param value int
   * @return char
   */
  public char hexChar(int value) {
    return Character.forDigit(value, 16);
  }


  /**
   * Returns a read-only ByteBuffer containing the hexBinary data
   * @return ByteBuffer
   */
  public ByteBuffer getData() {
    return data.asReadOnlyBuffer();
  }


  public SPComparator getSPComparator() {
    return SPBinaryComparator.getInstance();
  }


  /**
   * Converts the data bytes into a String.
   * @return String
   */
  public String getLexicalForm() {
    StringBuffer lexicalForm = new StringBuffer();
    byte currentByte = 0;
    int hiChar = 0;
    int lowChar = 0;

    //start at the beginning
    data.rewind();

    while (data.limit() > data.position()) {
      currentByte = data.get();

      //get hi and low 4 bits
      hiChar = (currentByte >> 4) & 0x0F;
      lowChar = currentByte & 0x0F;

      //append the hex characters
      lexicalForm.append(hexChar(lowChar));
      lexicalForm.append(hexChar(hiChar));
    }

    //remove any trailing '0' chars (buffer will be reversed)
    int length = lexicalForm.length();
    if ((length >= 1) && (lexicalForm.charAt(length -1) == '0')) {
      lexicalForm.deleteCharAt(length - 1);
    }

    //return as big-endian
    return lexicalForm.reverse().toString();
  }


  /**
   * Compare's this SPBoolean to another object.
   * @param obj Object
   * @return int
   */
  public int compareTo(SPObject obj) {
    int compare = super.compareTo(obj);
    if (compare == 0) {
      //compare by lexical value
      compare = getLexicalForm().compareTo(((SPTypedLiteral)obj).getLexicalForm());

      //compare buffers
//      ByteBuffer objData = ((SPTypedLiteral) obj).getData();
//      ByteBuffer data = getData();
//      SPComparator comparator = getSPComparator();
//
//      //compare prefixes first
//      compare = comparator.comparePrefix(data, objData, data.limit());
//      if (compare == 0) {
//
//        compare = comparator.compare(data, objData);
//      }
    }

    return compare;
  }

}


