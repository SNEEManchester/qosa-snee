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
package uk.ac.manchester.cs.diasmc.querycompiler.routing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.LinkCostMetric;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.RadioLink;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Topology;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSVariable;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;

/**
 * Routing: Step 4 of query optimization
 * 
 * Routing decides which sites to use for routing the tuples involved in 
 * evaluating the query plan. The approach adopted by the SNEEql compiler/
 * optimizer to decide on a routing strategy is to compute a routing tree (RT)
 * for the physical-algebraic form (PAF) that is economical with respect to 
 * the total distance travelled by tuples. 
 * 
 * This algorithm considers the set of sites that are data sources, and the 
 * destination site, of the physical-algebraic form (PAF). The aim is, for each
 * source site, to minimize the total cost to the destination.  Thus, the 
 * optimal routing tree RT for the query is the Steiner tree given the sensor 
 * network topology and the Steiner points PAF.Sources union {PAF.Destination}. 
 * The problem of computing a Steiner tree is NP-complete, so a heuristic 
 * algorithm (reputed to perform well in practice) is used to compute an 
 * approximation of the Steiner tree.
 * 
 * The resulting query plan is the physical-algebraic form (PAF) coupled with 
 * a routing tree (RT). 
 */
public class Routing {

	/**
	 * Logger for this class.
	 */
    private static final Logger logger = 
    	Logger.getLogger(Routing.class.getName());
    
    /**
     * Carry out Routing step of query optimization.
     * 
     * @param paf			the query plan in physical-algeabraic form
     * @param sensornet		the topology of the sensor network
     * @param schemaMetadata relevant metadata relating to the query
     * @param queryName		the name of the query
     * @return a list of candidate routing trees
     */
    public static final ScoredCandidateList<RT> doRouting(
    		final PAF paf,
    		final Topology sensornet, 
    		final SchemaMetadata schemaMetadata,
    		final Integer sink,
    		final String queryName, 
    		final QoSSpec qos) {
    	
    	ScoredCandidateList<RT> rtList;    	
    	if (Settings.ROUTING_TREE_FILE!=null || !Settings.QOS_AWARE_ROUTING) {
    		rtList = new ScoredCandidateList<RT>();    		
    	} else {
    		//TODO: here we select the relevant scoring functions from the 
    		//QoSSpec
    		rtList = new ScoredCandidateList<RT>(
    				qos.selectRTScoringFunctions());
    	} 
    		
		logger.finer("Initiating Routing step.");
	
		logger.finest("Find the data source and destination nodes");
		
	
		final int[] sources = paf.getRootOperator().getSourceSites();
		logger.finest(Arrays.toString(sources));
	
		if (Settings.ROUTING_TREE_FILE!=null) {
			parseRoutingTreeFile(sensornet, queryName, rtList, sink);
		} else if (Settings.QOS_AWARE_ROUTING) {
			doQoSAwareRouting(sensornet, queryName, rtList, sink, sources, qos);	
		} else {
			doVanillaRouting(sensornet, queryName, rtList, sink, sources);
		}
		
        if (Settings.DISPLAY_RT) {
        	rtList.display(QueryCompiler.queryPlanOutputDir);
        }

		return rtList;
    }


	/**
     * Vanilla routing algorithm. In this case, only a single routing tree is 
     * produced using the radioLoss as the cost considered.
     * @param sensornet The sensor network topology
     * @param queryName The name of the query
     * @param rtList The output of routing trees
     * @param sink The sink node
     * @param sources The source nodes
     */
	private static void doVanillaRouting(final Topology sensornet, 
			final String queryName, 
			final ScoredCandidateList<RT> rtList, 
			final int sink, 
			final int[] sources) {
		//Vanilla version
		//For ICDE08, VLDB08 we used random seed = 4.
		RT rt = sensornet.steinerTree(sink, sources, queryName, 4, LinkCostMetric.ENERGY,
				FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false);
		rt.updateNumSourceWeighting();
		rtList.addCandidate(rt);
	}

	
	private static void doQoSAwareRouting(final Topology sensornet, 
			final String queryName, 
			final ScoredCandidateList<RT> rtList, 
			final int sink, 
			final int[] sources,
			final QoSSpec qos) {

		int n = 0;
		
		//add an RT using the original heuristic
		RT rt = sensornet.steinerTree(sink, sources, queryName, 4, LinkCostMetric.ENERGY,
				FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false);
		rt.updateNumSourceWeighting();
		rtList.addCandidate(rt);
		
		while (rtList.getTotalSize()<Settings.ROUTING_TREES_TO_GENERATE) {
			if (qos.getOptimizationVariable()==QoSVariable.LIFETIME) {
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.ENERGY, 
						FirstVHeuristic.SINK, 
						NextVHeuristic.RANDOM, true);
				rt.updateNumSourceWeighting();
				//v1=sink, vn=random, m=energy, dynamic=true				
				rtList.addCandidate(rt);
				
				//v1=sink, vn=random, m=randomPerBranch, dynamic=true
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_BRANCH, 
						FirstVHeuristic.SINK, 
						NextVHeuristic.RANDOM, true);
				rt.updateNumSourceWeighting();
				rtList.addCandidate(rt);

				//v1=sink, vn=random, m=randomPerHop, dynamic=true
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_HOP, 
						FirstVHeuristic.SINK, 
						NextVHeuristic.RANDOM, true);
				rt.updateNumSourceWeighting();		
				rtList.addCandidate(rt);
				
