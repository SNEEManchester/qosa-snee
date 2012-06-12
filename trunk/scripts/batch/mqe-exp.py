#!/usr/bin/python
import getopt, logging, sys, SneeqlLib, TossimLib, AvroraLib, os, UtilLib, checkTupleCount, RandomSeeder, networkLib, StatLib, GraphData 

#This script compiles individual query plans and an MQE plan for each query in the querySet, runs Avrora simulations to obtain energy measurement values, and plots an energy consumption graph for each Avrora simulation.


#TODO: unhardcode
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'

optSneeqlRoot = os.getenv("SNEEQLROOT")
#optNumEpochs = 20
optNumEpochs = 10
optOutputRoot = "/cygdrive/c/tmp/output" #full absolute path needed here in Cygwin format /cygdrive/c/
optTimeStampOutput = True
#optTimeStampOutput = False
optDoTossim = True
optDoAvrora = True
optNumAvroraRuns = 1 #The number times the same avrora simulation is repeated
optTossimSyncTime = 4
optDoAvroraIndividualQueries = True
optDoTossimIndividualQueries = False

#Network options
optGenerateRandomNet = False
optSneeqlNetFile = optSneeqlRoot + "/input/10-node-topology.xml"
optAvroraNetFile = None
optNetNumNodes = 10
optNetXDim = 100
optNetYDim = 100

#Schema options
optGenerateRandomSchemas = False
#optNumSchemas = 10
optNumSchemas = 6
optSchemaFile = optSneeqlRoot + "/input/10-node-schemas-indep.xml"

#Options which are fixed for all scenarios
optQuerySet = ["Q1","Q11"]
optSinks = "0,1" #need to specify a sink per query
optMaxDeliveryTime = 60000

#Scenario-generation options
optAcqRates = [3000]
optMaxBufferingFactors = [1]

def usage():
	print 'Usage: exp-rt-score.py [-options]'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--short\tdo a short run of the regTests'
	
	print '\nFor all tests:'
	print '--sneeql-root=<dir> \n\tdefault: '+ optSneeqlRoot
	print '--num-epochs=<sec> \n\tdefault: ' + str(optNumEpochs)
	print '--ouput-root=<dir> \n\tdefault: ' + str(optOutputRoot)
	print '--timestamp-output \n\tdefault: ' + str(optTimeStampOutput)
	print '--do-tossim \n\tdefault: ' + str(optDoTossim)
	print '--do-avrora \n\tdefault: ' + str(optDoAvrora)
	print '--num-avrora-runs \n\tdefault: ' + str(optNumAvroraRuns)
	print '--tossim-sync-time \n\tdefault: ' + str(optTossimSyncTime)
	print '--do-avrora-individual-queries \n\tdefault: ' + str(optDoAvroraIndividualQueries)
	print '--do-tossim-individual-queries \n\tdefault: ' + str(optDoTossimIndividualQueries)

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
	print '--query-set=[Q1,Q2,..Qn] \n\tdefault: '+ str(optQuerySet)
	print '--sinks=[N1,N2,..Nn] \n\tdefault: '+ str(optSinks)
	print '--acq-rates=[a1,..,an] \n\t default: ' + str(optAcqRates)
	print '--max-buffering-factors=[b1..bn] \n\t default: ' + str(optMaxBufferingFactors)
	print '--max-delivery-time=[D1..Dn] \n\t default: ' + str(optMaxDeliveryTime)

	SneeqlLib.usage()
	TossimLib.usage()
	AvroraLib.usage()
	RandomSeeder.usage()


