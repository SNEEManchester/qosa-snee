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
package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation;

import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;

/**
 * Represents an attribute
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class OldAttribute extends Variable {

    private final Logger logger = Logger.getLogger(OldAttribute.class.getName());

    private String sourceReference;

    /*
     * The relation to which the attribute belongs
     */
    private Source source;

    private String identifier;

    /**
     * Create an object to represent an attribute.
     * @param relation the relation to which the attribute belongs
     * @param attributeName the name of the attribute
     * @param javaType the type of the attribute
     */
    public OldAttribute(final Source source, final String identifier,
	    final String name, final AttributeType type) {
	super(name, type);
	this.logger.finest("New attribute " + this.getName()
		+ " created with source [" + source + "] and identifier "
		+ identifier);
	if (name == null) {
	    this.setName(identifier);
	}
	//if (name.indexOf('.' )>0)
	//  sourceReference = name.substring(0, name.indexOf('.'));
	//else 
	if (source == null) {
	    this.sourceReference = null;
	} else {
	    this.sourceReference = source.getName();
	}
	this.source = source;
	this.identifier = identifier;
    }

    /**
     * Returns the relation to which the attribute belongs.
     */
    public Source getSource() {
	return this.source;
    }

    @Override
    public String getPureName() {
	return this.sourceReference + "." + this.identifier;
    }

    /**
     * Returns a String representation of the attribute.
     */
    @Override
    public String toString() {
	if (this.getName().equalsIgnoreCase(
		this.sourceReference + "." + this.identifier)) {
	    return this.getName();
	} else {
	    return this.getName() + "<-" + this.sourceReference + "."
		    + this.identifier;
	}
    }

    // public void rename(String rename)
    // {
    // 	this.rename = rename;
    //}

    @Override
    public String getSourceReference() {
	return this.sourceReference;
    }

    @Override
    public void setSourceReference(final String sourceReference) {
	this.sourceReference = sourceReference;
    }

    public boolean same(final OldAttribute other) {
	this.logger.finest("Equals called " + this + " =?= " + other);
	if (!this.source.equals(other.source)) {
	    return false;
	}
	if (!this.identifier.equalsIgnoreCase(other.identifier)) {
	    return false;
	}
	if (!this.getName().equalsIgnoreCase(other.getName())) {
	    return false;
	}
	return true;
    }
}
