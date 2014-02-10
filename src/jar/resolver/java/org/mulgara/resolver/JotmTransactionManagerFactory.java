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

// Java 2 standard packages
import javax.naming.NamingException;

// Java 2 enterprise packages
import javax.transaction.TransactionManager;

// Third party packages
import org.mulgara.transaction.TransactionManagerFactory;
import org.objectweb.jotm.Jotm;  // JOTM transaction manager
import org.objectweb.transaction.jta.TMService;

// Local packages

/**
* Factory for JOTM {@link TransactionManager}s.
*
* @created 2004-11-02
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision: 1.8 $
* @modified $Date: 2005/01/05 04:58:24 $Author: newmana $
* @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
* @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
*      Software Pty Ltd</a>
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class JotmTransactionManagerFactory implements TransactionManagerFactory
{
  /**
   * The wrapped JOTM factory for {@link TransactionManager}s.
   */
  private final TMService tmService;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   */
  public JotmTransactionManagerFactory() throws NamingException
  {
    tmService = new Jotm(true,    // create as a local transaction factory
                         false);  // not bound in an RMI or CORBA registry
  }

  //
  // Methods implementing TransactionManagerFactory
  //

  public TransactionManager newTransactionManager()
  {
    return tmService.getTransactionManager();
  }

  public void close()
  {
    tmService.stop();
  }
}
