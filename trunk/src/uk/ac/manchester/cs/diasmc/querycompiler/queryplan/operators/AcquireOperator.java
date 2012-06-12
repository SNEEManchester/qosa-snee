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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators;

//import uk.ac.manchester.cs.diasmc.common.Constants;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.
	querycompiler.codeGeneration.adt.CodeGenerationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SourceMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.IDAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.TimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Stream;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * @author Christian Brenninkmeijer, Ixent Galpin  
 */
public class AcquireOperator extends OperatorImplementation {

	/** Standard Java Logger. */
	private static Logger logger 
		= Logger.getLogger(AcquireOperator.class.getName());

	/** Name as found in the DDL. */
    private String extentName;
    
    /** Name as found in the Query. */
    private String localName;
   
    /** Physical locations for this operator.*/
    private int[] sites;
    
    /** List of attributes to be output. */
    private ArrayList<Attribute> outputAttributes;

    /** 
     * List of the attributes that are acquired by this operator. 
     * Includes sensed attributes as well as time, id 
     * and any control attributes such as EvalTime.
     * Includes attributes required for predicates.
     * All attributes are in the original format 
     * before any expressions are applied.
     */
    private ArrayList<Attribute> acquiredAttributes;

    /** List of attributes to be sensed. */
    private ArrayList<DataAttribute> sensedAttributes;

	/**
	 * The expressions for building the attributes.
	 */
    private ArrayList <Expression> expressions;

    /**
     * Constructs a new Acquire operator.
     * 
     * @param token An AcquireOperator token
     * @param  schemaMetadata The DDL Schema
     */
    public AcquireOperator(final AST token, 
    		final SchemaMetadata schemaMetadata) {
        super();

        this.setOperatorName("ACQUIRE");
        this.setNesCTemplateName("acquire");
        this.setOperatorDataType(OperatorDataType.STREAM);
 
		AST localToken = token.getFirstChild();
		this.extentName = localToken.getText();
		this.localName = localToken.getNextSibling().getText();
        
        addMetaDataInfo(schemaMetadata);
        this.setParamStr(this.extentName + " (" 
        		+ Arrays.toString(sites) + ")");
        
        updateSensedAttributes(); 
	    } 
		 
    /**
     * Old Contructor.
     * @param stream DDL info about a Stream
     * @param acquisitionInterval How often to read data.
     * @deprecated Use Haskell Query parser.
     */
    public AcquireOperator(final Stream stream, final long acquisitionInterval) 
    {      
        super();

        this.setOperatorName("ACQUIRE1");
        this.setNesCTemplateName("acquire");
        this.setOperatorDataType(OperatorDataType.STREAM);
 
		this.extentName = stream.getName();
		this.localName = stream.getName();
        
        addMetaDataInfo(stream.getMetaData());
        updateSensedAttributes();
        this.setParamStr(this.extentName + " (" + Arrays.toString(sites) + ")");
    }

	/**
     * Sets up the attribute based on the schema.
     * 
     * @param schemaMetadata The DDL Schema
     */
    private void addMetaDataInfo(final SchemaMetadata schemaMetadata) {
    	try {
        	SourceMetadata sourceMetaData;
 			sourceMetaData = schemaMetadata.getSourceMetaData(extentName);
		    addMetaDataInfo(sourceMetaData);		
		} catch (Exception e) {
		   	Utils.handleCriticalException(e);
		}
    }
    
    /**
     * Sets up the attribute based on the schema.
     * @param sourceMetaData DDL declaration for this extent.
     */
    private void addMetaDataInfo(final SourceMetadata sourceMetaData) {
    	try {
    		outputAttributes = new ArrayList<Attribute>();
    		outputAttributes.add(new EvalTimeAttribute()); 
    		outputAttributes.add(new TimeAttribute(localName));
    		outputAttributes.add(new IDAttribute(localName));
    		sensedAttributes = new ArrayList<DataAttribute>();
    		LinkedHashMap<String, AttributeType> typeMap 
    			= sourceMetaData.getAttributes();
    		String[] attributeNames = new String[1];
    		attributeNames = typeMap.keySet().toArray(attributeNames);
    		DataAttribute attribute;
    		int numAttributes = typeMap.size();
    		for (int i = 0; i < numAttributes; i++) {
    			if (!(attributeNames[i].equals(Constants.ACQUIRE_TIME) 
    					|| attributeNames[i].equals(Constants.ACQUIRE_ID))) {
    				AttributeType type = typeMap.get(attributeNames[i]);
    				attribute = new DataAttribute(
    							localName, attributeNames[i], type);
    				outputAttributes.add(attribute);
    				sensedAttributes.add(attribute);
    				sites =  sourceMetaData.getSourceNodes();
    			}
    		}
    		copyExpressions(outputAttributes);
    		acquiredAttributes = outputAttributes;
    	} catch (SchemaMetadataException e) {
    		Utils.handleCriticalException(e);
    	}
    }

