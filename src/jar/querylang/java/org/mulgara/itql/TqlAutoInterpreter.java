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
package org.mulgara.itql;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.connection.ConnectionException;
import org.mulgara.connection.ConnectionFactory;
import org.mulgara.connection.DummyConnection;
import org.mulgara.parser.Interpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.operation.Command;
import org.mulgara.query.operation.Commit;
import org.mulgara.query.operation.LocalCommand;
import org.mulgara.query.operation.Rollback;
import org.mulgara.query.operation.SetAutoCommit;
import org.mulgara.query.operation.TxOp;
import org.mulgara.server.Session;

/**
 * This class interprets TQL statements, and automatically executes them,
 * establishing connections to servers when required.
 * 
 * @created Sep 11, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TqlAutoInterpreter {
  /** The logger. */
  private final static Logger logger = Logger.getLogger(TqlAutoInterpreter.class.getName());

  /** A connection for receiving state changes to the local machine. */
  private static Connection localStateConnection = new DummyConnection();

  /** The parser and AST builder for commands. */
  private Interpreter interpreter = new TqlInterpreter();

  /** A user readable message resulting from the most recent command. */
  private String lastMessage;

  /** The most answer returned from the most recent command, if it was a query. */
  private Answer lastAnswer;

  /** The most recent exception, if there was one. */
  private Exception lastException;

  /** Factory for building and caching connections. */
  private ConnectionFactory connectionFactory = new ConnectionFactory();

  /** Indicates that the client is in a transaction. */
  private boolean inTransaction;

  /** All the connections involved in the current transaction. */
  private Map<URI,Connection> transConnections = new HashMap<URI,Connection>();
  
  /**
   * Holds the client security domain. Need to connect this to URIs,
   * but the old interfaces don't know how to do this.
   * <em>Security is currently unimplemented.</em>
   */
  private URI securityDomain = null;

  /**
   * Creates a new autointerpreter with no prior connections.
   */
  public TqlAutoInterpreter() {
    inTransaction = false;
    resetState();
  }
  

  /**
   * Execute a query.  The results of the query will set the state of this object.
   * @param command The string containing the query to execute.
   * @return <code>false</code> if the command asks to exit, <code>true</code> to continue normal operation.
   */
  public boolean executeCommand(String command) {
    resetState();

    if (logger.isDebugEnabled()) logger.debug("Parsing the command: " + command);
    Command cmd = null;
    try {
      cmd = interpreter.parseCommand(command);
    } catch (Exception e) {
      lastMessage = "Error parsing the query";
      lastException = e;
      return true;
    }
    if (cmd == null) {
      lastMessage = null;
      return true;
    }

    // execute the operation
    try {
      // set up a connection, if required
      Connection conn = establishConnection(cmd);
      handleResult(cmd.execute(conn), cmd);
      updateConnectionsForTx(conn, cmd);
      lastMessage = cmd.getResultMessage();
    } catch (Exception e) {
      lastException = e;
      lastMessage = "Error: " + e.getMessage();
    }

    assert lastMessage != null;

    // test if the command wants the user to quit - return false if it does
    return !(cmd.isLocalOperation() && ((LocalCommand)cmd).isQuitCommand());
  }
  
  
  /**
   * Sets the security domain for the client.
   * <em>Security is currently unimplemented.</em>
   * @param domain The URI of the service which authenticates the client.
   *               e.g. ldap://ldap.domain.net/o=mycompany
   */
  public void setSecurityDomain(URI domain) {
    securityDomain = domain;
  }

  
  /**
   * Query for the currently used security domain.
   * <em>Security is currently unimplemented.</em>
   */
  public URI getSecurityDomain() {
    return securityDomain;
  }


  /** @return the message set from the last operation */
  public String getLastMessage() { return lastMessage; }

  /** @return the last answer returned from a query, or <code>null</code> if the last operation was not a query */
  public Answer getLastAnswer() { return lastAnswer; }

  /** @return the exception thrown from the last operation, or <code>null</code> if there was no error */
  public Exception getLastException() { return lastException; }


  /**
   * Close any resources that are still in use, and rolls back any outstanding transactions.
   */
  public void close() {
    if (inTransaction) {
      logger.info("Closing a current transaction.  Rolling back.");
      try {
        handleTxOp(new SetAutoCommit(true));
      } catch (QueryException e) {
        logger.error("Error while cleaning up a transaction", e);
      }
    }
    assert transConnections.isEmpty();
    connectionFactory.closeAll();
  }


  /**
   * Resets the internal state in preparation for a new operation to be executed.
   */
  private void resetState() {
    lastMessage = null;
    lastAnswer = null;
    lastException = null;
  }


  /**
   * Process the result from a command.
   * @param result The result to handle.
   * @param cmd The command that gave the result. Used for type checking.
   */
  private void handleResult(Object result, Command cmd) {
    if (result != null) {
      if (cmd.isAnswerable()) lastAnswer = (Answer)result;
      else logger.debug("Result: " + result);
    }
  }


  /**
   * Returns a connection to a server for a given command.
   * @param cmd The command to get a connection to execute on.
   * @return A connection to the server, cached if available.
   * @throws ConnectionException It was not possible to create a connection to the described server.
   * @throws QueryException There is a transaction underway, but the new connection cannot turn off autocommit.
   */
  private Connection establishConnection(Command cmd) throws ConnectionException, QueryException {
    URI serverUri = cmd.getServerURI();

    // check for server operations where we don't know the server
    if (serverUri == null && !cmd.isLocalOperation()) {
      // no server URI, but not local. Get a connection for a null URI
      // eg. select .... from <file:///...>
      Connection connection = transConnections.get(serverUri);
      if (connection == null) {
        connection = connectionFactory.newConnection(serverUri);
        configureForTransaction(serverUri, connection);
      }
      return connection;
    }
    
    // go the normal route for getting a connection for a given server location
    return establishConnection(serverUri);
  }


  /**
   * Returns a connection to the server with the given URI.
   * NB: Not for general use. Available to ItqlInterpreterBean only to support
   * legacy requests to get a session.
   * @param serverUri The URI for the server to get a connection to. <code>null</code> for
   *        Local operations that do not require a server.
   * @return A connection to the server, cached if available.
   * @throws ConnectionException It was not possible to create a connection to the described server.
   * @throws QueryException There is a transaction underway, but the new connection cannot turn off autocommit.
   */
  Connection establishConnection(URI serverUri) throws ConnectionException, QueryException {
    // get a new connection, or use the local one for non-server operations
    Connection connection = null;
    if (serverUri == null) {
      connection = localStateConnection;
    } else {
      serverUri = ConnectionFactory.normalizeLocalUri(serverUri);
      connection = transConnections.get(serverUri);
      if (connection == null) {
        connection = connectionFactory.newConnection(serverUri);
        // update the connection if it needs to enter a current transaction
        configureForTransaction(serverUri, connection);
      }
    }
    return connection;
  }


  /**
   * Set up the given connection for a current transaction, if one is active at the moment.
   * @param connection The connection to configure. The dummy connection is not configured.
   * @throws QueryException An error while setting up the connection for the transaction.
   */
  private void configureForTransaction(URI serverUri, Connection connection) throws QueryException {
    // If in a transaction, turn off autocommit - ignore the dummy connection
    if (inTransaction && connection.getAutoCommit() && connection != localStateConnection) {
      assert !(connection instanceof DummyConnection);
      connection.setAutoCommit(false);
      assert !transConnections.containsValue(connection);
      transConnections.put(serverUri, connection);
    }
  }


  /**
   * Returns the current alias map.  Needs to treat the internal interpreter
   * explicitly as a TqlInterpreter.
   * @deprecated Available to ItqlInterpreterBean only to support legacy requests.
   * @return The mapping of namespaces to the URI for that space.
   */
  Map<String,URI> getAliasesInUse() {
    return ((TqlInterpreter)interpreter).getAliasMap();
  }
  
  
  /**
   * Sets the current alias map.  Needs to treat the internal interpreter
   * explicitly as a TqlInterpreter.
   * @deprecated Available to ItqlInterpreterBean only to support legacy requests.
   */
  void setAliasesInUse(Map<String,URI> map) {
    ((TqlInterpreter)interpreter).setAliasMap(map);
  }
  
  
  /**
   * Clears the last exception.
   * @deprecated Available to ItqlInterpreterBean only to support legacy requests.
   */
  void clearLastException() {
    lastException = null;
  }


  /**
   * Returns the internal local connection.  Supports local operations for the current package.
   * @return The local "state" connection.
   */
  Connection getLocalConnection() {
    return localStateConnection;
  }


  /**
   * Commits all connections that started on a transaction.  This operates directly
   * on all known transacted connections.
   * @throws QueryException One of the connections could not be successfully committed.
   */
  void commitAll() throws QueryException {
    handleTxOp(new Commit());
  }

  /**
   * Rolls back all connections that started on a transaction.  This oeprates directly
   * on all known transacted connections.
   * @throws QueryException One of the connections could not be successfully rolled back.
   */
  void rollbackAll() throws QueryException {
    handleTxOp(new Rollback());
  }


  /**
   * Seeds the cache with a connection wrapping the given session.
   * @deprecated Only for use by {@link ItqlInterpreterBean}.
   * @param session The session to seed into the connection cache.
   */
  void preSeedSession(Session session) {
    // get back a new connection, and then drop it, since it will now be cached
    try {
      connectionFactory.newConnection(session);
    } catch (ConnectionException e) {
      logger.warn("Unable to use the given session for establishing a connection", e);
    }
  }


  /**
   * Perform any actions required on the update of the state of a connection.
   * Most commands will skip through this method. Only transaction commands do anything.
   * @param conn The connection whose state needs checking.
   * @throws QueryException Can be caused by a failed change into a transaction.
   */
  void updateConnectionsForTx(Connection conn, Command cmd) throws QueryException {
    // check if the transaction state changed on a setAutocommit operations, or if the command is a Tx operation
    if (inTransaction == conn.getAutoCommit() || cmd.isTxCommitRollback()) {
      // check that transaction changes came from setAutoCommit commands
      assert inTransaction != conn.getAutoCommit() ||
             cmd instanceof org.mulgara.query.operation.SetAutoCommit ||
             cmd instanceof org.mulgara.query.operation.Commit ||
             cmd instanceof org.mulgara.query.operation.Rollback :
             "Got a state change on " + cmd.getClass() + " instead of SetAutoCommit/Commit/Rollback";
      // check that if we are starting a transaction then the transConnections list is empty
      assert inTransaction != conn.getAutoCommit() || conn.getAutoCommit() || transConnections.isEmpty();
      // save the number of active connections
      int activeConnections = transConnections.size();
      // handle the transaction operation
      handleTxOp(cmd);
      // check that if we have left a transaction, then the connection list is empty
      assert inTransaction || transConnections.isEmpty();
      // check that if we are still in a transaction, then the connection list has not changed
      assert !inTransaction || activeConnections == transConnections.size();
    }
  }


  /**
   * This method wraps the simple loop of applying a command to all transaction connections.
   * The wrapping is done to attempt the operation on all connections, despite exceptions
   * being thrown.
   * @param op The operation to end the transaction.
   * @throws QueryException The operation could not be successfully performed.
   */
  private void handleTxOp(Command op) throws QueryException {
    // used to record the first exception, if there is one.
    QueryException qe = null;
    String errorMessage = null;

    // Operate on all outstanding transactions.
    Iterator<Connection> c = transConnections.values().iterator();
    while (c.hasNext()) {
      try {
        // do the work
        op.execute(c.next());
      } catch (QueryException e) {
        // store the details of the first exception only
        if (qe != null) logger.error("Discarding subsequent exception during operation: " + op.getClass().getSimpleName(), e);
        else {
          qe = e;
          errorMessage = op.getResultMessage();
        }
      } catch (Exception e) {
        throw new QueryException("Unexpected exception during operation: " + op, e);
      }
    }
    // will only get here once all connections were processed.
    if (op instanceof TxOp) {
      inTransaction = ((TxOp)op).stayInTx();
      if (!inTransaction) transConnections.clear();
    }

    // if an exception was recorded, then throw it
    if (qe != null) {
      // remember the error message associated with the exception
      op.setResultMessage(errorMessage);
      throw qe;
    }
  }

}
