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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.INIFile;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.logger.LoggerSetup;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.CodeGenTarget;

public class Settings {

	/**
	 * Logger for this class.
	 */
	private static Logger logger = Logger.getLogger(Settings.class.getName());	
	
	/**
	 * Determines whether graphs are to be generated.
	 */
	public static boolean GENERAL_GENERATE_GRAPHS = true;
	
	/**
	 * Specifies whether PDF output is desired.
	 */
	public static boolean GENERAL_GENERATE_PDFS = false;
	
	/**
	 * Specifies whether optimizer should clean up output directories before 
	 * outputting to them.
	 */
  	public static boolean GENERAL_DELETE_OLD_FILES;

	/**
	 * Output files root directory.
	 */
	public static String GENERAL_OUTPUT_ROOT_DIR;

	/**
	 * Specifies whether the QoS-aware partitioning algorithm is to be used, 
	 * or the plain vanilla one.
	 */
	public static boolean QOS_AWARE_PARTITIONING;
	
	/**
	 * Specifies whether the QoS-aware routing algorithm is to be used, or the 
	 * plain vanilla one.
	 */
	public static boolean QOS_AWARE_ROUTING;
	
	/**
	 * Specifies whether the QoS-aware where-scheduling algorithm is to be 
	 * used, or the plain vanilla one.
	 */
	public static boolean QOS_AWARE_WHERE_SCHEDULING;
	
	/**
	 * Specifies whether the QoS-aware when-scheduling algorithm is to be 
	 * used, or the plain vanilla one.
	 */
	public static boolean QOS_AWARE_WHEN_SCHEDULING;
		
	/**
	 * The location of the input query directory.
	 */	
	public static String INPUTS_QUERY_DIR;
	
	/**
	 * File with query to be run
	 */
	private static String INPUTS_QUERY;
	
	//  list of queries file names
	public static String INPUTS_QUERIES[];

	/**
	 * File with the QoS specification.
	 * Set to "none" if no qos-file is to be read (does not preclude 
	 * QoS-related command-line arguments being set)
	 */
	public static String INPUTS_QOS_FILE;
	
	/**
	 * The name of the file with the sensor network topology.
	 */
	public static String INPUTS_NETWORK_TOPOLOGY_FILE;

	/**
	 * The name of the file which specifies the resources available on each 
	 * site.
	 */	
	public static String INPUTS_SITE_RESOURCE_FILE;
	
	/**
	 * The name of the file with the schema.
	 */
	public static String INPUTS_SCHEMA_FILE;

	/**
	 * The name of the file with the operator metadata.
	 */
	public static String INPUTS_COST_PARAMETERS;

	/**
	 * The name of the file with the type definitions.
	 */
	public static String INPUTS_TYPES_FILE;

	/**
	 * The name of the file with the user unit definitions.
	 */	
	public static String INPUTS_USER_UNITS_FILE;
	
	/**
	 * Executable used to run the Haskell translator.
	 * Format is the full file path and the fine name.
	 */ 
	public static String INPUTS_RUNHUGS_FILE;
	
	/**
	 * Temporary setting to only use Haskell in test environment.
	 * Will be deprecated to true
	 */ 
	 private static boolean haskellUse;

	 /** Directory that has the Haskell decl file. */	
	 private static String haskellDeclDir; 

     /** See Get Method. */	
     private static String haskellMain;

     /** See Get Method. */	
	 private static String haskellDeclFile;
	 	
	/**
	 * Determines whether all graphs are to be displayed.
	 * Set to FALSE it blocks all calls to DISPLAY GRAPH, and overrides 
	 * display_distributed_query_plan and display routing tree properties.
	 */
	public static boolean DISPLAY_GRAPHS = false;
		
	/**
	 * Determines whether the sensor network topology is displayed.
	 */	
	public static boolean DISPLAY_SENSOR_NETWORK_TOPOLOGY;

	/**
	 * Determines whether AST is displayed.
	 */
	public static boolean DISPLAY_AST;

	/**
	 * Determines whether logical query plan is displayed.
	 */
	public static boolean DISPLAY_LAF = false;	

	/**
	 * Determines whether physical query plan is displayed.
	 */
	public static boolean DISPLAY_PAF = false;	
	
	/**
	 * Determines whether the routing tree is displayed.
	 */
	public static boolean DISPLAY_RT = false;

	/**
	 * Determines whether the fragmented-algebraic form is displayed.
	 */
	public static boolean DISPLAY_FAF = false;
	
	/**
	 * Determines whether distributed-algebraic form is displayed.
	 */
	public static boolean DISPLAY_DAF = false;	

	/**
	 * Determines whether the agenda is displayed.
	 */
	public static boolean DISPLAY_AGENDA = false;
	
	/**
	 * Determines whether the operator properties are displayed on the LAF,
	 * PAF, FAF and DAF.
	 */
	public static boolean DISPLAY_OPERATOR_PROPERTIES = false;
	
	/**
	 * Determines whether link properties are displayed in the sensornet 
	 * topology graph and routing tree.
	 */
	public static boolean DISPLAY_SENSORNET_LINK_PROPERTIES = false;

	
	/**
	 * Determines whether a site properties (energy available, operators 
	 * placed on it) are displayed or not on the topology and routing 
	 * tree graphs.
	 */
	public static boolean DISPLAY_SITE_PROPERTIES;
	
	/**
	 * Determines whether Tuple Attributes are displayed.
	 */
	public static boolean DISPLAY_TUPLE_ATTRIBUTES = false;

	/** 
	 * Cause the operator name to include All, Stream and such descriptions.
	 */
	public static boolean DISPLAY_OPERATOR_DATA_TYPE = true;

	/**
	 * Determines whether the exchange operator routing support function is 
	 * displayed.
	 */
	public static boolean DISPLAY_EXCHANGE_OPERATOR_ROUTING = false;

	/**
	 * Determines whether the config graphs are displayed.
	 */
	public static boolean DISPLAY_CONFIG_GRAPHS = true;

	/**
	 * Determines whether Cost Expressions displayed.
	 * Works even in the vanilla version which doesn't use the cost Expressions.
	 */
	public static boolean DISPLAY_COST_EXPRESSIONS = false;
		 
