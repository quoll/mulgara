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
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import java.net.URI;

// local packages
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.LongMapper;

/**
 * A wrapper that redirects the localizing of temporary query nodes
 * to persistent local nodes.
 *
 * @created 2004-10-21
 * @author <a href="http://www.pisoftware.com/tate">Tate Jones</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class PersistentResolverSession implements ResolverSession {
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(PersistentResolverSession.class.getName());

  /** The ResolverSession to be wraped **/
  private ResolverSession resolverSession = null;

  public PersistentResolverSession(ResolverSession resolverSession) {
    this.resolverSession = resolverSession;
  }
  /*
   * @see org.mulgara.resolver.spi.ResolverSession#globalize(long)
   */
  public Node globalize(long node) throws GlobalizeException {
    return resolverSession.globalize(node);
  }

  /*
   * @see org.mulgara.resolver.spi.ResolverSession#lookup(org.jrdf.graph.Node)
   */
  public long lookup(Node node) throws LocalizeException {
    return resolverSession.lookupPersistent(node);
  }

  /*
   * @see org.mulgara.resolver.spi.ResolverSession#lookupPersistent(org.jrdf.graph.Node)
   */
  public long lookupPersistent(Node node) throws LocalizeException {
    return resolverSession.lookupPersistent(node);
  }

  /* Perform the re-direction of localize to localizePersistent
   *
   * @see org.mulgara.resolver.spi.ResolverSession#localize(org.jrdf.graph.Node)
   */
  public long localize(Node node) throws LocalizeException {
    return resolverSession.localizePersistent(node);
  }

  /*
   * @see org.mulgara.resolver.spi.ResolverSession#localizePersistent(org.jrdf.graph.Node)
   */
  public long localizePersistent(Node node) throws LocalizeException {
    return resolverSession.localizePersistent(node);
  }

  /*
   * @see org.mulgara.resolver.spi.ResolverSession#newBlankNode()
   */
  public long newBlankNode() throws NodePoolException {
    return resolverSession.newBlankNode();
  }

  /**
   * @see org.mulgara.resolver.spi.ResolverSession#findStringPoolRange(SPObject,boolean,SPObject,boolean)
   */
  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    return resolverSession.findStringPoolRange(lowValue, inclLowValue, highValue, inclHighValue);
  }

  /**
   * @see org.mulgara.resolver.spi.ResolverSession#findStringPoolType(SPObject.TypeCategory,URI)
   */
  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    return resolverSession.findStringPoolType(typeCategory, typeURI);
  }

  /**
   * @see org.mulgara.resolver.spi.ResolverSession#findStringPoolObject(long)
   */
  public SPObject findStringPoolObject(long gNode) throws StringPoolException {
    return resolverSession.findStringPoolObject(gNode);
  }

  /**
   * Retrieve the SPObject factory from the stringpool to allow for the creation
   * of new SPObjects.
   *
   * @return The factory to allow for creation of SPObjects
   */
  public SPObjectFactory getSPObjectFactory() {
    return resolverSession.getSPObjectFactory();
  }

  public SPObject findSPObject(long gNode) throws StringPoolException {
    return resolverSession.findSPObject(gNode);
  }

  public long findGNode(SPObject spObject) throws StringPoolException {
    return resolverSession.findGNode(spObject);
  }

  public LongMapper getRestoreMapper() throws Exception {
    return resolverSession.getRestoreMapper();
  }
}
