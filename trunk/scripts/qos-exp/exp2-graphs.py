#!/usr/bin/python
import GraphData, UtilLib, sys, CSVLib
#Assumes FG-snee stuff first

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

optGoals = ['min-acq', 'min-delivery', 'min-energy', 'max-lifetime']
qosMetrics = ['alpha-ms', 'delta-ms', 'total-energy', 'network-lifetime']
goal2Metric = {'min-acq' : 'alpha-ms', 'min-delivery' : 'delta-ms', 'min-energy' : 'total-energy', 'max-lifetime' : 'network-lifetime'}

queryDuration =  UtilLib.monthsToSeconds(6)

counts = {}
sums = {}

colNames = None
first = True

for q in qosMetrics:
	counts[q] = 0.0
	sums[q] = 0.0
			
for line in open(resultsFile, 'r'):
	if first:
		colNames = CSVLib.colNameList(line)
		first = False
		continue
	resultLine = CSVLib.line2Dict(line, colNames)

	if int(resultLine['exitCode'])!=0:
		continue

	if resultLine['qosa']=='none':
		fgvals = {}
		for q in qosMetrics:
			val = resultLine[q]
			val = getVal(val, q, resultLine)
			print resultLine
			print q
			fgvals[q] = float(val)

	if resultLine['qosa']=='all':

		optGoal = resultLine['optgoal']
		relevantMetric = goal2Metric[optGoal]
		metricValue = float(resultLine[relevantMetric])
		
		improvement = calculateImprovement(relevantMetric, metricValue, fgvals[relevantMetric])
		sums[relevantMetric] += improvement
		counts[relevantMetric] += 1



lineLabels = qosMetrics
#xAxisPoints, lineLabels, xLabel, yLabel = None, title = None
graph = GraphData.GraphData([" "], lineLabels, " ", "Percentage improvement", " ")

for q in qosMetrics:
	if q in counts:
		print q
		plotValue = sums[q]/counts[q]
	else:
		plotValue = '?'
	print plotValue
	graph.addPoint(q, " ", plotValue)
		
plotId = "exp2" 
graph.generatePlotFile(plotId + ".txt", False)

gnuPlotExe = 'gnuplot'		
graph.plotGraph(plotId, "histogram", gnuPlotExe, False, 'top right')


