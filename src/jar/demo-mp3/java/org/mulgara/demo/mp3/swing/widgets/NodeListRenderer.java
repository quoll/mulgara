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

package org.mulgara.demo.mp3.swing.widgets;

// Java 2 standard packages
import javax.swing.*;
import java.awt.Component;
import java.awt.Color;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;
import java.net.URLEncoder;

import org.mulgara.demo.mp3.swing.ExceptionHandler;

import java.net.URLDecoder;

// Local packages


/**
 * Renders Nodes in a JList. If the node is a URIReference, only the trailing
 * fragment is rendered.
 *
 * @created 2004-12-14
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:10 $
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
public class NodeListRenderer implements ListCellRenderer {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(NodeListRenderer.class.
      getName());

  /** Encoding used to encode/decode URLs */
  private static final String URL_ENCODING = "UTF-8";

  /**
   * Default constructor
   */
  public NodeListRenderer() {
  }

  /**
   * getListCellRendererComponent
   *
   * @param list JList
   * @param value Object
   * @param index int
   * @param isSelected boolean
   * @param cellHasFocus boolean
   * @return Component
   */
  public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {

    JLabel label = new JLabel("", JLabel.LEFT);
    String text = "";
    if (isSelected) {
      label.setOpaque(true);
      label.setBackground(Color.BLACK);
      label.setForeground(Color.WHITE);
    }
    if (value != null) {
      if (value instanceof URIReference) {
        text = getTitle((URIReference) value);
      }
      else if (value instanceof Literal) {
        text = ((Literal) value).getLexicalForm();
      }
      else {
        text = value.toString();
      }
    } else {
      text = "<any>";
    }
    label.setText(text);
    return label;
  }

  /**
   * Returns a short title for the URI of a URIReference. Displays the text
   * after the last '/' or '#' or the entire URI if it does not contain any '/'
   * or '#' characters.
   *
   * @return String
   * @param resource URIReference
   */
  private String getTitle(URIReference resource) {
    String uri = (resource == null) ? "" : "" + resource.getURI();
    String text = uri;
    int lastChar = 0;
    int lastHash = uri.lastIndexOf("#");
    int lastSlash = uri.lastIndexOf("/");
    if ((lastHash > 0)
        || (lastSlash > 0)) {
      lastChar = Math.max(lastHash, lastSlash);
      text = text.substring(lastChar);
    }
    return decodeURL(text);
  }

  /**
   * Decodes the String using an URLDecoder.
   * @param url String
   * @return String
   */
  private String decodeURL(String url) {
    try {
      return URLDecoder.decode(url, URL_ENCODING);
    }
    catch (Exception exception) {
      ExceptionHandler.handleException(exception);
      return url;
    }
  }

}
