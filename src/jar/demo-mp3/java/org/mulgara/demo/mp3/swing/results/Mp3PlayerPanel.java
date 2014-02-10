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

package org.mulgara.demo.mp3.swing.results;

// Java 2 standard packages
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.playback.*;
import org.mulgara.demo.mp3.swing.*;
import org.mulgara.demo.mp3.swing.widgets.*;

/**
 * Panel used to play Mp3's.
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
public class Mp3PlayerPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Mp3PlayerPanel.class.
      getName());

  /** Button for playing mp3s */
  private IconButton playButton = null;

  /** Button for pausing current mp3s */
  private IconButton pauseButton = null;

  /** Button for stopping the current mp3 */
  private IconButton stopButton = null;

  /** Button for skipping the currently playing Mp3 */
  private IconButton skipButton = null;

  /** Icon used for playing a Mp3File */
  private Icon playIcon = null;

  /** Icon used for pausing playback of an Mp3File */
  private Icon pauseIcon = null;

  /** Icon used for stopping the playback of an Mp3File */
  private Icon stopIcon = null;

  /** Icon used for skipping the playback of the currently playing Mp3File */
  private Icon skipIcon = null;

  /** Used to play list shuffled or sorted */
  private JCheckBox shuffleCheckBox = null;

  /** Flag used to sort/shuffle the play list */
  private boolean shuffle = false;

  /** Used to monitor playback */
  private PlaybackThreadListener listener = null;

  /** List of PlaybackThreadListeners that are also notified of events */
  private List playbackListeners = null;

  /** Plays the mp3 Files */
  private Mp3Player player = null;

  /** List of Mp3s to play */
  private List playList = null;


  /**
   * Default constructor
   *
   * @throws Exception
   */
  public Mp3PlayerPanel() throws Exception {

    playbackListeners = new ArrayList();
    setup();
  }

  /**
   * Initializes and sets up components.
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    playButton = new IconButton(getPlayIcon());
    pauseButton = new IconButton(getPauseIcon());
    stopButton = new IconButton(getStopIcon());
    skipButton = new IconButton(getSkipIcon());
    shuffleCheckBox = new JCheckBox("Shuffle");
    player = (player == null) ? newMp3Player() : player;

    //initialize
    shuffleCheckBox.setSelected(shuffle);
    addListeners();
    updateButtons();

    //add
    add(getPlaybackPanel());
    add(getSortPanel());
  }

  /**
   * Returns a panel containing the playback buttons.
   * @return JPanel
   */
  private JPanel getPlaybackPanel() {
    JPanel panel = new JPanel();
    panel.add(playButton);
    panel.add(pauseButton);
    panel.add(stopButton);
    panel.add(skipButton);
    return panel;
  }

  /**
   * Returns a panel containing buttons for sorting/shuffling the playlist.
   * @return JPanel
   */
  private JPanel getSortPanel() {
    JPanel panel = new JPanel();
    panel.add(shuffleCheckBox);
    return panel;
  }

  /**
   * Determines if state of the Mp3Player and initialies buttons.
   * @throws IllegalStateException
   */
  private void updateButtons() throws IllegalStateException {
    int state = getMp3Player().getState();
    switch (state) {

      case (Mp3Player.PLAYING):
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
        skipButton.setEnabled(true);
        stopButton.setEnabled(true);
        break;

      case (Mp3Player.PAUSED):
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(true);
        skipButton.setEnabled(true);
        break;

      case (Mp3Player.STOPPED):
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        skipButton.setEnabled(false);
        stopButton.setEnabled(false);
        break;

      default:
        throw new IllegalStateException("Unknown Mp3Player state.");
    }
  }

  /**
   * Adds Listeners/Actions to the buttons.
   * @throws IllegalStateException
   */
  private void addListeners() throws IllegalStateException {
    if ((playButton == null)
        || (stopButton == null)) {
      throw new IllegalStateException("Buttons do not exist.");
    }
    playButton.addActionListener(new ActionListener() {
      /** Plays, Pauses or Resumes the file depending on player state.
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          Mp3Player player = getMp3Player();
          if (player.getState() == Mp3Player.STOPPED) {
            player.playFiles(getPlayList());
          }
          else if (player.getState() == Mp3Player.PAUSED) {
            player.resumePlayback();
          }
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    pauseButton.addActionListener(new ActionListener() {
      /** calls pause
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          Mp3Player player = getMp3Player();
          player.pausePlayback();
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    stopButton.addActionListener(new ActionListener() {
      /** calls Stop
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          Mp3Player player = getMp3Player();
          player.stopPlayback();
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    skipButton.addActionListener(new ActionListener() {
      /** calls Skip
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          Mp3Player player = getMp3Player();
          player.skipTrack();
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    shuffleCheckBox.addActionListener(new ActionListener() {
      /** updates shuffle flag
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          shuffle = shuffleCheckBox.isSelected();
          if ((playList != null)
              && (playList.size() > 0)) {
            setPlayList(playList.iterator());
            //playback must be restarted
            int state = getMp3Player().getState();
            getMp3Player().stopPlayback();
            if (state == Mp3Player.PLAYING) {
              getMp3Player().playFiles(getPlayList());
            }
          }
        } catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
  }

  /**
   * Closes any existing player and creates a new one.
   * @throws Exception
   * @return Mp3Player
   */
  private Mp3Player newMp3Player() throws Exception {
    if (player != null) {
      player.close();
    }
    player = new Mp3Player();
    player.setPlaybackListener(getPlaybackListener());
    return player;
  }

  /**
   * Returns the current Mp3Player.
   * @throws IllegalStateException
   * @return Mp3Player
   */
  private Mp3Player getMp3Player() throws IllegalStateException {
    if (player == null) {
      throw new IllegalStateException("Mp3Player does not exist.");
    }
    return player;
  }

  /**
   * Sets the List of Mp3s to be played.
   *
   * @param playIter Iterator
   * @throws IllegalArgumentException
   */
  public void setPlayList(Iterator playIter) throws IllegalArgumentException {
    if (playIter == null) {
      throw new IllegalArgumentException("'playList' is null");
    }
    try {
      playList = new ArrayList();
      Mp3File current = null;
      while (playIter.hasNext()) {
        current = (Mp3File) playIter.next();
        playList.add(current);
      }
      //should the list be shuffled?
      if (shuffle) {
        Collections.shuffle(playList);
      }
      else {
        Collections.sort(playList);
      }
    }
    catch (ClassCastException castException) {
      throw new IllegalArgumentException(
          "Iterator should only contain Mp3Files");
    }
  }

  /**
   * Adds the Listener to the List of listeners that are notified of events.
   * @param listener PlaybackThreadListener
   * @throws IllegalArgumentException
   */
  public void addPlaybackListener(PlaybackThreadListener listener) throws
      IllegalArgumentException {
    if (listener == null) {
      throw new IllegalArgumentException("PlaybackThreadListener is null");
    }
    playbackListeners.add(listener);
  }

  /**
   * Returns a listener to monitor playback.
   * @return PlaybackThreadListener
   */
  private PlaybackThreadListener getPlaybackListener() {
    if (listener == null) {
      listener = new PlaybackThreadListener() {
        /** @param t Throwable */
        public void exceptionOccurred(Throwable t) {
          playbackExceptionOccurred(t);
        }
        /** @param resource URIReference */
        public void playbackStarted(URIReference resource) {
          playbackStartedOccurred(resource);
        }
        /** Indicates that playback has finished */
        public void playbackComplete() {
          playbackCompleteOccurred();
        }
        /** Indicates that playback has been paused */
        public void playbackPaused() {
          playbackPausedOccurred();
        }
        /** Indicates that playback has resumed */
        public void playbackResumed() {
          playbackResumedOccurred();
        }
      };
    }
    return listener;
  }

  /**
   * Obtains all Mp3 to be played.
   * @throws IllegalStateException
   * @return Iterator
   */
  private Iterator getPlayList() throws IllegalStateException {
    if (playList == null) {
      throw new IllegalStateException("No Mp3(s) to play.");
    }
    //copy mp3 Iterator
    return playList.iterator();
  }

  /**
   * Closes the underlying player and releases resources.
   * @throws Exception
   */
  public void close() throws Exception {
    if (player != null) {
      player.close();
    }
  }

  /**
   * Notify listeners of the exception.
   * @param t Throwable
   */
  public void playbackExceptionOccurred(Throwable t) {
    Iterator iter = playbackListeners.iterator();
    while (iter.hasNext()) {
      ((PlaybackThreadListener) iter.next()).exceptionOccurred(t);
    }
  }

  /**
   * Update play Button and Notify listeners of the event.
   * @param resource URIReference
   */
  public void playbackStartedOccurred(URIReference resource) {
    updateButtons();
    Iterator iter = playbackListeners.iterator();
    while (iter.hasNext()) {
      ((PlaybackThreadListener) iter.next()).playbackStarted(resource);
    }
  }

  /**
   * Update play Button and Notify listeners of the event.
   */
  public void playbackCompleteOccurred() {
    updateButtons();
    Iterator iter = playbackListeners.iterator();
    while (iter.hasNext()) {
      ((PlaybackThreadListener) iter.next()).playbackComplete();
    }
  }

  /**
   * Update play Button and Notify listeners of the event.
   */
  public void playbackPausedOccurred() {
    updateButtons();
    Iterator iter = playbackListeners.iterator();
    while (iter.hasNext()) {
      ((PlaybackThreadListener) iter.next()).playbackPaused();
    }
  }

  /**
   * Update play Button and Notify listeners of the event.
   */
  public void playbackResumedOccurred() {
    updateButtons();
    Iterator iter = playbackListeners.iterator();
    while (iter.hasNext()) {
      ((PlaybackThreadListener) iter.next()).playbackResumed();
    }
  }

  /**
   * Returns the play Icon
   * @return Icon
   */
  private Icon getPlayIcon() {
    if (playIcon == null) {
      playIcon = IconLoader.getIcon(IconLoader.PLAY_16);
    }
    return playIcon;
  }

  /**
   * Returns the pause Icon
   * @return Icon
   */
  private Icon getPauseIcon() {
    if (pauseIcon == null) {
      pauseIcon = IconLoader.getIcon(IconLoader.PAUSE_16);
    }
    return pauseIcon;
  }

  /**
   * Returns the stop Icon
   * @return Icon
   */
  private Icon getStopIcon() {
    if (stopIcon == null) {
      stopIcon = IconLoader.getIcon(IconLoader.STOP_16);
    }
    return stopIcon;
  }

  /**
   * Returns the skip Icon
   * @return Icon
   */
  private Icon getSkipIcon() {
    if (skipIcon == null) {
      skipIcon = IconLoader.getIcon(IconLoader.SKIP_16);
    }
    return skipIcon;
  }

  /**
   * Kills any current playback
   * @throws Exception
   */
  public void stopPlayback() throws Exception {
    getMp3Player().stopPlayback();
  }

}
