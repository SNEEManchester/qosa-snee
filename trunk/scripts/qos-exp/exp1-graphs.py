#!/usr/bin/python
import StatLib, GraphData, sys, CSVLib, os

resultsFile = os.getcwd()+'/'+sys.argv[1]

qosaSNEEOptGoals = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
fgSNEEOptGoals = ['A', 'B', 'C'] 
optGoals = qosaSNEEOptGoals + fgSNEEOptGoals
qosMetrics = ['alpha-ms', 'delta-ms', 'beta', 'total-energy', 'network-lifetime', 'average-energy']
#Maps the QoS identifiers used in the experiments to those used the thesis
optGoalLabelMap = {'1' : '1', '2' : '2','3' : '3','4' : '4','5' : '5','6' : '6', '7' : '7','8' : '8','9' : '9','10' : '10','A' : 'A', 'B' : 'B', 'C' : 'C'}
optValidScenarioList = ['0', '1', '4', '5', '6', '7', '8', '12', '13', '15', '17', '18', '19', '25', '26'] #28 also works, removed to make 15 scenarios
optScenarioMap = {'0' : '1', '1': '2', '4' : '3', '5' : '4', '6' : '5', '7' : '6', '8' : '7', '12' : '8', '13' : '9', '15' : '10', '17' : '11', '18' : '12', '19' : '13', '25' : '14', '26' : '15'}

#maps QoSA-SNEE optGoal (NB: Not the label!) to most similar FG-SNEE optGoal 
mostSimilarFGSNEEGoal = {'1' : 'A', '2' : 'A', '3' : 'B', '4' : 'B', '5' : 'B', '6' : 'C', '7' : 'A', '8' : 'C', '9' : 'C', '10' : 'B'}

optDoBreakdown = True
optLatexOnly = False

counts = {}
sums = {}
graphs = {}

breakdown = {}

colNames = None
first = True

outfileCore = resultsFile.replace("-results.csv","")
d = outfileCore
if not os.path.exists(d):
	print 'create'
	os.makedirs(d)
os.chdir(d)

for line in open(resultsFile, 'r'):
		
	if first:
		colNames = CSVLib.colNameList(line)
		first = False
		continue
	resultLine = CSVLib.line2Dict(line, colNames)

	if int(resultLine['exitCode'])!=0:
		continue

	for q in qosMetrics:
		if resultLine['scenarioId'] in optValidScenarioList:
			key = resultLine['optgoal']+"_"+q
			#print key
			val = float(resultLine[q])

			if q=='alpha-ms' or q=='delta-ms':
                                #either delivery time or acquisition interval
				val = float(val)/1000.0

			if not (key in counts):
				counts[key] = 0
				sums[key] = 0
			counts[key] += 1
			sums[key] += float(val)
		
			if optDoBreakdown:
				breakdownKey = key+"_"+resultLine['scenarioId']
				breakdown[breakdownKey] = float(val)

#x-axis points
xAxisPoints = []
for o in qosaSNEEOptGoals:
	xAxisPoints += [optGoalLabelMap[o] + '  ' + mostSimilarFGSNEEGoal[optGoalLabelMap[o]]]
#print xAxisPoints

latexSource = ''
	
