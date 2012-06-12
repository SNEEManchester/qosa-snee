#!/usr/bin/python
import sys, AvroraLib

def __usage():
	print "usage: python readPackets.py <inputFile> [<outputFile>]"
	print "\tinputFile	The raw packet data output from the serial port."
	print "\toutputFile	The output file.  Omit this option to write to stdout."
	sys.exit(2)

if (len(sys.argv) == 1 or len(sys.argv) > 3):
	__usage()
	
if (sys.argv[1]=="-h" or sys.argv[1]=="--help"):
	__usage()

if (len(sys.argv) > 2):
	AvroraLib.readDeliveredPackets(sys.argv[1], sys.argv[2])
else:
	AvroraLib.readDeliveredPackets(sys.argv[1])