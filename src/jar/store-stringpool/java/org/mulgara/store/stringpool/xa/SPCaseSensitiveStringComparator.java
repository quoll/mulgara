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
 * A case sensitive string comparator.
 * <br>
 * "A binary sort of UTF-8 strings gives the same ordering as a binary
 * sort of unicode scalar values." - p.20 The Unicode Standard 3.0
 * What a pity ByteBuffers contain signed bytes.
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
public final class SPCaseSensitiveStringComparator
      extends AbstractSPStringComparator {

  private static final SPCaseSensitiveStringComparator INSTANCE =
      new SPCaseSensitiveStringComparator();

  public static SPCaseSensitiveStringComparator getInstance() {
    return INSTANCE;
  }

  protected int compare(CharBuffer d1cb, CharBuffer d2cb) {
    return d1cb.compareTo(d2cb);
  }

}
