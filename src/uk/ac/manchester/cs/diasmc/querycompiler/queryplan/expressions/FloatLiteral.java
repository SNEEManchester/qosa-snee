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

import java.util.ArrayList;

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.
	querycompiler.codeGeneration.adt.CodeGenerationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;

/** 
 * Wrapper for an Integer constant expression.
 * 
 * @author Christian
 *
 */
public class FloatLiteral implements Expression {

	/** Value of the constant. */
	private float value;
	
	/**
	 * Constructor.
	 * @param newValue Constant value.
	 */
	public FloatLiteral(final float newValue) { 
		this.value = newValue;
	}
	
	/**
	 * Constructor.
	 * @param token The FLOATLIT AST token.
	 */
	public FloatLiteral(final AST token) {
        AST valueToken = token.getFirstChild();
        value = Float.parseFloat(valueToken.getText());
	}

	/** {@inheritDoc} */
	public final String getNescText(final String leftHead, 
			final String rightHead, 
			final ArrayList<DataAttribute> leftAttributes, 
			final ArrayList<DataAttribute>rightAttributes) 
			throws CodeGenerationException {
		return ("" + value);
	}

	/** {@inheritDoc} */
	public final String getName() {
		return toString();
	}
	
	/** {@inheritDoc} */
	public final ArrayList<Attribute> getRequiredAttributes() {
    	return new ArrayList<Attribute>();
	}

	/** {@inheritDoc} */
	public final AttributeType getType() {
		try {
			return 	Types.getType("float");
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
			return null;
		}
	}
	
	/** 
	 * Extracts the aggregates from within this expression.
	 * 
	 * @return Empty list.
	 */
	public final ArrayList<AggregationExpression> getAggregates()	{
		return new ArrayList<AggregationExpression>(0);
	}
}
