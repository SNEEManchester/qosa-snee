import getopt, sys, logging, string, os, re, fileinput, time, ExperimentLib
#  re, fileinput, GraphData, counter, AvroraLib
 
#import xml.dom.minidom
#from xml.dom.minidom import Node

#Parameters relating to execution environment
javaClassPath = '.;C:\\SNEEql\\scripts\\SIGMOD08\\antlr-2.7.5.jar;C:\\SNEEql\\scripts\\SIGMOD08\\SNEEql.jar;C:C:\\SNEEql\\scripts\\SIGMOD08\\avrora-beta-1.6.0.jar'
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'
javaExe = "/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java"
experimentalLogRoot = 'c:/experiments'
SNEEqlDir = 'c:/SNEEql/'
sourceExtension = '/src/'
classExtension = '/bin/'
inputExtension = '/input/'
queryExtension = '/input/'
dataExtension = "/data"

tortoiseSVNbinDir = '/cygdrive/c/Program\ Files/TortoiseSVN/bin/'
#avroraPath = "c:/Program Files/Avrora/bin" 
avroraPath = 'c:/jars/avrora-beta-1.6.0.jar'
antlrPath = 'c:/jars/antlr-2.7.5.jar'#-- Not setable by parameter.

#Parameters relating to script functionality
experimentList = ['1','2','3','4'] #assume all experiments are to be run
	#IgnoreLedEnergy Prevents counting the Energy used by the LED's
	#Unless ledDebug is true SNEEql makes no use of the LEDs so this has no effect.
ignoreLedEnergy = True
	#mAH(2900) * columb/mAH(3.6) * Volatage(1.5) * batteries(2) = Joules
batteryEnergy = 31320
	#CheckQueryResults Request the post processor to check the reults.
	#NOTE: Checking for validity was not done during the published scripts
checkQueryResults = False
	#ExitOnValidityError If set causes the script to exit if an error is found
	#Even if not` set an error messages is still logged
	#NOTE: Checking for validity was not done during the published scripts
exitOnValidityError = False
	#ShowTitleInGraph Instructs the scripts to place a title on the graphs.
showTitleInGraph = True
	#ShowParametersInGraph Instructs the script to place more details in the graphs.
	#Only has an effect if ShowTitleInGraph = True
showParametersInGraph = True

#Parameters relating to all experiments
	#NetworkFile Specifies the file containing the topology
	#File must be in the Tossim accepted format, otherwise it is ignored by Tossim
networkFile = '9node_network_topology'
	#derived
tossimNetworkFilePath = SNEEqlDir + inputExtension + networkFile+'.nss'
	#derived
avroraNetworkFilePath = SNEEqlDir + inputExtension + networkFile+'.top'
	#The file that describes the schema used.
	#No error is caused if the file contains unused schemas
	#TODO Fix the file to reflect that in the paper
schemaFile = '9node_network_schemas.xml'
	#derived
schemaFilePath = SNEEqlDir + inputExtension + schemaFile
	#Directory of files with the avrora input sensor data
#dataPath = SNEEqlDir + dataExtension
	#The files containg the queries used in the experiments
queries = ['Q1.txt','Q2.txt','Q3.txt'] 
queryDir = ''
	#Snooze Inserts code to turn the cpu on and off
	#This will be either the Snooze componet or HLP power management
snooze = False
	#SimulationDuration dettermines how long the experiment should run
	#NOTE: This is in second where all other times are in milliseconds
simulationDuration = 600
	#SimulationSeed Set the Random seed to be used by Tossim
	#NOTE: This does not effect the Random seed of the Java query compiler
simulationSeed = 3
	#nescSynchronizationPeriod dettermines how long the sychnronization loop runs before the query starts
nescSynchronizationPeriod = 3000
	#UseLossy Instructs Tossim to use the network topology as a lossy file
	#The file need to be in unix format or tossim will ignore it wothout throwing an error
useLossy = True
	#DeliverLast When set to true will cause the last loop of the agenda to be adpated
	#This is insure all buffered evaluations are completed.
