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

package org.mulgara.resolver;

// Java 2 standard packages
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.ValidatingContentHandler;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 *  Mediates access to ContentHandlers
 *
 * @created 2004-10-07
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:23 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ContentHandlerManagerImpl implements ContentHandlerManager
{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(ContentHandlerManagerImpl.class);


  protected List<ContentHandler> contentHandlerList;
  protected ContentHandler defaultHandler;

  /**
   * @param defaultContentHandler  the content handler to use if the MIME type
   *   can't be determined; if <code>null</code>, no attempt will be made to
   *   deal with unclassified content
   */
  ContentHandlerManagerImpl(ContentHandler defaultContentHandler)
    throws ContentHandlerException
  {
    contentHandlerList  = new ArrayList<ContentHandler>();
    this.defaultHandler = defaultContentHandler;

    contentHandlerList.add(defaultContentHandler);
  }


  /**
   * Returns a ContentHandler that is capable of parsing the content.
   *
   * @param content Content
   * @throws QueryException
   * @return ContentHandler
   */
  public ContentHandler getContentHandler(Content content)
    throws ContentHandlerException, NotModifiedException
  {
    //validate
    if (content == null) {
      throw new IllegalArgumentException("Content is null");
    }

    if (logger.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder("Searching for handler for content ");
      if (content.getURI() != null) sb.append("<").append(content.getURI()).append("> ");
      if (content.isStreaming()) sb.append("from stream ");
      if (content.getContentType() != null) sb.append("with type ").append(content.getContentType());
      logger.debug(sb.toString());
      if (logger.isTraceEnabled()) logger.trace("getContentHandler stack trace", new Throwable());
    }
    
    // Prefer supplied MIME type when choosing handler.
    MimeType contentType = content.getContentType();
    if (contentType != null) {
      for (ContentHandler h : contentHandlerList) {
        Collection<MimeType> types = h.getContentTypes();
        if (types != null) {
          for (MimeType type : types) {
            if (type != null && type.match(contentType)) {
              if (logger.isDebugEnabled()) logger.debug(h.getClass() + " handles type " + contentType);
              return h;
            }
          }
        }
      }
    }
    
    // Content type wasn't specified or didn't match anything, so fall back to filename extension.
    if (content.getURI() != null) {
      // Peek at the file extension, if there is one.
      String extension = null;
      String path = content.getURI().getPath();
      if (path != null) {
        for (int i = path.length() - 1; i >= 0; i--) {
          char c = path.charAt(i);
          if (c == '.') {
            extension = path.substring(i + 1);
            break;
          } else if (c == '/') {
            break;
          }
        }
      }
      // Check for a handler that supports the extension.
      if (extension != null && extension.length() > 0) {
        for (ContentHandler h : contentHandlerList) {
          Collection<String> fileExts = h.getFileExtensions();
          if (fileExts != null) {
            for (String fileExt : fileExts) {
              if (extension.equalsIgnoreCase(fileExt)) {
                if (logger.isDebugEnabled()) logger.debug(h.getClass() + " handles file extension '" + extension + "'");
                return h;
              }
            }
          }
        }
      }
    }
    
    // This is a hack because the MBox content handler does a blind parse instead of properly
    // checking for content type or file extension.
    if (!content.isStreaming()) {
      if (logger.isDebugEnabled()) logger.debug("Content is not streaming; checking for validating handlers.");
      for (ContentHandler h : contentHandlerList) {
        if (h instanceof ValidatingContentHandler && ((ValidatingContentHandler)h).validate(content)) {
          if (logger.isDebugEnabled()) logger.debug(h.getClass() + " validated content.");
          return h;
        }
      }
    }

    if (defaultHandler == null) {
      if (content.getURI() != null) {
        throw new ContentHandlerException("Unable to determine content type for " + content.getURI());
      }
      if (content.getContentType() != null) {
        throw new ContentHandlerException("Unknown content type: " + content.getContentType());
      }
      throw new ContentHandlerException("Attempting to parse invalid content.");
    }
    assert defaultHandler != null;

    if (logger.isDebugEnabled()) logger.debug("Could not determine content handler, falling back to default " + defaultHandler.getClass());
    return defaultHandler;
  }


  public Statements blindParse(Content content, ResolverSession resolverSession)
    throws ContentHandlerException, NotModifiedException
  {
    //validate
    if (content == null) {
      throw new IllegalArgumentException("Content is null");
    } else if (resolverSession == null) {
      throw new IllegalArgumentException("ResolverSession is null");
    }

    if (defaultHandler == null) {
      if (content.getContentType() != null) {
        throw new ContentHandlerException("Unrecognized content type: " + content.getContentType());
      }
      if (content.getURI() != null) {
        throw new ContentHandlerException("Unable to determine type of " + content.getURI());
      }
      throw new ContentHandlerException("Attempted to parse invalid content");
    }
    assert defaultHandler != null;

    return defaultHandler.parse(content, resolverSession);
  }


  /**
   * Construct a {@link ContentHandler} and add it to the
   * {@link #contentLoaderList}.
   *
   * @param className  the name of a class implementing {@link ContentHandler}
   */
  void registerContentHandler(String className)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating content handler " + className);
    }

    // Validate "className" parameter
    if (className == null) {
      throw new IllegalArgumentException("Null \"className\" parameter");
    }

    Class<?> contentHandlerClass;
    try {
      contentHandlerClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      logger.warn("Error generating content handler", e);
      IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException(className + " is not installed");
      illegalArgumentException.initCause(e);
      throw illegalArgumentException;
    }
    assert contentHandlerClass != null;

    // Validate that the candidate supports the ContentHandler interface
    if (!ContentHandler.class.isAssignableFrom(contentHandlerClass)) {
      throw new IllegalArgumentException(
        className + " is not an " + ContentHandler.class.getName()
      );
    }

    try {
      // Instantiate the content handler
      ContentHandler handler = (ContentHandler)contentHandlerClass.getConstructor(new Class[] {})
                                                 .newInstance(new Object[] {});
      contentHandlerList.add(handler);
    } catch (IllegalAccessException e) {
      logger.warn("Error generating content handler", e);
      IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException("Unable to add content handler");
      illegalArgumentException.initCause(e);
      throw illegalArgumentException;
    } catch (InstantiationException e) {
      logger.warn("Error generating content handler", e);
      IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException("Unable to add content handler");
      illegalArgumentException.initCause(e);
      throw illegalArgumentException;
    } catch (InvocationTargetException e) {
      logger.warn("Error generating content handler", e);
      IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException("Unable to add content handler");
      illegalArgumentException.initCause(e);
      throw illegalArgumentException;
    } catch (NoSuchMethodException e) {
      logger.warn("Error generating content handler", e);
      IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException("Unable to add content handler");
      illegalArgumentException.initCause(e);
      throw illegalArgumentException;
    }
  }

}
