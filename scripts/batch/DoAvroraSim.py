import AvroraLib, CycleCompare, Energy, LedTimes, LedStateTimes, RandomSeeder, SneeqlLib, UtilLib, getopt, logging, os, re, shutil, sys

#Logging Options
optOutputRoot = "/cygdrive/c/measurements" #full absoluate path needed here in Cygwin format /cygdrive/c/
optTimeStampOutput = True
#optTimeStampOutput = False

#Run Options
optAvroraNetworkFile = None
optSimulationDuration = 100

#Setup Options
optCompileQuery = True
optCompileNesc = True
optNescInputDirectory = None #Default is use current directory


#scenario settings
#All must be of the same length. 
optMeasurementNameList = None
optMeasurementDir = "Test"
testLength = -1
optQueriesList = None
optBufferingFactors = None
optMeasurementsMaxActiveAgendaLoops = None
optMeasurementsIgnoreInList = None
optMeasurementsRemoveOperatorsList = None
optMeasurementsThinOperatorsList = None
optRemoveUnrequiredOperatorsList = None
optMeasurementsMultiAcquireList = None

#report settings
optReportLedStates = False
optReportLedDurations = True

def usage():
	print 'Usage: test.py [-options]s'
	print '\t\tto run this script.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'

	print '--ouput-root=<dir> \n\tdefault: ' + str(optOutputRoot)
	print '--timestamp-output \n\tdefault: ' + str(optTimeStampOutput)

	print '--avrora-network-file=<file> \n\tdefault: '+ str(optAvroraNetworkFile) + '\n\tif not provided freespace radio model won\'t be used by Avrora'
	print '--simulation-Duration=sec\n\tdefault: '+str(optSimulationDuration)

	print '\nScenarios to be tested:'
	print '--measurement-name=String\n\tUse either measurement-name or test-list'
	print '--measurement-Dir=String'
	print '--test-list=String:Q1,..Qn@..String:Q1,..Qn\n\tUse either measurement-name or measurement-name-list'
	print '--queries=Q1,..Qn\n\tUse either queries or test-list'
	print '--buffering-factors=b1,..bn \n\t default: ' + str(optBufferingFactors)
	print '--measurements-max-active-agenda-loops=b1,..bn \n\t default: ' + str(optMeasurementsMaxActiveAgendaLoops)	
	print '--measurements-ignore-in-list=St1,..Stn\n\t default: None'
	print '--measurements-remove-operators-list=St1,..Stn\n\t default: None'
	print '--measurements-thin-operators-list=St1,..Stn\n\t default: None'
	print '--remove-unrequired-operators-list=St1,..Stn\n\t default: None'
	print '--measurements-multi-acquire-list=I1..In\n\t default: None'
	print 'Ways to do tests:'
	print '--compile-query=True|False\n\tdefault: '+str(optCompileQuery)+'\n\tWhen set to True take precident over input directory'
	print '--compile-nesc=True|False\n\tdefault: '+str(optCompileQuery)+'\n\tMust be True if compile query is True'
	print '--nesc-input-directory=<Dir>"\n\tdefault: Use current directory\n\tParent directory of the Mote0..MoteN directories'
	print '--report-led-states=True|False\n\tdefualt:'+str(optReportLedStates)
	print '--report-led-durations=True|False\n\tdefualt:'+str(optReportLedDurations)
	
	AvroraLib.usage()
	RandomSeeder.usage()
	SneeqlLib.usage()
