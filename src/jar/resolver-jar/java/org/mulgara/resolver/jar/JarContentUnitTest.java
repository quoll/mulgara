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

package org.mulgara.resolver.jar;

//Java 2 standard packages
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.activation.MimeType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mulgara.util.TempDir;


/**
 * Test cases for JarContent.
 *
 * @author Tate Jones
 *
 * @created 2004-11-09
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:46 $ by $Author: newmana $
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

public class JarContentUnitTest extends TestCase {

  /**
   * Data directory for test files
   */
  private static String libDirectory = System.getProperty("cvs.root")+"/lib";

  static {
    if ( System.getProperty("os.name").indexOf("Windows") >= 0 ) {
      libDirectory = "/"+libDirectory.replace('\\','/');
    }
  }
  
  /**
   * Create the testing class
   *
   * @param name The name of the test.
   */
  public JarContentUnitTest(String name) {
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

    JarContent content = null;
    MimeType mimeType = null;
    
    // obtain a valid content type via a redirect site 
    content = new JarContent(new URL("jar:file:"+libDirectory+"/junit-3.8.1.jar!/stylesheet.css"));
    mimeType = content.getContentType();
    assertTrue("Expecting mime type of text/plain but found " + mimeType,
        mimeType != null && mimeType.getPrimaryType().equals("content")
            && mimeType.getSubType().equals("unknown"));    
  }

  /**
   * Test invalid Content type 
   *
   * @throws Exception Test fails
   */
  public void testInvalidContentType() throws Exception {

    JarContent content = null;
    MimeType mimeType = null;
            
    content = new JarContent(new URL("jar:file:"+libDirectory+"/junit-3.8.1.jar!/bad"));

    mimeType = content.getContentType();
    assertTrue("Expecting mime type of null but found " + mimeType,
        mimeType == null );    
        
  }

  public void testValidInputStream() throws Exception {

    JarContent content = null;
    content = new JarContent(new URL("jar:file:"+libDirectory+"/junit-3.8.1.jar!/stylesheet.css"));
    InputStream stream = content.newInputStream();
    assertTrue("Expecting an input stream ", stream != null);
    assertTrue("Expecting 15064 bytes from inputstream ", dumpAndCheck(stream) == 1380);
    stream.close(); 
    
  }

  public void testInvalidInputStream() throws Exception {

    boolean exceptionThrown = false;
    JarContent content = null;
    content = new JarContent(new URL("jar:file:"+libDirectory+"/junit-3.8.1.jar!/bad"));
    try {
      content.newInputStream();
    } catch ( java.io.FileNotFoundException ex) {
      exceptionThrown = true;
    }
    assertTrue("Expecting a FileNotFoundException exception ", exceptionThrown);
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
    suite.addTest(new JarContentUnitTest("testValidContentType"));
    suite.addTest(new JarContentUnitTest("testInvalidInputStream"));
    suite.addTest(new JarContentUnitTest("testValidInputStream"));
    suite.addTest(new JarContentUnitTest("testInvalidInputStream"));

    return suite;
  }

}
