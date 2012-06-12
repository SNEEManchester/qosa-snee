#!/usr/bin/python
import CSVLib, os, networkLib, re

colNames = None
first = True

latexSource = ''
optValidScenarioList = ['0', '1', '4', '5', '6', '7', '8', '12', '13', '15', '17', '18', '19', '25', '26'] #28 also works, removed to make 15 scenarios
optScenarioMap = {'0' : '1', '1': '2', '4' : '3', '5' : '4', '6' : '5', '7' : '6', '8' : '7', '12' : '8', '13' : '9', '15' : '10', '17' : '11', '18' : '12', '19' : '13', '25' : '14', '26' : '15'}

for line in open(os.getcwd()+os.sep+"scenarios.csv", 'r'):
	if first:
		scenarioAttrCols = CSVLib.colNameList(line)
		scenarioAttrCols += ["timeStamp"]
		first = False
		continue

	scenarioAttr = CSVLib.line2Dict(line, scenarioAttrCols)
	if not str(scenarioAttr['scenarioId']) in optValidScenarioList:
		continue

	thesisScenarioId = optScenarioMap[str(scenarioAttr['scenarioId'])]
	latexSource += "\\subsection*{Scenario %s}\n\n" % (thesisScenarioId)

	latexSource += '\\subsubsection{Query}\n\n\\begin{verbatim}\n'
	queryId = scenarioAttr['queryId']
	f = open(queryId+'.txt', 'r')
	latexSource += ' '.join(f.readlines())
	latexSource += '\\end{verbatim}\n'
	f.close()
	
	networkId = scenarioAttr['networkId']
	f = networkLib.parseAvroraTopFile(networkId+'.top')
	f.drawNetworkGeometry(networkId+'.eps')
	os.system('ps2pdf -dEPSCrop '+networkId+'.eps')

	latexStr = '''
\\subsubsection{Network}\n\n
\\includegraphics[width=0.8\\linewidth]{results/scenarios1/%s} \\\\
rValue=%s.
''' 
	latexSource += latexStr % (networkId, str(scenarioAttr['rValue']))

	latexSource += '\\subsubsection{Physical Schema}\n\n\\begin{verbatim}\n'
	schemaId = scenarioAttr['schemaId']
	extentName = ''
	extentSource = ''
	for l in open(schemaId+'.xml'):
		p = re.compile('\s*<stream name="(.+)">')
		m = p.match(l)
		if m != None:
			extentName = m.group(1)

		p = re.compile('\s*<sites>(.+)</sites>')
		m = p.match(l)
		if m != None:
			extentSource = m.group(1)
			latexSource += extentName + ' {' + extentSource + '}\n'
			extentName = ''
			extentSource = ''

	latexSource += '\\end{verbatim}\n\n\\noindent'
	latexSource += scenarioAttr['percentSources']+'\% of nodes in network are sources.\n\n'


os.system('rm *.eps')

fo = open("scenarios.tex", "w")
fo.writelines(latexSource)
fo.close()
