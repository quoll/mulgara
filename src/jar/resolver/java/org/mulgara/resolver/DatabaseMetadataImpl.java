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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;
import java.util.Set;

// Local packages
import static org.mulgara.query.rdf.Mulgara.NAMESPACE;
import org.mulgara.resolver.spi.DatabaseMetadata;

/**
 * An implementation of DatabaseMetadata.
 *
 * @created 2003-12-01
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:23 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2003-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DatabaseMetadataImpl implements DatabaseMetadata {
  private static final URI PREALLOCATED_SUBJECT_URI = URI.create(NAMESPACE + "preallocatedNodes");
  private static final URI PREALLOCATED_PREDICATE_URI = URI.create(NAMESPACE + "preallocatedNode");
  private static final URI PREALLOCATED_MODEL_URI = URI.create(NAMESPACE + "preallocatedNodeModel");
  private long preallocatedSubjectNode = -1;
  private long preallocatedPredicateNode = -1;
  private long preallocatedModelNode = -1;

  private URI uri;
  private URI securityDomainURI;
  private URI systemModelURI;
  private URI rdfTypeURI;
  private URI systemModelTypeURI;
  private URI defaultGraphURI;

  /** The set of alternative hostnames for the current host. */
  private final Set<String> hostnameAliases;

  private long systemModelNode = -1;
  private long systemModelTypeNode = -1;
  private long rdfTypeNode = -1;
  private String serverName;


  DatabaseMetadataImpl(URI uri, Set<String> hostnameAliases, URI securityDomainURI, URI systemModelURI, URI rdfTypeURI, URI systemModelTypeURI, URI defaultGraphURI) {
    // Validate parameters.
    if (uri == null) {
      throw new IllegalArgumentException("uri null");
    }
    if (hostnameAliases == null) {
      throw new IllegalArgumentException("hostnameAliases null");
    }
    if (systemModelURI == null) {
      throw new IllegalArgumentException("systemModelURI null");
    }
    if (rdfTypeURI == null) {
      throw new IllegalArgumentException("rdfTypeURI null");
    }
    if (systemModelTypeURI == null) {
      throw new IllegalArgumentException("systemModelTypeURI null");
    }
    if (defaultGraphURI == null) {
      throw new IllegalArgumentException("defaultGraphURI null");
    }


    this.uri = uri;
    this.hostnameAliases = hostnameAliases;
    this.securityDomainURI = securityDomainURI;
    this.systemModelURI = systemModelURI;
    this.rdfTypeURI = rdfTypeURI;
    this.systemModelTypeURI = systemModelTypeURI;
    this.defaultGraphURI = defaultGraphURI;

    // Set server name from the path part of the server URI.
    serverName = getServerName(uri);
  }


  public URI getURI() {
    return uri;
  }


  public URI getSecurityDomainURI() {
    return securityDomainURI;
  }


  public URI getSystemModelURI() {
    return systemModelURI;
  }


  public URI getSystemModelTypeURI() {
    return systemModelTypeURI;
  }


  public URI getDefaultGraphURI() {
    return defaultGraphURI;
  }


  public URI getRdfTypeURI() {
    return rdfTypeURI;
  }


  public Set<String> getHostnameAliases() {
    return hostnameAliases;
  }

  public String getServerName(URI serverURI) {
    if (serverURI.getPath() != null) {
      return serverURI.getPath().replaceAll("/", "");
    }
    return null;
  }

  public String getServerName() {
    return serverName;
  }

  public long getSystemModelNode() {
    if (systemModelNode == -1) {
      throw new IllegalStateException("Requesting systemModelNode before initialisation");
    }

    return systemModelNode;
  }


  public long getSystemModelTypeNode() {
    if (systemModelTypeNode == -1) {
      throw new IllegalStateException("Requesting systemModelTypeNode before initialisation");
    }

    return systemModelTypeNode;
  }


  public long getRdfTypeNode() {
    if (rdfTypeNode == -1) {
      throw new IllegalStateException("Requesting rdfTypeNode before initialisation");
    }

    return rdfTypeNode;
  }



  public URI getPreallocationSubjectURI() {
    return PREALLOCATED_SUBJECT_URI;
  }


  public URI getPreallocationPredicateURI() {
    return PREALLOCATED_PREDICATE_URI;
  }


  public URI getPreallocationModelURI() {
    return PREALLOCATED_MODEL_URI;
  }


  public long getPreallocationSubjectNode() {
    if (preallocatedSubjectNode == -1) {
      throw new IllegalStateException("Requesting preallocatedSubjectNode before initialisation");
    }

    return preallocatedSubjectNode;
  }


  public long getPreallocationPredicateNode() {
    if (preallocatedPredicateNode == -1) {
      throw new IllegalStateException("Requesting preallocatedPredicateNode before initialisation");
    }

    return preallocatedPredicateNode;
  }


  public long getPreallocationModelNode() {
    if (preallocatedModelNode == -1) {
      throw new IllegalStateException("Requesting preallocatedModelNode before initialisation");
    }

    return preallocatedModelNode;
  }


  public void initializeSystemNodes(long systemModelNode, long rdfTypeNode, long systemModelTypeNode) {
    if (systemModelNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid systemModelNode");
    }
    if (rdfTypeNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid rdfTypeNode");
    }
    if (systemModelTypeNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid systemModelTypeNode");
    }
    if (this.systemModelNode >= 0) {
      throw new IllegalArgumentException("Attempting to set systemModelNode twice");
    }
    if (this.rdfTypeNode >= 0) {
      throw new IllegalArgumentException("Attempting to set rdfTypeNode twice");
    }
    if (this.systemModelTypeNode >= 0) {
      throw new IllegalArgumentException("Attempting to set systemModelTypeNode twice");
    }

    this.systemModelNode = systemModelNode;
    this.rdfTypeNode = rdfTypeNode;
    this.systemModelTypeNode = systemModelTypeNode;
  }


  public void initializePreallocationNodes(long preallocatedSubjectNode, long preallocatedPredicateNode, long preallocatedModelNode) {
    if (preallocatedSubjectNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid preallocatedSubjectNode");
    }
    if (preallocatedPredicateNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid preallocatedPredicateNode");
    }
    if (preallocatedModelNode < 0) {
      throw new IllegalArgumentException("Attempting to set invalid preallocatedModelNode");
    }
    if (this.preallocatedSubjectNode >= 0) {
      throw new IllegalArgumentException("Attempting to set preallocatedSubjectNode twice");
    }
    if (this.preallocatedPredicateNode >= 0) {
      throw new IllegalArgumentException("Attempting to set preallocatedPredicateNode twice");
    }
    if (this.preallocatedModelNode >= 0) {
      throw new IllegalArgumentException("Attempting to set preallocatedModelNode twice");
    }

    this.preallocatedSubjectNode = preallocatedSubjectNode;
    this.preallocatedPredicateNode = preallocatedPredicateNode;
    this.preallocatedModelNode = preallocatedModelNode;
  }
}
