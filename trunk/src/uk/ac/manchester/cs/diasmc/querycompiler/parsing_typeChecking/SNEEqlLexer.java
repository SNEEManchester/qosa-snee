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

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;

public class SNEEqlLexer extends antlr.CharScanner implements
	SNEEqlParserTokenTypes, TokenStream {
    public SNEEqlLexer(final InputStream in) {
	this(new ByteBuffer(in));
    }

    public SNEEqlLexer(final Reader in) {
	this(new CharBuffer(in));
    }

    public SNEEqlLexer(final InputBuffer ib) {
	this(new LexerSharedInputState(ib));
    }

    public SNEEqlLexer(final LexerSharedInputState state) {
	super(state);
	this.caseSensitiveLiterals = false;
	this.setCaseSensitive(false);
	this.literals = new Hashtable();
	this.literals
		.put(new ANTLRHashString("seconds", this), new Integer(40));
	this.literals.put(new ANTLRHashString("to", this), new Integer(30));
	this.literals.put(new ANTLRHashString("count", this), new Integer(17));
	this.literals.put(new ANTLRHashString("sum", this), new Integer(18));
	this.literals.put(new ANTLRHashString("min", this), new Integer(21));
	this.literals.put(new ANTLRHashString("rows", this), new Integer(45));
	this.literals.put(new ANTLRHashString("sec", this), new Integer(42));
	this.literals.put(new ANTLRHashString("false", this), new Integer(53));
	this.literals.put(new ANTLRHashString("true", this), new Integer(52));
	this.literals
		.put(new ANTLRHashString("minutes", this), new Integer(37));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_PLUS",
				this), new Integer(61));
	this.literals.put(new ANTLRHashString("mins", this), new Integer(39));
	this.literals.put(new ANTLRHashString("and", this), new Integer(48));
	this.literals.put(new ANTLRHashString("select", this), new Integer(6));
	this.literals.put(new ANTLRHashString("hour", this), new Integer(36));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE",
				this), new Integer(64));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE_RENAME",
				this), new Integer(65));
	this.literals.put(new ANTLRHashString("secs", this), new Integer(43));
	this.literals.put(new ANTLRHashString("group", this), new Integer(55));
	this.literals.put(new ANTLRHashString("where", this), new Integer(47));
	this.literals.put(new ANTLRHashString("dstream", this), new Integer(8));
	this.literals.put(new ANTLRHashString("second", this), new Integer(41));
	this.literals.put(new ANTLRHashString("now", this), new Integer(24));
	this.literals.put(new ANTLRHashString("until", this), new Integer(44));
	this.literals.put(new ANTLRHashString("range", this), new Integer(31));
	this.literals.put(new ANTLRHashString("avg", this), new Integer(20));
	this.literals.put(new ANTLRHashString("in", this), new Integer(49));
	this.literals.put(new ANTLRHashString("slide", this), new Integer(32));
	this.literals.put(new ANTLRHashString("row", this), new Integer(46));
	this.literals.put(new ANTLRHashString("UNBOUNDED", this), new Integer(
		26));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_FUNCTION",
				this), new Integer(60));
	this.literals.put(new ANTLRHashString("max", this), new Integer(19));
	this.literals.put(new ANTLRHashString("from", this), new Integer(22));
	this.literals.put(new ANTLRHashString("istream", this), new Integer(7));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_QUERY",
				this), new Integer(62));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_AGGREGATE",
				this), new Integer(59));
	this.literals.put(new ANTLRHashString("new", this), new Integer(25));
	this.literals.put(new ANTLRHashString("at", this), new Integer(28));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SELECT_RENAME",
				this), new Integer(63));
	this.literals.put(new ANTLRHashString("up", this), new Integer(58));
	this.literals.put(new ANTLRHashString("minute", this), new Integer(38));
	this.literals.put(new ANTLRHashString("hours", this), new Integer(35));
	this.literals.put(new ANTLRHashString("rstream", this), new Integer(9));
	this.literals
		.put(
			new ANTLRHashString(
				"do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_TIME_NAME",
				this), new Integer(66));
	this.literals.put(new ANTLRHashString("materialise", this),
		new Integer(5));
	this.literals
		.put(new ANTLRHashString("groupby", this), new Integer(54));
	this.literals.put(new ANTLRHashString("by", this), new Integer(56));
	this.literals.put(new ANTLRHashString("as", this), new Integer(16));
    }

    public Token nextToken() throws TokenStreamException {
	tryAgain: for (;;) {
	    int _ttype = Token.INVALID_TYPE;
	    this.setCommitToPath(false);
	    this.resetText();
	    try { // for char stream error handling
		try { // for lexical error handling
		    switch (this.LA(1)) {
		    case '+':
		    case '-':
		    case '.':
		    case '0':
		    case '1':
		    case '2':
		    case '3':
		    case '4':
		    case '5':
		    case '6':
		    case '7':
		    case '8':
		    case '9': {
			this.mFloat(true);
			break;
		    }
		    case 'a':
		    case 'b':
		    case 'c':
		    case 'd':
		    case 'e':
		    case 'f':
		    case 'g':
		    case 'h':
		    case 'i':
		    case 'j':
		    case 'k':
		    case 'l':
		    case 'm':
		    case 'n':
		    case 'o':
		    case 'p':
		    case 'q':
		    case 'r':
		    case 's':
		    case 't':
		    case 'u':
		    case 'v':
		    case 'w':
		    case 'x':
		    case 'y':
		    case 'z': {
			this.mAttribute(true);
			break;
		    }
		    case '(': {
			this.mLeftParen(true);
			break;
		    }
		    case ')': {
			this.mRightParen(true);
			break;
		    }
		    case '[': {
			this.mLeftSquare(true);
			break;
		    }
		    case ']': {
			this.mRightSquare(true);
			break;
		    }
		    case '!':
		    case '<':
		    case '=':
		    case '>': {
			this.mPred(true);
			break;
		    }
		    case ';': {
			this.mSemi(true);
			break;
		    }
		    case '*': {
			this.mStar(true);
			break;
		    }
		    case ',': {
			this.mComma(true);
			break;
		    }
		    case '\'': {
			this.mQuotedString(true);
			break;
		    }
		    default: {
			if (this.LA(1) == EOF_CHAR) {
			    this.uponEOF();
			    this._returnToken = this.makeToken(Token.EOF_TYPE);
			} else {
			    this.consume();
			    continue tryAgain;
			}
		    }
		    }
		    if (this._returnToken == null) {
			continue tryAgain; // found SKIP token
		    }
		    _ttype = this._returnToken.getType();
		    _ttype = this.testLiteralsTable(_ttype);
		    this._returnToken.setType(_ttype);
		    return this._returnToken;
		} catch (final RecognitionException e) {
		    if (!this.getCommitToPath()) {
			this.consume();
			continue tryAgain;
		    }
		    throw new TokenStreamRecognitionException(e);
		}
	    } catch (final CharStreamException cse) {
		if (cse instanceof CharStreamIOException) {
		    throw new TokenStreamIOException(
			    ((CharStreamIOException) cse).io);
		} else {
		    throw new TokenStreamException(cse.getMessage());
		}
	    }
	}
    }

    public final void mFloat(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Float;
	boolean synPredMatched130 = false;
	if (((_tokenSet_0.member(this.LA(1))) && (_tokenSet_1
		.member(this.LA(2))))) {
	    final int _m130 = this.mark();
	    synPredMatched130 = true;
	    this.inputState.guessing++;
	    try {
		{
		    {
			switch (this.LA(1)) {
			case '+': {
			    this.match('+');
			    break;
			}
			case '-': {
			    this.match('-');
			    break;
			}
			case '.':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
			    break;
			}
			default: {
			    throw new NoViableAltForCharException(this.LA(1),
				    this.getFilename(), this.getLine(), this
					    .getColumn());
			}
			}
		    }
		    {
			_loop124: do {
			    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				this.matchRange('0', '9');
			    } else {
				break _loop124;
			    }

			} while (true);
		    }
		    this.match('.');
		    {
			int _cnt126 = 0;
			_loop126: do {
			    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				this.matchRange('0', '9');
			    } else {
				if (_cnt126 >= 1) {
				    break _loop126;
				} else {
				    throw new NoViableAltForCharException(this
					    .LA(1), this.getFilename(), this
					    .getLine(), this.getColumn());
				}
			    }

			    _cnt126++;
			} while (true);
		    }
		    this.match('e');
		    {
			switch (this.LA(1)) {
			case '+': {
			    this.match('+');
			    break;
			}
			case '-': {
			    this.match('-');
			    break;
			}
			default: {
			    throw new NoViableAltForCharException(this.LA(1),
				    this.getFilename(), this.getLine(), this
					    .getColumn());
			}
			}
		    }
		    {
			int _cnt129 = 0;
			_loop129: do {
			    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				this.matchRange('0', '9');
			    } else {
				if (_cnt129 >= 1) {
				    break _loop129;
				} else {
				    throw new NoViableAltForCharException(this
					    .LA(1), this.getFilename(), this
					    .getLine(), this.getColumn());
				}
			    }

			    _cnt129++;
			} while (true);
		    }
		}
	    } catch (final RecognitionException pe) {
		synPredMatched130 = false;
	    }
	    this.rewind(_m130);
	    this.inputState.guessing--;
	}
	if (synPredMatched130) {
	    {
		{
		    switch (this.LA(1)) {
		    case '+': {
			this.match('+');
			break;
		    }
		    case '-': {
			this.match('-');
			break;
		    }
		    case '.':
		    case '0':
		    case '1':
		    case '2':
		    case '3':
		    case '4':
		    case '5':
		    case '6':
		    case '7':
		    case '8':
		    case '9': {
			break;
		    }
		    default: {
			throw new NoViableAltForCharException(this.LA(1), this
				.getFilename(), this.getLine(), this
				.getColumn());
		    }
		    }
		}
		{
		    _loop134: do {
			if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
			    this.matchRange('0', '9');
			} else {
			    break _loop134;
			}

		    } while (true);
		}
		this.match('.');
		{
		    int _cnt136 = 0;
		    _loop136: do {
			if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
			    this.matchRange('0', '9');
			} else {
			    if (_cnt136 >= 1) {
				break _loop136;
			    } else {
				throw new NoViableAltForCharException(this
					.LA(1), this.getFilename(), this
					.getLine(), this.getColumn());
			    }
			}

			_cnt136++;
		    } while (true);
		}
		this.match('e');
		{
		    switch (this.LA(1)) {
		    case '+': {
			this.match('+');
			break;
		    }
		    case '-': {
			this.match('-');
			break;
		    }
		    default: {
			throw new NoViableAltForCharException(this.LA(1), this
				.getFilename(), this.getLine(), this
				.getColumn());
		    }
		    }
		}
		{
		    int _cnt139 = 0;
		    _loop139: do {
			if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
			    this.matchRange('0', '9');
			} else {
			    if (_cnt139 >= 1) {
				break _loop139;
			    } else {
				throw new NoViableAltForCharException(this
					.LA(1), this.getFilename(), this
					.getLine(), this.getColumn());
			    }
			}

			_cnt139++;
		    } while (true);
		}
	    }
	} else {
	    boolean synPredMatched146 = false;
	    if (((_tokenSet_0.member(this.LA(1))) && (_tokenSet_1.member(this
		    .LA(2))))) {
		final int _m146 = this.mark();
		synPredMatched146 = true;
		this.inputState.guessing++;
		try {
		    {
			{
			    switch (this.LA(1)) {
			    case '+': {
				this.match('+');
				break;
			    }
			    case '-': {
				this.match('-');
				break;
			    }
			    case '.':
			    case '0':
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
			    case '7':
			    case '8':
			    case '9': {
				break;
			    }
			    default: {
				throw new NoViableAltForCharException(this
					.LA(1), this.getFilename(), this
					.getLine(), this.getColumn());
			    }
			    }
			}
			{
			    _loop143: do {
				if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				    this.matchRange('0', '9');
				} else {
				    break _loop143;
				}

			    } while (true);
			}
			this.match('.');
			{
			    int _cnt145 = 0;
			    _loop145: do {
				if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				    this.matchRange('0', '9');
				} else {
				    if (_cnt145 >= 1) {
					break _loop145;
				    } else {
					throw new NoViableAltForCharException(
						this.LA(1), this.getFilename(),
						this.getLine(), this
							.getColumn());
				    }
				}

				_cnt145++;
			    } while (true);
			}
		    }
		} catch (final RecognitionException pe) {
		    synPredMatched146 = false;
		}
		this.rewind(_m146);
		this.inputState.guessing--;
	    }
	    if (synPredMatched146) {
		{
		    {
			switch (this.LA(1)) {
			case '+': {
			    this.match('+');
			    break;
			}
			case '-': {
			    this.match('-');
			    break;
			}
			case '.':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
			    break;
			}
			default: {
			    throw new NoViableAltForCharException(this.LA(1),
				    this.getFilename(), this.getLine(), this
					    .getColumn());
			}
			}
		    }
		    {
			_loop150: do {
			    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				this.matchRange('0', '9');
			    } else {
				break _loop150;
			    }

			} while (true);
		    }
		    this.match('.');
		    {
			int _cnt152 = 0;
			_loop152: do {
			    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				this.matchRange('0', '9');
			    } else {
				if (_cnt152 >= 1) {
				    break _loop152;
				} else {
				    throw new NoViableAltForCharException(this
					    .LA(1), this.getFilename(), this
					    .getLine(), this.getColumn());
				}
			    }

			    _cnt152++;
			} while (true);
		    }
		}
	    } else {
		boolean synPredMatched157 = false;
		if (((_tokenSet_2.member(this.LA(1))) && (true))) {
		    final int _m157 = this.mark();
		    synPredMatched157 = true;
		    this.inputState.guessing++;
		    try {
			{
			    {
				switch (this.LA(1)) {
				case '+': {
				    this.match('+');
				    break;
				}
				case '-': {
				    this.match('-');
				    break;
				}
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9': {
				    break;
				}
				default: {
				    throw new NoViableAltForCharException(this
					    .LA(1), this.getFilename(), this
					    .getLine(), this.getColumn());
				}
				}
			    }
			    {
				int _cnt156 = 0;
				_loop156: do {
				    if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
					this.matchRange('0', '9');
				    } else {
					if (_cnt156 >= 1) {
					    break _loop156;
					} else {
					    throw new NoViableAltForCharException(
						    this.LA(1), this
							    .getFilename(),
						    this.getLine(), this
							    .getColumn());
					}
				    }

				    _cnt156++;
				} while (true);
			    }
			}
		    } catch (final RecognitionException pe) {
			synPredMatched157 = false;
		    }
		    this.rewind(_m157);
		    this.inputState.guessing--;
		}
		if (synPredMatched157) {
		    {
			{
			    switch (this.LA(1)) {
			    case '+': {
				this.match('+');
				break;
			    }
			    case '-': {
				this.match('-');
				break;
			    }
			    case '0':
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
			    case '7':
			    case '8':
			    case '9': {
				break;
			    }
			    default: {
				throw new NoViableAltForCharException(this
					.LA(1), this.getFilename(), this
					.getLine(), this.getColumn());
			    }
			    }
			}
			{
			    int _cnt161 = 0;
			    _loop161: do {
				if ((((this.LA(1) >= '0') && (this.LA(1) <= '9')))) {
				    this.matchRange('0', '9');
				} else {
				    if (_cnt161 >= 1) {
					break _loop161;
				    } else {
					throw new NoViableAltForCharException(
						this.LA(1), this.getFilename(),
						this.getLine(), this
							.getColumn());
				    }
				}

				_cnt161++;
			    } while (true);
			}
		    }
		    if (this.inputState.guessing == 0) {
			_ttype = Int;
		    }
		} else {
		    boolean synPredMatched164 = false;
		    if (((this.LA(1) == '+') && (true))) {
			final int _m164 = this.mark();
			synPredMatched164 = true;
			this.inputState.guessing++;
			try {
			    {
				{
				    this.match('+');
				}
			    }
			} catch (final RecognitionException pe) {
			    synPredMatched164 = false;
			}
			this.rewind(_m164);
			this.inputState.guessing--;
		    }
		    if (synPredMatched164) {
			{
			    {
				this.match('+');
			    }
			}
			if (this.inputState.guessing == 0) {
			    _ttype = PLUS;
			}
		    } else {
			boolean synPredMatched169 = false;
			if (((this.LA(1) == '-') && (true))) {
			    final int _m169 = this.mark();
			    synPredMatched169 = true;
			    this.inputState.guessing++;
			    try {
				{
				    {
					this.match('-');
				    }
				}
			    } catch (final RecognitionException pe) {
				synPredMatched169 = false;
			    }
			    this.rewind(_m169);
			    this.inputState.guessing--;
			}
			if (synPredMatched169) {
			    {
				{
				    this.match('-');
				}
			    }
			    if (this.inputState.guessing == 0) {
				_ttype = Minus;
			    }
			} else {
			    throw new NoViableAltForCharException(this.LA(1),
				    this.getFilename(), this.getLine(), this
					    .getColumn());
			}
		    }
		}
	    }
	}
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mAttribute(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Attribute;
	boolean synPredMatched178 = false;
	if (((((this.LA(1) >= 'a') && (this.LA(1) <= 'z'))) && (_tokenSet_3
		.member(this.LA(2))))) {
	    final int _m178 = this.mark();
	    synPredMatched178 = true;
	    this.inputState.guessing++;
	    try {
		{
		    this.matchRange('a', 'z');
		    {
			_loop175: do {
			    switch (this.LA(1)) {
			    case 'a':
			    case 'b':
			    case 'c':
			    case 'd':
			    case 'e':
			    case 'f':
			    case 'g':
			    case 'h':
			    case 'i':
			    case 'j':
			    case 'k':
			    case 'l':
			    case 'm':
			    case 'n':
			    case 'o':
			    case 'p':
			    case 'q':
			    case 'r':
			    case 's':
			    case 't':
			    case 'u':
			    case 'v':
			    case 'w':
			    case 'x':
			    case 'y':
			    case 'z': {
				this.matchRange('a', 'z');
				break;
			    }
			    case '0':
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
			    case '7':
			    case '8':
			    case '9': {
				this.matchRange('0', '9');
				break;
			    }
			    case '_': {
				this.match('_');
				break;
			    }
			    default: {
				break _loop175;
			    }
			    }
			} while (true);
		    }
		    this.match('.');
		    this.matchRange('a', 'z');
		    {
			_loop177: do {
			    switch (this.LA(1)) {
			    case 'a':
			    case 'b':
			    case 'c':
			    case 'd':
			    case 'e':
			    case 'f':
			    case 'g':
			    case 'h':
			    case 'i':
			    case 'j':
			    case 'k':
			    case 'l':
			    case 'm':
			    case 'n':
			    case 'o':
			    case 'p':
			    case 'q':
			    case 'r':
			    case 's':
			    case 't':
			    case 'u':
			    case 'v':
			    case 'w':
			    case 'x':
			    case 'y':
			    case 'z': {
				this.matchRange('a', 'z');
				break;
			    }
			    case '0':
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
			    case '7':
			    case '8':
			    case '9': {
				this.matchRange('0', '9');
				break;
			    }
			    case '_': {
				this.match('_');
				break;
			    }
			    default: {
				break _loop177;
			    }
			    }
			} while (true);
		    }
		}
	    } catch (final RecognitionException pe) {
		synPredMatched178 = false;
	    }
	    this.rewind(_m178);
	    this.inputState.guessing--;
	}
	if (synPredMatched178) {
	    {
		this.matchRange('a', 'z');
		{
		    _loop181: do {
			switch (this.LA(1)) {
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z': {
			    this.matchRange('a', 'z');
			    break;
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
			    this.matchRange('0', '9');
			    break;
			}
			case '_': {
			    this.match('_');
			    break;
			}
			default: {
			    break _loop181;
			}
			}
		    } while (true);
		}
		this.match('.');
		this.matchRange('a', 'z');
		{
		    _loop183: do {
			switch (this.LA(1)) {
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z': {
			    this.matchRange('a', 'z');
			    break;
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
			    this.matchRange('0', '9');
			    break;
			}
			case '_': {
			    this.match('_');
			    break;
			}
			default: {
			    break _loop183;
			}
			}
		    } while (true);
		}
	    }
	} else if ((((this.LA(1) >= 'a') && (this.LA(1) <= 'z'))) && (true)) {
	    {
		this.matchRange('a', 'z');
		{
		    _loop186: do {
			switch (this.LA(1)) {
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z': {
			    this.matchRange('a', 'z');
			    break;
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
			    this.matchRange('0', '9');
			    break;
			}
			case '_': {
			    this.match('_');
			    break;
			}
			default: {
			    break _loop186;
			}
			}
		    } while (true);
		}
	    }
	    if (this.inputState.guessing == 0) {
		_ttype = Identifier;
	    }
	} else {
	    throw new NoViableAltForCharException(this.LA(1), this
		    .getFilename(), this.getLine(), this.getColumn());
	}

	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mLeftParen(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = LeftParen;
	this.match('(');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mRightParen(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = RightParen;
	this.match(')');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mLeftSquare(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = LeftSquare;
	this.match('[');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mRightSquare(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = RightSquare;
	this.match(']');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mPred(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Pred;
	switch (this.LA(1)) {
	case '=': {
	    this.match("=");
	    break;
	}
	case '!': {
	    this.match("!=");
	    break;
	}
	default:
	    if ((this.LA(1) == '<') && (this.LA(2) == '=')) {
		this.match("<=");
	    } else if ((this.LA(1) == '>') && (this.LA(2) == '=')) {
		this.match(">=");
	    } else if ((this.LA(1) == '<') && (true)) {
		this.match("<");
	    } else if ((this.LA(1) == '>') && (true)) {
		this.match(">");
	    } else {
		throw new NoViableAltForCharException(this.LA(1), this
			.getFilename(), this.getLine(), this.getColumn());
	    }
	}
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mSemi(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Semi;
	this.match(';');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mStar(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Star;
	this.match('*');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mComma(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = Comma;
	this.match(',');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    public final void mQuotedString(final boolean _createToken)
	    throws RecognitionException, CharStreamException,
	    TokenStreamException {
	int _ttype;
	Token _token = null;
	final int _begin = this.text.length();
	_ttype = QuotedString;
	this.match('\'');
	{
	    _loop197: do {
		if ((_tokenSet_4.member(this.LA(1)))) {
		    this.matchNot('\'');
		} else {
		    break _loop197;
		}

	    } while (true);
	}
	this.match('\'');
	if (_createToken && (_token == null) && (_ttype != Token.SKIP)) {
	    _token = this.makeToken(_ttype);
	    _token.setText(new String(this.text.getBuffer(), _begin, this.text
		    .length()
		    - _begin));
	}
	this._returnToken = _token;
    }

    private static final long[] mk_tokenSet_0() {
	final long[] data = { 288063250384289792L, 0L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

    private static final long[] mk_tokenSet_1() {
	final long[] data = { 288019269919178752L, 0L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

    private static final long[] mk_tokenSet_2() {
	final long[] data = { 287992881640112128L, 0L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

    private static final long[] mk_tokenSet_3() {
	final long[] data = { 288019269919178752L, 576460745860972544L, 0L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());

    private static final long[] mk_tokenSet_4() {
	final long[] data = { -549755813889L, -1L, 0L, 0L };
	return data;
    }

    public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

}
