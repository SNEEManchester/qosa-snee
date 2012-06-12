import getopt, os, sys, logging, re, fileinput, time, string, experimentLib, GraphData, counter
 
import xml.dom.minidom
from xml.dom.minidom import Node

#Parameters relating to execution environment
optJavaClassPath = '.;C:\\/dias-mc\\work2\\SNEEql\\lib\\antlr-2.7.5.jar;C:\\/dias-mc\\work2\\SNEEql\\bin'
optGnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'
optJava5exe = "/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java"
optExperimentalLogRoot = 'c:/dias-mc/icde/experiments'
optSNEEqlRootDir = 'c:/dias-mc/work2/SNEEql/'
optSNEEqlSourceDir = optSNEEqlRootDir + 'src/'
optSNEEqlClassDir = optSNEEqlRootDir + 'bin/'
optTortoiseSVNbinDir = '/cygdrive/c/Program\ Files/TortoiseSVN/bin/'

#Parameters relating to script functionality
optExperimentList = ['1','2','3','4'] #assume all experiments are to be run
	#optPostProcessOnly allowed changes to the post processing without rerunning the experiments
	#WARNING methods invoked by setting to true are out of date
optPostProcessOnly = False
	#optUsePowerFix Specifies if postprocesser should add power bedug lines.
	#These get around PowerTossims assumption that the simulation is from the first to the last debug
optUsePowerFix = True
	#optIgnoreLedPower Prevents PowerTossim from counting the LED's
	#Only effects TinyDB
optIgnoreLedPower = True
	#optUseSensorBoard Instructs PowerTossim to include power for a sensor board.
	#PowerTossim assumes the sensor board is always on. Even if the cpu is not.
optUseSensorBoard = True 
	#optIgnoreStartupTime Excludes the syncronisation time from the experiment
	#Simulation duration is extended to compensate.
	#Tossim output is postprocessed to adjust it accordingly.
	#SEE: nescSynchronizationPeriod
	#NOTE: Ignored in experiment 4 where it is always false
optIgnoreStartupTime = False
	#mAH(2900) * columb/mAH(3.6) * Volatage(1.5) * batteries(2) = Joules
optBatteryEnergy = 31320
	#optCheckQueryResults Request the post processor to check the reults.
optCheckQueryResults = False
#optExitOnValidityError If set causes the script to exit if an error is found
	#Even if not` set an error messages is still logged
	#NOTE: No error was logged during the publish experiments.
optExitOnValidityError = False
	#optShowParametersInGraph Instructs the script to place more details in the graphs.
	#NOTE: The published layout of the graphs was done outside of these scripts.
optShowParametersInGraph = True

#Parameters relating to all experiments
	#optNetworkFile Specifies the file containing the topology
	#File must be in the Tossim accepted format, otherwise it is ignored by Tossim
optNetworkFile = 'input/mini_network_topology.nss'
	#derived
networkFilePath = optSNEEqlRootDir + optNetworkFile 
	#The file that describes the schema used.
	#No error is caused if the file contains unused schemas
	#TODO Fix the file to reflect that in the paper
optSchemaFile = 'input/mini_network_schemas.xml'
	#derived
schemaFilePath = optSNEEqlRootDir + optSchemaFile
	#The files containg the queries used in the experiments
	#TODO fix text as in experiment
	#TODO fix grammer to match text in queries
	#TODO consider allow sub folders in queries
optQueries = ['Q1.txt'] 
optQueryDir = ''
	#optCountCpuCycles instructs the script to use PowerTossims cpu counting option
	#Was not used as we could not validate the results produced by PowerTossim
	#NOTE This will not work optUseCC1000Radio = True
	#NOTE Use either this or optCPUGoActive = True to avoid double counting
optCountCpuCycles = False
	#optSnooze Inserts code to turn the cpu on and off
	#NOTE: If used with optUseCC1000Radio = False we use an adapted version of snooze
	#NOTE: Ignored in experiment 4 where it is always false
optSnooze = False
	#optSimulationDuration dettermines how long the experiment should run
	#NOTE: This is in second where all other times are in milliseconds
	#NOTE: Requires optControlRadioOff = True
	#SEE: optIgnoreStartupTime
optSimulationDuration = 600
	#optSimulationSeed Set the Random seed to be used by Tossim
	#NOTE: This does not effect the Random seed of the Java query compiler
optSimulationSeed = 3
	#nescSynchronizationPeriod dettermines how long the sychnronization loop runs before the query starts
nescSynchronizationPeriod = 3000
	#optUseCC1000Radio dettermines if the CC1000 radio should be used.
	#NOTE: We used it enough to validate that optControlRadioOff = True worked correctly
	#NOTE: We stopped using it because it frequently caused TOSSIM to hang.
optUseCC1000Radio = True
	#optControlRadioOff dettermines if the radio should be turned off when not used.
	#NOTE: If used with optUseCC1000Radio = False the post processor will scrub out the phanton radio messages
		#These are messages overheard when the radio was set to off.
optControlRadioOff = False
	#optUseLossy Instructs Tossim to use the network topology as a lossy file
	#The file need to be in unix format or tossim will ignore it wothout throwing an error
optUseLossy = True
	#optCPUGoActive Instructs the NesC generator to add power debug statements
	#These help Power Tossim know when the CPU could be actively executing code
	#NOTE: Ignored in experiment 4 where it is always false
optCPUGoActive = True
	#optDeliverLast When set to true will cause the last loop of the agenda to be adpated
	#This is insure all buffered evaluations are completed.
optDeliverLast = False


#Parameters relating to specific experiments: defaults
#Only effect the experiment in the name
	#exp1AqRates The x axis
	#The various acquisition rates used
exp1AqRates = [1000,2000,3000,4000,5000,6000,10000,12000,15000,20000,30000,60000]
	#exp1MaxBufferingFactor Optional way of limiting buffering factor
	#Best results are obtained by setting it to max
exp1MaxBufferingFactor = 1 #sys.maxint
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
	#exp3MaxBufferingFactor Optional way of limiting buffering factor
	#Best results are obtained by setting it to max
exp4MaxBufferingFactor = sys.maxint
	#exp4TinyDBQueries Mapping of our query file names to TinyDB queries
	#TODO change query names
exp4TinyDBQueries = {'Q1.txt': 'select light from sensors sample period %s', 'Q2.txt':'select avg(temp) from sensors where temp > 500 sample period %s'} #


#Global variables automatcally set up the system
	#File to which the log information will be written
	#There will be one in the directory for each experiment
logger = logging.getLogger('experiment')
	#Version control update number
	#obtained automatically by the script
	#NOTE: Unless run with a version obtain from our SVN server it will always be undefined
		#There are no plans to make the svn server publicly accessable at present.
svnVersion = 'undefined'
	#Time the script started running
	#obtained automatically by the script
timeStamp = 'undefined'
	#Additional parameter for the Java complier
	#-nesc-generate-mote-code Specifies if code for each mote should be generated
	#TODO comment to what level this works
	#-display-graphs specifies it the java should popup every graph it generates
queryCompilerDefaultParams = ['-nesc-generate-mote-code=True','-display-graphs=False']
	#Number of nodes in the topology
	#obtained automatically by the script
numNodes = -1 

