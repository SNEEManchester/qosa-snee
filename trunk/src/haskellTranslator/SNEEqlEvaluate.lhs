\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlEvaluate

where

import List
import IO
import SNEEqlAst
import SNEEqlEnvironment
import SNEEqlOperators
import SNEEqlTypes
import SNEEqlTypeRules
\end{code}
\end{comment}

\section {Evaluate Methods in Haskell}

\subsection{Data Representation}
\begin{code}

data Data 
  = NullData
  | BagOf [Tuple]
  | StreamOf [Data]
  | TaggedTuple Int Int Tuple
  | Tuple ([Attribute], [RawData]) 
  | Window Int BagOfTuples
  | Group Tuple [Tuple]
  | Groups [Group]
  | WinGroups Int [Group]
              deriving (Eq, Ord, Show, Read)

type TaggedTuple = Data -- TaggedTuple

type BagOfTuples = Data -- Bagof [Tuple]

type Tuple = Data -- Tuple ([Attribute], [RawData]) 

type Window = Data -- Window Int BagOfTuples

type Group = Data -- Group

type Groups = Data -- Groups

type WinGroups = Data -- WinGroups Int [Group]

data Date = Date Int Int Int
              deriving (Eq, Ord, Show, Read)

data Time = Time Int Int
              deriving (Eq, Ord, Show, Read)

data RawData 
  = S String
  | I Int
  | F Float
  | D Date
  | T Time
  | Undef
              deriving (Eq, Ord, Show, Read)
\end{code}

\subsection{Display Fnctions}
\begin{code}
displaySome (StreamOf tuples) count 
  = (displayWithHead (StreamOf (take count tuples)))
displaySome a _ 
  = (displayWithHead a)

displayWithHead (NullData) 
  = "Unprogrammed"
displayWithHead (BagOf []) 
  = displayData (BagOf [])
displayWithHead (BagOf tuples) 
  = (show (head tuples)) ++ displayData (BagOf tuples)
displayWithHead (StreamOf values) 
  = (displayWithHead (head values)) ++ displayData (StreamOf values)
displayWithHead (TaggedTuple tick index tuple) 
  = "TaggedTuple Tick Index "++(show tuple)
displayWithHead window@(Window _ (BagOf []))
  = "Window tick first bagof of tuples empty"++ (displayData window)
displayWithHead window@(Window _ (BagOf tuples))
  = "Window tick "++ (show (head tuples)) ++ (displayData window)
displayWithHead group@(Group groupTuple []) 
  = "groupTuple empty"
displayWithHead group@(Group groupTuple tuples) 
  = "groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData group)
displayWithHead groups@(Groups ((Group groupTuple tuples):more)) 
  = "groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData groups)
displayWithHead winGroups@(WinGroups tick ((Group groupTuple tuples):more)) 
  = "WinGroup groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData winGroups)

displayData (BagOf []) 
  = "Bagof []" 
displayData (BagOf tuples) 
  = "Bagof ["++(displayData (head tuples)) ++ "," ++ (foldr (commaConcat) "]" (map displayData (tail tuples)))
displayData (StreamOf values)  
  = "StreamOf ["++(displayData (head values))++ ","++(foldr (commaConcat) ".." (map displayData (tail values)))
displayData (TaggedTuple tick index tuple) 
  = "("++(show tick) ++","++(show index)++ ","++ (displayData tuple) ++ ")"
displayData (Window tick bagOfTuples) 
  = "Window (" ++(show tick) ++","++ (displayData bagOfTuples) ++ ")"
displayData (Group groupTuple tuples) 
  = "GroupOf ("++(displayData groupTuple) ++", ["++(displayData (head tuples)) ++ "," ++ (foldr (commaConcat) "]" (map displayData (tail tuples)))
displayData (Groups groups) 
  = "Groups ["++(displayData (head groups))++ ","++(foldr (commaConcat) ".." (map displayData (tail groups)))
displayData (WinGroups tick groups) 
  = "WinGroups at "++(show tick) ++ " ["++(displayData (head groups))++ ","++(foldr (commaConcat) ".." (map displayData (tail groups)))
displayData (Tuple (_ , rawData))
  = (foldl (commaConcat) ("["++(display (head rawData))) (map display (tail rawData))) ++ "]"
  
display (S text) = text
display (I val) = show val
display (F val) = show val
display (D (Date day month year)) = (show day) ++ "/" ++ (show month) ++ "/" ++ (show year)
display (T (Time hour min)) = (show hour) ++ ":" ++ (show min)
display Undef = "Undef"

commaConcat a b 
  = a ++ "," ++ b
\end{code}

\subsection{Main Evaluation} 
\begin{code}
   -- Relational operators
evaluate :: Environment -> Operator -> IO Data
evaluate environment (OneOffScan extentName localName attributeNames)
  = do 
      output <- readTable environment extentName localName 
      return output  
evaluate environment (RelProject expressions attributeNames relationalOperator)
  = do 
      BagOf input <- evaluate environment relationalOperator
      let output = map (projectsOnTuple expressions attributeNames) input
      return (BagOf output)

evaluate environment (RelCrossProduct leftOperator rightOperator)
   = do
      left <- evaluate environment leftOperator
      right <- evaluate environment rightOperator
      return (crossProduct left right)

evaluate environment (RelSelect booleanExpression relationalOperator)
  = do 
      BagOf input <- evaluate environment relationalOperator
      let output = filter (select booleanExpression) input
      return (BagOf output)

