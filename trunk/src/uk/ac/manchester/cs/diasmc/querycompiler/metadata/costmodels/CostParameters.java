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
package uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.logger.LoggerSetup;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadataException;

public class CostParameters {

    private static final Logger logger = Logger.getLogger(CostParameters.class
	    .getName());

    private static float copyTuple;

    private static float applyPredicate;

    private static float checkTuple;

    //private static float compareTuple;

    private static float doCalculation;

    //private static float concatenate;

    private static float callMethod;

    private static float signalEvent;

    private static float acquireData;

    private static float scanTuple;

    private static float turnOnRadio;

    private static float turnOffRadio;

    private static float radioSyncWindow;

    private static float sendPacket;
    
    private static float betweenPackets;

    private static float setAValue;

    private static float deliverTuple;

    private static float minimumTimerInterval;

    public static void setupOperatorMetaData() {
	try {
	    final Document doc = parseFile();
	    final Element root = (Element) doc.getFirstChild();
	    copyTuple = getFloatValue(root, "CopyTuple");
	    applyPredicate = getFloatValue(root, "ApplyPredicate");
	    checkTuple = getFloatValue(root, "CheckTuple");
	    //compareTuple = getFloatValue(root,"CompareTuple");
	    doCalculation = getFloatValue(root, "DoCalculation");
	    //concatenate = getFloatValue(root,"Concatenate");
	    callMethod = getFloatValue(root, "CallMethod");
	    signalEvent = getFloatValue(root, "SignalEvent");
	    acquireData = getFloatValue(root, "AcquireData");
	    scanTuple = getFloatValue(root, "ScanTuple");
	    turnOnRadio = getFloatValue(root, "TurnOnRadio");
	    turnOffRadio = getFloatValue(root, "TurnOffRadio");
	    radioSyncWindow = getFloatValue(root, "RadioSyncWindow");
	    sendPacket = getFloatValue(root, "SendPacket");
	    betweenPackets = getFloatValue(root, "BetweenPackets");
	    setAValue = getFloatValue(root, "SetAValue");
	    deliverTuple = getFloatValue(root, "DeliverTuple");
	    minimumTimerInterval = getFloatValue(root, "MinimumTimerInterval");
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	}
    }

    private static float getFloatValue(final Element elem, final String name)
	    throws SchemaMetadataException {
	final NodeList elements = elem.getElementsByTagName(name);
	if (elements.getLength() == 0) {
	    throw new SchemaMetadataException("Operator MetaData " + name
		    + " not found. Please add it to "
		    + Settings.INPUTS_COST_PARAMETERS);
	}
	if (elements.getLength() > 1) {
	    logger.warning("Operator MetaData " + name
		    + " found twice. Please check "
		    + Settings.INPUTS_COST_PARAMETERS);
	}
	final String temp = ((Element) elements.item(0)).getAttribute("time");
	if ((temp == null) || (temp.length() == 0)) {
	    throw new SchemaMetadataException("Operator MetaData " + name
		    + " does not have a time attribute. Please add it to "
		    + Settings.INPUTS_COST_PARAMETERS);
	}
	try {
	    logger.finest("" + name + " = " + Float.parseFloat(temp));
	    return Float.parseFloat(temp);
	} catch (final Exception e) {
	    throw new SchemaMetadataException("Operator MetaData " + name
		    + " can not be converted to a float. Please add it to "
		    + Settings.INPUTS_COST_PARAMETERS);
	}
    }

    private static Document parseFile() {
	final DocumentBuilderFactory factory = DocumentBuilderFactory
		.newInstance();
	try {
	    final DocumentBuilder builder = factory.newDocumentBuilder();
	    logger.config("reading metadata from "
		    + Settings.INPUTS_COST_PARAMETERS);
	    return builder.parse(Settings.INPUTS_COST_PARAMETERS);
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	}
	return null;
    }

    public static float getCopyTuple() {
	return copyTuple;
    }

    public static float getApplyPredicate() {
	return applyPredicate;
    }

    /**/
    public static float getCheckTuple() {
	return checkTuple;
    }

    /** /
     public static float getConcatenate()
     {
     return concatenate;
     }
     /**/
    public static float getDoCalculation() {
	return doCalculation;
    }

    public static float getCallMethod() {
	return callMethod;
    }

    public static float getSignalEvent() {
	return signalEvent;
    }

    public static float getAcquireData() {
	return acquireData;
    }

    public static float getScanTuple() {
	return scanTuple;
    }

    public static float getTurnOnRadio() {
	return turnOnRadio;
    }

    public static float getTurnOffRadio() {
	return turnOffRadio;
    }

    public static float getRadioSyncWindow() {
	return radioSyncWindow;
    }

    public static float getSendPacket() {
	return sendPacket;
    }

    public static float getBetweenPackets() {
    	return betweenPackets;
    }
    
    public static float getSetAValue() {
	return setAValue;
    }

    public static float getDeliverTuple() {
	return deliverTuple;
    }

    public static float getMinimumTimerInterval() {
	return minimumTimerInterval;
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
	Settings.initialize(new Options(args));
	LoggerSetup.setLogLevel("finest", CostParameters.class.getName());
	logger.finest("Settings done");
	setupOperatorMetaData();
	logger.finest("" + CostParameters.getSignalEvent());
	logger.finest("" + CostParameters.getAcquireData());
	logger.finest("Success");
    }

}