#Help text which returned if incorrect paramteres provided
def usage():
	print 'Usage: python runExperiments.py [-options]s'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\n\tdisplay this message'
	
	#Parameters relating to execution environment
	print '-C<dir>, --java-class-path=<dir> \n\tdefault: '+optJavaClassPath
	print '-G<file>, --gnuplot-exe=<file> \n\tdefault: '+optGnuPlotExe
	print '-J<file>, --java-5-exe=<file> \n\tdefault: '+optJava5exe
	print '-L<dir>, --experimental-log-root=<dir> \n\tdefault: '+optExperimentalLogRoot
	print '-S<dir>, --SNEEql-root-dir=<dir> \n\tdefault: '+optSNEEqlRootDir
	print '-T<dir>, --tortoise-svn-bin-dir=<dir> \n\tdefault: '+optTortoiseSVNbinDir

	#Parameters relating to script functionality
	print '-x[1|2|3|4|1,..,4|all] \n\tthe experiments to be run.\n\tdefault: '+string.join(optExperimentList,',')
	print '-p, --post-process-only \n\tdefault: False'
	print '--use-power-fix=[True|False]\n\tdefault:  '+str(optUsePowerFix)
	print '--ignore-led-power=[True|False]\n\tdefault:  '+str(optIgnoreLedPower)
	print '--use-sensor-board=[True|False]\n\tdefault:  '+str(optUseSensorBoard)
	print '--ignore-startup-time=[True|False]\n\tdefault:  '+str(optIgnoreStartupTime)
	print '--battery-energy=<val> \n\tdefault: '+str(optUseCC1000Radio)
	print '--check-query-result=[True|False] \n\tdefault: '+str(optCheckQueryResults)
	print '--exitOnValidityError=[True|False] \n\tdefault: '+str(optExitOnValidityError)
	print '--show-parameters-in-graph==[True|False] \n\tdefault: '+str(optShowParametersInGraph)
	
	#Parameters relating to all experiments
	print '-n<file>, --network-file=<file> \n\tdefault: '+optNetworkFile
	print '-t<file>, --schema-file=<file> \n\tdefault: '+ optSchemaFile
	print '-q<val,..val>, --queries=<val,..val> \n\tdefault: '+string.join(optQueries,',') 
	print '-c[True|False], --count-cpu-cycles=[True|False] \n\tdefault: '+str(optCountCpuCycles)
	print '-d<val>, --simulation-duration=<val> \n\tdefault: '+str(optSimulationDuration)
	print '-s<val>, --simulation-seed=<val> \n\tdefault: '+ str(optSimulationSeed)
	print '--snooze=[True|False] \n\t default: '+ str(optSnooze)
	print '--nesc-synchronization-period=<val>\n\tdefault:  '+str(nescSynchronizationPeriod)
	print '--use-cc1000-radio=[True|False] \n\tdefault: '+str(optUseCC1000Radio)
	print '--control-radio-off=[True|False] \n\tdefault: '+str(optControlRadioOff)
	print '--use-lossy=[True|False] \n\tdefault: '+str(optUseLossy)
	print '--cpu-go-active=[True|False] \n\tdefault: '+str(optCPUGoActive)
	print '--deliver-last=[True|False] \n\tdefault: '+str(optDeliverLast)

	#Parameters relating to specific experiments: defaults
	print '--x1a=<val,..val>, --exp1-aq-rates=<val,..val> \n\tdefault: '+string.join(map(str,exp1AqRates),',') 
	print '--x1b=<val>, --exp1-max-buffering-factor=<val> \n\tdefault:  '+str(exp1MaxBufferingFactor)
	print '--x2a=<val>, --exp2-aq-rate=<val> \n\tdefault:  '+str(exp2AqRate)
	print '--x2b=<val,..val>, --exp2-buffering-factors=<val,..val> \n\tdefault:  '+string.join(map(str,exp2BufferingFactors),',')
	print '--x3a=<val>, --exp3-aq-rate=<val> \n\tdefault:  '+str(exp3AqRate)
	print '--x3b=<val>, --exp3-max-buffering-bactor=<val> \n\tdefault:  '+str(exp3MaxBufferingFactor)
	print '--x3d=<val,..val>, --exp3-delivery-times=<val,..val> \n\tdefault:  '+string.join(map(str,exp3DeliveryTimes),',')
	print '--x4a=<val>, --exp4-aq-rate=<val>\n\tdefault:  '+str(exp4AqRate)
	print '--x4b=<val>, --exp4-max-buffering-factor=<val> \n\tdefault:  '+str(exp4MaxBufferingFactor)

