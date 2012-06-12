\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlTypeRules
where

import List
import SNEEqlAst
import SNEEqlTypes
import SNEEqlEnvironment
\end{code}
\end{comment}

\section {Typing Rules in Haskell}

\subsection{Typing a Query}
Rule: \ref{rule_FullQueryType}
\begin{code}
typeFullQuery :: Environment -> FullQuery -> (Environment, Type)
typeFullQuery environment (FullQuery query)
   = typeQuery environment query
\end{code}

Rule: \ref{rule_RelationType}
\begin{code}
typeQuery :: Environment -> Query -> (Environment, Type)
typeQuery environment (SimpleQuery projections extentList predicate queryName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (instructions, names) = splitNamedExpressions localEnvironment projections
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment predicate) == Boolean
   then
      let newEnvironment1 = relateNamesToTypes environment names projectTypes
      in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName RelationView names)
      in (newEnvironment2, Bagof(Tupleof(projectTypes)))
   else
      error "Predicate did not type to Boolean"
\end{code}     
%Rule: \ref{rule_RelationAggregationType}      
\begin{code}
typeQuery environment (AggregationQuery namedExpressions extentList predicate queryName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (instructions, names) = splitNamedExpressions localEnvironment namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment predicate) == Boolean
   then
      let newEnvironment1 = relateNamesToTypes environment names projectTypes
      in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName RelationView names)
      in (newEnvironment2, Bagof(Tupleof(projectTypes)))
   else
      error "Predicate did not type to Boolean"
\end{code}     
Rule: \ref{rule_WindowType}      
\begin{code}
typeQuery environment (SimpleQuery namedExpressions extentList predicate queryName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (instructions, names) = splitNamedExpressions localEnvironment namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment predicate) == Boolean
   then
      let newEnvironment1 = relateNamesToTypes environment names projectTypes
      in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName WindowView names)
      in (newEnvironment2, Streamof (Tupleof ([Int] ++ [Bagof (Tupleof projectTypes)])))
   else
      error "Predicate did not type to Boolean"
\end{code}     
%Rule: \ref{rule_WindowAggregationType}  
\begin{code}
typeQuery environment (AggregationQuery namedExpressions extentList predicate queryName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (instructions, names) = splitNamedExpressions localEnvironment namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment predicate) == Boolean
   then
      let newEnvironment1 = relateNamesToTypes environment names projectTypes
      in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName WindowView names)
      in (newEnvironment2, Streamof (Tupleof ([Int] ++ [Bagof (Tupleof projectTypes)])))
   else
      error "Predicate did not type to Boolean"
\end{code}
Rule \ref{rule_RelationGroupByType}
\begin{code}
typeQuery environment (GroupByQuery namedExpressions extentList predicate groupByList havingPredicate queryName) 
   | hasBagofTupleof (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (localEnvironment2, groupByTypes) = typeGroupByList localEnvironment groupByList
   in let (instructions, names) = splitNamedExpressions localEnvironment2 namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment2 predicate) == Boolean
   then
      if (typePredicate localEnvironment2 havingPredicate) == Boolean
      then
         let newEnvironment1 = relateNamesToTypes environment names projectTypes
         in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName WindowView names)
         in (newEnvironment2, Bagof(Tupleof(projectTypes)))
      else
         error "Having Predicate does not type to Boolean"
   else
      error "Predicate did not type to Boolean"
\end{code}
Rule \ref{rule_WindowGroupByType}
\begin{code}
typeQuery environment (GroupByQuery namedExpressions extentList predicate groupByList havingPredicate queryName) 
   | hasWindow (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (localEnvironment2, groupByTypes) = typeGroupByList localEnvironment groupByList
   in let (instructions, names) = splitNamedExpressions localEnvironment2 namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment2 predicate) == Boolean
   then
      if (typePredicate localEnvironment2 havingPredicate) == Boolean
      then
         let newEnvironment1 = relateNamesToTypes environment names projectTypes
         in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName WindowView names)
         in (newEnvironment2, Streamof (Tupleof ([Int] ++ [Bagof (Tupleof projectTypes)])))
      else
         error "Having Predicate does not type to Boolean"
   else
      error "Predicate did not type to Boolean"
