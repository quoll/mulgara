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

package org.mulgara.store.nodepool;

/**
 * Exception thrown by {@link NodePool#releaseNode} method to indicate that the
 * node isn't in the pool.
 *
 * @created 2001-12-18
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class NoSuchNodeException extends NodePoolException {

  /**
   * The node which caused the exception.
   */
  private final long node;

  //
  // Constructors
  //

  /**
   * CONSTRUCTOR NoSuchNodeException TO DO
   *
   * @param node  the node which caused the exception
   * @param message  explanatory text
   */
  public NoSuchNodeException(long node, String message) {
    super(message);
    this.node = node;
  }

  /**
   * CONSTRUCTOR NoSuchNodeException TO DO
   *
   * @param node  the node which caused the exception
   * @param message  explanatory text
   * @param cause  chained cause of this exception
   */
  public NoSuchNodeException(long node, String message, Throwable cause) {
    super(message, cause);
    this.node = node;
  }

  //
  // Detail accessors
  //

  /**
   * @return the node which caused the exception
   */
  public long getNode()
  {
    return node;
  }
}
