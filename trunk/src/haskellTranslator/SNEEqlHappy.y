{
module SNEEqlHappy 
where

import Char
import SNEEqlLexer
import SNEEqlAst
}

%name parse fullQuery
%name testquery query
%name testinnerQuery innerQuery 
%name testdstream dstream
%name testistream istream
%name testrstream rstream 
%name testfromClause fromClause
%name testwhereClause whereClause
%name testprojectList projectList
%name testaggregationList aggregationList
%name testmixedList mixedList
%name testwithAggregationList withAggregationList
%name testeitherList  eitherList  
%name testprojection  projection  
%name testaggregation aggregation 
%name testmixed  mixed  
%name testliteral literal
%name testprojectExpr projectExpr 
%name testaggExpression aggExpression
%name testmixedExpression  mixedExpression  
%name testaggFunction aggFunction
%name testattributeName attributeName 
%name testprojectName projectName 
%name testaggregationName aggregationName
%name testextentList extentList 
%name testextentItem  extentItem  
%name testextentName extentName
%name testlocalName localName
%name testsubqueryName  subqueryName  
%name testwindowDef windowDef 
%name testwindowScopeDef windowScopeDef 
%name testtimeFrom timeFrom 
%name testtimeTo timeTo 
-- %name testtoNow toNow 
%name testrowFrom rowFrom
%name testrowTo rowTo
%name testtimeRange timeRange 
%name testtimeAt timeAt
%name testrowRange rowRange
%name testrowAt rowAt
%name testrowValue rowValue 
%name testtimeValue  timeValue  
%name testminutes minutes
%name testnowMinus nowMinus
%name testtimeSlide timeSlide
%name testrowSlide rowSlide
%name testslideValue slideValue
%name testpredicate predicate  
%name testandList andList
%name testorList orList
%name testsinglePredicate singlePredicate
%name testpredicatePart predicatePart
%name testgroupByClause groupByClause 
%name testgroupByAttributes groupByAttributes
%name testgroupByAttribute groupByAttribute 
%name testhavingClause havingClause
%name testhavingPredicates  havingPredicates  
%name testhavingAndList havingAndList
%name testhavingOrList havingOrList
%name testhavingPredicate havingPredicate 
%name testhavingExpression havingExpression 

%tokentype                             { Token }
%error                                 { parseError }

%token 
  int                                  { IntWrapper $$ }
  float                                { FloatWrapper $$ }
  string                               { StringWrapper $$ }
  var                                  { VarWrapper $$ }
  "and"                                { TokenAnd }
  "as"                                 { TokenAs }	
  "at"                                 { TokenAt }
  "avgerage"                           { TokenAverage }
  "by"                                 { TokenBy }
  "count"                              { TokenCount }
  "days"                               { TokenDays }
  "dstream"                            { TokenDStream }
  "from"                               { TokenFrom }
  "group"                              { TokenGroup }
  "having"                             { TokenHaving }
  "hours"                              { TokenHours }	
  "istream"                            { TokenIStream }
  "maximum"                            { TokenMaximum } 
  "millisecs"                          { TokenMilliSecs }
  "min"                                { TokenMin } 
  "minimum"                            { TokenMinimum }
  "minutes"                            { TokenMinutes }
  "not"                                { TokenNot }	
  "now"                                { TokenNow }	
  "or"                                 { TokenOr }
  "range"                              { TokenRange }
  "rows"                               { TokenRows }
  "rstream"                            { TokenRStream }
  "seconds"                            { TokenSeconds }
  "select"                             { TokenSelect }
  "slide"                              { TokenSlide }
  "square"                             { TokenSquare }
  "squareRoot"                         { TokenSquareRoot }
  "sum"                                { TokenSum }
  "to"                                 { TokenTo }
  "where"                              { TokenWhere }
  "="                                  { TokenEquals }
  "+"                                  { TokenPlus }
  "^"                                  { TokenPower }
  "-"                                  { TokenMinus }
  "*"                                  { TokenStar }
  "/"                                  { TokenDiv }
  "<"                                  { TokenLessThan }
  ">"                                  { TokenGreaterThan }
  "("                                  { TokenOpenBracket }
  ")"                                  { TokenCloseBracket }
  "["                                  { TokenOpenSquare }
  "]"                                  { TokenCloseSquare }
  ","                                  { TokenComma }
  "."                                  { TokenDot }
  ";"                                  { TokenSemi }