deliverLast = True
	#LedDebug When set to true allows any led debug statements to be included in the code.
	#This includes linking in the leds.
	#Otherwise all leds debug statemments are not included and the Leds are not linked in.
ledDebug = False
	#bug in q3 with true

#Parameters relating to specific experiments: defaults
#Only effect the experiment in the name
	#exp1AqRates The x axis
	#The various acquisition rates used
exp1AqRates = [1000,2000,3000,4000,5000,6000,10000,12000,15000,20000,30000,60000]
	#exp1MaxBufferingFactor Optional way of limiting buffering factor
	#Best results are obtained by setting it to max
exp1MaxBufferingFactor = sys.maxint
	#exp2AqRate: Acquistion rate used for all points
exp2AqRate = 3000
	#exp2BufferingFactors: The x axis
	#The suggested maximum values for buffering factors that can be used.
	#NOTE: the query optimizer will use a lower value if the suggested on is too high
	#NOTE: Not all poits used as result is a straight line:
exp2BufferingFactors = [1,2,3,4,5,6,7,8,9,10,14,15,20,25,30,35,40,42,43,45]
	#exp3AqRate: Acquistion rate used for all points
exp3AqRate = 3000
	#exp3MaxBufferingFactor Buffering factor used for all points
	#NOTE: May hide the effects of Delivery time unless set as high as possible
exp3MaxBufferingFactor = sys.maxint 
	#exp3DeliveryTimes The x axis
	#The maxium amount of time between an acquistion and it coorespondoin delivery
	#NOTE: Has the same effect as limiting buffering factor
	#NOTE: If the default is used the experiment takes very long to run.
exp3DeliveryTimes = [3000,6000,9000,12000,15000,18000,21000,24000,27000,30000,33000,36000,39000,42000,45000,48000,51000,54000,57000,60000,63000,66000,69000,72000,75000,78000,81000,84000,87000,90000]
	#exp4AqRate: Acquistion rate used for all points
exp4AqRate = 3000
	#exp4BufferingFactor: Partial X axis 
	#The suggested maximum values for buffering factors that SNEEql can use.
	#WARNING: Do not add "TinyDB" the script will do that.
	#NOTE: the query optimizer will use a lower value if the suggested on is too high
	#NOTE: Not only te first few buffering factors effect packet count
exp4BufferingFactors = [1,2]
	#exp4TinyDBQueries Mapping of our query file names to TinyDB queries
	#TODO Make this a readable parameter
#exp4Queries = {'Q0': 'select nodeid,light where nodeid>=7 sample period %s', 'Q2':'select AVG(light) where nodeid>= 7 sample period  %s'} 
exp4Queries = {'Q1.txt': 'select light from sensors where light > 25 sample period %s','Q2.txt':'select avg(temp) from sensors sample period %s'}

#Global variables automatcally set up the system
	#Version control update number
	#obtained automatically by the script
	#NOTE: Unless run with a version obtain from our SVN server it will always be undefined
		#There are no plans to make the svn server publicly accessable at present.
svnVersion = 'undefined'
	#Time the script started running
	#obtained automatically by the script
timeStamp = 'undefined'
numNodes = -1 

