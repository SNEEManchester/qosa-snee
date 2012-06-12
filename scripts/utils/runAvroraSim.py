#!/usr/bin/python
import sys, os, AvroraLib, UtilLib, logging, getopt

optNumNodes = None
optTopFile = None
optSimDuration = 60
optSkipODs = False

def usage():
	print "usage: runAvroraSim.py --num-nodes=[numNodes] --top-file=[topFile] --duration=[duration] [--skip-ods]"
	print "\tnumNodes       The number of nodes in simulation."
	print "\ttopFile        The Avrora topology file."
	print "\tduration       The simultion duration (default 60s)."
	print "\tskip-ods       Include this option if the ODs have already been generated (faster)."
	sys.exit(2)

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

def startLogger(timeStamp):
	global logger

	logger = logging.getLogger('log')

	hdlr = logging.FileHandler('test%s.log' % (timeStamp))
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting Avrora simulation')

def parseArgs(args):
	global optNumNodes, optTopFile, optSimDuration, optSkipODs
	
	try:
		optNames = ["help", "num-nodes=", "top-file=", "duration=", "skip-ods"]

		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
		
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()

		if (o == '--num-nodes'):
			optNumNodes = int(a)
			
		if (o == '--top-file'):
			optTopFile = a
			
		if (o == '--duration'):
			optSimDuration = int(a)
			
		if (o == '--skip-ods'):	
			optSkipODs = True
			

def runSimulation(nescDir):

	#run avrora simulation
	(avroraOutputFile, traceFilePath) = AvroraLib.runSimulation(nescDir, nescDir, 'sim', optNumNodes, optSimDuration, optTopFile)

	#Report total energy consumption
	siteLifetimeRankFile = "%s/site-lifetime-rank.csv" % nescDir
	(sumEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy, networkLifetime) = AvroraLib.computeEnergyValues(nescDir, optSimDuration, inputFile = "avrora-out.txt", ignoreLedEnergy = True, siteLifetimeRankFile = siteLifetimeRankFile)

	report ("The total energy consumption is %f" % (sumEnergy))
	report ("The lifetime for this network is %f" % (networkLifetime))


def main(): 	
	timeStamp = UtilLib.getTimeStamp()
	startLogger(timeStamp)

	if (not os.getcwd().endswith("avrora1")):
		print "This script should be run from the avrora directory"
		sys.exit(-1)

	parseArgs(sys.argv[1:])

	nescDir = os.getcwd()	
	if not optSkipODs:
		AvroraLib.compileNesCCode(nescDir)
		AvroraLib.generateODs(nescDir)

	runSimulation(nescDir)

if __name__ == "__main__":
	main()