evaluate environment (RelAggregation expressions attributeNames relationalOperator)
  = do 
      input <- evaluate environment relationalOperator
      let output = aggregationsOnTuples expressions attributeNames input
      return output

evaluate environment (RelRename localName relationalOperator)
  = do
      BagOf input <- evaluate environment relationalOperator
      let output = map (renameTuple localName) input
      return (BagOf output)

evaluate environment (RelUngroup expressions attributeNames relGrOperator)
  = do
      input <- evaluate environment relGrOperator 
      let output = projectsOnGroups expressions attributeNames input
      return (BagOf output)
   {- Relational Grouped Operators -}

evaluate environment (RelGroupBy attributes relationalOperator)
  = do
      input <- evaluate environment relationalOperator
      return (doGroupBy attributes input)

evaluate environment (RelGrSelect booleanExpression relGrOperator)
  = do
      Groups input <- evaluate environment relGrOperator
      let output = filter (select booleanExpression) input
      return (Groups output)
   {- Tuple Stream Operators -}

evaluate environment (StrSelect booleanExpression streamOperator)
  = do
      StreamOf input <- evaluate environment streamOperator
      let output = filter (selectOnTaggedTuple booleanExpression) input
      return (StreamOf output)

evaluate environment (StrProject expressions attributeNames streamOperator)
  = do
      StreamOf input <- evaluate environment streamOperator
      let output = map (projectsOnTaggedTuple expressions attributeNames) input
      return (StreamOf output)

evaluate environment (StrAcquire extentName localName attributeNames)
  = do
      output <- (simulateStrAcquire environment extentName localName attributeNames) 
      return output

evaluate environment (StrReceive extentName localName attributeNames)
  = do
      return (simulateStrReceive environment extentName localName attributeNames)

evaluate environment (StrRelCrossProduct streamOperator relationalOperator)
   = do
      left <- evaluate environment streamOperator
      right <- evaluate environment relationalOperator
      return (crossProduct left right)

