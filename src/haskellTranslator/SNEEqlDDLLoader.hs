module SNEEqlDDLLoader
where

import Char
import List
import SNEEqlAst
import SNEEqlDDLAst
import SNEEqlDDLLexer
import SNEEqlDDL
import SNEEqlEnvironment

getAttributeName :: DeclAtt -> AttributeName
getAttributeName (DeclAtt attributeName typeToken) 
  = attributeName
  
timeIDDeclaration
  = [(DeclAtt (AttributeName "time") IntToken),(DeclAtt (AttributeName "id") IntToken)]
  
timeID 
  = [(AttributeName "time"),(AttributeName "id")]
  
streamToDeclarations (Stream name declarations sites)
  = let attributeNames = (map getAttributeName declarations) ++ timeID
  in let allDeclarations = declarations ++ timeIDDeclaration 
  in allDeclarations ++ [(DeclExt (ExtentName name) SensedToken attributeNames)]

ddlToDeclarations (DDL sinks streams)
  = let declarations = (foldr (++) [] (map streamToDeclarations streams))
  in nub (declarations)

readLexer :: String -> IO [DDLItem]
readLexer fileName
  = do
      text <- readFile (fileName)
      return (ddlLex text)

readDDL :: String -> IO Environment
readDDL fileName
  = do
      ddlItems <- readLexer (fileName)
      let ddl = ddlParse (ddlItems)
      let declarations = ddlToDeclarations ddl
      print (show declarations)
      print ("--")
      let environment = (addDeclarations emptyEnvironment declarations)
      print environment
      return environment
      -- return ddl
      
readSysCat :: String -> IO Environment
readSysCat fileName
  | isSuffixOf ".xml" fileName = (readDDL fileName)
  | otherwise = (readDecFile fileName)