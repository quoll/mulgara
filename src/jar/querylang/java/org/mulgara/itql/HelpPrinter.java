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

// Third party packages
import org.apache.log4j.Logger;

// Automatically generated packages (SableCC)
import org.mulgara.itql.analysis.DepthFirstAdapter;
import org.mulgara.itql.node.*;

/**
 * Prints help for ITQL commands.
 *
 * @created 2001-09-10
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/06/26 12:48:09 $ by $Author: pgearon $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class HelpPrinter {

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log = Logger.getLogger(HelpPrinter.class);

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * message to print if a command does not have a help message
   */
  private final static String NO_HELP_MESSAGE =
      eol + "No help exists for this command." + eol;

  //
  // Public API
  //

  /**
   * Returns a help message for the given <code>command</code> (prefix).
   *
   * @param command the comand (prefix) to retrieve help for
   * @return The Help value
   */
  public static String getHelp(PCommandPrefix command) {

    // log that we're about to print the help
    log.debug("Getting help for command - " + command);

    // the help message
    final StringBuffer helpMsg = new StringBuffer();

    // delegate to the correct help printing method
    command.apply(new DepthFirstAdapter() {

      // we've got an empty help command (they want basic help)
      public void caseAEmptyCommandPrefix(AEmptyCommandPrefix node) {
        helpMsg.append(printDefaultHelp());
      }

      // caseAEmptyCommandPrefix()

      /*
             // NOTE: we need to hack the states in the grammar to get help on comments
             // we've got a comment
           public void caseACommentCommandPrefix(ACommentCommandPrefix node) {
               helpMsg.append(printCommentHelp());
             } // caseACommentCommandPrefix()
       */

      // we've got an alias
      public void caseAAliasCommandPrefix(AAliasCommandPrefix node) {
        helpMsg.append(printAliasHelp());
      }

      // caseAAliasCommandPrefix()
      // we've got a create
      public void caseACreateCommandPrefix(ACreateCommandPrefix node) {
        helpMsg.append(printCreateHelp());
      }

      // caseACreateCommandPrefix()
      // we've got a delete
      public void caseADeleteCommandPrefix(ADeleteCommandPrefix node) {
        helpMsg.append(printDeleteHelp());
      }

      // caseADeleteCommandPrefix()

      /*
             // NOTE: we don't currently support the directory command
             // we've got directory
           public void caseADirectoryCommandPrefix(ADirectoryCommandPrefix node) {
               helpMsg.append(printDirectoryHelp());
             } // caseADirectoryCommandPrefix()
       */

      // we've got a drop
      public void caseADropCommandPrefix(ADropCommandPrefix node) {
        helpMsg.append(printDropHelp());
      }

      // we've got an execute
      public void caseAExecuteCommandPrefix(AExecuteCommandPrefix node) {
        helpMsg.append(printExecuteHelp());
      }

      // we've got a help
      public void caseAHelpCommandPrefix(AHelpCommandPrefix node) {
        helpMsg.append(printHelpHelp());
      }

      // we've got an insert
      public void caseAInsertCommandPrefix(AInsertCommandPrefix node) {
        helpMsg.append(printInsertHelp());
      }

      // we've got a load
      public void caseALoadCommandPrefix(ALoadCommandPrefix node) {
        helpMsg.append(printLoadHelp());
      }

      // we've got a load
      public void caseAExportCommandPrefix(AExportCommandPrefix node) {
        helpMsg.append(printExportHelp());
      }

      // we've got a quit
      public void caseAQuitCommandPrefix(AQuitCommandPrefix node) {
        helpMsg.append(printQuitHelp());
      }

      // we've got a select
      public void caseASelectCommandPrefix(ASelectCommandPrefix node) {
        helpMsg.append(printSelectHelp());
      }

      // we've got a set
      public void caseASetCommandPrefix(ASetCommandPrefix node) {
        helpMsg.append(printSetHelp());
      }

      // we've got an su
      public void caseASuCommandPrefix(ASuCommandPrefix node) {
        helpMsg.append(printSuHelp());
      }

      // we've got a backup
      public void caseABackupCommandPrefix(ABackupCommandPrefix node) {
        helpMsg.append(printBackupHelp());
      }

      // we've got a restore
      public void caseARestoreCommandPrefix(ARestoreCommandPrefix node) {
        helpMsg.append(printRestoreHelp());
      }

      // we've got a commit
      public void caseACommitCommandPrefix(ACommitCommandPrefix node) {
        helpMsg.append(printCommitHelp());
      }

      // we've got a rollback
      public void caseARollbackCommandPrefix(ARollbackCommandPrefix node) {
        helpMsg.append(printRollbackHelp());
      }

      // caseARollbackCommandPrefix()

      public void caseAApplyCommandPrefix(AApplyCommandPrefix node) {
        helpMsg.append(printApplyHelp());
      }

    });

    // make sure that we got a message back (we may have implemented a new
    // command but forgot to add a method to the visitor pattern above)
    if (helpMsg.length() == 0) {

      // append a "no help avaliable" message
      helpMsg.append(NO_HELP_MESSAGE);
    }

    // end if
    // log that we're returning help
    log.debug("Returning help for command " + command);

    // returning
    return helpMsg.toString();
  }

  // getHelp()
  //
  // Internal methods
  //

  /**
   * Print the default help message.
   *
   * @return the default help
   */
  private static StringBuffer printDefaultHelp() {

    // log the command we're printing help for
    log.debug("Printing default help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append(eol + "Valid commands are:" + eol + eol);

    //helpMsg.append("  #       comment");
    helpMsg.append("  su       authenticate a user" + eol);
    helpMsg.append("  set      set a property" + eol);
    helpMsg.append("  execute  execute an iTQL script" + eol);
    helpMsg.append("  alias    define an alias" + eol);
    helpMsg.append("  create   create a graph" + eol);
    helpMsg.append("  commit   commits a transaction" + eol);
    helpMsg.append("  drop     drop an entire resource" + eol);
    helpMsg.append("  insert   insert a set of triples" + eol);
    helpMsg.append("  delete   delete a set of triples" + eol);
    helpMsg.append("  load     load contents of a file info a graph" + eol);
    helpMsg.append("  export   export contents of a graph intoa file" + eol);
    helpMsg.append("  backup   backup the contents of a server to a file" + eol);
    helpMsg.append("  restore  restore a server from a backup file" + eol);
    helpMsg.append("  rollback rolls back a transaction" + eol);
    helpMsg.append("  select   perform a query" + eol);
    helpMsg.append("  set      sets various options" + eol);
    helpMsg.append("  apply    applies a set of rules" + eol);
    helpMsg.append("  quit     end the ITQL session" + eol);
    helpMsg.append("  help     display this help screen" + eol + eol);
    helpMsg.append("You can also get detailed help on each command:" + eol + eol);
    helpMsg.append("  $ help <command> ;" + eol + eol);
    helpMsg.append("For example, to display help on the select command:" +
        eol + eol);
    helpMsg.append("  $ help select ;" + eol + eol);
    helpMsg.append("Note. All commands must be terminated with \";\"." + eol);

    // return the help mesage
    return helpMsg;
  }

  // printDefaultHelp()

  /**
   * Print the su help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printSuHelp() {

    // log the command we're printing help for
    log.debug("Printing su help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nAuthenticates a user." + eol);
    helpMsg.append(eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> su <ldap://bar.org> fred Fo0Bar ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printSuHelp()

  /**
   * Print the set help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printSetHelp() {

    // log the command we're printing help for
    log.debug("Printing ser help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append(eol + "Sets an iTQL interpreter option." + eol);
    helpMsg.append(eol);
    helpMsg.append("The following options are supported:" + eol + eol);

    //helpMsg.append("  o echo - displays command results" + eol);
    //helpMsg.append("  o statistics - " + eol);
    //helpMsg.append("  o stoponerror - " + eol);
    helpMsg.append("  o autocommit - automatically commits after each insert" + eol);
    helpMsg.append("  o time - displays command execution time" + eol);
    helpMsg.append(eol + "For example:" + eol + eol);
    helpMsg.append("  TQL> set time on ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printSetHelp()

  /**
   * Print the execute help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printExecuteHelp() {

    // log the command we're printing help for
    log.debug("Printing execute help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nExecutes an iTQL script." + eol);
    helpMsg.append(eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> execute " +
        "<file:/usr/local/mulgara/sample/itql/loaddc.itql> ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printExecuteHelp()

  /**
   * Print the alias help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printAliasHelp() {

    // log the command we're printing help for
    log.debug("Printing alias help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nCreates an alias to a URL, graph or database." + eol);
    helpMsg.append(eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> alias <http://www.w3.org/2000/01/rdf-schema#> as " +
        "rdfs ;" + eol);
    helpMsg.append("  TQL> select $subj $obj from " +
        "<http://purl.org/dc/elements/1.1/> where $subj <rdfs:label> $obj ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printAliasHelp()

  /**
   * Print the create help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printCreateHelp() {

    // log the command we're printing help for
    log.debug("Printing create help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append(eol + "Creates a new (empty) graph." + eol);
    helpMsg.append(eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> create <rmi://localhost/foo#bar> ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printCreateHelp()

  /**
   * Print the drop help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printDropHelp() {

    // log the command we're printing help for
    log.debug("Printing drop help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append(eol + "Drops a graph." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> drop <rmi://localhost/foo#bar> ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printDropHelp()

  /**
   * Print the insert help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printInsertHelp() {

    // log the command we're printing help for
    log.debug("Printing insert help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nInserts a triple, graph, database or the results of a " +
        "query into a graph or database." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> insert (<http://purl.org/dc/elements/1.1/subject> " +
        "<http://www.w3.org/2000/01/rdf-schema#label> 'Subject') into " +
        "<rmi://localhost/foo#bar> ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printInsertHelp()

  /**
   * Print the delete help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printDeleteHelp() {

    // log the command we're printing help for
    log.debug("Printing delete help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nDeletes a triple, graph, database or the results of a " +
        "query from a graph or database." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> delete (<http://purl.org/dc/elements/1.1/subject> " +
        "<http://www.w3.org/2000/01/rdf-schema#label> 'Subject') from " +
        "<rmi://localhost/foo#bar> ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printDeleteHelp()

  /**
   * Print the load help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printLoadHelp() {

    // log the command we're printing help for
    log.debug("Printing load help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nLoads the contents of a file into a graph." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> load <http://foo.com/bar.rdf> into " +
        "<rmi://localhost/foo#bar> ;" + eol);
    helpMsg.append("  TQL> load local <file:/c:/data/foo.rdf> into " +
        "<rmi://localhost/foo#foo> ;" + eol);
    helpMsg.append("  TQL> load remote <file:/m:/files/foo.rdf> into " +
        "<rmi://localhost/foo#foo> ;" + eol);
    helpMsg.append("" + eol);
    helpMsg.append("'local' refers to the file system on the client" + eol);
    helpMsg.append("'remote' refers to the file system on the server [default]" + eol);

    // return the help mesage
    return helpMsg;
  }

  /**
   * Print the load help message.
   * @return The text for the export command.
   */
  private static StringBuffer printExportHelp() {
    // log the command we're printing help for
    log.debug("Printing export help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nExports the contents of a graph to a file." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> export <rmi://localhost/server1#foo> to local <file:/tmp/foo.rdf> ;" + eol);
    helpMsg.append("  TQL> export local <rmi://localhost/server1#foo> to local <file:/home/me/foo.rdf> ;" + eol);
    helpMsg.append("" + eol);
    helpMsg.append("'local' refers to the file system on the client" + eol);
    helpMsg.append("'remote' refers to the file system on the server [default]" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printLoadHelp()

  /**
   * Print the backup help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printBackupHelp() {

    // log the command we're printing help for
    log.debug("Printing backup help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nPerforms a backup of a server." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append(
        "  TQL> backup <rmi://localhost/server1> to remote <file:/tmp/server1backup> ;" + eol);
    helpMsg.append("" + eol);
    helpMsg.append("'local' refers to the file system on the client" + eol);
    helpMsg.append("'remote' refers to the file system on the server [default]" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printBackupHelp()

  /**
   * Print the restore help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printRestoreHelp() {

    // log the command we're printing help for
    log.debug("Printing restore help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nRestores a server from a backup file. This will " +
        "perform a union of the current contents of the server with the " +
        "contents of the file" + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> restore <rmi://localhost/server1> from local " +
        "<file:/tmp/server1backup> ;" + eol);
    helpMsg.append("  TQL> restore <rmi://localhost/server1> from remote " +
        "<file:/tmp/server1backup> ;" + eol);
    helpMsg.append("" + eol);
    helpMsg.append("'local' refers to the file system on the client" + eol);
    helpMsg.append("'remote' refers to the file system on the server [default]" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printRestoreHelp()

  /**
   * Print the select help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printSelectHelp() {

    // log the command we're printing help for
    log.debug("Printing select help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nPerforms a query on a set of triples." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> select $subj $obj from " +
        "<http://purl.org/dc/elements/1.1/> where $subj <rdfs:label> $obj ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printSelectHelp()

  /**
   * Print the commit help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printCommitHelp() {

    // log the command we're printing help for
    log.debug("Printing commit help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nCommits a transaction." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> commit ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printCommitHelp()

  /**
   * Print the rollback help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printRollbackHelp() {

    // log the command we're printing help for
    log.debug("Printing rollback help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nRolls back changes." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol + eol);
    helpMsg.append("  TQL> rollback ;" + eol);

    // return the help mesage
    return helpMsg;
  }

  // printRollbackHelp()

  /**
   * Print the apply help message.
   *
   * @return A string containing the help message for the apply command
   */
  private static StringBuffer printApplyHelp() {

    // log the command we're printing help for
    log.debug("Printing apply help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nApplies a set of rules." + eol);
    helpMsg.append("" + eol);
    helpMsg.append("For example:" + eol);
    helpMsg.append("  TQL> apply <rmi://localhost/server1#krule> to <rmi://localhost/server1#camera>;" + eol);
    helpMsg.append("This puts entailed statements into <rmi://localhost/server1#camera>" + eol);
    helpMsg.append("Or to put the entailed statements into another graph:" + eol);
    helpMsg.append("  TQL> apply <rmi://localhost/server1#krule> to <rmi://localhost/server1#camera> <rmi://localhost/server1#entailed>;" + eol);
    return helpMsg;
  }

  // printApplyHelp()

  /**
   * Print the quit help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printQuitHelp() {

    // log the command we're printing help for
    log.debug("Printing quit  help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nQuits the iTQL interpreter." + eol);

    // return the help mesage
    return helpMsg;
  }

  // printQuitHelp()

  /**
   * Print the help help message.
   *
   * @return RETURNED VALUE TO DO
   */
  private static StringBuffer printHelpHelp() {

    // log the command we're printing help for
    log.debug("Printing help help screen");

    // create the help message
    StringBuffer helpMsg = new StringBuffer();
    helpMsg.append("\nDisplays the help screen." + eol);

    // return the help mesage
    return helpMsg;
  }

  // printHelpHelp()
}
