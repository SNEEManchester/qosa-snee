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

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/**
 * The fragmented-algebraic form (FAF) of the query plan operator tree. 
 * @author galpini
 *
 */
public class FAF extends PAF implements Scorable {

	/**
	 * The physical-algebraic form of the query plan operator tree from which
	 * FAF is derived.
	 */ 
	private PAF paf = null;

    /**
     *  The root fragment in the query plan. 
     *  Determined during partitioning
     * 	 
     */
    private Fragment rootFragment;

    /** 
     *  Set of fragments in the query plan.
     *  Determined during partitioning
     *  
     */
    private final HashSet<Fragment> fragments = new HashSet<Fragment>();	
	
    /**
     * Counter to assign unique id to different candidates.
     */
    protected static int candidateCount = 0;
    
    /**
     * The score assigned to this FAF (QoS-aware version only)
     */
    private double score = -1;
    
    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger(FAF.class.getName());    
    
	/**
	 * Implicit constructor used by subclass.
	 */
	protected FAF() { }
	
	/**
	 * Constructor.
	 * @param inPAF The physical-algebraic form of the query plan operator tree 
	 * from which FAF is derived.
	 * @param queryName The name of the query
	 */
	public FAF(final PAF inPAF, final String queryName) {
		super(inPAF, generateName(queryName));
		this.paf = inPAF;
		Fragment.resetFragmentCounter(); 
	//TODO: make sure this is called before partitioning, for each candidate paf
		this.rootFragment = new Fragment();		
		this.fragments.add(this.rootFragment);
	}

