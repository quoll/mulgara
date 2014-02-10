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

package org.mulgara.webquery;

import java.io.IOException;
import java.io.InputStream;
import static org.mulgara.util.ObjectUtil.getClassLoader;

public abstract class ResourceFile {

  /** The location of resource files. */
  static final String RESOURCES = "resources";
  
  /** The path of the resource file to load. */
  protected String resourceFile;

  /**
   * Create the ResourceFile.
   * @param resourceFile The path to the resource file.
   */
  ResourceFile(String resourceFile) {
    this.resourceFile = RESOURCES + resourceFile;
  }

  /**
   * Get the data from the resource file as a stream.
   * @return An InputStream for accessing the resource file.
   */
  protected InputStream getStream() throws IOException {
    InputStream in = getClassLoader(this).getResourceAsStream(resourceFile);
    if (in == null) throw new IOException("Unable to load resource: " + resourceFile);
    return in;
  }
}
