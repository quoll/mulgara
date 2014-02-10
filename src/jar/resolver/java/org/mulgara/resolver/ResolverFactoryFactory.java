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

package org.mulgara.resolver;

// Java 2 standard packages
import java.lang.reflect.Method;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.resolver.spi.*;
import org.mulgara.store.xa.XAResolverSessionFactory;

/**
 * Constructs {@link ResolverFactory} instances, given a classname.
 *
 * This also constructs the {@link SystemResolverFactory} subclass of
 * {@link ResolverFactory}.
 *
 * @created 2004-10-07
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
abstract class ResolverFactoryFactory
{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(ResolverFactoryFactory.class.getName());

  /**
   * Construct a {@link ResolverFactory}.
   *
   * @param className  the name of a class implementing {@link ResolverFactory}
   * @return the constructed {@link ResolverFactory}
   */
  static ResolverFactory newResolverFactory(
                           String className,
                           ResolverFactoryInitializer initializer
                         )
    throws InitializerException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating resolver factory " + className);
    }

    // Validate parameters
    if (className == null) {
      throw new IllegalArgumentException("Null 'className' parameter");
    }

    try {
      Class<?> resolverFactoryClass = null;
      try {
        resolverFactoryClass = Class.forName(className);
      } catch (Throwable t) {
        throw new IllegalArgumentException(className + " could not be loaded", t);
      }

      // Validate that the candidate supports the ResolverFactory interface
      if (!ResolverFactory.class.isAssignableFrom(resolverFactoryClass)) {
        throw new IllegalArgumentException(className + " is not an " + ResolverFactory.class.getName());
      }

      // Invoke the static ResolverFactory.newInstance method
      Method newInstanceMethod =
        resolverFactoryClass.getMethod(
          "newInstance",
           new Class[] { ResolverFactoryInitializer.class }
        );

      ResolverFactory resolverFactory = (ResolverFactory)
        newInstanceMethod.invoke(null, new Object[] { initializer });

      return resolverFactory;
    }
    catch (Exception e) {
      logger.warn("Error generating resolver factory", e);
      throw new InitializerException("Unable to add resolver factory", e);
    }
  }

  /**
   * Construct a {@link SystemResolverFactory}.
   *
   * @param className  the name of a class implementing
   *   {@link SystemResolverFactory}
   * @param spSessionFactory
   * @return the constructed {@link SystemResolverFactory}
   */
  static SystemResolverFactory newSystemResolverFactory(
                                 String                   className,
                                 FactoryInitializer       initializer,
                                 StringPoolSessionFactory spSessionFactory)
    throws InitializerException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating system resolver factory " + className);
    }

    // Validate parameters
    if (className == null) {
      throw new IllegalArgumentException("Null 'className' parameter");
    } else if (spSessionFactory == null) {
      throw new IllegalArgumentException("Null 'spSessionFactory' parameter");
    }

    try {
      Class<?> resolverFactoryClass = null;
      try {
        resolverFactoryClass = Class.forName(className);
      } catch (Throwable t) {
        throw new IllegalArgumentException(className + " could not be loaded", t);
      }

      // Validate that the candidate supports the ResolverFactory interface
      if (!SystemResolverFactory.class.isAssignableFrom(resolverFactoryClass)) {
        throw new IllegalArgumentException(
            className + " is not an " + SystemResolverFactory.class.getName());
      }

      // Invoke the static ResolverFactory.newInstance method
      Method newInstanceMethod = resolverFactoryClass.getMethod("newInstance",
          new Class[] { FactoryInitializer.class, XAResolverSessionFactory.class });

      SystemResolverFactory systemResolverFactory = (SystemResolverFactory)
        newInstanceMethod.invoke(
          null,
          new Object[] { initializer, spSessionFactory }
        );

      return systemResolverFactory;
    } catch (Exception e) {
      logger.warn("Error generating resolver factory", e);
      throw new InitializerException("Unable to add resolver factory", e);
    }
  }
}
