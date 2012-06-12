#!/usr/bin/python
#This script assumes that gnuplot is in the environment PATH
import getopt, logging, sys, SneeqlLib, os, UtilLib, checkTupleCount, RandomSeeder, networkLib, StatLib, GraphData, CSVLib

SneeqlLib.optCompileSneeql = True

optLabel = ""
optScenarioDir = os.getenv('HOME')+os.sep+"tmp"+os.sep+"results"+os.sep
optOutputDir = os.getenv('HOME')+os.sep+"tmp"+os.sep+"results"+os.sep

optStartScenario = 0
optEndScenario = 29

optRoutingTreesToGenerate = 50
optRoutingTreesToKeep = 5

#TESTING
#optValidScenarioList = range(0,30)
#optValidScenarioList = [0]
optValidScenarioList = [5, 8, 12, 14, 17, 19, 25, 26, 27]

#for QoSA-SNEE with 30 nodes 
#not working: 8, 25 (used to work for thesis)
#optValidScenarioList = [0, 1, 4, 5, 6, 7, 12, 13, 15, 17, 18, 19, 26, 27, 28] #with original rValues

#For TDB-SNEE vs FG-SNEE comparison, only vary alpha. no other constraints, opt goal implicit.
optTDBAlpha = [3,15,30,60,150,300,600,1000,3000]

#optQoSA = ['none', 'routing', 'where', 'when', 'all']
#optQoSA = ['none']
optQoSA = ['all']

#QoSA-SNEE QoS, read from files
#For 30 node scenarios
#qosaQoSSpec = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]

#For 100 node scenarios
#qosaQoSSpec = ["101", "102", "103", "104", "105", "106", "107", "108", "109", "110"]
#qosaQoSSpec = ["105"]
qosaQoSSpec = ["102", "103", "104", "105", "106", "107", "108", "109", "110"]

#FG-SNEE QoS, list of pairs [alpha, delta upper bound] - both in seconds
fgQoSSpec = ["A", "B", "C"] 
fgQoSSpecMap = {"A" : [5, None], "B" : [15, None], "C" : [25, 100] }

#optExp = "tinydb" #Compare FG-SNEE to how TinyDB would behave
optExp = "1_2" #Compare FG-SNEE to QoSA-SNEE


def parseArgs(args):	
	global optScenarioDir, optOutputDir, optStartScenario, optEndScenario, optRoutingTreesToGenerate, optRoutingTreesToKeep, optQoSA, optTDBAlpha, qosaQoSSpec, fgQoSSpec, optExp
	try:
		optNames = ["start=", "end=", "rt-generate=", "rt-keep=", "tdb-alpha=", "qa-qos=", "fg-qos=", "qosa=", "exp="]
	
		#append the result of getOpNames to all the libraries 
		optNames += SneeqlLib.getOptNames();
		optNames = UtilLib.removeDuplicates(optNames)
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
			
	for o, a in opts:
	
		if (o == "--start"):
			optStartScenario = int(a)
			continue
	
		if (o == "--end"):
			optEndScenario = int(a)
			continue
	
		if (o == "--rt-generate"):
			optRoutingTreesToGenerate = int(a)
			continue

		if (o == "--rt-keep"):
			optRoutingTreesToKeep = int(a)
			continue

		if (o == "--tdb-alpha"):
			optTDBAlpha = map(int,a.split(","))

		if (o == "--qa-qos"):
			qosaQoSSpec = a.split(",")
			continue

		if (o == "--fg-qos"):
			fgQoSSpec = a.split(",")
			continue

		if (o == "--qosa"):
			optQoSA = a.split(",")
			continue

		if (o == "--exp"):
			optExp = a
			if a=="1_2":
				optQoSA = ['none', 'all']
			elif a=="tinydb":
				optQoSA = ['none', 'tinydb']
			elif a=="3":
				optQoSA = ['none', 'routing', 'where', 'when', 'all']
			else:
				print "Unrecognised experiment id"
				sys.exit(1)

	SneeqlLib.setOpts(opts)
	if SneeqlLib.optCygwin:
		optScenarioDir = "c:/ixent/tmp/results"
		optOutputDir = "c:/ixent/tmp/results"

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
	#if not os.path.isdir(optOutputDir):
	#		os.makedirs(optOutputDir)
			
	hdlr = logging.FileHandler('%s/%s-%s.log' % (optOutputDir, optLabel, timeStamp))
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting Regression Test')

	#Register the logger with the libraries this module uses
	SneeqlLib.registerLogger(logger)

