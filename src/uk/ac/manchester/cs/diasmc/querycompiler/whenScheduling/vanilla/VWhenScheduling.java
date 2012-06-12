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
package uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.vanilla;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Agenda;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaLengthException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePart;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FragmentTask;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.QWhenScheduling;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

public class VWhenScheduling {

    /**
     * Logger for this class.
     */
    private static Logger logger = 
    		Logger.getLogger(VWhenScheduling.class.getName());

    /**
     * Carry out <i>When Scheduling</i> step of query optimization.
     * 
     * @param daf	The query plan DAF
     * @param qos	The quality-of-service expectations (in particular, the 
     * 				tuple delivery time)
     * @param queryName The name of the query being optimized
     * @throws OptimizationException An optimization exception
     * @return The generated agenda
     */
    public static Agenda doWhenScheduling(final DAF daf, 
    		final QoSSpec qos, final String queryName)
	    throws OptimizationException {

	try {
    	CostExpressions costExpressions = CostExpressions.costExpressionFactory(daf);
		if (Settings.DISPLAY_COST_EXPRESSIONS) {
			//Even if not used in this version of When Scheduling.
			//They are now!!
	    	costExpressions.display();
		}
	    logger.fine("Computing maximum possible buffering factor based on "
	    		+ " memory");
	    long maxBFactorSoFar = computeMaximumBufferingFactorBasedOnMemory(
		    daf, qos, costExpressions);
	    logger.fine("Max possible buffering factor according to memory " 
	    		+ " available on nodes in sensor network: "	+ maxBFactorSoFar);
	    
		// optimizer should compute best buffering factor based on memory, 
		// maxBFactor and delivery time
	    if (qos.getBufferingFactor() == -1) { 
			logger.finest("qos.getBufferingFactor()==-1");			
			if ((qos.getMaxBufferingFactor() < maxBFactorSoFar)
				&& (qos.getMaxBufferingFactor() != -1)) {
			    maxBFactorSoFar = qos.getMaxBufferingFactor();
			}
	
			logger.fine("Reduce buffering factor to meet delivery time QoS, "
					+ "if given");
			maxBFactorSoFar = computeMaximumBufferingFactorToMeetDeliveryTime(
				daf, qos, maxBFactorSoFar);
			
			//We need this until we can support overlapping agendas
			logger.fine("Reduce buffering factor if agenda overlap will occur");
			maxBFactorSoFar = computeMaximumBufferingFactorWithoutAgendaOverlap(
				daf, qos, maxBFactorSoFar);
			
	    } else {
		//use the buffering factor specified in the Qos 
	    //(overrides Max buffering factor)

			if (qos.getBufferingFactor() 
				> computeMaximumBufferingFactorToMeetDeliveryTime(
				daf, qos, maxBFactorSoFar)) {
			    throw new OptimizationException(
				    "Buffering factor " + qos.getBufferingFactor() 
				    + " specified in QoS cannot meet the maximum delivery time "
					+ qos.getMaxDeliveryTime());
			}
			if (maxBFactorSoFar < qos.getBufferingFactor()) {
			    throw new OptimizationException(
				"Buffering factor " + qos.getBufferingFactor()
				+ " specified in QoS cannot be supported due to "
				+ "lack of memory");
			}
			if (computeMaximumBufferingFactorWithoutAgendaOverlap(daf,
				qos, maxBFactorSoFar) < qos.getMaxBufferingFactor()) {
			    throw new OptimizationException(
				"Buffering factor " + qos.getBufferingFactor()
				+ " specified in QoS would require an agenda overlap;"
				+ " these are currently not supported");
			}
			maxBFactorSoFar = qos.getMaxBufferingFactor();
	    }

	    try {
	    	final Agenda agenda = new Agenda(qos.getMaxAcquisitionInterval(), maxBFactorSoFar, daf, queryName);
		    return agenda;
	    } catch (Exception e) {
	    	Utils.handleCriticalException(e);
	    }
	    return null;

	} catch (final AgendaException e) {
	    throw new OptimizationException(e.getMessage());
	}
    }

