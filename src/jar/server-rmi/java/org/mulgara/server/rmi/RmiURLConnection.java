/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.server.rmi;

import java.util.Hashtable;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.UnmarshalException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.mulgara.util.ClassDescriber;
import org.mulgara.util.ClassDescriberXML;

/**
 * This class represents a connection to an RMI server. Reading from this connection
 * results in an Object coming across the connection, or an XML string to describe
 * what could have come over if the RMI deserializer cannot find a local class to
 * instantiate the object.
 *
 * @created Mar 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class RmiURLConnection extends URLConnection {

  /** The context of the registry containing the RMI references. */
  private Context rmiRegistryContext = null;

  /** A string description of the object referenced by the URL. */
  private String description = null;

  /** An object representing what the URL refers to. If possible, this is a remote object, else a String. */
  private Object content = null;

  /** Contains the raw bytes representing the data that will be "read" from this connection. */
  private byte[] buffer = null;

  /**
   * Build a URLConnection to an RMI server.
   * @param url The URL to use for locating the service on the server.
   */
  protected RmiURLConnection(URL url) {
    super(url);
    setDefaultUseCaches(false);
    setDefaultAllowUserInteraction(false);
  }

  /**
   * This type of connection does not permit user interaction.
   * @see java.net.URLConnection#setAllowUserInteraction(boolean)
   */
  public void setAllowUserInteraction(boolean allow) {
    if (allow) throw new IllegalStateException("Not valid to interact with an rmi connection");
  }

  /**
   * Caching is not valid for this kind of connection.
   * @see java.net.URLConnection#setUseCaches(boolean)
   */
  public void setUseCaches(boolean use) {
    if (use) throw new IllegalStateException("Not valid to cache an rmi connection");
  }

  /**
   * Connects to the specified service, and gets what content it can.
   * @see java.net.URLConnection#connect()
   */
  public void connect() throws IOException {
    Hashtable<String,String> environment = new Hashtable<String,String>();
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
    environment.put(Context.PROVIDER_URL, url.getProtocol() + "://" + url.getAuthority());
    String name = url.getPath().substring(1);
    try {
      rmiRegistryContext = new InitialContext(environment);
      updateContent(name);
    } catch (NamingException ne) {
      throw new IOException("Unable to establish connection", ne);
    }
  }

  /**
   * Reads the number of bytes for the content that can be read from this connection.
   * @return The number of bytes that can be returned from {@link #getContent()}.
   * @see java.net.URLConnection#getContentLength()
   */
  public int getContentLength() {
    return (buffer == null) ? 0 : buffer.length;
  }

  /**
   * Gets the data representing the service connected to.
   * @return Either a remote object, accessed through RMI, or a String containing XML if
   *   that object cannot be instantiated.
   * @throws IOException If not connected.
   * @see java.net.URLConnection#getContent()
   */
  public Object getContent() throws IOException {
    if (content == null) throw new IOException("Not connected");
    return content;
  }

  /**
   * Gets the data representing the service connected to. Using a list of desired classes
   * to specify the desired return type. The class array will be searched for a match with
   * the following, with the first match being the returned type:
   * <ol><li>The class of the Remote object obtained through RMI.</li>
   * <li>{@link java.lang.String}: the result will contain an XML description of the object.</li>
   * <li>An array of <code>byte</code>: the result will contain a serialization of either
   *     the remote object or the XML description if the remote object cannot be instantiated.</li></ol>
   * Otherwise, a <code>null</code> is returned.
   * @return Either a remote object, accessed through RMI, or a String containing XML if
   *   that object cannot be instantiated.
   * @throws IOException If not connected.
   * @see java.net.URLConnection#getContent()
   */
  @SuppressWarnings("rawtypes")
  public Object getContent(Class[] cls) throws IOException {
    if (content == null) throw new IOException("Not connected");
    if (testType(content.getClass(), cls)) return content;
    if (testType(String.class, cls)) return description;
    if (testType(byte[].class, cls)) return buffer;
    return null;
  }

  /**
   * Gets an input stream to read bytes from this connection.
   * @return an input stream that will return the data of the content.
   * @throws IOException If not connected.
   * @see java.net.URLConnection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    if (buffer == null) throw new IOException("Not connected");
    return new ByteArrayInputStream(buffer);
  }

  /**
   * Utility to check if a class or one of its supertypes is present in an array.
   * @param clazz The class to check for.
   * @param cls The array of classes to test for the presence of clazz or one of its supertypes.
   * @return <code>true</code> if the class or one of its supertypes is present in cls.
   */
  private static boolean testType(Class<?> clazz, Class<?>[] cls) {
    for (Class<?> c: cls) if (c.isAssignableFrom(clazz)) return true;
    return false;
  }

  /**
   * Gets data from a server by name, and creates content at this side to represent it.
   * Binary content will be created if possible, but a string containing an XML representation
   * will always be created.
   * @param name The name of the RMI service to get from the server.
   * @throws NamingException Looking up the name on the server failed.
   * @throws IOException Transfering data from the server failed.
   */
  private void updateContent(String name) throws NamingException, IOException {
    if (name.length() == 0) {
      // server only
      description = getContextDescription(false);
      content = description;
      buffer = description.getBytes();
    } else {
      // service name. Get the object.
      try {
        Object content = rmiRegistryContext.lookup(name);
        description = getServiceDescription(content);
        buffer = (content instanceof Serializable) ? serialize((Serializable)content) : description.getBytes();
      } catch (NamingException ne) {
        Throwable e = ne.getCause();
        if (!(e instanceof UnmarshalException)) throw ne;
        if (e == null || !(e.getCause() instanceof ClassNotFoundException)) throw ne;
        description = getContextDescription(true);
        content = description;
        buffer = description.getBytes();
      }
    }
  }

  /**
   * Create an XML description of a {@link javax.naming.Context} from the server.
   * @param outOfScope <code>true</code> if the class for this context is not in the classpath. 
   * @return An XML string describing the current RMI context.
   * @throws NamingException If the RMI context could not be accessed.
   */
  private String getContextDescription(boolean outOfScope) throws NamingException {
    StringBuilder dsc = getHeader();
    dsc.append("<context name=\"").append(rmiRegistryContext.getNameInNamespace()).append("\">\n");
    NamingEnumeration<NameClassPair> ne = rmiRegistryContext.list("");
    while (ne.hasMore()) {
      NameClassPair nc = ne.next();
      dsc.append("  <name value=\"").append(nc.getName());
      if (nc.isRelative()) dsc.append("\" relative=\"").append(nc.isRelative());
      dsc.append("\">\n");

      dsc.append("    <class name=\"").append(nc.getClassName()).append("\"");
      if (outOfScope) dsc.append(" inscope=\"false\"");
      dsc.append("/>\n  </name>\n");
    }
    dsc.append("</context>");
    return dsc.toString();
  }

  /**
   * Create an XML description of an RMI service.
   * @param obj The Remote RMI object representing the service.
   * @return An XML string describing the service class.
   * @throws NamingException If the RMI data could not be read.
   */
  private String getServiceDescription(Object obj) throws NamingException {
    ClassDescriber cd = new ClassDescriberXML(obj);
    StringBuilder b = getHeader().append("<service>\n");
    b.append(cd.getDescription(1));
    b.append("</service>");
    return b.toString();
  }

  /**
   * Creates a {@link java.lang.StringBuilder} containing an XML header. This is ready to have
   * more data appended to it.
   * @return A new StringBuilder with the header in it.
   */
  private static StringBuilder getHeader() {
    StringBuilder header = new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n\n");
    return header;
  }

  /**
   * Convert an object into an array of bytes.
   * @param obj The object to serialize.
   * @return a <code>byte[]</code> containing the serialization of obj.
   * @throws IOException If there was an internal serialization error.
   */
  private byte[] serialize(Serializable obj) throws IOException {
    ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(dataOut);
    out.writeObject(obj);
    return dataOut.toByteArray();
  }

}
