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
import java.nio.ByteBuffer;

// Third party packages

// Locally written packages
import org.mulgara.store.stringpool.SPComparator;


/**
 * A SPComparator for sequences of unsigned bytes.
 *
 * @created 2004-10-06
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
public final class SPBinaryComparator implements SPComparator {

  private static final SPBinaryComparator INSTANCE = new SPBinaryComparator();

  public static SPBinaryComparator getInstance() {
    return INSTANCE;
  }

  public int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
    // Truncate d1 to the length of d2.
    if (d1.limit() > d2.limit()) {
      d1.limit(d2.limit());
    }
    return compare(d1, 0, d2, 0);
  }

  public int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
    while (d1.hasRemaining() && d2.hasRemaining()) {
      int c = (d1.get() & 0xff) - (d2.get() & 0xff);
      if (c != 0) return c;
    }
    if (d1.hasRemaining()) return 1;
    if (d2.hasRemaining()) return -1;
    return 0;
  }

}
