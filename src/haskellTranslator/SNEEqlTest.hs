module Main 
where

import IO
import System.Environment
import SNEEqlLexer
import SNEEqlDDLLoader
import SNEEqlAst
import SNEEqlHappy 
import SNEEqlEnvironment
import SNEEqlTypeRules
import SNEEqlTypes
import SNEEqlOperators
import SNEEqlTranslate
import SNEEqlEvaluate 
-- import SNEEqlCost 

{-- Demo and test stuff --}
twoExtents = "select name from employee, departments;"

twoExtents2 = "select name as myname from employee, departments;"

twoExtents1 = "select e.name from employee e, departments;"

basicQuery = "select e.name as myname from employee e;"

simpleQuery = "select id, name from employee where deptId = 2;"

subInner = "select id, name, deptId from employee where deptId < 10;"

subQuery = "select employeeInner.id, employeeInner.name from (select id, name, deptId from employee where deptId < 10) employeeInner where id > 25;"

subQuery2 = "select employeeInner.id, employeeInner.name, deptInner.deptName from (select id, name, deptId from employee where deptId < 10) employeeInner, (select deptName from departments) deptInner where id > 25;"

subQuerySugar = "select id, name from (select id, name, deptId from employee where deptId < 10) where id > 25;"

aggQuery = "select max(id), avg(salary) from employee where deptId = 2;"

simpleJoinNoWhere = "select name, deptName from departments, employee;"

simpleJoin = "select name, deptName from employee, departments where employee.deptId = departments.deptId;"

pushQuery = "select class, weight from traffic where class = \"Car\";"

sensorQuery = "select site, weight from road;"
 
sensorJoin = "select site, weight, deptId from road, departments;"

tableStreamQuery = "select name, class from departments, traffic, employee where employee.deptId = departments.deptId and employee.id = traffic.site;"

q1 = "SELECT e.name as name, d.deptname as department FROM departments d, employee e WHERE d.deptid = e.deptid and e.salary > 10000;"

q3 = "SELECT  RSTREAM OutFlow.time, InFlow.pressure, OutFlow.pressure FROM OutFlow[NOW], InFlow[at now - 10 sec] WHERE   InFlow.pressure > 50 AND OutFlow.pressure > InFlow.pressure;"

sensorSubQuery = "select max(weight) from (select site, weight from road)[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorSubQuery3 = "select max(weight) from (select site, weight from road)[from now - 1 rows to now - 0 rows slide 1 rows] sub1;"

