import string, os, UtilLib, logging, sys, threading, time, re, os.path, fileinput

#Default Parameters used by Avrora Library
optJava5exe = os.getenv("JAVA5BINDIR") + "/java.exe" 
optAvroraClassPath = "C:\\dias-mc\\work2\\avrora\\christian-avrora\\src"
optDataPath = None

logger = None

def getOptNames():
	optNames = ["avrora-class-path=", "java-5-exe="]
	return optNames

def usage():
	print '\nFor the Avrora library:'
	print '--avrora-class-path=<dir> \n\tdefault: '+optAvroraClassPath
	print '--java-5-exe=<file> \n\tdefault: '+optJava5exe
	

def setOpts(opts):
	global optAvroraClassPath, optJava5exe
	
	for o, a in opts:
		if (o== "--avrora-class-path"):
			optAvroraClassPath = a
			continue
		if (o== "--java-5-exe"):
			optJava5exe = a
			continue

			
#Registers a logger for this library
def registerLogger(l):
 	global logger
 	logger = l


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

#reads the logfile from the current directory and reports the main details
def readMakeLog(logfile, sizeFile):
	global ram, rom
	sizeStr = ""
	inFile =  open(logfile)
	while 1:
		line = inFile.readline()
		if not line:
			return 0;
		m = re.search("    compiled QueryPlan(\d+) to build/mica2/main.exe", line)
		if (m != None):
			mote = int(m.group(1))			
			report("Compiled mote "+str(mote))
		m = re.search("( +)(\d+) bytes in ROM", line)
		if (m != None):
			rom = int(m.group(2))
			report("ROM usage = "+str(rom))
		m = re.search("( +)(\d+) bytes in RAM", line)
		if (m != None):
			ram = int(m.group(2))
			report("RAM usage = "+str(ram))
			sizeFile.write(str(mote)+","+str(rom)+","+str(ram)+","+str(rom+ram)+"\n")
			if (ram > 4096):
				reportError ("RAM = "+str(ram)+" in file "+sizeFile)
				return ram	
			
#Given a nesC root directory, compiles the nesC code for use with Avrora as mica2 executable
def compileNesCCode(nescRootDir):
	exitVal = 0
	os.chdir(nescRootDir)
	sizeFile = open("size.csv","w")
	sizeFile.write("Mote,ROM,RAM,Total\n")
	for dir in os.listdir(nescRootDir):
		if (dir.startswith("mote") and os.path.isdir(dir)):
			os.chdir(dir)
			exitVal = os.system("make mica2 >make.log")
			if (exitVal != 0):
				reportError("Failed with "+dir+" compilation")
				return exitVal
			exitVal = readMakeLog("make.log",sizeFile)
			if (exitVal != 0):
				reportError("RAM overflow with "+dir+" compilation")
				return exitVal
		os.chdir(nescRootDir)
	sizeFile.close()
	return exitVal
	

#Given compiled nesC root directory, dissasembles all the mica2 executables for use with Avrora
def generateODs(nescRootDir):
	os.system("cp " + os.path.dirname(__file__) + "/empty.od " + nescRootDir)
	for dir in os.listdir(nescRootDir):
		m = re.match("mote(\d+)$", dir)
		if (m != None):
			i = m.group(1)
			os.chdir(nescRootDir + "/" + dir)
			if (os.path.exists(nescRootDir + "/" + dir + "/build/mica2/main.exe")):
				commandStr = "avr-objdump -zhD ./build/mica2/main.exe > ../mote"+str(i)+".od"
				exitVal = os.system(commandStr)
			else:	
				reportError(dir + " not compiled yet")
				sys.exit()
	os.chdir(nescRootDir)
				
#Returns a string specifying the data sources for the query plan
#TODO: check what dataPath is
def __getSensorData (numNodes):
	sensorData = []
	for i in range(numNodes):
		if optDataPath == None:
			sensorData = sensorData + ["light:"+str(i)+":."]		
		else:
			fileName = optDataPath + "/node"+str(i)+".txt"
			if os.path.exists(fileName):
				sensorData = sensorData + ["light:"+str(i)+":"+fileName]
			else:	
				print "Data file not found"
				exit(2)

	return string.join(sensorData,',')


#Returns a string of the form 1,1,1 used by the Avrora count parameter
def __getCountStr(numNodes):
	count = []
	for i in range(numNodes):
		count = count + [str(1)]
	return string.join(count,',')
	

