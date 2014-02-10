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

/**
 * Exception thrown when unable to initialise a {@link RuleLoader}.
 *
 * @created 2005-05-22
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/06/26 12:42:43 $ 
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy;2004 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */

public class InitializerException extends Exception {

  static final long serialVersionUID = 2100205805482232042L;

  /**
   * @param message  diagnostic text
   */
  public InitializerException(String message)
  {
    super(message);
  }

  /**
   * @param message  diagnostic text
   * @param cause  an exception to be chained as the reason for this one
   */
  public InitializerException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