{- These commands cause priority in reverse order listed here.-}
%nonassoc ">" "<"
%left "+" "-"
%left "*" "/"
%left "^"
%left "="
%left NEG

%%

fullQuery : query ";"                  { FullQuery $1 }

query
  : "select" innerQuery                { $2 }
  | dstream innerQuery                 { DStream $2 } 	
  | istream innerQuery                 { IStream $2 } 	
  | rstream innerQuery                 { RStream $2 } 	
        
innerQuery 
  : projectList fromClause whereClause       
        { SimpleQuery $1 $2 $3 (ExtentName "") } 
  | aggregationList fromClause whereClause 
        { AggregationQuery $1 $2 $3 (ExtentName "") } 
  | mixedList fromClause whereClause groupByClause havingClause
        { GroupByQuery $1 $2 $3 $4 $5 (ExtentName "") } 
    {- Syntactic Sugar / Group by queries need not included aggregation -}
  | projectList fromClause whereClause groupByClause havingClause
        { GroupByQuery $1 $2 $3 $4 $5 (ExtentName "") } 
    {- Syntactic Sugar / Group by queries need not included none aggregate attributes -}
  | aggregationList fromClause whereClause groupByClause havingClause
        { GroupByQuery $1 $2 $3 $4 $5 (ExtentName "") } 

dstream
  : "dstream" "select"                 { }
    {- Syntactic Sugar / To accept CQL queries -}
  | "select" "dstream"                 { }
  
istream
  : "istream" "select"                 { }
    {- Syntactic Sugar / To accept CQL queries-}
  | "select" "istream"                 { }
  
rstream
  : "rstream" "select"                 { }
    {- Syntactic Sugar / To accept CQL queries-}
  | "select" "rstream"                 { }

fromClause : "from" extentList         { ExtentList $2 }

whereClause
  : {- Empty -}                        { TRUE }
  | "where" predicate                  { $2 }
             
{- Identify list off just Projections -}
projectList
  : projection                         { [$1] } 
  | projection "," projectList         { $1 : $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" projection "," projectList ")" { $2 : $4 } 
  