#Returns a string with a list of the OD filenames for each mote in the query plan, used as parameter for Avrora.  Note that if a node does not participate in the query plan the list is padded with empty.od
def getODs(nescRootDir, numNodes):
	#print 'nescRootDir:'+nescRootDir
	ods = []
	for i in range(0, numNodes):
		fileName = nescRootDir + "/mote"+str(i)+".od"
		winNescRootDir = UtilLib.winpath(nescRootDir)
		if os.path.exists(fileName):
			ods = ods + ["\"" + winNescRootDir + "/mote"+str(i)+".od\""]
		else:
			ods = ods + ["\"" + winNescRootDir + "/empty.od\""]
			if not os.path.exists(nescRootDir+'/empty.od'):
				os.system("cp " + os.path.dirname(__file__) + "/empty.od "+nescRootDir+"/empty.od")
			
	return string.join(ods,' ')



class AvroraThread ( threading.Thread ):
	def __init__ ( self, commandStr, deliverToSerial ):
		self.commandStr = commandStr
		self.deliverToSerial = deliverToSerial
		threading.Thread.__init__ ( self )
		
	def run ( self ):
		os.putenv('CLASSPATH',optAvroraClassPath+';'+os.getenv('CLASSPATH'))
		report ("Executing " + self.commandStr)
		os.system(self.commandStr)
		if (self.deliverToSerial):
			#This should kill the other thread
			os.system('taskkill -f -im java.exe')

class ListenerThread ( threading.Thread ):
	def __init__ ( self, deliverOutputFile ):
		self.deliverOutputFile = deliverOutputFile
		threading.Thread.__init__ ( self )
	def run ( self ):
		os.putenv('MOTECOM', "network@127.0.0.1:2390")
		commandStr = "java net.tinyos.tools.Listen > %s 2> err.txt" % self.deliverOutputFile
		report ("Executing " + commandStr)		
		os.system(commandStr)
		reportWarning(UtilLib.getFileContents("err.txt"))
		

def readDeliveredPackets(inFilename, outFilename = None):
	inFile =  open(inFilename)

	tmpStr = ""
	while 1:
		line = inFile.readline()
		if not line:
			break
		bytes = line.split();
		for b in range(5, len(bytes)): #ignore the first 5 bytes which are header bytes
			nextChar = string.atoi(bytes[b],16)
			if (nextChar==ord('\0')):
				break		
			else:
				tmpStr += chr(nextChar)

	if (outFilename == None):
		print tmpStr
	else:
		outFile = open(outFilename, 'w') #create new file for writing
		outFile.writelines(tmpStr)
		outFile.close()


#Runs Avrora simulation according to given parameters		
def runSimulation(nescRootDir, outputDir, desc, numNodes, simulationDuration = 100, networkFilePath = None, useEnergyMonitor = True, deliverToSerial = True, packetMonitor = True, randomSeed = 0):

	#Random sensor data for each sensor
	sensorDataStr = __getSensorData(numNodes)

	#Relevant monitors
	monitorStr = ""	
	if (useEnergyMonitor or deliverToSerial):
		monitors = []
		if (useEnergyMonitor):
			monitors.append("energy")
		if (deliverToSerial):
			monitors.append("serial")
		if (packetMonitor):
			monitors.append("packet")
		
		#Suggest this is a parameter rather than hard-coded in.
		#(see above monitors)
		#monitors.append ("trip-time")
		
		monitorStr = "-monitors=" + string.join(monitors, ",")
		if (deliverToSerial):
			monitorStr += " -node=0 -port=2390 "

		#Suggest this is a parameter rather than hard-coded in.
		#this is a generic library...
		#monitorStr += " -pairs=0x2b52:0x2b92"
		
	#Topology string	
	topologyStr = ''
	if networkFilePath != None:
		if networkFilePath.endswith('.top'):
			topologyStr = '-topology='+UtilLib.winpath(networkFilePath)

	#Count str
	countStr = __getCountStr(numNodes)	
		
	#OD files
	odStr = getODs(nescRootDir, numNodes)
	
	outputFile = outputDir + "/avrora-out.txt"
	deliverOutputFile = outputDir + "/deliver-out.txt"
	tupleOutputFile = outputDir + "/tuple-out.txt"
	
	commandStr = optJava5exe + " -Xms32m -Xmx1024m avrora.Main -simulation=sensor-network -colors=false -random-seed=%d -sensor-data=%s -seconds=%s %s %s -nodecount=%s %s > %s"
	commandStr = commandStr % (randomSeed, sensorDataStr, str(simulationDuration), monitorStr, topologyStr, countStr, odStr, outputFile)
	
	AvroraThread(commandStr, deliverToSerial).start()
	if (deliverToSerial):
		time.sleep(10 + numNodes) #1 second per node seems to be more than enough
		ListenerThread(deliverOutputFile).start() 

		#Wait for both threads to finish
		while (len(threading.enumerate()) > 1):
			time.sleep(10)

		readDeliveredPackets(deliverOutputFile, tupleOutputFile)
	return (outputFile, tupleOutputFile)

