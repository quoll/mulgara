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

package org.mulgara.store.xa;

// Local packages
import org.mulgara.util.Constants;

/**
 * Various utility functions used by the triplestore.
 *
 * @created 2002-11-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/07/05 04:23:54 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XAUtils {

  /**
   * Performs a byteswap on an integer.
   *
   * @param i The integer to byte swap.
   * @return The swapped integer.
   */
  public static int bswap(int i) {

    return ((i >> 24) & 0xff) | (((i >> 16) & 0xff) << 8) |
    (((i >> 8) & 0xff) << 16) | ((i & 0xff) << 24);
  }

  /**
   * Returns the log (base 2) of a number.
   *
   * @param n The number to get the log from.
   * @return An integral base 2 log of n.
   */
  public static int log2(int n) {

    int count = 0;
    int b = Constants.SIZEOF_INT_BITS >> 1;

    // Treat n as an unsigned integer.
    if (n < 0) {

      return Constants.SIZEOF_INT_BITS - 1;
    }

    while ((n > 1) && (b > 0)) {

      if (n >= (1 << b)) {

        count += b;
        n >>= b;
      }

      b >>= 1;
    }

    return count;
  }

  /**
   * Perform a comparison between two longs.
   *
   * @param a The first value to compare.
   * @param b The second value to compare.
   * @return 1, 0 or -1 if a is greater than, equal to, or less than b.
   */
  public static int compare(long a, long b) {

    return (a == b) ? 0 : ((a < b) ? (-1) : 1);
  }

  /**
   * Perform an comparison between two unsigned integers.
   *
   * @param a The first value to compare.
   * @param b The second value to compare.
   * @return 1, 0 or -1 if a is greater than, equal to, or less than b.
   */
  public static int compareUInt(int a, int b) {

    if (a == b) {

      return 0;
    }

    long ua = ui(a);
    long ub = ui(b);

    return (ua < ub) ? (-1) : 1;
  }

  /**
   * Converts an unsigned int stored in a signed int into a long.
   *
   * @param i An int containing an unsigned int.
   * @return A long with the unsigned integer value from i.
   */
  public static long ui(int i) {

    return (long) i & Constants.MASK32;
  }
}
