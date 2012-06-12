#!/usr/bin/python
import sys, os, checkTupleCount


def __usage():
	print "Checks that the correct number of tuples are delivered."
	print "usage: python countTuples.py <deliverFile> <tuplesPerEpoch> <numEpochsExpected>"
	print "\tdeliverFile		The file containing the delivered tuples."
	print "\ttuplesPerEpoch		The number of tuples expected per epoch."
	print "\tnumEpochsExpcetd 	The number of epochs expected."	
	sys.exit(2)


if (len(sys.argv) > 4):
	__usage()
	
if (len(sys.argv) < 4):
	__usage()

if (sys.argv[1]=="-h" or sys.argv[1]=="--help"):
	__usage()


traceFilePath = sys.argv[1]
tuplesPerEpoch = int(sys.argv[2])
numEpochsExpected = int(sys.argv[3])
checkTupleCount.checkTupleCount(traceFilePath, tuplesPerEpoch, numEpochsExpected, {})