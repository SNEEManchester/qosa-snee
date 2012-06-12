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

import antlr.collections.AST;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.
	querycompiler.metadata.costmodels.CostParameters;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.DAF;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParserTokenTypes;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.operator_rewriting.OperatorFactory;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.RowWindow;
import uk.ac.manchester.cs.diasmc.
	querycompiler.translation_rewriting.translation.TimeWindow;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaExpression;
import uk.ac.manchester.cs.diasmc.
	querycompiler.whenScheduling.qosaware.cvx.AlphaBetaTerm;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSException;

/**
 * Window Operator.
 * @author Christian Brenninkmeijer, Ixent Galpin and Steven Lynden 
*/
public class WindowOperator extends OperatorImplementation {
  
	/** Number of rows or tick relative to now to start window at. */
	private int from;
	/** Number of rows or tick relative to now to start window at. */
    private int to;
    /** If true from and to are in ticks otherwise in rows. */
    private boolean timeScope;
    /** The timeSlide to use if any. */
    private int timeSlide = 0;
    /** The rowSlide to use if any. */
    private int rowSlide = 0;
    /** Acquistion interval of the whole query. (Alpha)*/
    private double acInt;
    
	/**
     * Constructs a new Window operator 
     * that slides every time input is received.
     * 
     * @param token An TIMEWINDOW, ROWWINDOW or INPUTWINDOW token
     */
    public WindowOperator(final AST token) {
        super();

        setOperatorName("WINDOW");
        setNesCTemplateName("window");
        setOperatorDataType(OperatorDataType.WINDOWS);
        convertScopeDef(token.getFirstChild());
        
        AST child = convertSlide(token);
        
        Operator inputOperator 
           = OperatorFactory.convertAST(child);       
        setChildren(new Operator[] {inputOperator});
    }  
    
    /**
     * 
     * @param inputOperator Child operator
     * @param window Representation of row window values.
     * @deprecated
     */
    public WindowOperator(final Operator inputOperator, 
    		final RowWindow window) {
        super();

        setOperatorName("WINDOW");
        setNesCTemplateName("Window");
        setOperatorDataType(OperatorDataType.WINDOWS);

        this.from = window.getFrom();
        this.to = window.getTo();
        this.timeScope = false;
        if (window.isRowSlide()) {
        	this.rowSlide = (int) window.getSlide();
        } else {
        	this.timeSlide = (int) window.getSlide();
        }
                
        recordScopeDef();
        setChildren(new Operator[] {inputOperator});
    }

    /*
     * @deprecated
     */
    /**
     * Old Constructor.
     * @param inputOperator Previous Operator
     * @param window Parameters for the windows
     * @param newEvalRate Query window evaluation rate.
     */
    public WindowOperator(final Operator inputOperator, final TimeWindow window,
    		final double newEvalRate) {
        super();

        setOperatorName("WINDOW");
        setNesCTemplateName("Window");
        setOperatorDataType(OperatorDataType.WINDOWS);

        this.from = window.getFrom();
        this.to = window.getTo();
        this.timeScope = true;
        this.rowSlide = 0;
       	this.timeSlide = (int) window.getSlide();
        this.acInt = newEvalRate;        
       	
        recordScopeDef();
        setChildren(new Operator[] {inputOperator});
    }
    /**
     * Converts the scope defintion of the window.
     * 
     * @param token ROWSCOPEDEF or TIMESCOPEDEF token
     */
    private void convertScopeDef(final AST token) {
    	if (token.getType() == SNEEqlOperatorParserTokenTypes.TIMESCOPEDEF) {
    		timeScope = true;
    	} else if (token.getType() 
    			== SNEEqlOperatorParserTokenTypes.ROWSCOPEDEF) {
    		timeScope = false;
    	} else {
        	Utils.handleCriticalException(new Exception(
        		"Unexpected Token in convertScopeDef. Found " 
       			+ token.getText()));
    	}	
        from = -Integer.parseInt(token.getFirstChild().getText());
        to = -Integer.parseInt(token.getFirstChild().
        		getNextSibling().getText());
        recordScopeDef();
    }    
    /**
     * Records the scope defintion of the window.
     */
    private void recordScopeDef() {
    	StringBuffer buf = new StringBuffer();
    	if (timeScope) {
    		buf.append("TimeScope (");
    	} else {
    		buf.append("RowScope (");
    	}
        buf.append(from);
        buf.append(",");
        buf.append(to);
        buf.append(")");
        if (rowSlide > 0) {
        	buf.append(" With RowSlide: " + rowSlide);
        }
        if (timeSlide > 0) {
        	buf.append(" With Timeslide: " + timeSlide);
        }
        	
        setParamStr(buf.toString());
    }

