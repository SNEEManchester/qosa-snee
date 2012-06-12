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
import java.util.TreeMap;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Path;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/**
 * The Distributed-algebraic form of the query.
 * 
 * @author galpini
 */
public class DAF extends FAF implements Scorable {

	/**
	 * The fragmented-algebraic form of the query plan operator tree.
	 */
	private FAF faf = null;

	/**
	 * The routing tree which is couple to the FAF to produce the DAF.
	 */
	private RT rt = null;

    /**
     * Counter to assign unique id to different candidates.
     */
    protected static int candidateCount = 0;
	
    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger(DAF.class.getName());	
	
	/**
	 * Constructor.
	 * @param inFAF	The fragmented-algebraic form of the query plan operator 
	 * 				tree from which DAF is derived.
	 * @param inRT	The routing tree from from which the DAF is derived
	 * @param queryName The name of the query
	 */
	public DAF(final FAF inFAF, 
			final RT inRT, 
			final String queryName) {
		super(inFAF, generateName(queryName));
		this.faf = inFAF;
		this.rt = inRT.clone(); //This is necessary because we annotate the
								//sites with information about operators placed
	}
	
    /**
     * Resets the counter; use prior to compiling the next query.
     */
    public static void resetCandidateCounter() {
    	candidateCount = 0;
    }
	
	/**
	 * Generates a systematic name for this query plan strucuture, of the form
	 * {query-name}-{structure-type}-{counter}.
	 * @param queryName	The name of the query
	 * @return the generated name for the query plan structure
	 */
    private static String generateName(final String queryName) {
    	candidateCount++;
    	return queryName + "-DAF-" + candidateCount;
    }
	
    public static int getNextCandidateId() {
    	return (candidateCount+1);
    }
    
	 /**
     * Returns the routing tree used by the query plan.
     * @return the routing tree
     */
    public final FAF getFAF() {
    	return this.faf;
    }	
	
	 /**
     * Returns the routing tree used by the query plan.
     * @return the routing tree
     */
    public final RT getRoutingTree() {
    	return this.rt;
    }

    /**
     * Places the given fragment at the given sensor network node; 
     * note that the same fragment be be placed at several sites. 
     * @param frag The fragment to be placed
     * @param site The site at which the fragment is to be placed.
     */
    public final void placeFragment(final Fragment frag, final Site site) {
		//update the sensor network node in the routingTree		
		site.addFragment(frag);
	
		//update the fragment
		frag.addSite(site);
    }    

    /**
     * Places the given fragment at the given sensor network nodes.
     * @param frag The fragment to be placed
     * @param siteArray The site at which the fragment is to be placed
     */
    public final void placeFragment(
    		final Fragment frag, final ArrayList<Site> siteArray) {
		for (int i = 0; i < siteArray.size(); i++) {
		    this.placeFragment(frag, siteArray.get(i));
		}
    }

    /**
     * Links the copy of the source fragment allocated to source site to a 
     * copy of destination fragment executing on the destination site. 
     * @param sourceFrag the source fragment
     * @param sourceSite the source site
     * @param destFrag the destination fragment
     * @param destSite the destination site
     * @param path the path to be used to link the source fragment instance 
     * to the destination fragment instance.
     */
    public final void linkFragments(final Fragment sourceFrag, 
    		final Site sourceSite, final Fragment destFrag, 
    		final Site destSite, final Path path) {

		logger.finest("Linking F" + sourceFrag.getID() + " on site "
			+ sourceSite.getID() + " to F" + destFrag.getID() + " on site "
			+ destSite.getID());
		
		//check source and dest nodes are are first and last nodes in path, 
		//otherwise insert
		if (path.getFirstSite() != sourceSite) {
		    path.prepend(sourceSite);
		}
		if (path.getLastSite() != destSite) {
		    path.append(destSite);
		}
	
		//Update the routing table in the exchange operator
		final RoutingTableEntry entry = new RoutingTableEntry(destSite,
			destFrag, path);
		sourceFrag.getParentExchangeOperator().addRoutingEntry(sourceSite,
			entry);
	
		//Place the exchange components (producer, relay and consumer) on the 
		//respective sensor network nodes.  Check that producer and consumer 
		//are not on the same node; if they are, don't bother
		boolean isRemote = false;
		if (sourceSite != destSite) {
		    isRemote = true;
		}
	
		ExchangePart prev = null;
		Iterator<Site> pathIter = path.iterator();
		while (pathIter.hasNext()) {
			Site currentSite = pathIter.next();
			//insert the producer at the start of the path
			if (currentSite == sourceSite) {
				prev = new ExchangePart(sourceFrag, sourceSite, destFrag,
						destSite, currentSite,
						ExchangePartType.PRODUCER, isRemote, prev);
			}
			if (currentSite == destSite) {
				// insert the consumer at the end of the path
		    	prev = new ExchangePart(sourceFrag, sourceSite, destFrag,
				destSite, currentSite,
				ExchangePartType.CONSUMER, isRemote, prev);
		    }
			if (currentSite != sourceSite && currentSite != destSite) {
		    	//insert relays in intermediate nodes (only if applicable)
		    	prev = new ExchangePart(sourceFrag, sourceSite, destFrag,
				destSite, currentSite,
				ExchangePartType.RELAY, isRemote, prev);
		    }
		}
    }
    
    
    /**
     * Places the given fragment at the given sensor network node.
     * @param frag The fragment to be placed
     * @param siteID The site at which the fragment is to be placed
     */
    public final void placeFragment(final Fragment frag, 
    		final String siteID) {
    	this.placeFragment(frag, (Site) rt.getNode(siteID));
    }    
    
