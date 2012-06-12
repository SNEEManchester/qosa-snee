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
package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.rewriting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.LookupException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.LAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggregationOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.JoinOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ProjectOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ScanOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.SelectOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.WindowOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Aggregate;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.OldAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Element;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Literal;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Relation;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.RowWindow;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Source;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Stream;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.SubQuery;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.TimeWindow;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Translator;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Variable;

/**
 * The Logical Optimiser.
 *  
 *
 * Invoke the static method generatePlan to execute this
 * component.
 * @author Christian Brenninkmeijer and Steven Lynden 
 */
public class LogicalOptimiser {
    private static final Logger logger = Logger
	    .getLogger(LogicalOptimiser.class.getName());

    /*
     * The translated query.
     */
    private Translator query;

    /*
     * The root operator of the query plan.
     */
    private Operator rootOperator;

    /*
     * Relations not yet scanned by the query plan.
     */
    private Collection<Source> remainingSources;

    /*
     * Current estimated cardinality of the query plan.
     */
    private int currentCardinality;

    /*
     * Optimised sub-queries
     */
    private Hashtable<String, SubQueryPlan> subQueries;

    private Hashtable<String, Long> cardinalities;

    private Hashtable<String, Element> windows;

    private ArrayList<Predicate> loosePredicates;

    private ArrayList<Predicate> joinablePredicates;

    private CardinalityCalculator cardinalityCalculator;

    private long acquisitionInterval;

    /**
     * Generate a logical query plan from a translated query.
     */
    public static LAF generateLogicalPlan(final Translator query,
	    final CardinalityCalculator cardinalityCalculator,
	    final long acquisitionInterval,
	    final String queryName) {
	try {
	    logger.finer("Generating logical query plan");
	    final LogicalOptimiser optimiser = new LogicalOptimiser(query,
		    cardinalityCalculator, acquisitionInterval);
	    optimiser.processSubQueries();
	    logger.finer("LogicalOptimiser: " + query);
	    optimiser.initialise();
	    optimiser.iterate();
	    optimiser.finalise();
	    optimiser.rootOperator = new DeliverOperator(optimiser.rootOperator);
	    
	    LAF laf = new LAF(optimiser.rootOperator, queryName, acquisitionInterval);

	    logger.fine("plan: " + laf.toString());
        if (Settings.DISPLAY_LAF) {
        	laf.display(QueryCompiler.queryPlanOutputDir, laf.getName());
        }
	    
	    return laf;
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	    return null;
	}
    }

    /*
     * Generate the plan for a sub-query.
     */
    private SubQueryPlan optimiseSubQuery(final SubQuery subQuery)
	    throws OptimizationException {
	logger.finer("Generating logical query plan for sub-query "
		+ subQuery.getName());
	final LogicalOptimiser optimiser = new LogicalOptimiser(subQuery
		.getQuery(), this.cardinalityCalculator, acquisitionInterval);
	optimiser.processSubQueries();
	try {
	    optimiser.initialise();
	    optimiser.iterate();
	} catch (final LookupException e) {
	    throw new OptimizationException("Exception accessing metadata", e);
	}
	optimiser.finalise();
	this.cardinalities.put(subQuery.getName(), new Long(
		optimiser.currentCardinality));
	logger.finer("Logical plan for sub-query " + subQuery.getName() + " "
		+ optimiser.rootOperator.toString());
	return new SubQueryPlan(optimiser.rootOperator,
		optimiser.currentCardinality, subQuery);
    }

    /*
     * Create the optimiser object.
     */
    private LogicalOptimiser(final Translator query,
	    final CardinalityCalculator cardinalityCalculator,
	    final long acquisitionInterval) {
	this.query = query;
	this.rootOperator = null;
	this.currentCardinality = 0;
	this.subQueries = new Hashtable<String, SubQueryPlan>();
	this.remainingSources = query.getSources();
	this.loosePredicates = query.getPredicates();
	this.cardinalities = new Hashtable<String, Long>();
	this.windows = query.getWindows();
	this.cardinalityCalculator = cardinalityCalculator;
	this.acquisitionInterval = acquisitionInterval;
    }

    /*
     * Optimises all sub queries, placing the resulting
     * plans in a hashtable for future use.
     */
    private void processSubQueries() throws OptimizationException {
	final Collection<SubQuery> queries = this.query.getSubQueries();
	final Iterator it = queries.iterator();
	while (it.hasNext()) {
	    final SubQuery sq = (SubQuery) it.next();
	    this.subQueries.put(sq.getName(), this.optimiseSubQuery(sq));
	}
    }

