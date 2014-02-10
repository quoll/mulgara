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

package org.mulgara.store.stringpool;

// Java packages
import java.nio.ByteBuffer;

// Locally written packages

/**
 * This class represents a search limit for an SPObject.
 *
 * @created 2004-11-02
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 10:26:19 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPLimit implements SPObject {

  /** The type of this limit */
  private TypeCategory type;

  /** Indicates if this item should be considered the smallest of its type or the largest */
  private boolean smallest;

  /** A placeholder comparator fo use with getComparator */
  private static SPComparator comparator = new DummyComparator();

  /**
   * Constructor.
   *
   * @param typeID The ID of the type this limit applies to.
   * @param smallest Whether this it the lower or the higher limit for the type
   */
  public SPLimit(int typeID, boolean smallest) {
    type = TypeCategory.forId(typeID);
    this.smallest = smallest;
  }
  
  /**
   * Return the type this limit applies to.
   *
   * @return The type of this limit.
   */
  public TypeCategory getTypeCategory() {
    return type;
  }

  /**
   * Inform the caller that this class does not subscribe to normal comparison semantics.
   *
   * @return <code>true</code> to indicate that normal comparison is to be overridden.
   */
  public boolean compareOverride() {
    return true;
  }

  /**
   * Indicate if this is the smallest limit, or the largest.
   *
   * @return <code>true</code> if this is the smallest limit, <code>false</code> if it is the largest.
   */
  public boolean isSmallest() {
    return smallest;
  }

  /**
   * Return a dummy comparator, as one may be requested.
   *
   * @return A static comparator which always returns "equal"
   */
  public SPComparator getSPComparator() {
    return comparator;
  }

  public ByteBuffer getData() {
    throw new UnsupportedOperationException("Limit objects do not represent an RDF node");
  }

  public String getLexicalForm() {
    throw new UnsupportedOperationException("Limit objects do not represent an RDF node");
  }

  public String getEncodedString() {
    throw new UnsupportedOperationException("Limit objects do not represent an RDF node");
  }

  public org.jrdf.graph.Node getRDFNode() {
    throw new UnsupportedOperationException("Limit objects do not represent an RDF node");
  }

  public int compareTo(SPObject s) {

    // compare the types
    int c = type.ID - s.getTypeCategory().ID;
    // if the types are different, then return
    if (c != 0) {
      return c;
    }

    // same types, is the other object also a limit?
    if (s instanceof SPLimit) {
      SPLimit l = (SPLimit)s;
      // identical limits are equal, else smallest < largest
      if (smallest) {
        return l.smallest ? 0 : -1;
      } else {
        return l.smallest ? 1 : 0;
      }
    }
    return smallest ? -1 : 1;
  }

  /**
   * A placeholder for the SPComparator.
   */
  private static class DummyComparator implements SPComparator {
    public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
      return 0;
    }

    public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
      return 0;
    }

  }

  /** 
   * @see org.mulgara.store.stringpool.SPObject#isNumber()
   */
  public boolean isNumber() {
    return false;
  }

  /**
   * @see org.mulgara.store.stringpool.SPObject#numericalCompare(org.mulgara.store.stringpool.SPObject)
   */
  public int numericalCompare(SPObject o) {
    return compareTo(o);
  }

}
