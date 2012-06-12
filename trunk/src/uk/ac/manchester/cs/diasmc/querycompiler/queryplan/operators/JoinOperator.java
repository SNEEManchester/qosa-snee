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

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.NoPredicate;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operator_rewriting.OperatorFactory;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/** 
 * A Join or Cross product operator.
 * 
 * Will join exactly two inputs
 * 
 * @author Christian
 *
 */
public class JoinOperator extends OperatorImplementation implements Operator {

   /** 	Standard Java logger. * /
   private static Logger logger 
   		= Logger.getLogger(JoinOperator.class.getName());

   /** 
	 * List of the expressions to create the output attributes.
	 * 
	 * attributes used if not set/
	 */
	private ArrayList <Expression> expressions = null;;
    
	/**
	 * List of the attributes output by this operator.
	 */
    private ArrayList <Attribute> attributes;
    
    /**
     * Constructs a new Join operator.
     * 
     * @param token A JoinOperator token
     */
    public JoinOperator(final AST token) {
        super();
       
        this.setOperatorName("JOIN");
        this.setNesCTemplateName("join");
 
        setOperatorDataType(token);
        this.setParamStr("");
        
        AST leftToken = token.getFirstChild();
        AST rightToken = leftToken.getNextSibling();
        
        Operator leftOperator = OperatorFactory.convertAST(leftToken);
        Operator rightOperator = OperatorFactory.convertAST(rightToken);

        setChildren(new Operator[] {leftOperator, rightOperator});
        getIncomingAttributes();
    }  
    
    /** 
     * Sets the operator data type based on the AST token.
     * 
     * @param token AST token for the operator.
     */
    private void setOperatorDataType(final AST token) {
        switch (token.getType()) {
        	case SNEEqlOperatorParserTokenTypes.RELCROSSPRODUCT: 
        		setOperatorDataType(OperatorDataType.RELATION);
        		return;
        	case SNEEqlOperatorParserTokenTypes.STRRELCROSSPRODUCT: 
        		setOperatorDataType(OperatorDataType.STREAM);
        		return;
        	case SNEEqlOperatorParserTokenTypes.WINCROSSPRODUCT: 
        		setOperatorDataType(OperatorDataType.WINDOWS);
        		return;
        	case SNEEqlOperatorParserTokenTypes.WINRELCROSSPRODUCT: 
        		setOperatorDataType(OperatorDataType.WINDOWS);
        		return;
        	default: throw new AssertionError("Unexpected AST token"); 
    	}
    }
    
    /**
     * Constructor that creates a new operator
     * based on a model of an existing operator.
     *
     * Used by both the clone method and the constuctor of the physical methods.
     *
     * @param model Another JoinOperator 
     *   upon which this new one will be cloned.
     */
    protected JoinOperator(final JoinOperator model) {
        super(model);
        this.expressions = model.expressions;
        this.attributes = model.attributes;
   }

    /**
     * Non Haskell constructor.
     * @param rootOperator Left input
     * @param right Second input operator
     * @param bestJoinPreds Predicates that should be applied.
     */
    public JoinOperator(final Operator rootOperator, final Operator right, 
    		final ArrayList<Predicate> bestJoinPreds) {
        this.setOperatorName("JOIN");
        this.setNesCTemplateName("join");
 
		setOperatorDataType(OperatorDataType.WINDOWS);
        this.setParamStr("");
        
        setChildren(new Operator[] {rootOperator, right});
        Predicate[] predicateArray = new Predicate[bestJoinPreds.size()];
        predicateArray = bestJoinPreds.toArray(predicateArray); 
        getIncomingAttributes();
    	setPredicate(ExpressionConvertor.convert(predicateArray, attributes));
	}

	/**
     * Combines the attribute lists from the left and from the right.
     */
    private void getIncomingAttributes() {
    	attributes = new ArrayList<Attribute>(getInput(0).getAttributes());
    	ArrayList <Attribute> right
    		= new ArrayList<Attribute>(getInput(1).getAttributes());
    	try {
			right.remove(new EvalTimeAttribute());
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
		}
		attributes.addAll(right);
        expressions = new ArrayList <Expression>(attributes.size());
		for (int i = 0; i < attributes.size(); i++) {
			expressions.add(attributes.get(i));
		}
    }
    
    
    /** 
	 * List of the attribute returned by this operator.
	 * 
	 * @return ArrayList of the returned attributes.
	 */ 
	public final ArrayList<Attribute> getAttributes() {
		return attributes;
	}
    
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {

    	boolean accepted = false;
    	
    	if (projectAttributes.size() > 0) {
    		if (projectExpressions.size() == 0) {
    			//remove unrequired attributes. No expressions to accept
    			for (int i = 0; i < attributes.size(); ) {
    				if (projectAttributes.contains(attributes.get(i)))
    					i++;
    				else {
    					attributes.remove(i);
    					expressions.remove(i);		
    				}
    			}
    		}	
    		else {
    	   		expressions = projectExpressions;
    	    	attributes = projectAttributes;
    	    	accepted = true;
    		}
    	}
    	
    	ArrayList<Attribute> requiredLeftAttributes = (ArrayList<Attribute>)getInput(0).getAttributes().clone();
    	ArrayList<Attribute> unrequiredLeftAttributes = (ArrayList<Attribute>)requiredLeftAttributes.clone();
    	ArrayList<Attribute> requiredRightAttributes = (ArrayList<Attribute>)getInput(1).getAttributes().clone();
    	ArrayList<Attribute> unrequiredRightAttributes = (ArrayList<Attribute>)requiredRightAttributes.clone();

   		for (int i = 0; i < expressions.size(); i++) {
   			ArrayList<Attribute> requiredAttributes = expressions.get(i).getRequiredAttributes();
   	   		for (int j = 0; j < requiredAttributes.size(); j++) {
   	   			if (unrequiredLeftAttributes.contains(requiredAttributes.get(j)))
   	   	   			unrequiredLeftAttributes.remove(requiredAttributes.get(j));
   	   			if (unrequiredRightAttributes.contains(requiredAttributes.get(j)))
   	   	   			unrequiredRightAttributes.remove(requiredAttributes.get(j));
   	   		}	
   		}

    	requiredLeftAttributes.removeAll(unrequiredLeftAttributes);
    	requiredRightAttributes.removeAll(unrequiredRightAttributes);
    	
    	getInput(0).pushProjectionDown(new  ArrayList<Expression>(), requiredLeftAttributes);
    	getInput(1).pushProjectionDown(new  ArrayList<Expression>(), requiredRightAttributes);

	  	return accepted;
	}
	
