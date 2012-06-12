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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;

/**
 * 
 * Implementation of the data dictionary for looking up logical
 * and physical metadata about imported databases.
 * 
 * @author Christian Brenninkmeijer and Steven Lynden 
 */
public class SourceMetadata {

    Logger logger = Logger.getLogger(SourceMetadata.class.getName());

    private int cardinality = 100;

    /*
     * Attribute name (String key) and type (Java class). 
     */
    private LinkedHashMap<String, AttributeType> attributes;

    private String name;

    private boolean stream;

    private int[] nodes;

    public SourceMetadata(final String name) {
	this.attributes = new LinkedHashMap<String, AttributeType>();
	this.name = name;
	this.cardinality = -1;
    }

    public SourceMetadata() {
	this.attributes = new LinkedHashMap<String, AttributeType>();
	this.name = null;
	this.cardinality = -1;
    }

    public SourceMetadata(final Element xml, final boolean stream)
	    throws SchemaMetadataException, UnsupportedAttributeTypeException {
	this.stream = stream;
	//add the columns to the attributes hashtable
	final NodeList columns = xml.getElementsByTagName("column");
	this.name = xml.getAttribute("name").toLowerCase();
	this.logger.finest("Reading source " + this.name);
	this.attributes = SourceMetadata.NewMapWithEvalTime();
	for (int i = 0; i < columns.getLength(); i++) {
	    final Element tabElement = (Element) columns.item(i);
	    //attributeName = tabElement.getAttribute("name");
	    final String attributeName = tabElement.getAttribute("name")
		    .toLowerCase();
	    this.logger.finest("Reading attribute " + attributeName);
	    final NodeList xmlType = tabElement.getElementsByTagName("type");
	    final String sqlType = ((Element) xmlType.item(0)).getAttribute(
		    "class").toLowerCase();
	    this.logger.finest("sqlType = " + sqlType);
	    final AttributeType type = Types.getType(sqlType);
	    this.logger.finest("Type = " + type);
	    if ((type.getNesCName() == null)
		    || (type.getNesCName().length() == 0)) {
		throw new UnsupportedAttributeTypeException("The source "
			+ this.name + " has an attribte " + attributeName
			+ "with type " + type
			+ " which is currently unsupported");
	    }
	    type.setLength(((Element) xmlType.item(0)).getAttribute("length"));
	    this.attributes.put(attributeName, type);
	    //attributes.put(attributeName, sqlType );
	}
	final NodeList nodesxml = xml.getElementsByTagName("sites");
	if (nodesxml.getLength() == 0) {
	    throw new SchemaMetadataException("No sites info found for "
		    + this.name);
	}
	String nodesText = null;
	for (int i = 0; i < nodesxml.getLength(); i++) {
	    if (nodesText == null) {
		nodesText = nodesxml.item(i).getTextContent();
	    } else {
		nodesText = nodesText + "," + nodesxml.item(i).getTextContent();
	    }
	}
	this.logger.finest("sites text " + nodesText);
	this.nodes = this.convertNodes(nodesText);
	if (stream) {
	    this.cardinality = this.nodes.length; //*100;
	    //logger.warning("cardinality temporarily set to number of nodes * 100");
	} else {
	    try {
		this.cardinality = Integer.parseInt(xml.getAttribute("rows"));
	    } catch (final NumberFormatException e) {
		throw new SchemaMetadataException(
			"No numberical rows info found for table " + this.name);
	    }
	}
	this.logger.finest("Cardinality set to " + this.cardinality);
    }

    public static LinkedHashMap<String, AttributeType> NewMapWithEvalTime() {
    	final LinkedHashMap<String, AttributeType> data 
    		= new LinkedHashMap<String, AttributeType>();
   		return data;
    }

