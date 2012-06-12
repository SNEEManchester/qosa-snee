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
package uk.ac.manchester.cs.diasmc.querycompiler.qos;

/*
 * @(#)DomEcho02.java	1.9 98/11/10
 *
 * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.Units;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.UnrecognizedUnitException;

/**
 * @author Ixent Galpin and Christian Brenninkmeijer
 */

public class QoSSpecReader extends QoSSpec {
    private static final Logger logger = Logger.getLogger(QoSSpecReader.class
	    .getName());

    //sets QoS parameters based on command line options
    public QoSSpecReader(final Options opt , int queryNumber) throws QoSException , 
	    XPathExpressionException, ParserConfigurationException,
	    SAXException, IOException, UnrecognizedUnitException {

	if (!Settings.INPUTS_QOS_FILE.equals("none")) {
	    this.parseQoSXMLFile(Settings.INPUTS_QOS_FILE);
	}

	this.readCommandLineSettings(opt, queryNumber);
    }

    private void readCommandLineSettings(final Options opt, int queryNumber) throws QoSException {
    	String [] parts;
    	
	//check acquisition interval options given are consistent
	if ((opt.getSet().isSet("qos-acquisition-interval") && opt.getSet()
		.isSet("qos-min-acquisition-interval"))
		|| (opt.getSet().isSet("qos-acquisition-interval") && opt
			.getSet().isSet("qos-max-acquisition-interval"))) {
	    throw new QoSException(
		    "It is not possible to set a specific acquisition interval and a min/max bound for acquisition interval");
	}
	
	if (opt.getSet().isSet("qos-buffering-factor")
		&& opt.getSet().isSet("qos-max-buffering-factor")) {
	    throw new QoSException(
		    "It is not possible to set a specific buffering factor and a max bound for buffering factor");
	}

	if (opt.getSet().isSet("qos-acquisition-interval")) {
		parts = opt.getSet().getOption(
	    "qos-acquisition-interval").getResultValue(0).split(",");
		// Set  the qos-acquisition-interval if its being 
		//specified else keep the default QoS
		if (queryNumber < parts.length) {
		this.setMinAcquisitionInterval(Long.valueOf(parts[queryNumber]));
	    this.setMaxAcquisitionInterval(Long.valueOf(parts[queryNumber]));
		}
	}
	
	if (opt.getSet().isSet("qos-min-acquisition-interval")) {
	    if (!Settings.QOS_AWARE_WHEN_SCHEDULING) {
		throw new QoSException(
			"option \"qos-min-acquisition-interval\" not available in vanilla version of when-scheduling");
	    }
	    parts = opt.getSet().getOption(
	    "qos-min-acquisition-interval").getResultValue(0).split(",");

	    // Set  the qos-min-acquisition-interval if its being 
	    //specified else keep the default QoS 
		if (queryNumber < parts.length) {
			this.setMinAcquisitionInterval(Long.valueOf(parts[queryNumber]));
		}
	}

	if (opt.getSet().isSet("qos-max-acquisition-interval")) {
	    if (!Settings.QOS_AWARE_WHEN_SCHEDULING) {
		throw new QoSException(
			"option \"qos-max-acquisition-interval\" not available in vanilla version of when-scheduling");
	    }
	    parts = opt.getSet().getOption(
	    "qos-max-acquisition-interval").getResultValue(0).split(",");
	    
	    // Set  the qos-max-acquisition-interval if its 
	    //being specified else keep the default QoS
	    if (queryNumber < parts.length) {
			this.setMaxAcquisitionInterval(Long.valueOf(parts[queryNumber]));
		}
	    
	}
	if (opt.getSet().isSet("qos-max-delivery-time")) {
		parts = opt.getSet().getOption(
				"qos-max-delivery-time").getResultValue(0).split(",");
		// Set  the qos-max-delivery-time if its being 
		//specified else keep the default QoS
		if (queryNumber < parts.length) {
			this.setMaxDeliveryTime(Long.valueOf(parts[queryNumber]));
		}   
	}
	
	if (opt.getSet().isSet("qos-max-total-energy")) {
	    if (!Settings.QOS_AWARE_WHEN_SCHEDULING) {
		throw new QoSException(
			"option \"qos-max-total-energy\" not available in vanilla version of when-scheduling");
	    }
	    parts = opt.getSet().getOption(
		"qos-max-total-energy").getResultValue(0).split(",");
	    // Set  the qos-max-total-energy if its being 
	    //specified else keep the default QoS
	    if (queryNumber < parts.length) {
	    	this.setMaxTotalEnergy(Long.valueOf(parts[queryNumber]));
	    }
	    
	}
	if (opt.getSet().isSet("qos-min-lifetime")) {
	    if (!Settings.QOS_AWARE_WHEN_SCHEDULING) {
		throw new QoSException(
			"option \"qos-min-lifetime\" not available in vanilla version of when-scheduling");
	    }
	    parts = opt.getSet().getOption(
		"qos-min-lifetime").getResultValue(0).split(",");
	    // Set  the qos-min-lifetime if its being 
	    //specified else keep the default QoS
	    if (queryNumber < parts.length) {
	    	this.setMinLifetime(Long.valueOf(parts[queryNumber]));
	    }
	}
	if (opt.getSet().isSet("qos-buffering-factor")) {
		parts = opt.getSet().getOption(
				"qos-buffering-factor").getResultValue(0).split(",");
		// Set  the qos-buffering-factor if its being 
		//specified else keep the default QoS
		if (queryNumber < parts.length) {
	    	this.setMinBufferingFactor(Long.valueOf(parts[queryNumber]));
	    	this.setMaxBufferingFactor(Long.valueOf(parts[queryNumber]));
	    }
	}
	if (opt.getSet().isSet("qos-max-buffering-factor")) {
		parts = opt.getSet().getOption(
		"qos-max-buffering-factor").getResultValue(0).split(",");
		// Set  the qos-max-buffering-factor if its being 
		//specified else keep the default QoS
		if (queryNumber < parts.length) {
			this.setMaxBufferingFactor(Long.valueOf(parts[queryNumber]));
		}
	}
	
	if (opt.getSet().isSet("qos-query-duration")) {
		parts = opt.getSet().getOption(
		"qos-query-duration").getResultValue(0).split(",");
		// Set  the qos-query-duration if its being 
		//specified else keep the default QoS
		if (queryNumber < parts.length) {
			this.setQueryDuration(Long.valueOf(parts[queryNumber]));
		}
	}
	logger.info(this.toString());
    }