	/**
	 * Causes the application to exit as soon as an error is detected.
	 */
	public static boolean DEBUG_EXIT_ON_ERROR = true;

	/**
	 * 
	 * If set >= 0 causes the plan to only execute the fragments a set number of times.  
	 */
	public static int MEASUREMENTS_ACTIVE_AGENDA_LOOPS = -1;

	/** 
	 * Setting used for Measurement experiments.
	 * Should never be used outside of measurement experiments.
	 * Will affect the query results.
     * 
	 * List the operators to ignore Attribute with "ignore" in their name.
	 * Effects only the listed operators.
	 * 
	 * Not implemented for all operators yet.
	 */ 
	public static String MEASUREMENTS_IGNORE_IN = "";

	/** 
	 * Setting used for Measurement experiments.
	 * Should never be used outside of measurement experiments.
	 * Will affect the query results.
	 * 
	 * List the operators that the Nesc generator remove from the plan.
	 * This will generally be done by avoiding a call to the operator.
	 * How this is done is operator specific.
	 * 
	 * Warning: All upstream (parent..) operators must also be removed.
	 * Otherwise Nesc code could freeze or produce strange results.  
	 *  
	 * Not implemented for all operators yet.
	 */ 
	public static String MEASUREMENTS_REMOVE_OPERATORS = "";

	/** 
	 * Setting used for Measurement experiments.
	 * Should never be used outside of measurement experiments.
	 * Will affect the query results.

	 * List the operators for which the Nesc generator 
	 * will insert thin versions operator.
	 * Thin versions will break Nesc best coding practices.
	 * 
	 * Operator plus a number insert alternative thin versions.
	 * Alternative thin versions may alter the operator behaviour 
	 * Warning: All upstream (parent..) operators must also be removed.
	 * Otherwise Nesc code could freeze or produce strange results.  
	 * 
	 * Not implemented for all operators yet.
	 */ 
	public static String MEASUREMENTS_THIN_OPERATORS = "";

	/** 
	 * Setting used for Measurement experiments.
	 * Should never be used outside of measurement experiments.
	 * Will affect the query results.

	 * Causes acquire to generate more than one tuple at once.
	 * Multiple tuples may be duplicates.
	 */ 
	public static int MEASUREMENTS_MULTI_ACQUIRE = -1;
	
	/**
	 * When set to true this allows the physical optimiser to remove 
	 * unrequired operators.
	 * For example when using All window transmission the RSTREAM 
	 * and some windows (NEW for example) are not required.
	 * as do not change the data being sent at all. 
	 */
	public static boolean LOGICAL_OPTIMIZATION_REMOVE_UNREQUIRED_OPERATORS = true;

	/**
	 * When true projection is pushed into last operator that uses the data.
	 */
	public static boolean LOGICAL_OPTIMIZATION_PUSH_PROJECTION_DOWN = true;

	/**
	 * When true combines acquire and select whenever it is legal to do so.
	 */
	public static boolean LOGICAL_OPTIMIZATION_COMBINE_ACQUIRE_AND_SELECT = true;
	
	/**
	 * If set, the routing step does not compute a routing tree; instead, it 
	 * generates the routing tree specified in the file.  Each line in the 
	 * file is a child:parent pair in the routing tree.
	 */
	public static String ROUTING_TREE_FILE = null;
	
	/**
	 * The random seed to use when generating steiner trees.  For ICDE08, 
	 * VLDB08 we used random seed = 4. 
	 */
	public static int ROUTING_RANDOM_SEED;
	
	/**
	 * QoS-aware version only - The max number of routing trees to generate.
	 */
	public static int ROUTING_TREES_TO_GENERATE;
	
	/**
	 * QoS-aware version only - The max number of routing trees to keep after 
	 * scoring and ranking.
	 */
	public static int ROUTING_TREES_TO_KEEP;

	/**
	 * Flag which specifies whether exchange operators which are not required
	 * should be removed after where scheduling.  
	 */
	public static boolean WHERE_SCHEDULING_REMOVE_REDUNDANT_EXCHANGES;
	
	
	/**
	 * QoS-aware version only. Lower bound of maximum number of neighbours for a single invocation of the neighbour function to generate.
	 * Used by experiments to determine the optimal number of neighbours.
	 */
	public static int WHERE_SCHEDULING_MIN_NN = 0;
	
	/**
	 * QoS-aware version only. Upper bound of maximum number of neighbours for a single invocation of the neighbour function to generate.
	 * Used by experiments to determine the optimal number of neighbours.
	 */
	public static int WHERE_SCHEDULING_MAX_NN = 5;
	
	/**
	 * QoS-aware version only.  This makes odd sites "dummy" nodes, i.e., they cannot evaluate operators except acquire.  
	 * A dummy node can relay data.  Used to make where-scheduling experiments more interesting. 
	 */
	public static boolean WHERE_SCHEDULING_HETEROGENEOUS_NETWORK = false;
	
	/**
	 * QoS-aware version only. This makes the solver generate a second, initial point based on heuristics.  
	 * The point is generated by pushing all data decreasing operator instances to the lowest confluence site.
	 * Note that this point is not guaranteed to be feasible.
	 */
	public static boolean WHERE_SCHEDULING_HEURISTIC_INITIAL_POINT = false;
	
	/**
	 * QoS-aware version only. This option allows the user to specify a file, 
	 * which will be read as the where-scheduling solver output, instead of 
	 * invoking the solver.  If not set, where scheduling will invoke the
	 * solver as usual.
	 */
	public static String WHERE_SCHEDULING_SOLVER_OUTPUT_FILE = null;
	
	/** 
	 * Vanilla version only - when true when-scheduling reduces the buffering 
	 * factor to prevent the need for agendas to overlap.  Note that if this 
	 * cannot be achieved an optimization exception is thrown. When false an 
	 * optimization exception is thrown straight away without attempts being 
	 * made to reduce buffering factor.	 
	 */
	public static boolean 
		WHEN_SCHEDULING_DECREASE_BFACTOR_TO_AVOID_AGENDA_OVERLAP = true;
	
