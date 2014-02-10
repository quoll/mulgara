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

package org.mulgara.demo.mp3.swing;

// Java 2 standard packages
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.swing.widgets.FileChooser;


/**
 * Menu...
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:07 $
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
public class ApplicationMenu extends JMenuBar {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(ApplicationMenu.class.getName());

  /** The main File menu */
  private JMenu file = null;

  /** The file/clear */
  private JMenuItem clearMp3Model = null;

  /** Actin used to clear the Mp3Model */
  private Action clearMp3ModelAction = null;

  /** The file/import mp3s */
  private JMenuItem importMp3s = null;

  /** Option for existing the Application */
  private JMenuItem exit = null;

  /** Action executed by the import menu */
  private Action importMp3sAction = null;

  /** The main Settings menu */
  private JMenu settings = null;

  /** Loads settings from a file */
  private JMenuItem loadSettings = null;

  /** Loads deafult settings */
  private JMenuItem defaultSettings = null;

  /** The main help menu */
  private JMenu help = null;

  /** The help/about */
  private JMenuItem about = null;


  /** Application the menu belings to */
  private Mp3Application application = null;

  /**
   * Default constructor
   *
   * @param application Mp3Application
   * @throws IllegalArgumentException
   */
  public ApplicationMenu(Mp3Application application) throws IllegalArgumentException {
    if (application == null) {
      throw new IllegalArgumentException("Mp3Application is null.");
    }
    this.application = application;
    setup();
  }

  /**
   * Initializes and sets up components.
   */
  private void setup() {

    //instantiate
    file = new JMenu("File");
    importMp3s = new JMenuItem(importMp3sAction);
    clearMp3Model = new JMenuItem(clearMp3ModelAction);
    exit = new JMenuItem("Exit");

    settings = new JMenu("Settings");
    loadSettings = new JMenuItem("Load Configuration...");
    defaultSettings = new JMenuItem("Defaults");

    help = new JMenu("Help");
    about = new JMenuItem("About");

    //initialize
    file.setMnemonic('f');
    importMp3s.setMnemonic('i');
    clearMp3Model.setMnemonic('c');
    exit.setMnemonic('x');

    settings.setMnemonic('s');
    loadSettings.setMnemonic('l');
    defaultSettings.setMnemonic('d');

    help.setMnemonic('h');
    about.setMnemonic('a');

    //add
    file.add(importMp3s);
    file.addSeparator();
    file.add(clearMp3Model);
    file.addSeparator();
    file.add(exit);
    settings.add(loadSettings);
    settings.add(defaultSettings);
    help.add(about);
    addListeners();

    add(file);
    add(settings);
    add(help);
  }

  /**
   * Sets the Action that is executed by the Import Mp3s menu item.
   * @param action Action
   * @throws IllegalArgumentException
   */
  public void setImportAction(Action action) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("Action is null");
    }
    importMp3sAction = action;
    importMp3s.setAction(importMp3sAction);
    importMp3s.setMnemonic('i');
  }

  /**
   * Sets the Action that is executed by the Clear Mp3Model menu item.
   * @param action Action
   * @throws IllegalArgumentException
   */
  public void setClearMp3ModelAction(Action action) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("Action is null");
    }
    clearMp3ModelAction = action;
    clearMp3Model.setAction(clearMp3ModelAction);
    clearMp3Model.setMnemonic('c');
  }

  /**
   * Adds listeners to the menu items
   */
  private void addListeners() {
    loadSettings.addActionListener(new ActionListener() {
      /** Load the Application.
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          File file = FileChooser.chooseFile(application, "Load");
          if (file != null) {
            application.load(file.toString());
          }
        } catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    defaultSettings.addActionListener(new ActionListener() {
      /** Load default Settings
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          application.load(Mp3Application.DEFAULT_CONFIG_FILE);
        } catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    exit.addActionListener(new ActionListener(){
      /** Exit the Application
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    about.addActionListener(new ActionListener(){
      /** Displays application information
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        showAboutDialog();
      }
    });
  }

  /**
   * Shows help>about dialog
   */
  private void showAboutDialog() {
    Icon icon = IconLoader.getIcon(IconLoader.MUSIC_FILE_24);
    JOptionPane.showMessageDialog(application, getAboutMessage(),
        "About " + Mp3Application.getApplicationName(), JOptionPane.OK_OPTION,
        icon);
  }

  /**
   * Returns the help>about message
   * @return String
   */
  private String getAboutMessage() {
    return Mp3Application.getApplicationName() + " v0.1, 2004";
  }

}
