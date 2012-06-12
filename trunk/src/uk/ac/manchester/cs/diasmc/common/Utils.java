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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.diasmc.common.logger.LoggerSetup;
import uk.ac.manchester.cs.diasmc.common.utils.ExternalProcessException;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.SNEENamespaceContext;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.qosaware.cvx.CVXSolution;

/**
 * Provides utility methods
 * 
 * @author Christian Brenninkmeijer
 */
public class Utils {

    static Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Support meathod that allows all query exceptions to be handled in a constitent mannor.
     * Script can go on to next query
     */
    public static void handleQueryException(Exception e) {
	LoggerSetup.flush();
	logger.log(Level.SEVERE, "Error Found with Query", e);
	//logger.log(Level.SEVERE,"Message = ",e.getMessage());
	LoggerSetup.flush();
	if (Settings.DEBUG_EXIT_ON_ERROR)
	    System.exit(1);
	else
	    logger.warning("DEBUG_EXIT_ON_ERROR not requested");
    }

    /**
     *  Support meathod that allows all Major exceptions to be handled in a constitent manner.
     *  Scripts should end.  
     */
    public static void handleCriticalException(Exception e) {
	System.err.println(e.getMessage());
	LoggerSetup.flush();
	logger.log(Level.SEVERE, "Major Error Found", e);
	LoggerSetup.flush();
	if (Settings.DEBUG_EXIT_ON_ERROR)
	    System.exit(2);
	else
	    logger.warning("DEBUG_EXIT_ON_ERROR not requested");
    }

    public static void checkFile(String name, String source) throws IOException {
	if (name == null)
	    throw new IOException("Null File " + name + "Error in "
		    + source);
	File f = new File(name);
	if (!f.isFile())
	    throw new IOException("File " + name + " used in "
		    + source + " does not Exist");
    }

    public static void checkDirectory(String name, boolean createIfNonExistent)
	    throws IOException {
	File f = new File(name);
	if ((f.exists()) && (!f.isDirectory())) {
	    throw new IOException("File " + name
		    + "already exists but is not a directory");
	}

	if ((!f.exists() && (!createIfNonExistent))) {
	    throw new IOException("File " + name + " does not exist");
	}

	if (!f.exists() && (createIfNonExistent)) {
	    boolean success = f.mkdirs();
	    if (!success) {
		throw new IOException("File " + name
			+ " does not exist and cannot be created");
	    }
	}
    }

