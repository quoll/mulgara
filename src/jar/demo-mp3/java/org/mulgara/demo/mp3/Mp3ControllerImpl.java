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

// JRDF
import org.jrdf.graph.URIReference;
import org.mulgara.itql.ItqlInterpreterBean;

// Local packages


/**
 * Implementation of Mp3Controller. All models must be set before calling any
 * getXXModel() methods.
 *
 * <p>If a model resource changes, future calls to the corresponding
 * getXXModel() will return the new model. If any model changes, getMp3model()
 * will return a new mp3 model using the new supporting models.
 *
 *
 * @created 2004-12-06
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
public class Mp3ControllerImpl extends Mp3Controller {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3ControllerImpl.class.
      getName());

  /** Where the mp3Model stores it's information */
  private URIReference mp3ModelResource = null;

  /** Where the schemaModel stores it's information */
  private URIReference schemaModelResource = null;

  /** Where the fsModel stores it's information */
  private URIReference fsModelResource = null;

  /** Type of Graph to store Mp3s information */
  private URIReference mp3ModelType = null;

  /** Type of Graph to store Schema information */
  private URIReference schemaModelType = null;

  /** Type of Graph to store FileSystem information */
  private URIReference fsModelType = null;

  /** Returned by getMp3Model */
  private Mp3Model mp3Model = null;

  /** Returned by getSchemaModel */
  private SchemaModel schemaModel = null;

  /** Returned by getFileSystemModel */
  private FileSystemModel fsModel = null;

  /** Shared among the entire application */
  private ItqlInterpreterBean bean = null;

  /** Context shared among models */
  private ModelContextImpl context = null;

  /**
   * Returns the current Mp3Model
   * @throws Exception
   * @return Mp3Model
   */
  public Mp3Model getMp3Model() throws Exception {
    validate();
    if (mp3Model == null) {
      mp3Model = new Mp3ModelImpl();
      //init
      mp3Model.setResource(mp3ModelResource);
      mp3Model.setType(mp3ModelType);
      mp3Model.setSchemaModel(getSchemaModel());
      mp3Model.init(getModelContext());
    }
    return mp3Model;
  }

  /**
   * Returns the current SchemaModel (used by Mp3Model)
   * @throws Exception
   * @return SchemaModel
   */
  public SchemaModel getSchemaModel() throws Exception {
    validate();
    if (schemaModel == null) {
      schemaModel = new SchemaModelImpl();
      //init
      schemaModel.setResource(schemaModelResource);
      schemaModel.setType(schemaModelType);
      schemaModel.init(getModelContext());
    }
    return schemaModel;
  }

  /**
   * Returns the current FileSystemModel (used by Mp3Model)
   * @throws Exception
   * @return FileSystemModel
   */
  public FileSystemModel getFileSystemModel() throws Exception {
    validate();
    if (fsModel == null) {
      fsModel = new FileSystemModelImpl();
      //init
      fsModel.setResource(fsModelResource);
      fsModel.setType(fsModelType);
      fsModel.init(getModelContext());
    }
    return fsModel;
  }

  /**
   * Configures the Controller.
   * @param config Mp3Config
   * @throws Exception
   */
  public void init(Mp3Config config) throws Exception {
    if (config == null) {
      throw new IllegalArgumentException("Mp3Config is null");
    }
    mp3ModelResource = config.getMp3Model();
    schemaModelResource = config.getSchemaModel();
    fsModelResource = config.getFileSystemModel();
    mp3ModelType = config.getMp3ModelType();
    schemaModelType = config.getSchemaModelType();
    fsModelType = config.getFileSystemModelType();
    //validate
    if (mp3ModelResource == null) {
      throw new IllegalArgumentException("Mp3Config returned a null Mp3Model " +
          "Resource.");
    }
    if (schemaModelResource == null) {
      throw new IllegalArgumentException("Mp3Config returned a null " +
          "SchemaModel Resource.");
    }
    if (fsModelResource == null) {
      throw new IllegalArgumentException("Mp3Config returned a null " +
          "FileSystemModel Resource.");
    }
  }

  /**
   * Creates all Models described by the Mp3Controller's config.
   * @throws Exception
   */
  public void createModels() throws Exception {
    getMp3Model().create();
    getSchemaModel().create();
    getFileSystemModel().create();
  }

  /**
   * Frees any allocated resources.
   */
  public void close() {
    getBean().close();
    close(mp3Model);
    close(schemaModel);
    close(fsModel);
  }

  /**
   * Closes the Graph
   * @param model Graph
   */
  private void close(Model model) {
    if (model != null) {
      model.close();
    }
  }

  /**
   * Returns the current ModelContext or a new ModelContext if one does not
   * exist.
   * @return ModelContext
   */
  private ModelContext getModelContext() {

    if (context == null) {
      context = new ModelContextImpl();
      context.setBean(getBean());
    }
    return context;
  }

  /**
   * Returns the current ItqlInterpreterBean or creates one if it doesn't exist.
   * @return ItqlInterpreterBean
   */
  private ItqlInterpreterBean getBean() {

    if (bean == null) {
      bean = new ItqlInterpreterBean();
    }
    return bean;
  }

  /**
   * Throws an exception if the model resources are not set.
   * @throws IllegalStateException
   */
  private void validate() throws IllegalStateException {

    if (mp3ModelResource == null) {
      throw new IllegalStateException("Mp3Model Resource has not been set.");
    }
    if (schemaModelResource == null) {
      throw new IllegalStateException("SchemaModel Resource has not been set.");
    }
    if (fsModelResource == null) {
      throw new IllegalStateException("FileSystemModel Resource has not " +
          "been set.");
    }
  }

}
