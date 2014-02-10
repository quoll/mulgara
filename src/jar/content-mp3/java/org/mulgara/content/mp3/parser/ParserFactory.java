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

package org.mulgara.content.mp3.parser;

// Java
import java.util.Properties;
import java.io.*;
import java.net.URL;

// Log4j
import org.apache.log4j.Logger;

// Internal
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Factory used to create implementations of the parsers used by the MP3 to
 * RDF tool.  Which implementation is used is determined by the tool's
 * configuration.
 *
 * @created 2004-08-02
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/11 07:02:24 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Quoll License</a>
 */
public class ParserFactory {

  /** The logger */
  private static final Logger log = Logger.getLogger(ParserFactory.class);

  /** The singleton instance of this class */
  private static ParserFactory instance = null;

  /** The properties determining which implementation to use */
  private static Properties properties;

  /**
   * Constructor.  Creates and initialises the console factory.
   */
  private ParserFactory() {

  }

  /**
   * Get the ParserFactory instance.
   *
   * @return The ParserFactory singleton instance.
   *
   * @throws FactoryException
   */
  public static ParserFactory getInstance() throws FactoryException {

    synchronized (ParserFactory.class) {

      if (instance == null) {

        // Create the factory
        instance = new ParserFactory();
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
  public void initialiseFactory() throws FactoryException {

    // Initialise our properties
    properties = new Properties();

    // Retrieve the resource url for our configuration file
    URL parserConfiguration = this.getClass().getResource("/parserfactory.conf");

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
   * Creates an instance of a class given the name of the class.
   *
   * @param className The name of the class we are instantiating
   *
   * @return The created object
   *
   * @throws FactoryException
   */
  public Object getInstanceOf(String className) throws FactoryException {

    //value to be returned
    Object object = null;

    //get the implementing class from the properties file and instantiate
    object = this.createObject(className, className);

    return object;
  }

  /**
   * Creates and returns an instance of the class defined by
   * "classNameProperty" in the properties file. Returned Object needs to be
   * cast to correct type.
   *
   * @param classNameProperty String
   * @throws FactoryException
   * @return Object
   */
  private Object createObject(String classNameProperty, String classType) throws
      FactoryException {

    //The instance of the "classNameProperty" to be returned
    Object object = null;

    //class that classNameProperty represents from the properties file
    Class<?> objectClass = null;

    //class type that object should be instantiated as
    Class<?> type = null;

    try {

      //Get the objectClass
      objectClass = Class.forName(properties.getProperty(classNameProperty));

      //set type as Class "classType"
      type = Class.forName(classType);

      // Instantiate an instance of the class
      object = objectClass.newInstance();

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
   * Creates an ID3 Parser object using the implementation specified in the
   * properties and returns it.
   *
   * @return The created parser object
   */
  public ID3Parser createID3Parser() throws FactoryException {

    // Container for parser object
    ID3Parser parser = null;

    // Create an instance of the class defined in the properties file
    parser = (ID3Parser) this.createObject("id3parser.class", ID3Parser.class.getName());

    // Return the parser instance
    return parser;
  }

  /**
   * Creates an ID3v1 Parser object using the implementation specified in the
   * properties and returns it.
   *
   * @return The created parser object
   */
  public ID3v1Parser createID3v1Parser() throws FactoryException {

    // Container for parser object
    ID3v1Parser parser = null;

    // Create an instance of the class defined in the properties file
    parser = (ID3v1Parser) this.createObject("id3v1parser.class", ID3v1Parser.class.getName());

    // Return the parser instance
    return parser;
  }

  /**
   * Creates an ID3v2 Parser object using the implementation specified in the
   * properties and returns it.
   *
   * @return The created parser object
   */
  public ID3v2Parser createID3v2Parser() throws FactoryException {

    // Container for parser object
    ID3v2Parser parser = null;

    // Create an instance of the class defined in the properties file
    parser = (ID3v2Parser) this.createObject("id3v2parser.class", ID3v2Parser.class.getName());

    // Return the parser instance
    return parser;
  }
}
