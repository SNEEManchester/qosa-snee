\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlTranslate
where
import List
import SNEEqlAst
import SNEEqlEnvironment
import SNEEqlTypes
import SNEEqlTypeRules
import SNEEqlOperators
\end{code}
\end{comment}

\section {Translation Rules in Haskell}

\subsection{Translating a Query}
Rule: \ref{rule_TranslateFullQuery}
\begin{code}
translateFullQuery :: Environment -> FullQuery -> Operator
translateFullQuery environment (FullQuery query)
   = let finalOperator = translateQuery environment query
   in Deliver finalOperator
\end{code}

Rule: \ref{rule_TranslateRelation}
\begin{code}
translateQuery :: Environment -> Query -> Operator
translateQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in (RelProject qualifiedExpressions attributeNames (RelSelect qualifiedPredicate extentListOperator))
\end{code}     
Rule: \ref{rule_TranslateRelationAggregation}
\begin{code}
translateQuery environment (AggregationQuery namedExpressions extentList predicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in (RelAggregation expressions attributeNames (RelSelect qualifiedPredicate extentListOperator))
\end{code}     
Rule: \ref{rule_TranslateWindow}      
\begin{code}
translateQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in (WinProject qualifiedExpressions attributeNames (WinSelect qualifiedPredicate extentListOperator))
\end{code}     
Rule: \ref{rule_TranslateWindowAggregation}  
\begin{code}
translateQuery environment (AggregationQuery namedExpressions extentList predicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in (WinAggregation qualifiedExpressions attributeNames (WinSelect qualifiedPredicate extentListOperator))
\end{code}

Rule: \ref{rule_TranslateGroupByRelation} 
\begin{code}
translateQuery environment (GroupByQuery namedExpressions extentList predicate (GroupByList groupAttributes) havingPredicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedAttributes = map (qualifyExpression localEnvironment) groupAttributes
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let qualifiedHaving = qualifyBoolean localEnvironment havingPredicate
   in (RelUngroup qualifiedExpressions attributeNames (RelGrSelect qualifiedHaving (RelGroupBy qualifiedAttributes (RelSelect qualifiedPredicate extentListOperator))))
\end{code}

Rule: \ref{rule_TranslateGroupByWindow} 
\begin{code}
translateQuery environment (GroupByQuery namedExpressions extentList predicate (GroupByList groupAttributes) havingPredicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedAttributes = map (qualifyExpression localEnvironment) groupAttributes
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let qualifiedHaving = qualifyBoolean localEnvironment havingPredicate
   in (WinUngroup qualifiedExpressions attributeNames (WinGrSelect qualifiedHaving (WinGroupBy qualifiedAttributes (WinSelect qualifiedPredicate extentListOperator))))
\end{code}

Rule: \ref{rule_TranslatedStream}
\begin{code}
translateQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasStream (typeExtentList environment extentList) 
   = let extentListOperator = translateExtentList environment extentList
   in let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in (StrProject qualifiedExpressions attributeNames (StrSelect qualifiedPredicate extentListOperator))
\end{code}   
   
Rule \ref{rule_TranslateDStream}
\begin{code}
translateQuery environment (DStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translateQuery environment query
   in DStreamOp op1
\end{code}   
   
Rule \ref{rule_TranslateIStream}
\begin{code}
translateQuery environment (IStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translateQuery environment query
   in IStreamOp op1
\end{code}   
   
Rule \ref{rule_TranslateRStream}
\begin{code}
translateQuery environment (RStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translateQuery environment query
   in RStreamOp op1
translateQuery environment query
   = error ("Unexpected failure in translateQuery with "++(show query))
\end{code}

\subsection{Optimizsed Translating a Query}
Rule: \ref{rule_TranslateFullQuery}
\begin{code}
translatePlusFullQuery :: Environment -> FullQuery -> Operator
translatePlusFullQuery environment (FullQuery query)
   =  let finalOperator = translatePlusQuery environment query
   in Deliver finalOperator
\end{code}

Rule: \ref{rule_TranslateRelation}
\begin{code}
translatePlusQuery :: Environment -> Query -> Operator
translatePlusQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in (RelProject qualifiedExpressions attributeNames extentListOperator)
\end{code}     
Rule: \ref{rule_TranslateRelationAggregation}
\begin{code}
translatePlusQuery environment (AggregationQuery namedExpressions extentList predicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in (RelAggregation qualifiedExpressions attributeNames extentListOperator)
\end{code}     
Rule: \ref{rule_TranslateWindow}      
\begin{code}
translatePlusQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in (WinProject qualifiedExpressions attributeNames extentListOperator)
\end{code}    

Rule: \ref{rule_TranslateWindowAggregation}  
\begin{code}
translatePlusQuery environment (AggregationQuery namedExpressions extentList predicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in (WinAggregation qualifiedExpressions attributeNames extentListOperator)
\end{code}

Rule: \ref{rule_TranslateGroupByRelation} 
\begin{code}
translatePlusQuery environment (GroupByQuery namedExpressions extentList predicate (GroupByList groupAttributes) havingPredicate localName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedAttributes = map (qualifyExpression localEnvironment) groupAttributes
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedHaving = qualifyBoolean localEnvironment havingPredicate
   in (RelUngroup qualifiedExpressions attributeNames (RelGrSelect qualifiedHaving (RelGroupBy qualifiedAttributes extentListOperator)))
\end{code}

Rule: \ref{rule_TranslateGroupByWindow} 
\begin{code}
translatePlusQuery environment (GroupByQuery namedExpressions extentList predicate (GroupByList groupAttributes) havingPredicate localName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedAttributes = map (qualifyExpression localEnvironment) groupAttributes
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in let qualifiedHaving = qualifyBoolean localEnvironment havingPredicate
   in (WinUngroup qualifiedExpressions attributeNames (WinGrSelect qualifiedHaving (WinGroupBy qualifiedAttributes extentListOperator)))
\end{code}

Rule: \ref{rule_TranslatedStream}
\begin{code}
translatePlusQuery environment (SimpleQuery namedExpressions extentList predicate localName) 
   | hasStream (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let qualifiedPredicate = qualifyBoolean localEnvironment predicate
   in let predicates = splitPredicate qualifiedPredicate
   in let extentListOperator = translatePlusExtentList environment extentList predicates
   in let (expressions, attributeNames) = splitNamedExpressions localEnvironment namedExpressions
   in let qualifiedExpressions = map (qualifyExpression localEnvironment) expressions
   in (StrProject qualifiedExpressions attributeNames extentListOperator)
\end{code}   
   
Rule \ref{rule_TranslateDStream}
\begin{code}
translatePlusQuery environment (DStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translatePlusQuery environment query
   in DStreamOp op1
\end{code}   
   
Rule \ref{rule_TranslateIStream}
\begin{code}
translatePlusQuery environment (IStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translatePlusQuery environment query
   in IStreamOp op1
\end{code}   
   
Rule \ref{rule_TranslateRStream}
\begin{code}
translatePlusQuery environment (RStream query) 
   | hasWindow (typeQuery environment query) 
   = let op1 = translatePlusQuery environment query
   in RStreamOp op1
\end{code}

\begin{code}
translatePlusQuery _ query 
  = error ("Unexpected failure in translatePlusQuery with "++(show query))
\end{code}

\subsection{Translating the Extents in a Query}

\subsubsection{Translating an Extent}

Rules: \ref{rule_TranslateStored}, \ref{rule_TranslatePushed} and \ref{rule_TranslateSensed}.
\begin{code}
translateExtent:: Environment -> ExtentInstruction -> Operator
translateExtent environment (Extent extentName localName)
   = let (DeclExt _ extentType attributeNames) = getDeclExtsByName environment extentName
   in case extentType of
     StoredToken -> OneOffScan extentName localName attributeNames
     PushedToken -> StrReceive extentName localName attributeNames
           -- to do get acc rate and sites from X
     SensedToken 
         -> StrAcquire extentName localName attributeNames
\end{code}

Rule: \ref{rule_TranslateTimeWindow}
\begin{code}
translateExtent environment (WindowedExtent extentInstruction 
        (TimeWindowDef windowScopeDef slide))
    = let op1 = translateExtent environment extentInstruction
    in TimeWindow windowScopeDef slide op1
\end{code}

Rule: \ref{rule_TranslateRowWindow}
\begin{code}
translateExtent environment (WindowedExtent extentInstruction 
        (RowWindowDef windowScopeDef slide))
    = let op1 = translateExtent environment extentInstruction
    in RowWindow windowScopeDef slide op1
\end{code}

Rule: \ref{rule_TranslateInputWindow}
\begin{code}
translateExtent environment (WindowedExtent extentInstruction 
        (InputWindowDef windowScopeDef))
    = let op1 = translateExtent environment extentInstruction
    in InputWindow windowScopeDef op1
\end{code}

Rule: \ref {rule_TranslateRelSubQuery}
\begin{code}
translateExtent environment (Subquery query _ localName)
   | hasBagofTupleof (typeQuery environment query) 
    = RelRename localName (translateQuery environment query)
\end{code}

Rule: \ref {rule_TranslateWinSubQuery}
\begin{code}
translateExtent environment (Subquery query _ localName)
   | hasWindow (typeQuery environment query) 
    = WinRename localName (translateQuery environment query)
\end{code}

Rule: \ref {rule_TranslateStrSubQuery}
\begin{code}
translateExtent environment (Subquery query _ localName)
   | hasStream (typeQuery environment query) 
    = StrRename localName (translateQuery environment query)
translateExtent _ extent 
    = error ("Extent "++(show extent)
        ++" not yet programmed in translateExtent")
\end{code}

\subsubsection{Optimized Translating an Extent}

Rules: \ref{rule_TranslateStored}, \ref{rule_TranslatePushed} and \ref{rule_TranslateSensed}.
\begin{code}
findLocalNames :: ExtentInstruction -> [LocalName]
findLocalNames (Extent extentName localName) = [localName]
findLocalNames (WindowedExtent extentInstruction windowDef) 
   = findLocalNames extentInstruction
findLocalNames (Subquery query extentName localName) = [localName]

translatePlusExtent:: Environment -> ExtentInstruction -> [BooleanExpression] -> (Operator, [BooleanExpression])
translatePlusExtent environment (Extent extentName localName) predicates
   = let (DeclExt _ extentType attributeNames) = getDeclExtsByName environment extentName
   in let (applicable, others) = findApplicablePredicates [localName] predicates
   in if (applicable == TRUE)
      then case extentType of
         StoredToken -> (OneOffScan extentName localName attributeNames, others)
         PushedToken -> (StrReceive extentName localName attributeNames, others)
           -- to do get acc rate and sites from X
         SensedToken -> (StrAcquire extentName localName attributeNames, others)
      else case extentType of
         StoredToken -> (RelSelect applicable (OneOffScan extentName localName attributeNames), others)
         PushedToken -> (StrSelect applicable (StrReceive extentName localName attributeNames), others)
           -- to do get acc rate and sites from X
         SensedToken -> (StrSelect applicable (StrAcquire extentName localName attributeNames), others)         
\end{code}

Rule: \ref{rule_TranslateTimeWindow}
Windows with time scope and time slide the select can go below.
Otherwise select must be after window.
\begin{code}
translatePlusExtent environment (WindowedExtent extentInstruction (TimeWindowDef windowScopeDef@(TimeScopeDef _ _) slide)) predicates
    = let (op1,remainderPredicates) = translatePlusExtent environment extentInstruction predicates
    in ((TimeWindow windowScopeDef slide op1),remainderPredicates)
    
translatePlusExtent environment (WindowedExtent extentInstruction (TimeWindowDef windowScopeDef slide)) predicates
    = let op1 = translateExtent environment extentInstruction
    in let localNames = findLocalNames extentInstruction
    in let (predicate, remainingPredicates) = findApplicablePredicates localNames predicates
    in if (predicate == TRUE)
       then ((TimeWindow windowScopeDef slide op1),remainingPredicates)
       else ((WinSelect predicate (TimeWindow windowScopeDef slide op1)),remainingPredicates)
\end{code}

Rule: \ref{rule_TranslateRowWindow}
Row slide Select is always after window.
\begin{code}
translatePlusExtent environment (WindowedExtent extentInstruction (RowWindowDef windowScopeDef slide)) predicates
    = let op1 = translateExtent environment extentInstruction
    in let localNames = findLocalNames extentInstruction
    in let (predicate, remainingPredicates) = findApplicablePredicates localNames predicates
    in if (predicate == TRUE)
       then ((RowWindow windowScopeDef slide op1),remainingPredicates)
       else ((WinSelect predicate (RowWindow windowScopeDef slide op1)),remainingPredicates)
\end{code}

Rule: \ref{rule_TranslateInputWindow}
Windows with time scope the select can go below.
Otherwise select must be after window.
\begin{code}
translatePlusExtent environment (WindowedExtent extentInstruction (InputWindowDef windowScopeDef)) predicates
    = let (op1,remainderPredicates) = translatePlusExtent environment extentInstruction predicates
    in ((InputWindow windowScopeDef op1),remainderPredicates)
translatePlusExtent environment (WindowedExtent extentInstruction (InputWindowDef windowScopeDef)) predicates
    = let op1 = translateExtent environment extentInstruction
    in let localNames = findLocalNames extentInstruction
    in let (predicate, remainingPredicates) = findApplicablePredicates localNames predicates
    in if (predicate == TRUE)
       then ((InputWindow windowScopeDef op1),remainingPredicates)
       else ((WinSelect predicate (InputWindow windowScopeDef op1)),remainingPredicates)
\end{code}

Rule: \ref {rule_TranslateRelSubQuery}
\begin{code}
translatePlusExtent environment (Subquery query _ localName) predicates
   | hasBagofTupleof (typeQuery environment query) 
   = let (applicable, others) = findApplicablePredicates [localName] predicates
   in if (applicable == TRUE)
      then ((RelRename localName (translatePlusQuery environment query)), others)
      else ((RelSelect applicable (RelRename localName (translateQuery environment query))), others)
\end{code}




Rule: \ref {rule_TranslateWinSubQuery}
\begin{code}
translatePlusExtent environment (Subquery query _ localName) predicates
   | hasWindow (typeQuery environment query) 
   = let (applicable, others) = findApplicablePredicates [localName] predicates
   in if (applicable == TRUE)
      then ((WinRename localName (translatePlusQuery environment query)), others)
      else ((WinSelect applicable (WinRename localName (translateQuery environment query))), others)
\end{code}

Rule: \ref {rule_TranslateStrSubQuery}
\begin{code}
translatePlusExtent environment (Subquery query _ localName) predicates
   | hasStream (typeQuery environment query) 
   = let (applicable, others) = findApplicablePredicates [localName] predicates
   in if (applicable == TRUE)
      then ((StrRename localName (translatePlusQuery environment query)), others)
      else ((StrSelect applicable (StrRename localName (translateQuery environment query))), others)
\end{code}

\begin{code}
translatePlusExtent environment extentInstruction predicates
   = error ("translatePlusExtent not finished with " ++ (show extentInstruction))
   
isApplicable :: [LocalName] -> BooleanExpression -> Bool
isApplicable localNames predicate 
  = let requires = extractLocalNames predicate
  in let missing = requires \\ localNames
  in (missing == [])

findApplicablePredicates :: [LocalName] -> [BooleanExpression] -> (BooleanExpression,[BooleanExpression])
findApplicablePredicates _ [TRUE] = (TRUE,[])   
findApplicablePredicates localNames predicates
   = let applicable = filter (isApplicable localNames) predicates
   -- = let applicable = filter (\x -> ((extractLocalNames x \\ localNames) == [] ))
   in let others = predicates \\ applicable
   in ((mergePredicates applicable), others)

mergePredicates :: [BooleanExpression] -> BooleanExpression
mergePredicates [] = TRUE
mergePredicates (first:more)
   | more == [] = first
   | otherwise = And (first:more)
   
splitPredicate :: BooleanExpression -> [BooleanExpression]
splitPredicate (And predicates) = predicates
splitPredicate predicate = [predicate]

extractLocalNames :: BooleanExpression -> [LocalName]
extractLocalNames (Equals expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (NotEquals expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (GreaterThan expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (LessThan expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (GreaterThanOrEquals expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (LessThanOrEquals expression1 expression2) 
  = nub((extractLocalNames1 expression1) ++ (extractLocalNames1 expression2))
extractLocalNames (And expressionList) 
  = nub(foldr (++) [] (map extractLocalNames expressionList))
extractLocalNames (Or expressionList) 
  = nub(foldr (++) [] (map extractLocalNames expressionList))
extractLocalNames (Not expression) 
  = nub(extractLocalNames expression)
extractLocalNames TRUE = []
extractLocalNames FALSE = []

extractLocalNames1 :: Expression -> [LocalName]
extractLocalNames1 (Attribute localName _) = [localName]
extractLocalNames1 (Add expression1 expression2) 
  = (extractLocalNames1 expression1) ++ (extractLocalNames1 expression2)
extractLocalNames1 (Minus expression1 expression2) 
  = (extractLocalNames1 expression1) ++ (extractLocalNames1 expression2)
extractLocalNames1 (Multiply expression1 expression2) 
  = (extractLocalNames1 expression1) ++ (extractLocalNames1 expression2)
extractLocalNames1 (Divide expression1 expression2) 
  = (extractLocalNames1 expression1) ++ (extractLocalNames1 expression2)
extractLocalNames1 (Negate expression) 
  = (extractLocalNames1 expression) 
extractLocalNames1 (IntLit _) = []
extractLocalNames1 (FloatLit _) = []
extractLocalNames1 (Concat expression1 expression2) 
  = (extractLocalNames1 expression1) ++ (extractLocalNames1 expression2)
extractLocalNames1 (StringLit _) = []
extractLocalNames1 (Avg collection) 
  = (extractLocalNames2 collection) 
extractLocalNames1 (Count collection) 
  = (extractLocalNames2 collection) 
extractLocalNames1 (Max collection) 
  = (extractLocalNames2 collection) 
extractLocalNames1 (Min collection) 
  = (extractLocalNames2 collection) 
extractLocalNames1 (Sum collection) 
  = (extractLocalNames2 collection) 

extractLocalNames2 :: Collection -> [LocalName]
extractLocalNames2 (Collection expression) 
  = (extractLocalNames1 expression) 

\end{code}
  
\subsubsection{Queries with a single Extent}
Semantic Sugar so that the order of extents doesn't matter.
\begin{code}
translateExtentList :: Environment -> ExtentList -> Operator
translateExtentList environment extentList 
  = let extents = sortList environment extentList
  in translateSortedExtentList environment extents
\end{code}

Rule: \ref{rule_SingleExtent}
\begin{code}  
translateSortedExtentList :: Environment -> [ExtentInstruction] -> Operator
translateSortedExtentList environment extentList
  | length extentList == 1
  = translateExtent environment (head extentList)
\end{code}

Rule: \ref{rule_ProdRelTranslation}
\begin{code}  
translateSortedExtentList environment extentList
  | all hasRelation (map (typeExtent environment) extentList)
  = let op1 = translateExtent environment (head extentList)
  in let op2 = translateSortedExtentList environment (tail extentList)
  in RelCrossProduct op1 op2
\end{code}

Rule \ref{rule_ProdWinTranslation}
\begin{code}
translateSortedExtentList environment extentList
  | all hasWindow (map (typeExtent environment) extentList)
  = let op1 = translateExtent environment (head extentList)
  in let op2 = translateSortedExtentList environment (tail extentList)
  in WinCrossProduct op1 op2
\end{code}

Rule \ref{rule_TranslateProdRelStream}
\begin{code}
translateSortedExtentList environment extentList
  | hasStream (typeExtent environment (head extentList)) &&
    all hasRelation (map (typeExtent environment) 
        (tail extentList))
  = let op1 = translateExtent environment (head extentList)
  in let op2 = translateSortedExtentList environment (tail extentList)
  in StrRelCrossProduct op1 op2
\end{code}

Rule \ref{rule_TranslateProdRelWin}
\begin{code}
translateSortedExtentList environment extentList
  | any hasRelation (map (typeExtent environment) extentList) 
    && any hasWindow (map (typeExtent environment) extentList)
  = let exts1 = filter (typesToWindow environment) extentList
  in let exts2 = filter (typesToRelation environment) extentList
  in let op1 = translateSortedExtentList environment exts1
  in let op2 = translateSortedExtentList environment exts2
  in WinRelCrossProduct op1 op2

translateSortedExtentList environment extentList 
  = error ("program error in translateSortedExtentList: "
       ++ (show extentList))

\end{code}
  
\subsubsection{Optimized Queries with a single Extent}
Semantic Sugar so that the order of extents doesn't matter.
\begin{code}

translatePlusExtentList :: Environment -> ExtentList -> [BooleanExpression] -> Operator
translatePlusExtentList environment extentList predicates
  = let extents = sortList environment extentList
  in let (operator, remainingPredicate) = translatePlusSortedExtentList environment extents predicates
  in if ((length remainingPredicate) == 0)
     then operator
     else error ("Remaining Predicates: "++(show remainingPredicate))
\end{code}
Rule: \ref{rule_SingleExtent}

\begin{code}  
translatePlusSortedExtentList :: Environment -> [ExtentInstruction] -> [BooleanExpression] -> (Operator,[BooleanExpression])
translatePlusSortedExtentList environment extentList predicates
  | length extentList == 1
  = translatePlusExtent environment (head extentList) predicates
\end{code}

Rule: \ref{rule_ProdRelTranslation}
\begin{code}  
translatePlusSortedExtentList environment extentList predicates
  |  all hasRelation (map (typeExtent environment) extentList)
  =  let (op1, remainingPredicates) = translatePlusExtent environment (head extentList) predicates
  in let (op2,remainingPredicates2) = translatePlusSortedExtentList environment (tail extentList) remainingPredicates
  in let localNames = nub(foldr (++) [] (map findLocalNames extentList))
  in let (applicable, others) = findApplicablePredicates localNames remainingPredicates2
  in if (applicable == TRUE)
     then ((RelCrossProduct op1 op2),others)
     else ((RelSelect applicable (RelCrossProduct op1 op2)),others)
\end{code}

Rule \ref{rule_ProdWinTranslation}
\begin{code}
translatePlusSortedExtentList environment extentList predicates
  | all hasWindow (map (typeExtent environment) extentList)
  =  let (op1, remainingPredicates) = translatePlusExtent environment (head extentList) predicates
  in let (op2,remainingPredicates2) = translatePlusSortedExtentList environment (tail extentList) remainingPredicates
  in let localNames = nub(foldr (++) [] (map findLocalNames extentList))
  in let (applicable, others) = findApplicablePredicates localNames remainingPredicates2
  in if (applicable == TRUE)
     then ((WinCrossProduct op1 op2),others)
     else ((WinSelect applicable (WinCrossProduct op1 op2)),others)
\end{code}

Rule \ref{rule_TranslateProdRelStream}
\begin{code}
translatePlusSortedExtentList environment extentList predicates
  | hasStream (typeExtent environment (head extentList)) 
  && all hasRelation (map (typeExtent environment) (tail extentList))
  =  let (op1, remainingPredicates) = translatePlusExtent environment (head extentList) predicates
  in let (op2,remainingPredicates2) = translatePlusSortedExtentList environment (tail extentList) remainingPredicates
  in let localNames = nub(foldr (++) [] (map findLocalNames extentList))
  in let (applicable, others) = findApplicablePredicates localNames remainingPredicates2
  in if (applicable == TRUE)
     then ((StrRelCrossProduct op1 op2),others)
     else ((StrSelect applicable (StrRelCrossProduct op1 op2)),others)
\end{code}

Rule \ref{rule_TranslateProdRelWin}
\begin{code}
translatePlusSortedExtentList environment extentList predicates
  | any hasRelation (map (typeExtent environment) extentList) 
    && any hasWindow (map (typeExtent environment) extentList)
  =  let (op1, remainingPredicates) = translatePlusExtent environment (head extentList) predicates
  in let (op2,remainingPredicates2) = translatePlusSortedExtentList environment (tail extentList) remainingPredicates
  in let localNames = nub(foldr (++) [] (map findLocalNames extentList))
  in let (applicable, others) = findApplicablePredicates localNames remainingPredicates2
  in if (applicable == TRUE)
     then ((WinRelCrossProduct op1 op2),others)
     else ((WinSelect applicable (WinRelCrossProduct op1 op2)),others)
\end{code}

\begin{code}
translatePlusSortedExtentList environment extentList predicates
  = error ("program error in translatePlusSortedExtentList: "
       ++ (show extentList))

typesToRelation :: Environment -> ExtentInstruction -> Bool  
typesToRelation environment extent 
  = let (_, tau) = typeExtent environment extent
  in isRelation tau
  
typesToWindow :: Environment -> ExtentInstruction -> Bool  
typesToWindow environment extent 
  = let (_, tau) = typeExtent environment extent
  in isWindow tau
\end{code}