def parseArgs(args):
	global optSneeqlRoot, optNumEpochs, optOutputRoot, optTimeStampOutput, optDoTossim, optDoAvrora, optNumAvroraRuns, optTossimSyncTime, optDoAvroraIndividualQueries, optDoTossimIndividualQueries, optGenerateRandomNet, optSneeqlNetFile, optAvroraNetFile, optNetNumNodes, optNetXDim, optNetYDim, optGenerateRandomSchemas, optNumSchemas, optSchemaFile, optQuerySet, optSinks, optAcqRates, optMaxBufferingFactors, optMaxDeliveryTime
	
	try:
		optNames = ["help", "short", "sneeql-root=", "num-epochs=", "output-root=", "timestamp-output=", "do-tossim=", "do-avrora=", "num-avrora-runs=", "tossim-sync-time=", "do-avrora-individual-queries=", "do-tossim-individual-queries="]
		optNames += ["generate-random-network=", "sneeql-network-file=", "avrora-network-file=", "net-num-nodes=", "net-x-dim=", "net-y-dim="]
		optNames += ["generate-random-schemas=", "num-schemas=", "schema-file="]
		optNames += ["query-set=", "sinks=", "acq-rates=", "max-buffering-factors=", "max-delivery-time="]
	
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

		if (o == "--num-epochs"):
			optNumEpochs = int(a)
			continue

		if (o == "--output-root"):
			optOutputRoot = a
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
			
		if (o == "--do-avrora-individual-queries"):
			optDoAvroraIndividualQueries = UtilLib.convertBool(a)
			continue
			
		if (o == "--do-tossim-individual-queries"):
			optDoTossimIndividualQueries = UtilLib.convertBool(a)
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
			
		if (o == "--query-set"):
			optQuerySet = a.split(",")
			continue

		if (o == "--sinks"):
			optSinks = str(a)
			continue
			
		if (o == "--acq-rates"):
			optAcqRates = a.split(",")
			optAcqRates = [int(x) for x in optAcqRates]
			continue

		if (o == "--max-buffering-factors"):
			optMaxBufferingFactors = a.split(",")
			optMaxBufferingFactors = [int(x) for x in optMaxBufferingFactors]
			continue
			
		if (o == "--max-delivery-time"):
			optMaxDeliveryTime = int(a)
			continue	
			
		if (o == "--short"):
			optQuerySet = ["Q0"]
			optNumSchemas = 1
			optAcqRates = [5000]
			optMaxBufferingFactors = [1]
			optDoTossimIndividualQueries = False
			optDoAvroraIndividualQueries = False
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
			
	hdlr = logging.FileHandler('%s/test%s.log' % (optOutputRoot, timeStamp))
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
	#TODO: We can't do this for now because MQE branch doesn't produce query plan summary
	#queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.csv" % (outputPath, query)
	#actualBFactor = SneeqlLib.getBufferingFactor(queryPlanSummaryFile)
	#checkTupleCount.checkResults(query, traceFilePath, schemaPath, aqRate, actualBFactor, duration)


#get dictionary with summary information for that candidate
#TODO: move this to SNEEqlLib.py?
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
		print "nescDir=" + nescDir + " data[0]=" +  data[0]
		bestCandidateDirSuffix = "%s/%s/avrora" % (outputPath, query)
		altCandidateDirSuffix = data[0]+"/avrora/"
		if (nescDir.find(bestCandidateDirSuffix) > -1 and data[0].endswith("Agenda-1")) or nescDir.endswith(altCandidateDirSuffix):
			inFile.close()
			candidateInfo = {}
			for i in range(0, len(keys)):
				candidateInfo[keys[i].strip()] = str(data[i].strip())
			return candidateInfo


