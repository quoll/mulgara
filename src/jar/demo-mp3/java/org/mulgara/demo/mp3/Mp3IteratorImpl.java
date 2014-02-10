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

package org.mulgara.demo.mp3;

// Java 2 standard packages


// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.Triple;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;


/**
 * java.util.Iterator that returns Mp3Files.
 *
 * @created 2004-12-03
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
public class Mp3IteratorImpl implements Mp3Iterator {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3IteratorImpl.class.
      getName());

  /** Used to generate Mp3Files */
  private ClosableIterator<Triple> iterator = null;

  /** Used to intialize Mp3Files */
  private Mp3Context context = null;

  /** Cached next Triple with an unique subject (mp3) */
  private Triple next = null;

  /**
   * Constructor
   *
   * @param newIterator ClosableIterator
   * @param context Mp3Context
   */
  public Mp3IteratorImpl(ClosableIterator<Triple> newIterator, Mp3Context context) {
    if (newIterator == null) {
      throw new IllegalArgumentException("ClosableIterator is null");
    }
    if (context == null) {
      throw new IllegalArgumentException("Mp3Context is null");
    }
    iterator = newIterator;
    this.context = context;
    //pre-cache the first Triple
    if (iterator.hasNext()) {
      next = iterator.next();
    }
  }

  /**
   * Returns the next Mp3File.
   * @see java.util.Iterator#next()
   *
   * @throws IllegalStateException
   * @return Mp3File
   */
  public Mp3File nextMp3() throws IllegalStateException {
    Object mp3 = next();
    if (mp3 instanceof Mp3File) {
         return (Mp3File) mp3;
    } else {
      throw new IllegalStateException("next() should have returned an Mp3File.");
    }
  }

  /**
   * Closes the underlying iterator.
   */
  public void close() {
    iterator.close();
  }

  /**
   * Not supported. Throws Exception.
   */
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported.");
  }

  /**
   * Returns true if the underlying iterator has another mp3 that is not equal
   * to the last one returned.
   * @return boolean
   */
  public boolean hasNext() {
    return next != null;
  }

  /**
   * returns the pre-cached mp3File and pre-caches the next one.
   *
   * @return Object
   */
  public Mp3File next() {
    if (!hasNext()) {
      throw new IllegalStateException("hasNext() returns false.");
    }
    try {
      SubjectNode mp3 = next.getSubject();
      if (!(mp3 instanceof URIReference)) {
        throw new IllegalStateException("mp3 Resource is not a URIReference.");
      }
      return newMp3File((URIReference) mp3);
    } finally {
      //after returning the copy, pre-cache the next Triple
      getNext();
    }
  }

  /**
   * Pre-caches the next Triple (with a new subject/mp3). Sets it to null if
   * there are no more.
   */
  private void getNext() {

    if (next == null) {
      return;
    }
    SubjectNode currentSubject = next.getSubject();
    next = null;
    Triple nextTriple = null;
    SubjectNode nextSubject = null;

    if (currentSubject == null) {
      return;
    }
    while (iterator.hasNext()) {
      nextTriple = (Triple) iterator.next();
      nextSubject = nextTriple.getSubject();
      //has the subject changed?
      if (!currentSubject.equals(nextSubject)) {
        next = nextTriple;
        return;
      }
    }
  }

  /**
   * Creates a new Mp3File with the supplied Resource.
   * @param mp3 URIReference
   * @return Mp3File
   */
  private Mp3File newMp3File(URIReference mp3) {
    Mp3File file = new Mp3FileImpl();
    file.setResource(mp3);
    file.init(context);
    return file;
  }

}