#Use the provided parameters to reset any of the options.
#If parameters is not provided default is used.
def parseArgs(args, requireExplicit):
	#WARNING any new paramters added must be added to this line
	#Parameters relating to execution environment
	global optJavaClassPath, optGnuPlotExe, optJava5exe, optExperimentalLogRoot
	global optSNEEqlRootDir, optSNEEqlSourceDir, optSNEEqlClassDir, optTortoiseSVNbinDir
	
	#Parameters relating to script functionality
	global optExperimentList, optPostProcessOnly, optUsePowerFix
	global optIgnoreLedPower, optUseSensorBoard, optIgnoreStartupTime
	global optBatteryEnergy, optCheckQueryResults, optExitOnValidityError	
	global optShowParametersInGraph 	

	#Parameters relating to all experiments
	global optNetworkFile, networkFilePath, optSchemaFile, schemaFilePath
	global optQueries, optCountCpuCycles, optSnooze
	global optSimulationDuration, optSimulationSeed, nescSynchronizationPeriod
	global optUseCC1000Radio, optControlRadioOff, optUseLossy
	global optCPUGoActive, optDeliverLast
	
	#Parameters relating to specific experiments: defaults
	global exp1AqRates, exp1MaxBufferingFactor
	global exp2AqRate, exp2BufferingFactors
	global exp3MaxBufferingFactor, exp3AqRate, exp3DeliveryTimes
	global exp4AqRate, exp4MaxBufferingFactor

	try:
		#WARNING any new flags added must be added to this line.
		opNames = ["help"]

		#Parameters relating to execution environment
		opNames = opNames + ["java-class-path=", "gnu-plot-exe=","java-5-exe=","experimental-log-root="]
		opNames = opNames + ["SNEEql-root-dir=","tortoise-svn-bin-dir="]

		#Parameters relating to script functionality		
		opNames = opNames + ["post-process-only","use-power-fix="]
		opNames = opNames + ["ignore-led-power=","use-sensor-board=","ignore-startup-time="]
		opNames = opNames + ["battery-energy=","check-query-result=","exitOnValidityError="]
		opNames = opNames + ["show-parameters-in-graph="]

		#Parameters relating to all experiments
		opNames = opNames + ["network-file=","schema-file="]
		opNames = opNames + ["queries=","count-cpu-cycles=","snooze="]
		opNames = opNames + ["simulation-duration=","simulation-seed=","nesc-synchronization-period="]
		opNames = opNames + ["use-cc1000-radio=","control-radio-off=","use-lossy="]
		opNames = opNames + ["cpu-go-active=","deliver-last"]

		#Parameters relating to specific experiments: defaults
		opNames = opNames + ["x1a=","exp1-aq-rates=","x1b=","exp1-max-buffering-factor="]
		opNames = opNames + ["x2a=","exp2-buffering-factors=","x2b=","exp2-aq-rate="]
		opNames = opNames + ["x3a=","exp3-aq-rate=","x3b=","exp3-max-buffering-bactor=","x3d=","exp3-delivery-times="]
		opNames = opNames + ["x4a=","exp4-aq-rate=","x4b=","exp4-max-buffering-factor="]
		opts, args = getopt.getopt(args, "C:G:J:L:S:T:c:d:hn:pq:s:t:x:",opNames)

	except getopt.GetoptError:
		# print help information and exit:
		print getopt.GetoptError
		print 'hello'
		usage();
		sys.exit(2)

	for o, a in opts:
	
		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		
		#Parameters relating to execution environment
		if (o == "-C" or o== "--java-class-path"):
			optJavaClassPath = a
			continue
		if (o == "-G" or o== "--gnu-plot-exe"):
			optGnuPlotExe = a
			continue
		if (o == "-J" or o== "--java-5-exe"):
			optJava5exe = a
			continue
		if (o == "-L" or o== "--experimental-log-dir"):
			optExperimentalLogRoot = a	
			continue
		if (o == "-S" or o== "--SNEEql-root-dir"):
			optSNEEqlRootDir = a
			optSNEEqlSourceDir = optSNEEqlRootDir + 'src/'
			optSNEEqlClassDir = optSNEEqlRootDir + 'bin/'
			networkFilePath = optSNEEqlRootDir + optNetworkFile
			schemaFilePath = optSNEEqlRootDir + optSchemaFile
			continue
		if (o == "-T" or o== "--tortoise-svn-bin-dir"):
			optTortoiseSVNbinDir = a
			continue

		#Parameters relating to script functionality
		if (o == "-x"):
			if (a == 'all'):
				optExperimentList = ['1','2','3','4']
			else:
				optExperimentList = string.split(a,',')
			continue		
		if (o == "-p" or o== "--opt-post-process-only"):			
			optPostProcessOnly = True
			continue
		if (o == "--use-power-fix"):
			optUsePowerFix = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--ignore-led-power"):
			optIgnoreLedPower = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--use-sensor-board"):
			optUseSensorBoard = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--ignore-startup-time"):
			optIgnoreStartupTime = experimentLib.convertBoolean(o,a)
			continue
		if o == ("--nesc-synchronization-period"):
			nescSynchronizationPeriod = int(a)
			continue
		if o == ("--battery-energy"):
			optBatteryEnergd = int(a)
			continue
		if (o == "--check-query-result"):
			optCheckQueryResults = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--exitOnValidityError"):
			optExitOnValidityError = experimentLib.convertBoolean(o,a)
			continue
		if (o == "--show-parameters-in-graph"):
			optShowParametersInGraph = experimentLib.convertBoolean(o,a)
			continue
			
		#Experiment parameters (these must all be present if doing post-processing only, i.e., requireExplict==true)
		#Parameters relating to all experiments
		if (o == "-n" or o== "--network-file"):
			optNetworkFile = a;
			networkFilePath = optSNEEqlRootDir + optNetworkFile
		elif (o == "-t" or o== "--schema-file"):
			optSchemaFile = a;
			schemaFilePath = optSNEEqlRootDir + optSchemaFile
		elif (o == "-q" or o == "--queries" ):
			optQueries = string.split(a,',')
		elif (o == "-d" or o== "--simulation-duration"):
			optSimulationDuration = int(a)
		elif (o == "-s" or o== "--simulation-seed"):
			optSimulationSeed = int(a)
		elif (o == "--use-cc1000-radio"):
			optUseCC1000Radio = experimentLib.convertBoolean(o,a)
		elif (o == "--control-radio-off"):
			optControlRadioOff = experimentLib.convertBoolean(o,a)
		elif (o == "-c" or o== "--count-cpu-cycles"):
			optCountCpuCycles = experimentLib.convertBoolean(o,a)
		elif (o == "--snooze"):
			optSnooze = experimentLib.convertBoolean(o,a)
		elif (o== "--use-lossy"):
			optUseLossy = experimentLib.convertBoolean(o,a)
		elif (o== "--cpu-go-active"):
			optCPUGoActive = experimentLib.convertBoolean(o,a)
		elif (o=="--deliver-last"):
			optDeliverLast = experimentLib.convertBoolean(o,a)

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
		elif (o == "--x4b" or o== "--exp4-max-buffering-factor"):	
			exp4MaxBufferingFactor = int(a)
		elif (requireExplicit):
			print 'Parameter '+o+' missing in '+currentExperimentRoot+'parameters.txt'
			logger.critical('Parameter '+o+' missing in '+currentExperimentRoot+'parameters.txt')
			sys.exit(3)			

#Converts the parameters into a string to be sent to the screen, logger and paramter.txt file
def generateExperimentParameterStr():
	#Parameters relating to script functionality
	s = "-x=%s --use-power-fix=%s "
	s = s % (string.join(optExperimentList,','),str(optUsePowerFix))
	s = s+ "--ignore-led-power=%s --use-sensor-board=%s --ignore-startup-time=%s "
	s = s % (str(optIgnoreLedPower), str(optUseSensorBoard),  str(optIgnoreStartupTime))
	s = s+ "--battery-energy=%s --check-query-result=%s --exitOnValidityError=%s "
	s = s % (str(optBatteryEnergy), str(optCheckQueryResults), str(optExitOnValidityError))
	s = s+ "--show-parameters-in-graph=%s "
	s = s % (str(optShowParametersInGraph))
	
	#Parameters relating to all experiments
	s = s+ "--network-file=%s --schema-file=%s "
	s = s % (optNetworkFile, optSchemaFile,)
	s = s+ "--queries=%s --count-cpu-cycles=%s --snooze=%s "
	s = s % (string.join(optQueries,','), str(optCountCpuCycles), str(optSnooze))
	s = s+ "--simulation-duration=%s --simulation-seed=%s --nesc-synchronization-period=%s "
	s = s % (str(optSimulationDuration),  str(optSimulationSeed),str(nescSynchronizationPeriod))
	s = s+ "--use-cc1000-radio=%s --control-radio-off=%s --use-lossy=%s "
	s = s % (str(optUseCC1000Radio), str(optControlRadioOff), str(optUseLossy))
	s = s+ "--cpu-go-active=%s --deliver-last=%s "
	s = s % (str(optCPUGoActive), str(optDeliverLast))

	#Parameters relating to specific experiments: defaults
	s = s+ "--exp1-aq-rates=%s --exp1-max-buffering-factor=%s "
	s = s % (string.join(map(str,exp1AqRates),','), str(exp1MaxBufferingFactor))
	s = s+ "--exp2-aq-rate=%s --exp2-buffering-factors=%s "
	s = s % (str(exp2AqRate), string.join(map(str,exp2BufferingFactors),','))
	s = s+ "--exp3-aq-rate=%s --exp3-max-buffering-bactor=%s --exp3-delivery-times=%s "
	s = s % (str(exp3AqRate), str(exp3MaxBufferingFactor), string.join(map(str,exp3DeliveryTimes),','))
	s = s+ "--exp4-aq-rate=%s --exp4-max-buffering-factor=%s "
	s = s % (str(exp4AqRate), str(exp4MaxBufferingFactor))
	return s

#Records the parameters used to a file as well as logging them and showing thn on screen. 
def recordParameters(currentExperimentRoot, experiment):
	
	paramStr = generateExperimentParameterStr()
	
	print ('\nParamters used for this experiment:\n')
	print str(currentExperimentRoot)
	print string.replace(paramStr, ' ', '\n')+'\n'
	
	logger.info('\nParamters used for this experiment:\n')
	logger.info(string.replace(paramStr, ' ', '\n')+'\n')

	#Parameters are written at the currentExperimentRoot level as they may be different for each experiment
	f = open(currentExperimentRoot+'parameters'+str(experiment)+'.txt','w')
	f.write(string.replace(paramStr, ' ', '\n'))
	f.close()


