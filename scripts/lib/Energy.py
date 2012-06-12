import os, re

cpu = "x"
active = "0"
idle ="0"
adc = "0"
ps = "0"
es = "0"
radio ="0"
off ="0"
down ="0"
crys ="0"
cb ="0"
rx ="0"
tx ="0"
activeC = "0"
idleC ="0"
adcC = "0"
psC = "0"
esC = "0"
offC ="0"
downC ="0"
crysC ="0"
cbC ="0"
rxC ="0"
txC ="0"

logger = None

#Registers a logger for this library
def registerLogger(l):
	global logger
	logger = l

#Ouput info message to screen and logger if applicable
def report(message):
	if (logger != None):
		logger.info (message)
	print message


#Ouput warning message to screen and logger if applicable
def reportWarning(message):
	if (logger != None):
		logger.warning(message)
	print message


#Ouput error message to screen and logger if applicable
def reportError(message):
	if (logger != None):
		logger.error(message)
	print message


def detailLine(line, text, energy):
	m = re.search(text+":( +)(\d+).(\d+) Joule", line)
	if (m != None):
		#print text +"  "+m.group(2)+"."+m.group(3)
		return (m.group(2)+"."+m.group(3))

	m = re.search(text+":( +)(\d+).(\d+)E-(\d+) Joule", line)
	if (m != None):
		#report (text +" "+m.group(2)+"."+m.group(3)+"E-"+m.group(4))
		return (m.group(2)+"."+m.group(3)+"E-"+m.group(4))
	
	return energy

