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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.graph.Edge;
import uk.ac.manchester.cs.diasmc.common.graph.EdgeImplementation;
import uk.ac.manchester.cs.diasmc.common.graph.Graph;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.WindowOperator;

public class LAF extends Graph {

	/**
	 * The root of the operator tree.
	 */
	protected Operator rootOp;

    /**
     *  Set of leaf operators in the query plan.
     */
    private HashSet<Operator> leafOperators = 
    	new HashSet<Operator>();
	
    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger(LAF.class.getName());
    
    /**
     * Counter used to assign unique id to different candidates.
     */
    protected static int candidateCount = 0;
    
	/**
	 * Implicit constructor used by subclass.
	 */
	protected LAF() { }    
	
    /** Acquistion interval of the whole query. (Alpha)*/
    private double acInt;
    
	/**
	 * Main construction used by logical optimizer.
	 * @param inRootOp The root operator of the logical query plan
	 * @param queryName The name of the query
	 * @param acquisitionInterval Acquisition interval of the whole query.
	 *  (Alpha)
	 */
    public LAF(final Operator inRootOp, final String queryName,
    		final long acquisitionInterval) {
    	this.name = generateName(queryName);
		this.rootOp = inRootOp;
		this.updateNodesAndEdgesColls(this.rootOp);
		this.setAcquisitionInterval(acquisitionInterval);
	}

    /**
     * Constructor used by clone.
     * @param laf The LAF to be cloned
     * @param inName The name to be assigned to the new data structure.
     */
    public LAF(final LAF laf, final String inName) {
    	super(laf, inName);
    	
    	rootOp = (Operator) nodes.get(laf.rootOp.getID());
    	
    	Iterator<Operator> opIter = laf.leafOperators.iterator();
    	while (opIter.hasNext()) {
    		String opID = opIter.next().getID();
    		this.leafOperators.add((Operator) nodes.get(opID));
    	}
    	
    }
    
    /**
     * Resets the candidate counter; use prior to compiling the next query.
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
    	return queryName + "-LAF-" + candidateCount;
    }
    
    /**
     * Updates the nodes and edges collections according to the tree 
     * passed to it.
     * @param op The current operator being processed
     */
    public final void updateNodesAndEdgesColls(final Operator op) {
    	this.nodes.put(op.getID(), op);

    	/* Post-order traversal of operator tree */
    	if (!op.isLeaf()) {
    		for (int childIndex = 0; childIndex < op.getInDegree(); 
    				childIndex++) {
	    		final Operator c = (Operator) op.getInput(childIndex);
	
	    		this.updateNodesAndEdgesColls(c);
	    		final EdgeImplementation e = new EdgeImplementation(this
	    			.generateEdgeID(c.getID(), op.getID()), c.getID(), op
	    			.getID());
	    		this.edges.put(this.generateEdgeID(c.getID(), op.getID()), e);
    	    }
    	}
    }
    
	/**
	 * Returns the root operator in the tree.
	 * @return the root operator.
	 */
	public final Operator getRootOperator() {
		return this.rootOp;
	}

	/**
	 * Replaces an operator with another in the routing tree, e.g., 
	 * used during algorithm selection.
	 * @param oldOp the operator to be removed
	 * @param newOp the operator to take its place
	 */
    public final void replaceNode(final Operator oldOp,
    	    final Operator newOp) {
    	super.replaceNode(oldOp, newOp);
    	if (oldOp == this.rootOp) {
    	    this.rootOp = newOp;
    	}
    }	
	
    /**
     * Helper method to recursively generate the operator iterator.
     * @param op the operator being visited
     * @param opList the operator list being created
     * @param traversalOrder the traversal order desired 
     */
    private void doOperatorIterator(final Operator op,
	    final ArrayList<Operator> opList, 
	    final TraversalOrder traversalOrder) {

		if (traversalOrder == TraversalOrder.PRE_ORDER) {
		    opList.add(op);
		}
	
		for (int n = 0; n < op.getInDegree(); n++) {
		    this.doOperatorIterator(op.getInput(n), opList, traversalOrder);
		}
	
		if (traversalOrder == TraversalOrder.POST_ORDER) {
		    opList.add(op);
		}
    }	
	
    /**
     * Iterator to traverse the operator tree.
     * The structure of the operator tree may not be modified during iteration
     * @param traversalOrder the order to traverse the operator tree
     * @return an iterator for the operator tree
     */
    public final Iterator<Operator> operatorIterator(
    		final TraversalOrder traversalOrder) {

		final ArrayList<Operator> opList = 
			new ArrayList<Operator>();
		this.doOperatorIterator(this.getRootOperator(), opList, traversalOrder);
	
		return opList.iterator();
    }
   
