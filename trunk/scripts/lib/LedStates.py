import sys, string, os, re, fileinput, FrequencyLib 

class LedStates(object):

	def __init__(self, site, verbose = False):
		self.site = site
		self.counts = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
		self.verbose = verbose

	def parseLine(self,line):
		m = re.search("   (\d+)( +)(\d+)( +)(on|off)( +)(on|off)( +)(on|off)", line)
		#group(1) = site
		#group(2) = spaces
		cycle = int(m.group(3))
		yellow = m.group(5)
		green = m.group(7)
		red = m.group(9)

		state = 9
		if (yellow == "on"):
			if (green == "on"):
				if (red == "on"):
					state = 11
				else:	
					state = 12
			else:		
				if (red == "on"):
					state = 13
				else:	
					state = 14
		else:			
			if (green == "on"):
				if (red == "on"):
					state = 15
				else:	
					state = 16
			else:		
				if (red == "on"):
					state = 1
				else:	
					state = 0
		if self.verbose:			
			report ("site "+str(self.site) + " state "+str(state)+" cycle "+str(cycle))
		self.counts[state]= self.counts[state] + 1

	def reportResults(self):
		report('site: ' + str(self.site))
		for state in range(0,9):
			report("state "+str(state) + " : "+str(self.counts[state]))

logger = None

#Registers a logger for this library
def registerLogger(l):
	global logger
	logger = l
	FrequencyLib.registerLogger(l)

#Ouput info message to screen and logger if applicable
def report(message):
	if (logger != None):
		logger.info(message)
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

