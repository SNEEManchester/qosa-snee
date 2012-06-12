import re, LedStates

logger = None

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
			
#Registers a logger for this library
def registerLogger(l):
	global logger
	logger = l
	LedStates.registerLogger(l)

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

def getLedStates(traceFilePath, numSites, verbose = False):

	siteData = range(0, numSites)	
	for i in range(0, numSites):
		siteData[i] = LedStates.LedStates(i, verbose)
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
	for i in range(0, numSites):
		siteData[i].reportResults()
