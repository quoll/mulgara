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

// Java 2 standard packages
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

// Third party packages

// Locally written packages


/**
 * A case sensitive string comparator.
 *
 * @created 2004-11-12
 *
 * @author David Makepeace
 *
 * @version $Revision$
 *
 * @modified $Date$
 *
 * @maintenanceAuthor $Author$
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractSPStringComparator implements SPComparator {

  public final int comparePrefix(ByteBuffer d1, ByteBuffer d2, int d2Size) {
    CharBuffer d1cb = AbstractSPObject.CHARSET.decode(d1);
    CharBuffer d2cb = AbstractSPObject.CHARSET.decode(d2);

    // Remove any replacement char that may have been appended to d2 if the
    // source ByteBuffer ended with a partial multi-byte character.
    int d2Limit = d2cb.limit();
    if (d2Limit > 0 && d2cb.get(d2Limit - 1) == '\uFFFD') {
      d2cb.limit(--d2Limit);
    }

    // Truncate d1cb to the length of d2cb.
    int d1Limit = d1cb.limit();
    if (d1Limit > d2Limit) {
      d1Limit = d2Limit;
      d1cb.limit(d1Limit);
    }

    return comparePrefix(d1cb, d2cb);
  }

  protected int comparePrefix(CharBuffer d1cb, CharBuffer d2cb) {
    return compare(d1cb, d2cb);
  }

  public final int compare(ByteBuffer d1, int st1, ByteBuffer d2, int st2) {
    return compare(
        AbstractSPObject.CHARSET.decode(d1), AbstractSPObject.CHARSET.decode(d2)
    );
  }

  abstract protected int compare(CharBuffer d1cb, CharBuffer d2cb);

}
