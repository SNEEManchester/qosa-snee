import os, sys, logging, re, fileinput, time, string 

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

#Returns SVN version stamp
#NOTE: If a copy outside of version control is used it will always be undefined
def getSVNversionStamp(tortoiseSVNbinDir, SNEEqlRootDir, logger):
	svnVersion = 'undefined'
	
	#to do: add -nm options here, which only allow code which is identical to the repository to run
	print ('running: '+tortoiseSVNbinDir+'SubWCRev '+SNEEqlRootDir+' > SubWCRevOutput.txt')
	if (logger!= None):
		logger.info ('running: '+tortoiseSVNbinDir+'SubWCRev '+SNEEqlRootDir+' > SubWCRevOutput.txt')
	exitVal = os.system(tortoiseSVNbinDir+'SubWCRev "'+SNEEqlRootDir+'" > SubWCRevOutput.txt') 
	
	if (exitVal!=0):
		print 'Source code not identical to repository version.  Try performing an update.'
		logger.critical('Source code not identical to repository version.  Try performing an update.\n')
		sys.exit(3)
	
	pattern = re.compile('Updated to revision (\d+)')
	for line in fileinput.input(['SubWCRevOutput.txt']):
		match = pattern.match(line)
		if match:
			svnVersion = 'V'+match.group(1)


	os.remove('SubWCRevOutput.txt')
	if (logger != None):
		logger.info('Source code version: '+svnVersion)
	print 'Source code version: '+svnVersion
	return svnVersion


	if (svnVersion.find('undef') >-1):
		for line in fileinput.input(['SubWCRevOutput.txt']):
			print line
			if (line.find('Mixed revision') > -1):
				print 'Source code not identical to repository version.  Try performing an update.'
				logger.critical('Source code not identical to repository version.  Try performing an update.\n')
				sys.exit(3)
		print 'Source code identical failed.'
		logger.critical('Source code not identical failed.\n')
		sys.exit(3)



#Returns a timestamp with the current time
def getTimeStamp(logger):
	timeStamp = time.strftime("%Y-%m-%d_%H-%M-%S")
	if (logger!=None):
		logger.info('Experimental run timestamp: '+timeStamp)
	print 'Experimental run timestamp: '+timeStamp
	
	return timeStamp;


#Returns the number of nodes given a network connectivity file path
#Assumes the Tossim lossy file format
def getNumNodes(networkFilePath):
	
	numNodes = -1;
	for line in fileinput.input([networkFilePath]):
		list = string.split(line,':')
		if (int(list[0]) > numNodes):
			numNodes = int(list[0])
		if (int(list[1]) > numNodes):
			numNodes = int(list[1])
	
	#Don't forget node 0
	numNodes = int(numNodes) + 1
	return numNodes


#Compiles java query optimizer
#The 'c' directly after the java5exe is to make it a complier
def compileQueryOptimizer(SNEEqlSourceDir, java5exe, SNEEqlClassDir, javaClassPath, logger):
	os.chdir(SNEEqlSourceDir)
	
	#commandStr = optJava5exe+'c '+' -cp "' +optJavaClassPath+'" -sourcepath "'+optSNEEqlSourceDir+'\" '+' uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler.java'
	commandStr = java5exe+'c '+'-d "'+SNEEqlClassDir +'"'+' -cp "' +javaClassPath+'"'+' -sourcepath "'+SNEEqlSourceDir+'"'+' uk/ac/manchester/cs/diasmc/querycompiler/QueryCompiler.java'

	logger.info(commandStr)
	print 'running: '+commandStr
	exitVal = os.system(commandStr)
		
	if (exitVal!=0):
		logger.warning('Java compiler ended with exit value '+str(exitVal))
		print '************** Java compiler ended with exit value '+str(exitVal)
		sys.exit(2)
	else:
		logger.info('Java compiled succesfully')
		print 'Java compiled succesfully'
	return exitVal
	

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
#Will exit the script if the Java compiler exits with a value other than 0 or 1
def compileQuery(queryCompilerDefaultParams, queryCompilerParams, simulationDuration, queryPlanDir, desc, javaExe, SNEEqlRootDir, logger):

	#prepare the complier
	os.chdir(SNEEqlRootDir)
	commandStr = javaExe+' uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler '+ string.join(queryCompilerDefaultParams,' ')+' '+string.join(queryCompilerParams,' ')
	logger.info(commandStr)

	#Run the compiler
	print 'running: '+commandStr
	exitVal = os.system(commandStr)
		
	#Check if there is an error and if it is serious enough to stop the whole script
	if (exitVal==0):
		logger.info('Query compiled succesfully for query '+desc)
		print str(exitVal)+'Query compiled succesfully for query '+desc

		#As we now inform the compiler of the duration this should never happen any more		
		#if getAverageDeliveryTime(queryPlanDir, desc, logger) > (simulationDuration*1000):
		#	logger.warning('Query compiled successfully but single agenda duration is longer than simulation duration for query '+desc)
		#	print '*************** Query compiler successfully but single agenda duration is longer than simulation duration for query '+desc
		#	return 1
	elif (exitVal==256): #java 1
		#Java Exit 1 signals an exception what only effects the given query with the given paramters
		logger.warning('Query compiler ended with java exit value 1 shown as '+str(exitVal)+' for query '+desc)
		print '*************** Query compiler ended with exit value 1 shown as '+str(exitVal)+' for query '+desc
	else: #Java 2 = 512)
		logger.warning('Query compiler ended with exit value '+str(exitVal)+' for query '+desc)
		print '*************** Query compiler ended with exit value '+str(exitVal)+' for query '+desc
		sys.exit(2)
		
	return exitVal


