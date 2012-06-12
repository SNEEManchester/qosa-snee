import getopt, os, sys, logging, re, fileinput, time, string, experimentLib, GraphData, counter, AvroraLib 
 
import xml.dom.minidom
from xml.dom.minidom import Node

#Parameters relating to execution environment
optJavaClassPath = '.;C:\\SNEEql\\lib\\antlr-2.7.5.jar;C:\\SNEEql\\bin'
optGnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'
optJava5exe = "/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java"
optExperimentalLogRoot = 'c:/experiments'
optSNEEqlRootDir = 'c:/SNEEql/'
optSNEEqlSourceDir = optSNEEqlRootDir + 'src/'
optSNEEqlClassDir = optSNEEqlRootDir + 'bin/'
optTortoiseSVNbinDir = '/cygdrive/c/Program\ Files/TortoiseSVN/bin/'
optAvroraPath = "c:/Program Files/Avrora/bin" 

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
tossimNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.nss'
	#derived
avroraNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.top'
	#The file that describes the schema used.
	#No error is caused if the file contains unused schemas
	#TODO Fix the file to reflect that in the paper
optSchemaFile = 'input/mini_network_schemas.xml'
	#derived
schemaFilePath = optSNEEqlRootDir + optSchemaFile
	#Directory of files with the avrora input sensor data
optDataDirectory = 'Random Data'
	#Derived
dataPath = optSNEEqlRootDir + optDataDirectory
	#The files containg the queries used in the experiments
	#???WARNING: Only first query is considered????
optQueries = ['Q1.txt','Q2.txt','Q3.txt'] 
optQueryDir = ''
	#optCountCpuCycles instructs the script to use PowerTossims cpu counting option
	#Was not used as we could not validate the results produced by PowerTossim
	#NOTE This will not work optUseCC1000Radio = True
	#NOTE Use either this or optCPUGoActive = True to avoid double counting
optCountCpuCycles = False
	#optSnooze Inserts code to turn the cpu on and off
	#NOTE: If used with optUseCC1000Radio = False we use an adapted version of snooze
	#NOTE: Ignored in experiment 4 where it is always false
optSnooze = True
	#optSimulationDuration dettermines how long the experiment should run
	#NOTE: This is in second where all other times are in milliseconds
	#NOTE: Requires optControlRadioOff = True
	#SEE: optIgnoreStartupTime
optSimulationDuration = 15
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
optControlRadioOff = True
	#optAvrora dettermines if the code should be the Mica2 version of the code
	#This will make the code able to run in Avrora
	#It will also make the code run in Avrora.
optAvrora = True	
	#optUseLossy Instructs Tossim to use the network topology as a lossy file
	#The file need to be in unix format or tossim will ignore it wothout throwing an error
optUseLossy = True
	#optCPUGoActive Instructs the NesC generator to add power debug statements
	#These help Power Tossim know when the CPU could be actively executing code
	#NOTE: Ignored in experiment 4 where it is always false
optCPUGoActive = False
	#optDeliverLast When set to true will cause the last loop of the agenda to be adpated
	#This is insure all buffered evaluations are completed.
optDeliverLast = True
	#optLedDebug When set to true allows any led debug statements to be included in the code.
	#This includes linking in the leds.
	#Otherwise all leds debug statemments are not included and the Leds are not linked in.
optLedDebug = False
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
	#exp3MaxBufferingFactor Optional way of limiting buffering factor
	#Best results are obtained by setting it to max
exp4MaxBufferingFactor = sys.maxint
	#exp4TinyDBQueries Mapping of our query file names to TinyDB queries
	#TODO change query names