evaluate environment (RStreamOp windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = doRStream input
      return (StreamOf output) 	

evaluate environment (IStreamOp windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = doIStream input
      return (StreamOf output)

evaluate environment (DStreamOp windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = doDStream input
      return (StreamOf output)

evaluate environment (StrRename localName streamOperator)
  = do 
      StreamOf input <- evaluate environment streamOperator
      let output = map (renameTaggedTuple localName) input
      return (StreamOf output)

   -- WindowOperator = 
evaluate environment (TimeWindow windowScopeDef tick streamOperator)
  = do
      StreamOf input <- evaluate environment streamOperator
      let output = createTimeWindows windowScopeDef tick input
      return (StreamOf output)
  
evaluate environment (RowWindow windowScopeDef index streamOperator)
  = do
      StreamOf input <- evaluate environment streamOperator
      let output = createRowWindows windowScopeDef index input
      return (StreamOf output)
  
evaluate environment (InputWindow windowScopeDef streamOperator)
  = do 
      StreamOf input <- evaluate environment streamOperator
      let output = createInputWindows windowScopeDef input
      return (StreamOf output)
  
evaluate environment (RepeatedScan tick extentName localName attributeNames)
  = error "Repeated Scan not finished"
  
evaluate environment (WinProject expressions attributeNames windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = map (projectsOnWindow expressions attributeNames) input
      return (StreamOf output)
  
evaluate environment (WinCrossProduct leftWindowOperator rightWindowOperator)
  = do
      StreamOf left <- evaluate environment leftWindowOperator
      StreamOf right <- evaluate environment rightWindowOperator
      return (StreamOf (windowsCrossProduct left right))
  
evaluate environment (WinRelCrossProduct windowOperator relationalOperator)
  = do
      StreamOf left@((Window _ _):_) <- evaluate environment windowOperator
      right@(BagOf _) <- evaluate environment relationalOperator
      let output = map (crossProduct right) left
      return (StreamOf output)
  
evaluate environment (WinSelect booleanExpression windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = map (selectOnWindow booleanExpression) input
      return (StreamOf output)
  
evaluate environment (WinAggregation expressions attributeNames windowOperator)
  = do
      StreamOf input <- evaluate environment windowOperator
      let output = map (aggregationsOnWindow expressions attributeNames) input
      return (StreamOf output)
  
evaluate environment (WinRename localName windowOperator)
  = do StreamOf input <- evaluate environment windowOperator
       let output = map (renameWindow localName) input
       return (StreamOf output)

evaluate environment (WinUngroup expressions attributeNames winGrOperator)
  = do StreamOf input <- evaluate environment winGrOperator 
       let output = map (projectsOnWinGroups expressions attributeNames) input
       return (StreamOf output)
  
   {- Relational Grouped Operators -}
evaluate environment (WinGroupBy attributes windowOperator)
  = do input <- evaluate environment windowOperator
       return (doGroupBy attributes input)

evaluate environment (WinGrSelect booleanExpression winGrOperator)
  = do StreamOf input <- evaluate environment winGrOperator
       let output = map (selectOnWinGroups booleanExpression) input
       return (StreamOf output)

evaluate environment (Deliver inputOperator)
  = evaluate environment inputOperator

evaluate environment op
  = error ("Missing code : evaluate" ++ (show op))

\end{code}

\subsection{Window Operators}
\begin{code}
createTimeWindows :: WindowScopeDef -> Int -> [TaggedTuple] -> [BagOfTuples]
createTimeWindows windowScopeDef tick input
  = createTimeWindow windowScopeDef tick 0 input

createTimeWindow :: WindowScopeDef -> Int -> Int -> [TaggedTuple] -> [Data]
createTimeWindow windowScopeDef slide now taggedTuples
  = let input = takeWhile (lessEqualsTick now) taggedTuples
  in let (TaggedTuple lastTick lastIndex lastTuple) = last input
  in [Window now (getWindowTuples windowScopeDef now lastIndex input)]
     ++ createTimeWindow windowScopeDef slide (now+slide) taggedTuples
  
createRowWindows :: WindowScopeDef -> Int -> [TaggedTuple] -> [BagOfTuples]
createRowWindows windowScopeDef index input
  = createRowWindow windowScopeDef index index input

createRowWindow :: WindowScopeDef -> Int -> Int -> [TaggedTuple] -> [Data]
createRowWindow windowScopeDef slide index taggedTuples
  = let input = take index taggedTuples
  in let (TaggedTuple lastTick lastIndex lastTuple) = last input
  in [Window (lastTick) (getWindowTuples windowScopeDef lastTick index input)]
     ++ createRowWindow windowScopeDef slide (index+slide) taggedTuples

createInputWindows :: WindowScopeDef -> [TaggedTuple] -> [BagOfTuples]
createInputWindows windowScopeDef input
  = let (TaggedTuple firstTick firstIndex lastTuple) = head input
  in createInputWindow windowScopeDef firstTick input

createInputWindow :: WindowScopeDef -> Int -> [TaggedTuple] -> [Data]
createInputWindow windowScopeDef now taggedTuples
  = let input = takeWhile (lessEqualsTick now) taggedTuples
  in let (TaggedTuple lastTick lastIndex lastTuple) = last input
  in let (TaggedTuple nextTick nextIndex nextTuple) = head (dropWhile (lessEqualsTick now) taggedTuples)
  in [Window now (getWindowTuples windowScopeDef now lastIndex input)]
     ++ createInputWindow windowScopeDef nextTick taggedTuples

getWindowTuples::WindowScopeDef->Tick->Index->[TaggedTuple]->BagOfTuples
getWindowTuples _ _ _ [] = BagOf []
getWindowTuples windowScope@(RowScopeDef from to)now currentIndex input= 
  let passedFrom = dropWhile (lessThanIndex (currentIndex - from))input
  in let output = filter (lessEqualsIndex(currentIndex - to))passedFrom
  in BagOf (map stripTags output)
getWindowTuples windowScope@(TimeScopeDef from to)now lastIndex input
  = let passedFrom = dropWhile (lessThanTick (now - from)) input
  in let window = filter (lessEqualsTick (now - to)) passedFrom
  in BagOf (map stripTags window)

stripTags :: TaggedTuple-> Tuple
stripTags (TaggedTuple _ _ tuple) = tuple

lessEqualsTick :: Int -> Data -> Bool
-- lessEqualsTick :: Int -> TaggedTuple -> Bool
-- or lessEqualsTick :: Int -> WindowTuple -> Bool
lessEqualsTick now (TaggedTuple tick _ _ )
  | tick <= now = True
  | otherwise = False
lessEqualsTick now (Window tick _ )
  | tick <= now = True
  | otherwise = False
lessEqualsTick x y = error ("Pattern match failure in lessEqualsTick "++(show x)++"@"++(show y))
   
lessThanTick :: Int -> TaggedTuple -> Bool
lessThanTick now (TaggedTuple tick _ _ )
  | tick < now = True
  | otherwise = False
lessThanTick x y = error ("Pattern match failure in lessThanTick "++(show x)++"@"++(show y))

lessEqualsIndex :: Int -> TaggedTuple -> Bool
lessEqualsIndex now (TaggedTuple _ index _ )
  | index <= now = True
  | otherwise = False

lessThanIndex :: Int -> TaggedTuple -> Bool
lessThanIndex now (TaggedTuple _ index _ )
  | index < now = True
  | otherwise = False

\end{code}

\subsection{D/I/RStream}

\begin{code}
doRStream:: [Window] -> [TaggedTuple]
doRStream input 
  = toRStream 1 input
  
toRStream :: Int -> [Window] -> [TaggedTuple]
toRStream index ((Window tick (BagOf tuples)):windows) = 
  (append  tuples tick index) ++ 
      toRStream (index + (length tuples)) windows 

doIStream:: [Window] -> [TaggedTuple]
doIStream input 
  = toIStream 1 [] input

toIStream :: Int-> [Tuple] -> [Window] -> [TaggedTuple]
toIStream index previousTuples ((Window tick (BagOf tuples)):windows) =
  let insertTuples = tuples \\ previousTuples
  in (append  insertTuples tick index) ++ 
      toIStream (index + (length insertTuples)) tuples windows 

doDStream:: [Window] -> [TaggedTuple]
doDStream input 
  = toDStream 1 [] input

toDStream :: Int -> [Tuple] -> [Window] -> [TaggedTuple]
toDStream index previousTuples ((Window tick (BagOf tuples)):windows) =
 let deleteTuples = previousTuples \\ tuples 
 in (append deleteTuples tick index) ++ 
      toDStream (index + (length deleteTuples)) tuples windows 

append :: [Tuple] -> Int -> Int -> [TaggedTuple]
append [] _ _ = []
append (tuple:tuples) tick index = 
  [TaggedTuple tick index tuple]++(append tuples tick (index+1))
\end{code}


\subsection{Projection}
\begin{code}
projectsOnTaggedTuple :: [Expression] -> [AttributeName] -> TaggedTuple -> TaggedTuple
projectsOnTaggedTuple expressions attributeNames (TaggedTuple tick index input) 
  = TaggedTuple tick index (projectsOnTuple expressions attributeNames input)

projectsOnWindow :: [Expression] -> [AttributeName] -> Window -> Window
projectsOnWindow expressions attributeNames (Window tick (BagOf input))
  = let output = map (projectsOnTuple expressions attributeNames) input
  in Window tick (BagOf output)

projectsOnTuple :: [Expression] -> [AttributeName] -> Tuple -> Tuple
projectsOnTuple expressions attributeNames input
  = let attributes = map (convertToAttribute (LocalName "")) attributeNames
  in projectsOnTuple2 expressions attributes input

projectsOnTuple2 :: [Expression] -> [Attribute] -> Tuple -> Tuple
projectsOnTuple2 expressions attributes input
  = let outputData = map (project input) expressions
  in Tuple (attributes, outputData)

project :: Data -> Expression -> RawData
project (Tuple ([], [])) attribute 
   = error ("Unexpected Error:"++ (show attribute) ++" not found by projectOnTuple") 

project (Tuple ((name:names),(value:values))) attribute@(Attribute _ _)
  | name == attribute = value
  | otherwise = project (Tuple (names,values)) attribute

project input (Add leftExp rightExp)  
  = let left = project input leftExp
  in let right = project input rightExp
  in add left right

project input (Minus leftExp rightExp)  
  = let left = project input leftExp
  in let right = project input rightExp
  in minus left right

project input (Multiply leftExp rightExp)  
  = let left = project input leftExp
  in let right = project input rightExp
  in multiply left right

project input (Divide leftExp rightExp)  
  = let left = project input leftExp
  in let right = project input rightExp
  in divide left right

project input (Power leftExp rightExp)  
  = let left = project input leftExp
  in let right = project input rightExp
  in power left right

project input (Negate expression)  
  = let value = project input expression
  in doNegate value

project input (SquareRoot expression)  
  = let value = project input expression
  in doSquareRoot value

project _  (IntLit val) = I val

project _  (FloatLit val) = F val

--  | Concat    Expression Expression

project _  (StringLit val) = S val

project (Tuple (((Attribute _ name):names),(value:values))) a@(UnqualifiedAttribute attribute)
  | name == attribute = value
  | otherwise = project (Tuple(names,values)) a

project (BagOf tuples) (Avg (Collection expression))  
  = let values = map (invertProject expression) tuples
  in avg values
  
project (BagOf tuples) (Count (Collection expression))  
  = let values = map (invertProject expression) tuples
  in count values

project (BagOf tuples) (Max (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doMax values

project (BagOf tuples) (Min (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doMin values

project (BagOf tuples) (Sum (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doSum values

project (Group groupTuple tuples) attribute@(Attribute _ _)
  = project groupTuple attribute

project (Group groupTuple tuples) attribute@(UnqualifiedAttribute _)
  = project groupTuple attribute

project (Group groupTuple tuples) (Avg (Collection expression))  
  = let values = map (invertProject expression) tuples
  in avg values
  
project (Group groupTuple tuples) (Count (Collection expression))  
  = let values = map (invertProject expression) tuples
  in count values

project (Group groupTuple tuples) (Max (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doMax values

project (Group groupTuple tuples) (Min (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doMin values

project (Group groupTuple tuples) (Sum (Collection expression))  
  = let values = map (invertProject expression) tuples
  in doSum values

project _ exp
  = error ("Missing code : project " ++ (show exp))
  
add :: RawData -> RawData -> RawData
add (F left) (I right) 
  = F (left + (intToFloat right))
add (I left) (F right) 
  = F ((intToFloat left) + right)
add (I left) (I right)  
  = I (left + right)
add (F left) (F right)  
  = F (left + right)
add Undef _ = Undef  
add _ Undef = Undef  

minus :: RawData -> RawData -> RawData
minus (F left) (I right) 
  = F (left - (intToFloat right))
minus (I left) (F right) 
  = F ((intToFloat left) - right)
minus (I left) (I right)  
  = I (left - right)
minus (F left) (F right)  
  = F (left - right)
minus Undef _ = Undef  
minus _ Undef = Undef  

multiply :: RawData -> RawData -> RawData
multiply (F left) (I right) 
  = F (left * (intToFloat right))
multiply (I left) (F right) 
  = F ((intToFloat left) * right)
multiply (I left) (I right)  
  = I (left * right)
multiply (F left) (F right)  
  = F (left * right)
multiply Undef _ = Undef  
multiply _ Undef = Undef  

divide :: RawData -> RawData -> RawData
divide (F left) (I right) 
  = F (left / (intToFloat right))
divide (I left) (F right) 
  = F ((intToFloat left) / right)
divide (I left) (I right)  
  = I (div left right)
divide (F left) (F right)  
  = F (left / right)
divide Undef _ = Undef  
divide _ Undef = Undef  

power :: RawData -> RawData -> RawData
power (F left) (I right) 
  = F (left ^ right)
power (I left) (I right)  
  = I (left ^ right)
power _ (F right)  
  = error "X ^ Float unsupported in Haskell. Change power method if required."
power Undef _ = Undef  
power _ Undef = Undef  

doNegate (I val) = I (-val)
doNegate (F val) = F (-val)
doNegate Undef = Undef  

doSquareRoot (I val) = F (sqrt(fromIntegral val))
doSquareRoot (F val) = F (sqrt(val))
doSquareRoot Undef = Undef  
\end{code}

\subsection{Aggregation}
\begin{code}
aggregationsOnWindow :: [Expression] -> [AttributeName] -> Window -> Window
aggregationsOnWindow expressions attributeNames (Window tick input)
  = let output = aggregationsOnTuples expressions attributeNames input
  in Window tick output

aggregationsOnTuples :: [Expression] -> [AttributeName] -> BagOfTuples -> BagOfTuples
aggregationsOnTuples expressions attributeNames input
  = let attributes = map (convertToAttribute (LocalName "@main@")) attributeNames
  in let outputData = map (project input) expressions
  in BagOf ([Tuple (attributes, outputData)])

invertProject expression tuple 
  = project tuple expression

avg [] = Undef
avg vals@((I _):more)
  = let I sumV = doSum vals
  in let I countV = count vals
  in F ((intToFloat sumV) / (intToFloat countV))
  
count vals
  = I (length vals)

doSum (Undef:_) = Undef
doSum (x:[]) = x
doSum (x:more) = add x (doSum more)
  
doMin (Undef:_) = Undef
doMin (x:[]) = x
doMin (x:more) 
  | (doMin more) == Undef = Undef
  | x <= (doMin more) = x
  | otherwise = doMin more
  
doMax (Undef:_) = Undef
doMax (x:[]) = x
doMax (x:more) 
  | (doMax more) == Undef = Undef
  | x >= (doMin more) = x
  | otherwise = doMin more
\end{code}

\subsection{Group Projection}
\begin{code}
projectsOnGroups :: [Expression] -> [AttributeName] -> Groups -> [Tuple]
projectsOnGroups expressions attributeNames (Groups input)
  = let output = (map (projectsOnGroup expressions attributeNames) input)
  in output

projectsOnGroup :: [Expression] -> [AttributeName] -> Group -> Tuple
projectsOnGroup expressions attributeNames input
  = let attributes = map (convertToAttribute (LocalName "@main@")) attributeNames
  in let outputData = map (project input) expressions
  in Tuple (attributes, outputData)
  
projectsOnWinGroups :: [Expression] -> [AttributeName] -> WinGroups-> Window
projectsOnWinGroups expressions attributeNames (WinGroups tick groups)
  = let tuples = map (projectsOnGroup expressions attributeNames) groups
  in Window tick (BagOf tuples)
\end{code}

\subsection{Selection}
\begin{code}

selectOnTaggedTuple :: BooleanExpression -> TaggedTuple -> Bool
selectOnTaggedTuple boolExp (TaggedTuple _ _ tuple) 
  = select boolExp tuple

selectOnWindow :: BooleanExpression  -> Window -> Window
selectOnWindow boolExp (Window tick (BagOf input))
  = let output = filter (select boolExp) input
  in Window tick (BagOf output)

selectOnWinGroups :: BooleanExpression -> WinGroups -> WinGroups
selectOnWinGroups exp (WinGroups tick groups)
  = let output = filter (select exp) groups
  in WinGroups tick output

select :: BooleanExpression -> Data -> Bool
select (Equals leftExp rightExp) input
  = let left = project input leftExp
  in let right = project input rightExp
  in isEqual left right
  
select (NotEquals leftExp rightExp) input
  = let left = project input leftExp
  in let right = project input rightExp
  in not (isEqual left right)

select (GreaterThan leftExp rightExp)input
  = let left = project input leftExp
  in let right = project input rightExp
  in greaterThan left right

select (LessThan leftExp rightExp) input
  = let left = project input leftExp
  in let right = project input rightExp
  in lessThan left right

select (GreaterThanOrEquals leftExp rightExp) input
  = let left = project input leftExp
  in let right = project input rightExp
  in not (lessThan left right)

select (LessThanOrEquals leftExp rightExp) input
  = let left = project input leftExp
  in let right = project input rightExp
  in not (greaterThan left right)

select (And []) input = True
select (And (exp:more)) input 
  | select exp input = select (And more) input
  | otherwise = False
  
select (Or []) input = False
select (Or (exp:more)) input 
  | select exp input = True
  | otherwise = select (And more) input

select (Not exp) input 
  | select exp input = False
  | otherwise = True

select TRUE _ = True

select FALSE _ = False
  
isEqual :: RawData -> RawData -> Bool
isEqual (F left) (I right) 
  = left == (intToFloat right)
isEqual (I left) (F right) 
  = (intToFloat left) == right
isEqual left right
  = left == right

greaterThan :: RawData -> RawData -> Bool
greaterThan (F left) (I right) 
  = left > (intToFloat right)
greaterThan (I left) (F right) 
  = (intToFloat left) > right
greaterThan left right
  = left > right

lessThan :: RawData -> RawData -> Bool
lessThan (F left) (I right) 
  = left < (intToFloat right)
lessThan (I left) (F right) 
  = (intToFloat left) < right
lessThan left right
  = left < right
\end{code}
\begin{code}
intToFloat :: Int -> Float
intToFloat n = fromInteger (toInteger n)
\end{code}
  
\subsection{Cross Product}
\begin{code}
mapMap f [] _ = []
mapMap f (x:xs) y = 
  map (f x) y ++ mapMap f xs y

crossProduct :: Data -> Data -> Data

crossProduct (BagOf leftTuples) (BagOf rightTuples)
  = BagOf (mapMap concatTuples leftTuples rightTuples)

crossProduct (StreamOf leftData) (BagOf rightTuples)
  = StreamOf (mapMap concatTuples2 leftData rightTuples)

crossProduct (BagOf leftTuples) (Window tick (BagOf rightTuples)) 
  = Window tick (BagOf (mapMap concatTuples leftTuples rightTuples))

crossProduct (Window leftTick leftTuples)
           (Window rightTick rightTuples) = 
  let windowTick = max leftTick rightTick
  in let tuples = crossProduct leftTuples rightTuples
  in Window windowTick tuples

windowsCrossProduct :: [Window] -> [Window] -> [Window]
windowsCrossProduct left right =
  let ticks = getTickUnion left right
  in tickCrossProduct ticks left right

getTickUnion :: [Window]->[Window]->[Int]
getTickUnion left@((Window leftTick leftTuples):moreLeft) 
        right@((Window rightTick rightTuples_):moreRight) 
  | leftTick < rightTick = [leftTick] ++
       getTickUnion(dropWhile(lessEqualsTick leftTick)left)right
  | leftTick > rightTick = [rightTick] ++
       getTickUnion left(dropWhile(lessEqualsTick rightTick)right)
  | otherwise = [leftTick] ++
       getTickUnion (dropWhile (lessEqualsTick leftTick) left) 
         (dropWhile (lessEqualsTick rightTick) right)

tickCrossProduct :: [Int]->[Window]->[Window]->[Window]
tickCrossProduct (tick:ticks) left right =
  let leftTick = findLastTick tick left
  in let rightTick = findLastTick tick right
  in let leftWindows = getWindowsAtTick leftTick left
  in let rightWindows = getWindowsAtTick rightTick right
  in let windowPairs = mapMap crossProduct leftWindows rightWindows
  in windowPairs ++ tickCrossProduct ticks left right 

findLastTick :: Int->[Window]->Int
findLastTick tick 
     ((Window thisTick _):(Window nextTick nextTuple):more)
  | nextTick > tick = thisTick
  | otherwise =findLastTick tick((Window nextTick nextTuple):more)

getWindowsAtTick :: Int->[Window]->[Window]		
getWindowsAtTick tick ((Window thisTick thisTuple):more) 
  | thisTick < tick = getWindowsAtTick tick more
  | thisTick == tick = [Window thisTick thisTuple] 
    ++ getWindowsAtTick tick more
  | otherwise = []

concatTuples :: Tuple->Tuple->Tuple
concatTuples (Tuple (leftNames, leftData)) (Tuple (rightNames, rightData))
  = Tuple ((leftNames ++ rightNames),(leftData ++ rightData))

concatTuples2 :: Data -> Tuple -> Data
concatTuples2 (TaggedTuple tick index (Tuple (leftNames, leftData))) (Tuple (rightNames, rightData))
  = TaggedTuple tick index (Tuple ((leftNames ++ rightNames),(leftData ++ rightData)))

\end{code}

\subsection{Group By}
\begin{code}
doGroupBy :: [Attribute] -> Data -> Data
doGroupBy attributes (BagOf inputTuples)
  = let projects = map (projectsOnTuple2 attributes attributes) inputTuples
  in let groupTuples = removeDuplicates projects
  in let groups = map (getGroup projects inputTuples) groupTuples
  in Groups groups
doGroupBy attributes (Window tick (BagOf inputTuples))
  = let projects = map (projectsOnTuple2 attributes attributes) inputTuples
  in let groupTuples = removeDuplicates projects
  in let groups = map (getGroup projects inputTuples) groupTuples
  in WinGroups tick groups
doGroupBy attributes (StreamOf input)
  = let output = map (doGroupBy attributes) input
  in StreamOf output
  
-- doGroupBy attributes (Window tick (BagOf input))
doGroupBy expression inData 
  = error ("Unexpected input to doGroupBy. Attributes= "++(show expression)) -- ++" data= "++(displaySome inData 4))

removeDuplicates :: [Tuple]->[Tuple]
removeDuplicates [] = []
removeDuplicates (x:xs) = removeDuplicates (filter (< x) xs) ++ [x] ++ removeDuplicates (filter (> x) xs)

getGroup :: [Tuple] -> [Tuple] -> Tuple -> Group
getGroup projects inputTuples groupTuple = 
  let select = selectGroupTuples groupTuple projects inputTuples
  in Group groupTuple select

selectGroupTuples :: Tuple->[Tuple]->[Tuple]->[Tuple]
selectGroupTuples _ [] [] = []
selectGroupTuples groupID (project:projects) (tuple:tuples) 
  | project == groupID = 
      (tuple:(selectGroupTuples groupID projects tuples))
  | otherwise = selectGroupTuples groupID projects tuples

\end{code}

\subsection{Rename}
\begin{code}
renameTuple :: LocalName -> Tuple -> Tuple
renameTuple localName (Tuple (attributes,rawData))
  = Tuple ((map (renameAttribute localName) attributes), rawData)
  
renameTaggedTuple :: LocalName -> TaggedTuple -> TaggedTuple
renameTaggedTuple localName (TaggedTuple tick index tuple)
  = TaggedTuple tick index (renameTuple localName tuple)

renameWindow :: LocalName -> Window -> Window
renameWindow localName (Window tick (BagOf tuples))
  = Window tick (BagOf (map (renameTuple localName) tuples))
renameWindow localName window 
  = error ("in renameWindow with "++ (show localName) ++ (show window)) 
  
renameAttribute :: LocalName -> Attribute -> Attribute
renameAttribute localName (Attribute _ attributeName) 
  = Attribute localName attributeName
\end{code}
\subsection{Input Simulation}
\begin{code} 

hardCodeOneOffScan :: ExtentName -> LocalName -> Data
hardCodeOneOffScan (ExtentName "departments") localName
  = let attributeNames = [(AttributeName "deptid"),(AttributeName "deptname")]
  in let attributes = map (convertToAttribute localName) attributeNames
  in BagOf [Tuple (attributes,[I 1, S "Sales"]),
      Tuple (attributes,[I 2, S "Production"]),
      Tuple (attributes,[I 3, S "Accounts"]),
      Tuple (attributes,[I 4, S "Transport"])
    ]

hardCodeOneOffScan (ExtentName "employee") localName
  = let attributeNames = [(AttributeName "id"),(AttributeName "name"),(AttributeName "deptid"),(AttributeName "salary")]
  in let attributes = map (convertToAttribute localName) attributeNames
  in BagOf [Tuple (attributes, [I 1, S "John", I 2, I 12000]),
     Tuple (attributes, [I 2, S "Bill", I 2, I 12500]),
     Tuple (attributes, [I 3, S "Mary", I 3, I 14000]),
     Tuple (attributes, [I 4, S "Susan", I 3, I 11000]),
     Tuple (attributes, [I 5, S "Mike", I 1, I 18000]),
     Tuple (attributes, [I 6, S "Kim", I 1, I 18000]),
     Tuple (attributes, [I 7, S "Bob", I 4, I 15000])    
    ]
        
hardCodeOneOffScan extentName _ 
  = NullData

simulateOneOffScan environment extentName@(ExtentName "person") localName
  = let (DeclExt _ _ attributeNames) = getDeclExtsByName environment extentName
  in let attributes = map (convertToAttribute localName) attributeNames
  in let seeds = [1..100]
  in BagOf (map (getTuple environment 0 attributes) seeds) 

simulateOneOffScan environment extentName@(ExtentName "item") localName
  = let (DeclExt _ _ attributeNames) = getDeclExtsByName environment extentName
  in let attributes = map (convertToAttribute localName) attributeNames
  in let seeds = [1089, 1007, 1020, 2001, 2019, 1087, 1067, 1545, 1745, 1006]
  in BagOf (map (getTuple environment 0 attributes) seeds) 

simulateOneOffScan environment extentName localName
  = let (DeclExt _ _ attributeNames) = getDeclExtsByName environment extentName
  in let attributes = map (convertToAttribute localName) attributeNames
  in let seeds = [1..100]
  in BagOf (map (getTuple environment 0 attributes) seeds) 

getTuple environment tick attributes seed
  = let outputData = map (getData environment tick seed) attributes
  in (Tuple (attributes, outputData))
  
readTable2 :: String -> IO Data
readTable2 fileName 
  = do
      text <- catch (readFile fileName)
           (\_ -> return "NullData")
      return (read text)

readTable :: Environment -> ExtentName -> LocalName -> IO Data  
readTable environment (ExtentName extentName) localName   
  = do 
      let fileName = extentName ++ ".txt"
      readOutput <- readTable2 fileName
      if (readOutput == NullData)
      then 
         let hardCode = hardCodeOneOffScan (ExtentName extentName) localName
         in if (hardCode == NullData)
            then return (simulateOneOffScan environment (ExtentName extentName) localName)
            else return hardCode
      else 
         return readOutput

simulateStrReceive environment (ExtentName "traffic") localName attributeNames
  =  let attributes = map (convertToAttribute localName) attributeNames
  in StreamOf (simulateAStrReceive environment attributes 0 1 5 75 1000)

simulateStrReceive environment extentName localName attributeNames
  =  let attributes = map (convertToAttribute localName) attributeNames
  in let seeds = [1..10]
  in StreamOf (simulateAStrReceive environment attributes 0 1 5 75 1000)

simulateAStrReceive environment attributes tick index maxSite selectivity rate
  =  let sites = randomSites tick maxSite selectivity
  in let output = acquireTuples environment attributes tick index sites
  in let nextIndex = index + (length sites)
  in let nextTick = tick + ((mod (tick * 18) 39)+1) * rate
  in output ++ (simulateAStrReceive environment attributes nextTick nextIndex maxSite selectivity rate)

crowdenAttributes :: LocalName -> [Attribute]
crowdenAttributes localName =
	[Attribute localName (AttributeName "date"),Attribute localName (AttributeName "time"),Attribute localName (AttributeName "moisture"),Attribute localName (AttributeName "msp"),Attribute localName (AttributeName "external"),Attribute localName (AttributeName "rain"),Attribute localName (AttributeName "voltage"),Attribute localName (AttributeName "gsm"),Attribute localName (AttributeName "id")]

crowdenTick :: Date -> Time -> Int
crowdenTick (Date day month year) (Time hour min)
  = let mtick = div min 15
  in mtick + (hour * 4) + (day * 96) - 96

crowdenTime tick
   = let day = div (tick) 96
   in let hour = div (tick - (day * 96)) 4
   in let mins = (tick - (day * 96) - (hour * 4)) * 15
   in (show (day+1)) ++ "/11/07 "++(show hour) ++ ":" ++ (show mins)

addCrowdenAttributes :: LocalName -> Int -> Tuple -> TaggedTuple
addCrowdenAttributes localName indexNum (Tuple (_, [D date@(Date month day year),T time@(Time hour min),F f1,F f2,F f3,I i1,F f4,F f5,I i2]))
  = let temp = ((crowdenAttributes localName), [D (Date month day year),T (Time hour min),F f1,F f2,F f3,I i1,F f4,F f5,I i2])
  in TaggedTuple (crowdenTick date time) indexNum (Tuple temp)
  
readLine :: Handle -> IO Data
readLine handle 
  = do
      line <- hGetLine handle
      return (read line)

readCrowden :: Handle -> LocalName -> Int -> IO Data
readCrowden handle localName indexNum
  = do
      rawData <- readLine handle
      let crowdenData = addCrowdenAttributes localName indexNum rawData
      return crowdenData

readCrowdenLines :: Handle -> LocalName -> Int -> IO [Data]
readCrowdenLines handle localName indexNum
  = do
      eof <- hIsEOF handle
      if eof then return []
      else do	
	      adata <- readCrowden handle localName indexNum
	      more <- readCrowdenLines handle localName (indexNum + 1)
	      let alldata = [adata] ++ more
	      return alldata

readAllCrowden fileName localName indexNum
  = do
      handle <- openFile fileName ReadMode
      taggedTuples <- readCrowdenLines handle localName indexNum
      let stream = StreamOf taggedTuples
      return stream

simulateStrAcquire  :: Environment -> ExtentName -> LocalName -> [AttributeName] -> IO Data
simulateStrAcquire  environment (ExtentName "crowden") localName _
  = readAllCrowden "crowden.txt" localName 0	
simulateStrAcquire  environment (ExtentName "crowden1") localName _
  = readAllCrowden "crowden1.txt" localName 0	
simulateStrAcquire  environment (ExtentName "crowden2") localName _
  = readAllCrowden "crowden2.txt" localName 0	
simulateStrAcquire environment extentName localName attributeNames
  = do
      return (randomStrAcquire environment extentName localName attributeNames)


randomStrAcquire  :: Environment -> ExtentName -> LocalName -> [AttributeName] -> Data
randomStrAcquire  environment (ExtentName "road") localName attributeNames
  = let attributes = map (convertToAttribute localName) attributeNames
  in StreamOf (randomAnAcquire environment attributes 0 1 [1..4] 1)

randomStrAcquire environment (ExtentName "sensors") localName attributeNames
  =  let attributes = map (convertToAttribute localName) attributeNames
  in StreamOf (randomAnAcquire environment attributes 0 1 [1..4] 5)

randomStrAcquire environment (ExtentName "vinesensors") localName attributeNames
  =  let attributes = map (convertToAttribute localName) attributeNames
  in StreamOf (randomAnAcquire environment attributes 0 1 [1..4] 300)

randomStrAcquire environment extentName localName attributeNames
  =  let attributes = map (convertToAttribute localName) attributeNames
  in StreamOf (randomAnAcquire environment attributes 0 1 [1..5] 10)

randomAnAcquire environment attributes tick index sites rate
  =  let output = acquireTuples environment attributes tick index sites
  in let nextIndex = index + (length sites)
  in let nextTick = tick + rate
  in 
     -- if (tick < 1500)
     -- then
	 output ++ (randomAnAcquire environment attributes nextTick nextIndex sites rate)
     -- else output

acquireTuples :: Environment -> [Attribute]->Int->Int->[Int]-> [Data]
acquireTuples _ _ _ _ [] = []
acquireTuples environment attributes tick index (site:sites) 
  =  let tupleData = getTaggedTuple environment tick index attributes site
  in [tupleData] ++ acquireTuples environment attributes tick (index + 1) (sites) 

randomSites tick maxSite selectivity
   = let posSites = [0..maxSite]
   in filter (useSite tick selectivity) posSites
  
useSite tick selectivity site 
   = (mod ((tick + 1) * ((site +1) * 87)) 100) < selectivity
   
getTaggedTuple environment tick index attributes site 
  = let outputData = map (getData environment tick site) attributes
  in TaggedTuple tick index (Tuple (attributes, outputData))
  
getData _ _ site (Attribute _ (AttributeName "id"))
  = I site
getData _ _ site (Attribute _ (AttributeName "site"))
  = I site
  
getData _ tick _ (Attribute _ (AttributeName "time"))
  = I tick

getData _ tick site (Attribute _ (AttributeName "class"))
  = let seed = ((div (tick) 1)+1) * site * 57
  in case (mod seed 5) of 
     0 -> S "Car"
     1 -> S "Bike"
     2 -> S "Bus"
     3 -> S "Truck"
     4 -> S "Rv"

getData _ tick site (Attribute _ (AttributeName "state"))
  = S "OR"
  
getData _ tick site attribute@(Attribute _ (AttributeName "itemid")) 
   = let I raw = getIntData tick site 10 attribute
   in case raw of
      0 -> I 1089
      1 -> I 1007
      2 -> I 1020
      3 -> I 2001
      4 -> I 2019
      5 -> I 1087
      6 -> I 1067
      7 -> I 1545
      8 -> I 1745
      9 -> I 1006

getData environment tick site attribute@(Attribute _ attributeName) 
   | Int == getTypeByAttributeName environment attributeName
   = getIntData tick site 100 attribute
   | Float == getTypeByAttributeName environment attributeName
   = getFloatData tick site 100 attribute
   | otherwise
   = let I val = getIntData tick site 100 attribute
   in S ("ST"++(show val))

getIntData tick site maxVal (Attribute _ (AttributeName name))
  = let seed = (tickSeed tick*7+1) * (site + length name) * 57
  in I (mod seed maxVal)
getFloatData tick site maxVal (Attribute _ (AttributeName name))
  = let seed = (tickSeed tick*7+1) * (site + length name) * 57
  in F ((intToFloat(mod seed (maxVal))) / 0.99)

tickSeed :: Int -> Int
tickSeed x
  | x < 100 = x
  | otherwise = (tickSeed (div x 100)) + (mod x 100)
\end{code}

