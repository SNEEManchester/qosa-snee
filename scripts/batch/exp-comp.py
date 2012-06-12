#!/usr/bin/python
#This script assumes that gnuplot is in the environment PATH
import getopt, logging, sys, SneeqlLib, TossimLib, AvroraLib, os, UtilLib, checkTupleCount, RandomSeeder, networkLib, StatLib, GraphData

optSneeqlRoot = os.getenv("SNEEQLROOT")
optNumAgendaEvals = 2
optQueryDuration = UtilLib.monthsToSeconds(6)
optOutputRoot = os.getenv("HOME")+"/tmp/output"
optLabel = 'qosCmp'
optTimeStampOutput = True
#optTimeStampOutput = False
optDoTossim = False
optDoAvrora = True
optNumAvroraRuns = 1 #The number times the same avrora simulation is repeated
optTossimSyncTime = 4
optDoAvroraCandidates = False
optDoTossimCandidates = False
optDoModel = True

SneeqlLib.optCompileSneeql = True

#Network options
optGenerateRandomNet = False
optSneeqlNetFile = optSneeqlRoot + "/input/networks/10-node-topology.xml"
optAvroraNetFile = None
optNetNumNodes = 10
optNetXDim = 100
optNetYDim = 100

#Schema options
optGenerateRandomSchemas = False
#optNumSchemas = 10
optNumSchemas = 6
optSchemaFile = optSneeqlRoot + "/input/pipes/10-node-schemas.xml"

#Scenario-generation options
#optQueries = ["Q0", "Q2", "Q3temp"]
optQueries = ["Q0"]
optAcqRates = [15000]
optMaxBufferingFactors = [None]
#optMaxBufferingFactors = [1]
optQoS = ["min-acq", "min-delivery", "min-energy", "max-lifetime"]
#optQoS = ["min-energy", "max-lifetime"]

optQoSAwareRouting = False
optQoSAwareWhereScheduling = False
optQoSAwareWhenScheduling = False
optRoutingTreesToGenerate = 50
optRoutingTreesToKeep = 5
optBufferingFactor = None

def usage():
	print 'Usage: regTests.py [-options]'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--short\tdo a short run of the regTests'
	
	print '\nFor all tests:'
	print '--sneeql-root=<dir> \n\tdefault: '+ optSneeqlRoot
	print '--num-agenda-evals=<n> \n\tdefault: ' + str(optNumAgendaEvals)
	print '--query-duration=<secs>' + str(optQueryDuration)
	print '--ouput-root=<dir> \n\tdefault: ' + str(optOutputRoot)
	print '--timestamp-output \n\tdefault: ' + str(optTimeStampOutput)
	print '--do-tossim \n\tdefault: ' + str(optDoTossim)
	print '--do-avrora \n\tdefault: ' + str(optDoAvrora)
	print '--num-avrora-runs \n\tdefault: ' + str(optNumAvroraRuns)
	print '--tossim-sync-time \n\tdefault: ' + str(optTossimSyncTime)
	print '--do-avrora-candidates \n\tdefault: ' + str(optDoAvroraCandidates)
	print '--do-tossim-candidates \n\tdefault: ' + str(optDoTossimCandidates)
	print '--do-model: ' + str(optDoModel)

	print '--generate-random-network=[True|False] \n\tdefault: '+str(optGenerateRandomNet)
	print '\nIf a pre-existing network is to be used:'
	print '--sneeql-network-file=<file> \n\tdefault: '+ str(optSneeqlNetFile)
	print '--avrora-network-file=<file> \n\tdefault: '+ str(optAvroraNetFile) + '\n\tif not provided freespace radio model won\'t be used by Avrora'
	print '\nIf a network is to be generated	:'
	print '--net-num-nodes=<n> \n\tdefault: ' + str(optNetNumNodes)
	print '--net-x-dim=<n> \n\tdefault: ' + str(optNetXDim)
	print '--net-y-dim=<n> \n\tdefault: ' + str(optNetYDim)
	
	print '\nPhysical schema options:'
	print '--generate-random-schemas=[True|False] \n\tdefault: '+ str(optGenerateRandomSchemas)
	print '--num-schemas=<num> \n\tdefault: ' + str(optNumSchemas) + "\n\tonly applies if generate-random-schemas=True"
	print '--schema-file=<file> \n\tdefault: ' + str(optSchemaFile) + "\n\tonly applies if generate-random-schemas=False"
	
	print '\nScenarios to be tested:'
	print '--queries=[Q1..Qn] \n\tdefault: '+ str(optQueries)
	print '--acq-rates=[a1,..,an] \n\t default: ' + str(optAcqRates)
	print '--max-buffering-factors=[b1..bn] \n\t default: ' + str(optMaxBufferingFactors)

	print '--qos-aware-routing=[True|False] \n\tdefault:' + str(optQoSAwareRouting) #Move to sneeqlLib
	print '--routing-trees-to-generate=<n> \n\tdefault: ' + str(optRoutingTreesToGenerate) #Move to sneeqlLib
	print '--routing-trees-to-keep=<n> \n\tdefault: ' + str(optRoutingTreesToKeep) #Move to sneeqlLib
	print '--qos-aware-where-scheduling=[True|False] \n\tdefault:' + str(optQoSAwareWhereScheduling) #Move to sneeqlLib
	print '--qos-aware-when-scheduling=[True|False] \n\tdefault:' + str(optQoSAwareWhenScheduling) #Move to sneeqlLib

	SneeqlLib.usage()
	TossimLib.usage()
	AvroraLib.usage()
	RandomSeeder.usage()

