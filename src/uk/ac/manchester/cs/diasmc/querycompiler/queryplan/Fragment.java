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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;


import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Topology;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.mqe.MultiFragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;
 
public class Fragment implements Cloneable {

    /**
     * Logger for this class.
     */
    static Logger logger = Logger.getLogger(Fragment.class.getName());

    public static int fragmentCount = 0;

    /**
     * Identifier of this fragment
     */
    protected int fragID;

    /**
     * The output exchange operator of this fragment (currently, there can only be one).
     */
    protected ExchangeOperator parentExchange;

    /**
     * The input exchange operators of this fragment.
     */
    protected  ArrayList<ExchangeOperator> childExchanges = new ArrayList<ExchangeOperator>();

    /**
     * The root operator of the fragment
     */
    protected Operator rootOperator;

    /**
     * The operators which belong to this fragment
     */
    protected  HashSet<Operator> operators = new HashSet<Operator>();

    /**
     * The sensor network nodes to which this fragment has been allocated to run.
     */
    protected  ArrayList<Site> sites = new ArrayList<Site>();

    /**
     * The list of sensor nodes where the operators in the fragment are required to run.
     * e.g., an Acquire operator will require certain data sources
     */
    public ArrayList<String> desiredSites = new ArrayList<String>();

    //used by clone method
    //doesn't automatically generate the fragmentID
    private Fragment(final int fragID) {
	this.fragID = fragID;
	logger.finest("Fragment " + fragID + " created");
    }

    public Fragment() {
	this(fragmentCount);
	fragmentCount++;
    }

    /**
     * @return the id of the fragment
     */
    public  String getID() {
	return new Integer(this.fragID).toString();
    }

    /**
     * Returns true if this is a leaf fragment
     */
    public final boolean isLeaf() {
	return (this.getChildFragments().size() == 0);
    }

    /**
     * Return the root operator of the fragment
     * (NB: The parent exchange is not considered to be within in the fragment;
     * this method returns the child of the parent exchange)
     * @return
     */
    public final Operator getRootOperator() {
	return this.rootOperator;
    }

    public final void setRootOperator(final Operator op) {
	this.rootOperator = op;
    }

    /**
     * @return	the child exchange operators of the fragment
     */
    public final ArrayList<ExchangeOperator> getChildExchangeOperators() {
	return this.childExchanges;
    }

    /**
     * Get the parent fragments of this fragment
     * @return
     */
    public final Fragment getParentFragment() {
    	return this.parentExchange.getDestFragment();
    }

    /**
     * Get the child fragments of this fragment
     * @return
     */
    public final ArrayList<Fragment> getChildFragments() {
		final ArrayList<Fragment> childFrags = new ArrayList<Fragment>();
	
		final Iterator<ExchangeOperator> childExchangeIter = 
				this.childExchanges.iterator();
		while (childExchangeIter.hasNext()) {
		    final ExchangeOperator exch = childExchangeIter.next();
		    assert(exch.getSourceFragment() != null);
		    childFrags.add(exch.getSourceFragment());
		}
		return childFrags;
    }

    /**
     * Get the sites on which all the child fragments have been placed
     * @return
     */
    public final HashSet<Site> getChildFragSites() {
		final HashSet<Site> childFragSites = new HashSet<Site>();
		final Iterator<Fragment> fragIter = this.getChildFragments().iterator();
		while (fragIter.hasNext()) {
		    final Fragment f = fragIter.next();
		    childFragSites.addAll(f.getSites());
		}
		return childFragSites;
    }

    /**
     * @return	the parent exchange operators of the fragment
     */
    public final ExchangeOperator getParentExchangeOperator() {
	return this.parentExchange;
    }

    /**
     * Returns the ith child exchange operator
     * @param i		the positition of the child exchange operator
     * @return		the exchange operator
     */
    public final ExchangeOperator getChildExchangeOperator(final int i) {
	return this.childExchanges.get(i);
    }

    /**
     * Returns the number of child exchange operators.
     * @return
     */
    public final int getNumChildExchangeOperators() {
	return this.childExchanges.size();
    }