    /**
     * Converts the slide token to rowSlide, timeSlide or neither.
     * 
     * @param token An TIMEWINDOW, ROWWINDOW or INPUTWINDOW token
     *
     * @return token for the child operator.
     */
    private AST convertSlide(final AST token) {
        if (token.getType() == SNEEqlOperatorParserTokenTypes.TIMEWINDOW) {
          AST slideToken = token.getFirstChild().getNextSibling();
          timeSlide = Integer.parseInt(token.getFirstChild().getText());
          setParamStr(getParamStr() + "(TimeSlide" + timeSlide + ")");
          return slideToken.getNextSibling();
        }
        if (token.getType() == SNEEqlOperatorParserTokenTypes.ROWWINDOW) {
            AST slideToken = token.getFirstChild().getNextSibling();
            System.out.println(slideToken);
            rowSlide = Integer.parseInt(slideToken.getText());
            setParamStr(getParamStr() + "(RowSlide " + rowSlide + ")");
            return slideToken.getNextSibling();
        }
        if (token.getType() == SNEEqlOperatorParserTokenTypes.INPUTWINDOW) {
            return token.getFirstChild().getNextSibling();
        }
    	Utils.handleCriticalException(new Exception(
        		"Unexpected Token in convertSlide. Found " 
       			+ token.getText()));
    	return null;
	}

    /**
     * @return From Value which could be in ticks or rows.
     */
    public final int getFrom() {
    	return this.from;
    }
    
    /**
     * @return To Value which could be in ticks or rows.
     */
    public final int getTo() {
    	return this.to;
    }
    
    //used by clone method
    /**
     * Constructor that creates a new operator 
     * based on a model of an existing operator.
     * 
     * Used by both the clone method and the constuctor of the physical methods.
     * @param model Operator to get values from.
     */
    protected WindowOperator(final WindowOperator model) {
    	super(model);
 
        this.from = model.from;
    	this.to = model.to;
    	this.timeScope = model.timeScope;
    	this.timeSlide = model.timeSlide;
        this.rowSlide = model.rowSlide;
        this.acInt = model.acInt;
    }      
       
	/**
	 * Calculated the cardinality based on the requested type. 
	 * 
	 * @param card Type of cardinailty to be considered.
	 * 
	 * @return The Cardinality calulated as requested.
	 */
	public final int getCardinality(final CardinalityType card)	{
		if (timeScope) {
			final int evaluationsPerWindow 
				= (int) Math.ceil((-this.from + this.to + 1) / acInt);
			return Math.round((this.getInput(0)).getCardinality(card) 
					* evaluationsPerWindow);			
		} else {
			return -this.from + this.to + 1;			
		}
	}
   
