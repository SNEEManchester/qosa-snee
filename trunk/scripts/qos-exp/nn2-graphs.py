#!/usr/bin/python
import os, UtilLib, re, StatLib

#TODO: unhardcode
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'


def generateGraph(dataFile, outputGraphFile):

	scriptStr = '''
set title "Varying Max Neighbourhood Size "
set auto x
set auto y
set style data linespoints
set pointsize 5
set xlabel "Max neighbourhood size"
set ylabel "Objective function value"
set y2label "Measured value"
set key top right
set yrange [0:]
set y2range [0:]
set y2tics border
set xtics
set datafile missing '?'

set term postscript eps enhanced
set style fill solid border -1
set out '%s'
plot '%s' using 1:2 ti col, '%s' using 1:3 ti col axes x1y2
'''

	graphPath = UtilLib.winpath(outputGraphFile)
	dataFilePath = UtilLib.winpath(dataFile)

	scriptStr = scriptStr % (graphPath, dataFilePath, dataFilePath)
	scriptFile = 'c:/gplot-script.txt'
	f = open(scriptFile,'w')	
	f.write(scriptStr)	
	f.close()
	
	exitVal = os.system(gnuPlotExe+' < '+scriptFile)
	os.system('epstopdf  '+graphPath)
	os.system('rm '+graphPath)


def parseOutFile(outFilePath):
	ofile = open(outFilePath, 'r')

	summary = {}
	summaryFlag = False
	for line in ofile:
		if line.startswith('*** Summary ***'):
			summaryFlag = True
			continue
		if line.startswith('*** Assignment ***'):
			break
		if line.strip()=='':
			continue
		if summaryFlag:
			(key, val) = line.split('=')
			summary[key] = val.strip();
	ofile.close()
	return summary


def convertSecondsToDays(n):
	return float(n) /60.0/60.0/24.0


def convertLifetime(fnVal, optGoal):
	if optGoal=='max_lifetime':
		l = 1.0/float(fnVal)	#lifetime expressed in alpha*beta*s 
					#alpha=10, beta=1. alpha*beta=agenda eval duration
		l = l * 10.0		#to get seconds, multiply by 10
		l = convertSecondsToDays(l)	#to get dayss
		
		return str(int(l))
	else:
		return fnVal


#changes the lifetime-1 to lifetime in objective function
def buildDataFile(oldDataFile, newDataFile, optGoal):

	df = open(newDataFile, 'w')
	df.write('"NN"\t"fn_val"\t"measured"\t"num DAFs"\n')	

	ofile = open(oldDataFile,'r')
	first = True

	for line in ofile:
		if first:
			first = False
			continue
		elems = line.split('\t')
		if optGoal=='max_lifetime':
			elems[1] = convertLifetime(elems[1], optGoal)
			elems[2] = str(convertSecondsToDays(elems[2]))
		newLine = '\t'.join(elems)
		df.write(newLine+'\n')

	ofile.close()		
	df.close()


def addLatexHeader(slidesFile):
	latexSrc = '''\documentclass[a4paper]{article}

\usepackage{graphicx}

  
\\begin{document}	
'''
	f = open(slidesFile,'w')	
	f.write(latexSrc)	
	f.close()	


def testThresh(thresh, maxF, minF, dataFile):
	threshF = (float(maxF) - float(minF)) * (float(thresh)/100.0) + float(minF)
	#print 'minF='+str(minF)+', maxF='+str(maxF)+'thesh='+str(thresh)+'%, threshF='+str(threshF)
	
	nn = None
	first = True
	found = False
	for line in fileinput.input(dataFile):
		attrList = line.split('\t')
			
		if first or len(attrList) < 4 or found:
			first = False
			continue
		
		fval = float(attrList[1])

		if fval <= threshF:
			nn = int(attrList[0])
			#t = float(attrList[2])/1000
			numDAFs = int(attrList[3])
			
			print('f_val within '+str(thresh)+'% of the global minimum with nn='+str(nn)+',num DAFs='+str(numDAFs))
			found = True
			
	return nn;

def createSlide(rootDir, query, netSize, netType, numSources, optGoal, outputDir, id):
		
	latexSrc = '''\\subsubsection{Query=%s, Network Size=%s, Network Type=%s, Number of sources=%s, Optimization goal=%s}

\\includegraphics[scale=0.6]{%s.pdf}

'''
	slidesFile = outputDir+'/slides.tex'
	if not os.path.exists(slidesFile):
		addLatexHeader(slidesFile)

	latexSrc = latexSrc % (query, netSize, netType, numSources, optGoal.replace('_',' '), id)

	f = open(slidesFile,'a')	
	f.write(latexSrc)	
	f.close()


def appendToCorrelDataSet(oldDataFile, deltaGraph, epsilonGraph, lambdaGraph, optGoal, id, outputDir):
	
	ofile = open(oldDataFile,'r')
	first = True

	for line in ofile:
		if first:
			first = False
			continue
		elems = line.split('\t')
		nn = elems[0]
		pointId = 0 #id+'-nn'+nn
		
		
		if optGoal=='max_lifetime':
			x = float(convertLifetime(elems[1], optGoal))
			y = float(convertSecondsToDays(elems[2]))
			lambdaGraph.add(x, y, pointId)
		else:
			x = float(elems[1])
			y = float(elems[2])
			
			if optGoal=='min_delivery': 
				deltaGraph.add(x, y, pointId)
			else:	#optGoal must be min_energy
				epsilonGraph.add(x, y, pointId)
	
	
