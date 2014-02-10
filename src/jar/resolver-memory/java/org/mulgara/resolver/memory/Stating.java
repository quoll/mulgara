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

package org.mulgara.resolver.memory;

// Java 2 standard packages
import java.util.Arrays;

// Local packages
import org.mulgara.store.nodepool.NodePool;

/**
 * A 4-tuple of <code>long</code>s.
 *
 * @created 2004-11-26
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:48 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class Stating
{
  private final long[] x;

  Stating(long s, long p, long o, long m)
  {
    assert s != NodePool.NONE;
    assert p != NodePool.NONE;
    assert o != NodePool.NONE;
    assert m != NodePool.NONE;

    x = new long[] { s, p, o, m };
  }

  long get(int index)
  {
    return x[index];
  }

  public boolean equals(Object object)
  {
    if (object != null && getClass() == object.getClass()) {
      return Arrays.equals(x, ((Stating) object).x);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return (int) (x[0] * x[1] * x[2] * x[3]);
  }

  public String toString()
  {
    return "[" + x[0] + " " + x[1] + " " + x[2] + " " + x[3] + "]";
  }
}
