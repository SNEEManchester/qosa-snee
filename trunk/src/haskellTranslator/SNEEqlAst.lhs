\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlAst 
where

import Char
import List

class ToName a where
	toName :: a->AttributeName
\end{code}
\end{comment}

\section {Abstract Syntax Tree}
\label{sec_AST}

This section shows the actual Haskell data structure (see Section \ref{Sec_HaskellFormat}) used for the AST (Abstract Syntax Tree).

Every AST query structure has a \syn{FullQuery} as its root. 
\begin{code}	
data FullQuery  = FullQuery Query
\end{code}
\begin{comment}
\begin{code}		
  | ParseError
      deriving (Eq, Ord, Show, Read)      
\end{code}	
\end{comment}

SNEEql includes three types of queries.
The standard SELECT FROM WHERE query is split into a list of \syn{NamedExpressions}, an \syn{ExtentList} and \syn{BooleanExpression} respectively.
The parser will have identify if the NamedExpressions are built on simple attributes or aggregated attributes and construct a SimpleQuery of an AggregationQuery accordingly.

A Group by query in the format SELECT FROM WHERE GROUP\_BY HAVING will have a a list of \syn{NamedExpressions}, an \syn{ExtentList}, a \syn{BooleanExpression} for the WHERE, a \syn{GroupByList} and second \syn{BooleanExpression} for the having.
These \syn{NamedExpressions} can be made up of any combinations of simple and aggregated attributes.

Any of these queries can be wrapped with DStream, IStream or RStream.

\begin{code}	
data Query
  = SimpleQuery [NamedExpression] ExtentList BooleanExpression ExtentName
  | AggregationQuery [NamedExpression] ExtentList BooleanExpression ExtentName
  | GroupByQuery [NamedExpression] ExtentList BooleanExpression GroupByList BooleanExpression ExtentName 
  | DStream Query 
  | IStream Query 
  | RStream Query 
\end{code}
\begin{comment} 
\begin{code}	
   deriving (Eq, Ord, Show, Read)
             
setName :: Query -> String -> Query
setName (SimpleQuery projectList extentList booleanExpression _) extentName
   = (SimpleQuery projectList extentList booleanExpression (ExtentName extentName))
setName (AggregationQuery aggreationList extentList booleanExpression _) extentName
   = (AggregationQuery aggreationList extentList booleanExpression (ExtentName extentName))
setName (GroupByQuery mixedList extentList booleanExpression groupByAttributes havingPredicate _) extentName
   = (GroupByQuery mixedList extentList booleanExpression groupByAttributes havingPredicate (ExtentName extentName))
setName (DStream query) extentName	
   = DStream (setName query extentName)
setName (IStream query) extentName
   = IStream (setName query extentName)
setName (RStream query) extentName
   = RStream (setName query extentName)
\end{code}	
\end{comment} 

\subsection{Select Clause}
The Select cause will parse to a list of \syn{NamedExpressions}.

Every \syn{NamedExpression} is made up of an \syn{Expression} that describes what data to use and an \syn{AttributeName} which gives the name to assign to this data.
\begin{code}	
data NamedExpression = NamedExpression Expression AttributeName
    {- Syntactic Sugar / List all attributes -}
   | Star
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)      
\end{code}	
\end{comment}

\subsection{Expression AST}
The AST type \syn{Expression} is used for the SELECT, WHERE, GROUP BY and HAVING clauses of the query.
An \syn{Expression} can be an arbitrarily deep tree of expression which are rooted with on literals and attributes.
The parser distinguishes between expressions that contain no aggregates and those where all attributes are aggregated and those that mix aggregated and none aggregated attributes.

\begin{code}	
data Expression
  = Attribute LocalName AttributeName
  {- None Aggregate Artithmatic Expressions -}
  | Add        Expression Expression
  | Minus      Expression Expression
  | Multiply   Expression Expression
  | Divide     Expression Expression
  | Power      Expression Expression
  | Negate     Expression
  | SquareRoot Expression
  | IntLit     Int
  | FloatLit   Float
  {- String Expressions -}
  | Concat     Expression Expression
  | StringLit  String
  {- Aggregation Expressions -}
  | Avg Collection
  | Count Collection
  | Max Collection
  | Min Collection
  | Sum Collection
\end{code}
\begin{comment}
\begin{code}		
  {- Semantic Sugar -}
  | UnqualifiedAttribute AttributeName
      deriving (Eq, Ord, Show, Read)

