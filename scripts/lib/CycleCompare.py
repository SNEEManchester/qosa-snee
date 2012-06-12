import SneeqlLib, os

optOutputRoot = "/cygdrive/c/measurements" 

def concatFiles (mainDir, fileName, measurementName):
	fileCount = 0
	maxMote= 0
	#print mainDir
	allFile = open (mainDir + "/" + fileName + measurementName + ".csv", "w")
	for dir in os.listdir(mainDir):
		secondDir = mainDir+"/"+dir
		#print secondDir
		if os.path.isdir(secondDir):
			for dir in os.listdir(secondDir):
				if os.path.isdir(secondDir + "/"+ dir):
					dataPath = secondDir + "/"+ dir + "/avrora/"+fileName + ".csv"
					#print dataPath
					if os.path.isfile(dataPath):
						allFile.write(secondDir + dir + "\n")
						dataFile = open(dataPath,"r")
						moteCount = -2; #-2 to ignore title line and call mote0 0
						fileCount += 1
						while 1:
							line = dataFile.readline()
							if not line:
								break
							moteCount += 1	
							#print line
							if len(line) > 1:
								allFile.write(line)
						allFile.write("\n")	
						if (moteCount > maxMote):
							maxMote = moteCount
						dataFile.close()
	summarize (allFile, moteCount, fileCount)				
		
def copyFile (runDir, allFile, fileName):
	allFile.write(runDir+"\n")
	dataPath = runDir + "/avrora1/"+fileName + ".csv"
	#print ("copying from "+dataPath)
	count = 0 
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		while 1:
			line = dataFile.readline()
			if not line:
				break
			count = count + 1	
		dataFile.close()
	array = range(0, count)	
	count = -1;
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		while 1:
			line = dataFile.readline()
			if not line:
				break
			count = count + 1	
			array[count] = line.split(",")
			#print ("£"+line)
			if len(line) > 1:
				allFile.write(line)
		#allFile.write("\n")	
		dataFile.close()
	else:
		print ("Unable to copy "+dataPath)
	return array

def copyAFile (runDir, allFile, fileName, moteNum):
	print runDir
	dataPath = runDir + "/avrora1/"+fileName + ".csv"
	#print ("copying from "+dataPath)
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#ignore first line
		line = dataFile.readline()
		while 1:
			line = dataFile.readline()
			if not line:
				break
			#print ("£"+line)
			if len(line) > 1:
				newLine = line.replace ("Mote0",("Mote"+str(moteNum)))	
		#allFile.write("\n")	
		dataFile.close()

def copyFile2 (runDir, allFile, fileName, text = None):
	allFile.write(runDir+"\n")
	dataPath = runDir + "/"+fileName + ".csv"
	#print ("copying from "+dataPath)
	count = 0 
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		while 1:
			line = dataFile.readline()
			if not line:
				break
			count = count + 1	
		dataFile.close()
	array = range(0, count)	
	count = -1;

	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		while 1:
			line = dataFile.readline()
			#print line
			if not line:
				break
			if len(line) > 1:
				if text != None:
					line = line.replace ("Mote0",text)	
				allFile.write(line)
				count = count + 1	
				array[count] = line.split(",")
	#allFile.write("\n")	
		dataFile.close()
	else:
		print ("Unable to copy "+dataPath)
	return array	

def copyAFile2 (runDir, allFile, fileName, moteNum, text = None):
	dataPath = runDir + "/"+fileName + ".csv"
	#print ("copying from "+dataPath)
	count = 0 
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		while 1:
			line = dataFile.readline()
			if not line:
				break
			count = count + 1	
		dataFile.close()
	array = range(0, count)	
	count = -1;

	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#ignore first line
		line = dataFile.readline()
		while 1:
			line = dataFile.readline()
			#print line
			if not line:
				break
			if len(line) > 1:
				if text != None:
					line = line.replace ("Mote0",text)	
				count = count + 1	
				array[count] = line.split(",")
				allFile.write(line)
		dataFile.close()
	return array	

def summarize (allFile, maxMote, fileCount):
	if (fileCount == 1):
		summarize1(allFile, maxMote)
	elif (fileCount == 2):
		summarize2(allFile, maxMote)
	elif (fileCount == 4):
		summarize4(allFile, maxMote)
	else:
		allFile.close()