def parseArgs(args):
	global optSneeqlRoot, optNumAgendaEvals, optQueryDuration, optOutputRoot, optLabel, optTimeStampOutput, optDoTossim, optDoAvrora, optNumAvroraRuns, optTossimSyncTime, optDoAvroraCandidates, optDoTossimCandidates, optDoModel, optGenerateRandomNet, optSneeqlNetFile, optAvroraNetFile, optNetNumNodes, optNetXDim, optNetYDim, optGenerateRandomSchemas, optNumSchemas, optSchemaFile, optQueries, optAcqRates, optMaxBufferingFactors, optQoS, optQoSAwareRouting, optRoutingTreesToGenerate, optRoutingTreesToKeep, optQoSAwareWhereScheduling, optQoSAwareWhenScheduling, optBufferingFactor
	
	try:
		optNames = ["help", "sneeql-root=", "num-agenda-evals=", "query-duration=", "output-root=", "label=", "timestamp-output=", "do-tossim=", "do-avrora=", "num-avrora-runs=", "tossim-sync-time=", "do-avrora-candidates=", "do-tossim-candidates=", "do-model="]
		optNames += ["generate-random-network=", "sneeql-network-file=", "avrora-network-file=", "net-num-nodes=", "net-x-dim=", "net-y-dim="]
		optNames += ["generate-random-schemas=", "num-schemas=", "schema-file="]
		optNames += ["queries=", "acq-rates=", "max-buffering-factors="]
		optNames += ["qos-aware-routing=", "routing-trees-to-generate=", "routing-trees-to-keep=", "qos-aware-where-scheduling=", "qos-aware-when-scheduling=","buffering-factor="]
	
		#append the result of getOpNames to all the libraries 
		optNames += SneeqlLib.getOptNames();
		optNames += TossimLib.getOptNames();
		optNames += AvroraLib.getOptNames();
		optNames += RandomSeeder.getOptNames();

		optNames = UtilLib.removeDuplicates(optNames)
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
		
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
	
		if (o == "--sneeql-root"):
			optSneeqlRoot = a
			continue

		if (o == "--num-agenda-evals"):
			optNumAgendaEvals = int(a)
			continue

		if (o == "--query-duration"):
			optQueryDuration = int(a)
			continue

		if (o == "--output-root"):
			optOutputRoot = a
			continue
						
		if (o == "--label"):
			optLabel = a
			continue

		if (o == "--timestamp-output"):
			optTimeStampOutput = a
			continue

		if (o == "--do-tossim"):
			optDoTossim = UtilLib.convertBool(a)
			continue

		if (o == "--do-avrora"):
			optDoAvrora = UtilLib.convertBool(a)
			continue

		if (o == "--num-avrora-runs"):
			optNumAvroraRuns = int(a)
			continue
			
		if (o == "--tossim-sync-time"):
			optTossimSyncTime = int(a)
			continue
			
		if (o == "--do-avrora-candidates"):
			optDoAvroraCandidates = UtilLib.convertBool(a)
			continue
			
		if (o == "--do-tossim-candidates"):
			optDoTossimCandidates = UtilLib.convertBool(a)
			continue

		if (o == "--do-model"):
			optDoModel = UtilLib.convertBool(a)
			continue

		if (o == "--generate-random-network"):
			optGenerateRandomNet = UtilLib.convertBool(a)
			continue
		
		if (o == "--sneeql-network-file"):
			optSneeqlNetFile = a
			continue

		if (o == "--avrora-network-file"):
			optAvroraNetFile = a
			continue

		if (o == "--net-num-nodes"):
			optNetNumNodes = int(a)
			continue		

		if (o == "--net-x-dim"):
			optNetXDim = int(a)
			continue

		if (o == "--net-y-dim"):
			optNetYDim = int(a)
			continue

		if (o == "--generate-random-schemas"):
			optGenerateRandomSchemas = UtilLib.convertBool(a)
			continue

		if (o == "--num-schemas"):
			optNumSchemas = int(a)
			continue
			
		if (o == "--schema-file"):
			optSchemaFile = a
			continue		
			
		if (o == "--queries"):
			optQueries = a.split(",")
			continue
			
		if (o == "--acq-rates"):
			optAcqRates = a.split(",")
			optAcqRates = [int(x) for x in optAcqRates]
			continue

		if (o == "--max-buffering-factors"):
			optMaxBufferingFactors = a.split(",")
			optMaxBufferingFactors = [int(x) for x in optMaxBufferingFactors]
			continue

		if (o == "--qos-aware-routing"):
			optQoSAwareRouting = UtilLib.convertBool(a)
			continue

		if (o == "--routing-trees-to-generate"):	
			optRoutingTreesToGenerate = int(a)
			continue
			
		if (o == "--routing-trees-to-keep"):	
			optRoutingTreesToKeep = int(a)
			continue			
			
		if (o == "--qos-aware-where-scheduling"):
			optQoSAwareWhereScheduling = UtilLib.convertBool(a)
			continue
			
		if (o == "--qos-aware-when-scheduling"):
			optQoSAwareWhenScheduling = UtilLib.convertBool(a)
			continue			
			
		if (o == "--buffering-factor"):
			optBufferingFactor= a
			continue	

	SneeqlLib.setOpts(opts)
	TossimLib.setOpts(opts)
	AvroraLib.setOpts(opts)
	RandomSeeder.setOpts(opts)


