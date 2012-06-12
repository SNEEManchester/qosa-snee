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
package uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx;

import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.querycompiler.qos.OptimizationType;

public class MIGPSolver {

	/**
	 * Given an optimization type and a list of solutions, identifies and returns the best solution.s
	 * @param optType The optimization type, i.e., minimize or maximize
	 * @param solutionList The list of solutions
	 * @return
	 */
    public static CVXSolution findBestSolution(OptimizationType optType, final ArrayList<CVXSolution> solutionList) {

    	CVXSolution bestSolution = null;
    	Iterator<CVXSolution> solutionIter = solutionList.iterator();
    	while (solutionIter.hasNext()) {
    		CVXSolution solution = solutionIter.next();
        	if (solution.getStatus() == CVXSolutionStatus.SOLVED) {    		
        	    if (bestSolution == null) {
        	    	bestSolution = solution;
        	    	
        	    //For minimization problems, the optimization value should be as small as possible
        	    //And for maximization problems, the optimization value should be as large as possible
        	    } else if (((optType == OptimizationType.MINIMIZE) && (solution.getOpt_val() 
        	    		< bestSolution.getOpt_val()))
        	    		|| ((optType == OptimizationType.MAXIMIZE) && (solution.getOpt_val() 
        	    		> bestSolution.getOpt_val()))) {
        	    	bestSolution = solution;
        	    }
        	}
    	}
    	return bestSolution;
    }

    
    public static CVXSolution solve(final CVXProblem problem) {

		//First, solve the original CVX problem, and obtain real values for the variables
		final CVXSolution realSolution = CVXSolver.solve(problem);
		
		//If at this point the solution is infeasible or unbounded, return now
		if (realSolution.getStatus() != CVXSolutionStatus.SOLVED) {
		    return realSolution;
		}
	
		//Get the real values for the initial solution
		double alpha = realSolution.getAlpha();
		double beta = realSolution.getBeta();
    
	    //fix only b
		ArrayList<CVXProblem> problemList = new ArrayList<CVXProblem>();
		CVXProblem p = problem.addBetaIntegerConstraint(Math.floor(beta));
		problemList.add(p);
		p = problem.addBetaIntegerConstraint(Math.ceil(beta));
		problemList.add(p);
		
		//Send these as a batch to the solver, and obtain a list of solutions
		ArrayList<CVXSolution> solutionList = CVXSolver.solve(problemList);
		
		Iterator<CVXSolution> solutionListIter = solutionList.iterator();
		problemList = new ArrayList<CVXProblem>();
		while (solutionListIter.hasNext()) {
			CVXSolution s = solutionListIter.next();
			
			if (s.status==CVXSolutionStatus.SOLVED) {
				alpha = s.getAlpha();
				beta = s.getBeta();
				p = problem.addIntegerConstraints(Math.ceil(alpha), beta);
				problemList.add(p);
			}
		}
		
		solutionList = CVXSolver.solve(problemList);
		
		//Find the solution which gives the best optimal value
		CVXSolution bestSolution = 
			findBestSolution(problem.getOptimizationType(), solutionList);		
	
		
		return bestSolution;
    }
		
		
    /**
     * Solves a Mixed-Integer Geometric Program, i.e., one in which the variables have integer values.
     * 
     * This is done by first solving to obtain real values for the variables.  These are then 
     * resubmitted to the constraint solver with the variables rounded up or down; the solution 
     * with the best optimal value obtained is considered to be the optimal one.
     * 
     * @param problem The geometric problem to be solved.
     * @return The best solution found for the problem
     */
    public static CVXSolution solveOld(final CVXProblem problem) {

		//First, solve the original CVX problem, and obtain real values for the variables
		final CVXSolution realSolution = CVXSolver.solve(problem);
		
		//If at this point the solution is infeasible or unbounded, return now
		if (realSolution.getStatus() != CVXSolutionStatus.SOLVED) {
		    return realSolution;
		}
	
		//Get the real values for the initial solution
		final double alpha = realSolution.getAlpha();
		final double beta = realSolution.getBeta();
		
		//Now round alpha and beta up or down.  Create new problems with each different 
		//combination possible.
		ArrayList<CVXProblem> problemList = new ArrayList<CVXProblem>();
		CVXProblem p = problem.addIntegerConstraints(Math.floor(alpha), Math.floor(beta));
		problemList.add(p);
		p = problem.addIntegerConstraints(Math.floor(alpha), Math.ceil(beta));
		problemList.add(p);
		p = problem.addIntegerConstraints(Math.ceil(alpha), Math.floor(beta));
		problemList.add(p);
		p = problem.addIntegerConstraints(Math.ceil(alpha), Math.ceil(beta));
		problemList.add(p);
			
		//Send these as a batch to the solver, and obtain a list of solutions
		ArrayList<CVXSolution> solutionList = CVXSolver.solve(problemList);
		
		//Find the solution which gives the best optimal value
		CVXSolution bestSolution = 
			findBestSolution(problem.getOptimizationType(), solutionList);
		
	    return bestSolution;
    }

