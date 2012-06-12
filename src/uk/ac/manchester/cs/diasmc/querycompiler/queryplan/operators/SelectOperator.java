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
import java.util.Iterator;

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.MultiExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operator_rewriting.OperatorFactory;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * Select operator for the situations the predicates can not be pushed down
 *  into the next operator.
 * @author Christian
 *
 */
public class SelectOperator extends OperatorImplementation {    
    
    /**
     * Constucts a new Select Oprator.
     * Called because previous operator does not accept predicates.
     * @param predicate Predicate to apply
     * @param inputOperator Previous Operator.
     */
    private SelectOperator(final Expression predicate, 
    		final Operator inputOperator) {
        super();

        this.setOperatorName("SELECT");
        this.setNesCTemplateName("select");
        setOperatorDataType(inputOperator.getOperatorDataType());
        
        setChildren(new Operator[] {inputOperator});

        setPredicate(predicate);
        this.setParamStr(getPredicate().toString());
    }  

    //used by clone method
    /**
     * Constructor that creates a new operator 
     * based on a model of an existing operator.
     * 
     * Used by both the clone method and the constuctor of the physical methods.
     * @param model Operator to copy values from.
     */
   protected SelectOperator(final SelectOperator model) {
    	super(model);
    }  
   
   /**
    * Old Constructor.
    * @param input Previous Operator.
    * @param predicates Predicates to be applied.
    * @deprecated
    */   
   public SelectOperator(final Operator input, 
		   final Collection<Predicate> predicates) {
       super();

       this.setOperatorName("SELECT");
       this.setNesCTemplateName("select");
       setOperatorDataType(input.getOperatorDataType());

       setChildren(new Operator[] {input});

       ArrayList<Attribute> incoming = input.getAttributes();
       final Iterator<Predicate> predIter = predicates.iterator();
       Expression[] expressions = new Expression[2];
       while (predIter.hasNext()) {
    	   final Predicate p = predIter.next();
    	   expressions[0] = ExpressionConvertor.convert(
    			   p.getArgument1(), incoming);
    	   expressions[1] = ExpressionConvertor.convert(
    			   p.getArgument2(), incoming);
    	   setPredicate(new MultiExpression(expressions, p.getCompare()));
        }	 
       this.setParamStr(getPredicate().toString());
    }

   /**
    * Builds an Operator tree up to this select.
    * Will attempt to place the predicate in the previous operator.
    * Otherwise adds a select operator.
    * @param token A SelectOperator token
    * @return Either a SelectOperator or the previous operator
    * with the predicate of the select.
    */
   public static Operator applySelect(final AST token) {
       AST expressionsToken = token.getFirstChild();
       AST child = expressionsToken.getNextSibling();

       Operator inputOperator 
       	= OperatorFactory.convertAST(child);
       ArrayList<Attribute> incoming = inputOperator.getAttributes();
       
       Expression predicate 
          = ExpressionConvertor.convert(expressionsToken, incoming);
       if (inputOperator.acceptsPredicates()) {
    	   inputOperator.setPredicate(
    			   ExpressionConvertor.convert(expressionsToken, incoming));
    	   return inputOperator;
       }
       return new SelectOperator(predicate, inputOperator);
   }  
	
   /**
    * {@inheritDoc}
    */
   public final boolean pushProjectionDown(
   		final ArrayList<Expression> projectExpressions, 
   		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
	  	return false;
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
		int input = getInput(0).getCardinality(card);
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return input;
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
			return input / Constants.JOIN_PREDICATE_SELECTIVITY;
		}
        if (card == CardinalityType.MINIMUM) {
    		return 0;
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
	}

	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		int input = getInputCardinality(card, node, daf, 0);
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return input;
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
			return input / Constants.JOIN_PREDICATE_SELECTIVITY;
		}
        if (card == CardinalityType.MINIMUM) {
    		return 0;
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
	}
	
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		AlphaBetaExpression input = 
			getInputCardinality(card, node, daf, round, 0);
        if ((card == CardinalityType.MAX) 
        		|| (card == CardinalityType.PHYSICAL_MAX)) {
			return input;
        } 
        if ((card == CardinalityType.AVERAGE) 
        		|| (card == CardinalityType.MAX)) {
        	input.divideBy(Constants.JOIN_PREDICATE_SELECTIVITY);
			return input;
		}
        if (card == CardinalityType.MINIMUM) {
    		return new AlphaBetaExpression();
        } 
        throw new AssertionError("Unexpected CardinaliyType " + card);
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
	public final boolean isLocationSensitive() {
		return false;
	}

    /** {@inheritDoc} */
	public final boolean isRecursive() {
		return false;
	}
	
    /** {@inheritDoc} */
    public final boolean acceptsPredicates() {
        return true;
    }
    
    /** {@inheritDoc} */
	public final String toString() {
//        
		return this.getText() + "[ " + super.getInput(0).toString() + " ]";
    }

    /** {@inheritDoc} */
	public final SelectOperator shallowClone() {
		final SelectOperator clonedOp = new SelectOperator(this);
		return clonedOp;
	}
	
    private final double getTimeCost(final int tuples) {
		return getOverheadTimeCost()
			+ (CostParameters.getCopyTuple() 
			+ CostParameters.getApplyPredicate()) * tuples;
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
		result.addBetaTerm(getOverheadTimeCost());
		AlphaBetaExpression tuples 
			= this.getInputCardinality(card, node, daf, round, 0);
		tuples.multiplyBy(CostParameters.getCopyTuple() 
				+ CostParameters.getApplyPredicate());
		result.add(tuples);
		return result;
	}
	
     /** {@inheritDoc} */
    public final boolean isRemoveable() {
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
    public final ArrayList<Attribute> getAttributes() {
    	return super.defaultGetAttributes();
    }

    /** {@inheritDoc} */    
	public final ArrayList<Expression> getExpressions() {
		return super.defaultGetExpressions();
	}

	/** {@inheritDoc} */    
	public final int getDataMemoryCost(final Site node, final DAF daf) {
		return super.defaultGetDataMemoryCost(node, daf);
	}

}