#Ouput info message to screen and logger if applicable
def report(message):
 	if (logger != None):
 		logger.info (message)
 	print message


#Ouput warning message to screen and logger if applicable
def reportWarning(message):
 	if (logger != None):
 		logger.warning(message)
 	print message


#Ouput error message to screen and logger if applicable
def reportError(message):
 	if (logger != None):
 		logger.error(message)
 	print message

def startLogger(timeStamp):
	global logger

	logger = logging.getLogger('test')

	#create the directory if required
	if not os.path.isdir(optOutputRoot):
			os.makedirs(optOutputRoot)
			
	hdlr = logging.FileHandler('%s/%s-%s.log' % (optOutputRoot, optLabel, timeStamp))
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting Regression Test')

	#Register the logger with the libraries this module uses
	SneeqlLib.registerLogger(logger)
	TossimLib.registerLogger(logger)
	AvroraLib.registerLogger(logger)
	checkTupleCount.registerLogger(logger)
	RandomSeeder.registerLogger(logger)

#Query 3 has a time window, so the fromWindow parameters needs to be adjusted depending on the acquisition interval
def adjustQ3temp(aqRate):
	fromWindow = ((aqRate * 2) / 1000)
	queryFilePath = "%s/input/pipes/Q3temp.txt" % (optSneeqlRoot)
	qFile = open(queryFilePath, "w")
	queryStr = """
SELECT  RSTREAM OutFlow.time, InFlow.pressure, OutFlow.pressure, OutFlow.id, InFlow.id
FROM    OutFlow[NOW], InFlow[AT now - %d sec];
	""" % (fromWindow)
	qFile.writelines(queryStr)
	qFile.close()
	report("Q3temp adjusted to reflect aquisition interval")
	report(queryStr)
	
