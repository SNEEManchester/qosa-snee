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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.AggregationExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Aggregate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.OldAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.translation.Aggregate.AggregateType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;
import antlr.collections.AST;

/**
 * Aggregation operator.
 * Either for before the Aggreagtion is split in three 
 * Or as a superclass for the three seperate Operators.
 * 
 * @author Christian
 */
public class AggregationOperator extends PredicateOperator {

	/** 
	 * List of the aggregation within the requested expression.
	 */
	private ArrayList<AggregationExpression> aggregates;
	
	/**
	 * Constructor based on AST tokens.
	 * 
	 * @param token An Aggregation token.
	 */
	public AggregationOperator(final AST token) {
    	super(token);
        this.setOperatorName("AGGREGATION");
        this.setNesCTemplateName("aggregation");
        setAggregates();
        this.setOperatorDataType(token);
    }

	/**
	 * Old Constructor.
	 * @param inputOperator Previous operator
	 * @param groupByList Must be empty.
	 * @param newAggregates List of Aggregation required.
	 * @deprecated
	 */
    public AggregationOperator(final Operator inputOperator, 
    		final Collection<OldAttribute> groupByList, 
    		final HashSet<Aggregate> newAggregates) {
        super();

        // group by not supported.
        assert (groupByList.size() == 0);
        this.setOperatorName("AGGREGATION");
        this.setNesCTemplateName("aggregation");
 
        setOperatorDataType(inputOperator.getOperatorDataType());
        this.setParamStr("");
                
        ArrayList<Attribute> incoming = inputOperator.getAttributes();
        ArrayList<Expression>expressions 
        	= new ArrayList <Expression>(newAggregates.size());
        ArrayList<Attribute> attributes 
        	= new ArrayList <Attribute>(newAggregates.size()); 
		try {
			attributes.add(new EvalTimeAttribute());
			expressions.add(new EvalTimeAttribute());
		} catch (SchemaMetadataException e) {	
			Utils.handleCriticalException(e);
		}
		 Iterator<Aggregate> aggIt = newAggregates.iterator();
		 while (aggIt.hasNext()) {
			 Aggregate aggregate = aggIt.next();
       
        	Expression expression 
        		= ExpressionConvertor.convert(aggregate, incoming);
        	expressions.add(expression);
        	attributes.add(convertToAttribute(expression));
        }
	        setExpressions(expressions);
	        setAttributes(attributes);
        setChildren(new Operator[] {inputOperator});
        setAggregates();
    }
    
    /**
     * Constuctor based on another operator.
     * @param model Operator to copy values from.
     */
    public AggregationOperator(final AggregationOperator model) {
    	super(model);
    	this.aggregates = model.aggregates;
    }
 
    /**
     * Extracts and saves the actual aggregates from within the expressions.
     * 
     * For example an expression could be max(temp) - avg(temp)
     * Which contains two aggregates. 
     */
    private void setAggregates() {
		boolean needsCount = false;
    	aggregates = new ArrayList<AggregationExpression>();
    	//Place a null to match the evalTime
    	aggregates.add(null);
    	for (int i = 0; i < getExpressions().size(); i++) {
    		ArrayList<AggregationExpression> newAggs 
    			=  getExpressions().get(i).getAggregates();
    		for (int j = 0; j < newAggs.size(); j++) {
    			if (newAggs.get(j).getAggregationType() 
    					== AggregationType.COUNT) {
    				needsCount = true;
    			} else {
        			if (newAggs.get(j).needsCount()) {
        				needsCount = true;
        			}
    				if (!aggregates.contains(newAggs.get(j))) {
    					aggregates.add(newAggs.get(j));
    				}
    			}
    		}
    	}
    	if (needsCount) {
    		AggregationExpression count 
    			= new AggregationExpression(null, AggregateType.COUNT); 
    		aggregates.add(count);
    	}
    }
    
    /**
     * gets the aggregates.
     * @return the Aggregates.
     */
    protected final ArrayList<AggregationExpression> getAggregates() {
		return aggregates;
	}
    
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
    	
    	//TODO remove attributes not require upstream
    	//System.out.println(this.getExpressions());
    	//System.out.println(this.getAttributes());
    	
    	ArrayList<Attribute> requiredAttributes = new ArrayList<Attribute>(); 
    	for (int i = 0; i < getExpressions().size(); i++) {
    		ArrayList<Attribute> requiredAttributesX = getExpressions().get(i).getRequiredAttributes();
    		requiredAttributes.addAll(requiredAttributesX);
    	}
    	System.out.println(requiredAttributes);
    	getInput(0).pushProjectionDown(new  ArrayList<Expression>(), requiredAttributes);
    	
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

	/**
	 * Converts an Expression to an output Attribute.
	 * @param e Expression which must be an AggregationExpression
	 * @return An Attribute representing the result of the expression
	 */
    private DataAttribute convertToAttribute(final Expression e) {
    	AggregationExpression agg = (AggregationExpression) e; 
        return new DataAttribute(agg.getShortName(), e.getType());
    }

	/** 
     * Sets the operator data type based on the AST token.
     * 
     * @param token AST token for the operator.
     */
    protected final void setOperatorDataType(final AST token) {
        switch (token.getType()) {
        	case SNEEqlOperatorParserTokenTypes.RELAGGREGATION: 
        		setOperatorDataType(OperatorDataType.RELATION);
        		return;
        	case SNEEqlOperatorParserTokenTypes.WINAGGREGATION: 
        		setOperatorDataType(OperatorDataType.WINDOWS);
        		return;
        	default: throw new AssertionError("Unexpected AST token"); 
        	}
    }
 
    /** {@inheritDoc} */
     public final AggregationOperator shallowClone() {
    	final AggregationOperator clonedOp = new AggregationOperator(this);
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

	private final double getTimeCost(final int tuples) {
		return getOverheadTimeCost()
				+ CostParameters.getCopyTuple() 
				+ CostParameters.getDoCalculation() * tuples;
	}

    /** {@inheritDoc} */
	public final double getTimeCost(final CardinalityType card,
			final Site node, final DAF daf) {
		final int tuples = this.getInputCardinality(card, node, daf, 0);
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
		result.addBetaTerm(getOverheadTimeCost() + CostParameters.getCopyTuple());
		AlphaBetaExpression tuples 
			= this.getInputCardinality(card, node, daf, round, 0);
		tuples.multiplyBy(CostParameters.getDoCalculation());
		result.add(tuples);
		return result;
	}
	
	/** {@inheritDoc} */
	public final boolean isRemoveable() {
		return false;
	}

    /** {@inheritDoc} */
    public final boolean isRecursive() {
        return false;
    }
 


}
