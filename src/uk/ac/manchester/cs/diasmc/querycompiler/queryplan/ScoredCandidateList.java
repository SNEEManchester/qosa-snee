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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.
		ScoringFunction;
import uk.ac.manchester.cs.diasmc.querycompiler.routing.Routing;

/**
 * Collection type to stores and maintains an ordered list of candidates.
 * @author galpini
 *
 * @param <T> The candidate type.
 */
public class ScoredCandidateList<T extends Scorable> {

	/**
	 * Logger for this class.
	 */
    private static final Logger logger = 
    	Logger.getLogger(ScoredCandidateList.class.getName());

	
	/**
	 * The scoring functions which determine the order of the candidates.
	 */
	private ArrayList<ScoringFunction<T>> scoringFunctions = 
		new ArrayList<ScoringFunction<T>>();
	
	/**
	 * The list of candidates.
	 */
	private ArrayList<T> candidates = new ArrayList<T>();

	/**
	 * For scoring function x, gives the the score for candidate y.
	 */
	private ArrayList<HashMap<T, Double>> partialScores = new 
		ArrayList<HashMap<T, Double>>();
	
	/**
	 * The list of candidates which are deemed to be duplicates.
	 * For debugging/analysis purposes only.
	 */
	private ArrayList<T> duplicateCandidates = new ArrayList<T>();
	
	/**
	 * Maps a duplicate to a candidate already in the list.
	 */
	private HashMap<T, T> duplicateOf = new HashMap<T, T>();
	
	/**
	 * The list of candidates which have been removed as their rank is too low.
	 * For debugging/analysis purposes only.
	 */
	private ArrayList<T> trimmedCandidates = new ArrayList<T>();
	
	/**
	 * After the keepBestMethod has been called, we can't add any more 
	 * candidates.
	 */
	private boolean keepBestCalled = false;
	
	/**
	 * Constructor for ScoredCandidateList type.
	 * @param inScoringFunction The scoring function to be applied to all 
	 * candidates
	 */
	public ScoredCandidateList(final ScoringFunction<T> inScoringFunction) {
		this.scoringFunctions.add(inScoringFunction);
		this.partialScores.add(new HashMap<T, Double>());
	}
	
	/**
	 * Constructor when no scoring function is to be used (e.g., vanilla 
	 * version).
	 *
	 */
	public ScoredCandidateList() {
		this.scoringFunctions = new ArrayList<ScoringFunction<T>>();
	}
	
	/**
	 * Constructor which receives an array of scoring functions.
	 * @param inScoringFunctions the array of scoring functions
	 */
	public ScoredCandidateList(final ArrayList<ScoringFunction<T>> 
			inScoringFunctions) {
		this.scoringFunctions = inScoringFunctions;
		for (int n = 0; n < inScoringFunctions.size(); n++) {
			this.partialScores.add(new HashMap<T, Double>());
		}
	}
	
	/**
	 * Adds a candidate to the ScoredCandidateList, by scoring it and placing 
	 * it in the appropriate position.  Duplicate checking is performed; if a 
	 * duplicate is detected, it is discarded.
	 * 
	 * @param candidate The candidate to be added
	 */
	public final void addCandidate(final T candidate) {

		assert (!keepBestCalled);

		if (candidate == null) {
			return;
		}
		
		// First, check if there is a duplicate; otherwise no point in 
		// proceeding
		boolean dupFound = false;
		for (int n = 0; n < this.candidates.size(); n++) {
			if (candidate.isEquivalentTo(this.candidates.get(n))) {
				dupFound = true;
				this.duplicateCandidates.add(candidate);
				this.duplicateOf.put(candidate, this.candidates.get(n));
				break;
			}
		}

		if (!dupFound) {
			double currentScore;
			if (this.scoringFunctions.size() == 0) {
				currentScore = 0;
			} else {
				for (int n = 0; n < this.scoringFunctions.size(); n++) {
					currentScore = scoringFunctions.get(n).score(candidate);
					this.partialScores.get(n).put(candidate, currentScore);
				}
			}
			this.candidates.add(candidate);
		}
	}
	
