/****************************************************************************\ 
*                                                                            *
*  SNEE (Sensor NEtwork Engine)                                              *
*  http://code.google.com/p/snee                                             *
*  Release 1.0, 24 May 2009, under New BSD License.                          *
*                                                                            *
*  Copyright (c) 2009, University of Manchester                              *
*  All rights reserved.                                                      *
*                                                                            *
*  Redistribution and use in source and binary forms, with or without        *
*  modification, are permitted provided that the following conditions are    *
*  met: Redistributions of source code must retain the above copyright       *
*  notice, this list of conditions and the following disclaimer.             *
*  Redistributions in binary form must reproduce the above copyright notice, *
*  this list of conditions and the following disclaimer in the documentation *
*  and/or other materials provided with the distribution.                    *
*  Neither the name of the University of Manchester nor the names of its     *
*  contributors may be used to endorse or promote products derived from this *
*  software without specific prior written permission.                       *
*                                                                            *
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
*  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
*  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
*  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
*  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
*  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
*  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
*  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
*  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
*  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
*                                                                            *
\****************************************************************************/

package uk.ac.manchester.cs.diasmc.querycompiler;
  
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JFrame;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.logger.LoggerSetup;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.algorithmSelection.PhysicalOptimization;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.CodeGenTarget;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.NesCGeneration;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Topology;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.TopologyReader;
import uk.ac.manchester.cs.diasmc.querycompiler.mqe.MQE;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SNEEqlLexer;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SNEEqlParser;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SyntaxUtils;
import uk.ac.manchester.cs.diasmc.querycompiler.partitioning.Partitioning;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpecReader;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Agenda;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.AgendaException;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.FAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.LAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.PAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.QueryPlan;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.RT;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ScoredCandidateList;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.RTAcquisitionIntervalScoringFunction;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.RTDeliveryTimeScoringFunction;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.RTEnergyScoringFunction;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.scoringfns.RTLifetimeScoringFunction;
import uk.ac.manchester.cs.diasmc.querycompiler.routing.Routing;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.operator_rewriting.HaskellOperatorRewriter;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.rewriting.ClassicalCardinalityCalculator;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.rewriting.LogicalOptimiser;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Translator;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.WhenScheduling;
import uk.ac.manchester.cs.diasmc.querycompiler.whereScheduling.WhereScheduling;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;
  
/**
 * Main class for SNEEql Query Optimizer.
 * Compiles a Declarative sensor network query into executable code.
 */
public class QueryCompiler {
	
    /**
     * Logger for this class.
     */
	private static Logger logger = 
		Logger.getLogger(QueryCompiler.class.getName());

	/**
	 *  Metadata relating to the sensor network, such as topology and site 
	 *  resources.
	 */
	private static Topology sensornetMetadata;

	/**
	 * The database schema being used. 
	 */
	private static SchemaMetadata schemaMetadata;
	
	/**
	 * The QoS for each query.
	 */
	private static ArrayList<QoSSpec> qosCollection 
		= new ArrayList<QoSSpec>();
	
	/**
	 * Output directory for current query plan being generated.
	 */
	public static String queryPlanOutputDir;
	
    /**
     * Invokes the parser. The result is an Abstract Syntax Tree. 
     */ 
    private static AST parseQuery(final InputStream inputStream) {
    	try	{
    		final SNEEqlLexer l 
    		   = new SNEEqlLexer(inputStream);
    		final SNEEqlParser p 
    		   = new SNEEqlParser(l);
    		//set AST classes for various node types
    		p.sneeqlStatement(); 
    		AST ast = p.getAST(); 
			SyntaxUtils.textToLowerCase(ast);
			logger.info(ast.toStringList());
			if (Settings.DISPLAY_AST && Settings.DISPLAY_GRAPHS) {
				final JFrame astframe = new ASTFrame("Syntax Tree", ast);
				astframe.setVisible(true);
			}
			return ast;
    	} catch (final Exception e) {
    		Utils.handleCriticalException(e);
    		return null;
    	}
	}