#Designed to read paramets back in for reprocessing experiment results
#WARNING May be out of date
def readParameters(currentExperimentRoot):

	paramStr = ""

	#read the parameters.txt file 
	for line in fileinput.input([currentExperimentRoot+'parameters.txt']):
		paramStr += line
		
	parseArgs(string.split(paramStr),True)
	paramStr  = generateExperimentParameterStr()
	
	print 'generated'
	
	print 'Read parameters from previous run'
	print string.replace(paramStr, ' ', '\n')+'\n'
	logger.info('Read parameters from previous run\n')
	logger.info(string.replace(paramStr, ' ', '\n')+'\n')

#Creates a new log file for each run of the experiments
def startLogger(currentExperimentRoot):
	#create the directory if required
	if not os.path.isdir(currentExperimentRoot):
			os.makedirs(currentExperimentRoot)
			
	hdlr = logging.FileHandler(currentExperimentRoot+'/experiment.log')
	formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
	hdlr.setFormatter(formatter)	
	logger.addHandler(hdlr) 
	logger.setLevel(logging.INFO)
	logger.info('Starting experiment')
	print 'Logger Started'

#queryInstanceRoot is the directory that hold a data for a single point on the graph
#queryPlanDir is sub directory that hold all the informational java output
#nescDir is the sub directory that hold all the nesc code including the compiled version
#PowerTossimDir is the sub directory that hold all the output of tossim, PowerTossim and postprocessor output
#desc is a textual representation of the query and the x axis
def getExpDescAndDirs (currentExperimentRoot, query, prefix, number):
	queryInstanceRoot = currentExperimentRoot+string.rstrip(query,'.txt')+'_'+prefix+str(number)+'/'
	queryPlanDir = queryInstanceRoot+'query-plan/'
	nescDir = queryInstanceRoot+'nesc/'
	powerTossimDir = queryInstanceRoot+'powerTossim/'
	
	#print '"'+aqRate + '"/"' + query + '"'
	desc = 'query='+query+', '+prefix+'='+str(number)
	#print 'desc="'+desc+'"'
	return queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc


#Looks up the number of sources for a particular extent for the given schema file	
def countNumSources(extentName):

	global schemaFilePath
	print schemaFilePath
	print extentName

	doc = xml.dom.minidom.parse(schemaFilePath)
	for s in doc.getElementsByTagName("stream"):
		if (s.getAttribute("name")==extentName):
			sources =  s.getElementsByTagName("nodes")[0].childNodes[0].data
	return len(string.split(sources,','))


#sets up the various graphs plotted for experiments 1,3 and 4
#SEE:addtoPowerGraphs(..)
#NOTE: Layout of graphs used in paper is different to this.
def setupPowerGraphs(xlabelText, xRange, lineText, extraText):

	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph

	#graph variables
	xAxisPoints = xRange
	lineLabels = lineText
	xLabel = xlabelText
	
	#Add extra lines to the title. 
	if optShowParametersInGraph:
		titleExtraLines = '\\n'+str(numNodes)+ ' Sites with duration of '+str(optSimulationDuration)+' seconds'
		if optIgnoreStartupTime:
			titleExtraLines = titleExtraLines + ' after syncronization'
		else:
			titleExtraLines = titleExtraLines + ' including syncronization of' + str(nescSynchronizationPeriod/ 1000) + 'seconds'
		if optControlRadioOff:
			if optUseCC1000Radio:
				titleExtraLines = titleExtraLines + ' with CC1000 radio.'
			else:
				titleExtraLines = titleExtraLines + ' radio on/ off without CC1000 radio'
		else:
			titleExtraLines = titleExtraLines + ' without radio on/ off'
			
		titleExtraLines = titleExtraLines + '\\n'
		if optUsePowerFix:
			titleExtraLines = titleExtraLines + 'Time fix applied. '
		else:
			titleExtraLines = titleExtraLines + 'No Time fix applied. '
		if optIgnoreLedPower:
			titleExtraLines = titleExtraLines + 'With LED power. ' 
		else:
			titleExtraLines = titleExtraLines + 'NO LED power. '
		if optUseSensorBoard:
			titleExtraLines = titleExtraLines + 'With Sensor Board power. ' 
		else:
			titleExtraLines = titleExtraLines + 'No Sensor Board power. '
		titleExtraLines += extraText	
	else:
		titleExtraLines = ''

	#graph to output total energy consumed by network
	totalGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	totalGraph.yLabel = 'Network Total energy consumption (mJ)'
	totalGraph.title = xlabelText + ' vs. Network Total Energy Consumption'+titleExtraLines

	#graph to output average energy consumed by network
	averageGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	averageGraph.yLabel = 'Average node energy consumption (mJ)'
	averageGraph.title = xlabelText + ' vs. Average Node Energy Consumption'+titleExtraLines
	
	#graph to output max energy consumed by any node network
	maxGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	maxGraph.yLabel = 'Maximum node energy consumption (mJ)'
	maxGraph.title = xlabelText + ' vs. Maximum Node Energy Consumption'+titleExtraLines

	#graph to output lifetime of all nodes
	lifetimeGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	lifetimeGraph.yLabel = 'Lifetime of all nodes in days'
	lifetimeGraph.title = xlabelText + ' vs. Lifetime with battery of '+str(optBatteryEnergy)+' Joules'+titleExtraLines

	#graph to output total energy consumed by radio
	radioGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	radioGraph.yLabel = 'Radio Total energy consumption (mJ)'
	radioGraph.title = xlabelText + ' vs. Radio Total Energy Consumption'+titleExtraLines

	#graph to output total energy consumed by cpu_cycle
	cpu_cycleGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	cpu_cycleGraph.yLabel = 'cpu_cycle Total energy consumption (mJ)'
	cpu_cycleGraph.title = xlabelText + ' vs. cpu_cycle Total Energy Consumption'+titleExtraLines

	#graph to output total energy consumed by radio
	txGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	txGraph.yLabel = 'Number of Radio Tx debugs'
	txGraph.title = xlabelText + ' vs. Number of Radio Tx debugs'+titleExtraLines


#Converts the output of powerTossim into values to become points on the graphs
#totalGraph The sum of the total joules used in each site
#averageGraph The average of the joules used in eahc site
#maxGraph The total joules used by the site with the highest total 
#lifetimeGraph Lifetime of the network calculated at the battery/ max from above
#radioGraph The sum of the radio joules for each site
#cpu_cycleGraph The sum of the cpu_cycles joules for each site. SEE: optCountCpuCycles
#txGraph: The sum of the Power tx debug lines found on all sites
def addtoPowerGraphs(yValue,xValue,inputfile, txCount):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph

	print'************ Adding '+str(xValue)
	
	#compute the total power used
	totalPower, maxPower, averagePower, radioPower, cpu_cyclePower = experimentLib.computeEnergyConsumed(inputfile,optIgnoreLedPower)
	#print 'totalPower =' +str(totalPower)
	#print 'maxPower =' +str(maxPower)
	#print 'averagePower =' +str(averagePower)
			
	#add point to the graphs
	totalGraph.addPoint(yValue,xValue,str(totalPower))
	maxGraph.addPoint(yValue,xValue,str(maxPower))
	averageGraph.addPoint(yValue, xValue, str(averagePower))
	radioGraph.addPoint(yValue,xValue,str(radioPower))
	cpu_cycleGraph.addPoint(yValue,xValue,str(cpu_cyclePower))
		
	secondLifetime = optBatteryEnergy / (maxPower/(1000*optSimulationDuration)) 
	dayLifetime = secondLifetime / (24*60*60)
	lifetimeGraph.addPoint(yValue, xValue, str(dayLifetime))
	txGraph.addPoint(yValue, xValue, txCount)

