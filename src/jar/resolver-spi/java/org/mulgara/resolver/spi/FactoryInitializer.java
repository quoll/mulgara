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
import java.io.File;

// Local packages
import org.mulgara.store.nodepool.NodePoolFactory;
import org.mulgara.store.stringpool.StringPoolFactory;

/**
 * Interface defining the common initialization services which
 * {@link NodePoolFactory}, {@link ResolverFactory} and
 * {@link StringPoolFactory} must be provided with in order to plug into a
 * database.
 *
 * Initialization services are only provided at initialization.  If a client
 * tries to hold on to a reference to a {@link FactoryInitializer} after
 * initialization, calling any of the methods of this interface should
 * throw {@link IllegalStateException}.
 *
 * @created 2004-05-31
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface FactoryInitializer {

  /**
   * Obtain the canonical name of this database.
   * @return the unique {@link URI} naming this database.
   * @throws IllegalStateException if called outside of initialization
   */
  public URI getDatabaseURI();

  /**
   * Obtain the set of alternative names that the current host is known by.
   * @return the Set of Strings that enumerates the alternative hostnames.
   * @throws IllegalStateException if called outside of initialization
   */
  public Set<String> getHostnameAliases();

  /**
   * Obtain a persistence directory.
   *
   * @return a subdirectory in the database's persistence directory for the
   *   factory's exclusive use, or <code>null</code> if the database has no
   *   persistence directory configured for this resolver factory's use
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the requested <var>subdirectory</var> is
   *   not available
   */
  public File getDirectory() throws InitializerException;

  /**
   * Obtain an array of persistence directories.
   *
   * @return an array of directories for the factory's exclusive use, or <code>null</code>
   *   if the database has no persistence directory configured for this resolver factory's use.
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the requested <var>subdirectory</var> is not available
   */
  public File[] getDirectories() throws InitializerException;

}