def getModelQoSMetrics(outputDir, runAttr, runAttrCols):
	candidateInfoList = []
	
	qosModelFile = "%s/%s/query-plan/model-qos-metrics.csv" % (outputDir, runAttr['queryId'])
	first = True
	keys = []
	for line in open(qosModelFile, 'r'):
		if first:
			keys = CSVLib.colNameList(line)
			runAttrCols.extend(keys)
			first = False
			continue
		
		candidateInfo = CSVLib.line2Dict(line, keys)
		if candidateInfo['best']=="true":
			for k in keys:
				runAttr[k] = candidateInfo[k]
			break
	return (runAttr, runAttrCols)

def runSneeql(runAttr, runAttrCols, qosa, qos, scenarioDir, outputDir):
	print qosa + " " + str(qos)
	queryPlanOutputDir = outputDir + os.sep + 'scenario' + runAttr['scenarioId'] + os.sep + 'qosa-'+qosa 
	queryPlanOutputDir += os.sep + qos

	#query optimizer parameters
	QoSAwareRouting = False
	QoSAwareWhereScheduling = False
	QoSAwareWhenScheduling = False
	if qosa=='routing' or qosa=='all':
		QoSAwareRouting = True
	if qosa=='where' or qosa=='all':
		QoSAwareWhereScheduling = True
	if qosa=='when' or qosa=='all':
		QoSAwareWhenScheduling = True

	coreParams = ["-display-graphs=false", "-display-topology=false", "-sinks=0", "-targets=none", "-display-operator-properties=false", "-display-sensornet-link-properties=false","-site-resource-file=input/mica2-site-resources.xml",   "-qos-aware-routing=" + str(QoSAwareRouting), "-routing-random-seed=4", "-qos-aware-where-scheduling=" + str(QoSAwareWhereScheduling), "-qos-aware-when-scheduling=" + str(QoSAwareWhenScheduling), "-haskell-use=true"]
	
	if QoSAwareRouting:
		coreParams+=["-routing-trees-to-generate="+str(optRoutingTreesToGenerate), "-routing-trees-to-keep="+str(optRoutingTreesToKeep)]

	if QoSAwareWhereScheduling:
		coreParams+=["-where-scheduling-min-nn=15", "-where-scheduling-max-nn=15", "-where-scheduling-heuristic-init-point=true"]
	
	if qosa=="none" and optExp!="tinydb":
		alphadelta = fgQoSSpecMap[qos]
		coreParams += ["-qos-acquisition-interval=%d" % (alphadelta[0]*1000)]
		if alphadelta[1]!=None:
			coreParams += ["-qos-max-delivery-time=%d" % (alphadelta[1]*1000)]