    /**
     * @return the operators in the fragment
     */
    public final HashSet<Operator> getOperators() {
	return this.operators;
    }

    public final int[] getSourceNodes() {
	return this.getRootOperator().getSourceSites();
    }

    public final boolean containsOperatorType(final Class c) {
	final Iterator<Operator> i = this.operators.iterator();
	while (i.hasNext()) {
	    final Operator op = i.next();
	    if (c.isInstance(op)) {
		return true;
	    }
	}
	return false;
    }

    public final boolean isLocationSensitive() {
	boolean found = false;
	final Iterator<Operator> ops = this
		.operatorIterator(TraversalOrder.PRE_ORDER);
	while (ops.hasNext()) {
	    if (ops.next().isLocationSensitive()) {
		found = true;
		break;
	    }
	}
	return found;
    }

    public final boolean isRecursive() {
		boolean found = false;
		final Iterator<Operator> ops = this.operators.iterator();
		while (ops.hasNext()) {
		    if (ops.next().isRecursive()) {
			found = true;
			break;
		    }
		}
		return found;
    }

    public final boolean isAttributeSensitive() {
	boolean found = false;
	final Iterator<Operator> ops = this
		.operatorIterator(TraversalOrder.PRE_ORDER);
	while (ops.hasNext()) {
	    if (ops.next().isAttributeSensitive()) {
		found = true;
		break;
	    }
	}
	return found;
    }

    /**
     * 
     * @return True if the fragment ends in a deliver and therefor does not output tuples to a tray.
     */
    public final boolean isDeliverFragment() {
    	if (this.getRootOperator() instanceof DeliverOperator) {
    		return true;
    	}
    	return false;
    }

    /**
     * @return the sensor network nodes this fragment has been allocated to
     */
    public final ArrayList<Site> getSites() {
    	return this.sites;
    }

    /**
     * Helper method to implement the operator iterator.
     * @param op The current operator being visited
     * @param opList The list of operators being generated (the result).
     * @param traversalOrder The order in which to traverse the operators.
     */
    private void doOperatorIterator(final Operator op,
	    final ArrayList<Operator> opList, 
	    final TraversalOrder traversalOrder) {

		if (traversalOrder == TraversalOrder.PRE_ORDER) {
		    opList.add(op);
		}
	
		for (int n = 0; n < op.getInDegree(); n++) {
		    if (!(op.getInput(n) instanceof ExchangeOperator)) {
		    	this.doOperatorIterator(op.getInput(n), opList, traversalOrder);
		    }
		}
	
		if (traversalOrder == TraversalOrder.POST_ORDER) {
		    opList.add(op);
		}
    }

    /**
     * Iterator to traverse the operator tree.
     * The structure of the routing tree may not be modified during iteration
     * @param traversalOrder (constants defined in QueryPlan class)
     * @return an iterator of the operator tree in the fragment
     */
    public final Iterator<Operator> operatorIterator(
    		final TraversalOrder traversalOrder) {
		final ArrayList<Operator> opList =
				new ArrayList<Operator>();
		logger.finest("root =" + this.getRootOperator());
		this.doOperatorIterator(this.getRootOperator(), opList, traversalOrder);
		logger.finest("done");
		return opList.iterator();
    }

    /** 
     * Calculates the physical size of the state of the operators in this 
     * fragment.  Does not include the size of the exchange components 
     * including the consumers and producers.  Does not include the size 
     * of the code itself.
     * 
     * @return Sum of the cost of each of the operators
     */
    public final long getDataMemoryCost(final Site node, final DAF daf) {
	long total = 0;
	final Iterator<Operator> ops = this
		.operatorIterator(TraversalOrder.PRE_ORDER);
	while (ops.hasNext()) {
	    final Operator op = ops.next();
	    logger.finest("op=" + op);
	    total += op.getDataMemoryCost(node, daf);
	}
	logger.finest("done");
	return total;
    }

    public final AlphaBetaExpression getMemoryExpression(final Site node,
	    final DAF daf) {
    	return new AlphaBetaExpression(this.getDataMemoryCost(node, daf), 0);
    }

