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

package org.mulgara.rules;

// Java 2 standard packages
import java.lang.reflect.Method;
import java.net.URI;

import org.apache.log4j.Logger;
import org.mulgara.query.GraphExpression;

/**
 * Constructs {@link RuleLoader} instances, given a classname.
 *
 * @created 2005-05-22
 * @author <a href="pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/06/26 12:42:43 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy;2004 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class RuleLoaderFactory {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(RuleLoaderFactory.class.getName());

  /**
   * Construct a {@link RuleLoader}.
   *
   * @param className  the name of a class implementing {@link RuleLoader}.
   * @param source  the location of the rules to read.
   * @param base  the location of the base data for the rules to operate on.
   * @param target  the destination for rule consequences.
   * @return the constructed {@link RuleLoader}
   */
  public static RuleLoader newRuleLoader(String className, URI source, GraphExpression base, URI target) throws InitializerException {

    if (logger.isDebugEnabled()) logger.debug("Creating rule loader " + className);

    // Validate parameters
    if (className == null) throw new IllegalArgumentException("Null 'className' parameter");

    try {
      Class<?> ruleLoaderClass = Class.forName(className);

      // Validate that the candidate supports the RuleLoader interface
      if (!RuleLoader.class.isAssignableFrom(ruleLoaderClass)) {
        throw new IllegalArgumentException(
            className + " is not an " + RuleLoader.class.getName());
      }

      // Invoke the static RuleLoader.newInstance method
      // can't be described in the interface, but it must be there anyway.
      Method newInstanceMethod =
        ruleLoaderClass.getMethod(
          "newInstance",
           new Class[] { URI.class, GraphExpression.class, URI.class }
        );

      RuleLoader ruleLoader = (RuleLoader)
        newInstanceMethod.invoke(null, new Object[] { source, base, target });

      return ruleLoader;

    } catch (Exception e) {
      logger.warn("Error generating rule loader factory", e);
      throw new InitializerException("Unable to add rule loader factory", e);
    }
  }

}
