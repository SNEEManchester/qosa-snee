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
package uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Triple;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.graph.Edge;
import uk.ac.manchester.cs.diasmc.common.graph.Graph;
import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.TupleTransmissionMethod;
import uk.ac.manchester.cs.diasmc.querycompiler.routing.FirstVHeuristic;
import uk.ac.manchester.cs.diasmc.querycompiler.routing.NextVHeuristic;

/**
 * 
 * @author Ixent Galpin and Christian Brenninkmeijer
 */

public class Topology extends Graph {

    static Logger logger = Logger.getLogger(Topology.class.getName());
    
    /** 	
     * Constructor.
     * @param name 		the name of the graph
     * @param directed 	whether the graph should have directed edges or not
     * 
     */
    public Topology(final String name, final boolean directed) {
	super(name, directed, false);
    }

    @Override
    public Site addNode(final String id) {
	final Site newNode = new Site(id);
	return (Site) super.addNode(newNode);
    }

    public Site getSite(final String id) {
	return (Site) this.getNode(id);
    }

    public Site getSite(final int id) {
	return (Site) this.getNode(id);
    }

    public RadioLink addRadioLink(final String id1, final String id2,
	    final boolean bidirectional, final double radioLossCost) {
    	final RadioLink link = (RadioLink) super.addEdge(id1, id2,
		bidirectional);
    	link.setRadioLossCost(radioLossCost);
    	return link;
    }

    @Override
    public Site nodeFactory(final String id) {
	return new Site(id);
    }

    @Override
    public RadioLink edgeFactory(final String id, final String sourceID,
	    final String destID) {
	return new RadioLink(id, sourceID, destID);
    }

    /*
     * Used by dijkstra's shortest path algorithm 
     */
    private String dijkstra_getNextClosestNodeID(
	    final HashSet<String> shortestDistanceNotFound,
	    final HashMap<String, Double> distance) {
	Double nextClosestDist = new Double(Double.POSITIVE_INFINITY);
	String nextClosestNodeID = null;
	final Iterator<String> j = shortestDistanceNotFound.iterator();
	while (j.hasNext()) {
	    final String jid = (String) j.next();
	    if ((distance.get(jid)).compareTo(nextClosestDist) < 0) {
		nextClosestDist = distance.get(jid);
		nextClosestNodeID = jid;
	    }
	}
	return nextClosestNodeID;
    }

    /**
     * Compute the shortest distance between two vertices using Dijkstra's algorithm
     * @param 	sourceID	The identifier of the source vertex.
     * @param 	destID		The identifier of the destination vertex. 
     * @throws DisconnectedTopologyException 
     * 
     */
    public Path getShortestPath(final String sourceID, 
    		final String destID,
    		final LinkCostMetric linkCostMetric,
    		RT rt, boolean optDynamicLinkCosts, HashMap<String,LinkCostMetric> hopCostMetricMap) throws DisconnectedTopologyException {
    	
	final HashMap<String, Double> distance = new HashMap<String, Double>();
	final HashMap<String, String> previous = new HashMap<String, String>();
	final HashSet<String> shortestDistanceFound = new HashSet<String>();
	final HashSet<String> shortestDistanceNotFound = new HashSet<String>();

	if (optDynamicLinkCosts) { //Option: dynamically update edge costs
		rt.updateNumSourceWeighting();
	}
		
	logger.finest("Computing shortest path from " + sourceID + " to "
		+ destID + " using cost metric " + linkCostMetric);
	final Iterator<String> i = this.nodes.keySet().iterator();
	while (i.hasNext()) {
	    final String vid = i.next();
	    distance.put(vid, new Double(Double.POSITIVE_INFINITY));
	    shortestDistanceNotFound.add(vid);
	}
	distance.put(sourceID, new Double(0));

	//find the next closest node
	while (!shortestDistanceNotFound.isEmpty()) {
		//System.err.println("adding vertex");
		
	    final String nextClosestVertexID = this
		    .dijkstra_getNextClosestNodeID(shortestDistanceNotFound,
			    distance);
	    if (nextClosestVertexID == null) {
		break;
	    }

	    shortestDistanceNotFound.remove(nextClosestVertexID);
	    shortestDistanceFound.add(nextClosestVertexID);

	    final Node[] nextClosestNodes = this.nodes.get(nextClosestVertexID)
		    .getOutputs();
	    for (Node element : nextClosestNodes) {
		final Site n = (Site) element;
		
		double edgeCost = getWeightedEdgeCost(linkCostMetric, rt,
				nextClosestVertexID, n.getID(), optDynamicLinkCosts,
				hopCostMetricMap);
		
		final double try_d = (distance.get(nextClosestVertexID))
			.doubleValue()
			+ edgeCost;
		final double current_d = (distance.get(n.getID()))
			.doubleValue();
		if (try_d < current_d) {
		    distance.put(n.getID(), new Double(try_d));
		    previous.put(n.getID(), nextClosestVertexID); //?
		}
	    }
	}

	final Path path = new Path((Site) this.nodes.get(destID));
	
	if (previous.containsKey(destID)) {
	    String siteID = destID;
	    while (previous.get(siteID) != null) {
	    	siteID = previous.get(siteID);
	    	path.prepend(this.getSite(siteID));
	    }
	    logger.finest("Shortest path: " + path.toString());
	} else if (!sourceID.equals(destID)) {
		String message = "No path from site " + sourceID + " to site " + destID + " found are not linked.";	
		logger.finest(message);
		throw new DisconnectedTopologyException(message);
	}
	return path;
    }

