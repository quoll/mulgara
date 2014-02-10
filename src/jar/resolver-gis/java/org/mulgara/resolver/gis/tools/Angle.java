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

package org.mulgara.resolver.gis.tools;


/**
 * Describes an Angle in terms of degrees, minutes and seconds.
 *
 * @created 2004-11-17
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.6 $
 *
 * @modified $Date: 2005/01/05 04:58:29 $
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
public interface Angle {

  /**
   * Returns the number of degrees (0-360) in the angle
   * @return double
   */
  public double getDegrees();

  /**
   * Returns the number of minutes in the angle (0-60)
   * @return double
   */
  public double getMinutes();

  /**
   * Returns the number of seconds in the angle (0-60)
   * @return double
   */
  public double getSeconds();

  /**
   * Sets the number of degrees. If degrees is larger than 360, it is converted
   * to the equivelant 0-360 angle.
   * @param degrees double
   */
  public void setDegrees(double degrees);

  /**
   * Sets the number of minutes. If minutes is larger than 60, excess minutes
   * are converted to degrees.
   * @param minutes double
   */
  public void setMinutes(double minutes);

  /**
   * Sets the number of seconds. If seconds is larger than 60, excess seconds
   * are converted to minutes.
   * @param seconds double
   */
  public void setSeconds(double seconds);

  /**
   * Returns the angle (degrees, minutes and seconds) in radians.
   * @return double
   */
  public double asRadians();
}
