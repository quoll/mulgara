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
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandlerException;

/**
 *  Mediates access to Content by protocol.
 *
 * @created 2004-10-07
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:23 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ContentFactory
{
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger =
    Logger.getLogger(ContentFactory.class.getName());


  private static Map<String,String> contentClasses = new HashMap<String,String>();
  private static Map<String,Constructor> contentConstructors = new HashMap<String,Constructor>();

  static {
    contentClasses.put("file", "org.mulgara.resolver.file.FileContent");
    contentClasses.put("http", "org.mulgara.resolver.http.HttpContent");
    contentClasses.put("https", "org.mulgara.resolver.http.HttpContent");
    contentClasses.put("jar", "org.mulgara.resolver.jar.JarContent");
  }

  /**
   * Returns a Content object that represents the resource.
   *
   * @param srcURI  source URI
   * @throws QueryException
   * @return Content
   */
  public static Content getContent(URI srcURI) throws ContentHandlerException {
    if (srcURI == null) {
      throw new IllegalArgumentException("Source URI is null");
    }

    try {
      // Determine the source type
      String scheme = srcURI.getScheme().toLowerCase();
      Constructor constructor = contentConstructors.get(scheme);
      if (constructor == null) {
        constructor = loadConstructor(scheme);
      }

      return (Content)constructor.newInstance(new Object[] { srcURI });
    } catch (ContentHandlerException ec) {
      throw ec;
    } catch (Exception e) {
      throw new ContentHandlerException("Failed to obtain Content from: " + srcURI, e);
    }
  }
  
  private static Constructor loadConstructor(String scheme) throws ContentHandlerException, ClassNotFoundException, NoSuchMethodException {
    String className = contentClasses.get(scheme);
    if (className == null) {
      throw new ContentHandlerException("No Content wrapper available for " + scheme);
    }

    Class<?> klass = Class.forName(className);
    Constructor constructor = klass.getConstructor(new Class[] { URI.class });

    contentConstructors.put(scheme, constructor);

    return constructor;
  }
}