    /**
     * Constructor that creates a new operator 
     * 		based on a model of an existing operator.
     * 
     * Used by both the clone method and the constuctor of the physical methods.
     * @param model Operator to clone.
     */
    protected AcquireOperator(final AcquireOperator model) {
    	super(model);
    	this.extentName = model.extentName;
    	this.localName = model.localName;
    	this.sites = model.sites;
    	this.outputAttributes = model.outputAttributes;
		this.sensedAttributes = model.sensedAttributes;
		this.expressions = model.expressions;
		this.acquiredAttributes = model.acquiredAttributes;
    }  
        
    /**
     * Returns a string representation of the operator.
     * @return Operator as a String.
     */
 	public final String toString() {
    	//CB: changed as relation now in params
    	//return getText()+"["+relation.toString()+"]";
    	return this.getText();
    }
        	
 	/**
 	 * @return The Sites this acquire will be done on.
 	 */
	public final int[] getSourceSites() {
		return sites;
	}
 		
	//TODO min is predicates added
	/**
	 * Calculated the cardinality based on the requested type. 
	 * 
	 * @param card Type of cardinailty to be considered.
	 * 
	 * @return The Cardinality calulated as requested.
	 */
	public final int getCardinality(final CardinalityType card) {
		if (Settings.MEASUREMENTS_MULTI_ACQUIRE >= 0) {
			return Array.getLength(sites) * Settings.MEASUREMENTS_MULTI_ACQUIRE;			
		} else {
			return Array.getLength(sites);
		}
	}

	/**
	 * Used to determine if the operator is Attribute sensitive.
	 * 
	 * @return false.
	 */
	public final boolean isAttributeSensitive() {
		return false;
	}

	/** {@inheritDoc} */
	public final boolean acceptsPredicates() {
		//logger.warning("Acquire does not yet accept predicates");
		//return true;
		return Settings.LOGICAL_OPTIMIZATION_COMBINE_ACQUIRE_AND_SELECT;
	}
	
	/** {@inheritDoc} */
	public final boolean isLocationSensitive() {
		return true;
	}

	/** {@inheritDoc} */
	public final boolean isRecursive() {
		return false;
	}
	
	/** {@inheritDoc} */
	public final AcquireOperator shallowClone() {
		//TODO: clone relation
		final AcquireOperator clonedOp = new AcquireOperator(this);
		return clonedOp;
	}

	/** 
	 * List of the attribute returned by this operator.
	 * 
	 * @return ArrayList of the returned attributes.
	 */ 
	public final ArrayList<Attribute> getAttributes() {
		return outputAttributes;
	}
	
    /** {@inheritDoc} */    
	public final ArrayList<Expression> getExpressions() {
		return expressions;
	}
	
	/**
	 * Copies attributes into the expressions.
	 * @param attributes Values to set them to.
	 */
    protected final void copyExpressions(
    		final ArrayList<Attribute> attributes) {
		expressions = new ArrayList<Expression>(); 
		expressions.addAll(attributes);
	}

