import fileinput, re
#import sys, string, os, re, fileinput

#Generates the files needed to run gnuplot and invokes gnuplot
class MonitorData(object):
    
	def __init__(self, id):
		self.id = id
	    	
	def totalEnergy(self):
		return self.cpu + self.yellow + self.green + self.red + self.radio + self.sensorBoard + self.flash	

	def CPU(self):
		#print self.id
		return self.cpu 

	def Active(self):
		return self.active
		
	def Radio(self):
		return self.radio 
	
	def Transmit0(self):
		return self.transmit0
		
 	def Tarnsmit15(self):
 		return self.transmit15
 		
	def Packets(self):
		return self.packets

	def LEDs(self):
		return self.yellow + self.green + self.red

	def SensorBoard(self):
		return self.sensorBoard
	
	def Flash(self):
		return self.flash	
	
	def noLEDenergy(self):
		return self.cpu + self.radio + self.sensorBoard + self.flash	
		
	def WorkingEnergy(self):
		return self.active + self.transmit0 + self.transmit15

	def readData (self, inputFile):
		#global self.packets, self.cpu, self.yellow, self.green, self.red, self.radio, self.sensorBoard, self.flash
        
 		packetsPattern = re.compile ('Packets sent: (\d.+) ')
    
		CPUPattern = re.compile ('CPU: (.+) Joule')
		ActivePattern = re.compile ('.+Active: (.+) Joule, (.+) cycles')
   		yellowPattern = re.compile ('Yellow: (.+) Joule')
		greenPattern = re.compile ('Green: (.+) Joule')
		redPattern = re.compile ('Red: (.+) Joule')
		radioPattern = re.compile ('Radio: (.+) Joule')
   		                               #   Transmit (Tx):        0:   : 9.127822265625E-4 Joule, 356072 cycles
   		Transmit0Pattern = re.compile ('.+Transmit .+0:.+:(.+)Joule,(.+)cycles')
   		Transmit15Pattern = re.compile ('.+Transmit .+15:.+:(.+)Joule,(.+)cycles')
		sensorBoardPattern = re.compile ('SensorBoard: (.+) Joule')
		flashPattern = re.compile ('flash: (.+) Joule')
	
		self.packets = 0
		
		self.transmit15 = 0

		for line in fileinput.input([inputFile]):
			match = packetsPattern.match(line)
			#print line
			if match:
				self.packets = int(match.group(1))
				#print str(self.id) + ': Packets sent = ' + str(self.packets) 
			match = CPUPattern.match(line)
			if match:
				self.cpu = float(match.group(1))
				#print str(self.id) + ': CPU = ' + str(self.cpu) 
			match = ActivePattern.match(line)
			if match:
				self.active = float(match.group(1))
				print str(self.id) + ': active = ' + str(self.active) 
			match = yellowPattern.match(line)
			if match:
				self.yellow = float(match.group(1))
				#print str(self.id) + ': Yellow = ' + str(self.yellow) 
			match = greenPattern.match(line)
			if match:
				self.green = float(match.group(1))
				#print str(self.id) + ': Green = ' + str(self.green) 
			match = redPattern.match(line)
			if match:
				self.red = float(match.group(1))
				#print str(self.id) + ': Red = ' + str(self.red) 
			match = radioPattern.match(line)
			if match:
				self.radio = float(match.group(1))
				print str(self.id) + ': Radio = ' + str(self.radio) 
			match = Transmit0Pattern.match(line)
			if match:
				self.transmit0 = float(match.group(1))
				print str(self.id) + ': Transmit0 = ' + str(self.transmit0) 
			match = Transmit15Pattern.match(line)
			if match:
				self.transmit15 = float(match.group(1))
				print str(self.id) + ': Transmit15 = ' + str(self.transmit15) 
			match = sensorBoardPattern.match(line)
			if match:
				self.sensorBoard = float(match.group(1))
				#print str(self.id) + ': SensorBoard = ' + str(self.sensorBoard) 
			match = flashPattern.match(line)
			if match:
				self.flash = float(match.group(1))
				#print str(self.id) + ': flash = ' + str(self.flash) 
	
		#print ("Total Energy ="+str(MonitorData.totalEnergy(self)))
		#print ("No LED Energy ="+str(MonitorData.noLEDenergy(self))+'\n')