    private int[] convertNodes(final String text) {
	try {
	    int count = 0;
	    String temp;
	    StringTokenizer tokens = new StringTokenizer(text, ",");
	    while (tokens.hasMoreTokens()) {
		temp = tokens.nextToken();
		if (temp.indexOf('-') == -1) {
		    count++;
		} else {
		    final int start = Integer.parseInt(temp.substring(0, temp
			    .indexOf('-')));
		    final int end = Integer.parseInt(temp.substring(temp
			    .indexOf('-') + 1, temp.length()));
		    //logger.finest(temp+" "+start+"-"+end);
		    if (end < start) {
			throw new SchemaMetadataException(
				"Start less than end in nodes defintion of "
					+ this.name);
		    }
		    count = count + end - start + 1;
		}
	    }
	    //logger.finest("count = "+count);
	    if (count == 0) {
		throw new SchemaMetadataException("No nodes defined for "
			+ this.name);
	    }
	    int[] list = new int[count];
	    count = 0;
	    tokens = new StringTokenizer(text, ",");
	    while (tokens.hasMoreTokens()) {
		temp = tokens.nextToken();
		if (temp.indexOf('-') == -1) {
		    list[count] = Integer.parseInt(temp);
		    count++;
		} else {
		    final int start = Integer.parseInt(temp.substring(0, temp
			    .indexOf('-')));
		    final int end = Integer.parseInt(temp.substring(temp
			    .indexOf('-') + 1, temp.length()));
		    for (int i = start; i <= end; i++) {
			list[count] = i;
			count++;
		    }
		}
	    }
	    Arrays.sort(list);
	    count = 0;
	    for (int i = 0; i < list.length - 2; i++) {
		if (list[i] >= list[i + 1]) {
		    list[i] = Integer.MAX_VALUE;
		    count++;
		}
	    }
	    if (count > 0) {
		Arrays.sort(list);
		final int[] newList = new int[list.length - count];
		for (int i = 0; i < newList.length; i++) {
		    newList[i] = list[i];
		}
		list = newList;
	    }
	    String t = "";
	    for (int element : list) {
		t = t + "," + element;
	    }
	    this.logger.finest("nodes =" + t);
	    return list;
	} catch (final Exception e) {
	    Utils.handleCriticalException(e);
	    return null;
	}
    }

    String getName() {
	return this.name;
    }

    public void setName(final String name) throws SchemaMetadataException {
	if (this.name == null) {
	    this.name = name;
	} else {
	    throw new SchemaMetadataException(
		    "Incorrect attempt to write over sourceMetaDataName");
	}
    }

    //protected void setCardinality (int cardinality)
    //{
    //		this.cardinality = cardinality;
    //}

    public int getCardinality() {
	return this.cardinality;
    }

    /**/
    public void addAttribute(String attribute, final AttributeType type)
	    throws LookupException {
	if (attribute.contains(".")) {
	    final String sourceName = attribute.substring(0, attribute
		    .indexOf('.'));
	    if (!this.name.equalsIgnoreCase(sourceName)) {
		throw new LookupException("Error adding attribute " + attribute
			+ " to metaData for " + this.name
			+ " Source names do not match");
	    }
	    attribute = attribute.substring(attribute.indexOf('.') + 1,
		    attribute.length());
	}
	final AttributeType test = this.attributes.put(attribute, type);
	if (test != null) {
	    throw new LookupException("Error adding attribute " + attribute
		    + " it already exists");
	}
    }

    /**/
    public AttributeType getAttributeType(final String attribute)
	    throws LookupException {
	final AttributeType type = this.attributes.get(attribute);
	if (type == null) {
	    throw new LookupException("Source " + this.name
		    + " does not have an attribute " + attribute);
	}
	return type;
    }

    /** 
     * @return The unqualified attribute names and their types
     */
    public LinkedHashMap<String, AttributeType> getAttributes() {
	return this.attributes;
    }

    public Class oldgetJavaType() throws LookupException {
	throw new LookupException("Ol method called");
    }

    public AttributeType getJavaType() throws LookupException {
	//logger.finest("attributes.size ="+attributes.size());
	if (this.attributes.size() == 0) {
	    //logger.finest("throwing exception");
	    throw new LookupException("No attributes listed");
	}
	if (this.attributes.size() == 1) {
	    //logger.finest("Metadata "+name+" JavaType = "+ attributes.elements().nextElement().getClass());
	    //logger.finest(attributes.elements().nextElement().toString());
	    return this.attributes.values().iterator().next();
	}
	//logger.finest("MetaData "+name+" type set to List");
	return Types.tupleType(this.attributes);
    }

    public boolean hasAttribute(final String attribute) {
	//logger.finest(attributes.toString());
	final AttributeType type = this.attributes.get(attribute);
	if (type == null) {
	    return false;
	} else {
	    return true;
	}
    }

    @Override
    public String toString() {
	final StringBuffer s = new StringBuffer(this.name);
	s.append("   Cardinality: " + this.cardinality);
	s.append(" - [");
	final Iterator<String> it = this.attributes.keySet().iterator();
	while (it.hasNext()) {
	    final String key = it.next();
	    s.append(key);
	    s.append(":");
	    s.append(this.attributes.get(key));
	    s.append("  ");
	}
	s.append("]");
	s.append(Constants.NEWLINE);
	return s.toString();
    }

    public void check() throws TypeMappingException {
	if (this.cardinality == 0) {
	    throw new TypeMappingException("sourceMetaData for " + this.name
		    + " is Zero");
	}
    }

    public boolean isStream() {
	return this.stream;
    }

    public int[] getSourceNodes() {
	return this.nodes;
    }

    public int getSize() {
	int count = 0;
	final Iterator<AttributeType> types = this.attributes.values()
		.iterator();
	while (types.hasNext()) {
	    count = count + types.next().getSize();
	}
	return count;
    }
}
