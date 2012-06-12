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

import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
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
 * Insert Stream Operator.
 * Returns all data in this window but not in the previous.
 * @author Christian
 *
 */
public class IStreamOperator extends OperatorImplementation 
		implements Operator {

	/**
     * Constructs a new IStream operator.
     * 
     * @param token A IStream Operator token
     */
    public IStreamOperator(final AST token) {
        super();
        Operator inputOperator 
           = OperatorFactory.convertAST(token.getFirstChild());

        this.setOperatorName("ISTREAM");
        this.setNesCTemplateName("istream never set");
        this.setOperatorDataType(OperatorDataType.STREAM);
        this.setParamStr("");
        
        setChildren(new Operator[] {inputOperator});
    }  

    /**
     * Old Constuctor.
     * @param inputOperator previous Operator.
     * @deprecated
     */
    public IStreamOperator(final Operator inputOperator) {
        super();

        this.setOperatorName("ISTREAM");
        this.setNesCTemplateName("istream never set");
        this.setOperatorDataType(OperatorDataType.STREAM);
        this.setParamStr("");
        
        setChildren(new Operator[] {inputOperator});
    }  

    //used by clone method
    /**
     * Constructor that creates a new operator 
     * based on a model of an existing operator.
     * 
     * Used by both the clone method and the constuctor of the physical methods.
     * @param model Operator to be cloned.
     */
   protected IStreamOperator(final IStreamOperator model) {
    	super(model);
    }  

    
    /**
     * Returns this operator's input operator.
     * @return Child operator.
     */
    public final Operator getInputOperator() {
        return super.getInput(0);   
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
	  * Push passed on to child.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 getInput(0).pushLocalNameDown(newLocalName); 
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
		return false;
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

	/**
	 * Produces a copy of this Operator.
	 * @return A copy of this operator.
	 */	
	public final IStreamOperator shallowClone() {
		final IStreamOperator clonedOp = new IStreamOperator(this);
		return clonedOp;
	}
    /** STUB.
     * {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
    	throw new AssertionError("Stub method called");
    }
    
    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances) {
	   	throw new AssertionError("Stub Method called");
    }

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
    	throw new AssertionError("Stub Method called");
    }

    /**
     * Some operators do not change the data in any way those could be removed.
     * This operator does not change the data so can be removed. 
     * 
     * @return true;
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
