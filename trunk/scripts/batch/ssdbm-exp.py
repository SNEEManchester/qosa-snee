#!/usr/bin/python
import getopt, logging, os, random, re, sys, SneeqlLib, TossimLib, AvroraLib, UtilLib, checkTupleCount, RandomSeeder, networkLib, GraphData

optSneeqlRoot = os.getenv("SNEEQLROOT")
optSimDuration = 600
optOutputRoot = "/cygdrive/c/tmp/output" #full absolute path needed here in Cygwin format /cygdrive/c/
optTimeStampOutput = True
#optTimeStampOutput = False
optDoTossim = False
optDoAvrora = True
optNumAvroraRuns = 1 #The number times the same avrora simulation is repeated
optTossimSyncTime = 4
optDoAvroraCandidates = False
optDoTossimCandidates = False

#Network options
optSneeqlNetFile = optSneeqlRoot + "/input/Networks/ssdbm-10-node-network.nss"
optNetNumNodes = 10

#Schema options
optSchemaFile = optSneeqlRoot + "/input/Pipes/ssdbm-10-node-schemas.xml"

#Scenario-generation options
optQueries = ["Q0", "Q2", "Q3temp"]
#optQueries = ["Q0"]
optAcqRates = [5000, 20000]
#optAcqRates = [2000, 5000, 10000, 20000, 30000]
optMaxBufferingFactors = [1]
optDeliveryTimes = [None]
optTestAll = True

#Experiment Options
optXValType = 'acq'
optLedDebug = False

#TODO: unhardcode
batteryEnergy = 31320

#TODO: unhardcode
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'

def usage():
	print 'Usage: regTests.py [-options]'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--short\tdo a short run of the regTests'
	
	print '\nFor all tests:'
	print '--sneeql-root=<dir> \n\tdefault: '+ optSneeqlRoot
	print '--sim-duration=<sec> \n\tdefault: ' + str(optSimDuration)
	print '--ouput-root=<dir> \n\tdefault: ' + str(optOutputRoot)
	print '--timestamp-output \n\tdefault: ' + str(optTimeStampOutput)
	print '--do-tossim \n\tdefault: ' + str(optDoTossim)
	print '--do-avrora \n\tdefault: ' + str(optDoAvrora)
	print '--num-avrora-runs \n\tdefault: ' + str(optNumAvroraRuns)
	print '--tossim-sync-time \n\tdefault: ' + str(optTossimSyncTime)
	print '--do-avrora-candidates \n\tdefault: ' + str(optDoAvroraCandidates)
	print '--do-tossim-candidates \n\tdefault: ' + str(optDoTossimCandidates)

	print '--sneeql-network-file=<file> \n\tdefault: '+ str(optSneeqlNetFile)
	print '--net-num-nodes=<n> \n\tdefault: ' + str(optNetNumNodes)
	
	print '\nPhysical schema options:'
	print '--schema-file=<file> \n\tdefault: ' + str(optSchemaFile) + "\n\tonly applies if generate-random-schemas=False"
	
	print '\nScenarios to be tested:'
	print '--queries=[Q1..Qn] \n\tdefault: '+ str(optQueries)
	print '--acq-rates=[a1,..,an] \n\t default: ' + str(optAcqRates)
	print '--max-buffering-factors=[b1..bn] \n\t default: ' + str(optMaxBufferingFactors)
	print '--delivery-times=[D1..Dn] \n\t default: ' + str(optDeliveryTimes)
	print '--test-all=[True|False] \n\tdefault: '+ str(optTestAll)

	print '\nExperiment Parameters:'
	print '--x-val-type=[acq|del|bf]' + str(optXValType)
	print '--led-debug\n\tdefault: ' + str(optLedDebug)
	
	SneeqlLib.usage()
	TossimLib.usage()
	AvroraLib.usage()
	RandomSeeder.usage()
	checkTupleCount.usage()

