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

import java.util.ArrayList;
import java.util.Collection;

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
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
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operator_rewriting.OperatorFactory;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Variable;

/**
 * Parent for Project, Aggregation and Having Operator.
 * @author Christian Brenninkmeijer, Ixent Galpin
 */
public abstract class PredicateOperator extends OperatorImplementation {

	/**
	 * The expressions for building the attributes.
	 */
    private ArrayList <Expression> expressions;
    
    /**
     * The names of the outgoing attributes.
     */
    private ArrayList <Attribute> attributes;

    /**
     * Old Style Constructor.
     * Sub classes do all the work.
     * @deprecated
     */
    protected PredicateOperator() {
        super();
    }

    /**
     * Constructs a new Project operator.
     * 
     * @param token A project Operator token
     */
    protected PredicateOperator(final AST token) {
        super();
               
        AST expressionsToken = token.getFirstChild();
        AST namesToken = expressionsToken.getNextSibling();
        AST child = namesToken.getNextSibling();
        
        Operator inputOperator 
        	= OperatorFactory.convertAST(child);

        ArrayList<Attribute> incoming = inputOperator.getAttributes();
        convertExpressions(expressionsToken, incoming);
        convertNames(namesToken);
        setChildren(new Operator[] {inputOperator});
    }  
 
    /**
     * Old Constucter.
     * @deprecated 
     * @param inputOperator Previous operator.
     * @param variables Attributes to included.
     */
    public PredicateOperator(final Operator inputOperator,
    		final Collection<Variable> variables) { 
        super();
         
        this.
        setOperatorDataType(inputOperator.getOperatorDataType());
        this.setParamStr("");
        
        ArrayList<Attribute> incoming = inputOperator.getAttributes();
        Variable[] variableArray = new Variable[variables.size()];
        variableArray = variables.toArray(variableArray);
        
        expressions = new ArrayList <Expression>(variables.size() + 1);
        attributes = new ArrayList <Attribute>(variables.size() + 1); 
		try {
			attributes.add(new EvalTimeAttribute());
			expressions.add(new EvalTimeAttribute());
		} catch (SchemaMetadataException e) {	
			Utils.handleCriticalException(e);
		}
       for (int i = 0; i < variableArray.length; i++) {
        	Expression attribute 
        		= ExpressionConvertor.convert(variableArray[i], incoming);
        	expressions.add(attribute);
        	attributes.add((DataAttribute) attribute);
        }
        setChildren(new Operator[] {inputOperator});
    	this.setParamStr(expressions.toString());
    }
        
    /**
     * 
     * @param token An AST Expression token 
     * @param incoming A list of incoming Attributes 
     */
    private void convertExpressions(final AST token, 
    		final ArrayList<Attribute> incoming) {
    	try	{
    		expressions 
    		   = new ArrayList<Expression>(token.getNumberOfChildren() + 1);
    		expressions.add(new EvalTimeAttribute()); 
    		AST expressionToken = token.getFirstChild();
    		while (expressionToken != null) {
    			Expression exp 
    				= ExpressionConvertor.convert(expressionToken, incoming); 
    			expressions.add(exp); 
    			expressionToken = expressionToken.getNextSibling();
    		}
    	} catch (Exception e) {
    		Utils.handleCriticalException(e);
    	}
    	this.setParamStr(expressions.toString());
    }
      
    /**
     * @param token An AST token of Project names. 
     */
    private void convertNames(final AST token) {
    	try	{
    		attributes = new ArrayList<Attribute>(token.getNumberOfChildren());
    		attributes.add(new EvalTimeAttribute());
    		AST nameToken = token.getFirstChild();
    		int count = 1; // Element - is evalTime
    		while (nameToken != null) {
    			DataAttribute attribute 
    				= new DataAttribute(nameToken.getText(),
    						expressions.get(count).getType());
    			attributes.add(attribute);
    			nameToken = nameToken.getNextSibling();
    			count++;
    		}
    	} catch (Exception e) {
    		Utils.handleCriticalException(e);
    	}
    }

    /**
     * Constructor that creates a new operator
     * based on a model of an existing operator.
     *
     * @param model Another ProjectOperator 
     *   upon which this new one will be cloned.
     */
    protected PredicateOperator(final PredicateOperator model) {
        super(model);
        this.expressions = model.expressions;
        this.attributes = model.attributes;
   }

