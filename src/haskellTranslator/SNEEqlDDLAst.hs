module SNEEqlDDLAst 
where

import Char
import List
import SNEEqlEnvironment

data DDL
  = DDL [Sink] [Stream]
   deriving (Eq, Ord, Show, Read)

data Stream
  = Stream Name [Declaration] [Sites]
   deriving (Eq, Ord, Show, Read)
             
type Name = String
  
type Sites = Int
 
type Sink = Int

data Date = Date Int Int Int
              deriving (Eq, Ord, Show, Read)

data Time = Time Int Int
              deriving (Eq, Ord, Show, Read)


toTypeToken :: String -> TypeToken
toTypeToken "integer" = IntToken
toTypeToken "date" = DateToken
toTypeToken "time" = TimeToken
toTypeToken "float" = FloatToken
toTypeToken "string" = StringToken

toTypeToken x = error ("Unprogrammed TypeToken "++x)

{-
data Declaration 
   = DeclAtt AttributeName TypeToken
   | DeclExt ExtentName ExtentType [AttributeName]
       deriving (Eq, Ord, Show, Read)

data AttributeName = AttributeName String
       deriving (Eq, Ord, Show, Read)
   
data TypeToken
  = BooleanToken
  | IntToken
  | FloatToken
  | StringToken           
       deriving (Eq, Ord, Show, Read)

data ExtentName = ExtentName String  
       deriving (Eq, Ord, Show, Read)

data ExtentType
  = StoredToken
  | PushedToken
  | SensedToken
  | RelationView
  | WindowView
  | StreamView
       deriving (Eq, Ord, Show, Read)
-}