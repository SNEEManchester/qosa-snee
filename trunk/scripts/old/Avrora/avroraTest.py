import os

optAvoraClassPath = "c:/Program Files/Avrora/bin" 
optJava5exe = "/cygdrive/c/Program\ Files/Java/jdk1.5.0_09/bin/java"
#odDirectory = "c:/tmp/Oscilloscope/"
#odDirectory = "c:/tmp/TinyDB/"
#odDirectory = os.getenv('TOSROOT')+"/apps/TinyDBApp/"
odDirectory = "c:/tinyos/cygwin/opt/tinyos-1.x/apps/TinyDBApp/"
#odDirectory = "c:/NescTests/Empty/"

makeOd = "avr-objdump -zhD build/mica2/main.exe > " 
#oneMica = optJava5exe +" avrora.Main -platform=mica2 -seconds=%s %s %s >c:/tmp/avoralog.txt"
#oneMica = optJava5exe +" avrora.Main -platform=mica2 -seconds=%s %s %s"
oneMica = optJava5exe +" avrora.Main -simulation=sensor-network -seconds=%s %s "+odDirectory+"empty.od"
twoMicas = optJava5exe+" avrora.Main -simulation=sensor-network -seconds=%s %s -nodecount=1,1 "+odDirectory+"Test0.od "+odDirectory+"Test1.od"
#tinyDB = optJava5exe +" avrora.Main -simulation=sensor-network -seconds=%s %s -nodecount=10 "+odDirectory+"TinyDB.od >"+odDirectory+"avrora.txt"
tinyDB = optJava5exe +" avrora.Main -simulation=sensor-network -seconds=%s %s -nodecount=10 "+odDirectory+"TinyDB.od"

simulationDuration = 60

def runCommand(string):
	print "running: "+string
	exitVal = os.system(string)
		
def makeOdFile (sourceRoot, odDirectory, odName):
	print "CB:running compiling Mica2"
	print sourceRoot
	os.chdir(sourceRoot)
	if (os.path.exists(sourceRoot+'build/mica2/main.exe')):
		os.remove(sourceRoot+'build/mica2/main.exe');
	
	exitVal = os.spawnl(os.P_WAIT, '/usr/bin/make', '/usr/bin/make', 'mica2')
	
	if not os.path.exists(odDirectory):
		os.makedirs(odDirectory)
	print "CB: Making od file"
	runCommand (makeOd + odDirectory + odName)
	
def main(): 

	#makeOdFile (odDirectory,odDirectory,"TinyDB.od")
	#makeOdFile (odDirectory,odDirectory,"Empty.od")
	#makeOdFile (odDirectory+"mote2/",odDirectory,"Test0.od")
	#makeOdFile (odDirectory+"mote1/",odDirectory,"Test1.od")

	print "running avrora"
	os.chdir(optAvoraClassPath)

	monitorStr = "-monitors="
	monitorStr = monitorStr + "uk.ac.manchester.cs.diasmc.SerialMonitor,"
	monitorStr = monitorStr + "uk.ac.manchester.cs.diasmc.CallLengthMonitor,"
	monitorStr = monitorStr + "energy"
	monitorStr = monitorStr + ",packet"
	#monitorStr = monitorStr + ",uk.ac.manchester.cs.diasmc.CallLengthMonitor"
	monitorStr = monitorStr + " -logfile="+odDirectory+".log "
	
	sensorStr = "-sensor-data=light:0:."
	sensorStr = sensorStr +",light:1:."
	sensorStr = sensorStr +",light:2:."
	sensorStr = sensorStr +",light:3:."
	sensorStr = sensorStr +",light:4:."
	sensorStr = sensorStr +",light:5:."
	sensorStr = sensorStr +",light:6:."
	sensorStr = sensorStr +",light:7:."
	sensorStr = sensorStr +",light:8:."
	sensorStr = sensorStr +",light:9:."
	sensorStr = sensorStr + " "
	
	devicesStr = ""
	#devicesStr = "-devices=0:0:c/tmp/Serial0_in.txt:c/tmp/Serial0_out.txt,1:0:c/tmp/Serial1_in.txt:c/tmp/Serial1_out.txt "

	#portsStr = "-ports=\"\" "
	#portsStr = "-port: long= 2390"
	#portsStr = "-ports=0:0:9001 "
	portsStr = "";
	
	dataStr = "light:0:.,light:1:.,light:2:.,light:3:.,light:4:."

	topologyStr = '-topology='+odDirectory+'9node_network_topology.top '

	#commandStr = twoMicas 
	#commandStr = oneMica
	commandStr = tinyDB
	#,uk.ac.manchester.cs.diasmc.CallLengthMonitor
	commandStr = commandStr % (str(simulationDuration),monitorStr + topologyStr + sensorStr+devicesStr+portsStr+"-colors=false")
	
	runCommand(commandStr)
	
	print "Done"

if __name__ == "__main__":
    main()
	

