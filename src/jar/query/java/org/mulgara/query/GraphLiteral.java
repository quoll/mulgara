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

package org.mulgara.query;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;

// Third party packages
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

/**
 * A leaf expression containing the statements comprising an RDF model.
 *
 * @created 2001-10-02
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GraphLiteral implements Graph {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 5132086338306266830L;

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(GraphLiteral.class);

  /**
   * The content of the model, an RDF syntax byte sequence.
   */
  private byte[] buffer;

  /**
   * The system ID of the model.
   */
  private String systemId;

  //
  // Constructors
  //

  /**
   * Construct a model from RDF syntax.
   *
   * @param source the RDF syntax source
   * @throws IllegalArgumentException if the <var>inputSource</var> is <code>null</code>
   * @throws TransformerConfigurationException EXCEPTION TO DO
   * @throws TransformerException EXCEPTION TO DO
   */
  public GraphLiteral(Source source) throws TransformerConfigurationException,
      TransformerException {

    // Validate "source" parameter
    if (source == null) {

      throw new IllegalArgumentException("Null \"source\" parameter");
    }

    // Copy from the RDF syntax source into a byte array
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TransformerFactory.newInstance().newTransformer().transform(source,
        // RDF syntax source
        new StreamResult(baos));

    // Initialize fields
    buffer = baos.toByteArray();
    systemId = source.getSystemId();
  }

  //
  // Methods implementing GraphExpression
  //

  /**
   * @return an empty {@link Set}
   */
  public Set<URI> getDatabaseURIs() {
    return Collections.emptySet();
  }

  /**
   * @return an empty {@link Set}
   */
  public Set<URI> getGraphURIs() {
    return Collections.emptySet();
  }

  //
  // API methods
  //

  /**
   * Gets the InputSource attribute of the GraphLiteral object
   *
   * @return The InputSource value
   */
  public InputSource getInputSource() {

    InputSource in = new InputSource(new ByteArrayInputStream(buffer));

    if (systemId != null) {

      in.setSystemId(systemId);
    }

    return in;
  }

  //
  // Methods of Object
  //

  /**
   * Equality is by value. The value of a literal requires that the RDF model be
   * parsed out of it and compared. We don't actually do this, so the method
   * will throw an {@link Error} if it's asked to do any serious work.
   *
   * @param object the object to test against
   * @return <code>false</code> if inequality can be proved
   * @throws Error if inequality can't be proved
   */
  public boolean equals(Object object) {

    // Check trivial cases
    if (object == null) return false;

    if (object == this) return true;

    if (!(object instanceof GraphLiteral)) return false;

    // Give up
    throw new Error("RDF literal equality not implemented");
  }

  /**
   * Added to match {@link #equals(Object)}. Throws {@link java.lang.UnsupportedOperationException}.
   */
  public int hashCode() {
    throw new UnsupportedOperationException("Cannot key a value from a graph");
  }

  /**
   * Returns just the default Object clone.
   */
  public Object clone() {

    try {

      GraphLiteral cloned = (GraphLiteral) super.clone();
      return cloned;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(
          "GraphLiteral not cloneable"
          );
    }
  }
}