    /**
     * Given a parent operator, the site of the parent operator, and an index 
     * of an operator child, returns the sites which that child is placed. 
     * @param op The parent operator. If op is an exchange it is assumed to be the producer.
     * @param site The site of the parent operator. 
     * 	Note for exchange operators this will be a consumer site
     * @param index The index of the child 
     * @return The sites on which the child of this operator is found.
     */
    public final Iterator<Site> getInputOperatorInstanceSites(
	    final Operator op, final Site site, final int index) {

		ArrayList<Site> results = new ArrayList<Site>();
		final Operator childOp = op.getInput(index);
		if (childOp instanceof ExchangeOperator) {
		    results = ((ExchangeOperator) childOp).getSourceSites(site);
		} else {
		    results.add(site);
		}
	
		return results.iterator();
    }    

    /**
     * Iterator to traverse the routing tree.
     * The structure of the routing tree may not be modified during iteration
     * @param traversalOrder the order to traverse the routing tree
     * @return an iterator for the routing tree
     */    
    public final Iterator<Site> siteIterator(
    		final TraversalOrder traversalOrder) {
    	return rt.siteIterator(traversalOrder);
    }    
    
    protected final void exportAsDOTFile(final String fname, 
			TreeMap<String, StringBuffer> opLabelBuff,
			TreeMap<String, StringBuffer> edgeLabelBuff,
			StringBuffer fragmentsBuff) {
    
	    final Iterator j = this.nodes.keySet().iterator();
	    while (j.hasNext()) {
			final Operator op = (Operator) this.nodes
				.get((String) j.next());
		
			StringBuffer strBuff = new StringBuffer();
			if (opLabelBuff.containsKey(op.getID())) {
				strBuff = opLabelBuff.get(op.getID());
			}
			
			if (op instanceof ExchangeOperator) {
	
			    if (Settings.DISPLAY_EXCHANGE_OPERATOR_ROUTING) {
					strBuff.append(((ExchangeOperator) op)
						.getDOTRoutingString());
				    }
			}
			opLabelBuff.put(op.getID(), strBuff);
	    }
    	
	    super.exportAsDOTFile(fname, opLabelBuff, edgeLabelBuff, 
    			fragmentsBuff);
    }
    
	@Override
	public boolean isEquivalentTo(Object other) {
		boolean equiv = super.isEquivalentTo(other);
		if (!equiv) {
			return false;
		}
		if (!(other instanceof DAF)) {
			return false;
		}
		DAF otherDAF = (DAF) other;
		if (!this.rt.isEquivalentTo(otherDAF.rt)) {
			return false;
		}
		if (this.faf != otherDAF.faf) {
			return false;
		}
		
		return true;
	}
	
	public String getProvenanceString() {
		return this.faf.getProvenanceString() + "->" + this.rt.getName() + "->" + this.name;
	}

