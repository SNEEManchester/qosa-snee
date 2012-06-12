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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JFrame;

import uk.ac.manchester.cs.diasmc.common.Triple;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostParameters;

/**
 * Class responsible for recording the schedules of nodes in a sensor network.
 * @author 	Ixent Galpin
 *
 */
public class Agenda implements Cloneable, Scorable {

    /**
     * The acquisition interval, in binary ms.  The finest granularity that may be given is 1/32ms (=32 bms)
     */
    private long alpha;

    /**
     * The buffering factor.
     */
    private long beta;

    /**
     * The task schedule for all the sites. 
     */
    //the list of tasks for each sensor network node
    private final HashMap<Site, ArrayList<Task>> tasks = new HashMap<Site, ArrayList<Task>>();

    //the list of all start times (used to display schedule)
    private ArrayList<Long> startTimes = new ArrayList<Long>();

    public static final boolean IGNORE_SLEEP = true;

    public static final boolean INCLUDE_SLEEP = false;

    private DAF daf;

    private String name;
    
    /**
     * Counter to assign unique id to different candidates.
     */
    private static int candidateCount = 0;

    /**
     * The score assigned to this FAF (QoS-aware version only)
     */
    private double score = -1;    
    
    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger(Agenda.class.getName());
    
    /**
     * Start of nonLeaf part of the agenda.
     * This will be where the last leaf jumps to at the end of the query duration 
     */
    private long nonLeafStart = Integer.MAX_VALUE;

    
    private double cvxPi;
    private double cvxEpsilon;
    private double cvxLambdaDays;    
    
    /**
     * Constructor for MQE Agenda.
     * @param uAcqusitionInterval unified Acquisition Interval
     * @param uBufferingFactor unified Buffering Factor  
     */
    public Agenda(final long uAcqusitionInterval, final long uBufferingFactor) {
		this.alpha = msToBms_RoundUp(uAcqusitionInterval);
		this.beta = uBufferingFactor;
    	this.name = "MQE-Agenda";
    }
    
    public Agenda(final long acquisitionInterval, final long bfactor,
	    final DAF daf, final String queryName) throws AgendaException, AgendaLengthException {
		this.alpha = msToBms_RoundUp(acquisitionInterval);
		this.beta = bfactor;
		this.daf = daf;
		if (!queryName.equals("")) {
			this.name = generateName(queryName);
		}
		logger.fine("About to schedule the leaf fragments alpha=" + this.alpha + " bms beta=" + this.beta);
		//First schedule the leaf fragments
		scheduleLeafFragments();
		logger.fine("Scheduled the leaf fragments");
		//Now traverse the routing tree, place everything except leaf fragments
		scheduleNonLeafFragments();
		logger.fine("Scheduled the NON leaf fragments");

		long length = this.getLength_bms(Agenda.INCLUDE_SLEEP);
		logger.fine("Agenda alpha=" + this.alpha + " beta=" + this.beta + " alpha*beta = " + this.alpha * this.beta + " length="+length);
 		if (length > (this.alpha * this.beta)) {
 			//display the invalid agenda, for debugging purposes
 			this.display(QueryCompiler.queryPlanOutputDir,
					this.getName()+"-invalid");
			throw new AgendaLengthException("Invalid agenda: alpha*beta = " + bmsToMs(this.alpha * this.beta) 
					+ "ms, length = "+bmsToMs(length) + "ms, alpha = "+bmsToMs(alpha) + "ms, beta = " + bmsToMs(beta));
		}
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
    	return queryName + "-Agenda-" + candidateCount;
    }
    
    public DAF getDAF() {
    	return this.daf;
    }
    
    public String getName() {
    	return this.name;
    }
    
    /**
     * Returns acquisition interval, in bms
     * @return the acquisition interval, in bms
     */
    public final long getAcquisitionInterval_bms() {
	return this.alpha;
    }

    /**
     * Returns acquisition interval, in ms
     * @return the acquisition interval, in ms
     */
	public long getAcquisitionInterval_ms() {
		return Agenda.bmsToMs(this.alpha);
	}

    /**
     * Returns the buffering factor for this agenda
     * @return the buffering factor
     */
    public final long getBufferingFactor() {
	return this.beta;
    }

    /**
     * Returns the processing time for this agenda, in bms
     * @return the processing time, in bms
     */
	public long getProcessingTime_bms() {
		return this.getDeliveryTime_bms() - this.alpha * (this.beta -1);
	}
	
    /**
     * Returns the processing time for this agenda, in ms
     * @return the processing time, in ms
     */
	public long getProcessingTime_ms() {
		return Agenda.bmsToMs(this.getDeliveryTime_bms() - this.alpha * (this.beta -1));
	}
	
    /**
     * Returns the delivery time for this agenda, in bms
     * @return the delivery time, in bms
     */
	public long getDeliveryTime_bms() {
	
		return this.getLength_bms(true);
	}

    /**
     * Returns the delivery time for this agenda, in ms
     * @return the delivery time, in ms
     */
	public long getDeliveryTime_ms() {
		
		return Agenda.bmsToMs(this.getLength_bms(true));
	}
	
    public static int bmsToMs(final long startTime) {
	return Utils.divideAndRoundUp(startTime * 1000, 1024);
    }

    public static int msToBms_RoundUp(final long ms) {
	final long temp = ms * 1024;
	return Utils.divideAndRoundUp(temp, 1000);
    }