{- Identify list off just Aggregations -}
aggregationList
  : aggregation                        { [$1] }
  | aggregation "," aggregationList    { $1 : $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggregation "," aggregationList ")" 
        { $2 : $4 } 
        
{- Identify list with both Projection and Aggregations -}        
mixedList
  : mixed                              { [$1] }
  | aggregation "," withProjectionList { $1: $3 }
  | projection "," withAggregationList { $1: $3 }
  | mixed "," eitherList               { $1: $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggregation "," withProjectionList ")" 
        { $2: $4 }
  | "(" projection "," withAggregationList ")" 
        { $2: $4 }
  | "(" mixed "," eitherList ")"       { $2: $4 }

{- Identify list with at least Projection -}
withProjectionList
  : projection                         { [$1] }
  | mixed                              { [$1] }
  | aggregation "," withProjectionList { $1: $3 }
  | projection "," eitherList          { $1: $3 }
  | mixed "," eitherList               { $1: $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggregation "," withProjectionList 
        { $2: $4 }
  | "(" projection "," eitherList ")"  { $2: $4 }
  | "(" mixed "," eitherList ")"       { $2: $4 }

{- Identify list with at least Aggregations -}
withAggregationList
  : aggregation                        { [$1] }
  | mixed                              { [$1] }
  | aggregation "," eitherList         { $1: $3 }
  | projection "," withAggregationList { $1: $3 }
  | mixed "," eitherList               { $1: $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggregation "," eitherList ")" { $2: $4 }
  | "(" projection "," withAggregationList ")" 
        { $2: $4 }
  | "(" mixed "," eitherList ")"       { $2: $4 }

{- List where type no longer matters -}
{- Required to avoid ambiguity because mixed type must 
   be in both withProjectionList and withAggregationList -}
eitherList  
  : aggregation                        { [$1] }
  | projection                         { [$1] }
  | mixed                              { [$1] }
  | aggregation "," eitherList         { $1: $3 }
  | projection "," eitherList          { $1: $3 }
  | mixed "," eitherList               { $1: $3 }
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggregation "," eitherList ")" { $2: $4 }
  | "(" projection "," eitherList ")"  { $2: $4 }
  | "(" mixed "," eitherList ")"       { $2: $4 }
      
projection  
  : projectExpr "as" projectName       { NamedExpression $1 $3 } 
    {- Syntactic Sugar / use default name -}
  | projectExpr
       { NamedExpression $1 ( toName $1)}
    {- Syntactic Sugar / Allow Star -}
  | "*"                                { Star }     
    {- Syntactic Sugar / Optional brackets -}
  | "(" projectExpr "as" projectName ")"
       { NamedExpression $2 $4 } 
    {- Backets around just pojectExpr handled there -}

aggregation 
  : aggExpression "as" aggregationName { NamedExpression $1 $3 } 
    {- Syntactic Sugar / use default name -}
  | aggExpression          
        { NamedExpression $1 (toName $1)}
    {- Syntactic Sugar / Optional brackets -}
  | "(" aggExpression "as" aggregationName ")"
        { NamedExpression $2 $4 } 
    {- Backets around just aggExpression handled there -}

mixed  
  : mixedExpression "as" projectName   { NamedExpression $1 $3 } 
  | mixedExpression                    { NamedExpression $1 ( toName $1)}

literal
  : int                                { IntLit $1 } 
  | float                              { FloatLit $1 }
  | string                             { StringLit $1 }
  {- literal operator literal is still a literal -}
  | literal    "^" literal             { Power $1 $3 }
  | literal    "*" literal             { Multiply $1 $3 }
  | literal    "/" literal             { Divide $1 $3 }
  | literal    "+" literal             { Add $1 $3 }
  | literal    "-" literal             { Minus $1 $3 }
  | literal    "+" "+" literal         { Concat $1 $4 }
  | "square" "(" literal ")"           { Multiply $3 $3 }
  | "squareRoot" "(" literal ")"       { SquareRoot $3 }
 {- Allow for signed literals -}
  | "-" literal %prec NEG              { Negate $2 }
  {- Allow Brackets -}
  | "(" literal ")"                    { $2 } 
    
projectExpr 
  : localName  "." attributeName       { Attribute (LocalName $1) (AttributeName $3) }  
  | "square" "(" projectExpr ")"       { Multiply $3 $3 }
  | "squareRoot" "(" projectExpr ")"   { SquareRoot $3 }
  | attributeName 
        { UnqualifiedAttribute (AttributeName $1) }
  {- All math involving only projectExpr and literals -}
  | projectExpr "^" projectExpr        { Multiply $1 $3 }
  | projectExpr "*" projectExpr        { Multiply $1 $3 }
  | projectExpr "/" projectExpr        { Divide $1 $3 }
  | projectExpr "+" projectExpr        { Add $1 $3 }
  | projectExpr "-" projectExpr        { Minus $1 $3 }
  | projectExpr "+" "+" projectExpr    { Concat $1 $4 }
  | literal     "^" projectExpr        { Power $1 $3 }
  | literal     "*" projectExpr        { Multiply $1 $3 }
  | literal     "/" projectExpr        { Divide $1 $3 }
  | literal     "+" projectExpr        { Add $1 $3 }
  | literal     "-" projectExpr        { Minus $1 $3 }
  | literal     "+" "+" projectExpr    { Concat $1 $4 }
  | projectExpr "^" literal            { Power $1 $3 }
  | projectExpr "*" literal            { Multiply $1 $3 }
  | projectExpr "/" literal            { Divide $1 $3 }
  | projectExpr "+" literal            { Add $1 $3 }
  | projectExpr "-" literal            { Minus $1 $3 }
  | projectExpr "+" "+" literal        { Concat $1 $4 }
  {- Allow for signed expressions -}
  | "-" projectExpr %prec NEG          { Negate $2 } 
  {- Allow Brackets -}
  | "(" projectExpr ")"                { $2 } 

aggExpression
  : aggFunction                        { $1 }
  | "square" "(" aggExpression ")"     { Multiply $3 $3 }
  | "squareRoot" "(" aggExpression ")" { SquareRoot $3 }
  {- All math involving only aggExpression and literals -}
  | aggExpression "^" aggExpression    { Power $1 $3 }
  | aggExpression "*" aggExpression    { Multiply $1 $3 }
  | aggExpression "/" aggExpression    { Divide $1 $3 }
  | aggExpression "+" aggExpression    { Add $1 $3 }
  | aggExpression "-" aggExpression    { Minus $1 $3 }
  | literal       "^" aggExpression    { Power $1 $3 }
  | literal       "*" aggExpression    { Multiply $1 $3 }
  | literal       "/" aggExpression    { Divide $1 $3 }
  | literal       "+" aggExpression    { Add $1 $3 }
  | literal       "-" aggExpression    { Minus $1 $3 }
  | aggExpression "^" literal          { Power $1 $3 }
  | aggExpression "*" literal          { Multiply $1 $3 }
  | aggExpression "/" literal          { Divide $1 $3 }
  | aggExpression "+" literal          { Add $1 $3 }
  | aggExpression "-" literal          { Minus $1 $3 }
  {- There is no String Aggregation so no Concatenation -}
  {- Allow for signed expressions -}
  | "-" aggExpression %prec NEG        { Negate $2 } 
  {- Allow Brackets -}
  | "(" aggExpression ")"              { $2 }  
  
mixedExpression  
  : "square" "(" mixedExpression ")"   { Multiply $3 $3 }
  | "squareRoot" "(" mixedExpression ")"
        { SquareRoot $3 }
  {- Combine a projectExpr with an aggExpressions -}
  | projectExpr   "^" aggExpression    { Power $1 $3 }
  | projectExpr   "*" aggExpression    { Multiply $1 $3 }
  | projectExpr   "/" aggExpression    { Divide $1 $3 }
  | projectExpr   "+" aggExpression    { Add $1 $3 }
  | projectExpr   "-" aggExpression    { Minus $1 $3 }
  {- Combine an aggExpression with a projectExpression -}
  | aggExpression "^" projectExpr      { Power $1 $3 }
  | aggExpression "*" projectExpr      { Multiply $1 $3 }
  | aggExpression "/" projectExpr      { Divide $1 $3 }
  | aggExpression "+" projectExpr      { Add $1 $3 }
  | aggExpression "-" projectExpr      { Minus $1 $3 }
  {- There is no String Aggregation so no Concatenation -}
  {- Allow for signed expressions -}
  | "-" mixedExpression %prec NEG      { Negate $2 } 
  {- Allow Brackets -}
  | "(" mixedExpression ")"            { $2 }

aggFunction
  : "avgerage" "(" projectExpr ")"     { Avg (Collection $3 )}
  | "count"    "(" projectExpr ")"     { Count(Collection $3)}
  | "maximum"  "(" projectExpr ")"     { Max (Collection $3 )}
  | "minimum"  "(" projectExpr ")"     { Min (Collection $3 )}
  | "sum"      "(" projectExpr ")"     { Sum (Collection $3 )}
  {- Handle abreviation Lexer can not (due to minutes) -}
  | "min"      "(" projectExpr ")"     { Min (Collection $3 )}
  {- Signed expressions and brackets handled by aggExpression -} 

attributeName : var                    { $1 }

projectName : var                      { AttributeName $1 }
        
aggregationName : var                  { AttributeName $1 }

extentList
  : extentItem                         { [$1] } 
  | extentItem "," extentList          { $1 : $3 }
           
extentItem  
  : extentName localName               
      { (Extent (ExtentName $1) (LocalName $2)) }
  | extentName windowDef localName
      { WindowedExtent (Extent (ExtentName $1) (LocalName $3)) $2 } 
    {- Sub query with no window -}
  | "(" query ")"  subqueryName        
      { Subquery (setName $2 $4)(ExtentName $4)(LocalName $4)}                   
    {-  Sub Query with windows -}
  | "(" query ")"  windowDef subqueryName 
      { WindowedExtent (Subquery (setName $2 $5)(ExtentName $5)(LocalName $5)) $4} 
  {- Syntactic Sugar / use extent name as local name -}
  | extentName                         
      { Extent (ExtentName $1) (LocalName $1) }
  | extentName windowDef 
      { WindowedExtent (Extent (ExtentName $1) (LocalName $1)) $2 } 

extentName : var                       { $1 }

localName : var                        { $1 }

subqueryName  
  : var                                { $1 }
  {- Syntactic Sugar / No name provided use "SubQuery" -}
  |                                    { "SubQuery" }

windowDef
  : "[" windowScopeDef timeSlide "]"   { TimeWindowDef $2 $3 }
  | "[" windowScopeDef rowSlide "]"    { RowWindowDef $2 $3 }
  | "[" windowScopeDef "]"             { InputWindowDef $2 }
  {- Syntactic Sugar -}
  | "[" "now" "]"
        { InputWindowDef (TimeScopeDef 0 0) }

windowScopeDef
  : timeFrom timeTo                    { TimeScopeDef $1 $2}
  | rowFrom rowTo                      { RowScopeDef $1 $2}
  {- Syntactic Sugar / To clause missing for just to now -}  
  {- Window goes to now so use now - 0 -}
  | timeFrom toNow                { TimeScopeDef $1 0}
  | rowFrom toNow                      { RowScopeDef $1 0} 
  {- Syntactic Sugar / Window expressed using "range" -}
  {- Window from is inclusive so use range -1         -} 
  {- Window goes to now so use now - 0                -}
  | timeRange                          { TimeScopeDef ($1-1) 0 }
  | rowRange                           { RowScopeDef ($1-1) 0 }
  {- Syntactic Sugar / Window express using "at"      -}
  {- Window from and to at the same point             -}
  | timeAt                             { TimeScopeDef $1 $1 }
  | rowAt                              { RowScopeDef $1 $1 }
  {- Syntactic Sugar / allow units only for "to" -}  
  | "from" "now" "-" int "to" nowMinus int "millisecs" 
        { TimeScopeDef ($4 * 1) ($7 * 1) }
  | "from" "now" "-" int "to" nowMinus int "seconds" 
        { TimeScopeDef ($4 * 1000 ) ($7 * 1000) }
  | "from" "now" "-" int "to" nowMinus int minutes 
        { TimeScopeDef ($4 * 60000 ) ($7 * 60000) }
  | "from" "now" "-" int "to" nowMinus int "hours" 
        { TimeScopeDef ($4 * 3600000 ) ($7 * 3600000) }
  | "from" "now" "-" int "to" nowMinus int "days" 
        { TimeScopeDef ($4 * 86400000 ) ($7 * 86400000) }              
        
timeFrom : "from" "now" "-" timeValue  { $4 }

timeTo 
  : "to" nowMinus timeValue            { $3 }
 
rowFrom : "from" "now" "-" rowValue    { $4 }

rowTo 
  : "to" nowMinus rowValue             { $3 }

toNow
  : "to" "now"                         { }
  |                                    { }
  
timeRange : "range" timeValue          { $2 }

timeAt : "at" "now" "-" timeValue      { $4 }

rowRange : "range" rowValue            { $2 }

rowAt : "at" "now" "-" rowValue        { $4 }

rowValue : int "rows"                  { $1 }

{- Assumes time granuality of milliSeconds -}
timeValue  
  : int "millisecs"                    { $1 }
  | int "seconds"                      { $1 *     1000 }
  | int minutes                        { $1 *    60000 }
  | int "hours"                        { $1 *  3600000 }
  | int "days"                         { $1 * 86400000 } 

minutes
  : "minutes"                          { }
  {- Handle abreviation Lexer can not (due to minimum) -}
  | "min"                              { }

nowMinus
  : "now" "-"                          {  }
  {- Syntactic Sugar / optional in "to" declaration -}  
  {- Ex. From now - 5 to 2 seconds -}
  |	                   { }

{- Alternative for time granulaity 15 minutes -}
{- timeValue  
  : int minutes                      { div $1 15 }
  | int "hours"                        { $1 * 4 }
  | int "days"                         { $1 * 96 } -}

{- Assumes time granuality of milliSeconds -}

timeSlide : "slide" timeValue          { $2 }
 
rowSlide : "slide" slideValue "rows"   { $2 }

slideValue : int                       { $1 }

predicate  
  : singlePredicate                    { $1 } 
  | singlePredicate "and" andList      { And ([$1] ++ $3) }
  | singlePredicate "or" orList        { Or ([$1] ++ $3) }

andList
  : singlePredicate                    { [$1] }                         
  | singlePredicate "and" andList      { [$1] ++ $3 }                 

orList
  : singlePredicate                    { [$1] }                         
  | singlePredicate "or" orList        { [$1] ++ $3 }                 

singlePredicate
  : "(" predicate ")"                  { $2 }
  | predicatePart "="    predicatePart { Equals $1 $3}   
  | predicatePart ">"    predicatePart { GreaterThan $1 $3}   
  | predicatePart ">""=" predicatePart { GreaterThanOrEquals $1 $4}   
  | predicatePart "<"    predicatePart { LessThan $1 $3}   
  | predicatePart "<""=" predicatePart { LessThanOrEquals $1 $4}                
  | predicatePart "<"">" predicatePart { NotEquals $1 $4}                
  | "not" singlePredicate              { Not $2 }
               
predicatePart
  : projectExpr                        { $1 }
  | literal                            { $1 }
                         
groupByClause 
  : "group" "by" groupByAttributes     { GroupByList $3 }

groupByAttributes
  : groupByAttribute                   { [$1] } 
  | groupByAttribute "," groupByAttributes     
         { $1 : $3 }
        
groupByAttribute 
  : localName  "." attributeName       { Attribute (LocalName $1) (AttributeName $3) }  
  | attributeName 
        { UnqualifiedAttribute (AttributeName $1) }

havingClause
  : {- Empty -}                        { TRUE }
  | "having" havingPredicates          { $2 }

havingPredicates  
  : havingPredicate                    { $1 } 
  | havingPredicate "and" havingAndList{ And ([$1] ++ $3) }
  | havingPredicate "or" havingOrList  { And ([$1] ++ $3) }

havingAndList
  : havingPredicate                    { [$1] }                         
  | havingPredicate "and" havingAndList{ [$1] ++ $3 }                 

havingOrList
  : havingPredicate                    { [$1] }                         
  | havingPredicate "or" havingOrList  { [$1] ++ $3 }                 

havingPredicate
  : "(" havingPredicate ")"            { $2 }
  | havingExpression "=" havingExpression
         { Equals $1 $3}   
  | havingExpression ">" havingExpression
         { GreaterThan $1 $3}   
  | havingExpression ">""=" havingExpression
         { GreaterThanOrEquals $1 $4}   
  | havingExpression "<" havingExpression    
         { LessThan $1 $3}   
  | havingExpression "<""=" havingExpression
         { LessThanOrEquals $1 $4}                
  | havingExpression "<"">" havingExpression
         { NotEquals $1 $4}                
  | "not" havingPredicate              { Not $2 }

havingExpression
  : projectExpr                        { $1 }
  | aggExpression                      { $1 }
  | mixedExpression                    { $1 }
  | literal                            { $1 }

{
parseError :: [Token] -> a
parseError _ = error "SNEEql Parse error"
}
