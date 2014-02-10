/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.swrl;

/**
 * Exception to indicate an error in the structure of an RDF graph describing
 * a collection of SWRL rules.
 * 
 * @created Jun 5, 2009
 * @author Alex Hall
 * @copyright &copy; 2009 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SWRLStructureException extends Exception {

  private static final long serialVersionUID = -8536158307400743724L;

  /**
   * @param message The exception message.
   */
  public SWRLStructureException(String message) {
    super(message);
  }

  /**
   * @param message The exception message.
   * @param cause The exception cause.
   */
  public SWRLStructureException(String message, Throwable cause) {
    super(message, cause);
  }

}
