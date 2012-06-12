#This module provides support for checking the tuple count for non-lossy queries

import re, sys, xml.dom.minidom, UtilLib

optExitOnTupleError = False

logger = None

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

def getOptNames():
	optNames = ["exit-on-tuple-error="]
	return optNames

def usage():
	print '\nFor the checkTupleCount library:'
	print '--exit-on-tuple-error=[True|False]\n\tdefault: ' + str(optExitOnTupleError)

def setOpts(opts):
	global optExitOnTupleError
	
	for o, a in opts:
		if (o == "--exit-on-tuple-error"):
			optExitOnTupleError = UtilLib.convertBool(a)
			continue			

#Returns the number of tuples per epoch for a particular stream
def getTuplesPerEpoch(schemaFilePath, streamName):
	dom = xml.dom.minidom.parse(schemaFilePath)
	schema = dom.getElementsByTagName("schema")[0]
	streams = schema.getElementsByTagName("stream")
	for stream in streams:
		if stream.getAttribute("name") == streamName:
			site = stream.getElementsByTagName("sites")[0]
			sites = site.childNodes[0].data.split(",")
			return len(sites)

#Returns the number of epochs expected for a simulation with a particular duration, aquisition interval and buffering factor
def getNumEpochsExpected(simulationDuration, acquisitionInterval, bufferingFactor):
	numEpochs = ((simulationDuration * 1000) // (acquisitionInterval * bufferingFactor)) * bufferingFactor
	return numEpochs

#Returns the evalEpoch that a DELIVER line in the result tuple file pertains to
def __getLineEpoch(line):
	m = re.search("DELIVER: \(evalEpoch=(\d+),", line)
	if (m != None):
		i = m.group(1)
		return int(i)
	else:
		return None

#Given a output trace file for either TOSSIM or Avrora, the expected number of tuples per epoch, the number of epochs expected for the duration of the simulation, and a dictionary with any exceptions, checks that the number of tuples in the trace file are as expected, flaggin up warnings when this is not the case.
def checkTupleCount(traceFilePath, tuplesPerEpoch, numEpochsExpected, exceptions = {}):
	
	tupleCount = []
	for i in range(0, numEpochsExpected):
		tupleCount.append(0)
	
	inFile =  open(traceFilePath)
	tmpStr = ""
	while 1:
		line = inFile.readline()
		if not line:
			break
		epoch = __getLineEpoch(line)
		if (epoch != None):
			if (epoch < numEpochsExpected):
				tupleCount[epoch] += 1
			else:
				reportWarning("Received tuple for epoch %d higher than numEpochsExpected=%d" % (epoch, numEpochsExpected))
	
	missing = False
	for i in range(0, numEpochsExpected):
		if (tupleCount[i] != tuplesPerEpoch):
			if (exceptions.has_key(i)):
				if (tupleCount[i] != exceptions.get(i)):
					reportWarning("*Epoch is an Exception* Expecting " + str(exceptions.get(i)) + " tuples for epoch " + str(i) + " and got " + str(tupleCount[i]))
					missing = True
			else:
				reportWarning("Expecting " + str(tuplesPerEpoch) + " tuples for epoch " + str(i) + " and got " + str(tupleCount[i]))
				missing = True
	if (not missing):
		report("All tuples present!")
	else:
		if optExitOnTupleError:
			sys.exit(2)	



#SELECT RSTREAM * FROM InFlow[NOW];

#SELECT myPres FROM (SELECT RSTREAM pressure AS myPres FROM InFlow[Now]);
def checkTupleCountQ0(traceFilePath, schemaFilePath, acquisitionInterval = 5000, bufferingFactor = 1, simulationDuration = 100):
	sourceTuplesPerEpoch = getTuplesPerEpoch(schemaFilePath, "InFlow")
	numEpochsExpected = getNumEpochsExpected(simulationDuration, acquisitionInterval, bufferingFactor)
	checkTupleCount(traceFilePath, sourceTuplesPerEpoch, numEpochsExpected)

#SELECT RSTREAM avg(pressure) FROM InFlow[Now];

#SELECT avgpres FROM (SELECT RSTREAM AVG(pressure) AS avgpres FROM InFlow[Now]);
def checkTupleCountQ2(traceFilePath, schemaFilePath, acquisitionInterval = 5000, bufferingFactor = 1, simulationDuration = 100):
	numEpochsExpected = getNumEpochsExpected(simulationDuration, acquisitionInterval, bufferingFactor)
	checkTupleCount(traceFilePath, 1, numEpochsExpected)

#SELECT  RSTREAM OutFlow.time, InFlow.pressure, OutFlow.pressure, OutFlow.id, InFlow.id
	#FROM    OutFlow[NOW], InFlow[AT now - 10 sec];
def checkTupleCountQ3temp(traceFilePath, schemaFilePath, acquisitionInterval = 5000, bufferingFactor = 1, simulationDuration = 100):
	inFlowTuplesPerEpoch = getTuplesPerEpoch(schemaFilePath, "InFlow")
	outFlowTuplesPerEpoch = getTuplesPerEpoch(schemaFilePath, "OutFlow")
	tuplesPerEpoch = inFlowTuplesPerEpoch * outFlowTuplesPerEpoch
	
	numEpochsExpected = getNumEpochsExpected(simulationDuration, acquisitionInterval, bufferingFactor)
	exceptions = {0:0, 1:0}
	checkTupleCount(traceFilePath, tuplesPerEpoch, numEpochsExpected, exceptions)

#SELECT  RSTREAM OutFlow.time, InFlow.pressure, OutFlow.pressure, OutFlow.id, InFlow.id
	#FROM    OutFlow[NOW], InFlow[AT now - 10 sec];
def checkTupleCountQ4temp(traceFilePath, schemaFilePath, acquisitionInterval = 5000, bufferingFactor = 1, simulationDuration = 100):
	sourceTuplesPerEpoch = getTuplesPerEpoch(schemaFilePath, "InFlow")
	numEpochsExpected = getNumEpochsExpected(simulationDuration, acquisitionInterval, bufferingFactor)
	exceptions = {0:0, 1:0}
	checkTupleCount(traceFilePath, sourceTuplesPerEpoch, numEpochsExpected, exceptions)

def checkResults(query, traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration):
	report("Checking tuple counts for query %s" % query)
	if (query == "Q0"):
		checkTupleCountQ0(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)
	elif (query == "QNest0"):
		checkTupleCountQ0(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)	
	elif (query == "Q2"):
		checkTupleCountQ2(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)	
	elif (query == "QNest2"):
		checkTupleCountQ2(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)	
	elif (query == "Q3temp"):
		checkTupleCountQ3temp(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)
	elif (query == "QNest3temp"):
		checkTupleCountQ3temp(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)
	elif (query == "Q4temp"):
		checkTupleCountQ4temp(traceFilePath, schemaFilePath, acquisitionInterval, bufferingFactor, simulationDuration)
	else:
		reportError("Query not found")