    /**
     * Returns the list of start times, in order, for each task on every node
     * @return
     */
    public final ArrayList<Long> getStartTimes() {
	return this.startTimes;
    }

    public final long getNonLeafStart() {
	return this.nonLeafStart;
    }

    public final void setNonLeafStart(final long nonLeafStart2) {
	this.nonLeafStart = nonLeafStart2;
    }

    /**
     * Given a time and a sensor network node, raises an exception if the given time is before the node's
     * next available time.
     * @param startTime
     * @param site
     * @throws AgendaException
     */
    private void assertConsistentStartTime(final double startTime,
	    final Site site) throws AgendaException {

	if (startTime < this.getNextAvailableTime(site, INCLUDE_SLEEP)) {
	    throw new AgendaException("Attempt to Schedule task on node "
		    + site.getID() + " at time " + startTime
		    + " which is before the nodes next available time ("
		    + this.getNextAvailableTime(site, INCLUDE_SLEEP) + ")");
	}

    }

    /**
     * Given a node, returns true if it has been assigned any tasks.
     * @param node
     * @return
     */
    public final boolean hasTasks(final Site node) {
	return (this.tasks.get(node) != null);
    }

    //adds a start time to the list of start times
    private void addStartTime(final long startTime) {

	boolean found = false;
	int i = 0;

	final Iterator<Long> startTimesIter = this.startTimes.iterator();
	while (startTimesIter.hasNext() && !found) {
	    final Long s = startTimesIter.next();
	    if (s.intValue() == startTime) {
		found = true;
	    }

	    if (s.intValue() > startTime) {
		break;
	    }

	    i++;
	}

	if (found == false) {
	    this.startTimes.add(i, new Long(startTime));
	    found = true;
	}

    }

    /**
     * Adds a task already who's cost been calculated already.
     * @param task Task to be added
     * @param site The ID of the site to be added to
     */
    public void addReadyTask(final Task task, final Site site) {
    
    	//start time shifting in case of queries with shared sensor nodes

    	addTask(task, site);
    }
    //adds a task to a nodes schedule, performing the necessary checks
    private void addTask(final Task t, final Site site) {

	//add node to schedule if necessary
	if (this.tasks.get(site) == null) {
	    this.tasks.put(site, new ArrayList<Task>());
	}

	//add task to the node schedule
	final ArrayList<Task> taskList = this.tasks.get(site);
	taskList.add(t);

	//add to list of start times
	this.addStartTime(t.getStartTime());
    }

    public final Task getTask(final long startTime, final Site site) {

	if (!this.tasks.keySet().contains(site)) {
	    return null;
	}
	final Iterator<Task> taskListIter = this.tasks.get(site).iterator();
	while (taskListIter.hasNext()) {
	    final Task t = taskListIter.next();
	    if (t.getStartTime() == startTime) {
		return t;
	    }
	}
	return null;
    }

    /**
     * Checks if site has been scheduled a task (including sleep tasks) at a given time.
     * @param site
     * @param startTime
     * @return
     */
    public final boolean isFree(final Site site, final long startTime, final long endTime) {
    	final Iterator<Task> taskListIter = this.tasks.get(site).iterator();
    	while (taskListIter.hasNext()) {
    	    final Task t = taskListIter.next();
    	    long taskStartTime = t.getStartTime();
    	    long taskEndTime = t.getEndTime();
    	    if (taskStartTime <= startTime && startTime < taskEndTime) {
    	    	return false;
    	    }
    	    if (taskStartTime <= endTime-1 && endTime-1 < taskEndTime) {
    	    	return false;
    	    }
    	}
    	return true;
    }
    

    public final boolean hasRadioOn(final Site site, final long time) {
    	boolean radioOn = false;
    	
    	final Iterator<Task> taskListIter = this.tasks.get(site).iterator();
    	while (taskListIter.hasNext()) {
    	    final Task t = taskListIter.next();
    	    long taskStartTime = t.getStartTime();

    	    if (taskStartTime > time) {
    	    	return radioOn;
    	    }
    	    
    	    if (t instanceof RadioOnTask) {
    	    	radioOn = true;
    	    }
    	    if (t instanceof RadioOffTask) {
    	    	radioOn = false;
    	    }
    	}
    	return radioOn;
    }
    
    /**
     * Shifts all the tasks for all the sites starting on or after a given time by offset.
     * @param time
     * @param offset
     */
    public final void shift(final long time, final long offset) {
    	
    	//update the list of startTimes
    	for (int i=0; i < this.startTimes.size(); i++) {
    		if (this.startTimes.get(i)>=time) {
    			long oldStartTime = this.startTimes.get(i);
    			long newStartTime = oldStartTime+offset;
    			this.startTimes.set(i, newStartTime);    			
    		}
    	}
    	
    	//update the list of tasks at each site
    	Iterator<Site> siteIter = this.getDAF().getRoutingTree().siteIterator(TraversalOrder.POST_ORDER);
    	while (siteIter.hasNext()) {
    		Site s = siteIter.next();
    		
    		ArrayList<Task> siteTaskList = this.tasks.get(s);
    		for (int i=0; i < siteTaskList.size(); i++) {
    			Task t = siteTaskList.get(i);
    			if (t.startTime>=time) {
	    			t.startTime += offset;
			    	t.endTime += offset;
    			}
    		}
    	}
    }
    
    
    public final void insertTask(final long time, final Site site, final Task t) {
    	boolean found = false;
    	
    	for (int i=0; i < this.startTimes.size(); i++) {
    		if (time == this.startTimes.get(i)) {
    			found = true;
    			break;
    		}
    		if (time < this.startTimes.get(i)) {
    			this.startTimes.add(i, new Long(time));
    			found = true;
    			break;
    		}
    	}

    	//append to the end
    	if (!found) {
    		this.startTimes.add(new Long(time));
    	}
    	
    	found = false;
    	ArrayList<Task> taskList = this.tasks.get(site);
    	for (int i=0; i < taskList.size(); i++) {
    		if (time < taskList.get(i).getStartTime()) {
    			taskList.add(i, t);
    			found = true;
    			break;
    		}
    	}
    
    	//append to end
    	if (!found) {
    		taskList.add(t);
    	}
    }
    
