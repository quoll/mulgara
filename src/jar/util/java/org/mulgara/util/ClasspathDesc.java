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

import java.io.File;
import java.io.IOException;

/**
 * Utility for describing a classpath, and extracting elements if needed.
 *
 * @created Aug 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ClasspathDesc {

  /** The identifier for the system classpath property. */
  private static final String JAVA_CLASS_PATH = "java.class.path";


  /**
   * @return The system classpath.
   */
  public static String getPath() {
    return System.getProperty(JAVA_CLASS_PATH);
  }


  /**
   * @return The elements of the classpath as an array of strings.
   */
  public static String[] getPaths() {
    return System.getProperty(JAVA_CLASS_PATH).split(File.pathSeparator);
  }


  /**
   * Looks in the classpath for a file that matches the <value>expected</value> parameter.
   * @param expected This will be part of a filename to search for.
   * @return The first filename in the classpath that contains <value>expected</value>.
   *         <code>null</code> if not found.
   */
  public static String getPath(String expected) {
    for (String path: getPaths()) if (path.contains(expected)) return path;
    return null;
  }

  /**
   * Creates a copy of a file from the classpath in a temporary directory.
   * @param expected Part of the name of the file being looked for in the classpath.
   * @return The path of the temporary file.
   * @throws IOException If there was a problem reading or writing the file.
   */
  public static String createTempCopy(String expected) throws IOException {
    String path = getPath(expected);
    if (path == null) return null;
    File dir = TempDir.getTempDir();
    return FileUtil.copyFile(path, new File(dir,path));
  }

}
