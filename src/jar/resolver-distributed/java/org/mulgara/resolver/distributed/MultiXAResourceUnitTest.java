/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.distributed;

// Java 2 standard packages
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

// Third party packages
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.resolver.spi.DummyXAResource;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Testing MultiXAResource.
 *
 * @created 2008-10-21
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Project Foundation</a>
 * @licence Apache License v2.0
 */
public class MultiXAResourceUnitTest extends TestCase {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(MultiXAResourceUnitTest.class);

  // XA error codes in shorter form
  private static final int UN = 0;
  private static final int RB = XAException.XA_RBOTHER;
  private static final int HR = XAException.XA_HEURHAZ;
  private static final int ER = XAException.XAER_RMERR;
  private static final int FL = XAException.XAER_RMFAIL;
  private static final int NT = XAException.XAER_NOTA;

  public MultiXAResourceUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new MultiXAResourceUnitTest("testCommit"));
    suite.addTest(new MultiXAResourceUnitTest("testRollback"));
    suite.addTest(new MultiXAResourceUnitTest("testSuspend"));
    suite.addTest(new MultiXAResourceUnitTest("testStartFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testSuspendFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testResumeFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testEndFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testPrepareFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testCommitFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testRollbackFailure"));
    suite.addTest(new MultiXAResourceUnitTest("testMultiFailure"));

    return suite;
  }

  //
  // Test cases
  //

  /**
   * Test simple sequence ending in commit.
   */
  public void testCommit() {
    logger.info("Testing commit");

    testCommit(new MockXAResource[] { new MockXAResource() });
    testCommit(new MockXAResource[] { new MockXAResource(), new MockXAResource() });
    testCommit(new MockXAResource[] {
            new MockXAResource(), new MockXAResource(), new MockXAResource() });
  }

  private void testCommit(MockXAResource[] mocks) {
    testCommit(mocks, new int[] { });
    testCommit(mocks, new int[] { 0 });

    if (mocks.length < 2) return;
    testCommit(mocks, new int[] { 1 });
    testCommit(mocks, new int[] { 0, 1 });

    if (mocks.length < 3) return;
    testCommit(mocks, new int[] { 2 });
    testCommit(mocks, new int[] { 0, 2 });
    testCommit(mocks, new int[] { 1, 2 });
    testCommit(mocks, new int[] { 0, 1, 2 });
  }

  private void testCommit(MockXAResource[] mocks, int[] readOnlys) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // one-phase commit
      for (MockXAResource mock : mocks) mock.reset();
      for (int idx : readOnlys) mocks[idx].prepareStatus = XAResource.XA_RDONLY;

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUCCESS);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ENDED, mock.state);

      xares.commit(xid, true);
      for (int idx = 0; idx < mocks.length; idx++) {
        if (Arrays.binarySearch(readOnlys, idx) >= 0) {
          assertEquals(MockXAResource.State.ROLLEDBACK, mocks[idx].state);
        } else {
          assertEquals(MockXAResource.State.COMMITTED, mocks[idx].state);
        }
      }

      assertEquals(0, xares.getTxns().size());

      // two-phase commit
      for (MockXAResource mock : mocks) mock.reset();
      for (int idx : readOnlys) mocks[idx].prepareStatus = XAResource.XA_RDONLY;

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUCCESS);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ENDED, mock.state);

      xares.prepare(xid);
      for (int idx = 0; idx < mocks.length; idx++) {
        if (Arrays.binarySearch(readOnlys, idx) >= 0) {
          assertEquals(MockXAResource.State.ROLLEDBACK, mocks[idx].state);
        } else {
          assertEquals(MockXAResource.State.PREPARED, mocks[idx].state);
        }
      }

      xares.commit(xid, false);
      for (int idx = 0; idx < mocks.length; idx++) {
        if (Arrays.binarySearch(readOnlys, idx) >= 0) {
          assertEquals(MockXAResource.State.ROLLEDBACK, mocks[idx].state);
        } else {
          assertEquals(MockXAResource.State.COMMITTED, mocks[idx].state);
        }
      }

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test simple sequence ending in rollback.
   */
  public void testRollback() {
    logger.info("Testing rollback");

    testRollback(new MockXAResource[] { new MockXAResource() });
    testRollback(new MockXAResource[] { new MockXAResource(), new MockXAResource() });
    testRollback(new MockXAResource[] {
            new MockXAResource(), new MockXAResource(), new MockXAResource() });
  }

  private void testRollback(MockXAResource[] mocks) {
    testRollback(mocks, new int[] { });
    testRollback(mocks, new int[] { 0 });

    if (mocks.length < 2) return;
    testRollback(mocks, new int[] { 1 });
    testRollback(mocks, new int[] { 0, 1 });

    if (mocks.length < 3) return;
    testRollback(mocks, new int[] { 2 });
    testRollback(mocks, new int[] { 0, 2 });
    testRollback(mocks, new int[] { 1, 2 });
    testRollback(mocks, new int[] { 0, 1, 2 });
  }

  private void testRollback(MockXAResource[] mocks, int[] readOnlys) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // rollback w/o prepare
      for (MockXAResource mock : mocks) mock.reset();
      for (int idx : readOnlys) mocks[idx].prepareStatus = XAResource.XA_RDONLY;

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUCCESS);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ENDED, mock.state);

      xares.rollback(xid);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ROLLEDBACK, mock.state);

      assertEquals(0, xares.getTxns().size());

      // rollback after prepare
      for (MockXAResource mock : mocks) mock.reset();
      for (int idx : readOnlys) mocks[idx].prepareStatus = XAResource.XA_RDONLY;

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUCCESS);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ENDED, mock.state);

      xares.prepare(xid);
      for (int idx = 0; idx < mocks.length; idx++) {
        if (Arrays.binarySearch(readOnlys, idx) >= 0) {
          assertEquals(MockXAResource.State.ROLLEDBACK, mocks[idx].state);
        } else {
          assertEquals(MockXAResource.State.PREPARED, mocks[idx].state);
        }
      }

      xares.rollback(xid);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ROLLEDBACK, mock.state);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test simple suspend/resume.
   */
  public void testSuspend() {
    logger.info("Testing suspend");

    testSuspend(new MockXAResource[] { new MockXAResource() });
    testSuspend(new MockXAResource[] { new MockXAResource(), new MockXAResource() });
    testSuspend(new MockXAResource[] {
            new MockXAResource(), new MockXAResource(), new MockXAResource() });
  }

  private void testSuspend(MockXAResource[] mocks) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // simple suspend/resume
      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUSPEND);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.SUSPENDED, mock.state);

      xares.start(xid, XAResource.TMRESUME);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ACTIVE, mock.state);

      xares.end(xid, XAResource.TMSUCCESS);
      for (MockXAResource mock : mocks) assertEquals(MockXAResource.State.ENDED, mock.state);

      assertEquals(1, xares.getTxns().size());

      // start/suspend/resume first, then enlist second, suspend and resume
      if (mocks.length < 2) return;

      for (MockXAResource mock : mocks) mock.reset();

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      xares.enlistResource(mocks[0]);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.IDLE, mocks[1].state);
      if (mocks.length > 2) assertEquals(MockXAResource.State.IDLE, mocks[2].state);

      xares.end(xid, XAResource.TMSUSPEND);
      assertEquals(MockXAResource.State.SUSPENDED, mocks[0].state);
      assertEquals(MockXAResource.State.IDLE, mocks[1].state);
      if (mocks.length > 2) assertEquals(MockXAResource.State.IDLE, mocks[2].state);

      xares.start(xid, XAResource.TMRESUME);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.IDLE, mocks[1].state);
      if (mocks.length > 2) assertEquals(MockXAResource.State.IDLE, mocks[2].state);

      xares.enlistResource(mocks[1]);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.ACTIVE, mocks[1].state);
      if (mocks.length > 2) assertEquals(MockXAResource.State.IDLE, mocks[2].state);

      xares.end(xid, XAResource.TMSUCCESS);
      assertEquals(MockXAResource.State.ENDED, mocks[0].state);
      assertEquals(MockXAResource.State.ENDED, mocks[1].state);
      if (mocks.length > 2) assertEquals(MockXAResource.State.IDLE, mocks[2].state);

      assertEquals(2, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test start failures.
   */
  public void testStartFailure() {
    logger.info("Testing start failure");

    testStartFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testStartFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testStartFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testStartFailure(MockFailingXAResource[] mocks) {
    testStartFailure(mocks, UN, 0);
    testStartFailure(mocks, RB, 0);
    testStartFailure(mocks, FL, 0);
    testStartFailure(mocks, NT, 0);

    if (mocks.length < 2) return;
    testStartFailure(mocks, UN, 1);
    testStartFailure(mocks, RB, 1);
    testStartFailure(mocks, FL, 1);
    testStartFailure(mocks, NT, 1);

    if (mocks.length < 3) return;
    testStartFailure(mocks, UN, 2);
    testStartFailure(mocks, RB, 2);
    testStartFailure(mocks, FL, 2);
    testStartFailure(mocks, NT, 2);
  }

  private void testStartFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, 1, -1, -1, -1, false, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (int idx = 0; idx < failer; idx++) xares.enlistResource(mocks[idx]);
      try {
        xares.enlistResource(mocks[failer]);
        fail("should have thrown exception");
      } catch (XAException xae) {
        if (errorCode == FL) assertTrue(FL != xae.errorCode);
        else if (errorCode == NT) assertTrue(NT != xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.ACTIVE,
                     (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                            MockXAResource.State.RB_ONLY,
                     MockXAResource.State.IDLE);
      }

      xares.end(xid, XAResource.TMFAIL);
      assertStates(mocks, failer, MockXAResource.State.ENDED,
                   (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                          MockXAResource.State.RB_ONLY,
                   MockXAResource.State.IDLE);

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.IDLE);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test suspend failures.
   */
  public void testSuspendFailure() {
    logger.info("Testing suspend failure");

    testSuspendFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testSuspendFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testSuspendFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testSuspendFailure(MockFailingXAResource[] mocks) {
    testSuspendFailure(mocks, UN, 0);
    testSuspendFailure(mocks, RB, 0);
    testSuspendFailure(mocks, FL, 0);
    testSuspendFailure(mocks, NT, 0);

    if (mocks.length < 2) return;
    testSuspendFailure(mocks, UN, 1);
    testSuspendFailure(mocks, RB, 1);
    testSuspendFailure(mocks, FL, 1);
    testSuspendFailure(mocks, NT, 1);

    if (mocks.length < 3) return;
    testSuspendFailure(mocks, UN, 2);
    testSuspendFailure(mocks, RB, 2);
    testSuspendFailure(mocks, FL, 2);
    testSuspendFailure(mocks, NT, 2);
  }

  private void testSuspendFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, 1, -1, -1, false, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      try {
        xares.end(xid, XAResource.TMSUSPEND);
        fail("should have thrown exception");
      } catch (XAException xae) {
        if (errorCode == FL) assertTrue(FL != xae.errorCode);
        else if (errorCode == NT) assertTrue(NT != xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.ENDED,
                     (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                            MockXAResource.State.RB_ONLY,
                     MockXAResource.State.ENDED);
      }

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test resume failures.
   */
  public void testResumeFailure() {
    logger.info("Testing resume failure");

    testResumeFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testResumeFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testResumeFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testResumeFailure(MockFailingXAResource[] mocks) {
    testResumeFailure(mocks, UN, 0);
    testResumeFailure(mocks, RB, 0);
    testResumeFailure(mocks, FL, 0);
    testResumeFailure(mocks, NT, 0);

    if (mocks.length < 2) return;
    testResumeFailure(mocks, UN, 1);
    testResumeFailure(mocks, RB, 1);
    testResumeFailure(mocks, FL, 1);
    testResumeFailure(mocks, NT, 1);

    if (mocks.length < 3) return;
    testResumeFailure(mocks, UN, 2);
    testResumeFailure(mocks, RB, 2);
    testResumeFailure(mocks, FL, 2);
    testResumeFailure(mocks, NT, 2);
  }

  private void testResumeFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, 1, -1, false, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUSPEND);
      assertStates(mocks, failer, MockXAResource.State.SUSPENDED, MockXAResource.State.SUSPENDED,
                   MockXAResource.State.SUSPENDED);

      try {
        xares.start(xid, XAResource.TMRESUME);
        fail("should have thrown exception");
      } catch (XAException xae) {
        if (errorCode == FL) assertTrue(FL != xae.errorCode);
        else if (errorCode == NT) assertTrue(NT != xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.ENDED,
                     (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                            MockXAResource.State.RB_ONLY,
                     MockXAResource.State.ENDED);
      }

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test end failures.
   */
  public void testEndFailure() {
    logger.info("Testing end failure");

    testEndFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testEndFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testEndFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testEndFailure(MockFailingXAResource[] mocks) {
    testEndFailure(mocks, UN, 0);
    testEndFailure(mocks, RB, 0);
    testEndFailure(mocks, FL, 0);
    testEndFailure(mocks, NT, 0);

    if (mocks.length < 2) return;
    testEndFailure(mocks, UN, 1);
    testEndFailure(mocks, RB, 1);
    testEndFailure(mocks, FL, 1);
    testEndFailure(mocks, NT, 1);

    if (mocks.length < 3) return;
    testEndFailure(mocks, UN, 2);
    testEndFailure(mocks, RB, 2);
    testEndFailure(mocks, FL, 2);
    testEndFailure(mocks, NT, 2);
  }

  private void testEndFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // straight start-end
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, 1, false, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      try {
        xares.end(xid, XAResource.TMSUCCESS);
        fail("should have thrown exception");
      } catch (XAException xae) {
        if (errorCode == FL) assertTrue(FL != xae.errorCode);
        else if (errorCode == NT) assertTrue(NT != xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.ENDED,
                     (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                            MockXAResource.State.RB_ONLY,
                     MockXAResource.State.ENDED);
      }

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());

      // start-suspend-resume-end
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, 1, false, false, false);

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUSPEND);
      assertStates(mocks, failer, MockXAResource.State.SUSPENDED, MockXAResource.State.SUSPENDED,
                   MockXAResource.State.SUSPENDED);

      xares.start(xid, XAResource.TMRESUME);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      try {
        xares.end(xid, XAResource.TMSUCCESS);
        fail("should have thrown exception");
      } catch (XAException xae) {
        if (errorCode == FL) assertTrue(FL != xae.errorCode);
        else if (errorCode == NT) assertTrue(NT != xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.ENDED,
                     (errorCode == FL || errorCode == NT) ? MockXAResource.State.ROLLEDBACK :
                                                            MockXAResource.State.RB_ONLY,
                     MockXAResource.State.ENDED);
      }

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test prepare failures.
   */
  public void testPrepareFailure() {
    logger.info("Testing prepare failure");

    testPrepareFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testPrepareFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testPrepareFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testPrepareFailure(MockFailingXAResource[] mocks) {
    testPrepareFailure(mocks, UN, 0);
    testPrepareFailure(mocks, RB, 0);
    testPrepareFailure(mocks, FL, 0);
    testPrepareFailure(mocks, NT, 0);

    if (mocks.length < 2) return;
    testPrepareFailure(mocks, UN, 1);
    testPrepareFailure(mocks, RB, 1);
    testPrepareFailure(mocks, FL, 1);
    testPrepareFailure(mocks, NT, 1);

    if (mocks.length < 3) return;
    testPrepareFailure(mocks, UN, 2);
    testPrepareFailure(mocks, RB, 2);
    testPrepareFailure(mocks, FL, 2);
    testPrepareFailure(mocks, NT, 2);
  }

  private void testPrepareFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // implied prepare (one phase commit)
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, true, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      try {
        xares.commit(xid, true);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("not a rollback: " + xae.errorCode, DummyXAResource.isRollback(xae.errorCode));

        assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK,
                     MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK);
      }

      assertEquals(0, xares.getTxns().size());

      // explicit prepare (two phase commit)
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, true, false, false);

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      try {
        xares.prepare(xid);
        fail("should have thrown exception");
      } catch (XAException xae) {
        boolean isEndCode = errorCode == FL || errorCode == NT || DummyXAResource.isRollback(errorCode);
        if (isEndCode) assertEquals(ER, xae.errorCode);
        else assertEquals(errorCode, xae.errorCode);

        assertStates(mocks, failer, MockXAResource.State.PREPARED,
                     (isEndCode) ? MockXAResource.State.ROLLEDBACK : MockXAResource.State.RB_ONLY,
                     MockXAResource.State.PREPARED);
      }

      xares.rollback(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK, MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test commit failures.
   */
  public void testCommitFailure() {
    logger.info("Testing commit failure");

    testCommitFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testCommitFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testCommitFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testCommitFailure(MockFailingXAResource[] mocks) {
    testCommitFailure(mocks, UN, 0);
    testCommitFailure(mocks, RB, 0);
    testCommitFailure(mocks, HR, 0);
    testCommitFailure(mocks, FL, 0);

    if (mocks.length < 2) return;
    testCommitFailure(mocks, UN, 1);
    testCommitFailure(mocks, RB, 1);
    testCommitFailure(mocks, HR, 1);
    testCommitFailure(mocks, FL, 1);

    if (mocks.length < 3) return;
    testCommitFailure(mocks, UN, 2);
    testCommitFailure(mocks, RB, 2);
    testCommitFailure(mocks, HR, 2);
    testCommitFailure(mocks, FL, 2);
  }

  private void testCommitFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // one-phase
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, false, true, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      try {
        xares.commit(xid, true);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("expected heuristic code: " + xae.errorCode, DummyXAResource.isHeuristic(xae.errorCode));

        assertStates(mocks, failer, MockXAResource.State.COMMITTED,
                     (errorCode == UN) ? MockXAResource.State.COMMITTED :
                     (errorCode == HR) ? MockXAResource.State.HEUR :
                                         MockXAResource.State.ROLLEDBACK,
                     (failer == 0 && errorCode == RB) ? MockXAResource.State.ROLLEDBACK : MockXAResource.State.COMMITTED);
      }

      xares.forget(xid);
      assertStates(mocks, failer, MockXAResource.State.COMMITTED,
                   (errorCode == UN) ? MockXAResource.State.COMMITTED :
                   (errorCode == HR) ? MockXAResource.State.HEUR_DONE :
                                       MockXAResource.State.ROLLEDBACK,
                   (failer == 0 && errorCode == RB) ? MockXAResource.State.ROLLEDBACK :
                                                      MockXAResource.State.COMMITTED);

      assertEquals(0, xares.getTxns().size());

      // two-phase
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, false, true, false);

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      xares.prepare(xid);
      assertStates(mocks, failer, MockXAResource.State.PREPARED, MockXAResource.State.PREPARED,
                   MockXAResource.State.PREPARED);

      try {
        xares.commit(xid, false);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("expected heuristic code: " + xae.errorCode, DummyXAResource.isHeuristic(xae.errorCode));

        assertStates(mocks, failer, MockXAResource.State.COMMITTED,
                     (errorCode == UN) ? MockXAResource.State.COMMITTED :
                     (errorCode == HR) ? MockXAResource.State.HEUR :
                                         MockXAResource.State.ROLLEDBACK,
                     (failer == 0 && errorCode == RB) ? MockXAResource.State.ROLLEDBACK :
                                                        MockXAResource.State.COMMITTED);
      }

      xares.forget(xid);
      assertStates(mocks, failer, MockXAResource.State.COMMITTED,
                   (errorCode == UN) ? MockXAResource.State.COMMITTED :
                   (errorCode == HR) ? MockXAResource.State.HEUR_DONE :
                                       MockXAResource.State.ROLLEDBACK,
                   (failer == 0 && errorCode == RB) ? MockXAResource.State.ROLLEDBACK :
                                                      MockXAResource.State.COMMITTED);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test rollback failures.
   */
  public void testRollbackFailure() {
    logger.info("Testing rollback failure");

    testRollbackFailure(new MockFailingXAResource[] { new MockFailingXAResource() });
    testRollbackFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource() });
    testRollbackFailure(new MockFailingXAResource[] {
          new MockFailingXAResource(), new MockFailingXAResource(), new MockFailingXAResource() });
  }

  private void testRollbackFailure(MockFailingXAResource[] mocks) {
    testRollbackFailure(mocks, UN, 0);
    testRollbackFailure(mocks, RB, 0);
    testRollbackFailure(mocks, HR, 0);
    testRollbackFailure(mocks, FL, 0);

    if (mocks.length < 2) return;
    testRollbackFailure(mocks, UN, 1);
    testRollbackFailure(mocks, RB, 1);
    testRollbackFailure(mocks, HR, 1);
    testRollbackFailure(mocks, FL, 1);

    if (mocks.length < 3) return;
    testRollbackFailure(mocks, UN, 2);
    testRollbackFailure(mocks, RB, 2);
    testRollbackFailure(mocks, HR, 2);
    testRollbackFailure(mocks, FL, 2);
  }

  private void testRollbackFailure(MockFailingXAResource[] mocks, int errorCode, int failer) {
    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());

      // after end
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, false, false, true);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      try {
        xares.rollback(xid);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("expected heuristic code: " + xae.errorCode, DummyXAResource.isHeuristic(xae.errorCode));

        assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK,
                     (errorCode == HR) ? MockXAResource.State.HEUR :
                                         MockXAResource.State.ROLLEDBACK,
                     MockXAResource.State.ROLLEDBACK);
      }

      xares.forget(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK,
                   (errorCode == HR) ? MockXAResource.State.HEUR_DONE :
                                       MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());

      // after prepare
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[failer], errorCode, -1, -1, -1, -1, false, false, true);

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertStates(mocks, failer, MockXAResource.State.ACTIVE, MockXAResource.State.ACTIVE,
                   MockXAResource.State.ACTIVE);

      xares.end(xid, XAResource.TMSUCCESS);
      assertStates(mocks, failer, MockXAResource.State.ENDED, MockXAResource.State.ENDED,
                   MockXAResource.State.ENDED);

      xares.prepare(xid);
      assertStates(mocks, failer, MockXAResource.State.PREPARED, MockXAResource.State.PREPARED,
                   MockXAResource.State.PREPARED);

      try {
        xares.rollback(xid);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("expected heuristic code: " + xae.errorCode, DummyXAResource.isHeuristic(xae.errorCode));

        assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK,
                     (errorCode == HR) ? MockXAResource.State.HEUR :
                                         MockXAResource.State.ROLLEDBACK,
                     MockXAResource.State.ROLLEDBACK);
      }

      xares.forget(xid);
      assertStates(mocks, failer, MockXAResource.State.ROLLEDBACK,
                   (errorCode == HR) ? MockXAResource.State.HEUR_DONE :
                                       MockXAResource.State.ROLLEDBACK,
                   MockXAResource.State.ROLLEDBACK);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testMultiFailure() {
    logger.info("Testing multiple failures");

    try {
      TestMultiXAResource xares = new TestMultiXAResource(15, new DummyResolverFactory());
      MockFailingXAResource[] mocks =
          new MockFailingXAResource[] { new MockFailingXAResource(), new MockFailingXAResource() };

      // two in end
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[0], FL, -1, -1, -1, 1, false, false, false);
      setFailMode(mocks[1], RB, -1, -1, -1, 1, false, false, false);

      Xid xid = new TestXid(1);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.ACTIVE, mocks[1].state);

      try {
        xares.end(xid, XAResource.TMSUCCESS);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue(xae.errorCode != FL);

        assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
        assertEquals(MockXAResource.State.RB_ONLY, mocks[1].state);
      }

      xares.rollback(xid);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[1].state);

      assertEquals(0, xares.getTxns().size());

      // two RMFAIL in end
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[0], FL, -1, -1, -1, 1, false, false, false);
      setFailMode(mocks[1], FL, -1, -1, -1, 1, false, false, false);

      xid = new TestXid(2);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.ACTIVE, mocks[1].state);

      try {
        xares.end(xid, XAResource.TMSUCCESS);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue(xae.errorCode != FL);

        assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
        assertEquals(MockXAResource.State.ROLLEDBACK, mocks[1].state);
      }

      xares.rollback(xid);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[1].state);

      assertEquals(0, xares.getTxns().size());

      // one in prepare, one in rollback
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[0], UN, -1, -1, -1, -1, true, false, false);
      setFailMode(mocks[1], HR, -1, -1, -1, -1, false, false, true);

      xid = new TestXid(3);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.ACTIVE, mocks[1].state);

      xares.end(xid, XAResource.TMSUCCESS);
      assertEquals(MockXAResource.State.ENDED, mocks[0].state);
      assertEquals(MockXAResource.State.ENDED, mocks[1].state);

      try {
        xares.prepare(xid);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue(xae.errorCode != FL);

        assertEquals(MockXAResource.State.RB_ONLY, mocks[0].state);
        assertEquals(MockXAResource.State.PREPARED, mocks[1].state);
      }

      try {
        xares.rollback(xid);
      } catch (XAException xae) {
        assertTrue(xae.errorCode != FL);

        assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
        assertEquals(MockXAResource.State.HEUR, mocks[1].state);
      }

      xares.forget(xid);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[0].state);
      assertEquals(MockXAResource.State.HEUR_DONE, mocks[1].state);

      assertEquals(0, xares.getTxns().size());

      // two in commit
      for (MockXAResource mock : mocks) mock.reset();
      setFailMode(mocks[0], UN, -1, -1, -1, -1, false, true, false);
      setFailMode(mocks[1], FL, -1, -1, -1, -1, false, true, false);

      xid = new TestXid(4);
      xares.start(xid, XAResource.TMNOFLAGS);

      for (MockXAResource mock : mocks) xares.enlistResource(mock);
      assertEquals(MockXAResource.State.ACTIVE, mocks[0].state);
      assertEquals(MockXAResource.State.ACTIVE, mocks[1].state);

      xares.end(xid, XAResource.TMSUCCESS);
      assertEquals(MockXAResource.State.ENDED, mocks[0].state);
      assertEquals(MockXAResource.State.ENDED, mocks[1].state);

      try {
        xares.commit(xid, true);
        fail("should have thrown exception");
      } catch (XAException xae) {
        assertTrue("expected heuristic code: " + xae.errorCode, DummyXAResource.isHeuristic(xae.errorCode));

        assertEquals(MockXAResource.State.COMMITTED, mocks[0].state);
        assertEquals(MockXAResource.State.ROLLEDBACK, mocks[1].state);
      }

      xares.forget(xid);
      assertEquals(MockXAResource.State.COMMITTED, mocks[0].state);
      assertEquals(MockXAResource.State.ROLLEDBACK, mocks[1].state);

      assertEquals(0, xares.getTxns().size());
    } catch (Exception e) {
      fail(e);
    }
  }


  //
  // Internal Test Helpers
  //

  private static void assertStates(MockFailingXAResource[] mocks, int failer,
                                   MockXAResource.State preFail, MockXAResource.State fail,
                                   MockXAResource.State postFail) {
    for (int idx = 0; idx < failer; idx++) assertEquals(preFail, mocks[idx].state);
    assertEquals(fail, mocks[failer].state);
    for (int idx = failer + 1; idx < mocks.length; idx++) assertEquals(postFail, mocks[idx].state);
  }

  private static void setFailMode(MockFailingXAResource mock, int errorCode, int failStartAfter,
                                  int failSuspendAfter, int failResumeAfter, int failEndAfter,
                                  boolean failPrepare, boolean failCommit, boolean failRollback) {
    mock.errorCode = errorCode;
    mock.failStartAfter = failStartAfter >= 0 ? failStartAfter : Integer.MAX_VALUE;
    mock.failSuspendAfter = failSuspendAfter >= 0 ? failSuspendAfter : Integer.MAX_VALUE;
    mock.failResumeAfter = failResumeAfter >= 0 ? failResumeAfter : Integer.MAX_VALUE;
    mock.failEndAfter = failEndAfter >= 0 ? failEndAfter : Integer.MAX_VALUE;
    mock.failPrepare = failPrepare;
    mock.failCommit = failCommit;
    mock.failRollback = failRollback;
  }

  /**
   * A simple extension to MultiXAResource so we can get at the list of active transactions.
   */
  private static class TestMultiXAResource extends MultiXAResource {
    public TestMultiXAResource(int transactionTimeout, ResolverFactory resolverFactory) {
      super(transactionTimeout, resolverFactory);
    }

    public Collection<MultiXAResource.MultiTxInfo> getTxns() {
      return resourceManager.transactions.values();
    }
  }

  /**
   * A stub/mock XAResource. This verifies state transitions to make sure they follow the rules as
   * layed out in the X/Open and JTA specs. The prepare-status can be modified to return RDONLY on
   * prepare.
   */
  private static class MockXAResource extends DummyXAResource {
    protected final ThreadLocal<Xid> currTxn = new ThreadLocal<Xid>();

    public static enum State { IDLE, ACTIVE, SUSPENDED, ENDED, RB_ONLY, PREPARED, COMMITTED,
                               ROLLEDBACK, HEUR, HEUR_DONE };
    public State state = State.IDLE;

    public int startCnt = 0;
    public int resumeCnt = 0;
    public int suspendCnt = 0;
    public int endCnt = 0;
    public int prepareCnt = 0;
    public int commitCnt = 0;
    public int rollbackCnt = 0;

    public int prepareStatus = XA_OK;

    public void start(Xid xid, int flags) throws XAException {
      super.start(xid, flags);

      if (currTxn.get() != null) {
        throw new XAException("transaction already active: " + currTxn.get());
      }
      currTxn.set(xid);

      if (flags == XAResource.TMNOFLAGS && state == State.ACTIVE) {
        throw new XAException("resource already active: " + state);
      }
      if (flags == XAResource.TMRESUME && state != State.SUSPENDED) {
        throw new XAException("resource not suspended: " + state);
      }
      state = State.ACTIVE;

      if (flags == XAResource.TMNOFLAGS) startCnt++;
      if (flags == XAResource.TMRESUME) resumeCnt++;
    }

    public void end(Xid xid, int flags) throws XAException {
      super.end(xid, flags);

      if (!(state == State.SUSPENDED && (flags == XAResource.TMSUCCESS || flags == XAResource.TMFAIL))) {
        if (!xid.equals(currTxn.get())) {
          throw new XAException("mismatched transaction end");
        }
        currTxn.set(null);

        if (state != State.ACTIVE) {
          throw new XAException("resource not active: " + state);
        }
      }
      state = (flags == XAResource.TMSUSPEND) ? State.SUSPENDED : State.ENDED;

      if (flags == XAResource.TMSUSPEND) suspendCnt++;
      if (flags != XAResource.TMSUSPEND) endCnt++;
    }

    public int prepare(Xid xid) throws XAException {
      super.prepare(xid);

      if (currTxn.get() != null) {
        throw new XAException("transaction still active: " + currTxn.get());
      }
      if (state != State.ENDED) {
        throw new XAException("resource not ended: " + state);
      }
      state = (prepareStatus == XA_OK) ? State.PREPARED : State.ROLLEDBACK;

      prepareCnt++;
      return prepareStatus;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      super.commit(xid, onePhase);

      if (currTxn.get() != null) {
        throw new XAException("transaction still active: " + currTxn.get());
      }

      if (state != State.HEUR) {
        if (onePhase && state != State.ENDED) {
          throw new XAException("resource not ended: " + state);
        }
        if (!onePhase && state != State.PREPARED) {
          throw new XAException("resource not prepared: " + state);
        }
        state = State.COMMITTED;
      }

      commitCnt++;
    }

    public void rollback(Xid xid) throws XAException {
      super.rollback(xid);

      if (currTxn.get() != null) throw new XAException("transaction still active: " + currTxn.get());

      if (state != State.HEUR) {
        if (state != State.ENDED && state != State.RB_ONLY && state != State.PREPARED) {
          throw new XAException("resource not ended or prepared: " + state);
        }
        state = State.ROLLEDBACK;
      }

      rollbackCnt++;
    }

    public void forget(Xid xid) throws XAException {
      super.forget(xid);

      if (state != State.HEUR) throw new XAException("transaction not heuristically completed: " + state);
      state = State.HEUR_DONE;
    }

    public void reset() {
      state = State.IDLE;
      startCnt = 0;
      resumeCnt = 0;
      suspendCnt = 0;
      endCnt = 0;
      prepareCnt = 0;
      commitCnt = 0;
      rollbackCnt = 0;
      prepareStatus = XA_OK;
      currTxn.set(null);
    }
  }

  /**
   * This extends MockXAResource to be able to force failures at various points. The fail* and
   * errorCode fields can be set to control the behaviour.
   */
  private static class MockFailingXAResource extends MockXAResource {
    public int failStartAfter = Integer.MAX_VALUE;
    public int failSuspendAfter = Integer.MAX_VALUE;
    public int failResumeAfter = Integer.MAX_VALUE;
    public int failEndAfter = Integer.MAX_VALUE;
    public int errorCode = 0;
    public boolean failPrepare = false;
    public boolean failCommit = false;
    public boolean failRollback = false;

    public void start(Xid xid, int flags) throws XAException {
      super.start(xid, flags);
      if (startCnt >= failStartAfter || resumeCnt >= failResumeAfter) {
        currTxn.set(null);
        state = State.RB_ONLY;
        fail("start");
      }
    }

    public void end(Xid xid, int flags) throws XAException {
      super.end(xid, flags);
      if (endCnt >= failEndAfter || suspendCnt >= failSuspendAfter) {
        state = State.RB_ONLY;
        fail("end");
      }
    }

    public int prepare(Xid xid) throws XAException {
      int sts = super.prepare(xid);
      if (failPrepare) {
        if (isRollback(errorCode)) state = State.ROLLEDBACK;
        else state = State.RB_ONLY;
        fail("prepare");
      }
      return sts;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      super.commit(xid, onePhase);
      if (failCommit) {
        if (isHeuristic(errorCode)) state = State.HEUR;
        if (isRollback(errorCode)) state = State.ROLLEDBACK;
        fail("commit");
      }
    }

    public void rollback(Xid xid) throws XAException {
      super.rollback(xid);
      if (failRollback) {
        if (isHeuristic(errorCode)) state = State.HEUR;
        fail("rollback");
      }
    }

    private void fail(String op) throws XAException {
      if (errorCode == XAException.XAER_RMFAIL || errorCode == XAException.XAER_NOTA) {
        state = State.ROLLEDBACK;
      }
      throw (errorCode != 0) ? new XAException(errorCode) : new XAException("Testing " + op + " failure");
    }

    public void reset() {
      super.reset();
      failStartAfter = Integer.MAX_VALUE;
      failSuspendAfter = Integer.MAX_VALUE;
      failResumeAfter = Integer.MAX_VALUE;
      failEndAfter = Integer.MAX_VALUE;
      errorCode = 0;
      failPrepare = false;
      failCommit = false;
      failRollback = false;
    }
  }

  /**
   * Basic Xid implementation.
   */
  private static class TestXid implements Xid {
    private int xid;
    public TestXid(int xid) {
      this.xid = xid;
    }

    public int getFormatId() {
      return 'X';
    }

    public byte[] getBranchQualifier() {
      return new byte[] {
        (byte)(xid >> 0x00),
        (byte)(xid >> 0x08)
      };
    }

    public byte[] getGlobalTransactionId() {
      return new byte[] {
        (byte)(xid >> 0x10),
        (byte)(xid >> 0x18)
      };
    }
  }

  /**
   * Just a dummy - nothing is ever called.
   */
  private static class DummyResolverFactory implements ResolverFactory {
    public void close() { }
    public void delete() { }
    public Graph[] getDefaultGraphs() { return null; }
    public boolean supportsExport() { return true; }
    public Resolver newResolver(boolean canWrite, ResolverSession resolverSession, Resolver systemResolver) { return null; }
  }

  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
