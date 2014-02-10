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
 *   The copyright to this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 *   SymbolicTransformationContext contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Java 2 standard packages
import java.util.*;
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.AbstractSymbolicTransformer;
import org.mulgara.resolver.spi.SymbolicTransformationContext;
import org.mulgara.resolver.spi.SymbolicTransformationException;

/**
 * A transformation rule that converts XSD ordering constraints into
 * {@link IntervalConstraint}s.
 *
 * @created 2005-05-12
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.3 $
 * @modified $Date: 2005/06/09 09:26:20 $ by $Author: raboczi $
 * @maintenanceAuthor $Author: raboczi $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class IntervalTransformation extends AbstractSymbolicTransformer {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(IntervalTransformation.class);

  /**
   * The node representing the &lt; relation between <tt>xsd:double</tt>s.
   */
  private URIReferenceImpl lessThan;

  /**
   * The node representing the &gt; relation between <tt>xsd:double</tt>s.
   */
  private URIReferenceImpl greaterThan;

  /**
   * The model type URI identifying the XSD Resolver.
   */
  private URI modelTypeURI;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param lessThan  property representing &lt; for <tt>xsd:double</tt>s,
   *   never <code>null</code>
   * @param greaterThan  property representing &gt; for <tt>xsd:double</tt>s,
   *   never <code>null</code>
   */
  IntervalTransformation(URI modelTypeURI, URIReferenceImpl lessThan, URIReferenceImpl greaterThan) {
    assert lessThan    != null;
    assert greaterThan != null;

    // Initialize fields
    this.modelTypeURI = modelTypeURI;
    this.greaterThan = greaterThan;
    this.lessThan    = lessThan;
  }

  @Override
  protected ConstraintExpression transformOperation(SymbolicTransformationContext context,
                                                    ConstraintOperation expr)
        throws SymbolicTransformationException {
    if (expr instanceof ConstraintConjunction)
      return transformConjunction(context, (ConstraintConjunction)expr);
    return super.transformOperation(context, expr);
  }

  private ConstraintExpression transformConjunction(SymbolicTransformationContext context, ConstraintConjunction constraintConjunction)
        throws SymbolicTransformationException {
    if (logger.isDebugEnabled()) {
      logger.debug("Transforming conjunction " + constraintConjunction);
    }

    assert constraintConjunction != null;

    List<ConstraintExpression> elements = constraintConjunction.getElements();
    List<ConstraintExpression> newElements = new ArrayList<ConstraintExpression>(elements.size());
    boolean modified = false;

    // This map is keyed on Variable instances, and maps to the cumulative
    // IntervalConstraint assocated with that variable
    Map<Variable,IntervalConstraint> map = new HashMap<Variable,IntervalConstraint>();

    for (Iterator<ConstraintExpression> i = elements.iterator(); i.hasNext();) {
      ConstraintExpression element = i.next();

      // Recursively transform the element
      ConstraintExpression transformedElement = transformExpression(context, element);
      if (transformedElement != element) {
        if (logger.isDebugEnabled()) {
          logger.debug("Recursively transformed " + element +
                       " to " + transformedElement);
        }

        modified = true;
        element = transformedElement;
      }

      // Merge any IntervalConstraints
      if (!(element instanceof IntervalConstraint)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Unknown conjunction element: " + element + " type " + element.getClass());
        }
        newElements.add(element);
      } else {
        IntervalConstraint interval = (IntervalConstraint) element;
        IntervalConstraint cumulative = map.get(interval.getVariable());
        if (cumulative == null) {
          cumulative = interval;

          if (logger.isDebugEnabled()) {
            logger.debug("Initial interval constraint: " + cumulative);
          }
        } else {
          cumulative = cumulative.conjoin(interval);
          modified = true;

          if (logger.isDebugEnabled()) {
            logger.debug("Cumulative interval constraint: " + cumulative);
          }
        }
        assert cumulative != null;
        map.put(interval.getVariable(), cumulative);
      }
    }

    // If any intervals were extracted or merged, return the new expression
    if (!modified) {
      if (logger.isDebugEnabled()) {
        logger.debug("Conjunction not modified");
      }

      return constraintConjunction;
    } else {
      newElements.addAll(map.values());

      // Convert the list of conjoined terms into a ConstraintExpression
      ConstraintExpression result;
      switch (newElements.size()) {
        case 0:  result = ConstraintFalse.INSTANCE;  break;
        case 1:  result = (ConstraintExpression) newElements.get(0);  break;
        default: result = new ConstraintConjunction(newElements);
      }
      assert result != null;

      if (logger.isDebugEnabled()) {
        logger.debug("Conjunction modified returning " + result);
      }

      return result;
    }
  }

  @Override
  protected ConstraintExpression transformConstraint(SymbolicTransformationContext context,
                                                     Constraint constraint)
        throws SymbolicTransformationException {
    assert constraint != null;
    if (constraint instanceof IntervalConstraint) return constraint;

    Variable variable;
    boolean  boundedBelow;
    Bound    bound;

    if (logger.isDebugEnabled()) {
      logger.debug("Transforming Constraint: " + constraint);
    }

    // Confirm model is of type XSDModel.
    try {
      ConstraintElement modelElement = constraint.getModel();
      if (!(modelElement instanceof URIReference)) {
        logger.debug("model not URIReference; cannot participate in transform, returning original");
        return constraint;
      }
      URI modelURI = ((URIReference)modelElement).getURI();
      URI modelTypeURI = context.mapToModelTypeURI(modelURI);
      if (!this.modelTypeURI.equals(modelTypeURI)) {
        logger.debug("model: " + modelURI + " is of type " + modelTypeURI + " not " + this.modelTypeURI + " ignoring constraint, returning original");

        return constraint;
      }
    } catch (QueryException eq) {
      throw new SymbolicTransformationException("Unable to check model on constraint", eq);
    }

    logger.debug("Graph suitable for IntervalTransformation");

    // Figure out the direction of bounding, assuming [$var op value] order
    if (constraint.getElement(1).equals(lessThan)) {
      boundedBelow = false;
    } else if (constraint.getElement(1).equals(greaterThan)) {
      boundedBelow = true;
    } else {
      logger.debug("Predicate not recognised by IntervalTransformation");
      return constraint;
    }

    // Determine whether we have a [$var op value] or [value op $var] form
    if (constraint.getElement(0) instanceof Variable) {
      if (constraint.getElement(2) instanceof Variable) {
        logger.debug("Both Subject and Object are Variables, returning original");
        return constraint;
      } else {
        variable = (Variable) constraint.getElement(0);
        double value = Double.parseDouble(
          ((Literal) constraint.getElement(2)).getLexicalForm()
        );
        bound = new Bound(value, false);
      }
    } else {
      if (!(constraint.getElement(2) instanceof Variable)) {
        logger.debug("Neither Subject nor Object are Variables, returning original");
        return constraint;
      } else {
        variable = (Variable) constraint.getElement(2);
        double value = Double.parseDouble(
          ((Literal) constraint.getElement(0)).getLexicalForm()
        );
        bound = new Bound(value, false);
        boundedBelow = !boundedBelow;  // reverse the comparison
      }
    }

    // Generate the constraint
    return new IntervalConstraint(
      variable,
      boundedBelow ? bound : null,
      boundedBelow ? null : bound,
      (URIReference)constraint.getModel()
    );
  }
}
