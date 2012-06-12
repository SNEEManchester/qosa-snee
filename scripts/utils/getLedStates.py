#!/usr/bin/python
import sys, os, LedStateTimes, logging

def __usage():
	print len(sys.argv)
	print sys.argv[1]
	print "Gets the Led times from an Avrora file."
	print "usage: python getLedTimes.py <avroraFile> <numberOfSites>"
	print "\tavroraFile		The file containing the avrora output."
	print "\tnumberOfSites	Maximum number of sites."
	sys.exit(2)

def startLogger(traceFilePath):
	global logger

	logger = logging.getLogger('ledTimes')
	logFile = traceFilePath.replace(".txt",".log");
	print logFile
	
	hdlr = logging.FileHandler(logFile)
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting Led Time Test')

	#Register the logger with the libraries this module uses
	LedStateTimes.registerLogger(logger)

def main():	
	if (len(sys.argv) > 3):
		__usage()

	if (len(sys.argv) < 3):
		__usage()

	if (sys.argv[1]=="-h" or sys.argv[1]=="--help"):
		__usage()

	traceFilePath = sys.argv[1]
	numSites = int(sys.argv[2])

	startLogger(traceFilePath)

	LedStateTimes.getLedStates(traceFilePath, numSites)

if __name__ == "__main__":
	main()
	