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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RoutingTableEntry;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * 
 * This class is used to represent the exchange operator.
 * @author Ixent Galpin
 */
public class ExchangeOperator extends OperatorImplementation 
    implements Operator {

	/**
	 * The destination fragment(s) of the exchange operator.
	 * An exchange operator may have more than one destination fragment in a
	 * multiple query execution context, i.e., the data flows are forked.
	 * Note that if the source fragment is recursive, it may send its output to
	 * other instances of the same fragment.
	 */
	private Fragment destFragment = null;
	
	/**
	 * The source fragment of the exchange operator.  Note that although a
	 * fragment may have more than one child fragment (e.g., in the case of a 
	 * join), this is implemented using a different exchange operator. 
	 */
	private Fragment sourceFragment;

	/**
	 * The paths used to link instances of the source fragment to instances 
	 * of the parent fragment.  Note that if the source fragment is recursive,
	 * it may be linked to a different instance of the same fragment.	 
	 * TODO: no checks are currently made to avoid circular paths!
	 */
	private HashMap<Site, RoutingTableEntry> sourceToDestPaths 
		= new HashMap<Site, RoutingTableEntry>();
	
        
	/**
	 * Constuctor of a newe exchange.
	 * Child and parents to be added later.
	 * @param newOperatorDataType Operator data type of incoming data 
	 */
	public ExchangeOperator(final OperatorDataType newOperatorDataType) {
        super();
        this.setOperatorName("EXCHANGE");
        this.setOperatorDataType(newOperatorDataType);
        this.setNesCTemplateName("producer");
    }
   
    /**
     * Constructor that creates a new operator based on a model of an existing 
     * operator.
     * 
     * Used by both the clone method and the constuctor of the physical methods.
     * @param model Operator to get values from.
     */
    protected ExchangeOperator(final ExchangeOperator model) {
    	super(model);
        this.setOperatorName("EXCHANGE");
        this.setOperatorDataType(model.getOperatorDataType());
        this.setNesCTemplateName("producer");
    }

    /**
     * Returns the destination fragments of this exchange operator.
     * @return the destination fragments
     */
    public final Fragment getDestFragment() {
    	return this.destFragment;
    }
    
    /**
     * Returns the source fragments of this exchange operator.
     * @return the source fragments
     */
    public final Fragment getSourceFragment() {
    	return this.sourceFragment;
    }
     
    /**
     * Add a destination fragment for this exchange operator.
     * @param frag the destination fragment
     */
    public final void setDestinationFragment(final Fragment frag) {
    	assert (frag != null);
    	this.destFragment = frag;
    }

    /**
     * Sets the source fragment for this exchange operator.
     * @param frag the source fragment
     */
    public final void setSourceFragment(final Fragment frag) {
    	this.sourceFragment = frag;
    }
    
    /**
     * Output a String representation of the operator.
     * @return a String representation of the operator.
     */
    @Override
	public final String toString() {
    	return this.getText() + "[ " + this.getInput(0).toString() + " ]";
    }

	public final void addRoutingEntry(final Site sourceSite, final RoutingTableEntry entry) { 
		this.sourceToDestPaths.put(sourceSite, entry);
	}

	public ArrayList<Site> getSourceSites(final Site consumerSite) {
		
		final ArrayList<Site> results = new ArrayList<Site>();
		
		Iterator<Site> allSourceNodes 
			= this.sourceToDestPaths.keySet().iterator();
		while (allSourceNodes.hasNext()) {
			final Site s = allSourceNodes.next();
			if ((this.sourceToDestPaths.get(s).getSite() == consumerSite)) {
				results.add(s);
			}
		}
		if (destFragment == null) {
			return results;
		}
		
		if (destFragment.isRecursive()) {
			final HashMap<Site, RoutingTableEntry> parentExchangeRoutingTable 
				= ((ExchangeOperator)destFragment.getParentExchangeOperator()).sourceToDestPaths; 
			
			allSourceNodes = parentExchangeRoutingTable.keySet().iterator();
			while (allSourceNodes.hasNext()) {
				final Site s = allSourceNodes.next();
				if ((parentExchangeRoutingTable.get(s).getSite() 
						== consumerSite) && (parentExchangeRoutingTable.get(s)
						.getFragment() == destFragment)) {
					results.add(s);
				}
			}
		}
		
		return results;
	}
	
	public String getDOTRoutingString() {
    	final StringBuffer s = new StringBuffer();
    	
    	final Iterator<Site> routingFunctionIter = this.sourceToDestPaths.keySet().iterator();
    	while (routingFunctionIter.hasNext()) {
    		final Site sourceNode = routingFunctionIter.next();
    		
    		final RoutingTableEntry entry = this.sourceToDestPaths.get(sourceNode);
    		s.append("n" + sourceNode.getID() + " -> ");
    		s.append("n" + entry.getSiteID());
    		s.append("f" + entry.getFragID());
    		
    		s.append(" [");
    		boolean first = true;
    		final Iterator<Site> pathIter = entry.getPath().iterator();
    		while (pathIter.hasNext()) {
    			if (!first) {
					s.append(",");
				}
    			final Site n = pathIter.next();
    			s.append(n.getID());
    			first = false;
    		}
    		s.append("]");

    		s.append("\\n");

    	}
    	
    	return s.toString();
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
	  * Should never be called as push down is done 
	  * before the placement of exchanges.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 throw new AssertionError("Unexpected call to pushLocalNameDown()"); 
	 }

	/**
     * This method is will be called from the deliver down to the leaf operator.
     * It will help operators identify which attributes they need to included and in which order. 
     * 
     * @param required Oreder list of required attributes
     * 
     * @return param as list is unchanged by this operator.
     */
    public LinkedHashMap<String, AttributeType> 
    	    checkTupleAttributes(final LinkedHashMap<String, AttributeType> 
    	    required) {
    	return required;
    }

    /**
     * Just returns the inputName as attributes are never renamed or changed.
     * 
     * @return inputName
     */
    public String getOutputAttributename(final String inputName) {
    	return inputName;
    }

	/**
	 * Some operator can identify attributes that they receive but discard.
	 * For example the project and aggregation operators.
	 * 
	 * @return Null As this operatos does not discard any attributes 
	 */
	public Set<String> getDiscardedAttributes() {
		return null;
	}
	
	/**
	 * Some operator specifically require attributes even if the final result does not.
	 * These include joins and selects that have predicates.
	 * Aggregate will also require the raw values.
	 * 
	 * @return Null As this operatos does not require any specific attributes
	 */
	public Set<String> getRequiredAttributes() {
		return null;
	}
	
	/**
	 * Calculated the producer cardinality based on the requested type. 
	 * 
	 * @param card Type of cardinality to be considered.
	 * 
	 * @return The Cardinality calculated as requested.
	 */
	public final int getCardinality(final CardinalityType card) {
		return (this.getInput(0)).getCardinality(card);
	}

	/** {@inheritDoc} 
	 * Returns the producer cardinality.
	 */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		return getInputCardinality(card, node, daf, 0);
	}
	
    /**
     * Obtains the input cardinality of the comsumers from all incoming sites.
     * @param card Type of cardinality to be used.
     * @param node Site for which incoming should be calculated
     * @param daf DAF this operator is part of
     * @param index Index of incoming operator. (0 unless a join)
     * @return The total physicalMaxCardinality for all sites 
     * that feed into node.
     */
   public final int getConsumerCardinality(final CardinalityType card, 
    		final Site node, final DAF daf, final int index) {
   		int total = 0;
   		final Operator inputOp = this.getInput(index);
   		Iterator<Site> inputs;
   		
   		inputs = this.getSourceSites(node).iterator();
   		while (inputs.hasNext()) {
   			total = total
		    	+ inputOp.getCardinality(card, inputs.next(), daf);
   		}
   		return total;
    }

   /** {@inheritDoc} 
	 * Returns the producer cardinality.
     */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		return getInput(0).getCardinality(card, node, daf, round);
    }

	/**
	 * Used to determine if the operator is Attribute sensitive.
	 * 
	 * @return false.
	 */
	public boolean isAttributeSensitive() {
		return false;
	}

	public boolean isLocationSensitive() {
		return false;
	}
	
	public boolean isRecursive() {
		return false;
	}
	
    /** {@inheritDoc}
     * @return false;
     */
    public final boolean acceptsPredicates() {
        return false;
    }

	@Override
	public ExchangeOperator shallowClone() {
		final ExchangeOperator clonedOp = new ExchangeOperator(this);
		return clonedOp;
	}
	
    /**
     * Stub method as cost is done on exchange components.
     * @param card stub
     * @param node stub.
     * @param daf stub.
     * @return error.
     */
     public final double getTimeCost(final CardinalityType card, 
    		 final Site node, final DAF daf) {
     	throw new AssertionError("Unexpected method call");
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
      * This operator changes the data's location or timing so can not be removed. 
      * 
      * @return False. 
      */
     public boolean isRemoveable() {
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