	/**
	 * Comma-separated list of targets to generate code for.
	 */
	public static HashSet<CodeGenTarget> CODE_GENERATION_TARGETS;
	
	/**
	 * Set to true to explicitly turn on/off the radio (Does not work in tossim).
	 */ 
	public static boolean NESC_CONTROL_RADIO_OFF;

	/**
	 * Adjust the radio power depending on the distance between a sender and receiver.
	 */
	public static boolean NESC_ADJUST_RADIO_POWER;
	
	/**
	 * The maximum size, in bytes, of data (i.e., payload) in message that can 
	 * be sent across the radio.
	 */
	public static int NESC_MAX_MESSAGE_PAYLOAD_SIZE;

	/**
	 * The overhead incurred, in bytes, with each message in the payload of the 
	 * message, e.g., for sending previous fragment information.
	 */
	public static int NESC_PAYLOAD_OVERHEAD;

	/**
	 * The permissable synchronization error between the nodes in the sensornet.
	 * The value by which the sync timer is incremented.
	 */
	public static int NESC_SYNCHRONIZATION_ERROR;

	/**
	 * Specifies if the last tuples should be delivered.
	 */
	public static boolean NESC_DELIVER_LAST = false;
	
	/**
	 * Specifies if LED debug lines should be included.
	 */
	public static boolean NESC_LED_DEBUG = true;
	
	/**
	 * The time which should be spent synchronizing with the other nodes in 
	 * the sensornet at the start.
	 * This should be considerably larger than the initial boot time.
	 * The value the sync timer must reach to stop.
	 */
	public static int NESC_SYNCHRONIZATION_PERIOD;

	
	/**
	 * Put the mote in a low-power state during periods of inactivity.
	 * Not applicable for Tossim.
	 */
	public static boolean NESC_POWER_MANAGEMENT = true;
	
	/**
	 * Prints detailed debug statements in tray component.
	 */
	public static boolean NESC_MAX_DEBUG_STATEMENTS_IN_TRAY = false;

	/**
	 * Prints detailed debug statements in row window component.
	 */
	public static boolean NESC_MAX_DEBUG_STATEMENTS_IN_ROWWINDOW = false;

	/**
	 * Prints detailed debug statements in time window component.
	 */
	public static boolean NESC_MAX_DEBUG_STATEMENTS_IN_TIMEWINDOW = false;
	    
	/**
	 * Prints detailed debug statements in time window component.
	 */
	public static boolean NESC_MAX_DEBUG_STATEMENTS_IN_AGGREGATES = false;

	/**
	 * The experiment to be run with the green Led lights.
	 * NesC Code "//"+NESC_YELLOW_EXPERIMENT 
	 *    will be replaced with "call Leds.Green" 
	 */
	public static String NESC_YELLOW_EXPERIMENT;
	
	/**
	 * The experiment to be run with the green Led lights.
	 * NesC Code "//" + NESC_GREEN_EXPERIMENT" 
	 *    will be replaced with "call Leds.Green" 
	 */
	public static String NESC_GREEN_EXPERIMENT;

	/**
	 * The experiment to be run with the green Led lights.
	 * NesC Code "//" + NESC_RED_EXPERIMENT" 
	 *    will be replaced with "call Leds.Green" 
	 */
	public static String NESC_RED_EXPERIMENT;
	
	/**
	 * 
	 */
	//MQE
	//Temporary variable to hold the sinks string e.g 2;3;4
	private static String sinksText=null;
	//Temporary variable to hold the default sink (from the ini file)
	private static Integer defaultSink;
	//final collection containing sinks for the queries correspondingly 
	public static ArrayList < Integer >  INPUTS_METADATA_SINKS  = 
		new ArrayList < Integer > ( ) ;
	
	private static String mainLogFile;
	
