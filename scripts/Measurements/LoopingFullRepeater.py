import AvroraLib, CycleCompare, Energy, LedTimes, LedStateTimes, RandomSeeder, SneeqlLib, UtilLib, getopt, logging, os, re, shutil, sys

#Logging Options
optOutputRoot = "/cygdrive/c/measurements" #full absoluate path needed here in Cygwin format /cygdrive/c/
optTimeStampOutput = True
#optTimeStampOutput = False

#Run Options
optAvroraNetworkFile = None
optSimulationDuration = 100
optSimulationDurationList = 100,200,300,400,500
optMeasurementsRemoveOperatorsList = ["everything", "trayAlldeliverAll"]

#Setup Options
optNescInputDirectory = None #Default is use current directory

#scenario settings
optMeasurementName = "Summary"
optMeasurementDir = "Test"

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
	print 'Ways to do tests:'
	print '--nesc-input-directory=<Dir>"\n\tdefault: Use current directory\n\tParent directory of the Mote0..MoteN directories'
	print '--report-led-states=True|False\n\tdefualt:'+str(optReportLedStates)
	print '--report-led-durations=True|False\n\tdefualt:'+str(optReportLedDurations)
	
	AvroraLib.usage()
	RandomSeeder.usage()
	SneeqlLib.usage()

def parseArgs(args):
	global optOutputRoot, optTimeStampOutput 
	global optAvroraNetworkFile, optSimulationDuration
	global optNescInputDirectory
	global optMeasurementName, optMeasurementDir
	global optReportLedStates, optReportLedDurations

	report ("running with: "+ str(args))

	try:
		optNames =  ['ouput-root=', 'timestamp-output=']
		optNames +=  ['avrora-network-file=','simulation-Duration=']
		optNames +=  ['nesc-input-directory=']
		optNames +=  ['measurement-name=','measurement-dir=']
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
	print "in"
	print nescRootDir
	print outputDir
	if not os.path.isdir(outputDir):
		os.makedirs(outputDir)
	for dir in os.listdir(nescRootDir):
		m = re.match("mote(\d+)$", dir)
		if (m != None):
			#create the directory if required
			copyNCFiles (nescRootDir + "/" + dir, outputDir+ "/" + dir)	
		else: 
			if not os.path.isdir(nescRootDir + "/" + dir):
				shutil.copy (nescRootDir + "/" + dir, outputDir)
			elif (dir == "avrora1"):
				copyNCFiles (nescRootDir + "/" + dir, outputDir+ "/" + dir)					
			else:
				reportWarning("Ignoring: "+str(dir))

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
	
#duplicates Mote0 code to ones for each agenda duration required
def multiplyNCFiles(nescRootDir, durations):
	mote0 = nescRootDir + "/mote0"
	for i in range(1, len(durations)):
		motei = nescRootDir + "/mote" + str(i)
		copyNCFiles (mote0, motei)	
	for i in range(0, len(durations)):
		motei = nescRootDir + "/mote" + str(i)
		print ("mote"  )+str(i) +" = " + motei + " = " +   str(durations[i])
		durationHeader = open(motei + "/DurationHeader.h","w")
		durationHeader.write("#define DURATION_INTERVAL ")
		durationHeader.write(str(durations[i]))
		durationHeader.close()

#prepares the Avrora input by either coping the input code
def copyInput(outputPath):
	report ("Copying files from" +optNescInputDirectory)
	if (optNescInputDirectory == None):
		currentDir = os.getcwd()
	else:
		currentDir = optNescInputDirectory
	copyNCFiles (currentDir, outputPath)

def getLedTimes(avroraOutputFile, numSites):
	if optReportLedDurations:
		LedTimes.getLedTimes(avroraOutputFile, numSites)
	else:
		report("No Request to report Led data")
	
def getLedStates(avroraOutputFile, numSites):
	if optReportLedStates:
		LedStateTimes.getLedStates(avroraOutputFile, numSites, verbose = True)
	else:
		report("No Request to report Led states")

