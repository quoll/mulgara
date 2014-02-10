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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.lucene;

// Java 2 standard packages
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.transaction.xa.XAResource;

// Log4j
import org.apache.log4j.Logger;
import org.apache.lucene.util.Version;

// JRDF
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.AbstractXAResource;
import org.mulgara.resolver.spi.AbstractXAResource.RMInfo;
import org.mulgara.resolver.spi.AbstractXAResource.TxInfo;
import org.mulgara.resolver.spi.EmptyResolution;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.util.StackTrace;
import org.mulgara.util.conversion.html.HtmlToTextConverter;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-04-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LuceneResolver implements Resolver {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(LuceneResolver.class);

  /** System property name used to look up Lucene query language version. */
  public final static String LUCENE_VERSION_PROPERTY = "org.mulgara.lucene.version";
  
  /** Default Lucene query language */
  public final static Version DEFAULT_LUCENE_VERSION = Version.LUCENE_34;
  
  /** Version of the Lucene query language to use. May be set via system properties for backwards compatibility. */
  public final static Version LUCENE_VERSION;
  static {
    Version ver = DEFAULT_LUCENE_VERSION;
    String versionProp = System.getProperty(LUCENE_VERSION_PROPERTY);
    if (versionProp != null && versionProp.length() > 0) {
      try {
        ver = Version.valueOf(versionProp);
      } catch (IllegalArgumentException e) {
        logger.warn("Illegal Lucene query language version property '" + versionProp +
            "', defaulting to " + DEFAULT_LUCENE_VERSION);
      }
    }
    assert ver != null;
    LUCENE_VERSION = ver;
  }
  
  /**
   * The preallocated node identifying the type of temporary model to create
   * in the {@link #modifyModel} method.
   */
  protected final URI modelTypeURI;

  protected final ResolverSession resolverSession;

  protected final LuceneResolverFactory resolverFactory;

  protected final boolean forWrites;

  protected final XAResource xares;

  // for abort() only
  protected Collection<FullTextStringIndex> indexes;

  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param modelTypeURI     the URI of the lucene model type
   * @param resolverSession  the session this resolver is associated with
   * @param resolverFactory  the resolver-factory that created us
   * @param forWrites        whether we may be getting writes
   */
  LuceneResolver(URI modelTypeURI, ResolverSession resolverSession,
                 LuceneResolverFactory resolverFactory, boolean forWrites) {
    // Initialize fields
    this.modelTypeURI = modelTypeURI;
    this.resolverSession = resolverSession;
    this.resolverFactory = resolverFactory;
    this.forWrites = forWrites;
    this.xares = new LuceneXAResource(10, resolverFactory, this);
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Create a model by treating the <var>model</var> as the {@link java.net.URL} of an
   * RDF document and downloading it into the database.
   *
   * @param model  {@inheritDoc}.  In this case, it should be the {@link java.net.URL} of
   *   an RDF/XML document.
   * @param modelTypeURI  {@inheritDoc}.  This field is ignored, because URL
   *   models are external.
   */
  public void createModel(long model, URI modelTypeURI) throws ResolverException, LocalizeException {
    if (logger.isDebugEnabled()) {
      logger.debug("Create Lucene model " + model);
    }
  }

  public XAResource getXAResource() {
    return xares;
  }

  /**
   * Insert or delete RDF statements in a model at a URL.
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Modify URL model " + model);
    }

    try {
      FullTextStringIndex stringIndex = getFullTextStringIndex(model);

      statements.beforeFirst();
      while (statements.next()) {
        Node subjectNode = resolverSession.globalize(statements.getSubject());

        // Do not insert the triple if it contains a blank node in subject.
        if (subjectNode instanceof BlankNode) {
          if (logger.isInfoEnabled()) {
            logger.info(statements.getSubject() + " is blank node; ignoring Lucene insert.");
          }

          continue;
        }

        Node predicateNode = resolverSession.globalize(statements.getPredicate());
        Node objectNode = resolverSession.globalize(statements.getObject());

        // Get the subject's string value.
        String subject = ((URIReference) subjectNode).getURI().toString();

        // Predicates can only ever be URIReferences.
        String predicate = ((URIReference) predicateNode).getURI().toString();

        if (objectNode instanceof URIReference) {
          URIReference objectURI = (URIReference) objectNode;
          String resource = objectURI.getURI().toString();
          
          try {
            // Assert or deny the statement in the Lucene model
            if (occurs) {
              InputStream input = null;
              Reader reader = null;
              try {
                // Connect to the resource's content
                URLConnection connection = objectURI.getURI().toURL().
                    openConnection();
                String contentType = connection.getContentType();

                if (logger.isDebugEnabled()) {
                  logger.debug("Content type of resource is " + contentType);
                }

                MimeType contentMimeType;

                try {
                  contentMimeType = new MimeType(contentType);
                } catch (MimeTypeParseException e) {
                  logger.warn("\"" + contentType + "\" didn't parse as MIME type",
                      e);
                  try {
                    contentMimeType = new MimeType("content", "unknown");
                  } catch (MimeTypeParseException em) {
                    throw new ResolverException("Failed to create mime-type", em);
                  }
                }

                assert contentMimeType != null;

                // If no character encoding is specified, guess at Latin-1
                String charSet = contentMimeType.getParameter("charset");
                if (charSet == null) {
                  charSet = "ISO8859-1";
                }

                assert charSet != null;

                // Get the content, performing appropriate character encoding
                input = connection.getInputStream();
                reader = new InputStreamReader(input, charSet);

                // Add a filter if the content type is text/html, to strip out
                // HTML keywords that will clutter the index
                try {
                  if (contentMimeType.match(new MimeType("text", "html"))) {
                    reader = HtmlToTextConverter.convert(reader);
                  }
                } catch (MimeTypeParseException em) {
                  throw new ResolverException("Failed to create mime-type", em);
                }

                if (logger.isDebugEnabled()) {
                  logger.debug("Inserting " + subject + " " + predicate + " " + resource);
                }

                if (!stringIndex.add(subject, predicate, resource, reader)) {
                  logger.warn("Unable to add {" + subject + ", " + predicate + ", " +
                      resource + "} to full text string index");
                }
              } catch (MalformedURLException e) {
                logger.info(resource + " is not a URL; ignoring Lucene insert");
              } catch (IOException e) {
                throw new ResolverException("Can't obtain content of " + resource, e);
              } catch (org.mulgara.util.conversion.html.ParseException e) {
                throw new ResolverException("Couldn't parse content of " + resource, e);
              } finally {
                try {
                  if (reader != null) reader.close();
                  else if (input != null) input.close();
                } catch (IOException e) {
                  logger.warn("Ignoring error closing resource content", e);
                }
              }
            } else { // (!occurs)
              if (logger.isDebugEnabled()) {
                logger.debug("Deleting " + subject + " " + predicate + " " + resource);
              }

              if (!stringIndex.remove(subject, predicate, resource)) {
                logger.warn("Unable to remove {" + subject + ", " + predicate +
                    ", " + resource + "} from full text string index");
              }
            }
          } catch (FullTextStringIndexException e) {
            throw new ResolverException("Unable to modify full text index\n" + new StackTrace(e));
          }

        } else if (objectNode instanceof Literal) {
          Literal objectLiteral = (Literal) objectNode;
          String literal = objectLiteral.getLexicalForm();

          // Insert the statement into the text index
          try {
            if (occurs) {
              if (logger.isDebugEnabled()) {
                logger.debug("Inserting " + subject + " " + predicate + " " + literal);
              }

              if (!stringIndex.add(subject, predicate, literal)) {
                logger.warn("Unable to add {" + subject + ", " + predicate +
                    ", " + literal + "} to full text string index");
              }
            } else {
              if (logger.isDebugEnabled()) {
                logger.debug("Deleting " + subject + " " + predicate + " " + literal);
              }

              if (!stringIndex.remove(subject, predicate, literal)) {
                logger.warn("Unable to remove {" + subject + ", " + predicate +
                    ", " + literal + "} from full text string index");
              }
            }
          } catch (FullTextStringIndexException e) {
            throw new ResolverException("Unable to " + (occurs ? "add" : "delete") + "'" +
                                        literal + "' to full text string index\n" + new StackTrace(e));
          }
        } else {
          if (logger.isInfoEnabled()) {
            logger.info(objectNode + " is blank node; ignoring Lucene insert.");
          }
        }
      }
    } catch (TuplesException et) {
      throw new ResolverException("Error fetching statements", et);
    } catch (GlobalizeException eg) {
      throw new ResolverException("Error localizing statements", eg);
    } catch (IOException ioe) {
      throw new ResolverException("Failed to open string index", ioe);
    } catch (FullTextStringIndexException ef) {
      throw new ResolverException("Error in string index\n" + new StackTrace(ef));
    }
  }

  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Removing full-text model " + model);
    }

    try {
      getFullTextStringIndex(model).removeAll();
    } catch (IOException ioe) {
      throw new ResolverException("Failed to open string index", ioe);
    } catch (FullTextStringIndexException ef) {
      throw new ResolverException("Query failed against string index\n" + new StackTrace(ef));
    }
  }

  /**
   * Resolve a constraint against an RDF/XML document.
   *
   * Resolution is by filtration of a URL stream, and thus very slow.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolve " + constraint);
    }

    // check the model
    ConstraintElement modelElement = constraint.getModel();
    if (modelElement instanceof Variable) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring solutions for " + constraint);
      return new EmptyResolution(constraint, false);
    } else if (!(modelElement instanceof LocalNode)) {
      throw new QueryException("Failed to localize Lucene Graph before resolution " + constraint);
    }

    /* temporary hack because $_from is not resolved before transformation occurs, and hence
     * no LuceneConstraint's are created when doing ... from <lucene-model> where ... .
     */
    if (!(constraint instanceof LuceneConstraint)) {
      constraint = new LuceneConstraint(constraint);
    }

    // generate the tuples
    try {
      FullTextStringIndex stringIndex = getFullTextStringIndex(((LocalNode)modelElement).getValue());
      return new FullTextStringIndexTuples(stringIndex, (LuceneConstraint)constraint, resolverSession);
    } catch (IOException ioe) {
      throw new QueryException("Failed to open string index", ioe);
    } catch (FullTextStringIndexException ef) {
      throw new QueryException("Query failed against string index\n" + new StackTrace(ef));
    } catch (TuplesException te) {
      throw new QueryException("Failed to query string index", te);
    }
  }

  private FullTextStringIndex getFullTextStringIndex(long model)
      throws FullTextStringIndexException, IOException {
    FullTextStringIndex index = LuceneXAResource.getCurrentIndexes().get(model);
    if (index == null) {
      index = new FullTextStringIndex(resolverFactory.getIndexerCache(Long.toString(model)), forWrites);
      LuceneXAResource.getCurrentIndexes().put(model, index);
    }
    return index;
  }

  public void abort() {
    try {
      closeIndexes(indexes, false);
    } catch (Exception e) {
      logger.error("Error closing fulltext index", e);
    }
  }

  private static void closeIndexes(Collection<FullTextStringIndex> indexes, boolean commit)
      throws Exception {
    Exception exc = null;

    for (FullTextStringIndex index : indexes) {
      try {
        if (commit) {
          // index.optimize();
          index.commit();
        } else {
          index.rollback();
        }
      } catch (Exception e) {
        if (exc == null)
          exc = e;
        else
          logger.error("Error rolling back fulltext index", e);
      } finally {
        try {
          index.close();
        } catch (Exception e) {
          if (exc == null)
            exc = e;
          else
            logger.error("Error closing fulltext index", e);
        }
      }
    }

    if (exc != null)
      throw exc;
  }


  /**
   * An XAResource to manage the lucene indexes.
   */
  private static class LuceneXAResource
      extends AbstractXAResource<RMInfo<LuceneXAResource.LuceneTxInfo>,LuceneXAResource.LuceneTxInfo> {
    private static final ThreadLocal<Map<Long, FullTextStringIndex>> currentIndexes = new ThreadLocal<Map<Long, FullTextStringIndex>>();
    private final LuceneResolver resolver;

    /**
     * Construct a {@link LuceneXAResource} with a specified transaction timeout.
     *
     * @param transactionTimeout transaction timeout period, in seconds
     * @param resolverFactory    the resolver-factory we belong to
     */
    public LuceneXAResource(int transactionTimeout, ResolverFactory resolverFactory, LuceneResolver resolver) {
      super(transactionTimeout, resolverFactory);
      this.resolver = resolver;
    }

    protected RMInfo<LuceneTxInfo> newResourceManager() {
      return new RMInfo<LuceneTxInfo>();
    }

    protected LuceneTxInfo newTransactionInfo() {
      return new LuceneTxInfo();
    }

    public static Map<Long, FullTextStringIndex> getCurrentIndexes() {
      return currentIndexes.get();
    }

    //
    // Methods implementing XAResource
    //

    protected void doStart(LuceneTxInfo tx, int flags, boolean isNew) {
      currentIndexes.set(tx.indexes);
      resolver.indexes = tx.indexes.values();
    }

    protected void doEnd(LuceneTxInfo tx, int flags) {
      currentIndexes.set(null);
    }

    protected int doPrepare(LuceneTxInfo tx) throws Exception {
      for (FullTextStringIndex index : tx.indexes.values())
        index.prepare();
      return XA_OK;
    }

    protected void doCommit(LuceneTxInfo tx) throws Exception {
      closeIndexes(tx.indexes.values(), true);
      tx.indexes.clear();       // so transactionCompleted does not close a second time
    }

    protected void doRollback(LuceneTxInfo tx) throws Exception {
      closeIndexes(tx.indexes.values(), false);
      tx.indexes.clear();       // so transactionCompleted does not close a second time
    }

    protected void doForget(LuceneTxInfo tx) {
    }

    protected void transactionCompleted(LuceneTxInfo tx) {
      super.transactionCompleted(tx);
      try {
        closeIndexes(tx.indexes.values(), false);
      } catch (Exception e) {
        logger.error("Error closing fulltext index", e);
      }
    }

    static class LuceneTxInfo extends TxInfo {
      public final Map<Long, FullTextStringIndex> indexes = new HashMap<Long, FullTextStringIndex>();
    }
  }
}