exp4TinyDBQueries = {'Q1': 'select light from sensors sample period %s', 'Q2':'select avg(temp) from sensors where temp > 500 sample period %s'} #

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
queryCompilerDefaultParams = ['-display-graphs=False']
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
	print '-A<dir>, --avrora-path=<dir> \n\tdefault: '+optAvroraPath
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
	print '--data-directory=<directory> \n\tdefault: '+optDataDirectory
	print '-q<val,..val>, --queries=<val,..val> \n\tdefault: '+string.join(optQueries,',') 
	print '-c[True|False], --count-cpu-cycles=[True|False] \n\tdefault: '+str(optCountCpuCycles)
	print '-d<val>, --simulation-duration=<val> \n\tdefault: '+str(optSimulationDuration)
	print '-s<val>, --simulation-seed=<val> \n\tdefault: '+ str(optSimulationSeed)
	print '--snooze=[True|False] \n\t default: '+ str(optSnooze)
	print '--nesc-synchronization-period=<val>\n\tdefault:  '+str(nescSynchronizationPeriod)
	print '--use-cc1000-radio=[True|False] \n\tdefault: '+str(optUseCC1000Radio)
	print '--control-radio-off=[True|False] \n\tdefault: '+str(optControlRadioOff)
	print '--mica2=[True|False] \n\tdefault: '+str(optAvrora)
	print '--use-lossy=[True|False] \n\tdefault: '+str(optUseLossy)
	print '--cpu-go-active=[True|False] \n\tdefault: '+str(optCPUGoActive)
	print '--deliver-last=[True|False] \n\tdefault: '+str(optDeliverLast)
	print '--led-debug=[True|False] \n\tdefault: '+str(optLedDebug)

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
	global optSNEEqlRootDir, optSNEEqlSourceDir, optSNEEqlClassDir, optTortoiseSVNbinDir, optAvroraPath
	
	#Parameters relating to script functionality
	global optExperimentList, optPostProcessOnly, optUsePowerFix
	global optIgnoreLedPower, optUseSensorBoard, optIgnoreStartupTime
	global optBatteryEnergy, optCheckQueryResults, optExitOnValidityError	
	global optShowParametersInGraph 	

	#Parameters relating to all experiments
	global optNetworkFile, tossimNetworkFilePath, avroraNetworkFilePath 
	global optSchemaFile, schemaFilePath, optDataDirectory, dataPath 
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
		opNames = opNames + ["SNEEql-root-dir=","tortoise-svn-bin-dir=","avrora-path="]

		#Parameters relating to script functionality		
		opNames = opNames + ["post-process-only","use-power-fix="]
		opNames = opNames + ["ignore-led-power=","use-sensor-board=","ignore-startup-time="]
		opNames = opNames + ["battery-energy=","check-query-result=","exitOnValidityError="]
		opNames = opNames + ["show-parameters-in-graph="]

		#Parameters relating to all experiments
		opNames = opNames + ["network-file=","schema-file=", "data-directory="]
		opNames = opNames + ["queries=","count-cpu-cycles=","snooze="]
		opNames = opNames + ["simulation-duration=","simulation-seed=","nesc-synchronization-period="]
		opNames = opNames + ["use-cc1000-radio=","control-radio-off=","mica2","use-lossy="]
		opNames = opNames + ["cpu-go-active=","deliver-last"]

		#Parameters relating to specific experiments: defaults
		opNames = opNames + ["x1a=","exp1-aq-rates=","x1b=","exp1-max-buffering-factor="]
		opNames = opNames + ["x2a=","exp2-buffering-factors=","x2b=","exp2-aq-rate="]
		opNames = opNames + ["x3a=","exp3-aq-rate=","x3b=","exp3-max-buffering-bactor=","x3d=","exp3-delivery-times="]
		opNames = opNames + ["x4a=","exp4-aq-rate=","x4b=","exp4-max-buffering-factor="]

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
			optJavaClassPath = a
			continue
		if (o == "-G" or o== "--gnu-plot-exe"):
			optGnuPlotExe = a
			continue
		if (o == "-J" or o== "--java-5-exe"):
			optJava5exe = a
			continue
		if (o == "-L" or o== "--experimental-log-root"):
			optExperimentalLogRoot = a	
			continue
		if (o == "-S" or o== "--SNEEql-root-dir"):
			optSNEEqlRootDir = a
			optSNEEqlSourceDir = optSNEEqlRootDir + 'src/'
			optSNEEqlClassDir = optSNEEqlRootDir + 'bin/'
			tossimNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.nss'
			avroraNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.top'
			schemaFilePath = optSNEEqlRootDir + optSchemaFile
			dataPath = optSNEEqlRootDir + optDataDirectory	
			continue
		if (o == "-T" or o == "--tortoise-svn-bin-dir"):
			optTortoiseSVNbinDir = a
			continue
		if (o == '-A' or o == "--avrora-path"):
			optAvroraPath = a
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
			tossimNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.nss'
			avroraNetworkFilePath = optSNEEqlRootDir + optNetworkFile+'.top'
		elif (o == "-t" or o== "--schema-file"):
			optSchemaFile = a;
			schemaFilePath = optSNEEqlRootDir + optSchemaFile
		elif (o == "--data-directory="):
			optDataDirectory = a
			dataPath = optSNEEqlRootDir + optDataDirectory	
		elif (o == "-q" or o == "--queries" ):
			optQueries = string.split(a,',')
		elif (o == "-d" or o== "--simulation-duration"):
			optSimulationDuration = int(a)
			#print "optSimulationDuration "+str(optSimulationDuration)
		elif (o == "-s" or o== "--simulation-seed"):
			optSimulationSeed = int(a)
		elif (o == "--use-cc1000-radio"):
			optUseCC1000Radio = experimentLib.convertBoolean(o,a)
		elif (o == "--control-radio-off"):
			optControlRadioOff = experimentLib.convertBoolean(o,a)
		elif (o == "--avrora"):
			optAvrora = experimentLib.convertBoolean(o,a)
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
		elif (o=="--led-debug"):
			optLedDebug = experimentLib.convertBoolean(o,a)

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
	s = s+ "--network-file=%s --schema-file=%s --data-directory=%s "
	s = s % (optNetworkFile, optSchemaFile, optDataDirectory)
	s = s+ "--queries=%s --count-cpu-cycles=%s --snooze=%s "
	s = s % (string.join(optQueries,','), str(optCountCpuCycles), str(optSnooze))
	s = s+ "--simulation-duration=%s --simulation-seed=%s --nesc-synchronization-period=%s "
	s = s % (str(optSimulationDuration),  str(optSimulationSeed),str(nescSynchronizationPeriod))
	s = s+ "--use-cc1000-radio=%s --control-radio-off=%s --avrora=%s --use-lossy=%s "
	s = s % (str(optUseCC1000Radio), str(optControlRadioOff), str(optAvrora), str(optUseLossy))
	s = s+ "--cpu-go-active=%s --deliver-last=%s --led-debug=%s "
	s = s % (str(optCPUGoActive), str(optDeliverLast), str(optLedDebug))

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
	outputDir = queryInstanceRoot+'output/'
	
	#print '"'+aqRate + '"/"' + query + '"'
	desc = 'query='+query+', '+prefix+'='+str(number)
	#print 'desc="'+desc+'"'
	return queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc


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
def setupPowerGraphs(xlabelText, xRange, yRange, extraText):

	global totalGraph, workingGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph, energyGraph

	#graph variables
	xAxisPoints = xRange
	lineLabels = yRange
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

	#graph to output working energy consumed by network
	workingGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	workingGraph.yLabel = 'Network Total active and transmit energy consumption (mJ)'
	workingGraph.title = xlabelText + ' vs. Active and Transmit Energy Consumption'+titleExtraLines

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

	newXAxisPoints = []
	for yValue in yRange:
		for xValue in xRange:
			newXAxisPoints = newXAxisPoints + [str(xValue)+str(yValue)]
	energyGraph = GraphData.GraphData(newXAxisPoints,["Sensor","CPU","Radio","Other"],xLabel)
	energyGraph.yLabel = 'Enery used'
	energyGraph.title = xlabelText + ' vs. Energy Used'+titleExtraLines