\end{code}
Rule: \ref{rule_StreamType}
\begin{code}
typeQuery environment (SimpleQuery namedExpressions extentList predicate queryName) 
   | hasStream (typeExtentList environment extentList) 
   = let (localEnvironment, extentTypes) = typeExtentList environment extentList
   in let (instructions, names) = splitNamedExpressions localEnvironment namedExpressions
   in let projectTypes = map (typeExpression localEnvironment) instructions
   in if (typePredicate localEnvironment predicate) == Boolean
   then
      let newEnvironment1 = relateNamesToTypes environment names projectTypes
      in let newEnvironment2 = addDeclaration newEnvironment1 (DeclExt queryName StreamView names)
         in (newEnvironment2, (Streamof (Tupleof (Int:Int:[Tupleof projectTypes]))))
   else
      error "Predicate did not type to Boolean"
typeQuery environment (AggregationQuery namedExpressions extentList predicate queryName) 
   | hasStream (typeExtentList environment extentList) 
   = error "Query Error: Aggregation not allowed on streams of tuples"
typeQuery environment (GroupByQuery namedExpressions extentList predicate groupByList havingPredicate queryName) 
   | hasStream (typeExtentList environment extentList) 
   = error "Query Error: Group By not allowed on streams of tuples"
typeQuery environment query@(SimpleQuery namedExpressions extentList predicate queryName) 
   = error ("Unexpected failure in typeQuery with "++(show query)++" Type = "++(show (typeExtentList environment extentList)))
\end{code}   
   
Rule \ref{rule_DStreamType}
\begin{code}
typeQuery environment (DStream query) 
   | hasWindow (typeQuery environment query) 
   = let (environment1, (Streamof (Tupleof (Int:[Bagof (Tupleof types)])))) = (typeQuery environment query) 
   in (environment1, Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types])))
   | otherwise = error "Query Error: DStream only allowed over windowed queries"
typeQuery environment (IStream query) 
   | hasWindow (typeQuery environment query) 
   = let (environment1, (Streamof (Tupleof (Int:[Bagof (Tupleof types)])))) = (typeQuery environment query) 
   in (environment1, Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types])))
   | otherwise = error "Query Error: IStream only allowed over windowed queries"
typeQuery environment (RStream query) 
   | hasWindow (typeQuery environment query) 
   = let (environment1, (Streamof (Tupleof (Int:[Bagof (Tupleof types)])))) = (typeQuery environment query) 
   in (environment1, Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types])))
   | otherwise = error "Query Error: RStream only allowed over windowed queries"
typeQuery environment query
   = error ("Unexpected failure in typeQuery with "++(show query))
\end{code}
\begin{code}
-- Support functions for typeQuery --
hasBagofTupleof (_ , (Bagof (Tupleof _)))  = True
hasBagofTupleof _ = False

isBagofTupleof (Bagof (Tupleof _))  = True
isBagofTupleof _ = False

hasStream (_ , (Streamof (Tupleof (Int:Int:[Tupleof types]))))  = True
hasStream _ = False

isStream (Streamof (Tupleof (Int:Int:[Tupleof _]))) = True
isStream _ = False

hasWindow (_, (Streamof (Tupleof (Int:[Bagof (Tupleof types)]))))  = True
hasWindow _ = False

isWindow (Streamof (Tupleof (Int:[Bagof (Tupleof types)])))  = True
isWindow _ = False

hasRelation ((_, Bagof (Tupleof types)))  = True
hasRelation _ = False

isRelation (Bagof (Tupleof types))  = True
isRelation _ = False

splitNamedExpressions :: Environment -> [NamedExpression] -> ([Expression],[AttributeName])
splitNamedExpressions environment projections 
    = let (instructions, names) = splitNamedExpressions2 environment projections
    in let distinquishedNames = distinquishNames names
    in (instructions, distinquishedNames)

splitNamedExpressions2 :: Environment -> [NamedExpression] -> ([Expression],[AttributeName])
splitNamedExpressions2 _ [] = ([],[])
splitNamedExpressions2 environment ((NamedExpression projectInstruction projectName):more)
    = let (instructions, names) = splitNamedExpressions2 environment more
    in ((projectInstruction:instructions),(projectName:names))
    
splitNamedExpressions2 environment (Star:more)
   = let localNames = getLocalNames environment
   in let expressions = foldl (++) [] (map (findAttributeNames environment) localNames)
   in let attributeNames = map toName expressions
   in let (instructions, names) = splitNamedExpressions2 environment more
   in (expressions++instructions, attributeNames++names)

