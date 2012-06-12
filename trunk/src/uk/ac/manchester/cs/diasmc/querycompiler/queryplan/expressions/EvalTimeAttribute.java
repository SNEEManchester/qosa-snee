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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;

/** Exsention of the Attribute for the special case 
 * 		when the Attribute is the EvalTime. 
 */
public class EvalTimeAttribute extends Attribute {

	/** 
	 * Creates a new EvalTime.
	 * @throws SchemaMetadataException The type could not be dettermined
	 *    from the metaData.
	 */
	public EvalTimeAttribute() throws SchemaMetadataException {
		super("", Constants.EVAL_TIME);
	}

	/**
	 * All evalTime objects are equals.
	 * @param other Object to compare with.
	 * @return True if and only if the other object is an EvalTime.
	 */
	public final boolean equals(final Object other) {
		// not sure this is redundant.
		return (other instanceof EvalTimeAttribute);
	}
	
	/** {@inheritDoc} */
	public final int hashCode() {
		return "evalTime".hashCode();
	}
	
	/** {@inheritDoc} */
	public final String toString() {
		return "EvalTime";
	}
	
	/**
	 * The raw data type of this expression.
	 *  
	 * @return The raw data type of this expression.
	 */
	public final AttributeType getType() {
		try {
			return 	Types.getType(Constants.TIME_TYPE);
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
			return null;
		}
	}
}
