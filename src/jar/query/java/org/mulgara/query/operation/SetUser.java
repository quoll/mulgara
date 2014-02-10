/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.operation;

import java.net.URI;

import org.mulgara.connection.Connection;


/**
 * An AST element for the COMMIT command.
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SetUser extends LocalCommand {
  
  private static final String MESSAGE = "Credential presented";

  /** The user logging in. */
  private final String user;
  
  /** The password for the user, as provided in the UI. */
  private final String password;
  
  /** The security domain to log in to. */
  private final URI securityDomain;

  /**
   * Create a new SetUser command.
   * @param user The user logging in.
   * @param password The password for the user.
   * @param securityDomain The domain to log in to.
   */
  public SetUser(String user, String password, URI securityDomain) {
    this.user = user;
    this.password = password;
    this.securityDomain = securityDomain;
  }


  /**
   * Indicates that this operation is not specific to a UI.
   * @return <code>false</code> as operation is not specific to UIs.
   */
  public boolean isUICommand() {
    return false;
  }


  /**
   * Retrieves the user name.
   * @return the user
   */
  public String getUser() {
    return user;
  }


  /**
   * Retrieves the user password.
   * @return the password
   */
  public String getPassword() {
    return password;
  }


  /**
   * Retrieves the URI of the security domain.
   * @return the securityDomain
   */
  public URI getSecurityDomain() {
    return securityDomain;
  }


  /**
   * Does nothing at the client, except to indicate that a user record has been created
   */
  public Object execute(Connection conn) {
    return setResultMessage(MESSAGE);
  }

}
