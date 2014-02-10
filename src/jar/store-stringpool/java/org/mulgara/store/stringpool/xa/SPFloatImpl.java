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
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;


/**
 * An SPTypedLiteral that represents xsd:float literals.
 *
 * @created 2004-10-05
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
public final class SPFloatImpl extends AbstractSPTypedLiteral implements SPNumber {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPFloatImpl.class);

  private float f;

  static final int TYPE_ID = 3; // Unique ID

  static final URI TYPE_URI = XSD.FLOAT_URI;


  SPFloatImpl(float f) {
    super(TYPE_ID, TYPE_URI);
    this.f = f;
  }


  SPFloatImpl(ByteBuffer data) {
    this(data.getFloat());
  }


  SPFloatImpl(String lexicalForm) {
    this(Float.parseFloat(lexicalForm));
  }


  /* from SPObject interface. */

  public ByteBuffer getData() {
    ByteBuffer data = ByteBuffer.allocate(Constants.SIZEOF_LONG);
    data.putFloat(f);
    data.flip();
    return data;
  }


  public SPComparator getSPComparator() {
    return SPFloatComparator.getInstance();
  }


  public String getLexicalForm() {
    return Float.toString(f);
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the floats.
    return Float.compare(f, ((SPFloatImpl)o).f);
  }


  /* from Object. */

  public int hashCode() {
    return Float.floatToRawIntBits(f);
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      return f == ((SPFloatImpl)obj).f;
    } catch (ClassCastException ex) {
      // obj was not an SPFloatImpl.
      return false;
    }
  }


  /** Compares the binary representations of two SPFloatImpl objects. */
  public static class SPFloatComparator implements SPComparator {

    private static final SPFloatComparator INSTANCE = new SPFloatComparator();

    public static SPFloatComparator getInstance() {
      return INSTANCE;
    }

    public int comparePrefix(ByteBuffer f1, ByteBuffer f2, int f2Size) {
      return 0;
    }

    public int compare(ByteBuffer f1, int st1, ByteBuffer f2, int st2) {
      return Float.compare(f1.getFloat(), f2.getFloat());
    }

  }


  /**
   * Comparison used for inequalities the value.
   * If o is not a number, then use the standard comparison.
   * @return -1 if this is smaller than o, +1 if larger, 0 if equal, or the result of compareTo
   *         if not a number.
   */
  public int numericalCompare(SPObject o) {
    return o.isNumber() ? -((SPNumber)o).numericalCompareTo(f) : compareTo(o);
  }


  /**
   * Indicates if this object is a number. Not usually, so returns <code>false</code> in this
   * abstract class. XSD extensions the represent numerical values should return true.
   * @return <code>true</code> if this object is a number. False otherwise.
   */
  public boolean isNumber() {
    return true;
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(java.math.BigDecimal)
   */
  public int numericalCompareTo(BigDecimal n) {
    double dn = n.doubleValue();
    return (double)f < dn ? -1 : (f > dn ? 1 : 0);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(double)
   */
  public int numericalCompareTo(double d) {
    return Double.compare(f, d);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(long)
   */
  public int numericalCompareTo(long l) {
    return Double.compare(f, (double)l);
  }

}
