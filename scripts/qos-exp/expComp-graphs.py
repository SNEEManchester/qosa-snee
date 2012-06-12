#!/usr/bin/python
import os, UtilLib, re, fileinput, sys

#TODO: unhardcode
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'

labels = {}

subExpSet = ['a', 'b', 'c', 'd', 'e']
measurementTypeMap = {}
scenarioSet = ['1','2','3','4']
expSet = ['2'] #3
improvements = {}

def addLatexHeader(slidesFile):
	latexSrc = '''\\documentclass[a4paper]{article}

\\usepackage{graphicx}
\\usepackage{subfigure}

\\newenvironment{changemargin}[2]{
  \\begin{list}{}{
    \\setlength{\\topsep}{0pt}
    \\setlength{\\leftmargin}{#1}
    \\setlength{\\rightmargin}{#2}
    \\setlength{\\listparindent}{\\parindent}
    \\setlength{\\itemindent}{\\parindent}
    \\setlength{\\parsep}{\\parskip}
  }
  \item[]}{\\end{list}}
  
\\begin{document}	
'''
	f = open(slidesFile,'w')	
	f.write(latexSrc)	
	f.close()	


def addLatexFooter(slidesFile):
	latexSrc = '\\end{document}'
	f = open(slidesFile,'a')	
	f.write(latexSrc)	
	f.close()


def createSlideByExp(outputDir, exp, scenario, label, slidesFile):
		
	latexSrc = '''\\begin{changemargin}{-2cm}{-2cm}

\\subsubsection{Experiment=%s, Scenario=%s, timestamp=%s}

\\includegraphics[scale=0.55]{%s}
\\includegraphics[scale=0.55]{%s}

\\includegraphics[scale=0.55]{%s}
\\includegraphics[scale=0.55]{%s}

\\end{changemargin}

'''
	alphaGraph = 'exp'+exp+'-scenario'+scenario+'-'+label+'-pi-ms.pdf'
	deltaGraph = 'exp'+exp+'-scenario'+scenario+'-'+label+'-delta-ms.pdf'
	epsilonGraph = 'exp'+exp+'-scenario'+scenario+'-'+label+'-total-energy.pdf'
	lambdaGraph = 'exp'+exp+'-scenario'+scenario+'-'+label+'-network-lifetime.pdf'

	latexSrc = latexSrc % (exp, scenario, label, alphaGraph, deltaGraph, epsilonGraph, lambdaGraph)

	f = open(slidesFile,'a')	
	f.write(latexSrc)	
	f.close()


def generatePDF(currentDir, f, g, outputDir, exp,scenario,label):
	for g in os.listdir(currentDir+'/'+f):
		if g.endswith('.eps'):
			oldGraphFile = currentDir+'/'+f+'/'+g
			os.system('epstopdf  '+UtilLib.winpath(oldGraphFile))
			#os.system('rm '+oldGraphFile)
			newGraphFile = outputDir+'/exp'+exp+'-scenario'+scenario+'-'+label+'-'+g.replace('.eps','.pdf')
			os.system('mv '+oldGraphFile.replace('.eps','.pdf')+' '+newGraphFile)


def createResultsByExperiment(currentDir, outputDir):

	outFile = outputDir+'/results-by-exp.tex'
	os.system('mv '+outFile+' '+outFile+'.old')
	addLatexHeader(outFile)

	os.chdir(currentDir)
	for f in os.listdir(currentDir):
		if os.path.isdir(currentDir+'/'+f):
			m = re.match("([23][abcde])\_scenario([\d])-(.*)", f) #
			if (m != None):
				exp = m.group(1)
				scenario = m.group(2)
				label = m.group(3)
				labels[exp+scenario] = label
			
				print 'exp='+exp+' scenario='+scenario +' label='+label
				
				#generatePDF(currentDir, f, g, outputDir, exp,scenario,label)
				
				createSlideByExp(outputDir, exp, scenario, label, outFile)
				
	addLatexFooter(outFile)
	os.chdir(outputDir)
	os.system('pdflatex '+UtilLib.winpath(outFile))
	os.chdir(currentDir)

def getLabel(exp, subExp, scenario):
	key = exp+subExp+scenario
	return labels[key]