	public double getWeightedEdgeCost(final LinkCostMetric linkCostMetric,
			RT rt, final String sourceID, final String destID,
			boolean optDynamicLinkCosts, HashMap<String,LinkCostMetric> hopCostMetricMap) {
		String eid = this.generateEdgeID(sourceID, destID);
		final RadioLink e = (RadioLink) this.edges.get(eid);
		double edgeCost = e.getCost(linkCostMetric, hopCostMetricMap); 
		if (optDynamicLinkCosts) {
			//option: dynamically update edge costs
			//penalise edges with more traffic
			if (rt.edges.containsKey(eid)) {
				String sourceId = e.getSourceID();
				Site sourceNode = rt.getSite(sourceId);
				
				//edgeCost *= (sourceNode.getNumSources() + 1);
				edgeCost = Math.pow(edgeCost + 1, (sourceNode.getNumSources() + 1));
			}
		}
		return edgeCost;
	}

    /**
     * Returns a graph representing the Steiner tree of the current graph.
     * @param sink 		the root of the graph
     * @param sources 	the vertices which are required to be in the steiner 
     * 					tree (known as Steiner nodes)
     * This is a non-deterministic algorithm and the result given depends 
     * on the order that the vertices given in the sources array are added to 
     * the Steiner tree. 
     * Simple algorithm used taken from "Protocols and Architectures from 
     * Wireless Sensor Networks" by Holger Karl and Andreas Willig,
     * page 309. 
     * @throws DisconnectedTopologyException 
     */
    public RT steinerTree(final int sink, final int[] sources,
	    final String name, int randomSeed, LinkCostMetric linkCostMetric,
	    FirstVHeuristic optFirstVHeuristic, NextVHeuristic optNextVHeuristic, 
	    boolean optDynamicLinkCosts) throws DisconnectedTopologyException {
		final RT steinerTree = new RT(name);
		steinerTree.setDesc("Simple Steiner approximation; linkCostMetric="+linkCostMetric+"; firstVertex="+optFirstVHeuristic
				+"; nextVertex="+optNextVHeuristic+"; dynamicLinkCosts="+optDynamicLinkCosts);
		System.err.println(steinerTree.getDesc());
		
		final ArrayList<String> nodesToAdd = new ArrayList<String>();
		final ArrayList<String> nodesAdded = new ArrayList<String>();
	
		final Random random = init(sink, sources, randomSeed, nodesToAdd, optFirstVHeuristic, optNextVHeuristic, optDynamicLinkCosts);
	
	    //***Simple approximation algorithm
	    //select first vertex to add to tree
	    selectFirstV(sink, steinerTree, nodesToAdd, nodesAdded, random, optFirstVHeuristic);
	    
	    //Only used if a different cost metric is used for each hop
	    HashMap<String, LinkCostMetric> hopCostMetricMap = populateHopCostMetricMap(random);
	    
		while (nodesToAdd.size() > 0) {
	
			//Choose the cost metric to be used for this iteration
			LinkCostMetric iterationLinkCostMetric = linkCostMetric;
		    if (linkCostMetric==LinkCostMetric.RANDOM_PER_BRANCH) {
		    	iterationLinkCostMetric = LinkCostMetric.chooseOne(random);
		    }
		    
		    //Select the next vertex to add the to the tree (nextSteinerNode), and
		    //the vertex in the tree to connect it to (destNode)
		    Triple<String,String,Path> nextBranch = selectNextV(random, nodesToAdd, 
		    		nodesAdded, iterationLinkCostMetric, steinerTree, ""+sink, 
		    		optNextVHeuristic, optDynamicLinkCosts, hopCostMetricMap);
		    String nextSteinerNode = nextBranch.getFirst();
		    String destNode = nextBranch.getSecond();
		    Path shortestPath = nextBranch.getThird();
		    	
		    //connect the next steiner node to the tree
		    connectVertexToTree(steinerTree, nodesAdded, nextSteinerNode, destNode,
					shortestPath);
		    
		}
		
		Site sinkSite = steinerTree.getSite(sink);
		//needed in case the sink is not the root of the tree
		steinerTree.rotateTree(sinkSite, sinkSite);
		steinerTree.setRoot(sinkSite);
	return steinerTree;
    }