#Converts the output of powerTossim into values to become points on the graphs
#totalGraph The sum of the total joules used in each site
#averageGraph The average of the joules used in eahc site
#maxGraph The total joules used by the site with the highest total 
#lifetimeGraph Lifetime of the network calculated at the battery/ max from above
#radioGraph The sum of the radio joules for each site
#cpu_cycleGraph The sum of the cpu_cycles joules for each site. SEE: optCountCpuCycles
#txGraph: The sum of the Power tx debug lines found on all sites
def addtoPowerGraphs(yValue,xValue,inputfile, txCount):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph, energyGraph

	print'************ Adding '+str(xValue)
	
	#compute the total power used

	if (optAvrora):		
		totalPower, maxPower, averagePower, radioPower, cpu_cyclePower, sensorEnergy, otherEnergy = AvroraLib.computeEnergyConsumed(inputfile)
	        totalPower = totalPower * 1000
	        maxPower = maxPower * 1000
	        averagePower = averagePower * 1000
	        radioPower = radioPower * 1000
	        cpu_cyclePower = cpu_cyclePower * 1000
		txCount = AvroraLib.getPacketCount()
		workingEnergy = AvroraLib.getWorkingEnergy() * 1000
	else:
		totalPower, maxPower, averagePower, radioPower, cpu_cyclePower = experimentLib.computeEnergyConsumed(inputfile,optIgnoreLedPower)

	#add point to the graphs
	totalGraph.addPoint(yValue,xValue,str(totalPower))
	maxGraph.addPoint(yValue,xValue,str(maxPower))
	averageGraph.addPoint(yValue, xValue, str(averagePower))
	radioGraph.addPoint(yValue,xValue,str(radioPower))
	cpu_cycleGraph.addPoint(yValue,xValue,str(cpu_cyclePower))	
	
	if (optAvrora):
		workingGraph.addPoint(yValue,xValue,str(workingEnergy))
	
	secondLifetime = optBatteryEnergy / (maxPower/(1000*optSimulationDuration)) 
	dayLifetime = secondLifetime / (24*60*60)
	lifetimeGraph.addPoint(yValue, xValue, str(dayLifetime))
	txGraph.addPoint(yValue, xValue, txCount)
	
	energyGraph.addPoint("Sensor",str(xValue)+str(yValue),str(sensorEnergy))
	print str(xValue)+str(yValue)+"sensorEnergy "+str(sensorEnergy)
	energyGraph.addPoint("CPU",str(xValue)+str(yValue),str(cpu_cyclePower/1000))
	print str(xValue)+str(yValue)+"cpu_cyclePower "+str(cpu_cyclePower/1000)
	energyGraph.addPoint("Radio",str(xValue)+str(yValue),str(radioPower/1000))
	print str(xValue)+str(yValue)+"radioPower "+str(radioPower/1000)
	energyGraph.addPoint("Other",str(xValue)+str(yValue),str(otherEnergy))
	print str(xValue)+str(yValue)+"otherEnergy "+str(otherEnergy)

