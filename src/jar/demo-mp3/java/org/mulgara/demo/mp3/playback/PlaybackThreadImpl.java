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

// Logging
import org.apache.log4j.Logger;

// JLayer
import javazoom.jl.player.*;
import javazoom.jl.player.advanced.*;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.*;



/**
 * Plays an Mp3File until terminated. If a next Play thread has been set, it is
 * played at the end of run().
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
public class PlaybackThreadImpl extends Thread implements PlaybackThread {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(PlaybackThreadImpl.class.
      getName());

  /** Mp3 to be played */
  private Mp3File mp3 = null;

  /** Stream that is playing (can be blocked) */
  private BlockableInputStream stream = null;

  /** Plays the mp3 */
  private AdvancedPlayer player = null;

  /** Device used to determine current frame */
  private AudioDevice device = null;

  /** To be notified of playback events/errors */
  private PlaybackThreadListener listener = null;

  /** Used to determine if this Thread has been terminated */
  private boolean terminated = false;


  /**
   * Constructor
   *
   * @param mp3 Mp3File
   * @param listener PlaybackListener can be null.
   * @throws Exception
   */
  public PlaybackThreadImpl(Mp3File mp3,
      PlaybackThreadListener listener) throws Exception {
    super();
    if (mp3 == null) {
      throw new IllegalArgumentException("Mp3File is null");
    }
    this.mp3 = mp3;
    this.listener = listener;
    stream = new BlockableInputStream(mp3.getURL().openStream());
    device = FactoryRegistry.systemRegistry().createAudioDevice();
    player = new AdvancedPlayer(stream, device);
    player.setPlayBackListener(listener);
  }

  /**
   * Stops the Player which allows play() to complete (and the Thread to
   * complete). NOT TO BE CALLED FROM WITHIN RUN.
   *
   * @throws Exception
   */
  public void terminate() throws Exception {
    if (!terminated) {
      terminated = true;
      //must be playing
      resumePlayback();
      //AdvancedPlayer.stop() has a NPE bug (before calling close())
      player.close();
      //Tell the caller thread to wait for run() to finish
      this.join();
    }
  }

  /**
   * Starts playing the mp3.
   */
  public void run() {
    try {
      if (!terminated) {
        notifyPlaybackStarted();
        player.play();
        player.close();
        notifyPlaybackComplete();
      }
    }
    catch (Exception exception) {
      notifyException(exception);
    }
  }

  /**
   * Pauses playback.
   * @throws Exception
   */
  public void pausePlayback() throws Exception {
    stream.block();
    notifyPlaybackPaused();
  }

  /**
   * Resumes paused playback
   * @throws Exception
   */
  public void resumePlayback() throws Exception {
    stream.unblock();
    notifyPlaybackResumed();
  }

  /**
   * Returns the resource that the Thread represents.
   * @throws Exception
   * @return URIReference
   */
  public URIReference getResource() throws Exception {
    return mp3.getResource();
  }

  /**
   * If there is a Playback Listener, it is informed that the mp3 has began
   * playing.
   *
   * @throws Exception
   */
  private void notifyPlaybackStarted () throws Exception {
    if (listener != null) {
      listener.playbackStarted(getResource());
    }
  }

  /**
   * If there is a Playback Listener, it is informed that the mp3 has finished
   * playing.
   *
   * @throws Exception
   */
  private void notifyPlaybackComplete () throws Exception {
    if (listener != null) {
      listener.playbackComplete();
    }
  }

  /**
   * If there is a Playback listener, it is notified of the Exception.
   *
   * @param t Throwable
   */
  private void notifyException (Throwable t) {
    if (listener != null) {
      listener.exceptionOccurred(t);
    }
    else {
      log.info("Exception occurred", t);
    }
  }

  /**
   * Notifies any listener that playback has been paused.
   * @throws Exception
   */
  private void notifyPlaybackPaused() throws Exception {
    if (listener != null) {
      listener.playbackPaused();
    }
  }

  /**
   * Notifies any listener that playback has resumed.
   * @throws Exception
   */
  private void notifyPlaybackResumed() throws Exception {
    if (listener != null) {
      listener.playbackResumed();
    }
  }


}
