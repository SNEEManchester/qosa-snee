import Globals, os, sys, logging, re, fileinput, time, string, ExperimentLib, GraphData, AvroraLib 
 
import xml.dom.minidom
from xml.dom.minidom import Node

#File to which the log information will be written
#There will be one in the directory for each experiment
logger = logging.getLogger('experiment')

#queryInstanceRoot is the directory that hold a data for a single point on the graph
#queryPlanDir is sub directory that hold all the informational SNEEql output
#nescDir is the sub directory that hold all the nesc code including the compiled version
#OutputDir is the sub directory that hold all the output of Tossim or Avrora
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


#sets up the various graphs plotted for experiments 1,3 and 4
#SEE:addtoEnergyGraphs(..)
#NOTE: Layout of graphs used in paper is different to this.
def setupEnergyGraphs(xLabel, xAxisPoints, lineLabels, extraText):

	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph, energyGraph, workingGraph

	#graph to output total energy consumed by network
	totalGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	totalGraph.yLabel = 'Network Total energy consumption (J)'
	totalGraph.setTitle (xLabel + ' vs. Network Total Energy Consumption', extraText)

	#graph to output average energy consumed by network
	averageGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	averageGraph.yLabel = 'Average node energy consumption (J)'
	averageGraph.setTitle (xLabel + ' vs. Average Node Energy Consumption', extraText)
	
	#graph to output max energy consumed by any node network
	maxGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	maxGraph.yLabel = 'Maximum node energy consumption (J)'
	maxGraph.setTitle (xLabel + ' vs. Maximum Node Energy Consumption', extraText)

	#graph to output lifetime of all nodes
	lifetimeGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	lifetimeGraph.yLabel = 'Lifetime of all nodes in days'
	lifetimeGraph.setTitle (xLabel + ' vs. Lifetime with battery of '+str(Globals.batteryEnergy)+' Joules', extraText)

	#graph to output total energy consumed by radio
	radioGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	radioGraph.yLabel = 'Radio Total energy consumption (J)'
	radioGraph.setTitle (xLabel + ' vs. Radio Total Energy Consumption', extraText)

	#graph to output total energy consumed by cpu_cycle
	cpu_cycleGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	cpu_cycleGraph.yLabel = 'cpu_cycle Total energy consumption (J)'
	cpu_cycleGraph.setTitle (xLabel + ' vs. cpu_cycle Total Energy Consumption', extraText)

	#graph to output total energy consumed by radio
	packetGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	packetGraph.yLabel = 'Number of Radio Packets'
	packetGraph.setTitle (xLabel + ' vs. Number of Radio Packets', extraText)

	#graph to output working energy consumed by network
	workingGraph = GraphData.GraphData(xAxisPoints,lineLabels,xLabel)
	workingGraph.yLabel = 'Network Total active and transmit energy consumption (mJ)'
	workingGraph.setTitle  (xLabel + ' vs. Active and Transmit Energy Consumption', extraText)

	newXAxisPoints = []
	for yValue in lineLabels:
		for xValue in xAxisPoints:
			newXAxisPoints = newXAxisPoints + [str(xValue)+str(yValue)]
	energyGraph = GraphData.GraphData(newXAxisPoints,["Sensor","CPU","Radio","Other"],xLabel)
	energyGraph.yLabel = 'Enery used'
	energyGraph.setTitle  (xLabel + ' vs. Energy Used', extraText)


