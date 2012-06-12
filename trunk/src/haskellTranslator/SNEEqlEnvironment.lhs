\begin{comment} 
\begin{code}
module SNEEqlEnvironment 
where

import SNEEqlAst
import SNEEqlTypes
\end{code}
\end{comment}

\subsection{Deducing Types From DDL Statements in the System Catalog}
\label{sec:dec2types}
The system catalog \syscat and (therefore because of rule \ref{rule_syscat}) the environment \environment includes DDL attribute declarations in the Format: \syn{(DeclAtt (AttributeName TypeToken))}.
Where \syn{AttributeName} is a wrapper around a String representation of the attribute Name and \syn{TypeToken} an enumeration from the list \syn{BooleanToken}, \syn{IntToken}, \syn{FloatToken} and \syn{StringToken}
In \SNEEql the restriction is that each attribute name can only represent an attribute of a single type, although it may be used in various extents.

For example:

\syn{(DeclAtt ((AttributeName name) StringToken))} and 

\noindent \syn{(DeclAtt ((AttributeName sal) IntToken))} which declare two attributes name and sal with types String and Int respectively.
         
The DDL also includes extent declarations in the format:

\noindent \syn{(DeclExt (ExtentName ExtentType [$AttributeName_1..AttributeName_n$]))}

Where \syn{ExtentName} and the \syn{AttributeName} are wrappers around the string representation of the extent and attribute names, while extentType is an enumeration.
\syn{StoredToken}, \syn{PushedToken} and \syn{SensedToken} are used in DDL declarations while \syn{RelationView}, \syn{WindowView} and \syn{StreamView} are used in the rule for sub queries (Rule \ref{rule_SubqueryType}) and when added views.

For example the declaration \syn{(DeclExt ((ExtentName emp) StoredToken [(AttributeName name), (AttributeName sal)]))} defines an stored (Relational) extent emp with two attributes name and sal. 

In Haskell \syn{DeclAtt} and \syn{DeclExt} are represented as:
\begin{code}
data Declaration 
   = DeclAtt AttributeName TypeToken
   | DeclExt ExtentName ExtentType [AttributeName]
\end{code}
\begin{comment}
\begin{code}
            deriving (Eq, Ord, Show, Read)
            
type DeclAtt = Declaration

type DeclExt = Declaration


data DATE = DATE
              deriving (Eq, Ord, Show, Read)

data TIME = TIME
              deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}    
        
\begin{code} %Actual code in AST
data AttributeName = AttributeName String

data ExtentName = ExtentName String

\end{code} %Actual code in AST 
\begin{code}
data TypeToken
  = BooleanToken
  | IntToken
  | FloatToken
  | DateToken
  | TimeToken
  | StringToken           
\end{code}
\begin{comment}
\begin{code}
            deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}            

\begin{code}
data ExtentType
  = StoredToken
  | PushedToken
  | SensedToken
  | RelationView
  | WindowView
  | StreamView
\end{code}
\begin{comment}
\begin{code}
            deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}            

%One kind of metadata available in \syscat is statements in the
%data definition language, i.e., schema declarations. These are typeset
%as follows. For example, \syn{(DeclAtt (name StringToken))}, \syn{(DeclAtt (sal IntToken))} and
%\syn{(DeclExt (emp SensedToken [name, sal]))} respectively declare two attributes and a
%acquisition stream defined in terms of the latter. 
\begin{comment}
\begin{code}      
type AttributeMap = (AttributeName, Type)
type ExtentMap = (ExtentName, Type)
type BoundMap = (LocalName, ExtentName)
type Environment = ([DeclExt], [AttributeMap], [ExtentMap], [BoundMap], [Attribute])

emptyEnvironment :: Environment 
emptyEnvironment =  ([], [], [], [], [])

addDeclaration :: Environment -> Declaration -> Environment
addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName BooleanToken)
  = (declExt, attributeMap ++ [(attributeName, Boolean)], extentMap, boundMap, groupByAttributes)

addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName IntToken)
    = (declExt, attributeMap ++ [(attributeName, Int)], extentMap, boundMap, groupByAttributes)

addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName StringToken)
    = (declExt, attributeMap ++ [(attributeName, String)], extentMap, boundMap, groupByAttributes)

addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName FloatToken)
    = (declExt, attributeMap ++ [(attributeName, Float)], extentMap, boundMap, groupByAttributes)

addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName DateToken)
    = (declExt, attributeMap ++ [(attributeName, String)], extentMap, boundMap, groupByAttributes)

addDeclaration (declExt, attributeMap, extentMap, boundMap, groupByAttributes) new@(DeclAtt attributeName TimeToken)
    = (declExt, attributeMap ++ [(attributeName, String)], extentMap, boundMap, groupByAttributes)

addDeclaration  environment@(declExts, attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName StoredToken attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Bagof (Tupleof types)
   in (declExts ++ [newExt], attributeMap, extentMap ++ [(extentName, newType)], boundMap, groupByAttributes)

addDeclaration  environment@(declExts, attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName RelationView attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Bagof (Tupleof types)
   in (declExts ++ [newExt], attributeMap, extentMap ++[(extentName, newType)], boundMap, groupByAttributes)

addDeclaration  environment@(declExts,  attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName PushedToken attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types]))
   in (declExts ++ [newExt],  attributeMap, extentMap ++[(extentName, newType)], boundMap, groupByAttributes)

addDeclaration  environment@(declExts,  attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName SensedToken attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types]))
   in (declExts ++ [newExt],  attributeMap, extentMap ++[(extentName, newType)], boundMap, groupByAttributes)

