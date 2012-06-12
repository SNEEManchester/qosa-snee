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
package uk.ac.manchester.cs.diasmc.querycompiler.mqe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FragmentTask;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Agenda;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FragmentTask;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.QueryPlan;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Task;
/**
 * Performs Multiple Query Execution operations.
 * @author Jamil Naja
 *
 */
public class MQE {
	
	private static Logger logger = Logger.getLogger(MQE.class.getName());
	
	/**
	 * Takes two agendas as parameters and returns one agenda as a final result.
	 * @param individualAgendas Agendas to be merged
	 * @return merged agenda
	 */
	public static Agenda mergeAgendas(ArrayList<Agenda> agendas) {
		
		Agenda finalAgenda = new Agenda(
				agendas.get(0).getAcquisitionInterval_bms(),
				agendas.get(0).getBufferingFactor());
		
		Iterator<Agenda> agendaIterator = agendas.iterator();
		int queryCount = 0;
		while (agendaIterator.hasNext()) {
			
			final Agenda currentAgenda = agendaIterator.next();
			final Iterator<Site> siteIterator = currentAgenda.siteIterator();
			while (siteIterator.hasNext()) {
				final Iterator<Task> taskIterator = 
					currentAgenda.taskIterator(siteIterator.next());
				while (taskIterator.hasNext()) {
					
					final Task currentTask = taskIterator.next();
					if (currentTask instanceof FragmentTask) {
						FragmentTask currentFragmentTask = 
							(FragmentTask) currentTask;
						//needed to access the private member Fragment
						currentFragmentTask.storeMultiFragment(queryCount);
						
					}
					finalAgenda.addReadyTask(currentTask, currentTask.getSite());
					
				}
			}
			queryCount++;
		}
		finalAgenda.display(QueryCompiler.queryPlanOutputDir,
					finalAgenda.getName());
		return finalAgenda;	
	}
	
	public static void generateMultipleQueryPlan(
			final ArrayList<QueryPlan> queryPlanColl) throws IOException {
	
		String outputRootDir = Settings.GENERAL_OUTPUT_ROOT_DIR + "mqe/";
	    QueryCompiler.queryPlanOutputDir = outputRootDir + Constants.QUERY_PLAN_DIR;
		Utils.checkDirectory(QueryCompiler.queryPlanOutputDir, true);

    	ArrayList<Agenda> individualAgendas = new ArrayList<Agenda>();
    	Iterator<QueryPlan> queriesplansIterator = queryPlanColl.iterator();
		while (queriesplansIterator.hasNext()) {
			individualAgendas.add(queriesplansIterator.next().getAgenda());
		}
		mergeAgendas(individualAgendas);

	}

}