#Tests a candidate plan using Avrora
def evaluateAvroraQueryPlan(nescDir, query, outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, energyGraph):

	#Compile the nesC
	exitVal = AvroraLib.compileNesCCode(nescDir)
	if (exitVal != 0):
		return;		
	AvroraLib.generateODs(nescDir)

	for i in range(0, optNumAvroraRuns):
		if optNumAvroraRuns > 1:
			report("Avrora simulation run #%d for candidate %s\n" % (i, outputPath))
	
		#run avrora simulation
		(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(nescDir, outputPath+'/'+query+'/avrora', query, numNodes, simulationDuration = duration, networkFilePath = avroraNetFile)

		#check all tuples present
		#TODO: We can't do this for now because MQE branch doesn't produce query plan/candidate summary
		#queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.txt" % (outputPath, query)
		#actualBFactor = SneeqlLib.getBufferingFactor(queryPlanSummaryFile)
		#checkTupleCount.checkResults(query, traceFilePath, schemaPath, aqRate, actualBFactor, duration)	

		#Report total energy consumption
		siteLifetimeRankFile = "%s/site-lifetime-rank.csv" % nescDir
		(sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime) = AvroraLib.computeEnergyValues(nescDir, duration, inputFile = "avrora-out.txt", ignoreLedEnergy = True, siteLifetimeRankFile = siteLifetimeRankFile)
		report ("The total energy consumption is %f" % (sumEnergy))
		report ("The lifetime for this network is %f" % (networkLifetime))

		#candidateInfo = getCandidateSummaryInformation(nescDir, query, outputPath)
		#print candidateInfo
		
		energyGraph.addPoint("Sensor",query,str(sensorEnergy))
		energyGraph.addPoint("CPU",query,str(cpu_cycleEnergy))
		energyGraph.addPoint("Radio",query,str(radioEnergy))
		energyGraph.addPoint("Other",query,str(otherEnergy))
		
#		rtID = candidateInfo['rt-id'].split("-")[2]
#		#TODO: Cross-candidate comparison: plot acquisition interval score vs min possible acq interval
#		alphaGraph.add(float(candidateInfo['rt-alpha-score']),float(candidateInfo['pi-ms'])/1000.0,rtID) 
#		#TODO: Cross-candidate comparison: plot delivery time score vs simulated total energy
#		deltaGraph.add(float(candidateInfo['rt-delta-score']),float(candidateInfo['delta-ms'])/1000.0,rtID)
#		#TODO: Cross-candidate comparison: plot total energy score vs simulated total energy
#		epsilonGraph.add(float(candidateInfo['rt-epsilon-score']),float(sumEnergy),rtID)
#		#TODO: Cross-candidate comparison: plot lifetime score vs simulated lifetime
#		lambdaGraph.add(float(candidateInfo['rt-lambda-score']),UtilLib.secondsToDays(networkLifetime),rtID)


#Invoke tossim simulation for each candidate query-plan
#def doTossimCandidates(outputPath, query, numNodes, duration, schemaPath, aqRate):
#	codeAltDir = "%s/%s/alt" % (outputPath, query)
#	#check if there are any candidate plans
#	for candDir in os.listdir(codeAltDir):
#		if os.path.isdir(codeAltDir + "/" + candDir):
#			nescDir = codeAltDir + "/" + candDir + "/tossim"
#			traceFilePath = "%s/%s/tossim-trace-%s.txt" % (outputPath, query, candDir)
#			report("\nCandidate Plan for %s Tossim simulation\n=================\n" % (candDir))
#			testTossimCandidate(nescDir, traceFilePath, query, numNodes, duration, outputPath, schemaPath, aqRate)


#Invoke avrora simulation for each candidate query-plan
def doAvroraIndividualQueries(outputPath, querySet, numNodes, duration, schemaPath, aqRate, avroraNetFile, energyGraph):
	for query in querySet:
		avroraDir = "%s/%s/avrora/" % (outputPath, query)
		if os.path.isdir(avroraDir):
			report("\nPlan for individual query %s Avrora simulation\n=================\n" % (query))
			evaluateAvroraQueryPlan(avroraDir, query, outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, energyGraph)


#draw the output graphs
def generateGraph(energyGraph, outputPath):
	energyGraph.generatePlotFile(outputPath+'/energy.txt',False)
	energyGraph.plotGraph(outputPath+'energy','histograms',gnuPlotExe, False, 'top right',True)


def initializeGraph():

	energyGraph = GraphData.GraphData(["mqe"]+optQuerySet,["Sensor","CPU","Radio","Other"],"QuerySet")
	energyGraph.yLabel = 'Enery used'
	energyGraph.setTitle  ('QuerySet vs. Energy Used', "")
	return energyGraph


#Tests a specific scenario in Tossim and Avrora
def testScenario(schemaPath, aqRate, bFactor, numNodes, outputPath, sneeqlNetFile, avroraNetFile):

	global optDoTossim, optDoAvrora, optMaxDeliveryTime, optSinks

	report("\n\n*********************************************************************\n")
	report("Testing scenario:\nquerySet=%s\nschema=%s\naqRate=%d\nbFactor=%d\nnumNodes=%d\noutputPath=%s\n" % (optQuerySet, schemaPath, aqRate, bFactor, numNodes, outputPath))

	energyGraph = initializeGraph()

	#Q3temp and Q4temp only: Set the fromWindow parameter depending on the acquisiton rate
	#TODO: Sort this out later
	#if (query == "Q3temp"):
	#	adjustQ3temp(aqRate)
	#if (query == "Q4temp"):
	#	adjustQ4temp(aqRate)

	#Set the simulation duration depending on the acquisition rate
	duration = (optNumEpochs * aqRate) / 1000
	
	#query optimizer parameters
	coreParams = ["-nesc-control-radio-off=true", "-nesc-adjust-radio-power=true", "-nesc-power-management=true", "-display-graphs=false", "-qos-aware-partitioning=false", "-qos-aware-routing=false", "-qos-aware-where-scheduling=false", "-qos-aware-when-scheduling=false", "-sinks="+optSinks, "-nesc-led-debug=false", "-network-topology-file="+UtilLib.winpath(sneeqlNetFile), "-nesc-generate-tossim-code=" + str(optDoTossim), "-nesc-generate-avrora-code="+ str(optDoAvrora), "-site-resource-file=input/mica2-site-resources.xml", "-display-sensornet-link-properties=true", "-qos-max-delivery-time="+str(optMaxDeliveryTime)]

	#Add the query directory
	#coreParams += ["-query-dir="+SneeqlLib.optQueryDir]
	coreParams += ["-query-dir=input/queries"]

	schemaFile = "input/scriptData/" + os.path.split(schemaPath)[1]
	report("using schema file: %s" % schemaFile)
	schemaFileStr = UtilLib.getFileContents(schemaPath)
	report(schemaFileStr)
	
	querySetStr = ",".join(["%s%s" % (q,".txt") for q in optQuerySet])
	
	#TODO: create bFactor string
	scenarioParams = ["-schema-file="+schemaFile, "-query="+querySetStr, "-qos-acquisition-interval=%d" % (aqRate), "-qos-max-buffering-factor=%d,%d" % (bFactor,bFactor), "-output-root-dir="+UtilLib.winpath(outputPath)]	

	#compile the query for using the SNEEql optimizer
	queryCompilerParams = coreParams + scenarioParams	
	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, querySetStr)
	if (exitVal != 0):
		return;

	if (optDoTossim):
		#MQE plan
		report("\nMQE Query Plan Tossim simulation\n=================\n")
		nescDir = "%s/mqe/tossim" % (outputPath)
		traceFilePath = "%s/tossim-trace.txt" % (outputPath)
		testTossimCandidate(nescDir, traceFilePath, "mqe", numNodes, duration, outputPath, schemaPath, aqRate)
		
		#now do other candidate plans
		#if (optDoTossimIndividualQueries):
		#	doTossimIndividualQueries(outputPath, optQuerySet, numNodes, duration, schemaPath, aqRate)
		
	if (optDoAvrora):
		#MQE plan
		report("\nMQE Query Plan Avrora simulation\n=================\n")
		nescDir = "%s/mqe/avrora" % (outputPath)
		evaluateAvroraQueryPlan(nescDir, "mqe", outputPath, numNodes, duration, schemaPath, aqRate, avroraNetFile, energyGraph)

		#now do the individual queries
		if (optDoAvroraIndividualQueries):
			doAvroraIndividualQueries(outputPath, optQuerySet, numNodes, duration, schemaPath, aqRate, avroraNetFile, energyGraph)
	
	generateGraph(energyGraph, outputPath)


def doGenerateAndTestScenarios(timeStamp, i, schemaPath, numNodes, sneeqlNetFile, avroraNetFile):
	#for each query
	#for querySet in optQuerySet:

	#for each acquisition interval
	for aqRate in optAcqRates:

		#for each buffering factor
		for bFactor in optMaxBufferingFactors:
			outputPath = "%s/%s/schema%d-aqRate%d-bFactor%d" % (optOutputRoot, timeStamp, i, aqRate, bFactor)

			testScenario(schemaPath, aqRate, bFactor, numNodes, outputPath, sneeqlNetFile, avroraNetFile)
			#copy the physical schema file to the outputPath
		os.system("cp %s %s/schema%d.xml" % (schemaPath, outputPath, i))


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
		print "copying network file"
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
		if (optQuerySet.count('Q3temp') > 0):
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
			
	os.system("mv %s %s/%s" % (sneeqlNetFile, optOutputRoot, timeStamp))
	if (avroraNetFile != None):
		os.system("mv %s %s/%s" % (avroraNetFile, optOutputRoot, timeStamp))


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