def parseArgs(args):
	global optOutputRoot, optTimeStampOutput 
	global optAvroraNetworkFile, optSimulationDuration
	global optCompileQuery, optCompileNesc, optNescInputDirectory
	global testLength, optBufferingFactors, optMeasurementDir, optMeasurementNameList, optQueriesList
	global optMeasurementsMaxActiveAgendaLoops
	global optMeasurementsIgnoreInList, optMeasurementsRemoveOperatorsList
	global optMeasurementsThinOperatorsList, optRemoveUnrequiredOperatorsList	
	global optMeasurementsMultiAcquireList
	global optReportLedStates, optReportLedDurations, loopLength

	report ("running with: "+ str(args))

	try:
		optNames =  ['ouput-root=', 'timestamp-output=', 'avrora-network-file=']
		optNames += ['simulation-Duration=', 'compile-query=', 'compile-nesc=']
		optNames += ['nesc-input-directory=','measurement-dir=','measurement-name=','test-list=']
		optNames += ['queries=', 'buffering-factors=', 'measurements-max-active-agenda-loops=']
		optNames += ['measurements-ignore-in-list=']
		optNames += ['measurements-remove-operators-list=', 'measurements-thin-operators-list=']
		optNames += ['measurements-multi-acquire-list=']
		optNames += ['remove-unrequired-operators-list=']
		optNames += ['report-led-states=', 'report-led-durations=']
	
		#append the result of getOpNames to all the libraries 
		optNames += AvroraLib.getOptNames();
		optNames += RandomSeeder.getOptNames();
		optNames += SneeqlLib.getOptNames();
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
		
	for o, a in opts:
		#print o
		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		if (o =='--ouput-root'):
			optOutputRoot = a
			continue
		if (o == "--timestamp-output"):
			optTimeStampOutput = UtilLib.convertBool(a)
			continue
		if (o == '--avror-network-file'):
			optAvroraNetFile = a
			continue
		if (o =='--simulation-Duration'):
			optSimulationDuration = int(a)
			continue
		if (o =='--compile-query'):
			optCompileQuery = UtilLib.convertBool(a)
			continue
		if (o =='--compile-nesc'):
			optCompileNesc = UtilLib.convertBool(a)
			continue
		if (o =='--nesc-input-directory'):
			optNescInputDirectory = a
			continue

		#scenario settings
		if (o == "--measurement-name"):
			optMeasurementNameList = [a]
			continue
		if (o == "--measurement-dir"):
			optMeasurementDir = a
			continue
		if (o == "--test-list"):
			testList = a.split("@")
			optMeasurementNameList = range(0, len(testList))
			optQueriesList = range(0, len(testList))
			#print testList
			#print len(testList)
			for i in range(0, len(testList)):
				#print testList[i]
				#print i
				testSubList = testList[i].split(":")
				if len(testSubList) == 1:
					optMeasurementNameList[i] = testSubList[0]
					optQueriesList[i] = testSubList[0]
				else:
					optMeasurementNameList[i] = testSubList[0]
					optQueriesList[i] = testSubList[1]
			#print optQueriesList
			continue
		if (o == "--queries"):
			optQueriesList = a 
			continue
		if (o == "--buffering-factors"):
			optBufferingFactors = a.split(":")
			print optBufferingFactors
			optBufferingFactors = [int(x) for x in optBufferingFactors]
			print optBufferingFactors
			continue
		if (o == "--measurements-max-active-agenda-loops"):
			optMeasurementsMaxActiveAgendaLoops = int(a)
			continue
		if (o == "--measurements-ignore-in-list"):
			optMeasurementsIgnoreInList = a.split(",")
			continue
		if (o == "--measurements-remove-operators-list"):
			optMeasurementsRemoveOperatorsList = a.split(",")
			print optMeasurementsRemoveOperatorsList
			continue
		if (o == "--measurements-thin-operators-list"):
			optMeasurementsThinOperatorsList = a.split(",")
			continue
		if (o == "--measurements-multi-acquire-list"):
			optMeasurementsMultiAcquireList = a.split(",")
			optMeasurementsMultiAcquireList = [int(x) for x in optMeasurementsMultiAcquireList]
			continue
		if (o == "--remove-unrequired-operators-list"):
			optRemoveUnrequiredOperatorsList = a.split(",")
			#print optRemoveUnrequiredOperatorsList
			optRemoveUnrequiredOperatorsList = [UtilLib.convertBool(x) for x in optRemoveUnrequiredOperatorsList]
			#print optRemoveUnrequiredOperatorsList
			continue

		#report settings
		if (o == "--report-led-states"):
			optReportLedStates = UtilLib.convertBool(a)
			continue
		if (o == "--report-led-durations"):
			optReportLedDurations = UtilLib.convertBool(a)
			continue
	
	AvroraLib.setOpts(opts)
	RandomSeeder.setOpts(opts)
	SneeqlLib.setOpts(opts)
	
	if optCompileQuery:
		if (not optCompileNesc):
			reportWarning("compile nesc overridded to true as compile Query = true")
			optCompileNesc = True
		
	#check Lengths of test settings which must all be the same
	if optQueriesList != None:
		print optQueriesList
		loopLength = len(optQueriesList)
		loopLength = checkLength (optMeasurementNameList, loopLength)

		queries = optQueriesList[0].split(",")
		#print queries
		testLength = len(queries)
		#print ("testLength" + str(testLength))
		#if testLength == 1:
		#	testLength = -1	
		#print ("testLength" + str(testLength))
		
		testLength = checkLength (optBufferingFactors, testLength)
		testLength = checkLength (optMeasurementsRemoveOperatorsList, testLength)
		testLength = checkLength (optMeasurementsIgnoreInList, testLength)
		testLength = checkLength (optMeasurementsThinOperatorsList, testLength)
		testLength = checkLength (optMeasurementsMultiAcquireList, testLength)
		testLength = checkLength (optRemoveUnrequiredOperatorsList, testLength)
		report (str(testLength) + " tests to run")	
	
