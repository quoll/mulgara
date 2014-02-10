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

package org.mulgara.resolver;

// Local packages
import org.mulgara.resolver.spi.NullSecurityAdapter;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePool;

/**
 * This {@link org.mulgara.resolver.spi.SecurityAdapter} enforces the sanctity of the system model
 * (<code>#</code>).
 *
 * A {@link org.mulgara.server.Session} may only modify the contents of the system model
 * indirectly, via the <code>backup</code>, <code>create</code>,
 * <code>drop</code> and <code>restore</code> methods.
 *
 * @created 2004-10-15
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

class SystemGraphSecurityAdapter extends NullSecurityAdapter {
  /**
   * The preallocated node representing the system model.
   */
  private final long systemModel;

  /**
   * Sole constructor.
   *
   * @param systemModel  the preallocated node representing the system model
   */
  SystemGraphSecurityAdapter(long systemModel)
  {
    assert systemModel != NodePool.NONE;

    // Initialize fields
    this.systemModel = systemModel;
  }

  //
  // Methods overriding NullSecurityAdapter
  //

  /**
   * @return <code>true</code> unless <var>model</var> represents the system
   *   model
   */
  public boolean canModifyModel(long model, ResolverSession resolverSession)
  {
    return model != systemModel;
  }
}
