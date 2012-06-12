#Util library for frequency distributions held as dictionaries of key:count
from math import sqrt

def sortedReport(frequencies):
	keys = frequencies.keys()
	keys.sort()
	for key in keys:
		report(str(key)+" :" + str(frequencies[key]))

def showSumary(frequencies):
	sortedReport(frequencies)
	if len(frequencies) > 0:
		keys = frequencies.keys()
		min = keys[0]
		max = keys[0]
		sum = 0
		square = 0
		sumSquare = 0
		count = 0
		for (key, value) in frequencies.iteritems():
			if (key < min):
				min = key
			if (key > max):
				max = key
			count = count + value
			sum = sum + (key * value)
			sumSquare = sumSquare + (key * key * value)
		#report("Count: " + str(count))
		#report("Min: " + str(min))
		#report("Max: " + str(max))
		average = (sum / count)
		#report("Average: " + str(average))
		#report("Standard Deviation: "+ str(sqrt((sumSquare/count)-(average*average))))
		return (str(count)+","+str(min)+","+str(max)+","+str(sum)+","+str(average)+","+str(sqrt((sumSquare/count)-(average*average))))
	else:
		#report ("count = 0")
		return ("0,n/a,n/a,n/a,n/a")

def merge(frequency1, frequency2):
	results = {}
	results.update(frequency1)
	for (key, value) in frequency2.iteritems():
		results[key] = frequency1.get(key,0) + value
	return results		

def add(frequency1, frequency2):
	print frequency1
	print frequency2
	for (key, value) in frequency2.iteritems():
		frequency1[key] = frequency1.get(key,0) + value
	print frequency1
	print frequency2
	return frequency1		

logger = None

#Registers a logger for this library
def registerLogger(l):
	global logger
	logger = l

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
	

def main(): 	
	test = {12: 4139, 54: 4127, 34: 4098}
	showSumary(test)
	
if __name__ == "__main__":
	main()
	