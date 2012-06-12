#!/usr/bin/python
import os, UtilLib, re, fileinput

#TODO: unhardcode
gnuPlotExe = '/cygdrive/c/Program\ Files/gnuplot/bin/pgnuplot.exe'


def generateGraph(dataFile, outputGraphFile):

	scriptStr = '''
set title "Varying Max Neighbourhood Size "
set auto x
set auto y
set style data linespoints
set xlabel "Max neighbourhood size"
set ylabel "Objective function value"
set y2label "num DAFs considered"
set key top right
set style histogram cluster gap 1
set style fill pattern border -1
set boxwidth 0.9 absolute
set yrange [0:]
set y2range [0:]
set y2tics border
set xtics
set datafile missing '?'

set term postscript eps enhanced
set style fill solid border -1
set out '%s'
plot '%s' using 1:2 ti col, '%s' using 1:4 ti col axes x1y2

set term png
set style fill solid border -1
set out '%s'
plot '%s' using 1:2 ti col, '%s' using 1:4 ti col axes x1y2
'''

	epsGraphPath = UtilLib.winpath(outputGraphFile+'.eps')
	pngGraphPath = UtilLib.winpath(outputGraphFile+'.png')
	dataFilePath = UtilLib.winpath(dataFile)

	scriptStr = scriptStr % (epsGraphPath, dataFilePath, dataFilePath, pngGraphPath, dataFilePath, dataFilePath)
	scriptFile = 'c:/gplot-script.txt'
	f = open(scriptFile,'w')	
	f.write(scriptStr)	
	f.close()
	
	exitVal = os.system(gnuPlotExe+' < '+scriptFile)
	os.system('epstopdf  '+epsGraphPath)
	os.system('rm '+epsGraphPath)


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


def convertLifetime(fnVal, optGoal):
	if optGoal=='max_lifetime':
		l = 1.0/float(fnVal)	#lifetime expressed in alpha*beta*s 
					#alpha=10, beta=1. alpha*beta=agenda eval duration
		l = l * 10.0		#to get seconds, multiply by 10
		l = l /60.0/60.0/24.0	#to get dayss
		
		return str(int(l))
	else:
		return fnVal


def buildDataFile(nnDataDir,optGoal):
	dataFilePath = nnDataDir+'/data.txt'
	print 'writing to ' + dataFilePath
	df = open(dataFilePath, 'w')
	df.write('"NN"\t"fn_val"\t"time"\t"num DAFs"\n')	

	for n in range(0,31):
		print n
		outFilePath = nnDataDir+'/out'+str(n)+'.txt'
		if os.path.exists(outFilePath):
			summary = parseOutFile(outFilePath)
			fnVal = summary['min_f']
			fnVal = convertLifetime(fnVal, optGoal)
			numDAFs = summary['num DAFs considered']
			df.write(str(n)+'\t'+fnVal+'\t?\t'+numDAFs+'\n')

			if nnDataDir.find('heuristic')>-1:
				break	
	df.close()


def addLatexHeader(slidesFile):
	latexSrc = '''\documentclass[a4paper]{article}

\usepackage{graphicx}

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

def createSlide(rootDir, comment, query, netSize, numSources, optGoal, outputDir):
		
	typeANetId = getId(comment, query,netSize,'A',numSources,optGoal,'')
	typeBNetId = getId(comment, query,netSize,'B',numSources,optGoal,'')
	typeANetHId = getId(comment, query,netSize,'A',numSources,optGoal,'-heuristic')
	typeBNetHId = getId(comment, query,netSize,'B',numSources,optGoal,'-heuristic')

	typeANetDir = getNNDataDir(rootDir, typeANetId, query)
	outFilePath = typeANetDir+'/out30.txt'
	summary = parseOutFile(outFilePath)

	objFnInitA = summary['init_val']
	objFnOptA = summary['min_f']
	numOpInst = summary['num operator instances']
	numSites = summary['num sites']
	numDataReducingOpInst = summary['num data reducing op instances']

	typeBNetDir = getNNDataDir(rootDir, typeBNetId, query)
	outFilePath = typeBNetDir+'/out30.txt'	
	summary = parseOutFile(outFilePath)
	objFnInitB = summary['init_val']
	objFnOptB = summary['min_f']

	typeANetHDir = getNNDataDir(rootDir, typeANetHId, query)
	outFilePath = typeANetHDir+'/out0.txt'
	summary = parseOutFile(outFilePath)
	objFnInitAH = summary['init_val']
	objFnOptAH = summary['min_f']

	typeBNetHDir = getNNDataDir(rootDir, typeBNetHId, query)
	outFilePath = typeBNetHDir+'/out0.txt'
	summary = parseOutFile(outFilePath)
	objFnInitBH = summary['init_val']
	objFnOptBH = summary['min_f']

	threshA10 = testThresh(10, objFnInitA, objFnOptAH, typeANetDir+'/data.txt')
	threshA20 = testThresh(20, objFnInitA, objFnOptAH, typeANetDir+'/data.txt')
	threshA30 = testThresh(30, objFnInitA, objFnOptAH, typeANetDir+'/data.txt')

	latexSrc = '''\\begin{changemargin}{-2cm}{-2cm}

