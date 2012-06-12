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
package uk.ac.manchester.cs.diasmc.querycompiler.mqe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
/**
 * Extends the fragment class to contain queryID.
 * @author Jamil Naja
 *
 */
public class MultiFragment extends Fragment {
	/**
	 * query which the fragment belongs to.
	 */
	private int queryID;
	
	public  MultiFragment(final int queryID, final Fragment original) {

    	this.fragID = Integer.valueOf(original.getID());
    	this.queryID = queryID;
    	this.rootOperator = original.getRootOperator();
    	
    	if (original.getParentExchangeOperator() != null) {
    	    this.parentExchange = original.getParentExchangeOperator();
    	}

    	for (int i = 0; i < original.getNumChildExchangeOperators(); i++) {
    		ExchangeOperator clonedExchOp = 
    					original.getChildExchangeOperator(i);
    	    this.childExchanges.add(clonedExchOp);
    	}

    	
    	
    	final Iterator<Operator> opIter = 
    		original.operatorIterator(TraversalOrder.PRE_ORDER);
    	while (opIter.hasNext()) {
    	    final Operator clonedOp = (Operator) opIter.next();
    	    this.operators.add(clonedOp);
    	    clonedOp.setContainingFragment(this);
    	}

    	
    	Iterator<String> siteIDIter = original.desiredSites.iterator();
    	while (siteIDIter.hasNext()) {
    	    final String siteID = siteIDIter.next();
    	    this.desiredSites.add(siteID);
    	}
    	
    	
    }

    
	/**
	 * Sets the query ID  for the fragement.
	 * @param queryID query which the fragment belongs to.
	 */
	public final void  setQueryID(final int queryID) {
		this.queryID = queryID;
	}
	/**
	 * @return retuns the fragment ID prefixed by the query ID.
	 * e.g Fragment-1 from queryID 2
	 * ID = 1_Q2
	 * 
	 */
	@Override
	public  final String getID() {
		return (String.valueOf(this.fragID) 
				+ "_Q" + String.valueOf(this.queryID) + "_");
	    }

}