def parseArgs(args):
	global optSneeqlRoot, optSimDuration, optOutputRoot
	global optTimeStampOutput, optDoTossim, optDoAvrora 
	global optTossimSyncTime, optDoAvroraCandidates, optDoTossimCandidates
	global optSneeqlNetFile
	global optNetNumNodes
	global optSchemaFile 
	global optQueries, optAcqRates, optMaxBufferingFactors, optDeliveryTimes
	global optTestAll, optLedDebug, optXValType
	
	try:
		optNames = ["help", "short", "sneeql-root=", "sim-duration=", "output-root="]
		optNames += ["timestamp-output=", "compile-sneeql=", "do-tossim=", "do-avrora="]
		optNames += ["tossim-sync-time=", "do-avrora-candidates=", "do-tossim-candidates="]
		optNames += ["generate-random-network=", "sneeql-network-file=", "net-num-nodes="]
		optNames += ["schema-file="]
		optNames += ["queries=", "acq-rates=", "max-buffering-factors=", "delivery-times=", "test-all=", "x-val-type="]
	
		#append the result of getOpNames to all the libraries 
		optNames += SneeqlLib.getOptNames();
		optNames += TossimLib.getOptNames();
		optNames += AvroraLib.getOptNames();
		optNames += RandomSeeder.getOptNames();
		optNames += checkTupleCount.getOptNames();

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

		if (o == "--sim-duration"):
			optSimDuration = int(a)
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
			
		if (o == "--do-avrora-candidates"):
			optDoAvroraCandidates = UtilLib.convertBool(a)
			continue
			
		if (o == "--do-tossim-candidates"):
			optDoTossimCandidates = UtilLib.convertBool(a)
			continue

		if (o == "--sneeql-network-file"):
			optSneeqlNetFile = a
			continue

		if (o == "--net-num-nodes"):
			optNetNumNodes = int(a)
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
			
		if (o == "--delivery-times"):
			optDeliveryTimes = a.split(",")
			optDeliveryTimes = [int(x) for x in optDeliveryTimes]
			continue
			
		if (o == "--x-val-type"):
			optXValType = str(a)
			continue	
			
		if (o == "--test-all"):
			optTestAll = UtilLib.convertBool(a)
			continue

		if (o == "--short"):
			optQueries = ["Q0"]
			optNumSchemas = 1
			optAcqRates = [10000]
			optMaxBufferingFactors = [3]
			optDoTossimCandidates = False
			optDoAvroraCandidates = False
			continue

	SneeqlLib.setOpts(opts)
	TossimLib.setOpts(opts)
	AvroraLib.setOpts(opts)
	RandomSeeder.setOpts(opts)
	checkTupleCount.setOpts(opts)

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


#sets up the various graphs plotted for experiments 1,3 and 4
#SEE:addtoEnergyGraphs(..)
#NOTE: Layout of graphs used in paper is different to this.
def setupEnergyGraphs(xLabel, xAxisPoints, lineLabels, extraText):

	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph, energyGraph, workingGraph

	#graph to output total energy consumed by network
	totalGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	totalGraph.yLabel = 'Network Total energy consumption (J)'
	totalGraph.setTitle (xLabel + ' vs. Network Total Energy Consumption', extraText)

	#graph to output average energy consumed by network
	averageGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	averageGraph.yLabel = 'Average node energy consumption (J)'
	averageGraph.setTitle (xLabel + ' vs. Average Node Energy Consumption', extraText)
	
	#graph to output max energy consumed by any node network
	maxGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	maxGraph.yLabel = 'Maximum node energy consumption (J)'
	maxGraph.setTitle (xLabel + ' vs. Maximum Node Energy Consumption', extraText)

	#graph to output lifetime of all nodes
	lifetimeGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	lifetimeGraph.yLabel = 'Lifetime of all nodes in days'
	lifetimeGraph.setTitle (xLabel + ' vs. Lifetime with battery of '+str(batteryEnergy)+' Joules', extraText)

	#graph to output total energy consumed by radio
	radioGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	radioGraph.yLabel = 'Radio Total energy consumption (J)'
	radioGraph.setTitle (xLabel + ' vs. Radio Total Energy Consumption', extraText)

	#graph to output total energy consumed by cpu_cycle
	cpu_cycleGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	cpu_cycleGraph.yLabel = 'cpu_cycle Total energy consumption (J)'
	cpu_cycleGraph.setTitle (xLabel + ' vs. cpu_cycle Total Energy Consumption', extraText)

	#graph to output total energy consumed by radio
	packetGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	packetGraph.yLabel = 'Number of Radio Packets'
	packetGraph.setTitle (xLabel + ' vs. Number of Radio Packets', extraText)

	#graph to output working energy consumed by network
	workingGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	workingGraph.yLabel = 'Network Total active and transmit energy consumption (mJ)'
	workingGraph.setTitle  (xLabel + ' vs. Active and Transmit Energy Consumption', extraText)

	newXAxisPoints = []
	for q in optQueries:
		for xValue in xAxisPoints:
			newXAxisPoints = newXAxisPoints + [str(q)+str(xValue)]
	energyGraph = GraphData.GraphData(newXAxisPoints,["Sensor","CPU","Radio","Other"],xLabel)
	energyGraph.yLabel = 'Enery used'
	energyGraph.setTitle  (xLabel + ' vs. Energy Used', extraText)


