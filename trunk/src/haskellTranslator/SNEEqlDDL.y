{
module SNEEqlDDL 
where

import Char
import SNEEqlDDLLexer
import SNEEqlDDLAst
import SNEEqlAst
import SNEEqlEnvironment
}

%name ddlParse ddlDeclaration

%tokentype                             { DDLItem }
%error                                 { ddlParseError }

%token 
  int                                  { DDLInt $$ }
  string                               { DDLString $$ }
  "class"                              { DDLClass }
  "column"                             { DDLColumn }
  "encoding"                           { DDLEncoding }
  "name"                               { DDLName }
  "schema"                             { DDLSchema }
  "sinks"                              { DDLSinks }
  "sites"                              { DDLSites }
  "stream"                             { DDLStream }
  "type"                               { DDLType }
  "xml"                                { DDLXml }
  "version"                            { DDLVersion }
  ","                                  { DDLComma }
  "/"                                  { DDLBackSlash }
  "="                                  { DDLEquals }
  ">"                                  { DDLGreaterThan }
  "<"                                  { DDLLessThan }
  "?"                                  { DDLQuestionMark }

%%

ddlDeclaration
  : head schema sinks streamList schemaTail  { DDL $3 $4 }
  | head schema streamList schemaTail  { DDL [] $3 }
  
sinks : "<" "sinks" ">" intList "<" "/" "sinks" ">"  { $4 }

head 
   : "<" "?" "xml" "version" "=" string "encoding" "=" string "?" ">" { }

schema
  : "<" "schema" ">"                   { }
  
schemaTail
  : "<" "/" "schema" ">"               { }
  
streamList
  : stream                             { [$1] }
  | streamList stream                  { $2 : $1 }
  
stream
  : streamHead columnList sites streamTail {Stream $1 $2 $3 }
    
streamHead 
  : "<" "stream" "name" "=" string ">" { $5 }
  
streamTail 
  : "<" "/" "stream" ">"               { }

columnList 
  : column                             { [$1] }
  | columnList column                  { $2 : $1 }
  
column
  : columnHead type columnTail         { DeclAtt (AttributeName $1) $2 }
    
columnHead
  : "<" "column" "name" "=" string ">" { $5 }
  
columnTail
  : "<" "/" "column" ">"               { }

type
  : "<" "type" "class" "=" string "/" ">" { toTypeToken $5 }
  
sites
 : "<" "sites" ">" intList "<" "/" "sites" ">" { $4 }
  
intList
  : int                                { [$1] }
  | int "," intList                    { $1 : $3 }
  
{
ddlParseError :: [DDLItem] -> a
ddlParseError _ = error "DDL Parse error"
}

