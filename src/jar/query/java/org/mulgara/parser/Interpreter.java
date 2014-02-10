package org.mulgara.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


import org.mulgara.query.operation.Command;
import org.mulgara.query.Query;


/**
 * This interface defines the namespaces used while interpreting RDF code.
 * @author pag
 *
 */
public interface Interpreter {

  //
  // Constants
  //
  /** The rdf namespace prefix. */
  public static final String RDF = "rdf";

  /** The rdfs namespace prefix. */
  public static final String RDFS = "rdfs";

  /** The owl namespace prefix. */
  public static final String OWL = "owl";

  /** The mulgara namespace prefix. */
  public static final String MULGARA = "mulgara";

  /** The krule namespace prefix. */
  public static final String KRULE = "krule";

  /** The Dublin Core namespace prefix. */
  public static final String DC = "dc";

  /** The Simple Knowledge Organization System namespace prefix. */
  public static final String SKOS = "skos";

  /** The Friend Of A Friend namespace prefix. */
  public static final String FOAF = "foaf";

  /** The URI of the rdf namespace. */
  public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /** The URI of the rdfs namespace. */
  public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";

  /** The URI of the owl namespace. */
  public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";

  /** The URI of the mulgara namespace. */
  public static final String MULGARA_NS = "http://mulgara.org/mulgara#";

  /** The URI of the krule namespace. */
  public static final String KRULE_NS = "http://mulgara.org/owl/krule/#";

  /** The URI of the dc namespace. */
  public static final String DC_NS = "http://purl.org/dc/elements/1.1/";

  /** The URI of the skos namespace. */
  public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";

  /** The URI of the foaf namespace. */
  public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

  /**
   * Parses the given TQL command.
   * @param command the command to parse in TQL syntax
   * @return An AST for the command
   * @throws MulgaraParserException if the syntax of the command is incorrect
   * @throws MulgaraLexerException if the syntax of the command is incorrect
   * @throws IOException if the <var>command</var> cannot be parsed
   * @throws IllegalArgumentException if the <var>command</var> is <code>null</code>
   */
  public Command parseCommand(String command) throws MulgaraParserException, MulgaraLexerException, IllegalArgumentException, IOException;
  
  /**
   * Parses the given TQL command.
   * @param command the command to parse in TQL syntax
   * @return A {@link List} of ASTs, one for each command
   * @throws MulgaraParserException if the syntax of the command is incorrect
   * @throws MulgaraLexerException if the syntax of the command is incorrect
   * @throws IOException if the <var>command</var> cannot be parsed
   * @throws IllegalArgumentException if the <var>command</var> is <code>null</code>
   */
  public List<Command> parseCommands(String command) throws MulgaraParserException, MulgaraLexerException, IOException, IllegalArgumentException;
  
  /**
   * Parse a string into a {@link Query}. Convenience method over parseCommand.
   * @param queryString a string containing an ITQL query
   * @return the corresponding {@link Query} instance
   * @throws IOException if <var>queryString</var> can't be buffered.
   * @throws MulgaraLexerException if <var>queryString</var> can't be tokenized.
   * @throws MulgaraParserException if <var>queryString</var> is not syntactic.
   */
  public Query parseQuery(String queryString) throws IOException, MulgaraLexerException, MulgaraParserException;

  /**
   * Sets the default graph to use when one is not specified. May not be supported by the protocol
   * in which case it should be ignored.
   * @param graph A string with the URI of the graph to use by default.
   * @return The current interpreter object. This is returned to facilitate chaining of commands.
   * @throws URISyntaxException If the <var>graph</var> could not be interpreted as a valid URI.
   */
  public Interpreter setDefaultGraphUri(String graph) throws URISyntaxException;

  /**
   * Sets the default graph to use when one is not specified. May not be supported by the protocol
   * in which case it should be ignored.
   * @param graph A string with the URI of the graph to use by default.
   * @return The current interpreter object. This is returned to facilitate chaining of commands.
   */
  public Interpreter setDefaultGraphUri(URI graph);
}