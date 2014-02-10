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
package org.mulgara.content.rdfxml.writer;

// Java 2 standard packages
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.util.XMLChar;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.vocabulary.RDF;
import org.jrdf.vocabulary.RDFS;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Map that contains all namespaces for a set of Statements.
 *
 * @created 2004-02-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:03 $
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
public class NamespaceMap extends HashMap<String,String> {

  /** For serialization */
  private static final long serialVersionUID = 1161744419591660130L;

  /** Logger. This is named after the class. */
  private final static Logger logger = Logger.getLogger(NamespaceMap.class.getName());

  /** A mirror of this map (where keys and values are swapped) */
  private Map<String,String> mirror = null;
  
  /** A mapping of user-supplied namespace URI to prefix string */
  private Map<String,String> userPrefixes = null;

  /** Prefix used to abbreviate RDF Namespace */
  private static final String RDF_PREFIX = "rdf";

  /** Prefix used to abbreviate RDFS Namespace */
  private static final String RDFS_PREFIX = "rdfs";
  
  /**
   * Constructor.  Pre-populates the map with prefixes for a set of default namespaces
   * (RDF, RDFS, OWL, DC), and then populates the map with generated prefixes for all
   * unique namespaces in the statements.
   * 
   * @param statements The statements which will be parsed for namespaces.
   * @param session The session used to globalize statement URI's.
   * @throws QueryException if an error occurred reading the statements.
   */
  public NamespaceMap(Statements statements, ResolverSession session) throws QueryException {
    this(statements, session, null);
  }

  /**
   * Constructor.  Pre-populates the map with prefixes for a set of default namespaces
   * (RDF, RDFS, OWL, DC), and adds a set of user-defined initial namespace prefixes.
   * Any initial prefix mapping which attempts to redefine a default namespace or prefix, or which
   * contains a prefix that is not a valid XML NCName, will be ignored.  It then populates 
   * the map with generated prefixes for all unique namespaces in the statements that do not
   * match a default or initial namespace prefix mapping. User-supplied namespace prefixes
   * that do not appear in the statements will not appear in the RDF/XML.  The default namespace may
   * be defined by including an initial mapping for the empty prefix in the namespace map.
   *
   * @param statements The statements which will be parsed for namespaces.
   * @param session The session used to globalize statement URI's.
   * @param initialPrefixes A set of user-defined namespace prefixes.
   * @throws QueryException if an error occurred reading the statements.
   */
  public NamespaceMap(Statements statements, ResolverSession session, Map<String,URI> initialPrefixes) throws QueryException {

    mirror = new HashMap<String,String>();

    //add default namespaces
    put(RDF_PREFIX, RDF.BASE_URI.toString());
    put(RDFS_PREFIX, RDFS.BASE_URI.toString());
    put("owl", "http://www.w3.org/2002/07/owl#");
    put("dc", "http://purl.org/dc/elements/1.1/");
    
    if (initialPrefixes != null) {
      userPrefixes = validateUserPrefixes(initialPrefixes);
    }

    //read namespaces from the statements
    try {
      populate(statements, session);
    } catch (TuplesException tuplesException) {
      throw new QueryException("Could not read statements.", tuplesException);
    } catch (GlobalizeException globalException) {
      throw new QueryException("Could not globalize statements.", globalException);
    }
  }
  
  /**
   * Pre-populates the namespace mapping with a user-defined set of initial prefixes.
   * All prefixes are validated as XML NCNames, and prefixes for namespaces that have
   * already been defined will be ignored.
   * @param existingMap A mapping of prefix to namespace URI.
   */
  private Map<String,String> validateUserPrefixes(Map<String,URI> existingMap) {
    Map<String,String> mappings = new HashMap<String,String>();
    for (Map.Entry<String,URI> entry : existingMap.entrySet()) {
      String prefix = entry.getKey();
      if (prefix != null) {
        // If the value is a valid XML namespace, it will be untouched.  If it is not a namespace, the
        // namespace portion will be extracted and used.
        String namespace = toNamespaceURI(entry.getValue().toString());
        
        if (namespace != null && !mappings.containsKey(namespace) && validatePrefix(prefix)) {
          mappings.put(namespace, prefix);
        }
      }
    }
    
    return mappings;
  }
  
