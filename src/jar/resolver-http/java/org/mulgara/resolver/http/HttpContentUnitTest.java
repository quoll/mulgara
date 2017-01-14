/*
 *
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

package org.mulgara.resolver.http;

//Java 2 standard packages
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

//Java 2 enterprise packages
import javax.activation.MimeType;

// Third party packages
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mulgara.util.TempDir;


/**
 * Test cases for HttpContent.
 *
 * @author Tate Jones
 *
 * @created 2002-03-17
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/07/03 13:03:59 $ by $Author: pgearon $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class HttpContentUnitTest extends TestCase {

  /**
   * Create the testing class
   *
   * @param name The name of the test.
   */
  public HttpContentUnitTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test Content type 
   *
   * @throws Exception Test fails
   */
  public void testValidContentType() throws Exception {

    // obtain a valid content type
    HttpContent content = new HttpContent(new URL("http://google.com/"));
    MimeType mimeType = content.getContentType();
    assertTrue("Expecting mime type of text/html but found " + mimeType,
        mimeType != null && mimeType.getPrimaryType().equals("text")
            && mimeType.getSubType().equals("html"));

    // obtain a valid content type via https
    content = new HttpContent(new URL("https://sourceforge.net"));
    mimeType = content.getContentType();
    assertTrue("Expecting mime type of text/html but found " + mimeType,
        mimeType != null && mimeType.getPrimaryType().equals("text")
            && mimeType.getSubType().equals("html"));

    // obtain a valid content type of XML via http
    content = new HttpContent(new URL(
        "http://rss.news.yahoo.com/rss/topstories"));
    mimeType = content.getContentType();
    assertTrue("Expecting mime type of text/xml but found " + mimeType,
        mimeType != null && mimeType.getPrimaryType().equals("text")
            && mimeType.getSubType().equals("xml"));

    // obtain a valid content type of XML via https
    content = new HttpContent(new URL(
        "https://sourceforge.net/export/rss_sfnewreleases.php"));
    mimeType = content.getContentType();
    assertTrue("Expecting mime type of application/rss+xml but found " + mimeType,
        mimeType != null && mimeType.getPrimaryType().equals("application")
            && mimeType.getSubType().equals("rss+xml"));

  }

  /**
   * Test invalid Content type 
   *
   * @throws Exception Test fails
   */
  public void testInvalidContentType() throws Exception {

    HttpContent content = null;
    MimeType mimeType = null;

    //  obtain an invalid content type
    content = new HttpContent(new URL("http://bad.slashdot.org/"));
    mimeType = content.getContentType();
    assertTrue("Expecting mime type of null but found " + mimeType,
        mimeType == null);
  }

  public void testValidInputStream() throws Exception {

    // obtain a valid content type
    HttpContent content = new HttpContent(new URL("http://google.com/"));
    InputStream stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);

    //  obtain an invalid content type
    content = new HttpContent(new URL("http://bad.google.org/"));
    try {
      stream = null;
      stream = content.newInputStream();
    }
    catch (UnknownHostException ex) {
      assertTrue("Expecting a null input stream ", stream == null);
    }

    // obtain a valid content type via https
    content = new HttpContent(new URL("https://sourceforge.net"));
    stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertTrue("Expecting data from inputstream ", dumpAndCheck(stream) > 0);
    stream.close();

    // obtain a valid content type of XML via http
    content = new HttpContent(new URL(
        "http://rss.news.yahoo.com/rss/topstories"));
    stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertTrue("Expecting data from inputstream ", dumpAndCheck(stream) > 0);
    stream.close();

    // obtain a valid content type of XML via https
    content = new HttpContent(new URL(
        "https://sourceforge.net/export/rss_sfnewreleases.php"));
    stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertTrue("Expecting data from inputstream ", dumpAndCheck(stream) > 0);
    stream.close();

    // obtain a valid content type of XML via https
    content = new HttpContent(new URL("http://purl.org/dc/elements/1.1"));
    stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertTrue("Expecting data from inputstream ", dumpAndCheck(stream) > 0);
    stream.close();

  }

  public void testInvalidInputStream() throws Exception {

    HttpContent content = null;
    InputStream stream = null;
    
    //  obtain an invalid content type
    content = new HttpContent(new URL("http://bad.google.org/"));
    try {
      stream = null;
      stream = content.newInputStream();
    }
    catch (UnknownHostException ex) {
      assertTrue("Expecting a null input stream ", stream == null);
    }
  }

  public void testRedirect() throws Exception {

    HttpContent content = null;
    
    // obtain a valid content type via a redirect site
    // content = new HttpContent(new URL("http://purl.org/dc/elements/1.1"));
    // mimeType = content.getContentType();
    // assertTrue("Expecting mime type of text/plain but found " + mimeType,
    //     mimeType != null && mimeType.getPrimaryType().equals("text")
    //         && mimeType.getSubType().equals("plain"));
    // assertEquals(content.getURI(), new URI("http://dublincore.org/2003/03/24/dces#"));

    InputStream stream = null;
    content = new HttpContent(new URL("http://purl.org/dc/elements/1.1"));
    stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertEquals("Expecting 263018 bytes from inputstream ", 263018, dumpAndCheck(stream));
    stream.close();
    
  }

  private int dumpAndCheck(InputStream stream) throws Exception {

    File tmpFile = TempDir.createTempFile("stream", ".dat");
    tmpFile.deleteOnExit();
    
    FileOutputStream out = new FileOutputStream(tmpFile);

    byte[] buffer = new byte[10000];
    int len;
    int size = 0;
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
      size = size + len;
    }
    out.close();
    return size;
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new HttpContentUnitTest("testValidContentType"));
    suite.addTest(new HttpContentUnitTest("testValidInputStream"));
    suite.addTest(new HttpContentUnitTest("testInvalidContentType"));
    suite.addTest(new HttpContentUnitTest("testInvalidInputStream"));
    suite.addTest(new HttpContentUnitTest("testRedirect"));

    return suite;
  }

}