    /** 
     * The single-site optimizer phase.
     * @param queryID Number assigned to this query
     * @param fullQueryPath Full path where query will be found
     * @param queryName Name to be used for this query.
     * @return PAF representation of this query.
     * @throws FileNotFoundException If query could not be found.
     */
    private static PAF doSingleSitePhase(final int queryID, 
    	final String fullQueryPath, final String queryName) 
    	throws FileNotFoundException {

    	LAF laf; 
		if (Settings.useHaskell()) {
		    Operator ops = HaskellOperatorRewriter.generateOperators(
		    		fullQueryPath, queryPlanOutputDir);
		    
			laf = new LAF(ops, queryName, 
			   qosCollection.get(queryID).getMaxAcquisitionInterval());
		    laf.display(QueryCompiler.queryPlanOutputDir, laf.getName());
		    
		} else {
			// Parse query and produce Abstract Syntax Tree 
			final InputStream in = new FileInputStream(fullQueryPath);
			final AST ast = parseQuery(in);
					
			// Translation from Abstract Syntax Tree to representation 
			// from which predicates etc. can be easily extracted
			Translator tran  = new Translator(ast, schemaMetadata, 
					qosCollection.get(queryID).getMaxAcquisitionInterval());
			logger.info(tran.toString());
		       	
			// 	Logical Optimisation
			laf = LogicalOptimiser.generateLogicalPlan(
					tran, 
				new ClassicalCardinalityCalculator(), 
				qosCollection.get(queryID).getMaxAcquisitionInterval(),
				queryName);
		}
		
		// Physical Optimisation
		logger.info("Starting Physical Optimization");
		PAF paf = PhysicalOptimization.doPhysicalOptimization(
				laf, 
				queryName);
		return paf;
	}
    
