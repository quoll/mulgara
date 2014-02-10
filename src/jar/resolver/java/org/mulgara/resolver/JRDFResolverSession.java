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
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;

//JRDF
import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;

// Local packages
import org.mulgara.resolver.jrdf.BlankNodeMap;
import org.mulgara.resolver.jrdf.BlankNodeMapFactory;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.StringPool;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAStringPool;
import org.mulgara.util.UIDGenerator;


/**
 * ResolverSession that supports JRDF operations.
 *
 * @created 2004-10-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:23 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JRDFResolverSession extends StringPoolSession {

  /** Logger.  */
  private static final Logger logger =
      Logger.getLogger(JRDFResolverSession.class.getName());

  /** Used to create BlankNodeMaps */
  private BlankNodeMapFactory nodeMapFactory = null;

  /** Used to localize/globalize external BlankNodes */
  private BlankNodeMap blankNodeMap = null;


  /**
   * Constructor.
   *
   * @param databaseURI URI
   * @param aliases Set of Strings that are alternative hostnames for this host
   * @param persistentStringPool XAStringPool
   * @param persistentNodePool XANodePool
   * @param temporaryStringPool StringPool
   * @param temporaryNodePool NodePool
   * @param globalLock Object
   */
  JRDFResolverSession(URI databaseURI,
                      Set aliases,
                      XAStringPool persistentStringPool,
                      XANodePool persistentNodePool,
                      StringPool temporaryStringPool,
                      NodePool temporaryNodePool,
                      Object globalLock) {

    super(
        databaseURI, aliases, persistentStringPool, persistentNodePool,
        temporaryStringPool, temporaryNodePool, globalLock
    );

    this.nodeMapFactory = new BlankNodeMapFactory();
    logger.debug("JRDFResolverSession created.");
  }

  //
  // Globalize/Localize methods.
  //

  /**
   * Converts a local node ID to a Global Node.
   *
   * @param localNode long
   * @throws GlobalizeException
   * @return Node
   */
  public Node globalize(long localNode) throws GlobalizeException
  {

    Node node = super.globalize(localNode);

    return node;
  }

  /**
   * Checks the BlankNodeMap for the localNode. If it is not in the map, a new
   * BlankNode is mapped for the id.
   *
   * @param localNode long
   * @param spObject SPObject
   * @throws GlobalizeException
   * @return BlankNode
   */
  protected Node globalizeBlankNode(long localNode, SPObject spObject) throws
      GlobalizeException {

    BlankNode node = null;

    try {

      //check map first
      node = blankNodeMap.get(localNode);

      //was it in there?
      if (node == null) {

        //create a new mapping
        node = newJrdfBlankNode();
        blankNodeMap.put( (BlankNodeImpl) node, localNode);
      }
    } catch (GraphException graphException) {

      throw new GlobalizeException(localNode, "Failed to Globalize BlankNode.",
                                   graphException);
    }

    assert node != null;
    return node;
  }

  /**
   * Converts a Global Node to a local node id.
   *
   * @param node Node
   * @param flags int
   * @throws LocalizeException
   * @return long
   */
  protected long localize(Node node, int flags) throws LocalizeException
  {

    long localNode = super.localize(node, flags);

    return localNode;
  }

  /**
   * If the node is an org.jrdf.graph.mem.BlankNodeImpl, the BlankNodeMap is
   * checked, otherwise it is handled normally.
   *
   * @param node BlankNode
   * @param flags int
   * @throws LocalizeException
   * @return long
   */
  protected long localizeBlankNode(BlankNode node, int flags) throws
      LocalizeException {

    //BlankNodeImpl objects can be handled by this class
    if (node instanceof BlankNodeImpl) {

      return localizeBlankNodeImpl((BlankNodeImpl) node, flags);
    } else {

      return super.localizeBlankNode(node, flags);
    }
  }

  /**
   * Checks the BlankNodeMap before localizing. If the node is not in the map,
   * it is added.
   *
   * @param node BlankNodeImpl
   * @param flags int
   * @throws LocalizeException
   * @return long
   */
  protected long localizeBlankNodeImpl(BlankNodeImpl node, int flags) throws
      LocalizeException {

    long localNode = 0;

    try {

      //has it been mapped?
      localNode = blankNodeMap.get(node);
      if (localNode <= 0) {

        //get a new node id
        localNode = super.localizeBlankNode(node, flags);
        //map
        blankNodeMap.put(node, localNode);
      }
    } catch (GraphException graphException) {

      throw new LocalizeException(node, "Failed to localize BlankNode.",
                                  graphException);
    }

    assert localNode > 0;
    return localNode;
  }

  /**
   * Creates a new org.jrdf.graph.mem.BlankNodeImpl containing a new
   * Globally Unique Identifier.
   *
   * @throws GraphException
   * @return BlankNodeImpl
   */
  private BlankNodeImpl newJrdfBlankNode() throws GraphException {

    String uid = null;

    try {

      uid = UIDGenerator.generateUID();
    } catch (Exception exception) {

      throw new GraphException("Failed to create BlankNode.", exception);
    }

    //blank node will not have an id
    return (BlankNodeImpl)BlankNodeImpl.valueOf(uid + "#0");
  }

  //
  // Transaction code.
  //

  public void refresh(SimpleXAResource[] resources) throws
      SimpleXAResourceException {

    super.refresh(resources);
  }

  /**
   * Creates a new BlankNodeMap for the phase.
   * @throws SimpleXAResourceException
   */
  public void prepare() throws SimpleXAResourceException {

    super.prepare();
    createBlankNodeMap();
  }

  /**
   * Closes and Invalidates the BlankNodeMap used for the phase.
   * @throws SimpleXAResourceException
   */
  public void commit() throws SimpleXAResourceException {

    super.commit();
    deleteBlankNodeMap();
  }

  /**
   * Closes and Invalidates the BlankNodeMap used for the phase.
   * @throws SimpleXAResourceException
   */
  public void rollback() throws SimpleXAResourceException {

    super.rollback();
    deleteBlankNodeMap();
  }

  /**
   * Closes and Invalidates the BlankNodeMap used for the phase.
   * @throws SimpleXAResourceException
   */
  public void release() throws SimpleXAResourceException {

    super.release();
    deleteBlankNodeMap();
  }

  /**
   * Closes any existing BlankNodeMap and creates a new one.
   *
   * @throws SimpleXAResourceException
   */
  private void createBlankNodeMap() throws SimpleXAResourceException {

    //is there already a map?
    try {

      if (blankNodeMap != null) {

        deleteBlankNodeMap();
      }
    } catch (SimpleXAResourceException resourceException) {

      throw new SimpleXAResourceException("Failed to delete existing " +
                                          "BlankNodeMap.", resourceException);
    }

    //create a new one
    try {

      blankNodeMap = nodeMapFactory.newBlankNodeMap();
    }
    catch (GraphException graphException) {

      throw new SimpleXAResourceException("Failed to create BlankNodeMap.",
                                            graphException);
    }
  }

  /**
   * Closes and invalidates the BlankNodeMap and frees any resources associated
   * with it.
   *
   * @throws SimpleXAResourceException
   */
  private void deleteBlankNodeMap() throws SimpleXAResourceException {

    try {

      blankNodeMap.close();
      blankNodeMap = null;
    }
    catch (GraphException graphException) {

      throw new SimpleXAResourceException("Failed to close BlankNodeMap.",
                                          graphException);
    }
  }

}
