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

import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Scorable;

public abstract class ScoringFunction<T extends Scorable> {

    /**
     * Logger for this class.
     */
	private static Logger logger = 
		Logger.getLogger(ScoringFunction.class.getName());
	
	/**
	 * The weighting to be given to this scoring function.
	 */
	private int weighting = 1;
	
	/**
	 * Specifies whether the inverse is to be applied.
	 */
	private boolean inverse = false;
	
	
	/**
	 * Implicit constructor.
	 *
	 */
	public ScoringFunction() {
	}
			
	/**
	 * Constructor for scoring function.
	 * @param inWeighting The weighting to be given to this scoring function.
	 * @param inInverse Specifies whether the inverse is to be applied.
	 */
	public ScoringFunction(final int inWeighting, 
			final boolean inInverse) {
		this.weighting = inWeighting;
		this.inverse = inInverse;	
	}

	/**
	 * Returns a score for the candidate.
	 * @param candidate The candidate
	 * @return the score computed for the candidate
	 */
	protected abstract double doScore(final T candidate);
	
	/**
	 * Returns a score for the candidate, applying a weighting 
	 * and, if applicable, returns the score as an inverse. 
	 * @param candidate The candidate
	 * @return the score computed for the candidate
	 */
	public final double score(final T candidate) {
		double score = doScore(candidate);
		if (this.inverse) {
			score = (1 / doScore(candidate)) * this.weighting;
		} else {
			score = doScore(candidate) * this.weighting; 
		}
		logger.fine(candidate.getName() + " score " + score);
		return score;
	}
}
