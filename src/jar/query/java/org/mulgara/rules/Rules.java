/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.rules;

import java.net.URI;
import java.io.Serializable;


/**
 * Represents an executable structure of rules.
 *
 * @created 2005-5-22
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/06/26 12:42:43 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface Rules extends Serializable {

  static final long serialVersionUID = -4614382685160461725L;

  /**
   * Set the target model for the rules.
   *
   * @param base The URI of the target model to insert the inferences into.
   */
  public void setTargetModel(URI base);

  /**
   * Starts the rules engine.
   *
   * @param param Implementation dependent data for running.
   */
  public void run(Object param) throws RulesException;

}
