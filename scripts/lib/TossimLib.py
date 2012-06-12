import logging, UtilLib, fileinput, string, os, sys

optSneeqlRoot = os.getenv("SNEEQLROOT")
optJavaClassPath = os.getenv("CLASSPATH")
optJavac5exe = os.getenv("JAVA5BINDIR") + "/javac.exe"
optJava5exe  = os.getenv("JAVA5BINDIR") + "/java.exe"

logger = None

def getOptNames():
	optNames = ["java-class-path=", "java-5-exe=", "simulation-duration="]
	return optNames

def usage():
	print '\nFor the Tossim library:'
	print '--java-class-path=<dir> \n\tdefault: '+optJavaClassPath
	print '--java-5-exe=<file> \n\tdefault: '+optJava5exe
	

def setOpts(opts):
	global optJavaClassPath, optJavaClassPath, optSimulationDuration
	
	for o, a in opts:
		if (o == "-C" or o== "--java-class-path"):
			optJavaClassPath = a
			continue
		if (o == "-J" or o== "--java-5-exe"):
			optJava5exe = a
			continue

			
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
 	


def compileNesCCode(nescRootDir):
	os.chdir(nescRootDir)
	exitVal = os.system("make pc")
	if (exitVal != 0):
		reportError("Failed with "+nescRootDir+" compilation")
	return exitVal
		
def runSimulation(nescRootDir, outputFilePath, desc, numNodes, simulationDuration = 100, networkFilePath = None):
	if (os.path.isdir(nescRootDir)):	
	
		report('Running Tossim simulation for query '+desc)
		os.putenv("DBG", "usr1")
		tossimExecutable = './build/pc/main.exe'
		tossimParams = ["-b=1", "-t=%d" % simulationDuration, str(numNodes)]
		
		commandStr = "%s %s > %s" % (tossimExecutable, string.join(tossimParams,' '), outputFilePath)
		report(commandStr)
		exitVal = os.system(commandStr)
		
		#Exit the script if Tossim fails to run
		if (exitVal!=0):
			reportError('Error during tossim simulation for query '+desc)	
		else:
			report('Tossim simulation successful for query '+desc)
		return exitVal;
	
	reportWarning('In RunTossimSimulation: No NesC directory found for query '+desc)
	exit(2)

		
#Returns the number of nodes given a network connectivity file path
#Assumes the Tossim lossy file format
def getNumNodes(networkFile):
	
	numNodes = -1;
	for line in fileinput.input([networkFile]):
		list = string.split(line,':')
		if (int(list[0]) > numNodes):
			numNodes = int(list[0])
		if (int(list[1]) > numNodes):
			numNodes = int(list[1])
	
	#Don't forget node 0
	numNodes = int(numNodes) + 1
	return numNodes
	