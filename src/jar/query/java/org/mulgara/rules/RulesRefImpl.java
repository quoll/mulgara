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

// Java 2 standard packages
import java.rmi.RemoteException;

import org.mulgara.util.Rmi;

/**
 * Contains a reference to a local Rules object, while this object can be
 * shipped over RMI.
 *
 * @created 2005-6-23
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/06/26 12:42:43 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RulesRefImpl extends Rmi implements RulesRef {

  /** Generated UID. */
  private static final long serialVersionUID = 2797292807815290355L;

  /** The internal reference to the local object. */
  private Rules rules;

  /**
   * Principle constructor.
   */
  public RulesRefImpl(Rules rules) throws RemoteException {
    this.rules = rules;
  }

  /**
   * Retrieves the local rules reference.
   *
   * @return The local Rules object.
   * @throws RemoteException This should never happen, as this method is only
   *         for local access.
   */
  public Rules getRules() throws RemoteException {
    return rules;
  }

  private void writeObject(java.io.ObjectOutputStream out)
       throws java.io.IOException {
    throw new java.io.IOException("This class should not be serialized");
  }

}
