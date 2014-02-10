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

package org.mulgara.server;

// Java 2 standard packages
import java.io.File;
import java.net.URI;

import org.mulgara.config.MulgaraConfig;

/**
 * Management interface for a {@link org.mulgara.server.SessionFactory}.
 * Describes the MBean services.
 *
 * @created 2001-09-15
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:58:59 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface ServerMBean {

  //
  // MBean states
  //

  public enum ServerState {
    /**
     * Value of the <var>state</var> MBean property indicating that the server has
     * not been created. This is the initial state when the MBean is created, and
     * is also the result of calling the {@link #destroy} action from the {@link
     * #STOPPED} state. The {@link #init} action can be used to transition from
     * this state to the {@link #STOPPED} state.
     */
    UNINITIALIZED,

    /**
     * Value of the <var>state</var> MBean property indicating that the server has
     * been created, but is not accepting client requests from the network. This
     * state is entered from the {@link #UNINITIALIZED} state via the {@link
     * #init} action. The {@link #start} action can be used to transition from
     * this state to the {@link #STARTED} state.
     */
    STOPPED,

    /**
     * Value of the <var>state</var> MBean property indicating that the server is
     * accepting client requests from the network. This state is entered from the
     * {@link #STOPPED} state via the {@link #start} action. The {@link #stop}
     * action can be used to transition from this state to the {@link #STOPPED}
     * state.
     */
    STARTED
  };

  //
  // MBean actions
  //

  /**
   * Initialize the server.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void init() throws Exception;

  /**
   * Start the server.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void start() throws Exception;

  /**
   * Stop the server.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void stop() throws Exception;

  /**
   * Destroy the server.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void destroy() throws Exception;

  //
  // MBean properties
  //

  /**
   * Read the server state. This property has no setter method. It's modified
   * using the {@link #init}, {@link #start}, {@link #stop} and {@link #destroy}
   * methods.
   *
   * @return The current server state, always one of the values
   *   {@link ServerState#UNINITIALIZED}, {@link ServerState#STOPPED} or {@link ServerState#STARTED}
   */
  public ServerState getState();

  /**
   * Read the database persistence directory.
   *
   * @return The Dir value
   */
  public File getDir();

  /**
   * Set the database persistence directory.
   *
   * @param dir The new Dir value
   */
  public void setDir(File dir);

  /**
   * Returns the hostname of the server.
   *
   * @return The Hostname value
   */
  public String getHostname();

  /**
   * Sets the hostname of the server.
   *
   * @param hostname The new Hostname value
   */
  public void setHostname(String hostname);

  /**
   * Sets the port of the server to bind to.
   *
   * @param newPortNumber the port number value.
   */
  public void setPortNumber(int newPortNumber);

  /**
   * Returns the port number that the server is to bind to.
   *
   * @return the port number value.
   */
  public int getPortNumber();

  /**
   * Read the database provider classname.
   *
   * @return The ProviderClassName value
   */
  public String getProviderClassName();

  /**
   * Set the database provider classname.
   *
   * @param providerClassName The new ProviderClassName value
   */
  public void setProviderClassName(String providerClassName);

  /**
   * Read the server URI.
   *
   * @return The URI value
   */
  public URI getURI();

  /**
   * Read the temporary database persistence directory.
   *
   * @return The Dir value
   */
   public File getTempDir();

  /**
   * Set the temporary database persistence directory.
   *
   * @param tempdir The new tempdir value
   */
   public void setTempDir(File tempdir);

   /**
    * Retrieves the configuration used when initialising a session factory.
    *
    * @return The configuration used when initialising a session factory
    */
   public MulgaraConfig getConfig();

   /**
    * Sets the configuration to be used when bringing up a session factory.
    *
    * @param config The configuration to be used when bringing up a session
    *               factory
    */
   public void setConfig(MulgaraConfig config);
}