	private HashMap<String, LinkCostMetric> populateHopCostMetricMap(
			final Random random) {
		HashMap<String,LinkCostMetric> hopCostMetricMap = new HashMap<String,LinkCostMetric>(); 
	    Iterator<String> nodeIter = this.nodes.keySet().iterator();
	    while (nodeIter.hasNext()) {
	    	String nodeId = nodeIter.next();
	    	
	    	if (random.nextInt(100)<80) {
	    		hopCostMetricMap.put(nodeId,LinkCostMetric.ENERGY);
//	    		System.err.println("ENERGY");
	    	} else {
	    		hopCostMetricMap.put(nodeId,LinkCostMetric.LATENCY);
//	    		System.err.println("LATENCY");
	    	}
	    }
		return hopCostMetricMap;
	}

	private Random init(final int sink, final int[] sources, int randomSeed,
			final ArrayList<String> nodesToAdd, FirstVHeuristic optFirstVHeuristic,
			NextVHeuristic optNextVHeuristic, boolean optDynamicEdgeCosts) {
		//The Steiner nodes must be present in the tree (i.e., the sources and the sink)
		for (int element : sources) {
		    nodesToAdd.add(new Integer(element).toString());
		}
		nodesToAdd.add(new Integer(sink).toString());
	
		//Set random seed
		final Random random = new Random(randomSeed);
	
		//Set isSource flag to true in source nodes
	    Iterator<Node> siteIter = nodes.values().iterator();
	    while (siteIter.hasNext()) {
	    	Site s = (Site) siteIter.next();
	    	if (nodesToAdd.contains(s.getID())) {
	    		s.setIsSource(true);
	    	}
	    }
	    
	    if (optDynamicEdgeCosts && optFirstVHeuristic!=FirstVHeuristic.SINK) {
	    	Utils.handleCriticalException(new Exception("Steiner tree algorithm: DynamicEdgeCosts=true and FirstVHeuristic!=SINK"));
	    }
	    if (optNextVHeuristic==NextVHeuristic.CLOSEST_SINK && optFirstVHeuristic!=FirstVHeuristic.SINK) {
	    	Utils.handleCriticalException(new Exception("Steiner tree algorithm: optNextVHeuristic==CLOSEST_SINK && optFirstVHeuristic!=SINK"));
	    }
	    
		return random;
	}

    //connect the next steiner node to the tree
	private void connectVertexToTree(final RT steinerTree,
			final ArrayList<String> nodesAdded, String nextSteinerNode,
			String destNode, Path path) {
		//now traverse shortest path from the currentSource, adding edges to the steiner tree,
	    //until you find a node already in the steiner tree
	    String tmpPrev = nextSteinerNode;
	    String tmpCurrent = nextSteinerNode;
	    boolean foundFlag = false;

//	    System.err.println("connecting vertex "+nextSteinerNode+" to tree node "+destNode 
//	    		+ "via path "+path.toString());
	    
	    if (path.size()<1) {
	    	logger.finest("Empty path from "+nextSteinerNode+" to "+destNode);
	    }
	    
	    if (!steinerTree.nodes.containsKey(nextSteinerNode)) {
	    	Iterator<Site> pathIter = path.iterator();
	    	while (pathIter.hasNext() && !foundFlag) {
			    tmpPrev = tmpCurrent;
			    tmpCurrent = pathIter.next().getID();
	
			    if (steinerTree.nodes.containsKey(tmpCurrent)) {
			    	logger.finest("Found tmpCurrent="+tmpCurrent+" already in tree");
			    	foundFlag = true;
			    }

//			    System.err.println("About to add edge to steiner tree:" + tmpPrev + "-"
//					    + tmpCurrent);
			    
			    if (!tmpPrev.equals(tmpCurrent)) {
			    	String linkID = this.generateEdgeID(tmpPrev, tmpCurrent);
			    	RadioLink oldLink = (RadioLink) this.getEdge(linkID);
			    	steinerTree.addExternalSiteAndRadioLinkClone(
			    			this.getSite(tmpPrev), this.getSite(tmpCurrent), oldLink);
			    }	
	
//			    System.err.println("Added edge to steiner tree:" + tmpPrev + "-"
//				    + tmpCurrent);
	    	}
	    	if (foundFlag==false) {
	    		logger.finest("never found "+nextSteinerNode+" to "+destNode);
	    	}
		}

		nodesAdded.add(nextSteinerNode.toString());
	}