#Converts the output of avrora into values to become points on the graphs
#totalGraph The sum of the total joules used in each site
#averageGraph The average of the joules used in eahc site
#maxGraph The total joules used by the site with the highest total 
#lifetimeGraph Lifetime of the network calculated at the battery/ max from above
#radioGraph The sum of the radio joules for each site
#cpu_cycleGraph The sum of the cpu_cycles joules for each site. SEE: Globals.CountCpuCycles
#packetGraph: The sum of the Pakcets counted on all sites
def addtoEnergyGraphs(yValue,xValue,avroraOutDir, duration, query):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph, energyGraph, logger

	print'************ Adding '+str(xValue)

	#Get energy used
	(totalEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime) = AvroraLib.computeEnergyValues(avroraOutDir, duration, inputFile = "avrora-out.txt", ignoreLedEnergy = True)

	scaling = float(optSimDuration)/float(duration)

#TODO: don't know what these are so have left them out for now	
#	packetCount = AvroraLib.getPacketCount()
#	workingEnergy = AvroraLib.getWorkingEnergy()
			
	#add point to the graphs
	totalGraph.addPoint(yValue,xValue,str(totalEnergy*scaling))
	logger.info('yValue= '+str(yValue)+' xValue= '+str(xValue) +' total energy= '+str(totalEnergy*scaling))
	
	maxGraph.addPoint(yValue,xValue,str(maxEnergy*scaling))
	averageGraph.addPoint(yValue, xValue, str(averageEnergy*scaling))
	radioGraph.addPoint(yValue,xValue,str(radioEnergy*scaling))
	cpu_cycleGraph.addPoint(yValue,xValue,str(cpu_cycleEnergy*scaling))
#	workingGraph.addPoint(yValue,xValue,str(workingEnergy))
	
	secondLifetime = batteryEnergy / (maxEnergy/(duration)) 
	dayLifetime = secondLifetime / (24*60*60)
	lifetimeGraph.addPoint(yValue, xValue, str(dayLifetime))
#	packetGraph.addPoint(yValue, xValue, packetCount)

	energyGraph.addPoint("Sensor",query+str(xValue),str(sensorEnergy*scaling))
	energyGraph.addPoint("CPU",query+str(xValue),str(cpu_cycleEnergy*scaling))
	energyGraph.addPoint("Radio",query+str(xValue),str(radioEnergy*scaling))
	energyGraph.addPoint("Other",query+str(xValue),str(otherEnergy*scaling))

