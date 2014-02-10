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

package org.mulgara.resolver.distributed;

import org.mulgara.query.rdf.BlankNodeImpl;
import org.jrdf.graph.AbstractBlankNode;
import java.net.URI;

/**
 * A BlankNode that represents nodes from a foreign server.
 *
 * @created 2007-04-18
 * @author Paul Gearon
 * @version $Revision: $
 * @modified $Date: $ @maintenanceAuthor $Author: $
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
@SuppressWarnings("serial")
public class ForeignBlankNode extends AbstractBlankNode {

  /** The internal ID for the node. */
  long nodeId;
  
  /** The URI of the foreign server. */
  final URI serverUri;
  
  /** The hashcode of the foreign server. */
  final int serverHashCode;

  private String stringValue;
  
  public ForeignBlankNode(URI serverUri, BlankNodeImpl remoteNode) {
    this.nodeId = remoteNode.getNodeId();
    this.serverUri = serverUri;
    this.serverHashCode = serverUri.hashCode();
    stringValue = serverUri.toString() + ":" + nodeId;
  }


  /**
   * Provide a representation that is unique for this node.
   * @return A string containing all the unique features of the node.
   */
  public String getID() {
    return stringValue;
  }


  /**
   * Compare node for equality.
   *
   * @param obj The object to compare against.
   * @return True if the object evaluates as an equivalent blank node.
   */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    if (obj instanceof ForeignBlankNode) {
      ForeignBlankNode fbn = (ForeignBlankNode)obj;
      return (serverUri.equals(fbn.serverUri) && nodeId == fbn.nodeId);
    }
    return super.equals(obj);
  }

  /**
   * Compute a hash code based on the server URI and nodeId. Need to override the default hash code
   * because that's based on the hash code of the server URI string, and equality is determined
   * based on URI.equals which does case normalization.
   * @return the hash code.
   */
  public int hashCode() {
    return 41 * serverUri.hashCode() + (int)(nodeId ^ (nodeId >>> 32));
  }

}
