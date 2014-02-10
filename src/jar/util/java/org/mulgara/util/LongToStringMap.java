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

package org.mulgara.util;

// Java 2 standard packages
import java.io.*;

// Third party packages
import org.apache.log4j.Logger;

import org.mulgara.util.TempDir;


/**
 * Temporary disk storage based on the {@link StringToLongMap} for indexing
 * the mapping by long value.
 *
 * @created 2004-09-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $ by $Author: newmana $
 *
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class LongToStringMap {

  /** Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LongToStringMap.class);

  /** File used to store mappings */
  private File stringFileName = null;

  /** File handle used to read/write */
  private RandomAccessFile stringFile = null;

  /** Map between id (as a String) and the stringFile offset */
  private StringToLongMap indexMap = null;


  /**
   * Default Constructor. Creates the map File(s) in the mulgara temp directory.
   *
   * @throws IOException
   */
  public LongToStringMap() throws IOException {
    //create the files
    indexMap = new StringToLongMap();
    stringFileName = TempDir.createTempFile("string", ".map");
    stringFile = new RandomAccessFile(stringFileName, "rw");

    //remove contents
    clear();
  }

  /**
   * Returns the String value associated with the long id. Returns null if the
   * long id is not mapped.
   *
   * @param id long
   * @throws IOException
   * @return String
   */
  public String get(long id) throws IOException {
    return readString(findOffset(id));
  }

  /**
   * Inserts the mapping.
   *
   * @param value String
   * @param id long
   * @throws IOException
   */
  public void put(String value, long id) throws IOException {

    //put the string and id-offset mapping
    long offset = writeString(value);
    indexMap.put("" + id, offset);
  }

  /**
   * Deletes the contents of the map file(s) without deleting the actual file(s)
   *
   * @throws IOException
   */
  public void clear() throws IOException {

    //start indexing at byte 1 (zero is an invalid offset)
    stringFile.setLength(1);
    stringFile.seek(1);
    indexMap.clear();
  }

  /**
   * Removes the map file(s).
   *
   * @throws IOException
   */
  public void delete() throws IOException {

    //done with file
    stringFile.close();

    //try to delete the file
    if (!stringFileName.delete()) {
      throw new IOException("Failed to delete map File: " + stringFileName);
    }

    //delete id-offset map
    indexMap.delete();
  }

  /**
   * Looks up the id and returns an index in the String file for the mapped
   * String.
   *
   * @param id long
   * @throws IOException
   * @return long
   */
  private long findOffset(long id) throws IOException {

    long offset = -1;
    offset = indexMap.get("" + id);
    return offset;
  }

  /**
   * Returns the String that begins at the offset or null if the offset is
   * zero or negative.
   *
   * @param offset long
   * @throws IOException
   * @return String
   */
  private String readString (long offset) throws IOException {

    if (offset > 0) {

      stringFile.seek(offset);
      return stringFile.readUTF();
    } else {

      //zero or negative offset
      return null;
    }
  }

  /**
   * Writes the String to the string file and returns the offset to it.
   *
   * @param string String
   * @throws IOException
   * @return long
   */
  private long writeString (String string) throws IOException {

    // go to end of file and write
    long pos = stringFile.length();
    stringFile.seek(pos);
    stringFile.writeUTF(string);
    return pos;
  }

}
