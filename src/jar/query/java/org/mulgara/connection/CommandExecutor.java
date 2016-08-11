/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.connection;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mulgara.query.Answer;
import org.mulgara.query.AskQuery;
import org.mulgara.query.BooleanAnswer;
import org.mulgara.query.ConstructQuery;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.operation.Command;
import org.mulgara.query.operation.Load;
import org.mulgara.server.Session;


/**
 * A central point to direct to commands on a connection.
 * 
 * This class also synchronizes access to the session backing this connection, to
 * ensure that the session is not accessed concurrently by multiple threads.
 * 
 * Cancellation is implemented by calling {@link Thread#interrupt()} on the thread
 * currently accessing the session.
 *
 * @created Feb 22, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class CommandExecutor implements Connection {
  
  /** Factory for creating proxy threads. */
  private ThreadFactory threadFactory;
  
  // Fields used to implement session locking and cancellation.
  private Thread sessionThread = null;
  private final Lock sessionLock = new ReentrantLock();
  private final ReadWriteLock threadLock = new ReentrantReadWriteLock();

  /**
   * Construct an command executor, with an optional thread factory that will be used
   * to create proxy threads for executing operations.
   * @param threadFactory If non-null, then every call to {@link #execute(org.mulgara.connection.Connection.SessionOp)}
   * will perform the operation in a proxy thread created with this factory.
   */
  public CommandExecutor(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }

  /**
   * Sets the factory for creating proxy threads.
   * @param threadFactory If non-null, then every call to {@link #execute(org.mulgara.connection.Connection.SessionOp)}
   * will perform the operation in a proxy thread created with this factory.
   */
  void setThreadFactory(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }
  
  /**
   * @see org.mulgara.connection.Connection#execute(org.mulgara.query.operation.Command)
   */
  public String execute(Command cmd) throws Exception {
    return cmd.execute(this).toString();
  }

  /**
   * @see org.mulgara.connection.Connection#execute(org.mulgara.query.operation.Load)
   */
  public Long execute(Load cmd) throws QueryException {
    return (Long)cmd.execute(this);
  }

  /**
   * @see org.mulgara.connection.Connection#execute(org.mulgara.query.Query)
   */
  public Answer execute(Query cmd) throws QueryException, TuplesException {
    return (Answer)cmd.execute(this);
  }

  /**
   * @see org.mulgara.connection.Connection#execute(org.mulgara.query.AskQuery)
   */
  public BooleanAnswer execute(AskQuery cmd) throws QueryException, TuplesException {
    return (BooleanAnswer)cmd.execute(this);
  }

  /**
   * @see org.mulgara.connection.Connection#execute(org.mulgara.query.AskQuery)
   */
  public GraphAnswer execute(ConstructQuery cmd) throws QueryException, TuplesException {
    return (GraphAnswer)cmd.execute(this);
  }

  /* (non-Javadoc)
   * @see org.mulgara.connection.Connection#execute(org.mulgara.connection.Connection.Executable)
   */
  final public <T,E extends Exception> T execute(SessionOp<T,E> cmd) throws E {
    return (this.threadFactory != null) ? executeWithProxy(cmd) : doExecute(cmd);
  }
  
  /**
   * Execute the given operation atomically on the session, using a new proxy thread.
   */
  @SuppressWarnings("unchecked")
  private <T,E extends Exception> T executeWithProxy(final SessionOp<T,E> cmd) throws E {
    assert this.threadFactory != null;
    final Wrapper<T> result = new Wrapper<T>();
    final Wrapper<Throwable> exception = new Wrapper<Throwable>();
    
    Runnable r = new Runnable() {
      public void run() {
        try {
          result.set(doExecute(cmd));
        } catch (Throwable t) {
          // Save the error to re-throw in the calling thread - catch Throwable to make
          // sure all possible errors get reported to the caller (uncaught errors in a
          // child thread are usually lost).
          exception.set(t);
        }
      }
    };
    
    Thread t = this.threadFactory.newThread(r);
    t.start();
    
    while (t.isAlive()) {
      try {
        t.join();
      } catch (InterruptedException e) {
        t.interrupt();
      }
    }
    
    Throwable th = exception.get();
    if (th != null) {
      // The caught exception should have been an instance of the generic type E, but
      // we can't use generic types in a catch statement. First, try an unchecked cast
      // to the declared generic exception type.
      try {
        throw (E)th;
      } catch (ClassCastException cce) {
        // Whatever was caught wasn't of the declared generic exception type.
        // It could be RuntimeException or Error, which don't need to be declared.
        // Check for those, and if all else fails wrap in a RuntimeException so we can re-throw.
        if (th instanceof RuntimeException) {
          throw (RuntimeException)th;
        } else if (th instanceof Error) {
          throw (Error)th;
        } else {
          // TODO This could potentially mask a more serious exception -- 
          // don't know how else to throw the proper generic exception type
          throw new RuntimeException("Unexpected exception in proxy thread", th);
        }
      }
    }
    
    // No error, so return the operation result.
    return result.get();
  }

  /**
   * Execute the given operation atomically on the session that backs this connection.
   */
  @SuppressWarnings("deprecation")
  private <T,E extends Exception> T doExecute(SessionOp<T,E> cmd) throws E {
    sessionLock.lock();
    try {
      Session session = getSession();
      setSessionThread(Thread.currentThread());
      
      try {
        // TODO To be completely safe, we could wrap the session in a closeable facade, but that's probably overkill.
        return cmd.fn(session);
      } finally {
        setSessionThread(null);
      }
    } finally {
      sessionLock.unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.mulgara.connection.Connection#cancel()
   */
  final public void cancel() {
    threadLock.readLock().lock();
    try {
      if (sessionThread != null) sessionThread.interrupt();
    } finally {
      threadLock.readLock().unlock();
    }
  }

  /**
   * Sets the thread currently using the session, subject to a write-lock on the thread.
   * @param t
   */
  private void setSessionThread(Thread t) {
    threadLock.writeLock().lock();
    try {
      this.sessionThread = t;
    } finally {
      threadLock.writeLock().unlock();
    }
  }
  
  /**
   * Utility class for wrapping a variable so it can be get and set from an anonymous inner class.
   * @param <T> The type of object being wrapped.
   */
  private static class Wrapper<T> {
    private T value = null;
    
    /** Set the wrapped value. */
    public void set(T value) { this.value = value; }
    
    /** Get the wrapped value. */
    public T get() { return this.value; }
  }
}
