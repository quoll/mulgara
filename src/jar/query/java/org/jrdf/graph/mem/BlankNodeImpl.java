/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph.mem;

// JRDF objects

import org.jrdf.graph.AbstractBlankNode;
import org.jrdf.graph.BlankNode;

/**
 * RDF blank node. Note that blank nodes are deliberately devoid of external indentifying
 * attributes.
 *
 * Blank nodes, can either be subjects or objects.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 *
 * @version $Revision: 624 $
 */
public class BlankNodeImpl extends AbstractBlankNode implements MemNode {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 1573129076314000518L;

  /** The internal identifier for this node. */
  private Long id;

  /** Globally Unique Identifier. */
  private String uid;

  private String stringValue;

  /**
   * The constructor for this node.  Package scope so that only NodeFactory and
   * static methods can call it.
   *
   * @param newId The id to be used for this node.
   * @param newUid String Globally Unique Identifier for external communication.
   */
  BlankNodeImpl(Long newId, String newUid) {
    id = newId;
    uid = newUid;
    stringValue = uid + "#" + id;
  }

  /**
   * Retrieves an internal identifier for this node.
   *
   * @return A numeric identifier for thisa node.
   */
  public Long getId() {
    return id;
  }

  /**
   * Retrieves a Globally Unique Identifier for this node.
   *
   * @return A global String identifier for this node.
   */
  private String getUID() {
    return uid;
  }

  /**
   * While the internal structure of a BlankNode is not defined equality between
   * two nodes should be able to be determined.
   *
   * @param obj the reference object with which to compare.
   * @return true if this object is the same as the obj argument; false otherwise.
   */
  public boolean equals(Object obj) {

    // Check equal by reference
    if (this == obj) {
      return true;
    }

    // Check for null and ensure exactly the same class - not subclass.
    if (obj == null || getClass() != obj.getClass()) {
      return super.equals(obj);
    }

    // Cast and check for equality by value. (same class)
    try {
      BlankNodeImpl tmpNode = (BlankNodeImpl) obj;
      return tmpNode.getId().equals(id) && tmpNode.getUID().equals(uid);
    }
    catch (ClassCastException cce) {
      return false;
    }
  }

  /** @see org.jrdf.graph.AbstractBlankNode#hashCode() */
  public int hashCode() {
    return id.hashCode() ^ uid.hashCode();
  }

  /**
   * Returns the String value of this BlankNode as:
   * uid#id (eg. 29fbf7ba364f1425dda058737d764603#69)
   *
   * @return String
   */
  public String toString() {

    return uid + "#" + id;
  }

  public String getID() {

    return stringValue;
  }

  /**
   * Parses a String in the format of:
   * uid#id (eg. 29fbf7ba364f1425dda058737d764603#69) and creates a new
   * BlankNodeImpl from it.
   *
   * Should only be applied to a value previously returned by toString()
   *
   * @param nodeString String previously returned by toString()
   * @return BlankNodeImpl
   * @throws IllegalArgumentException
   */
  public static BlankNode valueOf(String nodeString) throws
      IllegalArgumentException {

    String[] split = nodeString.split("#");

    //validate
    if (null == split || 2 > split.length) {
      throw new IllegalArgumentException("String: " + nodeString + " is not " +
          "of the format: uid#id");
    }

    return new BlankNodeImpl(Long.valueOf(split[1]), split[0]);
  }

}
