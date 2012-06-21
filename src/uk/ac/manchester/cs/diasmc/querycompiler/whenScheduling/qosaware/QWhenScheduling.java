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
package uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostExpressions;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.OptimizationType;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSVariable;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Agenda;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.CommunicationTask;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePart;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePartType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.CardinalityType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXConstraint;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXProblem;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXSolution;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXSolutionStatus;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.MIGPSolver;

public class QWhenScheduling {
	
    static HashMap<Site, AlphaBetaExpression> siteEnergyConsumptions = new HashMap<Site, AlphaBetaExpression>();
    
    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger(QWhenScheduling.class.getName());

    /**
     * Generates an expression for the sum of energy consumptions of all nodes in the query plan.
     * @param daf				the query plan daf
     * @param costExpressions	energy cost models
     * @return the total energy consumption
     */
    private static AlphaBetaExpression computeAllSiteEnergyConsumption(final DAF daf, AvroraCostExpressions costExpressions,
    		HashMap<String,HashMap<String, AlphaBetaExpression>> debug) {

        final AlphaBetaExpression allSitesEnergyConsumption = AlphaBetaExpression.ZERO();
        
		final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.PRE_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			
			HashMap<String, AlphaBetaExpression> siteDebug = new HashMap<String, AlphaBetaExpression>();
			debug.put(site.getID(), siteDebug);
			
			//CB The true tells cost expressions to add rounding so estimates are never under.
			AlphaBetaExpression siteEnergyCost = costExpressions.getSiteEnergyExpression(site, true, siteDebug);

			//siteEnergyCost = 
			//	AlphaBetaExpression.divideByConstant(siteEnergyCost,7.5);
			
			siteEnergyConsumptions.put(site, siteEnergyCost);
			
			//This code has been commented out because it causes 
			//cvx-lambda to be grossly underestimated.  It is not
			//clear to me why this happens, as this should only affect
			//epsilon, but have commented this for quick fix.
			
			//if (site!=daf.getRoutingTree().getRoot()) {
			//	System.err.println(site.getID()+" - "+siteEnergyCost.toString());
				allSitesEnergyConsumption.add(siteEnergyCost);
			//	System.err.println(site.getID()+" - "+siteEnergyCost.toString());
			//}
		}

