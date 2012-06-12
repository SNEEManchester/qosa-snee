#!/usr/bin/python
#Generates scenarios for QoS-awareness experiments
#Scenarios 1 (exp1,2): random <query, schema, network> 30 times
#Scenarios 2 (exp3): fixed query, schema min/maj, queries with different densities
#NB: outputs dirs assumed to exist
#TODO: Move query generator out to a separate library
import os, random, RandomSeeder, math, networkLib, SneeqlLib, sys, getopt, UtilLib

#optNumNodes = 30
#Used for scaling experiments
optNumNodes = 100
optScenariosFile = 'scenarios.csv'

#Scenarios 1
optNumScenarios = 30 

#Scenarios with max 30 nodes
#optOutputDir1 = os.getenv('HOME')+os.sep+"tmp"+os.sep+"results"+os.sep+"scenarios1"

#Scenarios with max 100 nodes
optOutputDir1 = os.getenv('HOME')+os.sep+"tmp"+os.sep+"results"+os.sep+"scenarios100"

#Scenarios 2
optPercentSources = [50,100]
optRValues = [1, 6, 30]
optNumNetworksPerNetworkType = 5
optOutputDir2 = os.getenv('HOME')+os.sep+"tmp"+os.sep+"results"+os.sep+"scenarios2"

#Query generation
optMaxQueryNesting = 3
optMaxSourcesPerQueryLevel = 3
optProbabilitySubquery = 0.5 #prob that each source in a query level is a sub-query
optProbabilityAggregate = 0.3 #prob that aggregate is projected in query level

#network generation
optRadioRange = 60

def parseArgs(args):	
	global optOutputDir1, optOutputDir2
	try:
		optNames = []
	
		#append the result of getOpNames to all the libraries 
		optNames += SneeqlLib.getOptNames();
		optNames = UtilLib.removeDuplicates(optNames)
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
			
	SneeqlLib.setOpts(opts)
	if SneeqlLib.optCygwin:
		optOutputDir1 = "c:/ixent/tmp/results/scenarios1"
		optOutputDir2 = "c:/ixent/tmp/results/scenarios2"

def getNextQueryLevel(nextExtent, nextSubQuery, topLevel, levelQualifier, extentsUsed, indentLevel):

    numSources = random.randrange(1, optMaxSourcesPerQueryLevel, 1)
    selectClause = "SELECT\t"
    fromClause = "%sFROM\t" % ("\t" * indentLevel)
    whereClause = "\t" * indentLevel

    qualifier = None
    prevQualifier = None
    attribute = None
    prevAttribute = None

    for i in range(0, numSources):
        if (i>0):
            fromClause += ",\n%s" % ("\t" * (indentLevel+1))

        if (random.random() <= optProbabilitySubquery or indentLevel >= optMaxQueryNesting):
            qualifier = "%snow" % (chr(nextExtent).lower())
            attribute = 'x'
            fromClause += "%s[NOW] %s" % (chr(nextExtent), qualifier)
            extentsUsed.extend(chr(nextExtent))
            nextExtent += 1
        else:
            qualifier = "sq%d" % (nextSubQuery)
            attribute = "%sx" % (qualifier)
            nextSubQuery += 1
            (subQuery, nextExtent, nextSubQuery, extentsUsed) = getNextQueryLevel(nextExtent, nextSubQuery, False, qualifier, extentsUsed, (indentLevel+1))
            fromClause += "(%s) %s" % (subQuery, qualifier) 
        if i==0:
            if topLevel:
                selectClause += "RSTREAM "
            if (random.random() > optProbabilityAggregate):
                selectClause += "AVG(%s.%s) as %sx" % (qualifier, attribute, levelQualifier)
            else:
                selectClause += "%s.%s as %sx" % (qualifier, attribute, levelQualifier)
        if i==1:
            whereClause = "\n%sWHERE\t" % ("\t" * indentLevel)
        if i>1:
            whereClause += "%sAND\t" % ("\t" * indentLevel)
        if i>0:
            
            whereClause += "%s.%s=%s.%s" % (prevQualifier, prevAttribute, qualifier, attribute)
        prevQualifier = qualifier
        prevAttribute = attribute

    semiColon=""
    if topLevel:
        semiColon=";\n"
    levelQuery = "%s\n%s%s%s" % (selectClause, fromClause, whereClause, semiColon)
    return (levelQuery, nextExtent, nextSubQuery, extentsUsed)

#Returns a list of the extents used in the query to aid schema generation
def generateRandomQuery(queryId = 'q1', outputDir = '.'):
    nextExtent = ord('A')
    nextSubQuery = 1
    topLevel = True
    extentsUsed = []

    (query, nextExtent, nextSubQuery, extentsUsed)  = getNextQueryLevel(nextExtent, nextSubQuery, True, "q", extentsUsed,0)

    outFile = open(outputDir+os.sep+queryId+".txt", 'w')
    outFile.writelines(query)
    return extentsUsed

def generatePhysicalSchema(numNodes, percentSources, extentList, schemaId, outputDir):
    numSources = int(math.ceil(float(numNodes)*(percentSources/100.0)))
    nodes = range(0,numNodes)
    sourceNodes = random.sample(nodes, numSources)
    numExtents = len(extentList)

    schemaStr = """<?xml version="1.0" encoding="UTF-8"?>
<schema>
"""

