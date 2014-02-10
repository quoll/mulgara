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

package org.mulgara.resolver.lucene;

// Java 2 standard packages
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;

import static org.mulgara.resolver.lucene.LuceneResolver.LUCENE_VERSION;

/**
 * The utility class which provides an interface of adding, finding and removing
 * statements and documents for Lucene.
 *
 * @created 2002-03-15
 *
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:47 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FullTextStringIndex {
  /** Logger. This is named after the class. */
  private final static Logger logger = Logger.getLogger(FullTextStringIndex.class);

  //
  // Constants
  //

  /** The field name for the actual literal */
  public final static String ID_KEY = "id";

  /** The field name for the stemmed literal */
  public final static String LITERAL_KEY = "stemmedliteral";

  /** The field name for the actual subject */
  public final static String SUBJECT_KEY = "subject";

  /** The field name for the actual predicate */
  public final static String PREDICATE_KEY = "predicate";

  /** The field name for the reverse literal */
  public final static String REVERSE_LITERAL_KEY = "reverseliteral";
  
  //
  // Fields
  //

  /** The lucene indexer cache */
  private LuceneIndexerCache indexerCache;

  /** The index writer */
  private IndexWriter indexer;

  /** The index searcher */
  private IndexSearcher indexSearcher;

  /** Whether any modifications have been made to the index. */
  private boolean madeMods = false;

  /** Whether to close the indexers when closing this index */
  private boolean closeIndexers = false;

  /** Enable reverse literal lookup */
  private boolean enableReverseTextIndex;

  /** Analyzer used for writing and reading */
  private Analyzer analyzer = getAnalyzer();

  /**
   * Create a new FullTextStringIndex object. Uses the system property
   * "mulgara.textindex.reverse.enabled" to set the desired value for
   * enableReverseTextIndex, or will default to "false" if not set.
   *
   * @param indexerCache the indexer-cache to use to get the indexers
   * @param forWrites    whether writes will occur or not
   * @throws FullTextStringIndexException on failure to obtain an index reader or writer
   */
  public FullTextStringIndex(LuceneIndexerCache indexerCache, boolean forWrites)
      throws FullTextStringIndexException {
    this(indexerCache, forWrites, Boolean.getBoolean("mulgara.textindex.reverse.enabled"));
  }

  /**
   * Create a new FullTextStringIndex object.
   *
   * @param indexerCache the indexer-cache to use to get the indexers
   * @param forWrites whether to acquire an index writer
   * @param enableReverseTextIndex true if you can begin Lucene queries with wildcards.
   * @throws FullTextStringIndexException on failure to obtain an index reader or writer
   */
  public FullTextStringIndex(LuceneIndexerCache indexerCache, boolean forWrites,
                             boolean enableReverseTextIndex)
      throws FullTextStringIndexException {
    this.indexerCache = indexerCache;
    this.enableReverseTextIndex = enableReverseTextIndex;
    initialize(forWrites);
  }

  /**
   * Get an instance of the analyzer used on text to produce the index.
   *
   * @return The analyzer used.
   */
  public static Analyzer getAnalyzer() {
    return new StandardAnalyzer(LUCENE_VERSION);
  }

  /**
   * Determine if the literal search string contains a leading wildcard.
   *
   * @param literal PARAMETER TO DO
   * @return The LeadingWildcard value
   */
  private static boolean isLeadingWildcard(String literal) {
    return (literal.startsWith("?") || literal.startsWith("*") ||
        (literal.indexOf(" *") >= 0) || (literal.indexOf(" ?") >= 0) ||
        (literal.indexOf("-*") >= 0) || (literal.indexOf("-?") >= 0) ||
        (literal.indexOf("+*") >= 0) || (literal.indexOf("+?") >= 0));
  }

  /**
   * Create a key to uniquely identify a triple Used for performing deletions.
   * TODO : hashcode is not the most appropriate technique. In future change to
   * MD5 sum.
   *
   * @param subject PARAMETER TO DO
   * @param predicate PARAMETER TO DO
   * @param literal PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  private static String createKey(String subject, String predicate, String literal) {
    return String.valueOf(subject.hashCode()) + String.valueOf(predicate.hashCode()) +
           String.valueOf(literal.hashCode());
  }

  /**
   * Reverse the literal search string to ensure the + and - contraints are
   * prefixed.
   *
   * @param literal PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  private static String reverseLiteralSearch(String literal) {
    String newReversedString = null;

    // does it contain any +'s or -'s in the search request?
    if ((literal.indexOf("+") >= 0) || (literal.indexOf("-") >= 0)) {
      StringBuilder searchReversed = new StringBuilder();
      String[] tokens = literal.split(" ");

      for (int i = tokens.length - 1; i >= 0; i--) {
        StringBuilder reversedStringBuff = new StringBuilder(tokens[i]).reverse();

        char lastChar = reversedStringBuff.charAt(reversedStringBuff.length() - 1);

        // move the + || - to the start of the reversed string
        if ((lastChar == '+') || (lastChar == '-')) {
          reversedStringBuff.deleteCharAt(reversedStringBuff.length() - 1).insert(0, lastChar);
        }

        searchReversed.append(reversedStringBuff);

        if (i != 0) {
          searchReversed.append(" ");
        }
      }

      newReversedString = searchReversed.toString();
    } else {
      // perform a simple reverse
      newReversedString = (new StringBuilder(literal).reverse()).toString();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Reversed literal search from : " + literal + " to " + newReversedString);
    }

    return newReversedString;
  }

  /**
   * Add a subject, predicate and literal into the fulltext string pool. {@link
   * StandardAnalyzer} sets the filters used to on the literal field index.
   *
   * @param subject the subject to be added
   * @param predicate the predicate to be added
   * @param literal literal to be analyzed for fulltext searching
   * @return boolean Return true if successful
   * @throws FullTextStringIndexException Failure to add string due to an
   *      IOException
   */
  public boolean add(String subject, String predicate, String literal) throws FullTextStringIndexException {
    if (literal == null   ||
        subject == null   || subject.length() == 0 ||
        predicate == null || predicate.length() == 0) {
      throw new FullTextStringIndexException("Subject, predicate or literal has " +
                                             "not been supplied a value");
    }

    boolean added = false;

    // Warn for an empty literal
    if (literal.trim().length() == 0) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring empty literal");
    } else {
      // debug logging
      if (logger.isDebugEnabled()) {
        logger.debug("Adding subject <" + subject + "> predicate <" + predicate + "> literal <'" +
                     literal + "'> to fulltext string index");
      }

      Document indexDocument = new Document();

      // Add the literal value to the predicate field and tokenize it for
      // fulltext searching
      indexDocument.add(new Field(LITERAL_KEY, literal, Field.Store.YES, Field.Index.ANALYZED));

      // Add the literal value to the predicate field and tokenize it for
      // fulltext searching in reverse order
      if (enableReverseTextIndex) {
        indexDocument.add(new Field(REVERSE_LITERAL_KEY,
            (new StringBuilder(literal).reverse()).toString(), Field.Store.YES, Field.Index.ANALYZED));
      }

      // Add the actual literal, do not tokenize it. Required for exact
      // matching. ie. removal
      indexDocument.add(new Field(ID_KEY,
          createKey(subject, predicate, literal), Field.Store.YES, Field.Index.NOT_ANALYZED));

      // Add the predicate, do not tokenize it, required for exact matching
      indexDocument.add(new Field(PREDICATE_KEY, predicate, Field.Store.YES, Field.Index.NOT_ANALYZED));

      // Add the subject, do not tokenize it, required for exact matching
      indexDocument.add(new Field(SUBJECT_KEY, subject, Field.Store.YES, Field.Index.NOT_ANALYZED));

      try {
        indexer.addDocument(indexDocument, analyzer);
        added = true;
        madeMods = true;
      } catch (IOException ex) {
        closeIndexers = true;
        logger.error("Unable to add fulltext string subject <" + subject + "> predicate <" +
                     predicate + "> literal <'" + literal + "'> to fulltext string index", ex);
        throw new FullTextStringIndexException(
            "Unable to add fulltext string subject <" + subject + "> predicate <" +
            predicate + "> literal <'" + literal + "'> to fulltext string index",
            ex);
      }

    } // Warn empty literals

    return added;
  }

  /**
   * Add a subject, predicate and literal into the fulltext string pool. {@link
   * StandardAnalyzer} sets the filters used to on the literal field index.
   *
   * @param subject the subject to be added
   * @param predicate the predicate to be added
   * @param resource resource to be analyzed for fulltext searching
   * @param reader stream containing text of the resource
   * @return <code>true</code> if successful
   * @throws FullTextStringIndexException Failure to add string due to an
   *      IOException
   */
  public boolean add(String subject, String predicate, String resource, Reader reader)
      throws FullTextStringIndexException {
    // Validate "subject" parameter
    if ((subject == null) || (subject.length() == 0)) {
      throw new FullTextStringIndexException("No \"subject\" parameter");
    }

    // Validate "predicate" parameter
    if ((predicate == null) || (predicate.length() == 0)) {
      throw new FullTextStringIndexException("No \"predicate\" parameter");
    }

    // Validate "resource" parameter
    if ((resource == null) || (resource.length() == 0)) {
      throw new FullTextStringIndexException("No \"resource\" parameter");
    }

    // Validate "reader" parameter
    if (reader == null) {
      throw new FullTextStringIndexException("Null \"reader\" parameter");
    }

    boolean added = false;

    // debug logging
    if (logger.isDebugEnabled()) {
      logger.debug("Adding subject <" + subject + "> predicate <" + predicate + "> resource <" +
                   resource + "> to fulltext string index");
    }

    Document indexDocument = new Document();

    // Add the resource content to the predicate field and tokenize it for
    // fulltext searching
    indexDocument.add(new Field(LITERAL_KEY, reader));

    // Add the resource label, do not tokenize it. Required for exact
    // matching. ie. removal
    indexDocument.add(new Field(ID_KEY,
        createKey(subject, predicate, resource), Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the predicate, do not tokenize it, required for exact matching
    indexDocument.add(new Field(PREDICATE_KEY, predicate, Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the subject, do not tokenize it, required for exact matching
    indexDocument.add(new Field(SUBJECT_KEY, subject, Field.Store.YES, Field.Index.NOT_ANALYZED));

    try {
      indexer.addDocument(indexDocument, analyzer);
      added = true;
      madeMods = true;
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to add fulltext string subject <" + subject + "> predicate <" +
                   predicate + "> resource <" + resource + "> to fulltext string index", ex);
      throw new FullTextStringIndexException(
          "Unable to add fulltext string subject <" + subject + "> predicate <" +
          predicate + "> resource <" + resource + "> to fulltext string index",
          ex);
    }

    return added;
  }

  /**
   * Add a document into the fulltext string pool. The constants {@link
   * #SUBJECT_KEY}, {@link #PREDICATE_KEY}, {@link #LITERAL_KEY} should be used
   * in the query to reference the relevant index fields if the index is to read
   * by queries.
   *
   * @param indexDocument The document to be indexed.
   * @return true if successful
   * @throws FullTextStringIndexException Failure to add string due to an
   *      IOException
   */
  public boolean add(Document indexDocument) throws FullTextStringIndexException {
    if (indexDocument == null) {
      throw new FullTextStringIndexException("The document to be indexed was null.");
    }

    boolean added = false;

    // debug logging
    if (logger.isDebugEnabled()) {
      logger.debug("Adding document " + indexDocument + " to fulltext string index");
    }

    try {
      indexer.addDocument(indexDocument, analyzer);
      added = true;
      madeMods = true;
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to add " + indexDocument + " to fulltext string index", ex);
      throw new FullTextStringIndexException("Unable to add " + indexDocument + " to fulltext string index", ex);
    }

    return added;
  }

  /**
   * Remove the extact string from the fulltext string pool
   *
   * @param subject subject must be supplied
   * @param predicate predicate must be supplied
   * @param literal literal must be supplied
   * @return True is the string was successfully removed
   * @throws FullTextStringIndexException An IOException occurs on index
   *      modification
   */
  public boolean remove(String subject, String predicate, String literal)
      throws FullTextStringIndexException {
    boolean removed = false;
    if ((literal == null) ||
        (subject == null) ||
        (subject.length() == 0) ||
        (predicate == null) ||
        (predicate.length() == 0)) {
      throw new FullTextStringIndexException("Subject, predicate or literal has not been supplied a value");
    }

    //Create the composite key for searching
    String key = createKey(subject, predicate, literal);

    try {
      Term term = new Term(ID_KEY, key);
      indexer.deleteDocuments(term);
      removed = true; // TODO: could use docCount(), but that seems overly expensive
      madeMods = true;

      if (logger.isDebugEnabled()) {
        if (removed) {
          logger.debug("Removed key '" + key + "' from fulltext string pool");
        } else {
          logger.debug("Unable to removed string '" + key + "' from fulltext string pool");
        }
      }
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to delete the string '" + key + "'", ex);
      throw new FullTextStringIndexException("Unable to delete the string '" + key + "'", ex);
    }

    return removed;
  }

  /**
   * Remove all entries in the string pool. Unlike {@link LuceneIndexerCache#removeAllIndexes},
   * this may be * called while readers are active. However, this method may be very slow. Also
   * note that this will <strong>not</strong> remove entries that have been added as part of the
   * current transaction!
   *
   * @throws FullTextStringIndexException Exception occurs when attempting to remove the documents
   */
  public void removeAll() throws FullTextStringIndexException {
    // debug logging
    if (logger.isDebugEnabled()) {
      logger.debug("Removing all documents from " + indexerCache.getDirectory());
    }

    try {
      indexer.deleteDocuments(new MatchAllDocsQuery());
      madeMods = true;
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to delete all documents", ex);
      throw new FullTextStringIndexException("Unable to delete all documents", ex);
    }
  }

  /**
   * Close this index and return the indexers to the cache.
   */
  public void close() {
    if (logger.isDebugEnabled()) {
      logger.debug("Closing fulltext indexes");
    }

    if (indexer != null) {
      indexerCache.returnWriter(indexer, closeIndexers);
      indexer = null;
    }

    if (indexSearcher != null) {
      indexerCache.returnReader(indexSearcher.getIndexReader(), closeIndexers);
      indexSearcher = null;
    }
  }

  /**
   * Optimize the index and then flush it to disk.
   *
   * @throws FullTextStringIndexException If there was a problem reading from or writing to the disk.
   */
  public void optimize() throws FullTextStringIndexException {
    if (indexer == null) return;

    if (logger.isInfoEnabled()) {
      logger.info("Optimizing fulltext index at " + indexerCache.getDirectory() + " please wait...");
    }

    try {
      indexer.optimize();
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to optimize existing fulltext string pool index", ex);
      throw new FullTextStringIndexException("Unable to optimize existing fulltext string pool index", ex);
    }
  }

  /**
   * Find a string within the fulltext string pool. The search is based on the
   * {@link StandardAnalyzer} used to add the string.
   *
   * @param subject subject; may be null
   * @param predicate predicate; may be null
   * @param literal literal to be searched via the analyzer; may be null
   * @return Object containing the hits
   * @throws FullTextStringIndexException IOException occurs on reading index
   */
  public Hits find(String subject, String predicate, String literal) throws FullTextStringIndexException {
    Query query;

    if (subject == null && predicate == null && literal == null) {
      query = new MatchAllDocsQuery();
    } else {
      BooleanQuery bQuery = new BooleanQuery();
      query = bQuery;

      // debug logging
      if (logger.isDebugEnabled()) {
        logger.debug("Searching the fulltext string index pool with  subject :" + subject +
                     " predicate :" + predicate + " literal :" + literal);
      }

      if (subject != null) {
        TermQuery tSubject = new TermQuery(new Term(SUBJECT_KEY, subject));
        if (literal != null) tSubject.setBoost(0);      // if scoring, don't affect the score
        bQuery.add(tSubject, BooleanClause.Occur.MUST);
      }

      if (predicate != null) {
        TermQuery tPredicate = new TermQuery(new Term(PREDICATE_KEY, predicate));
        if (literal != null) tPredicate.setBoost(0);    // if scoring, don't affect the score
        bQuery.add(tPredicate, BooleanClause.Occur.MUST);
      }

      if (literal != null) {
        Query qliteral = null;

        try {
          // Are we performing a reverse string lookup?
          if (enableReverseTextIndex && isLeadingWildcard(literal)) {
            literal = reverseLiteralSearch(literal);
            QueryParser parser = new QueryParser(LUCENE_VERSION, REVERSE_LITERAL_KEY, analyzer);
            qliteral = parser.parse(literal);
          } else {
            QueryParser parser = new QueryParser(LUCENE_VERSION, LITERAL_KEY, analyzer);
            qliteral = parser.parse(literal);
          }
        } catch (ParseException ex) {
          logger.error("Unable to parse query '" + literal + "'", ex);
          throw new FullTextStringIndexException("Unable to parse query '" + literal + "'", ex);
        }

        bQuery.add(qliteral, BooleanClause.Occur.MUST);
      }

      // debug logging
      if (literal != null && logger.isDebugEnabled()) {
        if ((literal.startsWith("*") || literal.startsWith("?")) && enableReverseTextIndex) {
          logger.debug("Searching the fulltext string index pool with parsed query as " +
                       bQuery.toString(REVERSE_LITERAL_KEY));
        } else {
          logger.debug("Searching the fulltext string index pool with parsed query as " +
                       bQuery.toString(LITERAL_KEY));
        }
      }
    }

    //Perform query
    return find(query);
  }

  /**
   * Execute a query against the string pool. The constants {@link
   * #SUBJECT_KEY}, {@link #PREDICATE_KEY}, {@link #LITERAL_KEY} should be used
   * in the query to reference the relevant index filds if the index was created
   * by queries. Use the method {@link #getAnalyzer()} to get the analyzer
   * used by this class.
   *
   * @param query The query to execute.
   * @return RETURNED VALUE TO DO
   * @throws IOException occurs on reading index.
   * @throws FullTextStringIndexException EXCEPTION TO DO
   */
  public Hits find(Query query) throws FullTextStringIndexException {
    if (query == null) {
      throw new FullTextStringIndexException("The query may not be null.");
    }

    Hits hits = null;

    try {
      // debug logging
      if (logger.isDebugEnabled()) {
        logger.debug("Searching the fulltext string index pool with query " + query.toString(LITERAL_KEY));
      }

      //Perform query
      indexSearcher.search(query, hits = new Hits(indexSearcher.getIndexReader()));

      if (logger.isDebugEnabled()) {
        logger.debug("Got hits: " + hits.length());
      }
    } catch (IOException ex) {
      closeIndexers = true;
      logger.error("Unable to read results for query '" + query.toString(LITERAL_KEY) + "'", ex);
      throw new FullTextStringIndexException("Unable to read results for query '" + query.toString(LITERAL_KEY) + "'", ex);
    }

    return hits;
  }

  /**
   * The maximum number of documents the given query could return.
   *
   * @param subject   the subject; may be null
   * @param predicate the predicate; may be null
   * @param object   literal to be searched via the analyzer; may be null
   * @return the maximum number of documents
   * @throws FullTextStringIndexException if an error occurred
   */
  public long getMaxDocs(String subject, String predicate, String object)
      throws FullTextStringIndexException {
    long total = -1;

    try {
      if (subject != null) {
        total = indexSearcher.docFreq(new Term(SUBJECT_KEY, subject));
        if (total == 0) return 0;
      }

      if (predicate != null) {
        if (total > 0)
          total = Math.min(indexSearcher.docFreq(new Term(PREDICATE_KEY, predicate)), total);
        if (total == 0) return 0;
      }

      if (object != null) {
        QueryParser parser = new QueryParser(LUCENE_VERSION, LITERAL_KEY, analyzer);
        total = findMinDocCount(parser.parse(object), total);
      }

      return (total >= 0) ? total : indexSearcher.maxDoc();
    } catch (IOException ioe) {
      closeIndexers = true;
      throw new FullTextStringIndexException("Unable to count results for query '" + object + "'", ioe);
    } catch (ParseException pe) {
      throw new FullTextStringIndexException("Unable to parse query '" + object + "'", pe);
    }
  }

  private long findMinDocCount(Query q, long max) throws IOException {
    long count = max;

    if (q instanceof TermQuery) {
      Term term = ((TermQuery)q).getTerm();
      count = Math.min(indexSearcher.docFreq(term), count);
    } else if (q instanceof BooleanQuery) {
      for (BooleanClause clause : ((BooleanQuery)q).getClauses()) {
        if (clause.isRequired()) count = findMinDocCount(clause.getQuery(), count);
      }
    } else if (q instanceof PhraseQuery) {
      for (Term term : ((PhraseQuery)q).getTerms()) {
        count = Math.min(indexSearcher.docFreq(term), count);
      }
    }

    return count;
  }

  /**
   * Acquire the indexers.
   *
   * @param forWrites whether to acquire an index writer
   * @throws FullTextStringIndexException if an exception occurs while trying to
   *      locate or create the indexes
   */
  private void initialize(boolean forWrites) throws FullTextStringIndexException {
    if (forWrites) {
      openWriteIndex();
    }

    try {
      openReadIndex();
    } catch (FullTextStringIndexException e) {
      if (indexer != null) {
        logger.debug("Failed to get reader, closing writer");
        try {
          indexerCache.returnWriter(indexer, true);
        } finally {
          indexer = null;
        }
      }
      throw e;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Fulltext string index initialized");
    }
  }

  /**
   * Open the index on disk for writing.
   *
   * @throws FullTextStringIndexException if there is an error whilst opening the index.
   */
  private void openWriteIndex() throws FullTextStringIndexException {
    try {
      indexer = indexerCache.getWriter();
    } catch (IOException ioe) {
      closeIndexers = true;
      throw new FullTextStringIndexException("Unable to open fulltext string pool index", ioe);
    }
  }

  /**
   * Open the index on disk for reading.
   *
   * @throws FullTextStringIndexException if there is an error whilst opening the index.
   */
  private void openReadIndex() throws FullTextStringIndexException {
    try {
      indexSearcher = new IndexSearcher(indexerCache.getReader());
    } catch (IOException ioe) {
      closeIndexers = true;
      throw new FullTextStringIndexException("Unable to open fulltext index for reading", ioe);
    }
  }

  public void prepare() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Preparing fulltext indexes");
    }

    if (indexer != null) indexer.prepareCommit();
  }

  public void rollback() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Rolling back fulltext indexes");
    }

    closeIndexers = true;
    if (indexer != null) indexer.rollback();
  }

  public void commit() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Comitting fulltext indexes");
    }

    if (indexer != null) {
      indexer.commit();
      if (madeMods) indexerCache.indexModified(indexer);
    }
  }

  /**
   * Lucene Hits has been deprecated, so this is our simple version thereof. Since we always
   * read all results, this is more efficient too.
   *
   * <p>TODO: since we collect all hits in memory (just id and score, not actual Document's),
   * we could have problems with very large results. However, jdk 1.5 memory usage seems to be
   * around 20B/entry on 32-bit and 33B/entry on 64-bit systems for the hits array, allowing us
   * to handle 1M hits without too much trouble.
   */
  public static class Hits extends Collector implements Cloneable {
    private final IndexReader reader;
    private final List<ScoreDoc> hits = new ArrayList<ScoreDoc>();
    private boolean closed = false;
    private Scorer scorer = null;
    private int docBase = 0;

    public Hits(IndexReader reader) {
      this.reader = reader;
      reader.incRef();
    }

    public final int length() {
      return hits.size();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }

    @Override
    public void collect(int doc) throws IOException {
      float score = scorer.score();
      if (score > 0.0f) hits.add(new ScoreDoc(docBase + doc, score));
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;
    }

    public final Document doc(int n, FieldSelector fs) throws IOException {
      return reader.document(hits.get(n).doc, fs);
    }

    public final float score(int n) throws IOException {
      return hits.get(n).score;
    }

    public void sort() {
      Collections.sort(hits, new Comparator<ScoreDoc>() {
        public int compare(ScoreDoc sd1, ScoreDoc sd2) {
          return (sd1.doc < sd2.doc) ? -1 : (sd1.doc == sd2.doc) ? 0 : +1;
        }
      });
    }

    public void close() throws IOException {
      if (closed) return;

      closed = true;
      reader.decRef();
    }

    public Hits clone() {
      reader.incRef();
      try {
        return (Hits) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new Error(getClass() + " doesn't support clone, which it must", e);
      }
    }
  }
}