    /**
     * Calculates the time cost for a single evaluation of all the operators 
     * in this fragment.  The time cost is based on the maximum cardinality 
     * not the average cardinality.  Does not include the time of the exchange 
     * components including the consumers and producers
     * 
     * Based on the time estimates provided in the OperatorsMetaData file.
     * Includes the cost of copying the tuples to the tray.
     * 
     * @param node The site the operator has been placed on
     * @param daf The distributed-algebraic form of the corresponding 
     * operator tree.
     * @return Sum of the cost of each of the operators in ms
     */
    public final double getTimeCost(final Site node, final DAF daf) {
		long total = 0;
		final Iterator<Operator> ops = this
			.operatorIterator(TraversalOrder.PRE_ORDER);
		while (ops.hasNext()) {
		    final Operator op = ops.next();
		    final double temp = op.getTimeCost(
		    		CardinalityType.MAX, node, daf);
		    total += temp;
		}
		if (!this.isDeliverFragment()) {
		    final int cardinality = this.getRootOperator()
			    .getCardinality(CardinalityType.MAX, node, daf);
		    total += cardinality * CostParameters.getCopyTuple();
		}
		return total;
    }

    /**
     * Calculates the time cost for a single evaluation of all the operators 
     * in this fragment.  The time cost is based on the maximum cardinality 
     * not the average cardinality.  Does not include the time of the exchange 
     * components including the consumers and producers
     * 
     * Based on the time estimates provided in the OperatorsMetaData file.
     * Includes the cost of copying the tuples to the tray.
     * 
 	 * @param card Type of cardinality to be considered.
     * @param node The site the operator has been placed on
     * @param daf The distributed-algebraic form of the corresponding 
     * operator tree.
	 * @param round Defines if rounding reserves should be included or not
     * @return Sum of the cost of each of the operators' expression.
     */
    public final AlphaBetaExpression getTimeExpression(
    		final CardinalityType card, final Site node, final DAF daf, 
    		final boolean round) {

    	AlphaBetaExpression result = new AlphaBetaExpression();
   		
   		double variableCost = this.getTimeCost(node, daf);
   		result.addBetaTerm(variableCost);
   		
   		double fixedCost = 0;
   		if (this.isDeliverFragment()) {
			fixedCost += CommunicationTask.getTimeCostOverhead();
		}
   		result.add(fixedCost);
   		
    	return result;
    }

    public final void setParentExchange(final ExchangeOperator p) {
	this.parentExchange = p;
    }

    public final void addChildExchange(final ExchangeOperator c) {
	if (!this.childExchanges.contains(c)) {
	    this.childExchanges.add(c);
	}
    }

    /**
     * Adds an operator to the fragment
     * @param op
     */
    public final void addOperator(final Operator op) {
	this.operators.add(op);
    }

    /**
     * Adds a sensor network node to execute fragment
     * They are added in order (for good looking display purposes)
     * @param n
     */
    public final void addSite(final Site newSite) {
	boolean addedFlag = false;

	//insert at current sites position in the array
	for (int i = 0; i < this.sites.size(); i++) {
	    if (Integer.parseInt(this.sites.get(i).getID()) == Integer
			    .parseInt(newSite.getID())) {
			addedFlag = true;
			break;
	    }
		
	    if (Integer.parseInt(this.sites.get(i).getID()) > Integer
		    .parseInt(newSite.getID())) {
		this.sites.add(i, newSite);
		addedFlag = true;
		break;
	    }
	}

	//add to the end
	if (!addedFlag) {
	    this.sites.add(newSite);
	}
    }

    /**
     * Resets the fragment counter (for use when a new query plan is instantiated)
     *
     */
    public static void resetFragmentCounter() {
	fragmentCount = 0;
    }

    public final void addDesiredSite(final Site n) {
	this.desiredSites.add(n.getID());
    }

    public final void addDesiredSites(final int[] nindices, final Topology routingTree) {
	for (int element : nindices) {
	    this.addDesiredSite((Site) routingTree.getNode(element));
	}
    }