    private Triple<String,String,Path> selectNextV(Random random, ArrayList<String> nodesToAdd,
    		ArrayList<String> nodesAdded, LinkCostMetric iterationLinkCostMetric,
    		RT rt, String sinkId, NextVHeuristic optNextVHeuristic, boolean optDynamicLinkCosts,
    		HashMap<String,LinkCostMetric> hopCostMetricMap) throws DisconnectedTopologyException {
	    Triple<String,String,Path> nextBranch; 
	    
	    //Choose next Steiner node to add to tree
		String nextSteinerNode; //the node being added to the tree
		String destNode = null; //the node already in the tree
		Path minP=new Path(); //the path from nextSteinerNode to destNode
		
		if (optNextVHeuristic==NextVHeuristic.RANDOM) {
			//Option 1: Add a Steiner node at random
		    int randomPos = random.nextInt(nodesToAdd.size());
		    nextSteinerNode = nodesToAdd.get(randomPos);
		    destNode = nodesAdded.get(random.nextInt(nodesAdded.size()));
		    minP = this.getShortestPath(nextSteinerNode, destNode, iterationLinkCostMetric,
		    			rt, optDynamicLinkCosts, hopCostMetricMap);
		} else {
			//Option 2: Add the Steiner node closest to (any node in the tree|sink)
			//This option makes it slow, and too deterministic :(
			double minCost = Double.MAX_VALUE;
			nextSteinerNode = null; //nodesToAdd.get(0);
			Path p;
			
			//for each Steiner node waiting to be added...
			for (int i = 0; i<nodesToAdd.size(); i++) {
				String snId = nodesToAdd.get(i);

				if (optNextVHeuristic==NextVHeuristic.CLOSEST_ANY) {
					//Option 2a: Add the Steiner node closest to any node in the tree,
					//connect via path to that node in the tree
					
					//for each node already in the Steiner tree (including non-Steiner nodes)
					Iterator<String> stNodeIter = rt.nodes.keySet().iterator();
					while (stNodeIter.hasNext()) {
						String stNodeId = stNodeIter.next();
						p = this.getShortestPath(snId, stNodeId, iterationLinkCostMetric,
								rt, optDynamicLinkCosts, hopCostMetricMap);
						double cost = p.getCost(iterationLinkCostMetric, this, 
								rt, optDynamicLinkCosts, hopCostMetricMap);
						if (cost < minCost) {
							nextSteinerNode = nodesToAdd.get(i);
							destNode = stNodeId;
							minCost = cost;
							minP = p;
						}
					}					
				} else {
					//Option 2b: Add the Steiner node closest to the sink,
					//connect via path to the sink node in the tree
					//NB: This option requires sink node to be added first
					String stNodeId = sinkId;
					p = this.getShortestPath(snId, stNodeId, iterationLinkCostMetric,
							rt, optDynamicLinkCosts, hopCostMetricMap);
					double cost = p.getCost(iterationLinkCostMetric, this, rt, optDynamicLinkCosts, hopCostMetricMap);
					if (cost < minCost) {
						nextSteinerNode = nodesToAdd.get(i);
						destNode = stNodeId;
						minCost = cost;
						minP = p;
					}					
				}
			}

		} 
	    nodesToAdd.remove(nextSteinerNode);
	    nextBranch = new Triple<String,String,Path>(nextSteinerNode,destNode,minP);
	    return nextBranch;
    }
    	
