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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.BackupRestoreSession;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.SingletonStatements;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.IntFile;
import org.mulgara.util.LongMapper;
import org.mulgara.util.TempDir;

/**
 * An {@link Operation} that restores the state of the database from a backup
 * file generated using the complementary {@link BackupOperation}.
 *
 * If the database is not currently empty then the current contents of the database
 * will be repalced with the content of the backup file when this
 * method returns.
 *
 * @created 2004-10-07
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:09 $ by $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RestoreOperation extends TuplesBasedOperation implements BackupConstants, Operation {

  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(RestoreOperation.class.getName());

  private final InputStream inputStream;
  private final URI sourceURI;

  //
  // Constructor
  //

  /**
   * Create the operation.
   *
   * @param inputStream a client supplied inputStream to obtain the restore
   *        content from. If null assume the sourceURI has been supplied.
   * @param sourceURI The URI of the backup file to restore from.
   * @throws IllegalArgumentException if the <var>sourceURI</var> is a
   *   relative URI
   */
  public RestoreOperation(InputStream inputStream, URI sourceURI)
  {
    // Validate "sourceURI" parameter
    if (sourceURI != null && sourceURI.getScheme() == null) {
      throw new IllegalArgumentException(
          "Relative URIs are not supported as restore source");
    }

    this.inputStream = inputStream;
    this.sourceURI   = sourceURI;
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext         operationContext,
                      SystemResolver           systemResolver,
                      DatabaseMetadata         metadata) throws Exception
  {
    InputStream is = inputStream;
    BufferedReader br = null;
    try {
      // Open an is if none has been supplied.
      if (is == null) {
        is = sourceURI.toURL().openStream();
      }

      // NOTE: The BufferedInputStream is required for GZIP due to
      // incompatibilities between GZIPInputStream and RemoteInputStream.
      // (It probably helps with performance, too.)
      br = new BufferedReader(new InputStreamReader(
          new GZIPInputStream(new BufferedInputStream(is)),
          "UTF-8"
      ));

      restoreDatabase(systemResolver, systemResolver, metadata, br);
    } finally {
      try {
        if (br != null) {
          // Close the BufferedReader if it exists.  This will also close the
          // wrapped InputStream.
          br.close();
        } else if (is != null) {
          // Close the InputStream if it exists.
          is.close();
        }
      } catch (IOException e) {
        logger.warn("I/O exception closing input to system restore", e);
      }
    }
  }


  /**
   * Restore the entire database.
   *
   * @param resolver Resolver
   * @param metadata DatabaseMetadata
   * @param br BufferedReader
   */
  private void restoreDatabase(
      Resolver resolver, ResolverSession resolverSession,
      DatabaseMetadata metadata, BufferedReader br
  ) throws Exception {
    // Check the header of the backup file.
    String line = readLine(br);
    if (line == null || !line.startsWith(BACKUP_FILE_HEADER)) {
      throw new QueryException("Not a backup file");
    }
    String versionString = line.substring(BACKUP_FILE_HEADER.length());

    if (versionString.equals(BACKUP_VERSION6)) {
      assert BACKUP_VERSION6.equals("6");
      restoreDatabaseV6(resolver, resolverSession, metadata, br);
    } else if (versionString.equals(BACKUP_VERSION4)) {
      restoreDatabaseV4(resolver, resolverSession, metadata, br);
    } else {
      throw new QueryException("Unsupported backup file version: V" + versionString);
    }
  }


  private static final String TKS_NAMESPACE = "<http://pisoftware.com/tks";
  private static final String TUCANA_NAMESPACE = "<http://tucana.org/tucana";
  private static final String TKS_INT_MODEL_URI = TKS_NAMESPACE + "-int#model>";

  /**
   * Restore the entire database from a V4 backup file.
   *
   * @param resolver Resolver
   * @param resolverSession resolverSession
   * @param metadata DatabaseMetadata
   * @param br BufferedReader
   */
  private void restoreDatabaseV4(
      Resolver resolver, ResolverSession resolverSession,
      DatabaseMetadata metadata, BufferedReader br
  ) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info(
          "Loading V4 backup " + sourceURI + " which was created on: " +
          readLine(br)
      );
    }

    // Skip to the start of the RDFNODES section.
    String line;
    do {
      line = readLine(br);
      if (line == null) throw new QueryException("Unexpected EOF in header section while restoring from backup file: " + sourceURI);
    } while (!line.equals("RDFNODES"));

    // Remove all statements from store except those reserving
    // preallocated nodes.
    // TODO remove the need to preallocate nodes in this way so that we can do
    // a bulk remove of all triples in the store.
    Tuples tuples = resolver.resolve(new ConstraintImpl(
                                     StatementStore.VARIABLES[0],
                                     StatementStore.VARIABLES[1],
                                     StatementStore.VARIABLES[2],
                                     StatementStore.VARIABLES[3]));
    int[] colMap = mapColumnsToStd(tuples.getVariables());

    boolean success = false;
    try {
      tuples.beforeFirst();

      long preallocationModelNode = metadata.getPreallocationModelNode();
      while (tuples.next()) {
        long modelNode = tuples.getColumnValue(colMap[3]);
        if (modelNode != preallocationModelNode) {
          resolver.modifyModel(
              modelNode,
              new SingletonStatements(tuples.getColumnValue(colMap[0]),
                                      tuples.getColumnValue(colMap[1]),
                                      tuples.getColumnValue(colMap[2])),
              DatabaseSession.DENY_STATEMENTS
          );
        }
      }
      success = true;
    } finally {
      try {
        tuples.close();
      } catch (TuplesException e) {
        if (success) throw e; // New exception, need to re-throw it.
        else logger.info("Suppressing exception closing failed tuples", e); // Already failed, log this exception.
      }
    }

    // n2nMap maps from node IDs in the backup file to node IDs in the
    // store.
    File n2nFile = TempDir.createTempFile("n2n", null);
    IntFile n2nMap = null;
    File tplFile = TempDir.createTempFile("tpl", null);
    RandomAccessFile tplRAF = null;
    FileChannel tplFC = null;

    try {
      n2nMap = IntFile.open(n2nFile);

      SPObjectFactory spof = resolverSession.getSPObjectFactory();

      // Nodes in the backup file's coordinate space.
      long systemModelNode = BackupRestoreSession.NONE;
      long emptyGroupNode = BackupRestoreSession.NONE;
      long tksIntModelNode = BackupRestoreSession.NONE;

      // Load the strings.
      while (((line = readLine(br)) != null) && !line.equals("TRIPLES")) {
        int nrLen = line.indexOf(' ');
        long gNode = Long.parseLong(line.substring(0, nrLen));
        String str = line.substring(nrLen + 1);

        // Note the value of some gNodes and convert some values.
        if (str.equals("<#>")) {
          systemModelNode = gNode;
        } else if (str.equals("\"EMPTY_GROUP\"")) {
          emptyGroupNode = gNode;
        } else if (str.equals(TKS_INT_MODEL_URI)) {
          tksIntModelNode = gNode;
        } else {
          // Map the old tks namespace to the tucana namespace.
          if (str.startsWith(TKS_NAMESPACE)) {
            // Verify that the next char is a '#' or a '/'.
            char nextChar = str.charAt(TKS_NAMESPACE.length());
            if (nextChar == '#' || nextChar == '/') {
              // Replace the old tks namespace with the tucana namespace.
              str = TUCANA_NAMESPACE + str.substring(TKS_NAMESPACE.length());
            }
          }
        }

        // createSPObjectFromBackupEncodedString() handles the old TKS
        // double and dateTime formats.
        SPObject spObject = spof.createSPObjectFromBackupEncodedString(str);

        // If the SPObject is already in the string pool then use the
        // existing node ID, otherwise allocate a new node and put the
        // SPObject into the string pool.
        long newGNode = resolverSession.findGNode(spObject);

        n2nMap.putLong(gNode, newGNode);
      }

      if (line == null) {
        throw new QueryException(
            "Unexpected EOF in RDFNODES section while restoring from " +
            "backup file: " + sourceURI
        );
      }

      // Check that the systemModel, emptyGroup and tksIntModel nodes were
      // found.
      if (systemModelNode == BackupRestoreSession.NONE) {
        throw new QueryException(
            "The system model node \"<#>\" was not found in the RDFNODES " +
            "section of the backup file: " + sourceURI
        );
      }
      if (emptyGroupNode == BackupRestoreSession.NONE) {
        throw new QueryException(
            "The node for EMPTY_GROUP was not found in the RDFNODES " +
            "section of the backup file: " + sourceURI
        );
      }
      if (tksIntModelNode == BackupRestoreSession.NONE) {
        throw new QueryException(
            "The node for \"" + TKS_INT_MODEL_URI +
            "\" was not found in the RDFNODES section of the backup file: " +
            sourceURI
        );
      }

      // Copy the triples to a temporary file while setting up a mapping from
      // overlap group meta nodes to model nodes.

      // Open the temporary triple file.
      tplRAF = new RandomAccessFile(tplFile, "rw");
      tplFC = tplRAF.getChannel();

      long nrTriples = 0;
      ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
      LongBuffer tripleBuffer = buffer.asLongBuffer();

      // Maps from a group (Long) to a Set of models (Set of Longs).
      Map<Long,Set<Long>> g2mMap = new HashMap<Long,Set<Long>>();

      for (;;) {
        try {
          if ((line = readLine(br)) == null) {
            throw new QueryException(
                "Unexpected EOF in TRIPLES section while restoring from " +
                "backup file: " + sourceURI
            );
          }

          if (line.equals("END")) {
            // End of triples section and end of file.
            break;
          }
        } catch (IOException ioe) {
          if (ioe.getMessage().equals("Corrupt GZIP trailer")) {
            // Workaround for 4 GB limit in GZIPInputStream.
            // We get an IOException on end of file.
            break;
          }
          throw ioe;
        }

        int spc0 = line.indexOf(' ');
        assert spc0 > 0;
        long node0 = Long.parseLong(line.substring(0, spc0));
        int spc1 = line.indexOf(' ', ++spc0);
        assert spc1 > 0;
        long node1 = Long.parseLong(line.substring(spc0, spc1));
        int spc2 = line.indexOf(' ', ++spc1);
        assert spc2 > 0;
        long node2 = Long.parseLong(line.substring(spc1, spc2));
        long meta = Long.parseLong(line.substring(++spc2));

        if (meta == emptyGroupNode) {
          // Statements in the EMPTY_GROUP.
          if (node1 == tksIntModelNode) {
            // Set up a mapping from each V4 Group node to (multiple) Graph
            // nodes.
            Long groupL = new Long(node0);
            Set<Long> modelSet = g2mMap.get(groupL);
            if (modelSet == null) {
              assert n2nMap.getLong(node0) == BackupRestoreSession.NONE;
              modelSet = new HashSet<Long>();
              g2mMap.put(groupL, modelSet);
            }
            assert n2nMap.getLong(node2) != BackupRestoreSession.NONE;
            modelSet.add(new Long(getNode(n2nMap, node2, resolverSession)));

            // Mark this node as a group.  This indicates that a lookup must
            // be performed on g2mMap.
            n2nMap.putLong(node0, -1L);
          }
        } else {
          // Omit the statement that declares EMPTY_GROUP to be a Group.
          if (node0 != emptyGroupNode || meta != systemModelNode) {
            // Append all other triples to the temporary file.
            ++nrTriples;
            tripleBuffer.put(node0);
            tripleBuffer.put(node1);
            tripleBuffer.put(node2);
            tripleBuffer.put(meta);

            if (!tripleBuffer.hasRemaining()) {
              // Write out the full buffer.
              tripleBuffer.rewind();
              buffer.rewind();
              int n = tplFC.write(buffer);
              assert n == buffer.capacity();
            }
          }
        }
      }

      // Write out the last partial buffer of triples to the temporary file.
      if (tripleBuffer.position() > 0) {
        // Clear the remainder of the buffer.
        while (tripleBuffer.hasRemaining()) {
          tripleBuffer.put(0L);
        }

        buffer.rewind();
        int n = tplFC.write(buffer);
        assert n == buffer.limit();
      }

      // Rewind the temporary file.
      tplFC.position(0);

      // Ensure no remaining longs in tripleBuffer.  This will ensure that
      // the first buffer is read immediately.
      tripleBuffer.position(tripleBuffer.limit());

      // Load the triples from the temporary file.
      for (long tripleIndex = 0; tripleIndex < nrTriples; ++tripleIndex) {
        if (!tripleBuffer.hasRemaining()) {
          // Read in more triples.
          tripleBuffer.rewind();
          buffer.rewind();

          do {
            int n = tplFC.read(buffer);
            if (n == -1) {
              throw new QueryException(
                  "Premature EOF on temporary triple file (" + tplFile +
                  ") during restore from V4 backup file"
              );
            }
          } while (buffer.hasRemaining());
        }
        long node0 = getNode(n2nMap, tripleBuffer.get(), resolverSession);
        long node1 = getNode(n2nMap, tripleBuffer.get(), resolverSession);
        long node2 = getNode(n2nMap, tripleBuffer.get(), resolverSession);
        long meta = tripleBuffer.get();
        long node3 = getNode(n2nMap, meta, resolverSession);

        // TODO Write a class that implements Statements to restore the
        // entire TRIPLES section with one call to modifyModel().
        if (node3 == -1) {
          // This is a group node that maps to multiple model nodes.
          Set<Long> modelSet = g2mMap.get(new Long(meta));
          assert modelSet != null;
          for (Iterator<Long> it = modelSet.iterator(); it.hasNext(); ) {
            node3 = it.next().longValue();
            resolver.modifyModel(
              node3,
              new SingletonStatements(node0, node1, node2),
              DatabaseSession.ASSERT_STATEMENTS
            );
          }
        } else {
          resolver.modifyModel(
            node3,
            new SingletonStatements(node0, node1, node2),
            DatabaseSession.ASSERT_STATEMENTS
          );
        }
      }
    } finally {
      try {
        try {
          // Close and delete the temporary node-to-node map file.
          if (n2nMap != null) {
            n2nMap.delete();
          } else {
            n2nFile.delete();
          }
        } finally {
          // Close and delete the temporary triple file.
          if (tplFC != null) {
            tplFC.close();
          }
          if (tplRAF != null) {
            tplRAF.close();
          }
          tplFile.delete();
        }
      } catch (IOException e) {
        logger.warn("I/O error on close", e);
      }
    }
  }


  /**
   * Restore the entire database from a V6 backup file.
   *
   * @param resolver Resolver
   * @param resolverSession resolverSession
   * @param metadata DatabaseMetadata
   * @param br BufferedReader
   */
  private void restoreDatabaseV6(
      Resolver resolver, ResolverSession resolverSession,
      DatabaseMetadata metadata, BufferedReader br
  ) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info(
          "Loading V6 backup " + sourceURI + " which was created on: " +
          readLine(br)
      );
    }

    // Skip to the start of the RDFNODES section.
    String line;
    do {
      line = readLine(br);
      if (line == null) throw new QueryException("Unexpected EOF in header section while restoring from backup file: " + sourceURI);
    } while (!line.equals("RDFNODES"));

    // Remove all statements from store except those reserving
    // preallocated nodes.
    // TODO remove the need to preallocate nodes in this way so that we can do
    // a bulk remove of all triples in the store.
    Tuples tuples = resolver.resolve(new ConstraintImpl(
                                     StatementStore.VARIABLES[0],
                                     StatementStore.VARIABLES[1],
                                     StatementStore.VARIABLES[2],
                                     StatementStore.VARIABLES[3]));
    int[] colMap = mapColumnsToStd(tuples.getVariables());

    boolean success = false;
    try {
      tuples.beforeFirst();

      long preallocationModelNode = metadata.getPreallocationModelNode();
      while (tuples.next()) {
        long modelNode = tuples.getColumnValue(colMap[3]);
        if (modelNode != preallocationModelNode) {
          resolver.modifyModel(
              modelNode,
              new SingletonStatements(tuples.getColumnValue(colMap[0]),
                                      tuples.getColumnValue(colMap[1]),
                                      tuples.getColumnValue(colMap[2])),
              DatabaseSession.DENY_STATEMENTS
          );
        }
      }
      success = true;
    } finally {
      try {
        tuples.close();
      } catch (TuplesException e) {
        if (success) throw e; // New exception, need to re-throw it.
        else logger.info("Suppressing exception closing failed tuples", e); // Already failed, log this exception.
      }
    }

    // n2nMap maps from node IDs in the backup file to node IDs in the store.
    LongMapper n2nMap = null;

    try {
      n2nMap = resolverSession.getRestoreMapper();

      SPObjectFactory spof = resolverSession.getSPObjectFactory();

      // Load the strings.
      while (((line = readLine(br)) != null) && !line.equals("TRIPLES")) {
        int nrLen = line.indexOf(' ');
        long gNode = Long.parseLong(line.substring(0, nrLen));
        String str = line.substring(nrLen + 1);

        SPObject spObject = spof.createSPObjectFromEncodedString(str);

        // If the SPObject is already in the string pool then use the
        // existing node ID, otherwise allocate a new node and put the
        // SPObject into the string pool.
        long newGNode = resolverSession.findGNode(spObject);

        n2nMap.putLong(gNode, newGNode);
      }

      if (line == null) {
        throw new QueryException(
            "Unexpected EOF in RDFNODES section while restoring from backup file: " + sourceURI
        );
      }

      // Load the triples.
      for (;;) {
        try {
          if ((line = readLine(br)) == null) {
            throw new QueryException(
                "Unexpected EOF in TRIPLES section while restoring from " +
                "backup file: " + sourceURI
            );
          }

          if (line.equals("END")) {
            // End of triples section and end of file.
            break;
          }
        } catch (IOException ioe) {
          if (ioe.getMessage().equals("Corrupt GZIP trailer")) {
            // Workaround for 4 GB limit in GZIPInputStream.
            // We get an IOException on end of file.
            break;
          }
          throw ioe;
        }

        int spc0 = line.indexOf(' ');
        assert spc0 > 0;
        long node0 = Long.parseLong(line.substring(0, spc0));
        int spc1 = line.indexOf(' ', ++spc0);
        assert spc1 > 0;
        long node1 = Long.parseLong(line.substring(spc0, spc1));
        int spc2 = line.indexOf(' ', ++spc1);
        assert spc2 > 0;
        long node2 = Long.parseLong(line.substring(spc1, spc2));
        long node3 = Long.parseLong(line.substring(++spc2));

        // TODO Write a class that implements Statements to restore the
        // entire TRIPLES section with one call to modifyModel().
        resolver.modifyModel(
          getNode(n2nMap, node3, resolverSession),
          new SingletonStatements(getNode(n2nMap, node0, resolverSession),
                                  getNode(n2nMap, node1, resolverSession),
                                  getNode(n2nMap, node2, resolverSession)),
          DatabaseSession.ASSERT_STATEMENTS
        );
      }
    } finally {
      try {
        if (n2nMap != null) n2nMap.delete();
      } catch (Exception e) {
        logger.warn("I/O error on close", e);
      }
    }
  }

  /**
   * Returns the new node ID that the specified backup file node ID maps to. A
   * new node will be allocated if the node has not been seen before.
   *
   * @param n2nMap the IntFile that maps from backup file node IDs to current
   *      store node IDs.
   * @param oldNode the backup file node ID.
   * @param resolverSession Used to allocate new nodes.
   * @return the new node ID that the specified backup file node ID maps to.
   * @throws Exception EXCEPTION TO DO
   */
  private static long getNode(LongMapper n2nMap, long oldNode, ResolverSession resolverSession) throws Exception {
    long newNode = n2nMap.getLong(oldNode);

    // IntFile.getLong() returns zero for entries that have never been
    // written to.
    if (newNode == 0) {
      newNode = resolverSession.newBlankNode();
      try {
        n2nMap.putLong(oldNode, newNode);
      } catch (IOException e) {
        String m = "Error allocating new blank node for oldNode=" + oldNode + ". newNode=" + newNode + ". ";
        logger.fatal(m, e);
        throw new IOException(m + e.getMessage());
      }
    }

    return newNode;
  }

  /** Need to maintain compatibility with the largest possible items */
  private static final int MAX_LINE = 3 * org.mulgara.util.io.LMappedBufferedFileRO.PAGE_SIZE;

  /**
   * A wrapper around the {@link org.mulgara.util.io.IOUtil#readLine(BufferedReader, int)}
   * utility function, to provide a default buffer size.
   * @param br The BufferedReader to read the line from.
   * @return The string read from the reader, representing a line of text.
   * @throws IOException If there was an exception accessing the stream.
   */
  private static final String readLine(BufferedReader br) throws IOException {
    String result = org.mulgara.util.io.IOUtil.readLine(br, MAX_LINE);
    if (result.length() == MAX_LINE) throw new IOException("Excessively sized blob in backup file.");
    return result;
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation() {
    return true;
  }
}
