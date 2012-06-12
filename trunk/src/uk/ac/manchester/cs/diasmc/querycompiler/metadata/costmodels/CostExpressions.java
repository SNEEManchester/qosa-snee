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
package uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels;

import java.util.HashMap;

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.
	whenScheduling.qosaware.cvx.AlphaBetaExpression;

/** 
 * This method holds the static cost models.
 * 
 * Most take a DAF and a Site and return AlphaBetaExpressions. 
 * @author Christian
 *
 */
public abstract class CostExpressions {
	
	/** 
	 * Generates an expression for the total energy cost 
	 * for this site on this DAF.
	 * Includes all fragments, and communication tasks.
	 * 
	 * @param site Site for which to generate costs.
	 * @param round Defines if rounding reserves should be included or not
	 * @return Energy cost function. 
	 */
	public abstract AlphaBetaExpression getSiteEnergyExpression(Site site, 
		final boolean round);
		
	/** 
	 * Generates an expression for the total memory 
	 * for this site on this DAF.
	 * Includes all fragments, and communication tasks.
	 * 
	 * @param site Site for which to generate costs.
	 * @param round Defines if rounding reserves should be included or not
	 * @return Energy cost function. 
	 */
	public abstract AlphaBetaExpression getSiteMemoryExpression(Site site, 
		final boolean round);
	
	/**
	 * Super-class constructor for costExpression Object.
	 * Will return one of the implementations of this abstract class.
	 * Which class is determined by the settings. 
	 * Loads the class with the DAF 
	 * so once all site calculation need only be done once.
	 * 
	 * @param daf Distribute Algebra Format 
	 * @return Correct cost functions to be used.
	 */
	public static final CostExpressions costExpressionFactory(final DAF daf) {
		return new AvroraCostExpressions(daf);
	}
	
	/** 
	 * Displays the cost expression for debugging and reporting.
	 */
	public abstract void display();
	
	/** Exports an overview of the Cost Expressions used to Latex. 
	 * @param fname Full Name of the file to write the latex to. 
	 */
    public abstract void exportToLatex(final String fname);

}
