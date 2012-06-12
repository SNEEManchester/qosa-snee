#!/usr/bin/python
import getopt, logging, sys, SneeqlLib, UtilLib

def usage():
	print '''Usage: nn-sweep.py <parameters>
	--query=[Q2|Q4|Q5] 
	--network_size=[n]
	--network_type=[A|B]
	--num_sources=[3|10|min|maj]
	--optimization-goal=[min_delivery,min_energy,max_lifetime] 
	--use-heuristic=[true|false]
	--output-root=<path>
	'''

queryMap = {'Q2' : 'input/pipes/Q2.txt', 'Q4' : 'input/pipes/QNest4.txt', 'Q5' : 'input/pipes/QNest5.txt'}
networkMap = {'10' : 'input/networks/10-node-topology.xml', '30' : 'scripts/qos-exp/scenarios/30-dense-net.xml', '100' : 'scripts/qos-exp/scenarios/ix-100-dense-net.xml'}
numSourcesMap = {'10_3' : 'input/pipes/10Sites-3Sources-schemas.xml', '10_10' : 'input/pipes/10Sites-10Sources-schemas.xml', '30_min' : 'scripts/qos-exp/scenarios/30-node-min-schema.xml', '30_maj' : 'scripts/qos-exp/scenarios/30-node-maj-schema.xml', '100_min' : 'scripts/qos-exp/scenarios/100-node-min-schema.xml', '100_maj' : 'scripts/qos-exp/scenarios/100-node-maj-schema.xml'}
optGoalMap = {'min_delivery' : 'input/QoS/qos-spec-min-delivery.xml', 'min_energy' : 'input/QoS/qos-spec-min-energy.xml', 'max_lifetime' : 'input/QoS/qos-spec-max-lifetime.xml'}

optRT = None
optMinNN = '0'
optMaxNN = '30'


def parseArgs(args):
	global optQuery, optNetworkSize, optNetworkType, optNumSources, optOptGoal, optUseHeuristic, optOutputRoot, optRT, optMinNN, optMaxNN
	
	try:
		optNames = ["help", "query=", "net-size=", "net-type=", "num-sources=", "opt-goal=", "use-heuristic=", "output-root=", 'rt=', 'min-nn=', 'max-nn=']
		optNames += SneeqlLib.getOptNames();
		
		opts, args = getopt.getopt(args, "h",optNames)
	except getopt.GetoptError, err:
		print str(err)
		usage()
		sys.exit(2)		

	for o, a in opts:

		if (o == "-h" or o== "--help"):
			usage()
			sys.exit()
	
		if (o == '--query'):
			if queryMap.has_key(a):
				optQuery = a;
			else:
				print 'invalid query'
				sys.exit(2)
		if (o == '--net-size'):
			if networkMap.has_key(a):
				optNetworkSize = a;
			else:
				print 'invalid network'
				sys.exit(2)
		if (o == '--net-type'):
			if (a=='A' or a == 'B'):
				optNetworkType = a;
			else:
				print 'invalid network type'
				sys.exit(2)
		if (o == '--num-sources'):
			if numSourcesMap.has_key(optNetworkSize+'_'+a):
				optNumSources = a;
			else:
				print 'invalid network type'
				sys.exit(2)
		if (o == '--opt-goal'):
			if optGoalMap.has_key(a):
				optOptGoal = a;
			else:
				print 'invalid optimization goal'
				sys.exit(2)
		if (o == '--use-heuristic'):
			optUseHeuristic = UtilLib.convertBool(a)
		
		if (o == '--output-root'):
			optOutputRoot = a;
		
		if (o == '--rt'):
			optRT = a	
			
		if (o == '--min-nn'):
			optMinNN = a	

		if (o == '--max-nn'):
			optMaxNN = a

	SneeqlLib.setOpts(opts)	


def invokeQueryOptimizer():
	outputDir = optOutputRoot + '/' + optQuery + '-' + optNetworkSize + 'n-type' + optNetworkType + '-' + optNumSources + 's-' + optOptGoal 
	if optUseHeuristic:
		outputDir += '-heuristic'

	queryFile = queryMap[optQuery]
	networkFile = networkMap[optNetworkSize]
	heteroNet = (optNetworkType == 'B')
	schemaFile = numSourcesMap[optNetworkSize+'_'+optNumSources] 
	qosFile = optGoalMap[optOptGoal]

	fixedParams = ['-qos-acquisition-interval=10000', '-qos-buffering-factor=1', '-targets=tossim1', '-qos-aware-where-scheduling=true', '-haskell-use=true']
	varParams = ['-query='+queryFile, '-network-topology-file='+networkFile, '-where-scheduling-hetero-net='+str(heteroNet), '-schema-file='+schemaFile, '-qos-file='+qosFile, '-output-root-dir='+outputDir, '-delete-old-files=false']
	
	if optRT!=None:
		varParams +=['-routing-tree-file='+optRT]
	
	if optUseHeuristic:
		varParams += ['-where-scheduling-min-nn=0', '-where-scheduling-max-nn=0', '-where-scheduling-heuristic-init-point=true']
	else:
		varParams += ['-where-scheduling-min-nn='+optMinNN, '-where-scheduling-max-nn='+optMaxNN, '-where-scheduling-heuristic-init-point=false']
	
	queryCompilerParams = fixedParams + varParams
	print queryCompilerParams

	exitVal = SneeqlLib.compileQueryParamStr(queryCompilerParams, optQuery)
	if (exitVal != 0):
		return;


def main():
	#parse the command-line arguments
	parseArgs(sys.argv[1:]) 

	SneeqlLib.compileQueryOptimizer()
	
	invokeQueryOptimizer()

if __name__ == "__main__":
	main()
	
	
	