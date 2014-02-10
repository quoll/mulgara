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

package org.mulgara.query.operation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.parser.MulgaraLexerException;
import org.mulgara.parser.Interpreter;
import org.mulgara.parser.MulgaraParserException;

/**
 * An AST element for running an external script.
 * @created Aug 14, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ExecuteScript extends LocalCommand {

  /** The logger */
  static final Logger logger = Logger.getLogger(ExecuteScript.class.getName());

  /** The script to be executed. */
  private final URL script;
  
  /** The interpreter for the script. */
  final Interpreter interpreter;
  
  
  /**
   * Creates a script execution command.
   * @param script The script to execute.
   */
  public ExecuteScript(URL script, Interpreter interpreter) {
    this.script = script;
    this.interpreter = interpreter;
  }

  /**
   * Indicates that this is a command for the UI.
   * @return <code>true</code> to indicated that script execution is a UI feature.
   */
  public boolean isUICommand() {
    return true;
  }

  /**
   * @return the script URL
   */
  public URL getScript() {
    return script;
  }

  /**
   * Perform a series of actions, read from a file.
   * @param conn The connection to perform the actions on.  Only one connection can be used.
   * @return A string containing the accumulation of output messages from all commands in the
   *         script, separated by newlines.
   */
  public Object execute(Connection conn) throws Exception {
    // create a buffer to hold the results in
    StringBuffer resultsMsg = new StringBuffer();

    // create a reader to read the contents of the script
    BufferedReader scriptIn = new BufferedReader(new InputStreamReader(script.openStream()));

    // execute the script!
    int line = 0;
    try {
      String commandLine;
      while (null != (commandLine = scriptIn.readLine())) {
        line++;
        if (!commandLine.equals("")) {
          Command cmd = interpreter.parseCommand(commandLine);
          cmd.execute(conn);
          resultsMsg.append(cmd.getResultMessage()).append("\n");
        }
      }
      resultsMsg.append("Completed execution of script " + script);
      setResultMessage(resultsMsg.toString());
    } catch (MulgaraParserException pe) {
      setResultMessage("Syntax error in script (line " + line + "): " + pe.getMessage());
      logger.warn("Unable to execute script - " + script + "\n" + pe.getMessage());
    } catch (MulgaraLexerException le) {
      setResultMessage("Syntax error in script (line " + line + "): " + le.getMessage());
      logger.warn("Unable to execute script - " + script + "\n" + le.getMessage());
    }

    return getResultMessage();
  }

}
