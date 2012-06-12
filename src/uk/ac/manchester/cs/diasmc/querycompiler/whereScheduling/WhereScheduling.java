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
package uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Counter;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Path;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.partitioning.Partitioning;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.qosaware.QWhereScheduling;
import uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.vanilla.VWhereScheduling;

/**
 * Where Scheduling: Step 6 of query optimization
 * 
 * <i>Where scheduling</i> decides which FAF fragments are to run on which 
 * routing tree nodes (sites). This results in the <i>distributed-algebraic 
 * form</i> (DAF) of the query by deciding on the creation and assignment of 
 * fragment instances in the FAF to sites in the routing tree.
 * 
 * @author Ixent Galpin
 */
public class WhereScheduling {

	/**
	 * Logger for this class.
	 */
    private static Logger logger = 
    	Logger.getLogger(WhereScheduling.class.getName());

    /**
     * 
     * Generates instances of fragments and assigns them to sites in the sensor
     * network.  The resulting query plan is in DAF (distributed-algebraic form,
     * FAF with sites annotated to each fragment).  
     * @param faf		The query plan in fragmented-algerbaic form 
     * 		(i.e., paf with exchange operators inserted)
     * @param schemaMetadata	Metadata for the query
     * @param queryName The name of the query being optimized
     * @param qoSSpec 
     * @throws OptimizatonException
     * 
     */
    public static ScoredCandidateList<DAF> doWhereScheduling(
    		final PAF paf, 
    		final ScoredCandidateList<RT> rtList,
    		final SchemaMetadata schemaMetadata,
    		final Integer Sink,
    		final String queryName, QoSSpec qoSSpec) throws OptimizationException {

    	ScoredCandidateList<DAF> dafList = new ScoredCandidateList<DAF>();
    		
    	Iterator<RT> rtIter = rtList.iterator();
		while (rtIter.hasNext()) {
	    	RT rt = rtIter.next();

	    	if (Settings.QOS_AWARE_WHERE_SCHEDULING) {
    		    QWhereScheduling.doWhereScheduling(schemaMetadata, Sink, 
    		    		queryName, paf, rt, dafList, qoSSpec);
	    	} else {
	    		// Partitioning (fragment definition)
	    		logger.info("Starting Partitioning");
	    		ScoredCandidateList<FAF> fafList = Partitioning.doPartitioning(
	    				paf, 
	    				queryName);
	        	Iterator<FAF> fafIter = fafList.iterator();
	        	
	        	// Vanilla Where sched (fragment instance assignment)
	        	while (fafIter.hasNext()) {
	            	FAF faf = fafIter.next();   
	            	
	    		    VWhereScheduling.doWhereScheduling(schemaMetadata, Sink, 
	    		    		queryName, faf, rt, dafList);	    		    

	    	    	//Remove redundant exchanges
	    	    	Iterator<DAF> dafIter = dafList.iterator();
	    	    	while (dafIter.hasNext()) {
	    	    		DAF daf = dafIter.next();
	    	        	if (Settings.WHERE_SCHEDULING_REMOVE_REDUNDANT_EXCHANGES) {
	    	    		    daf.removeRedundantRecursiveFragments();
	    	    	    	daf.removeRedundantExchanges();
	    	    	    }    		
	    	    	}
	        	}    		    
    		}
    	}
  	
    	//dafList.keepBest(2); //TODO: this is temporary
    	System.out.println("daf list size="+dafList.getSize());
    	
        if (Settings.DISPLAY_DAF) {
        	dafList.display(QueryCompiler.queryPlanOutputDir);
        }
        
		return dafList;
    }

    
}
