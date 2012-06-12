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

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Aggregate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Literal;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.OldAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.TranslationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Variable;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Aggregate.AggregateType;
import antlr.collections.AST;

/**
 * Convertor from AST tokens to expressions.
 * 
 * @author Christian
 *
 */
public final class ExpressionConvertor {
	
	/** Hides default constructor. */
	private ExpressionConvertor() {
	}
	
	/**
	 * Converts an AST token into an expression.
	 * 
	 * @param token Expression or BooleanExpression AST token
	 * 
	 * @param incoming List of attribute that can be used.
	 * 
	 * @return The resulting expression.
	 */
    public static Expression convert(final AST token, 
    		final ArrayList<Attribute> incoming) {
    	try	{
            switch (token.getType()) {
            	case SNEEqlOperatorParserTokenTypes.ATTRIBUTE: 
   					return convertAttribute(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.TUPLEATTRIBUTE: 
   					return convertAttribute(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.AVG:
        			return new AggregationExpression(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.COUNT:
        			return new AggregationExpression(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.MIN:
        			return new AggregationExpression(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.MAX:
        			return new AggregationExpression(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.SUM:
        			return new AggregationExpression(token, incoming);
            	case SNEEqlOperatorParserTokenTypes.FLOATLIT:
            		return new FloatLiteral(token);
            	case SNEEqlOperatorParserTokenTypes.INTLIT:
            		return new IntLiteral(token);
            	default: 
            		return new MultiExpression(token, incoming);
            }
        } catch (Exception e) {
    		Utils.handleCriticalException(e);
        	return null;
    	}
    }

    /** 
     * Converts an Attribute AST by finding the matching Attribute
     *     from the list of incoming attributes.
     * @param token An Attribute 
     * @param incoming List of incoming Attributes
     * @return The found attribute.
     * @throws TranslationException The Attribute was not in the list.
     */
    private static Attribute convertAttribute(final AST token,
    		final ArrayList<Attribute> incoming) throws TranslationException {
    	//IFind Attribute in input list.
    	AST localToken = token.getFirstChild();
    	String localName = localToken.getText();
    	String attributeName = localToken.getNextSibling().getText();
    	for (int i = 0; i < incoming.size(); i++) {
    		Attribute attribute = incoming.get(i);
    		if ((attribute.getLocalName().equalsIgnoreCase(localName)) 
    				&& (attribute.getAttributeName().
    						equalsIgnoreCase(attributeName))) {
    			return attribute;
    		}
    	}
    	throw new TranslationException("Attribute localName "
			+ localName + " AttributeName " + attributeName
			+ " not returned by child operator");
    }
	
    /**
     * Converter for Old Constructors.
     * @param var A variable to be converted to. 
     * @param incoming List of Attributed to choose from.
     * @return One of the Attributes from the list.
     * @deprecated 
     */
    public static Expression convert(final Variable var, 
       		final ArrayList<Attribute> incoming) {
    	try	{
    		if (var instanceof OldAttribute) {
    			String name = ((OldAttribute) var).getPureName();
    			String localName = name.substring(0, name.indexOf('.')); 
    			String attributeName = name.substring(name.indexOf('.') + 1); 
    			for (int i = 0; i < incoming.size(); i++) {
    				Attribute attribute = incoming.get(i);
    				if ((attribute.getLocalName().equalsIgnoreCase(localName)) 
    					&& (attribute.getAttributeName().
    							equalsIgnoreCase(attributeName))) {
    					return attribute;
    				}
    			}
     			throw new TranslationException("Attribute localName "
    					+ localName + " AttributeName " + attributeName
    					+ " not returned by child operator");
    		}	
    		if (var instanceof Literal) {
    			String name = var.getPureName();
    			float floatValue = Float.parseFloat(name);
    			int intValue = Integer.parseInt(name);
    			if (intValue == floatValue) {
    				return new IntLiteral(intValue);
    			} else {
    				return new FloatLiteral(floatValue);
    			}
    		}
    		if (var instanceof Aggregate) {
    			Aggregate agg = (Aggregate) var;
    			AggregateType aggType = agg.getOperator();
    			Variable innerVar = agg.getArgument();
    			Expression innerExp = convert(innerVar, incoming);
    			return new AggregationExpression(innerExp, aggType);
    		}
			throw new AssertionError("convertExpression not finished: "
					+ var + " type:" + var.getClass());
    	} catch (Exception e) {
    		Utils.handleCriticalException(e);
    	}
    	return null;
    }
    
    /** 
     * Converts a predicate into an Expression.
     * @param p A Predicate
     * @param incoming List of Attributes passed in by previous operator.
     * @return A Expression representing the predicate.
     */
    public static Expression convert(
    		final Predicate p, final ArrayList<Attribute> incoming) {
	   Expression[] expressions = new Expression[2]; 
	   expressions[0] = ExpressionConvertor.convert(p.getArgument1(), incoming);
	   expressions[1] 
	   		= ExpressionConvertor.convert(p.getArgument2(), incoming);
	   return new MultiExpression(expressions, p.getCompare());
    }
    
    /**
     * Converts an array of predicate into an Expression.
     * @param ps Array of predicates.
     * @param incoming List of Attributes passed in by previous operator.
     * @return An Expression representing the predicate list.
     */
    public static Expression convert(
    		final Predicate[] ps, final ArrayList<Attribute> incoming) {
    	Expression[] exps = new Expression[ps.length];
    	for (int i = 0; i < ps.length; i++) {
    		exps[i] = convert(ps[i], incoming);
    	}
    	if (exps.length == 0) {
    		return new NoPredicate();
    	}
    	if (exps.length == 1) {
    		return exps[0];
    	}
    	assert (exps.length > 1);
    	return new MultiExpression(exps, MultiType.AND);
    }
}
