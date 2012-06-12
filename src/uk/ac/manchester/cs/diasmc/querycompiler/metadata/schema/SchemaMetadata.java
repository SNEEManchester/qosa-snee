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
package uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema;

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

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.logger.LoggerSetup;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;

public class SchemaMetadata {
    private static final Logger logger = Logger.getLogger(SchemaMetadata.class
	    .getName());

    private Hashtable<String, SourceMetadata> sources;

//    private int[] sinks;

    public SchemaMetadata() {
	this.sources = new Hashtable<String, SourceMetadata>();
	AttributeType timeType = null;
	AttributeType idType = null;
	try {
	    timeType = Types.getType(Constants.TIME_TYPE);
	    timeType.setSorted(true);
	    idType = Types.getType(Constants.ID_TYPE);
	} catch (final SchemaMetadataException e) {
	    Utils.handleCriticalException(e);
	}
	try {
	    final Document doc = this.parseFile();
	    final Element root = (Element) doc.getFirstChild();
	    NodeList xmlSources = root.getElementsByTagName("stream");
	    for (int i = 0; i < xmlSources.getLength(); i++) {
		try {
		    final SourceMetadata source = new SourceMetadata(
			    (Element) xmlSources.item(i), true);
		    //   				source.addAttribute("time", timeType);
		    source.addAttribute("id", idType);
		    source.addAttribute("time", timeType);
		    this.sources.put(source.getName(), source);
		    logger.finest("Added source " + source.getName());
		} catch (final UnsupportedAttributeTypeException e) {
		    logger.severe(e.toString());
		}
	    }
	    xmlSources = root.getElementsByTagName("table");
	    for (int i = 0; i < xmlSources.getLength(); i++) {
		try {
		    final SourceMetadata source = new SourceMetadata(
			    (Element) xmlSources.item(i), false);
		    this.sources.put(source.getName(), source);
		    logger.finest("Added source " + source.getName());
		} catch (final UnsupportedAttributeTypeException e) {
		    logger.severe(e.toString());
		}
	    }

	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	}
    }




    private Document parseFile() {
	final DocumentBuilderFactory factory = DocumentBuilderFactory
		.newInstance();
	try {
	    final DocumentBuilder builder = factory.newDocumentBuilder();
	    logger.config("reading metadata from "
		    + Settings.INPUTS_SCHEMA_FILE);
	    return builder.parse(Settings.INPUTS_SCHEMA_FILE);
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	}
	return null;
    }

    public SourceMetadata getSourceMetaData(final String name)
	    throws SourceDoesNotExistException {
	final SourceMetadata temp = this.sources.get(name);
	if (temp == null) {
	    throw new SourceDoesNotExistException("Source " + name
		    + " not found in Schema");
	}
	return temp;
    }



    public static void main(final String args[]) 
    {
	Settings.initialize(new Options(args));
	LoggerSetup.setLogLevel("finest", SchemaMetadata.class.getName());
	LoggerSetup.setLogLevel("finest", SourceMetadata.class.getName());
	final SchemaMetadata me = new SchemaMetadata();
	try {
	    logger.finest("flow");
	    final SourceMetadata flow = me.getSourceMetaData("flow");
	    final LinkedHashMap<String, AttributeType> tupleAttr = flow
		    .getAttributes();
	    final Iterator<String> tupleAttrIter = tupleAttr.keySet()
		    .iterator();
	    while (tupleAttrIter.hasNext()) {
		final String attrName = tupleAttrIter.next();
		final AttributeType attrType = tupleAttr.get(attrName);
		logger.finest("\t" + attrName + ":\n\t\t" + attrType.getName()
			+ "\t" + attrType.getSize());
	    }
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	}
	logger.finest("TEST OK" + me);
    }
}