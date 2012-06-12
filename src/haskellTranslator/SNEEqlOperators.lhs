\section{Algebraic Operators Expressed in Haskell}
\label{sec_LAF}
\texttt{This section provides the formal definitions of the Operators into which the query will be translated in Section }\ref{sec_translating}. 


This section introduces the algebraic operators that make up \SNEEql.

The purpose is to show the Haskell data structure used to represent the algebra while the semantic description of the algebra is left until later.

The operators are split into the classes \sem{RelOperator}s, \sem{StrOperator}s and \sem{WinOperator}s depending on if they return relations, streams of tuples or windows.
\footnote{Due to the fact that Haskell does not actually support subclasses this division could not be enforced on the data structure.} 
The final \sem{Deliver} operator has not been split into classes as it meaning is exactly the same for all types of output.

Each operator has as parameters its particular settings and where applicable the operator(s) from which it will receive data.
\syn{ExtentName, LocalName, AttributeName, Expression, BooleanExpression} and \syn{WindowScopeDef} are the original AST tokens as these already convey the required information.

\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlOperators

where

import SNEEqlAst
import SNEEqlEnvironment
\end{code}
\end{comment}

\begin{code}
data Operator 
   {- Relational operators -}
  = OneOffScan ExtentName LocalName [AttributeName]
  | RelProject [Expression][AttributeName] RelOperator
  | RelCrossProduct RelOperator RelOperator
  | RelSelect BooleanExpression RelOperator
  | RelAggregation [Expression][AttributeName] RelOperator
  | RelRename LocalName RelOperator
  | RelUngroup [Expression][AttributeName] RelGrOperator
   {- Relational Grouped Operators -}
  | RelGroupBy [Attribute] RelOperator
  | RelGrSelect BooleanExpression RelGrOperator
   {- Tuple Stream Operators -}
  | StrSelect BooleanExpression StrOperator
  | StrProject [Expression][AttributeName] StrOperator
  | StrAcquire ExtentName LocalName [AttributeName]
  | StrReceive ExtentName LocalName [AttributeName]
  | StrRelCrossProduct StrOperator RelOperator
  | DStreamOp WinOperator
  | IStreamOp WinOperator
  | RStreamOp WinOperator 	
  | StrRename LocalName StrOperator
   {- WindowOperator -}
  | TimeWindow WindowScopeDef Tick StrOperator
  | RowWindow WindowScopeDef Index StrOperator
  | InputWindow WindowScopeDef StrOperator
  | WinSelect BooleanExpression WinOperator
  | WinProject [Expression][AttributeName] WinOperator
  | RepeatedScan Tick ExtentName LocalName [AttributeName]
  | WinCrossProduct WinOperator WinOperator
  | WinRelCrossProduct WinOperator RelOperator
  | WinAggregation [Expression][AttributeName] WinOperator  
  | WinRename LocalName WinOperator
  | WinUngroup [Expression][AttributeName] WinGrOperator
   {- Window Grouped Operators -}
  | WinGroupBy [Attribute] WinOperator
  | WinGrSelect BooleanExpression WinGrOperator
   {- Final -}
  | Deliver !Operator
   {- Test -}
  | OperatorStub String -- used as a base case 
\end{code}
\begin{comment}
\begin{code}  
      deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}

\begin{code}
type RelOperator = Operator
type RelGrOperator = Operator
type StrOperator = Operator
type WinOperator = Operator
type WinGrOperator = Operator

type Tick = Int
type Index = Int
\end{code}