    private ArrayList<Predicate> findApplicablePredicates(final Source rel) {
	final String source = rel.getName();
	final ArrayList<Predicate> found = new ArrayList<Predicate>(
		this.loosePredicates.size());
	for (int i = 0; i < this.loosePredicates.size(); i++) {
	    final Predicate pred = this.loosePredicates.get(i);
	    final Variable var1 = pred.getArgument1();
	    final String source1 = var1.getSourceReference();
	    final Variable var2 = pred.getArgument2();
	    final String source2 = var2.getSourceReference();
	    logger.finest(var1.toString() + "=" + source1 + "  "
		    + var2.toString() + "=" + source2 + " to " + source);
	    if (var1 instanceof Literal) {
		if (source2.equalsIgnoreCase(source)) {
		    logger.finest("var1 = Literal, source2 = source");
		    found.add(pred);
		}
	    } else if (var2 instanceof Literal) {
		if (source1.equalsIgnoreCase(source)) {
		    logger.finest("var2 = Literal, source1:" + source1
			    + " = source:" + source);
		    found.add(pred);
		}
	    } else if (source1.equalsIgnoreCase(source)
		    && source2.equalsIgnoreCase(source)) {
		logger.finest("source1+2 = source");
		found.add(pred);
	    }
	}
	logger.finest("found= " + found.toString());
	return found;
    }

    private ArrayList<Predicate> findJoinablePredicates(
	    final ArrayList<Predicate> predicates, final Source rel) {
	final String source = rel.getName();
	final ArrayList<Predicate> found = new ArrayList<Predicate>(predicates
		.size());
	logger.finest("Source = " + source);
	logger.finest("predicates = " + predicates);
	for (int i = 0; i < predicates.size(); i++) {
	    //logger.finest("predicate = "+predicates.get(i));
	    final Predicate pred = predicates.get(i);
	    if (source.equalsIgnoreCase(pred.getArgument1()
		    .getSourceReference())
		    || source.equalsIgnoreCase(pred.getArgument2()
			    .getSourceReference())) {
		found.add(pred);
	    }
	}
	logger.finest("found " + found);
	return found;
    }

    private void removePredicates(final ArrayList<Predicate> big,
	    final ArrayList<Predicate> small) {
	//logger.finest("to remove"+small);
	for (int i = 0; i < small.size(); i++) {
	    if (!big.remove(small.get(i))) {
		logger.warning("Incorrect attempt to remove predicate "
			+ small.get(i) + " from list " + big);
		//else
		//	logger.finest("removed "+small.get(i));
	    }
	}
	//logger.finest("returning "+big);
	//return big;
    }

    private ArrayList<Predicate> addPredicates(final ArrayList<Predicate> big,
	    final ArrayList<Predicate> small) {
	final ArrayList<Predicate> temp = new ArrayList<Predicate>();
	temp.addAll(big);
	for (int i = 0; i < small.size(); i++) {
	    temp.add(small.get(i));
	}
	return temp;
    }

    /*
     * Choose a relation to scan and insert the operator.
     */
    private void initialise() throws LookupException, OptimizationException {
	Source bestCandidate = null;
	ArrayList<Predicate> applicablePreds = null;
	int bestCard = Integer.MAX_VALUE;
	logger.finest("remainingSources =" + this.remainingSources);
	final Iterator it = this.remainingSources.iterator();
	while (it.hasNext()) {
	    final Source r = (Source) it.next();
	    final String relationName = r.getName();
	    final Long subCard = this.cardinalities.get(relationName);
	    int card;
	    if (subCard == null) {
		card = this.query.lookupTableCardinality(relationName);
		this.cardinalities.put(relationName, new Long(card));
	    } else {
		card = subCard.intValue();
	    }
	    final ArrayList<Predicate> preds = this.findApplicablePredicates(r);
	    card = this.cardinalityCalculator.estimateCardinality(card, preds);
	    if (card <= bestCard) {
		bestCard = card;
		bestCandidate = r;
		applicablePreds = preds;
	    }
	}
	this.currentCardinality = bestCard;
	this.remainingSources.remove(bestCandidate);
	final String bestName = bestCandidate.getName();
	final SubQueryPlan sq = this.subQueries.get(bestName);
	if (sq == null) {
	    logger.finer("Initialising query plan with scan("
		    + bestCandidate.getTableName() + ")"
		    + ", Estimated cardinality = " + this.currentCardinality);
	    if (bestCandidate instanceof Stream) {
		this.rootOperator = new AcquireOperator((Stream) bestCandidate,
				acquisitionInterval);
	    } else if (bestCandidate instanceof Relation) {
		this.rootOperator = new ScanOperator((Relation) bestCandidate);
	    } else {
		throw new OptimizationException("Unexpected source");
	    }
	} else {
	    this.rootOperator = sq.plan;
	    logger.finer("Initialising query plan with:" + this.rootOperator);
	    logger.finer("Estimated cardinality = " + this.currentCardinality);
	}
	this.rootOperator = this.applyWindowAndSelection(this.rootOperator,
		bestName, applicablePreds);
	this.removePredicates(this.loosePredicates, applicablePreds);
	this.joinablePredicates = this.findJoinablePredicates(
		this.loosePredicates, bestCandidate);
	this.removePredicates(this.loosePredicates, this.joinablePredicates);
	logger.finest("loosePredicates =" + this.loosePredicates);
	logger.finest("joinablePredicates =" + this.joinablePredicates);
    }