    public final ArrayList<String> getDesiredSites() {
	return this.desiredSites;
    }

    public final String getDesiredSitesString() {
	final Iterator<String> siteIter = this.desiredSites.iterator();
	boolean first = true;
	final StringBuffer s = new StringBuffer();

	while (siteIter.hasNext()) {
	    final String siteID = siteIter.next();
	    s.append(siteID);
	    if (first) {
		first = false;
	    } else {
		s.append(",");
	    }
	}

	return s.toString();
    }

    /**
     * 
     * @return
     */
    public final int getNumChildFragments() {
	return this.getChildFragments().size();
    }


    public final Fragment clone(FAF clonedFAF) {

    	final Fragment clone = new Fragment(this.fragID);

    	clone.rootOperator = (Operator) clonedFAF.getNode(this
    		.getRootOperator().getID());

    	if (this.getParentExchangeOperator() != null) {
    	    clone.parentExchange = (ExchangeOperator) clonedFAF
    		    .getNode(this.getParentExchangeOperator().getID());
    	}

    	for (int i = 0; i < this.getNumChildExchangeOperators(); i++) {
    		ExchangeOperator clonedExchOp = 
    			(ExchangeOperator) clonedFAF.getNode(
    			this.getChildExchangeOperator(i).getID());
    	    clone.childExchanges.add(clonedExchOp);
    	}

    	final Iterator<Operator> opIter = this.operators.iterator();
    	while (opIter.hasNext()) {
    	    final Operator clonedOp = (Operator) clonedFAF
    		    .getNode(opIter.next().getID());
    	    clone.operators.add(clonedOp);
    	    clonedOp.setContainingFragment(clone);
    	}

    	Iterator<String> siteIDIter = this.desiredSites.iterator();
    	while (siteIDIter.hasNext()) {
    	    final String siteID = siteIDIter.next();
    	    clone.desiredSites.add(siteID);
    	}
    	
    	return clone;
    }

    public final Fragment clone(FAF clonedFAF, RT clonedRT) {
    	Fragment clone = clone(clonedFAF);
    	
    	Iterator<Site> siteIter = this.sites.iterator();
    	while (siteIter.hasNext()) {
    	    final Site site = siteIter.next();
    	    final Site clonedSite = (Site) clonedRT.getNode(site.getID());
    	    clone.sites.add(clonedSite);
    	}
    	
    	return clone;
    }
   
    
    /**
     * Merges a child fragment into the current fragment.
     * This will only work if the given fragment is indeed a child fragment
     * of the current fragment, and both fragments have been assigned to the
     * same set of sites.
     * 
     * @param childFrag
     */
    public final void mergeChildFragment(Fragment childFrag) {
    	
    	//Must be a child fragment
    	assert(this==childFrag.getParentFragment());
		//Sites should be the same; error if not
    	assert(this.getSites().equals(childFrag.getSites()) || childFrag.getNumSites()==0);
    	
		//The fragID, parentExchange and rootOperator of the current fragment are preserved.
    	
		//childExchanges of this fragment need to be replaced with those of child fragment
		this.childExchanges = childFrag.childExchanges;
    	
		//operators of source fragment need to be added into destination fragment
		this.operators.addAll(childFrag.operators);

		//Set all child fragment operators' containing fragment field to this fragment
		Iterator<Operator> opIter = childFrag.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			op.setContainingFragment(this);
		}
		
		//Set all destination fragments of the child exchanges to this fragment
		Iterator<ExchangeOperator> exchOpIter = this.childExchanges.iterator();
		while (exchOpIter.hasNext()) {
			ExchangeOperator exchOp = exchOpIter.next();
			exchOp.setDestinationFragment(this);
		}
		
    }

    /**
     * Returns the number of sites that this fragment has been assigned to.
     * @return The number of sites executing this fragment
     */
    public int getNumSites() {
		return this.sites.size();
	}

	public void removeOperator(Operator op) {
		this.operators.remove(op);
	}

	public int getNumOps() {
		return this.operators.size();
	}
    
}
