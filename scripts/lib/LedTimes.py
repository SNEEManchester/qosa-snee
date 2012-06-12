import re, LedData

logger = None

#Registers a logger for this library
def registerLogger(l):
	global logger
	logger = l
	LedData.registerLogger(l)

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

#Returns the evalEpoch that a DELIVER line in the result tuple file pertains to
def getSite(line):
	#   0         48551( +)(on |off) (on |off)(on|off)
	
	m = re.search("   (\d+)( +)(\d+)( +)(on|off)( +)(on|off)( +)(on|off)", line)
	if (m != None):
		i = m.group(1)
		return int(i)
	else:
		return None

logger = None
			
def getLedTimes(traceFilePath, numSites, 
	yellowDescription = "Yellow", greenDescription = "Green", redDescription = "Red"):

	if (yellowDescription == None):
		yellowDescription = "Yellow"
	if (greenDescription == None):
		greenDescription = "Green"
	if (redDescription == None):
		redDescription = "Red"
	#Setup the data storage
	siteData = range(0, numSites)	
	for i in range(0, numSites):
		siteData[i] = LedData.LedData(i, yellowDescription = yellowDescription, 
			greenDescription = greenDescription, redDescription = redDescription)
			
	#parse the avrora output		
	inFile =  open(traceFilePath)
	tmpStr = ""
	while 1:
		line = inFile.readline()
		if not line:
			break
		site = getSite(line)
		if not (site == None): 	
			thisSite = siteData[int(site)]
			siteData[site].parseLine(line)	
	
	#prepare the csv files
	#print traceFilePath
	#print yellowDescription
	yellowPath = traceFilePath.replace("avrora-out.txt",yellowDescription+".csv")
	yellowFile = open(yellowPath,"w")
	yellowFile.write ("Site, Count, Min ,Max , Sum, Average , StDev\n")

	greenPath = traceFilePath.replace("avrora-out.txt",greenDescription+".csv")
	greenFile = open(greenPath,"w")
	greenFile.write ("Site, Count, Min ,Max , Sum, Average , StDev\n")
	
	redPath = traceFilePath.replace("avrora-out.txt",redDescription+".csv")
	redFile = open(redPath,"w")
	redFile.write ("Site, Count, Min ,Max , Sum, Average , StDev\n")

	#report the results
	for i in range(0, numSites):
		siteData[i].reportResults(yellowFile = yellowFile, greenFile = greenFile, redFile = redFile)

	if (yellowDescription != None):
		yellowFile.close();
	if (greenDescription != None):
		greenFile.close();
	if (redDescription != None):
		redFile.close();