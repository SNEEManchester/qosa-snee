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
package uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operator_rewriting;
  
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JFrame;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.LAF;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.AggregationOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.DStreamOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.IStreamOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.JoinOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.ProjectOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.RStreamOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.SelectOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.WindowOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operatorTranslator.SNEEqlOperatorLexer;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operatorTranslator.SNEEqlOperatorParser;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

/** Test class.
 * 
 * @author Christian
 *
 */
public final class OperatorFactory {
	
	/** Hides the default constructor. */
	private OperatorFactory() {
	}
	
	/** The metadata. */
	private static SchemaMetadata schemaMetadata = null;
	
	/**
	 * Recursive method to convert an AST token into operators.
	 * 
	 * @param token The AST token representing an operator and its children. 
	 * @return The Operator and its children.
	 */
    public static Operator convertAST(final AST token) {
    	//Ignore Predicates that evaluate to true.
    	if ((token.getType() == SNEEqlOperatorParserTokenTypes.RELSELECT)
        	|| (token.getType() == SNEEqlOperatorParserTokenTypes.STRSELECT)
            || (token.getType() == SNEEqlOperatorParserTokenTypes.WINSELECT)) {
    		AST booleanExpression = token.getFirstChild(); 
    		AST child = booleanExpression.getNextSibling();
    		if (booleanExpression.getType() 
    				== SNEEqlOperatorParserTokenTypes.TRUE) {
    			return convertAST(child);
    	    } 
    	}
    	// OneOffScan ExtentName LocalName [AttributeName]
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.RELPROJECT) {
        	   return new ProjectOperator(token);    	
       	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.RELCROSSPRODUCT) {
     	   return new JoinOperator(token);
     	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.RELSELECT) {
     	   return SelectOperator.applySelect(token);    	
    	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.RELAGGREGATION) {
        	   return new AggregationOperator(token);    	
       	}
    	// RelRename LocalName RelOperator
    	// RelUngroup [Expression][AttributeName] RelGrOperator
    	// Relational Grouped Operators -}
    	// RelGroupBy [Attribute] RelOperator
    	// RelGrSelect BooleanExpression RelGrOperator
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.STRSELECT) {
     	   return SelectOperator.applySelect(token);    	
    	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.STRPROJECT) {
        	   return new ProjectOperator(token);    	
       	}
       	if (token.getType() == SNEEqlOperatorParserTokenTypes.STRACQUIRE) {
       	   if (schemaMetadata == null) {
       		   schemaMetadata = new SchemaMetadata(); 
       	   }
       	   return new AcquireOperator(token, schemaMetadata);
       	} 
    	// StrReceive ExtentName LocalName [AttributeName]
    	if (token.getType() 
    			== SNEEqlOperatorParserTokenTypes.STRRELCROSSPRODUCT) {
     	   return new JoinOperator(token);
     	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.DSTREAMOP) {
     	   return new DStreamOperator(token);    	
    	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.ISTREAMOP) {
     	   return new IStreamOperator(token);    	
    	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.RSTREAMOP) {
        	   return new RStreamOperator(token);    	
       	}
    	// StrRename LocalName StrOperator
    	// TimeWindow WindowScopeDef Tick StrOperator
    	// RowWindow WindowScopeDef Index StrOperator
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.INPUTWINDOW) {
       	   return new WindowOperator(token);    	
      	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.TIMEWINDOW) {
       	   return new WindowOperator(token);    	
      	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.ROWWINDOW) {
      	   return new WindowOperator(token);    	
     	}
    	// RepeatedScan Tick ExtentName LocalName [AttributeName]
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.WINPROJECT) {
       	   return new ProjectOperator(token);    	
      	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.WINCROSSPRODUCT) {
    	   return new JoinOperator(token);
    	}
    	if (token.getType() 
    			== SNEEqlOperatorParserTokenTypes.WINRELCROSSPRODUCT) {
     	   return new JoinOperator(token);
     	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.WINSELECT) {
        	   return SelectOperator.applySelect(token);    	
       	}
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.WINAGGREGATION) {
       	   return new AggregationOperator(token);    	
      	}
    	// WinRename LocalName WinOperator
    	// WinUngroup [Expression][AttributeName] WinGrOperator
    	// WinGroupBy [Attribute] WinOperator
    	// WinGrSelect BooleanExpression WinGrOperator
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.DELIVER) {
      	   return new DeliverOperator(token);    	
     	}
    	if ((token.getType() == SNEEqlOperatorParserTokenTypes.RELRENAME)
            	|| (token.getType() == SNEEqlOperatorParserTokenTypes.STRRENAME)
                || (token.getType() 
            		== SNEEqlOperatorParserTokenTypes.WINRENAME)) {
        	AST localName = token.getFirstChild(); 
        	AST child = localName.getNextSibling();
        	Operator childOp = convertAST(child);
        	childOp.pushLocalNameDown(localName.getText());
        	return childOp;
       	}
    	
    	throw new AssertionError("convertAst not finished. Missing "
    					+ token.getText());
    }
    
    /**
     * Invokes the parser. The result is an Abstract Syntax Tree.
     * 
     *  @param fullOperatorPath inout file
     *  @return AST tree
     */ 
    private static AST parse(final String fullOperatorPath) {
    	try	{
    		final InputStream in = new FileInputStream(fullOperatorPath);
    		final SNEEqlOperatorLexer l = new SNEEqlOperatorLexer(in);
    		final SNEEqlOperatorParser p = new SNEEqlOperatorParser(l);
    		//set AST classes for various node types
    		p.operator(); 
    		return p.getAST(); 
    	} catch (final Exception e) {
    		Utils.handleCriticalException(e);
    		return null;
    	}
	}
    
	/** 
	 * Test Method.
	 *
	 * @param args First is the query as a Haskell String
	 */
    public static void main(final String[] args) {
    	final Options opt = new Options(args, Options.Multiplicity.ZERO_OR_ONE);
    	Settings.initialize(opt);

		final String operatorFileName 
    	  //= "C:\\manchester\\SNEEql\\trunk\\input\\operators.txt";
	      = "C:\\SNEEql\\input\\operators.txt";
    	AST ast = parse(operatorFileName);
    	System.out.println(ast.toStringList());
    	final JFrame astframe = new ASTFrame("Syntax Tree", ast);
    	astframe.setVisible(true);
    	
    	Operator ops = convertAST(ast);
    	System.out.println(ops);
	    LAF laf = new LAF(ops, "Test", 1000);
	    laf.display(QueryCompiler.queryPlanOutputDir, laf.getName());
    }	 
}
