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

package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.io.File;
import java.io.IOException;

// Third-party packages
import org.apache.log4j.*;

// Local packages
import org.mulgara.resolver.spi.FactoryInitializer;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.store.stringpool.StringPool;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.StringPoolFactory;

/**
 * A {@link StringPoolFactory} that constructs {@link XAStringPoolImpl}
 * instances.
 *
 * @created 2004-06-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 11:06:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XAStringPoolFactory implements StringPoolFactory
{
  /**
   * Logger.
   */
  private static final Logger logger =
    Logger.getLogger(XAStringPoolFactory.class.getName());

  /**
   * The singleton instance of this class.
   */
  private static StringPoolFactory stringPoolFactory = null;

  /**
   * The base file name.
   */
  private final String baseName;

  /**
   * This constructor is only for internal use.
   *
   * Use the {@link #newInstance} method to obtain instances.
   */
  private XAStringPoolFactory(String baseName)
  {
    this.baseName = baseName;
  }

  /**
   * Obtain the singleton instance of this factory.
   *
   * @param factoryInitializer  not used
   */
  static public StringPoolFactory newInstance(FactoryInitializer factoryInitializer)
    throws InitializerException
  {
    File directory = factoryInitializer.getDirectory();
    if (stringPoolFactory == null) {
      // Lazily initialize the singleton instance
      stringPoolFactory = new XAStringPoolFactory(
          directory.toString() + File.separatorChar + "xa"
      );
    }

    return stringPoolFactory;
  }

  //
  // Methods implementing StringPoolFactory
  //

  /**
   * {@inheritDoc StringPoolFactory}
   */
  public StringPool newStringPool() throws StringPoolException
  {
    try {
      XAStringPoolImpl xaStringPoolImpl = new XAStringPoolImpl(baseName);
      return xaStringPoolImpl;
    } catch (IOException e) {
      logger.error("Couldn't construct string pool", e);
      throw new StringPoolException("Couldn't construct string pool", e);
    }
  }
}
