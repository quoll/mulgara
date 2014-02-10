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

package org.mulgara.util;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;
import org.mulgara.config.MulgaraConfig;
import org.neilja.net.interruptiblermi.InterruptibleRMISocketFactory;

/**
 * A utility to centralize the port handling for RMI objects.
 * This class is not set to handle different protocols. If this is needed, then the
 * super constructor for {@link UnicastRemoteObject} with socket factories
 * would need to be overridden.
 *
 * @created Sep 23, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Rmi extends UnicastRemoteObject {

  /** Logger */
  private final static Logger logger = Logger.getLogger(Rmi.class.getName());

  /** Generation UID */
  private static final long serialVersionUID = -8087526398171872888L;

  /** Java system property used to set the default RMI client object port. */
  public static final String CLIENT_OBJECT_PORT = "mulgara.rmi.objectPort";
  
  /** Java system property used to enable/disable the use of interruptible RMI sessions. */
  public static final String INTERRUPT = "mulgara.rmi.interrupt";
  
  /** The default port used for exporting objects. */
  protected static int defaultPort = 0;
  
  /**
   * Flag used to configure the enabling of interruptible RMI sessions by default.
   * Initially set from the value of the {@link #INTERRUPT} system property, if present.
   */
  protected static Boolean defaultInterrupt = null;
  
  // Check if a system property has been set for the default port or interruptible RMI.
  static {
    String val = System.getProperty(CLIENT_OBJECT_PORT);
    if (val != null) {
      try {
        defaultPort = Integer.parseInt(val);
      } catch (NumberFormatException e) {
        logger.warn("Unable to parse the client peer port for RMI: " + val);
      }
    }
    val = System.getProperty(INTERRUPT);
    if (val != null) {
      defaultInterrupt = Boolean.valueOf(val);
    }
  }


  /**
   * Default constructor. Uses the default port.
   * @throws RemoteException If the object could not be exported.
   */
  protected Rmi() throws RemoteException {
    super(defaultPort);
  }


  /**
   * Constructor with a specified port.
   * @param port A specified port. If 0 then a default port will be used.
   * @throws RemoteException If the object could not be exported.
   */
  protected Rmi(int port) throws RemoteException {
    super(port == 0 ? defaultPort : port);
  }


  /**
   * We don't want users using this method, since we cannot control the port, and we cannot
   * control the return type if a port is specified through another method.
   * @param obj The object to export.
   * @return Not implemented.
   * @throws RemoteException There was an error exporting the object.
   */
  public static RemoteStub exportObject(Remote obj) throws RemoteException {
    throw new UnsupportedOperationException("Use the export() method instead.");
  }


  /**
   * Exports an object through RMI, using a known port if configured, or a random port otherwise.
   * This will not create a default exporter if one does not exist.
   * @param obj The object to export.
   * @return An exported object.
   * @throws RemoteException There was an error exporting the object.
   */
  public static Remote export(Remote obj) throws RemoteException {
    return export(obj, false);
  }
  
  
  /**
   * Exports an object through RMI, enabling interruptible operations on the exported
   * object if specified. If a known port is configured then it will be used to export the
   * object, otherwise a random anonymous port will be chosen.
   * @param obj The object to export.
   * @param interruptible <tt>true</tt> to enable interruptible RMI operations on the exported object.
   * @return An exported object.
   * @throws RemoteException There was an error exporting the object.
   */
  public static Remote export(Remote obj, boolean interruptible) throws RemoteException {
    if (interruptible) {
      InterruptibleRMISocketFactory sf = new InterruptibleRMISocketFactory();
      return UnicastRemoteObject.exportObject(obj, defaultPort, sf, sf);
    }
    if (defaultPort == 0) return UnicastRemoteObject.exportObject(obj);
    return UnicastRemoteObject.exportObject(obj, defaultPort);
  }


  /**
   * Unexport an object from RMI.
   * @param obj The object to unexport.
   * @return <code>false</code> if the object could not be unexported. This may happen if it is
   *         still in use.
   * @throws RemoteException There was an error exporting the object.
   */
  public static boolean unexportObject(Remote obj) throws RemoteException {
    return UnicastRemoteObject.unexportObject(obj, false);
  }

  
  /**
   * Sets the default system behavior for enabling/disabling interruptible RMI operations,
   * based on the <tt>RMIInterrupt</tt> property from the specified Mulgara XML config file.
   * If the default behavior has already been set by the {@link #INTERRUPT} system property,
   * then this method has no effect.
   * @param config A Mulgara XML configuration.
   */
  public static void configure(MulgaraConfig config) {
    if (defaultInterrupt == null && config.hasRMIInterrupt()) {
      defaultInterrupt = Boolean.valueOf(config.getRMIInterrupt());
    }
  }
  

  /**
   * Sets the port for the default exporter to use. 
   * @param port The port number to use.
   */
  public static void setDefaultPort(int port) {
    defaultPort = port;
  }


  /**
   * Gets the port to use. 
   * @return The port number, or 0 if a random port is to be used.
   */
  public static int getDefaultPort() {
    return defaultPort;
  }

  
  /**
   * Manually override the configured system behavior for interruptible RMI operations.
   * @param interrupt <tt>true</tt> to enable interruptible RMI sessions where appropriate.
   */
  public static void setDefaultInterrupt(boolean interrupt) {
    defaultInterrupt = Boolean.valueOf(interrupt);
  }
  
  
  /**
   * Get the configured system default enabled status of interruptible RMI operations.
   * The order of precedence is as follows:
   * <ol>
   *   <li>If the default has been set in code via the {@link #setDefaultInterrupt(boolean)}
   *       method, return that value.</li>
   *   <li>The value of the {@link #INTERRUPT} Java system property, if present.</li>
   *   <li>The value set by the Mulgara XML config file via the {@link #configure(MulgaraConfig)}
   *       method, if applicable.</li>
   *   <li>If the default has not been configured by any of the above means, return <tt>false</tt>.</li>
   * </ol>
   * @return <tt>true</tt> if interruptible RMI operations should be enabled by default.
   */
  public static boolean getDefaultInterrupt() {
    if (defaultInterrupt != null) return defaultInterrupt.booleanValue();
    return false;
  }
  
  
  /**
   * Determines whether the current thread is an RMI server thread that has been
   * interrupted by the client. This method only gives useful results if the remote
   * object in question was exported using the {@link #export(Remote, boolean)} method
   * with the <tt>interruptible</tt> parameter set to <tt>true</tt>.
   * @return <tt>true</tt> if the current thread is an RMI server thread, and the underlying
   *         socket has been closed by the client.
   */
  public static boolean isInterrupted() {
    return InterruptibleRMISocketFactory.isCurrentThreadRMIServer() &&
        !InterruptibleRMISocketFactory.isCurrentRMIServerThreadSocketAlive();
  }
  

  /**
   * Unexport this object from RMI.
   * @return <code>false</code> if the object could not be unexported. This may happen if it is
   *         still in use.
   * @throws RemoteException There was an error exporting the object.
   */
  public boolean unexport() throws RemoteException {
    return unexportObject(this);
  }


  /**
   * Unexport an object from RMI.
   * @param force If true, then unexport the object, even if it still in use.
   * @return <code>false</code> if the object could not be unexported. This may happen if
   *         <var>force</var> is <code>false</code> and <code>obj</code> is still in use.
   * @throws RemoteException There was an error exporting the object.
   */
  public boolean unexport(boolean force) throws RemoteException {
    return unexportObject(this, force);
  }

}