#Help text which returned if incorrect paramteres provided
def usage():
	print 'Usage: python runExperiments.py [-options]s'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\n\tdisplay this message'
	
	#Parameters relating to execution environment
	#print '-A<dir>, --avrora-path=<dir> \n\tdefault: '+avroraPath
	print '-C<dir>, --java-class-path=<dir> \n\tdefault: '+javaClassPath
	print '-G<file>, --gnuplot-exe=<file> \n\tdefault: '+gnuPlotExe
	print '-J<file>, --java-exe=<file> \n\tdefault: '+javaExe
	print '-L<dir>, --experimental-log-root=<dir> \n\tdefault: '+experimentalLogRoot
	print '-S<dir>, --SNEEql-root-dir=<dir> \n\tdefault: '+SNEEqlDir
	#print '-T<dir>, --tortoise-svn-bin-dir=<dir> \n\tdefault: '+tortoiseSVNbinDir

	#Parameters relating to script functionality
	print '-x[1|2|3|4|1,..,4|all] \n\tthe experiments to be run.\n\tdefault: '+string.join(experimentList,',')
	print '-p, --post-process-only \n\tdefault: False'
	print '--ignore-led-energy=[True|False]\n\tdefault:  '+str(ignoreLedEnergy)
	print '--check-query-result=[True|False] \n\tdefault: '+str(checkQueryResults)
	print '--exitOnValidityError=[True|False] \n\tdefault: '+str(exitOnValidityError)
	print '--show-title-in-graph=[True|False] \n\tdefault: '+str(showTitleInGraph)
	print '--show-parameters-in-graph=[True|False] \n\tdefault: '+str(showParametersInGraph)
	
	#Parameters relating to all experiments
	print '-n<file>, --network-file=<file> \n\tdefault: '+networkFile
	print '-t<file>, --schema-file=<file> \n\tdefault: '+ schemaFile
	#print '--data-directory=<directory> \n\tdefault: '+dataDirectory
	print '-q<val,..val>, --queries=<val,..val> \n\tdefault: '+string.join(queries,',') 
	print '-d<val>, --simulation-duration=<val> \n\tdefault: '+str(simulationDuration)
	print '-s<val>, --simulation-seed=<val> \n\tdefault: '+ str(simulationSeed)
	print '--snooze=[True|False] \n\t default: '+ str(snooze)
	print '--nesc-synchronization-period=<val>\n\tdefault:  '+str(nescSynchronizationPeriod)
	print '--use-lossy=[True|False] \n\tdefault: '+str(useLossy)
	print '--deliver-last=[True|False] \n\tdefault: '+str(deliverLast)
	print '--led-debug=[True|False] \n\tdefault: '+str(ledDebug)

	#Parameters relating to specific experiments: defaults
	print '--x1a=<val,..val>, --exp1-aq-rates=<val,..val> \n\tdefault: '+string.join(map(str,exp1AqRates),',') 
	print '--x1b=<val>, --exp1-max-buffering-factor=<val> \n\tdefault:  '+str(exp1MaxBufferingFactor)
	print '--x2a=<val>, --exp2-aq-rate=<val> \n\tdefault:  '+str(exp2AqRate)
	print '--x2b=<val,..val>, --exp2-buffering-factors=<val,..val> \n\tdefault:  '+string.join(map(str,exp2BufferingFactors),',')
	print '--x3a=<val>, --exp3-aq-rate=<val> \n\tdefault:  '+str(exp3AqRate)
	print '--x3b=<val>, --exp3-max-buffering-factor=<val> \n\tdefault:  '+str(exp3MaxBufferingFactor)
	print '--x3d=<val,..val>, --exp3-delivery-times=<val,..val> \n\tdefault:  '+string.join(map(str,exp3DeliveryTimes),',')
	print '--x4a=<val>, --exp4-aq-rate=<val>\n\tdefault:  '+str(exp4AqRate)
	print '--x4b=<val>, --exp4--bufferin-factors=<val,..val> \n\tdefault:  '+str(exp4BufferingFactors)

