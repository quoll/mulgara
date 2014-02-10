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

package org.mulgara.content.mbox;

// Java 2 standard packages
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.ValidatingContentHandler;
import org.mulgara.content.mbox.parser.model.exception.InvalidMBoxException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints in models defined by mbox files.
 *
 * @created 2004-10-11
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:39 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MBoxContentHandler implements ValidatingContentHandler {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(MBoxContentHandler.class);

  /** Don't know our MIME type. */
  public Collection<MimeType> getContentTypes() {
    return Collections.emptyList();
  }

  /** Don't know our filename extensions. */
  public Collection<String> getFileExtensions() {
    return Collections.emptyList();
  }

  /**
   * Parses the messages of an mbox file pointed to by the content object which
   * are then converted to a statements object.
   *
   * @param content The actual content we are going to be parsing
   * @param resolverSession The session in which this resolver is being used
   *
   * @return The parsed statements object
   *
   * @throws ContentHandlerException
   */
  public Statements parse(Content content, ResolverSession resolverSession) throws
      ContentHandlerException, NotModifiedException {

    // Container for our statements
    MBoxStatements statements = null;

    try {

      // Attempt to create the MBox statements
      statements = new MBoxStatements(content, resolverSession);
    } catch (TuplesException tuplesException) {

      throw new ContentHandlerException("Unable to create statements object from " +
                                        "content object: " + content.getURIString(),
                                        tuplesException);
    }

    return statements;
  }

  /**
   * @throws ContentHandlerException  always, as mboxes aren't pure metadata
   *   stores and don't make any sense to overwrite
   */
  public void serialize(Statements      statements,
                        Content         content,
                        ResolverSession resolverSession,
                        Map<String,URI> prefixMap)
    throws ContentHandlerException
  {
    throw new ContentHandlerException("Mboxes are not writable.");
  }

  //
  // Internal methods
  //

  /**
   * Validates the given input stream and determines whether it is a proper mbox
   * or not.
   *
   * @param stream The stream to validate
   *
   * @throws InvalidMBoxException
   */
  public boolean validate(Content content) throws NotModifiedException {
    if (content == null) return false;
    
    InputStream stream;
    try {
      stream = content.newInputStream();
    } catch (IOException e) {
      return false;
    }
    if (stream == null) {
      // The mbox file cannot be null
      return false;
    }

    // Create an input stream reader
    InputStreamReader inputReader = new InputStreamReader(stream);

    // Create a buffered reader to read our file
    BufferedReader reader = new BufferedReader(inputReader);

    // Container for our line of the file
    String line = "";

    try {

      while (line != null && line.equals("")) {

        // Get the first line of text from the mbox file
        line = reader.readLine();
      }
    } catch (IOException ioException) {
      return false;
    }

    if (line == null || !line.toLowerCase().startsWith("from ")) {
      return false;
    } else {

      try {

        // Get the next line of text (if any)
        line = reader.readLine();
      } catch (IOException ioException) {
        return false;
      }
      if (line != null && line.length() > 0 &&
          !line.split(" ")[0].endsWith(":")) {

        // The mbox is not RFC822 compliant if the next line is not a header
        return false;
      }
    }

    try {

      reader.close();
    } catch (IOException ioException) {
      return false;
    }
    
    return true;
  }
}