def plotScatterdiagrams(deltaGraph, epsilonGraph, lambdaGraph, outputDir):
	resultFilename = "%s/delta.eps" % (outputDir)
	deltaCorrelCoeff = deltaGraph.getCorrelCoeff()
	xLabel = "Obj function value\\n\\nSweet spot = SE quadrant, correlation coefficient = " + str(deltaCorrelCoeff)  + "\\n"
	yLabel = "Measured value"
	title = "Where-scheduling Delivery Time Objective Function Value vs. Minimum Delivery Time Possible"
	deltaGraph.drawScatterDiagram(resultFilename, xLabel, yLabel, title, labelPoints = False, xZeroToOne = False)
	print("The delta correlation coefficient is " + str(deltaCorrelCoeff))

	os.system('epstopdf  '+UtilLib.winpath(resultFilename))
	os.system('rm '+resultFilename)

	resultFilename = "%s/epsilon.eps" % (outputDir)
	epsilonCorrelCoeff = epsilonGraph.getCorrelCoeff()
	xLabel = "Obj function value\\n\\nSweet spot = SE quadrant, correlation coefficient = "  + str(epsilonCorrelCoeff)  + "\\n"
	yLabel = "Measured value (J)"
	title = "Where-scheduling Total Energy Objective Function Value vs. Simulated Total Energy"
	epsilonGraph.drawScatterDiagram(resultFilename, xLabel, yLabel, title, labelPoints = False, xZeroToOne = False)
	print ("The epsilon correlation coefficient is " + str(epsilonCorrelCoeff))
	
	os.system('epstopdf  '+UtilLib.winpath(resultFilename))
	os.system('rm '+resultFilename)
	
	resultFilename = "%s/lambda.eps" % (outputDir)
	lambdaCorrelCoeff = lambdaGraph.getCorrelCoeff()
	xLabel = "Objective function value\\n\\nSweet spot = NE quadrant, correlation coefficient = " + str(lambdaCorrelCoeff)  + "\\n"
	yLabel = "Measured value (days)"
	title = "Where-scheduling Lifetime Objective Function Value vs. Simulated Lifetime"
	lambdaGraph.drawScatterDiagram(resultFilename, xLabel, yLabel, title, labelPoints = False, xZeroToOne = False)
	print("The lambda correlation coefficient is " + str(lambdaCorrelCoeff))

	os.system('epstopdf  '+UtilLib.winpath(resultFilename))
	os.system('rm '+resultFilename)

	return (deltaCorrelCoeff, epsilonCorrelCoeff, lambdaCorrelCoeff)


def createSlide2(outputDir, deltaCorrelCoeff, epsilonCorrelCoeff, lambdaCorrelCoeff):
		
	latexSrc = '''\\subsubsection{Overall min delivery time correlation between objective function and measured value}
\\includegraphics[scale=0.6]{delta.pdf}

The correlation coefficient is %s

\\subsubsection{Overall min energy correlation between objective function and measured value}
\\includegraphics[scale=0.6]{epsilon.pdf}

The correlation coefficient is %s

\\subsubsection{Overall max lifetime correlation between objective function and measured value}
\\includegraphics[scale=0.6]{lambda.pdf}

The correlation coefficient is %s

'''
	slidesFile = outputDir+'/slides.tex'
	if not os.path.exists(slidesFile):
		addLatexHeader(slidesFile)

	latexSrc = latexSrc % (str(deltaCorrelCoeff), str(epsilonCorrelCoeff), str(lambdaCorrelCoeff))

	f = open(slidesFile,'a')	
	f.write(latexSrc)	
	f.close()


def getId(query,netSize,netType,numSources,optGoal):
	return query+'-'+netSize+'n-type'+netType+'-'+numSources+'s-'+optGoal

def getDataFile(rootDir,id):
	return rootDir+'/'+id+'/data.txt'
	
def visitDir(currentDir, outputDir):

	deltaGraph = StatLib.Dataset()
	epsilonGraph = StatLib.Dataset()
	lambdaGraph = StatLib.Dataset()

	os.chdir(currentDir)
	for f in os.listdir(currentDir):
		if os.path.isdir(currentDir+'/'+f):
			m = re.match("(Q2|Q4|Q5)-(10|30|100)n-type(A|B)-(3|10|min|maj)s-(min\_delivery|min\_energy|max\_lifetime)", f)
			if (m != None):
				query = m.group(1)
				netSize = m.group(2)
				netType = m.group(3)
				numSources = m.group(4)
				optGoal = m.group(5)

#				print 'query=' + query + ' netSize=' + netSize + ' numSources=' + numSources + ' optGoal=' + optGoal + ' heuristic=' + str(heuristic)
				id = getId(query,netSize,netType,numSources,optGoal)
				
				dataFile = getDataFile(currentDir,id)
				buildDataFile(dataFile, outputDir+'/'+id+'.txt', optGoal)
				generateGraph(outputDir+'/'+id+'.txt', outputDir+'/'+id+'.eps')
							
				createSlide(id, query, netSize, netType, numSources, optGoal, outputDir, id)

				correlDataFile = outputDir+'/'+optGoal+'.txt'
				appendToCorrelDataSet(dataFile, deltaGraph, epsilonGraph, lambdaGraph, optGoal, id, outputDir)
				
	(deltaCorrelCoeff, epsilonCorrelCoeff, lambdaCorrelCoeff) = plotScatterdiagrams(deltaGraph, epsilonGraph, lambdaGraph, outputDir)			
	createSlide2(outputDir, deltaCorrelCoeff, epsilonCorrelCoeff, lambdaCorrelCoeff)

def main():
	visitDir(os.getcwd(),os.getcwd()+'/out')


if __name__ == "__main__":
	main()