	 /** 
	  * {@inheritDoc}
	  * Should never be called as there is always a project or aggregation 
	  * between this operator and the rename operator.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 throw new AssertionError("Unexpected call to pushLocalNameDown()"); 
	 }

	/**
	 * Calculated the cardinality based on the requested type.
	 * Currently very crued. 
	 * 
	 * @param card Type of cardinailty to be considered.
	 * 
	 * @return The Cardinality calulated as requested.
	 */
	public final int getCardinality(final CardinalityType card) {
		int left = getInput(0).getCardinality(card);
		int right = getInput(1).getCardinality(card);
		if (getPredicate() instanceof NoPredicate) {
			return (left * right);
		}
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return (left * right);
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
			return (left * right) / Constants.JOIN_PREDICATE_SELECTIVITY;
		}
        if (card == CardinalityType.MINIMUM) {
    		return 0;
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
	}

    /**
     * Used to determine if the operator is Attribute sensitive.
     *
     * @return true`.
     */
    public final boolean isAttributeSensitive() {
        return true;
    }

    /** {@inheritDoc} */
    public final boolean isLocationSensitive() {
        return false;
    }
 
    /** {@inheritDoc} */
    public final boolean isRecursive() {
        return false;
    }
 
    /** {@inheritDoc} */
    public final String toString() {
        return this.getText() + "[ " + getInput(0).toString() 
        	+ "," + getInput(0).toString() + " ]";
    }
 
    /** {@inheritDoc} */
    public final JoinOperator shallowClone() {
        final JoinOperator clonedOp = new JoinOperator(this);
        return clonedOp;
    }

    /** {@inheritDoc} */
    public final double getTimeCost(final int tuples) {
    	return getOverheadTimeCost()
    		+ CostParameters.getCopyTuple() * tuples
    		+ CostParameters.getApplyPredicate() * tuples;
        }

    /** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
    	final int left = getInputCardinality(card, node, daf, 0);
    	final int right = getInputCardinality(card, node, daf, 0);
    	final int tuples = left * right;
    	return getTimeCost(tuples);
        }

    /** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		int numberOfInstances) {
    	assert (numberOfInstances == 1);
    	final int left = getInputCardinality(card, 0, 1);
    	final int right = getInputCardinality(card, 0, 1);
    	final int tuples = left * right;
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
		tuples.multiplyBy(getInputCardinality(card, node, daf, round, 1));
		tuples.multiplyBy(CostParameters.getDoCalculation()
			+ CostParameters.getApplyPredicate());
		result.add(tuples);
		return result;
	}
    /** {@inheritDoc} */
    public final int getCardinality(final CardinalityType card, 
    		final Site node, final DAF daf) {
    	int left = getInputCardinality(card, node, daf, 0);
    	int right = getInputCardinality(card, node, daf, 1);
		if (getPredicate() instanceof NoPredicate) {
			return (left * right);
		}
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return (left * right);
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
			return (left * right) / Constants.JOIN_PREDICATE_SELECTIVITY;
		}
        if (card == CardinalityType.MINIMUM) {
    		return 0;
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
    }

	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		AlphaBetaExpression left 
			= getInputCardinality(card, node, daf, round, 0);
		AlphaBetaExpression right 
			= getInputCardinality(card, node, daf, round, 1);
		if (getPredicate() instanceof NoPredicate) {
			return AlphaBetaExpression.multiplyBy(left, right);
		}
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return AlphaBetaExpression.multiplyBy(left, right);
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
        	AlphaBetaExpression result 
        		= AlphaBetaExpression.multiplyBy(left, right);
        	result.divideBy(Constants.JOIN_PREDICATE_SELECTIVITY);
			return result;
		}
        if (card == CardinalityType.MINIMUM) {
    		return new AlphaBetaExpression();
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
    }

    /** {@inheritDoc} */
    public final boolean acceptsPredicates() {
        return true;
    }
    
    /** {@inheritDoc} */
    public final boolean isRemoveable() {
    	return false;
    }

    /** {@inheritDoc} */
	public final ArrayList<Expression> getExpressions() {
		return expressions;
	}

    /** {@inheritDoc} */
	public final boolean comesFromRightChild(final String attrName) {
		// TODO Auto-generated method stub
		return false;
	}
	
    //Call to default methods in OperatorImplementation

    /** {@inheritDoc} */
    public final int[] getSourceSites() {
    	return super.defaultGetSourceSites();
    }

	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(final Site node, final DAF daf) {
    	return super.defaultGetOutputQueueCardinality(node, daf);
    }

 	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(int numberOfInstances) {
    	return super.defaultGetOutputQueueCardinality(numberOfInstances);
    }

    /** {@inheritDoc} */    
	public final int getDataMemoryCost(final Site node, final DAF daf) {
		return super.defaultGetDataMemoryCost(node, daf);
	}


}