	/**
	 * 
	 */
	private static String MemoryHandlerFile;
	
	
	private static String maxFileLogLevel;
	
	
	/**
	 * Reads the ini file settings.
	 * @param iniFile The ini file to be read.
	 */
	public static void readIniFile(final INIFile iniFile) {

    	GENERAL_OUTPUT_ROOT_DIR = Utils.fixDirName(
    			iniFile.getStringProperty("General", "output_root_dir"));

		LoggerSetup.setupLoggers();
		
    	///Except for very int logs you want one of these two. 
    	//Utils.checkFile(iniFile.getStringProperty("Logger","log_file"));
    	mainLogFile = iniFile.getStringProperty("Logger", "log_file");
    	//Utils.checkFile(iniFile.getStringProperty("Logger","memory_file"));
    	MemoryHandlerFile = iniFile.getStringProperty("Logger", "memory_file");
    	//Default for classes that do not set there own.
    	LoggerSetup.setLogLevel(
    			iniFile.getStringProperty("Logger", "all_log_level"), "");
    	//Warning will show least of this and log_level.
    	LoggerSetup.setConsoleLevel(
    			iniFile.getStringProperty("Logger", "max_console_log_level"));
    	maxFileLogLevel = iniFile.getStringProperty("Logger", 
    			"max_file_log_level");

      	
    	GENERAL_GENERATE_GRAPHS = 
    		iniFile.getBooleanProperty("General", "generate_graphs");
    	GENERAL_GENERATE_PDFS = 
    		iniFile.getBooleanProperty("General", "generate_pdfs");    	
    	GENERAL_DELETE_OLD_FILES = 
    		iniFile.getBooleanProperty("General", "delete_old_files"); 
    	
    	QOS_AWARE_PARTITIONING = 
    		iniFile.getBooleanProperty("QoSAware", "partitioning");
    	QOS_AWARE_ROUTING = 
    		iniFile.getBooleanProperty("QoSAware", "routing");
    	QOS_AWARE_WHERE_SCHEDULING = 
    		iniFile.getBooleanProperty("QoSAware", "where_scheduling");
    	QOS_AWARE_WHEN_SCHEDULING = 
    		iniFile.getBooleanProperty("QoSAware", "when_scheduling");
    	
       	INPUTS_QUERY = 
       		iniFile.getStringProperty("Inputs", "query");
    	INPUTS_QOS_FILE = 
    		iniFile.getStringProperty("Inputs", "qos_file").toLowerCase();
    	INPUTS_NETWORK_TOPOLOGY_FILE = 
    		iniFile.getStringProperty("Inputs", "network_topology_file");
    	INPUTS_SITE_RESOURCE_FILE = 
    		iniFile.getStringProperty("Inputs", "site_resource_file");
    	INPUTS_SCHEMA_FILE = 
    		iniFile.getStringProperty("Inputs", "schema_file");
    	INPUTS_COST_PARAMETERS = 
    		iniFile.getStringProperty("Inputs", "cost_parameters");
    	INPUTS_TYPES_FILE = 
    		iniFile.getStringProperty("Inputs", "types_file");
    	INPUTS_USER_UNITS_FILE = 
    		iniFile.getStringProperty("Inputs", "user-units-file");
    	INPUTS_RUNHUGS_FILE = 
    		iniFile.getStringProperty("Inputs", "runhugs_file");
    	
    	haskellUse =
    		iniFile.getBooleanProperty("Haskell", "use");
    	haskellDeclDir =  
    		iniFile.getStringProperty("Haskell", "decl_dir");
        haskellMain =
     	    iniFile.getStringProperty("Haskell", "main");
    	haskellDeclFile =
    		iniFile.getStringProperty("Haskell", "decl_file");
    	 		
    	//MQE
    	defaultSink = 
    		iniFile.getIntegerProperty("Inputs", "default_sink");
    	
    	DISPLAY_GRAPHS = 
    		iniFile.getBooleanProperty("Display", "graphs");
    	DISPLAY_SENSOR_NETWORK_TOPOLOGY = 
    		iniFile.getBooleanProperty("Display", "sensor_network_topology");
    	DISPLAY_AST = 
    		iniFile.getBooleanProperty("Display", "ast");
    	DISPLAY_LAF = 
    		iniFile.getBooleanProperty("Display", "laf");
    	DISPLAY_PAF = 
    		iniFile.getBooleanProperty("Display", "paf");
    	DISPLAY_RT = 
    		iniFile.getBooleanProperty("Display", "rt");
    	DISPLAY_FAF = 
    		iniFile.getBooleanProperty("Display", "faf");
    	DISPLAY_DAF = 
    		iniFile.getBooleanProperty("Display", "daf");
    	DISPLAY_AGENDA = 
    		iniFile.getBooleanProperty("Display", "agenda");
    	DISPLAY_OPERATOR_PROPERTIES = 
    		iniFile.getBooleanProperty("Display", "operator_properties");
    	DISPLAY_SENSORNET_LINK_PROPERTIES = 
    		iniFile.getBooleanProperty("Display", "sensornet_link_properties");
    	DISPLAY_SITE_PROPERTIES = 
    		iniFile.getBooleanProperty("Display", "site_properties");
    	DISPLAY_TUPLE_ATTRIBUTES = 
    		iniFile.getBooleanProperty("Display", "tuple_attributes");
       	DISPLAY_OPERATOR_DATA_TYPE = 
       		iniFile.getBooleanProperty("Display", "operator_data_type");
    	DISPLAY_EXCHANGE_OPERATOR_ROUTING = 
    		iniFile.getBooleanProperty("Display", "exchange_operator_routing");
    	DISPLAY_CONFIG_GRAPHS =
    		iniFile.getBooleanProperty("Display", "config_graphs");
    	DISPLAY_COST_EXPRESSIONS = 
    		iniFile.getBooleanProperty("Display", "cost_expressions");
    	
       	DEBUG_EXIT_ON_ERROR = 
       		iniFile.getBooleanProperty("Debug", "exit_on_error");
       	
       	//Measurement options are not set in the ini file.
       	
    	LOGICAL_OPTIMIZATION_REMOVE_UNREQUIRED_OPERATORS = 
    		iniFile.getBooleanProperty("Logical_Optimization", 
    		"remove_unrequired_operators");
    	LOGICAL_OPTIMIZATION_PUSH_PROJECTION_DOWN = 
    		iniFile.getBooleanProperty("Logical_Optimization", 
    		"push_projection_down");
    	LOGICAL_OPTIMIZATION_COMBINE_ACQUIRE_AND_SELECT = 
    		iniFile.getBooleanProperty("Logical_Optimization", 
    		"combine_acquire_and_select");
    	
    	ROUTING_RANDOM_SEED = 
    		iniFile.getIntegerProperty("Routing", "random_seed");
    	
    	ROUTING_TREES_TO_GENERATE = 
    		iniFile.getIntegerProperty("Routing", "trees_to_generate");    	
    	
    	ROUTING_TREES_TO_KEEP = 
    		iniFile.getIntegerProperty("Routing", "trees_to_keep");
    	
    	WHERE_SCHEDULING_REMOVE_REDUNDANT_EXCHANGES =
    		iniFile.getBooleanProperty("Where_Scheduling", "remove_redundant_exchanges");
    	
    	WHEN_SCHEDULING_DECREASE_BFACTOR_TO_AVOID_AGENDA_OVERLAP = 
    		iniFile.getBooleanProperty("When_Scheduling", 
    		"decrease_bfactor_to_avoid_agenda_overlap");
    	
    	String targetStr = iniFile.getStringProperty("Code_Generation", "targets");
    	CODE_GENERATION_TARGETS = CodeGenTarget.parseCodeTargets(targetStr);
    		
    	NESC_CONTROL_RADIO_OFF = 
    		iniFile.getBooleanProperty("NesC", "control_radio_off");
    	NESC_ADJUST_RADIO_POWER = 
    		iniFile.getBooleanProperty("NesC", "adjust_radio_power");
    	NESC_MAX_MESSAGE_PAYLOAD_SIZE = 
    		iniFile.getIntegerProperty("NesC", "max_message_payload_size");
    	NESC_PAYLOAD_OVERHEAD = 
    		iniFile.getIntegerProperty("NesC", "payload_overhead");
    	NESC_SYNCHRONIZATION_ERROR = 
    		iniFile.getIntegerProperty("NesC", "synchronization_error");
    	NESC_SYNCHRONIZATION_PERIOD = 
    		iniFile.getIntegerProperty("NesC", "synchronization_period");
    	NESC_POWER_MANAGEMENT = 
    		iniFile.getBooleanProperty("NesC", "power_management");
    	NESC_DELIVER_LAST = iniFile.getBooleanProperty("NesC", "deliver_last");
    	NESC_LED_DEBUG = iniFile.getBooleanProperty("NesC", "led_debug");
    	NESC_MAX_DEBUG_STATEMENTS_IN_TRAY = 
    		iniFile.getBooleanProperty("NesC", "max_debug_statements_in_tray"); 
    	NESC_MAX_DEBUG_STATEMENTS_IN_ROWWINDOW = 
    		iniFile.getBooleanProperty("NesC", 
    		"max_debug_statements_in_row_window"); 
    	NESC_MAX_DEBUG_STATEMENTS_IN_TIMEWINDOW = 
    		iniFile.getBooleanProperty("NesC", 
    		"max_debug_statements_in_time_window");
    	NESC_MAX_DEBUG_STATEMENTS_IN_AGGREGATES =
    		iniFile.getBooleanProperty("NesC", 
    		"max_debug_statements_in_aggregates");

    	//LED Experiment can not be set from the ini file as the default is none.
	}
	