#Generates txt files used to plot graphs - Including versions used in Paper
#Generates gif and eps version of the graphs 
#SEE:addtoPowerGraphs(..)
def generatePowerGraphs(graphType, experiment,numericXValue):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph

	exText = 'Exp'+str(experiment)
	#generate the graphs
	totalGraph.generatePlotFile(currentExperimentRoot+exText+'totalEnergy.txt',numericXValue, logger)
	totalGraph.plotGraph(currentExperimentRoot+exText+'totalEnergy',graphType,optGnuPlotExe, numericXValue, 'top right', logger)
	maxGraph.generatePlotFile(currentExperimentRoot+exText+'maxEnergy.txt',numericXValue, logger)
	maxGraph.plotGraph(currentExperimentRoot+exText+'maxEnergy',graphType,optGnuPlotExe, numericXValue, 'top right',logger)
	averageGraph.generatePlotFile(currentExperimentRoot+exText+'averageEnergy.txt',numericXValue, logger)
	averageGraph.plotGraph(currentExperimentRoot+exText+'averageEnergy',graphType,optGnuPlotExe, numericXValue, 'top right', logger)
	lifetimeGraph.generatePlotFile(currentExperimentRoot+exText+'lifetime.txt',numericXValue, logger)
	lifetimeGraph.plotGraph(currentExperimentRoot+exText+'lifetime',graphType,optGnuPlotExe, numericXValue, 'top left', logger)
	radioGraph.generatePlotFile(currentExperimentRoot+exText+'radioEnergy.txt',numericXValue, logger)
	radioGraph.plotGraph(currentExperimentRoot+exText+'radioEnergy',graphType,optGnuPlotExe, numericXValue, 'top right', logger)
	cpu_cycleGraph.generatePlotFile(currentExperimentRoot+exText+'cpu_cycleEnergy.txt', numericXValue, logger)
	cpu_cycleGraph.plotGraph(currentExperimentRoot+exText+'cpu_cycleEnergy',graphType,optGnuPlotExe, numericXValue, 'top right', logger)
	txGraph.generatePlotFile(currentExperimentRoot+exText+'tx_count.txt',numericXValue, logger)
	txGraph.plotGraph(currentExperimentRoot+exText+'tc_count',graphType,optGnuPlotExe, numericXValue, 'top right', logger)

#Converts all the script parameters into Java parmeters
def generateQueryCompilerParams(query, queryInstanceRoot, aqRate, maxBufferingFactor, maxDeliveryTime, useCC1000Radio, controlRadioOff, simulationDuration, doSnooze):
	#Make sure at least some data is delivered
	if optIgnoreStartupTime:
		runTime = simulationDuration * 1000
	else:
		runTime = simulationDuration * 1000 - nescSynchronizationPeriod
	deliveryTime = min (runTime, maxDeliveryTime) 
	
	queryCompilerParams = ['-query='+query]
	queryCompilerParams+= ['-output-root-dir="'+queryInstanceRoot+'"']
	queryCompilerParams+= ['-qos-acquisition-rate='+str(aqRate)]
	queryCompilerParams+= ['-qos-max-buffering-factor='+str(maxBufferingFactor)]				
	queryCompilerParams+= ['-qos-response-time='+str(deliveryTime)]
	queryCompilerParams+= ['-qos-query-duration='+str(runTime)]
	queryCompilerParams+= ['-network-topology-file="'+networkFilePath+'"']
	queryCompilerParams+= ['-schema-file="'+schemaFilePath+'"']
	queryCompilerParams+= ['-nesc-use-cc1000-radio='+str(useCC1000Radio)]
	queryCompilerParams+= ['-nesc-control-radio-off='+str(controlRadioOff)]				
	queryCompilerParams+= ['-nesc-synchronization-period='+str(nescSynchronizationPeriod)]
	
	if (doSnooze):
		queryCompilerParams+= ['-nesc-do-snooze=true']
	else:
		queryCompilerParams+= ['-nesc-do-snooze=false']
	if (optCPUGoActive):
		queryCompilerParams+= ['-nesc-cpu-go-active=true']
	else:
		queryCompilerParams+= ['-nesc-cpu-go-active=false']
	
	if (optDeliverLast):
		queryCompilerParams+= ['-nesc-deliver-last=true']
	else:
		queryCompilerParams+= ['-nesc-deliver-last=false']
	
	return queryCompilerParams  
	
#Converts the Script parameters into Tossim Parameters
#adds -p Tossim parameter to get the power debug lines PowerTossim needs
#adds -b=1 Which sets the motes to start up within one second of each other. Lowest possible value
def generateTossimParameters(extra, numNodes, file):	
	if (optSimulationSeed == None):
		tossimParams = []
	else:
		tossimParams = ['-seed='+str(optSimulationSeed)] 
	if optIgnoreStartupTime:
		tossimParams.extend(['-t='+str(optSimulationDuration+nescSynchronizationPeriod/1000+3)])
	else:	
		tossimParams.extend(['-t='+str(optSimulationDuration)])
	if optUseLossy:
		tossimParams.extend(['-r=lossy','-rf='+networkFilePath])
	tossimParams.extend(['-p','-b=1'])
	if len(extra) > 0:
		tossimParams.extend(extra)
	tossimParams.extend([str(numNodes),' > '+file])
	return tossimParams

#Runs the full experiment 1
#Sets up the logger and graphs
#Loops through all queries for all acquitsion rates generaating data
#Generates the result txt files and graphs
def doExperiment1():

	#set up the logger
	startLogger(currentExperimentRoot)

	if (optPostProcessOnly):
		#WARNING May be out of date
		print ("reprocessing data from "+currentExperimentRoot)
		readParameters(currentExperimentRoot)
		logger.info ("reprocessing data from "+currentExperimentRoot)
		
	else:
		if not os.path.isdir(currentExperimentRoot):
			os.makedirs(currentExperimentRoot)
		recordParameters(currentExperimentRoot,1)

	setupPowerGraphs('Acquisition interval (ms)',exp1AqRates,optQueries,'Max buffering factor: '+str(exp1MaxBufferingFactor))

	for aqRate in exp1AqRates:
		
		for query in optQueries:	
			queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs(currentExperimentRoot, query, 'aqRate', aqRate)

			if (not optPostProcessOnly):
				#run the query compiler
				
				queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, aqRate, exp1MaxBufferingFactor, sys.maxint,optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)

				exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
				if (exitVal != 0):
					continue

				#compile the nesC code
				os.makedirs(powerTossimDir)
				experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

				experimentLib.runAvrora(nescDir,optSimulationDuration,logger)