	private void selectFirstV(final int sink, final RT steinerTree,
			final ArrayList<String> nodesToAdd,
			final ArrayList<String> nodesAdded, final Random random,
			FirstVHeuristic optFirstVHeuristic) {
		// Choose First Steiner node
		String firstSteinerNode;
		if (optFirstVHeuristic==FirstVHeuristic.SINK) {
			// Option 1: add sink first
			// This option avoids the sink from having a high degree, resulting in a more linear
			// tree. for lifetime/delivery it is desirable for the sink to have a high degree. 
			// This option also makes results more deterministic.
			firstSteinerNode = ""+sink;
			nodesToAdd.remove(firstSteinerNode);
		} else {
		    //Option 2: Add random node first
			//Note that this is incompatible with some other options
		    int randomPos = random.nextInt(nodesToAdd.size());
		    firstSteinerNode = nodesToAdd.get(randomPos);
		    nodesToAdd.remove(randomPos);
		}
		steinerTree.addExternalSiteClone(this.getSite(firstSteinerNode));
		nodesAdded.add(new Integer(firstSteinerNode).toString());
		
		if (optFirstVHeuristic==FirstVHeuristic.SINK) {
			Site sinkSite = steinerTree.getSite(sink);
			steinerTree.setRoot(sinkSite);
		}
	}

    /**
     * Exports the graph as a file in the DOT language used by GraphViz,
     * @see http://www.graphviz.org/
     * 
     * @param fname 	the name of the output file
     */
    @Override
    public void exportAsDOTFile(final String fname, final String label) {
	try {
	    final PrintWriter out = new PrintWriter(new BufferedWriter(
		    new FileWriter(fname)));

	    String edgeSymbol;
	    if (this instanceof RT) {
		out.println("digraph \"" + this.getName() + "\" {");
		edgeSymbol = "->";
	    } else {
		out.println("graph \"" + this.getName() + "\" {");
		edgeSymbol = "--";
	    }

	    out.println("size = \"8.5,11\";"); // do not exceed size A4
	    out.println("label = \"" + label + "\";");
	    out.println("rankdir=\"BT\";");

	    final Iterator j = this.nodes.keySet().iterator();
	    while (j.hasNext()) {
			final Site n = (Site) this.nodes.get(j.next());
			out.print(n.getID() + " [fontsize=20 ");
			
			if (n.isSource()) {
				out.print("shape = doublecircle ");
			}
				
			out.print("label = \"" + n.getID());
			if (Settings.DISPLAY_SITE_PROPERTIES) {
				out.print("\\nenergy stock: " + n.getEnergyStock() + "\\n");
				out.print("RAM: " + n.getRAM() + "\\n");
				out.print(n.getFragmentsString() + "\\n");
				out.print(n.getExchangeComponentsString());
			}
			
			out.println("\"];");
	    }

	    //traverse the edges now
	    final Iterator i = this.edges.keySet().iterator();
	    while (i.hasNext()) {
		final RadioLink e = (RadioLink) this.edges.get(i.next());
		
		//This prevents bi-directional links from being drawn twice and thus cluttering up
		//the topology graph
		if ((Integer.parseInt(e.getSourceID()) < Integer.parseInt(e.getDestID())) || (this instanceof RT)) {
			out.print("\"" + this.nodes.get(e.getSourceID()).getID() + "\""
					+ edgeSymbol + "\""
					+ this.nodes.get(e.getDestID()).getID() + "\" ");

				if (Settings.DISPLAY_SENSORNET_LINK_PROPERTIES) {
				    //TODO: find a more elegant way of rounding a double to 2 decimal places
				    out.print("[label = \"radio loss =" 
				    		+ (Math.round(e.getRadioLossCost() * 100))
				    		/ 100 + "\\n ");
				    out.print("energy = " + e.getEnergyCost() + "\\n");
				    out.print("latency = " + e.getLatencyCost() + "\"");
				} else {
				    out.print("[");
				}

				out.println("style = dashed]; ");

			    }
		}

	    out.println("}");
	    out.close();

	} catch (final IOException e) {
	    Utils.handleCriticalException(e);
	}
    }

    
    protected void cloneNodesAndEdges(Topology clone) {
    	// create shallow clones of each node in nodes collection
    	// don't link any nodes yet
    	Iterator<String> siteIDIter = this.nodes.keySet().iterator();
    	while (siteIDIter.hasNext()) {
    	    final String siteID = siteIDIter.next();
    	    final Site clonedSite = ((Site) this.nodes.get(siteID))
    		    .shallowClone();
    	    clone.addNode(clonedSite);
    	}

    	// now link all the nodes
    	// this relies on the id of nodes of the clone being the same as the sensor network graph
    	siteIDIter = clone.nodes.keySet().iterator();
    	while (siteIDIter.hasNext()) {
    	    final String siteID = siteIDIter.next();
    	    final Site site = (Site) this.nodes.get(siteID);
    	    final Site siteClone = (Site) clone.nodes.get(siteID);

    	    for (int i = 0; i < site.getInDegree(); i++) {
	    		final String childID = site.getInput(i).getID();
	    		final Site childClone = (Site) clone.nodes.get(childID);
	    		siteClone.setInput(childClone, i);
    	    }

    	    for (int i = 0; i < site.getOutDegree(); i++) {
	    		final String parentID = site.getOutput(i).getID();
	    		final Site parentClone = (Site) clone.nodes.get(parentID);
	    		siteClone.setOutput(parentClone, i);
    	    }
    	}

    	// alternatively update edges could be used...
    	clone.edges = (TreeMap<String, Edge>) this.edges.clone();
    }
    