    /**
     * Given a plan, computes the memory used by a single evaluation of the query plan fragments placed on 
     * each sensor network node, and returns the highest possible buffering factor, i.e., number of 
     * evaluations that can take place of the leaf fragments before onward transmission takes place.
     * 
     * @param plan
     * @return the maximum possible buffering factor for this plan
     * @throws OptimizationException
     */
    private static long computeMaximumBufferingFactorBasedOnMemory(
	    final DAF daf, final QoSSpec qos, CostExpressions costExpressions)
	    throws OptimizationException {

		long maxBufferingFactor = qos.getMaxBufferingFactor();
		
		//traverse each sensor network node in the routing tree
		final Iterator<Site> nodeIter = daf.siteIterator(TraversalOrder.PRE_ORDER);
		while (nodeIter.hasNext()) {
	
		    final Site currentNode = nodeIter.next();
		    if (currentNode.getID().equals("0")) {
		    	continue;
		    }
	
		    logger.finest("Computing maximum local buffering factor for node "
			    + currentNode.getID());
	
/*		    int totalFragmentDataMemoryCost = 0;
		    final Iterator<Fragment> fragments = 
		    	currentNode.getFragments().iterator();
		    while (fragments.hasNext()) {
				final Fragment fragment = fragments.next();
				logger.finest("fragment = " + fragment);
				totalFragmentDataMemoryCost += fragment.getDataMemoryCost(
					currentNode, daf);
		    }
		    logger.finest("Data Memory Cost of all fragments is "
			    + totalFragmentDataMemoryCost);
	
		    int totalExchangeComponentsDataMemoryCost = 0;
	
		    final Iterator<ExchangePart> comps = currentNode
			    .getExchangeComponents().iterator();
		    while (comps.hasNext()) {
				final ExchangePart comp = comps.next();
				logger.finest("exchange component =" + comp);
				totalExchangeComponentsDataMemoryCost += comp
					.getDataMemoryCost(currentNode, daf);
		    }
	
		    //compute the maximum possible buffering factor on this node, based on the memory available and
		    //the memory cost
		    long localMaxBufferingFactor;
		    if (totalExchangeComponentsDataMemoryCost > 0) {
				localMaxBufferingFactor = (currentNode.getRAM() - totalFragmentDataMemoryCost)
					/ totalExchangeComponentsDataMemoryCost;
		    } else {
		    	localMaxBufferingFactor = Long.MAX_VALUE;
		    }
		    logger.finest("Memory maximum buffering factor is "
			    + localMaxBufferingFactor);
*/
		    
		    AlphaBetaExpression siteMemExpr = costExpressions.getSiteMemoryExpression(currentNode, true);
		    long siteMemoryStock = currentNode.getRAM(); 
		    long localMaxBufferingFactor;
		    if (siteMemExpr.getBetaCoeff() > 0) {
		    	localMaxBufferingFactor = (long)((siteMemoryStock - siteMemExpr.getConstant()) / 
		    			siteMemExpr.getBetaCoeff());
		    	
		    } else {
		    	localMaxBufferingFactor = Long.MAX_VALUE;
		    }
		    
		    
		    
		    //A buffering factor of 0 indicates this query plan is not possible, throw an error
		    if (localMaxBufferingFactor <= 0) {
			throw new OptimizationException(
				"Query plan not feasible due to lack of memory at node "
					+ currentNode.getID() /*+ "; At least " + (totalExchangeComponentsDataMemoryCost 
					+ totalFragmentDataMemoryCost) + " is required and " + currentNode.getRAM()
					+ " is available."*/ );
		    }
	
		    //If the local maximum buffering factor is smaller than the global one, reduce the global
		    //buffering factor accordingly
		    if (localMaxBufferingFactor < maxBufferingFactor) {
			maxBufferingFactor = localMaxBufferingFactor;
			logger
				.finest("Global buffering factor reduced because of memory at node"
					+ currentNode.getID() + "to " + maxBufferingFactor);
		    }
		    
		}
		return maxBufferingFactor;
    }