	/**
	 * Removes a given exchange operator, merging the source fragment of the exchange operator into
	 * the destination fragment.
	 * @param exchOp	The exchange operator to be removed.
	 * @throws OptimizationException
	 */
	public void removeExchangeOperator(ExchangeOperator exchOp) throws OptimizationException {
		//Merge the source fragment operators into the destination fragment			
		Fragment sourceFrag = exchOp.getSourceFragment();
		Fragment destFrag = exchOp.getDestFragment();
		destFrag.mergeChildFragment(sourceFrag);
		
		//Remove the exchange operator and fragment
		this.removeNode(exchOp);
		this.removeFragment(sourceFrag);
		
		Iterator<Site> siteIter = this.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
		
			//Remove the exchange components from any routing tree sites
			site.removeExchangeComponents(exchOp);
			
			//Remove the source fragment from any routing tree sites
			site.removeFragment(sourceFrag);
			
			//Change the destination fragment on any exchange component which has the 
			//old destination fragment
			Iterator<ExchangePart> exchCompIter = site.getExchangeComponents().iterator();
			while (exchCompIter.hasNext()) {
				ExchangePart exchComp = exchCompIter.next();
				if (exchComp.getDestFrag()==sourceFrag) {
					exchComp.setDestFrag(destFrag);
				}
			}
		}

	}

	/**
	 * Given a DAF, removes any exchange operators from it which are not required.
	 * An exchange operator is deemed not be required if both the source fragment
	 * and destination fragment of the exchange operator are are placed on the same
	 * set of sites.
	 * @throws OptimizationException 
	 */
	public void removeRedundantExchanges() throws OptimizationException {
		HashSet<ExchangeOperator> exchangesToBeRemoved = new HashSet<ExchangeOperator>(); 
		
		Iterator<Operator> opIter = this.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			if (op instanceof ExchangeOperator) {
				ExchangeOperator exchOp = (ExchangeOperator)op;
				Fragment sourceFrag = exchOp.getSourceFragment();
				ArrayList<Site> sourceSites = sourceFrag.getSites();
				Fragment destFrag = exchOp.getDestFragment();
				ArrayList<Site> destSites = destFrag.getSites();
				
				if (sourceSites.equals(destSites)) {
					logger.finest("Fragment " + sourceFrag.getID() + " and Fragment " + destFrag.getID()+"\n");
					exchangesToBeRemoved.add(exchOp);
				}
			}
		}
		
		Iterator<ExchangeOperator> exchOpIter = exchangesToBeRemoved.iterator();
		while (exchOpIter.hasNext()) {
			ExchangeOperator exchOp = exchOpIter.next();
			this.removeExchangeOperator(exchOp);
		}
	}

	/**
	 * A recursive fragment is deemed to redundant if is has not been assigned to execute on 
	 * any site, or has only been assigned to execute on one site.
	 * Note that this assumes that a recursive operator is the only one in the recursive fragment.
	 * This is in accordance with what partitioning does
	 * @throws OptimizationException 
	 */
	public void removeRedundantRecursiveFragments() throws OptimizationException {
	
		HashSet<Fragment> fragmentsToRemove = new HashSet<Fragment>(); 
		Iterator<Fragment> fragIter = this.fragmentIterator(TraversalOrder.POST_ORDER);
		while (fragIter.hasNext()) {
			Fragment frag = fragIter.next();
			if (frag.isRecursive() && frag.getNumSites()<=1) {
				fragmentsToRemove.add(frag);
			}
		}
		
		fragIter = fragmentsToRemove.iterator();
		while (fragIter.hasNext()) {
			Fragment frag = fragIter.next();
			this.deleteFrag(frag);
		}
	}

	/**
	 * Deletes a fragment from the DAF.
	 * @param frag The fragment to be deleted.
	 * @throws OptimizationException 
	 */
	private void deleteFrag(Fragment frag) throws OptimizationException {
		HashSet<Operator> opsToDelete = frag.getOperators();
		
		ExchangeOperator exchOp = frag.getParentExchangeOperator();
		this.removeExchangeOperator((ExchangeOperator)exchOp);
		Fragment parentFragment = frag.getParentFragment();
		
		Iterator<Operator> opIter = opsToDelete.iterator();
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			this.removeNode(op);
			parentFragment.operators.remove(op);
		}
	}

	public void removeNode(Operator op) throws OptimizationException {
	
		Fragment f = op.getContainingFragment();
		f.removeOperator(op);
		super.removeNode(op);
	}
}