#Use the provided parameters to reset any of the options.
#If parameters is not provided default is used.
def parseArgs(args, requireExplicit):
	#WARNING any new paramters added must be added to this line
	#Parameters relating to execution environment
	#global javaClassPath, gnuPlotExe, javaExe, experimentalLogRoot
	global javaClassPath
	global gnuPlotExe, javaExe, experimentalLogRoot
	global SNEEqlDir #, sourceExtension, classExtension, tortoiseSVNbinDir, avroraPath
	
	#Parameters relating to script functionality
	global experimentList
	global ignoreLedEnergy
	global batteryEnergy, checkQueryResults, exitOnValidityError	
	global showParametersInGraph, showTitleInGraph

	#Parameters relating to all experiments
	global networkFile, tossimNetworkFilePath, avroraNetworkFilePath 
	global schemaFile, schemaFilePath #, dataDirectory, dataPath 
	global queries, snooze
	global simulationDuration, simulationSeed, nescSynchronizationPeriod
	global useLossy
	global deliverLast
	
	#Parameters relating to specific experiments: defaults
	global exp1AqRates, exp1MaxBufferingFactor
	global exp2AqRate, exp2BufferingFactors
	global exp3MaxBufferingFactor, exp3AqRate, exp3DeliveryTimes
	global exp4AqRate, exp4BufferingFactors

	try:
		#WARNING any new flags added must be added to this line.
		opNames = ["help"]

		#Parameters relating to execution environment
		opNames = opNames + ["java-class-path=", "gnu-plot-exe=","java-exe=","experimental-log-root="]
		opNames = opNames + ["SNEEql-dir="]#,"tortoise-svn-bin-dir=","avrora-path="]

		#Parameters relating to script functionality		
		opNames = opNames + ["ignore-led-energy="]
		opNames = opNames + ["battery-energy=","check-query-result=","exitOnValidityError="]
		opNames = opNames + ["show-parameters-in-graph=","show-title-in-graph="]

		#Parameters relating to all experiments
		opNames = opNames + ["network-file=","schema-file="]#, "data-directory="]
		opNames = opNames + ["queries=","count-cpu-cycles=","snooze="]
		opNames = opNames + ["simulation-duration=","simulation-seed=","nesc-synchronization-period="]
		opNames = opNames + ["use-cc1000-radio=","control-radio-off=", "use-lossy="]
		opNames = opNames + ["deliver-last"]

		#Parameters relating to specific experiments: defaults
		opNames = opNames + ["x1a=","exp1-aq-rates=","x1b=","exp1-max-buffering-factor="]
		opNames = opNames + ["x2a=","exp2-buffering-factors=","x2b=","exp2-aq-rate="]
		opNames = opNames + ["x3a=","exp3-aq-rate=","x3b=","exp3-max-buffering-bactor=","x3d=","exp3-delivery-times="]
		opNames = opNames + ["x4a=","exp4-aq-rate=","x4b=","exp4-buffering-factors="]

		#print "args are"
		#print args;
		#print opNames
		opts, args = getopt.getopt(args, "C:G:J:L:S:T:c:d:hn:pq:s:t:x:",opNames)

	except getopt.GetoptError:
		# print help information and exit:
		print "Error in parseArgs(args, requireExplicit)"
		print args
		usage();
		sys.exit(2)

	#print "opts are "
	#print opts
	
	for o, a in opts:
	
		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		
		#Parameters relating to execution environment
		if (o == "-C" or o== "--java-class-path"):
			javaClassPath = a
			continue
		if (o == "-G" or o== "--gnu-plot-exe"):
			gnuPlotExe = a
			continue
		if (o == "-J" or o== "--java-exe"):
			javaExe = a
			continue
		if (o == "-L" or o== "--experimental-log-root"):
			experimentalLogRoot = a	
			continue
		if (o == "-S" or o== "--SNEEql-root-dir"):
			SNEEqlDir = a
			tossimNetworkFilePath = SNEEqlDir + inputExtension + networkFile +'.nss'
			avroraNetworkFilePath = SNEEqlDir + inputExtension + networkFile +'.top'
			schemaFilePath = SNEEqlDir + inputExtension + schemaFile
			#dataPath = SNEEqlDir + dataExtension
			continue
		if (o == "-T" or o == "--tortoise-svn-bin-dir"):
			tortoiseSVNbinDir = a
			continue
		if (o == '-A' or o == "--avrora-path"):
			avroraPath = a
			continue

		#Parameters relating to script functionality
		if (o == "-x"):
			if (a == 'all'):
				experimentList = ['1','2','3','4']
			else:
				experimentList = string.split(a,',')
			continue		
		if (o == "--ignore-led-energy"):
			ignoreLedEnergy = experimentLib.convertBoolean(o,a)
			continue
		if o == ("--nesc-synchronization-period"):
			nescSynchronizationPeriod = int(a)
			continue
		if o == ("--battery-energy"):
			batteryEnergd = int(a)
			continue
		if (o == "--check-query-result"):
			checkQueryResults = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--exitOnValidityError"):
			exitOnValidityError = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--show-title-in-graph"):
			showTitleInGraph = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--show-parameters-in-graph"):
			showParametersInGraph = experimentLib.convertBoolean(o,a)
			continue
			
		#Experiment parameters (these must all be present if doing post-processing only, i.e., requireExplict==true)
		#Parameters relating to all experiments
		if (o == "-n" or o== "--network-file"):
			networkFile = a;
			tossimNetworkFilePath = SNEEqlDir + inputExtension + networkFile+'.nss'
			avroraNetworkFilePath = SNEEqlDir + inputExtension + networkFile+'.top'
		elif (o == "-t" or o== "--schema-file"):
			schemaFile = a;
			schemaFilePath = SNEEqlDir + inputExtension + schemaFile
		#elif (o == "--data-directory="):
		#	dataDirectory = a
		#	dataPath = SNEEqlDir + dataExtension	
		elif (o == "-q" or o == "--queries" ):
			queries = string.split(a,',')
		elif (o == "-d" or o== "--simulation-duration"):
			simulationDuration = int(a)
			#print "SimulationDuration "+str(SimulationDuration)
		elif (o == "-s" or o== "--simulation-seed"):
			simulationSeed = int(a)
		elif (o == "--snooze"):
			snooze = experimentLib.convertBoolean(o,a)
		elif (o== "--use-lossy"):
			useLossy = experimentLib.convertBoolean(o,a)
		elif (o=="--deliver-last"):
			deliverLast = experimentLib.convertBoolean(o,a)
		elif (o=="--led-debug"):
			ledDebug = experimentLib.convertBoolean(o,a)

		#Parameters relating to specific experiments: defaults
		elif (o == "--x1a" or o== "--exp1-aq-rates"):
			exp1AqRates = map(int,string.split(a,','))
		elif (o == "--x1b" or o== "--exp1-max-buffering-factor"):
			exp1MaxBufferingFactor = int(a)
		elif (o == "--x2a" or o== "--exp2-aq-rate"):
			exp2AqRate = int(a)
		elif (o == "--x2b" or o== "--exp2-buffering-factors"):
			exp2BufferingFactors = map(int,string.split(a,','))
		elif (o == "--x3a" or o== "--exp3-aq-rate"):
			exp3AqRate = int(a)
		elif (o == "--x3b" or o== "--exp3-max-buffering-bactor"):
			exp3MaxBufferingFactor = int(a)
		elif (o == "--x3d" or o== "--exp3-delivery-times"):
			exp3DeliveryTimes = map(int,string.split(a,','))
		elif (o == "--x4a" or o== "--exp4-aq-rate"):
			exp4AqRate = int(a)
		elif (o == "--x4b" or o== "--exp4-buffering-factors"):	
			exp4BufferingFactors = int(a)

		elif (requireExplicit):
			print 'Parameter '+o+' missing in '+currentExperimentRoot+'parameters.txt'
			sys.exit(3)			