def summarize1 (allFile, moteCount):
	#print "summarize1"
	allFile.write ("Delta \n")
	allFile.write ("StartUp,")
	for l in "bcdefghijklmnop":
		allFile.write ('+' + l + str(moteCount+6) + '-' + l + str(moteCount*2+5) +',')
	allFile.write('\n')	
	for i in range (1,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(i+3) + '-' + l + str(i+2) +',')
		allFile.write('\n')	
	allFile.close()			

def summarize2 (allFile, moteCount):
	#print "summarize2"
	allFile.write ("Difference \n")
	for i in range (0,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(i+3) + '-' + l + str(i+moteCount+6) +',')
		allFile.write('\n')	
	allFile.write ("Delta \n")
	allFile.write ("StartUp,")
	for l in "bcdefghijklmnop":
		allFile.write ('+' + l + str(moteCount*2+7+2) + '-' + l + str(moteCount*4+10) +',')
	allFile.write('\n')	
	for i in range (1,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(moteCount*2+i+8) + '-' + l + str(moteCount*2+i+7) +',')
		allFile.write('\n')	
	allFile.close()			

def summarize4 (allFile, moteCount):
	#print "summarize4"
	allFile.write ("Check Difference \n")
	for i in range (0,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(i+3) + '-' + l + str(i+moteCount+6) +',')
		allFile.write('\n')	
	allFile.write ("Control Difference \n")
	for i in range (0,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(i+moteCount*2+9)+ '-' + l + str(i+moteCount*3+12) +',')
		allFile.write('\n')	
	allFile.write ("Test Difference \n")
	for i in range (0,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(i+moteCount*4+14)+ '-' + l + str(i+moteCount*5+16) +',')
		allFile.write('\n')	
	allFile.write ("Delta \n")
	allFile.write ("StartUp,")
	for l in "bcdefghijklmnop":
		allFile.write ('+' + l + str(moteCount*6+18) +',')
	allFile.write('\n')	
	for i in range (1,moteCount+1):
		allFile.write ("Mote"+str(i)+",")
		for l in "bcdefghijklmnop":
			allFile.write ('+' + l + str(moteCount*6+i+18) + '-' + l + str(moteCount*6+i+17) +',')
		allFile.write('\n')	
	allFile.close()			

def concatDataFiles (mainDir, measurementName = ""):
	concatFiles (mainDir, "cycle", measurementName);
	concatFiles (mainDir, "energy", measurementName);
	concatFiles (mainDir, "Green", measurementName);
	concatFiles (mainDir, "Red", measurementName);
	concatFiles (mainDir, "size", measurementName);
	concatFiles (mainDir, "Yellow", measurementName);
	if SneeqlLib.optNescGreenExperiment != None:
		concatFiles (mainDir, SneeqlLib.optNescGreenExperiment, measurementName);
	if SneeqlLib.optNescYellowExperiment != None:
		concatFiles (mainDir, SneeqlLib.optNescYellowExperiment, measurementName);
	if SneeqlLib.optNescRedExperiment != None:
		concatFiles (mainDir, SneeqlLib.optNescRedExperiment, measurementName);

def checkFile (mainDir, fileName, measurementName):
	allFile = open (mainDir + "/" + fileName + measurementName + ".csv", "w")
	allFile.write ("interrupted")
	allFile.close()			

def checkFiles (mainDir, measurementName = ""):
	if os.path.isdir(mainDir):	
		checkFile (mainDir, "cycle", measurementName);
		checkFile (mainDir, "energy", measurementName);
		checkFile (mainDir, "Green", measurementName);
		checkFile (mainDir, "Red", measurementName);
		checkFile (mainDir, "size", measurementName);
		checkFile (mainDir, "Yellow", measurementName);
		if SneeqlLib.optNescGreenExperiment != None:
			checkFile (mainDir, SneeqlLib.optNescGreenExperiment, measurementName);
		if SneeqlLib.optNescYellowExperiment != None:
			checkFile (mainDir, SneeqlLib.optNescYellowExperiment, measurementName);
		if SneeqlLib.optNescRedExperiment != None:
			checkFile (mainDir, SneeqlLib.optNescRedExperiment, measurementName);

def main(): 	
	mainDir = optOutputRoot + "/Producer_all"	
	SneeqlLib.optNescGreenExperiment ="GET_PRODUCER_TIMES_EXPERIMENT"
	concatDataFiles (mainDir, "gh");

if __name__ == "__main__":
	main()
   