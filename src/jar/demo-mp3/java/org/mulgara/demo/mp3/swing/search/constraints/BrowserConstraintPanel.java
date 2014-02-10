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

package org.mulgara.demo.mp3.swing.search.constraints;

// Java 2 standard packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.Container;
import java.awt.event.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.util.*;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.swing.*;
import org.mulgara.demo.mp3.swing.search.*;
import org.mulgara.demo.mp3.swing.widgets.*;


/**
 * Contains a PropertyComboBox and ValueList that together represent a
 * Predicate-Object constraint. Represents a single constraint in the
 * ConstraintPanel.
 *
 * @created 2004-12-07
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
public class BrowserConstraintPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(BrowserConstraintPanel.class.
      getName());

  /** Default preferred scrollpane width */
  private static final int DEFAULT_WIDTH = 250;

  /** Default preferred scrollpane height */
  private static final int DEFAULT_HEIGHT = 300;

  /** Number of panels that should be visible in one screen (parent size) */
  private static final float VISIBLE_PANELS = 4.0F;

  /** The percentage of the parent container's available space to occupy */
  private static final float HEIGHT_SCALE = 0.8F;

  /** The percentage of the parent container's available space to occupy */
  private static final float WIDTH_SCALE = 0.95F;

  /** List of Schema properties to choose from */
  private PropertyComboBox propertyCombo = null;

  /** Used to detect Property selections */
  private ItemListener propertyListener = null;

  /** List of values (property related) to choose from */
  private ValueList valueList = null;

  /** Used to detect Value selections */
  private ListSelectionListener valueListener = null;

  /** Controls and Manages the Panel */
  private ConstraintController model = null;

  /** Used to adjust scrollpane size when resized */
  private BrowserPanel parent = null;

  /** Used to add a new panel to the Parent */
  private IconButton addButton = null;

  /** Used to remove the panel from its Parent */
  private IconButton removeButton = null;

  /**
   * Constructor
   *
   * @throws Exception
   * @param schemaProperties ClosableIterator
   * @param model ConstraintController
   */
  public BrowserConstraintPanel(ClosableIterator schemaProperties,
      ConstraintController model) throws Exception {
    if (model == null) {
      throw new IllegalArgumentException("BrowserConstraintModel is null");
    }
    this.model = model;
    propertyCombo = new PropertyComboBox(schemaProperties);
    propertyCombo.addItemListener(getPropertyListener());
    setup();
  }

  /**
   * Initializes and sets up components.
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    addButton = new IconButton(IconLoader.getIcon(IconLoader.ADD_16));
    removeButton = new IconButton(IconLoader.getIcon(IconLoader.REMOVE_16));

    //initialize
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    addListeners();
    resize();

    //add
    add(getNorthPanel(), BorderLayout.NORTH);
    add(new JScrollPane(getCenterPanel()), BorderLayout.CENTER);
  }

  /**
   * Clears the Panel and re-draws/sets up
   * @throws Exception
   */
  private void redraw() throws Exception {
    invalidate();
    removeAll();
    setup();
    repaint();
    revalidate();
  }

  /**
   * Returns the Top panel.
   * @return JPanel
   */
  private JPanel getNorthPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(propertyCombo, BorderLayout.CENTER);
    panel.add(getActionPanel(), BorderLayout.EAST);
    return panel;
  }

  /**
   * Returns the Middle panel.
   * @return JPanel
   */
  private JPanel getCenterPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    //Value list is only displayed when a property is selected
    if (valueList != null) {
      panel.add(valueList, BorderLayout.CENTER);
    }
    return panel;
  }

  /**
   * Executed when a property is selected from the List. A new value List must
   * be created and the Panel redrawn.
   * @throws Exception
   */
  private void propertySelected() {
    model.propertySelected(this, getProperty());
  }

  /**
   * Executed when a value is selected from the List.
   * @throws Exception
   */
  private void valueSelected() {
    model.valueSelected(this, getValue());
  }

  /**
   * Returns the selected Property or null if no Property is selected.
   * @return URIReference
   */
  public URIReference getProperty() {
    return (propertyCombo == null) ? null : propertyCombo.getSelected();
  }

  /**
   * Returns the selected Value or null if no Value is selected.
   * @return ObjectNode
   */
  public ObjectNode getValue() {
    return (valueList == null) ? null : valueList.getSelected();
  }

  /**
   * Sets the values to be displayed by the valueList.
   *
   * @param values ClosableIterator
   * @throws Exception
   */
  public void setValues(ClosableIterator values) throws Exception {
    valueList = new ValueList(values);
    valueList.addListSelectionListener(getValueListener());
    redraw();
  }

  /**
   * Returns the ItemListener used to detect a property selection.
   * @return ItemListener
   */
  public ItemListener getPropertyListener() {
    if (propertyListener == null) {
      propertyListener = new ItemListener() {
        /** @param e ItemEvent
         */
        public void itemStateChanged(ItemEvent e) {
          propertySelected();
        }
      };
    }
    return propertyListener;
  }

  /**
   * Returns the ListSelectionListener used to detect value selections.
   * @return ListSelectionListener
   */
  public ListSelectionListener getValueListener() {
    if (valueListener == null) {
      valueListener = new ListSelectionListener() {
        /** @param e ListSelectionEvent
         */
        public void valueChanged(ListSelectionEvent e) {
          valueSelected();
        }
      };
    }
    return valueListener;
  }

  /**
   * Sets the outer Container used for resizing.
   *
   * @param parent BrowserPanel
   * @throws Exception
   */
  public void setParent(BrowserPanel parent) throws Exception {
    if (parent == null) {
      throw new IllegalArgumentException("'parent' is null");
    }
    this.parent = parent;
    addParentListener(parent);
    resize();
  }

  /**
   * Re-adjusts the Panels size depending on the Parents size.
   *
   * @throws Exception
   */
  public void resize() throws Exception {
    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;
    if (parent != null) {
      width = (int) (parent.getWidth() / VISIBLE_PANELS);
      height = parent.getHeight();
    }
    width = (int) (width * WIDTH_SCALE);
    height = (int) (height * HEIGHT_SCALE);
    Dimension size = new Dimension(width, height);
    setPreferredSize(size);
    setSize(size);
  }

  /**
   * Adds a listener to the Container to detect resize events.
   * @param parent Container
   * @throws IllegalArgumentException
   */
  private void addParentListener(final Container parent) throws
      IllegalArgumentException {
    if (parent == null) {
      throw new IllegalArgumentException("'parent' is null");
    }
    parent.addComponentListener(new ComponentListener() {
      /** @param e ComponentEvent */
      public void componentHidden(ComponentEvent e) {
        doResize();
      }
      /** @param e ComponentEvent */
      public void componentMoved(ComponentEvent e) {
        doResize();
      }
      /** @param e ComponentEvent */
      public void componentResized(ComponentEvent e) {
        doResize();
      }
      /** @param e ComponentEvent */
      public void componentShown(ComponentEvent e) {
        doResize();
      }
      /** Resizes the BrowserConstraintPanel */
      private void doResize() {
        try {
          resize();
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
  }

  /**
   * Returns the panel used to perform operations on the browser constraint
   * panel.
   * @return JPanel
   */
  private JPanel getActionPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(addButton, BorderLayout.CENTER);
    panel.add(removeButton, BorderLayout.EAST);
    return panel;
  }

  /**
   * Adds Listeners to the add and remove buttons.
   */
  private void addListeners() {
    addButton.addActionListener(new ActionListener() {
      /**Add a new panel to the parent
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          getParentPanel().addNewBrowserConstraintPanel();
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    removeButton.addActionListener(new ActionListener(){
      /** Remove the panel from its parent
       * @param e ActionEvent
       */
      public void actionPerformed(ActionEvent e) {
        try {
          getParentPanel().removePanel(BrowserConstraintPanel.this);
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
  }

  /**
   * Returns the Parent BrowserPanel. Throws an Exception if it has not been set.
   * @throws IllegalStateException
   * @return BrowserPanel
   */
  private BrowserPanel getParentPanel() throws IllegalStateException {
    if (parent == null) {
      throw new IllegalStateException("Parent has not been set.");
    }
    return parent;
  }

}
