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
package uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Template;
import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AMRecieveComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AMSendComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AcquireComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.ActiveMessageIDGenerator;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AggrEvalComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AggrInitComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.AggrMergeComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.CC1000ControlComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.CodeGenUtils;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.CodeGenerationException;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.DeliverComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.ExchangeProducerComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.FragmentComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.JoinComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.LedComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.MainComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.NesCComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.NesCConfiguration;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.PowerManagementComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.ProjectComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.QueryPlanModuleComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.RXT1Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.RXT2Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.RadioComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.SelectComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.SensorT1Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.SensorT2Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.TXT1Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.TXT2Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.TimerT1Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.TimerT2Component;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.TrayComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt.WindowComponent;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePart;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.ExchangePartType;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.QueryPlan;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.TraversalOrder;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrEvalOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrInitializeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AggrIterateOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.DeliverOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ExchangeOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.JoinOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.ProjectOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.SelectOperator;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.WindowOperator;

/**
 *
 * Code Generation: step 8 of query compilation.
 *
 * <i>Code Generation</i> generates executable code for each site based on the
 * distributed QEP, RT and the agenda.
 *
 * @author Ixent Galpin, Farhana Jabeen, Christian Brenninkmeijer
 *
 */
public class NesCGeneration {

    static Logger logger = Logger.getLogger(NesCGeneration.class.getName());

	/**
	 * The directory where nesC interfaces, used as input to the generator,
	 * are located.
	 */
	public static String NESC_TEMPLATES_DIR;

	/**
	 * The directory where nesC interfaces, used as input to the generator,
	 * are located.
	 * This is derived from NESC_TEMPLATES_DIR
	 */
	public static String NESC_INTERFACES_DIR;

	/**
	 * The directory where nesC module templates, used as input to the
	 * generator, are located.
	 * This is derived from NESC_TEMPLATES_DIR
	 */
	public static String NESC_MODULES_DIR;

	/**
	 * The directory where nesC misc files, used as input to the
	 * generator, are located.
	 * This is derived from NESC_TEMPLATES_DIR
	 */
	public static String NESC_MISC_FILES_DIR;


	/**
	 * Constants for the components and interfaces used
     * TinyOS 1 and 2
	 */
    public static String COMPONENT_RADIO;

    private static String COMPONENT_MAIN;

    public static String COMPONENT_QUERY_PLAN;

    private static String COMPONENT_QUERY_PLANC;

    private static String COMPONENT_SENSOR;

    public static String INTERFACE_DO_TASK;

    public static String INTERFACE_GET_TUPLES;

    public static String INTERFACE_PUT_TUPLES;

    public static String INTERFACE_RECEIVE;

    public static String INTERFACE_SEND;

    public static String INTERFACE_TIMER;

    private static String INTERFACE_READ;

    // TinyOS 1 only
    private static String COMPONENT_TIMER;

    private static String COMPONENT_LEDS;

    private static String COMPONENT_POWER_MANAGEMENT;

    private static String COMPONENT_CC1000CONTROL;

    private static String INTERFACE_SENSOR;

    public static String INTERFACE_STDCONTROL;

    private static String INTERFACE_LEDS;

    private static String INTERFACE_CC1000CONTROL;

    // TinyOS2 Only
    private static String COMPONENT_AGENDA_TIMER;

    private static String COMPONENT_RADIOTX;

    private static String COMPONENT_RADIORX;

    private static String INTERFACE_BOOT;

    private static String INTERFACE_PACKET;

    public static String INTERFACE_SNOOZE;

    public static String INTERFACE_SPLITCONTROL;

    public static String TYPE_TMILLI;

    public static String TYPE_READ;


    /**
     * Initialization method.
     * @param tosVersion
     * @param tossimFlag
     * @param plan
     * @throws IOException
     */
    public static void initialize(int tosVersion, boolean tossimFlag,
    		QueryPlan plan) throws IOException {
    	initConstants(tosVersion, tossimFlag);

    	if (tosVersion == 1) {
    		NESC_TEMPLATES_DIR = "src/tinyOS1Templates/";
    	} else {
    		NESC_TEMPLATES_DIR = "src/tinyOS2Templates/";
    	}
    	NESC_INTERFACES_DIR = NESC_TEMPLATES_DIR + "interfaces/";
    	NESC_MODULES_DIR = NESC_TEMPLATES_DIR + "modules/";
    	NESC_MISC_FILES_DIR = NESC_TEMPLATES_DIR + "misc/";

    	Utils.checkDirectory(NESC_MODULES_DIR, false);
    	Utils.checkDirectory(NESC_INTERFACES_DIR, false);
    	Utils.checkDirectory(NESC_MISC_FILES_DIR, false);

		computeOperatorTupleSizes(plan);
    }

    /**
     * Initialises the constants which specify the component names. These vary
     * depending on whether we are generating TinyOS1/TinyOS2 nesC code, and
     * whether it is for the Tossim simulator or not.
     *
     * @param tossimFlag	Specifies whether Tossim code is being generated.
     */
    public static void initConstants(int tosVersion, boolean tossimFlag) {
		if (tosVersion == 1) {
		    COMPONENT_RADIO = "GenericComm";
		    COMPONENT_MAIN = "Main";
		    COMPONENT_QUERY_PLAN = "QueryPlanM";
		    COMPONENT_QUERY_PLANC = "QueryPlan";
		    if (!tossimFlag) {
		    	COMPONENT_SENSOR = "PhotoTemp";
		    } else {
		    	COMPONENT_SENSOR = "ADCC";
		    }
		    COMPONENT_TIMER = "TimerC";
		    COMPONENT_LEDS = "LedsC";
		    COMPONENT_POWER_MANAGEMENT = "HPLPowerManagementM";
		    COMPONENT_CC1000CONTROL = "CC1000ControlM";
		    INTERFACE_DO_TASK = "DoTask";
		    INTERFACE_GET_TUPLES = "GetTuples";
		    INTERFACE_PUT_TUPLES = "PutTuples";
		    INTERFACE_RECEIVE = "ReceiveMsg";
		    INTERFACE_SEND = "SendMsg";
		    INTERFACE_TIMER = "Timer";
		    INTERFACE_READ = "ADC";
		    INTERFACE_SENSOR = "ExternalPhotoADC";
		    INTERFACE_STDCONTROL = "StdControl";
		    INTERFACE_LEDS = "Leds";
		    INTERFACE_CC1000CONTROL = "CC1000Control";
		} else {
		    COMPONENT_AGENDA_TIMER = "AgendaTimer";
		    COMPONENT_MAIN = "MainC";
		    COMPONENT_QUERY_PLAN = "QueryPlanC";
		    COMPONENT_QUERY_PLANC = "QueryPlanAppC";
		    COMPONENT_RADIO = "ActiveMessageC";
		    COMPONENT_RADIOTX = "AMSenderC";
		    COMPONENT_RADIORX = "AMRecieverC";
		    COMPONENT_SENSOR = "DemoSensorC"; // was Photo
		    INTERFACE_DO_TASK = "DoTask";
		    INTERFACE_GET_TUPLES = "GetTuples";
		    INTERFACE_PUT_TUPLES = "PutTuples";
		    INTERFACE_RECEIVE = "Receive";
		    INTERFACE_SEND = "AMSend";
		    INTERFACE_TIMER = "Timer";
		    INTERFACE_BOOT = "Boot";
		    INTERFACE_PACKET = "Packet";
		    INTERFACE_READ = "Read";
		    INTERFACE_SNOOZE = "Snooze";
		    INTERFACE_SPLITCONTROL = "SplitControl";
		    TYPE_TMILLI = "TMilli";
		    TYPE_READ = "uint16_t";
		}
    }


