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

package org.mulgara.resolver.view;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.QueryException;
import org.mulgara.store.tuples.Tuples;

/**
 * This is a SHORT-TERM hack to allow views to work before we have implemented
 * the ResolverFactory query rewriting interface.
 *
 * Use of this interface is HIGHLY discouraged!  
 *
 * If you require this functionality, let the core team know as it
 * may affect scheduling of the new query rewriting interface.
 *
 * Deprecation warnings removed to assist with development in this area.
 * Note: This interface is considered deprecated.  If you don't tell us you're
 * using it it might just disappear without warning.
 *
 * @created 2004-09-22
 * @author <a href="http://staff.tucanatech.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:51 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ViewMarker
{
  public void setSession(SessionView session);
}
