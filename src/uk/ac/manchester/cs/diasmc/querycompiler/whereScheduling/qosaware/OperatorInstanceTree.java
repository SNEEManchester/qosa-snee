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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.common.HashMapList;
import uk.ac.manchester.cs.diasmc.common.graph.Edge;
import uk.ac.manchester.cs.diasmc.common.graph.Graph;
import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

public class OperatorInstanceTree extends Graph {

	RT rt;
	
	PAF paf;
	
	OperatorInstance root;
	
	private HashMapList<String,OperatorInstance> opInstMapping =
		new HashMapList<String,OperatorInstance>();
	
	private HashMapList<Site,OperatorInstance> siteToOpInstMap =
		new HashMapList<Site,OperatorInstance>();
	
	
	public OperatorInstanceTree(RT rt, PAF paf) {
		this.rt = rt;
		this.paf = paf;
	}
	
	public void setRoot(OperatorInstance root) {
		this.root = root;
	}
	
	
	public OperatorInstance getRoot() {
		return this.root;
	}

    /**
     * Helper method to recursively generate the operator iterator.
     * @param node the operator being visited
     * @param siteList the operator list being created
     * @param traversalOrder the traversal order desired 
     */    
    private void doIterator(final OperatorInstance node,
    	    final ArrayList<OperatorInstance> nodeList, 
    	    final TraversalOrder traversalOrder) {

    	if (traversalOrder == TraversalOrder.PRE_ORDER) {
    	    nodeList.add(node);
    	}

    	for (int n = 0; n < node.getInDegree(); n++) {
    	    this.doIterator((OperatorInstance)node.getInput(n), nodeList, traversalOrder);
    	}

    	if (traversalOrder == TraversalOrder.POST_ORDER) {
    	    nodeList.add(node);
    	}
    }

    /**
     * Iterator to traverse the routing tree.
     * The structure of the routing tree may not be modified during iteration
     * @param traversalOrder the order to traverse the routing tree
     * @return an iterator for the routing tree
     */    
    public final Iterator<OperatorInstance> iterator(
    		final TraversalOrder traversalOrder) {

    	final ArrayList<OperatorInstance> nodeList = 
    		new ArrayList<OperatorInstance>();
    	this.doIterator(this.getRoot(), nodeList,
    		traversalOrder);

    	return nodeList.iterator();
    }

    
    public void exportAsDOTFile(final String fname, final String label) {
		try { 	
		    PrintWriter out = new PrintWriter(new BufferedWriter(
				    new FileWriter(fname)));
		    out.println("digraph \"" + (String) this.getName() + "\" {");
		    String edgeSymbol = "->";
		    
		    out.println("label = \"" + label + "\";");
		    out.println("rankdir=\"BT\";");
		    
	       /**
		     * Draw the operators on the site
		     */
		    final Iterator<Site> siteIter = this.rt.siteIterator(TraversalOrder.POST_ORDER);
		    while (siteIter.hasNext()) {
				final Site site = siteIter.next();
				ArrayList<OperatorInstance> opInstSubTree = this.getOpInstances(site);
				if (!opInstSubTree.isEmpty()) {
					out.println("subgraph cluster_" + site.getID() + " {");
					out.println("style=\"rounded,dotted\"");
					out.println("color=blue;");
			
					final Iterator<OperatorInstance> opInstIter 
						= opInstSubTree.iterator();
					while (opInstIter.hasNext()) {
					    final OperatorInstance opInst = opInstIter.next();
					    out.println("\"" + opInst.getID() + "\" ;");
					}
			
					out.println("fontsize=9;");
					out.println("fontcolor=red;");
					out.println("labelloc=t;");
					out.println("labeljust=r;");
					out.println("label =\"Site " + site.getID()+"\"");
					out.println("}\n");
				}
		    }
		    
		    //traverse the edges now
		    Iterator<String> i = edges.keySet().iterator();
		    while (i.hasNext()) {
			Edge e = edges.get((String) i.next());
			out.println("\"" + this.nodes.get(e.getSourceID()).getID()
				+ "\"" + edgeSymbol + "\""
				+ this.nodes.get(e.getDestID()).getID() + "\" ");
		    }
		    out.println("}");
		    out.close();
		} catch (IOException e) {
		    System.err.println("Export failed: " + e.toString());
		}
    }

    
	private ArrayList<OperatorInstance> getOpInstances(Site site) {
		return this.siteToOpInstMap.get(site);
	}
	
