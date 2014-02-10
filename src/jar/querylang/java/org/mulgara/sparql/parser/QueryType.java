/*
 * Copyright 2008 Fedora Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser;

import org.mulgara.sparql.parser.cst.IRIReference;
import org.mulgara.sparql.parser.cst.Node;
import org.mulgara.sparql.parser.cst.Ordering;
import org.mulgara.sparql.parser.cst.TripleList;

/**
 * The various query types present in SPARQL.
 * Each type also includes the {@link #toString(QueryStructure)} method to convert
 * a {@link QueryStructure} to an equivalent form of the query.
 *
 * @created January 25, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public enum QueryType {
  
  select {
    public String toString(QueryStructure qs) {
      StringBuffer s = new StringBuffer("SELECT ");
      if (qs.isDistinct()) s.append("DISTINCT\n");
      if (qs.isReduced()) s.append("REDUCED\n");
      appendSelection(s, qs);
      appendDataset(s, qs);
      appendWhere(s, qs);
      appendModifier(s, qs);
      return s.toString();
    }
  },

  construct {
    public String toString(QueryStructure qs) {
      StringBuffer s = new StringBuffer("CONSTRUCT\n");
      TripleList template = qs.getConstructTemplate();
      s.append("{ ");
      if (template != null) s.append(template.getImage());
      s.append("}\n");
      appendDataset(s, qs);
      appendWhere(s, qs);
      appendModifier(s, qs);
      return s.toString();
    }
  },

  describe {
    public String toString(QueryStructure qs) {
      StringBuffer s = new StringBuffer("DESCRIBE\n");
      appendSelection(s, qs);
      appendDataset(s, qs);
      appendWhere(s, qs);
      appendModifier(s, qs);
      return s.toString();
    }
  },

  ask {
    public String toString(QueryStructure qs) {
      StringBuffer s = new StringBuffer("ASK\n");
      appendDataset(s, qs);
      appendWhere(s, qs);
      return s.toString();
    }
  };

  /**
   * Converts a {@link QueryStructure} to a string that is equivalent to the original query
   * @param qs The structure to convert
   * @return A string containing a query equivalent to the one used to build qs.
   */
  public abstract String toString(QueryStructure qs);
  
  /**
   * Internal method to add the dataset clause.
   * @param s The {@link java.lang.StringBuffer} to append to.
   * @param qs The {@link QueryStructure} to read the dataset clause from.
   */
  void appendDataset(StringBuffer s, QueryStructure qs) {
    for (IRIReference f: qs.getDefaultFroms()) s.append("FROM ").append(f.getImage()).append("\n");
    for (IRIReference f: qs.getNamedFroms()) s.append("FROM NAMED ").append(f.getImage()).append("\n");
  }
  
  /**
   * Internal method to add the where clause.
   * @param s The {@link java.lang.StringBuffer} to append to.
   * @param qs The {@link QueryStructure} to read the where clause from.
   */
  void appendWhere(StringBuffer s, QueryStructure qs) {
    s.append("WHERE { ").append(qs.getWhereClause().getImage()).append(" }\n");
  }

  /**
   * Internal method to add the solution modifier clause.
   * @param s The {@link java.lang.StringBuffer} to append to.
   * @param qs The {@link QueryStructure} to read the solution modifier clause from.
   */
  void appendModifier(StringBuffer s, QueryStructure qs) {
    for (Ordering o: qs.getOrderings()) s.append("ORDER BY ").append(o.getImage()).append("\n");
    if (qs.getOffset() > 0) s.append("OFFSET ").append(qs.getOffset()).append("\n");
    if (qs.getLimit() >= 0) s.append("LIMIT ").append(qs.getLimit()).append("\n");
  }
  
  /**
   * Internal method to add the query selection.
   * @param s The {@link java.lang.StringBuffer} to append to.
   * @param qs The {@link QueryStructure} to read the query selection from.
   */
  void appendSelection(StringBuffer s, QueryStructure qs) {
    if (qs.isSelectAll()) s.append("*");
    else for (Node n: qs.getSelection()) s.append(n.getImage()).append(" ");
    s.append("\n");
  }
}
