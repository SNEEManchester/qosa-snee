import os,string,re,fileinput,logging,experimentLib 
	
def runTinyDBQuery (query, clientFile,serverFile):	

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
	tossimParams = ['-gui','-b=1','-p','-r=lossy','-rf=SIGMOD_network_topology.nss','-t=360','10']
	tossimP = string.join(tossimParams,' ')
	print "tossimParams="+tossimP
	runStr = tossimExecutable+' '+tossimP+' >'+serverFile
	print "running:"+runStr
	exitVal = os.system(runStr)

	os.system('taskkill -f -im java.exe')


def readData(inputFile):
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

def SNEE():	
	global optSimulationDuration,logger
	currentExperimentRoot = 'c:/tmp/data/'
	query = 'Q0.txt'
	deliveryTime = 3000
	optQueryDir = ''
	aqRate = 3000
	optUseCC1000Radio = True
	optControlRadioOff = True
	optSnooze = True
	optCountCpuCycles = False
	desc = 'Test'
	queryCompilerDefaultParams = ['-nesc-generate-mote-code=True','-display-graphs=False']
	optJava5exe = '/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java'
	optSNEEqlRootDir = 'c:/SNEEql/'
	numNodes = 10
	
	#startLogger(currentExperimentRoot)
	
	queryInstanceRoot, queryPlanDir, nescDir, powerTossimDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'deliveryTime', deliveryTime)
			
	#run the query compiler
	queryCompilerParams = generateQueryCompilerParams(optQueryDir+query, queryInstanceRoot, aqRate, sys.maxint, deliveryTime,optUseCC1000Radio,optControlRadioOff, optSimulationDuration, optSnooze)
	exitVal = experimentLib.compileQuery(queryCompilerDefaultParams, queryCompilerParams, optSimulationDuration, queryPlanDir, desc, optJava5exe, optSNEEqlRootDir, logger)
	#compile the nesC code
	os.makedirs(powerTossimDir)
	experimentLib.compileQueryPlan(nescDir, desc, optCountCpuCycles, logger)

	#run tossim simulation
	tossimParams  = generateTossimParameters([], numNodes, powerTossimDir+'tossim.trace')
	experimentLib.runTossimSimulation(nescDir, tossimParams, desc, optCountCpuCycles, logger)



def main(): 
	optSimulationDuration = 60
	clientFile = 'c:/tmp/data/client.txt'
	serverFile = 'c:/tmp/data/server.txt'
	#query = "select light,nodeid from sensors sample period 3076"
	query = "select light,nodeid from sensors where nodeid >= 7 sample period 3076"
	query = "select AVG(light) from sensors where nodeid >= 7 sample period 3076"

	os.putenv('DBG', 'usr1')
	#runTinyDBQuery (query, clientFile,serverFile)
	#readData (serverFile)
	SNEE()
	
if __name__ == "__main__":
    main()