class EnergyMonitorData(object):
    
	def __init__(self, id):
		self.id = id
	    	
	def getTotalEnergy(self):
		return self.cpu + self.yellow + self.green + self.red + self.radio + self.sensorBoard + self.flash	

	def getLedsEnergy(self):
		return self.yellow + self.green + self.red

	def getTotalEnergyLessLeds(self):
		return self.cpu + self.radio + self.sensorBoard + self.flash	
		
	def readData (self, line):
        
 		packetsPattern = re.compile ('Packets sent: (\d.+) ')
    
		CPUPattern = re.compile ('CPU: (.+) Joule')
		ActivePattern = re.compile ('.+Active: (.+) Joule, (.+) cycles')
   		yellowPattern = re.compile ('Yellow: (.+) Joule')
		greenPattern = re.compile ('Green: (.+) Joule')
		redPattern = re.compile ('Red: (.+) Joule')
		radioPattern = re.compile ('Radio: (.+) Joule')
   		Transmit0Pattern = re.compile ('.+Transmit .+0:.+:(.+)Joule,(.+)cycles')
   		Transmit15Pattern = re.compile ('.+Transmit .+15:.+:(.+)Joule,(.+)cycles')
		sensorBoardPattern = re.compile ('SensorBoard: (.+) Joule')
		flashPattern = re.compile ('flash: (.+) Joule')
	
		self.packets = 0
		
		self.transmit15 = 0

		match = packetsPattern.match(line)
		#print line
		if match:
			self.packets = int(match.group(1))
			#print str(self.id) + ': Packets sent = ' + str(self.packets) 
		match = CPUPattern.match(line)
		if match:
			self.cpu = float(match.group(1))
			#print str(self.id) + ': CPU = ' + str(self.cpu) 
		match = ActivePattern.match(line)
		if match:
			self.active = float(match.group(1))
			#print str(self.id) + ': active = ' + str(self.active) 
		match = yellowPattern.match(line)
		if match:
			self.yellow = float(match.group(1))
			#print str(self.id) + ': Yellow = ' + str(self.yellow) 
		match = greenPattern.match(line)
		if match:
			self.green = float(match.group(1))
			#print str(self.id) + ': Green = ' + str(self.green) 
		match = redPattern.match(line)
		if match:
			self.red = float(match.group(1))
			#print str(self.id) + ': Red = ' + str(self.red) 
		match = radioPattern.match(line)
		if match:
			self.radio = float(match.group(1))
			#print str(self.id) + ': Radio = ' + str(self.radio) 
		match = Transmit0Pattern.match(line)
		if match:
			self.transmit0 = float(match.group(1))
			#print str(self.id) + ': Transmit0 = ' + str(self.transmit0) 
		match = Transmit15Pattern.match(line)
		if match:
			self.transmit15 = float(match.group(1))
			#print str(self.id) + ': Transmit15 = ' + str(self.transmit15) 
		match = sensorBoardPattern.match(line)
		if match:
			self.sensorBoard = float(match.group(1))
			#print str(self.id) + ': SensorBoard = ' + str(self.sensorBoard) 
		match = flashPattern.match(line)
		if match:
			self.flash = float(match.group(1))
			#print str(self.id) + ': flash = ' + str(self.flash) 
	
	
def __parseOutputFile(inputFile):

	monitors = {}
	site = -1
	newSitePattern= re.compile ('=={ Monitors for node (\d+) }=*')
	
	for line in fileinput.input([inputFile]):
		newSiteMatch = newSitePattern.match(line)
		if (newSiteMatch):
			site = int(newSiteMatch.group(1))
			monitors[site] = EnergyMonitorData(site)
			continue
	
		if (site > -1):
			monitors[site].readData(line)	
	return monitors


