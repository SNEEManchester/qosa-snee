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

/**
 * Represents a window.
 * @author Christian Brenninkmeijer 
 */
public class TimeWindow extends Element {

    private boolean relativeFrom = true;

    private boolean relativeTo = true;

    private int from;

    private int to;

    private int slide;

    public TimeWindow(final int from, final int to, final int slide) {
	super(null);
	this.from = from;
	this.to = to;
	this.slide = slide;
    }

    public TimeWindow(final int from, final boolean relativeFrom, final int to,
	    final boolean relativeTo, final int slide) {
	super(null);
	this.from = from;
	this.relativeFrom = relativeFrom;
	this.relativeTo = relativeTo;
	this.to = to;
	this.slide = slide;
    }

    public int getFrom() {
	return this.from;
    }

    public int getTo() {
	return this.to;
    }

    public int getSlide() {
	return this.slide;
    }

    public boolean getrelativeFrom() {
	return this.relativeFrom;
    }

    public boolean getrelativeTo() {
	return this.relativeTo;
    }

    /**
     * Returns a String representation of the relation.
     */
    @Override
    public String toString() {
	final StringBuffer s = new StringBuffer("TimeWindow (");
	if (this.relativeFrom) {
	    s.append("r");
	}
	if (this.from >= 0) {
	    s.append("+");
	}
	s.append(this.from + ",");
	if (this.relativeTo) {
	    s.append("r");
	}
	if (this.to >= 0) {
	    s.append("+");
	}
	s.append(this.to + ",");
	s.append(this.slide + ")");
	return s.toString();
    }

}