		return allSitesEnergyConsumption;
    }
    

    /**
     * Given the energy consumption corresponding to an agenda evaluation 
     * (either for a single site, or a group of sites) returns an expression
     * denoting the power consumption.
     * @param energyConsumption
     * @return powerConsumption
     */
    private static AlphaBetaExpression computePowerConsumption(AlphaBetaExpression energyConsumption) {
    	AlphaBetaExpression powerConsumption = AlphaBetaExpression.divideByAlphaBeta(energyConsumption);
		return powerConsumption;
    }
	
    
    /**
     * Performs the QoS-aware when-scheduling.
     * @param daf
     * @param qos
     * @param queryName
     * @return
     * @throws OptimizationException
     * @throws AgendaException
     */
    public static Agenda doWhenScheduling(
    		final DAF daf, 
    		final QoSSpec qos,
    		final String queryName)
	    throws OptimizationException, AgendaException {

    	/**
    	 * Initialization stuff
    	 */
    	AvroraCostExpressions costExpressions = new AvroraCostExpressions(daf);	
		if (Settings.DISPLAY_COST_EXPRESSIONS) {
	    	costExpressions.display();
		}

		HashMap<String,HashMap<String, AlphaBetaExpression>> cvxDebug = new HashMap<String,HashMap<String, AlphaBetaExpression>>();
		
    	//populate global hash table with this information for later use
		AlphaBetaExpression allSitesEnergyConsumption = computeAllSiteEnergyConsumption(daf, costExpressions, cvxDebug); 
		AlphaBetaExpression allSitesPowerConsumption = computePowerConsumption(allSitesEnergyConsumption);
		
		/**
		 * Optimization goal
		 */
		OptimizationType optType = getOptimizationType(qos);
		ArrayList<AlphaBetaExpression> optExpr = getOptimizationExpr(qos, allSitesPowerConsumption, daf);

		//Instantiate CVX problem with the optimization type/goal
		//If null, this is a feasibility problem rather than an optimization problem
		final CVXProblem problem = new CVXProblem(optType, optExpr);
		
		/**
		 * Add QoS Constraints
		 */
		addAcquisitionIntervalConstraint(qos, problem);
		addBufferingFactorConstraint(qos, problem);		
		addDeliveryTimeConstraint(qos, problem);
		addTotalEnergyConstraint(qos, allSitesPowerConsumption, problem);
		addNetworkLifetimeConstraint(daf, qos, problem);

		/**
		 * Add other Constraints 
		 */
		addOtherConstraints(daf, problem, costExpressions);

		problem.setLamba_1(getLifetimeExpression(daf));
		
		/**
		 * Solve the problem by delegating it to the CVX constraint solver 
		 */
		CVXSolution solution = MIGPSolver.solve(problem);
		Agenda agenda = null;
		if (solution.getStatus() == CVXSolutionStatus.SOLVED) {
			
			//Now attempt to construct an agenda with the 
			//alpha and beta parameters given by the solver.
			agenda = constructAndScoreAgenda(daf, queryName, problem, solution);
			
		}

		//Output CVX problem and solution as PDF
		displayCVXProblem(problem, solution, daf);
		
		displayCVXDebugInfo(agenda, cvxDebug);
		
		return agenda;
    }

    
    private static void displayCVXDebugInfo(Agenda agenda,
			HashMap<String, HashMap<String, AlphaBetaExpression>> cvxDebug) {
		
    	if (agenda==null) {
    		return;
    	}
    	
		String filename = QueryCompiler.queryPlanOutputDir + agenda.getName() + "-cvx-cost-estimates.txt";
		double alpha = agenda.getAcquisitionInterval_ms();
		double beta = agenda.getBufferingFactor();
		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
				    new FileWriter(filename, true)));
			
			Iterator<Site> rtIter = agenda.getDAF().getRoutingTree().siteIterator(TraversalOrder.POST_ORDER);
			while (rtIter.hasNext()) {
				Site site = rtIter.next();
				String siteID  = site.getID();
				HashMap<String, AlphaBetaExpression> siteDebug = cvxDebug.get(siteID);
				
				out.println("Site "+siteID);
				out.println("=============");
				
				Iterator<String> elementIter = siteDebug.keySet().iterator();
				while (elementIter.hasNext()) {
					String element = elementIter.next();
					AlphaBetaExpression ab = siteDebug.get(element);
					
					double val = ab.evaluate(alpha, beta);
					out.println(element+" = "+val);
				}
				out.println("\n");
			}			
			
			
			out.close();
		} catch (IOException e) {
			Utils.handleCriticalException(e);
		}
		
	}


	/**
     * Displays the CVX problem formulation on the console, and produces a PDF with the problem formulation.
     * @param problem
     * @param solution
     * @param daf
     */
	private static void displayCVXProblem(CVXProblem problem, CVXSolution solution, DAF daf) {
		System.out.println(problem.toString());
		
		String filename = QueryCompiler.queryPlanOutputDir + daf.getName() + "-constraint-problem.tex";
		String provenanceStr = daf.getProvenanceString().replace("->", "$\\rightarrow$");
		
		problem.exportToPDF(filename, 
			solution,
			provenanceStr);			
	}


	/**
	 * Constructs an agenda with the alpha and beta parameters given 
	 * by the solver.  Exits if agenda is invalid, as this is a bug which 
	 * should not happen.  Assigns a score to the agenda based on the 
	 * optimization variable value obtained.
	 * 
	 */
	private static Agenda constructAndScoreAgenda(final DAF daf,
			final String queryName, CVXProblem problem, CVXSolution solution) {
		
		//Update the evaluation rate (=acquisition rate), as this affects cardinalties
		//and hence the time cost models
		daf.setAcquisitionInterval((long)(solution.getAlpha()));
		
		Agenda agenda = null;
		try {
		    agenda = new Agenda((long) (solution.getAlpha()), 
		    		(long) solution.getBeta(), 
		    		daf, 
		    		queryName);
		} catch (Exception e) {
			displayCVXProblem(problem, solution, daf);
			Utils.handleCriticalException(e);
	}
		
		//Assign the agenda a score equal to the reciprocal of the 
		//opt_val (the optimization value) obtained by solver.
		if (problem.getOptimizationType() != null) {
			double score = (1 / solution.getOpt_val());
			agenda.setScore(score);
		}
		
		//Assign the agenda a score equal to the reciprocal of the 
		//opt_val (the optimization value) obtained by solver.
		if (problem.getOptimizationType() != null) {
			double score = (1 / solution.getOpt_val());
			agenda.setScore(score);
		}
		
		agenda.setCVXPi(solution.getPi());
		agenda.setCVXEpsilon(solution.getEpsilon());
		agenda.setCVXLambdaDays(solution.getLambdaDays());	
		
		return agenda;
	}


    /**
     * Add the constraints which enforce system properties and consistency conditions.
     * @param daf The DAF for which the constraints are being computed for
     * @param problem The CVX problem to add the constraint to
     */
	private static void addOtherConstraints(DAF daf, final CVXProblem problem, 
			CostExpressions costExpressions) {
		AlphaBetaExpression pi = computePi(daf);
		CVXConstraint constraint = new CVXConstraint(AlphaBetaExpression.ALPHA(),
				CVXOperator.GREATER_EQUALS, pi,
				"Acquisition interval >= duration of tasks in last epoch");
		problem.addDerivedConstraint(constraint);
		problem.setPi(pi);
	
		deriveMemoryConstraint(daf, problem, costExpressions);
		
		// Add consistency condition constraints
		constraint = new CVXConstraint(AlphaBetaExpression.BETA(), 
			CVXOperator.GREATER_EQUALS, new AlphaBetaExpression(1),
			"Buffering factor >= 1");
		problem.addConsistencyCondition(constraint);
	}


	/**
	 * Adds a memory constraint for all sites, by taking the most restrictive constraint, to the problem.
	 * @param daf	The query plan DAF
	 * @param problem The constraint problem being constructed.
	 * @param costExpressions The cost model expressions.
	 */
	private static void deriveMemoryConstraint(DAF daf,
			final CVXProblem problem, CostExpressions costExpressions) {
		CVXConstraint constraint;
		//Memory expression for each site
		//This is simplified into a single expression to avoid the constraint solver from bombing out
		double allSitesMaxBeta = Double.MAX_VALUE;
		Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();
			AlphaBetaExpression siteMemoryConsumption = 
				costExpressions.getSiteMemoryExpression(site, true);
			long siteMemoryStock = site.getRAM();
			if (site.getID().equals(daf.getRoutingTree().getRoot().getID())) {
				continue;
			}
			
			//Memory expression must be a Beta expression
			assert(siteMemoryConsumption.isBetaExpression());
			
			//Check if current site requires beta to be smaller
			double maxBeta = (siteMemoryStock - siteMemoryConsumption.getConstant()) 
				/ siteMemoryConsumption.getBetaCoeff();
			if (maxBeta < allSitesMaxBeta) {
				allSitesMaxBeta = maxBeta; 
			}			
		}
		
		//add single constraint for all sites
		constraint = new CVXConstraint(AlphaBetaExpression.BETA(), 
				CVXOperator.LESS_EQUALS, new AlphaBetaExpression(allSitesMaxBeta), 
				"memory constraint");
		problem.addDerivedConstraint(constraint);
	}


    /**
     * Adds a constraint relating to the acquisition interval QoS specified by the user.
     * @param qos	the user Quality-of-service specification.
     * @param problem 	the problem to be passed to the CVX solver.
     */
	private static void addAcquisitionIntervalConstraint(final QoSSpec qos,
			final CVXProblem problem) {
		problem.addDerivedRangeConstraint(AlphaBetaExpression.ALPHA(),
			qos.getMinAcquisitionInterval(), qos
				.getMaxAcquisitionInterval(), "acquisition interval");
	}


    /**
     * Adds a constraint relating to the buffering factor QoS specified by the user.
     * @param qos	the user Quality-of-service specification.
     * @param problem 	the problem to be passed to the CVX solver.
     */	
	private static void addBufferingFactorConstraint(final QoSSpec qos,
			final CVXProblem problem) {
		problem.addDerivedRangeConstraint(AlphaBetaExpression.BETA(),
			qos.getMinBufferingFactor(), qos
				.getMaxBufferingFactor(), "buffering factor");
	}

    /**
     * Adds a constraint relating to the delivery time QoS specified by the user.
     * @param qos	the user Quality-of-service specification.
     * @param problem 	the problem to be passed to the CVX solver.
     */
	private static void addDeliveryTimeConstraint(final QoSSpec qos,
			final CVXProblem problem) {
		problem.addDerivedRangeConstraint(AlphaBetaExpression.ALPHABETA(),
			qos.getMinDeliveryTime(), qos.getMaxDeliveryTime(),
			"delivery time");
	}

    /**
     * Adds a constraint relating to the total energy QoS specified by the user.
     * @param qos	the user Quality-of-service specification.
     * @param problem 	the problem to be passed to the CVX solver.
     */
	private static void addTotalEnergyConstraint(final QoSSpec qos,
			AlphaBetaExpression allSitesPowerConsumption,
			final CVXProblem problem) {
		
		double minEpsilon = qos.getMinTotalEnergy();
		double maxEpsilon = qos.getMaxTotalEnergy();
		
        //	TODO: 6 months in s hard-coded in, added 000 for ms
		if (minEpsilon!=-1) {
			minEpsilon = new Double(qos.getMinTotalEnergy()/new Double("15778463000").doubleValue()).doubleValue();
		}
		if (maxEpsilon!=-1) {
			maxEpsilon = new Double(qos.getMaxTotalEnergy()/new Double("15778463000").doubleValue()).doubleValue();
		}
		
		problem.addDerivedRangeConstraint(allSitesPowerConsumption, 
			minEpsilon, maxEpsilon, "total energy");
		AlphaBetaExpression sixMonthEpsilon = AlphaBetaExpression.multiplyBy(
				allSitesPowerConsumption,
				new Double("15778463").doubleValue());
		problem.setEpsilon(sixMonthEpsilon);
		problem.setEpsilonString(sixMonthEpsilon);
	}

    /**
     * Adds a constraint relating to the total network lifetime QoS specified by the user.
     * @param qos	the user Quality-of-service specification.
     * @param problem 	the problem to be passed to the CVX solver.
     */
	private static void addNetworkLifetimeConstraint(final DAF daf,
			final QoSSpec qos, final CVXProblem problem) {
		final Iterator<Site> siteIter = daf.siteIterator(TraversalOrder.PRE_ORDER);
		while (siteIter.hasNext()) {
		    final Site site = siteIter.next();
		    
		    if (site.getID().equals(daf.getRoutingTree().getRoot().getID()))
		    {
		    	continue;
		    }
		    
		    final long siteEnergyStock = site.getEnergyStock();
	    	final AlphaBetaExpression siteEnergyConsumption = siteEnergyConsumptions.get(site);
	    	final AlphaBetaExpression expr1 = AlphaBetaExpression.divideByAlphaBeta(siteEnergyConsumption);
	    	
		    final double minNetworkLifetime = qos.getMinLifetime();

		    if (minNetworkLifetime != -1) {
				AlphaBetaExpression expr2 = 
					AlphaBetaExpression.multiplyBy(expr1, minNetworkLifetime);
				
				problem.addDerivedConstraint(new CVXConstraint(expr2,
					CVXOperator.LESS_EQUALS, new AlphaBetaExpression(0,0,0,siteEnergyStock), 
					"site " + site.getID() + " lifetime"));
		    }
	
		    final long maxNetworkLifetime = qos.getMaxLifetime();
		    if (maxNetworkLifetime != -1) {
		    	final AlphaBetaExpression expr2 = 
		    		AlphaBetaExpression.multiplyBy(expr1, maxNetworkLifetime);
		    	
				problem.addDerivedConstraint(new CVXConstraint(expr2,
					CVXOperator.GREATER_EQUALS, new AlphaBetaExpression(0,0,0,siteEnergyStock), 
						"site " + site.getID() + " lifetime"));
			    }
		}
	}
	
	
    /**
     * Given a QoS, returns the optimization problem type (i.e., whether it is a minimization 
     * or maximization) for the CVX solver.  If null, the problem to be solved is considered a
     * feasibility problem and not an optimization problem.
     * @param qos
     * @return the optimization type for the CVX problem to solve
     */
    private static OptimizationType getOptimizationType(QoSSpec qos) {
    	OptimizationType optType = null;
    	
    	/*For lifetime, since the expression that we derive is the 
    	 * reciprocal of the lifetime, as invert the type of optimization goal */
    	if (qos.getOptimizationVariable() == QoSVariable.LIFETIME) {
        	//TODO: see what happens if we minimize lifetime
    	    if (qos.getOptimizationType() == OptimizationType.MINIMIZE) {
    	    	optType = OptimizationType.MAXIMIZE;
    	    } else if (qos.getOptimizationType() == OptimizationType.MAXIMIZE) {
    	    	optType = OptimizationType.MINIMIZE;
    	    }    		
    	} else {
    	    if (qos.getOptimizationType() == OptimizationType.MINIMIZE) {
    			optType = OptimizationType.MINIMIZE;
   		    } else if (qos.getOptimizationType() == OptimizationType.MAXIMIZE) {
    			optType = OptimizationType.MAXIMIZE;
   		    }    		
    	}
    	return optType;
    }

    /**
     * Given a QoS returns the expression representing the optimization goal specified
     * by the user.  If null, the problem to be solved is considered a
     * feasibility problem and not an optimization problem.
     * @param qos
     * @param allSitesPowerConsumption
     * @return
     */
    private static ArrayList<AlphaBetaExpression> getOptimizationExpr(QoSSpec qos, 
    		AlphaBetaExpression allSitesPowerConsumption, DAF daf) {
    	ArrayList<AlphaBetaExpression> optExpr = 
    		new ArrayList<AlphaBetaExpression>();
    	
		if (qos.getOptimizationVariable() == QoSVariable.ACQUISITION_INTERVAL) {
			optExpr.add(new AlphaBetaExpression(0, 1, 0, 0));
			
		} else if (qos.getOptimizationVariable() == QoSVariable.DELIVERY_TIME) {
			optExpr.add(new AlphaBetaExpression(1, 0, 0, 0));
			
		} else if (qos.getOptimizationVariable() == QoSVariable.TOTAL_ENERGY) {
			optExpr.add(allSitesPowerConsumption);
			
		} else if (qos.getOptimizationVariable() == QoSVariable.LIFETIME) {	
		    optExpr = getLifetimeExpression(daf);
		}  	
    	return optExpr;
    }


	private static ArrayList<AlphaBetaExpression> getLifetimeExpression(DAF daf) {
		ArrayList<AlphaBetaExpression> expr = 
    		new ArrayList<AlphaBetaExpression>();
		
		Iterator<Site> siteIter = siteEnergyConsumptions.keySet().iterator();
		while (siteIter.hasNext()) {
			final Site site = siteIter.next();
			
			if (site.getID().equals(daf.getRoutingTree().getRoot().getID())) {
				continue;
			}
			
			AlphaBetaExpression localLambda_1Expr = getLocalLifetime(site);
			expr.add(localLambda_1Expr);
		}
		return expr;
	}
    
    /**
     * Generates an AlphaBeta expression with the local lifetime for a site
     * @param site The site whose local lifetime is to be computed.
     * @return The expression for the site local lifetime.
     */
	private static AlphaBetaExpression getLocalLifetime(final Site site) {
		final AlphaBetaExpression siteEnergyExpr = siteEnergyConsumptions
			.get(site);
		final double siteEnergyStock = site.getEnergyStock();
		
		AlphaBetaExpression localLambda_1Expr = 
			AlphaBetaExpression.divideByAlphaBeta(siteEnergyExpr);
		
		localLambda_1Expr = AlphaBetaExpression.divideByConstant(
			localLambda_1Expr, siteEnergyStock);
		
//			final String latexRep = "\\frac{"
//				+ siteEnergyConsumptions.get(site).toLatexString() + "}{"
//					+ AlphaBetaExpression.prettyPrint(siteEnergyStock)
//				+ "\\alpha\\beta}";
//			localLambda_1Expr.setAltLatexRep(latexRep);
		return localLambda_1Expr;
	}

	/** 
	 * Generates an expression for the total time cost for the fragments in the 
	 * last epoch in the agenda evaluation.  
	 *  
	 * Note that the duration of instances of the same fragment may vary, as different
	 * instances may have a different cardinality of input tuples.
	 * 
	 * For non-recursive fragments, instances of the same fragment are assumed to be 
	 * able to execute concurrently, thus the duration of the fragment instance with 
	 * the longest duration is taken, i.e., the time assumed for a non-recursive
	 * fragment F is Max(f1,f2,...fn) where f1,f2...fn are instances of the same 
	 * fragment.
	 * 
	 * For recursive fragments, instances of the same fragment may not execute 
	 * concurrently, as precedence constraints may exist between instances of the 
	 * same fragment.  In this case, we sum the duration of each fragment instance,
	 * i.e., the time assumed for recursive fragment F is Sum(f1,f2,...fn) where 
	 * f1,f2...fn are instances of the same fragment. This is basically the "worst-case"
	 * and may result in over-estimation (which is tolerable; an under-estimation, 
	 * on the other hand, would result in an invalid agenda being generated.)
	 * 
	 * 
     * @param cardType CardinalityType The type of cardinality to be considered.
	 * @param round Defines if rounding reserves should be included or not
	 * @return Energy cost function. 
	 */
	public static AlphaBetaExpression computeFragmentsPi(DAF daf) {
		
		//We only consider MAX because the agenda must be based on the Max
		//cardinality
		
		
		AlphaBetaExpression result = new AlphaBetaExpression();
		Iterator<Fragment> fragments = daf.getFragments().iterator();
		while (fragments.hasNext()) {
			Fragment fragment = fragments.next();
			
			if (fragment.isRecursive()) {
				
				//The time cost of recursive fragment instances is added
				Iterator<Site> siteIter = fragment.getSites().iterator();
				while (siteIter.hasNext()) {
					Site site = siteIter.next();
					result.add(fragment.getTimeExpression(CardinalityType.MAX, 
							site, daf, true)); 
					//Round true guarantees there is never an underestimate.
					//For exchanges where more than one tuple can be sent per packet 
					//The assumption is that the last packet will waste all but one tuple. 
				}
			} else {
				//find the fragment instance with the highest duration 
				//as this value will be assumed as the fragment duration
				//note that since it is not possible to compare two 
				//AlphaBetaExpressions, we use the version of the cost models
				//which returns a numeric constant rather than an AlphaBetaExpression.
				Operator rootOp = fragment.getRootOperator();
				Iterator<Site> siteIter = fragment.getSites().iterator();
				double maxTimeCost  = 0;
				AlphaBetaExpression maxExpr = new AlphaBetaExpression();  
				while (siteIter.hasNext()) {
					Site site = siteIter.next();
					double timeCost = rootOp.getTimeCost(CardinalityType.MAX, site, daf);
					if (timeCost > maxTimeCost) {
						maxTimeCost = timeCost;
						maxExpr	= fragment.getTimeExpression(
								CardinalityType.MAX, site, daf, true);
					}
				}
				result.add(maxExpr);
				System.out.println("pi adding fragment " + fragment.getID() + " = " + maxExpr.toString() );				
			}
		}
		return result;
	}
	
	
	/**
	 * Generates an expression for the total time cost for the
	 * communication tasks in the last epoch in the agenda evaluation.
	 * 
	 * Note that for communications we currently assume that they cannot take 
	 * place concurrently.
	 */
	private static AlphaBetaExpression computeCommsPi(DAF daf) {
		AlphaBetaExpression result = new AlphaBetaExpression();		
		
		//
		Iterator<Site> siteIter = daf.getRoutingTree().siteIterator(
				TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			Site site = siteIter.next();		
			
			Iterator<ExchangePart> exchCompIter = 
				site.getExchangeComponents().iterator();
			while (exchCompIter.hasNext()) {
				final ExchangePart exchComp = 
					exchCompIter.next();
				if ((exchComp.getComponentType() == ExchangePartType.PRODUCER ||
					exchComp.getComponentType() == ExchangePartType.RELAY) && 
						exchComp.isRemote()) { 
					AlphaBetaExpression exchCompDuration = exchComp.getTimeExpression(
							CardinalityType.MAX, daf, true);
					result.add(exchCompDuration);
				}			
			}
	    	//remove one getBetweenpackets cost for first packet
			result.subtract(CostParameters.getBetweenPackets());
			//Now add the overheads associated with turning the radio on
			result.add(CommunicationTask.getTimeCostOverhead());
		}	
			
		return result;
	}
	
	
	/**
	 * Generates an expression for the total time cost for the fragments and 
	 * communication tasks in the last epoch in the agenda evaluation.  
	 * This is approximated by summing the time cost of
	 * all fragments and communication tasks.
	 * 
	 */
	public static AlphaBetaExpression computePi(DAF daf){
		AlphaBetaExpression result = new AlphaBetaExpression();		
		
		AlphaBetaExpression fragsDuration = computeFragmentsPi(daf);
		result.add(fragsDuration);
		System.out.println("estimated frags pi =" + fragsDuration.toString());
		
		AlphaBetaExpression commsDuration = computeCommsPi(daf);
		result.add(commsDuration);
				
		System.out.println("estimated comms pi=" + commsDuration.toString());
		result.multiplyBy(1.2); //pi under estimation problem
		
		return result;		
	}
	
}