def doTest(measurementsRemoveOperators):
	mainPath = optOutputRoot + "/" + SneeqlLib.optQuery + "/" + measurementsRemoveOperators + "/"
	if SneeqlLib.optQuery != None:
		SneeqlLib.compileQuery("AvroraSim", targets = "Avrora1", outputRootDir = mainPath, measurementsRemoveOperators = measurementsRemoveOperators)
		queryParts = SneeqlLib.optQuery.split("/")
		query = queryParts[len(queryParts)-1]
		codePath = mainPath + "/" + query + "/avrora1/"
	else:	
		copyInput(mainPath)
		codePath = mainPath + "/avrora1/"
	#Use this to get many durations in one run.
	#multiplyNCFiles(codePath, range(0, 1))
		
	report ("codePath = "+ codePath)

	exitVal = AvroraLib.compileNesCCode(codePath)
	if (exitVal != 0):
		return;		
	AvroraLib.generateODs(codePath)
	print "complied"
		
	numNodes =getMaxMote(codePath)
	desc = "test"
	
	#Multiple loops of same code
	openSummarises(mainPath)
	if optSimulationDurationList != None:
		queryEnergyFile.write(measurementsRemoveOperators + ",")
		queryYellowFile.write(measurementsRemoveOperators + ",")
		queryYellowMinFile.write(measurementsRemoveOperators + ",")
		queryGreenFile.write(measurementsRemoveOperators + ",")
		queryRedFile.write(measurementsRemoveOperators + ",")
		querySizeFile.write (measurementsRemoveOperators + "," + str(AvroraLib.ram) + "," + str(AvroraLib.rom)+ "\n")
		for i in range(0, len(optSimulationDurationList)):
			outputDir = mainPath + "/duration" + str(optSimulationDurationList[i])
			if not os.path.isdir(outputDir):
				os.makedirs(outputDir)
		
			#run avrora simulation
			(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(codePath, outputDir, desc, numNodes, simulationDuration = optSimulationDurationList[i], networkFilePath = optAvroraNetworkFile)

			getLedTimes(avroraOutputFile, numNodes)
			getLedStates(avroraOutputFile, numNodes)
			Energy.getEnergy(outputDir)
			copyFiles(outputDir, i, "dur" + str(optSimulationDurationList[i]))
		queryEnergyFile.write("\n")
		queryRedFile.write("\n")
		queryYellowFile.write("\n")
		queryYellowMinFile.write("\n")
		queryGreenFile.write("\n")
	else:	
		for i in range(0, 1):
			outputDir = mainPath + "/run" + str(i)
			if not os.path.isdir(outputDir):
				os.makedirs(outputDir)
		
			#run avrora simulation
			(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(codePath, outputDir, desc, numNodes, simulationDuration = optSimulationDuration, networkFilePath = optAvroraNetworkFile)

			getLedTimes(avroraOutputFile, numNodes)
			getLedStates(avroraOutputFile, numNodes)
			Energy.getEnergy(outputDir)
			copyFiles(outputDir, i, "Run "+str(i))
	closeFiles()
	
def openSummarises(testDir):
	global cycleFile, greenFile, energyFile, redFile, yellowFile

	print ("Writing to "+testDir)
	ending = "Summary.csv"

	if not os.path.isdir(testDir):
		os.makedirs(testDir)

	cycleFile = open (testDir +  "cycle" + ending, "w")
	print (str(cycleFile))
	greenFile = open (testDir + "green" + ending, "w")
	energyFile = open (testDir + "energy" + ending, "w")
	redFile = open (testDir + "red" + ending, "w")
	yellowFile = open (testDir + "yellow" + ending, "w")
			
def openAQuerySummary(name):
	file = open (name, "w")
	file.write ("evals,")
	for i in range(0, len(optSimulationDurationList)):
		file.write(str(optSimulationDurationList[i]*1000/SneeqlLib.optQosAcquisitionInterval)+",")
	file.write("\n")
	return file

def openQuerySummarises(queryDir):
	global queryEnergyFile,querySizeFile, queryGreenFile, queryRedFile, queryYellowFile, queryYellowMinFile

	print ("Writing to "+queryDir)
	ending = "QuerySummary.csv"

	if not os.path.isdir(queryDir):
		os.makedirs(queryDir)

	#cycleFile = open (testDir +  "cycle" + ending)
	queryGreenFile = openAQuerySummary (queryDir + "green" + ending)
	queryEnergyFile = openAQuerySummary (queryDir + "energy" + ending)
	queryRedFile =openAQuerySummary (queryDir + "red" + ending)
	querySizeFile = open (queryDir + "size" + ending, "w")
	querySizeFile.write (",RAM,ROM\n")
	queryYellowFile = openAQuerySummary (queryDir + "yellow" + ending)
	queryYellowMinFile = openAQuerySummary (queryDir + "yellowMin" + ending)

def copyFiles(queryDir, moteNum, text):
	print (str(cycleFile))
	print (queryDir)
	if ((moteNum == None) or (moteNum == 0)):
		CycleCompare.copyFile2(queryDir, cycleFile, "Cycle", text)
		cycleFile.write("\n")
		array = CycleCompare.copyFile2(queryDir, energyFile, "energy", text)
		queryEnergyFile.write (array[1][2]+",")
		energyFile.write("\n")
		array = CycleCompare.copyFile2(queryDir, greenFile, "green", text)
		queryGreenFile.write (array[1][3]+",")
		array = CycleCompare.copyFile2(queryDir, redFile, "red", text)
		queryRedFile.write (array[1][3]+",")
		array = CycleCompare.copyFile2(queryDir, yellowFile, "yellow", text)
		queryYellowFile.write (array[1][3]+",")
		queryYellowMinFile.write (array[1][2]+",")
	else:
		CycleCompare.copyAFile2(queryDir, cycleFile, "Cycle", moteNum, text)
		cycleFile.write("\n")
		array = CycleCompare.copyAFile2(queryDir, energyFile, "energy", moteNum, text)
		queryEnergyFile.write (array[0][2]+",")
		energyFile.write("\n")
		array = CycleCompare.copyAFile2(queryDir, greenFile, "green", moteNum, text)
		queryGreenFile.write (array[0][3]+",")
		array = CycleCompare.copyAFile2(queryDir, redFile, "red", moteNum, text)
		queryRedFile.write (array[0][3]+",")
		array = CycleCompare.copyAFile2(queryDir, yellowFile, "yellow", moteNum, text)
		queryYellowFile.write (array[0][3]+",")
		queryYellowMinFile.write (array[0][2]+",")

def closeFiles():
	global cycleFile, greenFile, energyFile, redFile, sizeFile, yellowFile
	cycleFile.close()
	greenFile.close()
	energyFile.close()
	redFile.close()
	yellowFile.close()

def closeQuerySummary(allFile):
	allFile.write("Difference,")
	for l in "bcdef":
		allFile.write ('+' + l + '3-' + l +'2,')
	allFile.write("\n")
	allFile.write("mu sec,")
	for l in "bcdef":
		allFile.write ('=+'+l+'4/7.3728,')
	allFile.write("\n")
	allFile.close()

def closeQuerySummarises(allFile):
	allFile.write("Difference,")
	for l in "bcdef":
		allFile.write ('+' + l + '3-' + l +'2,')
	allFile.write("\n")
	allFile.write("Per Eval,")
	for l in "bcdef":
		allFile.write ('+' + l + '4/' + l +'1,')
	allFile.write("\n")
	allFile.write("Delta,,")
	previous = 'b'
	for l in "cdef":
		allFile.write ('+' + l + '4-' + previous +'4,')
		previous = l
	allFile.write("\n")
	allFile.write("Per Eval,,")
	previous = 'b'
	for l in "cdef":
		allFile.write ('+' + l + '6/(' + l +'1-'+ previous +'1),')
	allFile.write("\n")
	allFile.write("Average,=average(c7:b7)\n")
	allFile.write("Max,=max(c7:b7)\n")
	allFile.write("Min,=Min(c7:b7)\n")
	allFile.write("Error,=(+B10-B9)/B8,=+B11*100\n")
	allFile.close()

def summarizeFiles():
	if cycleFile == None:
		return
	if optMeasurementsMaxActiveAgendaLoops == None:
		CycleCompare.summarize (cycleFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (greenFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (energyFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (redFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (yellowFile, SneeqlLib.optMeasurementsActiveAgendaLoops-1, testLength)
	else:
		CycleCompare.summarize (cycleFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (greenFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (energyFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (redFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)
		CycleCompare.summarize (yellowFile, optMeasurementsMaxActiveAgendaLoops-1, testLength)

def main(): 	
	global logger
	
	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	startLogger(UtilLib.getTimeStamp())
	RandomSeeder.setRandom()

	openQuerySummarises (optOutputRoot + "/" + SneeqlLib.optQuery + "/")
	for i in range(0, len(optMeasurementsRemoveOperatorsList)):
		doTest(optMeasurementsRemoveOperatorsList[i])
	closeQuerySummarises(queryEnergyFile)
	closeQuerySummary(queryGreenFile)
	closeQuerySummary(queryRedFile)
	closeQuerySummary(queryYellowFile)
	closeQuerySummary(queryYellowMinFile)
	closeQuerySummary(querySizeFile)
	report ("Done");
	logging.shutdown();

if __name__ == "__main__":
	main()
