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
package uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.vanilla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Counter;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Path;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;


public class VWhereScheduling {

	/**
	 * Logger for this class.
	 */
    private static Logger logger = 
    	Logger.getLogger(VWhereScheduling.class.getName());
	
    
    public static void doWhereScheduling(final SchemaMetadata schemaMetadata,
			final Integer sink,
			final String queryName, 
			final FAF faf, 
			final RT rt,
			ScoredCandidateList<DAF> dafList) throws OptimizationException {
		
		DAF daf = new DAF(faf, rt, queryName);	
		Fragment prevFrag = null;
 		
		System.err.println("RT id = "+rt.getName());
		
		final Iterator<Fragment> fragIter = daf
			.fragmentIterator(TraversalOrder.POST_ORDER);
		while (fragIter.hasNext()) {
 	 	
		    final Fragment frag = fragIter.next();
		    if (frag.isLocationSensitive()) {
			int[] sites;
//			data source 
			//Add scanOperator here
			if (frag.containsOperatorType(AcquireOperator.class)) {
			    sites = frag.getSourceNodes(); 		
			} else { //deliver
			    sites = new int[] { sink };
			}
 		
			frag.addDesiredSites(sites, daf.getRoutingTree());
			placeLocationSensitiveFragment(daf, frag);
 		
		    } else if (frag.isRecursive()) {
			placeRecursiveFragment(daf, frag);
 		
		    } else if (frag.isAttributeSensitive() && (prevFrag.isRecursive())) {
			//i.e., used by fragments containing AggrEval
			placeAttributeSensitiveOverRecursiveFragment(daf, frag);
 		
		    } else if (frag.isAttributeSensitive()) {
			placeAttributeSensitiveFragment(daf, frag);
 		
			//TODO: property: floating fragment: place a copy of each fragment for each copy of its children
			//TODO: assumes pull down advantageous
		    } else if (frag.getChildFragments().size() == 1) {
			placeFloatingFragment(daf, frag);
		    } else {
			throw new OptimizationException("Unable to place fragment " + frag.getID());
		    }
 		
		    prevFrag = frag;
		}
		dafList.addCandidate(daf);
	}

    /**
     * Places a location sensitive fragment at the required sites.
     * @param plan
     * @param frag
     */
    private static void placeLocationSensitiveFragment(final DAF daf,
	    final Fragment frag) throws OptimizationException {

	if (frag.getDesiredSites().size() < 1) {
	    throw new OptimizationException(
		    "Required sites not specified for location sensitive fragment "
			    + frag.getID());
	}

	logger.finer("Placing location sensitive fragment " + frag.getID()
		+ " on required sites " + frag.getDesiredSitesString());

	ArrayList<Site> desiredSites = daf.getRoutingTree().getSites(frag.getDesiredSites());
	daf.placeFragment(frag, desiredSites);

	for (int i = 0; i < frag.getNumChildFragments(); i++) {
	    final Fragment childFrag = frag.getChildFragments().get(i);

	    final Iterator<Site> siteIter = childFrag.getSites().iterator();
	    while (siteIter.hasNext()) {
		final Site childSite = siteIter.next();

		final Path path = daf.getRoutingTree()
			.getPath(childSite.getID(), frag.getDesiredSites().get(0));

		//currently assumes that there is only one desired site for non-leaf nodes.
		//TODO: need to check
		daf.linkFragments(childFrag, childSite, frag, daf.getRoutingTree().getSite(frag.
				getDesiredSites().get(0)), path);
	    }
	}

    }

