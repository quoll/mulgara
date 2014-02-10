/*
 * Copyright 2010 Paul Gearon
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

package org.mulgara.content.rdfa;

// Third party packages
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jrdf.graph.AbstractTriple;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.mulgara.content.Content;
import org.mulgara.query.rdf.MimeTypes;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.TestResolverSession;


/**
 * @created August 9, 2010
 * @author Paul Gearon
 */
public class RdfaStatementsUnitTest extends TestCase {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RdfaStatementsUnitTest.class.getName());

  //
  // Constructors
  //

  /**
   * Construct a test.
   *
   * @param name  the name of the test to construct
   */
  public RdfaStatementsUnitTest(String name) {
    super(name);
  }

  //
  // Methods implementing TestCase
  //

  public void setup() {
  }

  /**
   * Hook from which the test runner can obtain a test suite.
   *
   * @return the test suite
   */
  public static Test suite() {
    return new TestSuite(RdfaStatementsUnitTest.class);
  }


  /**
   * Test {@link rdfaStatements} parsing a file.
   */
  public void testParse() throws Exception {
    Content content = new StringContent(HCARD);
    // Content content = new HttpContent(URI.create("http://examples.tobyinkster.co.uk/hcard"));
    System.err.println(content.getContentType().toString());
    StatementParser parser = new StatementParser(content, new TestResolverSession());
    parser.run();
  }

  public void testParseData() throws Exception {
    ResolverSession session = new TestResolverSession();
    Content content = new StringContent(HCARD);
    // Content content = new HttpContent(URI.create("http://examples.tobyinkster.co.uk/hcard"));
    List<Triple> triples = toTriples(new RdfaStatements(session, content), session);
    assertEquals(8, triples.size());
    URI page = URI.create("http://examples.tobyinkster.co.uk/hcard");
    for (int i = 0; i < 2; i++) assertEquals(page, ((URIReference)triples.get(i).getSubject()).getURI());
    assertTrue(triples.get(2).getSubject().isBlankNode());
    URI jack = URI.create("http://examples.tobyinkster.co.uk/hcard#jack");
    for (int i = 3; i < 8; i++) {
      assertTrue(triples.get(i).getSubject().isURIReference());
      assertEquals(jack, ((URIReference)triples.get(i).getSubject()).getURI());
    }
  }

  static List<Triple> toTriples(Statements s, ResolverSession session) throws Exception {
    List<Triple> triples = new ArrayList<Triple>();
    s.beforeFirst();
    while (s.next()) {
      triples.add(new TripleImpl(
          session.globalize(s.getSubject()),
          session.globalize(s.getPredicate()),
          session.globalize(s.getObject())
      ));
    }
    return triples;
  }
  
  static final String HCARD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\"\n" +
  "          \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n" +
  "\n" +
  "<html xml:lang=\"en\"\n" +
  " xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
  " xmlns:vcard=\"urn:ietf:rfc:2426#\"\n" +
  " xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
  " xmlns:w3card=\"http://www.w3.org/2006/vcard/ns#\"\n" +
  " xmlns:pim=\"http://www.w3.org/2000/10/swap/pim/contact#\"\n" +
  " xmlns:dc=\"http://purl.org/dc/terms/\">\n" +
  "\n" +
  " <head>\n" +
  "   <title>Complex hCard + RDFa Example</title>\n" +
  "   <link rel=\"foaf:primaryTopic\" href=\"#jack\" />\n" +
  " </head>\n" +
  "\n" +
  " <body style=\"max-width:50em\">\n" +
  "\n" +
  "   <p style=\"font-style:italic\">No, I'm not obsessed with Jack Bauer (well, maybe\n" +
  "   a little bit). <span property=\"dc:abstract\">This page is intended to be a demonstration of\n" +
  "   the use of RDFa (including FOAF, Dublin Core and W3C PIM vocabularies) in\n" +
  "   conjunction with Microformats (including hCard and rel-tag).</span></p>\n" +
  "\n" +
  "   <div id=\"jack\" class=\"vcard\" typeof=\"pim:Male\">\n" +
  "\n" +
  "     <img class=\"photo\" alt=\"Jack could kick your ass.\" src=\"JackB4.jpg\" style=\"float:right;\n" +
  "     margin:1em 0 1em 2em;    /* Don't let Jack too near the text as he might kick its ass! */\n" +
  "     border: 4px solid black; /* But don't fool yourself into thinking that will constrain him! */\"\n" +
  "     />\n" +
  "\n" +
  "     <h1 class=\"fn\">Jack Bauer</h1>\n" +
  "\n" +
  "     <p class=\"org\">\n" +
  "       <span about=\"#jack\" property=\"w3card:category\" class=\"organization-name\">Counter-Terrorist Unit</span>\n" +
  "       (<span class=\"organization-unit\">Los Angeles Division</span>)\n" +
  "     </p>\n" +
  "\n" +
  "     <p class=\"adr\">\n" +
  "       <span class=\"street-address\">10201 W. Pico Blvd.</span><br />\n" +
  "       <span class=\"locality\">Los Angeles</span>,\n" +
  "       <span class=\"region\">CA</span>\n" +
  "       <span class=\"postal-code\">90064</span><br />\n" +
  "       <span class=\"country-name\">United States</span><br />\n" +
  "       <small class=\"geo\" style=\"color:#999;font-size:67%\">34.052339;-118.410623</small>\n" +
  "     </p>\n" +
  "\n" +
  "     <h2>Assorted Contact Methods</h2>\n" +
  "     <ul about=\"#jack\">\n" +
  "       <li class=\"tel\">+1 (310) 597 3781 <span class=\"type\">work</span></li>\n" +
  "       <li><a rel=\"tag foaf:homepage\" href=\"http://en.wikipedia.org/wiki/Jack_Bauer\">I'm on Wikipedia</a> so you can leave a message on my user talk page.</li>\n" +
  "       <li rel=\"foaf:workInfoHomepage\"><a href=\"http://www.jackbauerfacts.com/\">Jack Bauer Facts</a></li>\n" +
  "       <li class=\"email\">j.bauer@la.ctu.gov.invalid</li>\n" +
  "       <li><a rel=\"w3card:mobileTel\" href=\"tel:+1-310-555-3781\">mobile phone</a></li>\n" +
  "     </ul>\n" +
  "\n" +
  "     <p class=\"note\">If I'm out in the field, you may be better off contacting <span class=\"agent vcard\">\n" +
  "     <a class=\"email fn\" href=\"mailto:c.obrian@la.ctu.gov.invalid\">Chloe O'Brian</a></span>\n" +
  "     if it's about work, or ask <span class=\"agent\">Tony Almeida</span> if you're interested\n" +
  "     in the CTU five-a-side football team we're trying to get going.</p>\n" +
  "\n" +
  "     <h2>Plan</h2>\n" +
  "     <p about=\"#jack\" property=\"foaf:plan\">I will kick your terrorist ass!</p>\n" +
  "\n" +
  "     <ins class=\"tel rev\" datetime=\"2008-07-20T21:00:00+0100\">\n" +
  "       <strong>Update!</strong>\n" +
  "       My new <span class=\"type\">home</span> phone number is\n" +
  "       <span class=\"value\">01632 960 123</span>.\n" +
  "     </ins>\n" +
  "   </div>\n" +
  "\n" +
  "\n" +
  "   <div style=\"border-top: 1px solid silver;margin-top:2em;padding-top:0.67em\">\n" +
  "\n" +
  "     <a href=\"http://validator.w3.org/check?uri=referer\">validate</a> |\n" +
  "\n" +
  "     <a href=\"http://srv.buzzword.org.uk/vcard/referer\">cognify (vCard)</a> | \n" +
  "\n" +
  "     <a href=\"http://srv.buzzword.org.uk/jcard/referer\">cognify (jCard)</a> | \n" +
  "\n" +
  "     <a href=\"http://srv.buzzword.org.uk/rdf-xml/referer\">cognify (RDF)</a> | \n" +
  "\n" +
  "     tech:\n" +
  "     <a href=\"http://rdfa.info/\"><img style=\"border:0;vertical-align:middle\" src=\"http://buzzword.org.uk/cognition/buttons/rdfa.png\" alt=\"RDFa,\" /></a>\n" +
  "     <a href=\"http://www.foaf-project.org/\"><img style=\"border:0;vertical-align:middle\" src=\"http://buzzword.org.uk/cognition/buttons/foaf.png\" alt=\"FOAF,\" /></a>\n" +
  "     <a href=\"http://dublincore.org/\"><img style=\"border:0;vertical-align:middle\" src=\"http://buzzword.org.uk/cognition/buttons/dc.png\" alt=\"Dublin Core and\" /></a>\n" +
  "     <a href=\"http://microformats.org/wiki/hcard\"><img style=\"border:0;vertical-align:middle\" src=\"http://buzzword.org.uk/cognition/buttons/hcard.png\" alt=\"hCard\" /></a>\n" +
  "\n" +
  "   </div>\n" +
  "\n" +
  " </body>\n" +
  "\n" +
  "</html>\n";
}

class StringContent implements Content {
  private final String content;

  public StringContent(String content) {
    this.content = content;
  }

  public MimeType getContentType()                       { return MimeTypes.TEXT_HTML; }
  public URI getURI()                                    { return URI.create("http://examples.tobyinkster.co.uk/hcard"); }
  public String getURIString()                           { return "http://examples.tobyinkster.co.uk/hcard"; }
  public OutputStream newOutputStream()                  { return null; }
  public boolean isStreaming()                           { return false; }

  public InputStream newInputStream() throws IOException {
    return new ByteArrayInputStream(content.getBytes("UTF-8"));
  }
}

class TripleImpl extends AbstractTriple {
  private static final long serialVersionUID = -26504102803266709L;
  TripleImpl(Node node, Node node2, Node node3) {
    subjectNode = (SubjectNode)node;
    predicateNode = (PredicateNode)node2;
    objectNode = (ObjectNode)node3;
  }
}

