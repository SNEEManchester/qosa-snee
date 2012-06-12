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

package uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking;

//This file was generated using ANTLR, (ANother Tool for Language Recognition)
//Antlr was obtained from www.antlr.org

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import antlr.ASTFactory;
import antlr.ASTPair;

public class SNEEqlParser extends antlr.LLkParser implements
	SNEEqlParserTokenTypes {

    protected SNEEqlParser(final TokenBuffer tokenBuf, final int k) {
	super(tokenBuf, k);
	this.tokenNames = _tokenNames;
	this.buildTokenTypeASTClassMap();
	this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
    }

    public SNEEqlParser(final TokenBuffer tokenBuf) {
	this(tokenBuf, 10);
    }

    protected SNEEqlParser(final TokenStream lexer, final int k) {
	super(lexer, k);
	this.tokenNames = _tokenNames;
	this.buildTokenTypeASTClassMap();
	this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
    }

    public SNEEqlParser(final TokenStream lexer) {
	this(lexer, 10);
    }

    public SNEEqlParser(final ParserSharedInputState state) {
	super(state, 10);
	this.tokenNames = _tokenNames;
	this.buildTokenTypeASTClassMap();
	this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
    }

    public final void sneeqlStatement() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST sneeqlStatement_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    {
		this.query();
		this.astFactory.addASTChild(currentAST, this.returnAST);
	    }
	    {
		x = this.LT(1);
		x_AST = this.astFactory.create(x);
		this.astFactory.makeASTRoot(currentAST, x_AST);
		this.match(Semi);
	    }
	    x_AST.setType(QUERY);
	    sneeqlStatement_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = sneeqlStatement_AST;
    }

    public final void query() throws RecognitionException, TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST query_AST = null;

	try { // for error handling
	    this.selectClause();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.fromClause();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    {
		switch (this.LA(1)) {
		case WHERE: {
		    this.whereClause();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		case Semi:
		case MATERIALISE:
		case RightParen:
		case GROUPBY:
		case GROUP: {
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    {
		switch (this.LA(1)) {
		case GROUPBY:
		case GROUP: {
		    this.groupByClause();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		case Semi:
		case MATERIALISE:
		case RightParen: {
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    {
		switch (this.LA(1)) {
		case MATERIALISE: {
		    AST tmp1_AST = null;
		    tmp1_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp1_AST);
		    this.match(MATERIALISE);
		    break;
		}
		case Semi:
		case RightParen: {
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    query_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_1);
	}
	this.returnAST = query_AST;
    }

    public final void selectClause() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST selectClause_AST = null;
	Token x1 = null;
	AST x1_AST = null;
	Token x2 = null;
	AST x2_AST = null;
	Token x3 = null;
	AST x3_AST = null;

	try { // for error handling
	    {
		if ((this.LA(1) == SELECT) && (_tokenSet_2.member(this.LA(2)))) {
		    AST tmp2_AST = null;
		    tmp2_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.makeASTRoot(currentAST, tmp2_AST);
		    this.match(SELECT);
		} else if ((this.LA(1) == SELECT) && (this.LA(2) == ISTREAM)) {
		    {
			this.match(SELECT);
			x1 = this.LT(1);
			x1_AST = this.astFactory.create(x1);
			this.astFactory.makeASTRoot(currentAST, x1_AST);
			this.match(ISTREAM);
		    }
		    x1_AST.setType(SELECT);
		} else if ((this.LA(1) == SELECT) && (this.LA(2) == DSTREAM)) {
		    {
			this.match(SELECT);
			x2 = this.LT(1);
			x2_AST = this.astFactory.create(x2);
			this.astFactory.makeASTRoot(currentAST, x2_AST);
			this.match(DSTREAM);
		    }
		    x2_AST.setType(SELECT);
		} else if ((this.LA(1) == SELECT) && (this.LA(2) == RSTREAM)) {
		    {
			this.match(SELECT);
			x3 = this.LT(1);
			x3_AST = this.astFactory.create(x3);
			this.astFactory.makeASTRoot(currentAST, x3_AST);
			this.match(RSTREAM);
		    }
		    x3_AST.setType(SELECT);
		} else {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}

	    }
	    this.selList();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    selectClause_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_3);
	}
	this.returnAST = selectClause_AST;
    }

    public final void fromClause() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fromClause_AST = null;

	try { // for error handling
	    AST tmp6_AST = null;
	    tmp6_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp6_AST);
	    this.match(FROM);
	    this.fromList();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fromClause_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_4);
	}
	this.returnAST = fromClause_AST;
    }

    public final void whereClause() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST whereClause_AST = null;

	try { // for error handling
	    AST tmp7_AST = null;
	    tmp7_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp7_AST);
	    this.match(WHERE);
	    this.conditions();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    whereClause_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_5);
	}
	this.returnAST = whereClause_AST;
    }

    public final void groupByClause() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST groupByClause_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case GROUPBY: {
		    AST tmp8_AST = null;
		    tmp8_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.makeASTRoot(currentAST, tmp8_AST);
		    this.match(GROUPBY);
		    break;
		}
		case GROUP: {
		    x = this.LT(1);
		    x_AST = this.astFactory.create(x);
		    this.astFactory.makeASTRoot(currentAST, x_AST);
		    this.match(GROUP);
		    x_AST.setType(GROUPBY);
		    this.match(BY);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    {
		switch (this.LA(1)) {
		case Attribute: {
		    AST tmp10_AST = null;
		    tmp10_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp10_AST);
		    this.match(Attribute);
		    break;
		}
		case Identifier: {
		    AST tmp11_AST = null;
		    tmp11_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp11_AST);
		    this.match(Identifier);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    {
		_loop112: do {
		    if ((this.LA(1) == Comma)) {
			this.match(Comma);
			{
			    switch (this.LA(1)) {
			    case Attribute: {
				AST tmp13_AST = null;
				tmp13_AST = this.astFactory.create(this.LT(1));
				this.astFactory.addASTChild(currentAST,
					tmp13_AST);
				this.match(Attribute);
				break;
			    }
			    case Identifier: {
				AST tmp14_AST = null;
				tmp14_AST = this.astFactory.create(this.LT(1));
				this.astFactory.addASTChild(currentAST,
					tmp14_AST);
				this.match(Identifier);
				break;
			    }
			    default: {
				throw new NoViableAltException(this.LT(1), this
					.getFilename());
			    }
			    }
			}
		    } else {
			break _loop112;
		    }

		} while (true);
	    }
	    groupByClause_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_6);
	}
	this.returnAST = groupByClause_AST;
    }

    public final void selList() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST selList_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    switch (this.LA(1)) {
	    case Star: {
		AST tmp15_AST = null;
		tmp15_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp15_AST);
		this.match(Star);
		selList_AST = currentAST.root;
		break;
	    }
	    case Attribute:
	    case Identifier:
	    case COUNT:
	    case SUM:
	    case MAX:
	    case AVG:
	    case MIN:
	    case Float:
	    case Int:
	    case QuotedString:
	    case TRUE:
	    case FALSE: {
		{
		    {
			switch (this.LA(1)) {
			case COUNT:
			case SUM:
			case MAX:
			case AVG:
			case MIN: {
			    this.aggregate();
			    this.astFactory.addASTChild(currentAST,
				    this.returnAST);
			    break;
			}
			case Attribute:
			case Identifier:
			case Float:
			case Int:
			case QuotedString:
			case TRUE:
			case FALSE: {
			    this.listItem();
			    this.astFactory.addASTChild(currentAST,
				    this.returnAST);
			    break;
			}
			default: {
			    throw new NoViableAltException(this.LT(1), this
				    .getFilename());
			}
			}
		    }
		    {
			switch (this.LA(1)) {
			case AS: {
			    this.match(AS);
			    x = this.LT(1);
			    x_AST = this.astFactory.create(x);
			    this.astFactory.addASTChild(currentAST, x_AST);
			    this.match(Identifier);
			    x_AST.setType(SELECT_RENAME);
			    break;
			}
			case Comma:
			case FROM: {
			    break;
			}
			default: {
			    throw new NoViableAltException(this.LT(1), this
				    .getFilename());
			}
			}
		    }
		    {
			switch (this.LA(1)) {
			case Comma: {
			    this.match(Comma);
			    this.selList();
			    this.astFactory.addASTChild(currentAST,
				    this.returnAST);
			    break;
			}
			case FROM: {
			    break;
			}
			default: {
			    throw new NoViableAltException(this.LT(1), this
				    .getFilename());
			}
			}
		    }
		}
		selList_AST = currentAST.root;
		break;
	    }
	    default: {
		throw new NoViableAltException(this.LT(1), this.getFilename());
	    }
	    }
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_3);
	}
	this.returnAST = selList_AST;
    }

    public final void listItem() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST listItem_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case Attribute: {
		    AST tmp18_AST = null;
		    tmp18_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp18_AST);
		    this.match(Attribute);
		    break;
		}
		case Float:
		case Int:
		case QuotedString:
		case TRUE:
		case FALSE: {
		    this.literal();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		default:
		    if ((this.LA(1) == Identifier)
			    && (_tokenSet_7.member(this.LA(2)))) {
			AST tmp19_AST = null;
			tmp19_AST = this.astFactory.create(this.LT(1));
			this.astFactory.addASTChild(currentAST, tmp19_AST);
			this.match(Identifier);
		    } else if ((this.LA(1) == Identifier)
			    && (this.LA(2) == LeftParen)) {
			this.functionCall();
			this.astFactory.addASTChild(currentAST, this.returnAST);
		    } else {
			throw new NoViableAltException(this.LT(1), this
				.getFilename());
		    }
		}
	    }
	    listItem_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_7);
	}
	this.returnAST = listItem_AST;
    }

    public final void literal() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST literal_AST = null;

	try { // for error handling
	    switch (this.LA(1)) {
	    case QuotedString: {
		AST tmp20_AST = null;
		tmp20_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp20_AST);
		this.match(QuotedString);
		literal_AST = currentAST.root;
		break;
	    }
	    case Int: {
		AST tmp21_AST = null;
		tmp21_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp21_AST);
		this.match(Int);
		literal_AST = currentAST.root;
		break;
	    }
	    case Float: {
		AST tmp22_AST = null;
		tmp22_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp22_AST);
		this.match(Float);
		literal_AST = currentAST.root;
		break;
	    }
	    case TRUE: {
		AST tmp23_AST = null;
		tmp23_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp23_AST);
		this.match(TRUE);
		literal_AST = currentAST.root;
		break;
	    }
	    case FALSE: {
		AST tmp24_AST = null;
		tmp24_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp24_AST);
		this.match(FALSE);
		literal_AST = currentAST.root;
		break;
	    }
	    default: {
		throw new NoViableAltException(this.LT(1), this.getFilename());
	    }
	    }
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_8);
	}
	this.returnAST = literal_AST;
    }

    public final void functionCall() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST functionCall_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    x = this.LT(1);
	    x_AST = this.astFactory.create(x);
	    this.astFactory.makeASTRoot(currentAST, x_AST);
	    this.match(Identifier);
	    x_AST.setType(FUNCTION);
	    this.match(LeftParen);
	    this.parameterList();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.match(RightParen);
	    functionCall_AST = currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_7);
	}
	this.returnAST = functionCall_AST;
    }

    public final void parameter() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST parameter_AST = null;

	try { // for error handling
	    switch (this.LA(1)) {
	    case Attribute: {
		AST tmp27_AST = null;
		tmp27_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp27_AST);
		this.match(Attribute);
		parameter_AST = currentAST.root;
		break;
	    }
	    case Float:
	    case Int:
	    case QuotedString:
	    case TRUE:
	    case FALSE: {
		this.literal();
		this.astFactory.addASTChild(currentAST, this.returnAST);
		parameter_AST = currentAST.root;
		break;
	    }
	    case Identifier: {
		AST tmp28_AST = null;
		tmp28_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp28_AST);
		this.match(Identifier);
		parameter_AST = currentAST.root;
		break;
	    }
	    default: {
		throw new NoViableAltException(this.LT(1), this.getFilename());
	    }
	    }
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_9);
	}
	this.returnAST = parameter_AST;
    }

    public final void parameterList() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST parameterList_AST = null;

	try { // for error handling
	    {
		this.parameter();
		this.astFactory.addASTChild(currentAST, this.returnAST);
	    }
	    {
		switch (this.LA(1)) {
		case Comma: {
		    this.match(Comma);
		    this.parameterList();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		case RightParen: {
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    parameterList_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_10);
	}
	this.returnAST = parameterList_AST;
    }

    public final void aggregate() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST aggregate_AST = null;
	Token x1 = null;
	AST x1_AST = null;
	Token x2 = null;
	AST x2_AST = null;
	Token x3 = null;
	AST x3_AST = null;
	Token x4 = null;
	AST x4_AST = null;
	Token x5 = null;
	AST x5_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case COUNT: {
		    x1 = this.LT(1);
		    x1_AST = this.astFactory.create(x1);
		    this.astFactory.makeASTRoot(currentAST, x1_AST);
		    this.match(COUNT);
		    x1_AST.setType(AGGREGATE);
		    break;
		}
		case SUM: {
		    x2 = this.LT(1);
		    x2_AST = this.astFactory.create(x2);
		    this.astFactory.makeASTRoot(currentAST, x2_AST);
		    this.match(SUM);
		    x2_AST.setType(AGGREGATE);
		    break;
		}
		case MAX: {
		    x3 = this.LT(1);
		    x3_AST = this.astFactory.create(x3);
		    this.astFactory.makeASTRoot(currentAST, x3_AST);
		    this.match(MAX);
		    x3_AST.setType(AGGREGATE);
		    break;
		}
		case AVG: {
		    x4 = this.LT(1);
		    x4_AST = this.astFactory.create(x4);
		    this.astFactory.makeASTRoot(currentAST, x4_AST);
		    this.match(AVG);
		    x4_AST.setType(AGGREGATE);
		    break;
		}
		case MIN: {
		    x5 = this.LT(1);
		    x5_AST = this.astFactory.create(x5);
		    this.astFactory.makeASTRoot(currentAST, x5_AST);
		    this.match(MIN);
		    x5_AST.setType(AGGREGATE);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    this.match(LeftParen);
	    {
		this.listItem();
		this.astFactory.addASTChild(currentAST, this.returnAST);
	    }
	    this.match(RightParen);
	    aggregate_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_11);
	}
	this.returnAST = aggregate_AST;
    }

    public final void fromList() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fromList_AST = null;

	try { // for error handling
	    this.fromItem();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    {
		_loop31: do {
		    if ((this.LA(1) == Comma)) {
			this.match(Comma);
			this.fromItem();
			this.astFactory.addASTChild(currentAST, this.returnAST);
		    } else {
			break _loop31;
		    }

		} while (true);
	    }
	    fromList_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_4);
	}
	this.returnAST = fromList_AST;
    }

    public final void fromItem() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fromItem_AST = null;
	Token x1 = null;
	AST x1_AST = null;

	try { // for error handling
	    x1 = this.LT(1);
	    x1_AST = this.astFactory.create(x1);
	    this.astFactory.makeASTRoot(currentAST, x1_AST);
	    this.match(Identifier);
	    x1_AST.setType(SOURCE);
	    this.window();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fromItem_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_12);
	}
	this.returnAST = fromItem_AST;
    }

    public final void subQuery() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST subQuery_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    {
		x = this.LT(1);
		x_AST = this.astFactory.create(x);
		this.astFactory.makeASTRoot(currentAST, x_AST);
		this.match(LeftParen);
		this.query();
		this.astFactory.addASTChild(currentAST, this.returnAST);
		this.match(RightParen);
	    }
	    x_AST.setText("subQuery");
	    subQuery_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = subQuery_AST;
    }

    public final void window() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST window_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case LeftSquare: {
		    this.match(LeftSquare);
		    {
			switch (this.LA(1)) {
			case NOW: {
			    AST tmp35_AST = null;
			    tmp35_AST = this.astFactory.create(this.LT(1));
			    this.astFactory.addASTChild(currentAST, tmp35_AST);
			    this.match(NOW);
			    break;
			}
			case NEW: {
			    AST tmp36_AST = null;
			    tmp36_AST = this.astFactory.create(this.LT(1));
			    this.astFactory.addASTChild(currentAST, tmp36_AST);
			    this.match(NEW);
			    break;
			}
			case UNBOUNDED: {
			    AST tmp37_AST = null;
			    tmp37_AST = this.astFactory.create(this.LT(1));
			    this.astFactory.addASTChild(currentAST, tmp37_AST);
			    this.match(UNBOUNDED);
			    break;
			}
			default:
			    if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (_tokenSet_13.member(this.LA(4)))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (_tokenSet_13.member(this.LA(8)))) {
				{
				    this.fullFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (_tokenSet_13.member(this.LA(4)))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && (this.LA(7) == Minus)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (_tokenSet_13.member(this.LA(9)))) {
				{
				    this.fullFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (_tokenSet_13.member(this.LA(4)))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (this.LA(8) == SLIDE)) {
				{
				    this.fullFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (_tokenSet_13.member(this.LA(4)))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && (this.LA(7) == Minus)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (this.LA(9) == SLIDE)) {
				{
				    this.fullFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (_tokenSet_13.member(this.LA(5)))
				    && (this.LA(6) == TO)
				    && (this.LA(7) == NOW)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (_tokenSet_13.member(this.LA(9)))) {
				{
				    this.fullFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (_tokenSet_13.member(this.LA(5)))
				    && (this.LA(6) == TO)
				    && (this.LA(7) == NOW)
				    && (this.LA(8) == Minus)
				    && ((this.LA(9) == Float) || (this.LA(9) == Int))
				    && (_tokenSet_13.member(this.LA(10)))) {
				{
				    this.fullFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (_tokenSet_13.member(this.LA(5)))
				    && (this.LA(6) == TO)
				    && (this.LA(7) == NOW)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (this.LA(9) == SLIDE)) {
				{
				    this.fullFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (_tokenSet_13.member(this.LA(5)))
				    && (this.LA(6) == TO)
				    && (this.LA(7) == NOW)
				    && (this.LA(8) == Minus)
				    && ((this.LA(9) == Float) || (this.LA(9) == Int))
				    && (this.LA(10) == SLIDE)) {
				{
				    this.fullFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (_tokenSet_13.member(this.LA(4)))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && ((this.LA(7) == RightSquare) || (this
					    .LA(7) == SLIDE))) {
				{
				    this.fullFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.nowTo();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (_tokenSet_13.member(this.LA(5)))
				    && (this.LA(6) == TO)
				    && (this.LA(7) == NOW)
				    && ((this.LA(8) == RightSquare) || (this
					    .LA(8) == SLIDE))) {
				{
				    this.fullFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.nowTo();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (this.LA(4) == TO)
				    && (this.LA(5) == NOW)
				    && ((this.LA(6) == Float) || (this.LA(6) == Int))
				    && (_tokenSet_13.member(this.LA(7)))) {
				{
				    this.noUnitFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (this.LA(4) == TO)
				    && (this.LA(5) == NOW)
				    && (this.LA(6) == Minus)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (_tokenSet_13.member(this.LA(8)))) {
				{
				    this.noUnitFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (this.LA(4) == TO)
				    && (this.LA(5) == NOW)
				    && ((this.LA(6) == Float) || (this.LA(6) == Int))
				    && (this.LA(7) == SLIDE)) {
				{
				    this.noUnitFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (this.LA(4) == TO)
				    && (this.LA(5) == NOW)
				    && (this.LA(6) == Minus)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (this.LA(8) == SLIDE)) {
				{
				    this.noUnitFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (_tokenSet_13.member(this.LA(8)))) {
				{
				    this.noUnitFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && (this.LA(7) == Minus)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (_tokenSet_13.member(this.LA(9)))) {
				{
				    this.noUnitFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.fullTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && ((this.LA(7) == Float) || (this.LA(7) == Int))
				    && (this.LA(8) == SLIDE)) {
				{
				    this.noUnitFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && (this.LA(7) == Minus)
				    && ((this.LA(8) == Float) || (this.LA(8) == Int))
				    && (this.LA(9) == SLIDE)) {
				{
				    this.noUnitFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.noUnitTo2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))
				    && (this.LA(4) == TO)
				    && (this.LA(5) == NOW)
				    && (this.LA(6) == SLIDE)) {
				{
				    this.noUnitFrom1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.nowTo();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == FROM)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)
				    && ((this.LA(4) == Float) || (this.LA(4) == Int))
				    && (this.LA(5) == TO)
				    && (this.LA(6) == NOW)
				    && (this.LA(7) == SLIDE)) {
				{
				    this.noUnitFrom2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.nowTo();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == RANGE)
				    && ((this.LA(2) == Float) || (this.LA(2) == Int))
				    && (_tokenSet_13.member(this.LA(3)))) {
				{
				    this.fullRange();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == RANGE)
				    && ((this.LA(2) == Float) || (this.LA(2) == Int))
				    && (this.LA(3) == SLIDE)) {
				{
				    this.noUnitRange();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else if ((this.LA(1) == AT)
				    && (this.LA(2) == NOW)
				    && ((this.LA(3) == Float) || (this.LA(3) == Int))) {
				{
				    this.fullAt1();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == AT)
				    && (this.LA(2) == NOW)
				    && (this.LA(3) == Minus)) {
				{
				    this.fullAt2();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    {
					switch (this.LA(1)) {
					case SLIDE: {
					    this.slideWindowPart();
					    this.astFactory.addASTChild(
						    currentAST, this.returnAST);
					    break;
					}
					case RightSquare: {
					    break;
					}
					default: {
					    throw new NoViableAltException(this
						    .LT(1), this.getFilename());
					}
					}
				    }
				}
			    } else if ((this.LA(1) == AT)
				    && ((this.LA(2) == Float) || (this.LA(2) == Int))) {
				{
				    this.noUnitAt();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				    this.slideWindowPart();
				    this.astFactory.addASTChild(currentAST,
					    this.returnAST);
				}
			    } else {
				throw new NoViableAltException(this.LT(1), this
					.getFilename());
			    }
			}
		    }
		    this.match(RightSquare);
		    break;
		}
		case Semi:
		case MATERIALISE:
		case Comma:
		case RightParen:
		case WHERE:
		case GROUPBY:
		case GROUP: {
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    window_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_12);
	}
	this.returnAST = window_AST;
    }

    public final void fullFrom1() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullFrom1_AST = null;

	try { // for error handling
	    AST tmp39_AST = null;
	    tmp39_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp39_AST);
	    this.match(FROM);
	    AST tmp40_AST = null;
	    tmp40_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp40_AST);
	    this.match(NOW);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullFrom1_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_14);
	}
	this.returnAST = fullFrom1_AST;
    }

    public final void fullTo1() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullTo1_AST = null;

	try { // for error handling
	    AST tmp41_AST = null;
	    tmp41_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp41_AST);
	    this.match(TO);
	    AST tmp42_AST = null;
	    tmp42_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp42_AST);
	    this.match(NOW);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullTo1_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = fullTo1_AST;
    }

    public final void slideWindowPart() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST slideWindowPart_AST = null;

	try { // for error handling
	    AST tmp43_AST = null;
	    tmp43_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp43_AST);
	    this.match(SLIDE);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    slideWindowPart_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_16);
	}
	this.returnAST = slideWindowPart_AST;
    }

    public final void fullTo2() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullTo2_AST = null;

	try { // for error handling
	    AST tmp44_AST = null;
	    tmp44_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp44_AST);
	    this.match(TO);
	    AST tmp45_AST = null;
	    tmp45_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp45_AST);
	    this.match(NOW);
	    AST tmp46_AST = null;
	    tmp46_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp46_AST);
	    this.match(Minus);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullTo2_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = fullTo2_AST;
    }

    public final void noUnitTo1() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitTo1_AST = null;

	try { // for error handling
	    AST tmp47_AST = null;
	    tmp47_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp47_AST);
	    this.match(TO);
	    AST tmp48_AST = null;
	    tmp48_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp48_AST);
	    this.match(NOW);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitTo1_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_17);
	}
	this.returnAST = noUnitTo1_AST;
    }

    public final void noUnitTo2() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitTo2_AST = null;

	try { // for error handling
	    AST tmp49_AST = null;
	    tmp49_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp49_AST);
	    this.match(TO);
	    AST tmp50_AST = null;
	    tmp50_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp50_AST);
	    this.match(NOW);
	    AST tmp51_AST = null;
	    tmp51_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp51_AST);
	    this.match(Minus);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitTo2_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_17);
	}
	this.returnAST = noUnitTo2_AST;
    }

    public final void fullFrom2() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullFrom2_AST = null;

	try { // for error handling
	    AST tmp52_AST = null;
	    tmp52_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp52_AST);
	    this.match(FROM);
	    AST tmp53_AST = null;
	    tmp53_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp53_AST);
	    this.match(NOW);
	    AST tmp54_AST = null;
	    tmp54_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp54_AST);
	    this.match(Minus);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullFrom2_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_14);
	}
	this.returnAST = fullFrom2_AST;
    }

    public final void nowTo() throws RecognitionException, TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST nowTo_AST = null;

	try { // for error handling
	    AST tmp55_AST = null;
	    tmp55_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp55_AST);
	    this.match(TO);
	    AST tmp56_AST = null;
	    tmp56_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp56_AST);
	    this.match(NOW);
	    nowTo_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = nowTo_AST;
    }

    public final void noUnitFrom1() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitFrom1_AST = null;

	try { // for error handling
	    AST tmp57_AST = null;
	    tmp57_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp57_AST);
	    this.match(FROM);
	    AST tmp58_AST = null;
	    tmp58_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp58_AST);
	    this.match(NOW);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitFrom1_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_14);
	}
	this.returnAST = noUnitFrom1_AST;
    }

    public final void noUnitFrom2() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitFrom2_AST = null;

	try { // for error handling
	    AST tmp59_AST = null;
	    tmp59_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp59_AST);
	    this.match(FROM);
	    AST tmp60_AST = null;
	    tmp60_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp60_AST);
	    this.match(NOW);
	    AST tmp61_AST = null;
	    tmp61_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp61_AST);
	    this.match(Minus);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitFrom2_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_14);
	}
	this.returnAST = noUnitFrom2_AST;
    }

    public final void fullRange() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullRange_AST = null;

	try { // for error handling
	    AST tmp62_AST = null;
	    tmp62_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp62_AST);
	    this.match(RANGE);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullRange_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = fullRange_AST;
    }

    public final void noUnitRange() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitRange_AST = null;

	try { // for error handling
	    AST tmp63_AST = null;
	    tmp63_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp63_AST);
	    this.match(RANGE);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitRange_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_17);
	}
	this.returnAST = noUnitRange_AST;
    }

    public final void fullAt1() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullAt1_AST = null;

	try { // for error handling
	    AST tmp64_AST = null;
	    tmp64_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp64_AST);
	    this.match(AT);
	    AST tmp65_AST = null;
	    tmp65_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp65_AST);
	    this.match(NOW);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullAt1_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = fullAt1_AST;
    }

    public final void fullAt2() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST fullAt2_AST = null;

	try { // for error handling
	    AST tmp66_AST = null;
	    tmp66_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp66_AST);
	    this.match(AT);
	    AST tmp67_AST = null;
	    tmp67_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp67_AST);
	    this.match(NOW);
	    AST tmp68_AST = null;
	    tmp68_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp68_AST);
	    this.match(Minus);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    this.unit();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    fullAt2_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_15);
	}
	this.returnAST = fullAt2_AST;
    }

    public final void noUnitAt() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST noUnitAt_AST = null;

	try { // for error handling
	    AST tmp69_AST = null;
	    tmp69_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp69_AST);
	    this.match(AT);
	    this.value();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    noUnitAt_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_17);
	}
	this.returnAST = noUnitAt_AST;
    }

    public final void value() throws RecognitionException, TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST value_AST = null;
	Token x1 = null;
	AST x1_AST = null;

	try { // for error handling
	    switch (this.LA(1)) {
	    case Float: {
		AST tmp70_AST = null;
		tmp70_AST = this.astFactory.create(this.LT(1));
		this.astFactory.addASTChild(currentAST, tmp70_AST);
		this.match(Float);
		value_AST = (AST) currentAST.root;
		break;
	    }
	    case Int: {
		x1 = this.LT(1);
		x1_AST = this.astFactory.create(x1);
		this.astFactory.addASTChild(currentAST, x1_AST);
		this.match(Int);
		x1_AST.setType(Float);
		value_AST = (AST) currentAST.root;
		break;
	    }
	    default: {
		throw new NoViableAltException(this.LT(1), this.getFilename());
	    }
	    }
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_18);
	}
	this.returnAST = value_AST;
    }

    public final void unit() throws RecognitionException, TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST unit_AST = null;
	Token x1 = null;
	AST x1_AST = null;
	Token x2 = null;
	AST x2_AST = null;
	Token x3 = null;
	AST x3_AST = null;
	Token x4 = null;
	AST x4_AST = null;
	Token x41 = null;
	AST x41_AST = null;
	Token x42 = null;
	AST x42_AST = null;
	Token x5 = null;
	AST x5_AST = null;
	Token x6 = null;
	AST x6_AST = null;
	Token x61 = null;
	AST x61_AST = null;
	Token x62 = null;
	AST x62_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case HOURS: {
		    x1 = this.LT(1);
		    x1_AST = this.astFactory.create(x1);
		    this.astFactory.makeASTRoot(currentAST, x1_AST);
		    this.match(HOURS);
		    x1_AST.setType(TIME_NAME);
		    break;
		}
		case HOUR: {
		    x2 = this.LT(1);
		    x2_AST = this.astFactory.create(x2);
		    this.astFactory.makeASTRoot(currentAST, x2_AST);
		    this.match(HOUR);
		    x2_AST.setType(TIME_NAME);
		    x2_AST.setText("hours");
		    break;
		}
		case MINUTES: {
		    x3 = this.LT(1);
		    x3_AST = this.astFactory.create(x3);
		    this.astFactory.makeASTRoot(currentAST, x3_AST);
		    this.match(MINUTES);
		    x3_AST.setType(TIME_NAME);
		    break;
		}
		case MINUTE: {
		    x4 = this.LT(1);
		    x4_AST = this.astFactory.create(x4);
		    this.astFactory.makeASTRoot(currentAST, x4_AST);
		    this.match(MINUTE);
		    x4_AST.setType(TIME_NAME);
		    x4_AST.setText("minutes");
		    break;
		}
		case MIN: {
		    x41 = this.LT(1);
		    x41_AST = this.astFactory.create(x41);
		    this.astFactory.makeASTRoot(currentAST, x41_AST);
		    this.match(MIN);
		    x41_AST.setType(TIME_NAME);
		    x41_AST.setText("minutes");
		    break;
		}
		case MINS: {
		    x42 = this.LT(1);
		    x42_AST = this.astFactory.create(x42);
		    this.astFactory.makeASTRoot(currentAST, x42_AST);
		    this.match(MINS);
		    x41_AST.setType(TIME_NAME);
		    x41_AST.setText("minutes");
		    break;
		}
		case SECONDS: {
		    x5 = this.LT(1);
		    x5_AST = this.astFactory.create(x5);
		    this.astFactory.makeASTRoot(currentAST, x5_AST);
		    this.match(SECONDS);
		    x5_AST.setType(TIME_NAME);
		    break;
		}
		case SECOND: {
		    x6 = this.LT(1);
		    x6_AST = this.astFactory.create(x6);
		    this.astFactory.makeASTRoot(currentAST, x6_AST);
		    this.match(SECOND);
		    x6_AST.setType(TIME_NAME);
		    x6_AST.setText("seconds");
		    break;
		}
		case SEC: {
		    x61 = this.LT(1);
		    x61_AST = this.astFactory.create(x61);
		    this.astFactory.makeASTRoot(currentAST, x61_AST);
		    this.match(SEC);
		    x61_AST.setType(TIME_NAME);
		    x61_AST.setText("seconds");
		    break;
		}
		case SECS: {
		    x62 = this.LT(1);
		    x62_AST = this.astFactory.create(x62);
		    this.astFactory.makeASTRoot(currentAST, x62_AST);
		    this.match(SECS);
		    x62_AST.setType(TIME_NAME);
		    x62_AST.setText("seconds");
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    unit_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_19);
	}
	this.returnAST = unit_AST;
    }

    public final void rowUntil() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST rowUntil_AST = null;

	try { // for error handling
	    AST tmp71_AST = null;
	    tmp71_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp71_AST);
	    this.match(UNTIL);
	    AST tmp72_AST = null;
	    tmp72_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp72_AST);
	    this.match(Int);
	    rowUntil_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = rowUntil_AST;
    }

    public final void rowSlide() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST rowSlide_AST = null;
	Token x = null;
	AST x_AST = null;

	try { // for error handling
	    AST tmp73_AST = null;
	    tmp73_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp73_AST);
	    this.match(SLIDE);
	    AST tmp74_AST = null;
	    tmp74_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.addASTChild(currentAST, tmp74_AST);
	    this.match(Int);
	    {
		switch (this.LA(1)) {
		case ROWS: {
		    this.match(ROWS);
		    break;
		}
		case ROW: {
		    x = this.LT(1);
		    x_AST = this.astFactory.create(x);
		    this.match(ROW);
		    x_AST.setType(ROWS);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    rowSlide_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = rowSlide_AST;
    }

    public final void conditions() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST conditions_AST = null;

	try { // for error handling
	    this.condition();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    {
		_loop100: do {
		    if ((this.LA(1) == AND)) {
			this.match(AND);
			this.condition();
			this.astFactory.addASTChild(currentAST, this.returnAST);
		    } else {
			break _loop100;
		    }

		} while (true);
	    }
	    conditions_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_5);
	}
	this.returnAST = conditions_AST;
    }

    public final void condition() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST condition_AST = null;

	try { // for error handling
	    if (((this.LA(1) == Attribute) || (this.LA(1) == Identifier))
		    && (this.LA(2) == IN)) {
		{
		    switch (this.LA(1)) {
		    case Attribute: {
			AST tmp77_AST = null;
			tmp77_AST = this.astFactory.create(this.LT(1));
			this.astFactory.addASTChild(currentAST, tmp77_AST);
			this.match(Attribute);
			break;
		    }
		    case Identifier: {
			AST tmp78_AST = null;
			tmp78_AST = this.astFactory.create(this.LT(1));
			this.astFactory.addASTChild(currentAST, tmp78_AST);
			this.match(Identifier);
			break;
		    }
		    default: {
			throw new NoViableAltException(this.LT(1), this
				.getFilename());
		    }
		    }
		}
		AST tmp79_AST = null;
		tmp79_AST = this.astFactory.create(this.LT(1));
		this.astFactory.makeASTRoot(currentAST, tmp79_AST);
		this.match(IN);
		{
		    this.match(LeftParen);
		    this.query();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    this.match(RightParen);
		}
		condition_AST = (AST) currentAST.root;
	    } else if ((_tokenSet_20.member(this.LA(1)))
		    && ((this.LA(2) == LeftParen) || (this.LA(2) == Pred))) {
		this.listItem();
		this.astFactory.addASTChild(currentAST, this.returnAST);
		AST tmp82_AST = null;
		tmp82_AST = this.astFactory.create(this.LT(1));
		this.astFactory.makeASTRoot(currentAST, tmp82_AST);
		this.match(Pred);
		{
		    switch (this.LA(1)) {
		    case Attribute:
		    case Identifier:
		    case Float:
		    case Int:
		    case QuotedString:
		    case TRUE:
		    case FALSE: {
			this.listItem();
			this.astFactory.addASTChild(currentAST, this.returnAST);
			break;
		    }
		    case LeftParen: {
			{
			    this.match(LeftParen);
			    this.query();
			    this.astFactory.addASTChild(currentAST,
				    this.returnAST);
			    this.match(RightParen);
			}
			break;
		    }
		    default: {
			throw new NoViableAltException(this.LT(1), this
				.getFilename());
		    }
		    }
		}
		condition_AST = (AST) currentAST.root;
	    } else {
		throw new NoViableAltException(this.LT(1), this.getFilename());
	    }

	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_21);
	}
	this.returnAST = condition_AST;
    }

    public final void havingClause() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST havingClause_AST = null;

	try { // for error handling
	    AST tmp85_AST = null;
	    tmp85_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp85_AST);
	    this.match(HAVING);
	    this.havingConditions();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    havingClause_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = havingClause_AST;
    }

    public final void havingConditions() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST havingConditions_AST = null;

	try { // for error handling
	    this.havingCondition();
	    this.astFactory.addASTChild(currentAST, this.returnAST);
	    {
		_loop116: do {
		    if ((this.LA(1) == AND)) {
			this.match(AND);
			this.havingCondition();
			this.astFactory.addASTChild(currentAST, this.returnAST);
		    } else {
			break _loop116;
		    }

		} while (true);
	    }
	    havingConditions_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_0);
	}
	this.returnAST = havingConditions_AST;
    }

    public final void havingCondition() throws RecognitionException,
	    TokenStreamException {

	this.returnAST = null;
	final ASTPair currentAST = new ASTPair();
	AST havingCondition_AST = null;

	try { // for error handling
	    {
		switch (this.LA(1)) {
		case Attribute: {
		    AST tmp87_AST = null;
		    tmp87_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp87_AST);
		    this.match(Attribute);
		    break;
		}
		case Float:
		case Int:
		case QuotedString:
		case TRUE:
		case FALSE: {
		    this.literal();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		case Identifier: {
		    AST tmp88_AST = null;
		    tmp88_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp88_AST);
		    this.match(Identifier);
		    break;
		}
		case COUNT:
		case SUM:
		case MAX:
		case AVG:
		case MIN: {
		    this.aggregate();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    AST tmp89_AST = null;
	    tmp89_AST = this.astFactory.create(this.LT(1));
	    this.astFactory.makeASTRoot(currentAST, tmp89_AST);
	    this.match(Pred);
	    {
		switch (this.LA(1)) {
		case Attribute: {
		    AST tmp90_AST = null;
		    tmp90_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp90_AST);
		    this.match(Attribute);
		    break;
		}
		case Float:
		case Int:
		case QuotedString:
		case TRUE:
		case FALSE: {
		    this.literal();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		case Identifier: {
		    AST tmp91_AST = null;
		    tmp91_AST = this.astFactory.create(this.LT(1));
		    this.astFactory.addASTChild(currentAST, tmp91_AST);
		    this.match(Identifier);
		    break;
		}
		case COUNT:
		case SUM:
		case MAX:
		case AVG:
		case MIN: {
		    this.aggregate();
		    this.astFactory.addASTChild(currentAST, this.returnAST);
		    break;
		}
		default: {
		    throw new NoViableAltException(this.LT(1), this
			    .getFilename());
		}
		}
	    }
	    havingCondition_AST = (AST) currentAST.root;
	} catch (final RecognitionException ex) {
	    this.reportError(ex);
	    this.recover(ex, _tokenSet_22);
	}
	this.returnAST = havingCondition_AST;
    }

    public static final String[] _tokenNames = {
	    "<0>",
	    "EOF",
	    "<2>",
	    "NULL_TREE_LOOKAHEAD",
	    "Semi",
	    "\"materialise\"",
	    "\"select\"",
	    "\"istream\"",
	    "\"dstream\"",
	    "\"rstream\"",
	    "Attribute",
	    "Identifier",
	    "Comma",
	    "LeftParen",
	    "RightParen",
	    "Star",
	    "\"as\"",
	    "\"count\"",
	    "\"sum\"",
	    "\"max\"",
	    "\"avg\"",
	    "\"min\"",
	    "\"from\"",
	    "LeftSquare",
	    "\"now\"",
	    "\"new\"",
	    "\"UNBOUNDED\"",
	    "RightSquare",
	    "\"at\"",
	    "Minus",
	    "\"to\"",
	    "\"range\"",
	    "\"slide\"",
	    "Float",
	    "Int",
	    "\"hours\"",
	    "\"hour\"",
	    "\"minutes\"",
	    "\"minute\"",
	    "\"mins\"",
	    "\"seconds\"",
	    "\"second\"",
	    "\"sec\"",
	    "\"secs\"",
	    "\"until\"",
	    "\"rows\"",
	    "\"row\"",
	    "\"where\"",
	    "\"and\"",
	    "\"in\"",
	    "Pred",
	    "QuotedString",
	    "\"true\"",
	    "\"false\"",
	    "\"groupby\"",
	    "\"group\"",
	    "\"by\"",
	    "HAVING",
	    "\"up\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_AGGREGATE\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_FUNCTION\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_PLUS\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_QUERY\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SELECT_RENAME\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE_RENAME\"",
	    "\"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_TIME_NAME\"" };

    protected void buildTokenTypeASTClassMap() {
	this.tokenTypeToASTClassMap = null;
    };

    private static final long[] mk_tokenSet_0() {
	final long[] data = { 2L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

    private static final long[] mk_tokenSet_1() {
	final long[] data = { 16400L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

    private static final long[] mk_tokenSet_2() {
	final long[] data = { 15762624469699584L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

    private static final long[] mk_tokenSet_3() {
	final long[] data = { 4194304L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());

    private static final long[] mk_tokenSet_4() {
	final long[] data = { 54183933016817712L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

    private static final long[] mk_tokenSet_5() {
	final long[] data = { 54043195528462384L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());

    private static final long[] mk_tokenSet_6() {
	final long[] data = { 16432L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());

    private static final long[] mk_tokenSet_7() {
	final long[] data = { 55450570416279600L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());

    private static final long[] mk_tokenSet_8() {
	final long[] data = { 55450570416279602L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());

    private static final long[] mk_tokenSet_9() {
	final long[] data = { 20480L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());

    private static final long[] mk_tokenSet_10() {
	final long[] data = { 16384L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());

    private static final long[] mk_tokenSet_11() {
	final long[] data = { 1407374887817218L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());

    private static final long[] mk_tokenSet_12() {
	final long[] data = { 54183933016821808L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());

    private static final long[] mk_tokenSet_13() {
	final long[] data = { 17557828403200L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());

    private static final long[] mk_tokenSet_14() {
	final long[] data = { 1073741824L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());

    private static final long[] mk_tokenSet_15() {
	final long[] data = { 4429185024L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());

    private static final long[] mk_tokenSet_16() {
	final long[] data = { 134217728L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());

    private static final long[] mk_tokenSet_17() {
	final long[] data = { 4294967296L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());

    private static final long[] mk_tokenSet_18() {
	final long[] data = { 17563197112320L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());

    private static final long[] mk_tokenSet_19() {
	final long[] data = { 5502926848L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());

    private static final long[] mk_tokenSet_20() {
	final long[] data = { 15762624465603584L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());

    private static final long[] mk_tokenSet_21() {
	final long[] data = { 54324670505173040L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());

    private static final long[] mk_tokenSet_22() {
	final long[] data = { 281474976710658L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());

}
