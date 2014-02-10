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

// Third party packages

// Local packages
import org.mulgara.resolver.spi.*;

/**
 * Base class for initialisers used to provide resources to pluggable database
 * components.
 *
 * The {@link #close} method should be called after initialization to prevent
 * access to further initialization services.
 *
 * @created 2004-10-14
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:23 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
abstract class DatabaseInitializer
{
  /**
   * Flag indicating whether this initializer has been closed.
   */
  private boolean closed = false;

  /**
   * Sole constructor.
   */
  protected DatabaseInitializer()
  {
    this.closed = false;
  }

  /**
   * Make sure we haven't been closed.
   *
   * @throws IllegalStateException if {@link #closed}
   */
  protected void checkState() throws IllegalStateException
  {
    if (closed) {
      throw new IllegalStateException("After initialization");
    }
  }

  /**
   * Disable the {@link ResolverFactoryInitializer} interface.
   */
  void close()
  {
    closed = true;
  }
}