				//v1=sink, vn=closestSink, m=latency, dynamic=true
				if (n==0)  {
					rt = sensornet.steinerTree(sink, sources, queryName, 
							n, LinkCostMetric.LATENCY, 
							FirstVHeuristic.SINK, NextVHeuristic.CLOSEST_SINK, true);
					rt.updateNumSourceWeighting();		
					rtList.addCandidate(rt);
				}
				
				//v1=sink, vn=closestSink, m=randomPerBranch, dynamic=true
/*				rtList.addCandidate(sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_BRANCH, 
						FirstVHeuristic.SINK, 
						NextVHeuristic.CLOSEST_SINK, true));
				
				//v1=sink, vn=closestSink, m=randomPerHop, dynamic=true
				rtList.addCandidate(sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_HOP, 
						FirstVHeuristic.SINK, 
						NextVHeuristic.CLOSEST_SINK, true));
*/				
			} else {
				//v1=sink, vn=closestAny, m=latency, dynamic=false
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.LATENCY, 
						FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false);
				rt.updateNumSourceWeighting();	
				rtList.addCandidate(rt);
				
				//v1=sink, vn=closestAny, m=randomPerBranch, dynamic=false
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_BRANCH, 
						FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false);
				rt.updateNumSourceWeighting();	
				rtList.addCandidate(rt);

				//v1=sink, vn=closestAny, m=randomPerHop, dynamic=false
				rt = sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_HOP, 
						FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false);
				rt.updateNumSourceWeighting();	
				rtList.addCandidate(rt);
				
				
/*				//v1=sink, vn=closestSink, m=latency, dynamic=false
				if (n==0) { 
					rtList.addCandidate(sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.LATENCY,FirstVHeuristic.SINK, 
						NextVHeuristic.CLOSEST_SINK, false));
				}

				//v1=sink, vn=closestSink, m=randomPerBranch, dynamic=false
				rtList.addCandidate(sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_BRANCH, FirstVHeuristic.SINK, 
						NextVHeuristic.CLOSEST_SINK, false));
				
				//v1=sink, vn=closestSink, m=randomPerHop, dynamic=false
				rtList.addCandidate(sensornet.steinerTree(sink, sources, queryName, 
						n, LinkCostMetric.RANDOM_PER_HOP, FirstVHeuristic.SINK, 
						NextVHeuristic.CLOSEST_SINK, false));
*/
			}
			n++;
		}
		rtList.keepBest(Settings.ROUTING_TREES_TO_KEEP);			
	}
	