distinquishNames :: [AttributeName] -> [AttributeName] 
distinquishNames [] = []
distinquishNames (name:names)
   = let newNames = changeNames name 2 names
   in (name:(distinquishNames newNames))

changeNames :: AttributeName -> Int -> [AttributeName] -> [AttributeName]
changeNames _ _ [] = []
changeNames (AttributeName name) count ((AttributeName first):more) 
   | name == first
   = ((AttributeName (name ++ (show count))):(changeNames (AttributeName name) (count+1) more))
   | otherwise
   = ((AttributeName first):(changeNames (AttributeName name) count more))
 
findAttributeNames environment localName
   = let boundName = getBoundName environment localName
   in let (DeclExt _ _ attributeNames) = getDeclExtsByName environment boundName
   in map (convertToAttribute localName) attributeNames

convertToAttribute :: LocalName -> AttributeName -> Attribute
convertToAttribute localName attributeName 
   = Attribute localName attributeName 
\end{code}

\subsection{Typing the Extents in a Query}
Rule: \ref{rule_ExtentType}
\begin{code}
typeExtent :: Environment -> ExtentInstruction -> (Environment, Type)
typeExtent environment (Extent extentName localName)
    = let tau = getTypeByExtentName environment extentName
    in let newEnvironment = bindName environment localName extentName
    in (newEnvironment, tau)
\end{code}

Rule: \ref {rule_SubqueryType}
\begin{code}
typeExtent environment (Subquery query extentName localName)
    = let (environment1, tau) = typeQuery environment query   
    in let environment2 = bindName environment1 localName extentName
    in (environment2, tau)
\end{code}

Rule: \ref{rule_WindowedExtentType}
\begin{code}
typeExtent environment (WindowedExtent extentInstruction _)
    | hasStream (typeExtent environment extentInstruction)
    = let (newEnvironment, (Streamof (Tupleof (Int:Int:[Tupleof types])))) = (typeExtent environment extentInstruction)
    in (newEnvironment, (Streamof (Tupleof (Int:[Bagof (Tupleof types)]))))
typeExtent environment (WindowedExtent _ _)
    = error "Illegal Query: Windows can only be placed on streams of tuples"
typeExtent _ extent = error ("Extent "++(show extent)++" not yet programmed in typeExtent")  
\end{code}

Support Functions for Typing ExtentList
\begin{code}
extentOrder environment extent1 extent2
  = let (_,tau1) = typeExtent environment extent1
  in let (_,tau2) = typeExtent environment extent2
  in 
    if tau1 < tau2
    then LT
    else GT

sortList environment (ExtentList extents)
  = List.sortBy (extentOrder environment) extents 

typeExtents :: Environment -> [ExtentInstruction] -> (Environment, [Type])
typeExtents environment [] = (environment, [])  
typeExtents environment (thisExtent:more) 
  = let (newEnvironment, types) = typeExtents environment more
  in let (thisEnvironment, thisType) = typeExtent newEnvironment thisExtent
  in (thisEnvironment, [thisType]++ types)
   
combineInners :: [Type] -> [Type]
combineInners [] = []
combineInners ((Bagof (Tupleof innerTypes)):more) =
   innerTypes ++ combineInners more

typeExtentList :: Environment -> ExtentList -> (Environment, Type)
typeExtentList environment extentList 
  = let extents = sortList environment extentList
  in let (newEnvironment, types) = typeExtents environment extents
  in (newEnvironment, combineTypes types)
\end{code}
Rule \ref{rule_SingleExtentType}

\begin{code}
combineTypes :: [Type] -> Type
combineTypes types
  | length types == 1 = head types
\end{code}
  
Rule: \ref{rule_ProdRelType}
\begin{code}
combineTypes types
  | all isRelation types
  = let innerTypes = combineInners types
  in Bagof(Tupleof(innerTypes))

\end{code}
Rule \ref{rule_ProdWinType}
\begin{code}
combineTypes types
  | all isWindow types
  = let innerTypes = combineInners types
  in Streamof (Tupleof ([Int] ++ [Bagof (Tupleof innerTypes)]))
\end{code}
Rule \ref{rule_ProdRelStreamType} 
\begin{code}
combineTypes types
  | (isStream (head types)) && (all isRelation (tail types))
  = let innerTypes = combineInners types
  in Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof innerTypes]))
\end{code}
Rule \ref{rule_ProdRelWin} 
\begin{code}
combineTypes types
  | (any isWindow types) && (any isRelation types)
  = let innerTypes = combineInners types
  in Streamof (Tupleof ([Int] ++ [Bagof (Tupleof innerTypes)]))
