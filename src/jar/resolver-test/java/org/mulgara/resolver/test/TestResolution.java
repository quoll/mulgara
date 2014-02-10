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
 * Contributor(s):
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.test;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.Logger; // Apache Log4J

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.MandatoryBindingAnnotation;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.WrappedTuples;

/**
 * @created 2005-05-03
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.4 $
 * @modified $Date: 2005/06/03 13:56:54 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TestResolution extends WrappedTuples implements Resolution {

  /** Logger */
  private static Logger logger = Logger.getLogger(TestResolution.class.getName());

  /** The constraint this instance resolves */
  private final TestConstraint constraint;

  private static Map<String,Tuples> tests = new HashMap<String,Tuples>();

  private Set<Variable> mandatoryBindings;
  private boolean nullAnnotation;

  static void initializeResults(ResolverFactoryInitializer initializer) {
    try {
      long A = initializer.preallocate(new LiteralImpl("A"));
      long B = initializer.preallocate(new LiteralImpl("B"));
      long C = initializer.preallocate(new LiteralImpl("C"));
      long D = initializer.preallocate(new LiteralImpl("D"));

      tests.put("test:a", LiteralTuples.create(
            new String[] { "a", "b" },
            new long[][] { new long[] { A, A }, new long[] { A, B }, new long[] { B, A }, new long[] { B, B }}));
      tests.put("test:b", LiteralTuples.create(
            new String[] { "a", "c" },
            new long[][] { new long[] { A, C }, new long[] { A, D }, }));
    } catch (TuplesException et) {
      throw new RuntimeException("Failed to initialise test results", et);
    } catch (InitializerException ei) {
      throw new RuntimeException("Failed to initialise test results", ei);
    }
  }

  /**
   * Construct the resolution to a constraint from a set of statings.
   *
   * @param constraint  the constraint to resolver, never <code>null</code>
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>result</var> are <code>null</code>
   */
  TestResolution(Constraint constraint) throws TuplesException {
    if (constraint == null) {
      throw new IllegalArgumentException("Null 'constraint' parameter");
    }
    this.constraint = (TestConstraint)constraint;

    if (logger.isDebugEnabled()) {
      logger.debug("testSelection = " + this.constraint.getTestSelection());
    }

    Tuples result = (Tuples)tests.get(this.constraint.getTestSelection());

    if (logger.isDebugEnabled()) {
      logger.debug("result = " + result + " from " + tests);
    }

    mandatoryBindings = new HashSet<Variable>();
    String bindingString = this.constraint.getTestParam();
    Variable[] variables = result.getVariables();
    nullAnnotation = false;
    for (int i = 0; i < bindingString.length(); i++) {
      Variable v = new Variable(bindingString.substring(i, i+1));
      mandatoryBindings.add(v);
      if (v.equals(variables[i])) {
        logger.warn("Adding variable to mbset: " + v);
      } else {
        logger.warn("Setting resolution use a null annotation");
        nullAnnotation = true;
      }
    }

    super.init(result);
  }

  /**
   * Retrieve the restraint this resolution is for.
   *
   * @return The constraint this resolution is for
   */
  public Constraint getConstraint() {
    return constraint;
  }

  /**
   * Determines whether the resolution is complete or not.
   *
   * @return Whether the resolution is complete or not
   */
  public boolean isComplete() {
    return true;
  }


  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    if (prefix.length < mandatoryBindings.size()) {
      throw new TuplesException(
          "Failed to provide sufficient prefix matching mandatory binding: " + mandatoryBindings);
    }

    super.beforeFirst(prefix, suffixTruncation);
  }

  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    if (annotationClass.equals(MandatoryBindingAnnotation.class) && !nullAnnotation) {
      return new MandatoryBindingAnnotation(mandatoryBindings);
    }

    return null;
  }
}
