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
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.SPURI;
import org.mulgara.store.stringpool.StringPool;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAResolverSession;
import org.mulgara.store.xa.XAStringPool;
import org.mulgara.util.LongMapper;
import org.mulgara.util.QueryParams;
import org.mulgara.util.StackTrace;

/**
 * A database session.
 *
 * @created 2004-04-26
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:10 $ by $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StringPoolSession implements XAResolverSession, BackupRestoreSession {
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(StringPoolSession.class.getName());

  /**
   * Stopgap to deal with the lack of a &quot;no match&quot; return value from the
   * string pool.
   */
  private static final long NONE = NodePool.NONE;

  private static final int OBTAIN   = 0;
  private static final int PREPARE  = 1;
  private static final int COMMIT   = 2;
  private static final int ROLLBACK = 3;
  private static final int RELEASE  = 4;

  static final int READ = 0;
  /** Query stringpool, create node if not found */
  static final int WRITE = 1;
  /** Query temp stringpool then persistent, node will be created in temp stringpool if required */
  static final int TEMP = 0;
  /** Query only persistent stringpool, node will be created in persistent stringpool if required */
  static final int PERSIST = 2;

  /** Extracts RW_FLAG */
  static final int WRITE_MASK = 1;
  /** Extracts STORE_FLAG */
  static final int STORE_MASK = 2;

  /** The name of the graph parameter in a URI */
  static final String GRAPH = "graph";

  /** The system URI scheme */
  static final String RMI_SCHEME = "rmi";

  /** The relative name of the system graph */
  static final String SYSTEM_GRAPH_NAME = "#";

  /** The relative URI for the system graph */
  static final URI SYSTEM_GRAPH_URI = URI.create(SYSTEM_GRAPH_NAME);

  /** The unique {@link URI} naming this database. */
  private final URI databaseURI;

  /** The set of alternative hostnames for the current host. */
  private final Set<String> hostnameAliases;

  /** Where to store literals for this phase.  */
  private XAStringPool persistentStringPool;

  /** The source of nodes for this phase.  */
  private XANodePool persistentNodePool;

  /** The source of nodes which won't outlive this session.  */
  private final NodePool temporaryNodePool;

  /** Where to store literals which won't outlive this session.  */
  private final StringPool temporaryStringPool;

  /** An enumeration of the various states for this session. */
  private int state;

  /** A lock for managing the modification of state. */
  private static Lock stateLock = new ReentrantLock();

  private SimpleXAResource[] resources;

  private Object globalLock;

  private Thread currentThread;

  StringPoolSession(URI          databaseURI,
                    Set<String>  hostnameAliases,
                    XAStringPool persistentStringPool,
                    XANodePool   persistentNodePool,
                    StringPool   temporaryStringPool,
                    NodePool     temporaryNodePool,
                    Object globalLock
  ) {

    if (logger.isDebugEnabled()) {
      logger.debug("Constructing StringPoolSession " + System.identityHashCode(this) + "\n" + new StackTrace());
    }

    assert databaseURI.getFragment() == null;

    this.databaseURI = databaseURI;
    this.hostnameAliases = hostnameAliases;
    this.persistentStringPool = persistentStringPool;
    this.persistentNodePool = persistentNodePool;
    this.temporaryStringPool = temporaryStringPool;
    this.temporaryNodePool = temporaryNodePool;
    this.globalLock = globalLock;
    this.state = OBTAIN;
    this.currentThread = null;
    this.persistentStringPool.setNodePool(this.persistentNodePool);
  }


  //
  // Globalize/Localize methods.
  //

  public Node globalize(long localNode) throws GlobalizeException {
    // this should not require guarding, as read-only operations will usually not be on the current phase
    // any reads on the current phase are about to start failing anyway if the state changes under us
    // this should not require guarding, as read-only operations will usually not be on the current phase
    // any reads on the current phase are about to start failing anyway if the state changes under us
    if (state == ROLLBACK || state == RELEASE) {
      throw new GlobalizeException(localNode, "Attempting to globalize outside transaction.");
    }

    // Validate "localNode" parameter
    if (localNode == NONE) {
      throw new IllegalArgumentException("NONE isn't a local node");
    }

    // Look up the local node in the string pool
    SPObject spObject;
    try {
      if (localNode < 0) {
        spObject = temporaryStringPool.findSPObject(-localNode);
      } else {
        spObject = mapAbsolute(persistentStringPool.findSPObject(localNode));
      }
    } catch (StringPoolException e) {
      throw new GlobalizeException(localNode, "String pool lookup failed", e);
    }

    // Generate and return the corresponding RDF node
    Node node = globalizeBlankNode(localNode, spObject);
    assert node != null;

    // Return the RDF node
    return node;
  }


  public long lookup(Node node) throws LocalizeException {
    return localize(node, READ | TEMP);
  }

  public long lookupPersistent(Node node) throws LocalizeException {
    return localize(node, READ | PERSIST);
  }

  public long localize(Node node) throws LocalizeException {
    return localize(node, WRITE | TEMP);
  }

  public long localizePersistent(Node node) throws LocalizeException {
    checkCurrentThread();
    try {
      return localize(node, WRITE | PERSIST);
    } finally {
      releaseCurrentThread();
    }
  }

  public long newBlankNode() throws NodePoolException {
    checkCurrentThread();
    try {
      return persistentNodePool.newNode();
    } finally {
      releaseCurrentThread();
    }
  }

  public void refresh(SimpleXAResource[] resources) throws SimpleXAResourceException {
    checkCurrentThread();
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Obtaining phase on StringPoolSession " + System.identityHashCode(this));
      }
      this.resources = resources;

      synchronized (this.globalLock) {
        this.persistentStringPool.refresh();  // calls refresh on the node pool
        // !!Review: Call rollback on temporary? NB. Can't rollback non XA-SP/NP.
        //this.temporaryStringPool.refresh();
        //this.temporaryNodePool.refresh();

        for (int i = 0; i < this.resources.length; i++) {
          this.resources[i].refresh();
        }
      }
    } finally {
      releaseCurrentThread();
    }
  }


  public void prepare() throws SimpleXAResourceException {
    checkCurrentThread();
    try {
      stateLock.lock();
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Preparing phase on StringPoolSession " + System.identityHashCode(this) + " SP=" + System.identityHashCode(persistentStringPool));
        }
        if (state == PREPARE) {
          return;
        } else if (state != OBTAIN) {
          throw new SimpleXAResourceException("Attempting to prepare phase without obtaining phase");
        }
    
        state = PREPARE;
    
        persistentStringPool.prepare();  // calls prepare on the node pool
        for (int i = 0; i < resources.length; i++) resources[i].prepare();
      } finally {
        stateLock.unlock();
      }
    } finally {
      releaseCurrentThread();
    }
  }


  public void commit() throws SimpleXAResourceException {
    checkCurrentThread();
    try {
      stateLock.lock();
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Committing phase on StringPoolSession " + System.identityHashCode(this));
        }
        if (state == COMMIT) {
          return;
        } else if (state != PREPARE) {
          throw new SimpleXAResourceException("Attempting to commit phase without preparing");
        }
    
        state = COMMIT;

        // now holding BOTH the stateLock AND the globalLock. This should be short!
        synchronized (globalLock) {
          persistentStringPool.commit();  // calls commit() on the node pool
          for (int i = 0; i < resources.length; i++) resources[i].commit();
        }
      } finally {
        stateLock.unlock();
      }
    } finally {
      releaseCurrentThread();
    }
  }


  public void rollback() throws SimpleXAResourceException {
    checkCurrentThread();
    try {
      stateLock.lock();
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Rollback phase on StringPoolSession " + System.identityHashCode(this));
        }
        if (state == RELEASE) {
          throw new SimpleXAResourceException("Attempting to rollback phase outside transaction");
        }
        state = ROLLBACK;
        persistentStringPool.rollback();  // calls rollback on the node pool
        for (int i = 0; i < resources.length; i++) resources[i].rollback();
      } finally {
        stateLock.unlock();
      }
    } finally {
      releaseCurrentThread();
    }
  }


  public void release() throws SimpleXAResourceException {
    checkCurrentThread();
    try {
      stateLock.lock();
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Release phase on StringPoolSession " + System.identityHashCode(this));
        }
        if (state == RELEASE) {
          return;
        } else if (state != COMMIT && state != ROLLBACK) {
          throw new SimpleXAResourceException("Attempting to release phase without commit or rollback");
        }
    
        state = RELEASE;
    
        persistentStringPool.release(); // calls release on the node pool
    
        // TODO determine if release() should be called for the temp components.
        //temporaryStringPool.release();
        //temporaryNodePool.release();
        for (int i = 0; i < resources.length; i++) resources[i].release();
      } finally {
        stateLock.unlock();
      }
    } finally {
      releaseCurrentThread();
    }
  }


  /**
   * {@inheritDoc}
   */
  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    try {
      // get the nodes from both string pools
      Tuples[] tuples = new Tuples[2];
      tuples[0] = persistentStringPool.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);
      tuples[1] = temporaryStringPool.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);

      Tuples result = appendTuples(tuples);
      tuples[0].close();
      tuples[1].close();
      return result;
    } catch (TuplesException te) {
      throw new StringPoolException(te);
    }
  }


  /**
   * {@inheritDoc}
   */
  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    try {
      // get the nodes from both string pools
      Tuples[] tuples = new Tuples[2];
      tuples[0] = persistentStringPool.findGNodes(typeCategory, typeURI);
      tuples[1] = temporaryStringPool.findGNodes(typeCategory, typeURI);

      return appendTuples(tuples);
    } catch (TuplesException te) {
      throw new StringPoolException(te);
    }
  }


  /**
   * {@inheritDoc}
   */
  public SPObject findStringPoolObject(long gNode) throws StringPoolException {
    // Container for our SPObject
    SPObject spo = null;

    if (gNode >= NodePool.MIN_NODE) {
      if (logger.isDebugEnabled()) {
        logger.debug("!! Searching for persistent node from id: " + gNode);
      }

      // Check if we have a persistent Node and find it if we have
      spo = mapAbsolute(persistentStringPool.findSPObject(gNode));
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("!! Searching for temporary node from id: " + gNode);
      }

      // We have a temporary node so check the temporary pool (Using the
      // inverted negative id)
      spo = temporaryStringPool.findSPObject(-gNode);
    }

    return spo;
  }

  /**
   * Retrieve the SPObject factory from the stringpool to allow for the creation
   * of new SPObjects.
   *
   * @return The factory to allow for creation of SPObjects
   */
  public SPObjectFactory getSPObjectFactory() {

    return persistentStringPool.getSPObjectFactory();
  }

  //
  // Type specific localize methods.
  //

  protected long localize(Node node, int flags) throws LocalizeException
  {
    // this should not require guarding, as read-only operations will usually not be on the current phase
    // any reads on the current phase are about to start failing anyway if the state changes under us
    if (state != OBTAIN) {
      throw new LocalizeException(node, "Attempting to localize outside transaction (STATE = " + state + ") " + System.identityHashCode(this));
    }

    if (node == null) {
      throw new IllegalArgumentException("Null 'node' parameter");
    }

    if (node instanceof BlankNode) {
      return localizeBlankNode((BlankNode)node, flags);
    }

    SPObjectFactory spoFactory = persistentStringPool.getSPObjectFactory();
    SPObject spObject;
    try {
      spObject = spoFactory.newSPObject(node);
    } catch (RuntimeException ex) {
      throw new LocalizeException(node, "Couldn't convert Node to SPObject", ex);
    }
    assert spObject != null;

    try {
      return localizeSPObject(spObject, flags);
    } catch (NodePoolException e) {
      throw new LocalizeException(node, "Couldn't localize node", e);
    } catch (StringPoolException e) {
      throw new LocalizeException(node, "Couldn't localize node", e);
    }
  }


  protected long localizeBlankNode(BlankNode node, int flags) throws LocalizeException {
    try {

      // Check to see that it's a blank node impl (a Mulgara blank node)
      if (node instanceof BlankNodeImpl) {
        BlankNodeImpl bi = (BlankNodeImpl) node;

        // If the blank node id is greater then zero return it.
        // FIXME: we should be checking that the BlankNodeImpl came from the
        //        correct phase, otherwise it is invalid to extract the NodeId.
        if (bi.getNodeId() > 0) return bi.getNodeId();

        // If the blank node does not have a blank node id and we are in a read
        // phase then throw an exception.
        if ((bi.getNodeId() == 0) && ((flags & WRITE_MASK) == READ)) {
          throw new LocalizeException(node, "Attempt to get a node ID from a non-allocated BlankNodeImpl in a read phase");
        }

        // If we are in a write phase.
        if ((flags & WRITE_MASK) == WRITE) {

          // If the blank node if less than zero (query node) and we are
          // persisting.
          if ((bi.getNodeId() < 0) && ((flags & STORE_MASK) == PERSIST)) {
            bi.setNodeId(persistentNodePool.newNode());
          } else if (bi.getNodeId() == 0) {
            if ((flags & STORE_MASK) == TEMP) {
              bi.setNodeId(-temporaryNodePool.newNode());
            } else {
              bi.setNodeId(persistentNodePool.newNode());
            }
          }
          return bi.getNodeId();
        }
        // Throw an exception here if we're in a read phase and the blank node
        // id is negative.
        throw new LocalizeException(node, "Attempt to persist a local blank node in a read phase");
      } else if ((flags & WRITE_MASK) == WRITE) {
      // Some other implementation of BlankNode, so we can't access internal
      // node ID and we can only create one - we must be in the WRITE phase.
        return getAllocatedNodeId(node, flags);
      } else {
        // If it's a read phase and not the local BlankNode then throw an
        // exception.
        throw new LocalizeException(node, "Attempt to read BlankNode from stringpool");
      }
    } catch (NodePoolException e) {
      throw new LocalizeException(node, "Couldn't create blank node", e);
    }
  }

  /**
   * Allocates new node IDs for unknown nodes.  Stores node IDs for later lookups.
   * @param bn The blank node to get the ID for.
   * @param flags Indicates the type of storage for the node ids.
   * @return The node ID for this given blank node.
   * @throws NodePoolException An error while allocating a new node.
   */
  protected long getAllocatedNodeId(BlankNode bn, int flags) throws NodePoolException {
    assert !(bn instanceof BlankNodeImpl);

    long nodeId;
    if ((flags & STORE_MASK) == TEMP) {
      nodeId = -temporaryNodePool.newNode();
    } else {
      nodeId = persistentNodePool.newNode();
    }
    return nodeId;
  }

  protected Node globalizeBlankNode(long localNode, SPObject spObject) throws GlobalizeException {

    return (spObject == null) ? new BlankNodeImpl(localNode) : spObject.getRDFNode();
  }


  private long localizeSPObject(SPObject spObject, int flags) throws StringPoolException, NodePoolException {

    boolean persistent = true;
    SPObject relativeSPObject = mapRelative(spObject);
    long localNode = persistentStringPool.findGNode(relativeSPObject);

    // If not found persistently then try the temp pool if permitted.
    if (localNode == NONE && ((flags & STORE_MASK) == TEMP)) {
      localNode = temporaryStringPool.findGNode(spObject);
      persistent = false;
    }

    /* The following block could cause misbehavior if someone tries to
       globalize a local node which was originally transient, but was later
       "promoted" to being persistent.

    if (localNode == NONE && ((flags & STORE_MASK) == PERSIST)) {
      localNode = temporaryStringPool.findGNode(spObject);
      if (localNode != NONE) {
        // The node exists in the temporary pool, but not in the persistent
        // pool, and we've been directed to write it into the persistent pool.
        // We can therefore remove the version in the temporary pool.
        if (logger.isDebugEnabled()) {
          logger.debug("Removing " + spObject + " as " + localNode + " from temeporary sp " + temporaryStringPool + " because it's about to be written persistently");
        }
        temporaryStringPool.remove(localNode);
        localNode = NONE;
      }
    }
    */

    // If the literal wasn't already in the string pool, create it if requested.
    if (localNode == NONE) {
      if ((flags & WRITE_MASK) == WRITE) {
        // Node does not already exist: create node in requested pool.
        if ((flags & STORE_MASK) == PERSIST) {
          localNode = persistentStringPool.put(relativeSPObject);  // allocates from the internal node pool
          if (logger.isDebugEnabled()) {
            //logger.debug("Inserted " + spObject + " as " + localNode + " into persistent sp");
          }
        } else {
          localNode = temporaryNodePool.newNode();
          temporaryStringPool.put(localNode, spObject);
          if (logger.isDebugEnabled()) {
            //logger.debug("Inserted " + spObject + " as " + localNode + " into temporary sp");
          }
          persistent = false;
        }
      } else {
        // Node not found when expected: throw exception.
        throw new StringPoolException("Unable to find literal in StringPool");
      }
    }
    // Node was found or has been successfully created in requested pool.
    assert localNode != NONE;

//    logger.warn("Localized " + spObject + " as " + (persistent ? localNode : -localNode));

    return persistent ? localNode : -localNode;
  }


  private SPObject mapAbsolute(SPObject spObject) {
    if (
        spObject != null &&
        spObject.getTypeCategory() == SPObject.TypeCategory.URI
    ) {
      URI uri = ((SPURI)spObject).getURI();
      if (!uri.isAbsolute()) {
        // Graph URIs are stored as a relative URI containing only a fragment.
        // Relative URIs with both a query string and a fragment are also used
        // for views.

        SPObjectFactory spObjectFactory =
            persistentStringPool.getSPObjectFactory();

        try {
          // Construct an absolute URI based on the database URI.
          String query = uri.getQuery();
          String ssp = databaseURI.getSchemeSpecificPart();
          if (query != null) ssp += '?' + query;
          String fragment = uri.getFragment();
          if (fragment != null) {
            // this is a graph fragment
            spObject = spObjectFactory.newSPURI(new URI(databaseURI.getScheme(), ssp, uri.getFragment()));
          } else {
            // this is a path-relative URI
            String relPath = uri.getSchemeSpecificPart();
            // ensure that the relPath can be concatenated to the ssp
            if (ssp.endsWith("/")) {
              if (relPath.startsWith("/")) relPath = relPath.substring(1);
            } else {
              if (!relPath.startsWith("/")) relPath = "/" + relPath;
            }
            spObject = spObjectFactory.newSPURI(new URI(databaseURI.getScheme(), ssp + relPath, null));
          }
        } catch (URISyntaxException ex) {
          logger.warn(
              "Cannot create absolute URI with base:\"" + databaseURI +
              "\", query:\"" + uri.getQuery() + "\", fragment:\"" +
              uri.getFragment() + "\"", ex
          );
        }
      }
    }
    return spObject;
  }


  private SPObject mapRelative(SPObject spObject) {
    if (
        spObject != null &&
        spObject.getTypeCategory() == SPObject.TypeCategory.URI
    ) {
      URI uri = ((SPURI)spObject).getURI();

      // Check if the URI is relative to the database URI.
      // The user info of the uri is ignored and is stripped from the URI if it
      // ends up being relativized.
      String scheme = uri.getScheme();
      String fragment = uri.getFragment();

      // we're only going to fiddle with this if the database scheme is RMI
      if (scheme != null && scheme.equals(databaseURI.getScheme()) && scheme.equals(RMI_SCHEME)) {
        if (databaseURI.isOpaque()) {
          // databaseURI is opaque.
          if (fragment != null && uri.isOpaque()) {
            // Get the query string.
            // We have to do it this way for opaque URIs.
            String ssp = uri.getSchemeSpecificPart();
            String query;
            int qIndex = ssp.indexOf('?');
            if (qIndex >= 0) {
              query = ssp.substring(qIndex + 1);
              ssp = ssp.substring(0, qIndex);
            } else {
              query = null;
            }

            if (ssp.equals(databaseURI.getSchemeSpecificPart())) {
              // Construct a new relative uri with just the fragment and
              // optional query string.
              SPObjectFactory spObjectFactory = persistentStringPool.getSPObjectFactory();
              try {
                spObject = spObjectFactory.newSPURI(new URI(null, null, null, query, fragment));
              } catch (URISyntaxException ex) {
                logger.warn("Cannot create relative URI with fragment:\"" + fragment + "\"", ex);
              }
            }
          }
        } else {
          // databaseURI is hierarchial.
          String path = null;
          String host;

          if (
              !uri.isOpaque() && (
                  uri.getSchemeSpecificPart().equals(
                      databaseURI.getSchemeSpecificPart()
                  ) || (
                      (host = uri.getHost()) != null &&
                      uri.getPort() == databaseURI.getPort() &&
                      (path = uri.getPath()) != null &&
                      path.startsWith(databaseURI.getPath()) &&
                      hostnameAliases.contains(host.toLowerCase())
                  )
              )
          ) {
            // Construct a new relative uri with just the fragment and
            // optional query string.
            SPObjectFactory spObjectFactory = persistentStringPool.getSPObjectFactory();
            QueryParams query = QueryParams.decode(uri);
            String gName = query.get(GRAPH);
            if (path == null) path = uri.getPath();
            String dbPath = databaseURI.getPath();

            if (gName != null) {
              // wrapped graph name
              try {
                spObject = spObjectFactory.newSPURI(new URI(gName));
              } catch (URISyntaxException ex) {
                logger.warn("Cannot extract a valid URI from:\"" + gName + "\"", ex);
              }
            } else if (!path.equals(dbPath)) {
              // relative URI
              path = path.substring(dbPath.length());
              if (path.startsWith("/")) path = path.substring(1);
              try {
                spObject = spObjectFactory.newSPURI(new URI(null, null, path, uri.getQuery(), fragment));
              } catch (URISyntaxException ex) {
                logger.warn("Cannot create relative URI with path:\"" + path + "\"", ex);
              }
            } else if (fragment != null) {
              // fragment graph name
              try {
                spObject = spObjectFactory.newSPURI(new URI(null, null, null, uri.getQuery(), fragment));
              } catch (URISyntaxException ex) {
                logger.warn("Cannot create relative URI with fragment:\"" + fragment + "\"", ex);
              }
            }
          }
        }
      } else if (scheme != null && scheme.equals(databaseURI.getScheme())) {
        // not RMI, but we still want to catch the system graph
        if (uri.toString().equals(databaseURI.toString() + SYSTEM_GRAPH_NAME)) {
          SPObjectFactory spObjectFactory = persistentStringPool.getSPObjectFactory();
          spObject = spObjectFactory.newSPURI(SYSTEM_GRAPH_URI);
        }
      }
    }
    return spObject;
  }


  /**
   * Internal helper method to append tuples.
   *
   * @param tuples Array of tuples to be unioned.  Each element will be closed.
   * @return A single tuples containing the contents of each of the elements in the tuples array.
   * @throws TuplesException Internal exception while manipulating tuples.
   */
  private Tuples appendTuples(Tuples[] tuples) throws TuplesException {
    assert tuples[0] != null && tuples[1] != null;

    // TODO: check to make sure these have the same variable names

    int t;

    if (logger.isDebugEnabled()) {
      for (t = 0; t < tuples.length; t++) {
        logger.debug("concatenating " + tuples[t].getRowCount() + " stringpool objects");
      }
    }

    // sort each tuples object
    Tuples[] st = new Tuples[tuples.length];
    for (t = 0; t < tuples.length; t++) {
      st[t] = TuplesOperations.sort(tuples[t]);
      // close the original tuples
      tuples[t].close();
    }

    // union the sorted tuples
    Tuples result = TuplesOperations.append(Arrays.asList(st));
    // close the sorted tuples
    for (t = 0; t < st.length; t++) {
      st[t].close();
    }

    return result;
  }

  /**
   * {@inheritDoc}
   *
   * NB: This method does not perform any absolute/relative URI mapping.
   */
  public SPObject findSPObject(long gNode) throws StringPoolException {
    if (gNode < NodePool.MIN_NODE) {
      throw new IllegalArgumentException("Attempt to resolve temporary gNode in BackupRestoreSession");
    }

    return persistentStringPool.findSPObject(gNode);
  }

  public long findGNode(SPObject spObject) throws StringPoolException {
    return persistentStringPool.findGNode(spObject, persistentNodePool);
  }


  /** @see org.mulgara.resolver.spi.BackupRestoreSession#getRestoreMapper() */
  public LongMapper getRestoreMapper() throws Exception {
    return persistentNodePool.getNodeMapper();
  }


  /**
   * Used purely as a sanity check in the hope that we might catch concurrency bugs in higher layers should
   * they exist.
   */
  private void checkCurrentThread() {
    synchronized(this) {
      if (currentThread == null || currentThread.equals(Thread.currentThread())) {
        currentThread = Thread.currentThread();
      } else {
        logger.warn("Concurrent Access of StringPoolSession Attempted");
        throw new IllegalStateException("Concurrent Access of StringPoolSession Attempted");
      }
    }
  }

  private void releaseCurrentThread() {
    synchronized(this) {
      currentThread = null;
    }
  }

}
