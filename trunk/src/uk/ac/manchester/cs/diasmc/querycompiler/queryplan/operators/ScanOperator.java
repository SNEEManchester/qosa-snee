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

import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Relation;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

//STUB.
public class ScanOperator extends OperatorImplementation implements
		Operator {

	public ScanOperator(Relation relation) {
	   	throw new AssertionError("Stub Method called");
	}

	public OperatorImplementation shallowClone() {
	   	throw new AssertionError("Stub Method called");
	}

	@Override
	public String toString() {
	   	throw new AssertionError("Stub Method called");
	}

	public int getCardinality(CardinalityType card) {
	   	throw new AssertionError("Stub Method called");
	}

	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		return getInputCardinality(card, node, daf, 0);
	}
	
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		return null;
    }

	public int getPhysicalMaxCardinality(Site node, DAF daf) {
	   	throw new AssertionError("Stub Method called");
	}

	public double getTimeCost(CardinalityType card, Site node, DAF daf) {
	   	throw new AssertionError("Stub Method called");
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

	public boolean isAttributeSensitive() {
	   	throw new AssertionError("Stub Method called");
	}

	public boolean isLocationSensitive() {
	   	throw new AssertionError("Stub Method called");
	}

	public boolean isRecursive() {
	   	throw new AssertionError("Stub Method called");
	}

    /** {@inheritDoc}
     * @return false;
     */
    public final boolean acceptsPredicates() {
        return false;
    }

	public boolean isRemoveable() {
	   	throw new AssertionError("Stub Method called");
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
