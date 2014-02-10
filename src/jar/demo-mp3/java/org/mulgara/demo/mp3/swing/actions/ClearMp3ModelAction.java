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

package org.mulgara.demo.mp3.swing.actions;

// Java 2 standard packages
import java.awt.event.ActionEvent;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.swing.*;

import javax.swing.*;
import org.jrdf.graph.*;

/**
 * Removes the contents of the Mp3Model.
 *
 * @created 2004-12-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:08 $
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
public class ClearMp3ModelAction extends AbstractMp3Action {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(ClearMp3ModelAction.class.
      getName());

  /**
   * Default constructor
   *
   * @param name String
   */
  public ClearMp3ModelAction(String name) {
    super(name);
  }

  /**
   * Searches the selected directories in the filesystem for Mp3 files.
   * @param e ActionEvent
   */
  public synchronized void actionPerformed(ActionEvent e) {
    try {
      Mp3Controller controller = getMp3Controller();
      Mp3Model mp3Model = controller.getMp3Model();
      if (confirmClear(mp3Model.getResource())) {
        getStatusPanel().startProcessing("Clearing Mp3 Graph...");
        mp3Model.clear();
        //underlying model/data has changed
        getSearchPanel().constraintsHaveChanged();
        getStatusPanel().stopProcessing("Successfully cleared Mp3 metadata.");
      }
    }
    catch (Exception exception) {
      getStatusPanel().stopProcessingError("Error(s) occurred while clearing " +
          "Mp3 Graph");
      ExceptionHandler.handleException(exception);
    }
  }

  /**
   * Shows a dialog asking to confirm the clear. Returns true if 'Yes' is
   * selected.
   *
   * @return boolean
   * @param model URIReference
   */
  private boolean confirmClear(URIReference model) {
    String message = "Are you sure you want to delete all statements from " +
        NEWLINE + "the following Graph: " + NEWLINE +  model + "?";
    int decision = JOptionPane.showConfirmDialog(super.getSearchPanel(), message,
        "Confirm Clear Mp3 Metadata", JOptionPane.YES_NO_OPTION);
    //was 'Yes' chosen
    return decision == JOptionPane.YES_OPTION;
  }

}
