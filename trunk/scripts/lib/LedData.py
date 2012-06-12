import sys, string, os, re, fileinput, FrequencyLib 

class LedData(object):

	def __init__(self, site, yellowDescription = "Yellow", greenDescription = "Green", redDescription = "Red"):
		self.site = site
		if yellowDescription == None:
			self.yellowDescription = "Yellow Led"	
		else:	
			self.yellowDescription =yellowDescription
		self.yellowOn = False
		self.yellowTimes = {}
		self.yellowInitFound = False
		if greenDescription == None:
			self.greenDescription = "Green Led"	
		else:	
			self.greenDescription = greenDescription
		self.greenOn = False
		self.greenTimes = {}
		self.greenInitFound = False
		if redDescription == None:
			self.redDescription = "Red Led"	
		else:	
			self.redDescription = redDescription
		self.redOn = False
		self.redTimes = {}
		self.redInitFound = False
		self.redInitFound = False

	def parseLine(self,line):
		m = re.search("   (\d+)( +)(\d+)( +)(on|off)( +)(on|off)( +)(on|off)", line)
		#group(1) = site
		#group(2) = spaces
		cycle = int(m.group(3))
		yellow = m.group(5)
		green = m.group(7)
		red = m.group(9)

		if (yellow == "on"):
			if (not self.yellowOn):
				self.yellowOn= True
				self.yellowStart = cycle
		else:
			if (self.yellowOn):
				self.yellowOn = False
				#ignore first one
				if self.yellowInitFound:
					self.yellowEnd = cycle
					yellowTime = self.yellowEnd - self.yellowStart
					#if (self.site == 0):
					#	report (str(self.site)+" yellow"+str(yellowTime)+" at "+str(cycle))
					self.yellowTimes[yellowTime] = self.yellowTimes.get(yellowTime,0) + 1  
				else:
					self.yellowInitFound = True
						
		if (green == "on"):
			if (not self.greenOn):
				self.greenStart = cycle
				self.greenOn= True
		else:
			if (self.greenOn):
				self.greenOn = False
				#ignore first one
				if self.greenInitFound:
					self.greenEnd = cycle
					greenTime = self.greenEnd - self.greenStart
					#if (self.site == 0):
					#	report (str(self.site)+" green"+str(greenTime)+" at "+str(cycle))
					self.greenTimes[greenTime] = self.greenTimes.get(greenTime,0) + 1  
				else:
					self.greenInitFound = True

		if (red == "on"):
			if (not self.redOn):
				self.redStart = cycle
				self.redOn= True
		else:
			if (self.redOn):
				self.redOn = False
				#ignore first one
				if self.redInitFound:
					self.redEnd = cycle
					redTime = self.redEnd - self.redStart
					#if (self.site == 0):
					#	report (str(self.site)+" red"+str(redTime)+" at "+str(cycle))
					self.redTimes[redTime] = self.redTimes.get(redTime,0) + 1  
				else:
					self.redInitFound = True

	def removeFirstSix(self, times):
		if (6 in times):
			sixCount = times.get(6)
			if (sixCount == 1):
				times.pop(6)
			else:
				times[6] = sixCount - 1
		else: 
			reportWarning ("No startup 6 found in :")
			reportWarning (times)

	def reportTimes (self, times, description):
		#if (len(times) < 1):
		#	report ("no data")
		#	return
		#self.removeFirstSix(times)
		#report(description + ":")
		return FrequencyLib.showSumary(times)

	def reportResults(self, yellowFile = None, greenFile = None, redFile = None):
		#report('site: ' + str(self.site))
		temp = self.reportTimes(self.yellowTimes, self.yellowDescription)
		if (yellowFile != None and temp != None):
			yellowFile.write (str(self.site)+","+temp+"\n")
		temp = self.reportTimes(self.greenTimes, self.greenDescription)
		if (greenFile != None and temp != None):
			greenFile.write (str(self.site)+","+temp+"\n")
		temp = self.reportTimes(self.redTimes, self.redDescription)
		if (redFile != None and temp != None):
			redFile.write (str(self.site)+","+temp+"\n")
		#report("")
		#logger.info('count = '+str(self.count))

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

