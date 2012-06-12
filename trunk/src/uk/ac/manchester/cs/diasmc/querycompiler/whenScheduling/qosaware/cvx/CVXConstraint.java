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
package uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx;

public class CVXConstraint {

    AlphaBetaExpression leftExpr;

    CVXOperator operator;

    AlphaBetaExpression rightExpr;

    String comment;

    public CVXConstraint(final AlphaBetaExpression _leftExpr,
	    final CVXOperator _operand, final AlphaBetaExpression _rightExpr,
	    final String _comment) {
	this.leftExpr = _leftExpr;
	this.operator = _operand;
	this.rightExpr = _rightExpr;
	this.comment = _comment;
    }

    public CVXConstraint(final AlphaBetaExpression _leftExpr,
	    final CVXOperator _operand, final AlphaBetaExpression _rightExpr) {
	this(_leftExpr, _operand, _rightExpr, "");
    }

    @Override
	public final String toString() {
	final StringBuffer strBuff = new StringBuffer();
	strBuff.append("% " + this.comment.toString() + "\n");
	strBuff.append(this.leftExpr.toString() + " ");
	strBuff.append(this.operator.toString() + " ");
	strBuff.append(this.rightExpr.toString());
	return strBuff.toString();
    }

    public final String toLatex() {
	final StringBuffer strBuff = new StringBuffer();
	strBuff.append("$" + this.leftExpr.toDecimalLatexString() + " ");
	strBuff.append(this.operator.toLatex() + " ");
	strBuff.append(this.rightExpr.toDecimalLatexString() + "$ ");
	return strBuff.toString();
    }

    public final String getComment() {
	return this.comment;
    }

    public final void setComment(final String comment) {
	this.comment = comment;
    }

}
