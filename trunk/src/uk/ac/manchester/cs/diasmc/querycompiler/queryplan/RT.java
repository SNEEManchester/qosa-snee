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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.graph.EdgeImplementation;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Path;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.RadioLink;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Topology;

/**
 * Class to represent a Routing Tree, data structure used to determine the 
 * data flows in the sensor network.
 * @author galpini
 *
 */
public class RT extends Topology implements Scorable {

	/**
	 * The sink of the routing tree.
	 */
    private Site rootSite = null;

    /**
     * Counter to assign unique id to different candidates.
     */
    protected static int candidateCount = 0;

    
    /**
     * The score assigned to this RT (QoS-aware version only)
     */
    private double score = -1;
    
    
    /**
     * Description of algorithm/parameters used to generate this RT.
     */
    private String desc="";
    
    /**
     * Logger for this class.
     */
    private static  Logger logger = Logger.getLogger(
    		RT.class.getName());    
    
    /**
     * Constructor for the RoutingTree class.
     * @param queryName the name of the routing tree
     * @param inRootSite the sink node for the query
     */
	public RT(final String queryName, final Site inRootSite) {
		super(generateName(queryName), true);
		this.rootSite = inRootSite.shallowClone();
		this.addNode(this.rootSite);
	}
    
	public RT(final String queryName) {
		super(generateName(queryName), true);
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
    	return queryName + "-RT-" + candidateCount;
    }
	
    /**
     * Copies a site from another topology, and adds it to this one.
     * @param site the site to be cloned
     * @return the new site created
     */
    public final Site addExternalSiteClone(final Site site) {
		final Site newSite = new Site(site.getID());
		newSite.setEnergyStock(site.getEnergyStock());
		newSite.setRAM(site.getRAM());
		newSite.setIsSource(site.isSource());
		return (Site) super.addNode(newSite);
    }    

    /**
     * Copies a radio link from another topology, and adds it to this one.
     * @param oldSource the source site
     * @param oldDest the destination site
     * @param oldLink the link to be copied
     * @return the edge which was created
     */
    public final RadioLink addExternalSiteAndRadioLinkClone(
    		final Site oldSource, final Site oldDest, 
    		final RadioLink oldLink) {
    	//clone the nodes
    	final Site newSource = this.addExternalSiteClone(oldSource);
    	final Site newDest = this.addExternalSiteClone(oldDest);
    	
    	//clone the radio link
    	RadioLink newLink = super.addRadioLink(newSource.getID(), 
    			newDest.getID(), false, oldLink.getRadioLossCost());
    	newLink.setEnergyCost(oldLink.getEnergyCost());
    	newLink.setLatencyCost(oldLink.getLatencyCost());		
    	return newLink;
    }   

    /**
     * Gets the root site of the routing tree (i.e., the sink for the query). 
     * @return the root site/sink
     */
    public final Site getRoot() {
    	return this.rootSite;
    }

    /**
     * Given a source site, and a destination site downstream (i.e., further up
     * the tree), returns the path from the source site to the destination site.
     * @param sourceID the id for the source site
     * @param destID the id for the destination site
     * @return the path from the source to destination site
     */
    public final Path getPath(final String sourceID, final String destID) {
    	Path path = new Path();
    	Site source = this.getSite(sourceID);
    	Site current = source;
    	while (current != null) {
    		path.append(current);
    		if (current.getID().equals(destID)) {
    			break;
    		}
    		current = (Site) current.getOutput(0);
    	}
    	
    	// a path was not found from the source and dest
    	if (path.getLastSite() == null) {
    		return new Path();
    	}
    	if (!path.getLastSite().getID().equals(destID)) {
    		return new Path();
    	}
    	
    	return path;
    }
    

    /**
     * Clone the current routing tree.
     * @return a clone of the current routing tree.
     */
    @Override
    public final  RT clone() {
    	RT clone = new RT(this.getName(), 
    			this.getRoot());
    	super.cloneNodesAndEdges(clone);
    	if (rootSite != null) {
    		clone.rootSite = (Site) clone.nodes.get(this.rootSite.getID());
    	}
    	clone.name = this.name; //a hack, constructor should know
    	clone.score = this.score;
    	clone.desc = this.desc;
    	return clone;
    }
	
    /**
     * Helper method to recursively generate the operator iterator.
     * @param site the operator being visited
     * @param siteList the operator list being created
     * @param traversalOrder the traversal order desired 
     */    
    private void doSiteIterator(final Site site,
    	    final ArrayList<Site> siteList, 
    	    final TraversalOrder traversalOrder) {

    	if (traversalOrder == TraversalOrder.PRE_ORDER) {
    	    siteList.add(site);
    	}

    	for (int n = 0; n < site.getInDegree(); n++) {
    	    this.doSiteIterator(site.getChild(n), siteList, traversalOrder);
    	}

    	if (traversalOrder == TraversalOrder.POST_ORDER) {
    	    siteList.add(site);
    	}
    }

    /**
     * Iterator to traverse the routing tree.
     * The structure of the routing tree may not be modified during iteration
     * @param traversalOrder the order to traverse the routing tree
     * @return an iterator for the routing tree
     */    
    public final Iterator<Site> siteIterator(
    		final TraversalOrder traversalOrder) {

    	return siteIterator(this.getRoot(), traversalOrder);
    }

    public final Iterator<Site> siteIterator(
    		Site rootSite,
    		final TraversalOrder traversalOrder) {
    	
    	final ArrayList<Site> siteList = new ArrayList<Site>();
    	this.doSiteIterator(rootSite, siteList, traversalOrder);
    	return siteList.iterator();
    }
    
	public ArrayList<Site> getSites(ArrayList<String> desiredSites) {
		ArrayList<Site> result = new ArrayList<Site>();
		
		Iterator<String> siteIDIter = desiredSites.iterator();
		while (siteIDIter.hasNext()) {
			result.add(this.getSite(siteIDIter.next()));
		}
		return result;
	}    
    
	@Override
	public boolean isEquivalentTo(Object other) {
		if (!(other instanceof RT)) {
			return false;
		}
		
		RT otherRT = (RT) other;
		boolean equiv = super.isEquivalentTo(otherRT);
		if (!equiv) {
			return false;
		}
		if (!this.getRoot().getID().equals(otherRT.getRoot().getID())) {
			return false;
		}
		return true;
	}
	
	public void exportAsDOTFile(String fname) {
		super.exportAsDOTFile(fname, this.getName());
	}
	
	public void exportAsDOTFile(String fname, String label) {
		super.exportAsDOTFile(fname, this.getName() + " \\n " + this.desc + " \\n "+ label);
	}
	
	public double getScore() {
		return this.score;
	}
    
	public void setScore(double s) {
		this.score = s;
	}

	public void updateNumSourceWeighting() {
		this.getRoot().updateNumSources();
	}

	public void rotateTree(Site current, Site prevNode) {
		for (int n=0; n<current.getInDegree(); n++) {
			Site inputNode = (Site)current.getInput(n);
			//Do nothing, edge has right direction
			rotateTree(inputNode, current);
		}
		for (int n=0; n<current.getOutDegree(); n++) {
			Site outputNode = (Site)current.getOutput(n);
			if (outputNode == prevNode) {
				continue;
			}
			super.reverseEdgeDirection(current, outputNode);
			rotateTree(outputNode, current);
		}
	}

	public void setRoot(Site sinkSite) {
		this.rootSite = sinkSite;
		
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
