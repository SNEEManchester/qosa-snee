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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators;

import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/** 
 * Exsention of the Aggregation operator 
 * 		to identify it as the third of three stages.
 * 
 * @author Christian
 *
 */
public class AggrIterateOperator extends AggrPartOperator {
	
	/**
	 * Constructor to split Aggrgeation into three.
	 * 
	 * @param model Aggregation operator being split.
	 */
	public AggrIterateOperator(final AggregationOperator model) {
        super(model);
        this.setOperatorName("AGGR_ITERATEL");
        this.setNesCTemplateName("aggriter");
        updateIntermediateAttributes();
	}

	/**
	 * Constructor for shallow clone.
	 * 
	 * @param model Aggregation operator being split.
	 */
	private AggrIterateOperator(final AggrIterateOperator model) {
        super(model);
        this.setOperatorName("AGGR_ITERATEL");
        this.setNesCTemplateName("aggriter");
	}
	
    private final double getTimeCost(final int tuples) { 
		return getOverheadTimeCost()
			+ CostParameters.getDoCalculation() * tuples
			+ CostParameters.getCopyTuple();
    }
	
	/** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		final int tuples 
			= this.getInput(0).getCardinality(card, node, daf);
		return getTimeCost(tuples);
    }
	
    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances) {
		final int tuples = this.getInputCardinality(card, 0, numberOfInstances);
		return getTimeCost(tuples);
	}

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
		AlphaBetaExpression result = new AlphaBetaExpression();
		result.addBetaTerm(getOverheadTimeCost());
		result.addBetaTerm(CostParameters.getCopyTuple());
		AlphaBetaExpression tuples 
			= this.getInputCardinality(card, node, daf, round, 0);
		tuples.multiplyBy(CostParameters.getDoCalculation());
		result.add(tuples);
		return result;
	}
	
	/** 
	 * Creates a copt of this operator.
	 * @return A shallow copy of this operator.
	 */
    public final AggrIterateOperator shallowClone() {
    	final AggrIterateOperator clonedOp = new AggrIterateOperator(this);
    	return clonedOp;
    }

    /**
     * Used to determine if the operator is Attribute sensitive.
     *
     * @return false.
     */
    public final boolean isAttributeSensitive() {
        return false;
    }

    /** {@inheritDoc} */
    public final boolean isRecursive() {
        return true;
    }
 
	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card) {
		int inCard = getInput(0).getCardinality(card);
		if (inCard <= 1) {
			return inCard;
		}
		return inCard - 1;
	}

	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		return 1;
	}
	
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		final AlphaBetaExpression result = new AlphaBetaExpression();
		result.addBetaTerm(1);
    	return result;
    }
	
}
