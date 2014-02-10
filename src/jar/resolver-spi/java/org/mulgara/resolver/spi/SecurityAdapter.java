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

package org.mulgara.resolver.spi;

// Local packages
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.server.Session;

/**
 * Pluggable component used to arbitrate which {@link Resolver} methods can be
 * exercised by a {@link org.mulgara.server.Session} upon a particular model.
 *
 * @created 2004-10-14
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface SecurityAdapter
{
  /**
   * Arbitrate whether the entire contents of the database may be exported.
   *
   * If access is denied, the user will be told that they are not permitted to
   * back up the database.
   */
  public boolean canBackup();

  /**
   * Arbitrate whether the entire contents of the database may be replaced by
   * the contents of a backup file.
   *
   * If access is denied, the user will be told that they are not permitted to
   * restore the database.
   */
  public boolean canRestore();

  /**
   * Arbitrate whether a model is visible in the system model (<code>#</code>).
   *
   * If access is denied, the system model will silently omit statements with
   * the <var>model</var> as their subject when resolving the system model.
   *
   * Attempts to modify, remove, or resolve the model will fail, claiming that
   * no such model exists.  Attempts to create a model which already exists
   * will fail, and the user will be told that they are not allowed to create
   * a model with that URI.  Note that this may allow the user to deduce that
   * the model exists, even though they can't see it in the system model.
   * Because of this, if access is denied, access to create a model will also
   * be denied.
   *
   * @param model  any node representing a URI reference
   * @return whether the {@link Session}'s view of the system model may reveal
   *   statements involving the <var>model</var>
   */
  public boolean canSeeModel(long model, ResolverSession resolverSession);

  //
  // Methods controlling access to Resolver methods
  //

  /**
   * Arbitrate access to the {@link Resolver#createModel} method.
   *
   * If access is denied, the user will be told that they are not allowed to
   * create a model with that URI.  If the user is not allowed to see the
   * model they're attempting to create (see {@link #canSeeModel}) then access
   * will be denied in exactly the same way, to prevent the user from deducing
   * the existence of the invisible model from the fact that they ought to have
   * been able to create it.
   *
   * @param model  any node representing a URI reference
   * @return whether the {@link Session} is permitted to create the
   *   <var>model</var> from the database and the system model
   */
  public boolean canCreateModel(long model, ResolverSession resolverSession);

  /**
   * Arbitrate access to the {@link Resolver#modifyModel} method.
   *
   * If access is denied, the user will be told that they are not allowed to
   * modify the model.
   *
   * @param model  any node representing a model
   * @return whether the {@link Session} is permitted to drop the
   *   <var>model</var> from the database and the system model
   */
  public boolean canModifyModel(long model, ResolverSession resolverSession);

  /**
   * Arbitrate access to the {@link Resolver#removeModel} method.
   *
   * If access is denied, the user will be told that they are not permitted to
   * remove the model.
   *
   * @param model  any node representing a model
   * @return whether the {@link Session} is permitted to remove the
   *   <var>model</var> from the database and the system model
   */
  public boolean canRemoveModel(long model, ResolverSession resolverSession);

  /**
   * Arbitrate access to the {@link Resolver#resolve} method.
   *
   * If access is denied, the user will be told that they are not permitted to
   * read the contents of the model.
   *
   * @param model  any node representing a model
   * @return whether the {@link Session} is permitted to drop the
   *   <var>model</var> from the database and the system model
   */
  public boolean canResolve(long model, ResolverSession resolverSession);

  /**
   * Add authentication data.
   *
   * @param username  the identity to authenticate as
   * @param password  the secret used to prove identity
   * @see Session#login
   */
  public void login(String username, char[] password);
}
