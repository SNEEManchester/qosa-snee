module Main 
where

import IO
import List
import System
import System.Environment
import SNEEqlLexer
import SNEEqlAst
import SNEEqlHappy 
import SNEEqlEnvironment
import SNEEqlDDLLoader
import SNEEqlTypeRules
import SNEEqlTypes
import SNEEqlOperators
import SNEEqlTranslate
import SNEEqlEvaluate 
import qualified Control.Exception as E
import System.Console.GetOpt

translate query declFile outfile 
   = do
	sysCat <- readSysCat declFile
	let tokens = lexer query
	let ast = parse tokens
	let (environment, queryType) = typeFullQuery sysCat ast 
	let operator = translateFullQuery environment ast
	bracket (openFile outfile WriteMode) hClose
	        (\h -> hPutStrLn h (show operator))

translatePlus query declFile outfile 
   = do
	sysCat <- readSysCat declFile
	let tokens = lexer query
	let ast = parse tokens
	let (environment, queryType) = typeFullQuery sysCat ast 
	let operator = translatePlusFullQuery environment ast
	print (show operator)
	bracket (openFile outfile WriteMode) hClose
	         (\h -> hPutStrLn h (show operator)) 

-- Below used to create a large error message.
-- sfdakjaafdssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssjgjfjhfghgsjdhgjkshgjsfkghsdjfhgsjkfhgsfdjkghskjfghsjkfhgskjfhgskjjghskjfjhgsdjfhgfhghhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh :: Int
-- sfdakjaafdssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssjgjfjhfghgsjdhgjkshgjsfkghsdjfhgsjkfhgsfdjkghskjfghsjkfhgskjfhgskjjghskjfjhgsdjfhgfhghhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh 
--  = let sfdakjaafdssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssjgjfjhfghgsjdhgjkshgjsfkghsdjfhgsjkfhgsfdjkghskjfghsjkfhgskjfhgskjjghskjfjhgsdjfhgfhghhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh ="q" 
--  in sfdakjaafdssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssjgjfjhfghgsjdhgjkshgjsfkghsdjfhgsjkfhgsfdjkghskjfghsjkfhgskjfhgskjjghskjfjhgsdjfhgfhghhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
  
checkArgs args
 = do
    if (length args < 4)
    then error "Missing arguements: Expected QueryFile, DDLFile, OutputFile"
    else print (show args)
 	
readOptimize :: String -> Bool
readOptimize "True" = True
readOptimize "False" = False
readOptimize text = error ("Unable to convert " ++ text ++ " to bool opt Optimize")

main = do
     args <- getArgs
     print $ (length args)
     checkArgs args
     print ("Reading: "++(head args))
     query <- readFile (head args)
     print ("query: " ++ query)
     let declFile = args!!1
     if (isSuffixOf ".xml" declFile)
        then print ("Using schema file: "++declFile)
        else print ("Using declarations file: "++declFile)
     let outFile = args!!2
     print ("Using output file: " ++ outFile)
     let optimize = readOptimize (args!!3)
     if optimize
        then 
           translatePlus query declFile outFile 
        else 
           translate query declFile outFile 
     print "Success"
     