addDeclaration  environment@(declExts, attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName StreamView attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Streamof (Tupleof ([Int] ++ [Int] ++ [Tupleof types]))
   in (declExts ++ [newExt],  attributeMap, extentMap ++[(extentName, newType)], boundMap, groupByAttributes)

addDeclaration  environment@(declExts,  attributeMap, extentMap, boundMap, groupByAttributes) 
      newExt@(DeclExt extentName WindowView attributeNames)
   = let types = map (getTypeByAttributeName environment) attributeNames
   in let newType = Streamof (Tupleof ([Int] ++ [Bagof (Tupleof types)]))
   in (declExts ++ [newExt],  attributeMap, extentMap ++[(extentName, newType)], boundMap, groupByAttributes)

addDeclarations environment [] = environment
addDeclarations environment (declaration:more)
   = let newEnvironment = addDeclaration environment declaration
   in addDeclarations newEnvironment more
        
getDeclExtsByName :: Environment -> ExtentName -> DeclExt
getDeclExtsByName environment@(declExts, _, _, _, _) name
 = findDeclExt environment declExts name
 
findDeclExt :: Environment -> [DeclExt] -> ExtentName -> DeclExt
findDeclExt environment [] name = error ((show name)++" not declared as a DeclExt in the environment:"++(show environment))
findDeclExt environment (declExt@(DeclExt thisName _ _):more) name
   | thisName == name = declExt
   | otherwise = findDeclExt environment more name	

getTypeByAttributeName :: Environment -> AttributeName -> Type 
getTypeByAttributeName environment@(_, attributeMap, _, _, _) attributeName
   = findTypeByAttributeName environment attributeMap attributeName
   
findTypeByAttributeName :: Environment -> [AttributeMap] -> AttributeName -> Type
findTypeByAttributeName environment [] name = error ((show name)++" not declared in the environment: "++(show environment))
findTypeByAttributeName environment ((thisName,thisType):more) name
   | thisName == name = thisType
   | otherwise = findTypeByAttributeName environment more name	

getTypeByExtentName :: Environment -> ExtentName -> Type 
getTypeByExtentName environment@(_, _, extentMap, _, _) extentName
   = findTypeByExtentName environment extentMap extentName

findTypeByExtentName :: Environment -> [ExtentMap] -> ExtentName -> Type
findTypeByExtentName environment [] name = error ((show name)++" not declared in the environment: "++(show environment))
findTypeByExtentName environment ((thisName,thisType):more) name
   | thisName == name = thisType
   | otherwise = findTypeByExtentName environment more name	
   
relateNamesToTypes :: Environment -> [AttributeName] -> [Type] -> Environment
relateNamesToTypes environment [] [] = environment
relateNamesToTypes environment (name:names) (projectType:projectTypes)   
   = let (declExts, attributeMap, extentMap, boundMap, groupByAttributes) = relateNamesToTypes environment names projectTypes
   in (declExts, attributeMap ++ [(name,projectType)], extentMap, boundMap, groupByAttributes)

bindName :: Environment -> LocalName -> ExtentName -> Environment
bindName (declExts, attributeMap, extentMap, boundMap, groupByAttributes) localName extentName
   = (declExts, attributeMap, extentMap, boundMap++[(localName, extentName)], groupByAttributes)

getBoundExtents :: Environment -> [ExtentName]
getBoundExtents (_, _, _, boundMap, _) 
   = map snd boundMap
   
getLocalNames :: Environment -> [LocalName]
getLocalNames (_, _, _, boundMap, _) 
   = map fst boundMap

getBoundName :: Environment -> LocalName -> ExtentName
getBoundName environment@(_, _, _, boundMap, _) localName
   = findBoundName environment boundMap localName

findBoundName :: Environment -> [BoundMap] -> LocalName -> ExtentName
findBoundName environment [] localName = error ((show localName)++" not bound in the environment: "++(show environment))
findBoundName environment ((thisName,thisBound):more) localName
   | thisName == localName = thisBound
   | otherwise = findBoundName environment more localName	
    
data DeclGroupBy = DeclGroupBy [Attribute]

-- ([DeclExt], [AttributeMap], [ExtentMap], [BoundMap], [Attribute])
hasDeclGroupBy :: Environment -> Bool
hasDeclGroupBy (_, _, _, _, groupByAttributes)
  | length (groupByAttributes) == 0 = False
  | otherwise = True

noDeclGroupBy :: Environment -> Bool
noDeclGroupBy (_, _, _, _, groupByAttributes)
  | length (groupByAttributes) == 0 = True
  | otherwise = False

setDeclGroupBy :: Environment -> DeclGroupBy -> Environment
setDeclGroupBy (declExts, attributeMap, extentMap, boundMap, _) (DeclGroupBy groupByAttributes)
   = (declExts, attributeMap, extentMap, boundMap, groupByAttributes)

getDeclGroupBy :: Environment -> DeclGroupBy 
getDeclGroupBy (_, _, _, _, groupByAttributes)
   = DeclGroupBy groupByAttributes

readDeclarations :: String -> IO [Declaration]
readDeclarations fileName
  = do
      text <- readFile (fileName)
      return (read text)

readDecFile :: String -> IO Environment
readDecFile fileName
  = do
      declarations <- readDeclarations fileName
      -- print (show declarations)
      -- error ("forced error in readSysCat");
      return (addDeclarations emptyEnvironment declarations)
\end{code}
\end{comment}
 