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

import java.io.*;

// Third party packages
import junit.framework.*;
import org.mulgara.content.Content;
import org.mulgara.content.rdfxml.RDFXMLContentHandler;
import org.mulgara.resolver.file.FileContent;
import org.mulgara.resolver.spi.*;
import org.mulgara.util.TempDir;


/**
 * Unit test for client-side JRDF Graph representing a mulgara model (modelURI)
 * and uses an ItqlInterpreterBeean and a Session.
 *
 * @created 2004-08-24
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
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RDFXMLWriterUnitTest extends TestCase {

  /** Used as a datasource */
  private RDFXMLContentHandler contentHandler = null;

  /** RDF file that can be parsed to provide Statements */
  private static final String RDF_FILE = System.getProperty("cvs.root") +
      System.getProperty("file.separator") + "data" +
      System.getProperty("file.separator") + "ical.rdf";
//  private static final String RDF_FILE = "/spare/test/mulgara/jxdata/iTQL/data.rdf";


  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public RDFXMLWriterUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new RDFXMLWriterUnitTest("testWriteRDFFile"));
    return suite;
  }

  /**
   * Writes the entire model to a file.
   *
   * @throws Exception
   */
  public void testWriteRDFFile() throws Exception {

    File tmpFile1 = null;
    File tmpFile2 = null;

    try {

      File rdfFile = new File(RDF_FILE);
      Content rdfContent = new FileContent(rdfFile);

      ResolverSession session1 = new TestResolverSession();
      Statements statements1 = contentHandler.parse(rdfContent, session1);
      //write them
      tmpFile1 = writeStatements(statements1, session1);

      // re-parse to ensure it is valid
      rdfContent = new FileContent(tmpFile1);
      ResolverSession session2 = new TestResolverSession();
      //output should be parsable
      Statements statements2 = contentHandler.parse(rdfContent, session2);
      tmpFile2 = writeStatements(statements2, session2);

      //Compare the two files
      assertTrue("RDF/XML files are not being written consistantly.",
          compareFiles(tmpFile1, tmpFile2));
    } catch (Exception exception) {

      exception.printStackTrace();
      throw new Exception(exception);
    } finally {

      //delete the tmp files
      if (tmpFile1 != null) {
        tmpFile1.delete();
      }
      if (tmpFile2 != null) {
        tmpFile2.delete();
      }
    }
  }

  /**
   * Writes the statements to a temp file.
   *
   * @param statements Statements
   * @throws Exception
   * @return File
   */
  private File writeStatements(Statements statements,
      ResolverSession resolverSession) throws Exception {

    File tmpFile = TempDir.createTempFile("RDFXMLWriterUnitTest", ".rdf");
    OutputStream outStream = new FileOutputStream(tmpFile);
    OutputStreamWriter out = new OutputStreamWriter(outStream);

    RDFXMLWriter writer = new RDFXMLWriter();
    writer.write(statements, resolverSession, out);

    out.close();

    return tmpFile;
  }

  /**
   * Compares the statements in the two RDFXML files.
   *
   * @param file1 File
   * @param file2 File
   * @throws Exception
   * @return boolean
   */
  private boolean compareFiles(File file1, File file2) throws Exception {

    //create new sessions
    ResolverSession session1 = new TestResolverSession();

    //RDFXML Parser's require the same session to ensure the same nodes are
    //allocated the same node id's and can then be sorted.
    ResolverSession session2 = session1;
//    ResolverSession session2 = new TestResolverSession();

    //get statements from the two files
    Content content1 = new FileContent(file1);
    Content content2 = new FileContent(file2);
    Statements statements1 = contentHandler.parse(content1, session1);
    Statements statements2 = contentHandler.parse(content2, session2);

    //compare them
    return new TestStatementsComparator().areEquivelant(statements1, session1,
        statements2, session2);
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {

    try {

      this.contentHandler = new RDFXMLContentHandler();

      //let superclass set up too
      super.setUp();
    } catch (Exception exception) {
      //try to tear down first
      try {
        tearDown();
      } catch (Exception e2) {
        // ignore
      }
      throw exception;
    }
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() throws Exception {
    // allow super to close down too
    super.tearDown();
  }
}
