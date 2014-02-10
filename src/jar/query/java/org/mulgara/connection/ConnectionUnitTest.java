/*
 * Copyright 2010 Revelytix.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.server.Session;

/**
 * Test the command execution methods of Connection interface.
 */
public class ConnectionUnitTest extends TestCase {
  
  protected static ThreadFactory threadFactory = new ThreadFactory() {
    public Thread newThread(Runnable r) {
      return new Thread(r);
    }
  };
  
  public ConnectionUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new ConnectionUnitTest("testExecute"));
    suite.addTest(new ConnectionUnitTest("testExecuteProxy"));
    suite.addTest(new ConnectionUnitTest("testException"));
    suite.addTest(new ConnectionUnitTest("testExceptionProxy"));
    suite.addTest(new ConnectionUnitTest("testLock"));
    suite.addTest(new ConnectionUnitTest("testLockProxy"));
    suite.addTest(new ConnectionUnitTest("testCancel"));
    suite.addTest(new ConnectionUnitTest("testCancelProxy"));
    return suite;
  }
  
  public void testExecute() throws Exception {
    doTestExecute(null);
  }
  
  public void testExecuteProxy() throws Exception {
    doTestExecute(threadFactory);
  }
  
  public void testException() throws Exception {
    doTestException(null);
  }
  
  public void testExceptionProxy() throws Exception {
    doTestException(threadFactory);
  }
  
  public void testLock() throws Exception {
    doTestLock(null);
  }
  
  public void testLockProxy() throws Exception {
    doTestLock(threadFactory);
  }
  
  public void testCancel() throws Exception {
    doTestCancel(null);
  }
  
  public void testCancelProxy() throws Exception {
    doTestCancel(threadFactory);
  }
  
  protected void doTestExecute(ThreadFactory factory) throws Exception {
    Connection conn = new DummyConnection(factory);
    final int testValue = 123;
    int value = conn.execute(new SessionOp<Integer,Exception>() {
      public Integer fn(Session session) throws Exception {
        return testValue;
      }
    });
    assertEquals(testValue, value);
    
    final String exMsg = "Test Exception Message";
    try {
      conn.execute(new SessionOp<Object,Exception>() {
        public Object fn(Session arg) throws Exception {
          throw new Exception(exMsg);
        }
      });
      fail("Should have thrown exception");
    } catch (Exception ex) {
      assertEquals(exMsg, ex.getMessage());
    }
  }
  
  protected void doTestException(ThreadFactory factory) throws Exception {
    Connection conn = new DummyConnection(factory);
    final Wrapper<Object> exception = new Wrapper<Object>();
    
    try {
      conn.execute(new SessionOp<Object,ConnectionTestException>() {
        public Object fn(Session arg) throws ConnectionTestException {
          ConnectionTestException e = new ConnectionTestException("Test Exception Message");
          exception.set(e);
          throw e;
        }
      });
      fail("Should have thrown exception");
    } catch (ConnectionTestException e) {
      assertTrue(e == exception.get());
    }
    
    exception.set(null);
    try {
      conn.execute(new SessionOp<Object,ConnectionTestException>() {
        public Object fn(Session arg) throws ConnectionTestException {
          RuntimeException e = new RuntimeException("Test Exception Message");
          exception.set(e);
          throw e;
        }
      });
      fail("Should have thrown exception");
    } catch (RuntimeException e) {
      assertTrue(e == exception.get());
    }
    
    exception.set(null);
    try {
      conn.execute(new SessionOp<Object,ConnectionTestException>() {
        public Object fn(Session arg) throws ConnectionTestException {
          Error e = new Error("Test Exception Message");
          exception.set(e);
          throw e;
        }
      });
      fail("Should have thrown exception");
    } catch (Throwable th) {
      assertTrue(th == exception.get());
    }
  }
  
  protected void doTestLock(ThreadFactory factory) throws Exception {
    final Connection conn = new DummyConnection(factory);
    final AtomicBoolean t1Started = new AtomicBoolean(false);
    final AtomicBoolean t1Complete = new AtomicBoolean(false);
    final AtomicBoolean t2Started = new AtomicBoolean(false);
    final AtomicBoolean t1Error = new AtomicBoolean(false);
    final AtomicBoolean t2Error = new AtomicBoolean(false);
    
    Runnable r1 = new Runnable() {
      public void run() {
        conn.execute(new SessionOp<Object,RuntimeException>() {
          public Object fn(Session session) throws RuntimeException {
            synchronized (t1Started) {
              t1Started.set(true);
              t1Started.notify();
            }
            safeSleep(5000);
            if (t2Started.get()) t1Error.set(true);
            t1Complete.set(true);
            return null;
          }
        });
      }
    };
    
    Runnable r2 = new Runnable() {
      public void run() {
        synchronized (t1Started) {
          while (!t1Started.get()) safeWait(t1Started);
        }
        conn.execute(new SessionOp<Object,RuntimeException>(){
          public Object fn(Session session) throws RuntimeException {
            if (!t1Complete.get()) t2Error.set(true);
            return null;
          }
        });
      }
    };
    
    runAll(r1, r2);
    
    assertFalse("t2 started before t1 complete", t1Error.get() || t2Error.get());
  }
  
  protected void doTestCancel(ThreadFactory factory) throws Exception {
    final Connection conn = new DummyConnection(factory);
    final AtomicBoolean interrupted = new AtomicBoolean(false);
    
    final Runnable r = new Runnable() {
      public void run() {
        boolean result = conn.execute(new SessionOp<Boolean,RuntimeException>() {
          public Boolean fn(Session session) throws RuntimeException {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              return true;
            }
            return false;
          }
        });
        interrupted.set(result);
      }
    };
    
    Thread t = new Thread(r);
    t.start();
    
    safeSleep(1000);
    conn.cancel();
    
    safeJoin(t);
    assertTrue("thread should have been interrupted", interrupted.get());
  }
  
  protected static void safeSleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      fail("Interrupted while sleeping");
    }
  }
  
  protected static void safeWait(Object obj) {
    try {
      obj.wait();
    } catch (InterruptedException e) {
      fail("Interrupted while waiting");
    }
  }
  
  protected static void safeJoin(Thread t) {
    try {
      t.join();
    } catch (InterruptedException e) {
      fail("Interrupted while joining");
    }
  }
  
  protected static void runAll(Runnable... ops) {
    List<Thread> threads = new ArrayList<Thread>(ops.length);
    for (Runnable r : ops) {
      Thread t = new Thread(r);
      t.start();
      threads.add(t);
    }
    for (Thread t : threads) {
      safeJoin(t);
    }
  }
  
  protected static class ConnectionTestException extends Exception {
    private static final long serialVersionUID = 7763762484291936870L;

    public ConnectionTestException() {
      super();
    }

    public ConnectionTestException(String message) {
      super(message);
    }
  }
  
  private static class Wrapper<T> {
    private T value = null;
    public void set(T value) { this.value = value; }
    public T get() { return this.value; }
  }
}
