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

import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;

/**
 *  Represents an aggregate function.
 *  @author Christian Brenninkmeijer and Steven Lynden
 */
public class Aggregate extends Variable {

    private final Logger logger = Logger.getLogger(Aggregate.class.getName());

    /*
     * The Aggregate operator as parsed from the query. Possible values:
     * SUM, MAX, AVG, MIN, COUNT. 
     */
    private AggregateType operator;

    /*
     * The argument to the aggregate function.
     */
    private Variable argument;

    /**
     * Construct an object to represent an aggregate function.
     * @param operator the aggregate operator, represesented as one of the following
     * strings: SUM, MAX, AVG, MIN, COUNT
     * @param argument the argument to the aggregate function
     * @param variableName a unique variable name 
     * @param javaType the output data type
     */
    public Aggregate(final String operator, final Variable argument,
	    final String variableName) throws TranslationException {
	super(variableName, null);
	if (variableName == null) {
	    throw new TranslationException(
		    "Attempt to create Aggregate without name");
	}
	this.operator = this.convertType(operator);
	this.argument = argument;
	this.type = this.operator.getType(argument);
    }

    public Aggregate(final String operator, final Element argument,
	    final String variableName, final AttributeType type) {
	super(null, null);
	this.logger.severe("CAll to old constructor");
    }

    /**
     * Return the aggregate operator
     * @return SUM, MAX, AVG, MIN or COUNT
     */
    public AggregateType getOperator() {
	return this.operator;
    }

    /**
     * Return the argument of the aggregate function
     */
    public Variable getArgument() {
	return this.argument;
    }

    /**
     * Returns a String representation of this query element
     */
    @Override
    public String toString() {
	return this.getName() + " <- " + this.getOperator() + "("
		+ this.getArgument().toString() + ")";
    }

    @Override
    public String getPureName() {
	return this.getName();
    }

    public String getRawPureName() {
	return this.getArgument().getPureName();
    }

    @Override
    public String getSourceReference() {
	return this.argument.getSourceReference();

    }

    @Override
    public void setSourceReference(final String sourceReference) {
	this.argument.setSourceReference(sourceReference);
    }

    /*private Type getAggregateClass (String aggregate, Attribute att) throws TranslationException
     {
     boolean standard = false;
     if (aggregate.equalsIgnoreCase("count"))
     try
     {
     return Types.getType("integer");
     }
     catch (MetaDataException e)
     {
     throw new TranslationException ("Type integer not defined so unable to type count",e);
     }
     if (aggregate.equalsIgnoreCase("max"))
     standard = true;
     else if (aggregate.equalsIgnoreCase("min"))
     standard = true;
     else if (aggregate.equalsIgnoreCase("avg"))
     standard = true;
     else if (aggregate.equalsIgnoreCase("sum"))
     standard = true;
     else
     throw new TranslationException ("Unexpected Aggregate "+aggregate+" found");
     Type type = att.getType();
     if (type.handledByStandardFunctions())
     {
     if (! type.handledByStandardFunctions())
     throw new TranslationException ("Standard Aggregate "+aggregate+" not allowed for type "+type.getName());
     logger.finest("Allowed Aggregate "+aggregate+" on type "+type);
     return type;
     //Others should be added here.
     }
     throw new TranslationException ("Unexpected Aggregate "+aggregate+" found");
     }
     */
    private AggregateType convertType(final String name)
	    throws TranslationException {
	for (final AggregateType type : AggregateType.values()) {
	    if (name.equalsIgnoreCase(type.toString())) {
		return (type);
	    }
	}
	throw new TranslationException("Unexpected Aggregate Type " + name);

    }

    public enum AggregateType {
	COUNT {
	    @Override
	    public String toString() {
		return "Count";
	    }

	    @Override
	    AttributeType getType(final Variable arguement)
		    throws TranslationException {
		try {
		    return Types.getType(Constants.TIME_TYPE);
		} catch (final SchemaMetadataException e) {
		    throw new TranslationException(
			    "Unable to apply Count because attempt to find Integer type gives "
				    + e.toString());
		}
	    }
	},
	SUM {
	    @Override
	    public String toString() {
		return "Sum";
	    }

	    @Override
	    AttributeType getType(final Variable arguement)
		    throws TranslationException {
		final AttributeType type = arguement.getType();
		if (type.handledByStandardFunctions()) {
		    return type;
		} else {
		    throw new TranslationException(
			    "Unable to apply Sum on variable " + arguement);
		}
	    }
	},
	MIN {
	    @Override
	    public String toString() {
		return "Min";
	    }

	    @Override
	    AttributeType getType(final Variable arguement)
		    throws TranslationException {
		final AttributeType type = arguement.getType();
		if (type.handledByStandardFunctions()) {
		    return type;
		} else {
		    throw new TranslationException(
			    "Unable to apply Min on variable " + arguement);
		}

	    }
	},
	MAX {
	    @Override
	    public String toString() {
		return "Max";
	    }

	    @Override
	    AttributeType getType(final Variable arguement)
		    throws TranslationException {
		final AttributeType type = arguement.getType();
		if (type.handledByStandardFunctions()) {
		    return type;
		} else {
		    throw new TranslationException(
			    "Unable to apply Max on variable " + arguement);
		}

	    }
	},
	AVG {
	    @Override
	    public String toString() {
		return "Avg";
	    }

	    @Override
	    AttributeType getType(final Variable arguement)
		    throws TranslationException {
		final AttributeType type = arguement.getType();
		if (type.handledByStandardFunctions()) {
		    return type;
		} else {
		    throw new TranslationException(
			    "Unable to apply Avg on variable " + arguement);
		}

	    }
	};
	@Override
	abstract public String toString();

	abstract AttributeType getType(Variable arguement)
		throws TranslationException;
    }
}