    /** {@inheritDoc} */
    public final int getCardinality(final CardinalityType card, 
    		final Site node, final DAF daf) {
    	int cardinality;
		if (timeScope) {
			//evaluation covered by from 
			final int fromEval = (int) Math.ceil((-this.from + 1) / acInt);
			if (fromEval == 0) {
				Utils.handleCriticalException(new QoSException(
					"QoS Query mismatch results in empty timeWindow. From: " 
					+ this.from + " does not cover any evaluations."));
			}
			
			//less evaluation excluded by to
			final int toEval = (int) Math.ceil(-this.to / acInt); 
			if (toEval >= fromEval) {
				Utils.handleCriticalException(new QoSException(
						"QoS Query mismatch results in empty timeWindow. From: " 
						+ this.from + " to " + this.to + " evalRate " + acInt
						+ "All evaluation covered by the From are excluded by the to."));
			}
			final int evaluationsPerWindow = fromEval - toEval;
			final int inCard = getInputCardinality(card, node, daf, 0);
			if (inCard == 0) {
				Utils.handleCriticalException(new OptimizationException(
						"Time window reported a zero input cardinality."));
			}
			cardinality = inCard * evaluationsPerWindow;			
			if (cardinality == 0) {
				Utils.handleCriticalException(new QoSException(
					"QoS Query mismatch results in empty timeWindow. From: " 
					+ this.from + " to " + this.to + " evalRate " + acInt 
					+ " fromEval " + fromEval + " toEval " + toEval
					+ " inCard " + inCard));
			}
		} else {
			cardinality = -this.from + this.to + 1;			
			if (cardinality == 0) {
				Utils.handleCriticalException(new QoSException(
					"QoS Query mismatch results in empty timeWindow. From: " 
					+ this.from + " to " + this.to + " evalRate " + acInt));
			}	
		}
		return cardinality;
    }

	/** {@inheritDoc} */
	public final AlphaBetaExpression getCardinality(final CardinalityType card, 
			final Site node, final DAF daf, final boolean round) {
		if (timeScope) {
			AlphaBetaExpression input = 
				getInputCardinality(card, node, daf, round, 0);
			//Single time instance such as Now and at windows
			if (this.from == this.to) {
				//Assume window is set correctly.
				//Input cardinality = output cardinality.
				return input;  
			} 
			AlphaBetaExpression evaluationsPerWindow =
				new AlphaBetaExpression();
			
			//evaluations in from range plus 1 as both ends inclusive
			//from expressed as relative to now so always negative.
			//(from+1)/ alpha 
			evaluationsPerWindow.add(
					new AlphaBetaTerm(-this.from + 1, -1, 0));
			//Answer if not rounded will already be an over estimate
			
			//evaluation in to range
			//to expressed as relative to now so always negative.
			if (to != 0) {
				evaluationsPerWindow.subtract(
						new AlphaBetaTerm(-this.to, -1, 0));
				if (round) {
					//to range could be over estimated.
					//round to correct for over removal.
					evaluationsPerWindow.add(
							new AlphaBetaTerm(1, 0, 0));
				}	
			}
			
			AlphaBetaExpression result = 
				AlphaBetaExpression.multiplyBy(input, evaluationsPerWindow);
			return result;			
		}
		//row scope so window size if fixed.
		return new AlphaBetaExpression(-this.from + this.to + 1);			
    }
    
    /**
     * {@inheritDoc}
     */
    public final boolean pushProjectionDown(
    		final ArrayList<Expression> projectExpressions, 
    		final ArrayList<Attribute> projectAttributes) 
	    	throws OptimizationException {
	  	return getInput(0).pushProjectionDown(
	  			projectExpressions, projectAttributes);
	}

	 /** 
	  * {@inheritDoc}
	  * Should never be called as there is always a project or aggregation 
	  * between this operator and the rename operator.
	  */   
	 public final void pushLocalNameDown(final String newLocalName) {
		 throw new AssertionError("Unexpected call to pushLocalNameDown()"); 
	 }

	 /**
	 * Used to determine if the operator is Attribute sensitive.
	 * 
	 * @return false.
	 */
	public final boolean isAttributeSensitive() {
		//Need all sites to slide by row
		if (rowSlide != 0) {
			return true;
		}
		//Do not need all sites for timeScope
		return !timeScope;
	}

	/** {@inheritDoc} */
	public final boolean isLocationSensitive() {
		return false;
	}
	
	/** {@inheritDoc} */
	public final boolean isRecursive() {
		return false;
	}
	
    /** {@inheritDoc}
     * @return false;
     */
    public final boolean acceptsPredicates() {
        return false;
    }

	/** {@inheritDoc} */
 	public final String toString() {
        return this.getText() + "[ " + super.getInput(0).toString() + " ]";
    }
    