    /**
     * Precompute the operator tuple sizes and store in hashtable.
     * @param plan The query plan to compute tuples sizes for.
     */
    public static void computeOperatorTupleSizes(final QueryPlan plan) {

		final Iterator<Operator> opIter = plan
			.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = (Operator) opIter.next();

		    final ArrayList<Attribute> attributes
		    	= op.getAttributes();
		    int tupleSize = 0;

		    for (int i = 0; i<attributes.size();i++) {
		    	final AttributeType attrType = attributes.get(i).getType();
				tupleSize += attrType.getSize();
		    }
		    assert (tupleSize > 0);

		    if (tupleSize != op.getPhysicalTupleSize()) {
				final OptimizationException e = new OptimizationException(
					"Tuple size calculated by NesCGeneration != tuple size reported by operator");
		    }
		    System.out.println("About to compute tuple size for operator "+op.getID());
		    CodeGenUtils.outputTypeSize.put(CodeGenUtils
			    .generateOutputTupleType(op), new Integer(tupleSize));
		    // store for later
		}
    }


    /**
     * Generates the configurations for each individual site.
     * @param plan  The query plan which code is being generated for.
     * @param qos  The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @return
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static HashMap<Site, NesCConfiguration> generateSiteConfigurations(
	    final QueryPlan plan, final QoSSpec qos,
	    Integer sink, int tosVersion, boolean tossimFlag) throws IOException,
	    CodeGenerationException {
		if (tosVersion == 1) {
		    return t1GenerateSiteConfigs(plan, qos, sink, tosVersion, tossimFlag);
		} else {
		    return t2GenerateSiteConfigs(plan, qos, sink, tosVersion, tossimFlag);
		}
    }


    /**
     * TOS1: Generates the top-level configurations for each site in the sensor
     * network.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @return
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static HashMap<Site, NesCConfiguration> t1GenerateSiteConfigs(
	    final QueryPlan plan,
	    final QoSSpec qos,
	    int sink,
	    int tosVersion,
	    boolean tossimFlag)
	    throws IOException, CodeGenerationException {

		final HashMap<Site, NesCConfiguration> siteConfigs = new HashMap<Site, NesCConfiguration>();

		int acquireCount = 0;

		final Iterator<Site> siteIter = plan.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			final Site currentSite = siteIter.next();
			final String currentSiteID = currentSite.getID();

			/* Instantiate the top-level site configuration */
			final NesCConfiguration config = new NesCConfiguration(
				COMPONENT_QUERY_PLANC + currentSite.getID(), //%
				plan,
				currentSite,
				tosVersion,
				tossimFlag);

			/* Add the components which are always needed */
			t1AddMainSiteComponents(plan, qos, sink, tosVersion, tossimFlag,
					config);

			/* Add optional components for Led debugging */
			t1AddLEDComponents(tosVersion, tossimFlag, config);

			/* Add the Power Management components */
			t1AddPowerManagmentComponents(tosVersion, tossimFlag, config);

			/* Add functionality for changing radio transmit power */
			t1AddRadioPowerAdjustComponents(tossimFlag, config);

			/* Add the DAF fragments allocated to this site */
			acquireCount = t1AddSiteFragments(tosVersion, tossimFlag,
					acquireCount, currentSite, config);

			/* Wire the fragments with trays, radio receive and transmit*/
			wireSiteFragments(plan, qos, tosVersion, tossimFlag, currentSite,
					currentSiteID, config);

			siteConfigs.put(currentSite, config);
		}
		return siteConfigs;
    }

    /**
     * TOS1: Add the components which are always needed a site configuration.
     * @param plan   The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @param config The nesC configuration which components/wirings are being added to.
     * @throws CodeGenerationException
     */
	private static void t1AddMainSiteComponents(final QueryPlan plan,
			final QoSSpec qos, int sink, int tosVersion, boolean tossimFlag,
			final NesCConfiguration config) throws CodeGenerationException {

		MainComponent mainComp = new MainComponent(COMPONENT_MAIN, config,
			tosVersion, tossimFlag);
		config.addComponent(mainComp);

		//Main query plan class
		QueryPlanModuleComponent queryPlanModuleComp =
			new QueryPlanModuleComponent(COMPONENT_QUERY_PLAN, config,
			plan, qos, sink, tosVersion, tossimFlag);
		config.addComponent(queryPlanModuleComp);

		TimerT1Component timerComp = new TimerT1Component( //$
			COMPONENT_TIMER, config, tossimFlag);
		config.addComponent(timerComp);

		//In TinyOS 1 the radio component is assumed to be always
		//required, as it is assumed that a node will transmit over
		//the airwaves or transmit over the serial port or both.  For all
		//the aforementioned cases a radio component is needed.
		final RadioComponent radioComp = new RadioComponent(
			COMPONENT_RADIO, config, tossimFlag);
		config.addComponent(radioComp);

		//Entry point to the query plan code
		config.addWiring(COMPONENT_MAIN, COMPONENT_QUERY_PLAN,
			INTERFACE_STDCONTROL, INTERFACE_STDCONTROL,
			INTERFACE_STDCONTROL);
		//Used to switch on the timer
		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_TIMER,
			INTERFACE_STDCONTROL, INTERFACE_TIMER + INTERFACE_STDCONTROL,
			INTERFACE_STDCONTROL);
		//Required to turn the radio on/off
		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_RADIO,
			INTERFACE_STDCONTROL, "CommControl", "Control");
		//Required for synchronization in TOS1	//$
		if (tossimFlag) {
			config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_TIMER,
					INTERFACE_TIMER, "SyncTimer", INTERFACE_TIMER
						+ "[unique(\"Timer\")]");
		}
		//Used to trigger agenda tasks
		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_TIMER,
			INTERFACE_TIMER, "AgendaTimer", INTERFACE_TIMER
				+ "[unique(\"Timer\")]");
	}


	/**
	 * TOS1: Add optional components for Led debugging.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param config The nesC confiugration which components/wirings are being added to.
	 * @throws CodeGenerationException
	 */
	private static void t1AddLEDComponents(int tosVersion, boolean tossimFlag,
			final NesCConfiguration config) throws CodeGenerationException {
		if (Settings.NESC_LED_DEBUG) {
			config.addComponent(new LedComponent(COMPONENT_LEDS, config,
				tosVersion, tossimFlag));
			config.addWiring(COMPONENT_QUERY_PLAN,
				COMPONENT_LEDS, INTERFACE_LEDS, INTERFACE_LEDS, INTERFACE_LEDS);
		}
	}


	/**
	 * TOS1: Add the Power Management components.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param config The nesC confiugration which components/wirings are being added to.
	 * @throws CodeGenerationException
	 */
	private static void t1AddPowerManagmentComponents(int tosVersion,
			boolean tossimFlag, final NesCConfiguration config)
			throws CodeGenerationException {
		if (Settings.NESC_POWER_MANAGEMENT) {
			config.addComponent(new PowerManagementComponent(
				COMPONENT_POWER_MANAGEMENT, config, tosVersion, tossimFlag));
			config.addWiring(COMPONENT_QUERY_PLAN,
				COMPONENT_POWER_MANAGEMENT, "command result_t PowerEnable();",
				"PowerEnable", "Enable");
			config.addWiring(COMPONENT_QUERY_PLAN,
				COMPONENT_POWER_MANAGEMENT, "command result_t PowerDisable();",
				"PowerDisable", "Disable");
		}
	}


	/**
	 * TOS1: Add functionality for changing radio transmit power.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param config The nesC confiugration which components/wirings are being added to.
	 * @throws CodeGenerationException
	 */
	private static void t1AddRadioPowerAdjustComponents(boolean tossimFlag,
			final NesCConfiguration config) throws CodeGenerationException {
		if (!tossimFlag && Settings.NESC_ADJUST_RADIO_POWER) {
			//Wiring to enable radio transmit power to be adjusted
			final CC1000ControlComponent CC1000ControlComp =
				new CC1000ControlComponent(COMPONENT_CC1000CONTROL, config, tossimFlag);
			config.addComponent(CC1000ControlComp);
			config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_CC1000CONTROL,
					INTERFACE_CC1000CONTROL);
		}
	}


	/**
	 * TOS1: Add the DAF fragments allocated to this site.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.s
	 * @param acquireCount The number of acquire operators encountered on this site.
	 * @param currentSite The site for which code is being generated.
	 * @param config The nesC configuration which components/wirings are being added to.
	 * @return
	 * @throws CodeGenerationException
	 */
	private static int t1AddSiteFragments(int tosVersion, boolean tossimFlag,
			int acquireCount, final Site currentSite,
			final NesCConfiguration config) throws CodeGenerationException {
		final Iterator<Fragment> fragIter = currentSite.getFragments()
			.iterator();
		while (fragIter.hasNext()) {
			final Fragment frag = fragIter.next();

			/* Add component for current fragment */
			final FragmentComponent fragComp = new FragmentComponent(
				frag, config, tosVersion, tossimFlag);
			config.addComponent(fragComp);

			/* Wire fragment to main query plan component */
			config.addWiring(COMPONENT_QUERY_PLAN, fragComp.getID(),
				INTERFACE_DO_TASK, CodeGenUtils
					.generateUserAsDoTaskName(frag, currentSite),
				INTERFACE_DO_TASK);

			/* Wire fragment to hardware devices as required */
			acquireCount = t1WireFragToDevices(tossimFlag, acquireCount,
					currentSite, config, frag, fragComp);
		}
		return acquireCount;
	}


	/**
	 * TOS1: Wire fragment component to required devices, e.g., serial port or
	 * sensors.
	 * @param tossimFlag
	 * @param acquireCount
	 * @param currentSite
	 * @param config
	 * @param frag
	 * @param fragComp
	 * @return
	 * @throws CodeGenerationException
	 */
	private static int t1WireFragToDevices(boolean tossimFlag, int acquireCount,
			final Site currentSite, final NesCConfiguration config,
			final Fragment frag, final FragmentComponent fragComp)
			throws CodeGenerationException {
		final Iterator<Operator> opIter = frag
			.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
			final Operator op = opIter.next();
			if (op instanceof AcquireOperator) {
				acquireCount = t1WireFragToSensors(tossimFlag,
					acquireCount, currentSite, config, fragComp, op);

			} else if (op instanceof DeliverOperator) {
				String sendInterface =
					CodeGenUtils.generateProviderSendInterfaceName(
					"AM_UART_MESSAGE");
				config.addWiring(fragComp.getID(), COMPONENT_RADIO,
					INTERFACE_SEND, "SendDeliver", sendInterface);
			}

			if (Settings.NESC_LED_DEBUG) {
			config.addWiring(fragComp.getID(), COMPONENT_LEDS,
				INTERFACE_LEDS, "Leds", INTERFACE_LEDS);
			}

		}
		return acquireCount;
	}


	/**
	 * TOS1: Wire fragment component to the sensor component.
	 * @param tossimFlag
	 * @param acquireCount
	 * @param currentSite
	 * @param config
	 * @param fragComp
	 * @param op
	 * @return
	 * @throws CodeGenerationException
	 */
	private static int t1WireFragToSensors(boolean tossimFlag,
			int acquireCount, final Site currentSite,
			final NesCConfiguration config, final FragmentComponent fragComp,
			final Operator op) throws CodeGenerationException {

		final SensorT1Component sensorComp = new SensorT1Component(
				currentSite, currentSite, COMPONENT_SENSOR, config,
				tossimFlag);

		config.addComponent(sensorComp);

		final int numSensedAttr
			= ((AcquireOperator) op).getNumSensedAttributes();
		for (int i = 0; i < numSensedAttr; i++) {
			acquireCount++;

			if (!tossimFlag) {
				config.addWiring(fragComp.getID(),
					COMPONENT_SENSOR, INTERFACE_READ,
					"Op" + op.getID() + INTERFACE_READ
					+ i,
					INTERFACE_SENSOR);
			} else {
				config.addWiring(fragComp.getID(),
					COMPONENT_SENSOR, INTERFACE_READ,
					"Op" + op.getID() + INTERFACE_READ + i,
					"ADC[" + acquireCount+ "]");
			}
		}
		return acquireCount;
	}


	/**
	 * TOS1: Wire the fragments with trays, radio receive and transmit.
	 * @param plan The query plan which code is being generated for.
	 * @param qos The user-specified quality-of-service requirements.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param currentSite The site for which code is being generated.
	 * @param currentSiteID The id of the site for which code is being generated.
	 * @param config The nesC configuration which components/wirings are being added to.
	 * @throws CodeGenerationException
	 */
    private static void wireSiteFragments(final QueryPlan plan,
			final QoSSpec qos, int tosVersion, boolean tossimFlag,
			final Site currentSite, final String currentSiteID,
			final NesCConfiguration config) throws CodeGenerationException {

		/* Wire fragments up accordingly */
		final Iterator<ExchangePart> exchPartIter =
			currentSite.getExchangeComponents().iterator();
		while (exchPartIter.hasNext()) {
			final ExchangePart exchPart = exchPartIter.next();
			final Fragment sourceFrag = exchPart.getSourceFrag();
			final Fragment destFrag = exchPart.getDestFrag();
			final String destSiteID = exchPart.getDestSiteID();

			final String trayName = addTrayComponent(plan, qos, tosVersion,
					tossimFlag, currentSite, config, sourceFrag, destFrag,
					destSiteID);

			/* linking two remote fragments */
			if (exchPart.isRemote()) {

				config.addComponent(new RadioComponent(COMPONENT_RADIO, config,
						tossimFlag));
				
				/* Remote consumer */
				if (exchPart.getComponentType()
						== ExchangePartType.CONSUMER) {
					addRemoteConsumer(plan, qos, tosVersion, tossimFlag,
							currentSite, currentSiteID, config, exchPart,
							sourceFrag, destFrag, destSiteID, trayName);

				/* Remote producer */
				} else if (exchPart.getComponentType()
						== ExchangePartType.PRODUCER) {
					addRemoteProducer(plan, qos, tosVersion, tossimFlag,
							currentSite, currentSiteID, config, exchPart,
							sourceFrag, destFrag, destSiteID, trayName);

				/* Relay */
				} else if (exchPart.getComponentType()
						== ExchangePartType.RELAY) {
					addRelay(plan, qos, tosVersion, tossimFlag, currentSite,
							currentSiteID, config, exchPart, sourceFrag,
							destFrag, destSiteID, trayName);
				}

			} else {
				/* link two local fragments on the same site */
				connectLocalFragments(tosVersion, currentSite, config,
						sourceFrag, destFrag, trayName);
			}
		}
	}


    /**
     * TOS1: Add a remote consumer.
     * @param plan
     * @param qos
     * @param tosVersion
     * @param tossimFlag
     * @param currentSite
     * @param currentSiteID
     * @param config
     * @param exchComp
     * @param sourceFrag
     * @param destFrag
     * @param destSiteID
     * @param trayName
     * @throws CodeGenerationException
     */
	private static void addRemoteConsumer(final QueryPlan plan,
			final QoSSpec qos, int tosVersion, boolean tossimFlag,
			final Site currentSite, final String currentSiteID,
			final NesCConfiguration config, final ExchangePart exchPart,
			final Fragment sourceFrag, final Fragment destFrag,
			final String destSiteID, final String trayName)
			throws CodeGenerationException {
		final String destFragCompName = FragmentComponent
			.generateName(destFrag, currentSite, tosVersion);
		config.addWiring(destFragCompName,
			trayName,
			CodeGenUtils
				.generateGetTuplesInterfaceInstanceName(sourceFrag),
			CodeGenUtils
				.generateGetTuplesInterfaceInstanceName(sourceFrag),
			INTERFACE_GET_TUPLES);

		if (tosVersion==1) {
			t1AddRXComponent(plan, qos, currentSite, currentSiteID,
				config, exchPart, sourceFrag, destFrag,
				destSiteID, trayName, tossimFlag);
		} else {
			t2AddRXComponent(plan, qos, tossimFlag, config, exchPart,
					sourceFrag, destFrag, destSiteID, trayName);
		}
	}


	/**
	 * TOS1: Add a remote producer.
	 * @param plan
	 * @param qos
	 * @param tosVersion
	 * @param tossimFlag
	 * @param currentSite
	 * @param currentSiteID
	 * @param config
	 * @param exchComp
	 * @param sourceFrag
	 * @param destFrag
	 * @param destSiteID
	 * @param trayName
	 * @throws CodeGenerationException
	 */
	private static void addRemoteProducer(final QueryPlan plan,
			final QoSSpec qos, int tosVersion, boolean tossimFlag,
			final Site currentSite, final String currentSiteID,
			final NesCConfiguration config, final ExchangePart exchPart,
			final Fragment sourceFrag, final Fragment destFrag,
			final String destSiteID, final String trayName)
			throws CodeGenerationException {
		final String sourceFragCompName = FragmentComponent
			.generateName(sourceFrag, currentSite, tosVersion);
		config.addWiring(sourceFragCompName,
			trayName,
			CodeGenUtils
				.generatePutTuplesInterfaceInstanceName(sourceFrag),
			CodeGenUtils
				.generatePutTuplesInterfaceInstanceName(sourceFrag),
			INTERFACE_PUT_TUPLES);

		if (tosVersion==1) {
			t1AddTXComponent(plan, qos, currentSiteID, config,
					exchPart, sourceFrag, destFrag, destSiteID,
					trayName, tossimFlag);			
		} else {
			t2AddTXComponent(plan, qos, tossimFlag, config, exchPart, sourceFrag,
					destFrag, destSiteID, trayName);
		}

	}


	/**
	 * TOS1: Add relay.
	 * @param plan
	 * @param qos
	 * @param tossimFlag
	 * @param currentSite
	 * @param currentSiteID
	 * @param config
	 * @param exchComp
	 * @param sourceFrag
	 * @param destFrag
	 * @param destSiteID
	 * @param trayName
	 * @throws CodeGenerationException
	 */
	private static void addRelay(final QueryPlan plan, final QoSSpec qos,
			int tosVersion, boolean tossimFlag, final Site currentSite,
			final String currentSiteID, final NesCConfiguration config,
			final ExchangePart exchPart, final Fragment sourceFrag,
			final Fragment destFrag, final String destSiteID,
			final String trayName) throws CodeGenerationException {
		
		if (tosVersion==1) {
			t1AddRXComponent(plan, qos, currentSite, currentSiteID,
				config, exchPart, sourceFrag, destFrag,
				destSiteID, trayName, tossimFlag);
			t1AddTXComponent(plan, qos, currentSiteID, config,
				exchPart, sourceFrag, destFrag, destSiteID,
				trayName, tossimFlag);
		} else {
			t2AddRXComponent(plan, qos, tossimFlag, config,
					exchPart, sourceFrag, destFrag, destSiteID, trayName);
			t2AddTXComponent(plan, qos, tossimFlag, config,
					exchPart, sourceFrag, destFrag, destSiteID, trayName);
		}
	}


	/**
	 * TOS1: Connect two local fragments via a tray.
	 * @param tosVersion
	 * @param currentSite
	 * @param config
	 * @param sourceFrag
	 * @param destFrag
	 * @param trayName
	 * @throws CodeGenerationException
	 */
	private static void connectLocalFragments(int tosVersion,
			final Site currentSite, final NesCConfiguration config,
			final Fragment sourceFrag, final Fragment destFrag,
			final String trayName) throws CodeGenerationException {
		final String destFragCompName = FragmentComponent
			.generateName(destFrag, currentSite, tosVersion);
		config.addWiring(
				destFragCompName,
				trayName,
				CodeGenUtils
					.generateGetTuplesInterfaceInstanceName(sourceFrag),
				CodeGenUtils
					.generateGetTuplesInterfaceInstanceName(sourceFrag),
				INTERFACE_GET_TUPLES);

		final String sourceFragCompName = FragmentComponent
			.generateName(sourceFrag, currentSite, tosVersion);
		config.addWiring(
				sourceFragCompName,
				trayName,
				CodeGenUtils
					.generatePutTuplesInterfaceInstanceName(sourceFrag),
				CodeGenUtils
					.generatePutTuplesInterfaceInstanceName(sourceFrag),
				INTERFACE_PUT_TUPLES);
	}


	/**
	 * TOS1: Creates a tray to buffer tuples for an exachange part
	 * (producer, consumer or relay).
	 * @param plan
	 * @param qos
	 * @param tosVersion
	 * @param tossimFlag
	 * @param currentSite
	 * @param config
	 * @param sourceFrag
	 * @param destFrag
	 * @param destSiteID
	 * @return
	 * @throws CodeGenerationException
	 */
	private static String addTrayComponent(final QueryPlan plan, final QoSSpec qos,
			int tosVersion, boolean tossimFlag, final Site currentSite,
			final NesCConfiguration config, final Fragment sourceFrag,
			final Fragment destFrag, final String destSiteID)
			throws CodeGenerationException {
		TrayComponent trayComp = new TrayComponent(sourceFrag,
			destFrag, destSiteID, currentSite, config, plan, qos,
			tosVersion, tossimFlag);
		trayComp = (TrayComponent) config.addComponent(trayComp);
		// tray may already exist
		final String trayName = trayComp.getID();
		if (Settings.NESC_LED_DEBUG) {
			config.addWiring(trayName, COMPONENT_LEDS, INTERFACE_LEDS,
				INTERFACE_LEDS, INTERFACE_LEDS);
		}
		return trayName;
	}


    /**
     * TOS1: Adds a tray-TX component wiring to a configuration.  The TX
     * component is added if not already present.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param currentSiteID The id of the site for which code is being generated.
     * @param config The nesC configuration which components/wirings are being added to.
     * @param exchComp The corresponding exchange producer.
     * @param sourceFrag The source Fragment (i.e., tuple type) of tuples to be transmitted.
     * @param destFrag The destination Fragment of tuples to be transmitted.
     * @param destSiteID The destination site of tuples to be transmitted.
     * @param trayName The name of the tray from which tuples are to be read from.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @throws CodeGenerationException
     */
    private static void t1AddTXComponent(final QueryPlan plan,
    	    final QoSSpec qos, final String currentSiteID,
    	    final NesCConfiguration config, final ExchangePart exchComp,
    	    final Fragment sourceFrag, final Fragment destFrag,
    	    final String destSiteID, final String trayName,
    	    final boolean tossimFlag)
    	    throws CodeGenerationException {

    	TXT1Component txComp = new TXT1Component(exchComp.getCurrentSite(),
    		exchComp.getNext().getCurrentSite(), config, plan, qos, tossimFlag);
    	txComp = (TXT1Component) config.addComponent(txComp);
    	txComp.addExchangeComponent(exchComp);
    	final String txCompName = txComp.getID();

    	config.addWiring(txCompName, trayName, CodeGenUtils
    		.generateGetTuplesInterfaceInstanceName(sourceFrag),
    		CodeGenUtils.generateGetTuplesInterfaceInstanceName(sourceFrag,
    			destFrag, destSiteID, currentSiteID),
    		INTERFACE_GET_TUPLES);
    	config.addWiring(txCompName, COMPONENT_RADIO, INTERFACE_SEND,
    		CodeGenUtils.generateUserSendInterfaceName(sourceFrag,
    			destFrag, destSiteID), CodeGenUtils
    			.generateProviderSendInterfaceName(sourceFrag,
    				destFrag, destSiteID, currentSiteID));

    	// Original Timer code
    	config.addWiring(COMPONENT_QUERY_PLAN, txCompName, INTERFACE_DO_TASK,
    		INTERFACE_DO_TASK + txCompName, INTERFACE_DO_TASK);

    	if (Settings.NESC_LED_DEBUG) {
    	    config.addWiring(txCompName, COMPONENT_LEDS, INTERFACE_LEDS,
    		    "Leds", INTERFACE_LEDS);
    	}
    }


    /**
     * TOS1: Adds a RX-tray component wiring to a configuration.  The RX
     * component is added if not already present.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param currentSite The site for which code is being generated.
     * @param currentSiteID The site id for which code is being generated.
     * @param config The nesC configuration which components/wirings are being added to.
     * @param exchComp The corresponding exchange consumer.
     * @param sourceFrag The source Fragment (i.e., tuple type) of tuples to be transmitted.
     * @param destFrag The destination Fragment of tuples to be transmitted.
     * @param destSiteID The destination site of tuples to be transmitted.
     * @param trayName The name of the tray from which tuples are to be written to.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @throws CodeGenerationException
     */
    private static void t1AddRXComponent(final QueryPlan plan,
    	    final QoSSpec qos, final Site currentSite,
    	    final String currentSiteID, final NesCConfiguration config,
    	    final ExchangePart exchComp, final Fragment sourceFrag,
    	    final Fragment destFrag, final String destSiteID,
    	    final String trayName,
    	    final boolean tossimFlag) throws CodeGenerationException {
    	RXT1Component rxComp = new RXT1Component(exchComp.getPrevious()
    		.getCurrentSite(), currentSite, config, plan, qos, tossimFlag);
    	rxComp = (RXT1Component) config.addComponent(rxComp); // may already
    	// exist
    	rxComp.addExchangeComponent(exchComp);
    	final String rxCompName = rxComp.getID();

    	config
    		.addWiring(
    			rxCompName,
    			trayName,
    			CodeGenUtils
    				.generatePutTuplesInterfaceInstanceName(sourceFrag),
    			CodeGenUtils
    				.generatePutTuplesInterfaceInstanceName(sourceFrag),
    			INTERFACE_PUT_TUPLES);
    	config.addWiring(rxCompName, COMPONENT_RADIO, INTERFACE_RECEIVE,
    		CodeGenUtils.generateUserReceiveInterfaceName(sourceFrag,
    			destFrag, destSiteID), CodeGenUtils
    			.generateProviderReceiveInterfaceName(sourceFrag,
    				destFrag, destSiteID, exchComp.getPrevious()
    					.getCurrentSiteID()));
    	config.addWiring(COMPONENT_QUERY_PLAN, rxCompName, INTERFACE_DO_TASK,
    		INTERFACE_DO_TASK + rxCompName, INTERFACE_DO_TASK);

    	if (Settings.NESC_LED_DEBUG) {
    	    config.addWiring(rxCompName, COMPONENT_LEDS, INTERFACE_LEDS,
    		    "Leds", INTERFACE_LEDS);
    		}
        }


    /**
     * TOS2: Generates the top-level configurations for each site in the sensor
     * network.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @return
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static HashMap<Site, NesCConfiguration> t2GenerateSiteConfigs(
	    final QueryPlan plan,
	    final QoSSpec qos,
	    int sink,
	    int tosVersion,
	    boolean tossimFlag)
	    throws IOException, CodeGenerationException {

		final HashMap<Site, NesCConfiguration> nodeConfigs = new HashMap<Site, NesCConfiguration>();

		final Iterator<Site> siteIter = plan.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
			final Site currentSite = siteIter.next();
			final String currentSiteID = currentSite.getID();

			/* Instantiate the top-level site configuration */
			final NesCConfiguration config = new NesCConfiguration(
				plan.getName() + COMPONENT_QUERY_PLANC + currentSiteID, //$
				plan,
				currentSite,
				tosVersion,
				tossimFlag);

			/* Add the components which are always needed */
			t2AddMainSiteComponents(plan, qos, sink, tosVersion, tossimFlag,
					config);

			/* Optional components for Led debugging not implemented yet for TOS2 */

			/* Power Management components not implemented yet for TOS2*/

			/* Functionality for changing radio transmit power not implemented yet for TOS2*/

			/* Add the fragments which have been placed onto this site */
			t2AddSiteFragments(tosVersion, tossimFlag, currentSite, config);

			/* Wire the fragments with trays, radio receive and transmit */
			wireSiteFragments(plan, qos, tosVersion, tossimFlag, currentSite, 
					currentSiteID, config);

			nodeConfigs.put(currentSite, config);

		}
		return nodeConfigs;
    }


    /**
     * TOS2: Add the components which are always needed.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @param config The nesC configuration which components/wirings are being added to.
     * @throws CodeGenerationException
     */
	private static void t2AddMainSiteComponents(final QueryPlan plan,
			final QoSSpec qos, int sink, int tosVersion, boolean tossimFlag,
			final NesCConfiguration config) throws CodeGenerationException {
		MainComponent mainComp = new MainComponent(COMPONENT_MAIN, config,
			tosVersion, tossimFlag);
		config.addComponent(mainComp);

		QueryPlanModuleComponent queryPlanModuleComp =
			new QueryPlanModuleComponent(COMPONENT_QUERY_PLAN, config,
			plan, qos, sink, tosVersion, tossimFlag);
		config.addComponent(queryPlanModuleComp);

		TimerT2Component timerComp = new TimerT2Component(		//$
			COMPONENT_AGENDA_TIMER, config, tossimFlag);
		config.addComponent(timerComp);

		//Entry point to the query plan code
		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_MAIN,
			INTERFACE_BOOT, INTERFACE_BOOT, INTERFACE_BOOT);

		//Used to trigger agenda tasks
		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_AGENDA_TIMER,
			INTERFACE_TIMER, TYPE_TMILLI, COMPONENT_AGENDA_TIMER,
			INTERFACE_TIMER);
	}


    /**
     * TOS2: Add the fragments which have been placed onto this site.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @param currentSite The site for which code is being generated.
     * @param config The nesC configuration which components/wirings are being added to.
     * @throws CodeGenerationException
     */
	private static void t2AddSiteFragments(int tosVersion, boolean tossimFlag,
			final Site currentSite, final NesCConfiguration config)
			throws CodeGenerationException {
		final Iterator<Fragment> fragIter = currentSite.getFragments()
			.iterator();
		while (fragIter.hasNext()) {
			final Fragment frag = fragIter.next();

			/* Add component for current fragment */
			final FragmentComponent fragComp = new FragmentComponent(
				frag, config, tosVersion, tossimFlag);
			config.addComponent(fragComp);

			/* Wire fragment to main query plan component */
			config.addWiring(COMPONENT_QUERY_PLAN, fragComp.getID(),
				INTERFACE_DO_TASK, CodeGenUtils
					.generateUserAsDoTaskName(frag, currentSite),
				INTERFACE_DO_TASK);

			/* Wire fragment to hardware devices as required */
			t2WireFragToDevices(tossimFlag, currentSite, config, frag, fragComp);
		}
	}


	/**
	 * TOS2: Wire fragment to hardware devices as required
	 * @param tossimFlag
	 * @param currentSite
	 * @param config
	 * @param frag
	 * @param fragComp
	 * @throws CodeGenerationException
	 */
	private static void t2WireFragToDevices(boolean tossimFlag,
			final Site currentSite, final NesCConfiguration config,
			final Fragment frag, final FragmentComponent fragComp)
			throws CodeGenerationException {
		final Iterator<Operator> opIter = frag
			.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
			final Operator op = opIter.next();
			if (op instanceof AcquireOperator) {

				/* Wire fragment component to the sensor component */
				t2wireFragToSensors(tossimFlag, currentSite, config,
						fragComp, op);
			}
		}
	}


	/**
	 * TOS2: Wire fragment component to the sensor component.
	 * @param tossimFlag
	 * @param currentSite
	 * @param config
	 * @param fragComp
	 * @param op
	 * @throws CodeGenerationException
	 */
	private static void t2wireFragToSensors(boolean tossimFlag,
			final Site currentSite, final NesCConfiguration config,
			final FragmentComponent fragComp, final Operator op)
			throws CodeGenerationException {
		final int numSensedAttr
		= ((AcquireOperator) op).getNumSensedAttributes();
		for (int i = 0; i < numSensedAttr; i++) {
			//TODO: look up sensorID in metadata
			String sensorId = new Integer(i).toString();
			final SensorT2Component sensorComp = new SensorT2Component(
					currentSite, sensorId, COMPONENT_SENSOR, config, "",
					tossimFlag);
			config.addComponent(sensorComp);
			final String sensorName = sensorComp.getID();

			config.addWiring(fragComp.getID(), sensorName,
					INTERFACE_READ, TYPE_READ,
					"Op" + op.getID() + INTERFACE_READ + i, INTERFACE_READ);
		}
	}


	private static void t2AddTXComponent(final QueryPlan plan,
			final QoSSpec qos, boolean tossimFlag,
			final NesCConfiguration config, final ExchangePart exchPart,
			final Fragment sourceFrag, final Fragment destFrag,
			final String destSiteID, final String trayName)
			throws CodeGenerationException {
		
		final String txActiveMessageIDKey =
			ActiveMessageIDGenerator.getActiveMessageIDKey(
					sourceFrag.getID(),
					destFrag.getID(),
					destSiteID,
					exchPart.getCurrentSite().getID());

		final TXT2Component txComp = new TXT2Component(
				sourceFrag, destFrag,
				exchPart.getDestSite(),
				exchPart.getNext().getCurrentSite(),
				config, plan, qos, tossimFlag);

		config.addComponent(txComp);
		final String txCompName = txComp.getID();

		final AMSendComponent radioTxComp = new AMSendComponent(
			exchPart.getCurrentSite(), sourceFrag, exchPart
				.getNext().getCurrentSite(), destFrag,
			COMPONENT_RADIOTX, config, txActiveMessageIDKey,
			tossimFlag);
		config.addComponent(radioTxComp);

		config
			.addWiring(
				txCompName,
				trayName,
				CodeGenUtils
					.generateGetTuplesInterfaceInstanceName(sourceFrag),
				INTERFACE_GET_TUPLES,
				INTERFACE_GET_TUPLES);
		config.addWiring(txCompName, radioTxComp.getID(),
			INTERFACE_SEND, INTERFACE_SEND, INTERFACE_SEND);

		config.addWiring(txCompName, radioTxComp.getID(),
			INTERFACE_PACKET, INTERFACE_PACKET,
			INTERFACE_PACKET);

		config.addWiring(COMPONENT_QUERY_PLAN, txCompName,
			INTERFACE_DO_TASK, CodeGenUtils
				.generateUserAsDoTaskName("tx",
					exchPart.getCurrentSite()
						.getID(), sourceFrag,
					exchPart.getNext()
						.getCurrentSite()
						.getID(), destFrag),
			INTERFACE_DO_TASK);
	}

	
	private static void t2AddRXComponent(final QueryPlan plan,
			final QoSSpec qos, boolean tossimFlag,
			final NesCConfiguration config, final ExchangePart exchPart,
			final Fragment sourceFrag, final Fragment destFrag,
			final String destSiteID, final String trayName)
			throws CodeGenerationException {

		final RXT2Component rxComp = new RXT2Component(
			sourceFrag,
			destFrag,
			exchPart.getDestSite(),
			exchPart.getPrevious().getCurrentSite(),
			config,
			plan,
			qos,
			tossimFlag);
		config.addComponent(rxComp);
		final String rxCompName = rxComp.getID();

		final String rxActiveMessageIDKey =
			ActiveMessageIDGenerator.getActiveMessageIDKey(
				sourceFrag.getID(),
				destFrag.getID(),
				destSiteID,
				exchPart.getPrevious().getCurrentSite().getID());
		// exchComp.getPrevious().getCurrentNode().getID(),
		// exchComp.getCurrentNode().getID());

		final AMRecieveComponent radioRxComp = new AMRecieveComponent(
			sourceFrag,
			exchPart.getDestSite(),
			destFrag,
			exchPart.getPrevious().getCurrentSite(),
			COMPONENT_RADIORX,
			config,
			rxActiveMessageIDKey,
			tossimFlag);
		config.addComponent(radioRxComp);

		config.addWiring(rxCompName,
			trayName,
			CodeGenUtils
				.generatePutTuplesInterfaceInstanceName(sourceFrag),
			INTERFACE_PUT_TUPLES,
			INTERFACE_PUT_TUPLES);
		config.addWiring(rxCompName, radioRxComp.getID(),
			INTERFACE_RECEIVE, INTERFACE_RECEIVE,
			INTERFACE_RECEIVE);

		config.addWiring(COMPONENT_QUERY_PLAN, rxCompName,
			INTERFACE_DO_TASK, CodeGenUtils
				.generateUserAsDoTaskName("rx",
					exchPart.getPrevious()
						.getCurrentSite()
						.getID(), sourceFrag,
					exchPart.getCurrentSite()
						.getID(), destFrag),
			INTERFACE_DO_TASK);

		config.addWiring(COMPONENT_QUERY_PLAN, COMPONENT_RADIO,
			INTERFACE_SPLITCONTROL, "CommControl",
			"SplitControl");
	}


    /**
     * Generate the configuration (i.e., wiring) with components for all
     * sites, used by Tossim.  This done by merging all the site
     * configurations into a single configuration.  This is necessary because
     * Tossim has the limitation that every site executes the same
     * query plan.
     *
     * @param plan The query plan which code is being generated for.
     * @param siteConfigs The collection of individual site configurations.
     * @return
     * @throws IOException
     */
    public static NesCConfiguration generateTossimConfiguration(
	    final QueryPlan plan,
	    final HashMap<Site, NesCConfiguration> siteConfigs,
	    final int tosVersion,
	    final boolean tossimFlag)
	    throws IOException {

		final NesCConfiguration tossimConfiguration = new NesCConfiguration(
			COMPONENT_QUERY_PLANC, plan, tosVersion, tossimFlag);

		final Iterator<Site> siteIter = siteConfigs.keySet().iterator();
		while (siteIter.hasNext()) {
		    final Site currentSite = siteIter.next();
		    final NesCConfiguration configuration = siteConfigs
			    .get(currentSite);
		    tossimConfiguration.mergeGraphs(configuration);
		}

		return tossimConfiguration;
    }


    /**
     * Factory method to instantiate an operator component
     * @param op The operator for which a component is being instantiated.
     * @param site The site that the operator instance has been placed on.
     * @param frag The fragment which the operator belongs to.
     * @param plan
     * @param qos
     * @param config
     * @param tosVersion
     * @param tossimFlag
     * @return
     * @throws CodeGenerationException
     */
    private static NesCComponent instantiateOperatorNesCComponent(
	    final Operator op, final Site site, final Fragment frag,
	    final QueryPlan plan, final QoSSpec qos,
	    final NesCConfiguration config,
	    int tosVersion, boolean tossimFlag) throws CodeGenerationException {

		if (op instanceof AcquireOperator) {
		    return new AcquireComponent((AcquireOperator) op, plan, qos,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof AggrEvalOperator) {
		    return new AggrEvalComponent((AggrEvalOperator) op, plan, qos,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof AggrInitializeOperator) {
		    return new AggrInitComponent((AggrInitializeOperator) op, plan,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof AggrIterateOperator) {
		    return new AggrMergeComponent((AggrIterateOperator) op, plan, qos,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof DeliverOperator) {
		    return new DeliverComponent((DeliverOperator) op, plan, qos,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof ExchangeOperator) {
		    return new ExchangeProducerComponent((ExchangeOperator) op, plan,
			    qos, config, tosVersion, tossimFlag);
		} else if (op instanceof JoinOperator) {
		    return new JoinComponent((JoinOperator) op, plan, qos, config,
		    		tosVersion, tossimFlag);
		} else if (op instanceof ProjectOperator) {
		    return new ProjectComponent((ProjectOperator) op, plan, qos,
		    		config, tosVersion, tossimFlag);
		} else if (op instanceof SelectOperator) {
		    return new SelectComponent((SelectOperator) op, plan, qos, config,
		    		tosVersion, tossimFlag);
		} else if (op instanceof WindowOperator) {
		    return new WindowComponent((WindowOperator) op, plan, qos, config,
		    		tosVersion, tossimFlag);
		} else {
		    throw new CodeGenerationException(
			    "No NesC Component found for operator type="
				    + op.getClass().toString() + ", id=" + op.getID());
		}
    }


    /**
     * Generate operator configurations for each instance of each fragment.
     * @param plan
     * @param nodeConfigs
     * @param qos
     * @param tosVersion
     * @param tossimFlag
     * @return
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static HashMap<String, NesCConfiguration> generateFragmentConfigurations(
	    final QueryPlan plan,
	    final HashMap<Site, NesCConfiguration> nodeConfigs,
	    final QoSSpec qos,
	    final int tosVersion,
	    final boolean tossimFlag) throws IOException, CodeGenerationException {

		// For each fragment on each node, generate finer-grained configuration
		// (i.e., operator-level)

		final HashMap<String, NesCConfiguration> fragConfigs = new HashMap<String, NesCConfiguration>();

		// for each site in the sensor network
		final Iterator<Site> siteIter = plan.siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
		    final Site site = siteIter.next();

		    // for each fragment on the node
		    final Iterator<Fragment> fragIter = site.getFragments().iterator();
		    while (fragIter.hasNext()) {
				final Fragment frag = fragIter.next();

				final String fragName = FragmentComponent.generateName(frag,
					site, tosVersion);
				final FragmentComponent fragComp = (FragmentComponent)
					nodeConfigs.get(site).getComponent(fragName);
				final NesCConfiguration fragConfig = new NesCConfiguration(
					fragName, plan, site, tosVersion, tossimFlag);
				fragComp.setInnerConfig(fragConfig);

				generateIntraFragmentConfig(plan, qos, tosVersion, tossimFlag,
						site, frag, fragConfig);
				fragConfigs.put(fragName, fragConfig);
		    }//while (fragIter.hasNext()) {

		}//	while (siteIter.hasNext()) {


		return fragConfigs;
    }


    /**
     * Generates operator configuration for a fragment instance.
     * @param plan
     * @param qos
     * @param tosVersion
     * @param tossimFlag
     * @param site
     * @param frag
     * @param fragConfig
     * @throws CodeGenerationException
     */
	private static void generateIntraFragmentConfig(final QueryPlan plan,
			final QoSSpec qos, final int tosVersion, final boolean tossimFlag,
			final Site site, final Fragment frag,
			final NesCConfiguration fragConfig) throws CodeGenerationException {

		/* Wire the operators inside the fragment to each other */
		addAndWireOperators(plan, qos, tosVersion, tossimFlag, site, frag,
				fragConfig);

		/* add the producer and link to outside world*/
		final Operator rootOp = frag.getRootOperator();
		final String fragID = frag.getID();
		final String rootOpName = CodeGenUtils
			.generateOperatorInstanceName(rootOp, frag, site, tosVersion);
		final ExchangeOperator producerOp = (ExchangeOperator) frag
			.getParentExchangeOperator();
		if (producerOp != null) {
		    addExternalProducerWiring(plan, qos, tosVersion, tossimFlag, site, frag,
					fragConfig, rootOp, fragID, rootOpName, producerOp);

		} else {
			/* Wire the deliver operator to the outside world */
		    addExternalDeliverWiring(fragConfig, rootOpName);
		}

		/* Wire the acquisition operators to the outside world */
		addExternalSensorWiring(tosVersion, site, frag, fragConfig);
	}


	/**
	 * Creates the operators in the fragment except the producer and
	 * wires them to each other.
	 * @param plan
	 * @param qos
	 * @param tosVersion
	 * @param tossimFlag
	 * @param site
	 * @param frag
	 * @param fragConfig
	 * @throws CodeGenerationException
	 */
	private static void addAndWireOperators(final QueryPlan plan,
			final QoSSpec qos, final int tosVersion, final boolean tossimFlag,
			final Site site, final Fragment frag,
			final NesCConfiguration fragConfig) throws CodeGenerationException {
		// for each operator in the fragment
		Iterator<Operator> opIter = frag
			.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
		    final NesCComponent opComp = instantiateOperatorNesCComponent(
			    op, site, frag, plan, qos, fragConfig, tosVersion, tossimFlag);
		    final String opName = opComp.getID();
		    fragConfig.addComponent(opComp);
		    opComp.setDescription(op.getText(false));

		    // link to each child of that operator
		    final Iterator<Operator> childOpIter = op
			    .childOperatorIterator();
		    int childCount = 0;
		    while (childOpIter.hasNext()) {
				final Operator childOp = childOpIter.next();
				final String childOpName = CodeGenUtils
					.generateOperatorInstanceName(childOp, frag,
						site, tosVersion);
				final String requestDataInterfaceType = CodeGenUtils
					.generateGetTuplesInterfaceInstanceName(childOp);

				if (childOp instanceof ExchangeOperator) {
				    addExternalTrayWiring(fragConfig, op, opName, childCount,
							childOp, requestDataInterfaceType);
				} else {
				    addIntraFragWiring(fragConfig, op, opName, childCount,
							childOpName, requestDataInterfaceType);
				}

				childCount++;
		    }
		}
	}


	/**
	 * Add wiring between two operators.
	 * @param fragConfig
	 * @param op
	 * @param opName
	 * @param childCount
	 * @param childOpName
	 * @param requestDataInterfaceType
	 * @throws CodeGenerationException
	 */
	private static void addIntraFragWiring(
			final NesCConfiguration fragConfig, final Operator op,
			final String opName, int childCount, final String childOpName,
			final String requestDataInterfaceType)
			throws CodeGenerationException {
		if (op.getInDegree() == 1) {
			fragConfig.addWiring(opName, childOpName,
				requestDataInterfaceType, "Child",
				"Parent");
		} else if ((op.getInDegree() == 2)
		    && (childCount == 0)) {
			fragConfig.addWiring(opName, childOpName,
				requestDataInterfaceType, "LeftChild",
				"Parent");
		} else if ((op.getInDegree() == 2)
		    && (childCount == 1)) {
			fragConfig.addWiring(opName, childOpName,
				requestDataInterfaceType, "RightChild",
				"Parent");
		}
	}


	/**
	 * Add external wiring between an operator which receive tuples from
	 * operators in other fragments and a tray.
	 * @param fragConfig
	 * @param op
	 * @param opName
	 * @param childCount
	 * @param childOp
	 * @param requestDataInterfaceType
	 * @throws CodeGenerationException
	 */
	private static void addExternalTrayWiring(
			final NesCConfiguration fragConfig, final Operator op,
			final String opName, int childCount, final Operator childOp,
			final String requestDataInterfaceType)
			throws CodeGenerationException {
		// this is a "leaf" operator of the fragment, so
		// needs to read data from input tray(s)
		// String trayGetName =
		// INTERFACE_TRAY_GET+"Frag"+((LogicalExchangeOperator)childOp).getChildFragment().getFragID();
		final String trayGetName = CodeGenUtils
		    .generateGetTuplesInterfaceInstanceName(((ExchangeOperator) childOp)
			    .getSourceFragment());

		if (op.getInDegree() == 1) {
			fragConfig.linkToExternalProvider(opName,
				requestDataInterfaceType, "Child",
				trayGetName);
		    } else if ((op.getInDegree() == 2)
			    && (childCount == 0)) {
			fragConfig.linkToExternalProvider(opName,
				requestDataInterfaceType, "LeftChild",
				trayGetName);
		    } else if ((op.getInDegree() == 2)
			    && (childCount == 1)) {
			fragConfig.linkToExternalProvider(opName,
				requestDataInterfaceType, "RightChild",
				trayGetName);
		 }
	}


	/**
	 * Add external wiring between producer operator component and tray
	 * component that it forwards tuples to.
	 * @param plan
	 * @param qos
	 * @param tosVersion
	 * @param tossimFlag
	 * @param site
	 * @param frag
	 * @param fragConfig
	 * @param rootOp
	 * @param fragID
	 * @param rootOpName
	 * @param producerOp
	 * @throws CodeGenerationException
	 */
	private static void addExternalProducerWiring(final QueryPlan plan,
			final QoSSpec qos, final int tosVersion, final boolean tossimFlag,
			final Site site, final Fragment frag,
			final NesCConfiguration fragConfig, final Operator rootOp,
			final String fragID, final String rootOpName,
			final ExchangeOperator producerOp) throws CodeGenerationException {
		final ExchangeProducerComponent producerComp = new ExchangeProducerComponent(
		    producerOp, plan, qos, fragConfig, tosVersion, tossimFlag);
		fragConfig.addComponent(producerComp);
		final String producerOpID = producerComp.getID();
		fragConfig.addWiring(producerOpID, rootOpName, CodeGenUtils
		    .generateGetTuplesInterfaceInstanceName(rootOp),
		    "Child", "Parent");
		if (Settings.NESC_LED_DEBUG) {
			fragConfig.linkToExternalProvider(producerOpID,
					INTERFACE_LEDS, INTERFACE_LEDS, INTERFACE_LEDS);
		}
		fragConfig.linkToExternalUser(producerOpID,
		    INTERFACE_DO_TASK, INTERFACE_DO_TASK,
		    INTERFACE_DO_TASK);
		producerComp.setDescription(producerOp.getText());

		final Iterator<ExchangePart> exchCompIter = site
		    .getExchangeComponents().iterator();
		while (exchCompIter.hasNext()) {
			final ExchangePart exchComp = exchCompIter.next();

			if (exchComp.getSourceFragID().equals(fragID)
				&& (exchComp.getComponentType() == ExchangePartType.PRODUCER)) {

			    fragConfig.linkToExternalProvider(
			    	producerOpID,
					CodeGenUtils
					.generatePutTuplesInterfaceInstanceName(frag),
					INTERFACE_PUT_TUPLES,
					CodeGenUtils
					.generatePutTuplesInterfaceInstanceName(frag));
			}
		}
	}


	/**
	 * Add external wiring between deliver operator component and component
	 * which receives messages for the serial port (radio in tinyOS 1)
	 * @param fragConfig
	 * @param rootOpName
	 * @throws CodeGenerationException
	 */
	private static void addExternalDeliverWiring(
			final NesCConfiguration fragConfig, final String rootOpName)
			throws CodeGenerationException {
		// deliver operator
		fragConfig.linkToExternalUser(rootOpName,
		    INTERFACE_DO_TASK, INTERFACE_DO_TASK,
		    INTERFACE_DO_TASK);
		fragConfig.linkToExternalProvider(rootOpName, INTERFACE_SEND,
			"SendDeliver", "SendDeliver");
	}


	/**
	 * Add external wiring between acquire operator components and
	 * sensor components.
	 * @param tosVersion
	 * @param site
	 * @param frag
	 * @param fragConfig
	 * @throws CodeGenerationException
	 */
	private static void addExternalSensorWiring(final int tosVersion,
			final Site site, final Fragment frag,
			final NesCConfiguration fragConfig) throws CodeGenerationException {
		Iterator<Operator> opIter;
		// link any acquisition or scan operators to the outside world
		opIter = frag.operatorIterator(TraversalOrder.POST_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = opIter.next();
		    final String opName = CodeGenUtils
			    .generateOperatorInstanceName(op, frag, site, tosVersion);
			if (Settings.NESC_LED_DEBUG) {
				fragConfig.linkToExternalProvider(opName,
					INTERFACE_LEDS, INTERFACE_LEDS, INTERFACE_LEDS);
			}
		    if (op instanceof AcquireOperator) {
			    final int numSensedAttr
			    	= ((AcquireOperator) op).getNumSensedAttributes();
			    for (int i = 0; i < numSensedAttr; i++) {
			    	if (tosVersion == 1) {
						fragConfig.linkToExternalProvider(opName,
							INTERFACE_READ,
							INTERFACE_READ + i,
							"Op" + op.getID() + INTERFACE_READ + i);
			    	} else {
					    fragConfig.linkToExternalProvider(opName,
						    INTERFACE_READ, TYPE_READ, INTERFACE_READ + i,
						    "Op" + op.getID() + INTERFACE_READ + i);
			    	}
			    }
		    }
		}
	}


    /**
     * Creates and writes the nesC files with the code for the tossim
     * configuration, and the components within each configuration recursively.
     * @param plan The query plan which code is being generated for.
     * @param tossimConfig The single tossim configuration.
     * @param nescOutputDir The nesC output directory for generated code.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static void instantiateTossimConfiguration(
    		final QueryPlan plan,
    		final NesCConfiguration tossimConfig,
    		final String nescOutputDir,
    		int tosVersion)
    		throws IOException, CodeGenerationException{

		String outputDir = nescOutputDir + "tossim"+tosVersion+"/";
	    tossimConfig.instantiateComponents(outputDir);
	    tossimConfig.display(outputDir, tossimConfig.getName());
    }


    /**
     * Creates and writes the nesC files with the code for each site
     * configuration, and the components within each configuration recursively.
     * @param plan The query plan which code is being generated for.
     * @param siteConfigs The collection of individual site configurations.
     * @param nescOutputDir The nesC output directory for generated code.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @throws IOException
     * @throws CodeGenerationException
     */
    public static void instantiateSiteConfigurations(final QueryPlan plan,
	    final HashMap<Site, NesCConfiguration> siteConfigs,  final String nescOutputDir,
	    int tosVersion)
    	throws IOException, CodeGenerationException {

	    final Iterator<Site> siteIter = plan
		    .siteIterator(TraversalOrder.POST_ORDER);
	    while (siteIter.hasNext()) {
			final Site currentSite = siteIter.next();
			final NesCConfiguration siteConfig = siteConfigs
				.get(currentSite);
			String outputDir = nescOutputDir + "avrora"+tosVersion+"/mote" + currentSite.getID() + "/";
			siteConfig.instantiateComponents(outputDir);
		    siteConfig.display(outputDir, siteConfig.getName());
	    }
	}

    /**
     * TOS1/TOS2: Generates the QueryPlan.h header file.
     * @param plan The query plan which code is being generated for.
     * @param configs The collection of nesC configuration for each site.
     * @param nescOutputDir The nesC output directory for generated code.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @throws IOException
     */
    public static void generateHeaderFile(final QueryPlan plan,
	    final HashMap<Site, NesCConfiguration> configs,
	    final String nescOutputDir, int tosVersion, boolean tossimFlag) throws IOException {

		final StringBuffer tupleTypeBuff = new StringBuffer();
		final StringBuffer messageTypeBuff = new StringBuffer();
		final StringBuffer activeIDDeclsBuff = new StringBuffer();

		addAMConstants(activeIDDeclsBuff);

		final Iterator<Operator> opIter = plan
			.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = (Operator) opIter.next();

		    /* Define the structures which define each operator or fragment
		     * output type. */
		    addOperatorTupleStructs(tupleTypeBuff, op);

		    /*Define the structures which define each message output
		     * type. */
		    addMessageStructs(messageTypeBuff, op);
		}

		StringBuffer deliverMsgBuff = addDeliverMessageStruct();

		/* Generate Tossim header file */
		doGenerateTossimHeaderFile(nescOutputDir, tosVersion, tossimFlag,
				activeIDDeclsBuff, tupleTypeBuff, messageTypeBuff, deliverMsgBuff);

		/* Generate Avrora header files */
		doGenerateAvroraHeaderFiles(configs, nescOutputDir, tosVersion,
				activeIDDeclsBuff, tossimFlag, tupleTypeBuff, messageTypeBuff, deliverMsgBuff);
    }


    /**
     * Generates the declarations for the ActiveMessageIDs used by the query plan
     * for the header file.
     * @param activeIDDeclsBuff  Buffer to store the AM ID declarations
     */
	private static void addAMConstants(final StringBuffer activeIDDeclsBuff) {
		Boolean first = true;

		if (!ActiveMessageIDGenerator.isEmpty()) {
		    activeIDDeclsBuff.append("enum" + "\n");
		    activeIDDeclsBuff.append("{ " + "\n");
		    final Iterator<String> amIter = ActiveMessageIDGenerator
			    .activeMessageIDKeyIterator();

		    while (amIter.hasNext()) {
				final String id = amIter.next();
					if (first) {
					    activeIDDeclsBuff.append("\t" + id + " = "
						    + ActiveMessageIDGenerator.getactiveMessageID(id));
					    first = false;
					} else {
					    activeIDDeclsBuff.append(",\n \t" + id + " = "
						    + ActiveMessageIDGenerator.getactiveMessageID(id)
						    + "\n");
					}
			    }
		    activeIDDeclsBuff.append("\n}; " + "\n");
		}
	}


	/**
	 * Generates the operator tuple structs used by the query plan, for the header
	 * file.
	 * @param tupleTypeBuff Buffer to store all tuple structs in the DAF.
	 * @param op The operator for which to generate a struct.
	 */
	private static void addOperatorTupleStructs(
			final StringBuffer tupleTypeBuff, final Operator op) {

		if (!(op instanceof ExchangeOperator) && (!op.isRecursive())) {
			tupleTypeBuff.append("// Tuple output type for operator "
				+ op.getID() + "\n");
			tupleTypeBuff.append("// " + op.getText(false) + "\n");
			tupleTypeBuff.append("// size = " +
					CodeGenUtils.outputTypeSize.get(
					CodeGenUtils.generateOutputTupleType(op))+ " bytes\n\n");

			tupleTypeBuff.append("typedef struct "
				+ CodeGenUtils.generateOutputTupleType(op) + " {\n");
		}

		final ArrayList <Attribute> attributes = op.getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
		    String attrName = CodeGenUtils.getNescAttrName(attributes.get(i));

			final AttributeType attrType = attributes.get(i).getType();

			final String nesCType = attrType.getNesCName();

			if (!(op instanceof ExchangeOperator) && (!op.isRecursive())) {
				tupleTypeBuff.append("\t" + nesCType + " " + attrName + ";\n");
			}
		}

		if (!(op instanceof ExchangeOperator) && (!op.isRecursive())) {
			tupleTypeBuff.append("} "
					+ CodeGenUtils.generateOutputTupleType(op) + ";\n\n");
			tupleTypeBuff.append("typedef "
					+ CodeGenUtils.generateOutputTupleType(op) + "* "
					+ CodeGenUtils.generateOutputTuplePtrType(op)
					+ ";\n\n\n");
		}
	}


	/**
	 * Generates the message tuple structs used by the query plan, for the
	 * header file.
	 * @param messageTypeBuff Buffer to store all message structs.
	 * @param op The operator for which to generate a message struct
	 */
	private static void addMessageStructs(final StringBuffer messageTypeBuff,
			final Operator op) {
		if ((op instanceof ExchangeOperator)
		    && (!(op.getInput(0).getContainingFragment().isRecursive() && 
		    		op.getInput(0).getContainingFragment().getNumOps()==1))) {

			final String fragID = op.getInput(0).getFragID();

			final int tupleSize = CodeGenUtils.outputTypeSize.get(
				CodeGenUtils.generateOutputTupleType(op)).intValue();
			final int numTuplesPerMessage = ExchangePart
				.computeTuplesPerMessage(tupleSize);
			assert (numTuplesPerMessage > 0);

			messageTypeBuff.append("// Message output type for Fragment "
				+ fragID + " (operator " + op.getID() + ")\n");
			messageTypeBuff.append("typedef struct "
				+ CodeGenUtils.generateMessageType(op) + " {\n");
			messageTypeBuff.append("\tTupleFrag" + fragID + " tuples["
				+ numTuplesPerMessage + "];\n");
			messageTypeBuff.append("}"
				+ CodeGenUtils.generateMessageType(op) + ";\n\n");
			messageTypeBuff.append("typedef "
				+ CodeGenUtils.generateMessageType(op) + " *"
				+ CodeGenUtils.generateMessagePtrType(op) + ";\n\n\n");
		}
	}


	/**
	 * Generates the deliver message struct, for the header file.
	 * @return
	 */
	private static StringBuffer addDeliverMessageStruct() {
		StringBuffer deliverMsgBuff = new StringBuffer();
		deliverMsgBuff.append("#define DELIVER_PAYLOAD_SIZE 28\n\n");
		deliverMsgBuff.append("typedef struct DeliverMessage {\n");
		deliverMsgBuff.append("\tchar text[DELIVER_PAYLOAD_SIZE];\n");
		deliverMsgBuff.append("} DeliverMessage;\n\n");
		deliverMsgBuff.append("typedef DeliverMessage* DeliverMessagePtr;\n\n");
		return deliverMsgBuff;
	}


	/**
	 * Generates an Avrora header file for each site.
	 * @param configs The collection of nesC configuration for each site.
	 * @param nescOutputDir The nesC output directory for generated code.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param activeIDDeclsBuff Buffer which stores ActiveID constant decls
	 * @param tupleTypeBuff Buffer which stores all tuple structs in the DAF.
	 * @param messageTypeBuff Buffer which stores all message structs.
	 * @param deliverMsgBuff Buffer which stores the deliver message struct.
	 * @throws IOException
	 */
	private static void doGenerateAvroraHeaderFiles(
			final HashMap<Site, NesCConfiguration> configs,
			final String nescOutputDir, int tosVersion, 
			final StringBuffer activeIDDeclsBuff, boolean tossimFlag,
			final StringBuffer tupleTypeBuff, final StringBuffer messageTypeBuff, 
			StringBuffer deliverMsgBuff)
			throws IOException {
		if (!tossimFlag) {
		    final Iterator<Site> siteIter = configs.keySet().iterator();
		    while (siteIter.hasNext()) {
				final String siteID = siteIter.next().getID();
				final String fname = nescOutputDir + "avrora"+tosVersion+"/mote"
					+ siteID + "/QueryPlan.h";
				doGenerateHeaderFile(activeIDDeclsBuff, tupleTypeBuff, messageTypeBuff,
						deliverMsgBuff, fname, false);

				addMeasurementsConstant(nescOutputDir, tosVersion, siteID);
		    }
		}
	}


	/**
	 * Generates a single Tossim header file for all sites.
	 * @param nescOutputDir The nesC output directory for generated code.
	 * @param tosVersion The TinyOS version which nesC is being generated for.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @param activeIDDeclsBuff Buffer which stores ActiveID constant decls 
	 * @param tupleTypeBuff Buffer which stores all tuple structs in the DAF.
	 * @param messageTypeBuff Buffer which stores all message structs.
	 * @param deliverMsgBuff Buffer which stores the deliver message struct.
	 * @throws IOException
	 */
	private static void doGenerateTossimHeaderFile(final String nescOutputDir,
			int tosVersion, boolean tossimFlag,
			final StringBuffer activeIDDeclsBuff, final StringBuffer tupleTypeBuff,
			final StringBuffer messageTypeBuff, StringBuffer deliverMsgBuff)
			throws IOException {

		if (tossimFlag) {
		    final String fname = nescOutputDir
			    + "tossim"+tosVersion+"/QueryPlan.h";
			doGenerateHeaderFile(activeIDDeclsBuff, tupleTypeBuff, messageTypeBuff,
					deliverMsgBuff, fname, true);
		}
	}

	/**
	 * Adds separate header file use by cost-model measurement scripts.
	 */
	private static void addMeasurementsConstant(final String nescOutputDir,
			int tosVersion, final String siteID) throws IOException {
		if (Settings.MEASUREMENTS_ACTIVE_AGENDA_LOOPS >= 0) {
		    final String fname2 = nescOutputDir + "avrora"+tosVersion+"/mote"
			+ siteID + "/ActiveLoop.h";
		    final PrintWriter out2 = new PrintWriter(new BufferedWriter(
			    new FileWriter(fname2)));
		    out2.println("#define MAX_ACTIVE_LOOP "
		    	+Settings.MEASUREMENTS_ACTIVE_AGENDA_LOOPS+"\n");
		    out2.close();
		}
	}


	/**
	 * Generates header file at specific location.
	 * @param tupleTypeBuff Buffer which stores all tuple structs in the DAF.
	 * @param messageTypeBuff Buffer which stores all message structs.
	 * @param deliverMsgBuff Buffer which stores the deliver message struct.
	 * @param fname The path of the file to be written.
	 * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
	 * @throws IOException
	 */
	private static void doGenerateHeaderFile(
			final StringBuffer activeIDDeclsBuff, final StringBuffer tupleTypeBuff,
			final StringBuffer messageTypeBuff, StringBuffer deliverMsgBuff,
			final String fname, boolean tossimFlag) throws IOException {
		final PrintWriter out = new PrintWriter(new BufferedWriter(
			new FileWriter(fname)));

		out.println("#ifndef __QUERY_PLAN_H__\n");
		out.println("#define __QUERY_PLAN_H__\n");
		out.println("enum {NULL_EVAL_EPOCH = -1};\n");

		out.println(activeIDDeclsBuff.toString()+"\n");
		
		if (tossimFlag) {
		    out.println("uint32_t sysTime = 0;\n");
		    out.println("uint8_t synchronizing = TRUE;\n");
		}

		out.println(tupleTypeBuff);
		out.println(messageTypeBuff);
		out.println(deliverMsgBuff);

		out.println("\n#endif\n\n");
		out.close();
	}


	/**
	 * Generates the typed interface files for the query plan.
	 * @param plan
	 * @param configs
	 * @param nescOutputDir
	 * @param tosVersion
	 * @param tossimFlag
	 * @throws IOException
	 */
    public static void generateTypedInterfaces(final QueryPlan plan,
	    final HashMap<Site, NesCConfiguration> configs,
	    final String nescOutputDir, int tosVersion, boolean tossimFlag) throws IOException {

    	final Iterator<Operator> opIter = plan
		.operatorIterator(TraversalOrder.PRE_ORDER);
		while (opIter.hasNext()) {
		    final Operator op = (Operator) opIter.next();

		    if (op instanceof ExchangeOperator) {
		    	continue;
		    } else if (op.isFragmentRoot()) {

		    	/* Between fragments a GetTuples and PutTuples interface is needed */
		    	generateInterFragmentInterfaces(configs, nescOutputDir, tosVersion,
						tossimFlag, op);
		    } else {

		    	/* Within a fragment only a GetTuples interface is needed */
				generateIntraFragmentInterfaces(configs, nescOutputDir,
						tosVersion, tossimFlag, op);
		    }
		}
    }


    /**
	 * Generates typed interfaces within fragments, i.e., just a GetTuples
     * interface.
     * @param configs
     * @param nescOutputDir
     * @param tosVersion
     * @param tossimFlag
     * @param op
     * @throws IOException
     */
	private static void generateIntraFragmentInterfaces(
			final HashMap<Site, NesCConfiguration> configs,
			final String nescOutputDir, int tosVersion, boolean tossimFlag,
			final Operator op)
			throws IOException {

	    final HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("__INTERFACE_NAME__", CodeGenUtils
			.generateGetTuplesInterfaceInstanceName(op));
		replacements.put("__TUPLE_TYPE_PTR__", CodeGenUtils.generateOutputTuplePtrType(op)); 

		generateTypedInterface(INTERFACE_GET_TUPLES, configs, nescOutputDir,
				tosVersion, tossimFlag, op, replacements);
	}


	/**
     * Generates typed interfaces between fragments, i.e., a GetTuples and PutTuples
     * interface.
	 * @param configs
	 * @param nescOutputDir
	 * @param tosVersion
	 * @param tossimFlag
	 * @param op
	 * @throws IOException
	 */
	private static void generateInterFragmentInterfaces(
			final HashMap<Site, NesCConfiguration> configs,
			final String nescOutputDir, int tosVersion, boolean tossimFlag,
			final Operator op)
			throws IOException {

	    final HashMap<String, String> replacements = new HashMap<String, String>();

		// For roots of fragments, the tuple name is TupleFrag{fragId}
		// Need both requestData and TrayPut interfaces

		replacements.put("__PUT_INTERFACE_NAME__", CodeGenUtils
				.generatePutTuplesInterfaceInstanceName(op
						.getContainingFragment()));
		replacements.put("__INTERFACE_NAME__", CodeGenUtils
				.generateGetTuplesInterfaceInstanceName(op
						.getContainingFragment()));
		replacements.put("__TUPLE_TYPE_PTR__", "TupleFrag"
				+ op.getFragID() + "Ptr");
		replacements
			.put("__TUPLE_TYPE__", "TupleFrag" + op.getFragID());

		generateTypedInterface(INTERFACE_GET_TUPLES, configs, nescOutputDir,
				tosVersion, tossimFlag, op, replacements);
		generateTypedInterface(INTERFACE_PUT_TUPLES, configs, nescOutputDir,
				tosVersion, tossimFlag, op, replacements);
	}


	/**
	 * Generates a typed interface according to a given set of replacements.
	 * @param interfaceName
	 * @param configs
	 * @param nescOutputDir
	 * @param tosVersion
	 * @param tossimFlag
	 * @param op
	 * @param replacements
	 * @throws IOException
	 */
	private static void generateTypedInterface(
			String interfaceName,
			final HashMap<Site, NesCConfiguration> configs,
			final String nescOutputDir, int tosVersion, boolean tossimFlag,
			final Operator op, final HashMap<String, String> replacements)
			throws IOException {

		//INTERFACE_GET_TUPLES
		String interfaceInstanceName = CodeGenUtils.
			generateGetTuplesInterfaceInstanceName(op)+ ".nc";
		if (interfaceName==INTERFACE_PUT_TUPLES) {
			interfaceInstanceName = CodeGenUtils
				.generatePutTuplesInterfaceInstanceName(
				op.getContainingFragment())+".nc";
		}


		if (!tossimFlag) {
		    final Iterator<Site> siteIter = configs.keySet().iterator();
		    while (siteIter.hasNext()) {
				final Site site = siteIter.next();

				Template.instantiate(NESC_INTERFACES_DIR +
						"/" + interfaceName + ".nc",
						nescOutputDir + "avrora"+tosVersion+"/mote" +
						site.getID() + "/" +
						interfaceInstanceName, replacements);
		    }
		}
		if (tossimFlag) {
		    Template.instantiate(NESC_INTERFACES_DIR +
		    		"/" + interfaceName + ".nc",
				    nescOutputDir + "tossim"+tosVersion+"/" +
				    interfaceInstanceName, replacements);
		}
	}


    /**
     * Helper function for methods resonsible for generating miscellaneous files.
     * @param interfaceName
     * @param plan
     * @param nescOutputDir
     * @param nodeDir
     * @throws IOException
     */
    public static void copyInterfaceFile(final String interfaceName,
    	    final QueryPlan plan, final String nescOutputDir, final String nodeDir) throws IOException {
    	Template.instantiate(NESC_INTERFACES_DIR + "/"
    		+ interfaceName + ".nc", nescOutputDir + nodeDir
    		+ "/" + interfaceName + ".nc");
        }

    /**
     * Generates Miscellaneous files (make files, and other supporting files).
     * @param plan
     * @param nescOutputDir
     * @param qos
     * @param numNodes
     * @param tosVersion
     * @param tossimFlag
     * @throws IOException
     */
    public static void copyMiscFiles(final QueryPlan plan, final String nescOutputDir,
    		QoSSpec qos, int numNodes, int tosVersion, boolean tossimFlag) throws IOException {

		if (!tossimFlag) {
		    generateAvroraMiscFiles(plan, nescOutputDir, tosVersion);
		}

		if (tossimFlag) {
			generateTossimMiscFiles(plan, nescOutputDir, qos, numNodes, tosVersion);
		}
    }


    /**
     * Generates Miscellaneous files for Avrora.
     * @param plan
     * @param nescOutputDir
     * @param tosVersion
     * @throws IOException
     */
	private static void generateAvroraMiscFiles(final QueryPlan plan,
			final String nescOutputDir, int tosVersion) throws IOException {
		final Iterator<Site> siteIter = plan
		    .siteIterator(TraversalOrder.POST_ORDER);
		while (siteIter.hasNext()) {
		    final String siteID = siteIter.next().getID();
			final String nodeDir = "avrora"+tosVersion+"/mote" + siteID;
			copyInterfaceFile(INTERFACE_DO_TASK, plan, nescOutputDir, nodeDir);

			generateMakefiles(nescOutputDir + nodeDir,
				"QueryPlan" + siteID);

		    if (tosVersion==2) {
		    	Template.instantiate(
					    NESC_MISC_FILES_DIR + "/itoa.h",
					    nescOutputDir + nodeDir
						    + "/itoa.h");
		    }
		}
	}


	/**
	 * Generates Miscellaneous files for Tossim.
	 * @param plan The query plan which code is being generated for.
	 * @param nescOutputDir
	 * @param qos
	 * @param numNodes
	 * @param tosVersion
	 * @throws IOException
	 */
	private static void generateTossimMiscFiles(final QueryPlan plan,
		final String nescOutputDir, QoSSpec qos, int numNodes,
		int tosVersion) throws IOException {

		copyInterfaceFile(INTERFACE_DO_TASK, plan, nescOutputDir, "tossim"+tosVersion+"/");

	    generateMakefiles(nescOutputDir+ "/tossim"+tosVersion, "QueryPlan");

	    if (tosVersion==2) {
	    	Template.instantiate(
				    NESC_MISC_FILES_DIR + "/itoa.h",
				    nescOutputDir + "/tossim"+tosVersion+"/itoa.h");

		    HashMap<String, String> replacements = new HashMap<String, String>();
			long duration = qos.getQueryDuration();
			replacements.put("__SIMULATION_DURATION__", new Long(duration).toString());
			replacements.put("__NUM_NODES__", new Integer(numNodes).toString());
			Template.instantiate(
		    		NESC_MISC_FILES_DIR + "/runTossim.py",
		    		nescOutputDir + "/tossim"+tosVersion+"/runTossim.py", replacements);

			Template.instantiate(
				    NESC_MISC_FILES_DIR + "/meyer-light.txt",
				    nescOutputDir + "/tossim"+tosVersion+"/meyer-light.txt");
		}
	}


	/**
	 * Generates Makefile and MakeRules files in the given directory.
	 * @param dir The directory where the files are to be created.
	 * @param mainConfigName The name of main nesC configuration.
	 * @throws IOException
	 */
	private static void generateMakefiles(final String dir,
			String mainConfigName) throws IOException {
		// Makefile
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("__MAIN_CONFIG_NAME__", mainConfigName);
		Template.instantiate(NESC_MISC_FILES_DIR
			+ "/Makefile", dir
			+ "/Makefile", replacements);

		Template.instantiate(
		    NESC_MISC_FILES_DIR + "/Makerules",
		    dir + "/Makerules");
	}




    /**
     * Main method for NesCGeneration class.
     * @param plan The query plan which code is being generated for.
     * @param qos The user-specified quality-of-service requirements.
     * @param sink The sink node of the network.
     * @param nescOutputDir The nesC output directory for generated code.
     * @param tosVersion The TinyOS version which nesC is being generated for.
     * @param tossimFlag Indicates whether code for Tossim simulator needs to be generated.
     * @throws OptimizationException
     */
    public static void doNesCGeneration(final QueryPlan plan,
	    final QoSSpec qos,
	    int sink,
	    final String nescOutputDir,
	    int tosVersion,
	    boolean tossimFlag)
	    throws OptimizationException {


    	try {
			initialize(tosVersion, tossimFlag, plan);

			/* Generate inter-fragment configurations */
			HashMap<Site, NesCConfiguration> siteConfigs = null;
			siteConfigs = generateSiteConfigurations(
				    plan, qos, sink, tosVersion, tossimFlag);
			final NesCConfiguration tossimConfig =
				generateTossimConfiguration(plan, siteConfigs, tosVersion,
					tossimFlag);

		    /* Generate an intra-fragment configuration (i.e., operator
		     * granularity) for each fragment instance */
		    generateFragmentConfigurations(plan, siteConfigs, qos, tosVersion,
		    		tossimFlag);

		    /* Instantiate code for the top-level configuration(s); the nested
		     * components and configurations are instantiated recursively */
		    if (tossimFlag) {
			    instantiateTossimConfiguration(plan, tossimConfig,
			    		nescOutputDir, tosVersion);
		    } else {
			    instantiateSiteConfigurations(plan, siteConfigs, nescOutputDir,
			    		tosVersion);
		    }

		    /* Generate QueryPlan.h file with constants and data
		     * structures etc.*/
		    generateHeaderFile(plan, siteConfigs, nescOutputDir, tosVersion,
		    	tossimFlag);

		    /* Generate the interface files */
		    generateTypedInterfaces(plan, siteConfigs, nescOutputDir,
		    	tosVersion, tossimFlag);

		    /* Copy interface files over from templates dir to nesC output
		     * dirs */
		    copyMiscFiles(plan, nescOutputDir, qos, siteConfigs.size(),
		    	tosVersion, tossimFlag);

		} catch (final IOException e) {
		    Utils.handleCriticalException(e);
		} catch (final CodeGenerationException e) {
		    Utils.handleCriticalException(e);
		}
    }
}