def cpuDetailLine(line, text, energy, cycle):
	m = re.search("   "+text+":( +)(\d+).(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#report (text +"  "+m.group(2)+"."+m.group(3)+ " cycles ="+m.group(5))
		return ((m.group(2)+"."+m.group(3)),(m.group(5)))

	m = re.search("   "+text+":( +)(\d+).(\d+)E-(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#report (text +" "+m.group(2)+"."+m.group(3)+"E-"+m.group(4)+ " cycles ="+m.group(6))
		return ((m.group(2)+"."+m.group(3)+"E-"+m.group(4)),(m.group(6)))

	return (energy, cycle)
	
def radioDetailLine(line, text, energy, cycle):
	m = re.search("   "+text+":( +):( +)(\d+).(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#report (text +"  "+m.group(3)+"."+m.group(4)+ " cycles ="+m.group(6))
		return ((m.group(3)+"."+m.group(4)), (m.group(6)))

	#   Power Down:           : 5.968244930013021E-5 Joule, 733377937 cycles
	m = re.search("   "+text+":( +):( +)(\d+).(\d+)E-(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#report (text +" "+m.group(3)+"."+m.group(4)+"E-"+m.group(5)+ " cycles ="+m.group(7))
		return ((m.group(3)+"."+m.group(4)+"E-"+m.group(5)), (m.group(7)))

	return (energy, cycle)

def readLine(line, energyFile, cycleFile):
	global cpu, active, idle, adc, ps, es, radio, off, down, crys, cb, rx, tx 
	global activeC, idleC, adcC, psC, esC, offC, downC, crysC, cbC, rxC, txC 
	
	#=={ Monitors for node 3 }=====================================================
	m = re.search("=={ Monitors for node (\d+) }(=+)", line)
	if (m != None):
		i = int(m.group(1))
		if (i>0):
			energyFile.write (cpu + "," + active + "," + idle + "," + adc + ","+ ps + "," +	es + ",")
			energyFile.write (radio +","+ off +","+	down +","+ crys +","+ cb +","+ rx + "," + tx)
			cycleFile.write (activeC + "," + idleC + ","+ adcC + ","+ psC + "," +	esC + ",")
			cycleFile.write (offC + ","+	downC +","+ crysC +","+ cbC +","+ rxC + "," + txC)

			#report (cpu + "," + active + "," + idle + "," + adc + ","+ ps + "," +	es + ",")
			#report (radio +","+ off +","+	down +","+ crys +","+ cb +","+ rx + "," + tx)
			#report (activeC + "," + idleC + "," + adcC + ","+ psC + "," +	esC + ",")
			#report (offC + ","+	downC +","+ crysC +","+ cbC +","+ rxC + "," + txC)

			cpu = "0"
			active = "0"
			idle ="0"
			adc = "0"
			ps = "0"
			es = "0"
			radio ="0"
			off ="0"
			down ="0"
			crys ="0"
			cb ="0"
			rx ="0"
			tx ="0"
			activeC = "0"
			idleC ="0"
			adcC = "0"
			psC = "0"
			esC = "0"
			offC ="0"
			downC ="0"
			crysC ="0"
			cbC ="0"
			rxC ="0"
			txC ="0"
			
		print "node "+ str(i)
		energyFile.write ("\n Mote"+str(i)+",")
		cycleFile.write ("\n Mote"+str(i)+",")
	
	cpu = detailLine (line, "CPU", cpu)
	(active, activeC) = cpuDetailLine(line, "Active",active, activeC)
	(idle, idleC) = cpuDetailLine(line, "Idle", idle, idleC)
	(adc, adcC) = cpuDetailLine(line, "ADC Noise Reduction", adc, adcC)
	(ps, psC) = cpuDetailLine(line, "Power Save", ps, psC)
	(es, esC) = cpuDetailLine(line, "Extended Standby",es, esC)

	radio = detailLine (line, "Radio", radio)
	(off, offC) = radioDetailLine(line, "Power Off", off, offC)
	(down, downC) = radioDetailLine(line, "Power Down", down, downC)
	(crys, crysC) = radioDetailLine(line, "Crystal", crys, crysC)
	(cb, cbC) = radioDetailLine(line, "Crystal \+ Bias", cb, cbC)
	(rx, rxC) = radioDetailLine(line, "Receive \(Rx\)", rx, rxC)

	#   Transmit (Tx):        0:   : 0.0016917176513671875 Joule, 659931 cycles
	m = re.search("   Transmit \(Tx\):( +)(\d+):( +):( +)(\d+).(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#print "Transmit = "+m.group(5)+"."+m.group(6)+ " power ="+m.group(2)+ " cycles ="+m.group(8)
		tx= (m.group(5)+"."+m.group(6))
		txC = (m.group(8)+","+ m.group(2))

	m = re.search("   Transmit \(Tx\):( +)(\d+):( +):( +)(\d+).(\d+)E-(\d+) Joule,( +)(\d+) cycles", line)
	if (m != None):
		#print "Transmit = "+m.group(5)+"."+m.group(6)+ " power ="+m.group(2)+ " cycles ="+m.group(9)
		tx = (m.group(5)+"."+m.group(6)+"E-"+m.group(7))
		txC = (m.group(9)+","+m.group(2))

def getEnergy (avroraDir):
	global cpu, active, idle, ps, es, radio, off, down, crys, cb, rx, tx 
	global activeC, idleC, psC, esC, offC, downC, crysC, cbC, rxC, txC 

	os.chdir(avroraDir);
	energyFile = open("Energy.csv","w")
	energyFile.write("Mote, Total, Millis, CPU, Active, Idle, ADC Noise reduction, Power Save, Extended Standby, Radio, Power Off, Power Down, Crystal, Crystal \+ Bias, Receive (Rx), Transmit (Tx)")

	cycleFile = open("Cycle.csv","w")
	cycleFile.write("Mote, Active, Idle, ADC Noise reduction, Power Save, Extended Standby, Power Off, Power Down, Crystal, Crystal \+ Bias, Receive (Rx), Transmit, TX")

	inFile =  open("avrora-out.txt")
	while 1:
		line = inFile.readline()
		if not line:
			break
		readLine(line, energyFile, cycleFile)
		
	total = float(cpu)+ float(radio)	
	energyFile.write (str(total) + "," + str(total * 1000000) + ",")
	energyFile.write (cpu + "," + active + "," + idle + "," + adc + ","+ ps + "," +	es + ",")
	energyFile.write (radio +","+ off +","+	down +","+ crys +","+ cb +","+ rx + "," + tx)
	cycleFile.write (activeC + "," + idleC + ","+ adcC + ","+ psC + "," + esC + ",")
	cycleFile.write (offC + ","+	downC +","+ crysC +","+ cbC +","+ rxC + "," + txC)

	#report (cpu + "," + active + "," + idle + "," + adc + ","+ ps + "," +	es + ",")
	#report (radio +","+ off +","+	down +","+ crys +","+ cb +","+ rx + "," + tx)
	#report (activeC + "," + idleC + "," + adcC + ","+ psC + "," +	esC + ",")
	#report (offC + ","+	downC +","+ crysC +","+ cbC +","+ rxC + "," + txC)
	energyFile.close()
	cycleFile.close();

def main(): 	
		
	getEnergy ("c:/tmp");

if __name__ == "__main__":
	main()

