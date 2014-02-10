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

package org.mulgara.server;

// third party packages
import jargs.gnu.CmdLineParser;
import static jargs.gnu.CmdLineParser.Option.*;

// third party packages
import org.apache.log4j.Logger;

/**
 * Command line option parser for the Mulgara server.
 *
 * @created 2001-12-21
 * @author Tom Adams
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class EmbeddedMulgaraOptionParser extends CmdLineParser {

  /** option to display the help */
  public final static Option HELP = new BooleanOption('h', "help");

  /** option to stop a running server */
  public final static Option SHUTDOWN = new BooleanOption('x', "shutdown");

  /** option to use an external logging configuration file */
  public final static Option LOG_CONFIG = new StringOption('l', "logconfig");

  /** option to use an external configuration file */
  public final static Option SERVER_CONFIG = new StringOption('c', "serverconfig");

  /** option to change the host the HTTP server is bound to */
  public final static Option HTTP_HOST = new StringOption('o', "httphost");

  /** option to disble the http host */
  public final static Option NO_HTTP = new BooleanOption('w', "nohttp");

  /** option to change the host the server is bound to */
  public final static Option SERVER_HOST = new StringOption('k', "serverhost");

  /** option to set the default graph to use with SPARQL */
  public final static Option DEFAULT_GRAPH = new StringOption('g', "defaultgraph");

  /** option to disble the automatic starting of a RMI Registry */
  public final static Option NO_RMI = new BooleanOption('n', "normi");

  /** option to change to the port the RMI registry listens on */
  public final static Option RMI_PORT = new StringOption('r', "rmiport");

  /** option to change to the peer client port for RMI */
  public final static Option RMI_OBJECT_PORT = new IntegerOption('t', "rmiobjectport");

  /** option to change the port the http server is bound to */
  public final static Option PORT = new StringOption('p', "port");

  /** option to change the public port the http server is bound to */
  public final static Option PUBLIC_PORT = new StringOption('u', "publicport");

  /** option to change the name of the server */
  public final static Option SERVER_NAME = new StringOption('s', "servername");

  /** option to change the location of database files */
  public final static Option PERSISTENCE_PATH = new StringOption('a', "path");

  /** option to set the smtp server */
  public final static Option SMTP_SERVER = new StringOption('m', "smtp");

  /** Logger */
  private final static Logger log = Logger.getLogger(EmbeddedMulgaraOptionParser.class);

  /** the command line arguments passed to the Mulgara server */
  protected String[] args = null;

  /** flag indicating whether we've parsed the options yet */
  protected boolean optionsParsed = false;


  /**
   * Creates a new Mulgara server command line option parser to parse the command
   * line <code>args</code> given.
   *
   * @param args the command line arguments
   */
  public EmbeddedMulgaraOptionParser(String[] args) {
    super();
    if (args == null) throw new IllegalArgumentException("Null \"args\" parameter");

    // set the member
    setArgs(args);

    // add the options
    addOption(HELP);
    addOption(SHUTDOWN);
    addOption(LOG_CONFIG);
    addOption(HTTP_HOST);
    addOption(NO_HTTP);
    addOption(SERVER_HOST);
    addOption(DEFAULT_GRAPH);
    addOption(PORT);
    addOption(PUBLIC_PORT);
    addOption(NO_RMI);
    addOption(RMI_PORT);
    addOption(RMI_OBJECT_PORT);
    addOption(SERVER_NAME);
    addOption(PERSISTENCE_PATH);
    addOption(SMTP_SERVER);
    addOption(SERVER_CONFIG);
  }


  /**
   * Parses the command line arguments given to this parser.
   * @throws UnknownOptionException if an unknown option was specified in the
   *      list of options given to the parser
   * @throws IllegalOptionValueException if an option given to the parser
   *      contains an illegal value
   */
  public void parse() throws UnknownOptionException, IllegalOptionValueException {

    // parse the arguments if we haven't done so already
    if (!this.optionsParsed) {

      // parse the arguments
      parse(this.getArgs());

      // fail if there are any remaing that we dodn't know about
      String[] remainingArgs = getRemainingArgs();
      if (remainingArgs.length > 0) {
        // throw a new exception to indicate that there were unkown arguments
        log.error("Unknown options: " + remainingArgs);
        throw new UnknownOptionException(remainingArgs[0]);
      }

      // we've now parser the options
      optionsParsed = true;
    }
  }


  /**
   * Validates the command line options.
   * @return true if the options are valid
   */
  public boolean optionsValid() {

    boolean optionsValid = false;

    try {
      // validate params by parsing the arguments
      this.parse();
      optionsValid = true;
    } catch (UnknownOptionException uoe) {
      optionsValid = false;
    } catch (IllegalOptionValueException iove) {
      optionsValid = false;
    }

    return optionsValid;
  }

  /**
   * Sets the command line arguments passed to the itql interpreter.
   * @param args The new Args value
   */
  protected void setArgs(String[] args) {
    this.args = args;
  }


  /**
   * Returns the command line arguments passed to the itql interpreter.
   * @return the command line arguments passed to the itql interpreter
   */
  protected String[] getArgs() {
    return args;
  }
}
