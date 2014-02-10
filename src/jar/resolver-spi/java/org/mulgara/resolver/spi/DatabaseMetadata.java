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

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.net.URI;
import java.util.Set;

// Local packages

/**
 * Provides access to the various fundamental URI's and associated preallocated
 * local nodes associated with a Database.
 *
 * @created 2003-12-01
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2003-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface DatabaseMetadata {
  /**
   * Returns the server name typically server1, server2, etc. which is the path
   * part of the URI.
   *
   * @return String the server name.
   */
  public String getServerName();

  /**
   * A consistent method of extracting a server's name.  Gets the path and
   * removes all instance of /.
   *
   * @return the name of the server based on the URI or null if no path is
   *   found.
   */
  public String getServerName(URI serverURI);

  public URI getURI();
  public URI getSecurityDomainURI();
  public URI getSystemModelURI();
  public URI getSystemModelTypeURI();
  public URI getRdfTypeURI();
  public Set<String> getHostnameAliases();

  public long getSystemModelNode();
  public long getSystemModelTypeNode();
  public long getRdfTypeNode();

  public URI getPreallocationSubjectURI();
  public URI getPreallocationPredicateURI();
  public URI getPreallocationModelURI();

  public long getPreallocationSubjectNode();
  public long getPreallocationPredicateNode();
  public long getPreallocationModelNode();
}