type Attribute = Expression

instance ToName Expression where
   toName (Attribute localName attributeName) = attributeName
   toName (Add       expression1 expression2) = AttributeName  "theadd"
   toName (Minus     expression1 expression2) = AttributeName  "theminus"
   toName (Multiply  expression1 expression2) = AttributeName  "themultiply"
   toName (Divide    expression1 expression2) = AttributeName  "thedivide"
   toName (Power     expression1 expression2) = AttributeName  "thepower"
   toName (SquareRoot expression1) = AttributeName  "thesqrt"
   toName (Negate    expression1) = AttributeName  "thenot"
   toName (IntLit    int) = AttributeName  (show int)
   toName (FloatLit  float) = AttributeName  (show float)
   toName (Concat    expression1 expression2) = AttributeName  "theconcat"
   toName (StringLit string ) = AttributeName string
   toName (Avg collection) = AttributeName  "theavg"
   toName (Count collection) = AttributeName  "thecount"
   toName (Max collection) = AttributeName  "themax"
   toName (Min collection) = AttributeName  "themin"
   toName (Sum collection) = AttributeName  "thesum"
   toName (UnqualifiedAttribute attributeName) = attributeName
\end{code}	
\end{comment}

\begin{code}	
data Collection
   = Collection Expression
\end{code}
\begin{comment}
\begin{code}		
              deriving (Eq, Ord, Show, Read)
              
instance ToName Collection where
   toName (Collection expression) = toName expression   
\end{code}	
\end{comment}

\begin{code}
data LocalName = LocalName String
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)
\end{code}	
\end{comment}

\begin{code}
data ExtentName = ExtentName String
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)
\end{code}	
\end{comment}

\begin{code}
data AttributeName = AttributeName String
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)

\end{code}	
\end{comment}

\subsection{ExtentList AST}
Each element in the FROM statement will convert to an \syn{ExtentInstruction} element in the \syn{ExtentList}.
Where an FROM element is followed by a window definition, that element will be wrapped in a \syn{WindowedExtent}.
\begin{code}	
data ExtentList 
  = ExtentList [ExtentInstruction]
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)
\end{code}	
\end{comment}

\begin{code}
data ExtentInstruction 
  = Extent ExtentName LocalName
  | WindowedExtent ExtentInstruction WindowDef
  | Subquery Query ExtentName LocalName
\end{code}
\begin{comment}
\begin{code}		
              deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}

\begin{code}	
data WindowDef 
  = TimeWindowDef WindowScopeDef Slide
  | RowWindowDef WindowScopeDef Slide
  | InputWindowDef WindowScopeDef
\end{code}
\begin{comment}
\begin{code}		
                     deriving (Eq, Ord, Show, Read)
\end{code}	
\end{comment}

\begin{code}	
data WindowScopeDef	
  = TimeScopeDef From To
  | RowScopeDef From To
\end{code}
\begin{comment}
\begin{code}		
                        deriving (Eq, Ord, Show, Read)
\end{code}	
\end{comment}

\begin{code}	
type From  = Int
type To    = Int
type Slide = Int
\end{code}	

\subsection{Predicate AST}
The WHERE clause will be represented as a single \syn{BooleanExpression}, with the constant \syn{TRUE} being used for queries without a WHERE clause.

\begin{code}
data BooleanExpression
  = Equals              Expression Expression
  | NotEquals           Expression Expression 
  | GreaterThan         Expression Expression
  | LessThan            Expression Expression
  | GreaterThanOrEquals Expression Expression
  | LessThanOrEquals    Expression Expression
  | And                 [BooleanExpression]
  | Or                  [BooleanExpression]
  | Not                 BooleanExpression
  | TRUE
  | FALSE
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}

\subsection{Group By List AST}
The Group By clause is represented as \syn{GroupByList} token which holds a list of \syn{Attribute} tokens.

\begin{code}
data GroupByList = GroupByList [Attribute]  
\end{code}
\begin{comment}
\begin{code}		
      deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}

\subsection{Having Clause}   
The HAVING clause will be represented as a single \syn{BooleanExpression}, with the constant \syn{TRUE} being used for queries without a HAVING clause.
The parser which prevents aggregates from being used in the WHERE clause will allow them in the HAVING clause.
However, there is no difference between the AST token for both clauses.