    /**
     *	Given a plan and the maximum buffering factor possible based on memory available on the sensor network nodes,
     *  checks if this buffering factor will result in an overlapping agenda.  If so, and the ini file setting 
     *  decrease_bfactor_to_avoid_agenda_overlap=true, it will iteratively decrease the buffering factor until the agenda
     *  does not overlap.  If the buffering factor=1 and the agenda still overlaps, or the ini file setting 
     *  decrease_bfactor_to_avoid_agenda_overlap=false, throw an exception.
     *  
     */
    private static long computeMaximumBufferingFactorWithoutAgendaOverlap(
	    final DAF daf, final QoSSpec qos, final long maxBFactorSoFar)
	    throws OptimizationException, AgendaException {

    	
    	AlphaBetaExpression pi = QWhenScheduling.computePi(daf);
    	System.err.println("pi="+pi.toString());
    	final long alpha_ms = qos.getMaxAcquisitionInterval();
    	long beta = maxBFactorSoFar;
    	
		if (Settings.WHEN_SCHEDULING_DECREASE_BFACTOR_TO_AVOID_AGENDA_OVERLAP) {
			if (pi.getBeta2Coeff()==0) {
				//linear expression, simple!
				beta = (long)((alpha_ms - pi.getConstant()) / pi.getBetaCoeff());				
			} else {
				//qudratic expression
				double a = pi.getBeta2Coeff();
				double b = pi.getBetaCoeff();
				double c = pi.getConstant() - alpha_ms;
				double[] sols = Utils.solveQuadraticEquation(a, b, c);
				beta = (long)Math.max(sols[0], sols[1]);
			}
		}
		
		if (beta==0) {
	    Utils.handleQueryException(new OptimizationException(
			    "Agenda requires overlap even with buffering factor=1.  Overlapping agendas are currently not supported; try increasing the acquisition interval in the QoS file."));
	    	}
		
		return Math.min(beta, maxBFactorSoFar);
    }

    /**
     * 
     * Given a plan and the maximum buffering factor possible based on memory available on the sensor network nodes,
     * checks if this buffering factor will meet the max delivery time QoS requirements (if any are given).  If they are not
     * met, the buffering factor is reduced until they are, or an exception is thrown if they can't be met.
     * 
     */
    private static long computeMaximumBufferingFactorToMeetDeliveryTime(
	    final DAF daf, final QoSSpec qos, final long maxBFactorSoFar)
	    throws OptimizationException, AgendaException {

    	//TODO: make this binary search like computeMaximumBufferingFactorWithoutAgendaOverlap
    	
	Agenda agenda;
	long currentMaxBuffFactor = maxBFactorSoFar;
	logger.finest("maxBFactorSoFar ="+maxBFactorSoFar);
	final long bmsDeliveryTime = Agenda.msToBms_RoundUp(qos.getMaxDeliveryTime());
	if (bmsDeliveryTime > 0) {
	    do {
	    	try {
	    		agenda = new Agenda(qos.getMaxAcquisitionInterval(), currentMaxBuffFactor, daf, "");
	    		//CB I don't think this is neseccary as new Agenda throw an error.
	    		//CB :Left in for safety
	    		if (agenda.getLength_bms(Agenda.IGNORE_SLEEP) > bmsDeliveryTime) {
	    		    currentMaxBuffFactor--;
	    		    logger.finest("Agenda time ="
	    			    + agenda.getLength_bms(Agenda.IGNORE_SLEEP)
	    			    + " > " + bmsDeliveryTime);
	    		    logger.finest("Max Buffering factor reduced to "
	    			    + currentMaxBuffFactor);
	    		} else {
	    			break;
	    		}
	    		
	    	} catch (AgendaLengthException e) {
	    		if (currentMaxBuffFactor > 1) {
	    		    currentMaxBuffFactor--;	    			
	    		} else {
	    			e.printStackTrace();
	    			throw new OptimizationException(
	    			"It is not possible to meet the delivery" 
	    					+ " time QoS requirements "
	    					+ "even with a buffering factor of 1.");
	    		}
	    	}
	    } while (true);

	}

	return currentMaxBuffFactor;
    }
}