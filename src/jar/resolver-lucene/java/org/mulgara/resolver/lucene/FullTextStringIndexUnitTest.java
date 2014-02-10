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
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

// 3rd party
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

import org.mulgara.util.TempDir;


/**
 * Test cases for FullTextStringIndex.
 *
 * @author Tate Jones
 *
 * @created 2002-03-17
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:47 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FullTextStringIndexUnitTest extends TestCase {
  /** Directory for the indexes */
  private final static String indexDirectory =
      TempDir.getTempDir().getPath() + File.separator + "fulltextsp";

  /** The directory containing the text documents */
  private final static String textDirectory =
      System.getProperty("cvs.root") + File.separator + "data" + File.separator +
      "fullTextTestData";

  /** Logger */
  private final static Logger logger = Logger.getLogger(FullTextStringIndexUnitTest.class);

  /** Hold a list of test data */
  private List<String> theStrings = new ArrayList<String>();

  /**
   * Create the testing class
   *
   * @param name The name of the test.
   */
  public FullTextStringIndexUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new FullTextStringIndexUnitTest("testFullTextStringPool"));
    suite.addTest(new FullTextStringIndexUnitTest("testFullTextStringPoolCornerCases"));
    suite.addTest(new FullTextStringIndexUnitTest("testFullTextStringPoolwithFiles"));
    suite.addTest(new FullTextStringIndexUnitTest("testFullTextStringPoolTransactions"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Creates a new index required to do the testing.
   *
   * @throws IOException Description of Exception
   */
  public void setUp() throws IOException {
    //Populate a list of strings
    theStrings.add("AACP Pneumothorax Consensus Group");
    theStrings.add("ALS-HPS Steering Group");
    theStrings.add(
        "ALSPAC (Avon Longitudinal Study of Parents and Children) Study Team");
    theStrings.add("ALTS Study group");
    theStrings.add("American Academy of Asthma, Allergy and Immunology");
    theStrings.add("American Association for the Surgery of Trauma");
    theStrings.add("American College of Chest Physicians");
    theStrings.add(
        "Antiarrhythmics Versus Implantable Defibrillator (AVID) Trial Investigators");
    theStrings.add("Antibiotic Use Working Group");
    theStrings.add("Atypical Squamous Cells Intraepithelial");
    theStrings.add("Lesion Triage Study (ALTS) Group");
    theStrings.add(
        "Australasian Society for Thrombosis and Haemostasis (ASTH) Emerging Technologies Group");
    theStrings.add("Benefit Evaluation of Direct Coronary Stenting Study Group");
    theStrings.add("Biomarkers Definitions Working Group.");
    theStrings.add(
        "Canadian Colorectal Surgery DVT Prophylaxis Trial investigators");
    theStrings.add("Cancer Research Campaign Phase I - II Committee");
    theStrings.add("Central Technical Coordinating Unit");
    theStrings.add(
        "Clinical Epidemiology Group from the French Hospital Database on HIV");
    theStrings.add("CNAAB3005 International Study Team");
    theStrings.add("Commissione ad hoc");
    theStrings.add("Committee to Advise on Tropical Medicine and Travel");
    theStrings.add(
        "Comparison of Candesartan and Amlodipine for Safety, Tolerability and Efficacy (CASTLE) Study Investigators");
    theStrings.add(
        "Council on Scientific Affairs, American Medical Association");
    theStrings.add(
        "Dana Consortium on the Therapy of HIV-Dementia and Related Cognitive Disorders");
    theStrings.add("Danish Committee on Scientific Dishonesty");
    theStrings.add("Dengue Network Philippines");
    theStrings.add("Donepezil Study Group");
    theStrings.add("EBPG (European Expert Group on Renal Transplantation)");
    theStrings.add(
        "Arbeitsgemeinschaft Dermatologische Histologie (ADH) der DDG.");
    theStrings.add("EORTC Early Clinical Studies Group");
    theStrings.add("European Renal Association (ERA-EDTA)");
    theStrings.add("European Society for Organ Transplantation (ESOT)");
    theStrings.add("European Study Investigators");
    theStrings.add("European Canadian Glatiramer Acetate Study Group");
    theStrings.add("FAMI Investigator Group");
    theStrings.add("French EGEA study");
    theStrings.add("French National Medical and Health Research Institute");
    theStrings.add(
        "French Parkinson's Disease Genetics Study Group. The European Consortium on Genetic");
    theStrings.add("Susceptibility in Parkinson's Disease");
    theStrings.add("German Hodgkin Study Group");
    theStrings.add("Groupe d'Etude des Lymphomes de l'Adulte (GELA)");
    theStrings.add(
        "Groupe d'Etude et de Recherche Clinique en Oncologie Radiotherapies");
    theStrings.add("Hemophilia Behavioral Intervention Study Group");
    theStrings.add("Hepatitis Interventional Therapy Group");
    theStrings.add("HIV Epidemiology Research Study Group");
    theStrings.add("Houston Congenital CMV Longitudinal Study Group");
    theStrings.add(
        "International Council for Science's Standing Committee on Responsibility and Ethics in Science");
    theStrings.add("International Evidence-Based Group for Neonatal Pain");

    theStrings.add("one");
    theStrings.add("one two");
    theStrings.add("one two three");
    theStrings.add("holidays");
  }

  /**
   * Closes the index used for testing.
   *
   * @throws IOException Description of Exception
   */
  public void tearDown() throws IOException {
  }

  /**
   * 1. Test the loading of strings into the fulltext string pool 2. Checking
   * for existance 3. Test non-stemming 4. Test removal of strings
   *
   * @throws Exception Test fails
   */
  public void testFullTextStringPool() throws Exception {
    LuceneIndexerCache cache = new LuceneIndexerCache(indexDirectory);
    FullTextStringIndex index = null;

    try {
      // Ensure that reverse search is enabled.
      String document = "http://mulgara.org/mulgara/document#";
      String has = "http://mulgara.org/mulgara/document#has";

      //Clean any existing indexes.
      cache.close();
      cache.removeAllIndexes();
      cache = new LuceneIndexerCache(indexDirectory);

      //create the index
      index = new FullTextStringIndex(cache, true, true);

      // Add strings to the index
      for (String literal : theStrings) {
        index.add(document, has, literal);
      }

      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      // Find the strings from the index with both subject & predicate
      for (String literal : theStrings) {
        testHas("failed to find '" + literal + "'", index, document, has, literal);
      }

      // Find the strings from the index with only subject
      for (String literal : theStrings) {
        testHas("failed to find '" + literal + "'", index, document, null, literal);
      }

      // Find the strings from the index with only predicate
      for (String literal : theStrings) {
        testHas("failed to find '" + literal + "'", index, null, has, literal);
      }

      testFind("Stemming match search failed", 0, index, null, null, "\"holiday\"");

      /* Enable when TODO in remove() is fixed
      assertFalse("Should not be able to delete fulltext literal due to incorrect value",
                  index.remove(document, has, "holiday"));
       */

      index.remove(document, has, "one two");
      index.remove(document, has, "one");
      index.remove(document, has, "one two three");

      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      testFind("Presumed deleted but found 'one two'", 0, 
                   index, document, has, "one two");
      testFind("Presumed deleted but found 'one'", 0,
                   index, document, has, "one");
      testFind("Presumed deleted but found 'one two three'", 0,
                   index, document, has, "one two three");

      // don't add empty literals
      assertFalse("Adding an empty literal string should fail",
                  index.add("subject","predicate", ""));
      assertFalse("Adding an empty literal string should fail",
                  index.add("subject","predicate", "  "));

      assertTrue("Adding a string containing slashes to the fulltext string pool",
                 index.add("subject", "predicate", "this/is/a/slash/test"));

      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      testFind("Reverse lookup was expecting 4 documents returned", 4, 
          index, document, has, "?ommittee");

      testFind("Reverse lookup was expecting 3 documents returned", 3, 
          index, document, has, "*iv");

      testFind("Reverse lookup was expecting 26 documents returned", 26, 
          index, document, has, "study *roup");

      testFind("Reverse lookup was expecting 10 documents returned", 10, 
          index, document, has, "+study +*roup");

      testFind("Reverse lookup was expecting 11 documents returned", 11, 
          index, document, has, "-study +*roup");

      testFind("Reverse lookup was expecting 1 document returned", 1, 
          index, document, has, "+*hrombosis");

      // test removing all documents
      index.removeAll();
      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      testFind("Got unexpected documents after removeAll:", 0, 
          index, document, has, "European");

      testFind("Got unexpected documents after removeAll:", 0, 
          index, document, has, "+study +*roup");
    } finally {
      if (index != null) index.close();
      cache.close();
      assertTrue("Unable to remove all index files", cache.removeAllIndexes());
    }
  }


  /**
   * Test corner cases (null subject, object, predicate, etc).
   *
   * @throws Exception Test fails
   */
  public void testFullTextStringPoolCornerCases() throws Exception {
    LuceneIndexerCache cache = new LuceneIndexerCache(indexDirectory);
    FullTextStringIndex index = null;

    try {
      String document = "http://mulgara.org/mulgara/document#";
      String has = "http://mulgara.org/mulgara/document#has";

      //Clean any existing indexes.
      cache.close();
      cache.removeAllIndexes();
      cache = new LuceneIndexerCache(indexDirectory);

      //create the index
      index = new FullTextStringIndex(cache, true, true);

      // Add strings to the index
      try {
        index.add(null, has, "foo");
        fail("exception expected for adding null subject");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.add(document, null, "foo");
        fail("exception expected for adding null predicate");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.add(document, has, null);
        fail("exception expected for adding null literal");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.add("", has, "foo");
        fail("exception expected for adding empty subject");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.add(document, "", "foo");
        fail("exception expected for adding empty predicate");
      } catch (FullTextStringIndexException ftsie) {
      }

      index.add(document, has, "");

      // remove strings from the index
      try {
        index.remove(null, has, "foo");
        fail("exception expected for removing null subject");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.remove(document, null, "foo");
        fail("exception expected for removing null predicate");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.remove(document, has, null);
        fail("exception expected for removing null literal");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.remove("", has, "foo");
        fail("exception expected for removing empty subject");
      } catch (FullTextStringIndexException ftsie) {
      }

      try {
        index.remove(document, "", "foo");
        fail("exception expected for removing empty predicate");
      } catch (FullTextStringIndexException ftsie) {
      }

      index.remove(document, has, "");

    } finally {
      if (index != null) index.close();
      cache.close();
      assertTrue("Unable to remove all index files", cache.removeAllIndexes());
    }
  }

  /**
   * 1. Test the loading of text files into the fulltext string pool 2. Checking
   * for existance 3. Test removal of files
   *
   * @throws Exception Test fails
   */
  public void testFullTextStringPoolwithFiles() throws Exception {
    // create a new index direcotry
    LuceneIndexerCache cache = new LuceneIndexerCache(indexDirectory);
    FullTextStringIndex index = null;

    try {
      // make sure the index directory is empty
      cache.close();
      assertTrue("Unable to remove all index files", cache.removeAllIndexes());
      cache = new LuceneIndexerCache(indexDirectory);

      // create a new index
      index = new FullTextStringIndex(cache, true, true);

      logger.debug("Obtaining text text documents from " + textDirectory);

      File directory = new File(textDirectory);
      File[] textDocuments = directory.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".txt");
        }
      });

      // keep a track of the number of documents added.
      int docsAdded = 0;

      // Loop over the text documents locatd in the text directory
      for (File doc : textDocuments) {
        if (doc.isFile()) {
          // open a reader to the text file.
          Reader reader = new InputStreamReader(new FileInputStream(doc));

          // Add the text document to the index
          if (index.add(doc.toURI().toString(), "http://mulgara.org/mulgara/Document#Content",
                        doc.toURI().toString(), reader)) {
            logger.debug("Indexed text document " + doc.toString());
            docsAdded++;
          }

          // clean up the stream
          reader.close();
        }
      }

      logger.debug("Text documents indexed :" + docsAdded);

      // check if all text documents were indexed
      assertEquals("Expected 114 text documents to be indexed", 114, docsAdded);

      // commit the new docs
      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      // Perform a search for 'supernatural' in the
      // document content predicate
      FullTextStringIndex.Hits hits =
          index.find(null, "http://mulgara.org/mulgara/Document#Content", "supernatural");

      // check if all text documents were indexed
      assertEquals("Expected 6 hits with the word 'supernatural'", 6, hits.length());

      // loop through the results and remove the documents containing
      // the word 'supernatural'
      int docsRemoved = 0;

      for (int docNo = 0; docNo < hits.length(); docNo++) {
        String uri = hits.doc(docNo, null).getFieldable(FullTextStringIndex.SUBJECT_KEY).stringValue();

        logger.debug("Found supernatural in :" + uri);

        // Remove the text documents from the index
        if (index.remove(uri, "http://mulgara.org/mulgara/Document#Content", uri)) {
          docsRemoved++;
        }
      }
      hits.close();

      // check the document were removed
      assertEquals("Expected 6 documents to be removed'", 6, docsRemoved);

      // commit the removal
      index.commit();
      index.close();
      index = new FullTextStringIndex(cache, true, true);

      // Perform a search for 'supernatural' in the
      // document content predicate
      // check if all text documents are not present.
      testFind("Expected 0 hits with the word 'supernatural'", 0,
          index, null, "http://mulgara.org/mulgara/Document#Content", "supernatural");
    } finally {
      // close the fulltextstringpool
      if (index != null) index.close();
      cache.close();
      assertTrue("Unable to remove all index files", cache.removeAllIndexes());
    }
  }

  /**
   * Test commit and rollback, with and without a prepare.
   *
   * @throws Exception Test fails
   */
  public void testFullTextStringPoolTransactions() throws Exception {
    doTestFullTextStringPoolTransactions(false);
    doTestFullTextStringPoolTransactions(true);
  }

  private void doTestFullTextStringPoolTransactions(boolean prepare) throws Exception {
    // create a new index direcotry
    LuceneIndexerCache cache = new LuceneIndexerCache(indexDirectory);
    FullTextStringIndex index = null;

    String document = "http://mulgara.org/mulgara/document#";
    String has = "http://mulgara.org/mulgara/document#has";

    try {
      //Clean any existing indexes.
      cache.close();
      cache.removeAllIndexes();
      cache = new LuceneIndexerCache(indexDirectory);

      //create the index
      index = new FullTextStringIndex(cache, true, false);

      // Add strings to the index
      for (String literal : theStrings) {
        index.add(document, has, literal);
      }

      // roll back
      if (prepare)
        index.prepare();
      index.rollback();
      index.close();

      // ensure strings are not there
      index = new FullTextStringIndex(cache, true, false);

      for (String literal : theStrings) {
        FullTextStringIndex.Hits hits = index.find(document, has, literal);
        assertTrue("Unexpectedly found '" + literal + "'", hits.length() == 0);
        hits.close();
      }

      // add strings to index again
      for (String literal : theStrings) {
        index.add(document, has, literal);
      }

      // this time commit
      if (prepare)
        index.prepare();
      index.commit();
      index.close();

      // ensure strings are there now
      index = new FullTextStringIndex(cache, true, false);

      for (String literal : theStrings) {
        FullTextStringIndex.Hits hits = index.find(document, has, literal);
        assertTrue("Did not find '" + literal + "'", hits.length() != 0);
        hits.close();
      }
    } finally {
      // close the fulltextstringpool
      if (index != null) index.close();
      cache.close();
      assertTrue("Unable to remove all index files", cache.removeAllIndexes());
    }
  }
  
  private static void testFind(String msg, int len, FullTextStringIndex index, String s, String p, String o) throws Exception {
    FullTextStringIndex.Hits hits = index.find(s, p, o);
    assertNotNull(hits);
    try {
      assertEquals(msg, len, hits.length());
    } finally {
      hits.close();
    }
  }
  
  private static void testHas(String msg, FullTextStringIndex index, String s, String p, String o) throws Exception {
    FullTextStringIndex.Hits hits = index.find(s, p, o);
    assertNotNull(hits);
    try {
      assertTrue(msg, hits.length() > 0);
    } finally {
      hits.close();
    }
  }
}
