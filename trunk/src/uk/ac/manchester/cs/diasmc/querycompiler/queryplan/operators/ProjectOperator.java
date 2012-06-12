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
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Variable;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * Project Operator.
 * @author Christian Brenninkmeijer, Ixent Galpin and Steven Lynden
 */
public class ProjectOperator extends PredicateOperator {

    
    /**
     * Constructor for old Aggregation Constructor.
     * @deprecated
     */
    protected ProjectOperator() {
    }

    /**
     * Constructs a new Project operator.
     * 
     * @param token A project Operator token
     */
    public ProjectOperator(final AST token) {
        super(token);
       
        this.setOperatorName("Project");
        this.setNesCTemplateName("project");
 
        setOperatorDataType(token);
        
    }  
 
    /**
     * Old Constructor.
     * @deprecated 
     * @param inputOperator Previous operator.
     * @param variables Attributes to included.
     */
    public ProjectOperator(final Operator inputOperator,
    		final Collection<Variable> variables) { 
        super();
        
        this.setOperatorName("Project");
        this.setNesCTemplateName("project");
 
        this.
        setOperatorDataType(inputOperator.getOperatorDataType());
        this.setParamStr("");
        
        ArrayList<Attribute> incoming = inputOperator.getAttributes();
        Variable[] variableArray = new Variable[variables.size()];
        variableArray = variables.toArray(variableArray);
        
        ArrayList <Expression> expressions 
        	= new ArrayList <Expression>(variables.size() + 1);
        ArrayList <Attribute> attributes 
        	= new ArrayList <Attribute>(variables.size() + 1); 
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
        	attributes.add((Attribute) attribute);
        }
        setExpressions(expressions);
        setAttributes(attributes);
        setChildren(new Operator[] {inputOperator});
    	this.setParamStr(expressions.toString());
    }
    
    /** 
     * Sets the operator data type based on the AST token.
     * 
     * @param token AST token for the operator.
     */
    private void setOperatorDataType(final AST token) {
        switch (token.getType()) {
        	case SNEEqlOperatorParserTokenTypes.RELPROJECT: 
        		setOperatorDataType(OperatorDataType.RELATION);
        		return;
        	case SNEEqlOperatorParserTokenTypes.STRPROJECT: 
        		setOperatorDataType(OperatorDataType.STREAM);
        		return;
        	case SNEEqlOperatorParserTokenTypes.WINPROJECT: 
        		setOperatorDataType(OperatorDataType.WINDOWS);
        		return;
        	default: throw new AssertionError("Unexpected AST token"); 
    	}
    }
          
    /**
     * Constructor that creates a new operator
     * based on a model of an existing operator.
     *
     * @param model Another ProjectOperator 
     *   upon which this new one will be cloned.
     */
    protected ProjectOperator(final ProjectOperator model) {
        super(model);
   }
        		
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {

    	if ((projectExpressions.size() == 0) && (projectAttributes.size() > 0)){
    		checkAttributes(projectAttributes);
    	}

    	if (getInput(0).pushProjectionDown(
	  			getExpressions(), getAttributes())) {
	  		    //as previous will now return the attributes.
	  		    copyExpressions(getAttributes());
	  		return true;
	  	} else {
	  		return false;
	  	}
	}

    /**
	 * Calculated the cardinality based on the requested type. 
	 * 
	 * @param card Type of cardinality to be considered.
	 * 
	 * @return The Cardinality calculated as requested.
	 */
	public final int getCardinality(final CardinalityType card) {
		return (this.getInput(0)).getCardinality(card);
	}

	/** {@inheritDoc} */
	public final int getCardinality(final CardinalityType card, 
			final Site node, final DAF daf) {
		return getInputCardinality(card, node, daf, 0);
	}
	
	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		return getInputCardinality(card, node, daf, round, 0);
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
    public final ProjectOperator shallowClone() {
        //TODO: projectList needs to be properly cloned
        final ProjectOperator clonedOp = new ProjectOperator(this);
        return clonedOp;
    }

    private final double getTimeCost(final int tuples) {
		return getOverheadTimeCost()
			+ CostParameters.getCopyTuple() * tuples;
    }

    /** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		final int tuples = this.getInputCardinality(card, node, daf, 0);
		return getTimeCost(tuples);
    }

    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances) {
		final int tuples = this.getInputCardinality(card, 0, numberOfInstances);
		return getTimeCost(tuples);
	}

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
		AlphaBetaExpression result = new AlphaBetaExpression();
		result.addBetaTerm(getOverheadTimeCost());
		AlphaBetaExpression tuples 
			= this.getInputCardinality(card, node, daf, round, 0);
		tuples.multiplyBy(CostParameters.getCopyTuple());
		result.add(tuples);
		return result;
	}
	

   /** {@inheritDoc} */
    public final boolean isRemoveable() {
    	return getExpressions().equals(this.getInput(0).getAttributes());
    }   
    
    /** {@inheritDoc} */
    public final boolean isRecursive() {
        return false;
    }
 

}