combineTypes types 
 = error ("Unable to combineType: "++(show types))
\end{code}
\subsection{Typing the Projected Attributes in a Query}

Rule: \ref {rule_TupleAttributeType}
\begin{code}
nameIn :: AttributeName -> [AttributeName] -> Bool
nameIn _ [] = False
nameIn check (this:more) 
    | this == check = True
    | otherwise = nameIn check more

withLocalName :: Environment -> AttributeName -> LocalName -> Bool
withLocalName environment attributeName localName
   = let boundName = getBoundName environment localName
   in let (DeclExt _ _ attributeNames) = getDeclExtsByName environment boundName
   in nameIn attributeName attributeNames

qualifyExpression :: Environment -> Expression -> Expression
qualifyExpression environment attribute@(Attribute _ _ )
   = attribute
qualifyExpression environment (UnqualifiedAttribute attributeName)
   = let localNames = getLocalNames environment
   in let goodLocalNames = filter (withLocalName environment attributeName) localNames
   in if (length goodLocalNames == 1)
     then Attribute (head goodLocalNames) attributeName
     else 
       if (length goodLocalNames == 0)
       then error ((show attributeName)++" not in an extent used in FROM"++(show environment))
       else error ((show attributeName)++" in more than one extent used in FROM"++(show environment))