    /* Olde version
     private Operator applyWindowAndSelection(Operator inner, String source, Collection<Predicate> applicablePreds)
     {
     logger.finest("In ApplyWindowAndSelction "+inner+" with "+applicablePreds);
     Element window = getWindow(source);
     if (window == null)
     {
     if (! applicablePreds.isEmpty())
     if (inner.acceptsPredicates())
     {
     try
     {
     inner.setPredicates(applicablePreds);
     return inner;
     }
     catch (OptimisationException e)
     {
     Utils.handleCriticalException(e);
     return null;
     }
     }
     else
     {
     logger.finest("Inner does not accept predicates");
     return new LogicalSelectOperator(inner, applicablePreds);
     }
     else
     return inner;
     }
     else if (window instanceof TimeWindow)
     {
     if (! applicablePreds.isEmpty())
     inner = new LogicalSelectOperator(inner, applicablePreds);
     return new LogicalTimeWindowOperator (inner,(TimeWindow)window, qos);
     }
     else //row window
     {
     inner = new LogicalRowWindowOperator (inner,(RowWindow)window);
     if (! applicablePreds.isEmpty())
     return new LogicalSelectOperator(inner, applicablePreds);
     else
     return inner;
     }
     }
     */

    private Operator applyWindowAndSelection(Operator inner,
	    final String source, final Collection<Predicate> applicablePreds) {
	logger.finest("In ApplyWindowAndSelction " + inner + " with "
		+ applicablePreds);
	final Element window = this.getWindow(source);
	//check if window allows selection to be pushed below it
	if ((window == null) || (window instanceof TimeWindow)) {
	    if (!applicablePreds.isEmpty()) {
		    if (inner.acceptsPredicates()) {
		        try {
                    inner.setPredicates(applicablePreds);
		        } catch (final OptimizationException e) {
			        Utils.handleCriticalException(e);
			        return null;
		        }
		    } else {
		        logger.finest("Inner does not accept predicates");
		        inner = new SelectOperator(inner, applicablePreds);
		    }
	    }

	    if (window instanceof TimeWindow) {
		return new WindowOperator(inner, (TimeWindow) window,
				acquisitionInterval);
	    } else {
		    return inner;
	    }
	} else { //row window
	    inner = new WindowOperator(inner, (RowWindow) window);
	    if (!applicablePreds.isEmpty()) {
		    return new SelectOperator(inner, applicablePreds);
	    } else {
		    return inner;
	    }
	}
    }

    private Element getWindow(final String name) {
	final Element window = this.windows.get(name);
	return window;
    }
 