#Query 4 has a time window, so the fromWindow parameters needs to be adjusted depending on the acquisition interval
def adjustQ4temp(aqRate):
	fromWindow = ((aqRate * 2) / 1000)
	queryFilePath = "%s/input/pipes/Q4temp.txt" % (optSneeqlRoot)
	qFile = open(queryFilePath, "w")
	queryStr = """
SELECT  RSTREAM InFlow.time, InFlow.pressure, InFlow.id
FROM    InFlow[AT now - %d sec];
	""" % (fromWindow)
	qFile.writelines(queryStr)
	qFile.close()
	report("Q4temp adjusted to reflect aquisition interval")
	report(queryStr)


#Tests a candidate plan using Tossim
def testTossimCandidate(nescDir, traceFilePath, query, numNodes, duration, outputPath, schemaPath, aqRate):

	#compile the nesc
	exitVal = TossimLib.compileNesCCode(nescDir)
	if (exitVal != 0):
		return;		

	#run tossim simulation		
	exitVal = TossimLib.runSimulation(nescDir, traceFilePath, query, numNodes, simulationDuration = (duration + optTossimSyncTime))
	if (exitVal != 0):
		return;

	#check all tuples present in Tossim simulation
	queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.csv" % (outputPath, query)
	
	candidateInfo = getCandidateSummaryInformation(nescDir, query, outputPath)
	alpha = int(candidateInfo['alpha-ms'])
	bufferingFactor = int(candidateInfo['beta'])
	checkTupleCount.checkResults(query, traceFilePath, schemaPath, alpha, bufferingFactor, duration)


#get dictionary with summary information for that candidate
def getCandidateSummaryInformation(nescDir, query, outputPath):

	candidatesSummaryFile = "%s/%s/query-plan/candidates-summary.csv" % (outputPath, query)
	inFile = open(candidatesSummaryFile, 'r')
	first = True
	keys = []
	while 1:
		line = inFile.readline()
		if not line:
			break
		if first:
			keys = line.split(",")
			first = False
			continue
		data = line.split(",")
		if nescDir.find(data[0]) > -1 or (nescDir.find("%s/%s/avrora" % (outputPath, query)) > -1 and data[0].find("Agenda-1")):
			inFile.close()
			candidateInfo = {}
			for i in range(0, len(keys)):
				candidateInfo[keys[i].strip()] = str(data[i].strip())
			return candidateInfo


