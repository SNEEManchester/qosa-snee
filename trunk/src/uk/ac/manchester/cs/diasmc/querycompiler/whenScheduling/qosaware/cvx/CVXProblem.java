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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.OptimizationType;

public class CVXProblem {

    //Either maxmimize or minimize
    OptimizationType optimizationType;

    //When optimizing for lifetime, we take the max of the optimization expressions
    //Otherwise, there is only one optimization expression
    ArrayList<AlphaBetaExpression> optimizationGoal;

    ArrayList<CVXConstraint> derivedConstraints = new ArrayList<CVXConstraint>();

    ArrayList<CVXConstraint> consistencyConditions = new ArrayList<CVXConstraint>();

    ArrayList<CVXConstraint> integerConstraints = new ArrayList<CVXConstraint>();

    String piString = "";
    
    String epsilonString = "";
    
    ArrayList<AlphaBetaExpression> lambda_1;

	private AlphaBetaExpression epsilon;

	private AlphaBetaExpression pi;
    
	public static int MAX_SUB_PROBLEMS = 31;
    
    public CVXProblem() {
	this.optimizationType = null;
	this.optimizationGoal = null;
    }

    public CVXProblem(final OptimizationType _optimizationType,
	    final ArrayList<AlphaBetaExpression> _optimizationGoal) {
	this.optimizationType = _optimizationType;
	this.optimizationGoal = _optimizationGoal;
    }

    public CVXProblem(final OptimizationType _optimizationType,
	    final AlphaBetaExpression _optimizationGoal) {
	this.optimizationType = _optimizationType;
	this.optimizationGoal = new ArrayList<AlphaBetaExpression>();
	this.optimizationGoal.add(_optimizationGoal);
    }

    public final void addDerivedConstraint(final CVXConstraint constraint) {
	this.derivedConstraints.add(constraint);
    }

    public final void addDerivedRangeConstraint(final AlphaBetaExpression expr,
	    final double min, final double max, final String comment) {
	if ((min == max) && (min != -1)) {
	    this.addDerivedConstraint(new CVXConstraint(expr,
		    CVXOperator.EQUALS, new AlphaBetaExpression(0, 0, 0, min),
		    comment));
	} else {
	    if (min != -1) {
		this.addDerivedConstraint(new CVXConstraint(expr,
			CVXOperator.GREATER_EQUALS, new AlphaBetaExpression(0,
				0, 0, min), comment));
	    }
	    if (max != -1) {
		this.addDerivedConstraint(new CVXConstraint(expr,
			CVXOperator.LESS_EQUALS, new AlphaBetaExpression(0, 0,
				0, max), comment));
	    }
	}
    }

    public final void addConsistencyCondition(final CVXConstraint constraint) {
	this.consistencyConditions.add(constraint);
    }

    public final void addIntegerConstraint(final CVXConstraint constraint) {
	this.integerConstraints.add(constraint);
    }

    public final void removeAllIntegerConstraints() {
	this.integerConstraints.clear();
    }

    /**
     * Creates a copy of the problem with variable constraints set to integers.
     * @param tmpAlpha
     * @param tmpBeta
     * @return
     */
	public CVXProblem addIntegerConstraints(final double tmpAlpha,
		    final double tmpBeta) {
		CVXProblem newProblem = this.clone();
		newProblem.removeAllIntegerConstraints();
		newProblem.addIntegerConstraint(new CVXConstraint(new AlphaBetaExpression(
			0, 1, 0, 0), CVXOperator.EQUALS, new AlphaBetaExpression(0,tmpAlpha)));
		newProblem.addIntegerConstraint(new CVXConstraint(new AlphaBetaExpression(
			1, 0), CVXOperator.EQUALS, new AlphaBetaExpression(0,tmpBeta)));
		return newProblem;
	}

	public CVXProblem addBetaIntegerConstraint(final double tmpBeta) {
		CVXProblem newProblem = this.clone();
		newProblem.removeAllIntegerConstraints();
		newProblem.addIntegerConstraint(new CVXConstraint(new AlphaBetaExpression(
			1, 0), CVXOperator.EQUALS, new AlphaBetaExpression(0,tmpBeta)));
		return newProblem;
	}
	