  /**
   * Validates a user-defined prefix. A prefix is rejected if it meets any of the following conditions:
   * <ul>
   * <li>Is not a valid NCName according to the XML specification, or empty to represent the default namespace.</li>
   * <li>Attempts to redefine one of the existing default prefixes (rdf, rdfs, owl, dc).</li>
   * <li>Begins with the sequence "ns[0-9]" as this could conflict with a generated prefix.</li>
   * </ul>
   * @param prefix The prefix to validate.
   * @return <code>true</code> if it is safe to include the prefix in the available namespace definitions.
   */
  private boolean validatePrefix(String prefix) {
    // Only accept prefixes that can be used in XML qnames.
    if (!(XMLChar.isValidNCName(prefix) || prefix.equals(""))) return false;
    
    // Don't allow existing prefixes to be redefined.
    if (containsKey(prefix)) return false;
    
    // Prefixes starting with "ns1", "ns2", etc. may conflict with generated prefixes.
    if (prefix.matches("^ns\\d")) return false;
    
    return true;
  }

  /**
   * Evaluates the statements and adds namespace mappings for all unique namespaces.
   *
   * @param statements The statements to be parsed for namespaces.
   * @param session ResolverSession Session used to globalize nodes from the statments.
   * @throws TuplesException if an error occurred iterating the statements.
   * @throws GlobalizeException if an error occurred globalizing a statement node.
   */
  private void populate(Statements statements, ResolverSession session) throws
      TuplesException, GlobalizeException {

    Statements clonedStatements = (Statements)statements.clone();

    boolean success = false;
    try {

      //last nodes to be evaluated
      long subject = -1;
      long predicate = -1;
      long object = -1;

      //current nodes
      long newSubject = -1;
      long newPredicate = -1;
      long newObject = -1;

      clonedStatements.beforeFirst();
      while (clonedStatements.next()) {

        newSubject = clonedStatements.getSubject();
        newPredicate = clonedStatements.getPredicate();
        newObject = clonedStatements.getObject();

        //evaluate nodes that have changed
        if (newSubject != subject) {
          subject = newSubject;
          evaluateAndPut(subject, session);
        }
        if (newPredicate != predicate) {
          predicate = newPredicate;
          evaluateAndPut(predicate, session);
        }
        if (newObject != object) {
          object = newObject;
          evaluateAndPut(object, session);
        }
      }
      success = true;
    } finally {
      try {
        clonedStatements.close();
      } catch (TuplesException e) {
        if (success) throw e; // This is a new exception, need to re-throw it.
        else logger.info("Suppressing exception cleaning up from failed read", e); // Log suppressed exception.
      }
    }
  }

  /**
   * Globalizes the node ID and adds it to the namespace mappings it if it is a URI.
   *
   * @param nodeID The local node ID.
   * @param session Session used to globalize the node.
   * @throws GlobalizeException if an error occurred during globalization.
   */
  protected void evaluateAndPut(long nodeID, ResolverSession session) throws GlobalizeException {

    //only URI's need namespace substitution
    Node node = session.globalize(nodeID);
    if ((node != null)
        && (node instanceof URIReference)) {

      this.addNamespaceURI(((URIReference) node).getURI());
    }
  }

