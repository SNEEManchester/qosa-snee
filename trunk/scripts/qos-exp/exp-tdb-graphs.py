#!/usr/bin/python
import StatLib, GraphData, sys, CSVLib, os

resultsFile = os.getcwd()+'/'+sys.argv[1]

alphas = map(str,[3,15,30,60,150,300,600,1000,3000])
qosMetrics = ['average-energy', 'network-lifetime', 'beta', 'delta-ms']
#Maps the QoS identifiers used in the experiments to those used the thesis
#alphaLabelMap = {'3' : '3s', '15' : '15s','30' : '30s','60' : '1 min','150' : '2:30 min','300' : '5 min', '1000' : '16:40 min', '3000' : '50 min'} #leave in secs
optValidScenarioList = ['0', '1', '4', '5', '6', '7', '8', '12', '13', '15', '17', '18', '19', '25', '26'] #28 also works, removed to make 15 scenarios
optScenarioMap = {'0' : '1', '1': '2', '4' : '3', '5' : '4', '6' : '5', '7' : '6', '8' : '7', '12' : '8', '13' : '9', '15' : '10', '17' : '11', '18' : '12', '19' : '13', '25' : '14', '26' : '15'}

optDoBreakdown = True
optLatexOnly = True

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
			qe = resultLine['qosa']
			alpha = resultLine['optgoal']

			key = qe + "_" + alpha +"_"+q
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
				print "@@@"+breakdownKey


qosa = {'none' : 'FG-SNEE', 'tinydb' : 'TDB-SNEE'}

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
                yAxisSymbol = "$\\hat{\\epsilon}$"
	elif q=='beta':
		yAxisLabel = "Buffering factor"
                yAxisSymbol = "$\\beta$"

	#logscale
	if (q=='delta-ms'): #q=='total-energy' or q=='average-energy' or
		graph = GraphData.GraphData(alphas, qosa.values(), "Acquisition interval (s) ", "Average "+yAxisLabel+" (log scale)", " ", logScaleY = True)
	else:
		graph = GraphData.GraphData(alphas, qosa.values(), "Acquisition interval (s)", "Average "+yAxisLabel, " ")

	plotId = "fg-tdb_" + q
	gnuPlotExe = 'gnuplot'

	latexSource += "\n\\section{"+yAxisLabel+" Results}\n\\label{sec:breakdown-"+plotId+"}\n\n"

	errors = 0
	for alpha in alphas:
		#xAxisPoints, lineLabels, xLabel, yLabel = None, title = None, logScaleY = False
		if q=='delta-ms':
			breakdownGraph = GraphData.GraphData(map(str,range(1,16))+['Avg'], qosa.values(), "Scenario", yAxisLabel+" (log scale)", " ", logScaleY = True)
		else:
			breakdownGraph = GraphData.GraphData(map(str,range(1,16))+['Avg'], qosa.values(), "Scenario", yAxisLabel, " ")

		for qe in qosa.keys():
			key = qe + "_" + alpha+"_"+q
			#print key

			if key in counts:
				plotValue = sums[key]/counts[key]
			else:
				plotValue = '?'

			#print plotValue
			graph.addPoint(qosa[qe], alpha, plotValue)
		
			if optDoBreakdown:

				for s in optValidScenarioList:
					breakdownKey = key+"_"+s
					if breakdownKey in breakdown:
						bPlotValue = breakdown[breakdownKey]
					else:
						bPlotValue = '?'
						errors += 1
					breakdownGraph.addPoint(qosa[qe], optScenarioMap[s], bPlotValue)
				breakdownGraph.addPoint(qosa[qe], 'Avg', plotValue)	

		breakdownPlotId = plotId + "_alpha" + alpha 
		if (not optLatexOnly) and (q!='alpha-ms'):
			breakdownGraph.generatePlotFile(breakdownPlotId + ".txt", False)		
			breakdownGraph.plotGraph(breakdownPlotId, "histogram", gnuPlotExe, False, 'outside top right', False)
			os.system('ps2pdf -dEPSCrop '+breakdownPlotId+'.eps')		
		
		if (q!='alpha-ms'):
			latexStr = '''

\\begin{center}
\\includegraphics[width=0.8\linewidth]{results/%s}
\\captionof{figure}[%s with $\\alpha$=%ss for \\fgqe vs. \\tdbqe.]{%s obtained for each scenario compiled with $\\alpha$=%ss.}\label{fig:eval-%s}
\\end{center}
'''
			latexSource += latexStr % (breakdownPlotId, yAxisSymbol, alpha, yAxisLabel, alpha, breakdownPlotId)

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

fo = open("fg-tdb-breakdown.tex", "w")
fo.writelines(latexSource)
fo.close()

print errors
