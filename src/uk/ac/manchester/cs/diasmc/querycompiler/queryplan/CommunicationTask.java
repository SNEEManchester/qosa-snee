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

import java.util.HashSet;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * This class represents a tasks which involves communication between two nodes in the query plan.
 * @author Ixent Galpin
 *
 */
public class CommunicationTask extends Task {

	//TODO move to task.
    //The site which is transmitting data
    private Site sourceNode;

    //The site which is receiving data
    private Site destNode;

    //The exchange components involved
    private HashSet<ExchangePart> exchangeComponents;

    //specifies whether task is sending or receiving
    private int mode;

    public static final int RECEIVE = 0;

    public static final int TRANSMIT = 1;

    private long bufferingFactor;

    private DAF daf;

    /**
     * Create an instance of a CommunicationTask.
     * @param startTime
     * @param sourceNode
     * @param destNode
     * @param exchangeComponents
     * @param mode
     */
    public CommunicationTask(final long startTime, final Site sourceNode,
	    final Site destNode,
	    final HashSet<ExchangePart> exchangeComponents,
	    final int mode, final long bufferingFactor, final DAF daf) {
	super(startTime);
	this.daf = daf;
	this.sourceNode = sourceNode;
	this.destNode = destNode;
	this.exchangeComponents = exchangeComponents;
	this.bufferingFactor = bufferingFactor;
	this.endTime = startTime + this.getTimeCost(daf);
	this.mode = mode;
    }

    public final Site getSourceNode() {
	return this.sourceNode;
    }

    public final String getSourceID() {
	return this.sourceNode.getID();
    }

    public final Site getDestNode() {
	return this.destNode;
    }

    public final String getDestID() {
	return this.destNode.getID();
    }

    public final int getMode() {
	return this.mode;
    }

    @Override
	public final Site getSite() {
	if (this.mode == RECEIVE) {
	    return this.destNode;
	} else {
	    return this.sourceNode;
	}
    }

    @Override
	public final String getSiteID() {
	return this.getSite().getID();
    }

    /**
     * Calculates the time overhead involved in a radio exchange.
     * 
     * Based on the time estimates provided in the OperatorsMetaData file.
     * 
     * @return time overhead of a radio message. 
     */
    public static long getTimeCostOverhead() {
/*    	return (long) Math.ceil(CostParameters.getCallMethod()
    			+ CostParameters.getSignalEvent()
    			+ CostParameters.getTurnOnRadio()
    			+ CostParameters.getRadioSyncWindow() * 2
    			+ CostParameters.getTurnOffRadio());*/

    	return (long) Math.ceil(CostParameters.getCallMethod()
    			+ CostParameters.getSignalEvent()
    			+ CostParameters.getRadioSyncWindow() * 2);
    		//radio turned on/off as part of separate task
    	}
        
    /**
     * @return	the exchange components which have been plaecd on a node
     */
    public HashSet<ExchangePart> getExchangeComponents() {
	return this.exchangeComponents;
    }    
    
    /**
     * Calculate the time to do thus Exchange.
     * Does not include overhead such turning radio on and off. 
     * @param card CardinalityType The type of cardinality to be considered.
     * @param daf Distributed query plan this operator is part of.
	 * @param round Defines if rounding reserves should be included or not
     * @return Expression for the time cost.
     */
    private AlphaBetaExpression getTimeExpression(
    		final CardinalityType card, final DAF daf, final boolean round) {
    	AlphaBetaExpression result = new AlphaBetaExpression();
    	final Iterator<ExchangePart> exchCompIter 
			= this.exchangeComponents.iterator();
    	while (exchCompIter.hasNext()) {
    		final ExchangePart exchComp = exchCompIter.next();
    		if ((exchComp.getComponentType() 
    					== ExchangePartType.PRODUCER)
    				|| (exchComp.getComponentType() 
    					== ExchangePartType.RELAY)) {
    			result.add(exchComp.getTimeExpression(card, daf, round));
    		}
    	}
    	//remove one getBetweenpackets cost for first packet
		result.subtract(CostParameters.getBetweenPackets());
    	result.add(getTimeCostOverhead());
    	return result;
    }
    
    /**
     * Returns the duration of this task.
     */
    @Override
	protected final long getTimeCost(final DAF daf) {
	
    		return (long) getTimeExpression(CardinalityType.PHYSICAL_MAX, daf, true).
    			evaluate(daf.getAcquisitionInterval(), this.bufferingFactor);
    
    }


    /**
     * String representation.
     */
    @Override
	public final String toString() {
	if (this.mode == RECEIVE) {
	    return "RX " + this.sourceNode.getID() + "_" + this.destNode.getID();
	}
	return "TX " + this.sourceNode.getID() + "_" + this.destNode.getID();
    }

    @Override
	public final CommunicationTask clone(final DAF clonedDAF) {
	final Site clonedSourceNode = (Site) clonedDAF.getRoutingTree()
		.getNode(this.sourceNode.getID());
	final Site clonedDestNode = (Site) clonedDAF.getRoutingTree().getNode(
		this.destNode.getID());

	final HashSet<ExchangePart> clonedExchangeComponents = new HashSet<ExchangePart>();
	Site clonedCurrentNode = null;
	if (this.mode == TRANSMIT) {
	    clonedCurrentNode = clonedSourceNode;
	} else if (this.mode == RECEIVE) {
	    clonedCurrentNode = clonedDestNode;
	}

	//dubious bit of code
	final HashSet<ExchangePart> exchComponents = this.exchangeComponents;
	final Iterator<ExchangePart> exchCompIter = exchComponents
		.iterator();
	while (exchCompIter.hasNext()) {
	    final ExchangePart exchComp = exchCompIter.next();

	    final Iterator<ExchangePart> exchCompIter2 = clonedCurrentNode
		    .getExchangeComponents().iterator();
	    while (exchCompIter2.hasNext()) {
		final ExchangePart clonedExchangeComponent = exchCompIter2
			.next();
		if (clonedExchangeComponent.toString().equals(
			exchComp.toString())) {
		    clonedExchangeComponents.add(clonedExchangeComponent);
		}
	    }
	}

	final CommunicationTask clone = new CommunicationTask(this.startTime,
		clonedSourceNode, clonedDestNode, clonedExchangeComponents,
		this.mode, this.bufferingFactor, this.daf);
	return clone;
    }

}