def checkLength (array, target):
	#print ("target =" +str(target))
	#print array
	if (array == None):
		return target
	if len(array) == target:
		#print target
		return target
	elif (target == 1):
		#print len(array)	
		return len(array)
	else:	
		#here = error
		reportError("Settings arrays not of length: "+ str(target))
		reportError (array)
		sys.exit(2)

logger = None

def startLogger(timeStamp):
	global logger

	logger = logging.getLogger('test')

	#create the directory if required
	if not os.path.isdir(optOutputRoot):
			os.makedirs(optOutputRoot)
			
	file = '%s/test%s.log' % (optOutputRoot, timeStamp)		
	print file
	hdlr = logging.FileHandler(file)
	
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting cb Test')

	#Register the logger with the libraries this module uses
	AvroraLib.registerLogger(logger)
	RandomSeeder.registerLogger(logger)
	SneeqlLib.registerLogger(logger)
	LedTimes.registerLogger(logger)

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

#Copies mote code to outputdir for keeping track of.
def copyNCFiles(nescRootDir,outputDir):
	#print "in"
	print nescRootDir
	#print outputDir
	if not os.path.isdir(outputDir):
		os.makedirs(outputDir)
	allDir = None
	for dir in os.listdir(nescRootDir):
		m = re.match("All", dir)
		if (m != None):
			allDir = dir
	for dir in os.listdir(nescRootDir):
		m = re.match("mote(\d+)$", dir)
		if (m != None):
			i = m.group(1)
			#create the directory if required
			copyNCFiles (nescRootDir + "/" + dir, outputDir+ "/" + dir)	
			if allDir:
				copyNCFiles (nescRootDir + "/" + allDir, outputDir+ "/" + dir)	
		elif (not optCompileNesc)  and ((str(dir) == "build") or (str(dir) == "mica2")):
			copyNCFiles (nescRootDir + "/" + dir, outputDir+ "/" + dir)	
		else: 
			if not os.path.isdir(nescRootDir + "/" + dir):
				shutil.copy (nescRootDir + "/" + dir, outputDir)
			else:
				reportWarning("Ignoring: "+str(dir))

#Given compiled nesC root directory, dissasembles all the mica2 executables for use with Avrora
def getMaxMote(nescRootDir):
	maxMote = 0
	for dir in os.listdir(nescRootDir):
		m = re.match("mote(\d+)$", dir)
		if (m != None):
			i = int(m.group(1))
			if i > maxMote:
				maxMote = i
	return maxMote + 1			
	
def findAvroraDirectory(directory):
	for queryDir in os.listdir(directory):
		query = directory+queryDir
		if os.path.isdir(query):
			for subDir in os.listdir(query):
				if (subDir == "avrora"):
					return query+"/"+subDir
	reportError("NO Avrora directory found in: "+directory)
	sys.exit(2)
	