#Generates txt files used to plot graphs - Including versions used in Paper
#Generates gif and eps version of the graphs 
#SEE:addtoPowerGraphs(..)
def generatePowerGraphs(graphType, experiment,numericXValue):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, txGraph, energyGraph

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
	txGraph.generatePlotFile(currentExperimentRoot+exText+'packet_count.txt',numericXValue, logger)
	txGraph.plotGraph(currentExperimentRoot+exText+'packet_count',graphType,optGnuPlotExe, numericXValue, 'top right', logger)
	energyGraph.generatePlotFile(currentExperimentRoot+exText+'energy.txt',False, logger)
	energyGraph.plotGraph(currentExperimentRoot+exText+'energy','histograms',optGnuPlotExe, numericXValue, 'top right', logger)

	if (optAvrora):
		workingGraph.generatePlotFile(currentExperimentRoot+exText+'workingEnergy.txt',numericXValue, logger)
		workingGraph.plotGraph(currentExperimentRoot+exText+'workingEnergy',graphType,optGnuPlotExe, numericXValue, 'top right', logger)


#Converts all the script parameters into Java parmeters
def generateQueryCompilerParams(query, queryInstanceRoot, aqRate, maxBufferingFactor, maxDeliveryTime, optUseCC1000Radio, optControlRadioOff, simulationDuration, optSnooze):
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
	queryCompilerParams+= ['-network-topology-file="'+tossimNetworkFilePath+'"']
	queryCompilerParams+= ['-schema-file="'+schemaFilePath+'"']
	queryCompilerParams+= ['-nesc-synchronization-period='+str(nescSynchronizationPeriod)]
	
	if (optAvrora):
		queryCompilerParams+= ['-nesc-mica2=true']
		queryCompilerParams+= ['-nesc-generate-mote-code=true']
		queryCompilerParams+= ['-nesc-generate-tossim-code=false']
		queryCompilerParams+= ['-nesc-use-cc1000-radio=true']
	else:
		queryCompilerParams+= ['-nesc-use-cc1000-radio='+str(optUseCC1000Radio)]
		queryCompilerParams+= ['-nesc-mica2=false']
		#queryCompilerParams+= ['-nesc-generate-mote-code=true']
		#queryCompilerParams+= ['-nesc-generate-tossim-code=false']
	
	if (optSnooze):
		queryCompilerParams+= ['-nesc-do-snooze=true']
		queryCompilerParams+= ['-nesc-control-radio-off=true']				
	else:
		queryCompilerParams+= ['-nesc-do-snooze=false']
		queryCompilerParams+= ['-nesc-control-radio-off='+str(optControlRadioOff)]				
		
	if (optCPUGoActive):
		queryCompilerParams+= ['-nesc-cpu-go-active=true']
	else:
		queryCompilerParams+= ['-nesc-cpu-go-active=false']
	
	if (optDeliverLast):
		queryCompilerParams+= ['-nesc-deliver-last=true']
	else:
		queryCompilerParams+= ['-nesc-deliver-last=false']
	if (optLedDebug):
		queryCompilerParams+= ['-nesc-led-debug=true']
	else:
		queryCompilerParams+= ['-nesc-led-debug=false']
	
	return queryCompilerParams  
	