    public static void main(final String[] args) {

	/*		AlphaBetaExpression l1 = new AlphaBetaExpression(0,0.2,0,0.2,0,0);
	 AlphaBetaExpression l2 = new AlphaBetaExpression(0,0.225,0,0.125,0,0);
	 AlphaBetaExpression l3 = new AlphaBetaExpression(0,0.083333333333333333333333333333333,0,0.05,0,0);
	 CVXProblem problem = new CVXProblem(CVXOptimizationType.MINIMIZE, new AlphaBetaExpression[] {l1, l2, l3, l1, l2, l3, l1, l2, l3});
	 problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(0,0,1,0,0,0),
	 CVXOperator.LESS_EQUALS, new BetaExpression(0,2000))); 
	 problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(1,0,0,0,0,0),
	 CVXOperator.LESS_EQUALS, new BetaExpression(0,6000)));
	 problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(0,0,1,0,0,0),
	 CVXOperator.GREATER_EQUALS, new BetaExpression(30,60)));
	 
	 problem.addConsistencyCondition(new CVXConstraint(new BetaExpression(1,0),
	 CVXOperator.GREATER_EQUALS, new BetaExpression(0,1)));		

	 //		problem.addIntegerConstraint(new CVXConstraint(new BetaExpression(1,0),
	 //				CVXOperator.EQUALS, new BetaExpression(0,5)));		
	 
	 System.out.println(problem.toString());
	 CVXSolution solution = solve(problem);
	 
	 System.out.println("Final solution: "+solution.toString());*/

	//////////////////////
	final AlphaBetaExpression optGoal = new AlphaBetaExpression(0, 1, 0, 0);
	final CVXProblem problem = new CVXProblem(OptimizationType.MINIMIZE,
		optGoal);
	problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(
		0, 1, 0, 0), CVXOperator.LESS_EQUALS, new AlphaBetaExpression(0,0,0,
		2000), "acquisition rate le 2000"));
	problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(
		1, 0, 0, 0), CVXOperator.GREATER_EQUALS, new AlphaBetaExpression(0,0,0,
		6000), "delivery time le 6000"));
	problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(0,0,30,
		60), CVXOperator.LESS_EQUALS, new AlphaBetaExpression(0, 1, 0,
		0), "avoid executions of agenda overlapping"));
	problem.addDerivedConstraint(new CVXConstraint(new AlphaBetaExpression(
		120000, 120000, 0, 0), CVXOperator.LESS_EQUALS,
		new AlphaBetaExpression(0,0,0, 100), "lifetime check for site 1"));
	problem.addConsistencyCondition(new CVXConstraint(new AlphaBetaExpression(0,0,1,
		0), CVXOperator.GREATER_EQUALS, new AlphaBetaExpression(0,0,0, 1)));

	System.out.println(problem.toString());

	CVXSolution solution = solve(problem);
	
	problem.exportToLatex("hello.tex", solution, "test");

	//System.out.println("Final solution: "+solution.toString());

    }

}
