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

package org.mulgara.server.local;

// Java APIs
import java.net.URI;

// Log4j
import org.apache.log4j.*;

// Junit API
import junit.framework.*;

// Internal Mulgara APIs
import org.mulgara.itql.*;
import org.mulgara.query.*;
import org.mulgara.server.Session;
import org.mulgara.server.driver.*;


/**
 * Test case for {@link LocalSessionFactory}.
 *
 * @created 2003-02-09
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:01 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LocalSessionFactoryUnitTest extends TestCase {

  /**
   * init the logging class
   */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(LocalSessionFactoryUnitTest.class.getName());

  /**
   * Calls the super classes constructor.
   *
   * @param name the name of the JUnit task.
   */
  public LocalSessionFactoryUnitTest(String name) {
    super(name);
  }

  /**
   * Answer a test suite that runs the Graph and Reifier tests on GraphMem
   * and on  WrappedGraphMem, the latter standing in for testing
   * WrappedGraph.
   */
  public static TestSuite suite() {

    TestSuite result = new TestSuite();
    result.addTest(new LocalSessionFactoryUnitTest("testCreateLocalAndQuery"));
    return result;
  }

  /**
   * Test creating a local session, adding it to the Jena implementation and
   * querying it using the ItqlInterpreterBean.
   */
  @SuppressWarnings("deprecation")
  public void testCreateLocalAndQuery() {

    LocalSessionFactory factory = null;
    Session session = null;

    try {

      //connect
      String serverhost = "localhost";
      URI serverURI = new URI("rmi", serverhost, "/testServer", null);

      factory = (LocalSessionFactory)
          SessionFactoryFinder.newSessionFactory(serverURI, false);
      session = (Session) factory.newSession();

/*      //add
      GraphMulgaraMaker graphMaker = new GraphMulgaraMaker(session,
          serverURI,
          ReificationStyle.Minimal);
      ModelMulgaraMaker modelMaker = new ModelMulgaraMaker(graphMaker);
      com.hp.hpl.jena.rdf.model.Model model = modelMaker.createModel("testModel");
      model.add(model.createResource("info:foo"),
          model.createProperty("info:bar#", "baz"),
          "quuux");
*/
      //query
      String modelURI = serverURI.toString() + "#testModel";
      String query = "select $s $p $o from <" + modelURI + "> where $s $p $o;";

      // this bit, for instance, will not work.
      ItqlInterpreterBean interpreter = new ItqlInterpreterBean(session, null);

      //create the model and insert data
      interpreter.executeQueryToString("create <" + modelURI + "> ;");
      interpreter.executeQueryToString("insert <info:foo> <info:bar#baz> " +
          "'quuux' into <" + modelURI + "> ;");

      Answer answer = interpreter.executeQuery(query);
      answer.beforeFirst();
      answer.next();
      Object subject = answer.getObject(0);
      Object predicate = answer.getObject(1);
      Object object = answer.getObject(2);

      assertTrue("Subject should be: ", subject.toString().equals("info:foo"));
      assertTrue("Predicate should be: ",
          predicate.toString().equals("info:bar#baz"));
      assertTrue("Object should be: ",
          object.toString().equals("\"quuux\""));
      answer.close();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Should have successfully created and queries the database");
    }
    finally {
      if (session != null) {
        try {
          session.close();
        }
        catch (QueryException qe) {
          qe.printStackTrace();
          fail("Should successfully close the factory");
        }
      }

      if (factory != null) {
        try {
          factory.delete();
        }
        catch (QueryException qe) {
          qe.printStackTrace();
          fail("Should successfully delete the factory");
        }
      }
    }
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }
}