#Tests a candidate plan using Avrora
def testAvroraCandidate(nescDir, query, outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph):

	#Compile the nesC
	exitVal = AvroraLib.compileNesCCode(nescDir)
	if (exitVal != 0):
		return;		
	AvroraLib.generateODs(nescDir)

	candidateInfo = getCandidateSummaryInformation(nescDir, query, outputPath)
	candidateInfo['total-energy'] = []
	candidateInfo['network-lifetime'] = []
	alpha = int(candidateInfo['alpha-ms'])
	bufferingFactor = int(candidateInfo['beta'])
	duration = int((float(optNumAgendaEvals) * float(alpha) * float(bufferingFactor)) / 1000.0)
	
	for i in range(0, optNumAvroraRuns):
		if optNumAvroraRuns > 1:
			report("Avrora simulation run #%d for candidate %s\n" % (i, outputPath))
	
		#run avrora simulation
		(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(nescDir, outputPath, query, numNodes, simulationDuration = duration, networkFilePath = avroraNetFile)

		#check all tuples present
		queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.txt" % (outputPath, query)
		checkTupleCount.checkResults(query, traceFilePath, schemaPath, alpha, bufferingFactor, duration)	

		#Report total energy consumption
		siteLifetimeRankFile = "%s/site-lifetime-rank.csv" % nescDir
		(sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime) = AvroraLib.computeEnergyValues(nescDir, duration, inputFile = "avrora-out.txt", ignoreLedEnergy = True, siteLifetimeRankFile = siteLifetimeRankFile)

		lifetimeDays = UtilLib.secondsToDays(networkLifetime) #Put this into avrora lib so rank file has it too
		
		report ("The total energy consumption is %f" % (sumEnergy))
		normSumEnergy = (float(sumEnergy) / float(duration)) * float(optQueryDuration) #to do: use new function (below)
		report ("The normalized total energy consumption is %f" % (normSumEnergy))
		report ("The lifetime for this network is %f" % (lifetimeDays))

		candidateInfo['total-energy'].append(float(normSumEnergy))
		candidateInfo['network-lifetime'].append(float(lifetimeDays))

	print "DEBUG: testAvroraCandidate.candidateInfo = " + str(candidateInfo)
	return candidateInfo

#Invoke tossim simulation for each candidate query-plan
def doTossimCandidates(outputPath, query, numNodes, duration, schemaPath, aqRate):
	codeAltDir = "%s/%s/alt" % (outputPath, query)
	#check if there are any candidate plans
	for candDir in os.listdir(codeAltDir):
		if os.path.isdir(codeAltDir + "/" + candDir):
			nescDir = codeAltDir + "/" + candDir + "/tossim"
			traceFilePath = "%s/%s/tossim-trace-%s.txt" % (outputPath, query, candDir)
			report("\nCandidate Plan for %s Tossim simulation\n=================\n" % (candDir))
			testTossimCandidate(nescDir, traceFilePath, query, numNodes, duration, outputPath, schemaPath, aqRate)


#Invoke avrora simulation for each candidate query-plan
def doAvroraCandidates(outputPath, query, numNodes, duration, schemaPath, aqRate, avroraNetFile, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph):
	candidateInfoList = []

	codeAltDir = "%s/%s/alt" % (outputPath, query)
	#check if there are any candidate plans
	if (os.path.exists(codeAltDir)):
		#process each one in turn
		for candDir in os.listdir(codeAltDir):
			nescDir = codeAltDir+ "/" + candDir + "/avrora/"
			if os.path.isdir(nescDir):
				report("\nCandidate Plan for %s Avrora simulation\n=================\n" % (candDir))
				candidateInfo = testAvroraCandidate(nescDir, query, outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph)
				candidateInfoList.append(candidateInfo)
	return candidateInfoList


def getModelQoSMetrics(queryPlanDir, query, outputPath, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph):
	candidateInfoList = []
	
	qosModelFile = "%s/%s/query-plan/model-qos-metrics.csv" % (outputPath, query)
	inFile = open(qosModelFile, 'r')
	first = True
	keys = []
	while 1:
		line = inFile.readline()
		if not line:
			break
		if first:
			keys = line.split(",")
			first = False
			continue
		data = line.split(",")
		candidateInfo = {}
		for i in range(0, len(keys)):
			candidateInfo[keys[i].strip()] = str(data[i].strip())
		#print '*****' + str(candidateInfo) + '*****'
		candidateInfoList.append(candidateInfo)
		
	inFile.close()
	
	return candidateInfoList
	

#Tests a specific scenario in Tossim and Avrora
def testScenario(schemaPath, query, aqRate, numNodes, outputPath, sneeqlNetFile, avroraNetFile, qos):

	report("\n\n*********************************************************************\n")
	report("Testing scenario:\nquery=%s\nschema=%s\naqRate=%d\nnumNodes=%d\noutputPath=%s\n" % (query, schemaPath, aqRate, numNodes, outputPath))

	alphaGraph = StatLib.Dataset()
	deltaGraph = StatLib.Dataset()
	epsilonGraph = StatLib.Dataset()
	lambdaGraph = StatLib.Dataset()

	#Q3temp and Q4temp only: Set the fromWindow parameter depending on the acquisiton rate
	if (query == "Q3temp"):
		adjustQ3temp(aqRate)
	if (query == "Q4temp"):
		adjustQ4temp(aqRate)

	#Set the simulation duration depending on the acquisition rate
	duration = (optNumAgendaEvals * aqRate) / 1000
	
	#query optimizer parameters
	coreParams = ["-nesc-control-radio-off=true", "-nesc-adjust-radio-power=true", "-nesc-power-management=true", "-display-graphs=false", "-display-topology=false", "-sinks=0", "-nesc-led-debug=false", "-network-topology-file="+UtilLib.winpath(sneeqlNetFile), "-targets=none", "-site-resource-file=input/mica2-site-resources.xml", "-display-sensornet-link-properties=true",  "-qos-aware-routing=" + str(optQoSAwareRouting), "-routing-random-seed=4", "-qos-aware-where-scheduling=" + str(optQoSAwareWhereScheduling), "-qos-aware-when-scheduling=" + str(optQoSAwareWhenScheduling)]
	
	if optBufferingFactor!=None:
		coreParams+=["-qos-buffering-factor="+optBufferingFactor]

	if optQoSAwareRouting:
		coreParams+=["-routing-trees-to-generate="+str(optRoutingTreesToGenerate), "-routing-trees-to-keep="+str(optRoutingTreesToKeep)]

	if optQoSAwareWhereScheduling:
		coreParams+=["-where-scheduling-min-nn=15", "-where-scheduling-max-nn=15", "-where-scheduling-heuristic-init-point=true"]		
	
	if (optQoSAwareWhenScheduling):
		coreParams += ["-qos-max-acquisition-interval=%d" % (aqRate)]
	else:
		coreParams += ["-qos-acquisition-interval=%d" % (aqRate)]

	#derive the qos-filename
	qosFileName = "input/qos/qos-spec-%s.xml" % (qos)

	schemaFile = "input/scriptData/" + os.path.split(schemaPath)[1]
	report("using schema file: %s" % schemaFile)
	schemaFileStr = UtilLib.getFileContents(schemaPath)
	report(schemaFileStr)
	scenarioParams = ["-schema-file="+schemaFile, "-query="+SneeqlLib.optQueryDir + "/" + query+".txt", "-output-root-dir="+UtilLib.winpath(outputPath), "-qos-file="+qosFileName]
	
	#compile the query for using the SNEEql optimizer
	queryCompilerParams = coreParams + scenarioParams	
	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, query)
	if (exitVal != 0):
		return

	if (optDoTossim):
		#best plan
		report("\nBest Query Plan Tossim simulation\n=================\n")
		nescDir = "%s/%s/tossim" % (outputPath, query)
		traceFilePath = "%s/tossim-trace.txt" % (outputPath)
		testTossimCandidate(nescDir, traceFilePath, query, numNodes, duration, outputPath, schemaPath, aqRate)
		
		#now do other candidate plans
		if (optDoTossimCandidates):
			doTossimCandidates(outputPath, query, numNodes, duration, schemaPath, aqRate)

	candidateInfoList = []	
	if (optDoAvrora):
		#best plan
		report("\nBest Query Plan Avrora simulation\n=================\n")
		nescDir = "%s/%s/avrora1" % (outputPath, query)
		
		bestCandidateInfo = testAvroraCandidate(nescDir, query, outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph)
		#print "DEBUG bestCandidateInfo = " + str(bestCandidateInfo)
		candidateInfoList.append(bestCandidateInfo)

		#now do other candidate plans	
		if (optDoAvroraCandidates):
			otherCandidateInfoList = doAvroraCandidates(outputPath, query, numNodes, duration, schemaPath, aqRate, avroraNetFile, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph)
			candidateInfoList.extend(otherCandidateInfoList)
			
	if (optDoModel):
		#best plan
		report("\nBest query plan Calculating Model\n=================\n")
		queryPlanDir = "%s/%s/query-plan" % (outputPath, query)
		candidateInfoList = getModelQoSMetrics(queryPlanDir, query, outputPath, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph)
	
	return candidateInfoList
	

