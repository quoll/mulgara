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

package org.mulgara.xml;

// Java 2 standard packages
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.*;

/**
 * A {@link DocumentBuilderFactory} that wraps another. This is useful when
 * subclassing and selectively overriding methods.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:13 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DocumentBuilderFactoryWrapper extends DocumentBuilderFactory {

  /**
   * The system property used to set the wrapped {@link DocumentBuilderFactory}
   * class.
   */
  public final String DOCUMENT_BUILDER_FACTORY_PROPERTY =
      "org.mulgara.xml.ResourceDocumentBuilderFactory";

  /**
   * The class name of the default {@link DocumentBuilderFactory} used if the
   * {@link #DOCUMENT_BUILDER_FACTORY_PROPERTY} isn't set.
   */
  public final String DEFAULT_DOCUMENT_BUILDER_FACTORY =
      "org.apache.crimson.jaxp.DocumentBuilderFactoryImpl";

  //"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";

  /**
   * Wrapped instance.
   */
  protected DocumentBuilderFactory wrapped;

  //
  // Constructors
  //

  /**
   * Default constructor. This wraps a {@link DocumentBuilderFactory}
   * implementation obtained from the
   * <code>org.kowiar.xml.DocumentBuilderFactory</code>
   * system property. If the system property isn't set, it defaults to
   * <code>org.apache.crimson.jaxp.DocumentBuilderFactoryImpl</code>
   * , the Crimson parser from Sun's JDK 1.4.
   *
   * @throws RuntimeException if the factory class is misconfigured
   */
  public DocumentBuilderFactoryWrapper() {

    try {

      // The name of the wrapped DocumentBuilderFactory class
      String className;

      // Determine the classname of the DocumentBuilderFactory to wrap
      className = System.getProperty(DOCUMENT_BUILDER_FACTORY_PROPERTY);

      if (className == null) {

        className = DEFAULT_DOCUMENT_BUILDER_FACTORY;
      }

      //assert className != null;
      // Initialize fields
      this.wrapped =
          (DocumentBuilderFactory) Class.forName(className).newInstance();
    }
    catch (Exception e) {

      RuntimeException e2 = new RuntimeException("Misconfigured factory");
      e2.initCause(e);
      throw e2;
    }
  }

  /**
   * Constructor.
   *
   * @param wrapped the {@link DocumentBuilderFactory} to delegate functionality
   *      to
   * @throws IllegalArgumentException if <var>wrapped</var> is <code>null</code>
   */
  public DocumentBuilderFactoryWrapper(DocumentBuilderFactory wrapped) {

    // Validate "wrapped" parameter
    if (wrapped == null) {

      throw new IllegalArgumentException("Null \"wrapped\" parameter");
    }

    // Initialize fields
    this.wrapped = wrapped;
  }

  /**
   * Sets the Attribute attribute of the DocumentBuilderFactoryWrapper object
   *
   * @param name The new Attribute value
   * @param value The new Attribute value
   * @throws IllegalArgumentException EXCEPTION TO DO
   */
  public void setAttribute(String name,
      Object value) throws IllegalArgumentException {

    wrapped.setAttribute(name, value);
  }

  //
  // Methods implementing DocumentBuilderFactory
  //

  /**
   * Gets the Attribute attribute of the DocumentBuilderFactoryWrapper object
   *
   * @param name PARAMETER TO DO
   * @return The Attribute value
   * @throws IllegalArgumentException EXCEPTION TO DO
   */
  public Object getAttribute(String name) throws IllegalArgumentException {

    return wrapped.getAttribute(name);
  }

  /**
   * Gets a feature from the DocumentBuilderFactoryWrapper
   * Uses reflection to allow it to compile with JDK1.4.
   */
  public boolean getFeature(String name) throws ParserConfigurationException {
    Class<?> c = wrapped.getClass();
    try {
      java.lang.reflect.Method getFeature = c.getMethod("getFeature", new Class[] { String.class });
      return ((Boolean)getFeature.invoke(wrapped, new Object[] { name })).booleanValue();
    } catch (Exception e) {
      if (e instanceof InvocationTargetException) {
        Throwable t = e.getCause();
        throw (ParserConfigurationException)t;
      }
      throw new ParserConfigurationException(e.getMessage());
    }
  }

  /**
   * Sets a feature on the DocumentBuilderFactoryWrapper
   * Uses reflection to allow it to compile with JDK1.4.
   */
  public void setFeature(String name, boolean value) throws ParserConfigurationException {
    Class<?> c = wrapped.getClass();
    try {
      java.lang.reflect.Method setFeature = c.getMethod("setFeature", new Class[] { String.class, Boolean.TYPE });
      setFeature.invoke(wrapped, new Object[] { name, new Boolean(value) });
    } catch (Exception e) {
      if (e instanceof InvocationTargetException) {
        Throwable t = e.getCause();
        throw (ParserConfigurationException)t;
      }
      throw new ParserConfigurationException(e.getMessage());
    }
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws ParserConfigurationException EXCEPTION TO DO
   */
  public DocumentBuilder newDocumentBuilder() throws
      ParserConfigurationException {

    return wrapped.newDocumentBuilder();
  }
}