	/**
	 * 
	 */
	public CVXProblem clone() {
		CVXProblem clone = new CVXProblem(this.getOptimizationType(), this.optimizationGoal);
		clone.integerConstraints = (ArrayList<CVXConstraint>)this.integerConstraints.clone();
		clone.derivedConstraints = (ArrayList<CVXConstraint>)this.derivedConstraints.clone();
		clone.consistencyConditions = (ArrayList<CVXConstraint>)this.consistencyConditions.clone();
		clone.piString = this.piString;
		clone.epsilonString = this.epsilonString;
		clone.lambda_1 = (ArrayList<AlphaBetaExpression>)this.lambda_1.clone();
		clone.epsilon = this.epsilon;
		clone.pi = this.pi;
		return clone;
	}

	public static boolean constraintListValid(ArrayList<CVXConstraint> constraints) {
		Iterator<CVXConstraint> cstrIter = constraints.iterator();
		while (cstrIter.hasNext()) {
		    CVXConstraint cstr = cstrIter.next();
		    if (!cstr.isValid()) {
		    	return false;
		    }
		}
		return true;
	}
	
	public boolean isValid() {
		System.err.println("Checking optimization goal...");
		if (this.optimizationGoal != null) {
			Iterator<AlphaBetaExpression> exprIter = this.optimizationGoal.iterator();
			while (exprIter.hasNext()) {
				AlphaBetaExpression expr = exprIter.next();
				if (!expr.isValid())
					return false;
			}
		}
		System.err.println("Checking derived constraints...");
		if (!constraintListValid(this.derivedConstraints))
			return false;
		System.err.println("Checking consistency conditions...");
		if (!constraintListValid(this.consistencyConditions))
			return false;
		System.err.println("Checking integer constraints...");
		if (!constraintListValid(this.integerConstraints))
			return false;
		System.err.println("Checking epsilon...");
		if (!this.epsilon.isValid())
			return false;
		System.err.println("Checking pi...");
		if (!this.pi.isValid())
			return false;
		return true;
	}
	
    
    @Override
	public final String toString() {
	final StringBuffer strBuff = new StringBuffer();
	strBuff.append("cvx_begin gp\n");
	strBuff.append("variables alpha beta\n");

	if (this.optimizationGoal != null) {
	    strBuff.append(this.optimizationType.toString() + " ");
	    if (this.optimizationGoal.size() == 1) {
		strBuff.append(this.optimizationGoal.get(0).toString() + "\n");
	    } else {
	    	strBuff.append(getLifetimeStr(this.optimizationGoal));
	    }
	}

	strBuff.append("%% Derived Constraints\n");
	Iterator<CVXConstraint> cstrIter = this.derivedConstraints.iterator();
	while (cstrIter.hasNext()) {
	    strBuff.append(cstrIter.next() + "\n");
	}
	strBuff.append("%% Consistency Conditions\n");
	cstrIter = this.consistencyConditions.iterator();
	while (cstrIter.hasNext()) {
	    strBuff.append(cstrIter.next() + "\n");
	}
	strBuff.append("%% Integer Constraints\n");
	cstrIter = this.integerConstraints.iterator();
	while (cstrIter.hasNext()) {
	    strBuff.append(cstrIter.next() + "\n");
	}
	strBuff.append("cvx_end\n");

	return strBuff.toString();
    }

	private String getLifetimeStr(ArrayList<AlphaBetaExpression> expr) {
		StringBuffer strBuff = new StringBuffer();
		int i;
		for (i = 0; i < (expr.size() - 1); i++) {
		    strBuff.append("max(" + expr.get(i).toString() + ",");
		}
		strBuff.append(expr.get(i));
		for (i = 0; i < expr.size() - 1; i++) {
		    strBuff.append(")");
		}
		strBuff.append("\n");
		return strBuff.toString();
	}