def generateScenarioKey(query, i, aqRate, bFactor, qos):
	scenarioKey = "%s-schema%d-aqRate%d-bFactor%d-%s" % (query, i, aqRate, bFactor, qos)
	return scenarioKey
	

#queryDuration in seconds
#alpha in ms
#total Energy in Joules
def normalizeEnergyValue(queryDuration, alpha, beta, totalEnergy):
	return queryDuration * totalEnergy / (alpha/1000.0*beta)

def doGenerateAndTestScenarios(timeStamp, i, schemaPath, numNodes, sneeqlNetFile, avroraNetFile):

	scenarioInfo = {}
	
	#for each query
	for query in optQueries:

		#for each acquisition interval
		for aqRate in optAcqRates:

				for qos in optQoS:

					outputPath = "%s/%s-%s/%s" % (optOutputRoot, optLabel, timeStamp, qos)
					candidateInfoList = testScenario(schemaPath, query, aqRate, numNodes, outputPath, sneeqlNetFile, avroraNetFile, qos)
					#copy the physical schema file to the outputPath
					os.system("cp %s %s/schema%d.xml" % (schemaPath, outputPath, i))
					
					#print "DEBUG adding candidateInfoList = " + str(candidateInfoList)
					scenarioInfo[qos] = candidateInfoList

	#plot graphs here
	#only one scenario per qos
	#lineLabels = ["Query plan optimized for min alpha", "Query plan optimized for min delta", "Query plan optimized for min epsilon", "Query plan optimized for max lambda"]
	lineLabels = scenarioInfo.keys()
	alphaGraph = GraphData.GraphData([" "], lineLabels, " ", "Acquisition interval (s)", " ")
	deltaGraph = GraphData.GraphData([" "], lineLabels, " ", "Delivery Time (s)", " ")
	epsilonGraph = GraphData.GraphData([" "], lineLabels, " ", "Total Energy (J)", " ")
	lambdaGraph = GraphData.GraphData([" "], lineLabels, " ", "Lifetime (days)", " ")

	metrics = {'pi-ms' : alphaGraph, 'delta-ms' : deltaGraph,'total-energy' : epsilonGraph, 'network-lifetime' : lambdaGraph}
	for metric in metrics.keys():
		graph = metrics[metric]
		print '*************************'
		print scenarioInfo
		print '*************************'
		
		for qos in scenarioInfo.keys():
			candidateInfo = scenarioInfo[qos][0]
			
			val = '?'
			if (metric=='total-energy'):
				totalEnergy = candidateInfo[metric]
				val = normalizeEnergyValue(optQueryDuration, float(candidateInfo['alpha-ms']), float(candidateInfo['beta']), float(totalEnergy))
			elif metric=='network-lifetime':
				val = candidateInfo[metric]
			else:
				#either delivery time or acquisition interval
				val = float(candidateInfo[metric])/1000.0
			print "metric " + metric + "=" + str(val)
			
			graph.addPoint(qos, " ", val)
			
		outputPath = "%s/%s-%s/" % (optOutputRoot, optLabel, timeStamp)			
		graph.generatePlotFile(outputPath + metric + ".txt", False)
		
		gnuPlotExe = 'gnuplot'		
		graph.plotGraph(outputPath + metric, "histogram", gnuPlotExe, False, 'top right')
	
	