	/** {@inheritDoc} */
	public final WindowOperator shallowClone() {
		final WindowOperator clonedOp = new WindowOperator(this);
		return clonedOp;
	}
	
	/** {@inheritDoc} */
	public final int getOutputQueueCardinality(
			final Site node, final DAF daf) {
    	int size;
     	if (timeScope) {
        	final int evaluationsInQueue;
    		if ((timeSlide > 1) || (timeSlide != acInt)) {
    			evaluationsInQueue = (int) Math.ceil((1 - this.from + timeSlide)
    					/ acInt);
    		} else {
    			evaluationsInQueue 
    				= (int) Math.ceil((1 - this.from) / acInt);
    		}
    		//Size is always the max.
    		size = getInputCardinality(CardinalityType.MAX, 
    				node, daf, 0)
    		    * evaluationsInQueue;
     	} else {
     		size = -this.from;
     	} 
     	if (rowSlide > 1) {
     		size = size + rowSlide - 1;
     	}
     	//logger.finest("queue =" +size);
     	return size;
    }

 	/** {@inheritDoc} */    
    public final int getOutputQueueCardinality(int numberOfInstances) {
    	int size;
     	if (timeScope) {
        	final int evaluationsInQueue;
    		if ((timeSlide > 1) || (timeSlide != acInt)) {
    			evaluationsInQueue = (int) Math.ceil((1 - this.from + timeSlide)
    					/ acInt);
    		} else {
    			evaluationsInQueue 
    				= (int) Math.ceil((1 - this.from) / acInt);
    		}
    		//Size is always the max.
    		size = getCardinality(CardinalityType.MAX) / numberOfInstances
    		    * evaluationsInQueue;
     	} else {
     		size = -this.from;
     	} 
     	if (rowSlide > 1) {
     		size = size + rowSlide - 1;
     	}
     	//logger.finest("queue =" +size);
     	return size;
    }


	
	/** 
	 * Similar to outputQueueCardinality expect that is returns an expression.
	 * 
	 * @param node Site
	 * @param daf Distributed Algebraic format
	 * @param round Defines if rounding reserves should be included or not
	 * @return Estimated Output Queue size as an expression.
	 */
	private AlphaBetaExpression getOutputQueueExpression(
			final Site node, final DAF daf, final boolean round) {
		if (((timeSlide > 1) && (timeSlide != acInt)) || (rowSlide > 1)) {
			throw new AssertionError("Cost Expressions not done for slides."); 
		}
		if (!timeScope) {
			//TODO double check if this does or does not need +1
			return new AlphaBetaExpression(-this.from);
		}
		AlphaBetaExpression input =	this.getInputCardinality(
			CardinalityType.MAX, node, daf, round, 0);
		if (this.from == this.to) {
			//Assume window is set correctly.
			//Input cardinality = output cardinality.
			return input;  
		} 
		AlphaBetaExpression evaluationsPerWindow =
			new AlphaBetaExpression();
			
		//evaluations in from range plus 1 as both ends inclusive
		//from expressed as relative to now so always negative.
		//(from+1)/ alpha 
		evaluationsPerWindow.add(
				new AlphaBetaTerm(-this.from + 1, -1, 0));
		//Answer will already be an over estimate so never round.
		
		//do not remove "to" as it is part of thr output queue.
		
		//multiply evaluation per window by size of input
		evaluationsPerWindow.multiplyBy(input);
     	return evaluationsPerWindow;
    }

	/** {@inheritDoc} */
    public final double getTimeCost(final CardinalityType card, 
    		final Site node, final DAF daf) {
		final int tuples 
			= this.getInputCardinality(card, node, daf, 0);
		// call method 
		double cost = getOverheadTimeCost();
		// save tuple in outQueue	
		cost = cost + CostParameters.getCopyTuple() * tuples;
		if (timeScope) {
			//add cost of checking each possible tuple
			cost = cost + CostParameters.getCheckTuple()
			   * this.getOutputQueueCardinality(node, daf);
			//add cost of recording input tuple arrival time
			cost = cost + CostParameters.getSetAValue() * tuples;
		}
		// set window eval time in each output tuple	
		cost = cost	+ CostParameters.getSetAValue()
			* this.getCardinality(card, node, daf);
		return cost;	
    }
    