#Converts the parameters into a string to be sent to the screen, and paramter.txt file
def generateExperimentParameterStr():
	#Parameters relating to script functionality
	s = "-x=%s "
	s = s % (string.join(experimentList,','))
	s = s+ "--ignore-led-energy=%s "
	s = s % (str(ignoreLedEnergy))
	s = s+ "--battery-energy=%s --check-query-result=%s --exitOnValidityError=%s "
	s = s % (str(batteryEnergy), str(checkQueryResults), str(exitOnValidityError))
	s = s+ "--show-parameters-in-graph=%s --show-title-in-graph=%s"
	s = s % (str(showParametersInGraph), str(showTitleInGraph))
	
	#Parameters relating to all experiments
	s = s+ "--network-file=%s --schema-file=%s "# --data-directory=%s "
	s = s % (networkFile, schemaFile) #, dataDirectory)
	s = s+ "--queries=%s --snooze=%s "
	s = s % (string.join(queries,','), str(snooze))
	s = s+ "--simulation-duration=%s --simulation-seed=%s --nesc-synchronization-period=%s "
	s = s % (str(simulationDuration),  str(simulationSeed),str(nescSynchronizationPeriod))
	s = s+ "--use-lossy=%s "
	s = s % (str(useLossy))
	s = s+ "--deliver-last=%s --led-debug=%s "
	s = s % (str(deliverLast), str(ledDebug))

	#Parameters relating to specific experiments: defaults
	s = s+ "--exp1-aq-rates=%s --exp1-max-buffering-factor=%s "
	s = s % (string.join(map(str,exp1AqRates),','), str(exp1MaxBufferingFactor))
	s = s+ "--exp2-aq-rate=%s --exp2-buffering-factors=%s "
	s = s % (str(exp2AqRate), string.join(map(str,exp2BufferingFactors),','))
	s = s+ "--exp3-aq-rate=%s --exp3-max-buffering-bactor=%s --exp3-delivery-times=%s "
	s = s % (str(exp3AqRate), str(exp3MaxBufferingFactor), string.join(map(str,exp3DeliveryTimes),','))
	s = s+ "--exp4-aq-rate=%s --exp4-buffering-factors=%s "
	s = s % (str(exp4AqRate), str(exp4BufferingFactors))

	return s

