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

// Java 2 standard packages

// Log4J
import org.apache.log4j.Logger;

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
public abstract class AbstractAngle implements Angle {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(AbstractAngle.class.
      getName());

  /** Number of degrees  */
  private double degrees = 0;

  /** Number of minutes  */
  private double minutes = 0;

  /** Number of seconds */
  private double seconds = 0;

  /** Representation in radians (Calculated) */
  private double radians = 0;

  /** Used to determine if the radians feild requires re-calculating */
  private boolean modified = false;

  /** Used to convert degrees to radians */
  private final double RADIANS_PER_DEGREE = (Math.PI / 180.0);

  /** Number of degrees in a circle */
  private final double NUM_DEGREES = 360;

  /** Number of minutes in a degree */
  private final double MINUTES_PER_DEGREE = 60;

  /** Number of seconds in a minute */
  private final double SECONDS_PER_MINUTE = 60;

  /** Number of seconds in a degree */
  private final double SECONDS_PER_DEGREE = SECONDS_PER_MINUTE *
      MINUTES_PER_DEGREE;

  /**
   * Default constructor.
   */
  public AbstractAngle() {
    super();
  }

  /**
   * Returns the number of degrees (0-360) in the angle
   * @return double
   */
  public double getDegrees() {
    return degrees;
  }

  /**
   * Returns the number of minutes in the angle (0-60)
   * @return double
   */
  public double getMinutes() {
    return minutes;
  }

  /**
   * Returns the number of seconds in the angle (0-60)
   * @return double
   */
  public double getSeconds() {
    return seconds;
  }

  /**
   * Sets the number of degrees. If degrees is larger than 360, it is converted
   * to the equivelant 0-360 angle. If it is negative it is converted to
   * 360 - *degrees*.
   * @param degrees double
   */
  public void setDegrees(double degrees) {

    //validate
    if (degrees < 0) {
      this.degrees = NUM_DEGREES - (degrees % NUM_DEGREES);
    } else {

      this.degrees = degrees % NUM_DEGREES;
    }
    modified = true;
  }

  /**
   * Sets the number of minutes. If minutes is larger than 60, excess minutes
   * are converted to degrees.
   * @param minutes double
   */
  public void setMinutes(double minutes) {

    //validate
    if (minutes < 0) {

      setDegrees(getDegrees() - (minutes / MINUTES_PER_DEGREE));
      this.minutes = MINUTES_PER_DEGREE - (minutes % MINUTES_PER_DEGREE);
    } else {

      //convert whole degrees and use left over minutes
      setDegrees(getDegrees() + (minutes / MINUTES_PER_DEGREE));
      this.minutes = minutes % MINUTES_PER_DEGREE;
    }
    modified = true;
  }

  /**
   * Sets the number of seconds. If seconds is larger than 60, excess seconds
   * are converted to minutes.
   * @param seconds double
   */
  public void setSeconds(double seconds) {

    //validate
    if (seconds < 0) {

      setMinutes(getMinutes() - (seconds / SECONDS_PER_MINUTE));
      this.seconds = SECONDS_PER_MINUTE - (seconds % SECONDS_PER_MINUTE);
    } else {

      //convert whole minutes and use left over seconds
      setMinutes(getMinutes() + (seconds / SECONDS_PER_MINUTE));
      this.seconds = seconds % SECONDS_PER_MINUTE;
    }
    modified = true;
  }

  /**
   * Returns the angle (degrees, minutes and seconds) in radians.
   * @return double
   */
  public double asRadians() {
    updateRadians();
    return radians;
  }

  /**
   * Updates the radians feild (if any feilds have changed).
   */
  private void updateRadians() {
    if (modified) {
      radians = calculateRadians();
      modified = false;
    }
  }

  /**
   * Calculates and returns the number of degrees, minutes and seconds in radians.
   *
   * @return float
   */
  private double calculateRadians() {

    //convert degrees, minutes and seconds to a single angle
    double angle = degrees + (minutes / MINUTES_PER_DEGREE) +
        (minutes / SECONDS_PER_DEGREE);
    return angle * RADIANS_PER_DEGREE;
  }

  /**
   * Compares this Angle to another.
   *
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj) {

    boolean equal = (obj != null) && (obj instanceof AbstractAngle);

    if (equal) {
      AbstractAngle angle = (AbstractAngle) obj;
      equal = (angle == this)
          || (angle.asRadians() == asRadians())
          || ((getDegrees() == angle.getDegrees())
              && (getMinutes() == angle.getMinutes())
              && (getSeconds() == angle.getSeconds()));
    }

    return equal;
  }

}
