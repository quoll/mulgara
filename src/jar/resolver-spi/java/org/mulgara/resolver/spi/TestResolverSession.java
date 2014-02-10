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
 * Contributor(s):
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.util.HashMap;
import java.util.Map;
import java.net.URI;

// Third party packages
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.LongMapper;
import org.mulgara.util.MemLongMapper;

/**
 * A minimal implementation of {@link ResolverSession}.
 *
 * This isn't capable of persistence, and is only appropriate for use in
 * unit tests.
 *
 * @created 2005-05-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TestResolverSession implements ResolverSession {
  /**
   * Our pretend node pool, a counter used to generate new local node values.
   */
  private long top = 1;

  /**
   * ID's used to reference BlankNodes (top counter for blank nodes)
   * Will cause problems if more nodes (non-blank) than the initial value
   * are inserted.
   */
  private long bNode = 1000000;

  /**
   * Our pretend string pool, a map from global JRDF nodes to local
   * {@link Long}s.
   */
  private final Map<Node,Long> map = new HashMap<Node,Long>();

  /** A map to map longs to nodes.  Inverse of the String pool */
  private final Map<Long,Node> globalMap = new HashMap<Long,Node>();

  /**
   * Source of {@link SPObject}s.
   */
  private final SPObjectFactory spObjectFactory = new SPObjectFactoryImpl();

  //
  // Methods implementing ResolverSession
  //

  public Node globalize(long node) throws GlobalizeException {

    Node object = globalMap.get(node);

    if (object == null) {
      throw new GlobalizeException(node, "No such node");
    }
    return object;
  }

  public long lookup(Node node) throws LocalizeException {
    Long object = map.get(node);
    if (object == null) {
      throw new LocalizeException(node, "No such node");
    }
    return object;
  }

  public long lookupPersistent(Node node) throws LocalizeException {
    throw new LocalizeException(node, "Not implemented");
  }

  public long localize(Node node) throws LocalizeException {
    Long object = map.get(node);
    if (object == null) {
      Long id = top++;  // repeating the errors of the rdf/xml writer test class

      if (node instanceof BlankNodeImpl) {
        id = bNode++;
        ((BlankNodeImpl)node).setNodeId(bNode);
      }

      map.put(node, id);
      globalMap.put(id, node);

      return id;
    } else {
      return object;
    }
  }

  public long localizePersistent(Node node) throws LocalizeException {
    return localize(node);
  }

  public long newBlankNode() {
    throw new UnsupportedOperationException("Not implemented");
  }

  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public SPObject findStringPoolObject(long gNode) throws StringPoolException {
    Node node = (Node) globalMap.get(new Long(gNode));
    return (node == null) ? null : spObjectFactory.newSPObject(node);
  }

  /**
   * Retrieve the SPObject factory from the stringpool to allow for the creation
   * of new SPObjects.
   *
   * @return The factory to allow for creation of SPObjects
   */
  public SPObjectFactory getSPObjectFactory() {
    return spObjectFactory;
  }

  public SPObject findSPObject(long gNode) {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public long findGNode(SPObject spObject) {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public LongMapper getRestoreMapper() throws Exception {
    return new MemLongMapper();
  }
}