    public final FragmentTask getFirstFragmentTask(final Site site,
	    final Fragment frag) {
	final Iterator<Task> taskIter = this.taskIterator(site);
	while (taskIter.hasNext()) {
	    final Task t = taskIter.next();
	    if (t instanceof FragmentTask) {
		final FragmentTask fragTask = (FragmentTask) t;
		if (fragTask.getFragment() == frag) {
		    return fragTask;
		}
	    }
	}
	return null;
    }

    public final CommunicationTask getFirstCommTask(final Site site, final int mode) {
	final Iterator<Task> taskIter = this.taskIterator(site);
	while (taskIter.hasNext()) {
	    final Task t = taskIter.next();
	    if (t instanceof CommunicationTask) {
		final CommunicationTask commTask = (CommunicationTask) t;
		if ((commTask.getSourceNode() == site)
			&& (commTask.getMode() == mode)) {
		    return commTask;
		}
	    }
	}
	return null;
    }

    /**
     * Adds a fragment task at the specified startTime on the specified node
     * @param startTime		the time the task should start
     * @param frag			the query plan fragment to be executed
     * @param node			the sensor network node
     * @param occurrence	the nth evaluation of a leaf fragment
     * @throws AgendaException
     */
    public final void addFragmentTask(final long startTime, final Fragment frag,
	    final Site node, final long occurrence) throws AgendaException {

	this.assertConsistentStartTime(startTime, node);
	final FragmentTask fragTask = new FragmentTask(startTime, frag, node,
		occurrence, this.beta, this.daf);
	this.addTask(fragTask, node);

	logger.finest("Scheduled Fragment " + frag.getID() + " on node "
		+ node.getID() + " at time " + startTime);

    }

    public final void addFragmentTask(final int startTime, final Fragment frag,
	    final Site node) throws AgendaException {

	this.addFragmentTask(startTime, frag, node, 1);
    }

    /**
     * Adds a fragment task at the next available time on the specified node
     * @param fragment		the query plan fragment to be executed
     * @param node			the sensor network node 
     * @throws AgendaException	
     */
    public final void addFragmentTask(final Fragment fragment, final Site node)
	    throws AgendaException {

	final long startTime = this.getNextAvailableTime(node, INCLUDE_SLEEP);
	logger.finest("start time =" + startTime);
	this.addFragmentTask(startTime, fragment, node, 1);
    }

    public final void addFragmentTask(final Fragment fragment, final Site node,
	    final long ocurrence) throws AgendaException {
	final long startTime = this.getNextAvailableTime(node, INCLUDE_SLEEP);
	this.addFragmentTask(startTime, fragment, node, ocurrence);
    }

    /**
     * Appends a communication task between two nodes in the sensor network
     * @param sourceNode				the node transmitting data
     * @param destNode					the node receiving data
     * @param exchangeComponents		the data being sent
     */
    public final void appendCommunicationTask(final Site sourceNode,
	    final Site destNode,
	    final HashSet<ExchangePart> exchangeComponents)
	    throws AgendaException {

    final long startTime = this.getLength_bms(true);

	final CommunicationTask commTaskTx = new CommunicationTask(startTime,
		sourceNode, destNode, exchangeComponents,
		CommunicationTask.TRANSMIT, this.beta, this.daf);
	final CommunicationTask commTaskRx = new CommunicationTask(startTime,
		sourceNode, destNode, exchangeComponents,
		CommunicationTask.RECEIVE, this.beta, this.daf);

	this.addTask(commTaskTx, sourceNode);
	this.addTask(commTaskRx, destNode);

	logger.finest("Scheduled Communication task from node "
		+ sourceNode.getID() + " to node " + destNode.getID()
		+ " at time " + startTime + "(size: "
		+ exchangeComponents.size() + " exchange components )");
    }

