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

import java.io.File;
import java.io.IOException;

import uk.ac.manchester.cs.diasmc.common.INIFile;
import uk.ac.manchester.cs.diasmc.common.Utils;

/**
 * SNEEql local Settings.
 * 
 * @author Ixent Galpin and Christian Brenninkmeijer 
 */
public final class LocalSettings {

	/** See Get Method. * /	
	private static String sneeqlDirectory;
	
	/** See Get Method. */
	private static String runHugsExe = null;
					
	/** See Get Method. */
    private static String graphvizExe = Constants.GRAPHVIZ_EXE; 
	    
	/** See Get Method. */
	private static int graphvizScreenWidth = 1800;

	/** See Get Method. */
 	private static int graphvizScreenHeight = 740; 
	
	/** See Get Method. */
	private static String latexCompilerExe = null;
	
	/** See Get Method. */
	private static String dvipsExe = null;
	
	/** See Get Method. */
	private static String ghostscriptExe = null;


	/**
	 * The full path for the matlab executable
	 */
	private static String matlabExe = null;
	
	/**
	 * The directory where the CXV installation resides.
	 */
	private static String cvxDir = null;
	
	/**
	 * Constructor to hide default constructor.
	 *
	 */
	private LocalSettings() {
	}
	
	/**
	 * Reads the ini file settings.
	 * @param iniFile The ini file to be read.
	 */
	private static void readIniFile(final INIFile iniFile) {
          	
    	if (Settings.useHaskell()) {
			runHugsExe = 
	    		iniFile.getStringProperty("Haskell", "exe");
    	}
		
    	if (Settings.GENERAL_GENERATE_GRAPHS) {
        	graphvizExe = 
        		iniFile.getStringProperty("GraphViz", "exe");
        	
        	String graphvizScreenWidthStr = 
        		iniFile.getStringProperty("GraphViz", "screen_width");
        	if (graphvizScreenWidthStr!=null) {
        		graphvizScreenWidth = new Integer(graphvizScreenWidthStr).intValue();
        	}
        	
        	String graphvizScreenHeightStr = 
        		iniFile.getStringProperty("GraphViz", "screen_height");
        	if (graphvizScreenHeightStr!=null) {
        		graphvizScreenHeight = new Integer(graphvizScreenHeightStr).intValue();
        	}        	
    	}
    	 
    	if (Settings.GENERAL_GENERATE_PDFS) {
	    	latexCompilerExe = 
	    		iniFile.getStringProperty("Latex", "compiler_exe");
	    	dvipsExe = 
	    		iniFile.getStringProperty("Latex", "dvips_exe");
	    	ghostscriptExe = 
	    		iniFile.getStringProperty("Latex", "ghostscript_exe");
    	}
	    
    	if (Settings.QOS_AWARE_WHEN_SCHEDULING || Settings.QOS_AWARE_WHERE_SCHEDULING) {
	    	matlabExe = 
	    		iniFile.getStringProperty("Solver", "matlab_exe");
	    	cvxDir =
	    		iniFile.getStringProperty("Solver", "cvx_dir");
    	}
	}
			
	/**
	 * Checks that the files and directories referenced exist (or, if 
	 * applicable, that they can be created).
	 *
	 */
	private static void checkFilesAndDirs(String source) {
		
		try {
	    	if (Settings.useHaskell()) {
	    		Utils.checkFile(runHugsExe, source + 
	    				" - Haskell interpreter exe not specified and -use-haskell=true.");
	    	}
	    	
	    	if (Settings.GENERAL_GENERATE_GRAPHS) {
	    		Utils.checkFile(graphvizExe, source + 
	    				" - Graphviz Exe not specified and -generate-graphs=true");
	    	}

	    	if (Settings.GENERAL_GENERATE_PDFS) {
		    	Utils.checkFile(ghostscriptExe, source 
		    			+ " - Ghostscript exe not specified and -generate-pdfs=true");	
		    	Utils.checkFile(latexCompilerExe, source
		    			+ " - Latex compiler not specified and -generate-pdfs=true");	
		    	Utils.checkFile(dvipsExe, source
		    			+ " - Dvips exe not specified and -generate-pdfs=true");	
	    	}
		    	
	    	if (Settings.QOS_AWARE_WHEN_SCHEDULING) {
	    		Utils.checkFile(matlabExe, source
	    				+ "Matlab exe not specified and -qos-aware-when-scheduling=true");
	    		Utils.checkDirectory(cvxDir, false);
	    	}
	    	
		} catch (final IOException e) {
			Utils.handleCriticalException(e);
		}
	}
		
	/**
	 * Intialization -- reads ini file and command-line settings.
	 */
	public static void initialize() {
				
		String localIniFile;
		localIniFile = Constants.LOCAL_INI_FILE;
		
		if (new File(localIniFile).exists()) {
			//read settings from ini file
			readIniFile(new INIFile(localIniFile));
		}
		
		//check that any files and directories required exist
		checkFilesAndDirs(localIniFile);	
	}

	/**
	 * Get Full path of the dvips executable, used to convert dvi to ps.
	 *
	 * @return Path as specified in Constants.LOCAL_INI_FILE
	 */
	public static String getDvipsExe() {
		return dvipsExe;
	}

	/**
	 * Full path of ghostscipt executable, used to convert ps to pdf.
     *
	 * @return Path as specified in Constants.LOCAL_INI_FILE
	 */
	public static String getGhostscriptExe() {
		return ghostscriptExe;
	}

	/**
     * Location of the GraphViz executables.
     * 
	 * @return Path as specified in Constants.LOCAL_INI_FILE
     */
	public static String getGraphvizExe() {
		return graphvizExe;
	}

    /**
     * Assumed height of the screen (for displaying graphs).
     * 
	 * @return Value as specified in Constants.LOCAL_INI_FILE
     */
	public static int getGraphvizScreenHeight() {
		return graphvizScreenHeight;
	}
	

    /**
     * Assumed width of the screen (for displaying graphs).
     * 
	 * @return Value as specified in Constants.LOCAL_INI_FILE
     */
	public static int getGraphvizScreenWidth() {
		return graphvizScreenWidth;
	}

	/**
	 * Full path of the latex compiler executable.
	 * 
	 * @return Path as specified in Constants.LOCAL_INI_FILE
	 */
	public static String getLatexCompilerExe() {
		return latexCompilerExe;
	}

	/**
	 * Executable used to run the Haskell translator.
	 * Format is the full file path and the fine name.
	 *
	 * @return Path as specified in Constants.LOCAL_INI_FILE
	 */ 
	public static String getRunHugsExe() {
		return runHugsExe;
	}
		
	/** 
	 * Testing method.
	 * 
	 * @param args Any args are ignored.
	 */
    public static void main(final String[] args) {
    	initialize();
    	System.out.println("Local Settings validated");
     }


	/**
	 * Getter method for matlabExe
	 * @return the full path to the matlab executable
	 */
	public static String getMatlabExe() {
		return matlabExe;
	}
	
	/**
	 * Getter method for CVXDir
	 * @return the directory for the CVX installation
	 */
	public static String getCVXDir() {
		return cvxDir;
	}
}
