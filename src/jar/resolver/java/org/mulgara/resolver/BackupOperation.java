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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.SecurityAdapter;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.tuples.Tuples;

/**
 * An {@link Operation} that serializes the state of the database into a backup
 * file which can be read back by the complementary {@link RestoreOperation}.
 *
 * @created 2004-10-07
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:06 $ by $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class BackupOperation extends OutputOperation implements BackupConstants, Operation {

  private static final Logger logger = Logger.getLogger(BackupOperation.class);
  
  //
  // Constructor
  //

  /**
   * Create an {@link Operation} that backs up all the data on the specified
   * server to a URI or an output stream.
   *
   * The database is not changed by this method.
   * If an {@link OutputStream} is supplied then the destinationURI is ignored.
   *
   * @param outputStream  output stream to receive the contents, may be
   *   <code>null</code> if a <var>destinationURI</var> is specified
   * @param destinationURI  URI of the file to backup into, may be
   *   <code>null</code> if an <var>outputStream</var> is specified
   */
  public BackupOperation(OutputStream outputStream, URI destinationURI) {
    super(outputStream, destinationURI);
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext operationContext,
      SystemResolver systemResolver,
      DatabaseMetadata metadata) throws Exception {
    
    // Make sure security adapters are satisfied before proceeding.
    for (SecurityAdapter securityAdapter : operationContext.getSecurityAdapterList()) {
      if (!securityAdapter.canBackup()) {
        throw new QueryException("You do not have permission to back up the database.");
      }
    }
    
    OutputStream os = getOutputStream();
    Writer writer = null;
    boolean success = false;
    try {
      // The existence of a fragment indicates that a model is to be backed
      // up otherwise the entire database is to be backed up.
      writer = new BufferedWriter(new OutputStreamWriter(
          new GZIPOutputStream(os), "UTF-8"
      ));
      
      backupDatabase(systemResolver, metadata, writer);
      success = true;
    } finally {
      // Clean up.
      try {
        if (writer != null) {
          // Close the writer if it exists.  This will also close the wrapped
          // OutputStream.
          writer.close();
        } else if (os != null) {
          // Close the os if it exists.
          os.close();
        }
      } catch (IOException e) {
        if (success) throw e; // The backup worked but we couldn't close, so re-throw.
        else logger.info("Suppressing I/O exception closing failed backup writer", e); // Log and ignore.
      }
    }
  }


  /**
   * Dumps the entire database to the specified Writer.
   *
   * @param systemResolver The system resolver for the database.
   * @param metadata The Metadata for the database.
   * @param writer Writer The output to write to.
   * @throws Exception Due to any error accessing the database or writing to the writer.
   */
  private void backupDatabase(SystemResolver systemResolver, DatabaseMetadata metadata, Writer writer)
      throws Exception {
    // Write the backup
    writer.write(BACKUP_FILE_HEADER + BACKUP_VERSION6 + '\n');
    writer.write(new Date().toString());
    writer.write('\n');

    // Dump the strings.
    writer.write("RDFNODES\n");

    Tuples t = systemResolver.findStringPoolType(null, null);
    assert t != null;
    try {
      t.beforeFirst();
      while (t.next()) {
        long localNode = t.getColumnValue(0);
        writer.write(Long.toString(localNode));
        writer.write(' ');

        SPObject spObject = systemResolver.findSPObject(localNode);
        writer.write(spObject.getEncodedString());
        writer.write('\n');
      }
    } finally {
      try {
        t.close();
      } catch (TuplesException e) {
        logger.warn("Error closing tuples during backup", e);
      }
    }

    // Dump the triples.
    Tuples tuples = systemResolver.resolve(new ConstraintImpl(
        StatementStore.VARIABLES[0],
        StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2],
        StatementStore.VARIABLES[3]));
    assert tuples != null;
    try {
      int[] colMap = mapColumnsToStd(tuples.getVariables());
      writer.write("TRIPLES\n");

      long preallocationModelNode = metadata.getPreallocationModelNode();
      for (tuples.beforeFirst(); tuples.next(); ) {
        // Suppress output of the preallocation model.
        long modelNode = tuples.getColumnValue(colMap[3]);
        if (modelNode != preallocationModelNode) {
          writer.write(Long.toString(tuples.getColumnValue(colMap[0])));
          writer.write(' ');
          writer.write(Long.toString(tuples.getColumnValue(colMap[1])));
          writer.write(' ');
          writer.write(Long.toString(tuples.getColumnValue(colMap[2])));
          writer.write(' ');
          writer.write(Long.toString(modelNode));
          writer.write('\n');
        }
      }
    } finally {
      try {
        tuples.close();
      } catch (TuplesException e) {
        logger.warn("Error closing tuples during backup", e);
      }
    }

    writer.write("END\n");
  }

}
