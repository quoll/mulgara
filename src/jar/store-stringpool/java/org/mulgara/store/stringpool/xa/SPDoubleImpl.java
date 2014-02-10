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
 * An SPTypedLiteral that represents xsd:double literals.
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
public final class SPDoubleImpl extends AbstractSPTypedLiteral implements SPDouble, SPNumber {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDoubleImpl.class);

  private double d;

  static final int TYPE_ID = 4; // Unique ID

  static final URI TYPE_URI = XSD.DOUBLE_URI;


  SPDoubleImpl(double d) {
    super(TYPE_ID, TYPE_URI);
    this.d = d;
  }


  SPDoubleImpl(ByteBuffer data) {
    this(data.getDouble());
  }


  SPDoubleImpl(String lexicalForm) {
    this(Double.parseDouble(lexicalForm));
  }


  /* from SPDouble interface. */

  public double getDouble() {
    return d;
  }


  /* from SPObject interface. */

  public ByteBuffer getData() {
    ByteBuffer data = ByteBuffer.allocate(Constants.SIZEOF_LONG);
    data.putDouble(d);
    data.flip();
    return data;
  }


  public SPComparator getSPComparator() {
    return SPDoubleComparator.getInstance();
  }


  public String getLexicalForm() {
    return Double.toString(d);
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the doubles.
    return Double.compare(d, ((SPDoubleImpl)o).d);
  }


  /* from Object. */

  public int hashCode() {
    long bits = Double.doubleToRawLongBits(d);
    return (int)bits | (int)(bits >> 32);
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      return d == ((SPDoubleImpl)obj).d;
    } catch (ClassCastException ex) {
      // obj was not an SPDoubleImpl.
      return false;
    }
  }


  /**
   * Comparison used for inequalities the value.
   * If o is not a number, then use the standard comparison.
   * @return -1 if this is smaller than o, +1 if larger, 0 if equal, or the result of compareTo
   *         if not a number.
   */
  public int numericalCompare(SPObject o) {
    return o.isNumber() ? -((SPNumber)o).numericalCompareTo(d) : compareTo(o);
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
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(BigDecimal)
   */
  public int numericalCompareTo(BigDecimal n) {
    double dn = n.doubleValue();
    return d < dn ? -1 : (d > dn ? 1 : 0);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(double)
   */
  public int numericalCompareTo(double d) {
    return Double.compare(this.d, d);
  }


  /**
   * @see org.mulgara.store.stringpool.xa.SPNumber#numericalCompareTo(long)
   */
  public int numericalCompareTo(long l) {
    return Double.compare(d, (double)l);
  }

  /** Compares the binary representations of two SPDoubleImpl objects. */
  public static class SPDoubleComparator implements SPComparator {

    private static final SPDoubleComparator INSTANCE = new SPDoubleComparator();

    public static SPDoubleComparator getInstance() {
      return INSTANCE;
    }

    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
      return Double.compare(d1.getDouble(), d2.getDouble());
    }

  }

}
