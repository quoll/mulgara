/*
 * Copyright 2011 Revelytix, Inc.
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

package org.mulgara.query;

import org.apache.log4j.Logger;

/**
 * Basic functionality for an Answer.
 */
public abstract class AbstractAnswer implements Answer {

  private static final Logger logger = Logger.getLogger(AbstractAnswer.class.getName());

  /**
   * Clone method so that subtypes are able to call super.
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone a " + getClass().toString(), e);
    }
  }

  public boolean equals(Object object) {
    if ((object != null) && (object instanceof Answer)) {
      try {
        return AnswerOperations.equal(this, (Answer) object);
      } catch (TuplesException e) {
        logger.fatal("Couldn't test equality of answers", e);
      }
    }
    return false;
  }

  /**
   * Added to match {@link #equals(Object)}.
   */
  public int hashCode() {
    return AnswerOperations.hashCode(this);
  }
}
