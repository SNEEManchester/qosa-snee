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
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/**
 * Class to represent a query plan.
 * @author Ixent Galpin
 *
 */
public class QueryPlan {

	/**
	 * The name of the query.
	 */
	private String name;
	
	/**
	 * The query plan operator tree in Distributed-algebraic form.
	 */
	private DAF daf;
	
	/**
	 * The query plan routng tree.
	 */
	private RT rt;	
	
	/**
	 * The query plan agenda.
	 */
	private Agenda agenda;
    
    /**
     * Logger for this class.
     */
    private static Logger logger = 
    	Logger.getLogger(QueryPlan.class.getName());


    /**
     * Constructor.
     * @param inName The name of the query.
     * @param inDAF The query operator tree in Distributed-Algebraic form.
     * @param inAgenda The Agenda to be coupled with the DAF.
     */
    public QueryPlan(final String inName, 
    		final DAF inDAF, 
    		final Agenda inAgenda) {
    	this.name = inName;
    	this.daf = inDAF;
    	this.rt = daf.getRoutingTree();
    	this.agenda = inAgenda;
    }

    /**
     * Returns the query plan name.
     * @return the name of the query plan
     */
    public final String getName() {
    	return this.name;
    }
    
    /**
     * Getter method for distributed-algebraic form of the query plan 
     * operator tree.
     * @return Distributed-algebraic form 
     */
    public final DAF getDAF() {
    	return this.daf;
    }

    /**
     * Getter method for the routing tree used by the query plan.
     * @return The routing tree
     */    
    public final RT getRoutingTree() {
    	return this.rt;
    }

    /**
     * Getter method for the agenda used by the query plan.
     * @return The agenda
     */     
    public final Agenda getAgenda() {
    	return this.agenda;
    }
    
    /**
     * Getter method for the buffering factor determined for the agenda.
     * @return the buffering factor
     */
    public final long getBufferingFactor() {
    	return this.getAgenda().getBufferingFactor();
    }

    /**
     * Returns the acquistion interval determined by the query plan.
     * Currently, this is assumed to be the same for all streams.
     * @return the time, in milliseconds, between acquisitions.
     */
    public final long getAcquisitionInterval_ms() {
	    return this.agenda.getAcquisitionInterval_ms();
    }    
    
    /**
     * Iterator to traverse the operator tree.
     * The structure of the operator tree may not be modified during iteration
     * @param traversalOrder the order to traverse the operator tree
     * @return an iterator for the operator tree
     */
    public final Iterator<Operator> operatorIterator(
    		final TraversalOrder traversalOrder) {
    	return daf.operatorIterator(traversalOrder);
    }

    /**
     * Iterator to traverse the operator tree.
     * The structure of the operator tree may not be modified during iteration
     * @param traversalOrder the order to traverse the operator tree
     * @return an iterator for the operator tree
     */    
    public final Iterator<Fragment> fragmentIterator(
    		final TraversalOrder traversalOrder) {
    	return daf.fragmentIterator(traversalOrder);
    }
    
    /**
     * Iterator to traverse the routing tree.
     * The structure of the routing tree may not be modified during iteration
     * @param traversalOrder the order to traverse the routing tree
     * @return an iterator for the routing tree
     */    
    public final Iterator<Site> siteIterator(
    		final TraversalOrder traversalOrder) {
    	return rt.siteIterator(traversalOrder);
    }
    
