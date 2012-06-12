import sys, string, os, re, fileinput, logging, UtilLib


showParametersInGraph = False

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


#Generates the files needed to run gnuplot and invokes gnuplot
class GraphData(object):

    
	def __init__(self, xAxisPoints, lineLabels, xLabel, yLabel = None, title = None, logScaleY = False):
        	self.xAxisPoints = xAxisPoints
        	self.lineLabels = lineLabels
        	self.xLabel = xLabel
        	self.yLabel = yLabel
        	self.title = title
		self.logScaleY = logScaleY
        	
	    	#plotfile and points automatically set later
	    	self.plotFile = None
	    	self.points = None
    		
	def setTitle(self, graphTitle, extraText):
		
		#Add extra lines to the title. 
		if showParametersInGraph:
			titleExtraLines = '\\n'+str(numNodes)+ ' Sites with duration of '+str(simulationDuration)+' seconds'
				
			titleExtraLines = titleExtraLines + '\\n'
			titleExtraLines += extraText	
		else:
			titleExtraLines = ''
	
		self.title = graphTitle + titleExtraLines
 
	#Method to test if the installed gnuplot is enough to run the script.
	def testGnuPlotVersion(self, gnuPlotExe):
		outputFile = "gnuPlotVersion.test"
		testString = str(gnuPlotExe)+' -V > '+str(outputFile)
		print testString
		exitVal = os.system(testString)
		if (exitVal!=0):
			reportError('Error checking gnuPlot Version run error')	
		else:
			#gnuplot 4.0 patchlevel 0
			pattern = re.compile('gnuplot (\d+).(\d+) patchlevel 0')
			version = 0
			for line in fileinput.input(outputFile):
				m = pattern.match(line)
				if m:
					version = int(m.group(1))
					subVersion = int(m.group(2))
					if version == 4:
						if subVersion < 2:
				    			reportError('Error gnuPlot Version is '+str(version)+' need 4.2 or higher')	
				    			sys.exit(2)
					if version < 4:
				    		reportError('Error gnuPlot Version is '+str(version)+' need 4.2 or higher')
			    			sys.exit(2)
			if version == 0:
				for line in fileinput.input(outputFile):
					print line
	    			reportError('Error checking gnuPlot Version version not found')	
	    			sys.exit(2)
			else:	
	    			report('gnuPlot version check successful Version ='+str(version)+'.'+str(subVersion))
	    			os.remove(outputFile);

	def getKey(self,line,xVal):
		key = str(line)+"_"+str(xVal)
		return key
	
	#Add a data point to later be used to plot the graphs.	
	def addPoint(self,line,xVal,yVal):
		key = self.getKey(line,xVal)
		if(self.points==None):
			self.points = {}
		self.points[key] = yVal	
    		report('key ="'+key+'" yVal = '+str(yVal))
    		report(str(self.points))
    	
    	#Records the data in a text file that is used as input to gnuplot	
    	#NOTE: These files where used plot the graphs in the paper
    	#TODO check that the removal of 'IDE08' works correctly
    	def generatePlotFile(self, fname, numericXAxis):
    		if self.points==None:
    			reportWarning('************ No points to be plotted')
    			return 2
    
    		self.plotFile = fname
    	
    		if (len(self.xAxisPoints)==0 or len(self.lineLabels)==0 or self.xLabel==None):
    			reportWarning('************ Insufficient information provided to plot graph')
	    		return 2
    		
	    	f = open(fname,'w')
    	
	    	#write the header line
		f.write('"'+self.xLabel+'"\t')
		for line in self.lineLabels:
			print self.lineLabels
			print line
			f.write('"'+string.rstrip(str(line),'.txt')+'"\t')
		f.write('\n')
	
	    	#write the data lines	
	    	for xVal in self.xAxisPoints:
	    		if (numericXAxis):
	    			f.write(str(xVal)+'\t')
	    		else:
	    			f.write('"'+str(xVal)+'"\t')
    	
	    		for line in self.lineLabels:
	    			report('xVal ='+str(xVal)+' line= '+str(line))
	    			key = self.getKey(line,xVal)
	    			report('xVal ='+str(xVal)+' line= '+str(line)+' key= "'+key+'"')
	    			if self.points.has_key(key):
	    				report(str(self.points[key]))
	    				f.write(str(self.points[key])+'\t')
	    			else:
	    				f.write('?\t')
	    				report("point not found")
	    		f.write('\n')
    	
	    	f.close()
    	
    	
	#writes a gnuplot script and invokes it to generate a graph
	def plotGraph(self, outputFile, graphStyle, gnuPlotExe, numericXAxis, keyPlacement, rowStacked = True):
    	
		if (self.plotFile == None):
			reportWarning('************** Need to generate plot file first')
			return 2
    
		plotLineList = []
		if (numericXAxis):
			for l in range(2,(len(self.lineLabels)+2)):
				plotLineList.append("'%s' using 1:%s ti col" % (UtilLib.winpath(self.plotFile), str(l)))
		else:	
			for l in range(2,(len(self.lineLabels)+2)):
				plotLineList.append("'%s' using %s:xtic(1) ti col" % (UtilLib.winpath(self.plotFile), str(l)))

		plotLineStr = 'plot '+string.join(plotLineList,', ')+'\n'

		scriptStr = """
set term postscript eps
set out '%s'

set title "%s"
set auto x
set auto y
set style data %s
set xlabel "%s"
set ylabel "%s"
set key %s
set style histogram %s
set style fill pattern border -1
set boxwidth 0.9 absolute

set xtics
set datafile missing '?'
%s
%s

set term gif
set style fill solid border -1
set out '%s'
%s
	"""
		if (rowStacked == False):
			histogramStyle = "cluster gap 1"
		else:
			histogramStyle = "rowstacked"
		
		if (self.logScaleY):
			logScaleStr = 'set logscale y'
		else:
			logScaleStr = 'set yrange [0:]'
	
		#if Globals.showTitleInGraph:
		scriptStr = scriptStr % (UtilLib.winpath(outputFile+'.eps'), self.title, graphStyle, self.xLabel, self.yLabel, keyPlacement, histogramStyle, logScaleStr, plotLineStr, UtilLib.winpath(outputFile+'.gif'), plotLineStr)
		#else:
		#	scriptStr = scriptStr % (outputFile+'.eps', ""        , graphStyle, self.xLabel, self.yLabel, keyPlacement, plotLineStr, outputFile+'.gif', plotLineStr)

		f = open(outputFile+'gnu-plot-script-file.txt','w')	
		f.write(scriptStr)	
		f.close()

		print 'running: '+gnuPlotExe+' < '+outputFile+'gnu-plot-script-file.txt'

		exitVal = os.system(gnuPlotExe+' < '+outputFile+'gnu-plot-script-file.txt')
		if (exitVal!=0):
			reportWarning('Error during graph plotting for experiment '+str(self.title))	
		else:
			report('Graph plotting successful for experiment '+ str(self.title))