    /**
     * Invoke the multi-site optimizer phase.
     * @param queryID the ID of the query.
     * @param queryName the name of the query.
     * @param paf the physical operator tree from the single-site phase.
     * @return a complete query plan (comprising DAF, RT and agenda)
     * @throws OptimizationException An optimization-related error.
     * @throws AgendaException An agenda-related error.
     */
	private static ScoredCandidateList<Agenda> doMultiSitePhase(final int queryID, 
			final String queryName, final PAF paf) 
			throws OptimizationException, AgendaException {
	
		// Routing	        
		logger.info("Starting Routing");
		ScoredCandidateList<RT> rtList = Routing.doRouting(
				paf, 
				sensornetMetadata,
				schemaMetadata,
				Settings.INPUTS_METADATA_SINKS.get(queryID),
				queryName,
				qosCollection.get(queryID));
		
		// Where Scheduling
		logger.info("Starting Where Scheduling");
		ScoredCandidateList<DAF> dafList = WhereScheduling.
				doWhereScheduling(paf, 
				rtList, 
				schemaMetadata,
				Settings.INPUTS_METADATA_SINKS.get(queryID),
				queryName,
				qosCollection.get(queryID));

		// When Scheduling
	   logger.info("Starting When Scheduling");
	   ScoredCandidateList<Agenda> agendaList = WhenScheduling.
				doWhenScheduling(dafList, 
				qosCollection.get(queryID), 
				queryName);        

	   return agendaList;
	}
    
	
	private static void produceCandidateSummary(ScoredCandidateList<Agenda> agendaList, String bestAgendaId) throws IOException {
		String fname = QueryCompiler.queryPlanOutputDir + "candidates-summary.csv";

		ArrayList<Double> alphaScores = new ArrayList<Double>();
	    ArrayList<Double> deltaScores = new ArrayList<Double>();
	    ArrayList<Double> epsilonScores = new ArrayList<Double>();
	    ArrayList<Double> lambdaScores = new ArrayList<Double>();
	    double maxAlpha = 0, maxDelta = 0, maxEpsilon = 0, maxLambda = 0;
	    
		Iterator<Agenda> agendaIter = agendaList.iterator();
		while (agendaIter.hasNext()) {
			Agenda agenda = agendaIter.next();
			RT rtCand = agenda.getDAF().getRoutingTree();
			
			//rt-alpha-score
			RTAcquisitionIntervalScoringFunction rtAlphaScorer = 
				new RTAcquisitionIntervalScoringFunction(1, false); 
			double score = rtAlphaScorer.score(rtCand);
			alphaScores.add(score);
			maxAlpha = Math.max(maxAlpha, score);
	
			//rt-delta-score
			RTDeliveryTimeScoringFunction rtDeltaScorer = 
				new RTDeliveryTimeScoringFunction(1, false); 
			score = rtDeltaScorer.score(rtCand);
			deltaScores.add(score);
			maxDelta = Math.max(maxDelta, score);
			
			//rt-epsilon-score
			RTEnergyScoringFunction rtEpsilonScorer = 
				new RTEnergyScoringFunction(1, false); 
			score = rtEpsilonScorer.score(rtCand);			
			epsilonScores.add(score);
			maxEpsilon = Math.max(maxEpsilon, score);
			
			//rt-lambda-score
			RTLifetimeScoringFunction rtLambdaScorer = 
				new RTLifetimeScoringFunction(1, false); 
			score = rtLambdaScorer.score(rtCand);
			lambdaScores.add(score);
			maxLambda = Math.max(maxLambda, score);
		}
		
	    final PrintWriter out = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname)));
	    out.println("agenda-id,best,rt-id,rt-alpha-score,rt-delta-score,rt-epsilon-score,rt-lambda-score,rt-final-score,beta,alpha-bms,delta-bms,pi-bms,alpha-ms,delta-ms,pi-ms");

	    agendaIter = agendaList.iterator();
	    int i = 0;
		while (agendaIter.hasNext()) {
			Agenda agenda = agendaIter.next();
			
			//agenda-id
			out.print(agenda.getName() + ",");
			RT rtCand = agenda.getDAF().getRoutingTree();

			//Is best?
			if (agenda.getName().equals(bestAgendaId)) {
				out.print("true,");
			} else {
				out.print("false,");
			}
			
			//rt-id
			out.print(rtCand.getName() + ",");
			
			//rt-alpha-score,rt-delta-score,rt-epsilon-score,rt-lambda-score 
			out.print(alphaScores.get(i)/maxAlpha + ",");
			out.print(deltaScores.get(i)/maxDelta + ",");			
			out.print(epsilonScores.get(i)/maxEpsilon + ",");			
			out.print(lambdaScores.get(i)/maxLambda + ",");			
			
			//rt-final-score
			out.print(agenda.getDAF().getRoutingTree().getScore() + ",");

			//beta
			out.print(agenda.getBufferingFactor() + ",");
			
			//alpha, delta, pi in bms
			out.print(agenda.getAcquisitionInterval_bms() + ",");
			out.print(agenda.getDeliveryTime_bms() + ",");
			out.print(agenda.getProcessingTime_bms() + ",");
			
			//alpha, delta, pi in ms
			out.print(agenda.getAcquisitionInterval_ms() + ",");
			out.print(agenda.getDeliveryTime_ms() + ",");
			out.println(agenda.getProcessingTime_ms());

			i++;
		}
		out.close();
	}
	
	/**
	 * Invokes the code generation phase.
	 * @param queryID the id of the query
	 * @param plan the query plan
	 * @throws OptimizationException An optimization-related exception
	 */
	private static void doCodeGenerationPhase(final int queryID, 
			final QueryPlan plan, final String nescOutputDir) throws OptimizationException {
		
		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.TOSSIM)) {
			logger.info("Starting TOS1 Code Generation for Tossim");
			NesCGeneration.doNesCGeneration(plan, 
					qosCollection.get(queryID), 
					Settings.INPUTS_METADATA_SINKS.get(queryID),
					nescOutputDir, 1, true);
		}
		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.AVRORA)) {
			logger.info("Starting TOS1 Code Generation for Avrora");
			NesCGeneration.doNesCGeneration(plan, 
					qosCollection.get(queryID), 
					Settings.INPUTS_METADATA_SINKS.get(queryID),
					nescOutputDir, 1, false);			
		}
		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.TOSSIM2)) {
			logger.info("Starting TOS2 Code Generation for Tossim");
			NesCGeneration.doNesCGeneration(plan, 
					qosCollection.get(queryID), 
					Settings.INPUTS_METADATA_SINKS.get(queryID),
					nescOutputDir, 2, true);			
		}
		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.AVRORA2)) {
			logger.info("Starting TOS2 Code Generation for Avrora");
			NesCGeneration.doNesCGeneration(plan, 
					qosCollection.get(queryID), 
					Settings.INPUTS_METADATA_SINKS.get(queryID),
					nescOutputDir, 2, false);
		}
	}
    
	
	/**
	 * 
	 * Processes a query all the way through to NesC code.
	 * 
	 * @param queryFileName Name of the file that hold the query * 
	 * @param queryID Number assigned to this query 
	 *        for multiple query processing.
	 * @return a query plan for the query
	 * @throws IOException One of the required files not found.
	 */
	public static QueryPlan processQuery(final String queryFileName,
			final int queryID) throws IOException {
		
		QueryPlan plan = null;
		
        try {
        	final String fullQueryPath = 
        		Settings.INPUTS_QUERY_DIR + queryFileName;
        	logger.info("Processing query " + fullQueryPath);
        	String queryName = queryFileName.replaceAll("\\.txt", "");
        	//remove any directories from query name 
        	if (queryName.lastIndexOf('/') > 0) {
        		queryName = queryName.substring(queryName.lastIndexOf('/'));
        	}
     
        	String outputRootDir = Settings.GENERAL_OUTPUT_ROOT_DIR + queryName + "/"; 
        	queryPlanOutputDir =  outputRootDir + Constants.QUERY_PLAN_DIR;
        	Utils.checkDirectory(queryPlanOutputDir, true);
        	
			//Display the sensor network topology
			if (Settings.DISPLAY_SENSOR_NETWORK_TOPOLOGY) {
				sensornetMetadata.display(
						QueryCompiler.queryPlanOutputDir, 
						sensornetMetadata.getName());
			}	

			//Display Query and QoS requirements for this query
			qosCollection.get(queryID).exportToPDF(
				queryPlanOutputDir + "user-input.tex", fullQueryPath);
			
			//Invoke the Single-site Phase of query optimization
			PAF paf = doSingleSitePhase(
					queryID, fullQueryPath, queryName);
        	
			//Invoke the Multi-site phase of query optimization
        	ScoredCandidateList<Agenda> agendaList 
        		= doMultiSitePhase(queryID, queryName, paf);	       

        	if (agendaList.getSize()==0) {
        		System.err.println("No feasible agenda could be generated.");
        		System.exit(2);
        	}
        	
   		    //Invoke Code generation phase for best plan
        	Agenda bestAgenda = agendaList.getBest();
        	String bestAgendaID = bestAgenda.getName();
        	
        	produceCandidateSummary(agendaList, bestAgendaID);
        	
        	generateQoSMetrics(agendaList);
        	

        	plan = new QueryPlan(bestAgenda.getDAF().getName(), 
    			   bestAgenda.getDAF(), bestAgenda);
    		doCodeGenerationPhase(queryID, plan, outputRootDir);
	        //Produce a text file characterising the query plan generated
	        plan.produceQueryPlanSummary();
	        plan.generateTrafficPatternsXML(qosCollection.get(queryID));
    	        
	        Iterator<Agenda> agendaIter = agendaList.iterator();
	        while (agendaIter.hasNext()) {
	        	Agenda agenda = agendaIter.next();
	        	if (agenda != bestAgenda) 
	        	{
	        		QueryPlan qp = new QueryPlan(agenda.getDAF().getName(), 
			     		agenda.getDAF(), agenda);
			        String nescOutputDir = outputRootDir 
		        		+ "/alt/" + agenda.getName() + "/";
			        doCodeGenerationPhase(queryID, qp, nescOutputDir);
	        	}
	        }
	        
        } catch (final OptimizationException e) {
        	Utils.handleQueryException(e);
        } catch (final AgendaException e) {
        	Utils.handleCriticalException(e);
        } catch (final FileNotFoundException e) {
        	Utils.handleCriticalException(e);
        } 

        return plan;    
   }

	 
    private static void generateQoSMetrics(ScoredCandidateList<Agenda> agendaList) throws IOException {
	    String fname = queryPlanOutputDir+"model-qos-metrics.csv";
    	final PrintWriter out = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname)));
	    out.println("agenda-id,best,rt-id,beta,alpha-ms,delta-ms,total-energy,network-lifetime,pi-ms,average-energy,cvx-pi,cvx-epsilon,cvx-lamda,rt-desc");
    	
	    String bestAgendaId = agendaList.getBest().getName();
	    
    	Iterator<Agenda> agendaIter = agendaList.iterator();
		while (agendaIter.hasNext()) {
			Agenda agenda = agendaIter.next();
			String agendaId = agenda.getName();
			String rtId = agenda.getDAF().getRoutingTree().getName();
			String beta = new Long(agenda.getBufferingFactor()).toString();
			
			String isBest = "false";
			if (agendaId.equals(bestAgendaId)) {
				isBest ="true";
			}
			
			double alpha = agenda.getAcquisitionInterval_ms();
			double delta = agenda.getDeliveryTime_ms();
			double epsilon = agenda.getTotalEnergy();
			double lambda = agenda.getLifetime();
			double lambdaDays = Utils.convertSecondsToDays(lambda);
			double pi = agenda.getProcessingTime_ms();
			double averageEpsilon = (epsilon/agenda.getDAF().getRoutingTree().getNumNodes());
			double cvxPi = agenda.getCVXPi();
			double cvxEpsilon = agenda.getCVXEpsilon();
			double cvxLambdaDays = agenda.getCVXLambdaDays();
			
			String rtDesc = agenda.getDAF().getRoutingTree().getDesc();
			
		    out.println(agendaId+","+isBest+","+rtId+","+beta+","+alpha+","+delta+","+epsilon+","+lambdaDays+
		    		","+pi+","+averageEpsilon+","+cvxPi+","+cvxEpsilon+","+cvxLambdaDays+","+rtDesc);
		}
		out.close();
	}

	/**
     * Process each query and then produce a Multiple query plan
     * and NesC code for multiple co-executing queries.
     * @throws IOException 
     */
    public static void processMultipleQueries() throws IOException {
    	ArrayList<QueryPlan> queryPlanColl = 
    		new ArrayList<QueryPlan>();
    	
    	for (int i = 0; i < Settings.INPUTS_QUERIES.length; i++) {
    		queryPlanColl.add(processQuery(Settings.INPUTS_QUERIES[i], i));
    	} 

    	if (Settings.INPUTS_QUERIES.length > 1) {
        	MQE.generateMultipleQueryPlan(queryPlanColl);    		
    	}	  	 
    }
	
    /**
     * Get user settings, qos information and load meta-data.
     * @param args		Command-line arguments
     */
    public static void initialize(final String[] args) {
    	
    	try {
        	/** get ini file and command line options **/
        	final Options opt = new Options(args, 
        			Options.Multiplicity.ZERO_OR_ONE);
        	Settings.initialize(opt);
        	
        	/** get metadata: units' scaling factors, schema, topology, 
        	 * operator costs **/
     	  	schemaMetadata = new SchemaMetadata();
    		sensornetMetadata = TopologyReader.readNetworkTopology(
    				Settings.INPUTS_NETWORK_TOPOLOGY_FILE, 
    				Settings.INPUTS_SITE_RESOURCE_FILE);       
            
    		CostParameters.setupOperatorMetaData();
            
    		// Quality of Service for each query
            for (int i = 0; i < Settings.INPUTS_QUERIES.length; i++) {
            	qosCollection.add(new QoSSpecReader(opt, i));
            }
                        
    	} catch (Exception e) {
    		Utils.handleCriticalException(e);
    	}
    }

    /**
     * The main entry point for the query optimizer.
     * @param args the command-line arguments
     * @throws IOException 
     */
	 public static void main(final String[] args) throws IOException {
	    	
		 	initialize(args);
	    	
	    	processMultipleQueries();
	   		
	   		logger.info("Success");
	        LoggerSetup.flush();
	     }
}