    /**
     * Clones an existing graph.  The nodes and edges of the graph are also cloned.
     */
    @Override
    public Topology clone() {
    	final Topology clone = new Topology(this.name + "clone", this.directed);
		this.cloneNodesAndEdges(clone);
		return clone;
    }
    
    public RadioLink getRadioLink(final Site source, final Site dest) {
    	String id = this.generateEdgeID(
    			source.getID(), dest.getID());
    	RadioLink link = (RadioLink) this.getEdge(id);
    	return link;
    }
    
    public double getRadioLossCost(final Site source, final Site dest) {
    	RadioLink link = this.getRadioLink(source, dest);
    	return link.getRadioLossCost();
    }    
    
    public double getLinkLatencyCost(final Site source, final Site dest) {
    	RadioLink link = this.getRadioLink(source, dest);
    	return link.getLatencyCost();
    }    
    
    public double getLinkEnergyCost(final Site source, final Site dest) {
    	RadioLink link = this.getRadioLink(source, dest);
    	return link.getEnergyCost();
    }
    
    
    public void reverseEdgeDirection(Site source, Site dest) {
    	String eid = generateEdgeID(source.getID(), dest.getID());
    	RadioLink oldLink = (RadioLink)this.getEdge(eid);
    	double energyCost = oldLink.getEnergyCost();
    	double latencyCost = oldLink.getLatencyCost();
    	double radioLossCost = oldLink.getRadioLossCost();
    	
    	super.reverseEdgeDirection(source, dest);
    	
    	eid = generateEdgeID(dest.getID(), source.getID());
    	RadioLink newLink = (RadioLink)this.getEdge(eid);
    	newLink.setEnergyCost(energyCost);
    	newLink.setLatencyCost(latencyCost);
    	newLink.setRadioLossCost(radioLossCost);
    }
    
    /**
     * Test script.
     * @param args  Command-line args
     */
 /*   public static void main(final String[] args) {

	final Options opt = new Options(args, Options.Multiplicity.ZERO_OR_ONE);
	Settings.initialize(opt);

	//Graph 1
	final Topology ug = new Topology("test", true);
	TopologyReader.readTopologyNSSFile(
			ug, Settings.INPUTS_NETWORK_TOPOLOGY_FILE);
	ug.display(Settings.GENERAL_OUTPUT_ROOT_DIR, ug.getName());

	//Graph 2
	/*Topology ug2 = new Topology("test2",true);
	 TopologyReader.readTopologyNSSFile(ug2,"input/100-node-topology.nss");
	 System.out.println(ug2.getNumNodes());
	 ug2.display("output/output", ug2.getName());
	 
	 //Merge graphs 1 and 2
	 ug.mergeGraphs(ug2);
	 //ug.display();
	 */
	//Find the shortest path from dijkstraSource to dijkstraDest
/*	final Path p = ug.getShortestPath("0", "9", LinkCostMetric.RADIO_LOSS);
	System.out.println(p.toString());

	//Generate Steiner trees (non-determinstic): numResult 
	//possibilities are generated with dest and sources as steiner nodes
	for (int n = 0; n < 3; n++) {
		RT st = ug.steinerTree(3, new int[]{1, 2, 4, 5, 6, 8}, "steiner", 4, null);
		st.display(Settings.GENERAL_OUTPUT_ROOT_DIR, st.getName());
	}

    }*/
}
