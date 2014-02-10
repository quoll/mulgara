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

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

// Third party packages
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

// Automatically generated packages (SableCC)
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;

import javax.swing.*;

/**
 * Interactive TQL session command line shell.
 *
 * @created 2001-Aug-17
 *
 * @author Simon Raboczi
 * @author Tom Adams
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlSession {

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * The prompt.
   */
  public final static String PROMPT = "iTQL> ";

  /**
   * The secondary prompt, indicating an incomplete command.
   */
  public final static String PROMPT2 = "      ";

  //
  // Constants
  //

  /**
   * the log4j configuration file path (withing the JAR file)
   */
  private final static String LOG4J_CONFIG_PATH = "log4j-itql.xml";

  /**
   * the logging category to log to
   */
  private final static Logger log = Logger.getLogger(ItqlSession.class);

  /**
   * the default path to the pre-loading script
   */
  private final static String PRE_LOADING_SCRIPT_PATH = "default-pre.itql";

  /**
   * the default path to the post-loading script
   */
  private final static String POST_LOADING_SCRIPT_PATH = "default-post.itql";

  //
  // Private state
  //

  /**
   * The graphical UI.
   */
  private static ItqlSessionUI ui;

  /**
   * The messages from the previous queries.
   */
  private List<String> messages = new ArrayList<String>();

  /**
   * The answers from the previous queries.
   */
  private List<Answer> answers = new ArrayList<Answer>();

  /**
   * The file name (URL) of the script to execute if -s is given.
   */
  private String script = null;

  //
  // Members
  //

  /**
   * the iTQL interpreter associated with this session
   */
  private final ItqlInterpreterBean itqlBean;

  /**
   * the URL of the post-loading script
   */
  private URL postLoadingScriptURL = null;

  /**
   * the URL of the pre-loading script
   */
  private URL preLoadingScriptURL = null;

  //
  // Constructors
  //

  /**
   * Creates a new ITQL session.
   *
   * @throws IOException if the logging configuration can't be read
   * @throws QueryException if a connection can't be established
   *   to the server
   */
  public ItqlSession() throws IOException, QueryException {

    // load the default logging configuration
    this.loadLoggingConfig();

    itqlBean = new ItqlInterpreterBean();
  }

  /**
   * Initiates a session using the given <code>session</code>
   *
   * @param session the interactive session to issue commands to
   * @param in the stream to read commands from
   * @param out the stream to print responses to
   */
  public static void session(ItqlSession session, InputStream in,
      PrintStream out) {

    // ui.print("@@build.label@@" + eol);

    // display the copyright notice
    ui.print("iTQL Command Line Interface" + eol +
        "Copyright (C) 2001-2004 Tucana Technologies, Inc." + eol);

    // print a help message
    ui.print(eol + "Type \"help ;\", then enter for help." + eol + eol);

    // print a prompt
    ui.print(PROMPT);
  }

  /**
   * Start an interactive TQL session.
   *
   * @param args command line parameters
   * @throws IOException EXCEPTION TO DO
   * @throws QueryException EXCEPTION TO DO
   */
  public static void main(String[] args) throws IOException, QueryException {

    // create a new session to work with
    ItqlSession itqlSession = new ItqlSession();

    try {

      // set the default pre- and post-loading scripts
      itqlSession.retrieveDefaultLoadingScripts();

      // create a parser to parse the command line options
      ItqlOptionParser optsParser = new ItqlOptionParser(args);

      // parse the
      optsParser.parse();

      // process the options
      boolean startSession = itqlSession.processOptions(optsParser);

      // log that we've processed the options
      if (log.isDebugEnabled()) {

        log.debug("Processed command line options");
      }

      // execute the pre-loading script - we need to do this after we get the
      // command line options as we can override the defaults
      itqlSession.executeLoadingScript(itqlSession.getPreLoadingScriptURL());

      if (itqlSession.getScript() != null) {

        itqlSession.executeScript(new URL(itqlSession.getScript()));

      } // end if

      // log that we've executed the pre-loading script
      if (log.isDebugEnabled()) {

        log.debug("Executed pre-loading script");
      }

      // if we can, execute this session using std in and std out
      if (startSession) {

        // Create the UI.
        JFrame app = new JFrame("iTQL Shell");
        app.setSize(640, 480);
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ui = new ItqlSessionUI(itqlSession);
        app.getContentPane().add(ui);

        if (log.isInfoEnabled()) {

          log.info("Starting iTQL interpreter");
        }

        ItqlSession.session(itqlSession, System.in, System.out);

        // Make the UI visible
        app.setVisible(true);

        if (log.isInfoEnabled()) {

          log.info("Stopping iTQL interpreter");
        }

        // log that we've executed the pre-loading script
        if (log.isDebugEnabled()) {

          log.debug("Executed post-loading script");
        }
      } else {

        // execute the post-loading script
        itqlSession.executeLoadingScript(itqlSession.getPostLoadingScriptURL());

        // Close the session and exit.
        itqlSession.close();
        System.exit(0);
      }
    } catch (ItqlOptionParser.UnknownOptionException uoe) {

      // log that the option we received was invalid
      log.warn("Invalid command line option specified: " + uoe.getOptionName());

      // let the user know about the invalid option
      System.err.println("Invalid option: " + uoe.getOptionName());

      // print the usage instructions
      itqlSession.printUsage();
    } catch (ItqlOptionParser.IllegalOptionValueException iove) {

      // format the incorrect option string
      String optionMsg =
          "-" + iove.getOption().shortForm() + ", --" +
          iove.getOption().longForm() + " = " + iove.getValue();

      // log that the option value we received was invalid
      log.warn("Invalid command line option value specified: " + optionMsg);

      // let the user know about the invalid option
      System.err.println("Invalid option value: " + optionMsg);

      // print the usage instructions
      itqlSession.printUsage();
    }
  }

  // ItqlSession()
  //
  // Methods overriding the AbstractSession superclass
  //

  /**
   * Returns a list of messages (Strings) from the execution of the last
   * command or series of commands.  Successful and unsuccessful commands will
   * have valid string objects.
   *
   * @return the message from the execution of the last command.
   */
  public List<String> getLastMessages() {
    return messages;
  }

  /**
   * Returns a list of Answers from the execution of the last command or series
   * of commands.  Failures will have null answers in place.
   */
  public List<Answer> getLastAnswers() {
    return answers;
  }

  /**
   * Returns the script to run.
   *
   * @return the script to run.
   */
  public String getScript() {
    return script;
  }

  /**
   * Executes a series of commands the given command.  Makes the result availabe
   * via getLastMessages and getLastAnswers.
   *
   * @param command the command to execute
   */
  public void executeCommand(String command) {

    // Reset answers and messages
    answers.clear();
    messages.clear();

    String message = null;
    Answer answer = null;
    String[] queries = ItqlInterpreterBean.splitQuery(command);

    for (int index = 0; index < queries.length; index++) {

      try {

        // log the command we're executing
        if (log.isDebugEnabled()) {

          log.debug("Starting execution of command \"" + command + "\"");
        }

        // execute the command
        answer = itqlBean.executeQuery(queries[index]);

        // close the session if requested
        if (itqlBean.isQuitRequested()) {

          close();
        }

        message = itqlBean.getLastMessage();

        // log that we've executed the command
        if (log.isDebugEnabled()) {

          log.debug("Completed execution of commmand \"" + command + "\"");
        }
      } catch (UnsupportedOperationException uoe) {

        // Known exception get the last message.
        message = itqlBean.getLastMessage();
        log.warn("Couldn't execute command", uoe);
      } catch (ItqlInterpreterException iee) {

        // Known exception get the last message.
        message = itqlBean.getLastMessage();
        log.warn("Couldn't execute command", iee);
      } catch (Exception e) {

        // Unknown exception get the exception's message.
        message = e.getMessage();
        log.error("Couldn't execute command", e);
      } catch (Error e) {

        // Unknown exception get the exception's message.
        message = e.getMessage();
        log.error("Couldn't execute command", e);
      }

      // Add the message and answer
      messages.add(message);
      answers.add(answer);
    }
  }

  /**
   * Sets the URL of the pre-loading script.
   *
   * @param preLoadingScriptURL the URL of the pre-loading script
   */
  private void setPreLoadingScriptURL(URL preLoadingScriptURL) {

    this.preLoadingScriptURL = preLoadingScriptURL;
  }

  // getPreLoadingScriptURL()

  /**
   * Sets the URL of the post-loading script.
   *
   * @param postLoadingScriptURL the URL of the post-loading script
   */
  private void setPostLoadingScriptURL(URL postLoadingScriptURL) {

    this.postLoadingScriptURL = postLoadingScriptURL;
  }

  // setInterpreter()

  /**
   * Returns the URL of the pre-loading script.
   *
   * @return the URL of the pre-loading script
   */
  private URL getPreLoadingScriptURL() {

    return this.preLoadingScriptURL;
  }

  // setPostLoadingScriptURL()

  /**
   * Returns the URL of the post-loading script.
   *
   * @return the URL of the post-loading script
   */
  private URL getPostLoadingScriptURL() {

    return this.postLoadingScriptURL;
  }

  // command()

  /**
   * Closes the session associated with this interpreter. Subclasses that
   * override this method <strong>must</strong> call <code>super.close()</code>.
   */
  @SuppressWarnings("deprecation")
  private void close() {

    // Close the session, if any
    itqlBean.setServerURI(null);
    itqlBean.close();

    // Exit program
    System.exit(0);
  }

  // main()
  //
  // Internal methods
  //

  /**
   * Retrieves the default loading scripts.
   */
  private void retrieveDefaultLoadingScripts() {

    // locate the pre-loading script
    URL preScriptURL = this.locateScript(PRE_LOADING_SCRIPT_PATH);

    if (preScriptURL != null) {

      this.setPreLoadingScriptURL(preScriptURL);
    }

    // locate the post-loading script
    URL postScriptURL = this.locateScript(POST_LOADING_SCRIPT_PATH);

    if (postScriptURL != null) {

      this.setPostLoadingScriptURL(postScriptURL);
    }
  }

  /**
   * Locates the loading script with the given path. <p>
   *
   * This locates scripts in the following order:</p>
   * <ol>
   *   <li> Current working directory;</li>
   *   <li> System classpath (if embedded in a JAR).</li>
   * </ol>
   * <p>
   *
   * Note. These could be overwritten by the command-line options <code>-o</code>
   * and <code>-p</code>. </p>
   *
   * @param scriptPath the path to the script to locate
   * @return a URL to the script, null if the script could not be found
   */
  private URL locateScript(String scriptPath) {

    URL scriptURL = null;

    // find the current directory
    String currentDirectory = System.getProperty("user.dir", ".");

    // append a "/" if we need to
    if (!currentDirectory.endsWith("/")) {

      currentDirectory += File.separator;
    }

    // end if
    // log that we're looking for scripts
    log.debug("Looking for script " + scriptPath + " in " + currentDirectory);

    // try to find the script
    File loadingScript = new File(currentDirectory + scriptPath);

    if (loadingScript.exists() && loadingScript.isFile()) {

      // log that we've found the file
      log.debug("Found loading script - " + loadingScript);

      // return the URL!!!
      try {

        scriptURL = loadingScript.toURI().toURL();
      } catch (MalformedURLException mue) {

        // log the error
        log.warn("Unable to convert loading script filename to URL - " +
            mue.getMessage());
        System.err.println("Unable to convert loading script filename " +
            "to URL - " + loadingScript);
      }

      // try-catch
    } else {

      // log that we're now looking in the classpath
      log.debug("Looking for loading script " + scriptPath + " in classpath");

      // try to obtain from the classpath
      URL loadingScriptURL = ClassLoader.getSystemResource(scriptPath);

      // set it
      if (loadingScriptURL != null) {

        log.debug("Found loading script at - " + loadingScriptURL);
        scriptURL = loadingScriptURL;
      }

      // end if
    }

    // end if
    // return the URL
    return scriptURL;
  }

  // locateScript()

  /**
   * Executes the pre-loading script.
   *
   * @param loadingScriptURL the URL of the loading (pre/post) script to execute
   */
  private void executeLoadingScript(URL loadingScriptURL) {

    // execute it
    if (loadingScriptURL != null) {

      // log that we're executing the script
      log.debug("Executing loading script " + loadingScriptURL);

      // execute the script
      this.executeScript(loadingScriptURL);
    }

    // end if
  }

  // executeLoadingScript()

  /**
   * Processes the command line options passed to the interpreter.
   *
   * @param parser the command line option parser to use to parse the command
   *      line options
   * @return RETURNED VALUE TO DO
   */
  private boolean processOptions(ItqlOptionParser parser) {

    // log that we're processing command line options
    log.debug("Processing command line options");

    // flag to indicate whether we can start the interpreter
    boolean startInterpreter = true;

    try {

      // find out if the user wants help
      if (parser.getOptionValue(ItqlOptionParser.HELP) != null) {

        // print the help
        this.printUsage();

        // don't start the interpreter
        startInterpreter = false;
      } else {

        // dump the interpreter configuration
        Object dumpConfig = parser.getOptionValue(ItqlOptionParser.DUMP_CONFIG);

        if (dumpConfig != null) {

          this.dumpConfig();
        }

        // end if
        // load an external interpreter configuration file
        Object itqlConf = parser.getOptionValue(ItqlOptionParser.ITQL_CONFIG);

        if (itqlConf != null) {

          this.loadItqlConfig(new URL( (String) itqlConf));
        }

        // end if
        // load an external logging configuration file
        Object logConf = parser.getOptionValue(ItqlOptionParser.LOG_CONFIG);

        if (logConf != null) {

          this.loadLoggingConfig(new URL( (String) logConf));
        }

        // end if
        // find out whether to execute pre-and post loading scripts
        Object defaultLoadingScripts =
            parser.getOptionValue(ItqlOptionParser.NO_LOAD);

        if (defaultLoadingScripts == null) {

          // override the default pre-loading script
          Object preScript = parser.getOptionValue(ItqlOptionParser.PRE_SCRIPT);

          if (preScript != null) {

            this.setPreLoadingScriptURL(new URL( (String) preScript));
          }

          // end if
          // override the default post-loading script
          Object postScript =
              parser.getOptionValue(ItqlOptionParser.POST_SCRIPT);

          if (postScript != null) {

            this.setPostLoadingScriptURL(new URL( (String) preScript));
          }

          // end if
        } else {

          // log that we've turned off pre- and post-loading scripts
          log.debug("Pre- and post-loading scripts disabled");

          // unset default pre- and post-loading scripts
          this.setPreLoadingScriptURL(null);
          this.setPostLoadingScriptURL(null);
        }

        // execute an iTQL script and quit
        script = (String) parser.getOptionValue(ItqlOptionParser.SCRIPT);

        if (script != null) {

          startInterpreter = false;

        } // end if

      } // end if
    } catch (IOException ioe) {

      // log the error
      log.warn("Invalid URL on command line - " + ioe.getMessage());

      // print the usage
      System.err.println("Invalid URL - " + ioe.getMessage());
      this.printUsage();

      // don't start the interpreter
      startInterpreter = false;
    } catch (Exception e) {

      // log the error
      log.warn("Could not start interpreter - " + e.getMessage());

      // let the user know
      System.err.println("Error - " + e.getMessage());

      // don't start the interpreter
      startInterpreter = false;
    }

    // try-catch
    // return the continue flag
    return startInterpreter;
  }

  // processOptions()

  /**
   * Prints the usage instructions for the interpreter.
   */
  private void printUsage() {

    // build the usage message
    StringBuffer usage = new StringBuffer();
    usage.append("Usage: java -jar <jarfile> ");

    //usage.append("[-d|");
    //usage.append("-g|");
    //usage.append("-h] ");
    usage.append("[-h|");
    usage.append("-n] ");

    //usage.append("[-i <url>] ");
    usage.append("[-l <url>] ");
    usage.append("[-o <url>] ");
    usage.append("[-p <url>] ");
    usage.append("[-s <url>]");
    usage.append(eol);
    usage.append(eol);

    //usage.append("-d, --dumpconfig    dump the interpreter configuration to " +
    //    "the current directory\n");
    //usage.append("-g, --gui           display the iTQL GUI  ");
    usage.append("-h, --help          display this help screen" + eol);
    usage.append("-n, --noload        do not execute pre- and post-loading " +
        "scripts (useful with -s)" + eol);

    //usage.append("-i, --itqlconfig    use an external configuration file\n");
    usage.append("-l, --logconfig     use an external logging configuration " +
        "file" + eol);
    usage.append("-o, --postload      execute an iTQL script after " +
        "interpreter stops," + eol);
    usage.append("                    overriding default post-loading script" +
        eol);
    usage.append("-p, --preload       execute an iTQL script before " +
        "interpreter starts," + eol);
    usage.append("                    overriding default pre-loading script" +
        eol);
    usage.append("-s, --script        execute an iTQL script and quit" + eol);
    usage.append(eol);
    usage.append("The intepreter executes default pre- and post-loading " +
        "scripts. These can be" + eol);
    usage.append("used to load aliases etc. into the interpreter to simplify " +
        "commands. The" + eol);
    usage.append("default scripts are contained within the JAR file, " +
        "however you can overide" + eol);
    usage.append("these by placing files named default-pre.itql and " +
        "default-post.itql in" + eol);
    usage.append("the directory from which you run the interpreter, or by " +
        "using the -p and" + eol);
    usage.append("-o options." + eol);

    // print the usage
    System.out.println(usage.toString());
  }

  // printUsage()

  /**
   * Dunps the current interpreter configuration to the current directory. This
   * will dump the entire interpreter configuration including the logging and
   * application logging.
   *
   */
  private void dumpConfig() {

    // we don't support this feature yet
    throw new UnsupportedOperationException();
  }

  // dumpConfig()

  /**
   * Loads an external iTQL interpreter configuration file. This will use the
   * configuration in the file located at <code>itqlConfURL</code>, instead of
   * the configuration contained within the distribution JAR file.
   *
   * @param itqlConfURL the URL of the external iTQL interpreter configuration
   *      file
   * @return RETURNED VALUE TO DO
   */
  private boolean loadItqlConfig(URL itqlConfURL) {

    // we don't support this feature yet
    throw new UnsupportedOperationException();
  }

  // loadItqlConfig()

  /**
   * Executes a script.
   *
   * @param scriptURL the URL of the script to load
   */
  private void executeScript(URL scriptURL) {

    // log that we're executing the script
    log.debug("Executing script from " + scriptURL);

    // keep a record of the line number
    int line = 0;

    try {

      // create a reader to read the contents of the script
      BufferedReader scriptIn =
          new BufferedReader(new InputStreamReader(scriptURL.openStream(),
          "UTF-8"));

      // execute the script!
      String command = scriptIn.readLine();

      while (command != null) {

        // increment the line number
        line++;

        if (!command.equals("")) {

          // execute the command
          Answer answer = itqlBean.executeQuery(command);

          // print the results
          if (answer != null) {
            answer.beforeFirst();

            if (answer.isUnconstrained()) {
              System.out.println("[ true ]");
            } else {
              while (answer.next()) {
                System.out.print("[ ");
                for (int index = 0; index < answer.getNumberOfVariables();
                    index++) {
                  System.out.print(String.valueOf(answer.getObject(index)));
                  if (index < (answer.getNumberOfVariables() - 1)) {
                    System.out.print(", ");
                  }
                }
                System.out.println(" ]");
              }
            }
          }

          if (answer != null) {

            answer.close();
          }

          String lastMessage = itqlBean.getLastMessage();
          if ((lastMessage != null) && (!lastMessage.equals("")) && (ui != null)) {

            System.out.println(lastMessage);
          }

        } // end if

        // get the next command
        command = scriptIn.readLine();

      } // end if
    } catch (ItqlInterpreterException pe) {

      // let the user know the problem
      System.err.println("Syntax error (line " + line + "): " +
          pe.getMessage());
      log.warn("Unable to execute script - " + scriptURL + " - " + pe);
    } catch (TuplesException te) {

      // let the user know the problem
      System.err.println("Syntax error (line " + line + "): " +
          te.getMessage());
      log.warn("Unable to execute script - " + scriptURL + " - " + te);
    } catch (IOException ioe) {

      // let the user know the problem
      System.err.println("Could not execute script - " + ioe);
      log.warn("Unable to execute script - " + scriptURL + " - " + ioe);

    } // try-catch

  } // executeScript()

  /**
   * Loads an external XML log4j configuration file. This will use the
   * configuration in the file located at <code>logConfURL</code>, instead of
   * the configuration contained within the distribution JAR file.
   *
   * @param logConfURL the URL of the external XML log4j configuration file
   * @throws Exception if unable to complete the method sucessfully
   */
  private void loadLoggingConfig(URL logConfURL) throws Exception {

    // configure the logging service
    DOMConfigurator.configure(logConfURL);
    log.info("Using new logging configuration from " + logConfURL);
  }

  // loadLoggingConfig()

  /**
   * Loads the embedded logging configuration (from the JAR file).
   *
   */
  private void loadLoggingConfig() {

    // get a URL from the classloader for the logging configuration
    URL log4jConfigURL = ClassLoader.getSystemResource(LOG4J_CONFIG_PATH);

    // if we didn't get a URL, tell the user that something went wrong
    if (log4jConfigURL == null) {

      System.err.println("Unable to find logging configuration file in JAR " +
          "with " + LOG4J_CONFIG_PATH + ", reverting to default configuration.");
      BasicConfigurator.configure();
    } else {

      try {

        // configure the logging service
        DOMConfigurator.configure(log4jConfigURL);
        log.info("Using logging configuration from " + log4jConfigURL);
      } catch (FactoryConfigurationError e) {

        System.err.println("Unable to configure logging service");
      }

      // try-catch
    }

    // end if
  }

  // getPostLoadingScriptURL()
}