def getSensorData ():
	global numNodes
	
	dataLines = []
	for i in range(numNodes):
		fileName = "c:/SNEEql/input/countData/node"+str(i)+".txt"
		if os.path.exists(fileName):
			dataLines = dataLines + ["light:"+str(i)+":"+fileName]
		else:
			dataLines = dataLine + ["light:"+str(i)+":."]
	return dataLines	
	
def getOds(nescDir):
	global numNodes

	print 'nescDir:'+nescDir
	ods = []
	for i in range(numNodes):
		fileName = nescDir +"mote"+str(i)+".od"
		if os.path.exists(fileName):
			ods = ods + [fileName]
		else:
			ods = ods + [optSNEEqlSourceDir+"empty.od"]
	return ods	
	
def getCount():
	global numNodes

	count = []
	for i in range(numNodes):
		count = count + [str(1)]
	return count	
	
#Runs the full experiment 1
#Sets up the logger and graphs
#Loops through all queries for all acquitsion rates generaating data
#Generates the result txt files and graphs
def doExperiment1():

	#set up the logger
	startLogger(currentExperimentRoot)

	if (optPostProcessOnly):
		print ("reprocessing data from "+currentExperimentRoot)
		#readParameters(currentExperimentRoot)
		logger.info ("reprocessing data from "+currentExperimentRoot)
		
	else:
		if not os.path.isdir(currentExperimentRoot):
			os.makedirs(currentExperimentRoot)
		recordParameters(currentExperimentRoot,1)

	setupPowerGraphs('Acquisition interval (ms)',exp1AqRates, optQueries,'Max buffering factor: '+str(exp1MaxBufferingFactor))

	for aqRate in exp1AqRates:
		
		for query in optQueries:	
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs(currentExperimentRoot, query, 'aqRate', aqRate)

			if (not optPostProcessOnly):
				#run the query compiler
				
				queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, aqRate, exp1MaxBufferingFactor, sys.maxint,optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)

				exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
				if (exitVal != 0):
					continue

				#compile the nesC code
				os.makedirs(outputDir)
				experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

				if optAvrora:
					AvroraLib.runSimulation(nescDir, outputDir, desc, numNodes, avroraNetworkFilePath, logger)
				else:	
					#run tossim simulation
					tossimParams  = generateTossimParameters([], numNodes, outputDir+'tossim.trace')
					experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)
		
			if (os.path.exists(outputDir+'/avrora.txt')):
				addtoPowerGraphs(query,aqRate,outputDir+'/avrora.txt', -1)
			
			elif (os.path.exists(outputDir+'tossim.trace')):
			
				#post-process tossim script				
				#checkResults(query,outputDir,countNumSources('InFlow'),optCheckQueryResults, (optControlRadioOff and not optUseCC1000Radio), optSnooze, optExitOnValidityError, logger)
				txCount = experimentLib.processTossimTrace(optUsePowerFix, outputDir, numNodes, optSimulationDuration, logger)
			
				#run PowerTossim
				experimentLib.runPowerTossimScript(optUseSensorBoard, outputDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
			
				#compute the total power used and add point to the graphs
				addtoPowerGraphs(query,aqRate,outputDir+'power-consumption.trace', txCount)
	
			else:	
				print 'Did not find '+outputDir+'tossim.trace or '+outputDir+'+/avrora.txt'
										
	#generate the graphs
	generatePowerGraphs('linespoints',1,True)	

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
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'deliveryTime', deliveryTime)
			
			if (not optPostProcessOnly):
				#run the query compiler
				queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp3AqRate, sys.maxint, deliveryTime,optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)
				exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
				if (exitVal != 0):
					continue
				#compile the nesC code
				os.makedirs(outputDir)
				experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

				if optAvrora:
					AvroraLib.runSimulation(nescDir, outputDir, desc, numNodes, avroraNetworkFilePath, logger)
				else:	
					#run tossim simulation
					tossimParams  = generateTossimParameters([], numNodes, outputDir+'tossim.trace')
					experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)
			
			if (os.path.exists(outputDir+'/avrora.txt')):
				addtoPowerGraphs(query,deliveryTime,outputDir+'/avrora.txt', -1)
			
			elif (os.path.exists(powerTossimDir+'tossim.trace')):
				#post-process tossim script
				#checkResults(query,powerTossimDir,countNumSources('InFlow'),optCheckQueryResults, (optControlRadioOff and not optUseCC1000Radio), optSnooze, optExitOnValidityError, logger)
			
				txCount = experimentLib.processTossimTrace(optUsePowerFix, optIgnoreStartupTime, optControlRadioOff, outputDir, numNodes, optSimulationDuration, logger)
			
				#run PowerTossim
				experimentLib.runPowerTossimScript(optUseSensorBoard, outputDir+'processedTossim.trace',powerTossimDir+'power-consumption.trace',desc,logger)
			
				#compute the total power used add point to the graphs
				addtoPowerGraphs(query,deliveryTime,outputDir+'power-consumption.trace',txCount)
			
			else:	
				print 'Did not find '+outputDir+'tossim.trace'	
					
	generatePowerGraphs('linespoints',3,True)	
				
