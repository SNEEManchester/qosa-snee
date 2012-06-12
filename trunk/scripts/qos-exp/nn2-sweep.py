#!/usr/bin/python
import getopt, logging, sys, SneeqlLib, UtilLib, AvroraLib, os, checkTupleCount

def usage():
	print '''Usage: nn-sweep.py <parameters>
	--query=[Q2|Q4|Q5] 
	--network_size=[n]
	--network_type=[A|B]
	--num_sources=[3|10|min|maj]
	--optimization-goal=[min_delivery,min_energy,max_lifetime] 
	--max-nn=<num_neighbours>
	--output-root=<path>
	'''

queryMap = {'Q2' : 'input/pipes/Q2.txt', 'Q4' : 'input/pipes/QNest4.txt', 'Q5' : 'input/pipes/QNest5.txt'}
networkMap = {'10' : 'input/networks/10-node-topology.xml', '30' : 'scripts/qos-exp/scenarios/30-dense-net.xml', '100' : 'scripts/qos-exp/scenarios/ix-100-dense-net.xml'}
numSourcesMap = {'10_3' : 'input/pipes/10Sites-3Sources-schemas.xml', '10_10' : 'input/pipes/10Sites-10Sources-schemas.xml', '30_min' : 'scripts/qos-exp/scenarios/30-node-min-schema.xml', '30_maj' : 'scripts/qos-exp/scenarios/30-node-maj-schema.xml', '100_min' : 'scripts/qos-exp/scenarios/100-node-min-schema.xml', '100_maj' : 'scripts/qos-exp/scenarios/100-node-maj-schema.xml'}
optGoalMap = {'min_delivery' : 'input/QoS/qos-spec-min-delivery.xml', 'min_energy' : 'input/QoS/qos-spec-min-energy.xml', 'max_lifetime' : 'input/QoS/qos-spec-max-lifetime.xml'}

optMinNN = 0
optMaxNN = 30
optSolverOutput = None

def parseArgs(args):
	global optQuery, optNetworkSize, optNetworkType, optNumSources, optOptGoal, optMinNN, optMaxNN, optOutputRoot, optSolverOutput
	
	try:
		optNames = ["help", "query=", "net-size=", "net-type=", "num-sources=", "opt-goal=", "min-nn=", "max-nn=", "output-root=", "solver-output="]
		optNames += SneeqlLib.getOptNames();
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)		

	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
	
		if (o == '--query'):
			if queryMap.has_key(a):
				optQuery = a;
			else:
				print 'invalid query'
				sys.exit(2)
		if (o == '--net-size'):
			if networkMap.has_key(a):
				optNetworkSize = a;
			else:
				print 'invalid network'
				sys.exit(2)
		if (o == '--net-type'):
			if (a=='A' or a == 'B'):
				optNetworkType = a;
			else:
				print 'invalid network type'
				sys.exit(2)
		if (o == '--num-sources'):
			if numSourcesMap.has_key(optNetworkSize+'_'+a):
				optNumSources = a;
			else:
				print 'invalid network type'
				sys.exit(2)
		if (o == '--opt-goal'):
			if optGoalMap.has_key(a):
				optOptGoal = a;
			else:
				print 'invalid optimization goal'
				sys.exit(2)
			
		if (o == '--min-nn'):
			optMinNN = int(a)

		if (o == '--max-nn'):
			optMaxNN = int(a)
		
		if (o == '--output-root'):
			optOutputRoot = a;
			
		if (o == '--solver-output'):
			optSolverOutput = a;
			
			
	SneeqlLib.setOpts(opts)	


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
	logger.info('nn2 sweep')

	#Register the logger with the libraries this module uses
	SneeqlLib.registerLogger(logger)
	#TossimLib.registerLogger(logger)
	AvroraLib.registerLogger(logger)
	checkTupleCount.registerLogger(logger)
	#RandomSeeder.registerLogger(logger)
	

def invokeQueryOptimizer(outputDir, queryFile, networkFile, heteroNet, schemaFile, qosFile, nn):
	fixedParams = ['-qos-acquisition-interval=10000', '-qos-buffering-factor=1', '-targets=avrora1,tossim1', '-qos-aware-where-scheduling=true', '-haskell-use=true', "-nesc-control-radio-off=true", "-nesc-adjust-radio-power=true", "-nesc-power-management=true", "-delete-old-files=true"]
	varParams = ['-query='+queryFile, '-network-topology-file='+networkFile, '-where-scheduling-hetero-net='+str(heteroNet), '-schema-file='+schemaFile, '-qos-file='+qosFile, '-output-root-dir='+outputDir]
	varParams += ['-where-scheduling-min-nn='+str(nn), '-where-scheduling-max-nn='+str(nn), '-where-scheduling-heuristic-init-point=false', '-nesc-led-debug=false']

	if (optSolverOutput!=None):
		varParams += ['-where-scheduling-solver-output='+optSolverOutput]
	
	queryCompilerParams = fixedParams + varParams

	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, optQuery)
	if (exitVal != 0):
		return;