def buildLifetimeIndex(siteLifetimes, numNodes):
	siteLifetimeIndex = range(0, numNodes)
	#basically a bubble sort on the index
	for i in range(0, numNodes):
		for j in range(i+1, numNodes):
			if (siteLifetimes[siteLifetimeIndex[i]] > siteLifetimes[siteLifetimeIndex[j]]):
				tmp = siteLifetimeIndex[j]
				siteLifetimeIndex[j] = siteLifetimeIndex[i]
				siteLifetimeIndex[i] = tmp
	return siteLifetimeIndex


def generateLifetimeRankFile(lifetimeRankFile, siteLifetimes, numNodes):
	#siteLifetimeIndex: Ordered list of site ids with longest lifetime to shortest lifetime
	siteLifetimeIndex = buildLifetimeIndex(siteLifetimes, numNodes)	
	f = open(lifetimeRankFile, "w")
	f.writelines("Site id, Lifetime\n")
	for i in range(0, numNodes):
		line = "%d,%d\n" % (siteLifetimeIndex[i], siteLifetimes[siteLifetimeIndex[i]])
		f.writelines(line)
	f.close()	
	
	
#given the output of an Avrora simulation with the energy monitor, computes the total power used
#returns: (possibly with the LED toatls removed)
#sumEnergy: The sum of the total of all motes
#maxEnergy: The maximum total of any one mote
#averageEnergy: The average total of all motes
#radioEnergy: The sum of the radio for all motes
#cpu_cycleEnergy: The sum of the cpy_cycle for all motes
#networkLifetime
#siteLifetimeRankFile: Filename which gives ranked list of individual site lifetimes as CSV

#defaultSiteEnergyStock = 31320J, two AA Lithium batteries, as used in our paper
#Energy values returned are in Joules
#SimulationDuration parameter needs to be given in seconds
#Lifetime value returned is in seconds
def computeEnergyValues(dirName, simulationDuration, inputFile = "avrora-out.txt", ignoreLedEnergy = True, defaultSiteEnergyStock = 31320, siteLifetimeRankFile = None):
	global maxSite, monitors

	sumEnergy = 0
	maxEnergy = 0
	radioEnergy =0
	sensorEnergy = 0
	otherEnergy = 0
	cpu_cycleEnergy = 0
	networkLifetime = -1

	monitors = __parseOutputFile(dirName + '/' + inputFile)

	numNodes = len(monitors.keys())
	#siteLifetimes: The lifetimes of each individual site
	siteLifetimes = [-1] * numNodes
	
	for site in monitors.keys():
		energyStock = defaultSiteEnergyStock
	
		if (ignoreLedEnergy):
			sumEnergy = sumEnergy + monitors[site].getTotalEnergyLessLeds()
			if (maxEnergy < monitors[site].getTotalEnergyLessLeds()):
				maxEnergy = monitors[site].getTotalEnergyLessLeds()
			otherEnergy = otherEnergy + monitors[site].flash
			siteLifetime = (energyStock / monitors[site].getTotalEnergyLessLeds()) * simulationDuration
		else:		
			sumEnergy = sumEnergy + monitors[site].getTotalEnergy()
			if (maxEnergy < monitors[site].getTotalEnergy()):
				maxEnergy = monitors[site].getTotalEnergy()
			otherEnergy = otherEnergy + monitors[site].flash+monitors[site].getLedsEnergy()
			siteLifetime = (energyStock / monitors[site].getTotalEnergy()) * simulationDuration


		radioEnergy = radioEnergy + monitors[site].radio
		sensorEnergy = sensorEnergy + monitors[site].sensorBoard
		cpu_cycleEnergy = cpu_cycleEnergy + monitors[site].cpu
		siteLifetimes[site] = siteLifetime

		if site != 0: #ignore the sink node
			if (networkLifetime == -1 or siteLifetime < networkLifetime):
				networkLifetime = siteLifetime

	averageEnergy = float(sumEnergy / numNodes)
	if (siteLifetimeRankFile != None):
		generateLifetimeRankFile(siteLifetimeRankFile, siteLifetimes, numNodes)

	return (sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime)

#Returns the number of nodes given a network connectivity file path
#Assumes the Avrora top file format
def getNumNodes(networkFilePath):
	
	numNodes = 0;
	for line in fileinput.input([networkFilePath]):
		if not line.strip().startswith('#'):
			numNodes = numNodes + 1
	
	return numNodes