import sys, string, os, re, fileinput 

#Generates the files needed to run gnuplot and invokes gnuplot
class GraphData(object):

    
    def __init__(self, xAxisPoints, lineLabels, xLabel):
    	self.xAxisPoints = xAxisPoints
    	self.lineLabels = lineLabels
    	self.xLabel = xLabel
    	#yLabel and title set in later calls as the length made the init call hard to read
    	self.yLabel = None
    	self.title = None
    	#plotfile and points automatically set later
    	self.plotFile = None
    	self.points = None
 
 	#Method to test if the installed gnuplot is enough to run the script.
    def testGnuPlotVersion(self, gnuPlotExe):
    	outputFile = "gnuPlotVersion.test"
    	testString = str(gnuPlotExe)+' -V > '+str(outputFile)
    	print testString
    	exitVal = os.system(testString)
    	if (exitVal!=0):
    		#logger.warning('Error checking gnuPlot Version run error')	
    		print 'Error checking gnuPlotVersion run error'
    		sys.exit(2)
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
			    			#logger.warning('Error gnuPlot Version is '+str(version)+' need 4.2 or higher')	
			    			print 'Error gnuPlot Version is '+str(version)+'.'+str(subVersion)+' need 4.2 or higher'
			    			sys.exit(2)
				if version < 4:
			    		#logger.warning('Error gnuPlot Version is '+str(version)+' need 4.2 or higher')	
			    		print 'Error gnuPlot Version is '+str(version)+' need 4.2 or higher'
			    		sys.exit(2)
		if version == 0:
			for line in fileinput.input(outputFile):
				print line
	    		#logger.warning('Error checking gnuPlot Version version not found')	
	    		print 'Error checking gnuPlotVersion version not found'
	    		sys.exit(2)
		else:	
	    		print 'gnuPlot version check successful Version ='+str(version)+'.'+str(subVersion)
	    		#logger.info('gnuPlot version check successful Version ='+str(version)+'.'+str(subVersion))	
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
    	
    #Records the data in a text file that is used as input to gnuplot	
    #NOTE: These files where used plot the graphs in the paper
    #TODO check that the removal of 'IDE08' works correctly
    def generatePlotFile(self, fname, numericXAxis, logger):
    	if self.points==None:
    		print '************ No points to be plotted'
    		logger.critical('************ No points to be plotted')
    		return 2
    
    	self.plotFile = fname
    	
    	if (len(self.xAxisPoints)==0 or len(self.lineLabels)==0 or self.xLabel==None):
    		print '************ Insufficient information provided to plot graph'
    		logger.critical('************ Insufficient information provided to plot graph')
    		return 2
    		
    	f = open(fname,'w')
    	
    	#write the header line
    	label = str(self.xLabel).replace('ICDE08','')
	f.write('"'+label+'"\t')
	for line in self.lineLabels:
		f.write('"'+string.rstrip(line,'.txt')+'"\t')
	f.write('\n')
	
    	#write the data lines	
    	for xVal in self.xAxisPoints:
    		if (numericXAxis):
    			f.write(str(xVal)+'\t')
    		else:
    			f.write('"'+str(xVal)+'"\t')
    	
    		for line in self.lineLabels:
    			key = self.getKey(line,xVal)
    			if self.points.has_key(key):
    				f.write(str(self.points[key])+'\t')
    			else:
    				f.write('?\t')
    		f.write('\n')
    	
    	f.close()
    	
    	
    #writes a gnuplot script
    #evokes the gnuplot script 
    #NOTE: The scripts and graph where not actually used in the paper.
    def plotGraph(self, outputFile, graphStyle, gnuPlotExe, numericXAxis, keyPlacement, logger):
    	
    	if (self.plotFile == None):
    		print '************** Need to generate plot file first'
    		logger.critical('************** Need to generate plot file first')
    		return 2
    
	plotLineList = []
	if (numericXAxis):
		for l in range(2,(len(self.lineLabels)+2)):
			plotLineList.append("'%s' using 1:%s ti col" % (self.plotFile, str(l)))
	else:	
		for l in range(2,(len(self.lineLabels)+2)):
			plotLineList.append("'%s' using %s:xtic(1) ti col" % (self.plotFile, str(l)))

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
set style histogram cluster gap 1
set style fill pattern border -1
set boxwidth 0.9 absolute
set xtics
set datafile missing '?'
%s

set term gif
set style fill solid border -1
set out '%s'
%s
	"""
	scriptStr = scriptStr % (outputFile+'.eps', self.title, graphStyle, self.xLabel, self.yLabel, keyPlacement, plotLineStr, outputFile+'.gif', plotLineStr)

	f = open(outputFile+'gnu-plot-script-file.txt','w')	
	f.write(scriptStr)	
	f.close()

	print 'running: '+gnuPlotExe+' < '+outputFile+'gnu-plot-script-file.txt'

	exitVal = os.system(gnuPlotExe+' < '+outputFile+'gnu-plot-script-file.txt')
	if (exitVal!=0):
		logger.warning('Error during graph plotting for experiment '+self.title)	
		print 'Error during graph plotting for experiment '+self.title
	else:
		print 'Graph plotting successful for experiment '+self.title
		logger.info('Graph plotting successful for experiment '+self.title)	

