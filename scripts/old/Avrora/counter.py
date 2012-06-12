import getopt, os, sys, logging, re, fileinput, time, string, experimentLib, GraphData 

def countFile (file):
	lines = 0
	blank = 0
	bracket = 0
	comment = 0
	imp = 0
	debug = 0
	
	for line in fileinput.input(file):
		check = line.lstrip()
		check = check.rstrip()
		#print '['+check+']'
		#print check[1]
		if len(check) == 0:
			blank+=1
		elif (check in ['}','{']):
			bracket+=1
		elif (check[0] in ['/','*','#']):
			comment+=1
		elif check.find ('import')>=0:
			imp+= 1
		elif check.find ('logger.fine')>=0:
			debug+= 1
		else:
			lines+=1
	return lines, blank, bracket, comment, imp, debug

def countJavaTree(directory, outfile, allLines, allBlank, allBracket, allComment, allImport, allDebug):

	paths = os.listdir(directory) 
	for path in paths:
		if path != '.svn':
			full = directory+'/'+path
			if (os.path.isdir(full)):
				allLines, allBlank, allBracket, allComment, allImport, allDebug = countJavaTree(full, outfile, allLines, allBlank, allBracket, allComment, allImport, allDebug)
			else:
				lines,blank,bracket,comment, imp, debug = countFile(full)
				allLines+= lines
				allBlank+= blank
				allBracket+= bracket
				allComment+= comment
				allImport+= imp
				allDebug+= debug
				total = lines+blank+bracket+comment+imp+debug
				out = path+' code='+str(lines)+' blank='+str(blank)+' brackets= '+str(bracket)+' comments='+str(comment)+' imports='+str(imp)+' fine debug='+str(debug)
				out = out + ' total= '+str(total);
				#print out
				outfile.write (out+'\n')
	return 	allLines, allBlank, allBracket, allComment, allImport, allDebug		

def countJava (directory, outfile):
	outfile = open(outfile,'w')
	allLines = 0
	allBlank = 0
	allBracket = 0
	allComment = 0
	allImport = 0
	allDebug = 0
	allLines, allBlank, allBracket, allComment, allImport, allDebug = countJavaTree (directory,outfile, allLines, allBlank, allBracket, allComment, allImport, allDebug)
	out = 'All'+' code='+str(allLines)+' blank='+str(allBlank)+' brackets= '+str(allBracket)+' comments='+str(allComment)+' imports='+str(allImport)+' fine debug='+str(allDebug)
	total = allLines+allBlank+allBracket+allComment+allImport+allDebug
	out = out + ' total= '+str(total);
	print out
	outfile.write (out+'\n')
	outfile.close()

def countNesCRoot(directory, outfile):
	paths = os.listdir(directory) 
	allLines = 0
	allBlank = 0
	allBracket = 0
	allComment = 0
	allDebug = 0
	for path in paths:
		full = directory+'/'+path
		if path.endswith('.nc') or path.endswith('.h'):
			#print full
			lines,blank,bracket,comment, imp, debug = countFile(full)	
			allLines+= lines
			allBlank+= blank
			allBracket+= imp+bracket
			allComment+= comment
			#ignore imp it should be zero
			allDebug+= debug
			total = lines+blank+bracket+comment+debug
			out = path+' code='+str(lines)+' blank='+str(blank)+' brackets= '+str(bracket)+' comments='+str(comment)+' other='+str(imp+debug)
			out = out + ' total= '+str(total);
			#print out
			outfile.write (out+'\n')
	out = directory+' code='+str(allLines)+' blank='+str(allBlank)+' brackets= '+str(allBracket)+' comments='+str(allComment)+' other='+str(allDebug)
	total = allLines+allBlank+allBracket+allComment+allDebug
	out = out + ' total= '+str(total);
	print out
	outfile.write (out+'\n')

def countNesCTree(directory, outfile):
	global allLines, allBlank, allBracket, allComment, allDebug

	paths = os.listdir(directory) 
	for path in paths:
		full = directory+'/'+path
		if path == 'nesc':
			rootPaths = os.listdir(full)
			for rp in rootPaths:
				countNesCRoot(full+'/'+rp, outfile)
		elif path != '.svn':
			if (os.path.isdir(full)):
				countNesCTree(full, outfile)

def countNesC (directory, outfile):
	outfile = open(outfile,'w')
	countNesCTree(directory, outfile)
	outfile.close()

def main(): 
	countJava ('c:/SNEEql/src','javaCount.txt')	
	countNesC ("c:/experiments/V546",'nescCount.txt')
	
if __name__ == "__main__":
    main()
	
