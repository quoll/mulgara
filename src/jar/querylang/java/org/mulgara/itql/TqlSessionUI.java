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
 */

package org.mulgara.itql;

/**
 * Swing based iTQL session command line shell.
 *
 * @created 2004-01-15
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.*;
import javax.swing.text.*;

import org.apache.log4j.*;
import org.jrdf.graph.Node;

import org.mulgara.query.Answer;

@SuppressWarnings("serial")
public class TqlSessionUI extends JScrollPane implements Runnable,
    KeyListener, java.awt.event.MouseListener, ActionListener {

  /** The logging category to log to */
  private final static Logger log = Logger.getLogger(TqlSessionUI.class);
  
  private static final String NEWLINE = System.getProperty("line.separator");
  
  /** The bold font used for output */
  private final Font boldFont = new Font("Monospaced", Font.BOLD, 12);
  
  /** The iTQL session to send queries and used to send results. */
  private TqlSession tqlSession;

  /** Used to pipe input. */
  private InputStream inPipe;

  /** The list of history items. */
  private ArrayList<String> history = new ArrayList<String>();

  /** Current index into the history. */
  private int historyIndex = 0;

  /** Current cursor position. */
  private int cursorPosition = 0;

  /** The UI widget for displaying all text. */
  private JTextPane text;

  /** The default styled document. */
  private DefaultStyledDocument doc;

  /** Popup menu for Windows users. */
  private JPopupMenu popupMenu = new JPopupMenu();

  /** Whether we are running a command still. */
  private volatile boolean runningCommand = false;
  
  /**
   * Create a new UI representation.
   * @param newTqlSession the itql session to call when we receive commands and
   *        when we want to display them.
   * @param inStream The console input for this session.
   * @param outStream The console output for this session.
   */
  public TqlSessionUI(TqlSession newTqlSession, InputStream inStream, OutputStream outStream) {
    super();
    tqlSession = newTqlSession;
    doc = new DefaultStyledDocument();
    text = new PasteablePane(doc);
    text.setFont(new Font("Monospaced", Font.PLAIN, 12));
    text.setMargin(new Insets(5, 5, 5, 5));
    text.addKeyListener(this);
    text.addMouseListener(this);
    setViewportView(text);

    // Consume middle click to handle properly for Unix/Linux
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    toolkit.addAWTEventListener(new MouseListener(), AWTEvent.MOUSE_EVENT_MASK);

    // Add popup menu for Windows users.
    JMenuItem copyItem = new JMenuItem("Copy");
    JMenuItem pasteItem = new JMenuItem("Paste");
    popupMenu.add(copyItem);
    popupMenu.add(pasteItem);
    copyItem.addActionListener(this);
    pasteItem.addActionListener(this);

    inPipe = inStream;

    // Start the inpipe watcher
    new Thread(this).start();
    requestFocus();
  }

  public void requestFocus() {

    super.requestFocus();
    text.requestFocus();
  }

  /**
   * Handle key pressed event.
   *
   * @param e the key that was pressed.
   */
  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {

      // Enter pressed
      case (KeyEvent.VK_ENTER):

        if (e.getID() == KeyEvent.KEY_PRESSED) {

          if (!runningCommand) {

            enterPressed();
            cursorPosition = textLength();
            text.setCaretPosition(cursorPosition);
          }
        }
        e.consume();
        text.repaint();
      break;

      // Up history
      case (KeyEvent.VK_UP):

        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyUp();
        }
        e.consume();
      break;

      // Down history
      case (KeyEvent.VK_DOWN):
        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyDown();
        }
        e.consume();
      break;

      // Left or delete.
      case (KeyEvent.VK_LEFT):
      case (KeyEvent.VK_DELETE):

        if (text.getCaretPosition() <= cursorPosition) {

          e.consume();
        }
      break;

      // Go right.
      case (KeyEvent.VK_RIGHT):

        if (text.getCaretPosition() < cursorPosition) {

          // move caret first!
        }
        text.repaint();
      break;

      // Control-A go to start of line.
      case (KeyEvent.VK_A):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(cursorPosition);
          e.consume();
        }
      break;

      // Control-C copy the text.
      case (KeyEvent.VK_C):

        if (text.getSelectedText() == null) {

          text.copy();
          e.consume();
        }
      break;


      // Control-E go to end of line.
      case (KeyEvent.VK_E):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(textLength());
          e.consume();
        }
      break;

      // Control-U remove line
      case (KeyEvent.VK_U):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          replaceText("", cursorPosition, textLength());
          historyIndex = 0;
          e.consume();
        }
      break;

      // Home go to start of line
      case (KeyEvent.VK_HOME):

        text.setCaretPosition(cursorPosition);
        e.consume();
      break;

      // Go to end of line
      case (KeyEvent.VK_END):

        text.setCaretPosition(textLength());
        e.consume();
      break;

      // Ignore modifiers
      case (KeyEvent.VK_ALT):
      case (KeyEvent.VK_CAPS_LOCK):
      case (KeyEvent.VK_CONTROL):
      case (KeyEvent.VK_ESCAPE):
      case (KeyEvent.VK_F1):
      case (KeyEvent.VK_F2):
      case (KeyEvent.VK_F3):
      case (KeyEvent.VK_F4):
      case (KeyEvent.VK_F5):
      case (KeyEvent.VK_F6):
      case (KeyEvent.VK_F7):
      case (KeyEvent.VK_F8):
      case (KeyEvent.VK_F9):
      case (KeyEvent.VK_F10):
      case (KeyEvent.VK_F11):
      case (KeyEvent.VK_F12):
      case (KeyEvent.VK_INSERT):
      case (KeyEvent.VK_META):
      case (KeyEvent.VK_PAUSE):
      case (KeyEvent.VK_PRINTSCREEN):
      case (KeyEvent.VK_SHIFT):
      case (KeyEvent.VK_SCROLL_LOCK):

        // Do nothing.

      break;

      // Handle normal characters
      default:

        if ( (e.getModifiers() & (InputEvent.ALT_MASK | InputEvent.CTRL_MASK |
            InputEvent.META_MASK)) == 0) {

          if (text.getCaretPosition() < cursorPosition) {

            text.setCaretPosition(textLength());
          }
          text.repaint();
        }

        // Handle back space - don't let it go too far back.
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.paramString().indexOf("Backspace") != -1) {

          if (text.getCaretPosition() <= cursorPosition) {

            e.consume();
            break;
          }
        }
      break;
    }
  }

  public void keyTyped(KeyEvent e) {

    if (e.paramString().indexOf("Backspace") != -1) {

      if (text.getCaretPosition() <= cursorPosition) {

        e.consume();
      }
    }
  }

  public void keyReleased(KeyEvent e) {

    // Do nothing.
  }

  public void mouseClicked(MouseEvent e) {

    // Do nothing.
  }

  public void mouseEntered(MouseEvent e) {

    // Do nothing.
  }

  public void mouseExited(MouseEvent e) {

    // Do nothing.
  }

  public void mousePressed(MouseEvent e) {

    if (e.isPopupTrigger()) {

      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void mouseReleased(MouseEvent e) {

    if (e.isPopupTrigger()) {

      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void actionPerformed(ActionEvent event) {

    String eventOccurred = event.getActionCommand();

    if (eventOccurred.equals("Copy")) {

      text.copy();
    } else if (eventOccurred.equals("Paste")) {

      text.paste();
    }
  }

  /**
   * Returns the length of the current text buffer.
   *
   * @return length of the current text buffer.
   */
  private int textLength() {
    return text.getDocument().getLength();
  }

  /**
   * Replaces the given string to a position in the currently displayed line.
   *
   * @param newString the string to add.
   * @param start the starting position.
   * @param end the end position.
   */
  private void replaceText(String newString, int start, int end) {
    text.select(start, end);
    text.replaceSelection(newString);
  }

  /**
   * When the enter key has been pressed process the current command.
   */
  private void enterPressed() {
    String command = getCommand();

    // Create null command.
    if (command.length() != 0) {
      // Put the command at the end of the array.
      history.add(command);

      command = command + NEWLINE;

      // If the array gets too large remove the last entry.
      if (history.size() > 30) history.remove(0);
      
      // Indicate that we are running a command.
      runningCommand = true;

      // Create a new thread and start it.
      ExecutionThread execThread = new ExecutionThread("execThread", command);
      execThread.start();
    } else {
      // We've just hit enter so print the prompt.
      printPrompt();
    }
  }

  /**
   * Prints out the prompt.
   */
  public void printPrompt() {
    print(NEWLINE + (tqlSession.isCommandIncomplete() ? TqlSession.PS2 : TqlSession.PROMPT));
    historyIndex = 0;
    text.repaint();
  }

  /**
   * Returns the current command.
   *
   * @return the current command.
   */
  private String getCommand() {
    String command = "";
    try {
      command = text.getText(cursorPosition, textLength() - cursorPosition);
    } catch (BadLocationException e) {
      log.error("Failed to get text command at position: " + cursorPosition, e);
    }
    return command;
  }

  /**
   * Display the next command in the history buffer.
   */
  private void historyUp() {
    // Ensure there's a history and that the index never goes above the array size.
    if ((history.size() != 0) && (historyIndex != history.size())) {
      historyIndex++;
      displayHistoryLine();
    }
  }

  /**
   * Display the previous command in the history buffer.
   */
  private void historyDown() {

    // Ensure there's a history and that the index is initially above 1.
    if ((history.size() != 0) && (historyIndex > 1)) {

      historyIndex--;
      displayHistoryLine();
    }
  }

  /**
   * Displays the history line to the screen.
   */
  private void displayHistoryLine() {
    String showline = (String) history.get(history.size() - historyIndex);
    replaceText(showline, cursorPosition, textLength());
    text.setCaretPosition(textLength());
    text.repaint();
  }

  /**
   * Prints a message to the UI with a line separator.
   *
   * @param message the message to display.
   */
  public void println(String message) {
    print(message + NEWLINE);
    text.repaint();
  }

  /**
   * Prints empty line.
   */
  public void println() {
    print(NEWLINE);
    text.repaint();
  }

  /**
   * Prints a message to the UI. Sends a little "closure" to the UI thread.
   * @param message the message to display.
   */
  public void print(final String message) {
    invokeAndWait(new Runnable() {
      public void run() {
        append(message);
        cursorPosition = textLength();
        text.setCaretPosition(cursorPosition);
      }
    });
  }


  /**
   * Adds a text to the UI. Sends a little "closure" to the UI thread.
   * @param cmd the command to send.
   */
  public void injectCommand(final String cmd) {
    invokeAndWait(new Runnable() {
      public void run() {
        append(cmd);
        text.setCaretPosition(cursorPosition);
        if (cmd.endsWith(";") || cmd.endsWith("\n")) enterPressed();
      }
    });
  }


  /**
   * Print out an error message to the UI.
   * @param errorMessage the error message to display.
   */
  public void error(String errorMessage) {
    print(errorMessage, Color.red);
  }


  /**
   * Print out the message with the given color using the current font.
   * @param message the message to display.
   * @param color the color to use.
   */
  public void print(String message, Color color) {
    print(message, null, color);
  }


  /**
   * Print out the message with the given font and colour.
   * Uses invoke and wait to send a "closure" to the UI thread.
   * @param message the message to display.
   * @param font the font to use.
   * @param color the color to use.
   */
  public void print(final String message, final Font font, final Color color) {
    invokeAndWait(new Runnable() {
      public void run() {
        try {
          AttributeSet oldStyle = text.getCharacterAttributes();
          setStyle(font, color);
          append(message);
          cursorPosition = textLength();
          text.setCaretPosition(cursorPosition);
          text.setCharacterAttributes(oldStyle, true);
        } catch (Exception e) {
          log.error("Error when printing: " + message, e);
        }
      }
    });
  }


  /**
   * Print out the message followed by a newline with the given font and colour.
   * @param message the message to display.
   * @param font the font to use.
   * @param color the color to use.
   */
  public void println(final String message, final Font font, final Color color) {
    print(message + NEWLINE, font, color);
    text.repaint();
  }


  /**
   * Sets the new style of a font and color to the text.
   * @param font the new font.
   * @param color the new color.
   * @return the attributes of the given font and color.
   */
  private AttributeSet setStyle(Font font, Color color) {
    MutableAttributeSet attr = new SimpleAttributeSet();
    StyleConstants.setForeground(attr, color);
    // Don't set if null
    if (font != null) {
      StyleConstants.setBold(attr, font.isBold());
      StyleConstants.setFontFamily(attr, font.getFamily());
      StyleConstants.setFontSize(attr, font.getSize());
    }
    text.setCharacterAttributes(attr, false);
    return text.getCharacterAttributes();
  }


  /**
   * Append the given string to the existing string.
   * @param newString the string to append to.
   */
  private void append(String newString) {
    int length = textLength();
    text.select(length, length);
    text.replaceSelection(newString);
  }


  /**
   * Thread that reads the inPipe, and prints the output.
   */
  public void run() {
    try {
      byte[] buffer = new byte[255];
      int read;
      log.info("Starting input reader");
      while ((read = inPipe.read(buffer)) != -1) {
        injectCommand(new String(buffer, 0, read));
      }
    } catch (IOException e) {
      log.error("Error reading input", e);
    }
    log.warn("End of input");
  }


  /**
   * If not in the event thread run via SwingUtilities.invokeAndWait().
   * @param runnable The operation that a client wants the UI to perform. Like a "closure".
   */
  private void invokeAndWait(Runnable runnable) {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception e) {
        log.error("Error while executing invoke and wait", e);
      }
    } else {
      runnable.run();
    }
  }


  /**
   * Extension to JTextPane to put all pastes at the end of the command line.
   */
  class PasteablePane extends JTextPane {

    public PasteablePane(StyledDocument doc) {
      super(doc);
    }

    public void paste() {
      super.paste();
    }
  }

  /** Class to listen for mouse press events. */
  class MouseListener implements AWTEventListener {
    public void eventDispatched(AWTEvent event) {
      MouseEvent me = (MouseEvent)event;
      if (me.getButton() == MouseEvent.BUTTON2) me.consume();
    }
  }


  /**
   * Executes the command in a separate thread and display the results.
   */
  class ExecutionThread extends Thread {

    /** The command to execute. */
    private String command;

    /**
     * Create a new execution thread.
     * @param threadName the name of the thread.
     * @param newCommand the iTQL command to execute.
     */
    public ExecutionThread(String threadName, String newCommand) {
      super(threadName);
      command = newCommand;
    }


    /**
     * Run the command and display answer results.
     */
    public void run() {
      tqlSession.executeCommand(command);
      println();

      List<Answer> answers = tqlSession.getLastAnswers();
      List<String> messages = tqlSession.getLastMessages();

      if (answers.isEmpty()) {
        for (String message: messages) println(message, boldFont, Color.BLACK);
      } else {
        int answerIndex = 0;
        while (answerIndex < answers.size()) {
          String lastMessage = (String)messages.get(answerIndex);
          try {
            // Assume the same number of answers and messages
            Answer answer = answers.get(answerIndex);
  
            // If there's more than one answer print a heading.
            if (answers.size() > 1) {
              println();
              // If there's more than one answer add an extra line before the heading.
              println("Executing Query " + (answerIndex+1), boldFont, Color.BLACK);
            }
  
            // print the results
            if (answer != null) {
              boolean hasAnswers = true;
  
              long rowCount = 0;
              answer.beforeFirst();
              if (answer.isUnconstrained()) {
                println("[ true ]");
                rowCount = 1;
              } else {
                if (!answer.next()) {
                  print("No results returned.", boldFont, Color.BLACK);
                  hasAnswers = false;
                } else {
                  do {
                    rowCount++;
                    print("[ ");
                    for (int index = 0; index < answer.getNumberOfVariables(); index++) {
                      Object object = answer.getObject(index);
                      assert(object instanceof Answer) ||
                            (object instanceof Node  ) ||
                            (object == null);
                      print(String.valueOf(object));
                      if (index < (answer.getNumberOfVariables() - 1)) print(", ");
                    }
                    println(" ]");
                  } while (answer.next());
                }
              }
              if (hasAnswers) println(rowCount + " rows returned.", boldFont, Color.BLACK);
              answer.close();
            }
          } catch (Exception te) {
            // Failed to iterate over or retrieve the answer.
            log.fatal("Failed to retrieve or iterate over answer", te);
            error("Failed to get answer");
          }
  
          if ((lastMessage != null) && (!lastMessage.equals(""))) print(lastMessage, boldFont, Color.BLACK);
  
          // If there's more than one answer add a new line.
          if (answers.size() > 1)  println();
  
          // Increment index
          answerIndex++;
        }
      }

      // Signal that the command has finished and display prompt
      runningCommand = false;
      printPrompt();
    }
  }
  
}
