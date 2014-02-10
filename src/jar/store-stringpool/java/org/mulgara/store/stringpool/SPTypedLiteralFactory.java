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
import java.util.Set;
import java.net.URI;
import java.nio.ByteBuffer;


/**
 * An XSD datatype handler implements this interface.
 *
 * @created 2004-09-24
 *
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
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
public interface SPTypedLiteralFactory {

  /**
   * Returns the type ID for all primitive and built-in derrived types
   * supported by this datatype handler.
   */
  public int getTypeId();

  /**
   * Returns a Set containing the type URIs of all primitive and built-in
   * derrived types supported by this datatype handler.
   */
  public Set<URI> getTypeURIs();

  public SPTypedLiteral newSPTypedLiteral(URI typeURI, String lexicalForm);
  public SPTypedLiteral newSPTypedLiteral(int subtypeId, ByteBuffer data);

}
