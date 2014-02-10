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

package org.mulgara.content.mbox.parser;

// Java standard packages
import java.io.*;
import java.util.*;


/**
 * The class of values returned from {@link MimeMessageToPart#process}.
 *
 * @created 2003-03-13
 *
 * @author <a href="http://staff.pisoftware.com/raboczi"/>Simon Raboczi</a>
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/LICENCE">License description</A>
 */

public class MimeMessageToPartBean {

  // Message header fields
  private final List<QuollEmailAddress> bccAddressList = new ArrayList<QuollEmailAddress>();
  private final List<QuollEmailAddress> ccAddressList = new ArrayList<QuollEmailAddress>();
  private Date date = null;
  private QuollEmailAddress fromAddress = null;
  private String messageID = null;
  private String subject = null;
  private final List<QuollEmailAddress> toAddressList = new ArrayList<QuollEmailAddress>();
  private final List<String> referenceList = new ArrayList<String>();

  /**
   * List of {@link File}s for each body or attachment part extracted from the
   * message.
   */
  private final List<Attachment> partList = new ArrayList<Attachment>();

  //
  // Methods
  //

  /**
   * @param address  a blind carbon copy address
   * @throws IllegalArgumentException  if <var>address</var> is <code>null</code>
   */
  public void addBCCAddress(QuollEmailAddress address) {
    if (address == null) throw new IllegalArgumentException("Null \"address\" parameter");
    bccAddressList.add(address);
  }

  /**
   * @return a non-<code>null</code> list of {@link String}-valued blind carbon copy addresses
   */
  public List<QuollEmailAddress> getBCCAddresses() {
    return bccAddressList;
  }

  /**
   * @param address  a carbon copy address
   * @throws IllegalArgumentException  if <var>address</var> is <code>null</code>
   */
  public void addCCAddress(QuollEmailAddress address) {
    if (address == null) throw new IllegalArgumentException("Null \"address\" parameter");
    ccAddressList.add(address);
  }

  /**
   * @return a non-<code>null</code> list of {@link String}-valued carbon copy addresses
   */
  public List<QuollEmailAddress> getCCAddresses() {
    return ccAddressList;
  }

  /**
   * @return the message's sent date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date  a sent date for the message
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the sender's address
   */
  public QuollEmailAddress getFromAddress() {
    return fromAddress;
  }

  /**
   * @param fromAddress  a sender address for the message
   */
  public void setFromAddress(QuollEmailAddress fromAddress) {
    this.fromAddress = fromAddress;
  }

  /**
   * @return the message ID
   */
  public String getMessageID() {
    return messageID;
  }

  /**
   * @param messageID  a message ID
   */
  public void setMessageID(String messageID) {
    this.messageID = messageID;
  }

  /**
   * @param partFile  the file containing the text of a body or attachment
   */
  public void addPart(Attachment partFile) {
    if (partFile == null) throw new IllegalArgumentException("Null filename for message page");
    partList.add(partFile);
  }

  /**
   * @return the number of body/attachment parts
   */
  public int getPartCount() {
    return partList.size();
  }

  /**
   * @return all the body/attachment parts
   */
  public Attachment[] getParts() {
    return partList.toArray(new Attachment[partList.size()]);
  }

  /**
   * @return subject  the message's subject line, or <code>null</code> if the
   *   message had no subject header
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject  a message's subject line
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @param address  a non-<code>null</code> recipient address
   * @throws IllegalArgumentException if a <code>null</code> <var>address</var> is passed
   */
  public void addToAddress(QuollEmailAddress address) {
    if (address == null) throw new IllegalArgumentException("Null \"address\" parameter");
    toAddressList.add(address);
  }

  /**
   * @return a non-<code>null</code> list of recipient addresses
   */
  public List<QuollEmailAddress> getToAddresses() {
    return toAddressList;
  }

  /**
   * @param reference  References another email message
   * @throws IllegalArgumentException  if <var>reference</var> is
   *   <code>null</code>
   */
  public void addReference(String reference) {
    if (reference == null) throw new IllegalArgumentException("Null \"reference\" parameter");
    referenceList.add(reference);
  }

  /**
   * @return a non-<code>null</code> list of mimeMessage references
   */
  public List<String> getReferences() {
    return referenceList;
  }

  /**
   * Converts the object into a string representation.
   * @return The string representation of the object
   */
  public String toString() {

    // Container for bean contentstring
    String content = "Bcc Addresses [";

    // Retrieve the bcc addresses
    for (int i = 0; i < bccAddressList.size(); i++) {
      // Add the next bcc address
      content += bccAddressList.get(i) + ", ";
    }

    content += "] \nccAddressList [";

    // Retrieve the cc addresses
    for (int i = 0; i < ccAddressList.size(); i++) {
      // Retrieve the next cc address
      content += ccAddressList.get(i) + ", ";
    }

    content += "] \ntoAddressList [";

    // Retrieve the to addresses
    for (int i = 0; i < toAddressList.size(); i++) {
      // Retrieve the next to address
      content += toAddressList.get(i) + ", ";
    }

    content += "] \nreferenceList [";

    // Retrieve the reference address list
    for (int i = 0; i < referenceList.size(); i++) {
      // Retrieve the next reference address
      content += referenceList.get(i) + ", ";
    }

    // Retrieve the from address
    content += "] \nfromAddress [" + fromAddress + "]";

    // Retrieve the date
    content += "] \ndate [" + date + "] \n";

    // Retrieve the message id
    content += "messageID [" + messageID + "] \n";

    // Retrieve the subject
    content += "Subject [" + subject + "] \n";

    return content;
  }
}
