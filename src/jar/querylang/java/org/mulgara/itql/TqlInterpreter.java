/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.itql;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

// Third party packages

import org.apache.log4j.Logger; // Apache Log4J
import org.jrdf.graph.*; // JRDF

// Locally written packages

// Automatically generated packages (SableCC)
import org.mulgara.itql.analysis.*;
import org.mulgara.itql.lexer.*;
import org.mulgara.itql.node.*;
import org.mulgara.itql.parser.*;
import org.mulgara.query.operation.AddAlias;
import org.mulgara.query.operation.ApplyRules;
import org.mulgara.query.operation.Backup;
import org.mulgara.query.operation.Command;
import org.mulgara.query.operation.Commit;
import org.mulgara.query.operation.CreateGraph;
import org.mulgara.query.operation.Deletion;
import org.mulgara.query.operation.DropGraph;
import org.mulgara.query.operation.ExecuteScript;
import org.mulgara.query.operation.Export;
import org.mulgara.query.operation.Help;
import org.mulgara.query.operation.Insertion;
import org.mulgara.query.operation.ListAlias;
import org.mulgara.query.operation.Load;
import org.mulgara.query.operation.Modification;
import org.mulgara.query.operation.Quit;
import org.mulgara.query.operation.Restore;
import org.mulgara.query.operation.Rollback;
import org.mulgara.query.operation.SetAutoCommit;
import org.mulgara.query.operation.SetTime;
import org.mulgara.query.operation.SetUser;
import org.mulgara.parser.Interpreter;
import org.mulgara.parser.MulgaraLexerException;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.server.Session;
import org.mulgara.util.ServerInfoRef;
import org.mulgara.util.URIUtil;


