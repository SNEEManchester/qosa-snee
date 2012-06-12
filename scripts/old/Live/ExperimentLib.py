import os, sys, logging, re, fileinput, time, string, Globals

#File to which the log information will be written
#There will be one in the directory for each experiment
logger = logging.getLogger('experiment')

#Logs, prints and runs a command
#Returns the exit value
def runCommand(string, logger):
	if (logger != None):
		logger.info ('running: '+string)
	print "running: "+string
	exitVal = os.system(string)
	return exitVal
	
#Logs, prints and runs a command
#Returns the exit value
def spawnlCommand(int1, string2, string3, string4, logger):
	if (logger != None):
		logger.info ('running: '+str(int1)+','+string2+','+string3+','+string4)
	print "running: "+str(int1)+','+string2+','+string3+','+string4
	exitVal = os.spawnl(int1,string2,string3,string4)
	return exitVal

#Logs, prints and runs a command
#Returns the exit value
def spawnlCommand2(int1, string2, string3, logger):
	if (logger != None):
		logger.info ('running: '+str(int1)+','+string2+','+string3)
	print "running: "+str(int1)+','+string2+','+string3
	exitVal = os.spawnl(int1,string2,string3)
	return exitVal

#Logs and prints
def report(string, logger):
	if (logger != None):
		logger.info(string)
	print string	

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
	
#Given a query plan dir, returns the average time between each acquire and the delivery
def getAverageDeliveryTimeAgendaMakespan(queryPlanDir, acRate, desc, logger):

	pattern = re.compile('Duration of single agenda evaluation\:\t+(\d+) time units')
	bfPattern = re.compile('Buffering factor used\:\t+(\d+)')
	makespan = -1
	bf = 1
	
	for line in fileinput.input([queryPlanDir+'query-plan-summary.txt']):
		m = pattern.match(line)
		if m:
			makespan = int(m.group(1))
		m = bfPattern.match(line)
		if m:
			bf = int(m.group(1))
	
	averageAcquireTime = acRate * (bf-1) / 2
	if (makespan!=-1):
		return makespan - averageAcquireTime
	else:
		logger.critical('Unable to determine duration of agenda evaluation for query '+desc)
		print '************** Unable to determine duration of agenda evaluation for query '+desc
		sys.exit(4)
		

#Given query optimizer parameters generates a query plan and nesC code accordingly
#Will exit the script if the SNEEql compiler exits with a value other than 0 or 1
def compileQuery(queryCompilerParams, desc, logger):

	#prepare the complier
	os.chdir(Globals.SNEEqlDir)
	commandStr = Globals.javaExe+' uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler '+string.join(queryCompilerParams,' ')
	#commandStr = Globals.javaExe+' -jar SNEEql.jar -jar antlr-2.7.5.jar -classpath='+Globals.SNEEqlRootDir+'/antlr-2.7.5.jar '+string.join(queryCompilerParams,' ')
	logger.info(commandStr)

	#Run the compiler
	print 'running: '+commandStr
	exitVal = os.system(commandStr)
		
	#Check if there is an error and if it is serious enough to stop the whole script
	if (exitVal==0):
		logger.info('Query compiled succesfully for query '+desc)
		print str(exitVal)+'Query compiled succesfully for query '+desc

	elif (exitVal==256): #java 1
		#Java Exit 1 signals an exception what only effects the given query with the given paramters
		logger.warning('Query compiler ended with java exit value 1 shown as '+str(exitVal)+' for query '+desc)
		print '*************** Query compiler ended with exit value 1 shown as '+str(exitVal)+' for query '+desc
	else: #Java 2 = 512)
		#Java Exit 2 signal a serious exception expected to affect all queries and QoS settings,
		logger.warning('Query compiler ended with exit value '+str(exitVal)+' for query '+desc)
		print '*************** Query compiler ended with exit value '+str(exitVal)+' for query '+desc
		sys.exit(2)
		
	return exitVal


