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
package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.rewriting;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.LookupException;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.OldAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Source;

/**
 * 
 * @author Christian Brenninkmeijer
 */
public class MaxCardinalityCalculator implements CardinalityCalculator {
    private final Logger logger = Logger
	    .getLogger(MaxCardinalityCalculator.class.getName());

    //SymbolTable symbolTable;

    //public void setSymbolTable(SymbolTable symbolTable)
    //{
    //	this.symbolTable = symbolTable;
    //}

    /*
     * Estimate the cardinality of applying the given predicates in scan or 
     * operator call operators (not join operators).
     */
    public int estimateCardinality(final int inputCardinality,
	    final Collection predicates) throws OptimizationException,
	    LookupException {
	final Iterator it = predicates.iterator();
	int card = inputCardinality;
	while (it.hasNext()) {
	    card = Math.max(1, this.estimateApplyPredicate(card, (Predicate) it
		    .next()));
	}
	this.logger.finest("cardinality set to " + card);
	return card;
    }

    /*
     * Estimate the cardinality of applying join predicates 
     */
    public int estimateJoinCardinality(final int leftCard, final int rightCard,
	    final Collection joinPredicates, final Source relation)
	    throws OptimizationException, LookupException {
	int card = leftCard * rightCard;
	final Iterator it = joinPredicates.iterator();
	while (it.hasNext()) {
	    final Predicate p = (Predicate) it.next();
	    //divide by 3 for each inequality
	    if (p.isInequality()) {
		card = Math.max(1, card / 3);
	    } else {
		boolean lKey = false; //the left relation attribute is a key
		boolean rKey = false; //the right relation attribute is a key
		if (p.getArgument1() instanceof OldAttribute) {
		    final OldAttribute attr1 = (OldAttribute) p.getArgument1();
		    if (this.isPrimaryKey(attr1) && (relation != null)) {
			if (attr1.getSource() == relation) {
			    rKey = true;
			} else {
			    lKey = true;
			}
		    }
		}
		if (p.getArgument2() instanceof OldAttribute) {
		    final OldAttribute attr2 = (OldAttribute) p.getArgument2();
		    if (this.isPrimaryKey(attr2) && (relation != null)) {
			if (attr2.getSource() == relation) {
			    rKey = true;
			} else {
			    lKey = true;
			}
		    }
		}
		//divide by max(r,l) if both keys
		if (lKey && rKey) {
		    card = Math.max(1, card / (Math.max(leftCard, rightCard)));
		    //divide by R if L is the only key    
		} else if (lKey) {
		    card = Math.max(1, card / rightCard);
		    //divide by L if R is the only key    
		} else if (rKey) {
		    card = Math.max(1, card / leftCard);
		    //if neither are keys divide by max(R/5,L/5)    
		} else {
		    card = Math.max(1, card
			    / (Math.max(rightCard / 5, leftCard / 5)));
		}
	    }
	}
	return card;
    }

    /*
     * Determine whether an attribute is a primary key
     * or a relation
     */
    // private boolean isKey(Attribute attribute)
    {
	//return false;    	
	//        try { 
	//        	String column = attribute.getName();
	//        	String source = attribute.getSource().getName();
	//            symbolTable.isKey(source, column);
	//        } catch ( LookupException e ) { }
	//         return false;
    }

    /*
     * Estimate the cardinality of applying the given predicate in scan or 
     * operation call operators (not join operators).
     */
    private int estimateApplyPredicate(final int inputCardinality,
	    final Predicate predicate) throws LookupException,
	    OptimizationException {
	if (predicate.isInequality()) {
	    return (inputCardinality / 3);
	} else if (predicate.isAttributeEquatedToConstant()) {
	    OldAttribute attr = null;
	    if (predicate.getArgument1() instanceof OldAttribute) {
		attr = (OldAttribute) predicate.getArgument1();
	    } else {
		attr = (OldAttribute) predicate.getArgument2();
	    }
	    if (this.isPrimaryKey(attr)) {
		return 1;
	    } else {
		return (inputCardinality / 10);
		//       } else if ( predicate.isFunctionEquatedToConstant() ) {
		//       	return ( inputCardinality / 10 );
		//       } else if ( predicate.isFunctionEquatedToFunction() ) {
		//       	return ( inputCardinality / 5 );
	    }
	} else if (predicate.isAttributeEquatedToAttribute()) {
	    return (inputCardinality / 20);
	    //       } else if ( predicate.isAttributeEquatedToFunction() ) {
	    //           return ( inputCardinality / 5 );
	}
	throw new OptimizationException("Unable to estimate cardinality for "
		+ "predicate " + predicate.toString());
    }

    /*
     * Determine whether an attribute is a primary key
     * or a relation
     */
    private boolean isPrimaryKey(final OldAttribute attribute) {
	//try {
	//	return symbolTable.isKey(attribute.getSource().getName(), attribute.getName());
	//} catch ( LookupException e ) { }
	return false;
    }

}