	/**
     * Makes a copy of the operator using a new opCount.
     * @param model Operator to get internal data from.
     * @param newID boolean flag expected to be true. 
     */
   protected PredicateOperator(final PredicateOperator model, 
		   final boolean newID) {
        super(model, newID);
        this.expressions = model.expressions;
        this.attributes = model.attributes;
   }

    /**
     * Constructor that creates a new operator
     * based on a information form a model.
     *
     * @param newExpressions expressions from model.
     * @param newAttributes attributes from model.
     * @param operatorDataType data type from model.
     * @param paramStr paramater string from model.
     */
    protected PredicateOperator(final ArrayList<Expression> newExpressions, 
    		final ArrayList<Attribute> newAttributes, 
    		final OperatorDataType operatorDataType,
    		final String paramStr) {
        super();
        this.expressions = newExpressions;
        this.attributes = newAttributes;
     }
    
	/** 
	 * List of the attribute returned by this operator.
	 * 
	 * @return ArrayList of the returned attributes.
	 */ 
	public final ArrayList<Attribute> getAttributes() {
		return attributes;
	}
    	
	/** 
	 * Gets the required attributes based on the local expressions. 
	 * 
	 * @return List of Attribute refered to by the expressions.
	 * /
	//CB Never used but please do not remove. As need to check why not used.
	private ArrayList<Attribute> getRequiredAttributes() {
		ArrayList<Attribute> required = new ArrayList<Attribute>();
		for (int i = 0; i < expressions.size(); i++) {
			ArrayList<Attribute> required1 
				= expressions.get(i).getRequiredAttributes();  
			for (int j = 0; j < required1.size(); j++) {
				if (!required.contains(required1.get(j))) {
					required.add(required1.get(j));
				}
			}
		}	 
		return required;		
	}
		
    /** {@inheritDoc} */
    public final boolean isLocationSensitive() {
        return false;
    }
 
    /** {@inheritDoc}
     * @return false;
     */
    public final boolean acceptsPredicates() {
        return false;
    }

    /**
     * @return a string representation of this function.
     */
    public final String toString() {
        return this.getText() + "[ " + super.getInput(0).toString() + " ]";
    }
   
    /** {@inheritDoc} */
	public final ArrayList<Expression> getExpressions() {
		return expressions;
	}
    //Call to default methods in OperatorImplementation

	/**
	 * Sets the expressions.
	 * @param newExpressions Values to set them to.
	 */
    protected final void setExpressions(
    		final ArrayList<Expression> newExpressions) {
		this.expressions = newExpressions;
	}

	/**
	 * Copys attributes into the expressions.
	 * @param outputAttributes Values to set them to.
	 */
    protected final void copyExpressions(
    		final ArrayList<Attribute> outputAttributes) {
		expressions = new ArrayList<Expression>(); 
		expressions.addAll(outputAttributes);
	}
    
    /**
     * Sets the Attributes.
     * @param newAttributes Values to set them to.
     */
	public final void setAttributes(
			final ArrayList<Attribute> newAttributes) {
		this.attributes = newAttributes;
	}

    /**
     * Checks if the attributes are in the list and removes those that are not. 
     */
    public final void checkAttributes(final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {

       	//remove unrequired attributes. No expressions to accept
   		for (int i = 0; i < attributes.size(); ) {
   			if (projectAttributes.contains(attributes.get(i)))
   				i++;
   			else {
   				attributes.remove(i);
   				expressions.remove(i);		
   			}
   		}
   	}

	/** {@inheritDoc} */
    public final int[] getSourceSites() {
    	return super.defaultGetSourceSites();
    }
 
	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(final Site node, final DAF daf) {
    	return super.defaultGetOutputQueueCardinality(node, daf);
    }

	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(int numberOfInstances) {
    	return super.defaultGetOutputQueueCardinality(numberOfInstances);
    }

    /** 
	  * {@inheritDoc}
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 for (int i = 0; i < attributes.size(); i++) {
			 attributes.get(i).setLocalName(newLocalName);
		 }
	 }
 
    /** {@inheritDoc} */    
	public final int getDataMemoryCost(final Site node, final DAF daf) {
		return super.defaultGetDataMemoryCost(node, daf);
	}

}