#duplicates Mote0 code to ones for eact active loop requested.
def multiplyNCFiles(nescRootDir):
	mote0 = nescRootDir + "/mote0"
	if (SneeqlLib.optMeasurementsActiveAgendaLoops < 0):
		activeLoop = open(mote0 + "/ActiveLoop.h","w")
		activeLoop.write("#define MAX_ACTIVE_LOOP ")
		activeLoop.write(str(-SneeqlLib.optMeasurementsActiveAgendaLoops))
		activeLoop.close()
		return
	for i in range(1, int(SneeqlLib.optMeasurementsActiveAgendaLoops)):
		motei = nescRootDir + "/mote" + str(i)
		copyNCFiles (mote0, motei)	
	for i in range(0, int(SneeqlLib.optMeasurementsActiveAgendaLoops)):
		motei = nescRootDir + "/mote" + str(i)
		activeLoop = open(motei + "/ActiveLoop.h","w")
		activeLoop.write("#define MAX_ACTIVE_LOOP ")
		activeLoop.write(str(i))
		activeLoop.close()

#prepares the Avrora input by either coping the input code
def copyInput(outputPath):
	report ("Copying files from" +optNescInputDirectory)
	if (optNescInputDirectory == None):
		currentDir = os.getcwd()
	else:
		currentDir = optNescInputDirectory
	copyNCFiles (currentDir, outputPath)

#prepares the Avrora input by compiling the query 
def compileInput(outputPath, query, bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators, measurementsMultiAcquire, removeUnrequiredOperators):
	SneeqlLib.compileQuery("AvroraSim", query = query, targets="Avrora1", outputRootDir = outputPath, qosBufferingFactor = bufferingFactor, measurementsActiveAgendaLoops = measurementsActiveAgendaLoops, measurementsIgnoreIn = measurementsIgnoreIn, measurementsRemoveOperators = measurementsRemoveOperators, measurementsThinOperators = measurementsThinOperators, measurementsMultiAcquire = measurementsMultiAcquire, removeUnrequiredOperators = removeUnrequiredOperators)			
	avroraDir = outputPath + "/" + query + "/avrora1"
	if measurementsActiveAgendaLoops != None:
		#doing tests run at a time
		return avroraDir			
	elif (SneeqlLib.optMeasurementsActiveAgendaLoops == None):
		return avroraDir
	else:
		multiplyNCFiles (avroraDir)			
		return avroraDir

def getLedTimes(avroraOutputFile, numSites):
	if optReportLedDurations:
		LedTimes.getLedTimes(avroraOutputFile, numSites,
			yellowDescription = SneeqlLib.optNescYellowExperiment, 
			greenDescription = SneeqlLib.optNescGreenExperiment, redDescription = SneeqlLib.optNescRedExperiment)
	else:
		report("No Request to report Led data")
	
def getLedStates(avroraOutputFile, numSites):
	if optReportLedStates:
		LedStateTimes.getLedStates(avroraOutputFile, numSites, verbose = True)
	else:
		report("No Request to report Led states")

