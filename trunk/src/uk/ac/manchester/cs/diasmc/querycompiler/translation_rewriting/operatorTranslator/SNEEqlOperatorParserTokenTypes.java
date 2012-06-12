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


public interface SNEEqlOperatorParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int ONEOFFSCAN = 4;
	int RELPROJECT = 5;
	int RELCROSSPRODUCT = 6;
	int RELSELECT = 7;
	int RELAGGREGATION = 8;
	int RELRENAME = 9;
	int RELUNGROUP = 10;
	int RELGROUPBY = 11;
	int RELGRSELECT = 12;
	int STRSELECT = 13;
	int STRPROJECT = 14;
	int STRACQUIRE = 15;
	int STRRECEIVE = 16;
	int STRSCROSSPRODUCT = 17;
	int STRRENAME = 18;
	int DSTREAMOP = 19;
	int ISTREAMOP = 20;
	int RSTREAMOP = 21;
	int TIMEWINDOW = 22;
	int Int = 23;
	int ROWWINDOW = 24;
	int INPUTWINDOW = 25;
	int WINSELECT = 26;
	int WINPROJECT = 27;
	int REPEATEDSCAN = 28;
	int WINCROSSPRODUCT = 29;
	int WINRELCROSSPRODUCT = 30;
	int WINAGGREGATION = 31;
	int WINRENAME = 32;
	int WINUNGROUP = 33;
	int WINGROUPBY = 34;
	int WINGROUPSELECT = 35;
	int DELIVER = 36;
	int ERROR = 37;
	int Identifier = 38;
	int LeftParen = 39;
	int RightParen = 40;
	int LeftSquare = 41;
	int RightSquare = 42;
	int Comma = 43;
	int ATTRIBUTE = 44;
	int TUPLEATTRIBUTE = 45;
	int ADD = 46;
	int MINUS = 47;
	int MULTIPLY = 48;
	int DIVIDE = 49;
	int POWER = 50;
	int NEGATE = 51;
	int SQUAREROOT = 52;
	int INTLIT = 53;
	int FLOTLIT = 54;
	int Float = 55;
	int CONCAT = 56;
	int STRINGLIT = 57;
	int AVG = 58;
	int COLLECTION = 59;
	int COUNT = 60;
	int MAX = 61;
	int MIN = 62;
	int SUM = 63;
	int ATTRIBUTENAME = 64;
	int Quote = 65;
	int EXTENTNAME = 66;
	int LOCALNAME = 67;
	int EQUALS = 68;
	int NOTEQULAS = 69;
	int GREATERTHAN = 70;
	int LESSTHAN = 71;
	int GREATERTHANOREQUALS = 72;
	int LESSTHANOREQUALS = 73;
	int AND = 74;
	int OR = 75;
	int NOT = 76;
	int TRUE = 77;
	int FALSE = 78;
	int TIMESCOPEDEF = 79;
	int ROWSCOPEDEF = 80;
	int FLOATLIT = 81;
	int NOTEQUALS = 82;
	int STRRELCROSSPRODUCT = 83;
	int Dollar = 84;
}