#
#	#When leaving the buffering factor to roam freely, e.g., when minimising for delivery time, the benefits of QoSA in {routing, where} cannot be seen because FG-when scheduling increases the buffering factor. THe bf will therefore be coerced to 1 when qosa in {none, routing, where} and the optimisation goal = min delivery or min alpha (so it doesn't affect pi value).
#	if (qosa == 'none') or (qosa in ['routing', 'where'] and qos in ["min-acq", "min-delivery"]):
#		coreParams+=["-qos-buffering-factor=1"]

	if qosa=="tinydb":
		coreParams += ["-qos-buffering-factor=1"]
	if optExp=="tinydb":
		coreParams += ["-qos-acquisition-interval=%d" % (int(qos)*1000)]

	#derive the qos-filename
	if qosa in ["routing", "where", "when", "all"]:
		qosFile = "input/qos/qos-spec%s.xml" % (qos)
		coreParams += ["-qos-file="+qosFile]

	queryFile = scenarioDir+os.sep+runAttr["queryId"]+".txt"
	schemaFile = scenarioDir+os.sep+runAttr["schemaId"]+".xml"
	networkFile = scenarioDir+os.sep+runAttr["networkId"]+".xml"

	scenarioParams = ["-schema-file="+schemaFile, "-query="+queryFile, "-network-topology-file="+networkFile, "-output-root-dir="+UtilLib.winpath(queryPlanOutputDir)]
	
	#compile the query for using the SNEEql optimizer
	queryCompilerParams = coreParams + scenarioParams	
	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, runAttr['queryId'])

	tmpRunAttrCols = runAttrCols + ['qosa', 'optgoal', 'exitCode']
	runAttr['qosa'] = qosa
	runAttr['optgoal'] = qos
	runAttr['exitCode'] = exitVal

	if exitVal==0:
		(runAttr, tmpRunAttrCols) = getModelQoSMetrics(queryPlanOutputDir, runAttr, tmpRunAttrCols)

	resultsFileName = outputDir+os.sep+"results.csv"
	if not os.path.exists(resultsFileName):
		resultsFile = open(resultsFileName, "w")
		resultsFile.writelines(CSVLib.header(tmpRunAttrCols))
	
	resultsFile = open(resultsFileName, "a")
	resultsFile.writelines(CSVLib.line(runAttr, tmpRunAttrCols))
	resultsFile.close()

def runScenario(scenarioAttr, scenarioAttrCols, scenarioDir, outputDir):
	runAttr = scenarioAttr
	runAttrCols = scenarioAttrCols 
	for qosa in optQoSA:
		if optExp == "tinydb":
			for qos in optTDBAlpha:
				runSneeql(runAttr, runAttrCols, qosa, str(qos), scenarioDir, outputDir)
		else:
			if qosa == "none":
				for qos in fgQoSSpec:
					runSneeql(runAttr, runAttrCols, qosa, qos, scenarioDir, outputDir)
			else:
				for qos in qosaQoSSpec:
					runSneeql(runAttr, runAttrCols, qosa, qos, scenarioDir, outputDir)
				
def runScenarios(timeStamp, startScenarioId, endScenarioId, scenarioDir, outputDir):
	colNames = None
	first = True

	for line in open(scenarioDir+os.sep+"scenarios.csv", 'r'):
		if first:
			scenarioAttrCols = CSVLib.colNameList(line)
			scenarioAttrCols += ["timeStamp"]
			first = False
			continue

		scenarioAttr = CSVLib.line2Dict(line, scenarioAttrCols)

		if not int(scenarioAttr['scenarioId']) in optValidScenarioList:
			continue

		scenarioAttr['timeStamp']=timeStamp
		#scenarioId,queryId,networkId,numNodes,rValue,schemaId,percentSources
		if (int(scenarioAttr['scenarioId'])>=startScenarioId and int(scenarioAttr['scenarioId'])<=endScenarioId):
			runScenario(scenarioAttr, scenarioAttrCols, scenarioDir, outputDir)
						

def main(): 	
	global optScenarioDir, optOutputDir

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 

	if optExp in ['tinydb','1_2']:
#30 node scenarios
#		optScenarioDir += os.sep + "scenarios1"		

#100 node scenarios
		optScenarioDir += os.sep + "scenarios100"		
	else:
		optScenarioDir += os.sep + "scenarios2"		
	optOutputDir += os.sep + "exp" + optExp

	timeStamp = UtilLib.getTimeStamp()
	#if (not optTimeStampOutput):
	#	timeStamp = ""
	startLogger(timeStamp)
	
	#RandomSeeder.setRandom()

	#TODO: get SVN latest version

	#compile java
	SneeqlLib.compileQueryOptimizer()
	
	runScenarios(timeStamp, optStartScenario, optEndScenario, optScenarioDir, optOutputDir+os.sep+timeStamp)

if __name__ == "__main__":
	main()
