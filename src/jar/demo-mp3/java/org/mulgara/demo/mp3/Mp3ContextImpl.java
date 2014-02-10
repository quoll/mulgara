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
 * Used to initialize Mp3 and Id3Tags and share resources/properties.
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
public class Mp3ContextImpl implements Mp3Context {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3ContextImpl.class.
      getName());

  /** Bean returned by getBean */
  private ItqlInterpreterBean bean = null;

  /** Mp3Model returned by getMp3Model */
  private Mp3Model mp3Model = null;

  /** SchemaModel returned by getSchemaModel */
  private SchemaModel schemaModel = null;

  /** Used to determine if the Mp3Context if ready for use. */
  private boolean initialized = false;


  /**
   * Returns the ItqlInterpreterBean shared among the Mp3s.
   * @return ItqlInterpreterBean
   */
  public ItqlInterpreterBean getBean() {
    checkInitialized();
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
   * Returns the model that contains the Mp3's metadata (Id3 info).
   * @return Mp3Model
   */
  public Mp3Model getMp3Model() {
    checkInitialized();
    return mp3Model;
  }

  /**
   * Sets the Mp3Model returned by getMp3Model().
   * @param model Mp3Model
   */
  public void setMp3Model(Mp3Model model) {
    if (model == null) {
      throw new IllegalArgumentException("Mp3Model is null.");
    }
    this.mp3Model = model;
  }

  /**
   * Returns the model containing the Id3 tag schema.
   * @return SchemaModel
   */
  public SchemaModel getSchemaModel() {
    checkInitialized();
    return schemaModel;
  }

  /**
   * Sets the SchemaModel returned by getSchemaModel().
   * @param model SchemaModel
   */
  public void setSchemaModel(SchemaModel model) {
    if (model == null) {
      throw new IllegalArgumentException("SchemaModel is null.");
    }
    this.schemaModel = model;
  }

  /**
   * Throws an exception if the Mp3Context has not been initilized.
   * @throws IllegalStateException
   */
  private void checkInitialized() throws IllegalStateException {
    if (!initialized) {
      if (hasInitialized()) {
        initialized = true;
      } else {
        throw new IllegalStateException("Mp3Context has not been Initialized.");
      }
    }
  }

  /**
   * Returns true if the Mp3Context has been initialized.
   * @return boolean
   */
  private boolean hasInitialized() {
    return (bean != null)
        && (mp3Model != null)
        && (schemaModel != null);
  }

}
