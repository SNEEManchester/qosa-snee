/****************************************************************************\ 
*                                                                            *
*  SNEE (Sensor NEtwork Engine)                                              *
*  http://code.google.com/p/snee                                             *
*  Release 1.0, 24 May 2009, under New BSD License.                          *
*                                                                            *
*  Copyright (c) 2009, University of Manchester                              *
*  All rights reserved.                                                      *
*                                                                            *
*  Redistribution and use in source and binary forms, with or without        *
*  modification, are permitted provided that the following conditions are    *
*  met: Redistributions of source code must retain the above copyright       *
*  notice, this list of conditions and the following disclaimer.             *
*  Redistributions in binary form must reproduce the above copyright notice, *
*  this list of conditions and the following disclaimer in the documentation *
*  and/or other materials provided with the distribution.                    *
*  Neither the name of the University of Manchester nor the names of its     *
*  contributors may be used to endorse or promote products derived from this *
*  software without specific prior written permission.                       *
*                                                                            *
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
*  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
*  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
*  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
*  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
*  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
*  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
*  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
*  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
*  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
*                                                                            *
\****************************************************************************/
package uk.ac.manchester.cs.diasmc.querycompiler.algorithmSelection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.LAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggrEvalOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggrInitializeOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggrIterateOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggregationOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.OperatorDataType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.RStreamOperator;

/**
 * This class represents the physical optimization of the query plan.
 * 
 * Pushes projection down. (Optional)
 * Removes unrequired operators. (Optional except for Rstream)
 * Splits aggregation into three phases.
 * @author Christian Brenninkmeijer
 */
public final class PhysicalOptimization {

	/** Standard Java logger. */
    private static Logger logger = Logger.getLogger(PhysicalOptimization.class
	    .getName());

    /**
     * Hides default constructor.
     */
    private PhysicalOptimization() {
    }
    
    /** 
     * Provides a new exchange but with no child or parent set.
     * @param newOperatorDataType Incoming data type.
     * @return new Exchange Operator.
     */
    public static ExchangeOperator newExchangeOperator(
    		final OperatorDataType newOperatorDataType) {
		return new ExchangeOperator(newOperatorDataType);
    }

    //Tranmission method not set as default all is used.
    //private static void doPhysicalOptimization(final PAF paf,
    //		final Operator logicalOp) throws OptimizationException {
    //	for (int n = 0; n < logicalOp.getInDegree(); n++) {
	//	    doPhysicalOptimization(paf, logicalOp.getInput(n));
	//	}
   	//  	setTupleTransmissionMethod(logicalOp);
    //}

    /**
     * The physical optimization of the query plan.
     * 
     * Pushes projection down. (Optional)
     * Removes unrequired operators. (Optional except for Rstream)
     * Splits aggregation into three phases.
     * @param laf The Logical Algebra format
     * @param queryName The name for this query
     * @return The Physical Algebra Format
     */
    public static PAF doPhysicalOptimization(
    		final LAF laf, 
    		final String queryName) {
		PAF paf = new PAF(laf, queryName);
			
		//doPhysicalOptimization(paf, paf.getRootOperator());
			
	    if (Settings.LOGICAL_OPTIMIZATION_PUSH_PROJECTION_DOWN) {
	    	pushProjectionDown(paf);
	    }
	    if (Settings.LOGICAL_OPTIMIZATION_REMOVE_UNREQUIRED_OPERATORS) {
	    	removeUnrequiredOperators(paf);
	    } else {
	    	removeRStream(paf);
	    }
	
	    logger.fine("replace aggregations with aggr+, aggr=and aggr-");
		splitAggregationOperators(paf);
			
        if (Settings.DISPLAY_PAF) {
        	paf.display(QueryCompiler.queryPlanOutputDir, paf.getName());
        }
		return paf;			
    }
    
    /**
     * Splits Aggregation operators into two operators, 
     * to allow incremental aggregation.
     * @param paf the physical algebra format.
     */
    private static void splitAggregationOperators(final PAF paf) {

	final Iterator<Operator> opIter = paf
		.operatorIterator(TraversalOrder.POST_ORDER);
	while (opIter.hasNext()) {

	    final Operator op = opIter.next();

	    //TODO: Only split the aggreation operator 
	    //if the function will yield efficiencies,
	    //e.g., it may not be worthwhile to split an operator 
	    //in the case of a median, because
	    //it can't be incrementally computed
	    if (op instanceof AggregationOperator) {

		//Split into three
	    AggregationOperator agg = (AggregationOperator) op;
		final AggrInitializeOperator aggrInitialize = 
			new AggrInitializeOperator(agg);
		final AggrIterateOperator aggrIterate = 
			new AggrIterateOperator(agg);
		final AggrEvalOperator aggrEval = 
			new AggrEvalOperator(agg);
		paf.replacePath(op, new Node[] { aggrEval, aggrInitialize });
		paf.insertNode(aggrInitialize, aggrEval, aggrIterate);
	    }
	}
    }
       
    /** 
     * Removes unrequired operators.
     * Removes operators that report they are removeable.
     * Removal operators are ones that have the same physical input as output.
     * @param paf The Physical logical format.
     */
 	private static void removeUnrequiredOperators(final PAF paf) {
		final Iterator<Operator> opIter = paf
			.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
		    try {
			if (op.isRemoveable()) {
			    logger.fine("Removing node " + op.getText());
			    paf.removeNode(op);
			}
		    } catch (final OptimizationException e) {
			logger.log(Level.SEVERE, "Optimisation Error ", e);
		    }
		}
    }

 	/** 
 	 * Removes all Rstsream operators.
     * @param paf The Physical logical format.
 	 */
    private static void removeRStream(final PAF paf) {
	final Iterator<Operator> opIter = paf
		.operatorIterator(TraversalOrder.POST_ORDER);
	while (opIter.hasNext()) {
	    final Operator op = opIter.next();
	    try {
		if (op instanceof RStreamOperator) {
		    logger.fine("Removing node " + op.getText());
		    paf.removeNode(op);
		}
	    } catch (final OptimizationException e) {
		logger.log(Level.SEVERE, "Optimisation Error ", e);
	    }
	}
    }

    /**
     * Pushes projections down into other operators.
     * @param paf The Physical logical format.
     */
    private static void pushProjectionDown(final PAF paf) {
    	try {
    		final Operator op 
    			= (Operator) paf.getRootOperator();
   			op.pushProjectionDown(new ArrayList<Expression>(),
   					new ArrayList<Attribute>());
    	} catch (final Exception e) {
    		Utils.handleCriticalException(e);
    	}
    }
    
}