	/**
	 * For each scoring function, normalizes the scores so that they are all 
	 * between 0 and 1.
	 *
	 */
	private void normalizeScores() {
		for (int n = 0; n < this.scoringFunctions.size(); n++) {
			
			//First, find out what the maximum is for scoring function n
			double tmpMax = 0;
			boolean first = true;
			HashMap<T, Double> currentScoreMap = this.partialScores.get(n);
			for (int m = 0; m < this.candidates.size(); m++) {
				double tmp = currentScoreMap.get(candidates.get(m));
				if (first) {
					first = false;
					tmpMax = tmp;
				} else if (tmpMax < tmp) {
					tmpMax = tmp;
				}
			}
			
			//Now, normalize all the scores so that they are between 0 and 1
			//for all the candidates in the scoring function
			if (tmpMax != 0) {
				for (int m = 0; m < this.candidates.size(); m++) {
					currentScoreMap.put(candidates.get(m), 
							currentScoreMap.get(candidates.get(m)) / tmpMax);
					logger.finest(candidates.get(m).getName() 
							+ " norm score = " 
							+ currentScoreMap.get(candidates.get(m)));
				}				
			}
		}
	}
	
	/**
	 * For each candidate, combines the scores for each scoring function, 
	 * according to the weightings specified, and stores them in the 
	 * combinedScores map.
	 *
	 */
	private void computeCombinedScores() {

		for (int n = 0; n < this.candidates.size(); n++) {
			double result = 0;
			for (int m = 0; m < this.scoringFunctions.size(); m++) {
				//TODO: add weighting, get from scoring function?
				result += this.partialScores.get(m).get(candidates.get(n));
			}
			candidates.get(n).setScore(result);
			logger.finest("In ScoredCandidateList"+candidates.get(n).getName()
					+ " combined score = " + result);
		}
	}
	
	/**
	 * Sorts the candidates based on each candidate's combined score,
	 * from highest to lowest.
	 *
	 */
	private void sortCandidates() {
		for (int n = 1; n < this.candidates.size(); n++) {
			for (int m = 0; m < n; m++) {
				if (this.candidates.get(n).getScore()
						> this.candidates.get(m).getScore()) {
						this.candidates.add(m, this.candidates.get(n));
						this.candidates.remove(n + 1);
				}
			}
		}
	}
	
	/**
	 * Keeps the best k candidate plans, discarding the rest. 
	 * @param k the number of plans to keep
	 */
	public final void keepBest(final int k) {
		keepBestCalled = true;
		
		this.normalizeScores();
		this.computeCombinedScores();
		this.sortCandidates();
		
		//Now discard the remaining candidates
		for (int n = (this.candidates.size() - 1); n >= k; n--) {
			T candidate = this.candidates.get(n);
			double currentScore = candidate.getScore();
			this.trimmedCandidates.add(candidate);
			partialScores.remove(candidate);
			this.candidates.remove(n);
		}		
	}
	
	/**
	 * Generates an iterator for the list of candidates.
	 * @return An iterator for the list of candidates.
	 */
	public final Iterator<T> iterator() {
		return candidates.iterator();
	}
	
	/**
	 * Returns the highest scoring candidate in the list of candidates.
	 * Should only be called after combined scores have been computed. 
	 * @return the highest scoring candidate
	 */
	public final T getBest() {
		if (candidates.size() > 0) {
			this.keepBest(1);
			return candidates.get(0);			
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the number of candidates in the list.
	 * @return
	 */
	public final int getSize() {
		return this.candidates.size();
	}
	
	/**
	 * Returns the number of candidates, including duplicates, in the list.
	 * @return
	 */
	public final int getTotalSize() {
		return this.candidates.size()+this.duplicateCandidates.size();
	}
	
	/**
	 * Produces graphical representations of all the candidates in the list.
	 * @param outputDir The destination output directory
	 */
	public final void display(final String outputDir) {
		
		for (int n = 0; n < this.candidates.size(); n++) {
			
			T candidate = candidates.get(n);
			
			String label = "score = " + candidate.getScore() + " \\n " 
					+ "rank = " + (n + 1);
			candidate.display(QueryCompiler.queryPlanOutputDir,
					candidate.getName(),
					label);
		}

		for (int n = 0; n < this.trimmedCandidates.size(); n++) {
			
			T candidate = this.trimmedCandidates.get(n);
			String label = "score = " 
				+ candidate.getScore() + " \\n " 
				+ "[This candidate was trimmed as its score was too low]";
			candidate.display(QueryCompiler.queryPlanOutputDir,
					candidate.getName(),
					label);
		}

		for (int n = 0; n < this.duplicateCandidates.size(); n++) {
			
			T candidate = this.duplicateCandidates.get(n);
			String label = "score = " + this.duplicateOf.get(candidate).getScore() 
					+ " \\n [Duplicate of " 
					+ this.duplicateOf.get(candidate).getName() + "] ";
			candidate.display(QueryCompiler.queryPlanOutputDir,
					candidate.getName(),
					label);
		}
	}
}
