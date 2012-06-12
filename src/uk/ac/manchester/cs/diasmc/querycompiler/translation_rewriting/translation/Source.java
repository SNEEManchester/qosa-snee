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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.LookupException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SourceMetadata;

/**
 * Superclass for Stream, Relations and subQueries.
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class Source extends Element {

    private final Logger logger = Logger.getLogger(Source.class.getName());

    /*
     * Name of the table.
     */
    protected String tableName;

    private String name;

    protected SourceMetadata metaData;

    private LinkedHashMap<String, AttributeType> attributes = null;

    /**
     * Passthrough constructor
     */
    public Source(final AttributeType type) { //throws LookupException 
	    super(type);
	    this.name = "";
    }

    /**
     * Construct an object to represent a relation.
     * @param tableName the table name
     * @param variableName the variable name of the relation 
     * @param type the type of theresult returned the relation
     */
    public Source(final String name, final SourceMetadata data,
	    final AttributeType type) {
	super(type);
	this.name = name;
	this.tableName = name;
	this.metaData = data;
	this.logger.finest("Relation " + this + " created with type " + type);
    }

    /** 
     * @return The qualified attribute names and their types
     */
    public LinkedHashMap<String, AttributeType> getAttributes() {
	if (this.attributes == null) {
	    final LinkedHashMap<String, AttributeType> unqualified = this.metaData
		    .getAttributes();
	    this.attributes = new LinkedHashMap<String, AttributeType>();
	    final Iterator<String> keys = unqualified.keySet().iterator();
	    while (keys.hasNext()) {
		final String att = keys.next();
		if (att.equalsIgnoreCase(Constants.EVAL_TIME)) {
		    this.attributes.put(Constants.EVAL_TIME, unqualified
			    .get(att));
		} else {
		    this.attributes.put(this.name + '.' + att, unqualified
			    .get(att));
		}
	    }
	}
	return this.attributes;
    }

    /**
     * Returns the table name
     */
    public String getTableName() {
	return this.tableName;
    }

    /**
     * 
     * @param Qualification name to be used for all attriutes
     */
    protected void setName(final String name) {
	this.name = name;
	this.attributes = null;
    }

    /**
     * Returns the relations name 
     */
    public String getName() {
	return this.name;
    }

    /**
     * Returns a String representation of the relation.
     */
    @Override
    public String toString() {
	if (this.getName().equalsIgnoreCase(this.getTableName())) {
	    return this.getName();
	} else {
	    return this.getName() + " <- " + this.getTableName();
	}
    }

    public boolean hasAttribute(final String attribute) {
	return this.metaData.hasAttribute(attribute);
    }

    public AttributeType getAttributeType(final String attribute)
	    throws LookupException {
	return this.metaData.getAttributeType(attribute);
    }

    protected int getCardinality() {
	return this.metaData.getCardinality();
    }

    public SourceMetadata getMetaData() {
	return this.metaData;
    }
    /**/
}