    /** {@inheritDoc} */
	public double getTimeCost(CardinalityType card, int numberOfInstances) {
		final int tuples = this.getInputCardinality(card, 0, numberOfInstances);
		// call method 
		double cost = getOverheadTimeCost();
		// save tuple in outQueue	
		cost = cost + CostParameters.getCopyTuple() * tuples;
		if (timeScope) {
			//add cost of checking each possible tuple
			cost = cost + CostParameters.getCheckTuple()
			   * this.getOutputQueueCardinality(numberOfInstances);
			//add cost of recording input tuple arrival time
			cost = cost + CostParameters.getSetAValue() * tuples;
		}
		// set window eval time in each output tuple	
		cost = cost	+ CostParameters.getSetAValue()
			* (this.getCardinality(card) / numberOfInstances);
		return cost;	
	}

	/** {@inheritDoc} */
	public final AlphaBetaExpression getTimeExpression(
			final CardinalityType card, final Site node, 
			final DAF daf, final boolean round) {
		// call method 
		AlphaBetaExpression result = new AlphaBetaExpression();
		result.addBetaTerm(getOverheadTimeCost());
		// save tuple in outQueue	
		AlphaBetaExpression tuples 
			= this.getInputCardinality(card, node, daf, round, 0);
		result.add(AlphaBetaExpression.multiplyBy(
			tuples, CostParameters.getCopyTuple()));
		if (timeScope) {
			//add cost of recording input tuple arrival time
			result.add(AlphaBetaExpression.multiplyBy(
					tuples, CostParameters.getSetAValue()));
			//add cost of checking each possible tuple
			result.add(AlphaBetaExpression.multiplyBy(
					getOutputQueueExpression(node, daf, round), 
					CostParameters.getCheckTuple()));
		}
		// set window eval time in each tuple
		result.add(AlphaBetaExpression.multiplyBy(
				tuples, CostParameters.getSetAValue()));
		return result;
	}
	     
 	/** {@inheritDoc} */
     public final boolean isRemoveable() {
    	if (this.to != 0) {
    	    return false;
    	}
    	if (rowSlide > 1) {
    	    return false;
    	}
   		if (timeSlide > 1) {
			    return false;
		}
    	if (timeScope) {
   			if (-this.from >= 1) {
   			    return false;
 			}
     	} else {
    		if (this.from != this.getInput(0).getCardinality(
    				CardinalityType.MAX)) {
    			return false;
    		}
    		if (this.from != this.getInput(0).getCardinality(
    				CardinalityType.MINIMUM)) {
    			return false;
    		}
     	}
    	return true;
    }

    /**
     * @return Slide in rows if any.
     */
    public final int getRowSlide() {
		return rowSlide;
	}

    /**
     * @return True if from and to are expressed in ticks.
     */
	public final boolean isTimeScope() {
		return timeScope;
	}

	/**
	 * @return Time slide if any.
	 */
	public final int getTimeSlide() {
		return timeSlide;
	}
	
	/** {@inheritDoc} */
	public final ArrayList<Expression> getExpressions() {
		ArrayList<Expression> expressions = new ArrayList<Expression>(); 
		expressions.addAll(getAttributes());
		return expressions;
	}
	
    //Call to default methods in OperatorImplementation

    /** {@inheritDoc} */
    public final int[] getSourceSites() {
    	return super.defaultGetSourceSites();
    }
 
	/** {@inheritDoc} */    
    public final ArrayList<Attribute> getAttributes() {
    	return super.defaultGetAttributes();
    }

	/** {@inheritDoc} */    
	public final int getDataMemoryCost(final Site node, final DAF daf) {
		return super.defaultGetDataMemoryCost(node, daf);
	}

	/** 
	 * Sets acquisition Interval.
	 * @param acquisitionInterval Acquisition interval of the whole query.
	 *  (Alpha)
	 */
	public final void setAcquisitionInterval(final double acquisitionInterval) {
		this.acInt = acquisitionInterval;
	}

  }