#Converts the output of avrora into values to become points on the graphs
#totalGraph The sum of the total joules used in each site
#averageGraph The average of the joules used in eahc site
#maxGraph The total joules used by the site with the highest total 
#lifetimeGraph Lifetime of the network calculated at the battery/ max from above
#radioGraph The sum of the radio joules for each site
#cpu_cycleGraph The sum of the cpu_cycles joules for each site. SEE: Globals.CountCpuCycles
#packetGraph: The sum of the Pakcets counted on all sites
def addtoEnergyGraphs(yValue,xValue,inputfile):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph, energyGraph, logger

	print'************ Adding '+str(xValue)
	
	#Get the nergy used
	totalEnergy, maxEnergy, averageEnergy, radioEnergy, cpu_cycleEnergy, sensorEnergy, otherEnergy = AvroraLib.computeEnergyConsumed(inputfile)
	packetCount = AvroraLib.getPacketCount()
	workingEnergy = AvroraLib.getWorkingEnergy()
			
	#add point to the graphs
	totalGraph.addPoint(yValue,xValue,str(totalEnergy),logger)
	logger.info('yValue= '+str(yValue)+' xValue= '+str(xValue) +' total energy= '+str(totalEnergy))
	
	maxGraph.addPoint(yValue,xValue,str(maxEnergy),logger)
	averageGraph.addPoint(yValue, xValue, str(averageEnergy),logger)
	radioGraph.addPoint(yValue,xValue,str(radioEnergy),logger)
	cpu_cycleGraph.addPoint(yValue,xValue,str(cpu_cycleEnergy),logger)
	workingGraph.addPoint(yValue,xValue,str(workingEnergy),logger)
	
	secondLifetime = Globals.batteryEnergy / (maxEnergy/(Globals.simulationDuration)) 
	dayLifetime = secondLifetime / (24*60*60)
	lifetimeGraph.addPoint(yValue, xValue, str(dayLifetime),logger)
	packetGraph.addPoint(yValue, xValue, packetCount,logger)

	energyGraph.addPoint("Sensor",str(xValue)+str(yValue),str(sensorEnergy),logger)
	#print str(xValue)+str(yValue)+"sensorEnergy "+str(sensorEnergy)
	energyGraph.addPoint("CPU",str(xValue)+str(yValue),str(cpu_cycleEnergy),logger)
	#print str(xValue)+str(yValue)+"cpu_cyclePower "+str(cpu_cycleEnergy)
	energyGraph.addPoint("Radio",str(xValue)+str(yValue),str(radioEnergy),logger)
	#print str(xValue)+str(yValue)+"radioPower "+str(radioEnergy)
	energyGraph.addPoint("Other",str(xValue)+str(yValue),str(otherEnergy),logger)
	#print str(xValue)+str(yValue)+"otherEnergy "+str(otherEnergy)

