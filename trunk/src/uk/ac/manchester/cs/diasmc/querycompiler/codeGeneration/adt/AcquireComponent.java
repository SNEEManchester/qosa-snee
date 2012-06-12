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
package uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.NesCGeneration;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.QueryPlan;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.IDAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.IntLiteral;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.MultiExpression;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.NoPredicate;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.TimeAttribute;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.AcquireOperator;

/**
 * Build the Nesc Component for an Acquire operator.
 * @author Ixent, Christian
 *
 */
public class AcquireComponent extends NesCComponent implements
	TinyOS1Component, TinyOS2Component {

	/** Operator class code is being build for. */
    private AcquireOperator op;

    /** Plan this Operator is in. */
    private QueryPlan plan;

    /** No longer required. */
    QoSSpec qos;

    /** No really required. */
    Fragment frag;

    public AcquireComponent(final AcquireOperator op, final QueryPlan plan,
	    final QoSSpec qos, final NesCConfiguration fragConfig,
	    int tosVersion, boolean tossimFlag) {
		super(fragConfig, tosVersion, tossimFlag);
		this.op = op;
		this.frag = op.getContainingFragment();
		this.plan = plan;
		this.qos = qos;
		this.id = CodeGenUtils.generateOperatorInstanceName(op, this.frag,
			this.site, tosVersion);
    }

    @Override
    public String toString() {
	return this.getID();
    }

    public void writeNesCFile(final String outputDir)
	    throws IOException, CodeGenerationException {

	final HashMap<String, String> replacements = new HashMap<String, String>();
	replacements.put("__MODULE_NAME__", this.getID());
	replacements.put("__OPERATOR_DESCRIPTION__", this.op.getText(false)
		.replace("\"", ""));
	replacements.put("__HEADER__", this.configuration
		.generateModuleHeader(this.getID()));
	replacements.put("__OUTPUT_TUPLE_TYPE__", CodeGenUtils
		.generateOutputTupleType(this.op));
	replacements.put("__OUT_QUEUE_CARD__", new Long(
		 this.op.getOutputQueueCardinality(
			(Site) this.plan.getRoutingTree().getNode(
				this.site.getID()), this.plan.getDAF())).toString());
    replacements.put("__FULL_ACQUIRE_PREDICATES__", 
    		getNescText(op.getPredicate()));
    replacements.put("__ACQUIRE_PREDICATES__", CodeGenUtils.getNescText(
    		op.getPredicate(), "", null, op.getAcquiredAttributes(), null));

	this.doTupleConstruction(replacements);

	final String outputFileName =
		generateNesCOutputFileName(outputDir, this.getID());
	
	if (Settings.MEASUREMENTS_REMOVE_OPERATORS.contains("acquire") 
			|| Settings.MEASUREMENTS_REMOVE_OPERATORS.contains("everything")) {
		this.doGetEmptyDataMethods(replacements);
		writeNesCFile(NesCGeneration.NESC_MODULES_DIR + "/measurements/empty_acquire.nc",
				outputFileName, replacements);		
	} else if (Settings.MEASUREMENTS_MULTI_ACQUIRE >= 0) {
		this.doGetDataMethods(replacements);
		writeNesCFile(NesCGeneration.NESC_MODULES_DIR + "/measurements/multi_acquire.nc",
				outputFileName, replacements);		
	} else {
		this.doGetDataMethods(replacements);
		writeNesCFile(NesCGeneration.NESC_MODULES_DIR + "/acquire.nc",
				outputFileName, replacements);
	}
    }

    /**
     * Writes the methods to actually acquire the data.
     * 
     * @param replacements Values to be replaced in the tamplates
     */
    private void doGetDataMethods(final HashMap<String, String> replacements) {
    	final ArrayList<DataAttribute> sensedAttribs = op.getSensedAttributes();
    	final StringBuffer getDataBuff = new StringBuffer();
    	final StringBuffer declsBuff = new StringBuffer();
    	for (int i = sensedAttribs.size()-1; i >= 0; i--) {
    		declsBuff.append("\tuint16_t reading" + i + ";\n");
    		declsBuff.append("\tbool acquiring" + i + " = FALSE;\n");
    		if (i > 0) {
    			getDataBuff.append("\tvoid task getReading" + i + "Task()\n");
    			getDataBuff.append("\t{\n");
    			getDataBuff.append("\t\tatomic\n");    		    		
    			getDataBuff.append("\t\t\t{\n");
    			getDataBuff.append("\t\t\tacquiring" + i 
    					+ " = TRUE;\n");    		
    			getDataBuff.append("\t\t\t}\n");
    			if (tosVersion==1) {
    				getDataBuff.append("\t\tcall ADC" + i + ".getData();\n");	
    			} else {
    				getDataBuff.append("\t\tcall Read"+ i + ".read();\n");
    			}
    			
    			getDataBuff.append("\t}\n\n");
    		}
    		
    		if (tosVersion==1) {
	    	    getDataBuff.append("\tasync event result_t ADC" + i
	    		    + ".dataReady(uint16_t data)\n");
    		} else {
	    	    getDataBuff.append("\tevent void Read" + i + 
	    	    	".readDone(error_t result, uint16_t data)\n");
	    	}
    	    getDataBuff.append("\t{\n");
    	    getDataBuff.append("\t\tif (acquiring" + i + ") {\n");
    	    getDataBuff.append("\t\t\tatomic\n");
    	    getDataBuff.append("\t\t\t{\n");
    	    getDataBuff.append("\t\t\t\tacquiring" + i + " = FALSE;\n");    	    
    	    getDataBuff.append("\t\t\t\treading" + i + " = data;\n");
     	    final String attribName = 
     	    	CodeGenUtils.getNescAttrName(sensedAttribs.get(i));
    	    String padding = "";

    	    //makes all the sensor readings be nicely aligned
    	    if (attribName.length() < 16) {
    	    	padding = Utils.pad(" ", 16 - attribName.length());
    	    }

    	    if (tosVersion==1) {
	    	    getDataBuff.append("\t\t\t\tdbg(DBG_USR1,\"ACQUIRE: " + attribName
	    		    + padding + "  %d at epoch %d\\n\",reading" + i
	    		    + ",currentEvalEpoch);\n");
    	    }
    	    getDataBuff.append("\t\t\t}\n");

    	    if (i + 1 < sensedAttribs.size()) {
    	    	getDataBuff.append("\t\t\tpost getReading" + (i + 1) + "Task();\n");
    	    } else {
    	    	getDataBuff.append("\t\t\tpost constructTupleTask();\n");
    	    }
    	    getDataBuff.append("\t\t}\n");
    	    if (tosVersion==1) {
    	    	getDataBuff.append("\t\treturn SUCCESS;\n");
    	    }
    	    getDataBuff.append("\t}\n\n");
    	}
    	replacements.put("__GET_DATA_METHODS__", getDataBuff.toString());
    	replacements.put("__READING_VAR_DECLS__", declsBuff.toString());
    }

    /**
     * Writes the methods to actually acquire the data.
     * 
     * @param replacements Values to be replaced in the tamplates
     */
    private void doGetEmptyDataMethods(final HashMap<String, String> replacements) {
    	final ArrayList<DataAttribute> sensedAttribs = op.getSensedAttributes();
    	final StringBuffer getDataBuff = new StringBuffer();
    	for (int i = sensedAttribs.size() - 1; i >= 0; i--) {
    	    getDataBuff.append("\tasync event result_t ADC" + i
    		    + ".dataReady(uint16_t data)\n");
    	    getDataBuff.append("\t{\n");
    	    getDataBuff.append("\t\treturn SUCCESS;\n");
    	    getDataBuff.append("\t}\n\n");
    	}
    	replacements.put("__GET_DATA_METHODS__", getDataBuff.toString());
    }

    /**
     * Generates the text for tuple construction.
     * @param replacements Values to be replaced in the tamplates
     * @throws CodeGenerationException Error if Attribute not acquired.
     */
    private void doTupleConstruction(final HashMap<String, 
    		String> replacements) throws CodeGenerationException {
    	final StringBuffer tupleConstructionBuff = new StringBuffer();
    	final ArrayList <Expression> expressions = op.getExpressions();
    	final ArrayList <Attribute> attributes = op.getAttributes();
    	for (int i = 0; i < expressions.size(); i++) {
    		Expression expression = expressions.get(i);
    		String attrName = CodeGenUtils.getNescAttrName(attributes.get(i));
    		//CB for validating attributes with ignore name are not written.
    		if (Settings.MEASUREMENTS_IGNORE_IN.contains("acquire") && attrName.contains("ignore")) {
   				tupleConstructionBuff.append(
					"\t\t\t\t//SKIPPING outQueue[outTail]." 
   					+ attrName + "=" + getNescText(expression) + ";\n");
    		} if (Settings.MEASUREMENTS_MULTI_ACQUIRE > 0) { 
				tupleConstructionBuff.append("\t\t\t\toutQueue[outTail]." 
	        			+ attrName + "=" + getNescText(expression) + "+ outTail;\n");
    		}
    		else {
				tupleConstructionBuff.append("\t\t\t\toutQueue[outTail]." 
        			+ attrName + "=" + getNescText(expression) + ";\n");
			}
    	}
    	replacements.put("__CONSTRUCT_TUPLE__", tupleConstructionBuff
    			.toString());
    }
    
    /**
     * Generates nescText based on ReadingX.
     * @param expression Expression to get nesc call for.
     * @return The text to be used in the Nesc code.
     * @throws CodeGenerationException
     * 
     * See also CodeGenUtils.getNescTExt
     */
	private String getNescText(final Expression expression)
			throws CodeGenerationException {
	    if (expression instanceof EvalTimeAttribute) {
	    	return "currentEvalEpoch";
	    }
	    if (expression instanceof TimeAttribute) {
	    	return "currentEvalEpoch";
	    }
	    if (expression instanceof IDAttribute) {
	    	if (tosVersion == 1) {
	    		return "TOS_LOCAL_ADDRESS";
	    	} else {
	    		return "TOS_NODE_ID;";
	    	}
	    }
		if (expression instanceof DataAttribute) {
			return "reading" + op.getSensedAttributeNumber(expression); 
		}	
		if (expression instanceof MultiExpression) {
			MultiExpression multi = (MultiExpression) expression;
			Expression[] expressions = multi.getExpressions(); 
			String output = "(" + getNescText(expressions[0]);
			for (int i = 1; i < expressions.length; i++) {
				output = output + multi.getMultiType().getNesC() 
					+ getNescText(expressions[i]);
			}
			return output + ")";
		}
		if (expression instanceof NoPredicate) {
			return "TRUE";
		}
		if (expression instanceof IntLiteral) {
			return expression.toString();
		}		
		throw new CodeGenerationException("Missing code. Expression "
			+ expression);	
	}
}
