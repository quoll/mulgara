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

package org.mulgara.content.mbox.parser.model;

import java.util.Properties;
import java.io.*;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.mulgara.content.mbox.parser.model.exception.*;

import org.apache.log4j.Logger;

/**
 * Factory used to create implementations of the parsers used by the mbox
 * resolver.  Which implementation is used is determined by the factory
 * configuration.
 *
 * @created 2004-08-24
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.7 $
 *
 * @modified $Date: 2005/01/05 04:57:42 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Quoll License</a>
 */
public class ModelFactory {

  /** The logger */
  private static final Logger log = Logger.getLogger(ModelFactory.class);

  /** The singleton instance of this class */
  private static ModelFactory instance = null;

  /** The properties determining which implementation to use */
  private static Properties properties;

  /**
   * Constructor.  Creates and initialises the console factory.
   */
  private ModelFactory() throws FactoryException {

    // Perform initialisation of the factory
    initialiseFactory();
  }

  /**
   * Get the ModelFactory instance.
   *
   * @return The ModelFactory singleton instance.
   *
   * @throws FactoryException
   */
  public static ModelFactory getInstance() throws FactoryException {

    synchronized (ModelFactory.class) {

      if (instance == null) {

        // Create the factory
        instance = new ModelFactory();
      }
    }

    return instance;
  }

  /**
   * Initialises the factory by reading in the configuration and any other setup
   * functions.
   *
   * @throws FactoryException
   */
  private void initialiseFactory() throws FactoryException {

    // Initialise our properties
    properties = new Properties();

    // Retrieve the resource url for our configuration file
    URL parserConfiguration = this.getClass().getResource("/modelfactory.conf");

    try {

      // Load the properties for the parser factory using the stream from the URL
      properties.load(parserConfiguration.openStream());
    } catch (IOException ioException) {

      throw new FactoryException("Unable to load the parser factory " +
                                 "configuration from: " +
                                 parserConfiguration.toString(), ioException);
    }
  }

  /**
   * Creates and returns an instance of the class defined by
   * "classNameProperty" in the properties file. Any parameters for the
   * constructor are also passed in and used when constructing a new instance.
   * Returned Object needs to be cast to correct type.
   *
   * @param classNameProperty The proprty which holds the class name to
   *                          instantiate
   * @param classType The class type to create the object in
   * @param parameterTypes The List of type for the constructor parameters
   * @param parameters The actual parameter values, matching the types array
   *
   * @return Object
   *
   * @throws FactoryException
   */
  private Object createObject(String classNameProperty, String classType,
                              Class<?>[] parameterTypes, Object[] parameters) throws
      FactoryException {

    //The instance of the "classNameProperty" to be returned
    Object object = null;

    //class that classNameProperty represents from the properties file
    Class<?> objectClass = null;

    //class type that object should be instantiated as
    Class<?> type = null;

    // Constructor for our object
    Constructor<?> constructor = null;

    try {

      //Get the objectClass
      objectClass = Class.forName(properties.getProperty(classNameProperty));

      //set type as Class "classType"
      type = Class.forName(classType);

      try {

        // Get the constructor we are after by parameter types
        constructor = objectClass.getConstructor(parameterTypes);
      } catch (NoSuchMethodException noSuchMethodException) {

        // Container for type string
        String types = "";

        for (int i = 0; i < parameterTypes.length; i++) {

          // Add the next parameter type name
          types += parameterTypes[i].getName() + ", ";
        }

        // An exception has occurred so report it and throw it as a factory exception
        log.error("Constructor with parameters [" + types + "] was not found.",
                  noSuchMethodException);
        throw new FactoryException("Constructor with parameters [" + types +
                                   "] was not found.",
                                   noSuchMethodException);
      }

      // Instantiate an instance of the class
      object = constructor.newInstance(parameters);
    } catch (ClassNotFoundException classNotFoundException) {

      // An exception has occurred so report it and throw it as a factory exception
      log.error(classNameProperty + " was not found.",
                classNotFoundException);
      throw new FactoryException(classNameProperty + " was not found.",
                                 classNotFoundException);
    } catch (InstantiationException instantiationException) {

      // An exception has occurred so report it and throw it as a factory exception
      log.error("Could not instantiate " + classNameProperty + ".",
                instantiationException);
      throw new FactoryException("Could not instantiate " + classNameProperty
                                 + ".", instantiationException);
    } catch (IllegalAccessException illegalAccessException) {

      // An exception has occurred so report it and throw it as a factory exception
      log.error("Illegal access when creating " + classNameProperty + ".",
                illegalAccessException);
      throw new FactoryException("Illegal access when creating "
                                 + classNameProperty + ".",
                                 illegalAccessException);
    } catch (InvocationTargetException invocationTargetException) {

      // An exception has occurred so report it and throw it as a factory exception
      log.error("Could not find target constructor for " + classNameProperty +
                ".",
                invocationTargetException);
      throw new FactoryException("Could not find target constructor for " +
                                 classNameProperty + ".",
                                 invocationTargetException);
    }

    if (type.isInstance(object)) {

      //return the instance of the class as java.lang.Object
      return object;

    } else {

      throw new FactoryException(classNameProperty + " could not be " +
                                 "instantiated because it is not a valid " +
                                 type.getName() + " class");
    }
  }

  /**
   * Creates an mbox file object using the implementation specified in the
   * properties and returns it.
   *
   * @param parameterTypes Any parameter types of constructor parameters
   * @param parameters The parameter values matching the types for the
   *                   constructor
   *
   * @return The created mbox object
   */
  public MBox createMBox(Class<?>[] parameterTypes, Object[] parameters) throws FactoryException {

    // Container for parser object
    MBox mbox = null;

    // Create an instance of the class defined in the properties file
    mbox = (MBox)this.createObject("mbox.class", MBox.class.getName(),
                                   parameterTypes, parameters);

    // Return the parser instance
    return mbox;
  }

  /**
   * Creates an MBox management object using the implementation specified in the
   * properties and returns it.
   *
   * @return The created mbox manager object
   */
  public MBoxManager createMBoxManager() throws FactoryException {

    // Container for parser object
    MBoxManager mboxManager = null;

    // Create an array of the types for the mbox constructor
    Class<?>[] parameterTypes = new Class[0];

    // Create an array of parameters for the mbox constructor
    Object[] parameters = new Object[0];

    // Create an instance of the class defined in the properties file
    mboxManager = (MBoxManager)this.createObject("mboxmanager.class",
                                                 MBoxManager.class.getName(),
                                                 parameterTypes, parameters);

    // Return the parser instance
    return mboxManager;
  }

}