#Gets network files specified by user, or generates random network
def getNetworkFiles(timeStamp):
	sneeqlNetFile = "%s/input/scriptData/network-%s.xml" % (optSneeqlRoot, timeStamp)
	avroraNetFile = "%s/input/scriptData/network-%s.top" % (optSneeqlRoot, timeStamp)

	if (optGenerateRandomNet):
		field = networkLib.generateRandomTopology(numNodes = optNetNumNodes, xDim= optNetXDim, yDim = optNetYDim)
		if not field.hasAllNodesConnected():
			reportError("Network has disconnected nodes")
			sys.exit(-2)
		field.trimEdgesRandomlyToMeetAverageDegree(6) #TODO: unhardcode this				
		field.generateTopFile(avroraNetFile)
		field.generateSneeqlNetFile(sneeqlNetFile)
		#field.generateTossimNetFile(optOutputFile+".nss") #TODO
	else:
		print "cp %s %s" % (optSneeqlNetFile, sneeqlNetFile)
		if os.path.isfile(optSneeqlNetFile):
			os.system("cp %s %s" % (optSneeqlNetFile, sneeqlNetFile))
		else:
			reportError("Sneeql network file %s not found")
		if optAvroraNetFile == None:
			avroraNetFile = None
		else:
			if os.path.isfile(optAvroraNetFile):
				os.system("cp %s %s" % (optAvroraNetFile, avroraNetFile))
			else:
				reportError("Avrora network file %s not found")

		#TODO: need to write method which reads Sneeql net file and returns numNodes, for now using optNumNodes
		#numNodes = TossimLib.getNumNodes(optSneeqlRoot + "/" + optTopologyFile)
		#numNodes=10 #TODO: unhardcode this
		#print "numNodes=%d" % numNodes

	return (sneeqlNetFile, avroraNetFile, optNetNumNodes)