#Given the directory for a nesC query plan compiles it into executable tossim code and avrora objects
#Compiles all subdirectories found so all notes and tossim
def compileQueryPlan(nescDir,desc,doCpuProfiling,logger):
	if (os.path.isdir(nescDir)):
		os.chdir(nescDir)
		print 'nescDir:'+str(nescDir)
		moteDirs = os.listdir('.')
		for moteDir in moteDirs:
			if os.path.isdir(moteDir):
				print "Compiling " +moteDir
				os.chdir(moteDir)
				if (str(moteDir).find('tossim')>=0):	
					if (doCpuProfiling):
						exitVal = runCommand(os.getenv('TOSROOT')+"/tools/scripts/PowerTOSSIM/compile.pl",logger)
					else:
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
def compileTinyDB(doCpuProfiling,logger):
	os.chdir(os.getenv('TOSROOT')+"/apps/TinyDBApp ")

	if (doCpuProfiling):
		#WARNING We where unable to get this to work.
		exitVal = os.system(os.getenv('TOSROOT')+"/tools/scripts/PowerTOSSIM/compile.pl")
	else:
		exitVal = os.spawnl(os.P_WAIT, '/usr/bin/make', '/usr/bin/make', 'pc')
				
	if (exitVal!=0):
		logger.warning('TinyDB compilation error')	
		print 'TinyDB compilation error'
		sys.exit(2);
	else:
		logger.info('TinyDB compilation successul')
		print 'TinyDB compilation successul'


#Given the directory for a nesC query plan, runs a tossim simulation with specified parameters
def runTossimSimulation(nescDir,tossimParams,desc,doCpuProfiling,logger):
	if (os.path.isdir(nescDir)):	
	
		#Prepare the directory 
		print 'Running Tossim simulation for query '+desc
		logger.info('Running Tossim simulation for query '+desc)
		os.chdir('tossim/build/pc')
		
		if (doCpuProfiling):
			#WARNING May be out of date
			tossimExecutable = './a.out -cpuprof'
		else:
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

#Returns the number of "RADIO_STATE TX" lines found
def processTossimTrace(usePowerFix, ignoreStartupTime, controlRadioOff, directory, numNodes, simulationDuration, logger):

	#Prepare the output files
	print "processing: "+directory	
	fout = open(directory +'processedTossim.trace','w')
	fdbg = open(directory +'dbgTossim.trace','w')
	
	#set the global variables
	#Identify lines used by PowerTossim
	powerPattern = re.compile('\d+: POWER: Mote \d+ .*? at (\d+)')
	#Identifies a line that signal the startup period is over
	startFinishedPattern = re.compile('\d+: POWER: Mote \d+ CPU_STATE ACTIVE at (\d+)')
	#Global constant from powerTossim
	tickPerSecond = 4000000
	#The number of tx lines found
	txCount = 0
	
	#Check if the startup time should be ignored	
	if ignoreStartupTime:
		inStartup= True
	else:	
		inStartup = False
		maxTick = simulationDuration * tickPerSecond
		if usePowerFix:
			#add lines to the start and end of the outputFile
			#This elimiates the random effect of motes starting at different times
			for i in range(int(numNodes)):
				if controlRadioOff:
					#Turn the radios off until the mote wakes up. When we turn them off
					fout.write (str(i)+': POWER: Mote '+str(i)+' RADIO_STATE OFF at '+str(0)+'\n')			
				else:
					#start the mote without effecting the radio at all.
					fout.write (str(i)+': POWER: Mote '+str(i)+' ADC ON at '+str(0)+'\n')			
	
	#Process each line the the tossim output
	for line in fileinput.input([directory +'tossim.trace']):
		m = powerPattern.match(line)
		if m:
			#Count the radio messages
			#NOTE: In SNEEql there are no messages in startup. But had there been they would have been counted 
			if line.find('RADIO_STATE TX') > 0:
				txCount+=1

			if inStartup:	
				#Look for the first line after startup
				n = startFinishedPattern.match(line)
				if n:
					inStartup = False
					if usePowerFix:
						#Add lines to show sites have started up as original lines showing mote startup have been ignore.
						newTick =  int(m.group(1)) 
						maxTick =  newTick + simulationDuration * tickPerSecond
						for i in range(int(numNodes)):
							if controlRadioOff:
								#Turn the radios off until the mote wakes up. When we turn them off
								fout.write (str(i)+': POWER: Mote '+str(i)+' RADIO_STATE OFF at '+str(newTick)+'\n')			
							else:
								#start the mote without effecting the radio at all.
								fout.write (str(i)+': POWER: Mote '+str(i)+' ADC ON at '+str(newTick)+'\n')			
					#Keep the line as it not part of startup
					fout.write(line)
				else:
					#Ignore all startup lines
					continue				
			else:
				#Keep all power line after startup has finished
				fout.write(line)
		else:
			#Move all none power lines to the dbg file
			fdbg.write(line)
	
	if usePowerFix:
		#Add a power line so all runs end at the same time		
		fout.write ('0: POWER: Mote 0 RADIO_STATE OFF at '+str(maxTick)+'\n')
	
	return txCount

