// $ANTLR 2.7.5 (20050128): "sneeql.g" -> "SNEEqlOperatorParser.java"$

package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.operatorTranslator;

//This file was generated using ANTLR, (ANother Tool for Language Recognition)
//Antlr was obtained from www.antlr.org
//
//  SNEE (Sensor NEtwork Engine)                                              *
//  http://code.google.com/p/snee                                             *
//  Release 1.0, 24 May 2009, under New BSD License.                          *
//                                                                            *
//  Copyright (c) 2009, University of Manchester                              *
//  All rights reserved.                                                      *
//                                                                            *
//  Redistribution and use in source and binary forms, with or without        *
//  modification, are permitted provided that the following conditions are    *
//  met: Redistributions of source code must retain the above copyright       *
//  notice, this list of conditions and the following disclaimer.             *
//  Redistributions in binary form must reproduce the above copyright notice, *
//  this list of conditions and the following disclaimer in the documentation *
//  and/or other materials provided with the distribution.                    *
//  Neither the name of the University of Manchester nor the names of its     *
//  contributors may be used to endorse or promote products derived from this *
//  software without specific prior written permission.                       *
//                                                                            *
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *


import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class SNEEqlOperatorParser extends antlr.LLkParser       implements SNEEqlOperatorParserTokenTypes
 {

protected SNEEqlOperatorParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public SNEEqlOperatorParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected SNEEqlOperatorParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public SNEEqlOperatorParser(TokenStream lexer) {
  this(lexer,2);
}

public SNEEqlOperatorParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void operator() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST operator_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ONEOFFSCAN:
			{
				AST tmp1_AST = null;
				tmp1_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp1_AST);
				match(ONEOFFSCAN);
				extentName();
				astFactory.addASTChild(currentAST, returnAST);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELPROJECT:
			{
				AST tmp2_AST = null;
				tmp2_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp2_AST);
				match(RELPROJECT);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELCROSSPRODUCT:
			{
				AST tmp3_AST = null;
				tmp3_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp3_AST);
				match(RELCROSSPRODUCT);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELSELECT:
			{
				AST tmp4_AST = null;
				tmp4_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp4_AST);
				match(RELSELECT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELAGGREGATION:
			{
				AST tmp5_AST = null;
				tmp5_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp5_AST);
				match(RELAGGREGATION);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELRENAME:
			{
				AST tmp6_AST = null;
				tmp6_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp6_AST);
				match(RELRENAME);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELUNGROUP:
			{
				AST tmp7_AST = null;
				tmp7_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp7_AST);
				match(RELUNGROUP);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELGROUPBY:
			{
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp8_AST);
				match(RELGROUPBY);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RELGRSELECT:
			{
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp9_AST);
				match(RELGRSELECT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRSELECT:
			{
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp10_AST);
				match(STRSELECT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRPROJECT:
			{
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp11_AST);
				match(STRPROJECT);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRACQUIRE:
			{
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp12_AST);
				match(STRACQUIRE);
				extentName();
				astFactory.addASTChild(currentAST, returnAST);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRRECEIVE:
			{
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp13_AST);
				match(STRRECEIVE);
				extentName();
				astFactory.addASTChild(currentAST, returnAST);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRSCROSSPRODUCT:
			{
				AST tmp14_AST = null;
				tmp14_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp14_AST);
				match(STRSCROSSPRODUCT);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case STRRENAME:
			{
				AST tmp15_AST = null;
				tmp15_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp15_AST);
				match(STRRENAME);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case DSTREAMOP:
			{
				AST tmp16_AST = null;
				tmp16_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp16_AST);
				match(DSTREAMOP);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case ISTREAMOP:
			{
				AST tmp17_AST = null;
				tmp17_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp17_AST);
				match(ISTREAMOP);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case RSTREAMOP:
			{
				AST tmp18_AST = null;
				tmp18_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp18_AST);
				match(RSTREAMOP);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case TIMEWINDOW:
			{
				AST tmp19_AST = null;
				tmp19_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp19_AST);
				match(TIMEWINDOW);
				windowScopeDef();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp20_AST = null;
				tmp20_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp20_AST);
				match(Int);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case ROWWINDOW:
			{
				AST tmp21_AST = null;
				tmp21_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp21_AST);
				match(ROWWINDOW);
				windowScopeDef();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp22_AST = null;
				tmp22_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp22_AST);
				match(Int);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case INPUTWINDOW:
			{
				AST tmp23_AST = null;
				tmp23_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp23_AST);
				match(INPUTWINDOW);
				windowScopeDef();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINSELECT:
			{
				AST tmp24_AST = null;
				tmp24_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp24_AST);
				match(WINSELECT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINPROJECT:
			{
				AST tmp25_AST = null;
				tmp25_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp25_AST);
				match(WINPROJECT);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case REPEATEDSCAN:
			{
				AST tmp26_AST = null;
				tmp26_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp26_AST);
				match(REPEATEDSCAN);
				AST tmp27_AST = null;
				tmp27_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp27_AST);
				match(Int);
				extentName();
				astFactory.addASTChild(currentAST, returnAST);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINCROSSPRODUCT:
			{
				AST tmp28_AST = null;
				tmp28_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp28_AST);
				match(WINCROSSPRODUCT);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINRELCROSSPRODUCT:
			{
				AST tmp29_AST = null;
				tmp29_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp29_AST);
				match(WINRELCROSSPRODUCT);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINAGGREGATION:
			{
				AST tmp30_AST = null;
				tmp30_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp30_AST);
				match(WINAGGREGATION);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINRENAME:
			{
				AST tmp31_AST = null;
				tmp31_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp31_AST);
				match(WINRENAME);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINUNGROUP:
			{
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp32_AST);
				match(WINUNGROUP);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				attributeNames();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINGROUPBY:
			{
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp33_AST);
				match(WINGROUPBY);
				expressions();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case WINGROUPSELECT:
			{
				AST tmp34_AST = null;
				tmp34_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp34_AST);
				match(WINGROUPSELECT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case DELIVER:
			{
				AST tmp35_AST = null;
				tmp35_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp35_AST);
				match(DELIVER);
				childOperator();
				astFactory.addASTChild(currentAST, returnAST);
				operator_AST = (AST)currentAST.root;
				break;
			}
			case ERROR:
			{
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp36_AST);
				match(ERROR);
				{
				_loop3:
				do {
					switch ( LA(1)) {
					case Identifier:
					{
						AST tmp37_AST = null;
						tmp37_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp37_AST);
						match(Identifier);
						break;
					}
					case ERROR:
					{
						AST tmp38_AST = null;
						tmp38_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp38_AST);
						match(ERROR);
						break;
					}
					default:
					{
						break _loop3;
					}
					}
				} while (true);
				}
				operator_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		returnAST = operator_AST;
	}
	
	public final void extentName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST extentName_AST = null;
		
		try {      // for error handling
			match(LeftParen);
			match(EXTENTNAME);
			match(Quote);
			AST tmp42_AST = null;
			tmp42_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp42_AST);
			match(Identifier);
			match(Quote);
			match(RightParen);
			extentName_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		returnAST = extentName_AST;
	}
	
	public final void localName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST localName_AST = null;
		
		try {      // for error handling
			match(LeftParen);
			match(LOCALNAME);
			match(Quote);
			AST tmp48_AST = null;
			tmp48_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp48_AST);
			match(Identifier);
			match(Quote);
			match(RightParen);
			localName_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = localName_AST;
	}
	
	public final void attributeNames() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST attributeNames_AST = null;
		
		try {      // for error handling
			AST tmp51_AST = null;
			tmp51_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp51_AST);
			match(LeftSquare);
			attributeNameList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RightSquare);
			attributeNames_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = attributeNames_AST;
	}
	
	public final void expressions() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressions_AST = null;
		
		try {      // for error handling
			AST tmp53_AST = null;
			tmp53_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp53_AST);
			match(LeftSquare);
			expressionList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RightSquare);
			expressions_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = expressions_AST;
	}
	
	public final void childOperator() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST childOperator_AST = null;
		
		try {      // for error handling
			match(LeftParen);
			operator();
			astFactory.addASTChild(currentAST, returnAST);
			match(RightParen);
			childOperator_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = childOperator_AST;
	}
	
	public final void booleanExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST booleanExpression_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EQUALS:
			{
				AST tmp57_AST = null;
				tmp57_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp57_AST);
				match(EQUALS);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case NOTEQULAS:
			{
				AST tmp58_AST = null;
				tmp58_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp58_AST);
				match(NOTEQULAS);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case GREATERTHAN:
			{
				AST tmp59_AST = null;
				tmp59_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp59_AST);
				match(GREATERTHAN);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case LESSTHAN:
			{
				AST tmp60_AST = null;
				tmp60_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp60_AST);
				match(LESSTHAN);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case GREATERTHANOREQUALS:
			{
				AST tmp61_AST = null;
				tmp61_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp61_AST);
				match(GREATERTHANOREQUALS);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case LESSTHANOREQUALS:
			{
				AST tmp62_AST = null;
				tmp62_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp62_AST);
				match(LESSTHANOREQUALS);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			{
				AST tmp63_AST = null;
				tmp63_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp63_AST);
				match(AND);
				booleanList();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case OR:
			{
				AST tmp64_AST = null;
				tmp64_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp64_AST);
				match(OR);
				booleanList();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case NOT:
			{
				AST tmp65_AST = null;
				tmp65_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp65_AST);
				match(NOT);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				AST tmp66_AST = null;
				tmp66_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp66_AST);
				match(TRUE);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				AST tmp67_AST = null;
				tmp67_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp67_AST);
				match(FALSE);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case LeftParen:
			{
				match(LeftParen);
				booleanExpression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		returnAST = booleanExpression_AST;
	}
	
	public final void windowScopeDef() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST windowScopeDef_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LeftParen) && (LA(2)==TIMESCOPEDEF)) {
				match(LeftParen);
				AST tmp71_AST = null;
				tmp71_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp71_AST);
				match(TIMESCOPEDEF);
				AST tmp72_AST = null;
				tmp72_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp72_AST);
				match(Int);
				AST tmp73_AST = null;
				tmp73_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp73_AST);
				match(Int);
				match(RightParen);
				windowScopeDef_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LeftParen) && (LA(2)==ROWSCOPEDEF)) {
				match(LeftParen);
				AST tmp76_AST = null;
				tmp76_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp76_AST);
				match(ROWSCOPEDEF);
				AST tmp77_AST = null;
				tmp77_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp77_AST);
				match(Int);
				AST tmp78_AST = null;
				tmp78_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp78_AST);
				match(Int);
				match(RightParen);
				windowScopeDef_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		returnAST = windowScopeDef_AST;
	}
	
	public final void expressionList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionList_AST = null;
		
		try {      // for error handling
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop8:
			do {
				if ((LA(1)==Comma)) {
					match(Comma);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop8;
				}
				
			} while (true);
			}
			expressionList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_6);
		}
		returnAST = expressionList_AST;
	}
	
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ATTRIBUTE:
			{
				AST tmp81_AST = null;
				tmp81_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp81_AST);
				match(ATTRIBUTE);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				match(LeftParen);
				attributeName();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case TUPLEATTRIBUTE:
			{
				AST tmp84_AST = null;
				tmp84_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp84_AST);
				match(TUPLEATTRIBUTE);
				localName();
				astFactory.addASTChild(currentAST, returnAST);
				match(LeftParen);
				attributeName();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case ADD:
			{
				AST tmp87_AST = null;
				tmp87_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp87_AST);
				match(ADD);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST tmp88_AST = null;
				tmp88_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp88_AST);
				match(MINUS);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case MULTIPLY:
			{
				AST tmp89_AST = null;
				tmp89_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp89_AST);
				match(MULTIPLY);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case DIVIDE:
			{
				AST tmp90_AST = null;
				tmp90_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp90_AST);
				match(DIVIDE);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case POWER:
			{
				AST tmp91_AST = null;
				tmp91_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp91_AST);
				match(POWER);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case NEGATE:
			{
				AST tmp92_AST = null;
				tmp92_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp92_AST);
				match(NEGATE);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case SQUAREROOT:
			{
				AST tmp93_AST = null;
				tmp93_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp93_AST);
				match(SQUAREROOT);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				AST tmp94_AST = null;
				tmp94_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp94_AST);
				match(INTLIT);
				AST tmp95_AST = null;
				tmp95_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp95_AST);
				match(Int);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case FLOTLIT:
			{
				AST tmp96_AST = null;
				tmp96_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp96_AST);
				match(FLOTLIT);
				AST tmp97_AST = null;
				tmp97_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp97_AST);
				match(Float);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case CONCAT:
			{
				AST tmp98_AST = null;
				tmp98_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp98_AST);
				match(CONCAT);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case STRINGLIT:
			{
				AST tmp99_AST = null;
				tmp99_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp99_AST);
				match(STRINGLIT);
				AST tmp100_AST = null;
				tmp100_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp100_AST);
				match(Identifier);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp101_AST = null;
				tmp101_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp101_AST);
				match(AVG);
				match(LeftParen);
				match(COLLECTION);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp105_AST = null;
				tmp105_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp105_AST);
				match(COUNT);
				match(LeftParen);
				match(COLLECTION);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case MAX:
			{
				AST tmp109_AST = null;
				tmp109_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp109_AST);
				match(MAX);
				match(LeftParen);
				match(COLLECTION);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case MIN:
			{
				AST tmp113_AST = null;
				tmp113_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp113_AST);
				match(MIN);
				match(LeftParen);
				match(COLLECTION);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp117_AST = null;
				tmp117_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp117_AST);
				match(SUM);
				match(LeftParen);
				match(COLLECTION);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			case LeftParen:
			{
				match(LeftParen);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RightParen);
				expression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
		returnAST = expression_AST;
	}
	
	public final void attributeName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST attributeName_AST = null;
		
		try {      // for error handling
			match(ATTRIBUTENAME);
			match(Quote);
			AST tmp125_AST = null;
			tmp125_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp125_AST);
			match(Identifier);
			match(Quote);
			attributeName_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		returnAST = attributeName_AST;
	}
	
	public final void attributeNameList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST attributeNameList_AST = null;
		
		try {      // for error handling
			attributeName();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop13:
			do {
				if ((LA(1)==Comma)) {
					match(Comma);
					attributeName();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop13;
				}
				
			} while (true);
			}
			attributeNameList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_6);
		}
		returnAST = attributeNameList_AST;
	}
	
	public final void booleanList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST booleanList_AST = null;
		
		try {      // for error handling
			match(LeftSquare);
			booleanExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop20:
			do {
				if ((LA(1)==Comma)) {
					match(Comma);
					booleanExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop20;
				}
				
			} while (true);
			}
			match(RightSquare);
			booleanList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		returnAST = booleanList_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"oneoffscan\"",
		"\"relproject\"",
		"\"relcrossproduct\"",
		"\"relselect\"",
		"\"relaggregation\"",
		"\"relrename\"",
		"\"relungroup\"",
		"\"relgroupby\"",
		"\"relgrselect\"",
		"\"strselect\"",
		"\"strproject\"",
		"\"stracquire\"",
		"\"strreceive\"",
		"STRSCROSSPRODUCT",
		"\"strrename\"",
		"\"dstreamop\"",
		"\"istreamop\"",
		"\"rstreamop\"",
		"\"timewindow\"",
		"Int",
		"\"rowwindow\"",
		"\"inputwindow\"",
		"\"winselect\"",
		"\"winproject\"",
		"\"repeatedscan\"",
		"\"wincrossproduct\"",
		"\"winrelcrossproduct\"",
		"\"winaggregation\"",
		"\"winrename\"",
		"\"winungroup\"",
		"\"wingroupby\"",
		"\"wingrselect\"",
		"\"deliver\"",
		"\"error\"",
		"Identifier",
		"LeftParen",
		"RightParen",
		"LeftSquare",
		"RightSquare",
		"Comma",
		"\"attribute\"",
		"\"tupleattribute\"",
		"\"add\"",
		"\"minus\"",
		"\"multiply\"",
		"\"divide\"",
		"\"power\"",
		"\"negate\"",
		"\"squareroot\"",
		"\"intlit\"",
		"FLOTLIT",
		"Float",
		"\"concat\"",
		"\"stringlit\"",
		"\"avg\"",
		"\"collection\"",
		"\"count\"",
		"\"max\"",
		"\"min\"",
		"\"sum\"",
		"\"attributename\"",
		"Quote",
		"\"extentname\"",
		"\"LocalName\"",
		"\"equals\"",
		"NOTEQULAS",
		"\"greaterthan\"",
		"\"lessthan\"",
		"\"greaterthanorequals\"",
		"\"lessthanorequals\"",
		"\"and\"",
		"\"or\"",
		"\"NOT\"",
		"\"true\"",
		"\"false\"",
		"\"timescopedef\"",
		"\"rowscopedef\"",
		"\"floatlit\"",
		"\"notequals\"",
		"\"strrelcrossproduct\"",
		"Dollar"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 1099511627776L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 549755813888L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 2748779069440L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 1649267441664L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 14843406974976L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 549764202496L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 4398046511104L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { -612492298101456896L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 14293651161088L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	
	}
