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
package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation;

import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.MultiType;

/**
 * Represents a predicate in the WHERE clause
 * of a query.
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class Predicate {

    /*
     * The two arguments of the predicate
     */
    private Variable argument1, argument2;

    /*
     * The predicate operator. May take the values:
     * =, !=, <, >, <=, >=, IN 
     */
    private String operator;

    /**
     * Construct a new object to represent a predicate.
     * @param argument1 the first argument of the predicate
     * @param argument2 the second argument of the predicate
     * @param operator the operator - one of (=, !=, <, >, <=, >=, IN)
     */
    public Predicate(final Variable argument1, final Variable argument2,
	    final String operator) {
	this.argument1 = argument1;
	this.argument2 = argument2;
	this.operator = operator;
	//ystem.out.println(this);
    }

    /**
     * Returns the predicates first argument
     */
    public Variable getArgument1() {
	return this.argument1;
    }

    /**
     * Returns the predicates second argument
     */
    public Variable getArgument2() {
	return this.argument2;
    }

    /**
     * Sets the predicates first argument
     * /
     public void setArgument1(Element argument1) {
     this.argument1 = argument1;   
     }
     
     /**
     * Sets the predicates second argument
     * /
     public void setArgument2(Element argument2) {
     this.argument2 = argument2;   
     }
     
     /**
     * Returns the operator (=, !=, <, >, <=, >= or IN)
     */
    public String getOperator() {
	return this.operator;
    }

    public MultiType getCompare() {
	   if (operator.equals("=")) {
		   return MultiType.EQUALS;
	   }
	   if (operator.equals("!=")) {
		   return MultiType.NOTEQUALS;
	   }
	   if (operator.equals("<")) {
		   return MultiType.LESSTHAN;
	   }
	   if (operator.equals("<=")) {
		   return MultiType.LESSTHANEQUALS;
	   }
	   if (operator.equals(">")) {
		   return MultiType.GREATERTHAN;
	   }
	   if (operator.equals(">=")) {
		   return MultiType.GREATERTHANEQUALS;
	   }
	   throw new AssertionError ("unexpected operator"+operator);
    }
    
    /**
     * Returns true if the predicate involves an inequality
     * operator.
     */
    public boolean isInequality() {
	if (this.operator.equalsIgnoreCase("<")) {
	    return true;
	}
	if (this.operator.equalsIgnoreCase("!=")) {
	    return true;
	}
	if (this.operator.equalsIgnoreCase(">")) {
	    return true;
	}
	if (this.operator.equalsIgnoreCase(">=")) {
	    return true;
	}
	if (this.operator.equalsIgnoreCase("<=")) {
	    return true;
	}
	return false;
    }

    /**
     * Replaces the predicate's operator. 
     */
    public void setOperator(final String operator) {
	this.operator = operator;
    }

    /*
     * Returns true if one of the predicate's arguments
     * is an attribute.
     */
    private boolean hasAttributeArgument() {
	if (this.argument1 instanceof OldAttribute) {
	    return true;
	}
	if (this.argument2 instanceof OldAttribute) {
	    return true;
	}
	return false;
    }

    /*
     * Returns true is one of the predicate's arguments
     * is a literal.
     */
    private boolean hasLiteralArgument() {
	if (this.argument1 instanceof Literal) {
	    return true;
	}
	if (this.argument2 instanceof Literal) {
	    return true;
	}
	return false;
    }

    /*
     * Returns true if one of the predicate's arguments is
     * a function call.
     * /
     private boolean hasFunctionArgument() {
     if ( argument1 instanceof Function ) return true;
     if ( argument2 instanceof Function ) return true;
     return false;
     }
     
     /**
     * Returns true if the predicate is of the from attribute=constant
     */
    public boolean isAttributeEquatedToConstant() {
	if (!this.operator.equalsIgnoreCase("=")) {
	    return false;
	}
	if (this.hasLiteralArgument() && this.hasAttributeArgument()) {
	    return true;
	}
	return false;
    }

    /**
     * Returns true if the predicate is of the from function=constant
     * /
     public boolean isFunctionEquatedToConstant() {
     if ( ! operator.equalsIgnoreCase("=") ) return false;
     if ( hasLiteralArgument() && hasFunctionArgument() )
     return true;
     return false;
     }
     
     /**
     * Returns true if the predicate is of the from attribute=function
     * / 
     public boolean isAttributeEquatedToFunction() {
     if ( ! operator.equalsIgnoreCase("=") ) return false;
     if ( hasAttributeArgument() && hasFunctionArgument() )
     return true;
     return false;
     }
     
     /**
     * Returns true if the predicate is of the from attribute=attribute
     */
    public boolean isAttributeEquatedToAttribute() {
	if (!this.operator.equalsIgnoreCase("=")) {
	    return false;
	}
	if (!(this.argument1 instanceof OldAttribute)) {
	    return false;
	}
	if (!(this.argument2 instanceof OldAttribute)) {
	    return false;
	}
	return true;
    }

    /**
     * Returns true if the predicate is of the from function=function
     * /
     public boolean isFunctionEquatedToFunction() {
     if ( ! operator.equalsIgnoreCase("=") ) return false;
     if (!( argument1 instanceof Function)) return false;
     if (!( argument2 instanceof Function)) return false;
     return true;
     }
     
     /**
     * Returns true if the predicate is known to be true
     * (This might be overkill! Just in case the query involves something
     * daft...)
     * /
     public boolean sameArguments(Element arg1, Element arg2) {
     if ((arg1==argument1)&&(arg2==argument2)) return true;
     if ((arg2==argument1)&&(arg1==argument2)) return true;
     return false;
     }
     
     /**
     * Returns a String representation of the predicate.
     */
    @Override
    public String toString() {
	String s = this.getArgument1().toString();
	s = s + " " + this.getOperator() + " ";
	s = s + this.getArgument2().toString();
	return s;
    }
}
