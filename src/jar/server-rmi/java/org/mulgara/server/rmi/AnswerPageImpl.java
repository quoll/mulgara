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

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.util.*;
import java.util.zip.*;
import java.io.*;

// Third party packages
import org.apache.log4j.*;
import org.mulgara.query.Answer;
import org.mulgara.query.ArrayAnswer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

import java.io.ObjectInput;
import java.io.IOException;

/**
 * Contains a serializable page of answers retrieved from an {@link org.mulgara.query.Answer} object.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @created 2004-03-26
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AnswerPageImpl implements AnswerPage, Externalizable {

 /**
  * Allow newer compiled version of the stub to operate when changes
  * have not occurred with the class.
  * NOTE : update this serialVersionUID when a method or a public member is
  * deleted.
  */
  static final long serialVersionUID = 2899965036062665141L;

  /** System property to set the level of compression to use  */
  static final String COMPRESSION_LEVEL = "mulgara.rmi.compression";

  /** Size of compression buffer */
  static final int SERVER_BUFFER_SIZE = 8192;

  /** Size of compression/decompression buffer */
  static final int CLIENT_BUFFER_SIZE = 4096;

  /** The logging category to log to. */
  private final static Logger log = Logger.getLogger(AnswerPageImpl.class.getName());

  /** The number of rows used in this page */
  private int pageSize = 0;

  /** The data held in the page. */
  private List<Object[]> rows;

  /** The current row number. */
  private int currentRow;

  /** The Column names. */
  private String[] columnNames;

  /** Efficiency flag to indicate if this is the last page buildable from an answer. */
  private boolean lastPage;

  public AnswerPageImpl() throws TuplesException {
    // nothing
  }

  /**
   * Main Constructor.  Copies rows from an Answer into internal serializable data.
   * @param answer The Answer to get data from.  This Answer gets moved on by up to pageSize rows.
   * @param pageSize The number of rows to copy from answer up to the max number of available rows from answer.
   * @throws TuplesException Can be thrown while accessing the answer parameter.
   */
  public AnswerPageImpl(Answer answer, int pageSize) throws TuplesException {
    rows = new ArrayList<Object[]>(pageSize);

    int width = answer.getNumberOfVariables();

    // fill the data list with rows from the answer
    int r = 0;  // not used for indexing, just counts the rows
    while (r < pageSize && answer.next()) {
      r++;
      Object[] row = new Object[width];

      // fill a row with data from the current answer row
      for (int i = 0; i < width; i++) {
        row[i] = answer.getObject(i);

        // if the current object is a non-serializeable answer, then change it so it is
        if (row[i] instanceof Answer && !(row[i] instanceof ArrayAnswer)) {
          Answer ans = (Answer)row[i];
          row[i] = new ArrayAnswer(ans);
          ans.close();
        }
      }
      rows.add(row);
    }
    this.pageSize = r;
    this.lastPage = this.pageSize < pageSize;
    currentRow = -1;

    // Convert variables to column name array
    Variable[] variables = answer.getVariables();
    columnNames = new String[variables.length];
    for (int i = 0; i < variables.length; i++) {
      columnNames[i] = variables[i].getName();
    }
  }


  /**
   * Constructs a page based on an Answer with a default number of rows.
   * @param answer The Answer to get data from.  This Answer gets moved on by up to DEFAULT_PAGE_SIZE rows.
   * @throws TuplesException Can be thrown while accessing the answer parameter.
   */
  public AnswerPageImpl(Answer answer) throws TuplesException {
    this(answer, Integer.getInteger(PAGE_SIZE_PROPERTY, DEFAULT_PAGE_SIZE).intValue());
  }


  /**
   * Move the internal state of this object onto the next row in the answer.
   * @return True if there is a new row.  False if there is no data left.
   */
  public boolean nextInPage() {
    return ++currentRow < pageSize;
  }


  /**
   * Gets the data for a particular column from the current row.
   * @param column The column number of the data to retrieve.
   * @return The object found at the requested location.
   */
  public Object getObjectFromPage(int column) {
    assert currentRow < pageSize && currentRow >= 0;
    return ((Object[])rows.get(currentRow))[column];
  }


  /**
   * Gets the data for a column of a particular name from the current row.
   * @param name The column name of the data to retrieve.
   * @return The object found at the requested location.
   * @throws TuplesException The name was not a valid column.
   */
  public Object getObjectFromPage(String name) throws TuplesException {
    assert currentRow < pageSize && currentRow >= 0;
    for (int i = 0; i < columnNames.length; i++) {
      if (name.equals(columnNames[i])) return ((Object[])rows.get(currentRow))[i];
    }
    throw new TuplesException("Field not found: " + name);
  }


  /**
   * Retrieves the number of rows in this page.
   * @return The number of rows in the current page, this is less than or equal to the page size.
   */
  public long getPageSize() {
    return pageSize;
  }


  /**
   * Resets the current row to the beginning.  Hopefully not needed, but included for completeness.
   */
  public void beforeFirstInPage() {
    currentRow = -1;
  }


  /**
   * Indicates that this is the last page constructable for the current answer.
   * @return true if there are no more pages after this one.
   */
  public boolean isLastPage() {
    return lastPage;
  }

  /**
   * Reads the entire page to the input stream, decompressing if requested.
   * @param in The data stream to read from.
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    // read the flag to see if the remaining stream is compressed
    boolean compression = in.readBoolean();

    // determine the uncompressed size of a compressed stream
    int uncompressedSize = 0;
    if (compression) uncompressedSize = in.readInt();

    // read in the bytes which make up the rest of the object
    byte[] byteArray = (byte[])in.readObject();

    // convert the bytes to a stream
    InputStream dataStream = new ByteArrayInputStream(byteArray);

    // extract the data if it was compressed
    if (compression) {
      // allocate a new array to hold uncompressed data
      byteArray = new byte[uncompressedSize];
      int bytesRead = 0;
      // build the decompression object
      InflaterInputStream inflater = new InflaterInputStream(dataStream, new Inflater(), CLIENT_BUFFER_SIZE);
      // loop until the whole data stream is decompressed
      while (uncompressedSize - bytesRead > 0) {
        int l = inflater.read(byteArray, bytesRead, uncompressedSize - bytesRead);
        bytesRead += l;
        if (l < 0) {
          throw new IOException("Error in compressed data stream");
        }
      }
      // convert the decompressed bytes to a stream
      dataStream = new ByteArrayInputStream(byteArray);
    }

    // build an object to extract objects from the stream
    ObjectInputStream data = new ObjectInputStream(dataStream);

    // The number of rows used in this page
    pageSize = data.readInt();

    // The current row number
    currentRow = data.readInt();

    // Efficiency flag to indicate if this is the last page buildable from an answer
    lastPage = data.readBoolean();

    // the number of columns
    int size = data.readInt();

    // The Column names
    columnNames = new String[size];
    for (int i = 0; i < size; i++) {
      columnNames[i] = (String) data.readObject();
    }

    // The data held in the page
    readRows(data);

  }


  /** This is a variable used for metrics when logging and debugging */
  private static long serialTime = 0;

  /**
   * Writes the entire page to the output stream, compressing if requested.
   * @param out The data stream to write to.
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    long currentTime = System.currentTimeMillis();
    ByteArrayOutputStream data =  new ByteArrayOutputStream();
    ObjectOutputStream objectData = new ObjectOutputStream(data);

    // The number of rows used in this page
    objectData.writeInt(pageSize);

    // The current row number
    objectData.writeInt(currentRow);

    // Efficiency flag to indicate if this is the last page buildable from an answer
    objectData.writeBoolean(lastPage);

    // The number of column names
    objectData.writeInt(columnNames.length);

    // The Column names
    for (int i = 0; i < columnNames.length; i++) {
      objectData.writeObject(columnNames[i]);
    }

    // The data held in the page
    writeRows(objectData);

    objectData.close();

    byte[] outputData = data.toByteArray();

    // compress if needed
    outputData = setCompressionLevel(out, outputData);

    /** Write the object data out */
    out.writeObject(outputData);

    long diffTime = System.currentTimeMillis() - currentTime;
    serialTime += diffTime;

    log.debug(
        "writeExternal - written : "+outputData.length+", down from: "+data.size()+
        " ("+(100-(outputData.length*100/data.size()))+"%) : total time=" + serialTime
    );

  }


  /**
   * Helper method for writeExternal to write the rows object.
   * @param output The object output stream to write rows into.
   */
  protected void writeRows(ObjectOutputStream output) throws IOException {
    // the number of rows
    try {
      output.writeInt(rows.size());

      // The data in each row
      Iterator<Object[]> ri = rows.iterator();
      while (ri.hasNext()) {
        Object[] line = ri.next();
        // now write each element in the line
        for (int c = 0; c < columnNames.length; c++) output.writeObject(line[c]);
      }
    } catch (IOException ei) {
      log.warn("IOException thrown", ei);
      throw ei;
    } catch (Throwable t) {
      log.warn("Throwable thrown", t);
      throw new RuntimeException("Throwable thrown", t);
    }
  }


  /**
   * Helper method for readExternal to read the rows object.
   *
   * @param input The object input stream to read rows from.
   */
  protected void readRows(ObjectInputStream input) throws IOException, ClassNotFoundException {
    // the number of rows
    int rowCount = input.readInt();
    rows = new ArrayList<Object[]>(pageSize);

    // iterate over each row and add
    for (int r = 0; r < rowCount; r++) {
      // build up the contents of the row
      Object[] line = new Object[columnNames.length];
      for (int c = 0; c < columnNames.length; c++) {
        line[c] = input.readObject();
      }
      // add the row to the page
      rows.add(line);
    }
  }


  /**
   * Helper method for writeExternal to test if compression is needed,
   * and compresses the data if applicable.
   * @param outStream The output stream to set the compression on.
   * @param data The data to compress for the stream.
   * @return The new data for the stream.
   */
  protected byte[] setCompressionLevel(ObjectOutput outStream, byte[] data) throws IOException {
    // test if compression should be used
    int clevel = Integer.getInteger(COMPRESSION_LEVEL, 1).intValue();
    if (clevel > 0) {
      if (clevel > Deflater.BEST_COMPRESSION) clevel = Deflater.BEST_COMPRESSION;
      // store the length of the buffer
      int datalength = data.length;

      // Create the compressed stream
      ByteArrayOutputStream zData = new ByteArrayOutputStream();
      DeflaterOutputStream deflater = new DeflaterOutputStream(zData, new Deflater(clevel), SERVER_BUFFER_SIZE);

      // compress the data into zData
      deflater.write(data);
      deflater.close();

      // set the byte array to the newly compressed data
      data = zData.toByteArray();

      // write a flag to indicate that compression was used
      outStream.writeBoolean(true);
      // write the length of the compressed data
      outStream.writeInt(datalength);
    } else {
      // write a flag to indicate that compression was NOT used
      outStream.writeBoolean(false);
    }

    // return the data to be written to the stream
    return data;
  }

}

