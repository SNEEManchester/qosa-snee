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
import java.util.logging.Logger;

import antlr.collections.AST;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.CodeGenUtils;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operator_rewriting.OperatorFactory;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * Encapsulates a Deliver operator, 
 * which should be placed on the destination site of query plan results.
 * @author Ixent Galpin, Christian Brenninkmeijer and Steven Lynden 
 */
public class DeliverOperator extends OperatorImplementation {
    	
	/** Standard Java Logger. */
	private static Logger logger 
		= Logger.getLogger(DeliverOperator.class.getName());
	/**
     * Constructs a new Deliver operator.
     * 
     * @param token A DeleiverOperator token
     */
    public DeliverOperator(final AST token) {
        super();
        Operator inputOperator 
           = OperatorFactory.convertAST(token.getFirstChild());

        this.setOperatorName("DELIVER");
        this.setNesCTemplateName("deliver");
        this.setOperatorDataType(inputOperator.getOperatorDataType());
        this.setParamStr("");
        
        setChildren(new Operator[] {inputOperator});
    }  
  
	/**
	 * Old Constuctor.
	 * @deprecated
	 * @param inputOperator Previous operator.
	 */
    public DeliverOperator(final Operator inputOperator) {
        super();

        this.setOperatorName("DELIVER");
        this.setNesCTemplateName("deliver");
        this.setOperatorDataType(inputOperator.getOperatorDataType());
        this.setParamStr("");
        
        setChildren(new Operator[] {inputOperator});
        if (inputOperator == null) {
        	throw new AssertionError("input operator can not be null.");
        }
    }  
    
    /**
     * Constructor that creates a new operator 
     * based on a model of an existing operator.
     * 
     * Used by the clone method.
     * 
     * @param model Another DeliverOperator on which to base new one.
     */
    protected DeliverOperator(final DeliverOperator model) {
    	super(model);
    }
   
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
	  	return getInput(0).pushProjectionDown(
	  			projectExpressions, projectAttributes);
	}

	 /** 
	  * {@inheritDoc}
	  * Should never be called as deliver is after the rename operator.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 throw new AssertionError("Unexpected call to pushLocalNameDown()"); 
	 }

	/**
	 * Calculated the cardinality based on the requested type. 
	 * 
	 * @param card Type of cardinailty to be considered.
	 * 
	 * @return The Cardinality calulated as requested.
	 */
	public final int getCardinality(final CardinalityType card) {
		return (this.getInput(0)).getCardinality(card);
	}
   
	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		return getInputCardinality(card, node, daf, 0);
	}
	
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		return getInputCardinality(card, node, daf, round, 0);
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
		return true;
	}
	
	/** {@inheritDoc} */
	public final boolean isRecursive() {
		return false;
	}
	
    /** {@inheritDoc}
     * @return false;
     */
    public final boolean acceptsPredicates() {
        return false;
    }

    /** {@inheritDoc} */
 	public final String toString() {
        return this.getText() + "[ " + super.getInput(0).toString() + " ]";  
    }

	/** {@inheritDoc} */
	public final DeliverOperator shallowClone() {
		final DeliverOperator clonedOp = new DeliverOperator(this);
		return clonedOp;
	}
    /** 
     * Calculates the physical size of the state of this operator.
     * 
     * This cost model assumes that deliver is instantaneous
     * so this operator has ne need to ever store data.
     * 
     * Does not included the size of the input 
     * as these are assumed passed by reference.
     *
     * Does not include the size of the code itself.
     * 
     * @param node Physical mote on which this operator has been placed.
     * @param daf Distributed query plan this operator is part of.
     * @return OutputQueueCardinality * PhytsicalTuplesSize
     */
    public final int getDataMemoryCost(final Site node, final DAF daf) {
    	return 0;
    }

    /** Constant for length of per tuple overhead String. */ 	
    private static final int DELIVER_OVERHEAD = 10; //"DELIVER (" ++ ")";
    
    /** Maximum String length to represent an attribute. */
    private static final int ATTRIBUTE_STRING_LENGTH = 5; //unit16 max = 65536
    
    /** Maximum size of a deliver packet. */
    public static final int DELIVER_PAYLOAD_SIZE = 28;
    
    /**
     * Objains the size of the String needed to represent this tuple.
     * @return Output String size in bytes
     */
    private final int packetsPerTuple() {
		int tupleSize = DELIVER_OVERHEAD;
		final ArrayList<Attribute> attributes = getAttributes(); 
		for (int i = 0; i < attributes.size(); i++) {
			String attrName = CodeGenUtils.getDeliverName(attributes.get(i));			
			tupleSize += attrName.length() + ATTRIBUTE_STRING_LENGTH;
			logger.finest("TuplesSize now " + tupleSize);
		}
		return (int) Math.ceil(tupleSize / DELIVER_PAYLOAD_SIZE);
    }
    
    private final double getTimeCost(final int tuples) { 
		final int packets = packetsPerTuple() * tuples;
		double duration = getOverheadTimeCost()
			+ CostParameters.getDeliverTuple() * packets;
		return duration;
    }

	/** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		final int tuples 
			= this.getInputCardinality(card, node, daf, 0);
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
		tuples.multiplyBy(packetsPerTuple());
		tuples.multiplyBy(CostParameters.getDeliverTuple());
		result.add(tuples);
		return result;
	}
	
   /**
     * Some operators do not change the data in any way those could be removed.
     * This operator does change the data so can not be. 
     * 
     * @return False. 
     */
    public final boolean isRemoveable() {
    	return false;
    }
    
    //Call to default methods in OperatorImplementation

    /** {@inheritDoc} */
    public final int[] getSourceSites() {
    	return super.defaultGetSourceSites();
    }

 	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(int numberOfInstances) {
    	return super.defaultGetOutputQueueCardinality(numberOfInstances);
    }

 	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(final Site node, final DAF daf) {
    	return super.defaultGetOutputQueueCardinality(node, daf);
    }

    /** {@inheritDoc} */    
    public final ArrayList<Attribute> getAttributes() {
    	return super.defaultGetAttributes();
    }

    /** {@inheritDoc} */    
	public final ArrayList<Expression> getExpressions() {
		return super.defaultGetExpressions();
	}
 }
