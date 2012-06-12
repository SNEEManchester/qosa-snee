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
package uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.qosaware;

import uk.ac.manchester.cs.diasmc.common.graph.Node;
import uk.ac.manchester.cs.diasmc.common.graph.NodeImplementation;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

public class OperatorInstance extends NodeImplementation implements Node {

	static int counter = 0;
	
	Operator op;
	
	/**
	 * The deepest site that this operator instance may be assigned to, 
	 * due to tuple confluence.
	 */
	Site deepestConfluenceSite;
	
	/**
	 * The site that this operator instance is assigned to following decision
	 * in where-scheduling.
	 */
	Site site;
	
	public OperatorInstance(Operator op, Site deepestConfluenceSite) {
		super();
		this.op = op;
		this.deepestConfluenceSite = deepestConfluenceSite;
		counter++;
		this.id = generateID(op, deepestConfluenceSite);
	}	
	
	private static String generateID(Operator op, Site site) {
		StringBuffer id = new StringBuffer();
		id.append(op.getNesCTemplateName().replace(".nc", ""));
		if (site!=null) {
			id.append("_s"+site.getID());
		}
		id.append("_c"+counter);
		return id.toString();
	}

	

	/**
	 * @return the op
	 */
	public final Operator getOp() {
		return op;
	}


	public Site getDeepestConfluenceSite() {
		return this.deepestConfluenceSite;
	}
	

	public boolean isLocationSensitive() {
		return this.op.isLocationSensitive();
	}
	
	public void setSite(Site s) {
		this.site = s;
	}

	public Site getSite() {
		return this.site;
	}
	
}
