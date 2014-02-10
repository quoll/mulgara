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

package org.mulgara.ant.task.rdf;

import java.io.*;
import java.net.*;

// Java
import java.util.*;

// log4j
import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

// Ant
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;

/**
 * An Ant Task to load RDF into a Mulgara database. If a log file is specified the
 * task can continue on from the last successfully loaded file in a previous
 * run. <p>
 *
 * The task has the following configuration options:
 * <ul>
 *   <li> logfile - The file to log to and keep persistence information
 *   (optional). </li>
 *   <li> modeluri - The URI of the model to load into (required). </li>
 *   <li> domainuri - The security domain of server where the model resides
 *   (optional). </li>
 *   <li> username - The username to use for the security domain (required if a
 *   domain uri is specified). </li>
 *   <li> password - The username to use for the security domain (required if a
 *   domain uri is specified). </li>
 *   <li> dir - The dir to load RDF from (required if rdfpath is not set). </li>
 *
 *   <li> rdfpath - A nested path element specifying the RDF location (required
 *   if dir is not set). </li>
 *   <li> maxerrors - The maximum number of permissable errors before aborting
 *   the load (default is 0). </li>
 *   <li> ignoreerrors - If errors should be ignored (default is false). </li>
 *
 *   <li> dropmodel - If the model should be dropped before loading (default is
 *   false). </li>
 *   <li> transactionsize - The number of documents loaded per transaction
 *   (default is 0). </li>
 *   <li> logconfig - the log4j XML config file (Optional - if none specified,
 *   console logging will be used). </li>
 * </ul>
 * </p> <p>
 *
 * Example Config: </p> <p>
 *
 * <pre>
 *&lt;rdfload
 *  logfile="/tmp/rdf-load.log"
 *  modeluri="rmi://${host.name}/server1#load-rdf-1"
 *  domainuri="rmi://${host.name}/server1"
 *  username="user"
 *  password="letmein"
 *  dropmodel="yes"
 *  maxerrors="5"
 *  ignoreerrors="false"
 *  transactionsize="100"
 *  logconfig="${conf.dir}/log4j.xml"&gt;
 *  &lt;rdfpath&gt;
 *    &lt;fileset dir="${rdf.dir}"&gt;
 *      &lt;include name="**\/*.rdf"/&gt;
  *    &lt;/fileset&gt;
  *  &lt;/rdfpath&gt;
  *&lt;/rdfload&gt;
  * </pre> </p>
  *
  * @created 2002-11-07
  *
  * @author Ben Warren
  *
  * @version $Revision: 1.8 $
  *
  * @modified $Date: 2005/01/05 04:57:32 $
  *
  * @maintenanceAuthor $Author: newmana $
  *
  * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
  *
  * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In
  *      Software Pty Ltd</a>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
public class RDFLoad extends Task {

  /** The logging catgory */
  private final static Logger logger = Logger.getLogger(RDFLoad.class);

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** The file to log to. */
  protected File logFile = null;

  /** The URI of the model to load into. */
  protected URI modelURI = null;

  /** The security domain of server where the model resides. */
  protected URI domainURI = null;

  /** The username for the security domain. */
  protected String username = null;

  /** The password for the security domain. */
  protected String password = null;

  /** The RDF files to load */
  protected Path rdfPath = null;

  /** The directory to load RDF from */
  protected File rdfDir = null;

  /** Drop the model before loading */
  protected boolean dropModel = false;

  /** The maximum number of permissable errors before aborting the load */
  protected int maxErrors = 0;

  /** Ignore errors */
  protected boolean ignoreErrors = false;

  /** The number of files to load in each transaction */
  protected int transactionSize = 0;

  /** The log4j XML config file */
  protected File logConfig = null;

  /** The ITQL interpreter */
  protected ItqlInterpreterBean interpreter = null;

  /** The load log */
  protected RDFLoadLog loadLog = null;

  /** Is logging enabled */
  private boolean loggingEnabled = false;

  /** Should credentials be used */
  private boolean presentCredentials = false;

  /** The number of files loaded */
  private int numLoaded = 0;

  /** The number of errors */
  private int numErrors = 0;

  /** The default value for <code>autcommit</code> is on */
  private boolean autocommit = true;
  
  /**
   * Set the file to log to and keep persistence information (optional).
   *
   * @param log The file to log to.
   */
  public void setLogfile(File log) {
    logFile = log;
  }

  /**
   * Set the URI of the model to load into (required).
   *
   * @param model The URI of the model to load into.
   */
  public void setModeluri(URI model) {
    modelURI = model;
  }

  /**
   * Set the security domain of server where the model resides (optional).
   *
   * @param domain The security domain of server where the model resides.
   */
  public void setDomainuri(URI domain) {
    domainURI = domain;
  }

  /**
   * Set the username to use for the security domain (required if a domain uri
   * is specified).
   *
   * @param user The username to use for the security domain.
   */
  public void setUsername(String user) {
    username = user;
  }

  /**
   * Set the password to use for the security domain (required if a domain uri
   * is specified).
   *
   * @param pass The password to use for the security domain.
   */
  public void setPassword(String pass) {
    password = pass;
  }

  /**
   * Set the dir to load RDF from (required if rdfpath is not set).
   *
   * @param dir The dir to load RDF from.
   */
  public void setDir(File dir) {
    rdfDir = dir;
  }

  /**
   * Set if the model should be dropped before loading (default is false).
   *
   * @param drop <code>true</code> if the model should be dropped before
   *      loading.
   */
  public void setDropmodel(boolean drop) {
    dropModel = drop;
  }

  /**
   * Set the maximum number of permissable errors before aborting the load
   * (default is 0)
   *
   * @param max The maximum number of permissable errors before aborting the
   *      load.
   */
  public void setMaxerrors(int max) {
    maxErrors = max;
  }

  /**
   * Set if errors should be ignored (default is false).
   *
   * @param ignore <code>true</code> if errors should be ignored.
   */
  public void setIgnoreErrors(boolean ignore) {
    ignoreErrors = ignore;
  }

  /**
   * Set the number of documents loaded per transaction (default is 0).
   *
   * @param size The number of documents loaded per transaction.
   */
  public void setTransactionsize(int size) {
    transactionSize = size;
  }

  /**
   * Set the log4j XML config file (Optional - if none specified, console
   * logging will be used).
   *
   * @param config The log4j XML config file.
   */
  public void setLogconfig(File config) {
    logConfig = config;
  }

  /**
   * Get the number of files loaded.
   *
   * @return The number of files loaded.
   */
  public int getNumLoaded() {
    return numLoaded;
  }

  /**
   * Get the number of errors that occurred.
   *
   * @return The number of errors that occurred.
   */
  public int getNumErrors() {
    return numErrors;
  }

  /**
   * Initialise the task.
   *
   * @throws BuildException on error.
   */
  public void init() throws BuildException {
    super.init();
  }

  /**
   * Creates a nested path element specifying the RDF location (required if dir
   * is not set).
   *
   * @return A path element specifying the RDF location.
   */
  public Path createRdfpath() {
    rdfPath = new Path(this.getProject());
    return rdfPath;
  }

  /**
   * Loads the RDF.
   *
   * @throws BuildException on error.
   */
  public void execute() throws BuildException {
    try {
      log("Running with this config:" + eol + this.toString(), Project.MSG_DEBUG);

      checkParams();
      setupLog();

      if (logger.isDebugEnabled()) {
        logger.debug("Running with this config:" + eol + this.toString());
      }

      interpreter = new ItqlInterpreterBean();

      presentCredentials();
      createModel();

      if (dropModel) {
        dropModel();
        createModel();
      }

      loadRDF();
    } finally {

      if (loadLog != null) loadLog.close();

      if (interpreter != null) {
        try {
          interpreter.close();
        } finally {
          interpreter = null;
        }
      }
    }
  }

  /**
   * Overrides Object.toString()
   *
   * @return A string representing this task.
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer();
    buffer.append("[RDFLoad ");
    buffer.append(": logfile = " + logFile);
    buffer.append(", modeluri = " + modelURI);
    buffer.append(", domainuri= " + domainURI);
    buffer.append(", username = " + username);
    buffer.append(", password = " + password);
    buffer.append(", dropmodel = " + dropModel);
    buffer.append(", maxerrors = " + maxErrors);
    buffer.append(", ignoreerrors = " + ignoreErrors);
    buffer.append(", transactionSize = " + ignoreErrors);
    buffer.append(", logconfig = " + logConfig);
    buffer.append(", rdfpath = " + rdfPath);
    buffer.append(", dir = " + rdfDir);
    buffer.append("]");

    return buffer.toString();
  }

  /**
   * Setup the log if required.
   *
   * @throws BuildException on error.
   */
  protected void setupLog() throws BuildException {

    // log4j logging
    if (logConfig != null) {
      try {
        DOMConfigurator.configure(logConfig.toURI().toURL());
      } catch (MalformedURLException me) {
        throw new BuildException("Could not configure log4j!", me);
      }
    } else {
      BasicConfigurator.configure();
      // Disable debug
      LogManager.getLoggerRepository().setThreshold(Level.OFF);
    }

    if (logFile != null) {
      loggingEnabled = true;

      // Don't read from existing log as model is dropped
      try {
        if (dropModel) loadLog = new RDFLoadLog(logFile, false);
        else loadLog = new RDFLoadLog(logFile, true);
      } catch (IOException ioe) {
        throw new BuildException("A problem occurred with the log file.", ioe);
      }
    }
  }

  /**
   * Get the result of a query as a string;
   *
   * @param query The query to execute.
   * @return The result of the query as a String or Exception or null if there
   *      was no result.
   * @throws BuildException if the result was of an unexpected type.
   */
  protected Object getQueryResult(String query) {

    if (logger.isDebugEnabled()) logger.debug("Executing query: " + query);

    log("Executing query: " + query, Project.MSG_DEBUG);

    List<Object> list = interpreter.executeQueryToList(query, true);

    if (list.size() > 0) {
      Object answer = list.get(0);
      try {
        if (answer instanceof String) {
          return (String) answer;
        } else if (answer instanceof Exception) {
          return answer;
        } else {
          throw new BuildException(
              "Expected a string or exception but got a result of type: " +
              answer.getClass().getName());
        }
      } finally {
        // there should be no answers in the list, but this is strictly correct
        try {
          for (int i = 0; i < list.size(); i++)
            if (list.get(i) instanceof Answer) ((Answer)list.get(i)).close();
        } catch (TuplesException qe) {
          throw new BuildException("Received bad answer from query: " + query);
        }
      }
    } else {
      return null;
    }
  }

  /**
   * Check the input parameters.
   *
   * @throws BuildException on error.
   */
  protected void checkParams() throws BuildException {

    // Only some set
    if ((domainURI != null) && (password != null) && (username != null)) {
      presentCredentials = true;
    } else if ( (domainURI != null) || (password != null) || (username != null)) {
      throw new BuildException("Either none or all of the attributes " +
                               "'domainuri' 'username' 'password' must be set.");
    }

    // Is model set?
    if (modelURI == null) {
      throw new BuildException("The modeluri attribute must be set.");
    }

    // RDF files to load
    if ((rdfDir != null) && (rdfPath != null)) {
      throw new BuildException("Only one of rdfpath or rdfdir may be set.");
    } else if ( (rdfDir == null) && (rdfPath == null)) {
      throw new BuildException("Either one of rdfpath or rdfdir must be set.");
    }
  }

  /**
   * Log in to the security domain.
   *
   * @throws BuildException on error.
   */
  protected void presentCredentials() throws BuildException {

    if (presentCredentials) {
      executeQuery("su <" + domainURI + "> " + username + " " + password +
                   " ;", "credentials", "Credential presented");
    }
  }

  /**
   * Create the model.
   *
   * @throws BuildException on error.
   */
  protected void createModel() throws BuildException {
    executeQuery("create <" + modelURI + "> ;", "create model",
                 "Successfully created graph ");
  }

  /**
   * Drop the model.
   *
   * @throws BuildException on error.
   */
  protected void dropModel() throws BuildException {
    executeQuery("drop <" + modelURI + "> ;", "drop model",
                 "Successfully dropped graph ");
  }

  /**
   * Load the RDF.
   *
   * @throws BuildException on error.
   */
  protected void loadRDF() throws BuildException {

    // TODO: This badly needs to be refactored - but it works!! :-)
    // If no path specified, then create one using the directory specified.
    if ((rdfPath == null) && (rdfDir != null)) {
      createRdfpath();

      FileSet fileSet = new FileSet();
      fileSet.setDir(rdfDir);
      rdfPath.addFileset(fileSet);
    }

    Set<String> loadSet = new HashSet<String>();
    String[] fileArray = rdfPath.list();

    // Build set of files to load
    for (int i = 0; i < fileArray.length; i++) {
      if (loggingEnabled && !loadLog.isLoaded(fileArray[i])) {
        loadSet.add(fileArray[i]);
      } else if (!loggingEnabled) {
        loadSet.add(fileArray[i]);
      }
    }

    // Sort the list - greater chance that problem files are grouped
    // and log will be easier to read.
    ArrayList<String> loadList = new ArrayList<String>(loadSet);
    Collections.sort(loadList);

    List<File> transList = new ArrayList<File>();
    int fileIndex = 0;
    numErrors = 0;
    numLoaded = 0;
    
    this.setAutoCommit(true);
    	       
    // Load the files
    while (fileIndex < loadList.size()) {

      // Turn off auto commit off if it needs to be
      if (transactionSize > 0) {
      	// if on then toggle it off for transaction support
      	if (autocommit) this.setAutoCommit(false);
      }

    	
      String filename = (String) loadList.get(fileIndex);
      File file = new File(filename);
      URI fileURI = file.toURI();

      Object result = getQueryResult("load <" + fileURI + "> into <" + modelURI + "> ;");

      if (result == null) {
        throw new BuildException("Did not get a result back from the load file query");
      } else if (result instanceof String) {
        String string = (String) result;

        if (!string.trim().startsWith("Successfully loaded ")) {

          numErrors++;

          // Keep going
          if (ignoreErrors || (numErrors <= maxErrors)) {

            log("Could not load file '" + filename + "'. The message was '" +
                string + "',  continuing...", Project.MSG_INFO);

            if (loggingEnabled) loadLog.logLoadError(file, string);

            // Remove file from load list
            loadList.remove(fileIndex);

            // Resume load from start of trans
            fileIndex -= transList.size();
                       
            transList.clear();
            
            // since an error has occured we need to
            // set autocommit is on;
            this.setAutoCommit(true);

          } else {
            // Max errors reached

            log("Could not load file '" + filename + "'. The message was '" + string + "'");

            if (loggingEnabled) loadLog.logLoadError(file, string);

            log("Maximum number of load errors (" + maxErrors + ") reached. Aborting load.", Project.MSG_INFO);

            // since an error has occured we need to
            // set autocommit is on;
            this.setAutoCommit(true);            
           
            break;
          }

        } else {
          // Successful load

          log("Successful load for: " + fileURI, Project.MSG_DEBUG);

          if (logger.isDebugEnabled()) logger.debug("Successful load for: " + fileURI);

          if (transactionSize > 0) {

            transList.add(file);

            // New transaction?
            if (transList.size() == transactionSize) {

              executeQuery("commit;", "commit", "Successfully committed transaction");

              // Log all files in transaction
              if (loggingEnabled) loadLog.logLoadedFiles(transList);

              numLoaded += transList.size();

              // New transaction
              transList.clear();
            }

          } else {
            // Not using transactions

            if (loggingEnabled) loadLog.logLoadedFile(file);

            numLoaded++;
          }
        }

        fileIndex++;

      } else {
        // Exception

        Exception ex = (Exception) result;
        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        ex.printStackTrace(pwriter);

        numErrors++;

        // Keep going
        if (ignoreErrors || (numErrors <= maxErrors)) {

          log("Could not load file '" + filename + "'. The exception was " +
              eol + swriter.toString() + eol + "  continuing...", Project.MSG_INFO);

          if (loggingEnabled) {

            loadLog.logLoadError(file, swriter.toString());
          }

          // Remove file from load list
          loadList.remove(fileIndex);

          // Resume load from start of trans
          fileIndex -= transList.size();
          
          transList.clear();

          // since an error has occured we need to
          // set autocommit is on;
          this.setAutoCommit(true);                     

        } else {
          // Max errors reached

          log("Could not load file '" + filename + "'. The exception was " +
              eol + swriter.toString(), Project.MSG_INFO);

          if (loggingEnabled) loadLog.logLoadError(file, swriter.toString());

          log("Maximum number of load errors (" + maxErrors +
              ") reached. Aborting load.", Project.MSG_INFO);
          
          // since an error has occured we need to
          // set autocommit is on;
          this.setAutoCommit(true);
          
          break;
        }
      }
    }

    // Turn on auto commit
    if (transactionSize > 0) {

      // commit if there is a transaction and
      // autcommit if off
      if (transList.size() > 0 && !autocommit) {

        executeQuery("commit;", "commit", "Successfully committed transaction");

        // Log all files in transaction
        if (loggingEnabled) loadLog.logLoadedFiles(transList);

        numLoaded += transList.size();
      }

      this.setAutoCommit(true);
    }

    log("Loaded " + numLoaded + " files with " + numErrors + " errors.", Project.MSG_INFO);
    logger.info("Loaded " + numLoaded + " files with " + numErrors + " errors.");

    if (loggingEnabled) {
      log("Total files loaded in log is " + loadLog.getNumLoaded() + ".", Project.MSG_INFO);
      logger.info("Total files loaded in log is " + loadLog.getNumLoaded() + ".");
    }
  }

  /**
   * Get the result of a query as a string;
   *
   * @param query The query to execute.
   * @param queryName The name of the query for logging.
   * @param successMessage The message returned from the server that indicates
   *      success.
   * @throws BuildException if the query was unsuccessful.
   */
  protected void executeQuery(String query, String queryName,
                              String successMessage) {

    Object result = getQueryResult(query);

    if (result == null) {
      throw new BuildException("Did not get a result back from the " + queryName + " query");
    } else if (result instanceof String) {

      String string = (String)result;

      if (!string.trim().startsWith(successMessage)) {     	
        // an unexpected response. 
        throw new BuildException("Bad " + queryName + " query: " + string);
      } else {

        log("Query result: " + string, Project.MSG_DEBUG);

        if (logger.isDebugEnabled()) logger.debug("Query result: " + string);
      }

    } else {
      // Exception

      Exception e = (Exception) result;
      throw new BuildException("Bad " + queryName + " query: (" + query + ")", e);
    }
  }
  
  /**
   * Toggle autoCommit setting 
   * 
   * @param state value of autocommit
   */
  private void setAutoCommit( boolean state ) { 
    if (state) {
      executeQuery("set autocommit on;", "autocommit on", "Auto commit is on");
    } else {
  	  executeQuery("set autocommit off;", "autocommit off", "Auto commit is off");      
    }
    this.autocommit = state;    
  }
}
