
import sys, getopt, AvroraLib

def usage():
	print 'Usage: python runExperiments.py [-options]s'
	print '\t\tto run the experiment.\n'
	print 'where options include:'
	print '-h, --help\n\tdisplay this message'

	AvroraLib.usage()


def parseArgs(args):

	try:
		optNames = ["help"]
		
		#append the result of getOpNames to all the libraries 
		optNames += AvroraLib.getOptNames();
		
		#TODO: What if option with same name provided by more than one library? Use a set?
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError:
		print getopt.GetoptError
		usage()
		sys.exit(2)
		
	
	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
		sys.exit()


	AvroraLib.setOpts(opts)


def main(): 

	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 
	
	#AvroraLib.compileNesCCode("/cygdrive/c/dias-mc/work2/SNEEql-test/output/Q3test/nesc");
	
	#AvroraLib.generateODs("/cygdrive/c/dias-mc/work2/SNEEql-test/output/Q3test/nesc")

	#AvroraLib.runSimulation("/cygdrive/c/dias-mc/work2/SNEEql-test/output/Q3test/nesc", "/cygdrive/c/tmp/output", "Q0test", 10, simulationDuration=20);

	print AvroraLib.computeEnergyConsumed("/cygdrive/c/tmp/output");

if __name__ == "__main__":
    main()	