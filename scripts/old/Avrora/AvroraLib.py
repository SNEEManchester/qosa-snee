import re,fileinput, os, MonitorData, string, experimentLib,time 
#import os, sys, logging, re, fileinput, time, string

def setOpts(optJava5exe, optDataPath, optIgnoreLedEnergy, optIgnoreStartupTime, optControlRadioOff, optSNEEqlRootDir, optSimulationSeed, optSimulationDuration, optAvroraPath):
	global java5exe, dataPath, ignoreLedEnergy, ignoreStartupTime, controlRadioOff, SNEEqlRootDir, SNEEqlSourceDir, simulationSeed, simulationDuration, avroraPath
	java5exe = optJava5exe
	dataPath = optDataPath
	ignoreLedEnergy = optIgnoreLedEnergy
	ignoreStartupTime = optIgnoreStartupTime
	controlRadioOff = optControlRadioOff
	SNEEqlRootDir = optSNEEqlRootDir
	SNEEqlSourceDir = optSNEEqlRootDir+"src/"
	simulationSeed = optSimulationSeed
	simulationDuration = optSimulationDuration
	avroraPath = optAvroraPath
	
def splitFile(inputFile):
	
	simulationEndPattern= re.compile('==========*')
	monitorPattern= re.compile ('=={ Monitors for node (\d+) }=*')
	
	directory = os.path.dirname(inputFile)
	
	outputFile = open(directory+'/simulationBody.txt','w')
	site = -1
	
	for line in fileinput.input([inputFile]):
		monitorMatch = monitorPattern.match(line)
		if monitorMatch: 
			outputFile.close()	
			site = int(monitorMatch.group(1))
			outputFile = open (directory + '/mote'+str(site)+'.txt','w')
		outputFile.write(line)
		
	outputFile.close()
	return site
	
def readData(inputFile):
	
	fileUsed = inputFile
	
	directory = os.path.dirname(inputFile)
	monitors = []
	maxSite = splitFile (inputFile)
	
	#print maxSite

	for site in range(maxSite+1):
		#print site
		monitors = monitors + [MonitorData.MonitorData(site)]
		#monitors[site] = MonitorData.MonitorData(site)
		monitors[site].readData(directory + '/mote'+str(site)+'.txt')

	return maxSite, monitors

#given the output of a PowerTossim script with total energy consumed, computes the total power used
#returns: (possibly with the LED toatls removed)
#sumEnergy: The sum of the total of all motes
#maxEnergy: The maximun total of any one mote
#averageEnergy: The average total of all motes
#radioEnergy: The sum of the radio for all motes
#cpu_cycleEnergy: The sum of the cpy_cycle for all motes
def computeEnergyConsumed(inputFile):
	global maxSite, monitors

	sumEnergy = 0
	maxEnergy = 0
	radioEnergy =0
	sensorEnergy = 0
	otherEnergy = 0
	cpu_cycleEnergy = 0;

	maxSite, monitors = readData(inputFile)

	for site in range(maxSite+1):
		if (ignoreLedEnergy):
			sumEnergy = sumEnergy + monitors[site].noLEDenergy()
			if (maxEnergy < monitors[site].noLEDenergy()):
				maxEnergy = monitors[site].noLEDenergy()
			otherEnergy = otherEnergy + monitors[site].Flash()
		else:		
			sumEnergy = sumEnergy + monitors[site].totalEnergy()
			if (maxEnergy < monitors[site].totalEnergy()):
				maxEnergy = monitors[site].totalEnergy()
			otherEnergy = otherEnergy + monitors[site].Flash()+monitors[site].LEDs()
		radioEnergy = radioEnergy + monitors[site].Radio()
		sensorEnergy = sensorEnergy + monitors[site].SensorBoard()
		#print "CPU on "+str(site)+" ="+str(monitors[site].CPU())
		cpu_cycleEnergy = cpu_cycleEnergy + monitors[site].CPU()
		#print "total = "+ str(cpu_cycleEnergy)

	averageEnergy = float(sumEnergy / (maxSite+1))
	
	#print ("maxEnergy ="+str(maxEnergy))
	#print ("cpu_cycleEnergy ="+str(cpu_cycleEnergy))
	#print ("sensorEnergy ="+str(sensorEnergy))
	#print ("radioEnergy ="+str(radioEnergy))
	#print ("otherEnergy ="+str(otherEnergy))
	#print ("sumEnergy ="+str(sumEnergy))

	return (sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy)
	
#Returns the number of nodes given a network connectivity file path
#Assumes the Avrora top file format
def getNumNodes(networkFilePath):
	
	numNodes = 0;
	for line in fileinput.input([networkFilePath]):
		if not line.strip().startswith('#'):
			numNodes = numNodes + 1
	
	return numNodes

#assumes computeEnergyConsumed has been run.
def getPacketCount():
	total = 0;
	for site in range(maxSite+1):
		total = total+ monitors[site].Packets()
	return total	
	
#assumes computeEnergyConsumed has been run.
def getWorkingEnergy():
	total = 0;
	for site in range(maxSite+1):
		total = total+ monitors[site].WorkingEnergy()
	return total	

def getOds(nescDir, numNodes):
	#print 'nescDir:'+nescDir
	ods = []
	for i in range(numNodes):
		fileName = nescDir +"mote"+str(i)+".od"
		if os.path.exists(fileName):
			ods = ods + [fileName]
		else:
			ods = ods + [SNEEqlSourceDir+"empty.od"]
	return ods	
	
def getCount(numNodes):
	count = []
	for i in range(numNodes):
		count = count + [str(1)]
	return count	
	