#Generates txt files used to plot graphs - Including versions used in Paper
#Generates gif and eps version of the graphs 
#SEE:addtoEnergyGraphs(..)
def generateEnergyGraphs(graphType, experiment,numericXValue,outputRoot,timeStamp):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph

	exText = 'Exp'+str(experiment)
	outputDir = outputRoot+'/'+timeStamp+'/'
	#generate the graphs
	totalGraph.generatePlotFile(outputDir+exText+'totalEnergy.txt',numericXValue)
	totalGraph.plotGraph(outputDir+exText+'totalEnergy',graphType, gnuPlotExe, numericXValue, 'top right')
	maxGraph.generatePlotFile(outputDir+exText+'maxEnergy.txt',numericXValue)
	maxGraph.plotGraph(outputDir+exText+'maxEnergy',graphType,gnuPlotExe, numericXValue, 'top right')
	averageGraph.generatePlotFile(outputDir+exText+'averageEnergy.txt',numericXValue)
	averageGraph.plotGraph(outputDir+exText+'averageEnergy',graphType,gnuPlotExe, numericXValue, 'top right')
	lifetimeGraph.generatePlotFile(outputDir+exText+'lifetime.txt',numericXValue)
	lifetimeGraph.plotGraph(outputDir+exText+'lifetime',graphType,gnuPlotExe, numericXValue, 'top left')
	radioGraph.generatePlotFile(outputDir+exText+'radioEnergy.txt',numericXValue)
	radioGraph.plotGraph(outputDir+exText+'radioEnergy',graphType,gnuPlotExe, numericXValue, 'top right')
	cpu_cycleGraph.generatePlotFile(outputDir+exText+'cpu_cycleEnergy.txt', numericXValue)
	cpu_cycleGraph.plotGraph(outputDir+exText+'cpu_cycleEnergy',graphType,gnuPlotExe, numericXValue, 'top right')
	packetGraph.generatePlotFile(outputDir+exText+'tx_count.txt',numericXValue)
	packetGraph.plotGraph(outputDir+exText+'tc_count',graphType,gnuPlotExe, numericXValue, 'top right')
	energyGraph.generatePlotFile(outputDir+exText+'energy.txt',False)
	energyGraph.plotGraph(outputDir+exText+'energy','histograms',gnuPlotExe, False, 'top right', rowStacked = True)
#	workingGraph.generatePlotFile(outputDir+exText+'workingEnergy.txt',numericXValue)
#	workingGraph.plotGraph(outputRoot+exText+'workingEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right')


#Query 3 has a time window, so the fromWindow parameters needs to be adjusted depending on the acquisition interval
def adjustQ3temp(aqRate):
	fromWindow = (aqRate / 1000)
	queryFilePath = "%s/input/Pipes/Q3temp.txt" % (optSneeqlRoot)
	qFile = open(queryFilePath, "w")
	queryStr = """
SELECT  RSTREAM OutFlow.time, InFlow.id, InFlow.pressure, OutFlow.id, OutFlow.pressure
FROM    OutFlow[NOW], InFlow[AT now - %d sec];
	""" % (fromWindow)
	qFile.writelines(queryStr)
	qFile.close()
	report("Q3temp adjusted to reflect aquisition interval, written to "+queryFilePath)
	report(queryStr)
	