    /**
     * Produces a text file containing a summary of the main query plan 
     * characteristics (e.g, the agenda duration, or the buffering factor 
     * used).
     */
    public final void produceQueryPlanSummary() {

		final String fname = QueryCompiler.queryPlanOutputDir
			+ "query-plan-summary.txt";
	
		final StringBuffer routingTreeStr = new StringBuffer();
		Iterator<Site> siteIter = 
			this.rt.siteIterator(TraversalOrder.PRE_ORDER);
		boolean first = true;
		while (siteIter.hasNext()) {
		    final Site site = siteIter.next();
		    for (int i = 0; i < site.getInDegree(); i++) {
			final Site c = site.getChild(i);
			if (first) {
			    first = false;
			} else {
			    routingTreeStr.append(";");
			}
			routingTreeStr.append(c.getID() + "->" + site.getID());
		    }
		}
		
		StringBuffer memoryStr = new StringBuffer(); 
		siteIter = this.rt.siteIterator(TraversalOrder.PRE_ORDER);
		while (siteIter.hasNext()) {
		    final Site site = siteIter.next();
		    memoryStr.append(site.getID() + ": "
	    		+ site.getDataMemoryCost(daf, agenda.getBufferingFactor()) + "\n");
		}
		
		try {
		    final PrintWriter out = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname)));
		    out.println("QUERY PLAN SUMMARY");
		    out.println("==================\n");
		    out.println("Routing tree sink node:				"
			    + this.rt.getRoot().getID());
		    out.println("Routing tree edges (child-parent):		"
				    + routingTreeStr);
		    out.println("Acquisition interval used:				"
				    + this.agenda.getAcquisitionInterval_ms());		    
		    out.println("Buffering factor used:					"
				    + this.agenda.getBufferingFactor());
		    out.println("Delivery time:							"
			    + Agenda.bmsToMs(this.agenda.getLength_bms(true)) + " time units");
		    out.println("Total Data Memory");
		    out.println (memoryStr);
		    out.close();
		} catch (final IOException e) {
		    logger.severe("Query plan summary generation failed: "
			    + e.toString());
		}
    }

    /**
     * Produces an XML file containing a summary of the main query plan 
     * traffic patterns. This has been created as part of the Manchester-
     * Strathclyde collaboration in DIAS.
     * @param qos The user quality-of-service specification.
     */
    public final void generateTrafficPatternsXML(final QoSSpec qos) {
		final String fname = QueryCompiler.queryPlanOutputDir
			+ "traffic-patterns.xml";
		try {
		    final PrintWriter out = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname)));
	
		    out.println("<query-plan>\n");
		    out.println("\t<buffering-factor>" + this.getBufferingFactor()
			    + "</buffering-factor>");
		    out.println("\t<qos-max-delivery-time>"
			    + (int) qos.getMaxDeliveryTime() + "</qos-delivery-time>");
		    out.println("\t<agenda-repeat-interval>"
			    + this.agenda.getLength_bms(true)
			    + "</agenda-repeat-interval>\n\n");
	
		    out.println("\t<transmissions>");
		    Iterator<Site> siteIter = this.rt.siteIterator(TraversalOrder.POST_ORDER);
		    while (siteIter.hasNext()) {
				final Site site = siteIter.next();
				final Iterator<ExchangePart> exchCompIter = site
					.getExchangeComponents().iterator();
				while (exchCompIter.hasNext()) {
		
				    final ExchangePart exchComp = exchCompIter.next();
				    if ((exchComp.getComponentType() == ExchangePartType.PRODUCER)
					    && exchComp.isRemote()) {
					out.println("\t\t<transmission id=frag"
						+ exchComp.getSourceFragID() + "Site"
						+ exchComp.getSourceSiteID() + "Frag"
						+ exchComp.getDestFragID() + "Site"
						+ exchComp.getDestSiteID() + ">");
					out.println("\t\t\t<from-fragment id="
						+ exchComp.getSourceFragID() + "/>");
					out.println("\t\t\t<from-site id="
						+ exchComp.getSourceSiteID() + "/>");
					out.println("\t\t\t<to-fragment id="
						+ exchComp.getDestFragID() + "/>");
					out.println("\t\t\t<to-site id="
						+ exchComp.getDestSiteID() + "/>\n");
		
					out.println("\t\t\t<data-size>");
					//TODO: need to check if this is correct for complex queries
					Operator root = exchComp.getSourceFrag().getRootOperator();
					out.println("\t\t\t\t<num-tuples>"
						+ root.getCardinality(
							CardinalityType.MAX, site, this.daf)
						* this.getBufferingFactor() + "</num-tuples>");
					//TODO: need to check if this takes into account 
					//the way packets are built in nesC
					out.println("\t\t\t\t<bytes-per-tuple>"
						+ root.getPhysicalTupleSize()
						+ "</bytes-per-tuple>");
					out.println("\t\t\t</data-size>\n");
		
					CommunicationTask commTask = this.agenda
						.getFirstCommTask(site,
							CommunicationTask.TRANSMIT);
					out.println("\t\t\t<start-time>"
						+ commTask.getStartTime() + "</start-time>");
		
					final FragmentTask fragTask = this.agenda
						.getFirstFragmentTask(exchComp.getDestSite(),
							exchComp.getDestFrag());
					out.println("\t\t\t<max-arrival-time>"
						+ fragTask.getStartTime()
						+ "</max-arrival-time>");
		
					out.println("\n");
					out.println("\t\t\t<hops>");
		
					ExchangePart currentExchComp = exchComp;
					while (currentExchComp.getComponentType() != ExchangePartType.CONSUMER) {
		
					    out.println("\t\t\t\t<hop id="
						    + currentExchComp.toString() + ">");
					    out.println("\t\t\t\t\t<from-site>"
						    + currentExchComp.getCurrentSiteID()
						    + "</from-site>");
					    out.println("\t\t\t\t\t<to-site>"
						    + currentExchComp.getNext()
							    .getCurrentSiteID() + "</to-site>");
		
					    commTask = this.agenda.getFirstCommTask(
						    currentExchComp.getCurrentSite(),
						    CommunicationTask.TRANSMIT);
					    out
						    .println("\t\t\t\t\t<start-time>"
							    + commTask.getStartTime()
							    + "</start-time>");
					    out.println("\t\t\t\t\t<max-arrival-time>"
						    + commTask.getEndTime()
						    + "</max-arrival-time>");
					    out.println("\t\t\t\t</hop>");
					    currentExchComp = currentExchComp.getNext();
					}
					out.println("\t\t\t</hops>");
		
					out.println("\t\t</transmission>\n");
				    }
				}
		    }
		    out.println("\t</transmissions>\n");
	
		    out.println("\t<sites>");
		    siteIter = this.siteIterator(TraversalOrder.POST_ORDER);
		    while (siteIter.hasNext()) {
			final Site site = siteIter.next();
			out.println("\t\t<site id=" + site.getID() + ">");
	
			final Iterator<ExchangePart> exchCompIter = site
				.getExchangeComponents().iterator();
			while (exchCompIter.hasNext()) {
			    final ExchangePart exchComp = exchCompIter.next();
			    if (exchComp.isRemote()) {
				if (exchComp.getComponentType() == ExchangePartType.PRODUCER) {
				    out.println("\t\t\t<transmission-xref-id>frag"
					    + exchComp.getSourceFragID() + "Site"
					    + exchComp.getSourceSiteID() + "Frag"
					    + exchComp.getDestFragID() + "Site"
					    + exchComp.getDestSiteID()
					    + "</transmission-xref-id>");
				    out.println("\t\t\t<hop-xref-id>"
					    + exchComp.toString() + "</hop-xref-id>");
				} else if (exchComp.getComponentType() == ExchangePartType.RELAY) {
				    out.println("\t\t\t<hop-xref-id>"
					    + exchComp.toString() + "</hop-xref-id>");
				}
			    }
			}
			out.println("\t\t</site>");
		    }
	
		    out.println("\t</sites>");
		    out.println("</query-plan>");
		    out.close();
		} catch (final IOException e) {
		    logger.severe("Query plan summary generation failed: "
			    + e.toString());
		}
    }
}
