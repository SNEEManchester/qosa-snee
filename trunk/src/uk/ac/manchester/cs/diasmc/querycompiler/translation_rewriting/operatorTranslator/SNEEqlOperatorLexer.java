// $ANTLR 2.7.5 (20050128): "sneeql.g" -> "SNEEqlOperatorLexer.java"$

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


import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class SNEEqlOperatorLexer extends antlr.CharScanner implements SNEEqlOperatorParserTokenTypes, TokenStream
 {
public SNEEqlOperatorLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public SNEEqlOperatorLexer(Reader in) {
	this(new CharBuffer(in));
}
public SNEEqlOperatorLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public SNEEqlOperatorLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = false;
	setCaseSensitive(false);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("rowscopedef", this), new Integer(80));
	literals.put(new ANTLRHashString("relrename", this), new Integer(9));
	literals.put(new ANTLRHashString("relgroupby", this), new Integer(11));
	literals.put(new ANTLRHashString("extentname", this), new Integer(66));
	literals.put(new ANTLRHashString("notequals", this), new Integer(82));
	literals.put(new ANTLRHashString("strrelcrossproduct", this), new Integer(83));
	literals.put(new ANTLRHashString("timescopedef", this), new Integer(79));
	literals.put(new ANTLRHashString("greaterthan", this), new Integer(70));
	literals.put(new ANTLRHashString("concat", this), new Integer(56));
	literals.put(new ANTLRHashString("strselect", this), new Integer(13));
	literals.put(new ANTLRHashString("NOT", this), new Integer(76));
	literals.put(new ANTLRHashString("and", this), new Integer(74));
	literals.put(new ANTLRHashString("squareroot", this), new Integer(52));
	literals.put(new ANTLRHashString("wincrossproduct", this), new Integer(29));
	literals.put(new ANTLRHashString("lessthanorequals", this), new Integer(73));
	literals.put(new ANTLRHashString("count", this), new Integer(60));
	literals.put(new ANTLRHashString("multiply", this), new Integer(48));
	literals.put(new ANTLRHashString("relproject", this), new Integer(5));
	literals.put(new ANTLRHashString("divide", this), new Integer(49));
	literals.put(new ANTLRHashString("add", this), new Integer(46));
	literals.put(new ANTLRHashString("relselect", this), new Integer(7));
	literals.put(new ANTLRHashString("winungroup", this), new Integer(33));
	literals.put(new ANTLRHashString("floatlit", this), new Integer(81));
	literals.put(new ANTLRHashString("lessthan", this), new Integer(71));
	literals.put(new ANTLRHashString("winrename", this), new Integer(32));
	literals.put(new ANTLRHashString("rstreamop", this), new Integer(21));
	literals.put(new ANTLRHashString("negate", this), new Integer(51));
	literals.put(new ANTLRHashString("wingrselect", this), new Integer(35));
	literals.put(new ANTLRHashString("inputwindow", this), new Integer(25));
	literals.put(new ANTLRHashString("wingroupby", this), new Integer(34));
	literals.put(new ANTLRHashString("istreamop", this), new Integer(20));
	literals.put(new ANTLRHashString("or", this), new Integer(75));
	literals.put(new ANTLRHashString("strreceive", this), new Integer(16));
	literals.put(new ANTLRHashString("winrelcrossproduct", this), new Integer(30));
	literals.put(new ANTLRHashString("timewindow", this), new Integer(22));
	literals.put(new ANTLRHashString("attributename", this), new Integer(64));
	literals.put(new ANTLRHashString("stringlit", this), new Integer(57));
	literals.put(new ANTLRHashString("stracquire", this), new Integer(15));
	literals.put(new ANTLRHashString("min", this), new Integer(62));
	literals.put(new ANTLRHashString("minus", this), new Integer(47));
	literals.put(new ANTLRHashString("relaggregation", this), new Integer(8));
	literals.put(new ANTLRHashString("collection", this), new Integer(59));
	literals.put(new ANTLRHashString("winaggregation", this), new Integer(31));
	literals.put(new ANTLRHashString("repeatedscan", this), new Integer(28));
	literals.put(new ANTLRHashString("winselect", this), new Integer(26));
	literals.put(new ANTLRHashString("rowwindow", this), new Integer(24));
	literals.put(new ANTLRHashString("equals", this), new Integer(68));
	literals.put(new ANTLRHashString("false", this), new Integer(78));
	literals.put(new ANTLRHashString("winproject", this), new Integer(27));
	literals.put(new ANTLRHashString("strrename", this), new Integer(18));
	literals.put(new ANTLRHashString("max", this), new Integer(61));
	literals.put(new ANTLRHashString("power", this), new Integer(50));
	literals.put(new ANTLRHashString("attribute", this), new Integer(44));
	literals.put(new ANTLRHashString("sum", this), new Integer(63));
	literals.put(new ANTLRHashString("tupleattribute", this), new Integer(45));
	literals.put(new ANTLRHashString("strproject", this), new Integer(14));
	literals.put(new ANTLRHashString("relcrossproduct", this), new Integer(6));
	literals.put(new ANTLRHashString("avg", this), new Integer(58));
	literals.put(new ANTLRHashString("oneoffscan", this), new Integer(4));
	literals.put(new ANTLRHashString("true", this), new Integer(77));
	literals.put(new ANTLRHashString("relungroup", this), new Integer(10));
	literals.put(new ANTLRHashString("dstreamop", this), new Integer(19));
	literals.put(new ANTLRHashString("deliver", this), new Integer(36));
	literals.put(new ANTLRHashString("intlit", this), new Integer(53));
	literals.put(new ANTLRHashString("relgrselect", this), new Integer(12));
	literals.put(new ANTLRHashString("greaterthanorequals", this), new Integer(72));
	literals.put(new ANTLRHashString("error", this), new Integer(37));
	literals.put(new ANTLRHashString("LocalName", this), new Integer(67));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		setCommitToPath(false);
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '(':
				{
					mLeftParen(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRightParen(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLeftSquare(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRightSquare(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':
				{
					mQuote(true);
					theRetToken=_returnToken;
					break;
				}
				case '$':
				{
					mDollar(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mComma(true);
					theRetToken=_returnToken;
					break;
				}
				case '+':  case '-':  case '.':  case '0':
				case '1':  case '2':  case '3':  case '4':
				case '5':  case '6':  case '7':  case '8':
				case '9':
				{
					mFloat(true);
					theRetToken=_returnToken;
					break;
				}
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':  case 'g':  case 'h':
				case 'i':  case 'j':  case 'k':  case 'l':
				case 'm':  case 'n':  case 'o':  case 'p':
				case 'q':  case 'r':  case 's':  case 't':
				case 'u':  case 'v':  case 'w':  case 'x':
				case 'y':  case 'z':
				{
					mIdentifier(true);
					theRetToken=_returnToken;
					break;
				}
				default:
				{
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {consume(); continue tryAgain;}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				if ( !getCommitToPath() ) {consume(); continue tryAgain;}
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mLeftParen(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LeftParen;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRightParen(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RightParen;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLeftSquare(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LeftSquare;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRightSquare(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RightSquare;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQuote(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Quote;
		int _saveIndex;
		
		match('"');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDollar(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Dollar;
		int _saveIndex;
		
		match('$');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mComma(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Comma;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mFloat(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Float;
		int _saveIndex;
		
		boolean synPredMatched39 = false;
		if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))))) {
			int _m39 = mark();
			synPredMatched39 = true;
			inputState.guessing++;
			try {
				{
				{
				switch ( LA(1)) {
				case '+':
				{
					match('+');
					break;
				}
				case '-':
				{
					match('-');
					break;
				}
				case '.':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				_loop33:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						break _loop33;
					}
					
				} while (true);
				}
				match('.');
				{
				int _cnt35=0;
				_loop35:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt35>=1 ) { break _loop35; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt35++;
				} while (true);
				}
				match('e');
				{
				switch ( LA(1)) {
				case '+':
				{
					match('+');
					break;
				}
				case '-':
				{
					match('-');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt38=0;
				_loop38:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt38>=1 ) { break _loop38; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt38++;
				} while (true);
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched39 = false;
			}
			rewind(_m39);
			inputState.guessing--;
		}
		if ( synPredMatched39 ) {
			{
			{
			switch ( LA(1)) {
			case '+':
			{
				match('+');
				break;
			}
			case '-':
			{
				match('-');
				break;
			}
			case '.':  case '0':  case '1':  case '2':
			case '3':  case '4':  case '5':  case '6':
			case '7':  case '8':  case '9':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			_loop43:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					break _loop43;
				}
				
			} while (true);
			}
			match('.');
			{
			int _cnt45=0;
			_loop45:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt45>=1 ) { break _loop45; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt45++;
			} while (true);
			}
			match('e');
			{
			switch ( LA(1)) {
			case '+':
			{
				match('+');
				break;
			}
			case '-':
			{
				match('-');
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			int _cnt48=0;
			_loop48:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt48>=1 ) { break _loop48; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt48++;
			} while (true);
			}
			}
		}
		else {
			boolean synPredMatched55 = false;
			if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))))) {
				int _m55 = mark();
				synPredMatched55 = true;
				inputState.guessing++;
				try {
					{
					{
					switch ( LA(1)) {
					case '+':
					{
						match('+');
						break;
					}
					case '-':
					{
						match('-');
						break;
					}
					case '.':  case '0':  case '1':  case '2':
					case '3':  case '4':  case '5':  case '6':
					case '7':  case '8':  case '9':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					{
					_loop52:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							break _loop52;
						}
						
					} while (true);
					}
					match('.');
					{
					int _cnt54=0;
					_loop54:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							if ( _cnt54>=1 ) { break _loop54; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt54++;
					} while (true);
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched55 = false;
				}
				rewind(_m55);
				inputState.guessing--;
			}
			if ( synPredMatched55 ) {
				{
				{
				switch ( LA(1)) {
				case '+':
				{
					match('+');
					break;
				}
				case '-':
				{
					match('-');
					break;
				}
				case '.':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				_loop59:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						break _loop59;
					}
					
				} while (true);
				}
				match('.');
				{
				int _cnt61=0;
				_loop61:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt61>=1 ) { break _loop61; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt61++;
				} while (true);
				}
				}
			}
			else {
				boolean synPredMatched66 = false;
				if (((_tokenSet_2.member(LA(1))) && (true))) {
					int _m66 = mark();
					synPredMatched66 = true;
					inputState.guessing++;
					try {
						{
						{
						switch ( LA(1)) {
						case '+':
						{
							match('+');
							break;
						}
						case '-':
						{
							match('-');
							break;
						}
						case '0':  case '1':  case '2':  case '3':
						case '4':  case '5':  case '6':  case '7':
						case '8':  case '9':
						{
							break;
						}
						default:
						{
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						}
						}
						{
						int _cnt65=0;
						_loop65:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9'))) {
								matchRange('0','9');
							}
							else {
								if ( _cnt65>=1 ) { break _loop65; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
							}
							
							_cnt65++;
						} while (true);
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched66 = false;
					}
					rewind(_m66);
					inputState.guessing--;
				}
				if ( synPredMatched66 ) {
					{
					{
					switch ( LA(1)) {
					case '+':
					{
						match('+');
						break;
					}
					case '-':
					{
						match('-');
						break;
					}
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					{
					int _cnt70=0;
					_loop70:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							if ( _cnt70>=1 ) { break _loop70; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt70++;
					} while (true);
					}
					}
					if ( inputState.guessing==0 ) {
						_ttype = Int;
					}
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}}
				if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
					_token = makeToken(_ttype);
					_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
				}
				_returnToken = _token;
			}
			
	public final void mIdentifier(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Identifier;
		int _saveIndex;
		
		matchRange('a','z');
		{
		_loop73:
		do {
			switch ( LA(1)) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				matchRange('a','z');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			default:
			{
				break _loop73;
			}
			}
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 288063250384289792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 288019269919178752L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 287992881640112128L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
