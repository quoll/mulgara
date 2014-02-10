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

// Logging
import org.apache.log4j.Logger;

// Local packages
import java.util.*;
import org.jrdf.graph.*;

/**
 * Plays a List of PlaybackThreadImpls until terminated.
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
public class PlaylistThreadImpl extends Thread implements PlaylistThread {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(PlaylistThreadImpl.class.
      getName());

  /** Used to notify caller of exception */
  private PlaybackThreadListener listener = null;

  /** currently executing thread */
  private PlaybackThreadImpl currentThread = null;

  /** List of Threads to be played */
//  private List threadList = null;

  /** Threads to be played */
  private Iterator<?> threadIter = null;

  /** Used to determine if this Thread has been terminated */
  private boolean terminated = false;

  /**
   * Constructor
   *
   * @param threads PlaybackThreadImpl []
   * @param listener PlaybackListener can be null.
   * @throws Exception
   */
  public PlaylistThreadImpl(PlaybackThreadImpl [] threads,
      PlaybackThreadListener listener) throws Exception {
    this(Arrays.asList(threads).iterator(), listener);
//    super();
//    threadList = Arrays.asList(threads);
//    threadIter = threadList.iterator();
//    this.listener = listener;
  }

  /**
   * Constructor
   *
   * @param threadIter PlaybackThreadImpl []
   * @param listener PlaybackListener can be null.
   * @throws Exception
   */
  public PlaylistThreadImpl(Iterator<?> threadIter,
      PlaybackThreadListener listener) throws Exception {
    super();
    this.threadIter = threadIter;
    this.listener = listener;
  }

  /**
   * Stops the Player which allows play() to complete (and the Thread to
   * complete). NOT TO BE CALLED FROM WITHIN RUN.
   *
   * @throws Exception
   */
  public void terminate() throws Exception {
    terminated = true;
    //stop current playback
    killCurrentThread();
    //Tell the caller thread to wait for run() to finish
    this.join();
  }

  /**
   * Skips the currently playing thread.
   * @throws Exception
   */
  public void skip() throws Exception {
    //stop current playback - next will excute as normal
    killCurrentThread();
  }

  /**
   * Starts playing the mp3.
   */
  public void run() {
    try {
      currentThread = null;
      while (threadIter.hasNext()
          && !terminated) {
        //start and wait
        currentThread = (PlaybackThreadImpl) threadIter.next();
        currentThread.start();
        currentThread.join();
      }
    }
    catch (Exception exception) {
      if (listener != null) {
        listener.exceptionOccurred(exception);
      }
      else {
        log.info("Exception occurred", exception);
      }
    }
  }

  /**
   * If there is a Thread running, it is killed.
   * @throws Exception
   */
  private synchronized void killCurrentThread() throws Exception {
    if (currentThread != null) {
      currentThread.terminate();
    }
  }

  /**
   * Returns the resource that is currently being played. Or the first resource
   * in the List. Throws an exception if the List is empty.
   * @throws Exception
   * @return URIReference
   */
  public URIReference getResource() throws Exception {
    if (currentThread != null) {
      return currentThread.getResource();
    } else {
      throw new IllegalStateException("Playlist is not playing.");
    }
  }

  /**
   * Pauses playback.
   * @throws Exception
   */
  public void pausePlayback() throws Exception {
    if (currentThread == null) {
      throw new IllegalStateException("Playlist is not playing.");
    }
    currentThread.pausePlayback();
  }

  /**
   * Resumes paused playback
   * @throws Exception
   */
  public void resumePlayback() throws Exception {
    if (currentThread == null) {
      throw new IllegalStateException("Playlist is not playing.");
    }
    currentThread.resumePlayback();
  }

}
