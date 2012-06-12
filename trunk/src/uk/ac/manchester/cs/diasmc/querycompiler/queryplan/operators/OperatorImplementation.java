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

//import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.graph.NodeImplementation;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.AvroraCostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.ExpressionConvertor;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.NoPredicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.Predicate;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;

/**
 * Base class for all operators. Specific operators
 * extend this class.
 * @author Ixent Galpin ,Christian Brenninkmeijerand Steven Lynden
 */
public abstract class OperatorImplementation extends NodeImplementation
                      implements Operator {

    /**
     * See java.util.logging.Logger.
     */
    private final Logger logger
                = Logger.getLogger(OperatorImplementation.class.getName());

    /**
     * Id to be given to the next Operator to be created.
     */
    private static int opCount = 0;
    
    /**
     * The value that the opCount should revert to if it is reset.
     * /
    private static int opCountResetVal = 0;
    
    /**
     * The name of the operator, e.g., ACQUIRE, JOIN.
     */
    private String operatorName;

    /**
     * The output data type of the operator,
     *  e.g., ALLTUPLES, RELATION, STREAM or WINDOW.
     */
    private OperatorDataType operatorDataType;

    /**
     * The name of the nesC template file.
     */
    private String nesCTemplateName;

    /**
     * String representation of operator parameters.
     */
    private String paramStr;

    /**
     * The fragment to which this operator belongs.
     */
    private Fragment containingFragment = null;

    //IG: TODO:review this
    //CB Appears never to be set.
    /**
     * Partitioning Attributes: The attribute(s) by which data is partitioned.
     */
    private String partitioningAttribute;

	/** 
	 * Predicate that this operator is expected to test data against.
	 */
    private Expression predicate = new NoPredicate();

	/**
     * Makes a clone of the operator without using a new opCount.
     * @param model Operator to get internal data from.
     */
    protected OperatorImplementation(final Operator model) {
        super(model);
        this.operatorDataType = model.getOperatorDataType();
        this.operatorName = model.getOperatorName();
        this.nesCTemplateName = model.getNesCTemplateName();
        this.paramStr = model.getParamStr();
    	this.predicate = model.getPredicate();
    }
        
    /**
     * Constructs a new operator. 
     */
    public OperatorImplementation() {
        /* Assign the operator an automatic ID */
        super(new Integer(opCount).toString());
        opCount++;        
    }

	/**
     * Makes a copy of the operator using a new opCount.
     * @param model Operator to get internal data from.
     * @param newID boolean flag expected to be true. 
     */
    protected OperatorImplementation(final Operator model,
    		final boolean newID) {
        super(new Integer(opCount).toString());
        opCount++;        
        assert (newID);
        this.operatorDataType = model.getOperatorDataType();
        this.operatorName = model.getOperatorName();
        this.nesCTemplateName = model.getNesCTemplateName();
        this.paramStr = model.getParamStr();
    	this.predicate = model.getPredicate();
    }
        
    /**
     * Constructs a new operator, building tree from leaves upwards.
     * 
     * @param children The children of an operator
     */
    // TODO: Change children to arrayList type?
    protected final void setChildren(final Operator[] children) {
        for (Operator element : children) {
            this.addInput(element);
            element.addOutput(this);
        }

        //for now, but probably oversimplifying
        if (children.length > 0) {
            this.partitioningAttribute = children[0].getPartitioningAttribute();
        } else {
        	throw new AssertionError("Unexpected point reached.");
        }
    }
    
    /**
    * @return the fragment to which this operator belongs
    */
    public final Fragment getContainingFragment() {
        return this.containingFragment;
    }
   
    /**
     * @return The Fragment Id.
     */
    public final String getFragID() {
        if (this.containingFragment == null) {
            return null;
        }
        
        return new Integer(this.containingFragment.getID()).toString();
    }
        
    //CB: Used by logicalOptimiser
    /**
     * Returns the input operator at the index
     * specified.
     * @param index Position of the operator to be returned.
     * @return The child operator with this index.
     */
    public final Operator getInput(final int index) {
   		return (Operator) super.getInput(index);
    }
    
    /** 
     * Gets the operator to which this operator send data.
     * @param index Position of the operator to be returned.
     * @return The parent operator with this index.
     */
    public final Operator getOutput(final int index) {
        return (Operator) super.getOutput(index);
    }
    
    /** 
     * @return The partioning Attribute which appears never to be set.
     */
    public final String getPartitioningAttribute() {
        return this.partitioningAttribute;
    }
       
    //CB: For debuging prints out info in this operator and its children
    //CB: IXENT feel free to add output here
    /**
     * Returns a String representation of the operator. This is used
     * for debugging.
     * 
     * @return A string representation of the operator including its children.
     */
    public abstract String toString();
    
    
    //CB: Used by TreeDisplay and toString for debugging
    //CB: IXENT feel free to add output here
    //CB: Should not include children use to string for that.
    /**
     * Gets a description of this operator only.
     * 
     * @param showProperties If set to true the text 
     *    includes property information such as cardinality.
     *     
     * @return A string representation of this operator but not its choldren
     */
    public final String getText(final boolean showProperties) {
        final StringBuffer s = new StringBuffer();
        s.append(this.getOperatorDataType());
        s.append(this.getOperatorName());
        if (this.getParamStr() != null) {
            s.append(" (");
            s.append(this.getParamStr());
            s.append(" )");
        }
        
        if (showProperties) {
            s.append(Constants.NEWLINE 	+ " - card: " 
            	 + (new Long(this.getCardinality(
            			 CardinalityType.MAX)).toString()));
        }
        return s.toString();
    }
    
     /**
      * Default getText including properties.
      * 
      * @return A string representation of this operator but not its choldren
      */
     public final String getText() {
        return this.getText(Settings.DISPLAY_OPERATOR_PROPERTIES);
    }
    
    /**
     * @return The output operator at index 0.
     */
    public final Operator getParent() {
        return this.getOutput(0);
    }
    
    /** 
     * Detects if this operator is the root of a fragment.
     * @return True if this operator is the root of a fragment.
     */
    public final boolean isFragmentRoot() {
        if (this instanceof DeliverOperator) {
            return true;
        }
        if (this.getParent() instanceof ExchangeOperator) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Sets the containing fragment of this operator.
     * @param f the containing fragment
     */
    public final void setContainingFragment(final Fragment f) {
        if (!(this instanceof ExchangeOperator)) {
            this.containingFragment = f;
        } else {
            System.err.println("Exchange operator cannot belong to fragment");
       }
    }

     /**
     * Iterator to traverse the immediate children of the current operator.
     * @return An iterator over the children
     */
    public final Iterator<Operator> childOperatorIterator() {
        
        final ArrayList<Operator> opList = new ArrayList<Operator>();
        
        for (int n = 0; n < this.getInDegree(); n++) {
            final Operator op = this.getInput(n);
            opList.add(op);
        }
                
        return opList.iterator();
    }    

   /**
     * Sets the opCount reset value the current value of opCount.
     * /
    public static void setOpCountResetVal() {
    	opCountResetVal = opCount;
    }

    /**
     * Merges two arrays of int (or sites).
     * With duplicates removed.
     * @param bigger The larger of the two arrays 
     * @param smaller The smaller of the two arrays.
     * @return A single array with all values of both 
     * sorted and without duplicates.
     */
    private int[] mergeIntArrays(final int[] bigger, final int[]smaller) {
        if (smaller.length > bigger.length) {
            return this.mergeIntArrays(smaller, bigger);
        }
        int size = bigger.length + smaller.length;
        int i;
        for (i = 0; i < smaller.length; i++) {
            if (Arrays.binarySearch(bigger, smaller[i]) >= 0) {
                size--;
            }
        }
        if (size != bigger.length) {
            final int[] result = new int[size];
            for (i = 0; i < bigger.length; i++) {
                result[i] = bigger[i];
            }
            for (int element : smaller) {
                if (Arrays.binarySearch(bigger, element) < 0) {
                    result[i] = element;
                    i++;
                }
            }
            this.logger.finest("result = " + Arrays.toString(result));
            Arrays.sort(result);
            return result;
        } else {
            return bigger;
        }
    }    
    
    /**
     * Get the source node for the operator by getting the source nodes of
     * it children.
     * 
     * @return Sorted list of nodes that provide data for this operator.
     */
    public final int[] defaultGetSourceSites() {
        final Iterator<Operator> ops = this.childOperatorIterator();
        int[] result = ops.next().getSourceSites();
        while (ops.hasNext()) {
            result =  this.mergeIntArrays(result, ops.next().getSourceSites());
        }
        return result;
    }
           
    /** {@inheritDoc} */
     public final void setPredicates(final Collection<Predicate> predicates) {
        assert (acceptsPredicates());
        ArrayList<Attribute> attributes = this.getAttributes();
        Predicate[] predicateArray = new Predicate[predicates.size()];
        predicateArray = predicates.toArray(predicateArray); 
    	setPredicate(ExpressionConvertor.convert(predicateArray, attributes));
    }
        
    /**
     * Obtains the input cardinality from all incoming sites.
     * For exchanges this is the producer cardinality
     * @param card Type of cardinality to be used.
     * @param node Site for which incoming should be calculated
     * @param daf DAF this operator is part of
     * @param index Index of incoming operator. (0 unless a join)
     * @return The total physicalMaxCardinality for all sites 
     * that feed into node.
     */
   public final int getInputCardinality(final CardinalityType card, 
    		final Site node, final DAF daf, final int index) {
   		int total = 0;
   		final Operator inputOp = this.getInput(index);
   		Iterator<Site> inputs;
   		
   		inputs = daf.getInputOperatorInstanceSites(this, node, index);
   		while (inputs.hasNext()) {
   			total = total
		    	+ inputOp.getCardinality(card, inputs.next(), daf);
   		}
   		return total;
    }

  /**
    * Obtains the input cardinality from one incoming sites.
    * For exchanges this is the producer cardinality
    *
    * WARNING: ASSUMES THAT ALL INSTANCES ARE DISTRIBUTED EVENLY!
    * 
    * @param card Type of Cardinality to be used to calculate cost.
    * @param index Index of incoming operator. (0 unless a join)
    * @param numberOfInstances Number of instances if this operator in the query plan.
    * @return The cardinality for all sites that feed into one node.
    */
  public final int getInputCardinality(final CardinalityType card, 
		  final int index, final int numberOfInstances) {
  		final Operator inputOp = this.getInput(index);
  		return inputOp.getCardinality(card)/numberOfInstances;
   }

   /** 
    * Provides the general overhead of running an operator.
    * Only includes cost that are the same for all operators, 
    * and any number of tuples (zero or more).
    * 
    * Current includes cost off calling method and signaling result ready. 
    * 
    * @return Overhead costs.
    */
	protected final double getOverheadTimeCost() {
		return CostParameters.getCallMethod()
				+ CostParameters.getSignalEvent();
	}
	
	/**
    * Obtains the input cardinality from all incoming sites.
     * For exchanges this is the producer cardinality
    * @param card Type of cardinality to be used.
    * @param node Site for which incoming should be calculated
    * @param daf DAF this operator is part of
    * @param round Defines if rounding reserves should be included or not
    * @param index Index of incoming operator. (0 unless a join)
    * @return The total physicalMaxCardinality for all sites 
    * that feed into node.
    */
  public final AlphaBetaExpression getInputCardinality(
		  final CardinalityType card,	final Site node, final DAF daf, 
		  final boolean round, final int index) {
	    AlphaBetaExpression total = new AlphaBetaExpression();
  		final Operator inputOp = this.getInput(index);
  		Iterator<Site> inputs;
  		
  		inputs = daf.getInputOperatorInstanceSites(this, node, index);
  		while (inputs.hasNext()) {
  			total.add(inputOp.getCardinality(card, inputs.next(), daf, round));
  		}
  		return total;
   }

    /**
     * The physical size of the tuple including any control information.
     * 
     * This takes into consideration that an attribute of size X
     * must start at a location where location mod x = 0.
     * The total size must also be a multiple of the largest attribute.
     * 
     * @return The size of the tuple
     */
    public final int getPhysicalTupleSize() {
        final ArrayList<Attribute> attributes = this.getAttributes();
        int totalSize = 0;
        int blockSize = 0;
        for (int i = 0; i < attributes.size(); i++) {
            final int size = attributes.get(i).getType().getSize();
            //keep track of largest object
            if (size > blockSize) {
                blockSize = size;
            }
            //make sure object starts on a multiple of itself
            if ((totalSize % size) > 0) {
                totalSize = totalSize + size - (totalSize % size);
            }
            //add object
            totalSize = totalSize + size;
        }
        //fill to a multiple of the size of largest object
        if ((totalSize % blockSize) > 0) {
            totalSize = totalSize + blockSize - (totalSize % blockSize);
        }
        return totalSize;
    }    

   /** {@inheritDoc} */
    public abstract OperatorImplementation shallowClone();

    /** {@inheritDoc} */
    public final String getNesCTemplateName() {
        return this.nesCTemplateName;
    }

    /**
     * @param newNesCTemplateName new value for nesCTemplateName 
     */
    protected final void setNesCTemplateName(
    		final String newNesCTemplateName) {
        this.nesCTemplateName = newNesCTemplateName;
    }
    
    /** {@inheritDoc} */
    public final OperatorDataType getOperatorDataType() {
    	assert (operatorDataType != null);
        return this.operatorDataType;
    }
    
    /**
     * @param newOperatorDataType New value.
     */
    protected final void setOperatorDataType(
    		final OperatorDataType newOperatorDataType) {
    	assert (newOperatorDataType != null);
        this.operatorDataType = newOperatorDataType;
    }

    /** {@inheritDoc} */    
    public final String getOperatorName() {
        return this.operatorName;
    }

    /**
     * @param newOperatorName new value.
     */
    protected final void setOperatorName(final String newOperatorName) {
         this.operatorName = newOperatorName;
    }

    /** {@inheritDoc} */    
    public final String getParamStr() {
        return this.paramStr;
    }

    /**
     * @param newParamStr new Value
     */
    protected final void setParamStr(final String newParamStr) {
        this.paramStr = newParamStr;
    }    
 
    /** {@inheritDoc} */    
    public final String getTupleAttributesStr(final int maxPerLine) {
        final ArrayList<Attribute> attributes = getAttributes();
        	
        final StringBuffer strBuff = new StringBuffer();
        strBuff.append("(");
        for (int i = 0; i < attributes.size(); i++) {
            //commas
            if (i > 0) {
                strBuff.append(", ");
                if ((i % maxPerLine == 0) && (i - 1) < attributes.size()) {  
                    strBuff.append("\\n");
                }
            }
            strBuff.append(attributes.get(i));
            strBuff.append(":");
            assert (attributes.get(i) != null);
            assert (attributes.get(i).getType() != null);            
            strBuff.append(Utils.capFirstLetter(
            		attributes.get(i).getType().getName()));

        }
        strBuff.append(")");
        return strBuff.toString();
    }
 
    /** {@inheritDoc} */    
    public final int defaultGetOutputQueueCardinality(
    		final Site node, final DAF daf) {
    	return this.getCardinality(CardinalityType.PHYSICAL_MAX, node, daf);
    }
        
    /** {@inheritDoc} */    
    public final int defaultGetOutputQueueCardinality(int numberOfInstances) {
    	return this.getCardinality(CardinalityType.PHYSICAL_MAX) / numberOfInstances;
    }

    /** 
	 * List of the attribute returned by this operator.
	 * 
	 * @return ArrayList of the returned attributes.
	 */
	public final ArrayList<Attribute> defaultGetAttributes() {
    	return (this.getInput(0)).getAttributes();		
	}
	
    /** {@inheritDoc} */    
	public final ArrayList<Expression> defaultGetExpressions() {
		ArrayList<Expression> expressions = new ArrayList<Expression>(); 
		expressions.addAll(getAttributes());
		return expressions;
	}
	
   /** 
     * Calculates the physical size of the state of this operator.
     * 
     * Default implmentation assumes the cost is 
     * the physical tuple size multiplied by the size of the output queue.
     * 
     * Does not included the size of the input 
     * as these are assumed passed by reference.
     *
     * Does not include the size of the code itself.
     * 
     * @param node Physical mote on which this operator has been placed.
     * @param daf Distributed query plan this operator is part of.
     * @return OutputQueueCardinality * PhytsicalTuplesSize
     */
    public final int defaultGetDataMemoryCost(final Site node, final DAF daf) {
    	logger.finest("Memory for OP"+ this);
    	final int size = this.getPhysicalTupleSize();
    	logger.finest("Tuple Size: " + size);
    	final int card = this.getOutputQueueCardinality(node, daf);
    	logger.finest("OutputQueueCardinality = "+card);
    	final int memoryCost = size * card;
    	logger.finest("Memory Cost = "+memoryCost);
		return memoryCost;
	    }

    /** 
     * Calculates the physical size of the state of this operator.
     * 
     * Does not included the size of the input 
     * as these are assumed passed by reference.
     *
     * Does not include the size of the code itself.
     * 
     * WARNING: ASSUMES THAT ALL INSTANCES ARE DISTRIBUTED EVENLY!
     * 
     * @param numberOfInstances Number of instances if this operator in the query plan.
     * Unless numberOfInstance = number of source sites or 1 the correctness 
     * 	of this method depends on instances being perfectly distributed.
     * @return Estimated number of bytes of RAM used by this operator.
     */
	public int getDataMemoryCost(int numberOfInstances){
    	logger.finest("Memory for OP"+ this);
    	final int size = this.getPhysicalTupleSize();
    	logger.finest("Tuple Size: " + size);
    	final int card = this.getOutputQueueCardinality(numberOfInstances);
    	logger.finest("OutputQueueCardinality = "+card);
    	final int memoryCost = size * card;
    	logger.finest("Memory Cost = "+memoryCost);
		return memoryCost;
	}

    
    /** {@inheritDoc} */    
    public final Expression getPredicate() {
    	return predicate;
    }

    /** {@inheritDoc} */    
    public final void setPredicate(final Expression newPredicate) {
    	if (this.acceptsPredicates()) {
    		this.predicate = newPredicate;
    		this.paramStr = paramStr + predicate.toString();
    	} else {
    		throw new AssertionError("Illegal call.");
    	}
    }

    /**
     * Displays the results of the cost functions.
     * Stub returns -1 until developed..
     * @param node Physical mote on which this operator has been placed.
     * @param daf Distributed query plan this operator is part of.
	 * @return the calculated time
     */
	public double getTimeCost2(Site node, DAF daf) {
		return -1;
	}

    /** {@inheritDoc} */    
	public double getEnergyCost(CardinalityType card, int numberOfInstances) {
		double milliSeconds = getTimeCost(card, numberOfInstances);
		double marginal = AvroraCostParameters.CPUACTIVEAMPERE - AvroraCostParameters.CPUPOWERSAVEAMPERE;
		return milliSeconds * marginal;
	}

	/**
     * Displays the results of the cost functions.
     * Underlying Methods are still under development.
     * @param node Physical mote on which this operator has been placed.
     * @param daf Distributed query plan this operator is part of.
     * @return OutputQueueCardinality * PhytsicalTuplesSize
     */
	public double getEnergyCost2(Site node, DAF daf) {
		return -1;
	}

	/**
     * Displays the results of the cost functions.
     * Underlying Methods are still under development.
     * @param node Physical mote on which this operator has been placed.
     * @param daf Distributed query plan this operator is part of.
     * @return OutputQueueCardinality * PhytsicalTuplesSize
     */
	public int getDataMemoryCost2(Site node, DAF daf) {
		return -1;
	}

}

