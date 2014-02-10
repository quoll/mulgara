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

package org.mulgara.krule.rlog.parser;

/**
 * Indicates an exception when parsing a URI.
 *
 * @created May 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class URIParseException extends Exception {

  /** For serializing */
  private static final long serialVersionUID = 8935948978390444100L;

  public URIParseException(String message) {
    super("URI not properly formed: " + message);
  }

  public URIParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public URIParseException(Throwable cause) {
    super(cause);
  }
}