#				#run tossim simulation
#				tossimParams  = generateTossimParameters([], numNodes, powerTossimDir+'tossim.trace')
#				experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)
#		
#			if (os.path.exists(powerTossimDir+'tossim.trace')):
#			
#				#post-process tossim script				
#				#checkResults(query,powerTossimDir,countNumSources('InFlow'),optCheckQueryResults, (optControlRadioOff and not optUseCC1000Radio), optSnooze, optExitOnValidityError, logger)
#				txCount = experimentLib.processTossimTrace(optUsePowerFix, optIgnoreStartupTime, optControlRadioOff, powerTossimDir, numNodes, optSimulationDuration, logger)
#			
#				#run PowerTossim
#				experimentLib.runPowerTossimScript(optUseSensorBoard, powerTossimDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
#			
#				#compute the total power used and add point to the graphs
#				addtoPowerGraphs(query,aqRate,powerTossimDir+'power-consumption.trace', txCount)
#			
#			else:	
#				print 'Did not find '+powerTossimDir+'tossim.trace'
#										
#	#generate the graphs
#	generatePowerGraphs('linespoints',1,True)	
	
#Runs the full experiment 2
#Sets up the logger and graphs
#Loops through all queries for all buffering factors
#NOTE: Data is computed from the Java output with compiling and simulation the NesC
#Generates the result txt files and graphs
def doExperiment2():

	#set up the logger
	startLogger(currentExperimentRoot)

	#This experiment does not have a post-processing stage	
	if not os.path.isdir(currentExperimentRoot):
		os.makedirs(currentExperimentRoot)
	recordParameters(currentExperimentRoot,2)

	#graph to output total energy consumed by network
	agendaLengthGraph = GraphData.GraphData(exp2BufferingFactors,optQueries,'Buffering factor specified')
	agendaLengthGraph.yLabel = 'Agenda Averaga Result Delivery Time (ms)'
	agendaLengthGraph.title = 'Buffering factor vs. Agenda Average Result Delivery Time'

	for bFactor in exp2BufferingFactors:		
	
		for query in optQueries:	
			
			queryInstanceRoot = currentExperimentRoot+string.rstrip(query,'.txt')+'_bFactor'+str(bFactor)+'/'
			queryPlanDir = queryInstanceRoot+'query-plan/'
			desc = 'query='+query+', buffering factor='+str(bFactor)
			

			# generateQueryCompilerParams(query, queryInstanceRoot, aqRate, maxBufferingFactor, maxDeliveryTime, useCC1000Radio, controlRadioOff, simulationDuration, doSnooze):
			queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp2AqRate, bFactor, sys.maxint/1000,optUseCC1000Radio,optControlRadioOff,sys.maxint/1000, optSnooze)

			#run the query compiler
			exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, sys.maxint, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
			if (exitVal != 0):
				continue

			#Use the java output to calculate the average time
			averageDeliveryTime = experimentLib.getAverageDeliveryTime(queryPlanDir,exp2AqRate, desc, logger)

			#add point to the graph input data file
			agendaLengthGraph.addPoint(query, str(bFactor), str(averageDeliveryTime))
			
	#generate the graph
	agendaLengthGraph.generatePlotFile(currentExperimentRoot+'Exp2agendaLength.txt',True,logger)
	numericXValue = False
	agendaLengthGraph.plotGraph(currentExperimentRoot+'Exp2agendaLength','histogram',optGnuPlotExe, numericXValue, 'top left', logger)

	agendaLengthGraph.generatePlotFile(currentExperimentRoot+'Exp2agendaLength2.txt',True,logger)
	numericXValue = True	
	agendaLengthGraph.plotGraph(currentExperimentRoot+'Exp2agendaLength2','linespoints',optGnuPlotExe, numericXValue, 'top left', logger)

