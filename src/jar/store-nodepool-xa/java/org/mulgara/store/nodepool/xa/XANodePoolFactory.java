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

package org.mulgara.store.nodepool.xa;

// Java 2 standard packages
import java.io.File;
import java.io.IOException;

// Third-party packages
import org.apache.log4j.*;

// Local packages
import org.mulgara.resolver.spi.FactoryInitializer;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.nodepool.NodePoolFactory;

/**
 * A {@link NodePoolFactory} that constructs {@link XANodePoolImpl}
 * instances.
 *
 * @created 2004-06-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:45 $
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
public class XANodePoolFactory implements NodePoolFactory
{
  /**
   * Logger.
   */
  private static final Logger logger =
    Logger.getLogger(XANodePoolFactory.class.getName());

  /**
   * The singleton instance of this class.
   */
  private static NodePoolFactory nodePoolFactory = null;

  /**
   * The base file name.
   */
  private final String baseName;

  /**
   * This constructor is only for internal use.
   *
   * Use the {@link #newInstance} method to obtain instances.
   */
  private XANodePoolFactory(String baseName) {
    this.baseName = baseName;
  }

  /**
   * Obtain the singleton instance of this factory.
   *
   * @param factoryInitializer  not used
   */
  static public NodePoolFactory newInstance(FactoryInitializer factoryInitializer)
    throws InitializerException {

    //!!FIXME: getDirectory has the side effect of creating the directory if
    //         required.  this needs to be fixed!  Down with side effects!
    File directory = factoryInitializer.getDirectory();
    if (nodePoolFactory == null) {
      // Lazily initialize the singleton instance
      nodePoolFactory = new XANodePoolFactory(
          directory.toString() + File.separatorChar + "xa"
      );
    }

    return nodePoolFactory;
  }

  //
  // Methods implementing NodePoolFactory
  //

  /**
   * {@inheritDoc NodePoolFactory}
   */
  public NodePool newNodePool() throws NodePoolException
  {
    try {
      XANodePoolImpl xaNodePoolImpl = new XANodePoolImpl(baseName);
      return xaNodePoolImpl;
    } catch (IOException e) {
      logger.error("Couldn't construct node pool", e);
      throw new NodePoolException("Couldn't construct node pool", e);
    }
  }
}
