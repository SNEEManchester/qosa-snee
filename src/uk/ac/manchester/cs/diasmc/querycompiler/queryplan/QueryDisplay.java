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
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.graph.EdgeImplementation;
import uk.ac.manchester.cs.diasmc.common.graph.Graph;
import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrInitializeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrIterateOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrInitializeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrIterateOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/**
 * Stripped down version of query plan purely fro displaying the logical Plan.
 * @author Ixent Galpin
 *
 */
public class QueryDisplay extends Graph {

    /** 
     *  Set of fragments in the query plan
     *  Populated during where scheduling
     *  
     */
    private final HashSet<Fragment> fragments = new HashSet<Fragment>();

    /**
     *  The root fragment in the query plan 
     *  Populated during where scheduling
     * 	 
     */
    private Fragment rootFragment;

    /**/
    static Logger logger = Logger.getLogger(QueryDisplay.class.getName());

    /**
     * Query plan constructor
     * @param root the root of the query plan tree built by the logical optimiser
     */
    public QueryDisplay(final Operator root, final String name) {
	super(name);
	Fragment.resetFragmentCounter();
	this.rootFragment = new Fragment();

	//hopefully we can get rid of this when christian builds op tree using addEdge
	this.updateEdges(root);

	this.fragments.add(this.rootFragment);
    }

    /**
     * Traverses the operator tree recursively to build collection of edges.
     * Must be invoked prior to exporting as DOT file or after any topological changes are made
     * to the query plan.
     * @param op	the current operator being visited
     */
    public final void updateEdges(final Node op) {
	this.nodes.put(op.getID(), op);

	// Post-order traversal of operator tree 
	if (!op.isLeaf()) {
	    for (int childIndex = 0; childIndex < op.getInDegree(); childIndex++) {
		final Node c = op.getInput(childIndex);

		this.updateEdges(c);
		//Edge e = new Edge(c.getID(),op.getID(),true);
		//edges.put(Edge.generateEdgeID(c.getID(), op.getID()),e);				
	    }
	}
    }

    /**
     * Exports the graph as a file in the DOT language used by GraphViz,
     * @see http://www.graphviz.org/
     * 
     * @param fname 	the name of the output file
     */
    @Override
	public final void exportAsDOTFile(final String fname) {
	try {
	    final PrintWriter out = new PrintWriter(new BufferedWriter(
		    new FileWriter(fname)));

	    String edgeSymbol;
	    if (this.isDirected()) {
		out.println("digraph \"" + this.getName() + "\" {");
		edgeSymbol = "->";
	    } else {
		out.println("graph \"" + this.getName() + "\" {");
		edgeSymbol = "--";
	    }

	    //query plan root at the top
	    out.println("rankdir=\"BT\"");

	    final Iterator<Fragment> f = this.fragments.iterator();
	    while (f.hasNext()) {
		final Fragment frag = f.next();
		out.println("subgraph cluster_" + frag.getID() + " {");
		out.println("style=\"rounded,dashed\"");
		out.println("color=red;");

		final Iterator<Operator> o = frag.getOperators().iterator();
		while (o.hasNext()) {
		    final Node op = o.next();
		    //out.print("\"" + op.getID()+ "\" [fontsize=9 label = \""+
		    //op.getAttribute("type")+"\\n"+op.getAttribute("params")+"\" ");
		    //out.println("]; ");
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

		out.println("fontsize=9;");
		out.println("fontcolor=red;");
		out.println("labelloc=t;");
		out.println("labeljust=r;");
		out.println("label =\"Fragment " + frag.getID() + "\\n {"
			+ sites + "}\";");
		out.println("}");

		//draw recursive loop
		if (frag.containsOperatorType(AggrInitializeOperator.class)
			|| frag.containsOperatorType(AggrIterateOperator.class)) {
		    final Operator op = frag.getParentExchangeOperator()
			    .getInput(0);
		    out
			    .println(frag.getParentExchangeOperator().getID()
				    + " -> " + op.getID()
				    + " [headport=s tailport=n];");
		}

	    }

	    final Iterator j = this.nodes.keySet().iterator();
	    logger.finest("size =" + this.nodes.size());
	    while (j.hasNext()) {
		final Node op = this.nodes.get(j.next());
		out.print("\"" + op.getID() + "\" [fontsize=9 ");
		if (op instanceof ExchangeOperator) {
		    out.print("fontcolor=blue ");
		    //out.print("label = \""+op.getAttribute("type")+"\\n");

		    if (Settings.DISPLAY_EXCHANGE_OPERATOR_ROUTING) {
			out
				.print(((ExchangeOperator) op)
					.getDOTRoutingString());
		    }
		} else {
		    //out.print("label = \""+op.getAttribute("type")+"\\n");
		    //out.print(op.getAttribute("params"));
		}
		out.println("\"]; ");
	    }

	    //traverse the edges now

	    final Iterator i = this.edges.keySet().iterator();
	    while (i.hasNext()) {
		final EdgeImplementation e = (EdgeImplementation) this.edges
			.get(i.next());
		logger.finest("SourceId = " + e.getSourceID());
		final Operator sourceNode = (Operator) this.nodes.get(e
			.getSourceID());

		out.print("\"" + e.getSourceID() + "\"" + edgeSymbol + "\""
			+ e.getDestID() + "\" ");

		if (Settings.DISPLAY_OPERATOR_PROPERTIES) {
		    logger.finest("Source = " + sourceNode);
		    if (sourceNode == null) {
			out
				.println("[fontsize=9 label = \" card: ERROR \\n partAttr: ERROR \"];");
		    } else {
			final String cardinality = new Long(sourceNode
				.getCardinality(CardinalityType.MAX)).toString();
			final String partAttr = sourceNode
				.getPartitioningAttribute();
			out.println("[fontsize=9 label = \" card: "
				+ cardinality + "\\n partAttr: " + partAttr
				+ " \"];");
		    }
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
}
