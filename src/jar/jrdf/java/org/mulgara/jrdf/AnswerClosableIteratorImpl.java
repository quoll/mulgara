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

package org.mulgara.jrdf;

// Standard java
import java.util.NoSuchElementException;

// Log4J
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;

// Internal packages
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.TripleImpl;


/**
 * An implementation {@link org.jrdf.util.ClosableIterator} that wraps
 * {@link Answer} and produces JRDF triples.
 *
 * @created 2004-06-28
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:18 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AnswerClosableIteratorImpl implements ClosableIterator<Triple> {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger logger =
      Logger.getLogger(AnswerClosableIteratorImpl.class.getName());

  /**
   * The answer object that is being wrapped.
   */
  private Answer answer;

  /**
   * Graph used to perform remove calls.
   */
  private Graph graph = null;

  /**
   * Whether there is a next element.
   */
  private boolean hasNext = false;

  /**
   * Triple returned by last next() call. Used by remove() method.
   */
  private Triple lastReturned = null;

  /**
   * Creates a new iterator.
   *
   * @param newAnswer the answer object to wrap.
   * @throws IllegalArgumentException if the answer object is invalid.
   */
  public AnswerClosableIteratorImpl(Answer newAnswer)
      throws IllegalArgumentException {

    this(newAnswer, null);
  }

  /**
   * Creates a new iterator.
   *
   * @param newAnswer the answer object to wrap.
   * @param graph Graph the Graph that created the Iterator (can be null).
   * Used for calls to remove().
   * @throws IllegalArgumentException if the answer object is invalid.
   */
  public AnswerClosableIteratorImpl(Answer newAnswer, Graph graph)
      throws IllegalArgumentException {

    if (newAnswer == null) {
      throw new IllegalArgumentException("Answer cannnot be null");
    }

    answer = newAnswer;

    //graph can be null
    this.graph = graph;

    try {

      if ((answer != null) && (answer.getRowCount() > 0)) {

        // Ensure tuples are at the start.
        answer.beforeFirst();

        // Go to first tuples
        hasNext = answer.next();
      }
    }
    catch (TuplesException te) {

      logger.error("Could not initalize tuples", te);
      throw new IllegalArgumentException("Could not initialize tuples");
    }
  }

  public boolean hasNext() {

    return hasNext;
  }

  /**
   * Returns the current triple that the iterator is on.  This will be a
   * {@link org.jrdf.graph.Triple}.
   *
   * @return a {@link org.jrdf.graph.Triple}.
   */
  public Triple next() {

    if (!hasNext) {

      // Close iterator and throw NoSuchElementException
      close();
      throw new NoSuchElementException("No more elements in iterator");
    }

    try {

      SubjectNode s = (SubjectNode) answer.getObject(0);
      PredicateNode p = (PredicateNode) answer.getObject(1);
      ObjectNode o = (ObjectNode) answer.getObject(2);

      //Recreate the triple
      Triple triple = new TripleImpl(s, p, o);

      // Get next tuples
      hasNext = answer.next();
      this.lastReturned = triple;
      return triple;
    }
    catch (TuplesException tuplesException) {

      //close Iterator as the tuples are not in a consistent state.
      close();
      throw new IllegalStateException("Failed to get next Triple: " +
                                      tuplesException.getMessage());
    }
  }

  public void reset() throws TuplesException{

    if(this.answer != null) {

      this.answer.beforeFirst();
    }
  }

  /**
   * Remove the last Triple returned by next() from the graph (if supplied).
   * If a Graph has not been supplied, an exception is thrown.
   *
   * @throws UnsupportedOperationException
   */
  public void remove() throws UnsupportedOperationException {

    if (graph != null) {
      try {

        graph.remove(lastReturned);
      }
      catch (GraphException graphException) {
        throw new UnsupportedOperationException("remove() failed: " +
            graphException.getMessage());
      }
    } else {

      throw new UnsupportedOperationException("remove() not supported.");
    }
  }

  public boolean close() {

    try {

      answer.close();
      return true;
    }
    catch (TuplesException te) {

      return false;
    }
  }
}
