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

package org.mulgara.content.mbox.parser;

/**
 * A class representing the fields of an attachment to a message.
 *
 * @created 2004-08-27
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:40 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/LICENCE">License description</A>
 */

public class Attachment {

  /** The file name of the attachment */
  private String filename;

  /** The mime type of the attachment */
  private String mimeType;

  /** The byte size of the attachment */
  private int size;

  /**
   * Constructor.
   */
  public Attachment() {

  }

  /**
   * Sets the filename for the attachment.
   *
   * @param filename The new filename for the attachment
   */
  public void setFilename(String filename) {

    // Set the new filename to use
    this.filename = filename;

    if (this.filename == null) {

      // If the filename passed in was null then set the filename to be the
      // default value
      this.filename = "attachment.atmt";
    }
  }

  /**
   * Retrieves the filename of the attachment.
   *
   * @return The filename of the attachment
   */
  public String getFilename() {

    return filename;
  }

  /**
   * Sets the mime type for the attachment.
   *
   * @param mimeType The new mime type for the attachment
   */
  public void setMimeType(String mimeType) {

    // Set the new mime type to use
    this.mimeType = mimeType;
  }

  /**
   * Retrieves the mime type of the attachment.
   *
   * @return The mime type of the attachment
   */
  public String getMimeType() {

    return mimeType;
  }

  /**
   * Sets the size for the attachment.
   *
   * @param size The new size for the attachment
   */
  public void setSize(int size) {

    // Set the new size to use
    this.size = size;
  }

  /**
   * Retrieves the size of the attachment.
   *
   * @return The size of the attachment
   */
  public int getSize() {

    return size;
  }
}
