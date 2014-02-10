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
 * Class that represents a XSD boolean prinitive datatype.
 *
 * @created 2004-10-04
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/12 02:53:28 $ by $Author: newmana $
 *
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPBooleanImpl
    extends AbstractSPTypedLiteral
    implements SPTypedLiteral {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SPBooleanImpl.class);

  /** Type code that identifies this type */
  static final int TYPE_ID = 12;

  /** the xsd:boolean URI */
  static final URI TYPE_URI = XSD.BOOLEAN_URI;

  /** native boolean value */
  private boolean value = false;

  /** ByteBuffer value of true */
  static final ByteBuffer TRUE_BUFFER;

  /** ByteBuffer value of false */
  static final ByteBuffer FALSE_BUFFER;

  //initialize the true/false byte buffers
  static {

    TRUE_BUFFER = ByteBuffer.wrap(new byte [] {1}).asReadOnlyBuffer();
    FALSE_BUFFER = ByteBuffer.wrap(new byte [] {0}).asReadOnlyBuffer();
  }


  /**
   * Constructor.
   *
   * @param lexicalForm String {true, false, 1, 0}
   */
  public SPBooleanImpl(String lexicalForm) {

    super(TYPE_ID, TYPE_URI);

    //is it valid?
    validate(lexicalForm);

    if (("true".equalsIgnoreCase(lexicalForm))
        || ("1".equals(lexicalForm))) {

      value = true;
    } else {

      value = false;
    }
  }

  /**
   * Constructor.
   *
   * @param buffer ByteBuffer
   */
  public SPBooleanImpl(ByteBuffer buffer) {

    super(TYPE_ID, TYPE_URI);

    //is it valid?
    validate(buffer);

    //1 indicates true
    value = (buffer.get(0) == 1) ? true : false;
  }

  /**
   * Checks to ensure that the lexicalForm is a valid value and is not null.
   *
   * @param lexicalForm String
   * @throws IllegalArgumentException
   */
  private void validate(String lexicalForm) throws IllegalArgumentException {

    //is it null?
    if (lexicalForm == null) {

      throw new IllegalArgumentException("'lexicalFrom' cannot be null.");
    }

    //is it a valid value?
    if (! ("true".equalsIgnoreCase(lexicalForm))
        && ! ("1".equals(lexicalForm))
        && ! ("false".equalsIgnoreCase(lexicalForm))
        && ! ("0".equals(lexicalForm))) {

      //value is not true, false, 1 or 0
      throw new IllegalArgumentException("'lexicalForm': " + lexicalForm +
                                         " is not a valid xsd:boolean " +
                                         "(true, false, 1 or 0)");
    }
  }

  /**
   * Ensure the buffer has either 0 or 1 as it's first byte.
   *
   * @param buffer ByteBuffer
   * @throws IllegalArgumentException
   */
  private void validate(ByteBuffer buffer) throws IllegalArgumentException {

    //is it null?
    if (buffer == null) {

      throw new IllegalArgumentException("'buffer' cannot be null.");
    }

    //is it a valid value?
    byte value = buffer.get(0);
    if (! (value == 1)
        && ! (value == 0)) {

      //value is not true, false, 1 or 0
      throw new IllegalArgumentException("'buffer' must be either 0 or 1");
    }
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

      //are they still equal?
      boolean objValue = ((SPBooleanImpl) obj).getBoolean();
      if (objValue != getBoolean()) {

        if (objValue) {
          //this == false, obj == true (correct order)
          compare = -1;
        } else {
          //this == true, obj == false (incorrect order)
          compare = 1;
        }
      }
    }

    return compare;
  }


  /**
   * Returns TRUE_BUFFER or FALSE_BUFFER depending on value.
   *
   * @return ByteBuffer
   */
  public ByteBuffer getData() {

    return (value) ? TRUE_BUFFER : FALSE_BUFFER;
  }

  /**
   * returns either "true" or "false"
   *
   * @return String
   */
  public String getLexicalForm() {

    return (value) ? "true" : "false";
  }

  /**
   * Returns the primitive value of this object.
   *
   * @return boolean
   */
  public boolean getBoolean () {

    return value;
  }

  /**
   * Returns a Comparator for comparing xsd:boolean literals.
   *
   * @return SPComparator
   */
  public SPComparator getSPComparator() {

    return SPBooleanComparator.getInstance();
  }

  /** Compares the binary representation of two SPBoolean objects */
  public static class SPBooleanComparator implements SPComparator {

    /** Singleton instance */
    private static final SPBooleanComparator INSTANCE = new SPBooleanComparator();

    /**
     * Returns the Singleton.
     *
     * @return SPBooleanComparator
     */
    public static SPBooleanComparator getInstance() {

      return INSTANCE;
    }

    /**
     * returns 0. use compare(ByteBuffer d1, ByteBuffer d2).
     *
     * @param d1 ByteBuffer
     * @param d2 ByteBuffer
     * @param d2Size int
     * @return int
     */
    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {

      return 0;
    }

    /**
     * Compares the first byte of each buffer (either 1 or 0). 'false' will be
     * ordered before 'true'.
     *
     * @param d1 ByteBuffer
     * @param d2 ByteBuffer
     * @return int
     */
    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {

      //value is stored in the first byte
      return d1.get(0) - d2.get(0);
    }
  }

}