def dotest(query, bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators,  measurementsMultiAcquire, removeUnrequiredOperators):
	global outputPath

	testName = runDir (bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators, measurementsMultiAcquire, removeUnrequiredOperators)
	outputPath = optOutputRoot + testName + "/"
	report ("DoTest to: " + outputPath)

	if optCompileQuery: 
		queryDir = outputPath	+ "/" + str(query)
		report ("Running: "+ queryDir)
		if os.path.isdir(queryDir):
			if not (SneeqlLib.optDeleteOldFiles):
				report ("Reusing " + queryDir)
				copyFiles(queryDir, measurementsActiveAgendaLoops);	
				return
		outputPath = compileInput(outputPath, query, bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators, measurementsMultiAcquire, removeUnrequiredOperators)
	else:	
		copyInput(outputPath)
		
	report ("Outpath = "+ outputPath)

	if (optCompileNesc):
		exitVal = AvroraLib.compileNesCCode(outputPath)
		if (exitVal != 0):
			return;		
		AvroraLib.generateODs(outputPath)
		print "complied"
	else:
		print 'reusing existing Nesc and od files'
		
	numNodes =getMaxMote(outputPath)
	desc = "test"
	#run avrora simulation
	(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(outputPath, outputPath, 
		desc, numNodes, simulationDuration = optSimulationDuration, networkFilePath = optAvroraNetworkFile)

	getLedTimes(avroraOutputFile, numNodes)
	getLedStates(avroraOutputFile, numNodes)
	Energy.getEnergy(outputPath)
	copyFiles(queryDir, measurementsActiveAgendaLoops)
	
def doTestLoop(query, bufferingFactor, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators,  measurementsMultiAcquire, removeUnrequiredOperators):
	if optMeasurementsMaxActiveAgendaLoops == None:
		dotest(query, bufferingFactor, None, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators,  measurementsMultiAcquire, removeUnrequiredOperators)
	else:
		for measurementsActiveAgendaLoops in range(0, optMeasurementsMaxActiveAgendaLoops):
			dotest(query, bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators,  measurementsMultiAcquire, removeUnrequiredOperators)

def runDir (bufferingFactor, measurementsActiveAgendaLoops, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators, measurementsMultiAcquire, removeUnrequiredOperators):
	runName = "/Runs" + str(optSimulationDuration)
	if bufferingFactor != None:
		runName += "/BF_"+str(bufferingFactor)
	if measurementsIgnoreIn != None:
		if len(measurementsIgnoreIn) > 0:
			runName += "/IG_"+measurementsIgnoreIn
	if measurementsRemoveOperators != None:
		if len(measurementsRemoveOperators) > 0:
			runName += "/RM_"+measurementsRemoveOperators
	if measurementsThinOperators != None:
		if len(measurementsThinOperators) > 0:
			runName += "/TH_"+measurementsThinOperators
	if measurementsMultiAcquire != None:
		runName += "/AcCard_"+str(measurementsMultiAcquire)
	if removeUnrequiredOperators:
		runName += "/NoUnrequired"
	else:	
		runName += "/KeepUnrequired"
	if measurementsActiveAgendaLoops != None:
		runName += "/AL_"+str(measurementsActiveAgendaLoops)
	if len(runName) == 0:
		runName = "/Default"	
	return runName	

def openSummarises(measurementName):
	global cycleFile, greenFile, energyFile, redFile, sizeFile, yellowFile
	if measurementName != None:
		testDir = optOutputRoot + "/"+ optMeasurementDir + "/" + measurementName + "/"
		ending = measurementName + optMeasurementDir.replace("/","_") + measurementName + ".csv"
		if not os.path.isdir(testDir):
			os.makedirs(testDir)

		print optMeasurementDir
		cycleFile = open (testDir +  "cycle" + ending, "w")
		if SneeqlLib.optNescGreenExperiment == None:
			greenFile = open (testDir + "green" + ending, "w")
		else:	
			greenFile = open (testDir + SneeqlLib.optNescGreenExperiment + ending, "w")
		energyFile = open (testDir + "energy" + ending, "w")
		if SneeqlLib.optNescRedExperiment == None:
			redFile = open (testDir + "red" + ending, "w")
		else:	
			redFile = open (testDir + SneeqlLib.optNescRedExperiment + ending, "w")
		sizeFile = open (testDir + "size" + ending, "w")
		if SneeqlLib.optNescYellowExperiment == None:
			yellowFile = open (testDir + "yellow" + ending, "w")
		else:	
			yellowFile = open (testDir + SneeqlLib.optNescYellowExperiment + ending, "w")
	else:
		cycleFile = None
			
def copyFiles(queryDir, moteNum):
	if cycleFile == None:
		return
	if ((moteNum == None) or (moteNum == 0)):
		dataArray = CycleCompare.copyFile(queryDir, cycleFile, "Cycle")
		cycleFile.write("\n")
		superCycleFile.write ("," + dataArray[len(dataArray)-1][1])
		superCycleFile.write ("," + dataArray[len(dataArray)-2][1])

		if SneeqlLib.optNescGreenExperiment == None:
			dataArray = CycleCompare.copyFile(queryDir, greenFile, "green")
		else:	
			dataArray = CycleCompare.copyFile(queryDir, "green", SneeqlLib.optNescGreenExperiment)
		superGreenFile.write ("," + dataArray[len(dataArray)-1][1])
			
		dataArray = CycleCompare.copyFile(queryDir, energyFile, "energy")
		energyFile.write("\n")	
		superEnergyFile.write ("," + dataArray[len(dataArray)-1][1])
		superEnergyFile.write ("," + dataArray[len(dataArray)-2][1])

		if SneeqlLib.optNescRedExperiment == None:
			dataArray = CycleCompare.copyFile(queryDir, redFile, "red")
		else:	
			dataArray = CycleCompare.copyFile(queryDir, redFile, SneeqlLib.optNescRedExperiment)
		superRedFile.write ("," + dataArray[len(dataArray)-1][1])

		dataArray = CycleCompare.copyFile(queryDir, sizeFile, "size")
		superRAMFile.write ("," + dataArray[len(dataArray)-1][1])
		superROMFile.write ("," + dataArray[len(dataArray)-1][1])

		if SneeqlLib.optNescYellowExperiment == None:
			dataArray = CycleCompare.copyFile(queryDir, yellowFile, "yellow")
		else:	
			dataArray = CycleCompare.copyFile(queryDir, yellowFile, SneeqlLib.optNescYellowExperiment)
		superYellowFile.write ("," + dataArray[len(dataArray)-1][1])
	else:
		CycleCompare.copyAFile(queryDir, cycleFile, "Cycle", moteNum)
		cycleFile.write("\n")	
		if SneeqlLib.optNescGreenExperiment == None:
			CycleCompare.copyAFile(queryDir, greenFile, "green", moteNum)
		else:	
			CycleCompare.copyAFile(queryDir, greenFile, SneeqlLib.optNescGreenExperiment, moteNum)
		CycleCompare.copyAFile(queryDir, energyFile, "energy", moteNum)
		energyFile.write("\n")	
		if SneeqlLib.optNescRedExperiment == None:
			CycleCompare.copyAFile(queryDir, redFile, "red", moteNum)
		else:	
			CycleCompare.copyAFile(queryDir, redFile, SneeqlLib.optNescRedExperiment, moteNum)
		CycleCompare.copyAFile(queryDir, sizeFile, "size", moteNum)
		if SneeqlLib.optNescYellowExperiment == None:
			CycleCompare.copyAFile(queryDir, yellowFile, "yellow", moteNum)
		else:	
			CycleCompare.copyAFile(queryDir, yellowFile, SneeqlLib.optNescYellowExperiment, moteNum)

def summarizeFiles():
	if cycleFile == None:
		return
	if SneeqlLib.optMeasurementsActiveAgendaLoops != None:
		CycleCompare.summarize (cycleFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (greenFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (energyFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (redFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (sizeFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (yellowFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		return
	if optMeasurementsMaxActiveAgendaLoops != None:
		CycleCompare.summarize (cycleFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (greenFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (energyFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (redFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (sizeFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (yellowFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		return
	cycleFile.close()
	greenFile.close()
	energyFile.close()
	redFile.close()
	sizeFile.close()
	yellowFile.close()
	
def doTests(measurementName, queryList, row):
	global testLength
	
	if measurementName == None:
		report ("Doing: "+ optMeasurementDir )
	else:	
		superCycleFile.write (measurementName);
		report ("doing: "+measurementName)
	openSummarises(measurementName)
	
	if queryList == None:
		queries = None	
		testLength = 1
	else:
		queries = queryList.split(",")
	
	print ("testLength "+str(testLength))
	for i in range(0, testLength):
		if queries == None:
			query = SneeqlLib.optQuery					
		elif len(queries) == 1:
			query = queries[0]
		else:
			query = queries[i]
		if optBufferingFactors != None:
			bufferingFactor = optBufferingFactors[i]
		else:
			bufferingFactor = None
		if optMeasurementsIgnoreInList != None:
			measurementsIgnoreIn = optMeasurementsIgnoreInList[i]
		else:
			measurementsIgnoreIn = SneeqlLib.optMeasurementsIgnoreIn
		if optMeasurementsRemoveOperatorsList != None:
			measurementsRemoveOperators = optMeasurementsRemoveOperatorsList[i]
		else:
			measurementsRemoveOperators = SneeqlLib.optMeasurementsRemoveOperators
		if optMeasurementsThinOperatorsList != None:
			measurementsThinOperators = optMeasurementsThinOperatorsList[i]
		else:
			measurementsThinOperators = SneeqlLib.optMeasurementsThinOperators
		if optMeasurementsMultiAcquireList != None:
			measurementsMultiAcquire = optMeasurementsMultiAcquireList[i]
		else:
			measurementsMultiAcquire = SneeqlLib.optMeasurementsMultiAcquire
		if optRemoveUnrequiredOperatorsList != None:
			removeUnrequiredOperators = optRemoveUnrequiredOperatorsList[i]
		else:
			removeUnrequiredOperators = SneeqlLib.optRemoveUnrequiredOperators
		doTestLoop(query, bufferingFactor, measurementsIgnoreIn, measurementsRemoveOperators, measurementsThinOperators, measurementsMultiAcquire, removeUnrequiredOperators)

	summarizeFiles()
	if measurementName != None:
		superCycleFile.write (",+b"+str(row)+"-c"+str(row)+",+d"+str(row)+"-e"+str(row)+",+f"+str(row)+"-g"+str(row)+"\n");
		superEnergyFile.write (",+b"+str(row)+"-c"+str(row)+",+d"+str(row)+"-e"+str(row)+",+f"+str(row)+"-g"+str(row)+"\n");
		superGreenFile.write (",+b"+str(row)+"-c"+str(row)+"\n");
		superRedFile.write (",+b"+str(row)+"-c"+str(row)+"\n");
		superRAMFile.write (",+b"+str(row)+"-c"+str(row)+"\n");
		superROMFile.write (",+b"+str(row)+"-c"+str(row)+"\n");
		superYellowFile.write (",+b"+str(row)+"-c"+str(row)+"\n");

def openSupers():
	global superCycleFile, superGreenFile, superEnergyFile, superRedFile, superRAMFile, superROMFile, superYellowFile

	mDir = optOutputRoot + "/"+ optMeasurementDir + "/"
	ending = optMeasurementDir.replace("/","_") + ".csv"

	if not os.path.isdir(mDir):
		os.makedirs(mDir)

	superCycleFile = open (mDir +  "cycle" + ending, "w")
	superCycleFile.write ("Test,With N,With N -1,Without N,without N -1,With Nth, Without Nth, Delta\n");

	if SneeqlLib.optNescGreenExperiment == None:
		superGreenFile = open (mDir + "green" + ending, "w")
	else:	
		superGreenFile = open (mDir + SneeqlLib.optNescGreenExperiment + ending, "w")
	superGreenFile.write ("Test,With,Without,Delta\n");

	superEnergyFile = open (mDir + "energy" + ending, "w")
	superEnergyFile.write ("Test,With N,With N -1,Without N,without N -1,With Nth, Without Nth, Delta\n");

	if SneeqlLib.optNescRedExperiment == None:
		superRedFile = open (mDir + "red" + ending, "w")
	else:	
		superRedFile = open (mDir + SneeqlLib.optNescRedExperiment + ending, "w")
	superRedFile.write ("Test,With,Without,Delta\n");
		
	superRAMFile = open (mDir + "RAM" + ending, "w")
	superRAMFile.write ("Test,With,Without,Delta\n");
	superROMFile = open (mDir + "ROM" + ending, "w")
	superROMFile.write ("Test,With,Without,Delta\n");
	
	if SneeqlLib.optNescYellowExperiment == None:
		superYellowFile = open (mDir + "yellow" + ending, "w")
	else:	
		superYellowFile = open (mDir + SneeqlLib.optNescYellowExperiment + ending, "w")

def main(): 	
	global logger, superCycleFile
	
	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	startLogger(UtilLib.getTimeStamp())
	RandomSeeder.setRandom()

	if optCompileQuery:
		SneeqlLib.compileQueryOptimizer()

	if optQueriesList == None:
		doTests(None, None, 0)
	else:	
		openSupers()
		for i in range(0, loopLength):
			superCycleFile.write (optMeasurementNameList[i]);
			superEnergyFile.write (optMeasurementNameList[i]);
			superGreenFile.write (optMeasurementNameList[i]);
			superRedFile.write (optMeasurementNameList[i]);
			superRAMFile.write (optMeasurementNameList[i]);
			superROMFile.write (optMeasurementNameList[i]);
			superYellowFile.write (optMeasurementNameList[i]);
			doTests(optMeasurementNameList[i], optQueriesList[i], i+2)
		superCycleFile.close()	
		superEnergyFile.close()	
		superGreenFile.close()	
		superRedFile.close()	
		superRAMFile.close()	
		superROMFile.close()	
		superYellowFile.close()	
			
	report ("Done");
	logging.shutdown();

if __name__ == "__main__":
	main()
