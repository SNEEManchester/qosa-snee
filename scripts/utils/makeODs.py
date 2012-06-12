#!/usr/bin/python
import sys, os, AvroraLib


def __usage():
	print "usage: python makeODs.py [numNodes]"
	print "\tnumNodes	Optional.  The number of nodes in simulation."
	print "\t		If specified, gives list of od files to pass to Avrora."
	sys.exit(2)

if (len(sys.argv) > 2):
	__usage()
	
if (len(sys.argv) > 1):
	if (sys.argv[1]=="-h" or sys.argv[1]=="--help"):
		__usage()

if (not os.getcwd().endswith("avrora")):
	print "This script should be run from the avrora directory"
	sys.exit(-1)

AvroraLib.compileNesCCode(os.getcwd())
AvroraLib.generateODs(os.getcwd())
if (len(sys.argv) > 1):
	numNodes = int(sys.argv[1])
	print AvroraLib.getODs(os.getcwd(), numNodes)