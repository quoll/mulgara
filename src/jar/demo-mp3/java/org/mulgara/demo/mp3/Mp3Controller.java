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

// JRDF

// Local packages


/**
 * Used to create and manage Mp3Models and their supporting interfaces.
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
public abstract class Mp3Controller {

  /**
   * Creates a new/un-initialized Mp3Controller instance
   * @return Mp3Controller
   */
  public static Mp3Controller newInstance() {
    return new Mp3ControllerImpl();
  }

  /**
   * Returns the current Mp3Model
   * @throws Exception
   * @return Mp3Model
   */
  public abstract Mp3Model getMp3Model() throws Exception;

  /**
   * Returns the current SchemaModel (used by Mp3Model)
   * @throws Exception
   * @return SchemaModel
   */
  public abstract SchemaModel getSchemaModel() throws Exception;

  /**
   * Returns the current FileSystemModel (used by Mp3Model)
   * @throws Exception
   * @return FileSystemModel
   */
  public abstract FileSystemModel getFileSystemModel() throws Exception;

  /**
   * Configures the Controller.
   * @param config Mp3Config
   * @throws Exception
   */
  public abstract void init(Mp3Config config) throws Exception;

  /**
   * Creates all Models described by the Mp3Controller's config.
   * @throws Exception
   */
  public abstract void createModels() throws Exception;

  /**
   * Frees any allocated resources.
   */
  public abstract void close();

}
