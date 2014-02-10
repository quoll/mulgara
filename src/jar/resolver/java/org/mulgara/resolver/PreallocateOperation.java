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
import java.io.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.SingletonStatements;
import org.mulgara.resolver.spi.SystemResolver;

/**
 * An {@link Operation} that implements node preallocation.
 *
 * @created 2004-11-24
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @version $Revision: $
 *
 * @modified $Date: $ by $Author: $
 *
 * @maintenanceAuthor $Author: $
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class PreallocateOperation implements Operation
{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(PreallocateOperation.class.getName());

  /**
   * The node to be preallocated
   */
  private final Node node;

  /** 
   * The localized node.
   */
  private long localNode;

  PreallocateOperation(Node node) {
    assert node != null;
    this.node = node;
  }

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception {

    this.localNode = systemResolver.localizePersistent(node);

    // Create a statement linking the node to the graph so it's never reaped
    systemResolver.modifyModel(metadata.getPreallocationModelNode(),
        new SingletonStatements(metadata.getPreallocationSubjectNode(),
              metadata.getPreallocationPredicateNode(), localNode), true);
  }

  public long getResult() {
    return localNode;
  }

  public boolean isWriteOperation() {
    return true;
  }
}
