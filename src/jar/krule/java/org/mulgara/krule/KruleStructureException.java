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

package org.mulgara.krule;

/**
 * Indicates a structural error in the Krule rules.
 *
 * @created 2005-6-03
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/06/26 12:41:14 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class KruleStructureException extends Exception {

  static final long serialVersionUID = 7335065544118029963L;

  /**
   * String constructor.
   *
   * @param message The exception message.
   */
  public KruleStructureException(String message) {
    super(message);
  }

  /**
   * Chained constructor.
   *
   * @param message The exception message.
   * @param ex The chained exception.
   */
  public KruleStructureException(String message, Exception ex) {
    super(message, ex);
  } 

}
