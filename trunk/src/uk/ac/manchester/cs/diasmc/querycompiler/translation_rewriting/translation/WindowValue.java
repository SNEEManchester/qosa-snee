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

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.Units;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.UnrecognizedUnitException;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SNEEqlParserTokenTypes;
import antlr.collections.AST;

public class WindowValue {

    private float value;

    private String timeUnit = null;

    public WindowValue(final AST node) throws TranslationException {
	AST next = node.getFirstChild();
	boolean minus;
	if (next.getType() == SNEEqlParserTokenTypes.NOW) {
	    next = next.getNextSibling();
	    if (next == null) {
		this.value = 0;
		this.timeUnit = null;
		return;
	    }
	    if (next == null) {
		this.value = 0;
		this.timeUnit = null;
		return;
	    }
	    if (next.getType() == SNEEqlParserTokenTypes.Minus) {
		minus = true;
		next = next.getNextSibling();
	    } else {
		minus = false;
	    }
	    if (next.getType() == SNEEqlParserTokenTypes.Float) {
		this.value = Float.parseFloat(next.getText());
		if (minus) {
		    this.value = 0 - this.value;
		}
		if (this.value > 0) {
		    throw new TranslationException(
			    "Negative value required after now in window found "
				    + next.getText());
		}
	    } else {
		throw new TranslationException(
			"Float value expected after now in window found "
				+ next.getText());
	    }
	} else if (next.getType() == SNEEqlParserTokenTypes.Float) {
	    this.value = Float.parseFloat(next.getText());
	    if (this.value <= 0) {
		throw new TranslationException(
			"Positive value required after now in window found "
				+ next.getText());
	    }
	} else {
	    throw new TranslationException(
		    "Now or Positive Float expected after from in window found "
			    + next.getText());
	}
	next = next.getNextSibling();
	if (next == null) {
	    this.timeUnit = null;
	} else {
	    this.timeUnit = next.getText();
	}
    }

    public WindowValue(final int value) {
	this.value = value;
	this.timeUnit = "";
    }

    public void setUndefinedUnit(final WindowValue other) {
	if (this.timeUnit == null) {
	    this.timeUnit = other.timeUnit;
	}
    }

    public boolean isRowValue() {
	return (this.timeUnit.equalsIgnoreCase("rows"));
    }

    public int getValue() throws TranslationException {
	final Units units = Units.getInstance();
	float result;
	try {
	    if (this.timeUnit.equalsIgnoreCase("rows")
		    || this.timeUnit.equals("")) {
		result = this.value;
	    } else {
		final long scalingFactor = units
			.getTimeScalingFactor(this.timeUnit);
		result = this.value * scalingFactor;
	    }
	} catch (final UnrecognizedUnitException e) {
	    throw new TranslationException(e.getMessage());
	}

	final int intResult = (int) result;
	if (result == intResult) {
	    return intResult;
	} else {
	    throw new TranslationException("Unable to convert " + this.value
		    + " " + this.timeUnit + " to an integer value");
	}
    }
}
