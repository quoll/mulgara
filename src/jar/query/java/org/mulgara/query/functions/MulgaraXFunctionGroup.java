/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathFunctionException;

import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;

/**
 * Container for functions in the mulgarax domain.
 *
 * @created Oct 5, 2009
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class MulgaraXFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the mulgarax: namespace */
  static final String PREFIX = "mulgarax";

  /** The mulgarax: namespace */
  static final String NAMESPACE = "http://mulgara.org/mulgarax#";

  /** Internal buffer size for reading from external processes */
  private static final int BUFFER_SIZE = 1024;

  /**
   * Get the prefix used for the namespace of these operations.
   * @return The short string used for a prefix in a QName.
   */
  public String getPrefix() {
    return PREFIX;
  }

  /**
   * Get the namespace of these operations.
   * @return The string of the namespace URI.
   */
  public String getNamespace() {
    return NAMESPACE;
  }

  /**
   * Get the set of MulgaraX functions.
   * @return A set of MulgaraFunction for this entire group.
   */
  public Set<MulgaraFunction> getAllFunctions() {
    Set<MulgaraFunction> functions = new HashSet<MulgaraFunction>();
    functions.add(new System());
    functions.add(new System2());
    return functions;
  }

  /**
   * Function to execute a command in a shell. Stdout is captured and returned as a string.
   * @see http://www.w3.org/TR/xpath-functions/#func-matches
   */
  static private class System extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      String str = (String)args.get(0);
      StringBuilder outputString = new StringBuilder();
      try {
        Process proc = Runtime.getRuntime().exec(str);
        proc.getOutputStream().close();
        BufferedReader procStdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        while ((len = procStdOut.read(buffer)) >= 0) outputString.append(buffer, 0, len);
        procStdOut.close();
        proc.getErrorStream().close();
      } catch (IOException e) {
        throw new XPathFunctionException("I/O error communicating with external process.");
      }
      return outputString.toString();
    }
  }

  /**
   * Function to execute a command in a shell. A second parameter provides stdin for the process.
   * Stdout is captured and returned as a string.
   */
  static private class System2 extends MulgaraFunction {
    public String getName() { return "system/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) throws XPathFunctionException {
      // get the command and arguments
      String str = (String)args.get(0);
      // get the data to feed to stdin
      String inputString = args.get(1).toString();

      StringBuilder outputString = new StringBuilder();
      try {
        Process proc = Runtime.getRuntime().exec(str);
        // get stdin for the process
        Writer procStdIn = new OutputStreamWriter(proc.getOutputStream());
        // get stdout for the process
        BufferedReader procStdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        // write to stdin
        procStdIn.append(inputString);
        procStdIn.close();
        // read from stdout
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        while ((len = procStdOut.read(buffer)) >= 0) outputString.append(buffer, 0, len);
        procStdOut.close();
        proc.getErrorStream().close();
      } catch (IOException e) {
        throw new XPathFunctionException("I/O error communicating with external process.");
      }
      return outputString.toString();
    }
  }
}
