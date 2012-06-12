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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions;

import java.util.ArrayList;

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggregationType;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Aggregate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Aggregate.AggregateType;

/** Expression to hold an aggregation. */
public class AggregationExpression implements Expression {

	/** 
	 * Keeps track of the next aggregation numeration to assign.
	 */
	private static int nextNumeration = 1;
	
	/**
	 * The numeration assigned to this aggregation.
	 * Used to assign short unique local names. 
	 */
	private int numeration;
	
	/** The expression over which the aggregation will be done. */
	private Expression expression;
	
	/** The aggregation to be done. */
	private AggregationType type;
	
	/**
	 * Constructor.
	 * 
	 * @param token An AST token representing the Aggregation
	 * @param incoming The Expression of the aggreagtion and its input.
	 */
	public AggregationExpression(final AST token,
			final ArrayList<Attribute> incoming) {
        AST child = token.getFirstChild();
        expression = ExpressionConvertor.convert(child, incoming);
        setOperatorDataType(token);
        numeration = nextNumeration++;
	}
		
	/** 
	 * Old Constructor.
	 * @param inner Expression being aggregated.
	 *    Can be null if AggType = Count;
	 * @param aggType Type of aggregation.
	 */
	public AggregationExpression(final Expression inner, 
			final AggregateType aggType) {
        expression = inner;
        setOperatorDataType(aggType);
        numeration = nextNumeration++;
	}
	
    /** 
     * Sets the type based on the AST token.
     * 
     * @param token AST token for the expression.
     */
    private void setOperatorDataType(final AST token) {
        switch (token.getType()) {
        	case SNEEqlOperatorParserTokenTypes.AVG: 
        		type = AggregationType.AVG;
        		return;
        	case SNEEqlOperatorParserTokenTypes.COUNT: 
        		type = AggregationType.COUNT;
        		return;
        	case SNEEqlOperatorParserTokenTypes.MAX: 
        		type = AggregationType.MAX;
        		return;
        	case SNEEqlOperatorParserTokenTypes.MIN: 
        		type = AggregationType.MIN;
        		return;
        	case SNEEqlOperatorParserTokenTypes.SUM: 
        		type = AggregationType.SUM;
        		return;
        	default: throw new AssertionError("Unexpected AST token " + token); 
    	}
    }

    /** 
     * Sets the type based on the old type.
     * @param aggType Old type.
      */
    private void setOperatorDataType(final Aggregate.AggregateType aggType) {
        if (aggType == Aggregate.AggregateType.AVG) {
        	type = AggregationType.AVG;
        } else if (aggType == Aggregate.AggregateType.COUNT) {
        	type = AggregationType.COUNT;
        } else if (aggType == Aggregate.AggregateType.MAX) {
        	type = AggregationType.MAX;
        } else if (aggType == Aggregate.AggregateType.MIN) {
        	type = AggregationType.MIN;
        } else if (aggType == Aggregate.AggregateType.SUM) {
        	type = AggregationType.SUM;
        }  else {
        	throw new AssertionError("Unexpected type " + aggType); 
        }
    }

    /** {@inheritDoc}*/
	public final ArrayList<Attribute> getRequiredAttributes() {
		return expression.getRequiredAttributes();
	}
	
	/** {@inheritDoc} */
	public final AttributeType getType() {
		try {
			if (type == AggregationType.AVG) {
				return Types.getType("integer");
			} 
			if (type == AggregationType.COUNT) {
				return Types.getType("integer");
			} 
			return expression.getType();
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
			return null;
		}
	}
	
	/** 
	 * Gets the type of the aggregation.
	 * @return The type of the aggregation.
	 */
	public final AggregationType getAggregationType() {
		return type;
	}
	
	/** 
	 * Checks if any of the aggregates need count.
	 * For example Average and Count both need a count kept.
	 * 
	 * @return TRUE if one or more of the aggregates need a count kept.
	 */
	public final boolean needsCount() {
		if (type == AggregationType.AVG) {
			return true;
		} 
		if (type == AggregationType.COUNT) {
			return true;
		}
		return false;
	}
	
	/** {@inheritDoc}*/
	public final String toString() {
		return (type + "(" + expression + ")");
	}
	
	/**
	 * Assign a value to any intermediate values 
	 * used to represent partial results. 
	 * @return A unique string name for this attribute.
	 */
	public final String getShortName() {
		if (type == AggregationType.COUNT) {
			return "count";
		}
		return type.toString() + numeration;		
	}

	/** 
	 * Extracts the aggregates from within this expression.
	 * 
	 * @return An array List of all the aggregates within this expressions.
	 * Could contain duplicates.
	 */
	public final ArrayList<AggregationExpression> getAggregates()	{
		ArrayList<AggregationExpression> list 
			= new ArrayList<AggregationExpression>(1);
		list.add(this);
		return list;
	}

	/** 
	 * Gets the expression inside the aggregate.
	 * @return The input expression.
	 */
	public final Expression getExpression() {
		return expression;
	}

}