    public final void exportToLatex(final String fname, 
    		CVXSolution solution,
    		String dafName) {
	try {
	    final PrintWriter out = new PrintWriter(new BufferedWriter(
		    new FileWriter(fname)));
	    out.println("\\documentclass[a4paper]{article}");
	    out.println("\\begin{document}");
	    out.println("\\section{Problem Formulation for " 
	    		+ dafName + "}");
	    out.println("\\begin{tabular}{p{2cm}p{7.5cm}p{5cm}}");
	    out.println("\\hline");
	    out.print("\\tiny\\texttt{Optimization goal: } 		&");

	    if (this.optimizationGoal == null) {
		out.println("\\texttt{none} & \\\\ ");
	    } else {
		out
			.print("\\texttt{" + this.optimizationType.toString()
				+ "} ");
		if (this.optimizationGoal.size() == 1) {
		    out.println("$ " + this.optimizationGoal.get(0).toDecimalLatexString()
			    + "$ & \\\\");
		} else {
		    out.print("$\\lambda^{-1} = Max(");
		    for (int i = 0; i < this.optimizationGoal.size(); i++) {
			if (i != 0) {
			    out.print(",");
			}
			out.print(this.optimizationGoal.get(i).toDecimalLatexString());
		    }
		    out.println(")$ & \\\\");
		}
	    }
	    out.println("\\hline");

	    boolean first = true;
	    Iterator<CVXConstraint> cstrIter = this.derivedConstraints
		    .iterator();
	    while (cstrIter.hasNext()) {
		if (first) {
		    out.print("\\tiny\\texttt{Derived Constraints: } & ");
		    first = false;
		} else {
		    out.print(" & ");
		}
		final CVXConstraint constraint = cstrIter.next();
		out.println(constraint.toLatex()
			+ " & \\tiny "
			+ constraint.getComment().replace("<=", "$\\le$")
				.replace(">=", "$\\ge$") + " \\\\ ");
	    }

	    out.println("\\hline");
	    first = true;
	    cstrIter = this.consistencyConditions.iterator();
	    while (cstrIter.hasNext()) {
		if (first) {
		    out.print("\\tiny\\texttt{Consistency Conditions: } & ");
		    first = false;
		} else {
		    out.print(" & ");
		}
		final CVXConstraint constraint = cstrIter.next();
		out.println(constraint.toLatex()
			+ " & \\tiny "
			+ constraint.getComment().replace("<=", "$\\le$")
				.replace(">=", "$\\ge$") + " \\\\ ");
	    }
	    out.println("&	$\\alpha \\in \\textbf{N}$ \\\\");
	    out.println("&	$\\beta \\in \\textbf{N}$ \\\\");
	    out.println("\\hline");
	    out.println("\\end{tabular}\n");
	    
	    if (solution != null) {
		    out.println("\\section{Solution}");
		    out.println("status = " + solution.getStatus() + "\\\\");
		    if (solution.getStatus() == CVXSolutionStatus.SOLVED) {
			    out.println("$\\alpha = " + solution.getAlpha() + "$\\\\");
			    out.println("$\\beta = " + solution.getBeta() + "$");		    
		    }	    	
	    }
	    
	    out.println("\\end{document}");

	    out.close();
	} catch (final Exception e) {
	    // TODO: handle exception
	}
    }

    /**
     * Generates latex source code and PDF output to visualise the problem
     * formulation, and its solution, if provided.
     * @param latexFilename The full path of the latex filename to be 
     * generated (and from which other files, such as the PDF, will be derived) 
     * @param solution The solution to the problem
     * @param dafName The name of the DAF which this problem is based on
     */
    public final void exportToPDF(
    		final String latexFilename, 
    		final CVXSolution solution,
    		final String dafName) {
    
    	this.exportToLatex(latexFilename, solution, dafName);
    	
    	if (Settings.GENERAL_GENERATE_PDFS) {
    		Utils.latexToPDF(latexFilename);    		
    	}
    }
    