def getSensorData (numNodes):
	dataLines = []
	for i in range(numNodes):
		fileName = dataPath + "/node"+str(i)+".txt"
		if os.path.exists(fileName):
			dataLines = dataLines + ["light:"+str(i)+":"+fileName]
		else:	
			fileName = dataPath + "/node.txt"
			if os.path.exists(fileName):
				dataLines = dataLines + ["light:"+str(i)+":"+fileName]
			else:
				dataLines = dataLines + ["light:"+str(i)+":."]
	return dataLines	

def runSimulation(nescDir, outputDir, desc, numNodes, networkFilePath, logger):	
	
	if (os.path.isdir(nescDir)):	
		if (simulationSeed == None):
			params = []
		else:
			logger.warning('Setting simulation seed not yet implemented in Avrora')	
			print 'Setting simulation seed not yet implemented in Avrora'	
			#params = ['-seed='+str(simulationSeed)] 

		topologyStr = ''
		if networkFilePath.endswith('.top'):
			topologyStr = '-topology='+networkFilePath
		
		#monitors = "-monitors=packet,uk.ac.manchester.cs.diasmc.MemoryMonitor -verbose-locations=9:pres:0x0268:uint16,9:id:0x0257:uint16,9:time:0x021C:int32,9:inhead:0x01C2:int8"
		#monitors = "-monitors=packet,energy,uk.ac.manchester.cs.diasmc.CallLengthMonitor"
		monitors = "-monitors=packet,energy"

		#Prepare the directory 
		print 'Running Avrora simulation for query '+desc
		logger.info('Running Avrora simulation for query '+desc)
	
		ods = getOds(nescDir, numNodes)
		count = getCount(numNodes)
		dataLines = getSensorData(numNodes)

		outputFile = outputDir+'/avrora.txt'

		#os.chdir(avroraPath)		
		os.chdir(SNEEqlSourceDir+"../scripts/avrora")
		#print "current dir:"+os.cwd()
		
		avroraString = java5exe +" -jar avrora-beta-1.6.0.jar -simulation=sensor-network -colors=false -sensor-data=%s -seconds=%s %s %s -nodecount=%s %s > %s"
	
		avroraString = avroraString % (string.join(dataLines,','), str(simulationDuration), monitors, topologyStr, string.join(count,','), string.join(ods,' '), outputFile )
	
		experimentLib.runCommand("export MOTECOM=serial@COM1:mica", logger)	

		experimentLib.runCommand(avroraString,logger)
	else:
		logger.warning('In RunSimulation: No NesC directory found for query '+desc)	
		print 'No NesC compilation directory found for query '+desc
	
def runTinyDB (tinydbDir, outputDir, query, numNodes, networkFilePath, logger):	
	
	print "numNodes = "+str(numNodes)
	
	if (simulationSeed == None):
		params = []
	else:
		logger.warning('Setting simulation seed not yet implemented in Avrora')	
		print 'Setting simulation seed not yet implemented in Avrora'	
		#params = ['-seed='+str(simulationSeed)] 
		topologyStr = ''
	if networkFilePath.endswith('.top'):
		topologyStr = '-topology='+networkFilePath
	
	#monitors = "-monitors=packet,uk.ac.manchester.cs.diasmc.MemoryMonitor -verbose-locations=9:pres:0x0268:uint16,9:id:0x0257:uint16,9:time:0x021C:int32,9:inhead:0x01C2:int8"
	#monitors = "-monitors=packet,energy,uk.ac.manchester.cs.diasmc.CallLengthMonitor"
	monitors = "-monitors=packet,energy,serial"
	#Prepare the directory 
	print 'Running TinyDB Avrora simulation for query '+query
	logger.info('Running TinyDB Avrora simulation for query '+query)

	#ods = getOds(nescDir, numNodes)
	od = "./tinydb.od"
	#count = getCount(numNodes)
	count = numNodes
	dataLines = getSensorData(numNodes)
	outputFile = outputDir+'avrora.txt'
	
	avroraString = java5exe +" -jar avrora-beta-1.6.0.jar -update-node-id=false -simulation=sensor-network -random-seed=1 -colors=false -sensor-data=%s -seconds=%s %s %s -nodecount=%s %s > %s"
	
	avroraString = avroraString % (string.join(dataLines,','), str(simulationDuration), monitors, topologyStr, count, od, outputFile )

	#avroraString = java5exe +" avrora.Main -simulation=sensor-network -colors=false -sensor-data=%s -seconds=%s %s %s -nodecount=%s %s"
	
	#avroraString = avroraString % (string.join(dataLines,','), str(simulationDuration), monitors, topologyStr, count, od)

	os.chdir(os.getenv('TOSROOT')+'/tools/java')
	#os.chdir("c:/myTinyosJava")
	
	print '\nRunning TinyDB Query: '+str(query)+'\n'

	#experimentLib.runCommand(java5exe+ ' net.tinyos.tinydb.TinyDBMain -run \"'+ query+'\"',logger)
	
	#experimentLib.runCommand("export MOTECOM=serial@COM1:mica", logger)	
	experimentLib.runCommand("export MOTECOM=network@127.0.0.1:2390", logger)

	java5Exe = java5exe.replace('\\','')	
	if (not java5Exe.endswith('.exe')):
		java5Exe = java5Exe + '.exe'
	os.spawnv(os.P_NOWAIT, java5Exe, ['java','net.tinyos.tinydb.TinyDBMain','-text','-run', query])

	#time.sleep(60)

	#os.chdir(avroraPath)		
	os.chdir(SNEEqlSourceDir+"../scripts/avrora")
	
	experimentLib.runCommand(avroraString,logger)
	

def main(): 
	global ignoreLedEnergy
	ignoreLedEnergy = False
	computeEnergyConsumed ("c:/tmp/output/avrora.txt")
	
if __name__ == "__main__":
    main()