#Tests a candidate plan using Avrora
def testAvroraCandidate(nescDir, query, outputPath, numNodes, duration, schemaPath, aqRate, deliveryTime, actualBFactor):

	#Compile the nesC
	exitVal = AvroraLib.compileNesCCode(nescDir)
	if (exitVal != 0):
		return;		
	AvroraLib.generateODs(nescDir)

	for i in range(0, optNumAvroraRuns):
		if optNumAvroraRuns > 1:
			report("Avrora simulation run #%d for candidate %s\n" % (i, outputPath))
	
		#run avrora simulation
		(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(nescDir, outputPath, query, numNodes, simulationDuration = duration, networkFilePath = None)

		#check all tuples present
		queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.txt" % (outputPath, query)
		checkTupleCount.checkResults(query, traceFilePath, schemaPath, aqRate, actualBFactor, duration)	
	
		#add points to graph here
		#TODO: exp dependent - deliveryTime
		if (optXValType=='acq'):
			addtoEnergyGraphs(query, aqRate, outputPath, duration, query)
		elif (optXValType=='del'):
			addtoEnergyGraphs(query, deliveryTime, outputPath, duration, query)	


def avoidQ2With1Site(query, schemaPath):
	print query
	if (query != "Q2"):
		return False
	inflowFound = False;	
	schemaFile = open(schemaPath, "r")
	while 1:
		line = schemaFile.readline()
		if not line:
			reportError ("No inflow found in schema used for Q2")
			return False #The error will be caught elsewhere
		if inflowFound:
			m = re.search("(.+)<sites>(\d+)</sites(.+)", line)
			if (m != None):
				return True
			m = re.search("(.+)<sites>(\d+),(\d+)(.+)", line)
			if (m != None):
				return False
		else:
			m = re.search("(.+)\"InFlow\"(.+)", line)		
			if (m != None):
				inflowFound = True;

#Tests a specific scenario in Tossim and Avrora
def testScenario(schemaPath, query, aqRate, deliveryTime, numNodes, outputPath, 
	sneeqlNetFile):

	report("\n\n*********************************************************************\n")
	report("Testing scenario:\nquery=%s\nschema=%s\naqRate=%d\nnumNodes=%d\noutputPath=%s\n" % (query, schemaPath, aqRate, numNodes, outputPath))

	#Q3temp, QNest3temp and Q4temp only: Set the fromWindow parameter depending on the acquisiton rate
	if (query == "Q3temp"):
		adjustQ3temp(aqRate)
	
	#query optimizer parameters
	coreParams = ["-nesc-control-radio-off=true", "-nesc-adjust-radio-power=false", "-nesc-power-management=true", "-display-graphs=false", "-qos-aware-partitioning=false", "-qos-aware-routing=false", "-qos-aware-where-scheduling=false", "-qos-aware-when-scheduling=false", "-sinks=0", "-network-topology-file="+UtilLib.winpath(sneeqlNetFile), "-targets=avrora1", "-site-resource-file=input/mica2-site-resources.xml", "-display-sensornet-link-properties=true"]

	scenarioParams = ["-schema-file="+UtilLib.winpath(schemaPath), "-query=input/Pipes/"+query+".txt", "-qos-acquisition-interval=%d" % (aqRate), "-output-root-dir="+UtilLib.winpath(outputPath)]
	
	if deliveryTime!=None:
		scenarioParams += ["-qos-max-delivery-time="+str(deliveryTime)]
		scenarioParams += ["-qos-max-buffering-factor=6"] #set to 30 to make 30 node networks faster...
	else:
		scenarioParams += ["-qos-max-buffering-factor=1"]

	#compile the query for using the SNEEql optimizer
	queryCompilerParams = coreParams + scenarioParams	
	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, query)
	if (exitVal != 0):
		return;
	
	queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.txt" % (outputPath, query)
	actualBFactor = SneeqlLib.getBufferingFactor(queryPlanSummaryFile)
	simDuration = (aqRate*actualBFactor)/1000.0

	report("\nAvrora simulation\n=================\n")
	nescDir = "%s/%s/avrora1" % (outputPath, query)
	testAvroraCandidate(nescDir, query, outputPath, numNodes, simDuration, schemaPath, aqRate, deliveryTime, actualBFactor)



def doGenerateAndTestScenarios(timeStamp, schemaPath, numNodes, sneeqlNetFile):

	#Set up graphs here
	#TODO: vary this depending on what needs to go on x-axis
	global optXValType
	
	if (optXValType=='acq'):
		xValues = optAcqRates
		xLabel = "alpha"
	elif (optXValType=='del'):
		xValues = optDeliveryTimes
		xLabel = "delta"
	else:
		xValues = optMaxBufferingFactors
		xLabel = "beta"

	setupEnergyGraphs(xLabel, xValues, optQueries, "extraText")

	#for each query
	for query in optQueries:
		#for each x-Value
		for xVal in xValues:
			
			if (optXValType=='acq'):
				aqRate = xVal
			else:	
				aqRate = optAcqRates[0]
			if (optXValType=='del'):
				deliveryTime = xVal
			else:
				deliveryTime = optDeliveryTimes[0]
		
			outputPath = "%s/%s/%s/net%d-aqRate%d-Del%s" % (optOutputRoot, timeStamp, query, int(numNodes), aqRate, str(deliveryTime))
			testScenario(schemaPath, query, aqRate, deliveryTime, numNodes, outputPath, sneeqlNetFile)

	#generate graphs here
	generateEnergyGraphs('linespoints',1,True,optOutputRoot, timeStamp)


#Generates different scenarios to be tested
def generateAndTestScenarios(timeStamp):

	if os.path.isfile(optSchemaFile):
		doGenerateAndTestScenarios(timeStamp, optSchemaFile, optNetNumNodes, optSneeqlNetFile)
	else:
		reportError("Error finding physical schema file " + optSchemaFile)
		sys.exit(2)


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
