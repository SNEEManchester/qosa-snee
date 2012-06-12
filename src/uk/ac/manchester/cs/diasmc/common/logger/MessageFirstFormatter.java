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
package uk.ac.manchester.cs.diasmc.common.logger;

//import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import uk.ac.manchester.cs.diasmc.querycompiler.Constants;

public class MessageFirstFormatter extends Formatter {

    public String format(LogRecord record) {
	StringBuffer b = new StringBuffer();
	Throwable error = record.getThrown();
	if (error != null) {
	    b.append(error);
	    b.append(Constants.NEWLINE);
	    StackTraceElement[] trace = error.getStackTrace();
	    for (int i = 0; i < trace.length; i++) {
		b.append("  at ");
		b.append(trace[i].toString());
		b.append(Constants.NEWLINE);
	    }
	} else {
	    b.append(record.getMessage());
	    b.append("[");
	    b.append(record.getSourceMethodName());
	    b.append(":");
	    b.append(record.getSourceClassName());
	    b.append("] ");
	    b.append(Constants.NEWLINE);
	}
	if (record.getLevel().equals(Level.SEVERE)) {
	    //JOptionPane.showMessageDialog(null, b,"Severe Message Logged",JOptionPane.ERROR_MESSAGE);
	    LoggerSetup.flush();
	    System.err.println(b);
	}
	return b.toString();
    }

}
