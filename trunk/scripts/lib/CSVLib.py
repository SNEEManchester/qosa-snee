

def colNameList(line):
	colNameList = line.rstrip().split(',')
	return colNameList

def line2Dict(line, colNameList):
	dict = {}
	vals = line.rstrip().split(',')
	for i in range(0,len(vals)):
		dict[colNameList[i]] = vals[i]
	return dict

def line(dict, colNameList):
	s = ""
	first = True
	for col in colNameList:
		if first:
			first = False
		else:
			s += ","
		s+=str(dict[col])
	return s + "\n"

def header(colNameList):
	return ",".join(colNameList) + "\n"

