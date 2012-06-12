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
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;

/**
 * Expression combining two or more boolean expression.
 * @author Christian
 */
public class MultiExpression implements Expression {

	/** List of the expression.*/
	private Expression[] expressions;

	/** The method of combining the expressions.*/
	private MultiType multiType;

	/**
	 * AST constructor.
	 * 
	 * @param token The St token of the AND
	 * 
	 * @param incoming The list of attribute from which to select.
	 */
	public MultiExpression(final AST token, 
			final ArrayList<Attribute> incoming) {
		int count = token.getNumberOfChildren();
		expressions = new Expression[count];
		AST ast = token.getFirstChild();
		count = 0;
		while (ast != null) {
			expressions[count] = ExpressionConvertor.convert(ast, incoming);
			ast = ast.getNextSibling();
			count++;
		}
		convertMultiType(token);
	}
	/**
	 * Constuctor.
	 * 
	 * @param newExpressions Expressions inside the AND
	 * @param type The Type of combination or expressions to do.
	 */
	public MultiExpression(final Expression[] newExpressions, 
			final MultiType type) {
		this.expressions = newExpressions;
		assert (expressions.length >= 2);
		assert (type != null);
		this.multiType = type;
	}
	
	/**
	 * Converts an AST token into a multiType.
	 * @param token The AST token of how the expressions are combined.
	 */
	private void convertMultiType(final AST token) {
        switch (token.getType()) {
        	case SNEEqlOperatorParserTokenTypes.ADD: 
        		multiType = MultiType.ADD;
        		assert (expressions.length >= 2);
        		return;
        	case SNEEqlOperatorParserTokenTypes.AND: 
    			multiType = MultiType.AND;
        		assert (expressions.length >= 2);
    			return;
        	case SNEEqlOperatorParserTokenTypes.OR: 
    			multiType = MultiType.OR;
        		assert (expressions.length >= 2);
    			return;
        	case SNEEqlOperatorParserTokenTypes.DIVIDE: 
				multiType = MultiType.DIVIDE;
        		assert (expressions.length == 2);
				return;
        	case SNEEqlOperatorParserTokenTypes.EQUALS:
    			multiType = MultiType.EQUALS;
        		assert (expressions.length == 2);
    			return;
        	case SNEEqlOperatorParserTokenTypes.GREATERTHAN:
        		assert (expressions.length == 2);
				multiType = MultiType.GREATERTHAN;
				return;
        	case SNEEqlOperatorParserTokenTypes.GREATERTHANOREQUALS: 
				multiType = MultiType.GREATERTHANEQUALS;
        		assert (expressions.length == 2);
				return;
        	case SNEEqlOperatorParserTokenTypes.LESSTHAN:
				multiType = MultiType.LESSTHAN;
        		assert (expressions.length == 2);
				return;
        	case SNEEqlOperatorParserTokenTypes.LESSTHANOREQUALS:
				multiType = MultiType.LESSTHANEQUALS;
        		assert (expressions.length == 2);
				return;
        	case SNEEqlOperatorParserTokenTypes.MULTIPLY:
				multiType = MultiType.MULTIPLY;
        		assert (expressions.length >= 2);
        		return;
        	case SNEEqlOperatorParserTokenTypes.MINUS:
				multiType = MultiType.MINUS;
        		assert (expressions.length >= 2);
        		return;
        	case SNEEqlOperatorParserTokenTypes.POWER:
				multiType = MultiType.POWER;
        		assert (expressions.length == 2);
        		return;
        	case SNEEqlOperatorParserTokenTypes.SQUAREROOT:
				multiType = MultiType.SQUAREROOT;
        		assert (expressions.length == 1);
        		return;
        	default: throw new AssertionError("Unexpected AST token " + token); 
        }
	}
	
	/** {@inheritDoc} */
	public final String toString() {
		String output = expressions[0].toString();
		for (int i = 1; i < expressions.length; i++) {
			output = output + multiType.toString() + expressions[i].toString();
		}
		return output;
	}

	/** {@inheritDoc} */
	public final ArrayList<Attribute> getRequiredAttributes() {
		ArrayList<Attribute> required = expressions[0].getRequiredAttributes();
		for (int i = 1; i < expressions.length; i++) {
			ArrayList<Attribute> more = expressions[i].getRequiredAttributes();
			for (int j = 0; j < more.size(); j++) {
				if (!required.contains(more.get(j))) {
					required.add(more.get(j));
				}
			}
		}
		return required;
	}

	/** Gets the data type returned by this expression. 
	 * @return The data type of this expression.
	 */
	public final AttributeType getType() {
		try {
			if (multiType.isBooleanDataType()) {
				return Types.getType("boolean");
			}
			//simplified verion assumes all types are the same.
			return expressions[0].getType();
		} catch (SchemaMetadataException e) {
			Utils.handleCriticalException(e);
			return null;
		}
	}

	/** 
	 * Extracts the aggregates from within this expression.
	 * 
	 * @return An array List of all the aggregates within this expressions.
	 * Could contain duplicates.
	 */
	public final ArrayList<AggregationExpression> getAggregates()	{
		ArrayList<AggregationExpression> list 
			= new ArrayList<AggregationExpression>(1);
		for (int i = 0;  i < expressions.length; i++) {
			list.addAll(expressions[i].getAggregates());			
		}
		return list;
	}
	
	/** 
	 * Accessor.
	 * @return List of the expressions being combined.
	 */
	public final Expression[] getExpressions() {
		return expressions;
	}
	
	/**
	 * Accessor.
	 * @return The type of combination being done.
	 */
	public final MultiType getMultiType() {
		assert (multiType != null);
		return multiType;
	}

}
