#!/usr/bin/python
import os, UtilLib, re, fileinput, sys

#copy the files from the top-level directory of an experiment (i.e., graphs, logs)
	
def visitDir(currentDir, outputDir):

	os.chdir(currentDir)
	for f in os.listdir(currentDir):
		if os.path.isdir(currentDir+'/'+f):
#			m = re.match("([23][abcde])\_scenario([\d])-(.*)", f) #
#			if (m != None):
#				exp = m.group(1)
#				scenario = m.group(2)
#				label = m.group(3)
#			
#				print 'exp='+exp+' scenario='+scenario +' label='+label
				
			os.chdir(currentDir+'/'+f)
			os.mkdir(outputDir+'/'+f)
			for g in os.listdir(currentDir+'/'+f):
				if not os.path.isdir(currentDir+'/'+f+'/'+g):
					commandStr = 'cp '+g+' '+outputDir+'/'+f+'/'+g
					print commandStr
					os.system(commandStr)

def main():
	visitDir(os.getcwd(),sys.argv[1])


if __name__ == "__main__":
	main()