#Generates different scenarios to be tested
def generateAndTestScenarios(timeStamp):

	(sneeqlNetFile, avroraNetFile, numNodes) = getNetworkFiles(timeStamp)

	if (optGenerateRandomSchemas):
		#Only Q3Temp needs OutFlow
		if (optQueries.count('Q3temp') > 0):
			streams = {"InFlow" : ["pressure"], "OutFlow" : ["pressure"]}
		else:	
			streams = {"InFlow" : ["pressure"]}

		#for each random schema
		for i in range(0, optNumSchemas):
			schemaPath = "%s/input/scriptData/schema%d.xml" % (optSneeqlRoot, i)
			SneeqlLib.generateRandomSchema(streams, schemaPath, True, numNodes)
			doGenerateAndTestScenarios(timeStamp, i, schemaPath, numNodes, sneeqlNetFile, avroraNetFile)
	else:
		if os.path.isfile(optSchemaFile):
			schemaPath = "%s/input/scriptData/schema1.xml" % (optSneeqlRoot)
			os.system("cp %s %s" % (optSchemaFile, schemaPath))
			doGenerateAndTestScenarios(timeStamp, 1, schemaPath, numNodes, sneeqlNetFile, avroraNetFile)
		else:
			reportError("Error finding physical schema file " + optSchemaFile)
			sys.exit(2)
			
	os.system("mv %s %s/%s-%s" % (sneeqlNetFile, optOutputRoot, optLabel, timeStamp))
	if (avroraNetFile != None):
		os.system("mv %s %s/%s-%s" % (avroraNetFile, optOutputRoot, optLabel, timeStamp))


def main(): 	

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	timeStamp = UtilLib.getTimeStamp()
	if (not optTimeStampOutput):
		timeStamp = ""
	startLogger(timeStamp)
	
	RandomSeeder.setRandom()

	#TODO: get SVN latest version

	#compile java
	SneeqlLib.compileQueryOptimizer()
	
	generateAndTestScenarios(timeStamp)

if __name__ == "__main__":
	main()