/**
 * Interactive TQL (ITQL) command interpreter.
 * Performs parsing and converting TQL requests to query objects for execution;
 * Based on ItqlInterpreter.
 * 
 * <em>This class is non-reentrant. Parsing should be serialized, or else use a new TqlInterpreters
 * for each thread.</em>
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy;2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TqlInterpreter extends DepthFirstAdapter implements SableCCInterpreter, Aliasing {

  /** The logger */
  static final Logger logger = Logger.getLogger(TqlInterpreter.class.getName());

  static {
    // force initialization of static, unsynchronized variables inside these classes
    new Parser(new Lexer2());
  }

  /** A constraint expression builder. */
  private ConstraintExpressionBuilder builder = new ConstraintExpressionBuilder(this);

  /** Variable factory for this interpreter. */
  private VariableFactory variableFactory = new VariableFactoryImpl();

  /** Lexer... */
  Lexer2 lexer = new Lexer2();

  //
  // Members
  //

  /** The map from targets to aliases */
  private Map<String,URI> aliasMap = null;

  /** The log file to record all iTQL requests */
  private PrintWriter itqlLog = null;

  /** The location of the log iTQL file */
  private String itqlLogFile = null;

  /** The command for the callbacks to fill, while parseCommand is running */
  Command lastCommand = null;
  
  /** The last exception or error, to be filled in during the callback operations. */
  Throwable lastError = null;

  //
  // Interpreter options
  //


  /** The next anonymous variable suffix. */
  private int anonSuffix = 0;

  //
  // Constructors
  //

  /**
   * Creates a new ITQL command interpreter, using the default alias set.
   */
  public TqlInterpreter() {
    this(getDefaultAliases());
  }


  /**
   * Creates a new ITQL command interpreter.
   *
   * @param aliasMap the map from targets to aliases, never <code>null</code>
   */
  public TqlInterpreter(Map<String,URI> aliasMap) {

    // validate aliasMap parameter
    if (aliasMap == null) throw new IllegalArgumentException("Null \"alias\" parameter");

    // set members
    setAliasMap(aliasMap);

    // log the creation of this interpreter
    if (logger.isDebugEnabled()) {
      logger.debug("Itql interpreter created");
    }

    // is this session configured for logging.
    if (System.getProperty("itql.command.log") != null) {
      itqlLogFile = System.getProperty("itql.command.log");
      logger.info("iTQL command logging has been enabled.  Logging to " + System.getProperty("itql.command.log"));
    }
  }

  /**   
   * Set up default aliases.
   *
   * @return A map of aliases to their fully qualified names
   */        
  public static Map<String,URI> getDefaultAliases() {
    Map<String,URI> aliases = new HashMap<String,URI>();
    aliases.put(RDF, URI.create(RDF_NS));
    aliases.put(RDFS, URI.create(RDFS_NS));
    aliases.put(OWL, URI.create(OWL_NS));
    aliases.put(MULGARA, URI.create(MULGARA_NS));
    aliases.put(KRULE, URI.create(KRULE_NS));
    aliases.put(DC, URI.create(DC_NS));
    aliases.put(SKOS, URI.create(SKOS_NS));
    aliases.put(FOAF, URI.create(FOAF_NS));
    return aliases;
  }  


  //
  // Public API
  //

  /**
   * Parses the given TQL command.
   *
   * @param command the command to parse in TQL syntax
   * @return An AST for the command
   * @throws MulgaraParserException if the syntax of the command is incorrect
   * @throws LexerException if the syntax of the command is incorrect
   * @throws IOException if the <var>command</var> cannot be parsed
   * @throws IllegalArgumentException if the <var>command</var> is <code>null</code>
   */
  public Command parseCommand(String command) throws MulgaraParserException, MulgaraLexerException, IOException {

    // validate command parameter
    if ((command == null) || command.equals("")) {
      throw new IllegalArgumentException("Null \"command\" parameter");
    }

    // log that we're going to execute the command
    if (logger.isDebugEnabled()) logger.debug("Parsing command " + command);

    resetInterpreter();

    // log the iTQL command - system property itql.command.log must be set

    // log the command abd push it into the lexer
    this.logItql(command);
    try {
      lexer.add(command);
    } catch (LexerException le) {
      flush();
      throw new MulgaraLexerException(le.getMessage(), le);
    }
    // test that this is a single command
    if (lexer.getCommandCount() > 1) logger.warn("Multiple commands given to parser");

    try {
      // if the lexer saw terminators, parse the associated commands
      if (lexer.nextCommand()) {
  
        Start commandTree = null;
        String commandText = lexer.getCurrentCommand();
  
        // parse the command
        Parser parser = new Parser(lexer);
        commandTree = parser.parse();

        // Build the command. This populates lastCommand
        commandTree.apply(this);
        lastCommand.setText(commandText);

        if (logger.isDebugEnabled()) logger.debug("Successfully parsed command " + command);
      }
    } catch (LexerException le) {
      throw new MulgaraLexerException(le);
    } catch (ParserException pe) {
      throw new MulgaraParserException(pe);
    } finally {
      flush();
    }
    
    return lastCommand;
  }


  /**
   * Parses the given TQL command.
   *
   * @param command the command to parse in TQL syntax
   * @return A {@link List} of ASTs, one for each command
   * @throws MulgaraParserException if the syntax of the command is incorrect
   * @throws LexerException if the syntax of the command is incorrect
   * @throws IOException if the <var>command</var> cannot be paersed
   * @throws IllegalArgumentException if the <var>command</var> is <code>null</code>
   */
  public List<Command> parseCommands(String command) throws MulgaraParserException, MulgaraLexerException, IOException {

    // validate command parameter
    if ((command == null) || command.equals("")) {
      throw new IllegalArgumentException("Null \"command\" parameter");
    }

    // log that we're going to execute the command
    if (logger.isDebugEnabled()) logger.debug("Parsing command " + command);

    // log the iTQL command - system property itql.command.log must be set
    this.logItql(command);

    // clean up command list
    command = command.trim();
    if (!command.endsWith(";")) command = command + ";";

    // Reset the variable incrementer in the query.
    variableFactory.reset();

    // push the command into the lexer
    try {
      lexer.add(command);
    } catch (LexerException le) {
      flush();
      throw new MulgaraLexerException(le);
    }

    // create a list of AST versions of the command
    List<Command> commandList = new LinkedList<Command>();

    // if the lexer saw terminators, parse the associated commands
    while (lexer.nextCommand()) {

      String commandText = lexer.getCurrentCommand();

      Start commandTree = null;

      // parse the command
      try {
        Parser parser = new Parser(lexer);
        commandTree = parser.parse();

        // build the command
        // this populates lastCommand
        resetInterpreter();
        commandTree.apply(this);
        if (lastCommand != null) {
          lastCommand.setText(commandText);
          
          // take the lastCommand result, and add it to the list of results
          commandList.add(lastCommand);
  
          if (logger.isDebugEnabled()) logger.debug("Successfully parsed command: " + command);
        } else {
          if (logger.isDebugEnabled()) logger.debug("Null parse: " + command);
        }

      } catch (ParserException e) {
        flush();
        throw new MulgaraParserException(e);
      } catch (LexerException e) {
        flush();
        throw new MulgaraLexerException(e);
      } catch (Error e) {
        flush();
        throw e;
      }

    }
    return commandList;
  }


  /**
   * Parse a string into a {@link Query}.  Convenience method over parseCommand.
   *
   * @param queryString a string containing an ITQL query
   * @return the corresponding {@link Query} instance
   * @throws IOException if <var>queryString</var> can't be buffered.
   * @throws LexerException if <var>queryString</var> can't be tokenized.
   * @throws MulgaraParserException if <var>queryString</var> is not syntactic.
   */
  public Query parseQuery(String queryString) throws IOException,
      MulgaraLexerException, MulgaraParserException {

    if (queryString == null) throw new IllegalArgumentException("Null \"queryString\" parameter");

    // clean up query
    queryString = queryString.trim();
    while (queryString.endsWith(";")) {
      queryString = queryString.substring(0, queryString.length() - 1);
    }

    // log that we're going to execute the command
    if (logger.isDebugEnabled()) logger.debug("Parsing query \"" + queryString + "\"");

    // parse the command via double dispatch
    Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(queryString), 256)));
    resetInterpreter();
    try {
      parser.parse().apply(this);
    } catch (LexerException le) {
      throw new MulgaraLexerException(le);
    } catch (ParserException pe) {
      throw new MulgaraParserException(pe);
    }

    // should now have the command parsed into lastCommand.
    // check the the command worked as expected
    if (lastCommand == null) throw new MulgaraParserException("Parameter was not a query");
    if (!(lastCommand instanceof Query)) throw new IllegalArgumentException("Command was not a query: " + queryString);
    // This may not be accurate if there was more than one query, but if the lexer stopped short, we're stuck here.
    lastCommand.setText(queryString);

    // return the results of the command
    return (Query)lastCommand;
  }


  //
  // Methods overridden from DepthFirstAdapter
  // Provides callback mechanism for SableCC
  //

  /**
   * Displays help information to the user.
   *
   * @param node the help command
   */
  public void outAHelpCommand(AHelpCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing help command " + node);

    lastCommand = new Help(HelpPrinter.getHelp(node.getCommandPrefix()));
  }


  /**
   * Quits a session.
   *
   * @param node the quit command
   */
  public void outAQuitCommand(AQuitCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing quit command " + node);

    lastCommand = new Quit();
  }


  /**
   * Commits a transaction.
   *
   * @param node the commit command
   */
  public void outACommitCommand(ACommitCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing commit command " + node);

    lastCommand = new Commit();
  }


  /**
   * Rolls back a transaction.
   *
   * @param node the rollback command
   */
  public void outARollbackCommand(ARollbackCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing rollback command " + node);
    lastCommand = new Rollback();
  }


  /**
   * Creates a query.
   *
   * @param node the query command
   */
  public void outASelectCommand(ASelectCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing select command " + node);

    resetInterpreter();

    // build the query
    try {
      lastCommand = this.buildQuery(node.getQuery());
    } catch (QueryException qe) {
      logger.warn("Couldn't answer query", qe);
      lastError = qe;
    } catch (URISyntaxException use) {
      logger.warn("Invalid resource URI. " + use.getMessage());
      lastError = use;
    }
  }


  /**
   * Substitutes the user associated with this session.
   *
   * @param node the su command
   */
  public void outASuCommand(ASuCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing su command " + node);

    lastCommand = new SetUser(node.getUser().getText(), node.getPassword().getText(), toURI(node.getResource()));
  }


  /**
   * Associates an alias prefix with a target.
   *
   * @param node the alias command
   */
  public void outAAliasCommand(AAliasCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing alias command " + node);

    // get the prefix and target
    String aliasPrefix = node.getPrefix().getText();
    String aliasTarget = node.getTarget().getText();

    try {

      // convert the target to a URI
      URI aliasTargetURI = new URI(aliasTarget);

      // log the conversion
      if (logger.isDebugEnabled()) logger.debug("Converted " + aliasTarget + " to URI " + aliasTargetURI);

      // add the alias pair to the map
      this.addAliasPair(aliasPrefix, aliasTargetURI);

      // log that we've added the pair to the map
      if (logger.isDebugEnabled()) logger.debug("Aliased " + aliasTarget + " as " + aliasPrefix);

      // Return an AST element, for reporting on what happened.
      lastCommand = new AddAlias(aliasPrefix, aliasTarget);

    } catch (URISyntaxException use) {
      // log the failed URI creation
      logger.warn("Unable to create URI from alias target " + aliasTarget);
    }
  }


  /**
   * Requests a list of current aliases.
   *
   * @param node the alias command
   */
  public void outAAliaslCommand(AAliaslCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing alias list command " + node);

    // Return an AST element, for reporting on what happened.
    // Use a Help command, with the alias listing as the help text.
    lastCommand = new ListAlias(Collections.unmodifiableMap(aliasMap));
  }

  /**
   * Applies a set of rules in a model to data in another model.
   *
   * @param node the alias command
   */
  public void outAApplyCommand(AApplyCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing apply command " + node);

    try {
      // get the rule graph and target graph
      URI ruleGraph = toURI(node.getRules());
      PModelExpression rawModelExpression = node.getBase();
      GraphExpression baseGraph = GraphExpressionBuilder.build(aliasMap, rawModelExpression);
      
      Token dest = null;
      PDestinationClause rawDestinationClause = node.getDestination();
      if (rawDestinationClause != null) {
        dest = ((ADestinationClause)rawDestinationClause).getResource();
      }
      
      URI destGraph = null;
      if (dest == null) {
        destGraph = baseGraph instanceof GraphResource ? ((GraphResource)baseGraph).getURI() : URI.create(Mulgara.DEFAULT_GRAPH);
      } else {
        destGraph = toURI(dest);
      }

      lastCommand = new ApplyRules(ruleGraph, baseGraph, destGraph);
    } catch (QueryException qe) {
      logger.warn("Couldn't apply rules", qe);
      lastError = qe;
    } catch (URISyntaxException use) {
      logger.warn("Invalid resource URI. " + use.getMessage());
      lastError = use;
    }
  }


  /**
   * Creates a new database/model.
   *
   * @param node the create command
   */
  public void outACreateCommand(ACreateCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing create command " + node);

    // get the name of the model to create
    URI graphURI = toURI(node.getModel());

    // get the type of model to create; default to mulgara:Graph is unspecified
    URI graphTypeURI = (node.getModelType() == null)
        ? Session.MULGARA_GRAPH_URI
        : toURI(node.getModelType());

    // log that we're asking the driver to create the resource
    if (logger.isDebugEnabled()) logger.debug("Creating new graph " + graphURI);

    graphURI = getCanonicalUriAlias(graphURI);

    if (logger.isDebugEnabled()) logger.debug("Graph is alias for " + graphURI);

    lastCommand = new CreateGraph(graphURI, graphTypeURI);
  }

  /**
   * Drop (delete) a database/model.
   *
   * @param node the drop command
   */
  public void outADropCommand(ADropCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing drop command " + node);

    // get the name of the database/model to drop
    lastCommand = new DropGraph(toURI(node.getResource()));
  }

  /**
   * Load the contents of a file into a database/model.
   *
   * @param node the load command
   */
  public void outALoadCommand(ALoadCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing load command " + node);

    // get constituents of the load command
    URI sourceURI = toURI(node.getSource());
    URI destinationURI = toURI(node.getDestination());
    boolean locality = node.getLocality() != null && (node.getLocality() instanceof ALocalLocality);

    lastCommand = new Load(sourceURI, destinationURI, locality);
  }

  /**
   * Executes a TQL script.
   *
   * @param node the execute command
   */
  public void outAExecuteCommand(AExecuteCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing execute command " + node);

    // get the name of the script to execute
    String resource = node.getResource().getText();

    try {
      lastCommand = new ExecuteScript(new URL(resource), this);
    } catch (MalformedURLException mue) {
      // let the user know the problem
      logger.warn("Invalid script source URL: " + resource);
      lastError = mue;
    }
  }


  /**
   * Inserts a triple, model, database or the results of a query into a model or
   * database.
   *
   * @param node the insert command
   */
  public void outAInsertCommand(AInsertCommand node) {
    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing insert command " + node);

    // get the resource we're inserting data into
    URI graphURI = toURI(node.getResource());

    lastCommand = buildModification(graphURI, node.getTripleFactor(), true);
  }

  /**
   * Deletes a triple, model, database or the results of a query from a model or
   * database.
   *
   * @param node the delete command
   */
  public void outADeleteCommand(ADeleteCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing delete command " + node);

    // get the resource we're inserting data into
    URI graphURI = toURI(node.getResource());

    lastCommand = buildModification(graphURI, node.getTripleFactor(), false);
  }


  /**
   * Sets an interpreter property.
   *
   * @param node the set command
   */
  public void outASetCommand(ASetCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing set command " + node);

    // get the option to set
    PSetOption option = node.getSetOption();

    // log that we've got the option
    if (logger.isDebugEnabled()) logger.debug("Found option " + option);

    // get the value
    boolean optionSet = !(node.getSetOptionMode() instanceof AOffSetOptionMode);

    // set the option
    if (option instanceof ATimeSetOption) {

      if (logger.isDebugEnabled()) logger.debug("Found set time: " + (optionSet ? "on" : "off"));
      lastCommand = new SetTime(optionSet);
    } else if (option instanceof AAutocommitSetOption) {

      if (logger.isDebugEnabled()) logger.debug("Found autocommit: " + (optionSet ? "on" : "off"));
      lastCommand = new SetAutoCommit(optionSet);

    } else {
      lastError = new ItqlInterpreterException("Unknown interpreter option for \"SET\"");
    }
  }

  /**
   * Backs up the contents of a server to a local or remote file.
   *
   * @param node the backup command
   */
  @SuppressWarnings("deprecation")
  public void outABackupCommand(ABackupCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing backup command " + node);

    // get constituents of the backup command
    URI destinationURI = toURI(node.getDestination());
    boolean locality = node.getLocality() != null && (node.getLocality() instanceof ALocalLocality);

    Token src = node.getSource();
    if (src != null) {
      URI sourceURI = toURI(src);
      lastCommand = new Backup(sourceURI, destinationURI, locality);
    } else {
      lastCommand = new Backup(destinationURI, locality);
    }
  }

  /**
   * Exports the contents of a graph to a local or remote file.
   *
   * @param node the backup command
   */
  public void outAExportCommand(AExportCommand node) {
    
    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing export command " + node);

    // get constituents of the export command
    URI sourceURI = toURI(node.getSource());
    URI destinationURI = toURI(node.getDestination());
    boolean locality = node.getLocality() != null && (node.getLocality() instanceof ALocalLocality);

    Export exportCommand = new Export(sourceURI, destinationURI, locality);
    exportCommand.setNamespacePrefixes(aliasMap);
    lastCommand = exportCommand;
  }
  
  /**
   * Restores the contents of a server from a file.
   *
   * @param node the restore command
   */
  @SuppressWarnings("deprecation")
  public void outARestoreCommand(ARestoreCommand node) {

    // log the command
    if (logger.isDebugEnabled()) logger.debug("Processing restore command " + node);

    URI sourceURI = toURI(node.getSource());
    boolean locality = node.getLocality() != null && (node.getLocality() instanceof ALocalLocality);

    Token dest = node.getDestination();
    if (dest != null) {
      URI destinationURI = toURI(dest);
      lastCommand = new Restore(sourceURI, destinationURI, locality);
    } else {
      lastCommand = new Restore(sourceURI, locality);
    }
  }

  /**
   * Returns a set of statements from the iTQL query object.
   *
   * @param setOfTriples the set of statements defined in the query.
   * @param variableMap the variable map to store the value of the variable
   *   against the variable object.
   * @throws URISyntaxException if <code>tripleFactor</code> contains a query or
   *      a resource that that violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC\uFFFD2396</a>
   * @throws QueryException if an invalid node is used in the set of triples.
   * @return a set of statements from the iTQL query.
   */
  @SuppressWarnings("unchecked")
  public Set<org.jrdf.graph.Triple> getStatements(ATripleSetOfTriples setOfTriples, Map<String,VariableNodeImpl> variableMap)
      throws QueryException, URISyntaxException {
  
    List<ATriple> tripleList = (List<ATriple>)setOfTriples.getTriple();
    HashSet<org.jrdf.graph.Triple> statements = new HashSet<org.jrdf.graph.Triple>();
  
    // Check that each set of triples has the predicate bound.
    for (Iterator<ATriple> i = tripleList.iterator(); i.hasNext(); ) {
  
      // get the triple
      ATriple triple = i.next();
  
      // Convert the Subject, Predicate and Object.
      org.jrdf.graph.Node subject = toNode(triple.getSubject(), variableMap);
      org.jrdf.graph.Node predicate = toNode(triple.getPredicate(), variableMap);
      org.jrdf.graph.Node object = toNode(triple.getObject(), variableMap);
  
      // Predicate cannot be a blank node.
      if (predicate instanceof BlankNode) {
        throw new QueryException("Predicate must be a valid URI");
      }
  
      // Check that the subject or predicate node is not a literal.
      if (subject instanceof LiteralImpl ||
          predicate instanceof LiteralImpl) {
  
        // throw an exception indicating we have a bad triple
        throw new QueryException(
            "Subject or Predicate cannot be a literal");
      }
  
      // Create a new statement using the triple elements
      org.jrdf.graph.Triple jrdfTriple = new TripleImpl(
          (SubjectNode) subject, (PredicateNode) predicate,
          (ObjectNode) object);
  
      // add the statement to the statement set
      statements.add(jrdfTriple);
    }
  
    return statements;
  }


  /**
   * Creates a query from iTQL syntax and grammar.
   * Despite onyl being accessed from within this package (both locally and from
   * {@link VariableBuilder}) this method must be public, as it has to appear in
   * the {@link Interpreter} interface.
   *
   * @param rawQuery a select query, represented as either a {@link
   *      org.mulgara.itql.node.ASelectCommand} or a {@link
   *      org.mulgara.itql.node.ASelectSetOfTriples}
   * @return the answer to the query
   * @throws QueryException if the query cannot be executed
   * @throws URISyntaxException if the <code>query</code> contains a resource
   *      whose text violates <a href="http://www.isi.edu/in-notes/rfc2396.txt">
   *      RFC\uFFFD2396</a>
   */
  @SuppressWarnings("unchecked")
  public Query buildQuery(org.mulgara.itql.node.Node rawQuery) throws QueryException, URISyntaxException {
  
    // validate query parameter
    if (rawQuery == null) throw new IllegalArgumentException("Null \"rawQuery\" parameter");
  
    // create the variables.  May contain a PElement; Count; URI literal; or a sub query
    LinkedList<PElement> variables = null;
    AFromClause fromClause;
    AWhereClause whereClause;
    AOrderClause orderClause;
    AHavingClause havingClause;
    ALimitClause limitClause;
    AOffsetClause offsetClause;
    boolean distinct = true;

    // cast the correct way (we don't have a common superclass, event though we
    // have methods with the same names)
    if (rawQuery instanceof AQuery) {
  
      AQuery query = (AQuery) rawQuery;
      PSelectClause selectClause = query.getSelectClause();
      if (selectClause instanceof ANormalSelectSelectClause) {
        distinct = ((ANormalSelectSelectClause)selectClause).getNondistinct() == null;
        variables = (LinkedList<PElement>)((ANormalSelectSelectClause)selectClause).getElement();
      }
      fromClause = ((AFromClause)query.getFromClause());
      whereClause = ((AWhereClause)query.getWhereClause());
      orderClause = ((AOrderClause)query.getOrderClause());
      havingClause = ((AHavingClause)query.getHavingClause());
      limitClause = ((ALimitClause)query.getLimitClause());
      offsetClause = ((AOffsetClause)query.getOffsetClause());
    } else if (rawQuery instanceof ASelectSetOfTriples) {
  
      ASelectSetOfTriples query = (ASelectSetOfTriples) rawQuery;
      variables = new LinkedList<PElement>();
      variables.add(query.getSubject());
      variables.add(query.getPredicate());
      variables.add(query.getObject());
      fromClause = ((AFromClause)query.getFromClause());
      whereClause = ((AWhereClause)query.getWhereClause());
      orderClause = ((AOrderClause)query.getOrderClause());
      havingClause = ((AHavingClause)query.getHavingClause());
      limitClause = ((ALimitClause)query.getLimitClause());
      offsetClause = ((AOffsetClause)query.getOffsetClause());
    } else {
  
      // we only handle AQuery and ASelectSetOfTriples
      throw new IllegalArgumentException("Invalid type for \"rawQuery\" parameter");
    }
  
    if (fromClause == null) throw new QueryException("FROM clause missing.");
    if (whereClause == null) throw new QueryException("WHERE clause missing.");

    // build the variable list: collection of Variable, ConstantValue, Count, Subquery
    if (logger.isDebugEnabled()) logger.debug("Building query variable list from " + variables);
    List<SelectElement> variableList = this.buildVariableList(variables);
    if (logger.isDebugEnabled()) logger.debug("Built variable list " + variableList);
  
    // get the model expression from the parser
    PModelExpression rawModelExpression = fromClause.getModelExpression();
    if (logger.isDebugEnabled()) logger.debug("Building model expression from " + rawModelExpression);
    // parse the text into a model expression
    GraphExpression graphExpression = GraphExpressionBuilder.build(this.getAliasMap(), rawModelExpression);
    if (logger.isDebugEnabled()) logger.debug("Built model expression " + graphExpression);
  
    // get the constraint expression from the parser
    PConstraintExpression rawConstraintExpression = whereClause.getConstraintExpression();
    if (logger.isDebugEnabled()) logger.debug("Building constraint expression from " + rawConstraintExpression);
    // parse the text into a constraint expression
    ConstraintExpression constraintExpression = build(rawConstraintExpression);
    if (logger.isDebugEnabled()) logger.debug("Built constraint expression " + constraintExpression);
  
    // build the order list
    List<Order> orderList = buildOrderList(orderClause);

    // build the having clause
    ConstraintHaving havingExpression = buildHaving(havingClause);
  
  
    // build the limit and offset
    Integer limit = null;
    int offset = 0;
    try {
      if (limitClause != null) limit = new Integer(limitClause.getNumber().getText());
      if (offsetClause != null) offset = Integer.parseInt(offsetClause.getNumber().getText());
    } catch (NumberFormatException e) {
      String failedType = (limit == null) ? "limit" : "offset";
      throw new Error("Parser permitted non-integer for " + failedType, e);
    }
  
    // build a query using the information we've obtained from the parser
    return new Query(variableList, graphExpression, constraintExpression,
        havingExpression, orderList, limit, offset, distinct, new UnconstrainedAnswer());
  }


  /**
   * Despite onyl being accessed from within this package (both locally and from
   * {@link VariableBuilder}) this method must be public, as it has to appear in
   * the {@link Interpreter} interface.
   *
   * @see org.mulgara.itql.SableCCInterpreter#toLiteralImpl(PLiteral)
   */
  public LiteralImpl toLiteralImpl(PLiteral p) {
  
    ALiteral aLiteral = (ALiteral)p;
  
    // Determine the datatype URI, if present
    ADatatype type = (ADatatype)aLiteral.getDatatype();
    URI datatypeURI = (type != null) ? toURI(type.getResource()) : null;
  
    if (datatypeURI != null) {
      return new LiteralImpl(getLiteralText(aLiteral), datatypeURI);
    } else {
      // Determine the language code
      String language = toLanguageId((ALanguage)aLiteral.getLanguage());
      if (language != null) return new LiteralImpl(getLiteralText(aLiteral), language);
      // no language, so return an untyped, no language literal
      return new LiteralImpl(getLiteralText(aLiteral));
    }
  }


  /**
   * @see org.mulgara.itql.SableCCInterpreter#toURI(Token)
   */
  public URI toURI(Token token) {
  
    assert token instanceof TResource;
    return URIUtil.convertToURI(token.getText(), aliasMap);
  }


  /**
   * Called by {@link ConstraintExpressionBuilder}
   * @see org.mulgara.itql.SableCCInterpreter#nextAnonVariable()
   */
  public Variable nextAnonVariable() {
    return new Variable("av__" + this.anonSuffix++);
  }

  
  /**
   * Builds a {@link org.mulgara.query.ConstraintExpression} object from a
   * {@link org.mulgara.itql.node.PConstraintExpression}, using an <code>aliasMap</code>
   * to resolve aliases.  Uses double-dispatch into the AST for the expression.
   *
   * @param expression a constraint expression from the parser
   * @return A new constraint expression, based on the AST passed as "expression".
   * @throws QueryException if <code>rawConstraintExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawConstraintExpression</code>
   *      contains a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  public ConstraintExpression build(PConstraintExpression expression) throws
      QueryException, URISyntaxException {
  
    // validate parameters
    if (aliasMap == null) throw new IllegalArgumentException("Null \"aliasMap\" parameter");
    if (expression == null) throw new IllegalArgumentException("Null \"expression\" parameter");
  
    if (logger.isDebugEnabled()) logger.debug("Building constraint expression from " + expression);
  
    // build the contraint expression from the parser input
    expression.apply((Switch)builder);
    ConstraintExpression constraintExpression = builder.getConstraintExpression();
    if (logger.isDebugEnabled()) logger.debug("Successfully built constraint expression from " + expression);
  
    // return the new constraint expression
    return constraintExpression;
  }


  /**
   * Returns the text of the given <code>literal</code>.
   *
   * @param literal the literal to retrieve the text from
   * @return The LiteralText value
   */
  @SuppressWarnings("unchecked")
  public static String getLiteralText(ALiteral literal) {
  
    // validate the literal parameter
    if (literal == null) throw new IllegalArgumentException("Null \"literal\" " + "parameter");
  
    // the text of the literal
    StringBuffer literalText = new StringBuffer();
  
    // get all the strands in this literal
    List<PStrand> strands = (List<PStrand>)literal.getStrand();
  
    // add each strand together to make the literal text
    for (PStrand strand: strands) {
      
      // add the strand to the literal text
      if (strand instanceof AUnescapedStrand) {
        literalText.append(((AUnescapedStrand)strand).getText().getText());
      } else if (strand instanceof AEscapedStrand) {
        literalText.append(((AEscapedStrand)strand).getEscapedtext().getText());
      }
    }

    return literalText.toString();
  }


  /**
   * Sets the alias map associated with this interpreter.
   *
   * @param aliasMap the alias map associated with this interpreter
   */
  public void setAliasMap(Map<String,URI> aliasMap) {
    this.aliasMap = aliasMap;
  }


  /**
   * Returns the alias map associated with this session.
   *
   * @return the alias namespace map associated with this session
   */
  public Map<String,URI> getAliasMap() {
    return aliasMap;
  }


  /** @see org.mulgara.parser.Interpreter#setDefaultGraphUri(java.lang.String) */
  public Interpreter setDefaultGraphUri(String graph) throws URISyntaxException {
    return this;
  }


  /** @see org.mulgara.parser.Interpreter#setDefaultGraphUri(java.net.URI) */
  public Interpreter setDefaultGraphUri(URI graph) {
    return this;
  }


  /**
   * Builds a list of {@link org.mulgara.query.Variable}s from a list of
   * {@link org.mulgara.itql.node.PVariable}s. Note. Variables in both the
   * <code>rawVariableList</code> and the returned list will <strong>not
   * </strong> contain the variable prefix <code>$</code> in their name.
   *
   * @param rawVariableList a list of {@link
   *      org.mulgara.itql.node.PVariable}s from the parser
   * @return a list of {@link org.mulgara.query.Variable}s, suitable for use
   *      in creating a {@link org.mulgara.query.Query}
   * @throws QueryException if the <code>rawVariableList</code> cannot be parsed
   *      into a list of {@link org.mulgara.query.Variable}s
   */
  List<SelectElement> buildVariableList(LinkedList<PElement> rawVariableList) throws
      QueryException, URISyntaxException {
  
    // Empty variable list.
    if (rawVariableList == null) return Collections.emptyList();
  
    // validate rawVariableList parameter
    if (rawVariableList.size() == 0) throw new IllegalArgumentException("Empty \"rawVariableList\" parameter");
  
    // Construct the required builder
    VariableBuilder variableBuilder = new VariableBuilder(this, variableFactory);
  
    // end if
    // log that we're building the variable list
    if (logger.isDebugEnabled()) logger.debug("Building variable list from " + rawVariableList);
  
    // copy each variable from the query into the list
    for (PElement element: rawVariableList) element.apply((Switch)variableBuilder);
  
    // Get the variable list
    List<SelectElement> variableList = variableBuilder.getVariableList();
  
    // make sure that we return a list with something in it
    if (variableList.size() == 0) {
      throw new QueryException("No variables parseable from query");
    }
  
    // log that we've successfully built the variable list
    if (logger.isDebugEnabled()) {
      logger.debug("Built variable list " + variableList);
    }
  
    // return the list
    return variableList;
  }

  /**
   * Builds a list of {@link org.mulgara.query.Variable}s from a list of
   * {@link org.mulgara.itql.node.POrderElement}s. Note. Variables in both
   * the <code>rawVariableList</code> and the returned list will <strong>not
   * </strong> contain the variable prefix <code>$</code> in their name.
   *
   * @param orderClause The SableCC list of elements to order by.
   * @return a list of {@link org.mulgara.query.Variable}s, suitable for use
   *      in creating a {@link org.mulgara.query.Query}, or an empty list if
   *      there are no elements to be ordered by.
   * @throws QueryException if the <code>rawOrderElementList</code> cannot be
   *      parsed into a list of {@link org.mulgara.query.Variable}s
   */
  @SuppressWarnings("unchecked")
  List<Order> buildOrderList(AOrderClause orderClause) throws QueryException {

    // short circuit for an empty clause 
    if (orderClause == null) return (List<Order>)Collections.EMPTY_LIST;
    
    // get the list of elements in the clause
    LinkedList<AOrderElement> rawOrderList = (LinkedList<AOrderElement>)orderClause.getOrderElement();
    
    assert rawOrderList != null && !rawOrderList.isEmpty();
  
    if (logger.isDebugEnabled()) logger.debug("Building order list from " + rawOrderList);
  
    // create a list for the parsed variables
    List<Order> orderList = new ArrayList<Order>(rawOrderList.size());
  
    // copy each variable from the query into the list
    for (AOrderElement order: rawOrderList) {

      // get the name of this variable
      String variableName = ((AVariable)order.getVariable()).getIdentifier().getText();
  
      if (logger.isDebugEnabled()) logger.debug("Found variable $" + variableName);
  
      // Figure out which way to order, ascending or descending
      boolean ascending;
      PDirection direction = order.getDirection();
  
      if (direction == null) {
        ascending = true;
      } else if (direction instanceof AAscendingDirection) {
        ascending = true;
      } else if (direction instanceof ADescendingDirection) {
        ascending = false;
      } else {
        throw new Error("Unknown direction field in order");
      }
  
      // add a new ordered variable to the list
      orderList.add(new Order(new Variable(variableName), ascending));
    }
  
    // make sure that we return a list with something in it
    if (orderList.size() == 0) throw new QueryException("No variables parseable from query");
  
    if (logger.isDebugEnabled()) logger.debug("Built order list " + orderList);
    return orderList;
  }


  /**
   * Builds a HAVING compliant {@link org.mulgara.query.ConstraintExpression} object from a
   * {@link org.mulgara.itql.node.PConstraintExpression}, using an <code>aliasMap</code>
   * to resolve aliases.  To comply with a HAVING clause the predicate must be one of:
   * mulgara:occurs mulgara:occursLessThan mulgara:occursMoreThan.
   *
   * @param havingClause a constraint expression from the parser
   * @return A new ConstraintExpression representing the HAVING condition
   * @throws QueryException if <code>rawConstraintExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawConstraintExpression</code>
   *      contains a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  ConstraintHaving buildHaving(AHavingClause havingClause) throws QueryException, URISyntaxException {
  
    // short circuit if there is no having clause
    if (havingClause == null) return null;
    
    // get the constraint expression from the parser
    PConstraintExpression rawHavingExpression = havingClause.getConstraintExpression();
    if (logger.isDebugEnabled()) logger.debug("Building constraint expression from " + rawHavingExpression);
  
    ConstraintExpression hExpr = build(rawHavingExpression);
  
    // do some gramatical checking on the clause
    if (hExpr instanceof ConstraintOperation) throw new QueryException("Having currently supports only one constraint");
    if (!checkHavingPredicates(hExpr)) throw new QueryException("Only \"occurs\" predicates can be used in a Having clause");
  
    return (ConstraintHaving)hExpr;
  }

  /**
   * Convert a literal's language node into a language ID
   * @param language The node containing the language node.
   * @return The 2 or 5 character identifier, or <code>null</code> if no code available.
   * @throws QueryException if the ID of the language is malformed.
   */
  private static String toLanguageId(ALanguage language) {
    if (language == null) return null;
    String langId = language.getLangid().getText();
    int len = langId.length();
    if (len != 2 && len != 5) {
      logger.error("Unknown form for language tag: " + langId);
      langId = null;
    }
    return langId;
  }


  /**
   * Resets the parser state in preparation for a new command.
   */
  private void resetInterpreter() {
    lastCommand = null;
    lastError = null;
    // Reset the variable incrementer in the query.
    variableFactory.reset();
  }
  
  
  /**
   * @param graphURI
   * @param tripleFactor
   */
  private Modification buildModification(URI graphURI, PTripleFactor tripleFactor, boolean asserting) {
    // get the set of triples out of the factor
    PSetOfTriples setOfTriples = null;
    if (tripleFactor instanceof ABracedTripleFactor) {
      setOfTriples = ((ABracedTripleFactor)tripleFactor).getSetOfTriples();
    } else if (tripleFactor instanceof AUnbracedTripleFactor) {
      setOfTriples = ((AUnbracedTripleFactor)tripleFactor).getSetOfTriples();
    } else throw new RuntimeException("Unhandled Grammar Exception: Unknown type of triple factor: " + tripleFactor.getClass().getName());

    try {
      // Create the correct type of modifier for the data 
      if (setOfTriples instanceof AResourceSetOfTriples) {
        // this is an insert of one model into another.
        throw new UnsupportedOperationException("No support for direct model to model insertion.");
      } else if (setOfTriples instanceof ASelectSetOfTriples) {
        // This is an INSERT/SELECT
        // build the query
        Query query = this.buildQuery((ASelectSetOfTriples)setOfTriples);
        if (logger.isDebugEnabled()) logger.debug("Insert query " + query);
        return newModifier(graphURI, query, asserting);
      } else if (setOfTriples instanceof ATripleSetOfTriples) {
        // This is an inline set of triples
        Set<Triple> statements = getStatements((ATripleSetOfTriples)setOfTriples, new HashMap<String,VariableNodeImpl>());
        return newModifier(graphURI, statements, asserting);
      }
    } catch (URISyntaxException ue) {
      logger.warn("Invalid URL in the insertion data: " + ue.getMessage());
      lastError = ue;
    } catch (QueryException qe) {
      logger.warn("Bad query for insertion: " + qe.getMessage());
      lastError = qe;
    }
    return null;
  }

  
  /**
   * Factory method to create a Modification object.
   * @param graphURI The URI of the graph to be modified.
   * @param query The query to select the data to be modified.
   * @param asserting Indicates if the data needs to be asserted (inserted)
   *        or denied (deleted).
   * @return An {@link Insertion} if asserting is <code>true</code>,
   *         otherwise a {@link Deletion}.
   */
  private Modification newModifier(URI graphURI, Query query, boolean asserting) {
    return asserting ? new Insertion(graphURI, query) : new Deletion(graphURI, query);
  }


  /**
   * Factory method to create a Modification object.
   * @param graphURI The URI of the graph to be modified.
   * @param statements A set of triples to be modified.
   * @param asserting Indicates if the data needs to be asserted (inserted)
   *        or denied (deleted).
   * @return An {@link Insertion} if asserting is <code>true</code>,
   *         otherwise a {@link Deletion}.
   */
  private Modification newModifier(URI graphURI, Set<Triple> statements, boolean asserting) {
    return asserting ? new Insertion(graphURI, statements) : new Deletion(graphURI, statements);
  }


  /**
   * Constructs a {@link org.jrdf.graph.Node} from a {@link
   * org.mulgara.itql.node.PTripleElement}.
   *
   * @param element dd
   * @param variableMap a {@link Map} of variable names (as string) to
   *   {@link VariableNodeImpl} that are used to contain all variables.
   * @return dd
   * @throws QueryException if <code>element</code> is a {@link
   *   org.mulgara.itql.node.AResourceTripleElement} whose text contains a
   *   <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified
   *   name</a> with a prefix not defined in the <code>aliasMap</code>
   * @throws URISyntaxException if <code>element</code> is a {@link
   *   org.mulgara.itql.node.AResourceTripleElement} whose text doesn't
   *   conform to <a href="http://www.isi.edu/in-notes/rfc2396.txt">
   *   RFC\uFFFD2396</a>
   */
  private org.jrdf.graph.Node toNode(PTripleElement element, Map<String,VariableNodeImpl> variableMap)
      throws QueryException, URISyntaxException {

    // validate the element parameter
    if (element == null) throw new IllegalArgumentException("Null \"element\" parameter");

    if (logger.isDebugEnabled()) logger.debug("Resolving " + element + "to a RDF node");

    // create the node
    org.jrdf.graph.Node node = null;

    // get the node
    if (element instanceof ALiteralTripleElement) {
      // create a new literal with the given text
      node = toLiteralImpl(((ALiteralTripleElement)element).getLiteral());
      
    } else if (element instanceof AResourceTripleElement) {
      // create a new resource
      node = new URIReferenceImpl(toURI(((AResourceTripleElement)element).getResource()), false);
      
    } else if (element instanceof AVariableTripleElement) {

      // get the variable
      String variableName = ((AVariable)((AVariableTripleElement)element).getVariable()).getIdentifier().getText();

      if (logger.isDebugEnabled()) logger.debug("Resolved " + element + " to variable " + variableName);

      // use a map to keep the same variable objects if they can be reused
      if (variableMap.containsKey(variableName)) {
        node = (VariableNodeImpl)variableMap.get(variableName);
      } else {
        node = new VariableNodeImpl(variableName);
        variableMap.put(variableName, (VariableNodeImpl)node);
      }
    }

    // return the node
    return node;
  }


  /**
   * Adds a name/value pair to the alias map. This method will add associate a
   * prefix for a target for subsequent commands, making commands like the
   * following possible: <PRE>
   * alias http://purl.org/dc/elements/1.1 as dc;
   * select $title where $uri dc:title $title ;
   * </PRE>
   *
   * @param aliasPrefix the alias that denotes the target
   * @param aliasTarget the target associated with the prefix
   */
  private void addAliasPair(String aliasPrefix, URI aliasTarget) {

    // validate the parameters
    if (aliasPrefix == null) throw new IllegalArgumentException("Null \"aliasPrefix\" " + "parameter");
    if (aliasTarget == null) throw new IllegalArgumentException("Null \"aliasTarget\" " + "parameter");

    // add the pair to the map
    getAliasMap().put(aliasPrefix, aliasTarget);
  }


  /**
   * Log the TQL command to a specified file
   *
   * @param command The TQL command to be validated
   */
  private void logItql(String command) {

    // Short circuit if not logging.  The constructor initialises this if
    // system property itql.command.log is set
    if (itqlLogFile == null) return;

    try {
      // open log if needed
      if (itqlLog == null) itqlLog = new PrintWriter(new FileWriter(itqlLogFile, true), true);
      // append the command to the file
      itqlLog.println(command);
    } catch (Exception ex) {
      logger.error("Unable to log itql commands", ex);
    }
  }


  /**
   * Discard any unparsed tokens.
   */
  private void flush() {
    lexer.leftoverTokenList.clear();
  }


  /**
   * Checks that all predicates in a constraint expression are valid Having predicates
   * from {@link SpecialPredicates}.
   *
   * @param e The constraint expression to check.
   * @return true if all constraints have special predicates.
   */
  private boolean checkHavingPredicates(ConstraintExpression e) {
    if (e instanceof Constraint) {
      return e instanceof ConstraintHaving;
    } else if (e instanceof ConstraintOperation) {
      // check all sub expressions
      for (ConstraintExpression expr: ((ConstraintOperation)e).getElements()) {
        if (checkHavingPredicates(expr)) return false;
      }
      // all sub expressions returned true
      return true;
    } else {
      // An unexpected type
      return false;
    }
  }

  /** Local constants list of supported protocols. */
  private static final Set<String> protocols = new HashSet<String>();
  static {
    protocols.add("rmi");
    protocols.add("soap");
  }

  /**
   * Try to recognise a uri alias, and return the canonical form instead.
   *
   * @param uri The URI being checked.
   * @return The updated URI.  May be the same as the uri parameter.
   */
  private URI getCanonicalUriAlias(URI uri) {
    // only do this for remote protocols
    if (!protocols.contains(uri.getScheme())) return uri;

    logger.debug("Checking for an alias on: " + uri);

    // extract the host name
    String host = uri.getHost();
    if (host == null) return uri;

    Set<String> hostnames = ServerInfoRef.getHostnameAliases();
    // Check with a DNS server to see if this host is recognised
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(host);
    } catch (UnknownHostException uhe) {
      // The host was unknown, so allow resolution to continue as before
      return uri;
    }
    // check the various names against known aliases and the given name
    if (
        hostnames.contains(host) ||
        hostnames.contains(addr.getHostName()) ||
        hostnames.contains(addr.getCanonicalHostName()) ||
        hostnames.contains(addr.getHostAddress())
    ) {
      // change the host name to one that is recognised
      // use the system uri to find the local host name
      URI serverURI = ServerInfoRef.getServerURI();
      if (serverURI == null) {
        return uri;
      }
      String newHost = serverURI.getHost();
      try {
        return new URI(uri.getScheme(), newHost, uri.getPath(), uri.getFragment());
      } catch (URISyntaxException e) { /* fall through */ }
    }

    // not found, so return nothing
    return uri;
  }


  private static class Lexer2 extends Lexer {
  
    int commandCount = 0;
    final LinkedList<Token> leftoverTokenList = new LinkedList<Token>();
    StringBuilder buildingCommand = new StringBuilder();
    LinkedList<String> commandQueue = new LinkedList<String>();
    String currentCommand = null;

    public Lexer2() {
      super(null);
    }
  
    public int getCommandCount() {
      return commandCount;
    }
  
    public void add(String command) throws LexerException, IOException {
      Lexer lexer = new Lexer(new PushbackReader(new StringReader(command), 256));
      Token t;
      while (!((t = lexer.next()) instanceof EOF)) {
        if (t instanceof TTerminator) {
          commandQueue.addLast(buildingCommand.toString());
          buildingCommand = new StringBuilder();
          t = new EOF();
          commandCount++;
          assert commandCount == commandQueue.size();
        } else {
          buildingCommand.append(t.getText());
        }
        leftoverTokenList.add(t);
      }
    }
  
    public Token next() throws LexerException, IOException {
      return leftoverTokenList.isEmpty() ? new EOF() : (Token) leftoverTokenList.removeFirst();
    }
  
    public Token peek() throws LexerException, IOException {
      return leftoverTokenList.isEmpty() ? new EOF() : (Token) leftoverTokenList.getFirst();
    }
  
    public boolean nextCommand() {
      if (commandCount == 0) {
        return false;
      } else {
        //assert commandCount > 0;
        commandCount--;
        currentCommand = commandQueue.remove();
        assert commandCount == commandQueue.size();
        return true;
      }
    }

    public String getCurrentCommand() {
      return currentCommand;
    }
  }

}