def createSlideByScenario(outputDir, measurementType, scenario, exp, measurementTypeMap, outFile):

	global subExpSet

	labelList = []

	graphName = {}
	for subExp in subExpSet:
		label = getLabel(exp, subExp, scenario)
		labelList.append(label)
		graphName[subExp] = 'exp'+exp+subExp+'-scenario'+scenario+'-'+label+'-'+measurementType+'.pdf'


	latexSrc = '''\\begin{changemargin}{-2cm}{-2cm}

\\begin{figure}[tb!]
\\subfigure[scriptsize,tight][Routing=false, Where-scheduling=false, When-scheduling=false \\\\ ts=%s]{
    \\includegraphics[width=.48\linewidth]{%s}
  }
\\subfigure[scriptsize,tight][Routing=true, Where-scheduling=false, When-scheduling=false \\\\ ts=%s]{
    \\includegraphics[width=.48\linewidth]{%s}
  }
\\subfigure[scriptsize,tight][Routing=false, Where-scheduling=true, When-scheduling=false \\\\ ts=%s]{
    \\includegraphics[width=.48\linewidth]{%s}
  }
\\subfigure[scriptsize,tight][Routing=false, Where-scheduling=false, When-scheduling=true \\\\ ts=%s]{
    \\includegraphics[width=.48\linewidth]{%s}
  }
\\subfigure[scriptsize,tight][Routing=true, Where-scheduling=true, When-scheduling=true \\\\ ts=%s]{
    \\includegraphics[width=.48\linewidth]{%s}
  }
\caption{Scenario=%s, experiment=%s, measurement=%s}
\label{fig:%s}
\\end{figure}

\\end{changemargin}

'''

	latexSrc = latexSrc % (labelList[0], graphName['a'], labelList[1], graphName['b'], labelList[2], graphName['c'], labelList[3], graphName['d'], labelList[4], graphName['e'], scenario, exp, measurementTypeMap[measurementType], scenario+'-'+exp+'-'+measurementType)

	f = open(outFile,'a')	
	f.write(latexSrc)	
	f.close()



def createResultsByScenario(currentDir, outputDir):
	global measurementTypeMap, scenarioSet, expSet

	outFile = outputDir+'/results-by-scenario.tex'
	os.system('mv '+outFile+' '+outFile+'.old')
	addLatexHeader(outFile)
	
	measurementTypeMap['pi-ms'] = 'Acquisition Interval'
	measurementTypeMap['delta-ms'] = 'Delivery Time'
	measurementTypeMap['total-energy'] = 'Total Energy'		
	measurementTypeMap['network-lifetime'] = 'Network Lifetime'		
	
	for measurementType in measurementTypeMap.keys():
		for scenario in scenarioSet:
			for exp in expSet:
				createSlideByScenario(outputDir, measurementType, scenario, exp, measurementTypeMap, outFile)

	addLatexFooter(outFile)
	os.chdir(outputDir)
	os.system('pdflatex '+UtilLib.winpath(outFile))
	os.chdir(currentDir)
	

def parseMeasurement(currentDir, measurementType, exp, subExp, scenario):
	global measurementTypes, improvements
	
	label = getLabel(exp, subExp, scenario)
	mFilename = currentDir+'/'+exp+subExp+'_scenario'+scenario+'-'+label+'/'+measurementType+'.txt'
	
	mfile = open(mFilename, 'r')
	first = True
	for line in mfile:
		if first:
			first = False
			continue
		tmpList = line.split()
		if (measurementType=='pi-ms'):
			index = 2
		elif (measurementType=='delta-ms'):	
			index = 4
		elif (measurementType=='total-energy'):
			index = 5
		elif (measurementType=='network-lifetime'):
			index = 3
		else:
			print 'not found'
			sys.exit(1)
		
	mfile.close()
	return float(tmpList[index])

	

def parseMeasurements(currentDir, measurementType, scenario, exp):
	global improvements

	for subExp in subExpSet:
		improvements[measurementType][subExp][scenario] = parseMeasurement(currentDir, measurementType, exp, subExp, scenario)
		print 'measurementType='+measurementType+',subExp='+subExp+',scenario='+scenario+','+str(improvements[measurementType][subExp][scenario])
		

def calculateImprovements(measurementType, scenario):
	global subExpSet

	baseline = improvements[measurementType]['a'][scenario]
	for subExp in subExpSet:
		if measurementType=='network-lifetime':
			improvement = ((improvements[measurementType][subExp][scenario] / baseline) - 1.0) * 100.0
		else:
			improvement = (1.0 - (improvements[measurementType][subExp][scenario] / baseline)) * 100.0
		improvements[measurementType][subExp][scenario+'pc'] = improvement
		
