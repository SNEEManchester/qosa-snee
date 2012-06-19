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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.common.Matlab;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.OptimizationType;

/**
 * Wrapper for the Matlab CVX GP solver 
 * @author galpini
 *
 */
public class CVXSolver {

	private static int counter = 1; 
	
    /**
     * Generate Matlab/CVX input script name
     * @return
     */
    private static String getInputScriptName() {
    	return "cvx_input" + counter + ".m";
    }
    
    /**
     * Generate Matlab/CVX output file name
     * @return
     */
    private static String getOutputFilename() {
    	return  "cvx_output" + counter + ".txt";
    }
    
    /**
     * Given a list of Geometric programming problems, generates Matlab script file.
     * @param problemList a list of CVXProblem 
     */
    private static void generateScript(ArrayList<CVXProblem> problemList) {
	    
    	PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(
				    new FileWriter(getInputScriptName())));
		    out.println("cd " + LocalSettings.getCVXDir()); 
		    //out.println("cvx_setup"); //Should speed it up
		    out.println("format long g");
		  
		    Iterator<CVXProblem> problemIter = problemList.iterator();
		    while (problemIter.hasNext()) {
		    	CVXProblem problem = problemIter.next();
		    	if (!problem.isValid()) {
		    		out.println("if 0");
		    	}
	    		
			    out.println(problem.toString());
			    out.println("cvx_optval");
			    out.println("alpha");
			    out.println("beta");
			    out.println(problem.getPiString());
			    out.println(problem.getEpsilonString());
			    out.println(problem.getLambda_1String());
			    
			    
			    if (!problem.isValid()) {
			    	out.println("end");
		    		out.println("disp('Status: Invalid')");
		    	}
		    }
		    out.println("exit");
		    out.close();		    
		} catch (IOException e) {
		    Utils.handleCriticalException(e);		
		}
    }
    

    private static ArrayList<CVXSolution> parseMatlabOutputFile() {
    	ArrayList<CVXSolution> solutionList = new ArrayList<CVXSolution>();
    	
	    CVXSolutionStatus _status = null;
		double _opt_val = Double.NaN;
		double _alpha = Double.NaN;
		double _beta = Double.NaN;
		double _pi = Double.NaN;
		double _epsilon = Double.NaN;
		double _lambdaDays = Double.NaN;
		boolean optValPending = false;
		boolean alphaPending = false;
		boolean betaPending = false;
		boolean piPending = false;
		boolean epsilonPending = false;
		boolean lambdaDaysPending = false;
	
		try {
			final FileReader fin = new FileReader(getOutputFilename());
		    final BufferedReader in = new BufferedReader(fin);
		    String line;
		    while ((line = in.readLine()) != null) {
				if (line.contains("Status: Solved") || line.contains("Status: Inaccurate/Solved")) {
				    _status = CVXSolutionStatus.SOLVED;
				} else if (line.contains("Status: Unbounded") || line.contains("Status: Inaccurate/Unbounded") 
						||(line.contains("Status: Failed"))) {
				    _status = CVXSolutionStatus.UNBOUNDED;
				    CVXSolution solution = new CVXSolution(_status);
				    solutionList.add(solution);				    
				    continue;
				} else if (line.contains("Status: Infeasible") || line.contains("Status: Inaccurate/Infeasible")) {
				    _status = CVXSolutionStatus.INFEASIBLE;
				    CVXSolution solution = new CVXSolution(_status);
				    solutionList.add(solution);				    
				    continue;
				} else if (line.contains("Status: Invalid")) {
					_status = CVXSolutionStatus.INVALID;
				    CVXSolution solution = new CVXSolution(_status);
				    solutionList.add(solution);				    
				    continue;
				}
		
				//skip until we reach the next solved one
				if (_status != CVXSolutionStatus.SOLVED) {
					continue;
				}
				
				if (line.contains("cvx_optval =")) {
				    optValPending = true;
				    continue;
				}
				if (line.contains("alpha =")) {
				    alphaPending = true;
				    continue;
				}
				if (line.contains("beta =")) {
				    betaPending = true;
				    continue;
				}
				if (line.contains("pi =")) {
				    piPending = true;
				    continue;
				}
				if (line.contains("epsilon =")) {
				    epsilonPending = true;
				    continue;
				}
				if (line.contains("lambda_days =")) {
				    lambdaDaysPending = true;
				    continue;
				}
				
				if (optValPending && !line.trim().equals("")) {
				    _opt_val = Double.parseDouble(line);
				    optValPending = false;
				}
		
				if (alphaPending && !line.trim().equals("")) {
				    _alpha = Double.parseDouble(line);
				    alphaPending = false;
				}
		
				if (betaPending && !line.trim().equals("")) {
				    _beta = Double.parseDouble(line);
				    betaPending = false;
				}
				
				if (piPending && !line.trim().equals("")) {
					_pi = Double.parseDouble(line);
					piPending = false;
				}
				
				if (epsilonPending && !line.trim().equals("")) {
					_epsilon = Double.parseDouble(line);
					epsilonPending = false;
				}			
				
				if (lambdaDaysPending && !line.trim().equals("")) {
					_lambdaDays = Double.parseDouble(line);
					lambdaDaysPending = false;
				    CVXSolution solution = new CVXSolution(_status, _opt_val, _alpha, _beta, _pi, _epsilon, _lambdaDays);
				    solutionList.add(solution);					
				}
		    }
		    
		    fin.close();
		} catch (final Exception e) {
		    // TODO: handle exception
		}	
	
		return solutionList;
    }

    /**
     * Given a list of geometric programming problems p1, p2.. pn, submit them to the
     * CVX solver and obtain solutions s1, s2.. sn. 
     * @param problemList The list of geometric problems to be solved.
     * @return A list of solutions to these problems.
     */
    public static ArrayList<CVXSolution> solve(final ArrayList<CVXProblem> problemList) {

    	//The lifetime optimization goal is an expression of the form Minimize Max(n1, n2, n3 ...)
    	//In CVX, this is expressed in the form Minimize Max(n1, Max(n2, Max(n3 ...))) and there is
    	//limit to the number of nested parentheses allowed.  This results in each problem submitted
    	//to the solver having a limited number of Max terms allowed.  If more are needed (as is 
    	//the case with large networks) then separate problems need to be submitted to the solver,
    	//and then their solutions combined.
    	
    	//List of all the sub-problems derived from the original problems.  These are to be broken 
    	//up into sub problems of the largest permissible size.  It is advantageous to submit problems
    	//of the largest possible size as the solver processes them faster than many small individual
    	//problems.
    	ArrayList<CVXProblem> subProblemList = 
    		new ArrayList<CVXProblem>();
    	//Stores how many sub-problems each problem in the problemList is made up of; this is necessary
    	//to keep the different problems separate
    	ArrayList<Integer> subProblemCount = new ArrayList<Integer>();
    	
    	Iterator<CVXProblem> problemIter = problemList.iterator();
    	while (problemIter.hasNext()) {
    		CVXProblem currentProblem = problemIter.next();
    		ArrayList<CVXProblem> subProblems = currentProblem.spawnSubProblems();
    		subProblemList.addAll(subProblems);
    		subProblemCount.add(subProblems.size());
    	}
    	
		generateScript(subProblemList);
		
		int numRetries = 5;
		ArrayList<CVXSolution> solutionList;
		do {
			Matlab.invokeMatlab(getInputScriptName(), getOutputFilename());
			solutionList = parseMatlabOutputFile();
			numRetries--;
			
			if (solutionList.size()==0) { 
				System.err.println("Unable to obtain solver solution, retrying...");
				
				if (numRetries==0) {
					Utils.handleCriticalException(new Exception("Unable to obtain solver solution, giving up"));	
				}
			}
			
		} while (solutionList.size()==0);
			
		//now reduce the sub-problem solutions into a single solution
		ArrayList<CVXSolution> reducedSolutionList = new ArrayList<CVXSolution>();  
		int pos = 0;
		Iterator<Integer> subProblemCountIter = subProblemCount.iterator();
		while (subProblemCountIter.hasNext()) {
			int subListSize = subProblemCountIter.next();
						
			//get a copy of the relevant part of the list
			ArrayList<CVXSolution> tmpSolutionSubList = 
				getSubList(solutionList, pos, pos+subListSize); 
			
			CVXSolution maxSolution = MIGPSolver.findBestSolution(OptimizationType.MAXIMIZE,
					tmpSolutionSubList);
			if (maxSolution!=null) {
				reducedSolutionList.add(maxSolution);
			} else {
				solutionList.get(pos);
			}
			pos += subListSize;
		}
		
		//move input/output files to query plan dir
		File inputSource = new File(getInputScriptName());
		File inputDest = new File(QueryCompiler.queryPlanOutputDir + getInputScriptName());
		inputSource.renameTo(inputDest);	
		File outputSource = new File(getOutputFilename());
		File outputDest = new File(QueryCompiler.queryPlanOutputDir + getOutputFilename());
		outputSource.renameTo(outputDest);
		counter++;
		
		return solutionList;
    }
    
    /**
     * Given a single geometric programming problem, submit it to the solver, and obtain a single solution.
     * @param problem The geometric programming problem to be solved.
     * @return The solution to this problem.
     */
    public static CVXSolution solve(CVXProblem problem) {
    	ArrayList<CVXProblem> problemList = new ArrayList<CVXProblem>();
    	problemList.add(problem);
    	CVXSolution solution = solve(problemList).get(0);
    	return solution;
    }
    
	private static ArrayList<CVXSolution> getSubList(ArrayList<CVXSolution> list, int startIndex, int endIndex) {
		ArrayList<CVXSolution> result = new ArrayList<CVXSolution>(); 
		for (int i = startIndex; i < endIndex; i++) {
			result.add(list.get(i));
		}		
		return result;
	}
}
