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
import java.awt.*;
import java.util.List;
import java.io.*;
import java.net.*;

// Logging
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.playback.*;
import org.mulgara.demo.mp3.swing.actions.*;
import org.mulgara.demo.mp3.swing.id3.*;
import org.mulgara.demo.mp3.swing.results.*;
import org.mulgara.demo.mp3.swing.search.SearchPanel;

/**
 * Main Window for the Mp3Application.
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
@SuppressWarnings("serial")
public class Mp3Application extends JFrame {

  /** Logger. This is named after the class. */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3Application.class.getName());

  /** Name of the Application */
  private static final String DEFAULT_APPLICATION_NAME = "Mulgara Mp3 Player";

  /** Configured application name */
  private static String applicationName = DEFAULT_APPLICATION_NAME;

  /** Default config file */
  public static final String DEFAULT_CONFIG_FILE = "/mp3-config.xml";

  /** Used in layout */
  public static final double GOLDEN_RATIO = 1.61803399F;

  /** Ratio of the Horizontal split */
  public static final double HORZ_RATIO = 1 / GOLDEN_RATIO; // 0.5F;

  /** Ratio of the Vertical split */
  public static final double VERT_RATIO = HORZ_RATIO; // 0.5F;

  /** Size of the JSplitPane Dividers */
  public static final int DIVIDER_SIZE = 4;

  /** Initial Window width */
  public static final int DEFAULT_WIDTH = 800;

  /** Initial Window height */
  public static final int DEFAULT_HEIGHT = 600;

  /** Used for searching mp3s */
  private SearchPanel searchPanel = null;

  /** Used to display Id3 Tag information */
  private TagPanel tagPanel = null;

  /** Used to display search results */
  private ResultPanel resultPanel = null;

  /** Indicates Application Status */
  private StatusPanel statusPanel = null;

  /** Menu */
  private ApplicationMenu menu = null;

  /** Does all the work */
  private Mp3Controller controller = null;

  /** Configuration for the Mp3Controller */
  private Mp3ConfigFile config = null;

  /** File to load/save configuration */
  private String configFile = null;

  /** Main playback listener for monitoring and displaying playback status */
  private PlaybackThreadListener playbackListener = null;

  /**
   * Default constructor.
   * @throws Exception
   */
  public Mp3Application() throws Exception {
    this(DEFAULT_CONFIG_FILE);
  }

  /**
   * Constructor. Sets the config file ot load from
   *
   * @throws Exception
   * @param config String
   */
  public Mp3Application(String config) throws Exception {
    load(config);
  }

  /**
   * Clears the Frame and re-initializes it using the new configuration.
   * @param config String
   * @throws Exception
   */
  public void load(String config) throws Exception {
    if (config == null) {
      throw new IllegalArgumentException("'config' is null.");
    }
    clear();
    configFile = config;
    init();
    setup();
  }

  /**
   * Saves the current configuration to the specified file.
   *
   * @param file String
   * @throws Exception
   */
  public void save(File file) throws Exception {
    getConfig().save(file);
  }

  /**
   * Returns the Configuration for the application.
   *
   * @throws IllegalStateException
   * @throws Exception
   * @return Mp3ConfigFile
   */
  public Mp3ConfigFile getConfig() throws IllegalStateException, Exception {
    //load a copy of the config
    return new Mp3ConfigFile(getConfigURL(configFile));
  }

  /**
   * Sets the config object (does not save it).
   *
   * @param config Mp3ConfigFile
   * @throws IllegalArgumentException
   * @throws Exception
   */
  public void setConfig(Mp3ConfigFile config) throws IllegalArgumentException,
      Exception {
    if (config == null) {
      throw new IllegalArgumentException("Mp3ConfigFile is null.");
    }
    this.config = config;
    reload();
  }

  /**
   * Reloads the configuration.
   * @throws Exception
   */
  private void reload() throws Exception {
    Mp3ConfigFile config = this.config;
    String configFile = this.configFile;
    clear();
    this.config = config;
    this.configFile = configFile;
    init();
    setup();
  }

  /**
   * Removes and reset's all components and members.
   *
   * @throws Exception
   */
  private void clear() throws Exception {
    stopPlayback();
    invalidate();
    getContentPane().removeAll();
    searchPanel = null;
    tagPanel = null;
    resultPanel = null;
    statusPanel = null;
    menu = null;
    controller = null;
    config = null;
    configFile = null;
    playbackListener = null;
  }

  /**
   * Initializes the Models and data
   * @throws Exception
   */
  @SuppressWarnings("unchecked")  // cannot add types to generated code
  private void init() throws Exception {
    //getConfig from the file
    config = (config != null) ? config :
        new Mp3ConfigFile(getConfigURL(configFile));
    applicationName = config.getApplicationName();
    //Create the Models (may not exist yet)
    getController().createModels();
    //load schemas
    List<URIReference> schemas = (List<URIReference>)config.getSchemaFiles();
    SchemaModel schemaModel = getController().getSchemaModel();
    if (schemas == null) {
      throw new IllegalStateException("Mp3ConfigFile returned a null " +
          "Schema File List.");
    }
    for (URIReference schema: schemas) schemaModel.loadSchema(schema);
  }

  /**
   * Initializes frame and sets up components.
   * @throws Exception
   */
  public void setup() throws Exception {

    //set properties
    setTitle(getApplicationName());

    //instantiate
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    searchPanel = new SearchPanel(getController());
    tagPanel = new TagPanel();
    resultPanel = new ResultPanel();
    statusPanel = new StatusPanel(this);
    menu = new ApplicationMenu(this);

    //initialize
    getContentPane().setLayout(new BorderLayout());
    resultPanel.addPlaybackListener(getPlaybackListener());
    initActions();
    updateMp3s();
    splitPane.setLeftComponent(getNorthPanel());
    splitPane.setRightComponent(getSouthPanel());
    splitPane.setResizeWeight(VERT_RATIO);
    splitPane.setDividerLocation((int) DEFAULT_HEIGHT / 2);
    splitPane.setDividerSize(DIVIDER_SIZE);

    //add
    setJMenuBar(menu);
    getContentPane().add(splitPane, BorderLayout.CENTER);

    //show
    repaint();
    validate();
  }

  /**
   * Returns the name/title of this Application.
   * @return String
   */
  public static String getApplicationName() {
    return applicationName;
  }

  /**
   * Returns the Top panel.
   * @return JPanel
   */
  public JPanel getNorthPanel() {

    return searchPanel;
  }

  /**
   * Returns the Lower panel.
   * @return JPanel
   */
  public JPanel getSouthPanel() {

    JPanel panel = new JPanel();
    JSplitPane splitPane = new JSplitPane();
    splitPane.setLeftComponent(resultPanel);
    splitPane.setRightComponent(new JScrollPane(tagPanel));
    splitPane.setResizeWeight(HORZ_RATIO);
    splitPane.setDividerLocation((int) DEFAULT_WIDTH / 2);
    splitPane.setDividerSize(DIVIDER_SIZE);
    panel.setLayout(new BorderLayout());
    panel.add(splitPane, BorderLayout.CENTER);
    panel.add(statusPanel, BorderLayout.SOUTH);
    return panel;
  }

  /**
   * Returns the Controller used to execute operations/queries.
   * @throws Exception
   * @return Mp3Controller
   */
  private Mp3Controller getController() throws Exception {
    if (controller == null) {
      controller = Mp3Controller.newInstance();
      controller.init(config);
    }
    return controller;
  }

  /**
   * Sets up and registers actions.
   * @throws Exception
   */
  private void initActions() throws Exception {
    searchPanel.setSearchAction(getSearchMp3sAction());
    menu.setImportAction(getImportMp3sAction());
    menu.setClearMp3ModelAction(getClearMp3ModelAction());
    resultPanel.setMp3SelectAction(getMp3SelectedAction());
  }

  /**
   * Informs the search panel to update the mp3 List (re-search).
   * @throws Exception
   */
  private void updateMp3s() throws Exception {
    searchPanel.updateMp3s();
  }

  /**
   * Returns an Action that will search for Mp3s from the mp3Model.
   * @throws Exception
   * @return Action
   */
  private Action getSearchMp3sAction() throws Exception {
    SearchMp3sAction action = new SearchMp3sAction("Search");
    action.setMp3Controller(getController());
    action.setSearchPanel(searchPanel);
    action.setResultPanel(resultPanel);
    action.setStatusPanel(statusPanel);
    return action;
  }

  /**
   * Returns an Action that will search for Mp3s from the filesystem and import
   * them into the Mp3Model.
   * @throws Exception
   * @return Action
   */
  private Action getImportMp3sAction() throws Exception {
    LoadMp3sAction action = new LoadMp3sAction("Import Mp3s...");
    action.setMp3Controller(getController());
    action.setSearchPanel(searchPanel);
    action.setResultPanel(resultPanel);
    action.setStatusPanel(statusPanel);
    return action;
  }

  /**
   * Returns an Action that will clear the Mp3Model.
   * @throws Exception
   * @return Action
   */
  private Action getClearMp3ModelAction() throws Exception {
    ClearMp3ModelAction action = new ClearMp3ModelAction("Clear Mp3 Metadata...");
    action.setMp3Controller(getController());
    action.setSearchPanel(searchPanel);
    action.setResultPanel(resultPanel);
    action.setStatusPanel(statusPanel);
    return action;
  }

  /**
   * Returns an Action that will display the Id3Tag of the selected mp3.
   * @throws Exception
   * @return Action
   */
  private Action getMp3SelectedAction() throws Exception {
    Mp3SelectedAction action = new Mp3SelectedAction("Find Mp3s");
    action.setMp3Controller(getController());
    action.setTagPanel(tagPanel);
    action.setResultPanel(resultPanel);
    return action;
  }

  /**
   * Returns the main PlaybackThreadListener.
   * @return PlaybackThreadListener
   */
  private PlaybackThreadListener getPlaybackListener() {
    if (playbackListener == null) {
      playbackListener = new PlaybackThreadListener() {
        /** Handles any exceptions generated exception
         * @param t Throwable
         */
        public void exceptionOccurred(Throwable t) {
          ExceptionHandler.handleException(t);
        }
        /** Display the resource URI.
         * @param resource URIReference
         */
        public void playbackStarted(URIReference resource) {
          statusPanel.setText("Playing: " + resource);
        }
        /** Resets the status/text
         */
        public void playbackComplete() {
          statusPanel.clear();
        }
        /** no-op */
        public void playbackPaused() {}
        /** no-op */
        public void playbackResumed() {}
      };
    }
    return playbackListener;
  }

  /**
   * Kills any current playback.
   * @throws Exception
   */
  public void stopPlayback() throws Exception {
    if (resultPanel != null) {
      resultPanel.stopPlayback();
    }
  }

  /**
   * Attempts to locate the config file. Throws an Exception if it cannot be
   * obtained.
   * @param config String
   * @throws Exception
   * @return URL
   */
  private URL getConfigURL(String config) throws Exception {
    if (config == null) {
      throw new IllegalArgumentException("'config' is null");
    }
    URL configURL = getClass().getResource(config);
    if (configURL == null) {
      try {
        configURL = new URL(config);
      }
      catch (Exception exception) {
        configURL = new File(config).toURI().toURL();
      }
    }
    if (configURL == null) {
      throw new IllegalArgumentException("Failed to obtain config File: " +
          config);
    }
    return configURL;
  }

  /**
   * Run the application.
   * @param args String[]
   */
  @SuppressWarnings("deprecation")
  public static void main(String[] args) {
    try {
      // Just use the basic Log configurator
      BasicConfigurator.configure();
      Logger.getRootLogger().setLevel(Level.WARN);
      // create an Application
      String config = Mp3Application.DEFAULT_CONFIG_FILE;
      if (args.length > 0) {
        config = args[0];
      }
      // show
      Mp3Application application = new Mp3Application(config);
      application.pack();
      application.show();
      // resize and position
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      int width = toolkit.getScreenSize().width;
      int height = toolkit.getScreenSize().height;
      application.setBounds((width - DEFAULT_WIDTH) / 4,
          (height - DEFAULT_HEIGHT) / 2, DEFAULT_WIDTH, DEFAULT_HEIGHT);
      application.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    catch (Exception exception) {
      ExceptionHandler.handleException(exception);
    }
  }

}
