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
package uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling;

import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Agenda;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.QWhenScheduling;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.vanilla.VWhenScheduling;

/**
 * 
 * When Scheduling: Step 7 of query optimization
 * 
 * <i>When Scheduling</i> times the execution of the fragments in the DAF.  
 * Doing so efficiently is seldom a specific optimization goal in classical 
 * DQP. However, in SNs, the need to co-ordinate transmission and reception 
 * and to abide by severe energy constraints make it important to favour duty 
 * cycles in which the hardware spends most of its time in energy-saving 
 * states. The approach adopted by the SNEEql compiler/optimizer to decide 
 * on the timed execution of each fragment instance at each site is to build an 
 * agenda that, insofar as permitted by the memory available at the site, and 
 * given the acquisition rate and the maximum expected delivery time set for 
 * the query, buffers as many results as possible before transmitting. The aim 
 * is to be economical with respect to both the time in which a site needs to be
 * active and the amount of radio traffic that is generated.
 * 
 * The output of When Scheduling is an agenda which specifies the start time 
 * of each fragment instance at each site on which it executes.
 * 
 * @author Ixent Galpin
 */
public class WhenScheduling {

    public static ScoredCandidateList<Agenda> doWhenScheduling(
    		final ScoredCandidateList<DAF> dafList, final QoSSpec qos,
    		final String queryName)
	    throws OptimizationException, AgendaException {

    	ScoredCandidateList<Agenda> agendaList =
    		new ScoredCandidateList<Agenda>();
    	
    	Iterator<DAF> dafIter = dafList.iterator();
    	while (dafIter.hasNext()) {
        	DAF daf = dafIter.next();
        	
        	Agenda agenda;
    		if (Settings.QOS_AWARE_WHEN_SCHEDULING) {
    		    agenda = QWhenScheduling.doWhenScheduling(daf, qos, queryName);
    		} else {
    		    agenda = VWhenScheduling.doWhenScheduling(daf, qos, queryName);
    		}
    		
    		agendaList.addCandidate(agenda);
    	}
		
        if (Settings.DISPLAY_AGENDA && Settings.GENERAL_GENERATE_GRAPHS) {
        	agendaList.display(QueryCompiler.queryPlanOutputDir);
		}	
        
		return agendaList;
    }

}
