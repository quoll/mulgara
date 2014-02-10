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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * Contributor(s):
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500507038
 *   contributed to the Mulgara Project 
 *   (per clause 4.1.4) under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

import java.net.URI;

import org.mulgara.query.QueryException;

/**
 * @created 2006-04-19
 * @author <a href="http://staff.netymon.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.1.1.1 $
 * @modified $Date: 2005/10/30 19:21:17 $ by $Author: prototypo $
 * @maintenanceAuthor $Author: prototypo $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface SymbolicTransformationContext
{
  /**
   * Maps a modelURI to the corresponding modelTypeURI.
   *
   * FIXME: What do we do about external resolvers?  They should have access to SymbolicTransformation as well.
   */
  public URI mapToModelTypeURI(URI modelURI) throws QueryException;
}
