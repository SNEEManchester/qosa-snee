import os,re

def removeFiles(rootDir):
	#print "in"
	#print rootDir
	for dir in os.listdir(rootDir):
		#m = re.match("All", dir)
		#if (m != None):
		#	allDir = dir
		#if (dir == "build"):
		#	print dir			
		if os.path.isdir(rootDir + "/" + dir):			
			removeFiles(rootDir + "/" + dir)
			os.rmdir (rootDir + "/" + dir)
		else:
			os.remove(rootDir + "/" + dir)


def removeBuildDirs(rootDir):
	#print "in"
	#print rootDir
	for dir in os.listdir(rootDir):
		#m = re.match("All", dir)
		#if (m != None):
		#	allDir = dir
		if (dir == "build"):
			removeFiles(rootDir + "/" + dir)
			os.rmdir (rootDir + "/" + dir)
		elif os.path.isdir(rootDir + "/" + dir):			
			removeBuildDirs(rootDir + "/" + dir)
		#else:
		#	print dir

def main(): 	
	removeBuildDirs("/cygdrive/c/measurements")
	print("Done");

if __name__ == "__main__":
	main()