#Runs the full experiment 3
#Sets up the logger and graphs
#Loops through all queries for all delivery times
#Generates the result txt files and graphs	
def doExperiment3():
	
	#set up the logger
	startLogger(currentExperimentRoot)

	if (optPostProcessOnly):
		#WARNING May be out of date
		print ("reprocessing data from "+currentExperimentRoot)
		readParameters(currentExperimentRoot)
		logger.info ("reprocessing data from "+currentExperimentRoot)
	else:
		if not os.path.isdir(currentExperimentRoot):
			os.makedirs(currentExperimentRoot)
		recordParameters(currentExperimentRoot,3)
		
	#graph to output total energy consumed by network
	setupPowerGraphs('Maximum result delivery time',exp3DeliveryTimes,optQueries,'Acquistion rate: '+str(exp3AqRate))
	
	for deliveryTime in exp3DeliveryTimes:
		
		for query in optQueries:	
			queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'deliveryTime', deliveryTime)
			
			if (not optPostProcessOnly):
				#run the query compiler
				queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp3AqRate, sys.maxint, deliveryTime,optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)
				exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
				if (exitVal != 0):
					continue
				#compile the nesC code
				os.makedirs(powerTossimDir)
				experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

				#run tossim simulation
				tossimParams  = generateTossimParameters([], numNodes, powerTossimDir+'tossim.trace')
				experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)
			
			#post-process tossim script
			if (os.path.exists(powerTossimDir+'tossim.trace')):
			
				#checkResults(query,powerTossimDir,countNumSources('InFlow'),optCheckQueryResults, (optControlRadioOff and not optUseCC1000Radio), optSnooze, optExitOnValidityError, logger)
			
				txCount = experimentLib.processTossimTrace(optUsePowerFix, optIgnoreStartupTime, optControlRadioOff, powerTossimDir, numNodes, optSimulationDuration, logger)
			
				#run PowerTossim
				experimentLib.runPowerTossimScript(optUseSensorBoard, powerTossimDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
			
				#compute the total power used add point to the graphs
				addtoPowerGraphs(query,deliveryTime,powerTossimDir+'power-consumption.trace',txCount)
			
			else:	
				print 'Did not find '+powerTossimDir+'tossim.trace'	
					
	generatePowerGraphs('linespoints',3,True)	

#Runs the full experiment 4
#Sets up the logger and graphs
#Loops through all queries both in SNEEql and if applicable TinyDB
#Generates the result txt files and graphs
#WARNING TinyDB assumes the sink nodes is always node zero
#We had hoped to compare joules vs TinyDB but as tinyDB does not power down in Tossim no fair comparison could be made
def doExperiment4():
	global optIgnoreStartupTime, optCountCpuCycles, optUseCC1000Radio, optControlRadioOff

	#To keep is fair as we don't elimiate TinyDB startup
	#optSnooze and optCPUGoActive do not effect the graph published in the paper.
	optIgnoreStartupTime = False
	optSnooze = False
	optCPUGoActive = False
	
	#Instead of the default
	#optNetworkFile = 'input/9node_network_topology.nss'
	#optSchemaFile = 'input/9node_network_schemas.xml'
	#This experiment was run with
	#optNetworkFile = 'input/9to0node_network_topology.nss'
	#optSchemaFile = 'input/9to0node_network_schemas.xml'
	#This is the same toplogy but numbered slightly different so node "0" is the sink

	#set up the logger
	startLogger(currentExperimentRoot)
	
	#TinyDB only is compiled once because queries are sent in later
	experimentLib.compileTinyDB(optCountCpuCycles,logger)
	
	if (optPostProcessOnly):
		#WARNING May be out of date
		print ("reprocessing data from "+currentExperimentRoot)
		readParameters(currentExperimentRoot)
		logger.info ("reprocessing data from "+currentExperimentRoot)
	else:
		if not os.path.isdir(currentExperimentRoot):
			os.makedirs(currentExperimentRoot)
		recordParameters(currentExperimentRoot,4)
		
	#graph to output total energy consumed by network
	setupPowerGraphs('Query', optQueries,['Ours with Mica Radio', 'TinyDB'], 'Acquistion rate: '+str(exp4AqRate))

	for query in optQueries:	
		qName = string.rstrip(query,'.txt')

		#SEE optUseCC1000Radio as to why this was not used
		#cc1000
#		queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, 'query', 'cc1000_', qName)
#		if (not optPostProcessOnly):
#			
#			#run the query on our system
#			queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp4AqRate, exp4MaxBufferingFactor, sys.maxint, True, True)
#			exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
#			if (exitVal != 0):
#				continue
#			#compile the nesC code
#			os.makedirs(powerTossimDir)
#
#			experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)
#
#			#run tossim simulation with CC1000 radio
#			tossimParams  = generateTossimParameters([], numNodes, powerTossimDir+'tossim.trace')
#			#todo: add output file parameter to this method
#			experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)
#
#		#post-process cc1000 tossim script
#		if (os.path.exists(powerTossimDir+'tossim.trace')):
#
#			#checkResults(query, powerTossimDir,countNumSources('InFlow'),optCheckQueryResults,(optControlRadioOff and not optUseCC1000Radio),  optSnooze, optExitOnValidityError, logger)
#
#			experimentLib.processTossimTrace(optUsePowerFix, optIgnoreStartupTime, optControlRadioOff, powerTossimDir, numNodes, optSimulationDuration, logger)
#
#			experimentLib.runPowerTossimScript(optUseSensorBoard, powerTossimDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
#			
#			#compute the total power used and add point to the graphs
#			addtoPowerGraphs('Ours with CC1000 Radio',query,powerTossimDir+'power-consumption.trace')
#
#		else:	
#			print 'Did not find '+powerTossimDir+'tossim.trace'

		#Run the SNEEql query with the mica radio
		queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, 'query', 'mica_', qName)
		if (not optPostProcessOnly):
			
			#run the query with SNEEql
			queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp4AqRate, exp4MaxBufferingFactor, sys.maxint, False, False, optSimulationDuration,optSnooze)
			exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
			if (exitVal != 0):
				continue
			#compile the nesC code
			os.makedirs(powerTossimDir)

			experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)
			#run tossim simulation without mica radio
			tossimParams  = generateTossimParameters([], numNodes, powerTossimDir+'tossim.trace')
			#todo: add output file parameter to this method
			experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)

		if (os.path.exists(powerTossimDir+'tossim.trace')):

			#checkQueries.checkResults(query, powerTossimDir,countNumSources('InFlow'),optCheckQueryResults, (optControlRadioOff and not optUseCC1000Radio), optSnooze, optExitOnValidityError, logger)

			txCount = experimentLib.processTossimTrace(optUsePowerFix, optIgnoreStartupTime, optControlRadioOff, powerTossimDir, numNodes, optSimulationDuration, logger)

			experimentLib.runPowerTossimScript(optUseSensorBoard, powerTossimDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
			
			#compute the total power used and add point to the graphs
			addtoPowerGraphs('Ours with Mica Radio',query,powerTossimDir+'power-consumption.trace',txCount)
		
		else:	
			print 'Did not find '+powerTossimDir+'tossim.trace'

		#run the query on TinyDB (if it can be expressed in their language)
		if (exp4TinyDBQueries.has_key(qName)):
		
			#Generate all directories and parameters
			queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, 'query', 'tinydb_', qName)

			os.makedirs(powerTossimDir)

			if (optSimulationSeed == None):
				tossimParams = []
			else:
				tossimParams = ['-seed='+str(optSimulationSeed)] 
			if optUseLossy:
				tossimParams.extend(['-r=lossy','-rf='+networkFilePath])
			tossimParams.extend(['-p','-b=1','-t='+str(optSimulationDuration),'-gui',str(numNodes)])				
			tossimOutputFile = powerTossimDir+'tossim.trace'
			tinyDBQueryText = str(exp4TinyDBQueries[qName] % str(exp4AqRate))

			#Run the tinyDB simuation
			experimentLib.runTinyDBQuery(optJava5exe, tossimParams, tossimOutputFile, tinyDBQueryText, optCountCpuCycles)

			#post-process tinydb tossim script
			txCount = experimentLib.processTossimTrace(optUsePowerFix, False, optControlRadioOff, powerTossimDir, numNodes, optSimulationDuration, logger)
	
			experimentLib.runPowerTossimScript(optUseSensorBoard, powerTossimDir+'processedtossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)

			#compute the total power used and add point to the graphs
			addtoPowerGraphs('TinyDB',query,powerTossimDir+'power-consumption.trace', txCount)

		else:
			print 'No tinyDB match found for '+query
			logger.info ('No tinyDB match found for '+query)

	generatePowerGraphs('histogram',4,False)	

#WARNING May be out of date
def doPostProcessingOnly():
	global currentExperimentRoot

	SVNDirs = os.listdir(optExperimentalLogRoot) 
	for svn in SVNDirs:
		if (os.path.isdir(optExperimentalLogRoot+'/'+svn)):
			expDirs = os.listdir(optExperimentalLogRoot+'/'+svn) 
			for exp in optExperimentList:
				timeStampDirs = os.listdir(optExperimentalLogRoot+'/'+svn+'/'+'experiment'+exp) 
				for ts in timeStampDirs:
					currentExperimentRoot = optExperimentalLogRoot+'/'+svn+'/experiment'+str(exp)+'/'+ts +'/'
					if exp=='1':
						doExperiment1()
					#Experiment 2 has no post-processing at present
					elif exp=='3':
						doExperiment3()
					elif exp=='4':
						doExperiment4()
					else:
						print 'Experiment {'+str(exp)+'} not recognised'

def runTinyDBQuery (query, serverFile):	
	global optSimulationDuration,logger

	print '\nRunning TinyDB Query: '+str(query)+'\n'
	java5exe = '/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java'
	java5Exe = java5exe.replace('\\','')	
	if (not java5Exe.endswith('.exe')):
		java5Exe = java5Exe + '.exe'
	os.chdir(os.getenv('TOSROOT')+'/tools/java')
	os.spawnv(os.P_NOWAIT, java5Exe, ['java','net.tinyos.tinydb.TinyDBMain','-text','-sim', '-run', query]) 
	
	print "now the client to "+serverFile
	os.chdir(os.getenv('TOSROOT')+'/apps/TinyDBApp')
	tossimExecutable = './build/pc/main.exe' 
	tossimParams = ['-gui','-b=1','-p','-r=lossy','-rf=SIGMOD_network_topology.nss','-t='+str(optSimulationDuration),'10']
	tossimP = string.join(tossimParams,' ')
	print "tossimParams="+tossimP
	runStr = tossimExecutable+' '+tossimP+' >'+serverFile
	print "running:"+runStr
	exitVal = os.system(runStr)

	os.system('taskkill -f -im java.exe')

def countRec(inputFile):
	#9: Received MessageFrag1Ptr from ReceiveF1_F0n9
	recPattern = re.compile ('(\d+): Received.+')
	#9: DELIVER: evalTime=0 (hex=0) inflow_pressure=222 (hex=de) inflow_id=7 (hex=7) inflow_time=0 (hex=0) 
	delPattern = re.compile ('(\d+): DELIVER: evalTime=.+')
	
	radio = 0
	deliver = 0
	
	for line in fileinput.input([inputFile]):
		match = recPattern.match(line)
		#print line
		if match:
			radio = radio + 1
			
		match = delPattern.match(line)
		#print line
		if match:
			deliver = deliver + 1
	
	print "radio ="+str(radio)
	print "deliver ="+str(deliver)
	return radio,deliver

def readTinyDB(inputFile):
	count101 = 0
	count104 = 0
	count106 = 0
	count107 = 0
	count240 = 0
	countUART = 0
	count250 = 0
	#5: AM_type = 250 dest = 65535
	msgPattern = re.compile ('(\d+): AM_type = (\d+) dest = (\d+)')
	#msgPattern = re.compile ('(d): AM_type = .+')
	
	for line in fileinput.input([inputFile]):
		match = msgPattern.match(line)
		#print line
		if match:
			#print line
			site = int(match.group(1))
			type = int(match.group(2))
			dest = int(match.group(3))
			if (type == 101):
				count101 = count101+1
			elif (type == 104):
				count104 = count104+1
			elif (type == 106):
				count106 = count106+1
			elif (type == 107):
				count107 = count107+1
			elif (type == 240):
				if (dest == 126):
					countUART = countUART + 1
				else:	
					count240 = count240+1
			elif (type == 250):
				count250 = count250+1
			else:
				print line
			#print "site="+str(site)+" type="+str(type)+" dest="+str(dest)
	
	print "count101 ="+str(count101)
	print "count104 ="+str(count104)
	print "count106 ="+str(count106)
	print "count107 ="+str(count107)
	print "count240 ="+str(count240)
	print "countUART="+str(countUART)
	print "count250 ="+str(count250)
	print "total =   "+str(count101+count104+count106+count107+count240+countUART+count250)
	return count240,countUART

def doExperiment0():

	global optSimulationDuration,logger
	#currentExperimentRoot = 'c:/tmp/data/'
	query = 'Q0.txt'
	#deliveryTime = 3000
	optQueryDir = ''
	aqRate = 3000
	optUseCC1000Radio = True
	optControlRadioOff = True
	optSnooze = False
	optCountCpuCycles = False
	desc = 'Test'
	queryCompilerDefaultParams = ['-nesc-generate-mote-code=False','-display-graphs=False']
	#optJava5exe = '/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java'
	#optSNEEqlRootDir = 'c:/SNEEql/'
	#numNodes = 10
	
	
	bfs = ['0']
	queries = ['q2.txt']

	delGraph = GraphData.GraphData(queries,bfs,'xLabel')
	delGraph.yLabel = 'Deliver'
	delGraph.title = "Deliver"
	radioGraph = GraphData.GraphData(queries,bfs,'xLabel')
	radioGraph.yLabel = 'Radio'
	radioGraph.title = "Radio"
	ratioGraph = GraphData.GraphData(queries,bfs,'xLabel')
	ratioGraph.yLabel = 'YLabel'
	ratioGraph.title = "Radio"
	
	startLogger(currentExperimentRoot)

	for query in queries:
		for bf in bfs:
	
			queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'acrate', aqRate)
			outFile = currentExperimentRoot+bf+query+'.trace'
			
			if (bf != '0'):
			
					#run the query compiler
				queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, aqRate, bf, sys.maxint, optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)
					#def generateQueryCompilerParams(query, queryInstanceRoot, aqRate, maxBufferingFactor, maxDeliveryTime, useCC1000Radio, controlRadioOff, simulationDuration, doSnooze):

				exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
				
				#compile the nesC code
				#if not os.path.exists(currentExperimentRoot):
				experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)
				#if not os.path.isdir(powerTossimDir):
				#if (bf.startswith('1')):
	
				#run tossim simulation
				tossimParams  = generateTossimParameters([], numNodes, outFile)
				print "bf="+bf
				experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)

				radio,deliver = countRec(outFile )
			else:	
				doTinyDB = False
				if (query.lower().startswith("q0")):
					tinyDBQuery = "select nodeid,light where nodeid>=7 sample period 3076"
					doTinyDB = True
				if (query.lower().startswith("q2")):
					tinyDBQuery = "select AVG(light) where nodeid>= 7 sample period 3076"
					doTinyDB = True
			
				if  (doTinyDB):
					runTinyDBQuery (tinyDBQuery , outFile)	
					radio,deliver = readTinyDB(outFile)
				else:
					radio ='?'
					deliver = '?'
			
			print "radio ="	+str(radio)+ "deliver = "+str(deliver)
			radioGraph.addPoint(str(bf),query,str(radio))
			delGraph.addPoint(str(bf),query,str(deliver))
			if (deliver > 0):
				print (radio+0.0)/deliver
				ratioGraph.addPoint(str(bf),query,str((radio+0.0)/deliver))
			else:
				print "no ratio"
	
	radioGraph.generatePlotFile(currentExperimentRoot+'Radio.txt',False, logger)
	radioGraph.plotGraph(currentExperimentRoot+'Radio','histogram',optGnuPlotExe, False, 'top right', logger)
	delGraph.generatePlotFile(currentExperimentRoot+'Deliver.txt',False, logger)
	delGraph.plotGraph(currentExperimentRoot+'Deliver','histogram',optGnuPlotExe, False, 'top right', logger)
	ratioGraph.generatePlotFile(currentExperimentRoot+'Ratio.txt',False, logger)
	ratioGraph.plotGraph(currentExperimentRoot+'Ratio','histogram',optGnuPlotExe, False, 'top right', logger)
	#def generatePowerGraphs(graphType, experiment,numericXValue):
	#generatePowerGraphs(,4,False)	

		