    private Range getQoSVariableRange(final String queryRoot)
	    throws XPathExpressionException, FileNotFoundException,
	    UnrecognizedUnitException {

	final Units units = Units.getInstance();
	final String unitName = Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		queryRoot + "/snee:units");
	final long scalingFactor = units.getScalingFactor(unitName);

	//less-equals
	final String lessEquals = Utils.doXPathStrQuery(
		Settings.INPUTS_QOS_FILE, queryRoot
			+ "/snee:constraint/snee:less-equals");
	if (lessEquals != null) {
	    return new Range(-1, new Long(lessEquals).longValue()
		    * scalingFactor);
	}

	//greater-equals
	final String greaterEquals = Utils.doXPathStrQuery(
		Settings.INPUTS_QOS_FILE, queryRoot
			+ "/snee:constraint/snee:greater-equals");
	if (greaterEquals != null) {
	    return new Range(new Long(greaterEquals).longValue()
		    * scalingFactor, -1);
	}

	//equals
	final String equals = Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		queryRoot + "/snee:constraint/snee:equals");
	if (equals != null) {
	    return new Range(new Long(equals).longValue() * scalingFactor,
		    new Long(equals).longValue() * scalingFactor);
	}

	//range
	final String minVal = Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		queryRoot + "/snee:constraint/snee:range/snee:min-val");
	final String maxVal = Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		queryRoot + "/snee:constraint/snee:range/snee:max-val");
	if (minVal != null) {
	    return new Range(new Long(minVal).longValue() * scalingFactor,
		    new Long(maxVal).longValue() * scalingFactor);
	}

	return null;
    }

    private void parseQoSXMLFile(final String filename) throws QoSException,
	    ParserConfigurationException, SAXException, IOException,
	    XPathExpressionException, UnrecognizedUnitException {
	//TODO: put this in ini file
	Utils.validateXMLFile(filename, "input/QoS/qos-specification.xsd");

	//Now read the data from the file

	this.setOptimizationType(Utils.doXPathStrQuery(
		Settings.INPUTS_QOS_FILE,
		"/snee:qos-specification/snee:expectations/snee:optimization-goal/snee:type"));
	this.setOptimizationVariable(Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		"/snee:qos-specification/snee:expectations/snee:optimization-goal/snee:variable"));
	this.setOptimizationGoalWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
		"/snee:qos-specification/snee:expectations/snee:optimization-goal/snee:weighting"));
	this.setAcquisitionIntervalRange(this
			.getQoSVariableRange("/snee:qos-specification/snee:expectations/snee:constraints/snee:acquisition-interval"));
	this.setAcquisitionConstraintWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
		"/snee:qos-specification/snee:expectations/snee:acquisition-interval/snee:weighting"));
	this.setDeliveryTimeRange(this
			.getQoSVariableRange("/snee:qos-specification/snee:expectations/snee:constraints/snee:delivery-time"));
	this.setDeliveryTimeConstraintWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
	"/snee:qos-specification/snee:expectations/snee:delivery-time/snee:weighting"));
	this.setTotalEnergyRange(this
			.getQoSVariableRange("/snee:qos-specification/snee:expectations/snee:constraints/snee:total-energy"));
	this.setTotalEnergyConstraintWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
	"/snee:qos-specification/snee:expectations/snee:total-energy/snee:weighting"));
	this.setLifetimeRange(this
			.getQoSVariableRange("/snee:qos-specification/snee:expectations/snee:constraints/snee:lifetime"));
	this.setLifetimeConstraintWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
	"/snee:qos-specification/snee:expectations/snee:lifetime/snee:weighting"));	
	this.setBufferingFactorRange(this
			.getQoSVariableRange("/snee:qos-specification/snee:expectations/snee:constraints/snee:buffering-factor"));
	this.setBufferingFactorConstraintWeighting(Utils.doXPathIntQuery(Settings.INPUTS_QOS_FILE,
	"/snee:qos-specification/snee:expectations/snee:buffering-factor/snee:weighting"));	

	
	if (Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
		"/snee:qos-specification/snee:query-duration") != null) {
	    final Units units = Units.getInstance();
		final String unitName = Utils.doXPathStrQuery(Settings.INPUTS_QOS_FILE,
				"/snee:qos-specification/snee:query-duration/snee:units");
	    final long durationScalingFactor = units
		    .getScalingFactor(unitName) / 1000;
	    this
		    .setQueryDuration(durationScalingFactor
			    * new Long(
				    Utils
					    .doXPathStrQuery(
						    Settings.INPUTS_QOS_FILE,
						    "/snee:qos-specification/snee:query-duration/snee:equals"))
				    .longValue());
	}
    }

}