#!/usr/bin/python
import math, os.path, UtilLib, logging, sys

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

#This library, given a list of (x,y) data, computes the regression function for the function using the least-squares method, and uses this to compute the correlation coefficient.
#All formulae in this library are taken from pages 311-314 of Schaum's Outlines, Statistics, 3rd edition, McGraw Hill 1998.
class Dataset(object):

	def __init__(self):
		self.data = []

	
	#The data to be considered
	def add(self, x, y, id):
		self.data.append([float(x), float(y), str(id)])

	
	def getSum_X(self):
		sum = 0.0
		for i in self.data:
			sum += i[0]
		return sum

	
	def getSum_Y(self):
		sum = 0.0
		for i in self.data:
			sum += i[1]
		return sum

	
	def getSum_X2(self):
		sum = 0.0
		for i in self.data:
			sum += math.pow(i[0],2)
		return sum

		
	def getSum_Y2(self):
		sum = 0.0
		for i in self.data:
			sum += math.pow(i[1],2)
		return sum

		
	def getSum_XY(self):
		sum = 0.0
		for i in self.data:
			sum += i[0]*i[1]
		return sum
		
		
	def getRegCoeff_a0(self):
		N = float(len(self.data))
		a0 = self.getSum_Y()*self.getSum_X2() - self.getSum_X()*self.getSum_XY()
		a0 /= float(N*self.getSum_X2() - math.pow(self.getSum_X(),2))
		return a0

		
	def getRegCoeff_a1(self):
		N = float(len(self.data))
		a1 = N*self.getSum_XY() - self.getSum_X() * self.getSum_Y()
		a1 /= float(N*self.getSum_X2() - math.pow(self.getSum_X(),2))
		return a1	

		
	def getMean_Y(self):
		sum = 0.0
		for i in self.data:
			sum += i[1]
		mean = sum / float(len(self.data))
		return mean	


	def getEst_Y(self, x):
		a0 = self.getRegCoeff_a0()
		a1 = self.getRegCoeff_a1()
		y = a0 + a1 * x
		return y

	# SUM((Yest-Ymean)^2)
	def getExplainedVariation(self):
		sum = 0.0
		for i in self.data:
			yEst = self.getEst_Y(i[0])
			yMean = self.getMean_Y()
			sum += math.pow(yEst - yMean, 2)
		return sum

	#SUM((Y-Yest)^2)
	def getUnexplainedVariation(self):
		sum = 0.0
		for i in self.data:
			y = i[1]
			yEst = self.getEst_Y(i[0])	
			sum += math.pow(y - yEst, 2)
		return sum


	#SUM((Y-Ymean)^2)
	def getTotalVariation(self):
		sum = 0.0
		for i in self.data:
			y = i[1]
			yMean = self.getMean_Y()
			sum += math.pow(y - yMean, 2)
		return sum


	def getCorrelCoeff(self):
		try:
			return math.sqrt(self.getExplainedVariation()/float(self.getTotalVariation()))
		except ZeroDivisionError:
			return "Div by zero error"
		
	def drawScatterDiagram(self, outputFilename, xLabel, yLabel, title, labelPoints = True, xZeroToOne = True):
    	
		#first generate plotFile
		outputDir = os.path.dirname(outputFilename)
		baseName = os.path.basename(outputFilename)
		plotFilename = "%s/%s" % (outputDir, baseName.replace(".eps",".txt"))
		plotFile = open(plotFilename, 'w')
		plotFile.write("Id \"%s\" \"%s\"\n" % (xLabel, yLabel))
		for i in self.data:
			id =str(i[2])
			x = str(i[0])
			y = str(i[1])
			plotFile.write("%s %s %s\n" % (id, x, y))
		plotFile.close()

		#now generate script file
		#TODO: unhardcode this
		gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'    	
		
		scriptStr = """
set term postscript eps font "Times-Bold,24" size 7,3.5
set out '%s'

set style data points
set xlabel "%s" font "Times-Bold,30" 
set ylabel "%s" font "Times-Bold,30" 
set title "%s"
set pointsize 5
set key off
set xtics
%s
set datafile missing '?'
linewidth 3 
%s
		"""
		
		if xZeroToOne:
			xRangeStr = 'set xrange [0:1]'
		else:
			xRangeStr = ''
		if labelPoints:
			plotCommandStr = 'plot \'%s\' using 2:3:1 with labels point pointtype 1 pointsize 3' % (UtilLib.winpath(plotFilename))
		else:
			plotCommandStr = 'plot \'%s\' using 2:3 point pointtype 1 pointsize 3' % (UtilLib.winpath(plotFilename))
		
		scriptStr = scriptStr % (UtilLib.winpath(outputFilename), xLabel, yLabel, title, xRangeStr, plotCommandStr)

		scriptFilename = "%s/gnu-plot-script.txt" % (outputDir)
		scriptFile = scriptFilename
		f = open(scriptFile,'w')	
		f.write(scriptStr)	
		f.close()

		report('running: '+gnuPlotExe+' < '+scriptFilename)
		exitVal = os.system(gnuPlotExe+' < '+scriptFilename)
		if (exitVal!=0):
			reportWarning('Error during graph plotting')
		
	
	def parsePlotfile(self, inputFilename):
		inFile = open(inputFilename, 'r')
		first = True
		while 1:
			line = inFile.readline()
			if not line:
				break
			if first:
				first = False
				continue
			data = line.split(" ")
			id = data[0]
			x = float(data[1])
			y = float(data[2])
			self.add(x, y, id)
		inFile.close()
		
	
def main(): 	
	p = Dataset()
	if len(sys.argv) == 1:
		p.add(1,1, 1)
		p.add(2,5, 2)
		p.add(3,33, 3)
		p.add(4,-9, 4)
	else:
		p.parsePlotFile(sys.argv[1])
	
	print p.data
	print "Regression coefficient a0 = " + str(p.getRegCoeff_a0())
	print "Regression coefficient a1 = " + str(p.getRegCoeff_a1())
	print "Explained variation       = " + str(p.getExplainedVariation())
	print "Unexplained variation     = " + str(p.getUnexplainedVariation())
	print "Total variation           = " + str(p.getTotalVariation())
	print "Correlation Coefficient   = " + str(p.getCorrelCoeff())


if __name__ == "__main__":
	main()

