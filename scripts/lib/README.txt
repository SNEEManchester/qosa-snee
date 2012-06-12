SNEEql Scripting libraries
==========================

The following libraries are currently available, with the corresponding functionality:

a. SNEEqlLib.py 	-- running the SNEEql query optimizer
b. TossimLib.py 	-- running query plan simulations using Tossim
c. AvroraLib.py 	-- running query plan simulations using Avrora
d. UtilLib.py 		-- general functionality
e. checkTupleCount.py 	-- checking query results (by counting number of tuples)
f. GraphData.py 	-- plotting graphs using pgnuplot.py

Users of the libraries should note the following:
=================================================

1. These libraries assume the existence of an environment varible, SNEEQLROOT, which points to the root directory of SNEEql.  Under Cygiwn, this would be declared thus:

$	declare -x SNEEQLROOT="/cygdrive/c/dias-mc/work2/SNEEql-test"

2. In order to make these libaries visible for other modules, the PYTHONPATH environment variable needs to be set using the following syntax:

$	declare -x PYTHONPATH="$PYTHONPATH:$SNEEQLROOT/scripts/lib"

3. Use of the logger.  All libraries may be used with a logger (in which case messages are written to a logfile as well as appearing in the terminal) or, alternatively, without a logger (in which case messages are only written to the terminal).  It is envisaged that a logfile will be useful for batch processing applications (e.g., experiment scripts and periodic regression tests), whereas it is unlikely to be useful for command-line utility scripts.  In order to write to a logfile, library users should call the registerLogger method of the library.  Note that within the library, all messages should be delivered using the report(), reportWarning() and reportError() methods.

4. Command line options.


Authors of new libraries should note the following:
===================================================