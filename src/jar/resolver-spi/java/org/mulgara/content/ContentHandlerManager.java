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

package org.mulgara.content;

// Java 2 standard packages
import java.io.InputStream;

// Local packages
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Mediates access to the system's ContentHandlers.  Manages the mapping from a
 * Content objects metadata/stream to the appropriate ContentHandler capable of
 * parsing it.
 *
 * @created 2004-09-10
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface ContentHandlerManager
{
  /**
   * Obtains a {@link ContentHandler} suitable for managing the given content.
   *
   * @param content  the content to handle, never <code>null</code>
   * @throws IllegalArgumentException if <var>content</var> is <code>null</code>
   * @throws NotModifiedException if the <var>content</var> is already cached
   */
  public ContentHandler getContentHandler(Content content)
    throws ContentHandlerException, NotModifiedException;

  /**
   * Will eventually try all content handers for unidentifiable streams,
   * currently defaults to RDFXML.
   *
   * Note: This method currently does not work, and won't until we can
   * successfully wrap arbitary {@link InputStream}s to ensure independent
   * parsing.
   *
   * @param content  the content to parse, never <code>null</code>
   * @param resolverSession  the context in which to localize RDF resources
   *   occuring within the <var>content</var>, never <code>null</code>
   * @throws IllegalArgumentException if <var>content</var> or
   *   <var>resolverSession</var> are <code>null</code>
   */
  public Statements blindParse(Content content, ResolverSession resolverSession)
    throws ContentHandlerException, NotModifiedException;

}