\\subsubsection{Query=%s, Network Size=%s, Number of sources=%s, Optimization goal=%s}

\\includegraphics[scale=0.6]{%s.pdf}
\\includegraphics[scale=0.6]{%s.pdf}

\\vspace{5 mm}

\\begin{tabular}{|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|p{1.2cm}|}
\\hline
\\tiny{Network Type} & 
\\tiny{Initial point opt val} & 
\\tiny{Best opt val obtained without heuristic (Max NN=30)} & 
\\tiny{Best opt val obtained with heuristic (Max NN=0)} & 
\\tiny{Number of operator instances in partial DAF} & 
\\tiny{Number of data-reducing operator instances in partial DAF} & 
\\tiny{Best Max-NN value within 30\%% of global optimum} & 
\\tiny{Best Max-NN value within 20\%% of global optimum} & 
\\tiny{Best Max-NN value within 10\%% of global optimum} \\\\
\\hline
A & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} \\\\
B & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} & \\tiny{%s} \\\\
\\hline
\\end{tabular}
\\end{changemargin}
'''
	slidesFile = outputDir+'/slides.tex'
	if not os.path.exists(slidesFile):
		addLatexHeader(slidesFile)

	#do this after calculating thresholds, as they assume min
	objFnInitA = convertLifetime(objFnInitA, optGoal)
	objFnOptA = convertLifetime(objFnOptA, optGoal)
	objFnOptAH = convertLifetime(objFnOptAH, optGoal)
	objFnInitB = convertLifetime(objFnInitB, optGoal)
	objFnOptB = convertLifetime(objFnOptB, optGoal)
	objFnOptBH = convertLifetime(objFnOptBH, optGoal)	

	latexSrc = latexSrc % (query, netSize, numSources, optGoal.replace('_',' '), typeANetId, typeBNetId, objFnInitA, objFnOptA, objFnOptAH, numOpInst, numDataReducingOpInst, threshA30, threshA20, threshA10, objFnInitB, objFnOptB, objFnOptBH, numOpInst, numDataReducingOpInst, '?', '?', '?')

	f = open(slidesFile,'a')	
	f.write(latexSrc)	
	f.close()

	
def getId(comment, query,netSize,netType,numSources,optGoal,heuristic):
	id='';
	if comment!=None:
		id=comment+'-'
	id+=query+'-'+netSize+'n-type'+netType+'-'+numSources+'s-'+optGoal+(heuristic)

	return id


def getNNDataDir(rootDir,id,query):
	return rootDir+'/'+id+'/'+query+'/query-plan/matlab/wheresched'
	
def visitDir(currentDir, outputDir):

	os.chdir(currentDir)
	for f in os.listdir(currentDir):
		if os.path.isdir(currentDir+'/'+f):
			m = re.match("((.*)-)?(Q2|Q4|Q5)-(10|30|100)n-type(A|B)-(3|10|min|maj)s-(min\_delivery|min\_energy|max\_lifetime)(-heuristic)?", f)
			
			if (m != None):
				comment = m.group(2)
				query = m.group(3)
				netSize = m.group(4)
				netType = m.group(5)
				numSources = m.group(6)
				optGoal = m.group(7)
				heuristic = m.group(8)
				if heuristic==None:
					heuristic=''
#				print 'query=' + query + ' netSize=' + netSize + ' numSources=' + numSources + ' optGoal=' + optGoal + ' heuristic=' + str(heuristic)
				id = getId(comment,query,netSize,netType,numSources,optGoal,heuristic)
				
				nnDataDir = getNNDataDir(currentDir,id,query)
				buildDataFile(nnDataDir, optGoal)
				generateGraph(nnDataDir+'/data.txt', outputDir+'/'+id)
				
#				if (netType=='A' and heuristic==''):
#					createSlide(currentDir, comment, query, netSize, numSources, optGoal, outputDir)

def main():
	visitDir(os.getcwd(),os.getcwd()+'/out')


if __name__ == "__main__":
	main()
