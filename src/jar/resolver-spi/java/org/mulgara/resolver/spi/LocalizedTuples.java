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

import java.io.IOException;
import java.util.*;

// Third party packages
import org.apache.log4j.*;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;

// Locally written packages
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.IntFile;
import org.mulgara.util.StringToLongMap;
import org.mulgara.util.TempDir;

/**
 * Wrapper around a globally valid {@link Answer} instance, converting into
 * a local {@link org.mulgara.store.tuples.Tuples}.
 *
 * @created 2003-10-28
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:50 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class LocalizedTuples extends AbstractTuples {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LocalizedTuples.class.getName());

  /** The session to localize into. */
  protected final ResolverSession session;

  /**
   * The wrapped {@link Answer} instance.
   * Treat as final except in clone.
   */
  protected Answer answer;

  /** Mapping between parsed blank node IDs and local node numbers. */
  private IntFile blankNodeIdMap;


  /** Mapping between blank node rdf:nodeIDs and local node numbers. */
  private StringToLongMap blankNodeNameMap;
  
  /** Indicates that the maps have been initialized and are no longer null. */
  private boolean mapsInitialized = false;

  /**
   * Does the localization need to be done in the persistent store.
   */
  protected boolean persist;

  /**
   * Wrap an {@link Answer} instance.
   *
   * @param globalAnswer  the global answer to wrap
   * @throws IllegalArgumentException  if <var>globalAnswer</var> is
   *                                   <code>null</code>
   */
  public LocalizedTuples(ResolverSession session, Answer globalAnswer, boolean persist)
  {
    if (session == null) {
      throw new IllegalArgumentException("Null \"session\" parameter");
    }
    if (globalAnswer == null) {
      throw new IllegalArgumentException("Null \"globalAnswer\" parameter");
    }

    this.session = session;
    answer = (Answer) globalAnswer.clone();
    setVariables(answer.getVariables());
    
    this.persist = persist;
  }

  public LocalizedTuples(ResolverSession session, Answer globalAnswer) throws TuplesException
  {
    this(session, globalAnswer, false);
  }

  public void beforeFirst() throws TuplesException {
    answer.beforeFirst();
  }


  public void beforeFirst(long[] prefix, int suffixTruncation)
      throws TuplesException
  {
    if (prefix.length == 0 && suffixTruncation == 0) {
      answer.beforeFirst();
    } else {
      throw new TuplesException(
          "LocalizedTuples.beforeFirst not implemented for prefix length " +
          prefix.length + " and suffix length " + suffixTruncation);
    }
  }


  public Object clone() {
    LocalizedTuples copy = (LocalizedTuples)super.clone();
    copy.answer = (Answer)answer.clone();
    return copy;
  }


  public void close() throws TuplesException {
    answer.close();
    if (mapsInitialized) {
      blankNodeNameMap.delete();
      try {
        blankNodeIdMap.delete();
      } catch (IOException ioe) {
        throw new TuplesException("Unable to manage temporary files", ioe);
      }
    }
  }


  public long getColumnValue(int column) throws TuplesException {
    try {
      Object node = answer.getObject(column);
      assert node instanceof Node;
      if (node instanceof BlankNode) return localizeBlankNode((BlankNode)node);

      return persist
          ? session.localizePersistent((Node)node)
          : session.localize((Node)node);
    } catch (NodePoolException e) {
      throw new TuplesException("Couldn't create new node to localize column " + column, e);
    } catch (Exception e) {
      throw new TuplesException("Couldn't localize column " + column, e);
    }
  }

  private long newBlankNode() throws NodePoolException, LocalizeException {
    return persist ? session.newBlankNode() : session.localize(new BlankNodeImpl());
  }

  public long getRowCount() throws TuplesException {
    return answer.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return answer.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return answer.getRowExpectedCount();
  }

  public boolean isEmpty() throws TuplesException {
    return answer.isEmpty();
  }

  /**
   * We can't possibly know whether an {@link Answer} column might be
   * <code>null</code> without materializing it, so we have to assume it could
   * be.
   *
   * @return <code>true</code>
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return true;
  }


  // We may not be able to trust the Answer received from a distributed query.
  public boolean hasNoDuplicates() {
    return false;
  }


  public List<Tuples> getOperands() {
    return new ArrayList<Tuples>(0);
  }


  public boolean next() throws TuplesException {
    return answer.next();
  }


  /**
   * Converts a blank node into a local gNode ID.
   * @param node The blank node to localize.
   * @return A gNode ID that is unique and reproducable for the blank node.
   * @throws NodePoolException There was an error allocating a new gNode ID.
   * @throws LocalizeException There was an error recalling an earlier conversion, or mixed local and remote nodes.
   * @throws TuplesException There was an error creating the maps needed for this tuples.
   * @throws IOException There was an error communicating with files used for recalling conversions.
   */
  private long localizeBlankNode(BlankNode node) throws NodePoolException, LocalizeException, TuplesException, IOException {
    long nodeId;
    if (node instanceof BlankNodeImpl) {
      nodeId = ((BlankNodeImpl)node).getNodeId();
      if (nodeId < 0) {
        long foreignId = -nodeId;
        if (mapsInitialized) {
          nodeId = blankNodeIdMap.getLong(foreignId);
        } else {
          initMaps();
          nodeId = 0;
        }
        if (nodeId == 0) {
          nodeId = newBlankNode();
          blankNodeIdMap.putLong(foreignId, nodeId);
        }
      }
    } else {
      String foreignIdStr = node.toString();
      if (mapsInitialized) {
        nodeId = blankNodeNameMap.get(foreignIdStr);
      } else {
        initMaps();
        nodeId = 0;
      }
      if (nodeId == 0) {
        nodeId = newBlankNode();
        blankNodeNameMap.put(foreignIdStr, nodeId);
      }
    }
    return nodeId;
  }

  
  private void initMaps() throws TuplesException {
    try {
      blankNodeIdMap = IntFile.open(TempDir.createTempFile("localIdMap", null), true);
      blankNodeNameMap = new StringToLongMap();
    } catch (IOException ioe) {
      throw new TuplesException("Unable to localize tuples", ioe);
    }
    mapsInitialized = true;
  }

}