for q in qosMetrics:

	if q=='alpha-ms':
		#xAxisPoints, lineLabels, xLabel, yLabel = None, title = None
		yAxisLabel = "Acquisition interval (s)"
                yAxisSymbol = "$\\alpha$"
	elif q=='delta-ms':
		yAxisLabel = "Delivery Time (s)"
                yAxisSymbol = "$\\delta$"
	elif q=='total-energy':
		yAxisLabel = "6-month Total Network Energy Consumption (J)"
                yAxisSymbol = "6-month $\\epsilon$"
	elif q=='network-lifetime':
		yAxisLabel = "Lifetime (days)"
                yAxisSymbol = "$\\lambda$"
	elif q=='average-energy':
		yAxisLabel = "6-month Energy Consumption per Node (J)"
                yAxisSymbol = "$\\lambda$"                
	elif q=='beta':
		yAxisLabel = "Buffering factor"
                yAxisSymbol = "$\\beta$"

	lineLabels = ['QoSA-SNEE', 'FG-SNEE']
	#logscale
	if (q=='delta-ms'): #q=='total-energy' or q=='average-energy' or 
		graph = GraphData.GraphData(xAxisPoints, lineLabels, "Quality-of-Service Expectation", "Average "+yAxisLabel + " (log scale)", " ",logScaleY = True)
	else:
		graph = GraphData.GraphData(xAxisPoints, lineLabels, "Quality-of-Service Expectation", "Average "+yAxisLabel, " ",logScaleY = False)

	plotId = "fg-qosa_" + q
	gnuPlotExe = 'gnuplot'
 
	latexSource += "\n\\section{"+yAxisLabel+" Results}\n\\label{sec:breakdown-"+plotId+"}\n\n"
	if (q=='alpha-ms'):
		latexSource += "Graphs for QoS expectations 2, 3, 9, 10, A, B and C have an equality constraint over $\\alpha$, i.e., have a fixed-acquisition interval, and have been omitted from this section.\n\n"

	for o in qosaSNEEOptGoals:
		key = o+"_"+q
		#print key

		if key in counts:
			plotValue = sums[key]/counts[key]
		else:
			plotValue = '?'

		#print plotValue
		graph.addPoint('QoSA-SNEE', optGoalLabelMap[o]+'  '+mostSimilarFGSNEEGoal[optGoalLabelMap[o]], plotValue)

		key = mostSimilarFGSNEEGoal[o]+"_"+q

		if key in counts:
			plotValue = sums[key]/counts[key]
		else:
			plotValue = '?'

		#print plotValue 
		graph.addPoint('FG-SNEE', optGoalLabelMap[o]+'  '+mostSimilarFGSNEEGoal[optGoalLabelMap[o]], plotValue)		

       	if optDoBreakdown:
		for o in optGoals:
			key = o+"_"+q

			if key in counts:
				plotValue = sums[key]/counts[key]
			else:
				plotValue = '?'
			
			breakdownGraph = GraphData.GraphData(map(str,range(1,16))+['Avg'], " ", "Scenario", yAxisLabel, " ")
			for s in optValidScenarioList:
				breakdownKey = key+"_"+s
				if breakdownKey in breakdown:
					bPlotValue = breakdown[breakdownKey]
				else:
					bPlotValue = '?'
				breakdownGraph.addPoint(" ", optScenarioMap[s], bPlotValue)
			breakdownGraph.addPoint(" ", "Avg", plotValue)	

			breakdownPlotId = plotId + "_qos" + o 
			if not optLatexOnly:
				breakdownGraph.generatePlotFile(breakdownPlotId + ".txt", False)		
				breakdownGraph.plotGraph(breakdownPlotId, "histogram", gnuPlotExe, False, 'off')
				os.system('ps2pdf -dEPSCrop '+breakdownPlotId+'.eps')		

			if not (q=='alpha-ms' and o in ['2','3','9','10','A','B','C']):
				latexStr = '''

\\begin{center}
\\includegraphics[width=0.8\linewidth]{results/%s}
\\captionof{figure}[%s with QoS %s for %s .]{%s obtained for each scenario compiled against QoS %s.}\label{fig:eval-%s}
\\end{center}

'''

                                qe = '\\qaqe'
                                if o in ['A','B','C']:
                                       qe = '\\fgqe'
				latexSource += latexStr % (breakdownPlotId, yAxisSymbol, optGoalLabelMap[o], qe, yAxisLabel, optGoalLabelMap[o], breakdownPlotId)


		if not optLatexOnly:
			graph.generatePlotFile(plotId + ".txt", False)		
			graph.plotGraph(plotId, "histogram", gnuPlotExe, False, 'outside top right', False)
			os.system('ps2pdf -dEPSCrop '+plotId+'.eps')


		latexSource += '''

%%% Local Variables:
%%% mode: latex
%%% TeX-master: "main"
%%% End:
'''

		fo = open("fg-qosa-breakdown.tex", "w")
		fo.writelines(latexSource)
		fo.close()

