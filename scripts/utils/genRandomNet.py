#!/usr/bin/python
import sys, networkLib, getopt

optOutputFile = "network"
optNumNodes = 10
optXDim = 100
optYDim = 100
optNumRetries = 10


def usage():
	print 'Usage: python genRandomNet.py [-options]'
	print '\t\tto generate a random topology in Sneeql and Avrora formats.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--output-file=<file> (without the extension) \n\tdefault: '+ optOutputFile
	print '--num-nodes=<n> \n\tdefault: '+ str(optNumNodes)
	print '--field-x-dim=<x> \n\tdefault: '+ str(optXDim)
	print '--field-y-dim=<x> \n\tdefault: '+ str(optYDim)
	print '--num-retries=<x> \n\tdefault: '+str(optNumRetries)

	sys.exit(2)

def parseArgs(args):
	global optOutputFile, optNumNodes, optYDim, optXDim, optNumRetries

	try:
		optNames = ["help", "output-file=", "num-nodes=", "field-x-dim=", "field-y-dim=", "num-retries="]

		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError:
		print getopt.GetoptError
		usage()
		sys.exit(2)
		
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		
		if (o == "--output-file"):
			optOutputFile = a
			if (optOutputFile.find('.') > -1):
				print "Please give output filename without the extension"
				sys.exit(2)
			continue

		if (o == "--num-nodes"):
			optNumNodes = int(a)
			continue

		if (o == "--field-x-dim"):
			optXDim= int(a)
			continue

		if (o == "--field-y-dim"):
			optYDim= int(a)
			continue
			
		if (o == "--num-retries"):
			optNumRetries = int(a)
			continue

def main(): 	

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	n = 0
	while 1:
		field = networkLib.generateRandomTopology(numNodes = optNumNodes, xDim= optXDim, yDim = optYDim)
		if field.hasAllNodesConnected():
			break
		if n >= optNumRetries:
			break
		n += 1
		print "Disconnected nodes, retrying...\n\n"

	field.trimEdgesRandomlyToMeetAverageDegree(6) #TODO: unhardcode this	
	field.generateTopFile(optOutputFile+".top")
	field.generateSneeqlNetFile(optOutputFile+".xml")
	field.generateTossimNetFile(optOutputFile+".nss")
	field.drawNetworkGeometry(optOutputFile+".eps")
	if field.hasAllNodesConnected():
		print "SUCCESS: Network generated has all nodes connected after %d tries" % n
	else:
		print "WARNING: Network generated contains disconnected nodes after %d tries" % n
	print "INFO: The average node degree is " + str(field.getAverageNodeDegree())

if __name__ == "__main__":
	main()
