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

package org.mulgara.demo.mp3.swing.results.list;

// Java 2 standard packages
import javax.swing.*;
import java.awt.Component;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.Mp3File;
import org.mulgara.demo.mp3.swing.widgets.NodeListRenderer;


/**
 * Renders Mp3Files in a JList
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:09 $
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
public class Mp3FileListRenderer extends NodeListRenderer {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Mp3FileListRenderer.class.
      getName());

  /**
   * Default constructor
   */
  public Mp3FileListRenderer() {
  }

  /**
   * getListCellRendererComponent
   *
   * @param list JList
   * @param value Object
   * @param index int
   * @param isSelected boolean
   * @param cellHasFocus boolean
   * @return Component
   */
  public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {

    Object obj = value;
    if ((value != null)
        && (value instanceof Mp3File)) {
      obj = ((Mp3File) value).getResource();
    }
    return super.getListCellRendererComponent(list, obj, index, isSelected,
        cellHasFocus);
  }

}
