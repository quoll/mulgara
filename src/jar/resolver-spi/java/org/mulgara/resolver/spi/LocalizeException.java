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
import java.io.Serializable;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J
import org.jrdf.graph.Node;      // JRDF

/**
 * Exception thrown by the {@link ResolverSession#localize} method;
 *
 * @created 2004-04-29
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class LocalizeException extends Exception
{
  /**
   * 
   */
  private static final long serialVersionUID = -2578231437826374352L;

  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(LocalizeException.class.getName());

  /** The RDF node that couldn't be localized.  */
  private final Node node;


  /**
   * Construct an exception.
   *
   * For the sake of the exception remaining {@link Serializable}, the
   * <var>node</var> parameter should be {@link Serializable}.  If this is not
   * the case, a warning will be logged.
   *
   * @param node  the RDF node that couldn't be localized; it's best for this
   *   parameter to be {@link Serializable}
   * @param message  diagnostic text
   */
  public LocalizeException(Node node, String message)
  {
    this (node, message, null);
  }

  /**
   * Construct an exception.
   *
   * For the sake of the exception remaining {@link Serializable}, the
   * <var>node</var> parameter should be {@link Serializable}.  If this is not
   * the case, a warning will be logged.
   *
   * @param node  the RDF node that couldn't be localized; it's best for this
   *   parameter to be {@link Serializable}
   * @param message  diagnostic text
   * @param cause  the exception which caused localization to fail
   */
  public LocalizeException(Node node, String message, Throwable cause)
  {
    super("Unable to localize " + node + " - " + message, cause);

    // Validate node parameter
    if (!(node instanceof Serializable)) {
      logger.warn("Node " + node + " (" + node.getClass() + ") is not serializable");
    }

    // Initialize field
    this.node = node;
  }

  /**
   * @return the RDF node that couldn't be localized
   */
  public Node getNode()
  {
    return node;
  }
}
