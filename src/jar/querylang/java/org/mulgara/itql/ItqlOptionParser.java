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

package org.mulgara.itql;

// third party packages
import jargs.gnu.CmdLineParser;

// third party packages
import org.apache.log4j.Logger;

/**
 * Command line option parser for the iTQL interpreter.
 *
 * @created 2001-09-20
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlOptionParser extends CmdLineParser {

  /**
   * option to dump the entire interpreter configuration
   */
  public final static Option DUMP_CONFIG =
      new CmdLineParser.Option.StringOption('d', "dumpconfig");

  /**
   * option to display the gui
   */
  public final static Option GUI =
      new CmdLineParser.Option.BooleanOption('g', "gui");

  /**
   * option to display the help
   */
  public final static Option HELP =
      new CmdLineParser.Option.BooleanOption('h', "help");

  /**
   * option to use an external configuration file
   */
  public final static Option ITQL_CONFIG =
      new CmdLineParser.Option.StringOption('i', "itqlconfig");

  /**
   * option to use an external logging configuration file
   */
  public final static Option LOG_CONFIG =
      new CmdLineParser.Option.StringOption('l', "logconfig");

  /**
   * option to turn off default pre- and post-loading scripts
   */
  public final static Option NO_LOAD =
      new CmdLineParser.Option.BooleanOption('n', "noload");

  /**
   * option to change the post-loading script
   */
  public final static Option POST_SCRIPT =
      new CmdLineParser.Option.StringOption('o', "postload");

  /**
   * option to change the pre-loading script
   */
  public final static Option PRE_SCRIPT =
      new CmdLineParser.Option.StringOption('p', "preload");

  /**
   * option to execute an iTQL script
   */
  public final static Option SCRIPT =
      new CmdLineParser.Option.StringOption('s', "script");
  
  
  /**
   * option to specify a remote host to query for model names
   */
  public final static Option REMOTE =
      new CmdLineParser.Option.StringOption('r', "remote");

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log = Logger.getLogger(ItqlOptionParser.class);

  //
  // members
  //

  /**
   * the command line arguments passed to the itql interpreter
   */
  private String[] args = null;

  /**
   * flag indicating whether we've parsed the options yet
   */
  private boolean optionsParsed = false;

  //
  // Constructors
  //

  /**
   * Creates a new iTQL command line option parser to parse the command line
   * <code>args</code> given.
   *
   * @param args the command line arguments
   */
  public ItqlOptionParser(String[] args) {

    // call the superclass constructor
    super();

    // validate args parameter
    if (args == null) {

      throw new IllegalArgumentException("Null \"args\" parameter");
    }

    // end if
    // log that we've created a parser
    log.debug("Created option parser for iTQL interpreter");

    // set the member
    this.setArgs(args);

    // add the options
    this.addOption(HELP);
    this.addOption(GUI);
    this.addOption(NO_LOAD);
    this.addOption(POST_SCRIPT);
    this.addOption(PRE_SCRIPT);
    this.addOption(LOG_CONFIG);
    this.addOption(SCRIPT);
    this.addOption(REMOTE);
  }

  // ItqlOptionParser()
  //
  // Public API
  //

  /**
   * Parses the command line arguments given to this parser.
   *
   * @throws UnknownOptionException if an unknown option was specified in the
   *      list of options given to the parser
   * @throws IllegalOptionValueException if an option given to the parser
   *      contains an illegal value
   */
  public void parse() throws UnknownOptionException,
      IllegalOptionValueException {

    // log that
    log.debug("Parsing iTQL interpreter arguments");

    // parse the arguments if we haven't done so already
    if (!this.optionsParsed) {

      // parse the arguments
      this.parse(this.getArgs());

      // fail if there are any remaing that we dodn't know about
      String[] remainingArgs = this.getRemainingArgs();

      if (remainingArgs.length > 0) {

        // throw a new exception to indicate that there were unkown arguments
        throw new UnknownOptionException(remainingArgs[0]);
      }

      // end if
      // we've now parser the options
      this.optionsParsed = true;
    }

    // end if
  }

  // parse()

  /**
   * Validates the command line options.
   *
   * @return true if the options are valid
   */
  public boolean optionsValid() {

    boolean optionsValid = false;

    try {

      // validate params by parsing the arguments
      this.parse();
      optionsValid = true;
    }
    catch (UnknownOptionException uoe) {

      optionsValid = false;
    }
    catch (IllegalOptionValueException iove) {

      optionsValid = false;
    }

    // try-catch
    // return whether the options are valid
    return optionsValid;
  }

  // optionsValid()

  /*
   CmdLineParser.Option[] allOptions =
    new CmdLineParser.Option[] { MyOptionsParser.VERBOSE,
                                MyOptionsParser.NAME,
                                MyOptionsParser.SIZE };
   for ( int j = 0; j<allOptions.length; ++j ) {
    System.out.println(allOptions[j].longForm() + ": " +
                      myOptions.getOptionValue(allOptions[j]));
   }
   String[] otherArgs = myOptions.getRemainingArgs();
   System.out.println("remaining args: ");
   for ( int i = 0; i<otherArgs.length; ++i ) {
    System.out.println(otherArgs[i]);
   }
   */

  //
  // Internal methods
  //

  /**
   * Sets the command line arguments passed to the itql interpreter.
   *
   * @param args The new Args value
   */
  private void setArgs(String[] args) {

    this.args = args;
  }

  // setArgs()

  /**
   * Returns the command line arguments passed to the itql interpreter.
   *
   * @return the command line arguments passed to the itql interpreter
   */
  private String[] getArgs() {

    return this.args;
  }

  // getArgs()
}