#Do one or more experiment depending on the parameters
def doCompleteExperiments():

	global currentExperimentRoot

	for experiment in optExperimentList:

		currentExperimentRoot = optExperimentalLogRoot+'/'+svnVersion+'/experiment'+str(experiment)+'/'+timeStamp+'/'
		
		if experiment=="0":
			doExperiment0()
		elif experiment=='1':
			doExperiment1()
		elif experiment=='2':
			doExperiment2()
		elif experiment=='3':
			doExperiment3()
		elif experiment=='4':
			doExperiment4()
		else:
			print 'Experiment {'+str(experiment)+'} not recognised'
		
		#counter.countJava (optSNEEqlSourceDir,currentExperimentRoot+'javaCount.txt')	
		#counter.countNesC (currentExperimentRoot,currentExperimentRoot+'nescCount.txt')

				
def main(): 
	global numNodes, timeStamp, svnVersion
		
	#parse the command-line arguments
	parseArgs(sys.argv[1:],False) 
	
	#initialize stuff
	svnVersion = experimentLib.getSVNversionStamp(optTortoiseSVNbinDir, optSNEEqlRootDir, logger)
	timeStamp = experimentLib.getTimeStamp(logger)
	numNodes = experimentLib.getNumNodes(networkFilePath)
	
	#test high enough version of gnuplot is installed
	#testGraph = GraphData.GraphData('null', 'null', 'null')
	#testGraph.testGnuPlotVersion(optGnuPlotExe)
	
	#set relevant environment variables
	os.putenv('CLASSPATH',optJavaClassPath+';'+optSNEEqlClassDir+';'+os.getenv('CLASSPATH'))
	os.putenv('DBG', 'usr1,power')
	
	#run the experiments
	if optPostProcessOnly:
		print 'skipping java compilation'
		doPostProcessingOnly()
	else:
		#experimentLib.compileQueryOptimizer(optSNEEqlSourceDir, optJava5exe, optSNEEqlClassDir, optJavaClassPath, logger)
		doCompleteExperiments()
	
if __name__ == "__main__":
    main()
	
