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

package org.mulgara.itql;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Locally written packages

// Automatically generated packages (SableCC)
import org.mulgara.query.*;


/**
 * A simple variable factory object.  Creates variables called "k" followed
 * by an incrementing variable value.
 *
 * @created 2004-06-18
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class VariableFactoryImpl implements VariableFactory {

  /** The logger */
  private final static Logger logger = Logger.getLogger(VariableFactory.class.getName());

  /**
   * Index to keep track.
   */
  private int index = 0;

  /**
   * Returns a new variable an increment index.
   *
   * @return Variable a new variable.
   */
  public Variable newVariable() {
    if (logger.isDebugEnabled()) logger.debug("Allocating variable k" + index);
    return new Variable("k" + Integer.toString(index++));
  }

  /**
   * Reset the index to 0.
   */
  public void reset() {
    if (logger.isDebugEnabled()) logger.debug("Resetting");
    index = 0;
  }
}
