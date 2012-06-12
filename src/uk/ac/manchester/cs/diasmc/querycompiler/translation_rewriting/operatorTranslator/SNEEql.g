header
{
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

}
//*******************
//** The Parser *****
//*******************

class SNEEqlOperatorParser extends Parser;

//k=2
options { k=2; buildAST=true;}

//operator could not be capitalized as that would make it a lexer token.
operator 
	// relations 
   : ONEOFFSCAN^ extentName localName attributeNames
   | RELPROJECT^ expressions attributeNames childOperator
   | RELCROSSPRODUCT^ childOperator childOperator
   | RELSELECT^ booleanExpression childOperator
   | RELAGGREGATION^ expressions attributeNames childOperator
   | RELRENAME^ localName childOperator
   | RELUNGROUP^ expressions attributeNames childOperator
    // Relational Grouped Operators 
   | RELGROUPBY^ expressions childOperator
   | RELGRSELECT^ booleanExpression childOperator
    // Tuple Stream Operators 
   | STRSELECT^ booleanExpression childOperator
   | STRPROJECT^ expressions attributeNames childOperator
   | STRACQUIRE^ extentName localName attributeNames
   | STRRECEIVE^ extentName localName attributeNames
   | STRSCROSSPRODUCT^ childOperator childOperator
   | STRRENAME^ localName childOperator
   | DSTREAMOP^ childOperator
   | ISTREAMOP^ childOperator
   | RSTREAMOP^ childOperator
     // WindowOperator 
   | TIMEWINDOW^ windowScopeDef Int childOperator
   | ROWWINDOW^ windowScopeDef Int childOperator
   | INPUTWINDOW^  windowScopeDef childOperator
   | WINSELECT^ booleanExpression childOperator
   | WINPROJECT^ expressions attributeNames childOperator
   | REPEATEDSCAN^ Int extentName localName attributeNames
   | WINCROSSPRODUCT^ childOperator childOperator
   | WINRELCROSSPRODUCT^ childOperator childOperator
   | WINAGGREGATION^ expressions attributeNames childOperator
   | WINRENAME^ localName childOperator
   | WINUNGROUP^ expressions attributeNames childOperator
     // Window Grouped Operators 
   | WINGROUPBY^ expressions childOperator
   | WINGROUPSELECT^ booleanExpression childOperator
     // Final 
   | DELIVER^ childOperator
   | ERROR^ (Identifier| ERROR)*;
      
childOperator 
   : LeftParen! operator RightParen!;
      
expressions 
   : LeftSquare^ expressionList RightSquare!;
expressionList
   : expression (Comma! expression)*;
     
expression
   : ATTRIBUTE^ localName LeftParen! attributeName RightParen!
   | TUPLEATTRIBUTE^ localName LeftParen! attributeName RightParen!
     // Artithmatic expressions 
   | ADD^       expression expression
   | MINUS^     expression expression
   | MULTIPLY^  expression expression
   | DIVIDE^    expression expression
   | POWER^     expression expression
   | NEGATE^    expression
   | SQUAREROOT^ expression
   | INTLIT^    Int
   | FLOTLIT^   Float
    // String Expressions 
   | CONCAT^    expression expression
   | STRINGLIT  Identifier
    // Aggregation expressions -}
   | AVG^ LeftParen! COLLECTION! expression RightParen!
   | COUNT^ LeftParen! COLLECTION! expression RightParen!
   | MAX^ LeftParen! COLLECTION! expression RightParen!
   | MIN^ LeftParen! COLLECTION! expression RightParen!
   | SUM^ LeftParen! COLLECTION! expression RightParen!
   | LeftParen! expression RightParen!;

attributeNames
   : LeftSquare^ attributeNameList RightSquare!;
   
attributeNameList 
   : attributeName (Comma! attributeName)*;
 
attributeName
   : ATTRIBUTENAME! Quote! Identifier Quote!;

extentName
   :  LeftParen! EXTENTNAME! Quote! Identifier Quote! RightParen!;    

localName 
   :  LeftParen! LOCALNAME! Quote! Identifier Quote! RightParen!;    

