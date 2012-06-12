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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns;

import java.util.ArrayList;

import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;

public class RTEnergyScoringFunction extends ScoringFunction<RT> {
	
	/**
	 * Constructor for scoring function.
	 * @param inWeighting The weighting to be given to this scoring function.
	 * @param inInverse Specifies whether the inverse is to be applied.
	 */
	public RTEnergyScoringFunction(final int inWeighting, 
			final boolean inInverse) {
		super(inWeighting, inInverse);
	}
	
	/**
	 * Recursively traverses the routing tree, computing a score based on 
	 * the relative energy consumption of each routing tree.  
	 * @param candidate	the routing tree being scored
	 * @param currentSite the current site being visited
	 * @return a score for the subtree
	 */
	private double doDoScore(final RT candidate, final Site currentSite) {
		
		ArrayList<Node> parents = currentSite.getOutputsList();
		double localEnergyConsumption = 10;
		if (parents.size() == 1) { //not the sink
			double linkToParentEnergyCost = 
				AvroraCostParameters.getTXAmpere((int)candidate.getLinkEnergyCost(
					currentSite, ((Site) currentSite.getOutput(0))));
			localEnergyConsumption += linkToParentEnergyCost * 
					currentSite.getNumSources();
		}
		
		ArrayList<Node> children = currentSite.getInputsList();
		for (int n = 0; n < children.size(); n++) {
			Site child = (Site) currentSite.getInput(n);
			double radioReceiveAmpereEnergy =
				AvroraCostParameters.getRadioReceiveAmpere();
			localEnergyConsumption += radioReceiveAmpereEnergy * 
			child.getNumSources();
			localEnergyConsumption += doDoScore(candidate, child); 
		}
		
		return localEnergyConsumption;
	}
	
	/**
	 * Returns a score for the candidate routing tree.
	 * @param candidate The candidate routing tree
	 * @return the score computed for the cadidate routing tree
	 */
	public final double doScore(final RT candidate) {
		double score = doDoScore(candidate, candidate.getRoot());
		return (1 / score);
	}
	
}
