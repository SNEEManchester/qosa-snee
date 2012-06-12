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
package uk.ac.manchester.cs.diasmc.querycompiler;

import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.DStreamOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.IStreamOperator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.
	querycompiler.queryplan.operators.RStreamOperator;

/**
 * Constants used by the SNEEql Components.
 * 
 * @author Christian Brenninkmeijer, Ixent Galpin
 */
public final class Constants {

	/** Hides default constructor. */
	private Constants() {
	}
	
	/**
     *  The newline character for debugging messages.
     */    
    public static final String NEWLINE 
    	= System.getProperty("line.separator");    

    /**
     * The default attribute by which sensor extents are partitioned.
     */
    public static final String DEFAULT_PARTITIONING_ATTRIBUTE = "id";

    /**
     * Used to represent the null partitioning attribute, 
     * i.e., when there is only
     * one partitition of the data.
     */
    public static final String NULL_PARTITIONING_ATTRIBUTE = "null";

	/**
	 * String to use for local name of intermediate attributes.
	 */
	public static final String PARTIAL_LOCALNAME = "temp";
	
	/**
     * Extra String to insert in the sum part of the the average.
     */
	public static final String AVG_PARTIAL_HEAD = "sum_";
   
	//These are constants used in various classes.
    //The actual values are not important only that the are unique.

	/** @deprecated */
    public static enum XSTREAM {
    	/** @deprecated */
    	NOSTREAM {
    		/** @deprecated 
    		 * @param before previous Operator
    		 * @return an Operator.  
    		 */
			public Operator addOperator(final Operator before) {
    			return before;
    		}

			/** {@inheritDoc} */
			public String toString() {
    			return "No Stream";
    		}
    	},
    	/** @deprecated */
    	DSTREAM {
    		/** @deprecated 
    		 * @param before previous Operator
    		 * @return an Operator.  
    		 */
			public Operator addOperator(final Operator before) {
    			return new DStreamOperator(before);
    		}
			/** {@inheritDoc} */
			public String toString() {
    			return "DStream";
    		}
    	},
    	/** @deprecated */
    	ISTREAM {
    		/** @deprecated 
    		 * @param before previous Operator
    		 * @return an Operator.  
    		 */
			public Operator addOperator(final Operator before) {
    			return new IStreamOperator(before);
    		}
			/** {@inheritDoc} */
			public String toString() {
    			return "IStream";
    		}
    	},
    	/** @deprecated */
    	RSTREAM {
    		/** @deprecated 
    		 * @param before previous Operator
    		 * @return an Operator.  
    		 */
			public Operator addOperator(final Operator before) {
    			return new RStreamOperator(before);
    		}
			/** {@inheritDoc} */
			public String toString() {
    			return "RStream";
    		}
    	};
    
		/** @deprecated 
		 * @param before previous Operator
		 * @return an Operator.  
		 */
    	  public abstract Operator addOperator(Operator before);
    	  
			/** {@inheritDoc} */
		public abstract String toString();
    }
    
	/**
	 * Local Ini file location.
	 */
	public static final String LOCAL_INI_FILE = "input/local.ini";
	
	/**
	 * Ini file location.
	 */
	public static final String INI_FILE = "input/SNEEql.ini";

	/**
	 * Debug file location (for specifying the level of debug 
	 * messages to be logged for each class.)
	 */
	public static final String DEBUG_FILE = "input/debug.ini";
	
	/**
	 * String representation of type used for the time attribute.
	 */
	public static final String TIME_TYPE = "time";

	/**
	 * String representation of type used for the time attribute.
	 */
	public static final String ID_TYPE = "integer";
	
	/**
	 * String representation of type used for the time attribute.
	 */
	public static final String COUNT_TYPE = "integer";

	/**
	 * String representation of attribute used for the eval time attribute.
	 */
	public static final String EVAL_TIME = "evalTime";

	/**
	 * String representation of attribute used for the acquire time attribute.
	 */
	public static final String ACQUIRE_TIME = "time";

	/**
	 * String representation of attribute used for the acquire time attribute.
	 */
	public static final String ACQUIRE_ID = "id";

	/**
	 * Directory to write query plans to.
	 */
	public static final String QUERY_PLAN_DIR = "query-plan/";
	
	/**
	 * Energy multiplier of time for CPU.
	 */
	public static final double CPU_ENERGY_FACTOR = 0.001;

	/** 
	 * Energy multiplier of time for Sensors.
	 */
	public static final double SENSOR_ENERGY_FACTOR = 0.01;

	/**
	 * Energy multiplier of time for radio.
	 */
	public static final double RADIO_ENERGY_FACTOR = 0.05;
	
	/** Factor by which join output is reduced by a simple predicate. */
	public static final int JOIN_PREDICATE_SELECTIVITY = 3;

	/**
	 * The default location of the Graphviz executable.
	 */
	public static final String GRAPHVIZ_EXE = "Graphviz/bin/dot.exe";
}