#Given the directory for a nesC query plan, runs a tossim simulation with specified parameters
def runPowerTossimScript(optUseSensorBoard, inputFile,outputFile,desc,logger):

	#To simulate sensor boards on and off with CPU use this
	#WARNING Replace "c:/SNEEql" with the location of the text file
	#exitVal = os.system(os.getenv('TOSROOT')+'/tools/scripts/PowerTOSSIM/postprocess.py --sb=1 --em '+'c:/SNEEql/scripts/ICDE08/turn_off_sensors_energy_model.txt '+inputFile+' > '+outputFile)

	#-sb=0 no sensor board attached assumed
	if optUseSensorBoard:
		exitVal = os.system(os.getenv('TOSROOT')+'/tools/scripts/PowerTOSSIM/postprocess.py --sb=1 --em '+os.getenv('TOSROOT')+'/tools/scripts/PowerTOSSIM/mica2_energy_model.txt '+inputFile+' > '+outputFile)
	else:
		exitVal = os.system(os.getenv('TOSROOT')+'/tools/scripts/PowerTOSSIM/postprocess.py --sb=0 --em '+os.getenv('TOSROOT')+'/tools/scripts/PowerTOSSIM/mica2_energy_model.txt '+inputFile+' > '+outputFile)

	if (exitVal!=0):
		logger.warning('Error during power tossim energy calculation for query '+desc)	
		print '************** Error during power tossim energy calculation for query '+desc
	else:
		logger.info('Power tossim energy calculation successful for query '+desc)	
		print 'Power tossim energy calculation successful for query '+desc

#given the output of a PowerTossim script with total energy consumed, computes the total power used
#returns: (possibly with the LED toatls removed)
#sumPower: The sum of the total of all motes
#maxPower: The maximun total of any one mote
#averagePower: The average total of all motes
#radioPower: The sum of the radio for all motes
#cpu_cyclePower: The sum of the cpy_cycle for ll motes
def computeEnergyConsumed(totalEnergyConsumptionFile, optIgnoreLedPower):
	pattern = re.compile('Mote \d+, Total energy: (\d+\.\d+)')
	ledPattern = re.compile('Mote \d+, leds total: (\d+\.\d+)')
	radioPattern = re.compile('Mote \d+, radio total: (\d+\.\d+)')
	cpu_cyclePattern = re.compile('Mote \d+, cpu_cycle total: (\d+\.\d+)')
	sumPower = 0
	countPower = 0
	maxPower = 0
	ledPower = 0
	radioPower =0
	cpu_cyclePower = 0;
	
	for line in fileinput.input([totalEnergyConsumptionFile]):
		if optIgnoreLedPower:
			m = ledPattern.match(line)
			if m:
				ledPower = float(m.group(1))
		m = radioPattern.match(line)
		if m:
			radioPower += float(m.group(1))
		m = cpu_cyclePattern.match(line)
		if m:
			cpu_cyclePower += float(m.group(1))
		m = pattern.match(line)
		if m:
			power = float(m.group(1))-ledPower
			sumPower += power
			countPower += 1
			if (maxPower < power):
				maxPower = power
		
	averagePower = float(sumPower / countPower)
	
	return (sumPower, maxPower, averagePower, radioPower, cpu_cyclePower)


#Runs a tinyDb query writing the results in the indicated directory
def runTinyDBQuery(java5Exe, tossimParams, tossimOutputFile, query, doCpuProfiling):
	#Invoke the tinyDB pc-side
	os.chdir(os.getenv('TOSROOT')+'/tools/java')
	print '\nRunning TinyDB Query: '+str(query)+'\n'
	java5Exe = java5Exe.replace('\\','')	
	if (not java5Exe.endswith('.exe')):
		java5Exe = java5Exe + '.exe'
	if (query==None):
		os.spawnv(os.P_NOWAIT, java5Exe, ['java','net.tinyos.tinydb.TinyDBMain','-text','-sim'])
	else:	
		os.spawnv(os.P_NOWAIT, java5Exe, ['java','net.tinyos.tinydb.TinyDBMain','-text','-sim', '-run', query])

	#Invoke the tinyDB mote side
	os.chdir(os.getenv('TOSROOT')+'/apps/TinyDBApp/build/pc/')
	if (doCpuProfiling):
		#WARNING: We Never got this working correctly
		tossimExecutable = './a.out -cpuprof' 	
	else:
		tossimExecutable = './main.exe' 
	
	exitVal = os.system(tossimExecutable+' '+string.join(tossimParams,' ')+' > '+tossimOutputFile)

	#Kill the tinyDB pc-side
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


