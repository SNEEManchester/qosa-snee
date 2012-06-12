#!/usr/bin/python
import sys, SneeqlLib, getopt

optOutputFile = "schema.xml"
optNumNodes = 10
optNumSources = None
optStreams =  {"InFlow" : ["pressure","temperature"]}

def usage():
	print 'Usage: genPhySchema.py [-options]'
	print '\t\tto generate a random physical schema for Sneeql optimizer.\n'
	print 'where options include:'
	print '-h, --help\tdisplay this message'
	print '--output-file=<file> (without the extension) \n\tdefault: '+ optOutputFile
	print '--num-nodes=<n> \n\tdefault: '+ str(optNumNodes)
	print '--num-sources=<x> \n\tdefault: [Random]'
	print '--streams=[stream1:attr1,...streamN:attrN]\n\tdefault: ' + str(optStreams)
	
	sys.exit(2)

def parseArgs(args):
	global optOutputFile, optNumNodes, optNumSources, optStreams

	try:
		optNames = ["help", "output-file=", "num-nodes=", "num-sources=", "streams="]

		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)
		
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
		
		if (o == "--output-file"):
			optOutputFile = a
			continue

		if (o == "--num-nodes"):
			optNumNodes = int(a)
			continue

		if (o == "--num-sources"):
			optNumSources = int(a)
			continue

		if (o == "--streams"):

			#Note: This assumes one attribute per stream
			optStreams = {}
			streams= a.split(",")
			
			for i in streams:
				stream = i.split(":")
				streamName = stream[0]
				streamAttr = stream[1]
				optStreams[streamName] = streamAttr
				
			continue

def main(): 	

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 

	SneeqlLib.generateRandomSchema(optStreams, optOutputFile, True, optNumNodes, optNumSources)

if __name__ == "__main__":
	main()