    /*
     * Add a scan+join, subQuery+join or function call to the current plan
     */
    private void iterate() throws OptimizationException, LookupException {
	while (!(this.remainingSources.isEmpty())) {
	    logger.finest("iterate" + this.remainingSources);
	    Source bestCandidate = null;
	    ArrayList<Predicate> bestScanPreds = null;
	    ArrayList<Predicate> bestJoinPreds = null;
	    int rightInputCard = Integer.MAX_VALUE;
	    //remembers the card of the best candidate
	    int bestCard = Integer.MAX_VALUE;
	    //first consider scanning each of the remaining relations and planting a join
	    final Iterator it = this.remainingSources.iterator();
	    while (it.hasNext()) {
		final Source r = (Source) it.next();
		final String sourceName = r.getName();
		//relations that are also subqueries are treated as subqueries
		int card = (this.cardinalities.get(sourceName)).intValue();
		final ArrayList<Predicate> scanPreds = this
			.findApplicablePredicates(r);
		logger.finest("Joinable =" + this.joinablePredicates);
		final ArrayList<Predicate> joinPreds = this
			.findJoinablePredicates(this.joinablePredicates, r);
		rightInputCard = this.cardinalityCalculator
			.estimateCardinality(card, scanPreds);
		// this needs to be remembered as the right input cardinality
		card = this.cardinalityCalculator.estimateJoinCardinality(
			this.currentCardinality, rightInputCard, joinPreds, r);
		logger.finest("JoinPreds =" + joinPreds);
		if (card <= bestCard) {
		    bestCard = card;
		    bestCandidate = r;
		    bestJoinPreds = joinPreds;
		    bestScanPreds = scanPreds;
		}
	    }
	    Operator right;
	    final String varName = bestCandidate.getName();
	    if (bestCandidate instanceof SubQuery) {
		final SubQueryPlan subplan = this.subQueries.get(varName);
		logger.finer("Adding subQuery " + subplan + " applying "
			+ bestJoinPreds.size() + " predicates"
			+ ", Estimated cardinality = " + bestCard);
		right = subplan.plan;
	    } else if (bestCandidate instanceof Relation) {
		final Relation r = (Relation) bestCandidate;
		logger.finer("Adding relation " + r.getTableName()
			+ " applying " + bestJoinPreds.size() + " predicates"
			+ ", Estimated cardinality = " + bestCard);
		right = new ScanOperator(r);
		// 	cardinalities.get(r.getName());
	    } else if (bestCandidate instanceof Stream) {
		final Stream s = (Stream) bestCandidate;
		logger.finer("Adding stream " + s.getTableName() + " applying "
			+ bestJoinPreds.size() + " predicates"
			+ ", Estimated cardinality = " + bestCard);
		right = new AcquireOperator(s, acquisitionInterval);
		// 	cardinalities.get(r.getName());
	    } else {
		throw new OptimizationException("Unexpected source");
	    }
	    right = this.applyWindowAndSelection(right, varName, bestScanPreds);
	    final JoinOperator join = new JoinOperator(this.rootOperator,
		    right, bestJoinPreds);
	    this.rootOperator = join;
	    this.remainingSources.remove(bestCandidate);
	    this.currentCardinality = bestCard;
	    this.removePredicates(this.loosePredicates, bestScanPreds);
	    this.removePredicates(this.joinablePredicates, bestJoinPreds);
	    final ArrayList<Predicate> newJoinPreds = this
		    .findJoinablePredicates(this.loosePredicates, bestCandidate);
	    logger.finest("newJoinPreds =" + newJoinPreds);
	    this.removePredicates(this.loosePredicates, newJoinPreds);
	    this.joinablePredicates = this.addPredicates(this.loosePredicates,
		    newJoinPreds);
	    logger.finest("loosePredicates =" + this.loosePredicates);
	    logger.finest("joinablePredicates =" + this.joinablePredicates);

	}
    }

    /*
     * Finalise the plan by adding any aggregate operator
     * and a reduce operator to select the result tuple.
     */
    private void finalise() {
    	if (this.loosePredicates.size() > 0) {
    		logger.severe("Not all loose predicates used.");
    		logger.severe(this.loosePredicates.toString());
    	}
    	if (this.joinablePredicates.size() > 0) {
    		logger.severe("Not all joinable predicates used.");
    		logger.severe(this.joinablePredicates.toString());
    	}
    	final HashSet<Aggregate> aggregates = this.query.getAggregates();
    	final Collection<OldAttribute> groupByList = this.query.getGroupByList();
    	if (aggregates.size() > 0) {
    		this.rootOperator = new AggregationOperator(this.rootOperator,
    				groupByList, aggregates);
    		System.out.println ("here");
	    //	TODO change cardinality
    	} else {
    		final Collection<Variable> selectList = this.query.getResultTuple();
    		logger.finest("list = " + selectList + selectList.size());
    		if (selectList.size() != 0) {
    			this.rootOperator = new ProjectOperator(this.rootOperator,
    				selectList);
    		}
    	}
    	this.rootOperator = this.query.getType().addOperator(this.rootOperator);
    }
}

/**
 * Represents an optimised sub-query plan. Used during logical optimisation
 * to store the query plans of already optimised sub-queries.
 * @author Christian Brenninkmeijer and Steven Lynden
 */
class SubQueryPlan {
    Operator plan; //the root operator of the plan

    //SubQuery query;//the translated sub-query
    //int cardinality;//estimated cardinality of the plan
    SubQueryPlan(final Operator plan, final int cardinality,
	    final SubQuery query) {
	this.plan = plan;
	//this.cardinality = cardinality;
	//  this.query = query;
    }
}