#    print "numSources="+str(numSources)
#    print "numExtents="+str(numExtents)

    step = float(numSources)/float(numExtents)
    s = 0.0
    e = 0
    while (s<numSources and e<numExtents):
        start = int(s)
        end = max(start + 1, int(s + step) + 1)
        extent = extentList[e]
        extentSources = sourceNodes[start:end]

        print "s="+str(s)

        schemaStr += """\t<stream name="%s">
\t\t<column name="x">
\t\t\t<type class ="integer"/>
\t\t</column>
\t\t<sites>%s</sites>
     </stream>
""" % (extent, ",".join(map(str,extentSources)))
        s += step
        e += 1

        print "new s="+str(s)

    schemaStr += "</schema>\n"
 
    outFile = open(outputDir+os.sep+schemaId+".xml", 'w')
    outFile.writelines(schemaStr)
    outFile.close()


def getCandidateNode(nodes, rValue, id):
        randomNode = random.sample(nodes, 1)[0]
        angle = random.randrange(0,360,1)
        dx = math.floor(math.cos(angle)*(optRadioRange/float(rValue)))
        dy = math.floor(math.sin(angle)*(optRadioRange/float(rValue)))
        n = networkLib.Node(id, int(randomNode.xPos + dx), int(randomNode.yPos + dy))
        return n

def generateSpiderNetwork(numNodes, rValue, networkId, outputDir):

    minx = 0
    miny = 0
    maxx = 0
    maxy = 0

    sink = networkLib.Node(0, 0, 0)
    nodes = [sink]
    dupsCheckList = ["0_0"]

    for i in range(0, numNodes):
        success = False
        while success==False:
            n = getCandidateNode(nodes, rValue, i)
            key = "%d_%d" % (n.xPos, n.yPos)
            if not (key in dupsCheckList):
#                print dupsCheckList
                success = True
                dupsCheckList += [key]
                print "x=%d,y=%d" % (n.xPos, n.yPos)
                minx = min(minx, n.xPos)
                miny = min(miny, n.yPos)
                maxx = max(maxx, n.xPos)
                maxy = max(maxy, n.yPos)
                nodes += [n]

    xDim = maxx - minx
    yDim = maxy - miny
        
    f = networkLib.Field(xDim, yDim, minx, miny)
    for n in nodes:
        f.addNode(n.id, n.xPos, n.yPos)

    f.trimEdgesRandomlyToMeetAverageDegree(6) #TODO: unhardcode this	
    f.generateSneeqlNetFile(outputDir+os.sep+networkId+".xml")
    f.generateTopFile(outputDir+os.sep+networkId+".top")


#30 random queries/networks/schemas
def generateScenarios1(numScenarios, numNodes, outputDir, scenariosFile):
    outFile = open(outputDir+os.sep+scenariosFile, 'w')
    outFile.writelines("scenarioId,queryId,networkId,numNodes,rValue,schemaId,percentSources\n")

    for i in range(0, numScenarios):
        percentSources = random.randrange(30, 100, 1)
        rValue = random.randrange(1, 5, 1)

        queryId = 'q%d' % (i)
        schemaId = 's%dpc_%d' % (percentSources, i)
        networkId = 'n_n%d_r%d_%d' % (numNodes, rValue, i)
    
        extentList = generateRandomQuery(queryId, outputDir)
        generatePhysicalSchema(numNodes, percentSources, extentList, schemaId, outputDir)
        generateSpiderNetwork(numNodes, rValue, networkId, outputDir)
    
        scenarioStr = "%d,%s,%s,%s,%s,%s,%s\n" % (i, queryId, networkId, numNodes, rValue, schemaId, percentSources)
        outFile.writelines(scenarioStr)
    outFile.close()

def generateScenarios2(numScenarios, numNodes, outputDir, scenariosFile, percentSources, rValues, numNetworksPerNetworkType):
    outFile = open(outputDir+os.sep+scenariosFile, 'w')
    outFile.writelines("scenarioId,queryId,networkId,numNodes,rValue,schemaId,percentSources\n")

    queryId = "q1"
    queryText = """SELECT RSTREAM anow.x
FROM A[now] anow;
"""
    queryFile = open(outputDir+os.sep+queryId+".txt","w")
    queryFile.writelines(queryText)
    queryFile.close()

    for s in percentSources:
        schemaId = "s_%dpc" % (s)
        generatePhysicalSchema(numNodes, s, ["A"], schemaId, outputDir)        
    
    for r in rValues:
        for n in range(0,numNetworksPerNetworkType):
            networkId = "n_n%d_r%d_%d" % (numNodes, r, n)
            generateSpiderNetwork(numNodes, r, networkId, outputDir)            

    i = 0;
    for r in rValues:
        for n in range(0,numNetworksPerNetworkType):
            networkId = "n_n%d_r%d_%d" % (numNodes, r, n)
            for s in percentSources:
                schemaId = "s_%dpc" % (s)
                scenarioStr = "%d,%s,%s,%s,%s,%s,%s\n" % (i, queryId, networkId, numNodes, r, schemaId, s)
                outFile.writelines(scenarioStr)
                i += 1
    outFile.close()

def main():
    #parse the command-line arguments
    parseArgs(sys.argv[1:]) 

    random.seed(1)
    generateScenarios1(optNumScenarios, optNumNodes, optOutputDir1, optScenariosFile)

    #Not used for journal paper
    #generateScenarios2(optNumScenarios, optNumNodes, optOutputDir2, optScenariosFile, optPercentSources, optRValues, optNumNetworksPerNetworkType)

if __name__ == "__main__":
	main()
