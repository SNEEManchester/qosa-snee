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

import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.graph.Edge;
import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/**
 * The fragmented-algebraic form (FAF) of the query plan operator tree. 
 * @author galpini
 *
 */
public class PAF extends LAF {

	/**
	 * The logical-algebraic form of the query plan operator tree from which
	 * PAF is derived.
	 */
	private LAF laf;
		
    /**
     * Logger for this class.
     */
    private static  Logger logger = Logger.getLogger(PAF.class.getName());
	
    /**
     * Counter to assign unique id to different candidates.
     */
    protected static int candidateCount = 0;
    
	/**
	 * Implicit constructor used by subclass.
	 */
	protected PAF() { }

	//TODO: put this in logical optimization
	//this.laf = new LAF(logicalTreeRoot);		
	
	
	/**
	 * Constructor for Physical-algebraic form.
	 * @param inLAF The logical-algebraic form of the query plan operator tree 
	 * from which PAF is derived.
	 * @param queryName The name of the query
	 */
	public PAF(final LAF inLAF, final String queryName) {
		super(inLAF, generateName(queryName));
		this.laf = inLAF;
	}
	
    /**
     * Constructor used by clone.
     * @param paf The PAF to be cloned
     * @param inName The name to be assigned to the data structure
     */
	public PAF(final PAF paf, final String inName) {
		super(paf, inName);
		
		this.laf = paf.laf; //This is ok because the laf is immutable now
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
    	return queryName + "-PAF-" + candidateCount;
    }
	
    protected void exportAsDOTFile(final String fname, 
			TreeMap<String, StringBuffer> opLabelBuff,
			TreeMap<String, StringBuffer> edgeLabelBuff,
			StringBuffer fragmentsBuff) {
    	
	    final Iterator i = this.edges.keySet().iterator();
	    while (i.hasNext()) {
			final Edge e = this.edges.get((String) i.next());
			final Operator sourceNode = (Operator) this.nodes
				.get(e.getSourceID());
			    
			StringBuffer strBuff = new StringBuffer();
			if (edgeLabelBuff.containsKey(e.getID())) {
				strBuff = edgeLabelBuff.get(e.getID());
			}
			strBuff.append("Maximum cardinality: " 
					+ ((Operator) sourceNode).
					getCardinality(CardinalityType.PHYSICAL_MAX)	+ " \\n");
			
			edgeLabelBuff.put(e.getID(), strBuff);  

	    }
	    super.exportAsDOTFile(fname, "", opLabelBuff, edgeLabelBuff, 
	    			fragmentsBuff);
    }
	
    public void replace(final Node oldNode, final Node newNode) {
		final Node[] inputs = oldNode.getInputs();
		for (final Node n : inputs) {
		    n.replaceOutput(oldNode, newNode);
		    newNode.addInput(n);
		}
		final Node[] outputs = oldNode.getOutputs();
		for (final Node n : outputs) {
		    n.replaceInput(oldNode, newNode);
		    newNode.addOutput(n);
		}
		if (this.rootOp == oldNode) {
			this.rootOp = (Operator) newNode;
		}
		
		nodes.remove(oldNode.getID());
		nodes.put(newNode.getID(), newNode);
    }
    
	public String getProvenanceString() {
		return this.laf.getProvenanceString() + "->" + this.name;
	}
    
}