    /**
     * Places a recursive fragment (i.e., one which may send output tuples to another copy of itself, 
     * required for incremental aggregation) as close to the sources as possible.  Note: It is assumed
     * that a recursive fragment only has one child fragment.
     *  
     * @param plan
     * @param currentNode
     * @param frag
     */
    private static void placeRecursiveFragment(final DAF daf,
	    final Fragment frag) {
	final Fragment childFrag = frag.getChildFragments().get(0);

	final HashMap<Site, Fragment> nodePrevFrag = new HashMap<Site, Fragment>();
	final HashMap<Site, Site> nodePrevNode = new HashMap<Site, Site>();
	final HashMap<Site, Path> nodePrevPath = new HashMap<Site, Path>();

	logger.finer("Placing recursive fragment " + frag.getID());

	final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site currentNode = siteIter.next();

	    logger.finest("Visiting site " + currentNode.getID());

	    //POST-ORDER code goes here
	    if (currentNode.isLeaf()) {

		//first occurrence of previous fragment
		if (childFrag.getSites().contains(currentNode)) {
		    nodePrevFrag.put(currentNode, childFrag);
		    nodePrevNode.put(currentNode, currentNode);
		    nodePrevPath.put(currentNode, new Path());
		    //no occurrence of previous fragment
		} else {
		    nodePrevFrag.put(currentNode, null);
		    nodePrevNode.put(currentNode, null);
		    nodePrevPath.put(currentNode, null);
		}
	    } else {

		//Sensor network node in routing tree with only one child
		if (currentNode.getInDegree() == 1) {

		    //check if previous fragment has been placed on this node
		    if (childFrag.getSites().contains(currentNode)) {

			//check if a copy of the previous or current fragment has been placed upstream
			if (nodePrevFrag.get(currentNode.getInput(0)) != null) {
			    //place here a copy of the current fragment
			    daf.placeFragment(frag, currentNode);

			    //link upstream fragment to current fragment
			    daf.linkFragments(nodePrevFrag.get(currentNode
				    .getInput(0)), nodePrevNode.get(currentNode
				    .getInput(0)), frag, currentNode,
				    nodePrevPath.get(currentNode.getInput(0)));

			    //link previous fragment on current node to current fragment
			    daf.linkFragments(childFrag, currentNode, frag,
				    currentNode, new Path());

			    //update nodePrevFrag and nodePrevNode
			    nodePrevFrag.put(currentNode, frag);
			    nodePrevNode.put(currentNode, currentNode);
			    nodePrevPath
				    .put(currentNode, new Path());

			} else {
			    // first occurence of previous fragment in this branch
			    nodePrevFrag.put(currentNode, childFrag);
			    nodePrevNode.put(currentNode, currentNode);
			    nodePrevPath
				    .put(currentNode, new Path());
			}
		    } else {
			//if previous fragment has not been placed on this node, propagate childs values for
			//nodePrevFrag and nodePrevNode, and incrementally construct path
			nodePrevFrag.put(currentNode, nodePrevFrag
				.get(currentNode.getInput(0)));
			nodePrevNode.put(currentNode, nodePrevNode
				.get(currentNode.getInput(0)));

			final Path p = nodePrevPath.get(currentNode
				.getInput(0));
			//quick-fix
			if (p != null) {
			    p.append(currentNode);
			    nodePrevPath.put(currentNode, p);
			} else {
			    nodePrevPath.put(currentNode, null);
			}
		    }

		    //Sensor network node with multiple children 
		} else {
		    //count the number of child nodes which have copies of the previous or current 
		    //fragment executing downstream
		    int flaggedChildCount = 0;
		    int flaggedChildIndex = -1;
		    for (int i = 0; i < currentNode.getInDegree(); i++) {
			if (nodePrevFrag.get(currentNode.getInput(i)) != null) {
			    flaggedChildCount++;
			}
			flaggedChildIndex = i; //TODO: check whether this should be in curly braces
		    }
		    logger.finest("Number of flagged children: "
			    + flaggedChildCount);

		    //number of flagged children == 1 and prev frag executing on current node or
		    //number of flagged children > 1
		    if (((flaggedChildCount == 1) && childFrag.getSites()
			    .contains(currentNode))
			    || (flaggedChildCount > 1)
			    || (childFrag.getSites().contains(currentNode) && (childFrag
				    .getSites().size() == 1))) { //try this
			//place current fragment on current node
			daf.placeFragment(frag, currentNode);

			//link prev frag in each child node as necessary
			for (int i = 0; i < currentNode.getInDegree(); i++) {
			    if (nodePrevFrag.get(currentNode.getInput(i)) != null) {
				daf.linkFragments(nodePrevFrag.get(currentNode
					.getInput(i)), nodePrevNode
					.get(currentNode.getInput(i)), frag,
					currentNode, nodePrevPath
						.get(currentNode.getInput(i)));
			    }
			}

			//link prev frag in current node if necessary
			if (childFrag.getSites().contains(currentNode)) {
			    daf.linkFragments(childFrag, currentNode, frag,
				    currentNode, new Path());
			}

			//update nodePrevFrag and nodePrevNode
			nodePrevFrag.put(currentNode, frag);
			nodePrevNode.put(currentNode, currentNode);
			nodePrevPath.put(currentNode, new Path());

			//child fragment on child node - propagate values
		    } else if (flaggedChildCount == 1) {

			nodePrevFrag.put(currentNode, nodePrevFrag
				.get(currentNode.getInput(flaggedChildIndex)));
			nodePrevNode.put(currentNode, nodePrevNode
				.get(currentNode.getInput(flaggedChildIndex)));

			final Path p = nodePrevPath.get(currentNode
				.getInput(flaggedChildIndex));
			p.append(currentNode);
			nodePrevPath.put(currentNode, p);
		    } else {
			nodePrevFrag.put(currentNode, null);
			nodePrevNode.put(currentNode, null);
			nodePrevPath.put(currentNode, null);
		    }
		}
	    }
	}
    }

    /**
     * Places an attribute sensitive fragment (i.e., one which requires all the tuples from the previous
     * fragments) as far upstream as possible.
     * 
     * @param plan
     * @param currentNode
     * @param frag
     */
    private static void placeAttributeSensitiveFragment(final DAF daf,
	    final Fragment frag) {
	final HashMap<Site, HashSet<Site>> nodePrevNodes = new HashMap<Site, HashSet<Site>>();
	final HashMap<Site, Path> nodePrevPath = new HashMap<Site, Path>();

	logger.finer("Placing attribute sensitive fragment " + frag.getID());

	final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site currentNode = siteIter.next();

	    logger.finest("Visiting site " + currentNode.getID());

	    //POST-ORDER code goes here
	    final HashSet<Site> prevNodes = new HashSet<Site>();
	    final Path prevPath = new Path();

	    //check if previous fragment has been placed on this node
	    if (frag.getChildFragSites().contains(currentNode)) {
	    	prevNodes.add(currentNode);
	    }

	    //look at how many copies of previous fragment have been enountered in each child of the current node
	    for (int i = 0; i < currentNode.getInDegree(); i++) {
	    	prevNodes.addAll(nodePrevNodes.get(currentNode.getInput(i)));
	    }

	    nodePrevNodes.put(currentNode, prevNodes);
	    nodePrevPath.put(currentNode, prevPath);

	    //not all copies of previous fragment encountered, do routing
	    if (prevNodes.size() < frag.getChildFragSites().size()) {

		final Iterator<Site> snIterator = nodePrevNodes
			.get(currentNode).iterator();
		while (snIterator.hasNext()) {
		    final Site n = snIterator.next();

		    if (n != currentNode) {
			nodePrevPath.get(n).append(currentNode);
		    }
		}

	    } else {
		//all copies of previous fragments encountered, place current fragment at current node
		daf.placeFragment(frag, currentNode);

		//link each copy of each child fragment to the current fragment
		final Iterator<Fragment> childFragIter = frag
			.getChildFragments().iterator();
		while (childFragIter.hasNext()) {
		    final Fragment childFrag = childFragIter.next();

		    final Iterator<Site> snIterator = childFrag.getSites()
			    .iterator();
		    while (snIterator.hasNext()) {
			final Site n = snIterator.next();
			final Path path = nodePrevPath.get(n);

			daf.linkFragments(childFrag, n, frag, currentNode,
				path);
		    }
		}

		break;
	    }
	}
    }

    private static void placeAttributeSensitiveOverRecursiveFragment(
	    final DAF daf, final Fragment frag) {

	logger
		.finest("Placing Attribute Sensitive Fragment over a recursive fragment "
			+ frag.getID());

	final Fragment prevFrag = frag.getChildFragments().get(0);
	final Counter prevFragNumEncountered = new Counter();

	final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site currentNode = siteIter.next();

	    logger.finest("Visiting site " + currentNode.getID());

	    if (prevFrag.getSites().contains(currentNode)) {
		prevFragNumEncountered.inc();
		logger.finest(prevFragNumEncountered.toString());
	    }

	    if (prevFragNumEncountered.getCount() == prevFrag.getSites().size()) {

		daf.placeFragment(frag, currentNode);
		daf.linkFragments(prevFrag, currentNode, frag, currentNode,
			new Path());
		break;
	    }
	}
    }

    private static void placeFloatingFragment(final DAF daf,
	    final Fragment frag) {

	logger.finest("Placing floating fragment " + frag.getID());

	//TODO: consider possibility that floating fragment might have more than one child (e.g. union op)
	final Fragment prevFrag = frag.getChildFragments().get(0);

	final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site currentNode = siteIter.next();

	    logger.finest("Visiting site " + currentNode.getID());

	    if (prevFrag.getSites().contains(currentNode)) {

		daf.placeFragment(frag, currentNode);
		daf.linkFragments(prevFrag, currentNode, frag, currentNode, new Path());
	    }
	}
    }

}