    /**
     * Delete all the folders and subdirectories of the given directory
     * @param path
     * @return
     */
    static public boolean deleteDirectoryContents(File path) {
		if (path.exists()) {
		    File[] files = path.listFiles();
		    for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
				    deleteDirectoryContents(files[i]);
				    files[i].delete();
				} else {
				    files[i].delete();
				}
		    }
		    return true;
		}
		return false;
    }

    static public boolean deleteDirectoryContents(String pathName) {
		File path = new File(pathName);
		return deleteDirectoryContents(path);
    }

    static public int divideAndRoundUp(int dividend, int divisor) {
	if ((dividend % divisor) == 0)
	    return dividend / divisor;
	return dividend / divisor + 1;
    }

    static public int divideAndRoundUp(long dividend, int divisor) {
	if ((dividend % (long) divisor) == 0)
	    return (int) (dividend / (long) divisor);
	return (int) (dividend / (long) divisor + 1);
    }

    static public String pad(String s, int n) {
	StringBuffer result = new StringBuffer();

	for (int i = 0; i < n; i++) {
	    result.append(s);
	}

	return result.toString();
    }

    static public String indent(int i) {
	return pad("\t", i);
    }

    public static void validateXMLFile(String filename, String schemaFile)
	    throws ParserConfigurationException, SAXException, IOException {
	//First validate the XML file according to XML schema file
	// Parse an XML document into a DOM tree.
	DocumentBuilder parser = DocumentBuilderFactory.newInstance()
		.newDocumentBuilder();
	Document document = parser.parse(new File(filename));
	// Create a SchemaFactory capable of understanding WXS schemas.
	SchemaFactory schemaFactory = SchemaFactory
		.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	Schema schema = schemaFactory.newSchema(new File(schemaFile));
	// Create a Validator object, which can be used to validate
	// an instance document.
	Validator validator = schema.newValidator();
	// Validate the DOM tree.
	//validator.validate(new DOMSource(document));
    }

    public static String doXPathStrQuery(String xmlFile, String query)
	    throws XPathExpressionException, FileNotFoundException {
	XPathFactory factory = XPathFactory.newInstance();
	XPath xPath = factory.newXPath();
	File xmlDocument = new File(xmlFile);
	InputSource inputSource = new InputSource(new FileInputStream(
		xmlDocument));
	xPath.setNamespaceContext(new SNEENamespaceContext());
	String result = xPath.evaluate(query, inputSource);
	if (result.equals(""))
	    return null;
	else
	    return result;
    }

    public static String doXPathStrQuery(Node node, String query)
	    throws XPathExpressionException, FileNotFoundException {
	XPathFactory factory = XPathFactory.newInstance();
	XPath xPath = factory.newXPath();
	xPath.setNamespaceContext(new SNEENamespaceContext());
	String result = (String) xPath.evaluate(query, node,
		XPathConstants.STRING);
	return result;
    }

    public static int doXPathIntQuery(String xmlFile, String query)
    throws XPathExpressionException, FileNotFoundException {
	XPathFactory factory = XPathFactory.newInstance();
	XPath xPath = factory.newXPath();
	File xmlDocument = new File(xmlFile);
	InputSource inputSource = new InputSource(new FileInputStream(
		xmlDocument));
	xPath.setNamespaceContext(new SNEENamespaceContext());
	String result = xPath.evaluate(query, inputSource);
	if (result.equals(""))
	    return -1;
	else
	    return Integer.parseInt(result);
}

    
    public static int doXPathIntQuery(Node node, String query)
    throws XPathExpressionException, FileNotFoundException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		xPath.setNamespaceContext(new SNEENamespaceContext());
		int result = (Integer) xPath.evaluate(query, node,
			XPathConstants.NUMBER);
		return result;
	}


    public static NodeList doXPathQuery(String xmlFile, String query)
	    throws XPathExpressionException, FileNotFoundException {
	XPathFactory factory = XPathFactory.newInstance();
	XPath xPath = factory.newXPath();
	File xmlDocument = new File(xmlFile);
	InputSource inputSource = new InputSource(new FileInputStream(
		xmlDocument));
	xPath.setNamespaceContext(new SNEENamespaceContext());
	NodeList result = (NodeList) xPath.evaluate(query, inputSource,
		XPathConstants.NODESET);
	return result;
    }

    public static String capFirstLetter(String str) {
	return str.substring(0, 1).toUpperCase()
		+ str.substring(1, str.length());
    }

    /**
     * Given a latex source file generates a DVI file.
     * @param inputLatexFile The full path of the latex source file.
     * @param outputDir The output directory where the DVI file is to be placed.
     */
    public static void latexToDVI(
    		final String inputLatexFile, 
    		final String outputDir) {
    	if (LocalSettings.getLatexCompilerExe()==null) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"Latex compiler is missing.  Please add it to the local.ini file."));
	    }
    	try {
    	    final Runtime rt = Runtime.getRuntime();

    	    final Process proc = rt.exec(new String[] { 
    	    		LocalSettings.getLatexCompilerExe(),
    	    		"--src", 
    	    		"-interaction=batchmode",
    	    		"-output-directory=" + outputDir,
    	    		inputLatexFile});

    	    //The following lines make sure the Java app waits for the program 
    	    //to finish
    	    final InputStream stderr = proc.getErrorStream();
    	    final InputStreamReader isr = new InputStreamReader(stderr);
    	    final BufferedReader br = new BufferedReader(isr);
    	    String line = null;

    	    while ((line = br.readLine()) != null) {
    	    	System.out.println("from Utils.latexToDV"+line);
    	    }
    	    
    	    final int exitVal = proc.waitFor();
    	    if (exitVal != 0) {
        	    Utils.handleCriticalException(new OptimizationException(
        	    		"Error converting " + inputLatexFile + " to " 
        	    		+ outputDir + ": exitVal=" + exitVal));
    	    }

    	    
    	} catch (final Throwable t) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"Error converting " + inputLatexFile + " to " 
    	    		+ outputDir));
    	}
    }
    
    /**
     * Given a DVI file, converts it to a postscript file.
     * @param inputDVIFilename The full path of the DVI file.
     * @param outputPSFilename The desired full path of the output 
     * postcript file. 
     */
    public static void dviToPS(final String inputDVIFilename, 
    		final String outputPSFilename) {
    	if (LocalSettings.getDvipsExe()==null) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"dvips.exe file missing"));
	    }
    	try {
    	    final Runtime rt = Runtime.getRuntime();

    	    final Process proc = rt.exec(new String[] { 
    	    		LocalSettings.getDvipsExe(),
    	    		"-t", "letter", "-P", "pdf",
    	    		"-o", 
    	    		outputPSFilename,
    	    		inputDVIFilename});

    	    //The following lines make sure the Java app waits for the 
    	    //program to finish
    	    final InputStream stderr = proc.getErrorStream();
    	    final InputStreamReader isr = new InputStreamReader(stderr);
    	    final BufferedReader br = new BufferedReader(isr);
    	    String line = null;

    	    while ((line = br.readLine()) != null) {
    	    }
    	    //System.out.println(line);
    	    final int exitVal = proc.waitFor();
    	    if (exitVal != 0) {
        	    Utils.handleCriticalException(new OptimizationException(
        	    		"Error converting " + inputDVIFilename + " to " 
        	    		+ outputPSFilename + ": exitVal=" + exitVal));
    	    }
    	    
    	} catch (final Throwable t) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"Error converting " + inputDVIFilename + " to " 
    	    		+ outputPSFilename));
    	}
    }
    
    /**
     * Given a postscript file, generates a PDF file.
     * @param psFile The full path for the input PS file.
     * @param pdfFile The full path for the output PDF file.
     */
    public static void psToPDF(final String psFile, final String pdfFile) {
    	if (LocalSettings.getGhostscriptExe()==null) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"Ghostscript exe file required is missing"));
	    }
    	try {
    	    final Runtime rt = Runtime.getRuntime();

    	    final Process proc = rt.exec(new String[] { 
    	    		LocalSettings.getGhostscriptExe(),
    	    		"-sPAPERSIZE=letter",
    	        	"-dSAFER",
    	        	"-dBATCH",
    	        	"-dNOPAUSE",
    	        	"-dCompatibilityLevel=1.3",
    	        	"-dMAxSubsetPct=100",
    	        	"-dSubsetFonts=true",
    	        	"-dEmbedAllFonts=true",
    	        	"-sDEVICE=pdfwrite",
    	        	"-sOutputFile=" + pdfFile,
    	        	"-c",
    	        	"save",
    	        	"pop",
    	        	"-f",
    	        	psFile});

    	    //The following lines make sure the Java app waits for the program to finish
    	    final InputStream stderr = proc.getErrorStream();
    	    final InputStreamReader isr = new InputStreamReader(stderr);
    	    final BufferedReader br = new BufferedReader(isr);
    	    String line = null;

    	    while ((line = br.readLine()) != null) {
    	    }
    	    //System.out.println(line);
    	    final int exitVal = proc.waitFor();
    	    if (exitVal != 0) {
        	    Utils.handleCriticalException(new OptimizationException(
        	    		"Error converting " + psFile + " to " 
        	    		+ pdfFile + ": exitVal=" + exitVal));
    	    }    	    
    	    
    	} catch (final Throwable t) {
    	    Utils.handleCriticalException(new OptimizationException(
    	    		"Error converting " + psFile + " to " 
    	    		+ pdfFile));
    	}
    }

    /**
     * Generates a PDF file based on the given latex file.  The .tex extension
     * of the input file is replaced with .pdf.
     * @param latexFilename The full path of the input file with latex source 
     * code.
     */
    public static void latexToPDF(final String latexFilename) {
    	Utils.latexToDVI(latexFilename, 
    			QueryCompiler.queryPlanOutputDir);
    	
    	String dviFilename = latexFilename.replaceAll("\\.tex", ".dvi");
    	String psFilename = latexFilename.replaceAll("\\.tex", ".ps");
    	Utils.dviToPS(dviFilename, psFilename);

    	String pdfFilename = latexFilename.replaceAll("\\.tex", ".pdf"); 
    	Utils.psToPDF(psFilename, pdfFilename);
    	
    	//Purge files which are generated by latex 
    	new File(dviFilename).deleteOnExit();
    	new File(psFilename).deleteOnExit();
    	
    	String auxFilename = latexFilename.replaceAll("\\.tex", ".aux"); 
    	File auxFile = new File(auxFilename);
    	auxFile.deleteOnExit();
    	
    	String logFilename = latexFilename.replaceAll("\\.tex", ".log"); 
    	File logFile = new File(logFilename);
    	logFile.deleteOnExit();
    }

	public static String fixDirName(final String dirName) {
		String fixedDirName = dirName.replaceAll("\\\\", "/");
		if (!fixedDirName.endsWith("/")) {
			return fixedDirName + "/";
		} else {
			return fixedDirName;
		}
	}

   /** 
    * Reads the values from an inpur Stream and returns a String.
    * @param in The incoming Stream
    * @return The Streams contents as a String
    */	
   private static String inputStreamToString(final InputStream in) {
	   StringBuffer buffer = new StringBuffer();
	   try {
		   for (int i = in.read(); i != -1; i = in.read()) {
			   buffer.append((char) i);
		   }
		   in.close();
	   } catch (Exception e) {
		  Utils.handleCriticalException(e);
	   }	
	   return buffer.toString();
	}
   
   private static String monitorProcess (Process p) throws IOException {
      InputStream result = p.getInputStream();
	  StringBuffer buffer = new StringBuffer();
	  for (int i = result.read(); i != -1; i = result.read()) {
		  buffer.append((char) i);
	  }
	  String output = buffer.toString(); 
	  logger.info(output);
	  return output;
   }
   
   /**
    * Runs an external process.
    * Current versions assumes all arguments that points to files or directories
    * are full qualified and not relative.
    * 
    * @param args List of arguments including the external process to run.
    * @return Any normal output of the process as a String.
    */
	public static String runExternalProcess(final List<String> args) {
	   ProcessBuilder pb = new ProcessBuilder(args);
	   pb.redirectErrorStream(true);
	   logger.info("Running: " + args.toString());
	   System.out.println("Running: " + args.toString());
	   String output = "Failed to Run";
	   try {
		   Process p = pb.start();
		   output = monitorProcess(p);
		   if (p.exitValue() != 0) {
			   throw new ExternalProcessException(
					"External program ended in an error:\n"
					+ "Args: " + args.toString() + "\n"   
					+ "Error: " + output);
			}
		   logger.info("Run Successfull.");  
		   output = inputStreamToString(p.getInputStream());
		   logger.info(output);
		} catch (IllegalThreadStateException e) { 
			e.printStackTrace();
			//desperate hack!!
			runExternalProcess(args);
		} catch (Exception e) {
			Utils.handleCriticalException(e);
		}	
		return output;
	} 

	
	/**
	 * Copy file in to file out.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
    public static void copyFile(File in, File out) 
    throws IOException 
    {
	    FileChannel inChannel = new
	        FileInputStream(in).getChannel();
	    FileChannel outChannel = new
	        FileOutputStream(out).getChannel();
	    try {
	        inChannel.transferTo(0, inChannel.size(),
	                outChannel);
	    } 
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	}

    
    /**
     * Copy file in to file out.
     * @param in
     * @param out
     * @throws IOException
     */
	public static void copyFile(String in, String out) throws IOException{
	    copyFile(new File(in),new File(out));
	}
	
	/**
	 * Converts a value in seconds into days
	 * @return
	 */
	public static double convertSecondsToDays(double secs){
		return (((secs/60.0)/60.0)/24.0);
	}

	
	/**
	 * Solve a quadratic equation of the form
	 * ax^2 + bx + c = 0
	 * Returns both solutions.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return 
	 */
	public static double[] solveQuadraticEquation(double a, double b, double c) {
		double tmp = Math.sqrt(Math.pow(b, 2.0)- (4.0*a*c));
		double sol1 = (-b + tmp) / (2*a);
		double sol2 = (-b - tmp) / (2*a);
		
		return new double[] {sol1, sol2};
	}
	
}