booleanExpression
   : EQUALS^              expression expression
   | NOTEQULAS^           expression expression 
   | GREATERTHAN^         expression expression
   | LESSTHAN^            expression expression
   | GREATERTHANOREQUALS^ expression expression
   | LESSTHANOREQUALS^    expression expression
   | AND^                 booleanList
   | OR^                  booleanList
   | NOT^                 booleanExpression
   | TRUE
   | FALSE
   | LeftParen! booleanExpression RightParen!;
   
booleanList 
   : LeftSquare! booleanExpression (Comma! booleanExpression)* RightSquare!;
   
windowScopeDef 
   : LeftParen! TIMESCOPEDEF^ Int Int RightParen!
   | LeftParen! ROWSCOPEDEF^ Int Int RightParen!;
  
//*******************
//** The Lexer ******
//*******************

class SNEEqlOperatorLexer extends Lexer;

options { k=2; filter=true;
  caseSensitive=false;
  charVocabulary='\u0000'..'\u007F';
  caseSensitiveLiterals=false;
}

tokens { 
ADD = "add";
AND = "and";
ATTRIBUTE = "attribute";
ATTRIBUTENAME = "attributename";
AVG = "avg";
COLLECTION = "collection";
CONCAT = "concat";
COUNT = "count";
DELIVER = "deliver";
DIVIDE = "divide";
DSTREAMOP = "dstreamop";
EQUALS = "equals";
ERROR = "error";
EXTENTNAME = "extentname";
FALSE = "false";
FLOATLIT = "floatlit";
GREATERTHAN = "greaterthan";
GREATERTHANOREQUALS = "greaterthanorequals";
INPUTWINDOW = "inputwindow";
INTLIT = "intlit";
ISTREAMOP = "istreamop";
LESSTHAN = "lessthan";
LESSTHANOREQUALS = "lessthanorequals";
LOCALNAME = "LocalName";
MAX = "max";
MIN = "min";
MINUS = "minus";
MULTIPLY = "multiply";
NEGATE = "negate";
NOT = "NOT";
NOTEQUALS = "notequals";
ONEOFFSCAN = "oneoffscan";
OR = "or";
POWER = "power";
RELAGGREGATION = "relaggregation";
RELCROSSPRODUCT = "relcrossproduct";
RELGROUPBY = "relgroupby";
RELGRSELECT = "relgrselect";
RELPROJECT = "relproject";
RELRENAME = "relrename";
RELSELECT = "relselect";
RELUNGROUP = "relungroup";
REPEATEDSCAN = "repeatedscan";
ROWSCOPEDEF = "rowscopedef";
ROWWINDOW = "rowwindow";
RSTREAMOP = "rstreamop";
SQUAREROOT = "squareroot";
STRACQUIRE = "stracquire";
STRINGLIT = "stringlit";
STRPROJECT = "strproject";
STRRECEIVE = "strreceive";
STRRENAME = "strrename";
STRRELCROSSPRODUCT = "strrelcrossproduct";
STRSELECT = "strselect";
SUM = "sum";
TIMESCOPEDEF = "timescopedef";
TIMEWINDOW = "timewindow";
TRUE = "true";
TUPLEATTRIBUTE = "tupleattribute";
WINAGGREGATION = "winaggregation";
WINGROUPSELECT = "wingrselect";
WINCROSSPRODUCT = "wincrossproduct";
WINGROUPBY = "wingroupby";
WINPROJECT = "winproject";
WINRELCROSSPRODUCT ="winrelcrossproduct";
WINRENAME = "winrename";
WINSELECT = "winselect";
WINUNGROUP = "winungroup";
}

LeftParen : '(';
RightParen : ')';
LeftSquare : '[';
RightSquare : ']';
Quote : '"';
Dollar : '$';
Comma : ',' ;

Float           : ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
                  => ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
                | ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ )  
                  => ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ )
                | ( ('+'|'-')? ('0'..'9')+ )
                  => ( ('+'|'-')? ('0'..'9')+ ) { $setType(Int); };

Identifier      : 'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')*; 