#Given the directory for a nesC query plan compiles it into executable tossim code and avrora objects
#Compiles all subdirectories found so all notes and tossim
def compileNesC(nescDir,desc,logger):
	if (os.path.isdir(nescDir)):
		os.chdir(nescDir)
		print 'nescDir:'+str(nescDir)
		moteDirs = os.listdir('.')
		for moteDir in moteDirs:
			if os.path.isdir(moteDir):
				print "Compiling " +moteDir
				os.chdir(moteDir)
				if (str(moteDir).find('tossim')>=0):	
					exitVal = spawnlCommand(os.P_WAIT, '/usr/bin/make', '/usr/bin/make', 'pc',logger)
				elif (str(moteDir).find('mote')>=0):	
					exitVal = spawnlCommand(os.P_WAIT, '/usr/bin/make', '/usr/bin/make', 'mica2',logger)
					if (exitVal == 0):			
						runCommand ("avr-objdump -zhD build/mica2/main.exe > "  + nescDir + str(moteDir)+'.od',logger)			
				else:
					print "Ignoring directory "+moteDir+" when compling query plans"
					exitVal = 0
				if (exitVal!=0):
					report ('NesC compilation error for query '+desc+' on '+str(moteDir), logger)	
					sys.exit(2);
				else:
					report ('NesC compilation successful for query '+desc+' on '+str(moteDir), logger)	
				os.chdir('..')
			else:
				report ("Ignoring non directory "+str(moteDir), logger);
	else:
		logger.warning('In compileQueryPlan: No NesC source code directory found for query '+desc)	
		print 'No NesC source code directory found for query '+desc
	
#Compiles the TinyDB code	
def compileTinyDB(logger):
	os.chdir(os.getenv('TOSROOT')+"/apps/TinyDBApp ")

	exitVal = os.spawnl(os.P_WAIT, '/usr/bin/make', '/usr/bin/make', 'pc')
				
	if (exitVal!=0):
		logger.warning('TinyDB compilation error')	
		print 'TinyDB compilation error'
		sys.exit(2);
	else:
		logger.info('TinyDB compilation successul')
		print 'TinyDB compilation successul'


#Given the directory for a nesC query plan, runs a tossim simulation with specified parameters
def runTossimSimulation(nescDir,tossimParams,desc,logger):
	if (os.path.isdir(nescDir)):	
	
		#Prepare the directory 
		print 'Running Tossim simulation for query '+desc
		logger.info('Running Tossim simulation for query '+desc)
		os.chdir('tossim/build/pc')
		
		tossimExecutable = './main.exe'
			
		#Prepare the operating system commandcommand
		print tossimExecutable+' '+string.join(tossimParams,' ')
		logger.info(tossimExecutable+' '+string.join(tossimParams,' '))
	
		#Run Tossim
		exitVal = os.system(tossimExecutable+' '+string.join(tossimParams,' '))
		
		#Exit the script is Tossim fails to run
		if (exitVal!=0):
			logger.warning('Error during tossim simulation for query '+desc)	
			logger.warning('Attempted to run '+ tossimExecutable+' '+string.join(tossimParams,' '))	
			print '***************** Error during tossim simulation for query '+desc
			print 'Attempted to run '+ tossimExecutable+' '+string.join(tossimParams,' ')
			sys.exit(2);
		else:
			logger.info('Tossim simulation successful for query '+desc)
			print 'Tossim simulation successful for query '+desc
	else:
		logger.warning('In RunTossimSimulation: No NesC directory found for query '+desc)	
		print 'No NesC compilation directory found for query '+desc

def runTinyDBQuery (query, outputFile,logger):	

	#Run the java client
	print '\nRunning TinyDB Query: '+str(query)+'\n'
	logger.info('\nRunning TinyDB Query: '+str(query)+'\n')
	
	javaExe = Globals.javaExe.replace('\\','')	
	if (not javaExe.endswith('.exe')):
		javaExe = javaExe + '.exe'
	os.chdir(os.getenv('TOSROOT')+'/tools/java')
	os.spawnv(os.P_NOWAIT, javaExe, ['java','net.tinyos.tinydb.TinyDBMain','-text','-sim', '-run', query]) 
	print 'Java side running'
	logger.info('Java side running')
	

	#Run the Nesc nodes 	
	print "Now the Mote code to"+outputFile
	logger.info ("Now the Mote code to"+outputFile)
	
	os.chdir(os.getenv('TOSROOT')+'/apps/TinyDBApp')
	tossimExecutable = './build/pc/main.exe' 
	tossimParams = ['-gui','-b=1','-r=lossy','-rf='+Globals.tossimNetworkFilePath,'-t='+str(Globals.simulationDuration),'10']
	tossimP = string.join(tossimParams,' ')
	print "tossimParams="+tossimP
	runStr = tossimExecutable+' '+tossimP+' >' + outputFile
	print "running:"+runStr
	
	exitVal = os.system(runStr)
	
	#Kill the java client once Tossim has finished.
	os.system('taskkill -f -im java.exe')

#Converts a string to boolean based on the value of the first character.
def convertBoolean(desc, text):
	if text == '':
		return False
	if text[0] in ('T','t'):
		return True
	if text[0] in ('F','f'):
		return False
	print 'Cannot convert "'+text+'" to boolean value for '+desc
	sys.exit(1)