def logToDataFile(overallOutputDir, nn, objFnVal, measuredVal, numDAFs):
	scriptFile = overallOutputDir+'/data.txt'
	if not os.path.exists(scriptFile):
		f = open(scriptFile,'w')	
		f.write('"NN"\t"fn_val"\t"measured"\t"num DAFs"\n')
		f.close()		

	f = open(scriptFile,'a')	
	f.write('%d\t%s\t%g\t%s\n' % (nn, objFnVal, measuredVal, numDAFs))
	f.close()
	

def parseOutFile(outFilePath):
	ofile = open(outFilePath, 'r')

	summary = {}
	summaryFlag = False
	for line in ofile:
		if line.startswith('*** Summary ***'):
			summaryFlag = True
			continue
		if line.startswith('*** Assignment ***'):
			break
		if line.strip()=='':
			continue
		if summaryFlag:
			(key, val) = line.split('=')
			summary[key] = val.strip();
	ofile.close()
	return summary


def mainLoop():
	queryFile = queryMap[optQuery]
	networkFile = networkMap[optNetworkSize]
	heteroNet = (optNetworkType == 'B')
	schemaFile = numSourcesMap[optNetworkSize+'_'+optNumSources] 
	qosFile = optGoalMap[optOptGoal]
	overallOutputDir = optOutputRoot + '/' + optQuery + '-' + optNetworkSize + 'n-type' + optNetworkType + '-' + optNumSources + 's-' + optOptGoal 

	duration = 100;
#	for nn in range(optMinNN,optMaxNN+1):
	for nn in [0,1,3,5,10,15,20]:
		runOutputDir = overallOutputDir + '/nn' + str(nn)
				
		invokeQueryOptimizer(runOutputDir, queryFile, networkFile, heteroNet, schemaFile, qosFile, nn)
		summary = parseOutFile(runOutputDir+'/'+optQuery+'/query-plan/matlab/wheresched/out'+str(nn)+'.txt')

		if optOptGoal=='min_delivery':
			#check all tuples present
			queryPlanSummaryFile = "%s/%s/query-plan/query-plan-summary.txt" % (runOutputDir, optQuery)

			deliveryTime = SneeqlLib.getDeliveryTime(queryPlanSummaryFile)
			logToDataFile(overallOutputDir, nn, summary['min_f'], float(deliveryTime), summary['num DAFs considered'])
		else:		
			#Compile the nesC
			nescDir = runOutputDir + '/'+optQuery + '/avrora1'
			exitVal = AvroraLib.compileNesCCode(nescDir)
			if (exitVal != 0):
				reportError("Error compiling avrora nesC code")
				return;		
			AvroraLib.generateODs(nescDir)

			#Invoke Avrora simulation
			avroraNetFile = UtilLib.winpath(os.getenv('SNEEQLROOT'))+'\\\\'+networkFile.replace('.xml', '.top')
			(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(nescDir, runOutputDir, optQuery, int(optNetworkSize), simulationDuration = duration, networkFilePath = avroraNetFile)

			#Check query results
			schemaPath = os.getenv('SNEEQLROOT')+'/'+schemaFile
			checkTupleCount.checkResults(optQuery, traceFilePath, schemaPath, 10000, 1, duration)

			#Report total energy consumption
			siteLifetimeRankFile = "%s/site-lifetime-rank.csv" % nescDir
			(sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime) = AvroraLib.computeEnergyValues(runOutputDir, duration, inputFile = "avrora-out.txt", ignoreLedEnergy = True, siteLifetimeRankFile = siteLifetimeRankFile)
			report("The total energy consumption is %f" % (sumEnergy))
			report("The lifetime for this network is %f" % (networkLifetime)) #seems to be in seconds

			if optOptGoal=='min_energy':
				logToDataFile(overallOutputDir, nn, summary['min_f'], float(sumEnergy), summary['num DAFs considered'])
			else:
				logToDataFile(overallOutputDir, nn, summary['min_f'], float(networkLifetime), summary['num DAFs considered'])

def main():
	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 

	timeStamp = UtilLib.getTimeStamp()
	startLogger(timeStamp)

	SneeqlLib.compileQueryOptimizer()
	
	mainLoop()

if __name__ == "__main__":
	main()
	
	
	