def calculateAggregations(measurementType, subExp):
	global scenarioSet
	
	count = 0.0
	sum = 0.0
	minv = 999999999999
	maxv = -999999999999
	for scenario in scenarioSet:	
		v = improvements[measurementType][subExp][scenario+'pc']
		
		count+=1.0
		sum+=v
		minv = min(minv, v) 
		maxv = max(maxv, v)

	improvements[measurementType][subExp]['average'] = sum/count
	improvements[measurementType][subExp]['min'] = minv
	improvements[measurementType][subExp]['max'] = maxv


def generateAggrGraphPlotfile(outputDir, measurementType, exp):
		
	plotStr = '" " "routing" "where" "when" "all"\n'
	
	for scenario in scenarioSet+['average','min','max']:
		plotStr += '"%s" ' % scenario

		for subExp in subExpSet[1:]:
			if improvements[measurementType][subExp].has_key(scenario+'pc'):
				v = improvements[measurementType][subExp][scenario+'pc']
			else:
				v = improvements[measurementType][subExp][scenario] #avg, min, max
			plotStr += '%f ' % v
		plotStr += '\n'	
			
	plotFile = outputDir+'/'+measurementType+'-summary.txt'
	f = open(plotFile,'w')	
	f.write(plotStr)	
	f.close()
	

def plotAggrGraph(outputDir, measurementType, exp):

	scriptStr = '''
set term postscript eps enhanced color
set out '%s'

set title "%s summary"
set auto x
set auto y
set style data histogram
set xlabel "Scenarios"
set ylabel "Percentage Improvement"
set key top right
set style histogram cluster gap 1
set style fill pattern border -1
set boxwidth 0.9 absolute
set xtics
set datafile missing '?'
plot '%s' using 2:xtic(1) ti col fillstyle pattern 1, '%s' using 3:xtic(1) ti col fillstyle pattern 2, '%s' using 4:xtic(1) ti col  fillstyle pattern 3, '%s' using 5:xtic(1) ti col fillstyle pattern 8


set term gif
set style fill solid border -1
set out '%s'
plot '%s' using 2:xtic(1) ti col, '%s' using 3:xtic(1) ti col, '%s' using 4:xtic(1) ti col, '%s' using 5:xtic(1) ti col
	
'''

	epsFile = UtilLib.winpath(outputDir+'/'+measurementType+'-summary.eps')
	mType = measurementTypeMap[measurementType]
	plotFile = UtilLib.winpath(outputDir+'/'+measurementType+'-summary.txt')
	gifFile = UtilLib.winpath(outputDir+'/'+measurementType+'-summary.gif')
	scriptStr = scriptStr % (epsFile, mType, plotFile, plotFile, plotFile, plotFile, gifFile, plotFile, plotFile, plotFile, plotFile)

	scriptFile = outputDir+'/'+measurementType+'-script.txt'
	f = open(scriptFile,'w')	
	f.write(scriptStr)	
	f.close()
	
	exitVal = os.system(gnuPlotExe+' < '+scriptFile)
	os.system('epstopdf  '+epsFile)
	os.system('rm '+epsFile)


def summary(currentDir, outputDir):
	global improvements, expSet
	
	improvements = {'pi-ms' : {'a':{}, 'b':{}, 'c':{}, 'd':{}, 'e':{}}, 'delta-ms' : {'a':{}, 'b':{}, 'c':{}, 'd':{}, 'e':{}}, 'total-energy' : {'a':{}, 'b':{}, 'c':{}, 'd':{}, 'e':{}}, 'network-lifetime' : {'a':{}, 'b':{}, 'c':{}, 'd':{}, 'e':{}}} #this will only work if one experiment!!

	for measurementType in measurementTypeMap.keys():
		for scenario in scenarioSet:
			for exp in expSet:
				parseMeasurements(currentDir, measurementType, scenario, exp) #this will only work if one experiment!!
				calculateImprovements(measurementType, scenario) #this will only work if one experiment!!

	for measurementType in measurementTypeMap.keys():
		for exp in expSet:
			for subExp in subExpSet:
				calculateAggregations(measurementType, subExp) #this will only work if one experiment!!
		generateAggrGraphPlotfile(outputDir, measurementType, exp)
		plotAggrGraph(outputDir, measurementType, exp)


def main():
	createResultsByExperiment(os.getcwd(),os.getcwd()+'/out')
	createResultsByScenario(os.getcwd(),os.getcwd()+'/out')
	summary(os.getcwd(),os.getcwd()+'/out')


if __name__ == "__main__":
	main()
