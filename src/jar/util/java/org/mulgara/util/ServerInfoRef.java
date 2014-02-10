/*
 * Copyright 2009 Fedora Commons, Inc.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Provides access to the global ServerInfo class when it is not available as a dependency.
 *
 * @created June 08, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ServerInfoRef {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(ServerInfoRef.class.getName());

  /** Singleton of the ServerInfo class. */
  private static Class<?> si = null;

  /** Singleton getters, stored by name. */
  private static Map<String,Method> getters = new HashMap<String,Method>();

  /** Singleton setters, stored by name. */
  private static Map<String,Method> setters = new HashMap<String,Method>();

  /**
   * Sets the hostnames on the ServerInfo object, if it is visible.
   *
   * @param names The set of hostnames to set on ServerInfo
   */
  public static void setHostnameAliases(Set<String> names) {
    setServerInfoProperty("HostnameAliases", names);
  }


  /**
   * Method to ask the ServerInfo for the local server aliases.
   * This will return an empty set if ServerInfo is not available -
   * ie. being run on a host which has no local database, such an an iTQL client.
   *
   * @return The set of server aliases as strings
   */
  @SuppressWarnings("unchecked")
  public static Set<String> getHostnameAliases() {
    Set<String> names = (Set<String>)getServerInfoProperty("HostnameAliases");
    return (names == null) ? (Set<String>)java.util.Collections.EMPTY_SET : names;
  }


  /**
   * Gets the hostnames on the ServerInfo object, if it is visible.
   *
   * @return The default graph URI, used by SPARQL
   */
  public static URI getDefaultURI() {
    URI u = (URI)getServerInfoProperty("DefaultGraphURI");
    if (u == null) {
      String c = getMulgaraConstant("DEFAULT_GRAPH");
      u = (c != null) ? URI.create(c) : URI.create("sys:default");
    }
    return u;
  }


  /**
   * Method to ask the ServerInfo for the local server URI.
   * This will return null if ServerInfo is not available -
   * ie. being run on a host which has no local database, such an an iTQL client.
   * 
   * @return The server URI
   */
  public static URI getServerURI() {
    return (URI)getServerInfoProperty("ServerURI");
  }


  /**
   * Get the ServerInfo class as a singleton.
   * @return The instance of the ServerInfo Class object.
   * @throws ClassNotFoundException If ServerInfo is not on the classpath.
   */
  private static final Class<?> getServerInfoClass() throws ClassNotFoundException {
    if (si == null) {
      si = Class.forName("org.mulgara.server.ServerInfo");
    }
    return si;
  }


  /**
   * Sets a property on the ServerInfo, if it is available
   * @param name The name of the property. Case sensitive.
   * @param value The value of the property to be set.
   */
  public static final void setServerInfoProperty(String name, Object value) {
    try {
      Method setter = findSetter(name, value);
      if (setter != null) {
        setter.invoke(null, new Object[] { value });
      } else {
        logger.info("No setter method found in Server Info for: " + name);
      }
    } catch (Exception e) {
      /* Not much that can be done here */
      logger.info("Unable to set '" + name + "' for Server Info", e);
    }
  }


  /**
   * Gets a property from the ServerInfo, if it is available
   * @param name The name of the property. Case sensitive.
   * @return The value, or <code>null</code> if not available.
   */
  public static final Object getServerInfoProperty(String name) {
    try {
      Method getter = findGetter(name);
      return getter.invoke(null, new Object[] { });
    } catch (Exception e) {
      /* Not much that can be done here */
      logger.debug("Unable to get '" + name + "' from Server Info", e);
      return null;
    }
  }


  /**
   * Get the Getter for a property as a singleton to be stored by name.
   * @param name The name of the property to find the getter for.
   * @return The method used for getting the property.
   * @throws SecurityException If the method is not allowed to be used.
   * @throws NoSuchMethodException If the readable property does not exist.
   * @throws ClassNotFoundException If the ServerInfo class is not on the classpath.
   */
  private static final Method findGetter(String name) throws SecurityException, NoSuchMethodException, ClassNotFoundException {
    String fullName = "get" + name;
    Method getter = getters.get(fullName);
    if (getter == null) {
      getter = getServerInfoClass().getMethod(fullName, new Class[] { });
      getters.put(fullName, getter);
    }
    return getter;
  }


  /**
   * Get the Setter for a property as a singleton to be stored by name.
   * @param name The name of the property to find the setter for.
   * @param value The value of the property to be set.
   * @return The method used for setting the property.
   * @throws SecurityException If the method is not allowed to be used.
   * @throws ClassNotFoundException If the ServerInfo class is not on the classpath.
   */
  private static final Method findSetter(String name, Object value) throws SecurityException, ClassNotFoundException {
    String fullName = "set" + name;
    Method setter = setters.get(fullName);
    if (setter == null) {
      for (Class<?>cls: getSuperTypes(value.getClass())) {
        try {
          setter = getServerInfoClass().getMethod(fullName, new Class[] { cls });
          break;
        } catch (NoSuchMethodException e) { /* continue */ }
      }
      setters.put(fullName, setter);
    }
    return setter;
  }


  /**
   * Get the list of all superclasses and interfaces that a class meets.
   * @param cls The class to get all supertypes of.
   * @return A List of classes and interfaces that this class extends.
   */
  private static final List<Class<?>> getSuperTypes(Class<?> cls) {
    List<Class<?>> result = new LinkedList<Class<?>>();
    for (Class<?> i: cls.getInterfaces()) result.add(i);
    do {
      result.add(cls);
    } while ((cls = cls.getSuperclass()) != null);
    return result;
  }


  /**
   * Gets a string constant from the Mulgara class.
   * @param name The name of the constant.
   * @return The string value of the constant, or null if this cannot be retrieved.
   */
  private static final String getMulgaraConstant(String name) {
    // reflect the data out of the Mulgara class
    String c = null;
    try {
      Class<?> mulgara = Class.forName("org.mulgara.query.rdf.Mulgara");
      Field field = mulgara.getDeclaredField(name);
      c = (String)field.get(null);
    } catch (Exception e) {
      logger.error("Unable to find Mulgara constants.", e);
    }
    return c;
  }
}
