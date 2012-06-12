import os

def addInfoTo1File (fileName):
	dataPath = fileName + ".csv"
	print ("copying from "+dataPath)
	count = -1 ##-1 toi remove first line
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		while 1:
			line = dataFile.readline()
			if not line:
				break
			count = count + 1	
		dataFile.close()
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		line = dataFile.readline()
		while 1:
			line = dataFile.readline()
			print line
			if not line:
				break
			subLine = line.split(",")
			print subLine
			test = type((subLine[0]))
			print test
		dataFile.close()
	else:
		print ("Unable to copy "+dataPath)


def arrayFile (fileName):
	dataPath = fileName + ".csv"
	print ("copying from "+dataPath)
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
	count2 = 0
	if os.path.isfile(dataPath):
		dataFile = open(dataPath,"r")
		#Remove first line
		line = dataFile.readline()
		while 1:
			line = dataFile.readline()
			count2 = count2 + 1
			print line
			if not line:
				break
			array[count2] = line.split(",")
		dataFile.close()
	else:
		print ("Unable to copy "+dataPath)
	return array

def main(): 	
	test = arrayFile ("/cygdrive/c/tmp/cycle")
	print test

if __name__ == "__main__":
	main()
