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

package org.mulgara.demo.mp3.playback;

// Java 2 standard packages
import java.util.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.*;

/**
 * Used to play, pause, resume, skip and stop playback of Mp3/Audio files.
 *
 * @created 2004-12-10
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
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
public class Mp3Player extends PlaybackThreadListener {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Mp3Player.class.
      getName());

  /** Indicates a playing state */
  public final static int PLAYING = 0;

  /** Indicates a paused state */
  public final static int PAUSED = 1;

  /** Indicates a stopped state */
  public final static int STOPPED = 2;

  /** Current state */
  private int state = STOPPED;

  /** Mp3 to be played */
  private Mp3File mp3 = null;

  /** CurrentThread being run */
  private PlaylistThread playThread = null;

  /** Receives events/exceptions during playback */
  private PlaybackThreadListener listener = null;

  /**
   * Default constructor
   */
  public Mp3Player() {
  }

  /**
   * Begins playback.
   * @throws Exception
   */
  public synchronized void play() throws Exception {
    newPlaybackThread(getMp3File()).start();
  }

  /**
   * Plays all Mp3s in the array until stop is called.
   *
   * @throws Exception
   * @param mp3s Mp3File[]
   */
  public synchronized void playFiles(Mp3File[] mp3s) throws Exception {
    if ((mp3s == null)
        || (mp3s.length <= 0)) {
      return;
    }
    //play each file (unless stopped)
    newPlaybackThread(mp3s).start();
  }

  /**
   * Plays all Mp3s in the array until stop is called.
   *
   * @throws Exception
   * @param mp3s Mp3File[]
   */
  public synchronized void playFiles(Iterator mp3s) throws Exception {
    if (mp3s == null) {
      return;
    }
    //play each file (unless stopped)
    newPlaybackThread(mp3s).start();
  }

  /**
   * Pauses platback.
   * @throws Exception
   */
  public synchronized void pausePlayback() throws Exception {
    if ((getState() != PLAYING)
        || (playThread == null)) {
      throw new IllegalStateException("Cannot pause. Mp3Player is not playing");
    }
    playThread.pausePlayback();
    setState(PAUSED);
  }

  /**
   * Resumes playback from where it was last paused
   * @throws Exception
   */
  public synchronized void resumePlayback() throws Exception {
    if (getState() != PAUSED) {
      throw new IllegalStateException("Cannot resume. Mp3Player has not " +
          "been paused.");
    }
    if (playThread == null) {
      throw new IllegalStateException("Cannot resume. Cannot determine mp3 " +
          "to resume.");
    }
    playThread.resumePlayback();
    setState(PLAYING);
  }

  /**
   * Stops playback
   * @throws Exception
   */
  public synchronized void stopPlayback() throws Exception {
    killPlaybackThread();
  }

  /**
   * Skips the currently playing track.
   * @throws Exception
   */
  public synchronized void skipTrack() throws Exception {
    if (playThread == null) {
      throw new IllegalStateException("No Mp3s are playing");
    }
    playThread.skip();
  }

  /**
   * Sets the Mp3 to be played.
   * @param mp3 Mp3File
   * @throws IllegalArgumentException
   */
  public synchronized void setMp3File(Mp3File mp3) throws
      IllegalArgumentException {
    if (mp3 == null) {
      throw new IllegalArgumentException("Mp3File is null");
    }
    this.mp3 = mp3;
  }

  /**
   * Returns the Mp3 set by setMp3File.
   *
   * @throws IllegalStateException
   * @return Mp3File
   */
  public synchronized Mp3File getMp3File() throws IllegalStateException {
    if (mp3 == null) {
      throw new IllegalStateException("Mp3File has not been set.");
    }
    return mp3;
  }

  /**
   * Returns the current state of the Player.
   * @return int
   */
  public int getState() {
    return state;
  }

  /**
   * Sets the current state of the player to be one of:
   * <ul>
   *   <li>PLAYING</li>
   *   <li>PAUSED</li>
   *   <li>STOPPED</li>
   * </ul>
   * @param state int
   * @throws IllegalArgumentException
   */
  private void setState(int state) throws IllegalArgumentException {
    if ((state > 2) || (state < 0)) {
      throw new IllegalArgumentException("Unknown state.");
    }
    this.state = state;
  }

  /**
   * Returns a String that represents the specific state.
   * @param state int
   * @throws IllegalArgumentException
   * @return String
   */
  private String stateString(int state) throws IllegalArgumentException {
    switch (state) {
      case (PLAYING) :
        return "PLAYING";
      case (PAUSED) :
        return "PAUSED";
      case (STOPPED) :
        return "STOPPED";
      default:
        throw new IllegalArgumentException("Unknown state");
    }
  }

  /**
   * Sets the Listener to recieve playback events/errors.
   * @param listener PlaybackListener
   */
  public synchronized void setPlaybackListener(PlaybackThreadListener listener) {
    this.listener = listener;
  }

  /**
   * Kills any currently executing threads and creates a new one for the mp3.
   * <p>Returns the current/new PlaybackThread (playThread).
   * @param file Mp3File
   * @throws Exception
   * @return PlaybackThread
   */
  private synchronized PlaybackThread newPlaybackThread(Mp3File file) throws
      Exception {
    killPlaybackThread();
    PlaybackThread thread = new PlaybackThreadImpl(file, this);
    Iterator iter = Collections.singletonList(thread).iterator();
    playThread = new PlaylistThreadImpl(iter, this);
    return playThread;
  }

  /**
   * Kills any currently executing threads and creates a new one that will play
   * all mp3s in the array.
   *
   * <p>Returns the current/new PlaybackThread (playThread).
   *
   * @param files Mp3File
   * @throws Exception
   * @return PlaybackThread
   */
  private synchronized PlaybackThread newPlaybackThread(Mp3File[] files) throws
      Exception {
    killPlaybackThread();
    if (files == null) {
      throw new IllegalArgumentException("Mp3File array is null.");
    }
    //convert files to PlaybackThreads
    PlaybackThreadImpl[] threads = new PlaybackThreadImpl[files.length];
    for (int i = 0; i < files.length; i++) {
      threads[i] = new PlaybackThreadImpl(files[i], this);
    }
    //return a list that contains all threads
    playThread = new PlaylistThreadImpl(threads, this);
    return playThread;
  }

  /**
   * Kills any currently executing threads and creates a new one that will play
   * all mp3s in the array.
   *
   * <p>Returns the current/new PlaybackThread (playThread).
   *
   * @param files Mp3File
   * @throws Exception
   * @return PlaybackThread
   */
  private synchronized PlaybackThread newPlaybackThread(Iterator files) throws
      Exception {
    killPlaybackThread();
    if (files == null) {
      throw new IllegalArgumentException("Mp3File array is null.");
    }
    //convert files to PlaybackThreads
    List threadList = new ArrayList();
    while (files.hasNext()) {
      threadList.add(new PlaybackThreadImpl((Mp3File) files.next(), this));
    }
    //return a list that contains all threads
    playThread = new PlaylistThreadImpl(threadList.iterator(), this);
    return playThread;
  }

  /**
   * Kills any executing Playback Thread and waits for it to die.
   * @throws Exception
   */
  private synchronized void killPlaybackThread() throws Exception {
    if (playThread != null) {
      playThread.terminate();
    }
  }

  /**
   * Returns the currently executing playback thread.
   * @throws IllegalStateException
   * @return PlaybackThread
   */
  private synchronized PlaybackThread getPlaybackThread() throws
      IllegalStateException {
    if (playThread == null) {
      throw new IllegalStateException("No Thread is currently playing.");
    }
    return playThread;
  }

  /**
   * Terminates any executing Thread.
   * @throws Exception
   */
  public synchronized void close() throws Exception {
    killPlaybackThread();
  }

  /**
   * Indicates an exception occurred during playback.
   * @param t Throwable
   */
  public void exceptionOccurred(Throwable t) {
    if (listener != null) {
      listener.exceptionOccurred(t);
    }
  }

  /**
   * Indicates that the resource has began playing.
   * @param resource URIReference
   */
  public void playbackStarted(URIReference resource) {
    setState(PLAYING);
    if (listener != null) {
      listener.playbackStarted(resource);
    }
  }

  /**
   * Indicates that the thread has finished.
   */
  public void playbackComplete() {
    setState(STOPPED);
    if (listener != null) {
      listener.playbackComplete();
    }
  }

  /**
   * Indicates that playback has been paused
   */
  public void playbackPaused() {
    setState(PAUSED);
    if (listener != null) {
      listener.playbackPaused();
    }
  }

  /**
   * Indicates that playback has resumed
   */
  public void playbackResumed() {
    setState(PLAYING);
    if (listener != null) {
      listener.playbackResumed();
    }
  }

}