    /**
     * Gets the optimization type of the geometric problem (minimize/maximize).
     * @return The optimization type.
     */
    public final OptimizationType getOptimizationType() {
    	return this.optimizationType;
    }

    /**
     * Breaks up the geometric problem into a number of sub-problems, which are to be solved 
     * individually.
     *   
     * Recall that the optimization goal is the Max of a list of AlphaBetaExpressions.  
     * This is necessary because Max can only be nested 32 times in Matlab; so the problems need to be
     * solved individually and subsequently, the solutions combined.
     * 
     * @return a list of CVXProblem
     */
    public ArrayList<CVXProblem> spawnSubProblems() {
    	ArrayList<CVXProblem> subProblemList = new ArrayList<CVXProblem>();
    	
    	for (int startPos = 0; startPos < this.optimizationGoal.size(); startPos += MAX_SUB_PROBLEMS) {
    		
    		if (startPos >= this.optimizationGoal.size())  {
    			break;
    		}
    		int endPos = Math.min(startPos + MAX_SUB_PROBLEMS, this.optimizationGoal.size());
    		CVXProblem clone = this.clone();
    		clone.optimizationGoal = getSubList(this.optimizationGoal, startPos, endPos);
    		subProblemList.add(clone);
    	}
    	
    	return subProblemList;
    }

    /**
     * Given a list of problems, a startIndex and an endIndex, returns a list from the startIndex up to and
     * including the (endIndex-1)
     * @param list
     * @param startIndex
     * @param endIndex
     * @return
     */
	private static ArrayList<AlphaBetaExpression> getSubList(ArrayList<AlphaBetaExpression> list, 
			int startIndex, int endIndex) {
		ArrayList<AlphaBetaExpression> result = new ArrayList<AlphaBetaExpression>(); 
		for (int i = startIndex; i < endIndex; i++) {
			result.add(list.get(i));
		}		
		return result;
	}

	public void setPi(AlphaBetaExpression pi) {
		this.pi = pi;
		this.piString = "pi = "+pi.toString();
	}
	
	public String getPiString() {
		return this.piString;
	}

	public void setEpsilonString(AlphaBetaExpression epsilon) {
		this.epsilonString = "epsilon = "+epsilon.toString();
	}
	
	public String getEpsilonString() {
		return this.epsilonString;
	}
	
	public void setLamba_1(ArrayList<AlphaBetaExpression> lambda_1) {
		this.lambda_1 = lambda_1;
	}
	
	public String getLambda_1String() {
		StringBuffer strBuff = new StringBuffer();
		int count = 0;
		
		if (this.lambda_1.size()<=MAX_SUB_PROBLEMS) {
			strBuff.append("lambda_1 = "+getLifetimeStr(lambda_1));
		}
		else
		{
	    	for (int startPos = 0; startPos < this.lambda_1.size(); startPos += MAX_SUB_PROBLEMS) {
	    		
	    		if (startPos >= this.lambda_1.size())  {
	    			break;
	    		}
	    		int endPos = Math.min(startPos + MAX_SUB_PROBLEMS, this.lambda_1.size());
	    		count++;
	    		ArrayList<AlphaBetaExpression> tmp = getSubList(this.lambda_1, startPos, endPos);
	    		strBuff.append("l"+count+" = ");
	    		strBuff.append(getLifetimeStr(tmp)+"\n");
	    	}
	    	
	    	strBuff.append("lambda_1 = ");
	    	int i = 0;
	    	for (i = 0; i < (count-1); i++) {
	    		strBuff.append("max(l" + (i+1) + ",");
	    	}
	    	strBuff.append("l"+(i+1)+Utils.pad(")", (count-1))+"\n");
    	}
    	strBuff.append("lambda_ms = (1 / lambda_1)\n");
    	strBuff.append("lambda_days = (lambda_ms / 86400000)");
		
    	return strBuff.toString();
	}

	public void setEpsilon(AlphaBetaExpression allSitesEnergyConsumption) {
		this.epsilon = allSitesEnergyConsumption;
	}
}
