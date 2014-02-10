/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com). Portions created
 * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
 * All Rights Reserved.
 */

package org.mulgara.resolver;

import org.mulgara.query.TuplesException;

public abstract class AnswerOperation {
  // Should use enum here.
  public static final int VOID = 0;
  public static final int OBJECT = 1;
  public static final int INT = 2;
  public static final int LONG = 3;
  public static final int BOOLEAN = 3;

  protected int returnType = VOID;

  protected Object object;
  protected int integer;
  protected long longint;
  protected boolean bool;

  public abstract void execute() throws TuplesException;

  protected void returnObject(Object object) {
    returnType = OBJECT;
    this.object = object;
  }

  protected void returnInt(int integer) {
    returnType = INT;
    this.integer = integer;
  }

  protected void returnLong(long longint) {
    returnType = LONG;
    this.longint = longint;
  }

  protected void returnBoolean(boolean bool) {
    returnType = BOOLEAN;
    this.bool = bool;
  }

  public AnswerOperationResult getResult() {
    return new AnswerOperationResult() {
        public Object getObject() {
          if (returnType != OBJECT) {
            throw new IllegalStateException("Invalid return type accessed: " + returnType);
          }
          return object;
        }

        public int getInt() {
          if (returnType != INT) {
            throw new IllegalStateException("Invalid return type accessed: " + returnType);
          }
          return integer;
        }

        public long getLong() {
          if (returnType != LONG) {
            throw new IllegalStateException("Invalid return type accessed: " + returnType);
          }
          return longint;
        }

        public boolean getBoolean() {
          if (returnType != BOOLEAN) {
            throw new IllegalStateException("Invalid return type accessed: " + returnType);
          }
          return bool;
        }
    };
  }
}
