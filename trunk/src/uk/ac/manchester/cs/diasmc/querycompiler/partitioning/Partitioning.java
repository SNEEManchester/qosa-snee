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
package uk.ac.manchester.cs.diasmc.querycompiler.partitioning;

import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.algorithmSelection.
	PhysicalOptimization;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;

import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.*;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.RTLifetimeScoringFunction;

/**
 * Step 5: Partitioning
 * 
 * <i>Partitioning</i> breaks up the PAF into fragments by inserting exchange 
 * operators, using semantic criteria such as operator sensitivity, and also 
 * identifying edges in the PAF with lower output sizes.
 * 
 * This results in the Fragmented-Algebraic Form (FAF) of the query plan.
 * 
 * @author galpini
 */
public class Partitioning {

    static Logger logger = Logger.getLogger(Partitioning.class.getName());
	
    /**
     * Fragments the query plan by inserting exchange operators based on semantic concerns (e.g., location
     * sensitivity or attribute sensitivity) and practical concerns (i.e., where a reduction in data
     * size is predicted, so as to be economical with respect to radio traffic generated by the plan)
     * 
     * The resulting query plan is in FAF (fragmented-algebraic form, i.e., PAF with exchange 
     * operators inserted).
     * 
     * @param paf		A collection of candidate plans in PAF 
     * (physical-algebraic form)
     * @param queryName The name of the query being optimized 
     * @return A collection of candidate plans in FAF
     */
    public static ScoredCandidateList<FAF> doPartitioning(
    		final PAF paf, 
    		final String queryName) {

        ScoredCandidateList<FAF> fafList = 
        	new ScoredCandidateList<FAF>();
   		//CB never used OperatorImplementation.setOpCountResetVal();
	    FAF faf = partitionPAF(paf, queryName);
		fafList.addCandidate(faf);
		
        if (Settings.DISPLAY_FAF) {
        	fafList.display(QueryCompiler.queryPlanOutputDir);
        }
		
		return fafList;
    }

	private static FAF partitionPAF(final PAF paf, final String queryName) {
		FAF faf = new FAF(paf, queryName);
		final Iterator<Operator> opIter = faf
			.operatorIterator(TraversalOrder.POST_ORDER);
		
   		//CB never used OperatorImplementation.setOpCountResetVal();
 		while (opIter.hasNext()) {
	
			    final Operator op = (Operator) opIter.next();
		
			    if (!op.isLeaf()) {
				for (int childIndex = 0; childIndex < op.getInDegree(); childIndex++) {
				    final Operator c = op.getInput(childIndex);
				    /* Communications may be desirable between an operator 
				     * and one of its children. 
				     * If it has a larger output size than that child,
				     * and below AggrEval and AggrPartial operators,
				     * and above AggrEval operators
				     * and below all attribute sensitive operator
				     */
				    if ((op.getCardinality(CardinalityType.PHYSICAL_MAX) > c
					    .getCardinality(CardinalityType.PHYSICAL_MAX))
					    || (op.isAttributeSensitive())
					    || (op instanceof AggrIterateOperator)
					    || (op instanceof DeliverOperator)
					    || (c instanceof AggrEvalOperator)) {
					logger.finer("Adding exchange op between "
						+ c.getText(false) + " and "
						+ op.getText(false)); 
					final ExchangeOperator exchOp = PhysicalOptimization
						.newExchangeOperator(c.getOperatorDataType());
					faf.insertNode(c, op, exchOp);
				    }
				}
		    }
		}
	
		logger.fine("Constructing query plan fragment tree.");
		faf.buildFragmentTree();
		return faf;
	}

	
}
