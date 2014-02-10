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

package org.mulgara.demo.mp3;

// Java 2 standard packages

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.itql.ItqlInterpreterBean;


/**
 * Implementation of ModelContext.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ModelContextImpl implements ModelContext {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(ModelContextImpl.class.
      getName());

  /** Bean returned by getBean */
  private ItqlInterpreterBean bean = null;

  /**
   * Returns the ItqlInterpreterBean shared among the Models.
   * @return ItqlInterpreterBean
   */
  public ItqlInterpreterBean getBean() {
    if (!hasInitialized()) {
      throw new IllegalStateException("ModelContext has not been intialized.");
    }
    return bean;
  }

  /**
   * Sets the bean to be returned by getBean().
   * @param bean ItqlInterpreterBean
   */
  public void setBean(ItqlInterpreterBean bean) {
    if (bean == null) {
      throw new IllegalArgumentException("ItqlInterpreterBean is null.");
    }
    this.bean = bean;
  }

  /**
   * Returns true if the Context has been initialized.
   * @return boolean
   */
  private boolean hasInitialized() {
    return (bean != null);
  }

}