#Generates txt files used to plot graphs - Including versions used in Paper
#Generates gif and eps version of the graphs 
#SEE:addtoEnergyGraphs(..)
def generateEnergyGraphs(graphType, experiment,numericXValue):
	global totalGraph, averageGraph, maxGraph, lifetimeGraph, radioGraph, cpu_cycleGraph, packetGraph

	exText = 'Exp'+str(experiment)
	#generate the graphs
	totalGraph.generatePlotFile(currentExperimentRoot+exText+'totalEnergy.txt',numericXValue, logger)
	totalGraph.plotGraph(currentExperimentRoot+exText+'totalEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)
	maxGraph.generatePlotFile(currentExperimentRoot+exText+'maxEnergy.txt',numericXValue, logger)
	maxGraph.plotGraph(currentExperimentRoot+exText+'maxEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right',logger)
	averageGraph.generatePlotFile(currentExperimentRoot+exText+'averageEnergy.txt',numericXValue, logger)
	averageGraph.plotGraph(currentExperimentRoot+exText+'averageEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)
	lifetimeGraph.generatePlotFile(currentExperimentRoot+exText+'lifetime.txt',numericXValue, logger)
	lifetimeGraph.plotGraph(currentExperimentRoot+exText+'lifetime',graphType,Globals.gnuPlotExe, numericXValue, 'top left', logger)
	radioGraph.generatePlotFile(currentExperimentRoot+exText+'radioEnergy.txt',numericXValue, logger)
	radioGraph.plotGraph(currentExperimentRoot+exText+'radioEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)
	cpu_cycleGraph.generatePlotFile(currentExperimentRoot+exText+'cpu_cycleEnergy.txt', numericXValue, logger)
	cpu_cycleGraph.plotGraph(currentExperimentRoot+exText+'cpu_cycleEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)
	packetGraph.generatePlotFile(currentExperimentRoot+exText+'tx_count.txt',numericXValue, logger)
	packetGraph.plotGraph(currentExperimentRoot+exText+'tc_count',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)
	energyGraph.generatePlotFile(currentExperimentRoot+exText+'energy.txt',False, logger)
	energyGraph.plotGraph(currentExperimentRoot+exText+'energy','histograms',Globals.gnuPlotExe, numericXValue, 'top right', logger)
	workingGraph.generatePlotFile(currentExperimentRoot+exText+'workingEnergy.txt',numericXValue, logger)
	workingGraph.plotGraph(currentExperimentRoot+exText+'workingEnergy',graphType,Globals.gnuPlotExe, numericXValue, 'top right', logger)

#Converts all the script parameters into SNEEql parmeters
def generateSNEEqlParams(query, queryInstanceRoot, aqRate, maxBufferingFactor, maxDeliveryTime, simulationDuration, mica2):
	#Make sure at least some data is delivered
	runTime = simulationDuration * 1000
	deliveryTime = min (runTime, maxDeliveryTime) 
	
	#SNEEqlParams = ['-query='+Globals.SNEEqlDir + Globals.queryExtension+query]
	#SIGMOD addition
	SNEEqlParams = ['-iniFile="'+Globals.SNEEqlDir + Globals.inputExtension +'SNEEql.ini'+'"']
	SNEEqlParams+= ['-display-graphs=False']
	SNEEqlParams+= ['-output-root-dir="'+queryInstanceRoot+'"']
	SNEEqlParams+= ['-qos-acquisition-rate='+str(aqRate)]
	SNEEqlParams+= ['-qos-max-buffering-factor='+str(maxBufferingFactor)]				
	SNEEqlParams+= ['-qos-response-time='+str(deliveryTime)]
	SNEEqlParams+= ['-qos-query-duration='+str(runTime)]
	SNEEqlParams+= ['-network-topology-file="'+Globals.tossimNetworkFilePath+'"']
	SNEEqlParams+= ['-schema-file="'+Globals.schemaFilePath+'"']
	SNEEqlParams+= ['-nesc-use-cc1000-radio=false']
	SNEEqlParams+= ['-nesc-control-radio-off=false']				
	SNEEqlParams+= ['-nesc-synchronization-period='+str(Globals.nescSynchronizationPeriod)]
	
	if (Globals.snooze):
		SNEEqlParams+= ['-nesc-do-snooze=true']
	else:
		SNEEqlParams+= ['-nesc-do-snooze=false']
	SNEEqlParams+= ['-nesc-cpu-go-active=false']
	
	if (Globals.deliverLast):
		SNEEqlParams+= ['-nesc-deliver-last=true']
	else:
		SNEEqlParams+= ['-nesc-deliver-last=false']
	
	if mica2:
		SNEEqlParams+= ['-nesc-generate-mote-code=true']
		SNEEqlParams+= ['-nesc-generate-tossim-code=false'] 
		SNEEqlParams+= ['-nesc-mica2=true']
	else:	
		SNEEqlParams+= ['-nesc-generate-mote-code=false']
		SNEEqlParams+= ['-nesc-generate-tossim-code=true'] 
		SNEEqlParams+= ['-nesc-mica2=false']

	return SNEEqlParams  
	
#Converts the Script parameters into Tossim Parameters
#adds -b=1 Which sets the motes to start up within one second of each other. Lowest possible value
def generateTossimParameters(extra, file):	
	if (Globals.simulationSeed == None):
		tossimParams = []
	else:
		tossimParams = ['-seed='+str(Globals.simulationSeed)] 
	tossimParams.extend(['-t='+str(Globals.simulationDuration)])
	tossimParams.extend(['-r=lossy','-rf='+Globals.tossimNetworkFilePath])
	tossimParams.extend(['-b=1'])
	if len(extra) > 0:
		tossimParams.extend(extra)
	tossimParams.extend([str(Globals.numNodes),' > '+file])
	return tossimParams

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

#Runs the full experiment 1
#Sets up the logger and graphs
#Loops through all queries for all acquitsion rates generaating data
#Generates the result txt files and graphs
def doExperiment1():

	#set up the logger
	startLogger(currentExperimentRoot)

	if not os.path.isdir(currentExperimentRoot):
		os.makedirs(currentExperimentRoot)
	Globals.recordParameters(currentExperimentRoot,logger)

	setupEnergyGraphs('Acquisition interval (ms)',Globals.exp1AqRates, Globals.queries,'Max buffering factor: '+str(Globals.exp1MaxBufferingFactor))

	for aqRate in Globals.exp1AqRates:
		
		for query in Globals.queries:	
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs(currentExperimentRoot, query, 'aqRate', aqRate)

			#run the SNEEql query compiler
			SNEEqlParams = generateSNEEqlParams(Globals.queryDir+query, queryInstanceRoot, aqRate, Globals.exp1MaxBufferingFactor, sys.maxint, Globals.simulationDuration, True)

			exitVal = ExperimentLib.compileQuery(SNEEqlParams, desc, logger)
			if (exitVal != 0):
				continue

			#compile the nesC code
			if not os.path.exists(outputDir):
				os.makedirs(outputDir)
				print "made "+outputDir
			else:
				print "Exists "+outputDir

			ExperimentLib.compileNesC(nescDir, desc, logger)

			AvroraLib.runSimulation(nescDir, outputDir, desc, logger)
		
			if (os.path.exists(outputDir+'/avrora.txt')):
				addtoEnergyGraphs(query,aqRate,outputDir+'/avrora.txt')
				
			else:	
				print 'Did not find '+outputDir+'/avrora.txt'
										
	#generate the graphs
	#generateEnergyGraphs('linespoints',1,True)	
	
	energyGraph.generatePlotFile(currentExperimentRoot+'Exp1energy.txt',False, logger)
	os.chdir(currentExperimentRoot)
	energyGraph.plotFig14(Globals.gnuPlotExe, logger)
	lifetimeGraph.generatePlotFile(currentExperimentRoot+'Exp1lifetime.txt',True, logger)
	os.chdir(currentExperimentRoot)
	lifetimeGraph.plotFig15(Globals.gnuPlotExe, logger)



	
#Runs the full experiment 2
#Sets up the logger and graphs
#Loops through all queries for all buffering factors
#NOTE: Data is computed from the SNEEql with compiling and simulation the NesC
#Generates the result txt files and graphs
def doExperiment2():

	#set up the logger
	startLogger(currentExperimentRoot)

	#This experiment does not have a post-processing stage	
	if not os.path.isdir(currentExperimentRoot):
		os.makedirs(currentExperimentRoot)
	Globals.recordParameters(currentExperimentRoot,logger)

	#graph to output total energy consumed by network
	agendaLengthGraph = GraphData.GraphData(Globals.exp2BufferingFactors,Globals.queries,'Buffering factor specified')
	agendaLengthGraph.yLabel = 'Agenda Averaga Result Delivery Time (ms)'
	agendaLengthGraph.setTitle ('Buffering factor vs. Agenda Average Result Delivery Time',"")

	for bFactor in Globals.exp2BufferingFactors:		
	
		for query in Globals.queries:	

			
			#run the SNEEql query compiler
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs(currentExperimentRoot, query, 'bufFac', bFactor)
						SNEEqlParams = generateSNEEqlParams(Globals.queryDir+query, queryInstanceRoot, Globals.exp2AqRate, bFactor, sys.maxint, sys.maxint/1000, True)

			#run the query compiler
			exitVal = ExperimentLib.compileQuery(SNEEqlParams, desc, logger)
			if (exitVal != 0):
				continue

			#Use the SNEEql output to calculate the average time
			#def getAverageDeliveryTimeAgendaMakespan(queryPlanDir, acRate, desc, logger):

			averageDeliveryTime = ExperimentLib.getAverageDeliveryTimeAgendaMakespan(queryPlanDir,Globals.exp2AqRate, desc, logger)

			#add point to the graph input data file
			agendaLengthGraph.addPoint(query, str(bFactor), str(averageDeliveryTime),logger)
			
	#generate the graph
	agendaLengthGraph.generatePlotFile(currentExperimentRoot+'Exp2agendaLength.txt',True,logger)
	os.chdir(currentExperimentRoot)
	agendaLengthGraph.plotFig16(Globals.gnuPlotExe, logger)		
	

#Runs the full experiment 3
#Sets up the logger and graphs
#Loops through all queries for all delivery times
#Generates the result txt files and graphs	
def doExperiment3():
	
	#set up the logger
	startLogger(currentExperimentRoot)

	if not os.path.isdir(currentExperimentRoot):
		os.makedirs(currentExperimentRoot)
	Globals.recordParameters(currentExperimentRoot,logger)
		
	#graph to output total energy consumed by network
	setupEnergyGraphs('Maximum result delivery time',Globals.exp3DeliveryTimes,Globals.queries,'Acquistion rate: '+str(Globals.exp3AqRate))
	
	for deliveryTime in Globals.exp3DeliveryTimes:
		
		for query in Globals.queries:	

			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'deliveryTime', deliveryTime)
			
			#run the SNEEql query compiler
			SNEEqlParams = generateSNEEqlParams(Globals.queryDir+query, queryInstanceRoot, Globals.exp3AqRate, sys.maxint, deliveryTime, Globals.simulationDuration, True)

			exitVal = ExperimentLib.compileQuery(SNEEqlParams, desc, logger)
			if (exitVal != 0):
				continue

			#compile the nesC code
			if not os.path.exists(outputDir):
				os.makedirs(outputDir)
				print "made "+outputDir
			else:
				print "Exists "+outputDir
			ExperimentLib.compileNesC(nescDir, desc, logger)

			AvroraLib.runSimulation(nescDir, outputDir, desc, logger)
			
			if (os.path.exists(outputDir+'/avrora.txt')):
				addtoEnergyGraphs(query,deliveryTime,outputDir+'/avrora.txt')
						
			else:	
				print 'Did not find '+outputDir+'/avrora.txt'	
					
	lifetimeGraph.generatePlotFile(currentExperimentRoot+'Exp3lifetime.txt',True,logger)
	os.chdir(currentExperimentRoot)
	lifetimeGraph.plotFig17(Globals.gnuPlotExe, logger)		

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

def doExperiment4():

	delGraph = GraphData.GraphData(Globals.exp4Queries.keys(),Globals.exp4BufferingFactors+['TinyDB'],'xLabel')
	delGraph.yLabel = 'Deliver'
	delGraph.setTitle ("Deliver","")
	radioGraph = GraphData.GraphData(Globals.exp4Queries.keys(),Globals.exp4BufferingFactors+['TinyDB'],'xLabel')
	radioGraph.yLabel = 'Radio'
	radioGraph.setTitle  ("Radio","")
	ratioGraph = GraphData.GraphData(Globals.exp4Queries.keys(),Globals.exp4BufferingFactors+['TinyDB'],'xLabel')
	ratioGraph.setTitle = ("Radio to Deliver Ratio","")
	ratioGraph.yLabel = 'YLabel'
	
	startLogger(currentExperimentRoot)

	for query in Globals.exp4Queries.keys():
		print query

	for query in Globals.exp4Queries.keys():
		for bf in Globals.exp4BufferingFactors:
	
			queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'bufFactor', bf)
			outputFile = outputDir +'tossim.trace'
			
			#run the query compiler
			SNEEqlParams = generateSNEEqlParams(Globals.queryDir+query, queryInstanceRoot, Globals.exp4AqRate, bf, sys.maxint, Globals.simulationDuration, False)
	
			exitVal = ExperimentLib.compileQuery(SNEEqlParams, desc, logger)
				
			#compile the nesC code
			ExperimentLib.compileNesC(nescDir, desc, logger)
	
			#run tossim simulation
			tossimParams  = generateTossimParameters([], outputFile)
			print "bf="+str(bf)

			if not os.path.exists(outputDir):
				os.makedirs(outputDir)
				print "made "+outputDir
			else:
				print "Exists "+outputDir
			ExperimentLib.runTossimSimulation(nescDir, tossimParams, desc, logger)
			
			if os.path.exists(outputFile):
				radio,deliver = countRec(outputFile )
			
				print "radio ="	+str(radio)+ "deliver = "+str(deliver)
				radioGraph.addPoint(str(bf),query,str(radio),logger)
				delGraph.addPoint(str(bf),query,str(deliver),logger)
				if (deliver > 0):
					print (radio+0.0)/deliver
					ratioGraph.addPoint(str(bf),query,str((radio+0.0)/deliver),logger)
				else:
					print "no ratio"
				
		queryInstanceRoot, queryPlanDir, nescDir, outputDir, desc = getExpDescAndDirs (currentExperimentRoot, query, 'TinyDB', 1)
		outputFile = outputDir +'tossim.trace'
		if not os.path.exists(outputDir):
			os.makedirs(outputDir)
			print "made "+outputDir
		else:
			print "Exists "+outputDir
		#continue	

		tinyDBQuery = str(Globals.exp4Queries[query] % str(Globals.exp4AqRate/1000*1024))
		print tinyDBQuery
			
		ExperimentLib.runTinyDBQuery (tinyDBQuery, outputFile, logger)	
		radio,deliver = readTinyDB(outputFile)
	
		print "radio ="	+str(radio)+ "deliver = "+str(deliver)
		radioGraph.addPoint('TinyDB',query,str(radio),logger)
		delGraph.addPoint('TinyDB',query,str(deliver),logger)
		if (deliver > 0):
			print (radio+0.0)/deliver
			ratioGraph.addPoint('TinyDB',query,str((radio+0.0)/deliver),logger)
		else:
			print "no ratio"
	
	radioGraph.generatePlotFile(currentExperimentRoot+'Exp4Radio.txt',False, logger)
	os.chdir(currentExperimentRoot)
	radioGraph.plotFig18(Globals.gnuPlotExe, logger)		
	
	#delGraph.generatePlotFile(currentExperimentRoot+'Deliver.txt',False, logger)
	#delGraph.plotGraph(currentExperimentRoot+'Deliver','histogram',Globals.gnuPlotExe, False, 'top right', logger)
	#ratioGraph.generatePlotFile(currentExperimentRoot+'Ratio.txt',False, logger)
	#ratioGraph.plotGraph(currentExperimentRoot+'Ratio','histogram',Globals.gnuPlotExe, False, 'top right', logger)

#Do one or more experiment depending on the parameters
def doCompleteExperiments():

	global currentExperimentRoot

	for experiment in Globals.experimentList:

		if Globals.versionString:
			currentExperimentRoot = Globals.experimentalLogRoot+'/'+Globals.versionString+'/experiment'+str(experiment)+'/'+Globals.timeStamp+'/'
		else:	
			currentExperimentRoot = Globals.experimentalLogRoot+'/experiment'+str(experiment)+'/'+Globals.timeStamp+'/'
	
		print experiment
		if experiment=='1':
			doExperiment1()
		elif experiment=='2':
			doExperiment2()
		elif experiment=='3':
			doExperiment3()
		elif experiment=='4':
			doExperiment4()
		else:
			print 'Experiment {'+str(experiment)+'} not recognised'
	
def main(): 
		
	#parse the command-line arguments
	Globals.setupGlobals(sys.argv[1:]) 
	
	numNodes = ExperimentLib.getNumNodes(Globals.tossimNetworkFilePath)
	Globals.numNodes = numNodes

	#test high enough version of gnuplot is installed
	testGraph = GraphData.GraphData('null', 'null', 'null')
	testGraph.testGnuPlotVersion(Globals.gnuPlotExe)
	
	#set relevant environment variables
	print Globals.SNEEqlDir+Globals.classExtension
	#sys.exit(2);
	os.putenv('CLASSPATH',Globals.javaClassPath+';'+Globals.SNEEqlDir+Globals.classExtension+';'+Globals.avroraPath+';'+Globals.antlrPath+';'+os.getenv('CLASSPATH'))
	#os.putenv('CLASSPATH',Globals.javaClassPath+';'+Globals.SNEEqlDir+';'+os.getenv('CLASSPATH'))
	#os.putenv('CLASSPATH','.;C:\\Jars\\antlr-2.7.5.jar;C:\\Jars\\SNEEql.jar;C:\\Jars\\avrora-beta-1.6.0.jar'+os.getenv('CLASSPATH'))
	#os.putenv('CLASSPATH',Globals.javaClassPath+';'+os.getenv('CLASSPATH'))
	os.putenv('DBG', 'usr1')
	
	#ExperimentLib.compileQueryGlobals.imizer(Globals.SNEEqlSourceDir, Globals.javaExe, Globals.SNEEqlDir+Globals.classExtension, Globals.JavaClassPath, logger)
	doCompleteExperiments()
	
if __name__ == "__main__":
    main()
	
