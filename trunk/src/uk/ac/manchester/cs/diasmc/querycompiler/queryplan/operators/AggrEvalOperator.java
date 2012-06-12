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
 * to identify it as the third of three stages.
 * 
 * @author Christian
 *
 */
public class AggrEvalOperator extends AggrPartOperator {
	
	/**
	 * Constructor to split Aggrgeation into three.
	 * 
	 * @param model Aggregation operator being split.
	 */
	public AggrEvalOperator(final AggregationOperator model) {
        super(model);
        this.setOperatorName("AGGR_EVAL");
        this.setNesCTemplateName("aggreval");
        assert (!this.id.equals(model.getID()));
	}

	/**
	 * Constructor for shallow clone.
	 * 
	 * @param model Aggregation operator being split.
	 */
	private AggrEvalOperator(final AggrEvalOperator model) {
        super(model);
        this.setOperatorName("AGGR_EVAL");
        this.setNesCTemplateName("aggreval");
	}

    private final double getTimeCost() {
		return getOverheadTimeCost()
			+ CostParameters.getDoCalculation()
			+ CostParameters.getCopyTuple();
    }

    /** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		return getTimeCost();
    }

    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances) {
		return getTimeCost();
	}

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
		AlphaBetaExpression result = new AlphaBetaExpression();
		result.addBetaTerm(getOverheadTimeCost() + CostParameters.getCopyTuple()
				+ CostParameters.getDoCalculation());
		return result;
	}

	/** {@inheritDoc} */
    public final AggrEvalOperator shallowClone() {
    	final AggrEvalOperator clonedOp = new AggrEvalOperator(this);
    	return clonedOp;
    }

    /**
     * Used to determine if the operator is Attribute sensitive.
     *
     * @return true.
     */
    public final boolean isAttributeSensitive() {
        return true;
    }
 
    /** {@inheritDoc} */
    public final boolean isRecursive() {
        return false;
    }

    /** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card) {
		return 1;
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
