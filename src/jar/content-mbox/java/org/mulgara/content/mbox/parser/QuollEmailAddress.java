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

// java standard classes
import java.io.Serializable;

// log4j classes
import org.apache.log4j.Logger;


/**
 * Email address data.
 *
 * @created 2003-05-23
 *
 * @author Ben Warren
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Quoll Licence</a>
 */
public class QuollEmailAddress implements Serializable {

  /** The logger to log to */
  @SuppressWarnings("unused")
  private static final Logger log =
      Logger.getLogger(QuollEmailAddress.class.getName());

  /** Make this consistent across all versions */
  private static final long serialVersionUID = 3535258501347682229L;

  /** The actual address text */
  private String address = null;

  /** The address number */
  private Integer addressNumber = null;

  /** The domain of the address */
  private String domain = null;

  /** The personal name for the address */
  private String personalName = null;

  /** The user id for the address */
  private String userId = null;


  /**
   * Create an email address.
   *
   * @param address The email address.
   */
  public QuollEmailAddress(String address) {
    this.address = address;
  }

  /**
   * Create an email address.
   *
   * @param address The eMAIL address.
   * @param personalName Personal name
   */
  public QuollEmailAddress(String address, String personalName) {
    this.address = address;
    this.personalName = personalName;
  }

  /**
   * Sets the address.
   *
   * @param address The address.
   */
  public void setAddress(String address) {
    this.address = address;
  }


  /**
   * Returns the address.
   *
   * @return The address.
   */
  public String getAddress() {
    return this.address;
  }


  /**
   * Sets the address number.
   *
   * @param addressNumber The address number.
   */
  public void setAddressNumber(int addressNumber) {
    this.addressNumber = new Integer(addressNumber);
  }


  /**
   * Returns the address number.
   *
   * @return The address number.
   */
  public Integer getAddressNumber() {
    return this.addressNumber;
  }


  /**
   * Sets the domain.
   *
   * @param domain The domain.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Returns the domain.
   *
   * @return The domain.
   */
  public String getDomain() {
    return this.domain;
  }


  /**
   * Returns the personal name.
   *
   * @return The personal name.
   */
  public String getPersonalName() {
    return this.personalName;
  }


  /**
   * Sets the personal name.
   *
   * @param personalName The personal name.
   */
  public void setPersonalName(String personalName) {
    this.personalName = personalName;
  }


  /**
   * Sets the userId.
   *
   * @param userId The userId.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }


  /**
   * Returns the userId.
   *
   * @return The userId.
   */
  public String getUserId() {
    return this.userId;
  }


  /**
   * Returns if the address is equal to another object.
   *
   * @return true if equal.
   */
  public boolean equals(Object obj) {

    boolean equal = false;

    if (obj != null) {

      try {

        QuollEmailAddress address = (QuollEmailAddress) obj;

        if (address.getAddress() == null && this.getAddress() == null) {
           equal = true;
         }
         else if (address.getAddress() != null &&
                  address.getAddress().equals(this.getAddress())) {
           equal = true;
         }
      }
      catch (ClassCastException e) {}
    }
    return equal;
  }


  /**
   * Returns the hashcode for the address.
   *
   * @return the hashcode.
   */
  public int hashCode() {

    if (address == null) {
      return 13;
    }
    else {
      return 13*this.getAddress().hashCode();
    }
  }


  /**
   * Returns a string representing this object.
   *
   * @return A string representing this object.
   */
  public String toString() {
    return "[QuollEmailAddress: address = "+getAddress()+
        ", domain = "+getDomain()+
        ", personalName = "+getPersonalName()+
        ", userId = "+getUserId()+
        ", addressNumber = "+getAddressNumber()+"]";
  }

}
