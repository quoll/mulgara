/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.resolver.distributed.remote;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * Represents an iterable object on a remote system as a local Set.
 * Created at the server side, and sent across the network.
 * @copyright 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class SetProxy<E extends Serializable> extends AbstractSet<E> implements Serializable {

  /** Serial ID for versioning. */
  private static final long serialVersionUID = -8343698708605937025L;

  /** Logger. */
  private static final Logger logger = Logger.getLogger(RemotePagerImpl.class.getName());

  /** Stores the currently running iterator. */
  private static Object currentIterator = null;

  /** A pager for returning sequential pages of a remote collection. */
  private final RemotePager<E> remotePager;

  /** The size of the remote collection. */
  private final int cachedSize;

  /**
   * Creates a new proxy for a remote collection, meeting the Set interface.
   * @param remotePager A device for sending data from the remote collection one page at a time.
   */
  public SetProxy(RemotePager<E> remotePager) {
    this.remotePager = remotePager;
    // local call for size
    try {
      cachedSize = remotePager.size();
    } catch (RemoteException re) {
      throw new IllegalStateException("The proxy should be instantiated on the host side");
    }
  }


  /**
   * Returns the number of elements in the underlying collection.
   * @return The size of the collection.
   */
  public int size() {
    return cachedSize;
  }


  /**
   * Returns an iterator which will access all the remote data.
   * NOTE: The current implementation allows only one iterator to be active at a time!
   * @return A new iterator for the remote data.
   */
  public Iterator<E> iterator() {
    return new PagedIterator();
  }


  /**
   * An iterator class for traversing remote data.  Network activity is reduced by moving
   * data in large pages at a time.
   */
  private class PagedIterator implements Iterator<E> {

    /** A thread for managing bringing the pages over the network. */
    private Pager pager;

    /** The most recent page of data. */
    private E[] currentPage;

    /** The current position in the current page of data. */
    int index;

    /**
     * Create a new iterator for traversing pages of data.
     */
    public PagedIterator() {
      currentPage = null;
      index = 0;
      currentIterator = this;
      logger.info("Starting pager");
      pager = new Pager();
      currentPage = pager.nextPage();
      logger.info("Started pager");
    }

    
    /**
     * Remove the current element from the data.  Unsupported.
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }


    /**
     * Queries the data to check if more data exists.  Should not need to block.
     * @return <code>true</code> if more data exists.
     * @throws ConcurrentModificationException If more than one iterator is active.
     */
    public boolean hasNext() {
      logger.info("called SetProxy$Iterator.hasNext()");
      testState();
      if (currentPage != null && index < currentPage.length) return true;
      return currentPage != null;
    }


    /**
     * Returns the next element of the data.  Will block until data is available.
     * @return The next item of data in sequence.
     * @throws ConcurrentModificationException If more than one iterator is active.
     */
    public E next() {
      logger.info("called SetProxy$Iterator.next()");
      testState();
      logger.info("Accessing element " + index + " of " + currentPage.length);

      if (currentPage != null && index < currentPage.length) return nextPageElement();
      return nextPageElement();
    }


    /**
     * Gets the next element out of the current page.
     * @return The next element from the current page.
     */
    private E nextPageElement() {
      logger.info("Getting next page element");
      E element = currentPage[index++];
      if (index == currentPage.length) updatePage();
      return element;
    }


    /**
     * Moves to the next page, if another page is available.
     */
    private void updatePage() {
      logger.info("Moving to next page");
      currentPage = pager.nextPage();
      index = 0;
    }


    /**
     * Check that this is the only iterator being accessed at the moment.
     * @throws ConcurrentModificationException If this iterator is being accessed
     *         after a new iterator has been created.
     */
    private void testState() {
      if (currentIterator != this) {
        throw new ConcurrentModificationException("Unable to use more than one remote iterator on the set");
      }
    }


    /**
     * Private thread for getting the next page in the background.
     */
    private class Pager extends Thread {

      /** The maximum number of pages that may be queued. */
      private final int maxPages = Config.getMaxPages();

      /** Maximum time to wait for a page to arrive, in milliseconds. */
      private final long timeout = Config.getTimeout();

      /** Indicates that the thread has finished. */
      private boolean complete;

      /** The retrieved pages. */
      private Queue<E[]> retrievedPages;

      /** Stores exception when one occurs. */
      private PagerException lastException;

      /**
       * Initialize and start the thread.
       * Main thread.
       */
      public Pager() {
        lastException = null;
        retrievedPages = new LinkedList<E[]>();
        try {
          logger.info("Getting first page");
          E[] page = remotePager.firstPage();
          if (page != null) {
            logger.info("Got data in first page: size=" + page.length);
            retrievedPages.add(page);
            complete = false;
            start();
          } else logger.info("Empty initial page");
        } catch (RemoteException re) {
          throw new PagerException("Unable to get the first page", re);
        }
      }


      /**
       * Checks if the thread is active.  Main thread.
       * @return <code>false</code> if the thread is still running, <code>true</code> when complete.
       */
      @SuppressWarnings("unused")
      public boolean isComplete() {
        if (lastException != null) throw lastException;
        return complete;
      }


      /**
       * Pick up all the pages.
       * Runs in the background Paging thread.
       */
      public void run() {
        try {
          while (true) {
            synchronized (retrievedPages) {
              while (retrievedPages.size() >= maxPages) {
                try {
                  logger.info("Waiting for queue to empty.  Currently at: " + retrievedPages.size());
                  retrievedPages.wait();
                } catch (InterruptedException ie) { }
              }
            }
            E[] page = remotePager.nextPage();
            if (page == null) {
              logger.info("Got final page");
              break;
            }
            logger.info("Got next page.  size=" + page.length);
            synchronized (retrievedPages) {
              retrievedPages.add(page);
              logger.info("Queue now at " + retrievedPages.size() + " pages");
            }
            synchronized (this) {
              this.notify();
            }
          }
        } catch (RemoteException re) {
          logger.error("Error retrieving remote data", re);
          lastException = new PagerException("Unable to retrieve page", re);
        }
        complete = true;
      }


      /**
       * Get the next page, if available.  The page will be an array of the configured length,
       * or shorter if it is the last page.  If there is no more data, then <code>null</code>
       * will be returned.
       * Runs in the Main thread.
       * @return The next page of data, or <code>null</code> if no more data exists.
       */
      public E[] nextPage() {
        logger.info("Request for next page");
        if (lastException != null) throw lastException;
        E[] page;
        long startTime = System.currentTimeMillis();
        while (true) {
          synchronized (retrievedPages) {
            int oldSize = retrievedPages.size();
            logger.info("Queue has " + oldSize + " pages");
            page = retrievedPages.poll();
            if (oldSize >= maxPages) retrievedPages.notify();
            logger.info("page @" + page);
          }
          long waitTime = timeout + startTime - System.currentTimeMillis();
          if (waitTime <= 0) throw new PagerException("Timed out waiting for page");
          try {
            synchronized (this) {
              if (page == null && !complete) {
                logger.info("Waiting for more pages to arrive");
                this.wait(waitTime);
              } else break;
            }
          } catch (InterruptedException ie) { }
          if (System.currentTimeMillis() - startTime >= timeout) throw new PagerException("Timed out waiting for page");
        }
        logger.info("Returning page = " + page);
        return page;
      }
    }
  }


  /** Exception class for paging.  Must be runtime so it can be thrown through Set interface. */
  @SuppressWarnings("serial")
  public static class PagerException extends RuntimeException {
    public PagerException() { }
    public PagerException(String message) { super(message); }
    public PagerException(String message, RemoteException cause) { super(message, cause); }
    public PagerException(RemoteException cause) { super(cause); }
  }
}