sensorQuery2 = "select site, weight from road[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorQuery3 = "rstream select site, weight from road[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorQuery4 = "istream select site, weight from road[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorQuery5 = "dstream select site, weight from road[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorSubQuery2 = "rstream select max(weight) from (select site, weight from road)[from now - 1 rows to now - 0 rows slide 1 rows];"

sensorQuery6 = "select site, weight from road[now] where weight > 50;"

timeSlideRowScope = "select site, weight from road[from now - 1 rows to now - 0 rows slide 1 second];"

timeQuery = "select site, weight from road[from now - 20 seconds to now - 10 seconds slide 5 seconds];"

aggTimeQuery = "select count(site), max(weight), min(weight), sum(weight), avg(weight) from road[from now - 20 seconds to now - 10 seconds slide 5 seconds];"
aggTimeQuery2 = "select avg(weight) from road[now];"

tableStreamQuery2 = "select name, class from departments d, traffic t, employee e where e.deptId = d.deptId and e.name = t.class;"

windowJoin = "select traffic.site as site, road.weight as weight from road[from now - 1 rows to now - 0 rows slide 1 rows], traffic[from now - 1 rows to now - 0 rows slide 1 rows] where traffic.site = road.site;"

windowJoin2 = "rstream select traffic.site, road.weight from road[from now - 1 rows to now - 0 rows slide 1 rows], traffic[from now - 1 rows to now - 0 rows slide 1 rows] where traffic.site = road.site;"

windowJoin3= "select road.weight as weight, e.name from road[from now - 1 rows to now - 0 rows], employee e;"

q2part = "RSTREAM SELECT r.site as site, r.time as time FROM road [FROM now -0 sec to now -0 sec slide 1 sec] r;"

errorq = "SELECT q.junk FROM road [FROM now -0 sec to now -0 sec slide 1 sec] r, road [FROM now -0 sec to now -0 sec slide 1 sec] r2, road [FROM now -0 sec to now -0 sec slide 1 sec] r3;"

q2Inner = "RSTREAM SELECT r.site as site, r.time as time, sub1.avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] r, (SELECT AVG(r.vibration) as avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] rq) sub1, (SELECT AVG(r.vibration) as avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] rw, employee ee) sub2;"

q2Inner2 = "SELECT r.site as site, r.time as time, sub1.avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] r, (SELECT AVG(r.vibration) as avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] rq) sub1, (SELECT AVG(r.vibration) as avgvib FROM road [FROM now -0 sec to now -0 sec slide 1 sec] rw, employee ee) sub2;"

simpleRelation = "select name from employee;"

selectTest = "select id, id + 2, id - 1, id * 2, -id, id / 2, 2+3*id/(2+3/4*id) from employee;"

relGroupTest = "select max(deptid), avg(salary), deptid, max(deptid) + avg(salary) from employee group by deptid having deptid <> 1;"

javaq1 = "SELECT RSTREAM * FROM InFlow[NOW];"

cost1 = "SELECT id, temperature, pressure FROM inflow;"

{- Stream Query Repository 
Source = http://infolab.stanford.edu/stream/sqr/onauc.html
-}

{- user defined function -}
online1 = "Select itemID, DolToEuro(bid_price), bidderID From Bid;" 
online2 = "Select itemID, bid_price From Bid Where itemID = 1007 or itemID = 1020 or itemID = 2001 or itemID = 2019 or itemID = 1087;"
online3 = "Select Istream(P.name, P.city, O.itemID) From OpenAuction [Now] O, Person P, Item I Where O.sellerID = P.id and P.state = 'OR' and O.itemID = I.id and I.categoryID = 10;"
{- In -}
online4 = "Select * From OpenAuction Where  itemID Not In (Select itemID From ClosedAuction);" 
{- Union and View
5. "CurrentPrice: 
    Select    P.itemID, Max(P.price) as price
    From      ((Select itemID, bid_price as price 
                From   Bid) Union All
               (Select itemID, start_price as price 
                From OpenAuction)) P
    Group By  P.itemID

Select   Rstream(C.itemID, P.price)
From     ClosedAuction [Now] C, CurrentPrice P
Where    C.itemID = P.itemID
-}
{- Union View 
6. ClosingPriceStream:
    Select   Rstream(T.id as catID, P.price as price)
    From     ClosedAuction [Now] C, CurrentPrice P, 
             Item I, Category T    
    Where    C.itemID = P.itemID and C.itemID = I.id and I.categoryID = T.id 

AvgPrice:
    Select   catID, Avg(price)
    From     ClosingPriceStream [Range 1 Hour]
    Group By catID 
-}
{- Single extent * 
7. Select Rstream(OpenAuction.*)
From   OpenAuction [Range 5 Hour] O, ClosedAuction [Now] C
Where  O.itemID = C.itemID
-}
{- SubQuery in Where
8. HotItemStream: 
    Select Rstream(itemID)
    From   (Select   B1.itemID as itemID, Count(*) as num
            From     Bid [Range 60 Minute
                          Slide 1 Minute] B1
            Group By B1.itemID) 
    Where   num >= All (Select   Count(*)
                        From     Bid [Range 60 Minute
                                      Slide 1 Minute] B2
                        Group By B2.itemID)

Select *
From   HotItemStream [Range 1 Minute]
-}
{- Union ALL 
9.CurrentPrice: 
    Select    P.itemID, Max(P.price) as price
    From      ((Select itemID, bid_price as price 
                From   Bid) Union All
               (Select itemID, start_price as price 
                From OpenAuction)) P
    Group By  P.itemID

ClosingPriceStream:
    Select   Rstream(O.sellerID as sellerID, P.price as price)
    From     ClosedAuction [Now] C, CurrentPrice P, 
             OpenAuction O
    Where    C.itemID = P.itemID and C.itemID = O.itemID

AvgSellingPrice:
    Select   sellerID, Avg(price)
    From     ClosingPriceStream [Partition By sellerID Rows 10] 
    Group By sellerID 
-}
online10 = "Select  Rstream(itemID, bid_price) From Bid [Range 10 Minute Slide 10 Minute] Where bid_price = (Select  Max(bid_price) From Bid [Range 10 Minute Slide 10 Minute]";
{- View 
11. NewPersonStream:
  Select Istream(P.id, P.name)
  From   Person P

Select Distinct(P.id, P.name)
From   Select Rstream(P.id, P.name)
       From   NewPersonStream [Range 12 Hour] P, OpenAuction A [Now]
       Where  P.id = A.sellerID

-}


evaluateSome :: Environment -> Operator -> Int -> IO Data
evaluateSome environment operator count
  = do
     all <- evaluate environment operator
     let output = getSome all count
     return output

evaluateAndDisplaySome :: Environment -> Operator -> Int -> IO String
evaluateAndDisplaySome environment operator count
  = do
     all <- evaluate environment operator
     -- let output = getSome all count
     -- let text = displayWithHead output
     let text = displaySome all count
     return text

getSome :: Data -> Int -> Data
getSome (StreamOf tuples) count 
  = StreamOf (take count tuples)
getSome a _ 
  = a

demo query = do
	print "----------------------------------------------------"
	print "SysCat"
	-- sysCat <- readSysCat "declarations.txt"
	sysCat <- readSysCat "crowden.xml"
	-- sysCat <- readSysCat "schemas.xml"
	print sysCat
	print "Query:"
	print query
	print "Tokens:"
	let tokens = lexer query
	print tokens
	print "Abstract Syntax Tree:"
	let ast = parse tokens
	print ast
	print "Type:"
	let (environment, queryType) = typeFullQuery sysCat ast 
	print queryType
--	print "Environments:"
--	print environment
	print "Operators:"
	let operator = translatePlusFullQuery environment ast
	print operator
	bracket (openFile "operators.txt" WriteMode) hClose
	        (\h -> hPutStrLn h (show operator))
--	print "cost"
--	let timeCost = evaluateDemo operator 
--	print timeCost
	print "Evaluate"
	results <- evaluateAndDisplaySome environment operator 200
	print results
--	-- let result = evaluateSome operator 20
--        let printResults = (getSome results 20)

-- 	print (displaySome result 20)
	print "-----------------------"

outData (NullData) 
  = do print "Unprogrammed"
outData (BagOf []) 
  = do print "Bagof []" 
outData (BagOf tuples) 
  = do
  	print "Bag of ["
  	outList tuples
  	print "]"
outData (StreamOf tuples)
  = do
  	print "Stream of;"
  	outList tuples
outData (TaggedTuple tick index tuple) 
  = do
  	let text = "(" ++ (show tick) ++ (show index) ++ ",[" ++ (outTuple tuple) ++ "]"
  	print text
outData (Window tick (BagOf tuples))
  = do 
  	print ("Window: ("++ (show tick)++", [")
  	outList tuples
  	print "])"
outData tuple@(Tuple _)
  = do
  	print (outTuple tuple)
outData x
  = do
  	print "unknown type in outdata"
  	print (show x)
  	
-- displayWithHead group@(Group groupTuple tuples) 
--  = "groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData group)
-- displayWithHead groups@(Groups ((Group groupTuple tuples):more)) 
--  = "groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData groups)
-- displayWithHead winGroups@(WinGroups tick ((Group groupTuple tuples):more)) 
--  = "WinGroup groupTuple = "++(show groupTuple) ++ " tuple = "++(show (head tuples))++ (displayData winGroups)
  	
outList []
  = do print ""    
outList (first:[])
  = do 
  	outData first
outList (first:more)
  = do 
  	outData first
  	outList more

outTuple (Tuple (attributes, rawdata))
  = outTogether attributes rawdata
outTuple x
  = "unknown type in outTuple" ++ (show x)

outTogether [][] = ""
outTogether (attribute:[]) (rawdata:[])
  = outAttribute attribute rawdata
outTogether (attribute:attributes) (rawdata:moredata)
  = outAttribute attribute rawdata ++ ", " ++ outTogether attributes moredata
outTogether x y
  = "unknown type in outTogether" ++ (show x) ++ (show y)

outAttribute (Attribute (LocalName localName) (AttributeName attributeName)) rawData
  | (localName == "@main@") = attributeName ++ ": " ++ display(rawData) 
  | otherwise = localName ++ "." ++ attributeName ++": " ++ display(rawData) 
outAttribute x y
  = "unknown type in outAttribute" ++  (show x) ++ (show y)

run query = do
	print "----------------------------------------------------"
	-- print "SysCat"
	-- sysCat <- readSysCat "declarations.txt"
	sysCat <- readSysCat "crowden.xml"
	-- print sysCat
	print "Query:"
	print query
	-- print "Tokens:"
	let tokens = lexer query
	-- print tokens
	print "Abstract Syntax Tree:"
	let ast = parse tokens
	print ast
	-- print "Type:"
	let (environment, queryType) = typeFullQuery sysCat ast 
	-- print queryType
--	print "Environments:"
--	print environment
	print "---- Operators: ----------"
	let operator = translateFullQuery environment ast
	print operator
	-- bracket (openFile "operators.txt" WriteMode) hClose
	--        (\h -> hPutStrLn h (show operator))

	print "Evaluate"
	results <- evaluate environment operator
	outData results
--	-- let result = evaluateSome operator 20
--        let printResults = (getSome results 20)

-- 	print (displaySome result 20)
	print "-----------------------"


translate query declFile outfile = do
	sysCat <- readSysCat declFile
	let tokens = lexer query
	let ast = parse tokens
	let (environment, queryType) = typeFullQuery sysCat ast 
	let operator = translatePlusFullQuery environment ast
	bracket (openFile outfile WriteMode) hClose
	        (\h -> hPutStrLn h (show operator))
	print "success"

main
 = do 
     args <- getArgs
     print ("Reading "++(head args))
     query <- readFile (head args)
     print query
     let rest = tail args
     let declFile = head rest
     let outfile = (head (tail rest))
     operators <- translate query declFile outfile
     print args
     print "success"

demoAll = do
	sysCat <- readSysCat "declarations.txt"
	print sysCat
	demo basicQuery          
	demo twoExtents
	demo twoExtents2
	demo twoExtents1
	demo simpleQuery 
	demo subInner
	demo subQuery
	demo subQuery2
	demo subQuerySugar
	demo simpleJoinNoWhere 
	demo simpleJoin 
	demo pushQuery
	demo sensorQuery
	demo sensorSubQuery
	demo sensorQuery2
	demo sensorQuery3
	demo sensorSubQuery2
	demo timeSlideRowScope
	demo timeQuery
	demo sensorSubQuery3
	demo tableStreamQuery2
	demo windowJoin
	demo windowJoin2
	demo simpleQuery 
	demo aggQuery
	demo tableStreamQuery
	demo q1
	demo simpleRelation
	print "SUCCESS"
	
demoCQL = do
	-- demo online1 -- can not do user defined functions
	demo online2
	demo online3  

crowdenAll = "select * from crowden;"

crowdenOne = "select * from crowden where id = 1;"

crowden1 = "select * from crowden1;"

crowdenRain = "select rain, id from crowden;"

crowdenIsRain = "select rain, id from crowden where rain > 0;"

crowdenRainAvg = "rstream select avg(rain) as avgrain from crowden[now];"

crowdenRainAvg1 = "rstream select avg(rain) as avgrain from crowden[now] where rain > 0;"

crowdenRainAvg2 = "rstream select * from (select avg(rain) as avgrain from crowden[now]) where rain > 0;"

crowdenRainAvg3 = "rstream select avg(rain) as avgrain from crowden[range 2 hours slide 2 hours];"

crowdenRainAvg4 = "rstream select avg(rain) as avgrain, id from crowden[range 2 hours slide 2 hours] group by id;"

crowdenRainAvg5 = "rstream select avg(rain) as avgrain, id from crowden[range 2 hours slide 2 hours] group by id having avg(rain) > 0;"

crowdenAll2 = "select id, moisture, msp, external, rain, voltage, gsm from crowden;"

crowdenSite1 = "select id, moisture, msp, external, rain, voltage from crowden where id = 1;"

crowdenSite2 = "select id, moisture, msp, external, rain, voltage from crowden where id = 2;"

crowdenSite3 = "select id, moisture, msp, external, rain, voltage from crowden where id = 3;"

crowdenSite4 = "select id, moisture, msp, external, rain, voltage, gsm from crowden where id = 1;"

crowdenAvgOne = "rstream select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden[range 2 hours slide 2 hours] where id = 1;"

crowdenAvg1 = "rstream select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden[range 2 hours slide 2 hours];"

crowdenAvg = "rstream select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden[range 2 hours slide 2 hours];"

crowdenAvgAll = "rstream select id, avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden[range 2 hours slide 2 hours] group by id;"

crowdenBattery = "select id, voltage, msp from crowden where (voltage < 3.3 and msp > 5) or (voltage < 3.25 and msp > 0);"

crowdenAvgWide = "rstream select * from "++inner1++","++inner2++","++inner3++","++inner4++";"

inner1 = "(select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden1[range 2 hours slide 2 hours])"
inner2 = "(select avg((moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden2[range 2 hours slide 2 hours])"
inner3 = "(select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden3[range 2 hours slide 2 hours])"
inner4 = "(select avg(moisture) as avgmoisture, avg(msp) as avgmsp, avg(external) as avgexternal, avg(rain) as avgrain, avg(voltage) as avgvoltage from crowden4[range 2 hours slide 2 hours])"

t1 = "rstream select * from (select avg(moisture) as avgmoisture from crowden1[range 2 hours slide 2 hours]) crowden1,(select avg(moisture) as avgmoisture from crowden2[range 2 hours slide 2 hours]) crowden2;"