#!/usr/bin/python
import StatLib, GraphData, UtilLib, sys, CSVLib
#assumes, for each scenario, that fgsnee data comes first, the qosa stuff
#scenario type data must match scenario generation

rValues = [1, 6, 30]
pcSources = [50, 100]

#queryDuration in seconds
#alpha in ms
#total Energy in Joules
def normalizeEnergyValue(queryDuration, alpha, beta, totalEnergy):
	return queryDuration * totalEnergy / (alpha/1000.0*beta)

def getVal(val, q, resultLine):
	if (q=='total-energy'):
		totalEnergy = val
		val = normalizeEnergyValue(queryDuration, float(resultLine['alpha-ms']), float(resultLine['beta']), float(totalEnergy))
	elif q=='alpha-ms' or q=='delta-ms':
               #either delivery time or acquisition interval
		val = float(val)/1000.0
	return val

def calculateImprovement(relevantMetric, metricValue, fgMetricValue):
		if relevantMetric in ['alpha-ms', 'delta-ms', 'total-energy']:
			improvement = (fgMetricValue - metricValue)/fgMetricValue * 100
		elif relevantMetric=='network-lifetime':
			improvement = ((metricValue / fgMetricValue) * 100) - 100
		return improvement

resultsFile = sys.argv[1]

qosMetrics = ['alpha-ms', 'delta-ms', 'total-energy', 'network-lifetime']
qosa = ['none', 'routing', 'where', 'when', 'all']

optGoals = ['min-acq', 'min-delivery', 'min-energy', 'max-lifetime']
goal2Metric = {'min-acq' : 'alpha-ms', 'min-delivery' : 'delta-ms', 'min-energy' : 'total-energy', 'max-lifetime' : 'network-lifetime'}

scenarioTypes = {}
scenarioTypeId = 0

for rValue in rValues:
	for pcSource in pcSources:
		scenarioTypeKey = "r"+str(rValue)+"_pc"+str(pcSource)
		scenarioTypes[scenarioTypeKey] = scenarioTypeId
		scenarioTypeId += 1

queryDuration =  UtilLib.monthsToSeconds(6)

counts = {}
sums = {}
graphs = {}

colNames = None
first = True

fgvals = {} #stores qos metrics for current scenario

for line in open(resultsFile, 'r'):
	if first:
		colNames = CSVLib.colNameList(line)
		first = False
		continue
	resultLine = CSVLib.line2Dict(line, colNames)

	if int(resultLine['exitCode'])!=0:
		continue

	qa = resultLine['qosa']
	if qa=="none":
		for q in qosMetrics:
			val = resultLine[q]
			fgvals[q] = float(getVal(val, q, resultLine))
	else:
		optgoal = resultLine['optgoal']
		relevantMetric = goal2Metric[optgoal]
		rValue = resultLine['rValue']
		pcSources = resultLine['percentSources']
		scenarioTypeKey = "r"+str(rValue)+"_pc"+str(pcSources)
		s = scenarioTypes[scenarioTypeKey]
		key = relevantMetric+"_"+str(s)+"_"+qa

		val = resultLine[relevantMetric]
		metricValue = float(getVal(val, relevantMetric, resultLine))

		if not (key in counts):
			counts[key] = 0
			sums[key] = 0
		counts[key] += 1
		
		improvement = calculateImprovement(relevantMetric, metricValue, fgvals[relevantMetric])
		sums[key] += improvement

for q in qosMetrics:
	if q=='alpha-ms':
		#xAxisPoints, lineLabels, xLabel, yLabel = None, title = None
		graph = GraphData.GraphData(range(0,len(scenarioTypes)), qosa[1:], " ", "Acquisition Interval (s)", "Percentage Improvement")
	elif q=='delta-ms':
		graph = GraphData.GraphData(range(0,len(scenarioTypes)), qosa[1:], " ", "Delivery Time (s)", "Percentage Improvement")
	elif q=='total-energy':
		graph = GraphData.GraphData(range(0,len(scenarioTypes)), qosa[1:], " ", "Average Total Energy (J)", "Percentage Improvement")
	elif q=='network-lifetime':
		graph = GraphData.GraphData(range(0,len(scenarioTypes)), qosa[1:], " ", "Average Lifetime (days)", "Percentage Improvement")

	for s in scenarioTypes.keys():
		for qa in qosa[1:]:

			sid = scenarioTypes[s]
			key = q+"_"+str(sid)+"_"+qa
			if key in counts:
				plotValue = sums[key]/counts[key]
			else:
				plotValue = '?'

			print key + " >>> " + str(plotValue)
			graph.addPoint(qa, sid, plotValue)
		
	plotId = "exp3_" + q 
	graph.generatePlotFile(plotId + ".txt", False)

	gnuPlotExe = 'gnuplot'		
	graph.plotGraph(plotId, "histogram", gnuPlotExe, False, 'top right')


