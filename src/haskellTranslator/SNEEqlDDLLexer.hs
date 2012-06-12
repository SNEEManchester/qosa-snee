module SNEEqlDDLLexer
where

import Char
data DDLDeclaration = DDLDeclaration Sink [DDLItem]
data DDLItem
      = DDLInt Int
      | DDLFloat Float      
      | DDLString String
      | DDLClass
      | DDLColumn
      | DDLEncoding
      | DDLName
      | DDLSchema
      | DDLSinks
      | DDLSites
      | DDLStream
      | DDLType
      | DDLXml
      | DDLVersion
      | DDLComma
      | DDLBackSlash
      | DDLEquals
      | DDLGreaterThan
      | DDLLessThan
      | DDLQuestionMark
      | DDLQuotes
 deriving Show

type Sink = Int

ddlLex :: String -> [DDLItem]
ddlLex text = ddlLexer (map toLower text)

ddlLexer :: String -> [DDLItem]
ddlLexer [] = []
ddlLexer (c:cs) 
      | isSpace c = ddlLexer cs
      | isAlpha c = ddlLexVar (c:cs)
      | isDigit c = ddlLexNum (c:cs)
ddlLexer ('\'':cs) = ddlLexString1 ('\'': cs)
ddlLexer ('"':cs) = ddlLexString2 ('"':cs)
ddlLexer ('/':cs) = DDLBackSlash : ddlLexer cs
ddlLexer ('=':cs) = DDLEquals : ddlLexer cs
ddlLexer ('<':cs) = DDLLessThan : ddlLexer cs
ddlLexer ('>':cs) = DDLGreaterThan : ddlLexer cs
ddlLexer (',':cs) = DDLComma : ddlLexer cs
ddlLexer ('?':cs) = DDLQuestionMark : ddlLexer cs
ddlLexNum cs
   | isFloat cs = DDLFloat (read num) : ddlLexer rest
      where (num,rest) = takeFloat cs
ddlLexNum cs
   | otherwise = DDLInt (read num) : ddlLexer rest
    where (num,rest) = span isDigit cs

isFloat (c:cs)
   | isDigit c = isFloat cs
   | (c == '.') && (isDigit (head cs)) = True
   | otherwise = False

takeFloat (c:cs) 
   | (c == '.') 
   = let (num, rest) = span isDigit cs
   in ((c:num),rest)
   | isDigit c 
   = let (num,rest) = takeFloat cs
   in ((c:num),rest)

isAlphaNumeric '_' = True  
isAlphaNumeric c 
    | isAlpha c = True
    | isDigit c = True
    | otherwise = False

ddlLexString1 cs = DDLString num : ddlLexer (tail rest)
    where (num,rest) = span (/='\'') (tail cs)

ddlLexString2 cs = DDLString num : ddlLexer (tail rest)
    where (num,rest) = span (/='"') (tail cs)

ddlLexVar cs =
   let (val, rest) = span isAlphaNumeric cs
   in let check = (map Char.toLower val, rest)
   in case check of
      ("class", rest) -> DDLClass : ddlLexer rest   
      ("column", rest) -> DDLColumn : ddlLexer rest
      ("encoding", rest) -> DDLEncoding : ddlLexer rest
      ("name", rest) -> DDLName : ddlLexer rest
      ("schema", rest) -> DDLSchema : ddlLexer rest
      ("sinks", rest) -> DDLSinks : ddlLexer rest
      ("sites", rest) -> DDLSites : ddlLexer rest
      ("stream", rest) -> DDLStream : ddlLexer rest
      ("type", rest) -> DDLType : ddlLexer rest
      ("xml", rest) -> DDLXml : ddlLexer rest
      ("version", rest) -> DDLVersion : ddlLexer rest