    /**
     * Sets the acquisition interval for the Plan 
     * 		and the operators where required.
     * @param acquisitionInterval Acquistion interval of the whole query. 
     * (Alpha)
     */
    public final void setAcquisitionInterval(final double acquisitionInterval) {
    	this.acInt = acquisitionInterval;
    	Iterator<Operator> opIter = operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
		    if (op instanceof WindowOperator) {
		    	((WindowOperator) op).setAcquisitionInterval(acInt);
		    }
		}
    }

    /**
     * Gets the acquisition interval for the Plan 
     * 		and the operators where required.
     * @return acquisitionInterval Acquistion interval of the whole query. 
     * (Alpha)
     */
    public final double getAcquisitionInterval() {
    	return this.acInt;
    }

    public final void exportAsDOTFile(final String fname) {
    	exportAsDOTFile(fname, new TreeMap<String, StringBuffer>(), 
    			new TreeMap<String, StringBuffer>(), new StringBuffer());
    }

    public final void exportAsDOTFile(final String fname, final String label) {
    	exportAsDOTFile(fname, new TreeMap<String, StringBuffer>(), 
    			new TreeMap<String, StringBuffer>(), new StringBuffer());
    }

    protected void exportAsDOTFile(final String fname,
			TreeMap<String, StringBuffer> opLabelBuff,
			TreeMap<String, StringBuffer> edgeLabelBuff,
			StringBuffer fragmentsBuff) {
    	exportAsDOTFile(fname, "", new TreeMap<String, StringBuffer>(), 
    			new TreeMap<String, StringBuffer>(), new StringBuffer());
    }
    
    /**
     * Exports the graph as a file in the DOT language used by GraphViz.
     * @see http://www.graphviz.org/
     *
     * @param fname 	the name of the output file
     */
    protected void exportAsDOTFile(final String fname,
    			final String label,
    			TreeMap<String, StringBuffer> opLabelBuff,
    			TreeMap<String, StringBuffer> edgeLabelBuff,
    			StringBuffer fragmentsBuff) {
    	
		try {
		    final PrintWriter out = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname)));
	
		    out.println("digraph \"" + (String) this.getName() + "\" {");
		    final String edgeSymbol = "->";
	
		    //query plan root at the top
		    out.println("size = \"8.5,11\";"); // do not exceed size A4
		    out.println("rankdir=\"BT\";");
		    out.println("label=\"" + this.getProvenanceString() 
		    		+ label + "\";");

		    //Draw fragments info; will be empty for LAF and PAF
		    out.println(fragmentsBuff.toString());
	
		    /**
		     * Draw the nodes, and their properties
		     */
		    final Iterator j = this.nodes.keySet().iterator();
		    while (j.hasNext()) {
				final Operator op = (Operator) this.nodes
					.get((String) j.next());
				out.print("\"" + op.getID() + "\" [fontsize=9 ");
	
				if (op instanceof ExchangeOperator) {
					out.print("fontcolor = blue ");
				}
				
			    out.print("label = \"");
			    if (Settings.DISPLAY_OPERATOR_DATA_TYPE) {
					out.print("(" + op.getOperatorDataType().toString()
						+ ") ");
			    }
			    out.print(op.getOperatorName() + "\\n");
			    
			    if (op.getParamStr() != null) {
			    	out.print(op.getParamStr() + "\\n");
			    }
			    
			    if (Settings.DISPLAY_OPERATOR_PROPERTIES) {
			    	out.print("id = " + op.getID() + "\\n");
			    }
			    	
		    	//print subclass attributes
			    if (opLabelBuff.get(op.getID()) != null) {
			    	out.print(opLabelBuff.get(op.getID())); 
			    }
				out.println("\" ]; ");
		    }
	
		    /**
		     * Draw the edges, and their properties
		     */
		    final Iterator i = this.edges.keySet().iterator();
		    while (i.hasNext()) {
				final Edge e = this.edges.get((String) i.next());
				final Operator sourceNode = (Operator) this.nodes
					.get(e.getSourceID());
		
				out.print("\"" + e.getSourceID() + "\"" + edgeSymbol + "\""
					+ e.getDestID() + "\" ");
		
				if (Settings.DISPLAY_OPERATOR_PROPERTIES) {
					out.print("[fontsize=9 label = \" ");

					out.print("type: " + sourceNode.getTupleAttributesStr(3)
							+ " \\n");

			    	//print subclass attributes
				    if (edgeLabelBuff.get(e.getID()) != null) {
				    	out.print(edgeLabelBuff.get(e.getID()));
				    }
					
					out.print("\"];\n");
				} else {
				    out.println(";");
				}
		    }
	
		    out.println("}");
		    out.close();
		} catch (final IOException e) {
		    logger.severe("Export failed: " + e.toString());
		}

	}

	public String getProvenanceString() {
		return this.name;
	}
}
