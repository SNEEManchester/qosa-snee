#!/usr/bin/python
import sys, networkLib, getopt

optInputFile = "network"
optXDim = 100
optYDim = 100
optRTFiles = None

def usage():
	print 'Usage: python coord2Net.py [-options]'
	print '\t\tGenerates a Sneeql and Tossim format network file from a Avora co-ordinate-based file.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--input-file=<file>  \n\tdefault: '+ optInputFile
	print '--field-x-dim=<x> \n\tdefault: '+ str(optXDim)
	print '--field-y-dim=<x> \n\tdefault: '+ str(optYDim)

	sys.exit(2)
	

def parseArgs(args):
	global optInputFile, optXDim, optYDim, optRTFiles

	try:
		optNames = ["help", "input-file=", "power-scaling-factor", "field-x-dim=", "field-y-dim=", "rt-files="]

		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError:
		print getopt.GetoptError
		usage()
		sys.exit(2)
		
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		
		if (o == "--input-file"):
			optInputFile = a

		if (o == "--field-x-dim"):
			optXDim= int(a)
			continue
			
		if (o == "--field-y-dim"):
			optYDim= int(a)
			continue
			
		if (o == "--rt-files"):
			optRTFiles = a
			continue

			
def main(): 	

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	#TODO: unhardcode the zeros
	field = networkLib.parseAvroraTopFile(optInputFile, optXDim, optYDim, 0, 0, rtFiles = optRTFiles)
	
	sneeqlNetOutputFile = optInputFile.replace(".top", ".xml")
	field.generateSneeqlNetFile(sneeqlNetOutputFile)
	tossimNetOutputFile = optInputFile.replace(".top", ".nss")
	field.generateTossimNetFile(tossimNetOutputFile)
	if field.hasAllNodesConnected():
		print "SUCCESS: Network has all nodes connected"
	else:
		print "WARNING: Network contains disconnected nodes"


if __name__ == "__main__":
	main()
