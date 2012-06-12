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
 * Represents a sub-query.
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class SubQuery extends Source {

    private final Logger logger = Logger.getLogger(SubQuery.class.getName());

    /*
     * The query.
     */
    private Translator query;

    /**
     * Construct an object to represent a sub-query.
     * @param query the sub-query
     * @param variableName the variable name assigned to the sub-query
     * @param javaType the type returned by the sub-query
     */
    public SubQuery(final Translator query, final String variableName,
	    final AttributeType type) throws TranslationException {
	super(variableName, query.getMetaData(), type);
	//query.renameSelect(variableName);
	if (variableName == null) {
	    throw new TranslationException(
		    "Attempt to create SubQuery without name");
	}
	this.query = query;
	this.logger.finest("SubQuery " + variableName.toString()
		+ " created with type " + type);
    }

    /**
     * Returns the query
     */
    public Translator getQuery() {
	return this.query;
    }

    /**/
    @Override
    public String toString() {
	return this.getName() + " <- see Sub queires";
    }
}