#Records the parameters used to a file as well as logging them and showing thn on screen. 
def recordParameters(currentExperimentRoot, logger):
	
	paramStr = generateExperimentParameterStr()
	
	print ('\nParamters used for this experiment:\n')
	print str(currentExperimentRoot)
	print string.replace(paramStr, ' ', '\n')+'\n'
	
	logger.info('\nParamters used for this experiment:\n')
	logger.info(string.replace(paramStr, ' ', '\n')+'\n')

	#Parameters are written at the currentExperimentRoot level as they may be different for each experiment
	f = open(currentExperimentRoot+'parameters'+'.txt','w')
	f.write(string.replace(paramStr, ' ', '\n'))
	f.close()

def getVersionString():
	global SNEEqlDir, tortoiseSVNbinDir

	#to do: add -nm options here, which only allow code which is identical to the repository to run
	print ('running: '+tortoiseSVNbinDir+'SubWCRev '+SNEEqlDir+' > SubWCRevOutput.txt')
	exitVal = os.system(tortoiseSVNbinDir+'SubWCRev "'+SNEEqlDir+'" > SubWCRevOutput.txt') 

	if (exitVal!=0):
		print 'Source code not identical to repository version.  Try performing an update.'
		sys.exit(3)
	
	pattern = re.compile('Updated to revision (\d+)')
	for line in fileinput.input(['SubWCRevOutput.txt']):
		match = pattern.match(line)
		if match:
			svnVersion = 'V'+match.group(1)

	os.remove('SubWCRevOutput.txt')
	print 'Source code version: '+svnVersion
	return svnVersion

	if (svnVersion.find('undef') >-1):
		for line in fileinput.input(['SubWCRevOutput.txt']):
			print line
			if (line.find('Mixed revision') > -1):
				print 'Source code not identical to repository version.  Try performing an update.'
				sys.exit(3)
		print 'Source code identical failed.'
		sys.exit(3)
		
#Returns a timestamp with the current time
def getTimeStamp():
	timeStamp = time.strftime("%Y-%m-%d_%H-%M-%S")
	print 'Experimental run timestamp: '+timeStamp
	
	return timeStamp;

def setupGlobals(args):
	global versionString, timeStamp, numNodes
	
	parseArgs(sys.argv[1:],False) 

	#initialize stuff
	versionString = getVersionString()
	timeStamp = getTimeStamp()


#Testing method
def main():
	usage()
	#parse the command-line arguments
	setupGlobals(sys.argv[1:]) 
	print generateExperimentParameterStr()
	
if __name__ == "__main__":

    main()
	