#Runs the full experiment 4
#Sets up the logger and graphs
#Loops through all queries for all delivery times
#Generates the result txt files and graphs	
def doExperiment4():
	
	q4Queries = ["select nodeid,light where nodeid=7 epoch duration 3072"]
	#set up the logger
	startLogger(currentExperimentRoot)

	if (not optAvrora):
		print "Experiment 4 needs optAvrora"
		return

	if not os.path.isdir(currentExperimentRoot):
		os.makedirs(currentExperimentRoot)
	recordParameters(currentExperimentRoot,4)
		
	#graph to output total energy consumed by network
	setupPowerGraphs('Query', optQueries,['SNEE', 'TinyDB'], 'Acquistion rate: '+str(exp4AqRate))
	
	for query in optQueries:
	
		queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'SNEE', exp4AqRate)
			
		#run the query compiler
		queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, exp4AqRate, sys.maxint, sys.maxint, optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)
		exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
		if (exitVal != 0):
			continue
		#compile the nesC code
		os.makedirs(outputDir)
		experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

		AvroraLib.runSimulation(nescDir, outputDir, desc, numNodes, avroraNetworkFilePath, logger)
			
		if (os.path.exists(outputDir+'/avrora.txt')):
			addtoPowerGraphs('SNEE',query,outputDir+'/avrora.txt', -1)
			
		else:	
			print 'Did not find '+outputDir+'tossim.trace'	

		doTinyDB = False
		
		if (query.lower().startswith("q1")):
			tinyDBQuery = "select nodeid,light where nodeid>=7 epoch duration "+str(exp4AqRate/1000*1024)
			doTinyDB = True
		if (query.lower().startswith("q2")):
			tinyDBQuery = "select AVG(light) epoch duration "+str(exp4AqRate/1000*1024)
			doTinyDB = True
		if (query.lower().startswith("q3")):
			doTinyDB = True
			tinyDBQuery = "select nodeid,light epoch duration "+str(exp4AqRate/1000*1024)
		
		if doTinyDB:
		
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'TinyDB', exp4AqRate/1000*1024)
			os.makedirs(outputDir)
			
			AvroraLib.runTinyDB("c:/tinyos/cygwin/opt/tinyos-1.x/apps/TinyDBApp/", outputDir, tinyDBQuery, numNodes, avroraNetworkFilePath, logger)
					
			if (os.path.exists(outputDir+'/avrora.txt')):
				addtoPowerGraphs('TinyDB',query,outputDir+'/avrora.txt', -1)
					
			else:	
				print 'Did not find '+outputDir+'avrora.txt'	
					
	generatePowerGraphs('histogram',4,False)	

