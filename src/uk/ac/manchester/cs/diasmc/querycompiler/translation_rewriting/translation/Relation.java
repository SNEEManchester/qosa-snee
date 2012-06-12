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
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.LookupException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SourceMetadata;

/**
 * Represents a relation (table).
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class Relation extends Source {

    private final Logger logger = Logger.getLogger(Relation.class.getName());

    /**
     * Construct an object to represent a relation.
     * @param tableName the table name
     * @param variableName the variable name of the relation 
     * /
     public Relation(String tableName, String variableName) {
     super(variableName,java.util.List.class);
     this.tableName = tableName;
     logger.severe("Non metadata constructor called");
     }
     
     /**
     * Construct an object to represent a relation.
     * @param tableName the table name
     * @param variableName the variable name of the relation 
     */
    public Relation(final String tableName, final SourceMetadata data,
	    final String name) throws LookupException {
	super(data.getJavaType());
	if (name == null) {
	    this.setName(tableName);
	} else {
	    this.setName(name);
	}
	this.tableName = tableName;
	this.metaData = data;
	this.logger.finest("Relation " + this + " created");
    }

    /**
     * Construct an object to represent a relation.
     * @param tableName the table name
     * @param variableName the variable name of the relation 
     * @param type the type of theresult returned the relation
     */
    public Relation(final String name, final SourceMetadata data,
	    final AttributeType type) {
	super(type);
	this.setName(name);
	this.tableName = name;
	this.metaData = data;
	this.logger.finest("Relation " + this + " created with type " + type);
    }

    /*
     * 
     public Relation(String variableName,  Class javaType) 
     {
     super(variableName,javaType);
     this.tableName = null;
     logger.severe("Non metadata constructor called");
     }

     
     /**
     * Returns a String representation of the relation.
     * /
     public String toString() {
     if (getName().equalsIgnoreCase(getTableName()))
     return getName();
     else	
     return getName() + " <- " + getTableName();   
     }
     
     public boolean hasAttribute (String attribute)
     {
     return metaData.hasAttribute(attribute);
     }

     public Type getAttributeType(String attribute)throws LookupException
     {
     return metaData.getAttributeType(attribute);
     }
     /**/
    @Override
    public int getCardinality() {
	return this.metaData.getCardinality();
    }
    /**/
}
