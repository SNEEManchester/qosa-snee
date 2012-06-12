module SNEEqlLexer
where

import Char
              
data Token
      = IntWrapper Int
      | FloatWrapper Float
      | StringWrapper String
      | VarWrapper String
      | TokenAnd
      | TokenAs
      | TokenAt
      | TokenAverage
      | TokenBy
      | TokenCount
      | TokenDays
      | TokenDStream
      | TokenFrom
      | TokenGroup
      | TokenHaving
      | TokenHours
      | TokenIStream
      | TokenMaximum
      | TokenMilliSecs
      | TokenMin
      | TokenMinimum
      | TokenMinutes
      | TokenNot
      | TokenNow
      | TokenOr
      | TokenPower
      | TokenPushed
      | TokenRange
      | TokenRows
      | TokenRStream
      | TokenSeconds
      | TokenSelect
      | TokenSensed
      | TokenSlide
      | TokenSquare
      | TokenSquareRoot
      | TokenStored
      | TokenSum
      | TokenTo
      | TokenVarchar
      | TokenWhere
      | TokenEquals
      | TokenPlus
      | TokenMinus
      | TokenStar
      | TokenDiv
      | TokenLessThan
      | TokenGreaterThan
      | TokenOpenBracket
      | TokenCloseBracket
      | TokenOpenSquare
      | TokenCloseSquare
      | TokenComma
      | TokenDot
      | TokenSemi
      
 deriving Show

lexer :: String -> [Token]
lexer [] = []
lexer (c:cs) 
      | isSpace c = lexer cs
      | isAlpha c = lexVar (c:cs)
      | isDigit c = lexNum (c:cs)
lexer ('\'':cs) = lexString1 ('\'':cs)
lexer ('"':cs) = lexString2 ('"':cs)
lexer ('=':cs) = TokenEquals : lexer cs
lexer ('+':cs) = TokenPlus : lexer cs
lexer ('^':cs) = TokenPower : lexer cs
lexer ('-':cs) = TokenMinus : lexer cs
lexer ('*':cs) = TokenStar : lexer cs
lexer ('/':cs) = TokenDiv : lexer cs
lexer ('<':cs) = TokenLessThan : lexer cs
lexer ('>':cs) = TokenGreaterThan : lexer cs
lexer ('(':cs) = TokenOpenBracket : lexer cs
lexer (')':cs) = TokenCloseBracket : lexer cs
lexer ('[':cs) = TokenOpenSquare : lexer cs
lexer (']':cs) = TokenCloseSquare : lexer cs
lexer (',':cs) = TokenComma : lexer cs
lexer ('.':cs) = TokenDot : lexer cs
lexer (';':cs) = TokenSemi : lexer cs

lexNum cs
   | isFloat cs = FloatWrapper (read num) : lexer rest
      where (num,rest) = takeFloat cs
lexNum cs
   | otherwise = IntWrapper (read num) : lexer rest
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

lexString1 cs = StringWrapper num : lexer (tail rest)
    where (num,rest) = span (/='\'') (tail cs)

lexString2 cs = StringWrapper num : lexer (tail rest)
    where (num,rest) = span (/='"') (tail cs)

lexVar cs =
   let (val, rest) = span isAlphaNumeric cs
   in let check = (map Char.toLower val, rest)
   in case check of
      ("and", rest) -> TokenAnd : lexer rest
      ("as", rest) -> TokenAs : lexer rest
      ("at", rest) -> TokenAt : lexer rest
      ("avgerage", rest) -> TokenAverage : lexer rest
      ("by", rest) -> TokenBy : lexer rest
      ("count", rest) -> TokenCount : lexer rest
      ("days", rest) -> TokenDays : lexer rest
      ("dstream", rest) -> TokenDStream : lexer rest
      ("from", rest) -> TokenFrom : lexer rest
      ("group", rest) -> TokenGroup : lexer rest
      ("having", rest) -> TokenHaving : lexer rest
      ("hours", rest) -> TokenHours : lexer rest
      ("istream", rest) -> TokenIStream : lexer rest
      ("maximum", rest) -> TokenMaximum : lexer rest
      ("millisecs", rest) -> TokenMilliSecs : lexer rest
      ("minimum", rest) -> TokenMinimum : lexer rest
      ("minutes", rest) -> TokenMinutes : lexer rest
        -- min could be minute of minimum depending on placement
      ("min", rest) -> TokenMin : lexer rest
      ("now", rest) -> TokenNow : lexer rest
      ("not", rest) -> TokenNot : lexer rest
      ("or", rest) -> TokenOr : lexer rest
      ("pushed", rest) -> TokenPushed : lexer rest
      ("range", rest) -> TokenRange : lexer rest
      ("rows", rest) -> TokenRows : lexer rest
      ("rstream", rest) -> TokenRStream : lexer rest
      ("seconds", rest) -> TokenSeconds : lexer rest
      ("select", rest) -> TokenSelect : lexer rest
      ("sensed", rest) -> TokenSensed : lexer rest
      ("slide", rest) -> TokenSlide : lexer rest
      ("square", rest) -> TokenSquare : lexer rest
      ("squareroot", rest) -> TokenSquareRoot : lexer rest
      ("stored", rest) -> TokenStored : lexer rest
      ("sum", rest) -> TokenSum : lexer rest
      ("to", rest) -> TokenTo : lexer rest
      ("varchar", rest) -> TokenVarchar : lexer rest
      ("where", rest) -> TokenWhere : lexer rest
      {-- Semnatic sugar abbreviations  & singles --}
      ("avg", rest) -> TokenAverage : lexer rest
      ("day", rest) -> TokenDays : lexer rest
      ("hour", rest) -> TokenHours : lexer rest
      ("max", rest) -> TokenMaximum : lexer rest
      ("millisec", rest) -> TokenMilliSecs : lexer rest
      ("milliseconds", rest) -> TokenMilliSecs : lexer rest
      ("millisecond", rest) -> TokenMilliSecs : lexer rest
      ("minute", rest) -> TokenMinutes : lexer rest      
      ("msecs", rest) -> TokenMilliSecs : lexer rest
      ("msec", rest) -> TokenMilliSecs : lexer rest
      ("mins", rest) -> TokenMinutes : lexer rest
      ("second", rest) -> TokenSeconds : lexer rest
      ("secs", rest) -> TokenSeconds : lexer rest
      ("sec", rest) -> TokenSeconds : lexer rest
      ("sqr", rest) -> TokenSquare : lexer rest
      ("sqrt", rest) -> TokenSquareRoot : lexer rest
	{-- base case for none Tokens --}
      (var, rest)   -> VarWrapper var : lexer rest


