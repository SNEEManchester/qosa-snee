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

import java.util.ArrayList;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.AggregationExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;

/** 
 * Part of a split Aggregation operator.
 * Allows keeping updateIntermediateAttributes method in only one place. 
 * 
 * @author Christian
 *
 */
public abstract class AggrPartOperator extends PredicateOperator {
	
	/** 
	 * List of the aggregation within the requested expression.
	 */
	private ArrayList<AggregationExpression> aggregates;

	/**
	 * Constructor for shallow clone.
	 * 
	 * @param model Aggregation operator being split.
	 */
	protected AggrPartOperator(final AggrPartOperator model) {
        super(model);
        this.aggregates = model.getAggregates();
	}

	/**
	 * Constructs a new operator base on the aggregate but with a new id.
	 * 
	 * @param model Aggregation operator being split.
	 */
	protected AggrPartOperator(final AggregationOperator model) {
        super(model, true);
        this.aggregates = model.getAggregates();
	}
	
	/**
     * Extracts and records the intermediate Attributes.
     */
	protected final void updateIntermediateAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		try {
			attributes.add(new EvalTimeAttribute());
			// skip 0 which is EvalTime
			for (int i = 1; i < aggregates.size(); i++) {
				if (aggregates.get(i).getAggregationType() 
						== AggregationType.AVG) {
					attributes.add(new DataAttribute(
							Constants.PARTIAL_LOCALNAME, 
							Constants.AVG_PARTIAL_HEAD 
							+ aggregates.get(i).getShortName(),  
						aggregates.get(i).getExpression().getType()));
				} else {
					attributes.add(new DataAttribute(
						Constants.PARTIAL_LOCALNAME, 
						aggregates.get(i).getShortName(), 
						aggregates.get(i).getType()));
				}
			}
			setAttributes(attributes);
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
		}	
	}
 		
   /**
     * gets the aggregates.
     * @return the Aggregates.
     */
    public final ArrayList<AggregationExpression> getAggregates() {
		return aggregates;
	}

    /** {@inheritDoc} */
	public final boolean isRemoveable() {
		return false;
	}
 
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
	  	throw new OptimizationException(
	  		"push Project down should be done before splitting aggregates");
	}

}
