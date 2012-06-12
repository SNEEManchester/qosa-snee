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

import java.util.logging.Logger;

import org.w3c.dom.Element;

public class AttributeType {
    private static final Logger logger = Logger.getLogger(AttributeType.class
	    .getName());

    /**
     * Name of the type as found in the schema document.
     */
    String name;

    /**
     * Size in bytes. As it is saved in the system.<br>
     * For types with variable length this is the header size regradless of length.
     */
    private int size;

    /**
     * Math level. Describes what operations can be done on this variable.<br>
     * Level 0 = No operations at all
     * Level 1 = Equals within type only<br>
     * Level 2 = Comparisons within type only<br>
     * Level 3 = Standard Functions and Comparisions within type only<br>
     * Level 4 = All Functions and Comparisions with type only<br>
     * Level 5 = Convertable to a real so full math support<br>
     */
    private int math;

    /**
     * Size factor for types with variable length.<br>
     * Full size is then size + factor * length.<br>
     * factor of zero means that this type can not have a variable length.
     */
    private int factor;

    /**
     * The length for the particular implementation of this time.<br>
     * Must be set by the schema.
     */
    private int length = 1;

    private String nesCName;

    private boolean sorted;

    public AttributeType(final Element xml) throws TypeMappingException {
	//add the columns to the attributes hashtable
	this.name = xml.getAttribute("name").toLowerCase();
	logger.finest("Reading type \"" + this.name + "\"");
	String temp = xml.getAttribute("size").toLowerCase();
	logger.finest("Size = \"" + temp + "\"");
	this.size = Integer.parseInt(temp);
	temp = xml.getAttribute("math").toLowerCase();
	logger.finest("Math = \"" + temp + "\"");
	this.math = Integer.parseInt(temp);
	temp = xml.getAttribute("factor").toLowerCase();
	if (temp.length() == 0) {
	    this.factor = 0;
	    logger.finest("Variable set to false. Factor = 0");
	} else {
	    this.factor = Integer.parseInt(temp);
	    logger.finest("Variable set to true. Factor = " + this.factor);
	}
	this.nesCName = xml.getAttribute("nesCName").toLowerCase();
	logger.finest("nesCName = \"" + this.nesCName + "\"");
	this.sorted = false;
    }

    AttributeType(final String name, final int size, final int math,
	    final int factor, final int length, final String nesCName,
	    final boolean sorted) {
	this.name = name;
	this.size = size;
	this.math = math;
	this.factor = factor;
	this.length = length;
	this.sorted = sorted;
	this.nesCName = nesCName;
    }

    public AttributeType clone() {
	return new AttributeType(this.name, this.size, this.math, this.factor,
		this.length, this.nesCName, this.sorted);
    }

    private void setLength(final int length) throws SchemaMetadataException {
	if (this.factor == 0) {
	    throw new SchemaMetadataException("Attempt to set length of type "
		    + this.name + " not supported as length is fixed.");
	}
	this.length = length;
    }

    void setLength(final String text) throws SchemaMetadataException {
	if (text == null) {
	    return;
	}
	if (text.length() == 0) {
	    return;
	}
	try {
	    this.setLength(Integer.parseInt(text));
	    logger.finest("Length of " + this.name + " set to");
	} catch (final Exception e) {
	    throw new SchemaMetadataException(
		    "Attempt to set length with non integer text of \"" + text
			    + "\"");
	}
    }

    public void setSorted(final boolean sorted) {
	this.sorted = sorted;
    }

    public int getSize() {
	return this.size + this.factor * this.length;
    }

    public boolean getSorted() {
	return this.sorted;
    }

    public boolean handledByStandardFunctions() {
	if (this.math >= 3) {
	    return true;
	} else {
	    return false;
	}
    }

    public String getName() {
	return this.name;
    }

    public String getNesCName() {
	return this.nesCName;
    }

    public boolean canEqual(final AttributeType other) {
	if (this.name.equalsIgnoreCase(other.name)) {
	    return true;
	}
	if ((this.math >= 5) && (other.math >= 5)) {
	    return true;
	}
	logger.finest("Type " + this.name
		+ " can not be tested for equal with type " + other.name);
	return false;
    }

    public boolean canCompare(final AttributeType other) {
	if (this.name.equalsIgnoreCase(other.name)) {
	    if (this.math >= 2) {
		return true;
	    } else {
		logger.finest("Type " + this.name
			+ " can not tested for comparision");
		return false;
	    }
	}
	if ((this.math >= 5) && (other.math >= 5)) {
	    return true;
	}
	logger.finest("Type " + this.name
		+ " can not be tested for compariosn with type " + other.name);
	return false;
    }

    public String toString() {
	return this.name + " size: " + this.size + " nesCName: "
		+ this.nesCName;
    }

}
