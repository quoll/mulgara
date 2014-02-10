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

package org.mulgara.server.rmi;


// Java 2 standard packages
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.mulgara.config.MulgaraConfig;
import org.mulgara.server.AbstractServer;
import org.mulgara.util.Rmi;

/**
 * Java RMI server.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @created 2002-01-12
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 * @see <a href="http://developer.java.sun.com/developer/JDCTechTips/2001/tt0327.html#jndi"/>
 *      <cite>JNDI lookup in distributed systems</cite> </a>
 */
public class RmiServer extends AbstractServer implements RmiServerMBean {

  /** Logger. This is named after the classname. */
  private final static Logger logger = Logger.getLogger(RmiServer.class.getName());

  /** The name of the URL protocol handler package property. */
  private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";

  // Initialize the system to know about RMI URLs
  static {
    String handler = System.getProperty(PROTOCOL_HANDLER);
    String thisPackage = RmiServer.class.getPackage().getName();
    assert thisPackage.endsWith(".rmi");
    String parentPackage = thisPackage.substring(0, thisPackage.lastIndexOf('.'));
    System.setProperty(PROTOCOL_HANDLER, (handler == null) ? parentPackage : handler + "|" + parentPackage);
  }

  /** The default port used for RMI. */
  public static final int DEFAULT_PORT = 1099;
  
  /** The Java RMI registry naming context. */
  private Context rmiRegistryContext;

  /** The RMI registry name of this server. */
  private String name;

  /**
   * The local copy of the RMI session factory. This reference must be held
   * because the garbage collector isn't aware of remote stubs on distant JVMs.
   */
  private RemoteSessionFactory remoteSessionFactory;

  /**
   * An RMI stub that proxies for {@link #remoteSessionFactory}. This instance
   * can be serialized and distributed to remote JVMs.
   */
  private RemoteSessionFactory exportedRemoteSessionFactory;

  /**
   * Set the name the server is bound to in the RMI registry. It's possible to
   * set <var>name</var> to <code>null</code>, but the
   * {@link org.mulgara.server.ServerMBean#start} action can't be used in that
   * case. The <var>name</var> cannot be set while the server is started.
   * @param name the new value
   * @throws IllegalStateException if the server is started or if the database already has a fixed URI
   */
  public void setName(String name) {
    // Prevent the name from being changed while the server is up
    if (this.getState() == ServerState.STARTED) {
      throw new IllegalStateException("Can't change name without first stopping the server");
    }

    // Set field
    this.name = name;
    updateURI();
  }

  /**
   * Sets the hostname of the server.
   *
   * @param hostname the hostname of the server, if <code>null</code> <code>localhost</code> will be used
   * @throws IllegalStateException if the service is started or if the underlying session factory already has a fixed hostname
   */
  public void setHostname(String hostname) {
    // Prevent the hostname from being changed while the server is up
    if (this.getState() == ServerState.STARTED) {
      throw new IllegalStateException("Can't change hostname without first stopping the server");
    }

    // Reset the field
    if (hostname == null) {
      this.hostname = "localhost";
      logger.warn("Hostname supplied is null, defaulting to localhost");
    } else {
      this.hostname = hostname;
    }

    updateURI();
  }

  /**
   * Sets the server port.
   * @param newPortNumber The new port to bind to.
   */
  public void setPortNumber(int newPortNumber) {
    super.setPortNumber(newPortNumber);
    updateURI();
  }

  //
  // MBean properties
  //

  /**
   * Read the name the server is bound to in the RMI registry.
   * @return The bound name of the server.
   */
  public String getName() {
    return name;
  }

  //
  // Methods implementing AbstractServer
  //

  /* (non-Javadoc)
   * @see org.mulgara.server.AbstractServer#setConfig(org.mulgara.config.MulgaraConfig)
   */
  @Override
  public void setConfig(MulgaraConfig config) {
    super.setConfig(config);
    Rmi.configure(config);
  }

  /**
   * Start the server.
   * @throws IllegalStateException if <var>name</var> is <code>null</code>
   * @throws NamingException Error accessing RMI registry.
   * @throws RemoteException Error accessing RMI services.
   */
  protected void startService() throws NamingException, RemoteException {

    // Validate "name" property
    if (name == null) throw new IllegalStateException("Must set \"name\" property");

    // Initialize fields
    rmiRegistryContext = new InitialContext();

    remoteSessionFactory = new RemoteSessionFactoryImpl(getSessionFactory(), Rmi.getDefaultInterrupt());
    exportedRemoteSessionFactory = (RemoteSessionFactory)Rmi.export(remoteSessionFactory);

    // Bind the service to the RMI registry
    rmiRegistryContext.rebind(name, exportedRemoteSessionFactory);
  }

  /**
   * Stop the server.
   * @throws NamingException Error accessing the registry.
   * @throws NoSuchObjectException The current server is not registered in the registry.
   */
  protected void stopService() throws NamingException, NoSuchObjectException {
    try {
      rmiRegistryContext.unbind(name);
      Rmi.unexportObject(remoteSessionFactory, true);
    } catch (Exception e) {
      if (e.getCause() instanceof javax.naming.ServiceUnavailableException) {
        logger.warn("RMI Server no longer available to be stopped. Abandoning.");
      } else {
        logger.warn("Unabled to deregister the RMI service. Abandoning.");
      }
    }
  }

  /**
   * Creates a new URI for the current hostname/servicename/port and sets this service
   * to register with that name.
   * @throws Error if the hostname or service name cannot be encoded in a URI.
   */
  private void updateURI() {
    URI newURI = null;

    if (hostname == null) {
      // try to use the local host name
      try {
        hostname = InetAddress.getLocalHost().getCanonicalHostName();
      } catch (UnknownHostException e) {
        logger.warn("Problem getting host name! - using \"localhost\"");
        hostname = "localhost";
      }
    }

    // Generate new server URI
    try {
      String path = "/" + (name == null ? "" : name);
      int portNr = getPortNumber();
      if (portNr == DEFAULT_PORT || portNr == -1) {
        newURI = new URI("rmi://" + hostname + path);
      } else {
        newURI = new URI("rmi://" + hostname + ":" + portNr + path);
      }
    } catch (URISyntaxException e) {
      throw new Error("Bad generated URI", e);
    }

    // Set URI.
    setURI(newURI);
  }
  
  /** @see org.mulgara.server.AbstractServer#getDefaultPort() */
  protected int getDefaultPort() {
    return DEFAULT_PORT;
  }
}
