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
package uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.qosaware;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.HashMapList;
import uk.ac.manchester.cs.diasmc.common.Matlab;
import uk.ac.manchester.cs.diasmc.common.Template;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.graph.DisplayGraph;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.algorithmSelection.PhysicalOptimization;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.TempRadioCosts;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Path;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePart;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrEvalOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrIterateOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.WhereScheduling;

public class QWhereScheduling {

	/**
	 * Logger for this class.
	 */
    private static Logger logger = 
    	Logger.getLogger(QWhereScheduling.class.getName());

    
    private static String MATLAB_TEMPLATE_DIR =
    	"src/matlab/wheresched/";
    
    private static String MATLAB_WORKING_DIR =
    	QueryCompiler.queryPlanOutputDir + "matlab/wheresched";
    
    public static void doWhereScheduling(final SchemaMetadata schemaMetadata,
			final Integer sink,
			final String queryName, 
			final PAF paf, 
			final RT rt,
			ScoredCandidateList<DAF> dafList, QoSSpec qoSSpec) throws OptimizationException {
    	
    	try {	
	    	Utils.checkDirectory(MATLAB_WORKING_DIR, true);
	    	int id = DAF.getNextCandidateId();
	    	
	    	//generate a partial DAF
	    	OperatorInstanceTree daf = constructPartialDAF(
	    			schemaMetadata, sink, paf, rt);
	    	daf.display(QueryCompiler.queryPlanOutputDir, "partial-DAF-"+id);

	    	//get optimal assignment
	    	getOptimalOpInstAssignment(sink, rt, daf, paf, qoSSpec, id);

	    	//remove redundant operator instances from the operator instance tree
	    	removeRedundantOpInstances(daf);
	    	daf.display(QueryCompiler.queryPlanOutputDir, "pruned-DAF-"+id);
	    	
	    	//Generate old-style daf for compatibility with when-scheduling
	    	//first partition the PAF, then assign fragments to operator instance sites, linking them accordingly
	    	DAF compactDaf = generateCompactDAF(queryName, paf, rt, daf);
	    	
	    	dafList.addCandidate(compactDaf);
	    	
		} catch (IOException e) {
			throw new OptimizationException(e.getMessage());
		}
    }
    
    
	private static OperatorInstanceTree constructPartialDAF(
			SchemaMetadata schemaMetadata, Integer sink, PAF paf, RT rt) {

		OperatorInstanceTree daf = new OperatorInstanceTree(rt, paf);
		HashMapList<String,OperatorInstance> disconnectedOpInstMapping =
			new HashMapList<String,OperatorInstance>();
		
		Iterator<Operator> opIter = paf.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			
	    	if (op.isLocationSensitive()) {
	    		//Location-sensitive operator
	    		addLocationSensitiveOpInstances(sink, rt, daf, op, disconnectedOpInstMapping);

	    	} else if (op.isAttributeSensitive()) {
	    		//Attribute-sensitive operator
	    		addAttributeSensitiveOpInstances(daf, op, rt, disconnectedOpInstMapping);			
			
	    	} else if (op.isRecursive()) {
	    		//Iterative operator
	    		addIterativeOpInstances(daf, op, rt, disconnectedOpInstMapping);
	    	
	    	} else {
	    		//Other operators
	    		addOtherOpTypeInstances(daf, op, disconnectedOpInstMapping);
	    	}
	    	
	    	if (op instanceof DeliverOperator) {
	    		OperatorInstance opInst = daf.getOpInstances(op).get(0);
	    		daf.setRoot(opInst);
	    	}	
		}
		return daf;
	}

	
	private static void addLocationSensitiveOpInstances(Integer sink, RT rt,
			OperatorInstanceTree daf, Operator op,
			HashMapList<String,OperatorInstance> disconnectedOpInstMapping) {
		
		//Get the sites that this operator has been assigned to
		//TODO: create a LocationSensitiveOperator interface with 
		//getDesiredSites method
		int[] sites;
		if (op instanceof AcquireOperator) {
		    sites = op.getSourceSites(); 		
		} else { //deliver
		    sites = new int[] { sink };
		}
		
		//For each site, spawn an operator instance
		for (int j=0; j<sites.length; j++) {
			Site site = rt.getSite(sites[j]);
			OperatorInstance opInst = new OperatorInstance(op, site);
			daf.addOpInst(op, opInst);
			daf.assign(opInst, site);
			disconnectedOpInstMapping.add(op.getID(), opInst);
			convergeAllChildOpSubstreams(op, opInst, daf, disconnectedOpInstMapping);
		}
	}

	private static void convergeAllChildOpSubstreams(Operator op, 
			OperatorInstance opInst, OperatorInstanceTree daf,
			HashMapList<String,OperatorInstance> disconnectedOpInstMapping) {
		for (int k=0; k<op.getInDegree(); k++) {
			Operator childOp = op.getInput(k);
			ArrayList<OperatorInstance> childOpInstColl = 
				disconnectedOpInstMapping.get(childOp.getID());
			convergeSubstreams(childOpInstColl, opInst, daf);
		}		
	}
	
	
	private static void convergeSubstreams(Collection<OperatorInstance> childOpInstColl,
			OperatorInstance opInst, OperatorInstanceTree daf) {
		Iterator<OperatorInstance> childOpInstIter = childOpInstColl.iterator();		
		while (childOpInstIter.hasNext()) {
			OperatorInstance childOpInst = childOpInstIter.next();
			daf.addEdge(childOpInst, opInst);
		}
	}

	
	
	private static void addAttributeSensitiveOpInstances(
			OperatorInstanceTree daf, Operator op, RT rt,
			HashMapList<String,OperatorInstance> disconnectedOpInstMapping) {
		Site dSite = findDeepestConfluenceSite(op, disconnectedOpInstMapping, rt);	
		OperatorInstance opInst = new OperatorInstance(op, dSite);
		daf.addOpInst(op, opInst);
		disconnectedOpInstMapping.add(op.getID(), opInst);
		
		convergeAllChildOpSubstreams(op, opInst, daf, disconnectedOpInstMapping);		
	}


	private static Site findDeepestConfluenceSite(Operator op, HashMapList<String,OperatorInstance> disconnectedOpInstMapping, RT rt) {
		HashSet<OperatorInstance> opInstSet = 
			new HashSet<OperatorInstance>();
		
		for (int i = 0; i<op.getInDegree(); i++) {
			Operator childOp = op.getInput(i);
			opInstSet.addAll(disconnectedOpInstMapping.get(childOp.getID()));
		}
		
		Iterator<Site> siteIter = rt.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();			
			HashSet<OperatorInstance> found = getConfluenceOpInstances(
					site, rt, opInstSet);
			if (found.equals(opInstSet)) {
				return site;
			}
			
		}
		return null;
	}

	private static void addIterativeOpInstances(
			OperatorInstanceTree daf, Operator op, RT rt,
			HashMapList<String,OperatorInstance> disconnectedOpInstMapping) {

		Operator childOp = op.getInput(0);
		//convert to set so that we can do set equality operation later
		HashSet<OperatorInstance> disconnectedChildOpInstSet = 
			new HashSet<OperatorInstance>(disconnectedOpInstMapping.get(childOp.getID()));
		
		Iterator<Site> siteIter = rt.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			HashSet<OperatorInstance> confluenceOpInstSet = 
				getConfluenceOpInstances(site, rt, disconnectedChildOpInstSet);
			if (confluenceOpInstSet.equals(disconnectedChildOpInstSet)) {
				break;
			}
			if (confluenceOpInstSet.size()>1) {
				OperatorInstance opInst = new OperatorInstance(op, site);
				daf.addOpInst(op, opInst);
				convergeSubstreams(confluenceOpInstSet, opInst, daf);
				disconnectedChildOpInstSet.add(opInst);
				disconnectedChildOpInstSet.removeAll(confluenceOpInstSet);
			}
		}
		disconnectedOpInstMapping.set(op.getID(), disconnectedChildOpInstSet);
	}
	
	
	private static HashSet<OperatorInstance> getConfluenceOpInstances(
			Site rootSite, RT rt, 
			HashSet<OperatorInstance> disconnectedChildOpInstances) {
		
		HashSet<OperatorInstance> operatorInstances = 
			new HashSet<OperatorInstance>();
		Iterator<Site> siteIter = rt.siteIterator(rootSite, 
				TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			
			Iterator<OperatorInstance> opInstIter = 
				disconnectedChildOpInstances.iterator();
			while (opInstIter.hasNext()) {
				OperatorInstance opInst = opInstIter.next();
				if (opInst.getDeepestConfluenceSite()==site) {
					operatorInstances.add(opInst);
				}
			}
		}
		return operatorInstances;
	}


	private static void addOtherOpTypeInstances(
			OperatorInstanceTree daf, Operator op,
			HashMapList<String,OperatorInstance> disconnectedOpInstMapping) {

		//data flows in parallel
		for (int k=0; k<op.getInDegree(); k++) {
			Operator childOp = op.getInput(k);
			Iterator<OperatorInstance> childOpInstIter =
				disconnectedOpInstMapping.get(childOp.getID()).iterator();
			while (childOpInstIter.hasNext()) {
				OperatorInstance childOpInst = childOpInstIter.next();
				Site site = childOpInst.getDeepestConfluenceSite();
				OperatorInstance opInst = new OperatorInstance(op,site);
				daf.addOpInst(op, opInst);
				daf.addEdge(childOpInst, opInst);
				disconnectedOpInstMapping.add(op.getID(), opInst);
			}
		}
	}

	private static void getOptimalOpInstAssignment(final Integer sink,
			final RT rt, OperatorInstanceTree daf, PAF paf, QoSSpec qoSSpec, int id) throws IOException {
		
		HashMap<String,String> replacements = 
			new HashMap<String,String>();
		
		//generate constraints relating to fabric
		generateSiteConstraints(sink, rt, replacements);
		//generate constraints relating to operator constraints
		generateOperatorInstanceConstraints(sink, daf, replacements);
		generateOperatorConstraints(paf, replacements);
		
		replacements.put("__PACKET_RX_ENERGY__", ""+TempRadioCosts.getReceiveCost());
		replacements.put("__OPTIMIZATION_GOAL__", qoSSpec.getOptimizationGoalStr());
		replacements.put("__HEURISTIC_INITIAL_POINT__", ""+Settings.WHERE_SCHEDULING_HEURISTIC_INITIAL_POINT);
		
//change this line to vary the number of neighbours		
		for (int nn=Settings.WHERE_SCHEDULING_MIN_NN; nn<=Settings.WHERE_SCHEDULING_MAX_NN; nn++) {
			String numNeighStr = new Integer(nn).toString();
			replacements.put("__NUM_NEIGHBOURS__", numNeighStr);
			//generate constraint file
			Template.instantiate(
					MATLAB_TEMPLATE_DIR + "/planning.m",
					MATLAB_WORKING_DIR + "/planning.m",
					replacements);

			long startTime = System.currentTimeMillis();
//uncomment this line to quit before the solver starts			
//			System.exit(0);
			
//Comment this line out to skip the solver. Use command-line setting 
//-delete-old-files=false to reuse the output from a previous solver
//invocation; note duration will be wrong though...
			invokeSolver(replacements, nn, id);
			
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime - startTime;
			
			HashMap<String,String> attrVal = new HashMap<String,String>();
			parseSolverOutput(rt, daf, attrVal, nn, id);
			
			appendToDataFile(attrVal, numNeighStr, elapsedTime);
			
	    	daf.display(QueryCompiler.queryPlanOutputDir, "DAF-"+id+"nn"+nn);
	       	DisplayGraph.displayGraph(MATLAB_WORKING_DIR + "/search-tree-nn"+nn+".dot");			
		}
	}


	private static void invokeSolver(HashMap<String, String> replacements, int nn, int id)
			throws IOException {
		
		String outputFileName = "out-nn"+nn+"-daf"+id+".txt";
		if (Settings.WHERE_SCHEDULING_SOLVER_OUTPUT_FILE==null) {
			//Start.m kickstarts the matlab script
			replacements.put("__MATLAB_TEMPLATE_DIR__", 
					System.getProperty("user.dir") + 
					File.separator + 
					MATLAB_TEMPLATE_DIR);
			replacements.put("__MATLAB_WORKING_DIR__", MATLAB_WORKING_DIR);
			Template.instantiate(
					MATLAB_TEMPLATE_DIR + "/start.m",
					"start.m",
					replacements);
			
			//delegate problem to solver
			Matlab.invokeMatlab("start.m", outputFileName);
			new File(outputFileName).renameTo(new File(MATLAB_WORKING_DIR 
					+ "/" + outputFileName));
		} else {
			Utils.copyFile(Settings.WHERE_SCHEDULING_SOLVER_OUTPUT_FILE, 
					MATLAB_WORKING_DIR + "/" + outputFileName);
			
		}
		
	}


	private static void appendToDataFile(HashMap<String, String> attrVal, String numNeigh, long elapsedTime) {

		String dataFile = MATLAB_WORKING_DIR + "/data.txt";
	    try {
			File f = new File(dataFile);
			if (!f.isFile()) {
				BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
				out.write("\"NN\"\t\"fn_val\"\t\"time\"\t\"num DAFs\"\n");
				out.write("0\t");
				out.write(attrVal.get("init_val")+"\t");
				out.write("0\t0\n");
				out.close();
			}

			BufferedWriter out = new BufferedWriter(new FileWriter(dataFile,true));
			out.write(numNeigh);
			out.write("\t");
			out.write(attrVal.get("min_f"));
			out.write("\t");
			out.write(Long.toString(elapsedTime));
			out.write("\t");
			out.write(attrVal.get("num DAFs considered"));
			out.write("\n");
			out.close();
	    } catch (IOException e) {
	    	System.err.println("An exception ocurred: "+e.getStackTrace());
	    }		
		
	}


	private static void parseSolverOutput(final RT rt,
			OperatorInstanceTree daf, HashMap<String, String> attrVal, int nn, int id) {
		//Parse solver output file
		//get solution(s) from solver
		try {
			final FileReader fin = new FileReader(MATLAB_WORKING_DIR 
					+ "/out-nn"+nn+"-daf"+id+".txt");
		    final BufferedReader in = new BufferedReader(fin);
		    String line;
		    boolean summaryFound = false;
		    boolean assignmentFound = false;
		    while ((line = in.readLine()) != null) {
		    	if (line.startsWith("*** Summary ***")) {
		    		summaryFound = true;
		    		continue;
		    	}
		    	if (line.startsWith("*** Assignment ***")) {
		    		summaryFound = false;
		    		assignmentFound = true;
		    		continue;
		    	}
		    	if (summaryFound && !line.trim().equals("")) {
		    		String lineArray[] = line.split("="); 
		    		String attr = lineArray[0];
		    		String val = lineArray[1];
		    		attrVal.put(attr, val);
		    	}
		    	if (assignmentFound && !line.trim().equals("")) {
		    		String lineArray[] = line.split("="); 
		    		String opInstId = lineArray[0];
		    		String siteId = lineArray[1];
		    		OperatorInstance opInst = (OperatorInstance)daf.getNode(opInstId);
		    		Site site = (Site)rt.getNode(siteId);
		    		daf.assign(opInst, site);
		    	}
		    }
		}  catch (final Exception e) {
		    Utils.handleCriticalException(e);
		}
	}

	private static void generateSiteConstraints(final Integer sink,
			final RT rt, HashMap<String, String> replacements) {
		StringBuffer sitesBuff = new StringBuffer();
    	StringBuffer memoryBuff = new StringBuffer();
    	StringBuffer energyBuff = new StringBuffer();
    	StringBuffer rtBuff = new StringBuffer();
    	StringBuffer parentSiteBuff = new StringBuffer();
    	StringBuffer siteChildrenBuff = new StringBuffer();
    	StringBuffer txEnergyCostBuff = new StringBuffer();
    	
    	Iterator<Site> siteIter = rt.siteIterator(TraversalOrder.POST_ORDER);
    	while (siteIter.hasNext()) {
    		Site site = siteIter.next();
    		String siteID = site.getID();
    		
    		
    		sitesBuff.append(siteID+" ");
    		
    		String mem = new Long(site.getRAM()).toString();
    		if (sink.toString().equals(siteID)) {
    			mem = "Inf";
    			
//this makes odd sites "dummy" nodes, i.e., they cannot evaluate fragments	
    		} else if (Settings.WHERE_SCHEDULING_HETEROGENEOUS_NETWORK && 
    				new Integer(siteID).intValue() / 2 != new Double(siteID).doubleValue() / 2) {
    			mem = "0"; //only acquires/relays can go on odd sites
    		}
    		
    		memoryBuff.append("memoryAvailable = put(memoryAvailable, "+
    				siteID+", "+mem+");\n");
    		
    		long energyStock = site.getEnergyStock();
    		energyBuff.append("energyAvailable = put(energyAvailable, "+
    				siteID+", "+energyStock+");\n");
    		
    		if (site.getOutDegree()>0) {
    			Site parent = (Site)site.getOutput(0);
    			String parentID = parent.getID();
    			rtBuff.append("add(rt,"+siteID+"+1,"+parentID+"+1);\n");
    			parentSiteBuff.append("parentSite = put(parentSite,"+
    					siteID+","+parentID+");\n");
    			
        		int energySetting = (int)rt.getLinkEnergyCost(site, parent);
    			double txEnergyCost = TempRadioCosts.getTransmitCost(energySetting);
    			txEnergyCostBuff.append("txEnergy = put(txEnergy, "+siteID+", "+
    					txEnergyCost+");\n");
    		}
    		
    		siteChildrenBuff.append("siteChildren=put(siteChildren,"+siteID+",{");
    		for (int i=0; i<site.getInDegree();i++) {
    			siteChildrenBuff.append(site.getChild(i).getID()+" ");
    		}
    		siteChildrenBuff.append("});\n");
       	}
    	replacements.put("__SITES__", sitesBuff.toString());
    	replacements.put("__MEMORY_AVAIL__", memoryBuff.toString());
    	replacements.put("__ENERGY_AVAIL__", energyBuff.toString());
    	replacements.put("__RT__", rtBuff.toString());
    	replacements.put("__PARENT_SITE__", parentSiteBuff.toString());
    	replacements.put("__SITE_CHILDREN__", siteChildrenBuff.toString());
    	replacements.put("__TX_ENERGY__", txEnergyCostBuff.toString());
	}

    private static void generateOperatorInstanceConstraints(int sink, OperatorInstanceTree oit,
    		HashMap<String,String> replacements) {
		StringBuffer opInstBuff = new StringBuffer();
		StringBuffer opInstOperatorBuff = new StringBuffer();
		StringBuffer locationConstraintBuff = new StringBuffer();
		StringBuffer outputSizeBuff = new StringBuffer();
		StringBuffer cardBuff = new StringBuffer();
		StringBuffer memoryCostBuff = new StringBuffer();
		StringBuffer opInstIndex = new StringBuffer();
		StringBuffer parentOpInstBuff = new StringBuffer();
		StringBuffer childrenOpInstBuff = new StringBuffer();
		StringBuffer deepestConfSiteBuff = new StringBuffer();
		StringBuffer assignmentBuff = new StringBuffer();
		StringBuffer energyCostBuff = new StringBuffer();
		
		int count = 1;
		int numDataReducingOpInst = 0;
		
		Iterator<OperatorInstance> opInstIter = oit.iterator(TraversalOrder.POST_ORDER);
		while (opInstIter.hasNext()) {
			OperatorInstance opInst = opInstIter.next();
			String opInstId = opInst.getID();
			opInstBuff.append("'"+opInstId+"' ");
			
			Operator op = opInst.getOp();
			opInstOperatorBuff.append("opInstOperator = put(opInstOperator, '"+
					opInstId+"', '"+op.getOperatorName()+op.getID()+"');\n");
			
//			System.out.println("card="+opInstId);
//			System.out.println("card="+op.getCardinality(CardinalityType.MAX));
//			System.out.println("phys tuple size="+op.getPhysicalTupleSize());
//			System.out.println("num sibling op instances="+oit.getNumOpInstances(op));
			
			//TODO: Ask Christian for Methods which will give me these three
			//NB: Operator.getOutputQueueCardinality will not give right result in the case of windows
			double outputSize;
			int card;
			int numInstances = oit.getNumOpInstances(op);
			if (op instanceof AggrIterateOperator) {
				card = 1;
			} else {
				card = op.getCardinality(CardinalityType.MAX) / numInstances;
			} 
			int tupleSize = op.getPhysicalTupleSize();
			outputSize = card * tupleSize;
			
/*			double childOutputSize = 0;
			for (int i=0; i<op.getInDegree(); i++) {
				Operator childOp = op.getInput(i);
				int childCard;
				int childNumInstances = oit.getNumOpInstances(childOp);
				if (childOp instanceof AggrIterateOperator) {
					childCard = 1;
				} else {
					childCard = childOp.getCardinality(CardinalityType.MAX) / childNumInstances;
				} 
				int childTupleSize = childOp.getPhysicalTupleSize();
				childOutputSize += childCard * childTupleSize;
			}
			
			if (outputSize<childOutputSize) {
				numDataReducingOpInst++;
			}*/
			if (op instanceof AggrIterateOperator || op instanceof AggrEvalOperator) {
				numDataReducingOpInst++;
			}
			
//			System.out.println("size of single op instance="+outputSize+"\n");
			outputSizeBuff.append("outputSize  = put(outputSize, '"+
					opInstId+"', "+outputSize+");\n");
			cardBuff.append("card = put(card, '"+
					opInstId+"', "+card+");\n");

			//get memory cost of single operator instance
			int memoryCost = op.getDataMemoryCost(numInstances);
//uncomment this to make Acquire operators zero; used for heterogeneous network experiments			
			if (Settings.WHERE_SCHEDULING_HETEROGENEOUS_NETWORK && op instanceof AcquireOperator) {
				memoryCost = 0;
			}
			memoryCostBuff.append("memoryCost = put(memoryCost, '"+
					opInstId + "', "+memoryCost+");\n");
			
			double energyCost = op.getEnergyCost(CardinalityType.MAX, numInstances); 
			energyCostBuff.append("energyCost = put(energyCost, '"+
					opInstId + "', "+energyCost+");\n");			
			
			opInstIndex.append("opInstanceIndex = put(opInstanceIndex, '"+
					opInstId+"', "+count+");\n");
			
			if (opInst.isLocationSensitive()) {
				String siteId = opInst.getDeepestConfluenceSite().getID();
				locationConstraintBuff.append(
						"locationConstraints = put(locationConstraints, '"+
						opInstId+"', "+siteId+");\n");
				assignmentBuff.append(siteId+" ");
			} else {
				assignmentBuff.append(sink+" ");
			}
			
			childrenOpInstBuff.append("opInstChildren = put(opInstChildren, '"+opInstId+"', {");
			for (int i=0; i<opInst.getInDegree(); i++) {
				OperatorInstance childOpInst = (OperatorInstance)opInst.getInput(i);
				String childOpInstId = childOpInst.getID();
				parentOpInstBuff.append("parentOpInst = put(parentOpInst, '"+
						childOpInstId+"', '"+opInstId+"');\n");
				childrenOpInstBuff.append("'"+childOpInstId+"' ");
			}
			childrenOpInstBuff.append("});\n");
			
			String siteId = opInst.getDeepestConfluenceSite().getID();
			deepestConfSiteBuff.append("opInstDeepestConfSite = put(opInstDeepestConfSite, '" + 
					opInstId+"', "+siteId+");\n");
			
			count++;
		}
		
		replacements.put("__OPERATOR_INSTANCES__", opInstBuff.toString());
		replacements.put("__OPINST_OPERATOR__", opInstOperatorBuff.toString());
		replacements.put("__LOCATION_CONSTRAINTS__", 
				locationConstraintBuff.toString());
		replacements.put("__OUTPUT_SIZE__", outputSizeBuff.toString());
		replacements.put("__CARDINALITY__", cardBuff.toString());
		replacements.put("__MEMORY_COST__", memoryCostBuff.toString());
		replacements.put("__ENERGY_COST__", energyCostBuff.toString());
		replacements.put("__OP_INST_INDEX__", opInstIndex.toString());
		replacements.put("__PARENTS__", parentOpInstBuff.toString());
		replacements.put("__CHILDREN__", childrenOpInstBuff.toString());
		replacements.put("__DEEPEST_CONFLUENCE_SITES__", deepestConfSiteBuff.toString());
		replacements.put("__ASSIGNMENT__", assignmentBuff.toString());
		replacements.put("__NUM_DATA_REDUCING_OP_INST__", ""+numDataReducingOpInst);
	}

    private static void generateOperatorConstraints(PAF paf,
    		HashMap<String,String> replacements) {
    	
		StringBuffer tuplesPerMessageBuff = new StringBuffer();
    	StringBuffer operatorIdBuff = new StringBuffer();
    	boolean first = true;
    	
    	Iterator<Operator> pafIter = paf.operatorIterator(TraversalOrder.POST_ORDER);
    	while (pafIter.hasNext()) {
    		Operator op = pafIter.next();
    		String opID = op.getOperatorName()+op.getID();
    		if (first) {
    			first = false;
    		} else {
    			operatorIdBuff.append(", ");
    		}
    		operatorIdBuff.append("'"+opID+"'");
    		
			int tuplesPerMessage;
			int tupleSize = op.getPhysicalTupleSize();
			tuplesPerMessage = ExchangePart.computeTuplesPerMessage(tupleSize);
			tuplesPerMessageBuff.append("tuplesPerMessage = put(tuplesPerMessage, '"+
					opID+"', "+tuplesPerMessage+");\n");
    	}
    	replacements.put("__OPERATORS__", operatorIdBuff.toString());
    	replacements.put("__TUPLES_PER_MESSAGE__", tuplesPerMessageBuff.toString());
    }
    

	private static void removeRedundantOpInstances(OperatorInstanceTree daf) throws OptimizationException {
		removeRedundantAggrIterOpInstances(daf);
		removeRedundantSiblingOpInstances(daf);
	}

	
	private static void removeRedundantAggrIterOpInstances(OperatorInstanceTree daf) throws OptimizationException {
		Iterator<OperatorInstance> opInstIter = daf.iterator(TraversalOrder.POST_ORDER);
		while (opInstIter.hasNext()) {
			OperatorInstance opInst = opInstIter.next();
			if (opInst.getOp() instanceof AggrIterateOperator || opInst.getOp() instanceof AggrEvalOperator) {
				for (int i=0; i<opInst.getInDegree(); i++) {
					OperatorInstance childOpInst = (OperatorInstance)opInst.getInput(i);
					Site opSite = opInst.getSite();
					Site childOpSite = childOpInst.getSite();
					if (childOpInst.getOp() instanceof AggrIterateOperator && opSite == childOpSite) {
						daf.removeOpInst(childOpInst);
					}
				}
			}
		}
		
	}

	private static void removeRedundantSiblingOpInstances(OperatorInstanceTree daf) throws OptimizationException {
		Iterator<OperatorInstance> opInstIter = daf.iterator(TraversalOrder.POST_ORDER);
		while (opInstIter.hasNext()) {
			OperatorInstance opInst = opInstIter.next();
			HashMapList<Site, OperatorInstance> siteOpInstMap = new HashMapList<Site, OperatorInstance>();
			
			for (int i=0; i<opInst.getInDegree(); i++) {
				OperatorInstance childOpInst = (OperatorInstance)opInst.getInput(i);
				Site site = childOpInst.getSite();
				siteOpInstMap.add(site, childOpInst);
			}
			
			Iterator<Site> siteIter = siteOpInstMap.keySet().iterator();
			while (siteIter.hasNext()) {
				Site site = siteIter.next();
				ArrayList<OperatorInstance> opInstColl = siteOpInstMap.get(site);
				daf.mergeSiblings(opInstColl);
			}
		}
	}

	
	/**
	 * Generate old-style DAF, for compatibility with when-scheduling.
	 * @param queryName
	 * @param paf
	 * @param rt
	 * @param daf
	 * @return
	 * @throws OptimizationException
	 */
	private static DAF generateCompactDAF(final String queryName,
			final PAF paf, final RT rt, OperatorInstanceTree daf)
			throws OptimizationException {
		FAF faf = partitionPAF(paf, daf, queryName);
		faf.display(QueryCompiler.queryPlanOutputDir,
				faf.getName(),
				"faf");
		
		DAF compactDaf = linkFragments(faf, rt, daf, queryName);
		compactDaf.display(QueryCompiler.queryPlanOutputDir,
				compactDaf.getName(),
				"cdaf");

		removeRedundantAggrIterOp(compactDaf);
		return compactDaf;
	}

	
    private static FAF partitionPAF(final PAF paf, OperatorInstanceTree oit, final String queryName) {
		FAF faf = new FAF(paf, queryName);
		
		//Get rid of unecessary aggrIter in FAF... (i.e., they have not been assigned to any site)
		Iterator<Operator> opIter = faf
		.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
 			final Operator op = (Operator) opIter.next();
 			HashSet<Site> opSites = oit.getSites(op);
 			if (opSites.size()==0) {
 				try {
					faf.removeNode(op);
				} catch (OptimizationException e) {
					e.printStackTrace();
				}
 			}
		}
		
		//Insert exchanges where necessary to partition the query plan
		opIter = faf.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
 			final Operator op = (Operator) opIter.next();
 			HashSet<Site> opSites = oit.getSites(op); 		
 			
 			if (op instanceof AggrIterateOperator) {
 				final Operator childOp = op.getInput(0);
					final ExchangeOperator exchOp = PhysicalOptimization
						.newExchangeOperator(childOp.getOperatorDataType());
						faf.insertNode(childOp, op, exchOp);				
 			} else {
 	 			for (int i=0; i<op.getInDegree(); i++) {
 	 				final Operator childOp = op.getInput(i);
 	 				
 	 				HashSet<Site> childSites = oit.getSites(childOp);
 	 				if (!opSites.equals(childSites)) {
 						final ExchangeOperator exchOp = PhysicalOptimization
 						.newExchangeOperator(childOp.getOperatorDataType());
 						faf.insertNode(childOp, op, exchOp);
 	 				}
 	 			}
 			}
 		}
 		
 		faf.buildFragmentTree();
 		return faf;
 	}

    
    /**
     * Given a new-style DAF and old-style FAF produces an old style DAF. Needed for backwards compatibility 
     * with when-scheduling, which still uses old-style DAFs.
     * @param faf
     * @param rt
     * @param daf
     * @param queryName
     * @return
     */
	private static DAF linkFragments(FAF faf, RT rt, OperatorInstanceTree daf,
			String queryName) {
		DAF compactDaf = new DAF(faf, rt, queryName);
		
		Iterator<OperatorInstance> opInstIter = daf.iterator(TraversalOrder.POST_ORDER);
		while (opInstIter.hasNext()) {
			OperatorInstance opInst = opInstIter.next();
			//have to get the cloned copy in compactDaf...
			Operator op = (Operator)compactDaf.getNode(opInst.getOp().getID());

			Site sourceSite = (Site)compactDaf.getRoutingTree().getNode(opInst.getSite().getID());
			Fragment sourceFrag = op.getContainingFragment();
			
			if (op.getOutDegree() > 0) {
				Operator parentOp = op.getParent();

				if (parentOp instanceof ExchangeOperator) {

					OperatorInstance paOpInst = (OperatorInstance)opInst.getOutput(0);
					Site destSite = (Site)compactDaf.getRoutingTree().getNode(paOpInst.getSite().getID());
					Fragment destFrag = ((Operator)compactDaf.getNode(((OperatorInstance)opInst.getOutput(0)).getOp().getID())).getContainingFragment();
					final Path path = compactDaf.getRoutingTree().getPath(sourceSite.getID(), destSite.getID());
					
					compactDaf.placeFragment(sourceFrag, sourceSite);
					compactDaf.linkFragments(sourceFrag, sourceSite, destFrag, destSite, path);
				}				
			} else {	
				compactDaf.placeFragment(sourceFrag, sourceSite);
			}
		}
		return compactDaf;
	}


	private static void removeRedundantAggrIterOp(DAF daf) throws OptimizationException {

		Iterator<Operator> opIter = daf.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
			Operator op = opIter.next();
			if (op instanceof AggrIterateOperator) {
				if (!(op.getParent() instanceof ExchangeOperator)) {
					daf.removeNode(op);
				}
			}
		}
	}
}