def main(): 
	global numNodes, timeStamp, svnVersion, currentExperimentRoot
			
	#parse the command-line arguments
	parseArgs(sys.argv[1:],False) 
		
	#initialize stuff
	svnVersion = experimentLib.getSVNversionStamp(optTortoiseSVNbinDir, optSNEEqlRootDir, logger)
	print svnVersion;
	
	if optAvrora:
		numNodes = AvroraLib.getNumNodes(avroraNetworkFilePath)
	else:
		numNodes = experimentLib.getNumNodes(tossimNetworkFilePath)
	print "numNodes "+str(numNodes) 
	
	#currentExperimentRoot = 'c:/tmp/testdata/'

	AvroraLib.setOpts (optJava5exe, dataPath, optIgnoreLedPower, optIgnoreStartupTime, optControlRadioOff, optSNEEqlRootDir, optSimulationSeed, optSimulationDuration, optAvroraPath)
	
	#set relevant environment variables
	os.putenv('CLASSPATH',optJavaClassPath+';'+optSNEEqlClassDir+';'+os.getenv('CLASSPATH'))
	os.putenv('DBG', 'usr1,power')
	
	for run in range(1):
	
		for experiment in optExperimentList:

			timeStamp = experimentLib.getTimeStamp(logger)
			currentExperimentRoot = optExperimentalLogRoot+'/'+svnVersion+'/'+timeStamp+'/'
		
			#if experiment=="0":
			#	doExperiment0()
			if experiment=='1':
				doExperiment1()
			#elif experiment=='2':
			#	doExperiment2()
			elif experiment=='3':
				doExperiment3()
			elif experiment=='4':
				doExperiment4()
			else:
				print 'Experiment {'+str(experiment)+'} not recognised'
	
if __name__ == "__main__":
    main()
	
