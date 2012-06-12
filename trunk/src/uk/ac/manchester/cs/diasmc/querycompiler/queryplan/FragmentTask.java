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

import java.util.logging.Logger;


import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.mqe.MultiFragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;

/**
 * This class represents a tasks which involves executing a query plan fragment.
 * @author Ixent Galpin
 *
 */
public class FragmentTask extends Task {

    static Logger logger = Logger.getLogger(FragmentTask.class.getName());

    //The query plan fragment
    private Fragment fragment;

    //The node this task is executing on
    private Site site;

    private DAF daf;

    //If a leaf fragment, the occurrence (i.e., evaluation) of the node.
    private long occurrence;

    private long bufferingFactor;

    /**
     * Creates a instance of a FragmentTask.
     * @param startTime
     * @param fragment
     * @param site
     * @param l
     */
    public FragmentTask(final long startTime, final Fragment fragment,
	    final Site site, final long l, final long bufferingFactor,
	    final DAF daf) {
	super(startTime);
	this.fragment = fragment;
	this.daf = daf;
	this.bufferingFactor = bufferingFactor;
	this.site = site;
	this.endTime = startTime + this.getTimeCost(daf);
	this.occurrence = l;
    }

    /**
     * Returns the duration of this task.
     */
    @Override
	public final long getTimeCost(final DAF daf) {

		double calcTime;
		if (this.fragment.isLeaf()) {
		    calcTime = this.fragment.getTimeExpression(CardinalityType.MAX, site, this.daf, true).
		    	evaluate(daf.getAcquisitionInterval(),1);
		} else {
		    calcTime = this.fragment.getTimeExpression(CardinalityType.MAX, site, this.daf, true).
		    	evaluate(daf.getAcquisitionInterval(), this.bufferingFactor);
		}
	
		if (calcTime > CostParameters.getMinimumTimerInterval()) {
		    return (long) Math.ceil(calcTime);
		}
		return (long) CostParameters.getMinimumTimerInterval();
    }

    public final Fragment getFragment() {
    	
    		return (Fragment) fragment;
		
    }
    /**
     * converts Fragments to MultiFragments and adds queryName for them.
     * @param queryID the query that the fragment belongs to.
     */
    public final void storeMultiFragment(final int queryID) {
    	this.fragment = new MultiFragment(queryID,this.fragment); 
    	    	
        }

    public final long getOccurrence() {
	return this.occurrence;
    }

    @Override
	public final Site getSite() {
	return this.site;
    }

    @Override
	public final String getSiteID() {
	return this.site.getID();
    }

    /**
     * String representation
     */
    @Override
	public final String toString() {
	return "F" + this.fragment.getID() + "n" + this.site.getID() + "_"
		+ this.occurrence;
    }

    @Override
	public final FragmentTask clone(final DAF clonedDAF) {
	final Fragment clonedFrag = clonedDAF.getFragment(this.fragment
		.getID());
	final Site clonedNode = (Site) clonedDAF.getRoutingTree().getNode(
		this.site.getID());

	final FragmentTask clone = new FragmentTask(this.startTime, clonedFrag,
		clonedNode, this.occurrence, this.bufferingFactor, this.daf);
	return clone;
    }

}
