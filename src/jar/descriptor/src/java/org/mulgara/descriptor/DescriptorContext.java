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

package org.mulgara.descriptor;


// Log4j
import org.apache.log4j.*;
import org.mulgara.itql.ItqlInterpreterBean;

/**
 * Holds context for a Descriptor such as which interpreter to use.
 *
 * @created 2002-06-25
 *
 * @author Keith Ahern
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:11 $
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
public class DescriptorContext {

  /**
   * log4j
   */
  private static final Logger log = Logger.getLogger(DescriptorContext.class);

  /**
   * Description of the Field
   */
  private ItqlInterpreterBean bean = null;

  /**
   * Set the bean to use for example a local session bean or an RMI capable
   * bean.
   *
   * @param bean see comment
   */
  public void setInterpreterBean(ItqlInterpreterBean bean) {

    if (log.isDebugEnabled()) {

      log.debug("Setting Interpreter bean");
    }

    this.bean = bean;
  }

  /**
   * Get the bean in use.
   *
   * @return bean see comment
   */
  public ItqlInterpreterBean getInterpreterBean() {

    if (log.isDebugEnabled()) {

      log.debug("Getting Interpreter bean");
    }

    return bean;
  }

  /**
   * Pretty version of contents
   *
   * @return String see comment
   */
  public String toString() {
    return "Contains Bean: " + bean;
  }
}
