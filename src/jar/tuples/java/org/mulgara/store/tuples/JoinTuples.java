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
 * The Initial Developer of the Original Code is Andrew Newman.
 *  Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.tuples;

// Java 2 standard packages

import org.apache.log4j.Logger;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common functionality from UnboundJoin.
 *
 * @created 2005-03-08
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/26 03:44:19 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class JoinTuples extends AbstractTuples {

    /** Logger. */
    protected static final Logger logger = Logger.getLogger(JoinTuples.class.getName());

    /**
     * Version of {@link #operandBinding}} including only columns to the left of
     * the first unbound column.
     */
    protected long[][] operandBindingPrefix;

    /**
     * For each column of the joined result, which operand contains the first
     * occurrence of that variable.
     */
    protected int[] mapOperand;

    /**
     * For each column of the joined result, which column of the operand
     * determined by {@link #mapOperand} contains the first occurrence of that
     * variable.
     */
    protected int[] mapColumn;

    /**
     * Magic value within the {@link #fooOperand} array, indicating that a column
     * is bound to one of the columns of the <var>prefix</var> parameter to
     * {@link #next}.
     */
    protected static final int PREFIX = -1;

    /**
     * For each column of each operand, which operand contains the first
     * occurrence of that variable, or {@link #PREFIX} if the prefix specified
     * to {@link #next} contains the occurrence.
     */
    protected int[][] fooOperand;

    /**
     * For each column of each operand, which column of the operand determined by
     * {@link #fooOperand} contains the first occurrence of that variable, or if
     * the corresponding value of {@link #fooOperand} is {@link #PREFIX}, which
     * column of the prefix specified to {@link #next} contains the occurrence.
     */
    protected int[][] fooColumn;

    /**
     * Whether each column of this instance might contain {@link #UNBOUND} rows.
     */
    protected boolean[] columnEverUnbound;

    /**
     * The propositions to conjoin.
     */
    protected Tuples[] operands;

    /**
     * The required values of the columns of each operand. A value of {@link
     * Tuples#UNBOUND} indicates that the column is free to vary.
     */
    protected long[][] operandBinding;

    /**
     * For each operand, for each variable, which output column contains the same variable.
     */
    protected int[][] operandOutputMap;

    /**
     * Do any of the operands with variables matching this output variable contain UNBOUND?
     */
    protected boolean[] columnOperandEverUnbound;

    /**
     * Flag indicating that the cursor is before the first row.
     */
    protected boolean isBeforeFirst = true;

    /**
     * Flag indicating that the cursor is after the last row.
     */
    protected boolean isAfterLast = false;

    /**
     * Do any of the operands contain duplicates.  Used to shortcircuit hasNoDuplicates.
     */
    protected boolean operandsContainDuplicates;

    /**
     * The prefix of the index.
     */
    protected long[] prefix = null;

    protected void init(Tuples[] operands) throws TuplesException {

        // Validate "operands" parameter
        if (operands == null) {
            throw new IllegalArgumentException("Null \"operands\" parameter");
        }

        // Initialize fields
        this.operands = clone(operands);
        operandBinding = new long[operands.length][];
        operandBindingPrefix = new long[operands.length][];
        this.operandsContainDuplicates = false;
        for (int i = 0; i < operands.length; i++) {

            // Debug
            if (logger.isDebugEnabled()) {
                logger.debug("Operands " + i + " : " + operands[i]);
                logger.debug("Operands variables " + i + " : " +
                        Arrays.asList(operands[i].getVariables()));
                logger.debug("Ooperands types " + i + " : " +
                        operands[i].getClass());
            }
            operandBinding[i] = new long[operands[i].getVariables().length];
            if (!operands[i].hasNoDuplicates()) {
                this.operandsContainDuplicates = true;
            }
        }

        fooOperand = new int[operands.length][];
        fooColumn = new int[operands.length][];
        operandOutputMap = new int[operands.length][];

        // Calculate the variables present and their mappings from operand
        // columns to result columns
        List<Variable> variableList = new ArrayList<Variable>();
        List<Integer> mapOperandList = new ArrayList<Integer>();
        List<Integer> mapColumnList = new ArrayList<Integer>();
        List<Integer> fooOperandList = new ArrayList<Integer>();
        List<Integer> fooColumnList = new ArrayList<Integer>();

        for (int i = 0; i < operands.length; i++) {
            fooOperandList.clear();
            fooColumnList.clear();

            Variable[] operandVariables = operands[i].getVariables();

            operandOutputMap[i] = new int[operandVariables.length];

            for (int j = 0; j < operandVariables.length; j++) {
                int k = variableList.indexOf(operandVariables[j]);

                if (k == -1) {
                    mapOperandList.add(new Integer(i));
                    mapColumnList.add(new Integer(j));
                    fooOperandList.add(new Integer(PREFIX));
                    fooColumnList.add(new Integer(variableList.size()));
                    variableList.add(operandVariables[j]);
                    operandOutputMap[i][j] = j;
                } else {
                    fooOperandList.add(mapOperandList.get(k));
                    fooColumnList.add(mapColumnList.get(k));
                    operandOutputMap[i][j] = k;
                }
            }

            // Convert per-operand lists into arrays
            assert fooOperandList.size() == fooColumnList.size();
            fooOperand[i] = new int[fooOperandList.size()];
            fooColumn[i] = new int[fooColumnList.size()];

            for (int j = 0; j < fooOperand[i].length; j++) {
                fooOperand[i][j] = fooOperandList.get(j).intValue();
                fooColumn[i][j] = fooColumnList.get(j).intValue();
            }
        }

        // Convert column mappings from lists to arrays
        setVariables(variableList);

        mapOperand = new int[mapOperandList.size()];
        mapColumn = new int[mapColumnList.size()];

        for (int i = 0; i < mapOperand.length; i++) {
            mapOperand[i] = ((Integer) mapOperandList.get(i)).intValue();
            mapColumn[i] = ((Integer) mapColumnList.get(i)).intValue();
        }

        // Determine which columns are ever unbound
        columnEverUnbound = new boolean[variableList.size()];
        columnOperandEverUnbound = new boolean[variableList.size()];
        Arrays.fill(columnEverUnbound, true);
        Arrays.fill(columnOperandEverUnbound, false);

        for (int i = 0; i < operands.length; i++) {
            Variable[] variables = operands[i].getVariables();
            for (int j = 0; j < variables.length; j++) {
                if (!operands[i].isColumnEverUnbound(j)) {
                    columnEverUnbound[getColumnIndex(variables[j])] = false;
                } else {
                    columnOperandEverUnbound[getColumnIndex(variables[j])] = true;
                }
            }
        }
    }

    /**
     * @return {@inheritDoc}  This occurs if and only if every one of the
     *   {@link #operands} is unconstrained.
     * @throws TuplesException {@inheritDoc}
     */
    public boolean isUnconstrained() throws TuplesException {
        for (int i = 0; i < operands.length; i++) {
            if (!operands[i].isUnconstrained()) {
                return false;
            }
        }

        return true;
    }

    public List<Tuples> getOperands() {
        return Arrays.asList(operands);
    }

    public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
        if (prefix == null) {
            throw new IllegalArgumentException("Null \"prefix\" parameter");
        }

        if (suffixTruncation != 0) {
            throw new TuplesException("Suffix truncation not implemented");
        }

        assert operands != null;
        assert operandBinding != null;

        isBeforeFirst = true;
        isAfterLast = false;
        this.prefix = prefix;
    }
}