    /**
     * Constructor used by clone.
     * @param faf The FAF to be cloned
     * @param inName The name to assign to the data structure
     */
	public FAF(final FAF faf, final String inName) {
		super(faf, inName);
		
		this.paf = faf.paf; //This is ok because the laf is immutable now

		//Root fragment
		String rootFragID = faf.getRootFragment().getID();
		
		//Copy fragments collection
		Iterator<Fragment> fragIter = faf.fragments.iterator();
		while (fragIter.hasNext()) {
			Fragment frag = fragIter.next();
			Fragment newFrag = frag.clone(this);
			this.fragments.add(newFrag);
			if (newFrag.getID().equals(rootFragID)) {
				this.rootFragment = newFrag;
			}
		}
		
		//TODO: check if the below code is not handled elsewhere
		//update exchangeOps parentFragments/childFragments
		Iterator<Operator> opIter = 
					faf.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			if (op instanceof ExchangeOperator) {
				ExchangeOperator oldExchOp = (ExchangeOperator) op;
				ExchangeOperator newExchOp = (ExchangeOperator) nodes.get(op.getID());
				Fragment oldParentFragment = oldExchOp.getDestFragment();
				//CB do we need to go via ID here?
				if (oldParentFragment == null) {
					logger.info("Destination fragment was Null");
				} else {
					String fragID = oldParentFragment.getID();
					Fragment newParentFragment = this.getFragment(fragID);
					newExchOp.setDestinationFragment(newParentFragment);
				}
				Fragment oldChildFrag = oldExchOp.getSourceFragment();
				if (oldChildFrag != null) {
					Fragment newChildFrag = 
							this.getFragment(oldChildFrag.getID());
					newExchOp.setSourceFragment(newChildFrag);
				}
			} 
		}	
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
    	return queryName + "-FAF-" + candidateCount;
    }
	
	/**
	 * Returns the fragments in the query plan.
	 * @return the fragments in the query plan.
	 */
	public final HashSet<Fragment> getFragments() {
		return fragments;
	}

    /**
     * Returns the root fragment of the query plan.
     * @return the root fragment
     */
	public final Fragment getRootFragment() {
		return rootFragment;
	}

    /**
     * Returns the fragment with the specified ID.
     * @param fragID the identifier of the desired fragment
     * @return the fragment the fragment requested
     */
    public final Fragment getFragment(final String fragID) {
		final Iterator<Fragment> fragIter = this.fragments.iterator();
		while (fragIter.hasNext()) {
		    final Fragment frag = fragIter.next();
		    if (frag.getID().equals(fragID)) {
			return frag;
		    }
		}
		return null;
    }	

    /**
     * Helper method to recursively get the leaf fragments in the FAF/DAF.
     * @param frag			the current fragment being visited
     * @param leafFragments the leaf fragments of the query plan
     * @return the leaf fragments in the query plan
     */
    private HashSet<Fragment> getLeafFragments(final Fragment frag,
    	    final HashSet<Fragment> leafFragments) {

    	if (frag.isLeaf()) {
    	    leafFragments.add(frag);
    	}

    	//fragment traversal
    	for (int fragChildIndex = 0; fragChildIndex < frag
    		.getNumChildExchangeOperators(); fragChildIndex++) {
    	    final ExchangeOperator c = frag
    		    .getChildExchangeOperator(fragChildIndex);

    	    this.getLeafFragments(c.getSourceFragment(), leafFragments);
    	}

    	return leafFragments;
    }

    /**
     * Gets the leaf fragments in the FAF/DAF.
     * @return the leaf fragments
     */
    public final HashSet<Fragment> getLeafFragments() {
    	final HashSet<Fragment> leafFragments = new HashSet<Fragment>();
    	return this.getLeafFragments(this.getRootFragment(), leafFragments);
    }    
    
    /**
     * Given a physical query plan with exchange operators inserted, 
     * constructs the fragment tree. 
     */
    public final void buildFragmentTree() {

		boolean newFrag = true;
		Fragment currentFrag = this.rootFragment;
	
		final Iterator<Operator> opIter = this
			.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
	
		    if (op instanceof ExchangeOperator) {
				//new fragment indicated by exhange
				//link the current fragment to the exchange operator
				currentFrag = op.getParent().getContainingFragment();
				((ExchangeOperator) op).setDestinationFragment(currentFrag);
				currentFrag.addChildExchange((ExchangeOperator) op);
				// create a new fragment
				currentFrag = new Fragment();
				this.fragments.add(currentFrag);
				newFrag = true;
				// link the new fragment to the exchange operator
				((ExchangeOperator) op).setSourceFragment(currentFrag);
				currentFrag.setParentExchange((ExchangeOperator) op);
		    } else if (newFrag) {
				//first operator in each fragment 
		    	//(i.e., the fragment's root operator)
				currentFrag.setRootOperator(op);
				newFrag = false;
				currentFrag.addOperator(op);
				op.setContainingFragment(currentFrag);
		    } else {
				//all other operators within a fragment
				currentFrag = ((Operator) op.getParent())
					.getContainingFragment();
				currentFrag.addOperator(op);
				op.setContainingFragment(currentFrag);
		    }
		}
    }
 
    /**
     * Helper method to recursively generate the operator iterator.
     * @param frag the fragment being visited
     * @param fragList the operator list being created
     * @param traversalOrder the traversal order desired 
     */    
    private void doFragmentIterator(final Fragment frag,
    	    final ArrayList<Fragment> fragList, 
    	    final TraversalOrder traversalOrder) {

	    	if (traversalOrder == TraversalOrder.PRE_ORDER) {
	    	    fragList.add(frag);
	    	}
	
	    	for (int n = 0; n < frag.getChildFragments().size(); n++) {
	
	    	    this.doFragmentIterator(frag.getChildFragments().get(n), 
	    	    		fragList, traversalOrder);
	    	}
	
	    	if (traversalOrder == TraversalOrder.POST_ORDER) {
	    	    fragList.add(frag);
	    	}
        }

    /**
     * Iterator to traverse the operator tree.
     * The structure of the operator tree may not be modified during iteration
     * @param traversalOrder the order to traverse the operator tree
     * @return an iterator for the operator tree
     */
    public final Iterator<Fragment> fragmentIterator(
        		final TraversalOrder traversalOrder) {

    	final ArrayList<Fragment> fragList = new ArrayList<Fragment>();
    	this.doFragmentIterator(this.getRootFragment(), fragList,
    		traversalOrder);

    	return fragList.iterator();
    }

    protected void exportAsDOTFile(final String fname, 
			TreeMap<String, StringBuffer> opLabelBuff,
			TreeMap<String, StringBuffer> edgeLabelBuff,
			StringBuffer fragmentsBuff) {
    	
       /**
	     * Draw the fragments
	     */
	    final Iterator<Fragment> f = this.fragments.iterator();
	    while (f.hasNext()) {
		final Fragment frag = f.next();
		fragmentsBuff.append("subgraph cluster_" + frag.getID() + " {\n");
		fragmentsBuff.append("style=\"rounded,dashed\"\n");
		fragmentsBuff.append("color=red;\n");

		final Iterator<Operator> opIter 
			= frag.getOperators().iterator();
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
		    fragmentsBuff.append("\"" + op.getID() + "\" ;\n");
		}

		final StringBuffer sites = new StringBuffer();
		boolean first = true;
		final Iterator<Site> sitesIter = frag.getSites().iterator();
		while (sitesIter.hasNext()) {
		    if (!first) {
			sites.append(",");
		    }
		    sites.append(sitesIter.next().getID());
		    first = false;
		}

		fragmentsBuff.append("fontsize=9;\n");
		fragmentsBuff.append("fontcolor=red;\n");
		fragmentsBuff.append("labelloc=t;\n");
		fragmentsBuff.append("labeljust=r;\n");
		fragmentsBuff.append("label =\"Fragment " + frag.getID());
		
		if (!sites.toString().equals("")) {
			fragmentsBuff.append("\\n {" + sites + "}");
		}	
		fragmentsBuff.append("\";\n}\n");
		
		//draw recursive loop
		if (frag.isRecursive()) {
				ExchangeOperator parentExchOp = frag.getParentExchangeOperator();
				if (parentExchOp!=null) {
				    final Operator op = parentExchOp.getInput(0);
				    fragmentsBuff.append(frag.getParentExchangeOperator().getID()
						    + " -> " + op.getID()
						    + " [headport=s tailport=n];\n");
				}
			}
	    }
	    
	    super.exportAsDOTFile(fname, opLabelBuff, edgeLabelBuff, 
    			fragmentsBuff);
    }
 
	public String getProvenanceString() {
		return this.paf.getProvenanceString() + "->" + this.name;
	}

	
	public PAF getPAF() {
		return this.paf;
	}
	
	public double getScore() {
		return this.score;
	}
    
	public void setScore(double s) {
		this.score = s;
	}
	
	/**
	 * Removes a fragment from the fragments collection.
	 * @param fragment	The fragment to be removed.
	 */
	public void removeFragment(Fragment fragment) {
		this.fragments.remove(fragment);
	}
}
