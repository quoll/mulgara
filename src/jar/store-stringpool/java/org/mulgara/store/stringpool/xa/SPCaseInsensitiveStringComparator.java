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
import java.nio.CharBuffer;

// Third party packages

// Locally written packages
import org.mulgara.store.stringpool.AbstractSPStringComparator;


/**
 * A case insensitive string comparator.
 *
 * @created 2004-11-12
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/12 02:53:28 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPCaseInsensitiveStringComparator
      extends AbstractSPStringComparator {

  private static final SPCaseInsensitiveStringComparator INSTANCE =
      new SPCaseInsensitiveStringComparator();

  public static SPCaseInsensitiveStringComparator getInstance() {
    return INSTANCE;
  }

  protected int comparePrefix(CharBuffer d1cb, CharBuffer d2cb) {
    String d1s = d1cb.toString();
    String d2s = d2cb.toString();
    return d1s.compareToIgnoreCase(d2s);
  }

  protected int compare(CharBuffer d1cb, CharBuffer d2cb) {
    String d1s = d1cb.toString();
    String d2s = d2cb.toString();
    int c = d1s.compareToIgnoreCase(d2s);
    if (c == 0) {
      // Switch to a case-sensitive compare for strings that compare equal.
      c = d1s.compareTo(d2s);
    }
    return c;
  }

}
