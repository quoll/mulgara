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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;

import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.resolver.relational.d2rq.ClassMapElem;
import org.mulgara.resolver.relational.d2rq.DatatypePropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.ObjectPropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.TranslationTableElem;
import org.mulgara.resolver.relational.d2rq.DatabaseElem.DBType;


public class PatternDesc extends VariableDesc {
  private enum UriEncoding { NONE, URLENCODING, URLIFY }
    
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(PatternDesc.class);

  private Set<String> tables;
  private Set<String> columns;

  private List<String> pattern; // Order is str : column : str : column : ....
  private List<UriEncoding> patternEncoding;  // Matches 1-1 with pattern list
  private Class<? extends Node> anticipClass;

  private URI datatype;
  private String lang;
  private Map<String,Integer> columnIndices;

  private TranslationTableElem ttable;
  private final DBType dbType;

  public PatternDesc(ClassMapElem cmap, DBType dbType) {
    super(cmap);
    init(cmap.uriPattern, cmap.translateWith, URIReference.class);
    this.dbType = dbType;
  }

  public PatternDesc(ObjectPropertyBridgeElem bridge, DBType dbType) {
    super(bridge);
    init(bridge.pattern, bridge.translateWith, URIReference.class);
    this.dbType = dbType;
  }

  public PatternDesc(DatatypePropertyBridgeElem bridge, DBType dbType) {
    super(bridge);
    init(bridge.pattern, bridge.translateWith, Literal.class);

    if (bridge.datatype != null) {
      try {
        datatype = new URI(bridge.datatype);
      } catch (URISyntaxException eu) {
        datatype = null;
      }
    } else if (bridge.lang != null) {
      lang = bridge.lang;
    }
    this.dbType = dbType;
  }

  private void init(String rawPattern, TranslationTableElem ttable, Class<? extends Node> anticipatedClass) {
    if (rawPattern == null) {
      throw new IllegalArgumentException("Attempt to create PatternDesc with null pattern");
    }

    this.ttable = ttable;
    anticipClass = anticipatedClass;
    String[] split = rawPattern.split("@@");
    tables =  new HashSet<String>();
    columns = new HashSet<String>();
    pattern = new ArrayList<String>();
    patternEncoding = new ArrayList<UriEncoding>();
    columnIndices = new HashMap<String,Integer>();
    boolean isColumn = true;
    for (int i = 0; i < split.length; i++) {
      isColumn = !isColumn;  // Note this makes the initial state *false*
      String patternPart = split[i];
      if (isColumn) {
        if(patternPart.endsWith("|urlify")) {
          patternPart = patternPart.substring(0, patternPart.length()-("|urlify".length()));
          patternEncoding.add(UriEncoding.URLIFY);
        } else if(patternPart.endsWith("|urlencoding")) {
          patternPart = patternPart.substring(0, patternPart.length()-("|urlencoding".length()));
          patternEncoding.add(UriEncoding.URLENCODING);
        } else { 
          patternEncoding.add(UriEncoding.NONE);
        }
        
        tables.add(RelationalResolver.parseTableFromColumn(patternPart));
        columns.add(patternPart);
      } else {
        patternEncoding.add(UriEncoding.NONE);
      }
      pattern.add(patternPart);
    }
  }

  public void assignColumnIndex(String column, int index) {
    columnIndices.put(column, new Integer(index));
  }

  public Node getNode(ResultSet resultSet) throws SQLException, TuplesException {
    StringBuffer buff = new StringBuffer();
    boolean isColumn = true;
    
    for (int patternIndex = 0; patternIndex < pattern.size(); patternIndex++) {
      String p = pattern.get(patternIndex);
      UriEncoding encoding = patternEncoding.get(patternIndex);
      
      isColumn = !isColumn;  // Note this makes the initial state *false*
      if (isColumn) {
        int index = columnIndices.get(p).intValue();
        String colValue = resultSet.getString(index + 1);
        
        // For URLIFY, replace spaces with _
        if(encoding == UriEncoding.URLIFY) {
          colValue = colValue.replace(' ', '_');
        }

        // For URLENCODING and URLIFY, apply URL encoding
        if(encoding != UriEncoding.NONE) {
          try {
            colValue = URLEncoder.encode(colValue, "UTF-8");
          } catch(UnsupportedEncodingException e) {
            // Should never happen - UTF-8 must be supported in all Java impls
            assert false : "Received unexpected encoding exception for UTF-8.";
          }
        }
        
        buff.append(colValue);
      } else {
        buff.append(p);
      }
    }

    String value;
    if (ttable != null) {
      value = (String)ttable.db2rdf.get(buff.toString());
      if (value == null) {
        value = buff.toString();
      }
    } else {
      value = buff.toString();
    }

    if (anticipClass == URIReference.class) {
      try {
        return new URIReferenceImpl(new URI(value));
      } catch (URISyntaxException eu) {
        return new LiteralImpl(value);
      }
    } else if (anticipClass == Literal.class) {
      if (datatype != null) {
        return new LiteralImpl(value, datatype);
      } else if (lang != null) {
        return new LiteralImpl(value, lang);
      } else {
        return new LiteralImpl(value);
      }
    } else {
      throw new TuplesException("Unknown expected node type: " + anticipClass);
    }
  }

  public Set<String> getTables() {
    return tables;
  }

  public Set<String> getColumns() {
    return columns;
  }

  private class EscapeContinuation extends Exception {
    private static final long serialVersionUID = -7899849917859481249L;
  }

  public String restrict(String rdfValue) {
    // Note: There are two possible approaches to this function.  The first (simpler) approach is to 
    // use concatenation within sql to assemble the pattern within the query itself.  This works, but
    // as it subverts indexing by combining all columns into a single opaque function in the query.
    // The result is that this reduces to a massive filter on a possibly enormous result-set, with
    // the ensuing performance problems.
    //       The alternative is to decompose the literal against the pattern, into a conjunction of 
    // independent terms.  The resulting query can therefore be run against indicies where available, 
    // and is amenable to optimisation by the sql engine.
    try {
      List<String> terms = new ArrayList<String>();
      String value = rdfValue;
      Iterator<String> i = pattern.iterator();
      String str = (String)i.next();
      if (!value.startsWith(str)) {
        throw new EscapeContinuation();
      }
      value = value.substring(str.length());
      String column = null;
      while (i.hasNext()) {
        str = (String)i.next();
        if (column == null) {
          column = str;
        } else {
          int index = value.indexOf(str);
          if (index == -1) {
            throw new EscapeContinuation();
          }
          terms.add(column + " = '" + value.substring(0, index - 1) + "'");
          value = value.substring(index);
          column = null;
        }
      }
      // If column != null then the final pattern term was a column, and should match the remainder
      // of the value.
      if (column != null) {
        terms.add(column + " = " + encode(column, value, dbType));
      }

      String result = RelationalQuery.toList(terms, " AND ");
      return result;
    } catch (EscapeContinuation ec) {
      return "1 = 2"; // Simple definition of 'false'
    }
  }
}
