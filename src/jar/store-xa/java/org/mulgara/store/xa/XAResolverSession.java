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

// Third party packages
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Extends ResolverSession with transactional support.
 *
 * @created 2004-03-28
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/02/22 08:17:02 $ 
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface XAResolverSession extends ResolverSession
{
  /**
   * Atomically calls refresh on all transactional resources encapsulated by
   * this ResolverSession as well as all resources passed in the parameter.
   */
  public void refresh(SimpleXAResource[] resources) throws SimpleXAResourceException;

  /**
   * Calls prepare on all transactional resources encapsulated by
   * this ResolverSession as well as all resources passed to refresh.
   */
  public void prepare() throws SimpleXAResourceException;

  /**
   * Atomically calls commit on all transactional resources encapsulated by
   * this ResolverSession as well as all resources passed to refresh.
   */
  public void commit() throws SimpleXAResourceException;

  /**
   * Calls rollback on all transactional resources encapsulated by
   * this ResolverSession as well as all resources passed to refresh.
   */
  public void rollback() throws SimpleXAResourceException;

  /**
   * Calls release on all transactional resources encapsulated by
   * this ResolverSession as well as all resources passed to refresh.
   */
  public void release() throws SimpleXAResourceException;
}