    public final void addSleepTask(final long sleepStart, final long sleepEnd,
	    final boolean lastInAgenda) throws AgendaException {
	if (sleepStart < 0) {
	    Utils.handleQueryException(new Exception("Start time < 0"));
	}
	final Iterator<Site> siteIter = this.daf
		.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site site = siteIter.next();
	    this.assertConsistentStartTime(sleepStart, site);
	    final SleepTask t = new SleepTask(sleepStart, sleepEnd, site,
		    lastInAgenda);
	    this.addTask(t, site);
	}
    }

    /**
     * @param node		node in the sensor network
     * @return 			the time the node has completed all the tasks it has been allocated so far
     */
    public final long getNextAvailableTime(final Site node, final boolean ignoreSleep) {
	if (this.tasks.get(node) == null) {
	    return 0;
	} else {
	    //find the last task and return its end time
	    final ArrayList<Task> taskList = this.tasks.get(node);
	    int taskNum = taskList.size() - 1;
	    Task last = taskList.get(taskNum);
	    if (ignoreSleep) {
		while (last.isSleepTask()) {
		    taskNum = taskNum - 1;
		    if (taskNum < 0) {
			return 0;
		    } else {
			last = taskList.get(taskNum);
		    }
		}
	    }
	    long nextAvailableTime = last.getEndTime();
	    //			if (nextAvailableTime % 10 != 0) { //TODO: unhardcode this
	    //				nextAvailableTime = (nextAvailableTime / 10 + 1)* 10;
	    //			}

	    return nextAvailableTime;
	}

    }

    /**
     * @return if ignoreLastSleep is true, returns the time that the last task on all nodes ends.  
     * Otherwise returns the length of the agenda. 
     * 
     */
    public final long getLength_bms(final boolean ignoreLastSleep) {
	long tmp = 0;

	final Iterator<Site> nodeIter = this.tasks.keySet().iterator();
	while (nodeIter.hasNext()) {
	    final Site n = nodeIter.next();
	    if (this.getNextAvailableTime(n, ignoreLastSleep) > tmp) {
		tmp = this.getNextAvailableTime(n, ignoreLastSleep);
	    }
	}

	return tmp;
    }

    public final void exportAsDOTFile(final String fname) {
	try {
	    final PrintWriter out = new PrintWriter(new BufferedWriter(
		    new FileWriter(fname)));

	    out.println("digraph \"" + fname + "\" {");
	    out.println("node [shape = plaintext fontsize = 8]");

	    //display all the start times
	    boolean first = true;
	    final Iterator<Long> startTimeIter = this.startTimes.iterator();
	    while (startTimeIter.hasNext()) {
		if (first) {
		    first = false;
		} else {
		    out.print(" -> ");
		}

		final Long s = startTimeIter.next();
		out.print(s.toString());
	    }

	    final Iterator<Site> nodeIter = this.tasks.keySet().iterator();
	    while (nodeIter.hasNext()) {
		final Site n = nodeIter.next();

		final ArrayList<Task> taskList = this.tasks.get(n);
		final Iterator<Task> taskIter = taskList.iterator();
		while (taskIter.hasNext()) {
		    final Task t = taskIter.next();

		    out.println("{ rank=same; " + t.getStartTime() + " "
			    + t.toString() + " }");
		}
	    }

	    out.println("}");
	    out.close();
	} catch (final IOException e) {
	    logger.severe("Export failed: " + e.toString());
	}

    }

    /**
     * Returns an iterator which gives the all the sites in the schedule
     * @return
     */
    public final Iterator<Site> siteIterator() {
	return this.tasks.keySet().iterator();
    }

    /**
     * Returns an iterator which gives the tasks, in order, for the given node
     * @param node
     * @return
     */
    public final Iterator<Task> taskIterator(final Site node) {
	return this.tasks.get(node).iterator();
    }

    //TODO: review thie taskschedule data structure in view of this inefficient access algorithm
    /**
     * Returns an iterator which gives the tasks at a given start time
     * @param node
     * @return
     */
    public final Iterator<Task> taskIterator(final long startTime) {
	final ArrayList<Task> results = new ArrayList<Task>();

	final Iterator<Site> nodeIter = this.tasks.keySet().iterator();
	while (nodeIter.hasNext()) {
	    final Site node = nodeIter.next();

	    final Iterator<Task> taskIter = this.tasks.get(node).iterator();
	    while (taskIter.hasNext()) {
		final Task t = taskIter.next();

		if (t.getStartTime() == startTime) {
		    results.add(t);
		}
	    }
	}

	return results.iterator();
    }

    /**
     * Returns an iterator which gives all the start times
     * @return
     */
    public final Iterator<Long> startTimeIterator() {
	return this.startTimes.iterator();
    }

    public final long getFragmentTimerOffsetVal(final String fragID,
	    final String siteID) {
	final Site site = (Site) this.daf.getRoutingTree().getNode(siteID);
	final Iterator<Task> taskIter = this.taskIterator(site);
	while (taskIter.hasNext()) {
	    final Task task = taskIter.next();
	    if (task instanceof FragmentTask) {
		final FragmentTask fragTask = (FragmentTask) task;
		final Fragment frag = fragTask.getFragment();
		if (frag.getID().equals(fragID)
			&& (fragTask.getOccurrence() == 1)) {

		    return fragTask.getStartTime();
		}
	    }
	}

	return -1;
    }

    public final long getFragmentTimerRepeatVal(final String fragID) {
	final Fragment frag = this.daf.getFragment(fragID);

	if (frag.isLeaf()) {
	    return this.getAcquisitionInterval_bms();
	}

	return this.getAcquisitionInterval_bms() * this.beta;
    }

    public final long getCommTaskTimerOffsetVal(final String sourceID,
	    final String destID, final int mode) {
	Site site;

	if (mode == CommunicationTask.RECEIVE) {
	    site = (Site) this.daf.getRoutingTree().getNode(destID);
	} else {
	    site = (Site) this.daf.getRoutingTree().getNode(sourceID);
	}

	final Iterator<Task> taskIter = this.taskIterator(site);
	while (taskIter.hasNext()) {
	    final Task task = taskIter.next();
	    if (task instanceof CommunicationTask) {
		final CommunicationTask commTask = (CommunicationTask) task;
		if (commTask.getSourceID().equals(sourceID)
			&& commTask.getDestID().equals(destID)) {
		    return commTask.getStartTime();
		}
	    }
	}

	return -1;
    }

    public final long getCommTaskTimerRepeatVal() {
	return this.alpha * this.beta;
    }

    /**
     * Schedule the leaf fragments in a query plan.  These are executed bFactor times at the
     * acquisition frequency specified by the user  
     * @param plan
     * @param bFactor
     * @param agenda
     * @throws AgendaException
     */
    private void scheduleLeafFragments()
	    throws AgendaException {

	//First schedule the leaf fragments, according to the buffering factor specified 
	//Note: a separate task needs to be scheduled for each execution of a leaf fragment
	for (long n = 0; n < this.beta; n++) {
	    final long startTime = this.alpha * n;

	    //For each leaf fragment
	    final Iterator<Fragment> fragIter = daf.getLeafFragments()
		    .iterator();
	    while (fragIter.hasNext()) {
		final Fragment frag = fragIter.next();

		//For each site the fragment is executing on 
		final Iterator<Site> nodeIter = frag.getSites().iterator();
		while (nodeIter.hasNext()) {
		    final Site node = nodeIter.next();

		    try {

			this.addFragmentTask(startTime, frag, node, (n + 1));

		    } catch (final AgendaException e) {

			//TODO: Use Christian's cost functions for this
			final long taskDuration = new FragmentTask(startTime,
				frag, node, (n + 1), this.beta, this.daf)
				.getTimeCost(this.daf);

			//If time to run task before the next acquisition time:
			if (this.getNextAvailableTime(node,
				Agenda.INCLUDE_SLEEP)
				+ taskDuration <= startTime
				+ this.alpha) {
			    //TODO: change this to time synchronisation QoS
			    this.addFragmentTask(frag, node, (n + 1));
			} else {
			    throw new AgendaException(
				    "Aquisition interval is smaller than duration of acquisition fragments on node "
					    + node.getID());
			}
		    }
		}
	    }
	    //Go active uses the disactivated sleep to represent all nodes do nothing
	    //if ((Settings.NESC_DO_SNOOZE) && ((n+1) != bFactor)) {
	    if ((n + 1) != this.beta) {
		final long sleepStart = this
			.getLength_bms(Agenda.INCLUDE_SLEEP);
		final long sleepEnd = (this.alpha * (n + 1));
		this.addSleepTask(sleepStart, sleepEnd, false);
	    }
	}
    }

    /**
     * Schedule the non-leaf fragments.  Then are executed as soon as possible after the leaf fragments
     * have finished executing.
     * @param plan
     * @param factor
     * @param agenda
     * @throws AgendaException
     */
    private void scheduleNonLeafFragments()
	    throws AgendaException {

		long nonLeafStart = Long.MAX_VALUE;
	
		Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
		    final Site currentNode = siteIter.next();
		    final long startTime = this.getNextAvailableTime(currentNode,
			    Agenda.IGNORE_SLEEP);
		    if (startTime < nonLeafStart) {
		    	nonLeafStart = startTime;
		    }
	
		    //Schedule all fragment which have been allocated to execute on this node,
		    //ensuring the precedence conditions are met
		    scheduleSiteFragments(currentNode);
	
		    //Then Schedule any onward transmissions
		    scheduleOnwardTransmissions(currentNode);
		}
	
		//add radioOn and radioOff tasks
		insertRadioOnOffTasks();
		
		//Schedule final sleep
		scheduleFinalSleep(nonLeafStart);
    }

	private void scheduleSiteFragments(final Site currentNode)
			throws AgendaException {
		final Iterator<Fragment> fragIter = daf
		    .fragmentIterator(TraversalOrder.POST_ORDER);
		while (fragIter.hasNext()) {
		final Fragment frag = fragIter.next();
		if (currentNode.hasFragmentAllocated(frag) && (!frag.isLeaf())) {
		    this.addFragmentTask(frag, currentNode);
		}
		}
	}

	private void scheduleOnwardTransmissions(final Site currentNode)
			throws AgendaException {
		if (currentNode.getOutputs().length > 0) {
		final HashSet<ExchangePart> tuplesToSend = new HashSet<ExchangePart>();
		final Iterator<ExchangePart> exchCompIter = currentNode
			.getExchangeComponents().iterator();
		while (exchCompIter.hasNext()) {
		    final ExchangePart exchComp = exchCompIter.next();
		    if ((exchComp.getComponentType() == ExchangePartType.PRODUCER)
			    || (exchComp.getComponentType() == ExchangePartType.RELAY)) {
			tuplesToSend.add(exchComp);
		    }
		}

		if (tuplesToSend.size() > 0) {
		    this.appendCommunicationTask(currentNode, (Site) currentNode
			    .getOutput(0), tuplesToSend);
		}
		}
	}

	private void scheduleFinalSleep(long nonLeafStart) throws AgendaException {
		final long sleepStart = this.getLength_bms(Agenda.INCLUDE_SLEEP);	
		final long sleepEnd = Math.max(this.alpha * this.beta, sleepStart);
		logger.fine("Sleep task scheduled from "+sleepStart+" to "+sleepEnd);
		this.addSleepTask(sleepStart, sleepEnd, true);
		this.setNonLeafStart(nonLeafStart);
	}

	private void insertRadioOnOffTasks() {
		Iterator<Site> siteIter;
		siteIter = this.daf.getRoutingTree().siteIterator(TraversalOrder.POST_ORDER);
		int radioOnTimeCost = (int)CostParameters.getTurnOnRadio();
		int radioOffTimeCost = (int)CostParameters.getTurnOffRadio();
		
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			ArrayList<Task> siteTasks = this.tasks.get(site);
			for (int i = 0; i<siteTasks.size(); i++) {
				Task t = siteTasks.get(i);
				long startTime = t.startTime;
				if ((t instanceof CommunicationTask || t.isDeliverTask()) && !this.hasRadioOn(site, startTime)) {
					if (this.isFree(site, startTime - radioOnTimeCost, startTime)) {
						this.insertTask(startTime - radioOnTimeCost, site, 
								new RadioOnTask(startTime - radioOnTimeCost, site));
					} else {
						this.shift(startTime, radioOnTimeCost);
						this.insertTask(startTime, site, new RadioOnTask(startTime, site));
					}
				}
				if ((t instanceof CommunicationTask || t.isDeliverTask())&& (radioNextNeededTime(siteTasks, i+1)==-1 
						|| radioNextNeededTime(siteTasks, i+1) > t.endTime + (radioOnTimeCost*1.5))) {
					
					if (this.isFree(site, t.endTime, t.endTime+radioOffTimeCost)) {
						this.insertTask(t.endTime, site, new RadioOffTask(t.endTime, site));
					} else {
						this.shift(t.endTime, radioOffTimeCost);
						this.insertTask(t.endTime, site, new RadioOffTask(t.endTime, site));
					}
				}
			}
		}
	}

    
    /**
     * Given the list of tasks for a particular site, and a index to start searching, returns
     * the startTime of the next task which requires use of the radio.
     * If there is none, returns -1.
     * @param siteTasks
     * @param startTaskNum
     * @return
     */
    public static long radioNextNeededTime(ArrayList<Task> siteTasks, int startTaskNum) {
    	
    	//startTaskNum is beyond the last task; radio never needed again 
    	if (startTaskNum >= siteTasks.size()) {
    		return -1;
    	}
    	
    	for (int i=startTaskNum; i<siteTasks.size(); i++) {
    		Task t = siteTasks.get(i);
    		
    		//check if communication task or deliver task
    		if (t instanceof CommunicationTask || t.isDeliverTask()) {
    			return t.startTime;
    		}    		
    	}
    	
    	return -1;
    }
    
    
    /**
     * Display the agenda
     * @param plan
     */
    public final JFrame display(final String outputDirName,
	    final String outputFileName,
	    final String label) {
    	
    	DisplayAgenda agendaDisplay = new DisplayAgenda(this, this.daf, false);
		agendaDisplay.display(outputDirName, outputFileName+"_bms");
		
    	agendaDisplay = new DisplayAgenda(this, this.daf, true);
		return agendaDisplay.display(outputDirName, outputFileName+"_ms");
    }
    
    public final JFrame display(final String outputDirName, final String outputFileName) {
    	return display(outputDirName, outputFileName, "");
    }
    

    
    public final Agenda clone(final DAF clonedDAF) throws AgendaException, AgendaLengthException {
	final Agenda clone = new Agenda(this.alpha, 
			this.beta, clonedDAF, this.name + "-clone");

	//copy tasks, the list of tasks for each sensor network node
	final Iterator<Site> siteIter = this.tasks.keySet().iterator();
	while (siteIter.hasNext()) {
	    final Site site = siteIter.next();

	    final Site clonedSite = (Site) clonedDAF.getRoutingTree().getNode(
		    site.getID());
	    final ArrayList<Task> clonedTaskList = new ArrayList<Task>();

	    final Iterator<Task> taskIter = this.taskIterator(site);
	    while (taskIter.hasNext()) {
			final Task task = taskIter.next();
			final Task clonedTask = task.clone(clonedDAF);
			clonedTaskList.add(clonedTask);
	    }
	
	    clone.tasks.put(clonedSite, clonedTaskList);
	}

	//copy startTimes, the list of all start times (used to display schedule)
	final ArrayList<Long> clonedStartTimes = new ArrayList<Long>();
	final Iterator<Long> startTimeIter = this.startTimeIterator();
	while (startTimeIter.hasNext()) {
	    final Long clonedStartTime = new Long(startTimeIter.next()
		    .intValue());
	    clonedStartTimes.add(clonedStartTime);
	}
	clone.startTimes = clonedStartTimes;

	return clone;
    }

    public boolean isEquivalentTo(Object other) {
    	return false; //TODO
    }
    
	public String getProvenanceString() {
		if (this.daf != null) {
			return this.daf.getProvenanceString() + "->" + this.name;
		} else {
			return "";
		}
		
	}
	
	public double getScore() {
		return this.score;
	}
    
	public void setScore(double s) {
		this.score = s;
		
	}
	
	/**
	 * Returns the total total network energy in Joules according to model.
	 * @return
	 */
	public double getTotalEnergy() {
		double sumEnergy = 0;
		Iterator<Site> siteIter = this.getDAF().getRoutingTree().siteIterator(
				TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()){
			Site site = siteIter.next();
			if (site!=this.getDAF().getRoutingTree().getRoot()) {
				sumEnergy += getSiteEnergyConsumption(site);
			}
		}
		double agendaLength = bmsToMs(this.getLength_bms(false))/1000.0; // ms to s
		double energyConsumptionRate = sumEnergy/agendaLength; // J/s
		return energyConsumptionRate*15778463.0;
	}

	/**
	 * Returns the total site energy in Joules according to model.
	 * @param site
	 * @return
	 */
	private double getSiteEnergyConsumption(Site site) {
		double sumEnergy = 0;
		long cpuActiveTimeBms = 0;
		
		recordEnergyDetails("Energy details for node "+site.getID());
		recordEnergyDetails("============================\n\n");
		
		double sensorEnergy = 0;
		ArrayList<Task> siteTasks = this.tasks.get(site);
		for (int i=0; i<siteTasks.size(); i++) {
			Task t = siteTasks.get(i);
			if (t instanceof SleepTask) {
				continue;
			}
			
			cpuActiveTimeBms += t.getDuration();
			if (t instanceof FragmentTask) {
				FragmentTask ft = (FragmentTask)t;
				Fragment f = ft.getFragment();
				if (f.containsOperatorType(AcquireOperator.class)) {
					sensorEnergy += getSensorEnergyCost(f);
				}
				sumEnergy += sensorEnergy;
			} else if (t instanceof CommunicationTask) {
				CommunicationTask ct = (CommunicationTask)t;
				sumEnergy += getRadioEnergy(ct);
			} else if (t instanceof RadioOnTask) {
				double taskDuration = bmsToMs(t.getDuration())/1000.0;
				double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere(); 
				double voltage = AvroraCostParameters.VOLTAGE;
				double taskEnergy = taskDuration * radioRXAmp * voltage;				; 
				recordEnergyDetails("RadioOn energy: "+(taskEnergy*1000.0)+"mJ ("+
						radioRXAmp+"A * "+voltage+"V * "+taskDuration+"s)");
				sumEnergy += taskEnergy;
			}
		}
		
		recordEnergyDetails("Sensor energy: "+(sensorEnergy*1000.0)+"mJ");
		sumEnergy += getCPUEnergy(cpuActiveTimeBms);
		
		recordEnergyDetails("TOTAL Energy = "+(sumEnergy*1000.0)+" mJ\n\n");
		return sumEnergy;
	}

	/**
	 * Return the CPU energy cost for an agenda, in Joules.
	 * @param cpuActiveTimeBms
	 * @return
	 */
	private double getCPUEnergy(long cpuActiveTimeBms) {
		double agendaLength = bmsToMs(this.getLength_bms(false))/1000.0; //bms to ms to s
		double cpuActiveTime = bmsToMs(cpuActiveTimeBms)/1000.0; //bms to ms to s
		double cpuSleepTime = agendaLength - cpuActiveTime; // s
		double voltage = AvroraCostParameters.VOLTAGE;
		double activeCurrent = AvroraCostParameters.CPUACTIVEAMPERE;
		double sleepCurrent = AvroraCostParameters.CPUPOWERSAVEAMPERE;
		
		double cpuActiveEnergy = cpuActiveTime * activeCurrent * voltage; //J
		double cpuSleepEnergy = cpuSleepTime * sleepCurrent * voltage; //J
		
		recordEnergyDetails("CPU active energy="+(cpuActiveEnergy*1000.0)+"mJ ("+
				cpuActiveTime+"s * "+activeCurrent+"A * "+voltage+"V)");
		recordEnergyDetails("CPU sleep energy="+(cpuSleepEnergy*1000.0)+"mJ ("+
				cpuSleepTime+"s * "+sleepCurrent+"A * "+voltage+"V)");
		return cpuActiveEnergy + cpuSleepEnergy;
	}
		
	/**
	 * Returns radio energy for communication tasks (J) for agenda according to model.
	 * Excludes radio switch on.
	 * @param ct
	 * @return
	 */
	private double getRadioEnergy(CommunicationTask ct) {
		double taskDuration = bmsToMs(ct.getDuration())/1000.0;
		double voltage = AvroraCostParameters.VOLTAGE;
		
		double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere();
		if (ct.getMode()==CommunicationTask.RECEIVE) {
			 
			double taskEnergy = taskDuration*radioRXAmp*voltage; 
			recordEnergyDetails("Commtask "+ct.toString()+
					": Adding "+(taskEnergy*1000.0)+
					"mJ rx energy ("+radioRXAmp+"A * "+
					voltage+"V * "+ taskDuration+"s)");
			return taskEnergy;
		}
		Site sender = ct.getSourceNode();
		Site receiver = (Site)sender.getOutput(0);
		int txPower = (int)this.getDAF().getRoutingTree().getLinkEnergyCost(sender, receiver);
		double radioTXAmp = AvroraCostParameters.getTXAmpere(txPower);
		
		HashSet<ExchangePart> exchComps = ct.getExchangeComponents();
		AvroraCostExpressions  costExpressions = 
			(AvroraCostExpressions)CostExpressions.costExpressionFactory(daf);
		AlphaBetaExpression txTimeExpr = AlphaBetaExpression.multiplyBy(
				costExpressions.getPacketsSent(exchComps, true),
				AvroraCostParameters.PACKETTRANSMIT);
		double txTime = (txTimeExpr.evaluate(alpha, beta))/1000.0;
		double rxTime = taskDuration-txTime;
		assert(rxTime>=0);
		
		double txEnergy = txTime*radioTXAmp*voltage; 
		recordEnergyDetails("Commtask "+ct.toString()+
				": Adding TX "+(txEnergy*1000.0)+
				"mJ ("+radioTXAmp+"A * "+voltage+"V * "+
				txTime+"s)");
		double rxEnergy = rxTime*radioRXAmp*voltage; 
		recordEnergyDetails("Commtask "+ct.toString()+
				": Adding RX "+(rxEnergy*1000.0)+
				"mJ ("+radioRXAmp+"A * "+voltage+"V * "+
				rxTime+"s)");
		return (txEnergy+rxEnergy);	
	}

	
	/**
	 * Returns sensor energy cost (J) for agenda according to model.
	 * @param f
	 * @return
	 */
	private double getSensorEnergyCost(Fragment f) {
		//accelerometer in TinyDB assumed
		//0.9ms/sample, 17ms startup time, 0.6mA current, 3V
		//(0.0009 milliseconds + 0.017 milliseconds)* 0.6 milliamps * 3 volts 
		// =3.22200e-8 Joules
		//TODO: This is a cut corner... (1) Need to consider sensor attributes 
		//in acquire and (2) adjust the length of this during agenda creation.
		//recordEnergyDetails("Fragment "+f.getID()+": Adding 0.0001074J sensor energy");
		return 0.00000003222;
	}

	
	private ArrayList<Triple<Site,Double,Double>> siteRanking = 
		new ArrayList<Triple<Site,Double,Double>>();  
	
	/**
	 * Returns the network lifetime in seconds according to model.
	 * @return
	 */
	public double getLifetime() {
		double shortestLifetime = Double.MAX_VALUE; //s
				
		Iterator<Site> siteIter = this.getDAF().getRoutingTree().siteIterator(
				TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			if (site!=this.getDAF().getRoutingTree().getRoot()) {
				
				double siteEnergySupply = site.getEnergyStock()/1000.0; // mJ to J 
				double siteEnergyCons = this.getSiteEnergyConsumption(site); // J
				double agendaLength = bmsToMs(this.getLength_bms(false))/1000.0; // ms to s
				double energyConsumptionRate = siteEnergyCons/agendaLength; // J/s
				double siteLifetime = siteEnergySupply / energyConsumptionRate; //s
				recordLifeTimeRank(site, siteEnergyCons, siteLifetime);
			
				shortestLifetime = Math.min((double)shortestLifetime, siteLifetime);
			}
		}
		
		generateLifetimeRankFile();
		return shortestLifetime;
	}

	private void generateLifetimeRankFile() {
	    String fname = QueryCompiler.queryPlanOutputDir+this.getName()+"-model-lifetime-rank.csv";
		try {
			PrintWriter  out = new PrintWriter(new BufferedWriter(
				    new FileWriter(fname)));
		    out.println("site-id,site-epsilon,site-lambda");
		    
		    for (int i=0; i<siteRanking.size(); i++) {
		    	Triple<Site, Double, Double> elem = siteRanking.get(i);
		    	String siteID = elem.getFirst().getID();
		    	double energy = elem.getSecond();
		    	double lifetime = elem.getThird();
		    	double lifetimeInDays = Utils.convertSecondsToDays(lifetime);
		    	out.println(siteID+","+energy+","+lifetimeInDays);
		    }		    
		    
		    out.close();
		} catch (IOException e) {
			Utils.handleCriticalException(e);
		}	
	}

	private void recordLifeTimeRank(Site site, double siteEnergyCons, double siteLifetime) {

		Triple<Site, Double, Double> newElem = 
			new Triple<Site, Double, Double>(site, siteEnergyCons, siteLifetime);
		boolean added = false;
		
		for (int i=0; i<siteRanking.size(); i++) {
			Triple<Site, Double, Double> currentElem = siteRanking.get(i);
			if (currentElem.getThird()>siteLifetime) {
				siteRanking.add(i,newElem);
				added = true;
				break;
			}
		}
		
		if (added==false) {
			siteRanking.add(newElem);
		}
	}
	
	
	/**
	 * Records the energy calculation details.
	 * @param str
	 */
	private void recordEnergyDetails(String str) {
		String fname = QueryCompiler.queryPlanOutputDir+this.getName()+"-model-energy-breakdown.txt";
		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
				    new FileWriter(fname, true)));
			out.println(str);
			out.close();
		} catch (IOException e) {
			Utils.handleCriticalException(e);
		}
	}
	
	public void setCVXPi(double cvxPi) {
		this.cvxPi = cvxPi;
	}

	public void setCVXEpsilon(double cvxEpsilon) {
		this.cvxEpsilon = cvxEpsilon;
	}
	
	public void setCVXLambdaDays(double cvxLambdaDays) {
		this.cvxLambdaDays = cvxLambdaDays;
	}
	
	public double getCVXPi() {
		return this.cvxPi;
	}

	public double getCVXEpsilon() {
		return this.cvxEpsilon;
	}
	
	public double getCVXLambdaDays() {
		return this.cvxLambdaDays;
	}
}