	public void addOpInst(Operator op, OperatorInstance opInst) {
		super.addNode(opInst);
		this.opInstMapping.add(op.getID(), opInst);
	}
	
	public ArrayList<OperatorInstance> getOpInstances(Operator op) {
		return this.opInstMapping.get(op.getID()); 
	}
	
	public void setOpInstances(Operator op, Collection<OperatorInstance> opInstColl) {
		this.opInstMapping.set(op.getID(), opInstColl);
	}
	
	public int getNumOpInstances(Operator op) {
		return this.getOpInstances(op).size();
	}
	
	public void assign(OperatorInstance opInst, Site site) {  
		opInst.setSite(site);
		this.siteToOpInstMap.add(site, opInst);
	}

	public HashSet<Site> getSites(Operator op) {
		ArrayList<OperatorInstance> opInstances = this.getOpInstances(op); 
		HashSet<Site> sites = new HashSet<Site>();
		for (int i=0; i<opInstances.size(); i++) {
			OperatorInstance opInst = opInstances.get(i);
			sites.add(opInst.getSite());
		}
		return sites;
	}
	
    public void removeOpInst(OperatorInstance opInst) throws OptimizationException {
    	Node[] inputs = opInst.getInputs();
    	Node[] outputs = opInst.getOutputs();
    	if (outputs.length != 1)
    	    throw new OptimizationException("Unable to remove node " + opInst.getID()
    		    + " as it does not have exactly one output");
    	
    	for (int i=0; i<inputs.length; i++) {
        	inputs[i].replaceOutput(opInst, outputs[0]);    		
    	}
    	
    	outputs[0].replaceInput(opInst, inputs[0]);
    	addEdge(inputs[0], outputs[0]);
    	
    	for (int i=1; i<inputs.length; i++) {
    		outputs[0].addInput(inputs[i]);
    		addEdge(inputs[i], outputs[0]);
    	}

    	removeNode(opInst.getID());
    	siteToOpInstMap.remove(opInst.getSite(), opInst);
    }

	public void mergeSiblings(ArrayList<OperatorInstance> siblings) throws OptimizationException {
		
		//empty check
		if (siblings.isEmpty()) {
			return;
		}
		
		//siblings check
		OperatorInstance firstParent = (OperatorInstance)siblings.get(0).getOutput(0);
		Site firstSite = siblings.get(0).getSite();
		for (int i=1; i<siblings.size(); i++) {
			OperatorInstance currentParent = (OperatorInstance)siblings.get(i).getOutput(0);
			if (currentParent!=firstParent) {
				throw new OptimizationException("Not all operator instances provided are siblings");
			}
			Site currentSite = siblings.get(i).getSite();
			if (currentSite!=firstSite) {
				throw new OptimizationException("Not all operator instances provided are on the same site");
			}
		}
		
		//merge siblings into the first...
		OperatorInstance firstSibling = siblings.get(0);
		for (int i=1; i<siblings.size(); i++) {
			OperatorInstance currentSibling =  siblings.get(i);
			for (int j=0; j<currentSibling.getInDegree(); j++) {
				OperatorInstance siblingChild = (OperatorInstance)currentSibling.getInput(j); 
				firstSibling.addInput(siblingChild);
//				siblingChild.replaceOutput(currentSibling, firstSibling);
				siblingChild.addOutput(firstSibling);
				this.addEdge(siblingChild, firstSibling);
			}
			removeNode(currentSibling.getID());
			siteToOpInstMap.remove(currentSibling.getSite(), currentSibling);	
		}
	}
}