qualifyExpression environment (Add expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Add newExp1 newExp2
qualifyExpression environment (Minus expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Minus newExp1 newExp2
qualifyExpression environment (Multiply expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Multiply newExp1 newExp2
qualifyExpression environment (Divide expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Divide newExp1 newExp2
qualifyExpression environment (Power expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Power newExp1 newExp2
qualifyExpression environment (Negate expression)
   = let newExp = qualifyExpression environment expression
   in Negate newExp
qualifyExpression environment (SquareRoot expression)
   = let newExp = qualifyExpression environment expression
   in SquareRoot newExp
qualifyExpression environment (Concat expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Concat newExp1 newExp2
qualifyExpression environment (Avg collection)
   = let newCol = qualifyCollection environment collection
   in Avg newCol
qualifyExpression environment (Count collection)
   = let newCol = qualifyCollection environment collection
   in Count newCol
qualifyExpression environment (Max collection)
   = let newCol = qualifyCollection environment collection
   in Max newCol
qualifyExpression environment (Min collection)
   = let newCol = qualifyCollection environment collection
   in Min newCol
qualifyExpression environment (Sum collection)
   = let newCol = qualifyCollection environment collection
   in Sum newCol
qualifyExpression environment expression@(IntLit _)
   = expression
qualifyExpression environment expression@(FloatLit _)
   = expression
qualifyExpression environment expression@(StringLit _)
   = expression
qualifyExpression environment expression
   = error ("Unprogrammed expression in qualifyExpression "++show(expression))

qualifyCollection environment (Collection expression)
   = let newExp = qualifyExpression environment expression
   in Collection newExp

qualifyBoolean environment (Equals expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in Equals newExp1 newExp2
qualifyBoolean environment (NotEquals expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in NotEquals newExp1 newExp2
qualifyBoolean environment (GreaterThan expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in GreaterThan newExp1 newExp2
qualifyBoolean environment (LessThan expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in LessThan newExp1 newExp2
qualifyBoolean environment (GreaterThanOrEquals expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in GreaterThanOrEquals newExp1 newExp2
qualifyBoolean environment (LessThanOrEquals expression1 expression2)
   = let newExp1 = qualifyExpression environment expression1
   in let newExp2 = qualifyExpression environment expression2
   in LessThanOrEquals newExp1 newExp2
qualifyBoolean environment (And expressions)
   = let newExps = map (qualifyBoolean environment) expressions
   in And newExps
qualifyBoolean environment (Or expressions)
   = let newExps = map (qualifyBoolean environment) expressions
   in Or newExps
qualifyBoolean environment (Not expression)
   = let newExp = (qualifyBoolean environment) expression
   in Not newExp
qualifyBoolean _ TRUE
   = TRUE
qualifyBoolean _ FALSE
   = FALSE
  
typeExpression :: Environment -> Expression -> Type
\end{code}

Rule \ref{rule_AttributeInGroupType}
\begin{code}
typeExpression environment attribute@(Attribute localName attributeName)
   | hasDeclGroupBy environment
   = let tau = getTypeByAttributeName environment attributeName
   in let boundName = getBoundName environment localName
   in let (DeclExt _ _ attributeNames) = getDeclExtsByName environment boundName
   in let (DeclGroupBy groupByAttributes) = getDeclGroupBy environment
   in 
     if nameIn attributeName attributeNames
     then 
        if elem attribute groupByAttributes
        then tau 
        else error ((show attribute)++" not found in GroupByList:"++(show groupByAttributes))
     else error ((show attributeName)++" not found in "++(show localName)++" bound to "++(show boundName)++" in "++(show environment))
\end{code}

Rule \ref{rule_AttributeType}
\begin{code}
typeExpression environment (Attribute localName attributeName)
   | noDeclGroupBy environment
   = let tau = getTypeByAttributeName environment attributeName
   in let boundName = getBoundName environment localName
   in let (DeclExt _ _ attributeNames) = getDeclExtsByName environment boundName
   in 
     if nameIn attributeName attributeNames
     then tau
     else error ((show attributeName)++" not found in "++(show localName)++" bound to "++(show boundName)++" in "++(show environment))
typeExpression environment attribute@(UnqualifiedAttribute _)
   = let qualifiedAttribute = qualifyExpression environment attribute
   in typeExpression environment qualifiedAttribute
\end{code}
Rule: \ref{rule_AddType}  
\begin{code}
typeExpression environment (Add expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Int     
typeExpression environment (Add expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Minus expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Int     
typeExpression environment (Minus expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Multiply expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Int     
typeExpression environment (Multiply expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Divide expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Int     
typeExpression environment (Divide expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Power expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Int     
  | (Float == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float
\end{code}   

Rule: \ref{rule_AddMixType}
\begin{code}
typeExpression environment (Add expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Add expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Float     
typeExpression environment (Minus expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
typeExpression environment (Minus expression1 expression2)
  | (Float == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Float     
typeExpression environment (Multiply expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
  | (Float == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Float     
typeExpression environment (Divide expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float     
  | (Float == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Float     
typeExpression environment (Power expression1 expression2)
  | (Int == typeExpression environment expression1)
     && (Float == typeExpression environment expression1) 
  = Float   
  | (Float == typeExpression environment expression1)
     && (Int == typeExpression environment expression1) 
  = Float     
\end{code}   

Rule: \ref{TODO}
\begin{code}
typeExpression environment (SquareRoot expression1)
  | (Int == typeExpression environment expression1)
  = Float
  | (Float == typeExpression environment expression1)
  = Float 
  | otherwise 
  = error ("Illegal expression in SquareRoot " ++ (show expression1) )
\end{code}  

Rule: \ref{rule_NegateType}
\begin{code}
typeExpression environment (Negate expression1)
  | Int == typeExpression environment expression1
  = Int     
  | Float == typeExpression environment expression1
  = Float     
\end{code}
These rules had to be placed here as they are Expressions.

Rule: \ref{rule_IntType} 
\begin{code} 
typeExpression environment (IntLit _) = Int
\end{code} 

Rule: \ref{rule_FloatType} 
\begin{code} 
typeExpression environment (FloatLit _) = Float
\end{code} 

Rule: \ref{rule_StringType} 
\begin{code} 
typeExpression environment (StringLit _) = String
\end{code} 

Rule: \ref{rule_ConcatType} TODO including Grammar

\subsection{Typing Aggregates}
Rule: \ref{rule_CollectionType} is lower down due to a Haskell Restriction.

Rule: \ref{rule_CountType} TODO
\begin{code}
typeExpression environment (Count collection) 
  = Int
\end{code}  

Rule: \ref{rule_AvgType} TODO
\begin{code}
typeExpression environment (Avg collection) 
   = let tau = typeCollection environment collection
   in 
     if isBagofNumeric tau
     then Int
     else error ("Query Error can not do Max over type "++(show tau))
\end{code}

Rule: \ref{rule_MinIntType} 
\begin{code}
typeExpression environment (Max collection) 
   = let tau = typeCollection environment collection
   in 
     if isBagofNumeric tau
     then 
       let (Bagof tau1) = tau
       in tau1
     else error ("Query Error can not do Max over type "++(show tau))
typeExpression environment (Min collection) 
   = let tau = typeCollection environment collection
   in 
     if isBagofNumeric tau
     then 
       let (Bagof tau1) = tau
       in tau1
     else error ("Query Error can not do Min over type "++(show tau))
typeExpression environment (Sum collection) 
   = let tau = typeCollection environment collection
   in 
     if isBagofNumeric tau
     then 
       let (Bagof tau1) = tau
       in tau1
     else error ("Query Error can not do Sum over type "++(show tau))
typeExpression _ aggregation = error ("Expression "++(show aggregation)++" not yet programmed in typeExpression")

isBagofNumeric :: Type -> Bool
isBagofNumeric (Bagof Int) = True
isBagofNumeric (Bagof Float) = True
isBagofNumeric _ = False

containsName :: AttributeName -> DeclExt -> Bool
containsName name (DeclExt _ _ attributeNames) 
    = nameIn name attributeNames

\end{code}

Rule: \ref{rule_CollectionType}
\begin{code}
typeCollection :: Environment -> Collection -> Type
typeCollection environment (Collection expression)
   = let tau = typeExpression environment expression
   in (Bagof tau)
\end{code}

\subsection{Typing Predicate Expressions}
Rule: \ref{rule_Equals}

\begin{code}
typePredicate :: Environment -> BooleanExpression -> Type
typePredicate environment (Equals expression1 expression2)
  | (typeExpression environment expression1) == (typeExpression environment expression2)
  = Boolean
typePredicate environment (NotEquals expression1 expression2)
  | (typeExpression environment expression1) == (typeExpression environment expression2)
  = Boolean
\end{code}
Rule: \ref{rule_compareType} 
\begin{code}       
typePredicate environment (GreaterThan expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Int
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Float
  = Boolean
typePredicate environment (GreaterThanOrEquals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Int
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Float
  = Boolean
typePredicate environment (LessThan expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Int
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Float
  = Boolean
typePredicate environment (LessThanOrEquals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Int
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Float
  = Boolean
\end{code}

Rule \ref{rule_castCompareType}

\begin{code}       
typePredicate environment (Equals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
typePredicate environment (NotEquals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
typePredicate environment (GreaterThan expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
typePredicate environment (GreaterThanOrEquals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
typePredicate environment (LessThan expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
typePredicate environment (LessThanOrEquals expression1 expression2)
  | (typeExpression environment expression1) == Int && (typeExpression environment expression2) == Float
  = Boolean
  | (typeExpression environment expression1) == Float && (typeExpression environment expression2) == Int
  = Boolean
\end{code}

\begin{code}
typePredicate environment (Equals expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
typePredicate environment (NotEquals expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
typePredicate environment (GreaterThan expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
typePredicate environment (GreaterThanOrEquals expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
typePredicate environment (LessThan expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
typePredicate environment (LessThanOrEquals expression1 expression2)
  = error ("Query Error: Equals not allowed in "++(show expression1)++"="++(show expression2))
\end{code}

Rule: \ref{rule_AndType}

\begin{code}
typePredicate environment (And expressions)
  = let types = map (typePredicate environment) expressions
  in 
    if all isBoolean types
    then Boolean
    else error ("Query error: Non Boolean types in And")
    
typePredicate environment (Or expressions)
  = let types = map (typePredicate environment) expressions
  in 
    if all isBoolean types
    then Boolean
    else error ("Query error: Non Boolean types in Or")

\end{code}

Rule: \ref{rule_NotType} 

\begin{code}
typePredicate environment (Not expression)
  = let tau = typePredicate environment expression
  in 
    if isBoolean tau
    then Boolean
    else error ("Query error: Non Boolean types in Not")

typePredicate environment TRUE = Boolean

typePredicate environment FALSE = Boolean

typePredicate environment pred
  = error ("Program error in typePredicate. Unexpected predicate: "++(show pred))
  
isNumeric :: Type -> Bool
isNumeric Int = True
isNumeric Float = True
isNumeric _ = False

isBoolean :: Type -> Bool
isBoolean Boolean = True
isBoolean _ = False   
\end{code}

\subsection{Typing the Group By Clause}

Rule \ref{rule_GroupByType}

\begin{code}
typeGroupByList :: Environment -> GroupByList -> (Environment, Type)
typeGroupByList environment (GroupByList attributes) 
  = let qualifiedAttributes = map (qualifyExpression environment) attributes
  in let types = map (typeExpression environment) qualifiedAttributes
  in let newEnvironment = setDeclGroupBy environment (DeclGroupBy qualifiedAttributes)
  in (newEnvironment, Tupleof types)
\end{code}