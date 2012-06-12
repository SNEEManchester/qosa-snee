header
{
package uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking;

//This file was generated using ANTLR, (ANother Tool for Language Recognition)
//Antlr was obtained from www.antlr.org

}
//IDEA: replace semi and subquery with QUERY

//*******************
//** The Parser *****
//*******************

class SNEEqlParser extends Parser;

//k=3 required for (Attribute|Identifier) in the list item rule
//k=10 required for window
options { k=10; buildAST=true;}

//SNEEqlStatement could not be capitalized as that would make it a laexer token.
sneeqlStatement    : (query)(x:Semi^) {#x.setType(QUERY);};

query           : selectClause fromClause (whereClause)? (groupByClause)? (MATERIALISE)?;

selectClause    : (SELECT^ | 
					(SELECT! x1:ISTREAM^ ) {#x1.setType(SELECT);}| 
					(SELECT! x2:DSTREAM^) {#x2.setType(SELECT);}| 
					(SELECT! x3:RSTREAM^) {#x3.setType(SELECT);}) 
					selList ;

listItem        : (Attribute | literal | Identifier  | functionCall);
 
parameter       : Attribute | literal | Identifier;

parameterList   : ( parameter ) (Comma! parameterList)?;
 
functionCall    : x:Identifier^ {#x.setType(FUNCTION);} LeftParen! parameterList RightParen!;

selList         : Star |
					((aggregate | listItem ) (AS! x:Identifier {#x.setType(SELECT_RENAME);})? (Comma! selList)?);
//selList         : (aggregate | listItem ) (Comma! selList)?;

aggregate       : (x1:COUNT^ {#x1.setType(AGGREGATE);}
					|x2:SUM^ {#x2.setType(AGGREGATE);}
					|x3:MAX^ {#x3.setType(AGGREGATE);}
					|x4:AVG^ {#x4.setType(AGGREGATE);}
					|x5:MIN^ {#x5.setType(AGGREGATE);}
					) LeftParen! (listItem) RightParen!;

fromClause      : FROM^ fromList;

fromList        : fromItem (Comma! fromItem)*;

subQuery        : (x:LeftParen^ query RightParen!) {#x.setText("subQuery");};

fromItem        : x1:Identifier^ {#x1.setType(SOURCE);}
//				  | x2:subQuery {#x2.setType(QUERY);} ) 
					window 
//					(x3:Identifier {#x3.setType(SOURCE_RENAME);}   )?
				; 
		
window          :	(LeftSquare!  
				     (NOW 
				     | NEW 
				     | UNBOUNDED
					 | (fullFrom1 fullTo1 (slideWindowPart)?)
					 | (fullFrom1 fullTo2 (slideWindowPart)?)
					 | (fullFrom1 noUnitTo1 slideWindowPart)
					 | (fullFrom1 noUnitTo2 slideWindowPart)
					 | (fullFrom2 fullTo1 (slideWindowPart)?)
					 | (fullFrom2 fullTo2 (slideWindowPart)?)
					 | (fullFrom2 noUnitTo1 slideWindowPart)
					 | (fullFrom2 noUnitTo2 slideWindowPart)
					 | (fullFrom1 nowTo (slideWindowPart)?)
					 | (fullFrom2 nowTo (slideWindowPart)?)
					 | (noUnitFrom1 fullTo1 (slideWindowPart)?)
					 | (noUnitFrom1 fullTo2 (slideWindowPart)?)
					 | (noUnitFrom1 noUnitTo1 slideWindowPart)
					 | (noUnitFrom1 noUnitTo2 slideWindowPart)
					 | (noUnitFrom2 fullTo1 (slideWindowPart)?)
					 | (noUnitFrom2 fullTo2 (slideWindowPart)?)
					 | (noUnitFrom2 noUnitTo1 slideWindowPart)
					 | (noUnitFrom2 noUnitTo2 slideWindowPart)
					 | (noUnitFrom1 nowTo slideWindowPart)
					 | (noUnitFrom2 nowTo slideWindowPart)
					 | (fullRange (slideWindowPart)?)
					 | (noUnitRange slideWindowPart)
					 | (fullAt1 (slideWindowPart)?)
					 | (fullAt2 (slideWindowPart)?)
					 | (noUnitAt slideWindowPart)
					)RightSquare!)? ; 	
					
fullAt1          : 	AT^ NOW value unit;
fullAt2          : 	AT^ NOW Minus value unit;
noUnitAt        : 	AT^ value;
fullFrom1       : 	FROM^ NOW value unit;
fullFrom2       : 	FROM^ NOW Minus value unit;
noUnitFrom1     : 	FROM^ NOW value;
noUnitFrom2     : 	FROM^ NOW Minus value;
nowTo           : 	TO^ NOW;
fullTo1         : 	TO^ NOW value unit;
fullTo2         : 	TO^ NOW Minus value unit;
noUnitTo1       : 	TO^ NOW value;
noUnitTo2       : 	TO^ NOW Minus value;
fullRange       : 	RANGE^ value unit;
noUnitRange     : 	RANGE^ value;
slideWindowPart :   SLIDE^ value unit;

//window          :	(NOW | NEW | UNBOUNDED 
//					| LeftSquare! (NOW | NEW | UNBOUNDED) RightSquare! 
//					|(RANGE^  timeIndentifer (timeUntil)? (timeSlide)?)	
//					| LeftSquare! (RANGE^  timeIndentifer (timeUntil)? (timeSlide)?) RightSquare!
//					|((ROWS^|x:ROW^ {#x.setType(ROWS); #x.setText("rows");}) Int (rowUntil)? (timeSlide | rowSlide)?)
//					|LeftSquare! ((ROWS^|r2:ROW^ {#r2.setType(ROWS);}) Int (rowUntil)? (timeSlide | rowSlide)? RightSquare!)
//					)?; 	
					
//timeUntil       : (UNTIL^| x1:UP^ {#x1.setType(UNTIL);} TO!) timeIndentifer;

//timeSlide       : SLIDE^ timeIndentifer;

//minusFloat        : NegativeFloat
//				  | (x1:NegativeInt {#x1.setType(NegativeFloat);})
//                  | (Minus! x2:Int {#x2.setType(NegativeFloat); #x2.setText("-"+#x2.getText());})
//                  | (Minus! x3:Float {#x3.setType(NegativeFloat); #x3.setText("-"+#x3.getText());})
//                  ; 

//positiveFloat     : PositiveFloat
//				  | (x1:PositiveInt {#x1.setType(PositiveFloat);})
//				  | (x2:Float {#x2.setType(PositiveFloat);})
//				  | (x3:Int {#x3.setType(PositiveFloat);})
//                  ; 

value             : Float
					|x1:Int {#x1.setType(Float);}
//					|x2:PositiveFloat {#x2.setType(Float);}
//					|x3:PositiveInt {#x3.setType(Float);}
//					|x4:NegativeFloat {#x4.setType(Float);}
//					|x5:NegativeInt {#x4.setType(Float);}
					;

//fromUnit          : unit;
//toUnit            : unit;
//slideUnit         : unit;

unit              :	(x1:HOURS^ {#x1.setType(TIME_NAME);}
					| x2:HOUR^ {#x2.setType(TIME_NAME); #x2.setText("hours");}
					| x3:MINUTES^ {#x3.setType(TIME_NAME);}
					| x4:MINUTE^ {#x4.setType(TIME_NAME); #x4.setText("minutes");}
					| x41:MIN^ {#x41.setType(TIME_NAME); #x41.setText("minutes");}
					| x42:MINS^ {#x41.setType(TIME_NAME); #x41.setText("minutes");}				
					| x5:SECONDS^ {#x5.setType(TIME_NAME);}
					| x6:SECOND^ {#x6.setType(TIME_NAME);#x6.setText("seconds");}
					| x61:SEC^ {#x61.setType(TIME_NAME);#x61.setText("seconds");}
					| x62:SECS^ {#x62.setType(TIME_NAME);#x62.setText("seconds");}
					);

rowUntil        : UNTIL^ Int;

rowSlide        : SLIDE^ Int (ROWS!|x:ROW! {#x.setType(ROWS);});

whereClause     : WHERE^ conditions;

conditions      : condition (AND! condition)*;

condition       : (Attribute|Identifier) IN^ (LeftParen! query RightParen!) 
                | listItem Pred^ (listItem|(LeftParen! query RightParen!));

//literal         : x1:QuotedString {#x1.setType(LITERAL);}
//				| x2:Int {#x2.setType(LITERAL);}
//				| x3:Float {#x3.setType(LITERAL);}
//				| x4:TRUE {#x4.setType(LITERAL);}
//				| x5:FALSE {#x5.setType(LITERAL);} ;
				
literal         : QuotedString | Int | Float | TRUE | FALSE;

groupByClause   : (GROUPBY^ | x:GROUP^ {#x.setType(GROUPBY);} BY!)
				(Attribute|Identifier) (Comma! (Attribute|Identifier))*;
				
havingClause    : HAVING^ havingConditions;

havingConditions: havingCondition (AND! havingCondition)*;

havingCondition : (Attribute | literal | Identifier  | aggregate) 
					Pred^ (Attribute | literal | Identifier  | aggregate);
	
//*******************
//** The Lexer ******
//*******************

class SNEEqlLexer extends Lexer;

options { k=2; filter=true;
  caseSensitive=false;
  charVocabulary='\u0000'..'\u007F';
  caseSensitiveLiterals=false;
}

tokens { 
AS = "as";
AT = "at";
AVG = "avg";
AND = "and";
BY = "by";
COUNT = "count";
DSTREAM = "dstream";
FALSE = "false";
FROM  = "from";
GROUP = "group";
GROUPBY = "groupby";
HOUR = "hour";
HOURS = "hours";
IN = "in";
ISTREAM = "istream";
NEW = "new";
NOW = "now";
MATERIALISE = "materialise";
MAX = "max";
MIN = "min";
MINS = "mins";
MINUTES = "minutes";
MINUTE = "minute";
RANGE = "range";
ROW = "row";
ROWS = "rows";
RSTREAM = "rstream";
SECOND = "second";
SEC = "sec"; 
SECS = "secs"; 
SECONDS = "seconds";
SELECT  = "select";
SLIDE = "slide";
SUM = "sum";
TO = "to";
TRUE = "true";
WHERE = "where";
UNBOUNDED = "UNBOUNDED";
UNTIL = "until";
UP = "up";
//These are used to retype in the parser.
AGGREGATE = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_AGGREGATE";
FUNCTION = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_FUNCTION";
//LITERAL = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_LITERAL";
//POSITIVE_INT = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_POSITIVE_INT";
PLUS = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_PLUS";
QUERY = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_QUERY";
SELECT_RENAME = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SELECT_RENAME";
SOURCE = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE";
SOURCE_RENAME = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_SOURCE_RENAME";
TIME_NAME = "do_not_use_this_string_as_it_is_just_a_place_filler_to_create_a_rename_token_for_TIME_NAME";

}

Float           : ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
                  => ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
                | ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ )  
                  => ( ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ )
                | ( ('+'|'-')? ('0'..'9')+ )
                  => ( ('+'|'-')? ('0'..'9')+ ) { $setType(Int); }
                | ( ('+') )
                  => ( ('+') ) { $setType(PLUS); }
                | ( ('-') )
                  => ( ('-') ) { $setType(Minus);} 
                ;  

//Float   : ( ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
//                  => ( ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
//                | ( ('0'..'9')* '.' ('0'..'9')+ )  
//                  => ( ('0'..'9')* '.' ('0'..'9')+ )
//                | ( ('0'..'9')+ )
 //                 => ( ('0'..'9')+ ) { $setType(Int); };

//NegativeFloat   : ( ('-') ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
//                  => ( ('-') ('0'..'9')* '.' ('0'..'9')+ 'e' ('+'|'-') ('0'..'9')+ )
//                | ( ('-') ('0'..'9')* '.' ('0'..'9')+ )  
//                  => ( ('-') ('0'..'9')* '.' ('0'..'9')+ )
//                | ( ('-') ('0'..'9')+ )
//                  => ( ('-') ('0'..'9')+ ) { $setType(NegativeInt); }
//                | ( ('-') )
//                  => ( ('-') ) { $setType(Minus); 
//                };

Attribute       : ('a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')* 
                      '.' 'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')*)
                      => ('a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')*
                      '.' 'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')*)
                | ( 'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_')* )
                      { $setType(Identifier); };

LeftParen : '(';
RightParen : ')';
LeftSquare : '[';
RightSquare : ']';

Pred : "=" | "!=" | "<" | ">" | "<=" | ">=";

Semi : ';' ;

Star: '*';

Comma : ',' ;

QuotedString : '\'' ( ~'\'' )* '\'' ;