	/**
	 * Reads the debug file settings.  This contains the level of debug 
	 * messages to be logged for each Java class.
	 * @param debugFile The debug file to be read.
	 */
	public static void readDebugFile(final INIFile debugFile) {
		
    	final Map loggerLevels = debugFile.getProperties("Logger_Levels");
    	final Object[] names = loggerLevels.keySet().toArray();
    	for (Object element : names) {
    		LoggerSetup.setLogLevel(debugFile.getStringProperty("Logger_Levels", 
    				element.toString()), element.toString());
    	}
	}
	
	
	/**
	 * Adds the permissible command-line options.
	 * @param opt The options object.
	 */
	public static void addOptions(final Options opt) {

		/* Add all options which may appear at the command line here
		Please enter these in the same order as they appear in the ini file
		note that I haven't included the ini file section name for options 
		in sections General and Input */		
		
		//location of the ini file itself
		opt.getSet().addOption("iniFile", Options.Separator.EQUALS);
	
		//options from the ini file
		opt.getSet().addOption("evaluate-all-queries", 
				Options.Separator.EQUALS);		
		opt.getSet().addOption("query", Options.Separator.EQUALS);

		//General
		opt.getSet().addOption("generate-pdfs", Options.Separator.EQUALS);
		opt.getSet().addOption("delete-old-files", Options.Separator.EQUALS);
				
		//MQE
		opt.getSet().addOption("sinks", Options.Separator.EQUALS);
		
		opt.getSet().addOption("qos-file", Options.Separator.EQUALS);
		opt.getSet().addOption("network-topology-file", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("site-resource-file", Options.Separator.EQUALS);
		opt.getSet().addOption("schema-file", Options.Separator.EQUALS);
		opt.getSet().addOption("operator-metadata-file", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("types-file", Options.Separator.EQUALS);
	
		opt.getSet().addOption("haskell-use", Options.Separator.EQUALS);
		opt.getSet().addOption("haskell-decl-dir", Options.Separator.EQUALS);
		opt.getSet().addOption("haskell-main", Options.Separator.EQUALS);
		opt.getSet().addOption("haskell-decl-file", Options.Separator.EQUALS);

		opt.getSet().addOption("display-topology", Options.Separator.EQUALS);
		opt.getSet().addOption("generate-graphs", Options.Separator.EQUALS);
		
		opt.getSet().addOption("qos-aware-routing", Options.Separator.EQUALS);
		opt.getSet().addOption("qos-aware-where-scheduling", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-aware-when-scheduling", 
				Options.Separator.EQUALS);
		
		opt.getSet().addOption("output-root-dir", Options.Separator.EQUALS);
		opt.getSet().addOption("output-query-plan-dir", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("output-nesc-dir", Options.Separator.EQUALS);
		
		opt.getSet().addOption("display-graphs", Options.Separator.EQUALS);
		opt.getSet().addOption("display-operator-properties", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("display-sensornet-link-properties", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("display-site-properties", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("display-config-graphs", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("display-cost-expressions", 
				Options.Separator.EQUALS);
		
		opt.getSet().addOption("debug-exit-on-error", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("measurements-active-agenda-loops", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("measurements-ignore-in", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("measurements-remove-operators", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("measurements-thin-operators", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("measurements-multi-acquire", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("remove-unrequired-operators", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("push-projection-down", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("combine-acquire-and-select", 
				Options.Separator.EQUALS);

		opt.getSet().addOption("routing-tree-file", Options.Separator.EQUALS);
		opt.getSet().addOption("routing-random-seed", Options.Separator.EQUALS);
		opt.getSet().addOption("routing-trees-to-generate", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("routing-trees-to-keep", 
				Options.Separator.EQUALS);
		
		opt.getSet().addOption(
				"where-scheduling-remove-redundant-exchanges", 
				Options.Separator.EQUALS);
		opt.getSet().addOption(
				"where-scheduling-min-nn",
				Options.Separator.EQUALS);	
		opt.getSet().addOption(
				"where-scheduling-max-nn",
				Options.Separator.EQUALS);	
		opt.getSet().addOption(
				"where-scheduling-hetero-net",
				Options.Separator.EQUALS);	
		opt.getSet().addOption(
				"where-scheduling-heuristic-init-point",
				Options.Separator.EQUALS);
		opt.getSet().addOption(
				"where-scheduling-solver-output",
				Options.Separator.EQUALS);
		
		opt.getSet().addOption(
				"when-scheduling-decrease-bfactor-to-avoid-agenda-overlap", 
				Options.Separator.EQUALS);
		
		opt.getSet().addOption("targets", Options.Separator.EQUALS);
		
		opt.getSet().addOption("nesc-control-radio-off", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-adjust-radio-power", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-synchronization-period", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-power-management", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-deliver-last", Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-led-debug", Options.Separator.EQUALS);

		opt.getSet().addOption("nesc-yellow-experiment", Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-green-experiment", Options.Separator.EQUALS);
		opt.getSet().addOption("nesc-red-experiment", Options.Separator.EQUALS);

		//settings in the QoS file
		opt.getSet().addOption("qos-acquisition-interval", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-min-acquisition-interval", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-max-acquisition-interval", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-max-delivery-time", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-max-total-energy", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-min-lifetime", Options.Separator.EQUALS);
		
		opt.getSet().addOption("qos-max-buffering-factor", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-buffering-factor", 
				Options.Separator.EQUALS);
		opt.getSet().addOption("qos-query-duration", Options.Separator.EQUALS);
		
	    if (!opt.check()) {
	        Utils.handleCriticalException(new Exception(
	        		"Illegal command line option: " + opt.getCheckErrors()));
	    }
	}
	
	/**
	 * Parses the command line agruments, overriding any of the options set in
	 * the ini file.
	 */
	public static void parseCommandLineArgs(final Options opt) {
		
		//please enter these in the same order as they appear in the ini file
		
		if (opt.getSet().isSet("generate-pdfs")) {
			GENERAL_GENERATE_PDFS = opt.getSet().getOption(
				"generate-pdfs").getBooleanResultValue(0);
		}
		
		if (opt.getSet().isSet("delete-old-files")) {
			GENERAL_DELETE_OLD_FILES = opt.getSet().getOption(
				"delete-old-files").getBooleanResultValue(0);
		}		
		
		if (opt.getSet().isSet("query")) {
			INPUTS_QUERY = opt.getSet().getOption("query").getResultValue(0);
		}
		
		//MQE
		if (opt.getSet().isSet("sinks")) { 
    		sinksText = opt.getSet().getOption("sinks").getResultValue(0);
    	
		}
		
		if (opt.getSet().isSet("qos-file")) {
			INPUTS_QOS_FILE = opt.getSet().getOption("qos-file").
					getResultValue(0).toLowerCase();	
		}
		if (opt.getSet().isSet("network-topology-file")) {
			INPUTS_NETWORK_TOPOLOGY_FILE = opt.getSet().
					getOption("network-topology-file").getResultValue(0);
		}
		if (opt.getSet().isSet("site-resource-file")) {
			INPUTS_SITE_RESOURCE_FILE = opt.getSet().
					getOption("site-resource-file").getResultValue(0);
		}
		if (opt.getSet().isSet("schema-file")) {
			INPUTS_SCHEMA_FILE = opt.getSet().getOption("schema-file").
					getResultValue(0);
		}
		if (opt.getSet().isSet("operator-metadata-file")) {
			INPUTS_COST_PARAMETERS = opt.getSet().
					getOption("operator-metadata-file").getResultValue(0);
		}
		if (opt.getSet().isSet("types-file")) {
			INPUTS_TYPES_FILE = opt.getSet().getOption("types-file").
					getResultValue(0);
		}

		if (opt.getSet().isSet("haskell-use")) {
			haskellUse = opt.getSet().getOption(
					"haskell-use").getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("haskell-decl-dir")) {
			haskellDeclDir = opt.getSet().getOption("haskell-decl-dir").
					getResultValue(0);
		}
		if (opt.getSet().isSet("haskell-main")) {
			haskellMain = opt.getSet().getOption("haskell-main").
					getResultValue(0);
		}
		if (opt.getSet().isSet("haskell-decl-file")) {
			haskellDeclFile = opt.getSet().getOption("haskell-decl-file").
					getResultValue(0);
		}
		
		if (opt.getSet().isSet("display-topology")) {
			DISPLAY_SENSOR_NETWORK_TOPOLOGY = opt.getSet().getOption("display-topology").
					getBooleanResultValue(0);
		}
		
		if (opt.getSet().isSet("generate-graphs")) {
			GENERAL_GENERATE_GRAPHS = opt.getSet().getOption("generate-graphs").
					getBooleanResultValue(0);
		}
		
		if (opt.getSet().isSet("qos-aware-routing")) {
			QOS_AWARE_ROUTING = opt.getSet().getOption("qos-aware-routing").
					getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("qos-aware-where-scheduling")) {
			QOS_AWARE_WHERE_SCHEDULING = opt.getSet().getOption(
					"qos-aware-where-scheduling").getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("qos-aware-when-scheduling")) {
			QOS_AWARE_WHEN_SCHEDULING = opt.getSet().getOption(
					"qos-aware-when-scheduling").getBooleanResultValue(0);
		}			
		if (opt.getSet().isSet("output-root-dir")) {
			GENERAL_OUTPUT_ROOT_DIR = Utils.fixDirName(opt.getSet().getOption(
					"output-root-dir").getResultValue(0));
		}		
			
		if (opt.getSet().isSet("debug-exit-on-error")) {
			DEBUG_EXIT_ON_ERROR = opt.getSet().getOption("debug-exit-on-error")
			.getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("measurements-active-agenda-loops")) {
			MEASUREMENTS_ACTIVE_AGENDA_LOOPS = opt.getSet().getOption(
				"measurements-active-agenda-loops").getIntResultValue(0);
		}
		if (opt.getSet().isSet("measurements-ignore-in")) {
			MEASUREMENTS_IGNORE_IN = opt.getSet().getOption(
				"measurements-ignore-in").getResultValue(0).toLowerCase();
		}
		if (opt.getSet().isSet("measurements-remove-operators")) {
			MEASUREMENTS_REMOVE_OPERATORS 
				= opt.getSet().getOption("measurements-remove-operators")
				.getResultValue(0).toLowerCase();
		}
		if (opt.getSet().isSet("measurements-thin-operators")) {
			MEASUREMENTS_THIN_OPERATORS 
				= opt.getSet().getOption("measurements-thin-operators")
				.getResultValue(0).toLowerCase();
		}
		if (opt.getSet().isSet("measurements-multi-acquire")) {
			MEASUREMENTS_MULTI_ACQUIRE 
				= opt.getSet().getOption("measurements-multi-acquire")
				.getIntResultValue(0);
		}
		if (opt.getSet().isSet("display-graphs")) {
			DISPLAY_GRAPHS = opt.getSet().getOption("display-graphs")
			.getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("display-operator-properties")) {
			DISPLAY_OPERATOR_PROPERTIES = opt.getSet().getOption(
					"display-operator-properties").getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("display-sensornet-link-properties")) {
			DISPLAY_SENSORNET_LINK_PROPERTIES = opt.getSet().getOption(
					"display-sensornet-link-properties").
					getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("display-site-properties")) {
			DISPLAY_SITE_PROPERTIES = opt.getSet().getOption(
					"display-site-properties").getBooleanResultValue(0);
		}	
		if (opt.getSet().isSet("display-config-graphs")) {
			DISPLAY_CONFIG_GRAPHS = opt.getSet().getOption(
					"display-config-graphs").getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("display-cost-expressions")) {
			DISPLAY_COST_EXPRESSIONS = opt.getSet().getOption(
					"display-cost-expressions").getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("routing-tree-file")) {
			ROUTING_TREE_FILE = opt.getSet().getOption(
					"routing-tree-file").getResultValue(0);
		}
		if (opt.getSet().isSet("routing-random-seed")) {
			ROUTING_RANDOM_SEED = opt.getSet().getOption(
					"routing-random-seed").getIntResultValue(0);
		}	
		if (opt.getSet().isSet("remove-unrequired-operators")) {
			LOGICAL_OPTIMIZATION_REMOVE_UNREQUIRED_OPERATORS = 
					opt.getSet().getOption(
					"remove-unrequired-operators").getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("push-projection-down")) {
			LOGICAL_OPTIMIZATION_PUSH_PROJECTION_DOWN = 
					opt.getSet().getOption(
					"push-projection-down").getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("combine-acquire-and-select")) {
			LOGICAL_OPTIMIZATION_COMBINE_ACQUIRE_AND_SELECT = 
					opt.getSet().getOption(
					"combine-acquire-and-select").getBooleanResultValue(0);
		}		

		
		if (opt.getSet().isSet("routing-trees-to-generate")) {
			ROUTING_TREES_TO_GENERATE = opt.getSet().getOption(
					"routing-trees-to-generate").getIntResultValue(0);
		}			
		if (opt.getSet().isSet("routing-trees-to-keep")) {
			ROUTING_TREES_TO_KEEP = opt.getSet().getOption(
					"routing-trees-to-keep").getIntResultValue(0);
		}
		if (opt.getSet().isSet("where-scheduling-remove-redundant-exchanges")) {
			WHERE_SCHEDULING_REMOVE_REDUNDANT_EXCHANGES = opt.getSet().getOption(
					"where-scheduling-remove-redundant-exchanges").getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("where-scheduling-min-nn")) {
			WHERE_SCHEDULING_MIN_NN = opt.getSet().getOption(
					"where-scheduling-min-nn").getIntResultValue(0);
		}		
		if (opt.getSet().isSet("where-scheduling-max-nn")) {
			WHERE_SCHEDULING_MAX_NN = opt.getSet().getOption(
					"where-scheduling-max-nn").getIntResultValue(0);
		}
		if (opt.getSet().isSet("where-scheduling-hetero-net")) {
			WHERE_SCHEDULING_HETEROGENEOUS_NETWORK = opt.getSet().getOption(
					"where-scheduling-hetero-net").getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("where-scheduling-heuristic-init-point")) {
			WHERE_SCHEDULING_HEURISTIC_INITIAL_POINT = opt.getSet().getOption(
					"where-scheduling-heuristic-init-point").getBooleanResultValue(0);
		}
		
		if (opt.getSet().isSet("where-scheduling-solver-output")) {
			WHERE_SCHEDULING_SOLVER_OUTPUT_FILE = opt.getSet().getOption(
					"where-scheduling-solver-output").getResultValue(0);
		}
		
		if (opt.getSet().isSet("when-scheduling-decrease-bfactor-to-avoid-"
		    	+ "agenda-overlap")) {
			WHEN_SCHEDULING_DECREASE_BFACTOR_TO_AVOID_AGENDA_OVERLAP 
			    = opt.getSet().getOption("when-scheduling-decrease-bfactor-to-"
			    		+ "avoid-agenda-overlap").getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("targets")) {
			String targetStr = opt.getSet().getOption("targets").getResultValue(0);
			CODE_GENERATION_TARGETS = CodeGenTarget.parseCodeTargets(targetStr);
		}
		if (opt.getSet().isSet("nesc-control-radio-off")) {
			NESC_CONTROL_RADIO_OFF = opt.getSet().
					getOption("nesc-control-radio-off").
					getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("nesc-adjust-radio-power")) {
			NESC_ADJUST_RADIO_POWER = opt.getSet().
					getOption("nesc-adjust-radio-power").
					getBooleanResultValue(0);
		}		
		if (opt.getSet().isSet("nesc-synchronization-period")) {
			NESC_SYNCHRONIZATION_PERIOD = opt.getSet().
					getOption("nesc-synchronization-period").
					getIntResultValue(0);
		}
		if (opt.getSet().isSet("nesc-power-management")) {
			NESC_POWER_MANAGEMENT = opt.getSet().
					getOption("nesc-power-management").
					getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("nesc-deliver-last")) {
			NESC_DELIVER_LAST = opt.getSet().getOption("nesc-deliver-last").
					getBooleanResultValue(0);
		}
		if (opt.getSet().isSet("nesc-led-debug")) {
			NESC_LED_DEBUG = opt.getSet().getOption("nesc-led-debug").
					getBooleanResultValue(0);
			if (! NESC_LED_DEBUG) {
				if ((NESC_YELLOW_EXPERIMENT != null) || 
					(NESC_GREEN_EXPERIMENT != null) ||
					(NESC_GREEN_EXPERIMENT != null)) {
					logger.info("Overriding nesc-led-debug flag"+
							"due to nesc experiment set.");
					NESC_LED_DEBUG = true;	
				}
			}
		}
		if (opt.getSet().isSet("nesc-yellow-experiment")) {
			NESC_YELLOW_EXPERIMENT = 
				opt.getSet().getOption("nesc-yellow-experiment").
					getResultValue(0);
			NESC_LED_DEBUG = true;
		}
		if (opt.getSet().isSet("nesc-green-experiment")) {
			NESC_GREEN_EXPERIMENT = 
				opt.getSet().getOption("nesc-green-experiment").
					getResultValue(0);
			NESC_LED_DEBUG = true;
		}
		if (opt.getSet().isSet("nesc-red-experiment")) {
			NESC_RED_EXPERIMENT = 
				opt.getSet().getOption("nesc-red-experiment").
					getResultValue(0);
			NESC_LED_DEBUG = true;
		}
	}

	/**
	 * Checks that the files and directories referenced exist (or, if 
	 * applicable, that they can be created).
	 *
	 */
	public static void checkFilesAndDirs(String source) {
		
		try {

			//check if files/dirs referenced exist
			Utils.checkFile(INPUTS_NETWORK_TOPOLOGY_FILE, source);
			Utils.checkFile(INPUTS_SITE_RESOURCE_FILE, source);
			Utils.checkFile(INPUTS_SCHEMA_FILE, source);
			if (!INPUTS_QOS_FILE.equals("none")) {
				Utils.checkFile(INPUTS_QOS_FILE, source);
			}
			Utils.checkFile(INPUTS_TYPES_FILE, source);
	    	for (int i =0;i<INPUTS_QUERIES.length;i++)
	    	    Utils.checkFile(INPUTS_QUERIES[i], source);
						
			Utils.checkDirectory(GENERAL_OUTPUT_ROOT_DIR, true);
			if (ROUTING_TREE_FILE!=null) {
				Utils.checkFile(ROUTING_TREE_FILE, source);				
			}
			if (WHERE_SCHEDULING_SOLVER_OUTPUT_FILE!=null) {
				Utils.checkFile(WHERE_SCHEDULING_SOLVER_OUTPUT_FILE, source);
			}
		} catch (final IOException e) {
			Utils.handleCriticalException(e);
		}
	}
	
	//MQE
	//Fills the queries array with queries file names 
	public static void fillQueriesArray()
	{
		INPUTS_QUERIES = INPUTS_QUERY.split(",");
	}
	
	//MQE
	// Fills the sinks collection
	public static void fillSinksCollecion() 
	{

		String [] sinks=null;    	
		
		int i=0;
    	if (sinksText !=null)
    	{
    		sinks = sinksText.split(",");
    		for (;i<sinks.length;i++)
    			INPUTS_METADATA_SINKS.add(Integer.valueOf(sinks[i]));
    	}
    	//Fill The Rest of the Sinks
    	for (;i<INPUTS_QUERIES.length;i++)
    		INPUTS_METADATA_SINKS.add(defaultSink);
	}
	/**
	 * Records some of the parameters on the log files.
	 */
	public static void recordParameters() {
		logger.info("nesc-control-radio-off=" + NESC_CONTROL_RADIO_OFF);
		logger.info("nesc-deliver-last=" + NESC_DELIVER_LAST);
	}
	
	/**
	 * Intialization -- reads ini file and command-line settings.
	 * @param opt the command-line object.
	 */
	public static void initialize(final Options opt) {
		
		//define the command-line options
		addOptions(opt);
		
		//get ini file to be read (if non specified in command line, 
		//use Constants.java)
		String iniFile = null;
		try {
			if (opt.getSet().isSet("iniFile")) {
				iniFile = opt.getSet().getOption("iniFile").getResultValue(0);
				Utils.checkFile(iniFile,"Command-line parameters");
			} else {
				iniFile = Constants.INI_FILE;
				Utils.checkFile(iniFile,"Constants class");
			}
		} catch (final IOException e) {
			Utils.handleCriticalException(e);
		}
		
		//read settings from ini file
		readIniFile(new INIFile(iniFile));
		
		//override the settings read in the ini file by any command line 
		//settings
		//(the QoS settings can only be done later)
		parseCommandLineArgs(opt);

		LocalSettings.initialize();
		
		//read settings from debug file
		if (new File(Constants.DEBUG_FILE).exists()) {
			readDebugFile(new INIFile(Constants.DEBUG_FILE));			
		}

		
		//MQE
		fillQueriesArray();
		fillSinksCollecion();
		//check constraints between settings
    	if (DISPLAY_GRAPHS) {
    		GENERAL_GENERATE_GRAPHS = true;
    	}
    	
		//purge old files if needed
    	if (GENERAL_DELETE_OLD_FILES) {
    		Utils.deleteDirectoryContents(GENERAL_OUTPUT_ROOT_DIR);
    	}

		//check files and directories referenced exist
		checkFilesAndDirs(iniFile);
	
		//Separates the query filename from the directory
		//We will need to do this slightly differently when MQE is supported
    	INPUTS_QUERY_DIR = 
       		Utils.fixDirName(new File(INPUTS_QUERY).getParent());
    	INPUTS_QUERY = new File(INPUTS_QUERY).getName();
    	INPUTS_QUERIES[0] = INPUTS_QUERY; 
		
		LoggerSetup.setMainLogFile(GENERAL_OUTPUT_ROOT_DIR + "/" + mainLogFile);
    	LoggerSetup.addMemoryHandler(GENERAL_OUTPUT_ROOT_DIR + "/" 
    			+ MemoryHandlerFile);
    	LoggerSetup.setFileLevel(maxFileLogLevel);

    	recordParameters();
	}
	 /**
	  * Directory that has the Haskell decl file.
	  * 
	  * @return Full Path.
	  */
	public static String getHaskellDeclFile() {
		return haskellDeclDir + "/" + haskellDeclFile;
	}

	 /**
	  * the Haskell translator.
	  * 
	  * @return Full Path.
	  */
	public static String getHaskellMain() {
		return haskellDeclDir + "/" + haskellMain;
	}
	
	/**
	 * Temporary setting to only use Haskell in test environment.
	 * Will be deprecated to true
	 * 
	 * @return Temp test to turn on Haskell for testing only.
	 */ 
	public static boolean useHaskell() {
		return haskellUse;
	}
	
}
