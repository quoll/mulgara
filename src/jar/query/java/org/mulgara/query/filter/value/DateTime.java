/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter.value;

import java.net.URI;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import static org.mulgara.query.rdf.XSD.DATE_TIME_URI;

/**
 * A date value.
 *
 * @created Mar 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DateTime extends TypedLiteral {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 7517437419033492738L;

  /** xsd:dateTime */
  public static final URI TYPE = DATE_TIME_URI;

  /** Singleton reference to the datatype factory for date parsers */
  private static DatatypeFactory factory;
  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException("Unable to initialize XML date parser");
    }
  }

  /**
   * Creates the date value
   * @param value The date to wrap
   */
  public DateTime(java.util.Date value) {
    super(value, TYPE);
  }

  /**
   * Converts a lexical representation of an XSD date into a Date object.
   * Note necessarily the most efficient way to handle this, but it has the
   * advantage of simplicity and of using standard Java libraries.
   * @param lexical The lexical form of the date.
   * @return A {@link java.util.Date} for the represented date.
   */
  public static Date parseDate(String lexical) {
    XMLGregorianCalendar cal = factory.newXMLGregorianCalendar(lexical);
    return cal.toGregorianCalendar().getTime();
  }
}
