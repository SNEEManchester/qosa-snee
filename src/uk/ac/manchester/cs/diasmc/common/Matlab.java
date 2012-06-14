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
package uk.ac.manchester.cs.diasmc.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;

public class Matlab {

    /**
     * Invokes Matlab with given script.
     * @param inputScriptName
     * @param outputFileName
     */
    public static void invokeMatlab(
    		final String inputScriptName, 
    		final String outputFileName) {
		try {
		    final Runtime rt = Runtime.getRuntime();
	
		    String matlabexe = LocalSettings.getMatlabExe();
		    String commandStr = matlabexe + " -nodesktop -nosplash -nodisplay " +
    			"-r " + inputScriptName.replace(".m", "") + " -logfile " + outputFileName; 
		    //older versions of matlab used /r amd /logfile
		    
		    System.err.println(commandStr);
		    
		    ProcessBuilder builder = new ProcessBuilder(commandStr);
		    builder.redirectErrorStream(true);
		    Process proc = builder.start();
		    		    
		    //The following lines make sure the Java app waits for the program to finish
		    final InputStream stdout = proc.getInputStream();
		    final InputStreamReader isr = new InputStreamReader(stdout);
		    final BufferedReader br = new BufferedReader(isr);
		    String line = null;
	
		    while ((line = br.readLine()) != null) {
		    }
		    final int exitVal = proc.waitFor();
		} catch (Exception e) {
			System.err.println("Unable to invoke solver");
		    Utils.handleCriticalException(e);
		}
    }
	
}