  /**
   * Extracts the namespace from a URI, and adds the namespace to the mappings using a generated
   * prefix if the namespace is not already part of the mappings.
   *
   * @param uri The URI containing a namespace to add.
   */
  protected void addNamespaceURI(URI uri) {

    if (uri == null) throw new IllegalArgumentException("URI argument is null.");

    //extract namespace from URI
    String uriString = uri.toString();
    String newURI = toNamespaceURI(uriString);

    //only add namespace if it is new
    if ((newURI != null) && !containsValue(newURI)) {
      //add to namespaces
      String prefix = null;
      
      // Look for a user-defined prefix for the new namespace.
      if (userPrefixes != null) prefix = userPrefixes.get(newURI);
      
      // If no user-defined prefix exists, generate a new one.
      if (prefix == null) prefix = "ns" + size();
      
      put(prefix, newURI);
    }
  }

  /**
   * Extracts the root namespace from an URI.  The root namespace is defined here as the substring
   * extending from the start of the URI string to the final occurrence of '/', '#', or ':', or
   * the entire URI string if none of these characters occurs.
   *
   * @param uri An input URI.
   * @return The namespace of the URI, or the original URI if no namespace could be found.
   */
  private String toNamespaceURI(String uri) {

    if (uri == null) throw new IllegalArgumentException("URI argument is null.");

    //return original string by default
    String nsURI = uri;

    //work backwards until a '/', '#' or ':' is encountered
    char currentChar = 0;
    for (int i = (uri.length() - 1); i >= 0; i--) {

      currentChar = uri.charAt(i);
      if ((currentChar == '/')
          || (currentChar == '#')
          || (currentChar == ':')) {

        //copy the string up to that point and return
        nsURI = uri.substring(0, i) + currentChar;
        return nsURI;
      }
    }

    assert nsURI != null : "Extracted namespace is null";
    return nsURI;
  }

  /**
   * Returns the key used to represent RDF.baseURI:
   * (http://www.w3.org/1999/02/22-rdf-syntax-ns#).
   *
   * @return The RDF namespace prefix (always <code>rdf</code>).
   */
  public String getRDFPrefix() {
    return RDF_PREFIX;
  }

  /**
   * Substitutes part of the uri with the corresponding namespace from the map.
   * If the URI contains no local part (i.e. is a namespace itself) then the entire URI is
   * replaced with an XML entity.
   *
   * @param uri The URI to perform substitution on.
   * @throws QueryException if the URI's namespace is not present in the map.
   * @return An XML QName representation of the URI suitable for use as an XML attribute name.
   */
  public String replaceNamespace(String uri) throws QueryException {

    String newURI = null;
    String nsURI = toNamespaceURI(uri);
    String key = mirror.get(nsURI);

    if (key == null) throw new QueryException("Namespace: " + nsURI + " has not been mapped.");

    //should all or part of the URI be replaced?
    if (uri.equals(nsURI)) {

      //replace uri with entity
      newURI = "&" + key + ";";

      //this may produce invalid XML
      logger.warn("Replacing URI: " + uri + " with ENTITY: " + newURI +
          ". Namepace replacement may be invalid XML.");
    } else if (uri.startsWith(nsURI)) {
      // URI's in the default namespace get shortened to local name only.
      String prefix = key.length() > 0 ? key + ":" : key;
      //replace namespace part with prefix
      newURI = uri.replaceAll(nsURI, prefix);
    }

    assert newURI != null;
    //replace any entities
    newURI = replaceCollection(newURI);
    return newURI;
  }

  /**
   * If the URI has a fragment representing a collection (eg. Bag) item, it is
   * replaced with li.
   *
   * @param original original URI.
   * @return new URI with any necessary li.
   */
  private String replaceCollection(String original){

    //value to be returned
    String uri = original;

    //validate URI
    if (original != null) uri = original.replaceAll("_[0-9]+", "li");

    return uri;
  }


  /**
   * Overridden to allow for bi-directional mapping. Not intended to be called
   * outside this class.
   *
   * @param key The prefix string
   * @param value The namespace URI string.
   * @return The previous namespace URI associated with the prefix, or <code>null</code> if there was none.
   */
  @Override
  public String put(String key, String value) {

    mirror.put(value, key);
    return super.put(key, value);
  }

}