//    /**
//     * QoS-aware routing algorithm. In this case, several routing trees are 
//     * produced using different costs (radioLoss, latency, energy). The idea
//     * is to inject variety into the routing trees generated. Then, only the
//     * best n trees are kept.
//     * @param sensornet The sensor network topology
//     * @param queryName The name of the query
//     * @param rtList The output of routing trees
//     * @param sink The sink node
//     * @param sources The source nodes
//     */
//	private static void doQoSAwareRouting(final Topology sensornet, 
//			final String queryName, 
//			final ScoredCandidateList<RT> rtList, 
//			final int sink, 
//			final int[] sources) {
//		
//		int n = 0;
//		while (rtList.getTotalSize()<Settings.ROUTING_TREES_TO_GENERATE) {
//
//			/* Generate routing tree considering only energy */
//			doDoQosAwareRouting(rtList, sensornet, sink, sources, queryName, 
//					Settings.ROUTING_RANDOM_SEED * n, LinkCostMetric.ENERGY);
//			
//			/* Generate routing tree considering only latency */
//			doDoQosAwareRouting(rtList, sensornet, sink, sources, queryName, 
//					Settings.ROUTING_RANDOM_SEED * n, LinkCostMetric.LATENCY);		
//			
//			/* Generate routing tree with consider mixed cost metrics - 
//			 * use a different one for each branch added to the tree*/
//			doDoQosAwareRouting(rtList, sensornet, sink, sources, queryName, 
//					Settings.ROUTING_RANDOM_SEED * n, LinkCostMetric.RANDOM_PER_BRANCH);
//			
//			/* Generate routing tree with consider mixed cost metrics - 
//			 * use a different one for each edge added to the tree*/
//			doDoQosAwareRouting(rtList, sensornet, sink, sources, queryName, 
//					Settings.ROUTING_RANDOM_SEED * n, LinkCostMetric.RANDOM_PER_HOP);
//
//			n++;
//		}
//		rtList.keepBest(Settings.ROUTING_TREES_TO_KEEP);
//	}
//
//	public static void doDoQosAwareRouting(final ScoredCandidateList<RT> rtList, Topology sensornet, 
//			final int sink, final int[] sources,
//		    final String queryName, int randomSeed, LinkCostMetric linkCostMetric) {
//	
//		ArrayList<RT> tmpRTList = new ArrayList<RT>();
//		
//		/* Generate rt using original heuristic */
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.SINK, NextVHeuristic.RANDOM, false));
//
//		/*Try other combinations */
//		//best heuristic for windAvg query for max lambda (with metric=energy)
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.SINK, NextVHeuristic.RANDOM, true));
//
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.SINK, NextVHeuristic.CLOSEST_ANY, false));
//
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.SINK, NextVHeuristic.CLOSEST_ANY, true));
//
//
//		//determinstic for LATENCY and ENERGY link cost metrics, as the nodes
//		//are added in predetermined order - no point in doing more than once
//		if (randomSeed==0 || linkCostMetric==LinkCostMetric.RANDOM_PER_BRANCH ||
//				linkCostMetric==LinkCostMetric.RANDOM_PER_HOP) { 
//			tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.SINK, NextVHeuristic.CLOSEST_SINK, false));
//
//			//best heuristic for getSmoke query for max lambda (with metric=latency)
//			tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//					randomSeed, linkCostMetric, 
//					FirstVHeuristic.SINK, NextVHeuristic.CLOSEST_SINK, true));
//		}
//			
//		/*If FirstVHeuristic!=sink, linkCostMetric cannot be dynamic */
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.RANDOM, NextVHeuristic.RANDOM, false));
//		
//		tmpRTList.add(sensornet.steinerTree(sink, sources, queryName, 
//				randomSeed, linkCostMetric, 
//				FirstVHeuristic.RANDOM, NextVHeuristic.CLOSEST_ANY, false));
//
//		for (int i=0; i< tmpRTList.size(); i++) {
//			RT rt = tmpRTList.get(i);
//			rt.updateNumSourceWeighting();
//			rtList.addCandidate(rt);
//		}
//
//	}
	
	
    private static void parseRoutingTreeFile(Topology sensornet,
			String name, ScoredCandidateList<RT> rtList, int sink) {
    	
    	try {
	    	final RT rt = new RT(name, sensornet.getSite(sink));
	    	
			final FileReader fin = new FileReader(Settings.ROUTING_TREE_FILE);
		    final BufferedReader in = new BufferedReader(fin);
		    String line;
		    while ((line = in.readLine()) != null) {
		    	if (line.trim().length() > 0) {
		    		String[] pair = line.split(":");
		    		String childId = pair[0];
		    		String parentId = pair[1];
		    		Site child = (Site)sensornet.getNode(childId);
		    		Site parent = (Site)sensornet.getNode(parentId);
		    		
		    		System.out.println("child="+childId+", parentId="+parentId);
		    		
		        	String linkID = sensornet.generateEdgeID(childId, parentId);
		        	RadioLink oldLink = (RadioLink) sensornet.getEdge(linkID);
		        	rt.addExternalSiteAndRadioLinkClone(
		        			child, parent, oldLink);    	
		    	}
		    }
		    rt.setDesc("Read from file: "+Settings.ROUTING_TREE_FILE);
		    rtList.addCandidate(rt);
    	} catch (IOException e) {
    		Utils.handleCriticalException(e);
    	}
    }
}
