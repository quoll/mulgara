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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.resolver.spi.*;

/**
 * An {@link Operation} that implements the {@link org.mulgara.server.Session#login(URI, String, char[])} method.
 *
 * @created 2004-11-24
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class LoginOperation implements Operation {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LoginOperation.class.getName());

  /**
   * The URI of the security domain to authenticate to.
   */
  private final URI securityDomain;

  /**
   * User name.
   */
  private final String user;

  /**
   * Password.
   */
  private final char[] password;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param securityDomain  the {@link URI} of the security domain to
   *   authenticate to, never <code>null</code>
   * @param user  user name, never <code>null</code>
   * @param password  password, never <code>null</code>
   * @throws IllegalArgumentException if <var>modelURI</var> is
   *   <code>null</code>
   */
  LoginOperation(URI securityDomain, String user, char[] password)
  {
    // Validate "securityDomain" parameter
    if (securityDomain == null) {
      throw new IllegalArgumentException("Null \"securityDomain\" parameter");
    }

    // Initialize fields
    this.securityDomain = securityDomain;
    this.user           = user;
    this.password       = password;
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception
  {
    if (securityDomain.equals(metadata.getSecurityDomainURI())) {
      // Propagate the login event to the security adapters
      for (SecurityAdapter adapter: operationContext.getSecurityAdapterList()) {
        adapter.login(user, password);
      }
    } 
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation()
  {
    return false;
  }
}