    /** 
     * Updates the sensed Attributes.
     * Extracts the attributes from the expressions.
     * Those that are data attributes become the sensed attributes.
     */	
    private void updateSensedAttributes() {
    	sensedAttributes = new ArrayList<DataAttribute>();
    	for (int i = 0; i < expressions.size(); i++) {
    		//DataAttribute sensed =  sensedAttributes.get(i);
    		Expression expression = expressions.get(i);
    		ArrayList<Attribute> attributes = 
    			expression.getRequiredAttributes();
        	for (int j = 0; j < attributes.size(); j++) {
        		Attribute attribute = attributes.get(j);
        		if (attribute instanceof DataAttribute) {
        			if (!sensedAttributes.contains(attribute)) {
        				sensedAttributes.add((DataAttribute) attribute);
        			}
        		}
        	}
    	}
   		ArrayList<Attribute> attributes = 
   			getPredicate().getRequiredAttributes();
        for (int j = 0; j < attributes.size(); j++) {
        	Attribute attribute = attributes.get(j);
        	if (attribute instanceof DataAttribute) {
        		if (!sensedAttributes.contains(attribute)) {
        			sensedAttributes.add((DataAttribute) attribute);
        		}
        	}
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
    	//if no project to push down Do nothing.
    	if (projectAttributes.size() == 0) {
    		return false;
    	}

    	if (projectExpressions.size() == 0) {
        	//remove unrequired attributes. No expressions to accept
    		for (int i = 0; i < outputAttributes.size(); ) {
    			if (projectAttributes.contains(outputAttributes.get(i)))
    				i++;
    			else {
    				outputAttributes.remove(i);
    				expressions.remove(i);		
    			}
    		}
        	updateSensedAttributes();
    		return false;
    	}

    	expressions = projectExpressions;
    	outputAttributes = projectAttributes;
    	updateSensedAttributes();
    	return true;
	}

	 /** 
	  * {@inheritDoc}
	  * Should never be called as there is always a project or aggregation 
	  * between this operator and the rename operator.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 throw new AssertionError("Unexpected call to pushLocalNameDown()"); 
	 }

	/** {@inheritDoc} */
    public final int getOutputQueueCardinality(final Site node, final DAF daf) {
    	return this.getCardinality(CardinalityType.PHYSICAL_MAX, node, daf);
    }

	/** {@inheritDoc} */
    public final int getOutputQueueCardinality(int numberOfInstances) {
    	assert(numberOfInstances == sites.length);
    	return this.getCardinality(CardinalityType.PHYSICAL_MAX);
    }

    /**
     * The physical maximum size of the output.
     * 
     * Each AcquireOperator on a single site 
     * 		returns exactly 1 tuple per evaluation. 
     *
     * @param card Ignored.
     * @param node Ignored
     * @param daf Ignored
     * @return 1
     */
    public final int getCardinality(final CardinalityType card, 
    		final Site node, final DAF daf) {
		if (Settings.MEASUREMENTS_MULTI_ACQUIRE >= 0) {
			return Settings.MEASUREMENTS_MULTI_ACQUIRE;			
		} else {
			return 1;
		}
    }
       
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		final AlphaBetaExpression result = new AlphaBetaExpression();
		if (Settings.MEASUREMENTS_MULTI_ACQUIRE >= 0) {
			result.addBetaTerm(Settings.MEASUREMENTS_MULTI_ACQUIRE);
		} else {
			result.addBetaTerm(1);
		}
    	return result;
    }
	
	/** {@inheritDoc} */
    private final double getTimeCost() {
		logger.finest("" + CostParameters.getSignalEvent());
		logger.finest("" + CostParameters.getAcquireData());
		return getOverheadTimeCost()
			+ CostParameters.getAcquireData()
			+ CostParameters.getCopyTuple() + CostParameters.getSetAValue()
			+ CostParameters.getApplyPredicate();
    }
    
	/** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		return getTimeCost();
    }

    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances){
		assert(numberOfInstances == sites.length);
		return getTimeCost();		
	}

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
		return new AlphaBetaExpression(getTimeCost(card, node, daf),0);
	}

	/**
     * Some operators do not change the data in any way those could be removed.
     * This operator does change the data so can not be. 
     * 
     * @return False. 
     */
    public final boolean isRemoveable() {
    	return false;
    }

    /**
     * STUB. To be removed.
     * @return Throws and Error.
     */
    public final Collection<Predicate> getPredicates() {
    	throw new AssertionError(
    		"Call to stub AcquireOperator.getPredicates()");
    }

    /**
     * Gets the attributes sensed by this source.
     * This may include attributes needed for a predicate.
     * @return Attributes that this source will sense.
     */
	public final ArrayList<DataAttribute> getSensedAttributes() {
		assert (sensedAttributes != null);
		return sensedAttributes;
	}
	
	/**
	 * Converts an attribute into a reading number.
	 * @param attribute An Expression which must be of subtype Attribute
	 * @return A constent number for this attribute (starting at 1)
	 */
	public final int getSensedAttributeNumber(final Expression attribute) {
		assert (attribute instanceof DataAttribute);
		for (int i = 0; i < sensedAttributes.size(); i++) {
			if (attribute.equals(sensedAttributes.get(i))) {
				return i;
			}
		}
    	Utils.handleCriticalException(new CodeGenerationException(
    			"Unable to find a number for attribute: " 
    			+ attribute.toString()));
		return 1;
	}

	/** 
	 * Gets the number of attributes that are actually sensed.
	 * So excluded time/epoch and (site) id.
	 * @return Number of sensed attributes.
	 */
	public final int getNumSensedAttributes() {
		return sensedAttributes.size();
	}
	
	/** {@inheritDoc} */    
	public final int getDataMemoryCost(final Site node, final DAF daf) {
		return super.defaultGetDataMemoryCost(node, daf);
	}

	/**
	 * Get the list of attributes acquired/ sensed by this operator.
	 * List is before projection is pushed down.
	 * @return list of acquired attributes.
	 */
	public final ArrayList<Attribute> getAcquiredAttributes() {
		assert (acquiredAttributes != null);
		return acquiredAttributes;
	}
 }
