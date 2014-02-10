/*
 * Copyright 2009 Revelytix.
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

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mulgara.query.Answer;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.SelectElement;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.server.Session;
import org.mulgara.server.driver.SessionFactoryFinder;

/**
 * Test case for {@link ConnectionFactory}
 * @created 2008-05-20
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
@SuppressWarnings("deprecation") // Tests use the deprecated Connection.getSession() method.
public class ConnectionFactoryUnitTest extends TestCase {
  
  /** Factory under testing */
  private ConnectionFactory factory;
  /** Server that we will connect to */
  private URI serverURI = URI.create("rmi://localhost/server1");
  /** Simple query to execute on a Connection */
  private Query query;
  
  public ConnectionFactoryUnitTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new ConnectionFactoryUnitTest("testConnection"));
    suite.addTest(new ConnectionFactoryUnitTest("testCloseConnection"));
    suite.addTest(new ConnectionFactoryUnitTest("testCacheSession"));
    suite.addTest(new ConnectionFactoryUnitTest("testMultipleConnections"));
    suite.addTest(new ConnectionFactoryUnitTest("testDisposeConnection"));
    suite.addTest(new ConnectionFactoryUnitTest("testCloseAll"));
    suite.addTest(new ConnectionFactoryUnitTest("testLocalAliasing"));
    suite.addTest(new ConnectionFactoryUnitTest("testSessionConnection"));
    suite.addTest(new ConnectionFactoryUnitTest("testValidSession"));
    return suite;
  }
  
  protected void setUp() throws Exception {
    factory = new ConnectionFactory();
    
    // Set up a simple query to execute on the connection
    Variable subjectVariable   = new Variable("subject");
    Variable predicateVariable = new Variable("predicate");
    Variable objectVariable    = new Variable("object");

    List<SelectElement> selectList = new ArrayList<SelectElement>(3);
    selectList.add(subjectVariable);
    selectList.add(predicateVariable);
    selectList.add(objectVariable);

    query = new Query(
      selectList,                                       // SELECT
      new GraphResource(URI.create("rmi://localhost/server1#")), // FROM
      new ConstraintImpl(subjectVariable,               // WHERE
                     predicateVariable,
                     objectVariable),
      null,                                             // HAVING
      Arrays.asList(new Order[] {                       // ORDER BY
        new Order(subjectVariable, true),
        new Order(predicateVariable, true),
        new Order(objectVariable, true)
      }),
      null,                                             // LIMIT
      0,                                                // OFFSET
      true,                                             // DISTINCT
      new UnconstrainedAnswer()                         // GIVEN
    );
  }
  
  protected void tearDown() {
    factory.closeAll();
  }
  
  public void testConnection() throws Exception {
    // Execute a simple query to ensure no exceptions.
    Connection conn = factory.newConnection(serverURI);
    conn.execute(query);
  }
  
  private void checkValidConnection(Connection c) {
    try {
      Answer answer = c.execute(query);
      answer.close();
    } catch (Exception e) {
      fail("Error executing query on connection that should be valid: " + e.getMessage());
    }
  }
  
  public void testCloseConnection() throws Exception {
    Connection conn1 = factory.newConnection(serverURI);
    Session session1 = conn1.getSession();
    conn1.close();
    
    try {
      conn1.execute(query);
      fail("Access to closed Connection should have thrown IllegalStateException");
    } catch (IllegalStateException ise) {
      // Do nothing -- expected failure.
    }
    
    // Should still be able to query on the underlying session.
    Answer answer = session1.query(query);
    answer.close();
    
    Connection conn2 = factory.newConnection(serverURI);
    checkValidConnection(conn2);
    conn2.close();
  }
  
  public void testCacheSession() throws Exception {
    Connection conn1 = factory.newConnection(serverURI);
    checkValidConnection(conn1);
    Session session1 = conn1.getSession();
    conn1.close();
    
    Connection conn2 = factory.newConnection(serverURI);
    checkValidConnection(conn2);
    Session session2 = conn2.getSession();
    
    Connection conn3 = factory.newConnection(serverURI);
    checkValidConnection(conn3);
    Session session3 = conn3.getSession();
    
    conn2.close();
    Connection conn4 = factory.newConnection(serverURI);
    checkValidConnection(conn4);
    Session session4 = conn4.getSession();
    
    conn3.close();
    conn4.close();
    
    assertTrue("Session should be re-used between connections", session1 == session2);
    assertTrue("Session should be re-used between connections", session1 == session4);
    assertFalse("Session should not be shared between active connections", session1 == session3);
  }
  
  public void testMultipleConnections() throws Exception {
    Set<Session> firstSessions = new HashSet<Session>();
    Set<Session> secondSessions = new HashSet<Session>();
    
    Connection conn1 = factory.newConnection(serverURI);
    Connection conn2 = factory.newConnection(serverURI);
    
    checkValidConnection(conn1);
    checkValidConnection(conn2);
    
    firstSessions.add(conn1.getSession());
    firstSessions.add(conn2.getSession());
    
    conn1.close();
    conn2.close();
    
    Connection conn3 = factory.newConnection(serverURI);
    Connection conn4 = factory.newConnection(serverURI);
    
    checkValidConnection(conn3);
    checkValidConnection(conn4);
    
    secondSessions.add(conn3.getSession());
    secondSessions.add(conn4.getSession());
    
    conn3.close();
    conn4.close();
    
    assertEquals(2, firstSessions.size());
    assertEquals(2, secondSessions.size());
    assertEquals(firstSessions, secondSessions);
  }
  
  public void testDisposeConnection() throws Exception {
    Connection conn1 = factory.newConnection(serverURI);
    checkValidConnection(conn1);
    Session session1 = conn1.getSession();
    conn1.dispose();
    
    try {
      session1.query(query);
      fail("Session for disposed connection should be closed");
    } catch (Exception e) {
      // Do nothing -- expected failure.
    }
    
    Connection conn2 = factory.newConnection(serverURI);
    checkValidConnection(conn2);
    Session session2 = conn2.getSession();
    conn2.close();
    
    assertFalse(session1 == session2);
  }
  
  public void testCloseAll() throws Exception {
    Connection conn1 = factory.newConnection(serverURI);
    checkValidConnection(conn1);
    
    Session session1 = conn1.getSession();
    conn1.close();
    factory.closeAll();
    
    try {
      session1.query(query);
      fail("Session should be closed after ConnectionFactory.closeAll()");
    } catch (Exception e) {
      // Do nothing -- expected failure.
    }
    
    Connection conn2 = factory.newConnection(serverURI);
    Session session2 = conn2.getSession();
    
    factory.closeAll();
    try {
      conn2.execute(query);
      fail("Session should be closed after ConnectionFactory.closeAll()");
    } catch (Exception e) {
      // Do nothing -- expected failure
    }
    
    conn2.close();
    
    Connection conn3 = factory.newConnection(serverURI);
    checkValidConnection(conn3);
    conn3.close();
    
    assertFalse(session1 == session2);    
  }
  
  public void testLocalAliasing() throws Exception {
    String localhost = InetAddress.getLocalHost().getHostName();
    if ("localhost".equals(localhost)) {
      localhost = "127.0.0.1";
    }
    URI localhostUri = URI.create("rmi://" + localhost + "/server1");
    
    Connection conn1 = factory.newConnection(serverURI);
    checkValidConnection(conn1);
    Session session1 = conn1.getSession();
    conn1.close();
    
    Connection conn2 = factory.newConnection(localhostUri);
    checkValidConnection(conn2);
    Session session2 = conn2.getSession();
    conn2.close();
    
    assertTrue("Aliases to localhost should have mapped to same session", session1 == session2);
  }
  
  public void testSessionConnection() throws Exception {
    Session session1 = SessionFactoryFinder.newSessionFactory(serverURI, true).newSession();
    Connection conn1 = factory.newConnection(session1);
    checkValidConnection(conn1);
    conn1.close();
    
    factory.closeAll();
    Answer answer = session1.query(query);
    answer.close();
    
    Connection conn2 = factory.newConnection(serverURI);
    checkValidConnection(conn2);
    Session session2 = conn2.getSession();
    conn2.close();
    
    // Session passed to ConnectionFactory.newConnection(Session) should not be cached by factory
    assertFalse(session1 == session2);
  }
  
  public void testValidSession() throws Exception {
    Connection conn1 = factory.newConnection(serverURI);
    checkValidConnection(conn1);
    Session session = conn1.getSession();
    conn1.close();
    session.close();
    
    // The session was manually closed after being cached; the factory should pick up on this.
    Connection conn2 = factory.newConnection(serverURI);
    checkValidConnection(conn2);
  }
}
