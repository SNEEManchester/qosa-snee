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

package uk.ac.manchester.cs.diasmc.common.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;
import uk.ac.manchester.cs.diasmc.querycompiler.OptimizationException;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation.Translator;

/**
 * @author Ixent Galpin
 * 
 * Graph Display operations.
 *   
 */

public class DisplayGraph {

    private static Logger logger = Logger.getLogger(DisplayGraph.class
	    .getName());

    /*Keeps track of where windows are placed to avoid them overlapping*/
    private static int x = 0;

    private static int y = 0;

    /**
     * Converts a file in the DOT, the Graphviz graph specification 
     * language into a PNG image.
     * @param inputFullPath	the DOT file
     * @param outputFullPath the PNG image
     */
    public static void convertDOT2PNG(final String inputFullPath,
	    final String outputFullPath) {

	//Code adapted from	
	//http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=3
	try {
	    Runtime rt = Runtime.getRuntime();
	    logger.finest(LocalSettings.getGraphvizExe() + "-Tpng " + "-o"
		    + outputFullPath + " " + inputFullPath);
	    Process proc = rt.exec(new String[] {LocalSettings.getGraphvizExe(),
		    "-Tpng", "-o" + outputFullPath, inputFullPath });
	    InputStream stderr = proc.getErrorStream();
	    InputStreamReader isr = new InputStreamReader(stderr);
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    while ((line = br.readLine()) != null) {
	    }
	    int exitVal = proc.waitFor();
	    logger.finest("Dotfile to PNG process exitValue: " + exitVal);
	} catch (Throwable t) {
	    logger.warning("Excepttion " + t);
	    Utils.handleCriticalException(new OptimizationException(
		    "Dotfile to PNG process failed"));
	}
    }

    /*
     * Converts a file in the DOT, the Graphviz graph specification 
     * language into a PS image.
     * @param inputFile	the DOT file
     * @param outputFile	the PS image
     */
    public static void convertDOT2PS(String inputFullPath, String outputFullPath) {

	//Code adapted from	
	//http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=3
	try {
	    Runtime rt = Runtime.getRuntime();
	    Process proc = rt.exec(new String[] { LocalSettings.getGraphvizExe(),
		    "-Tps", "-o" + outputFullPath, inputFullPath });
	    InputStream stderr = proc.getErrorStream();
	    InputStreamReader isr = new InputStreamReader(stderr);
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    while ((line = br.readLine()) != null) {
	    }
	    int exitVal = proc.waitFor();
	    logger.finest("Dotfile to PS process exitValue: " + exitVal);
	} catch (Throwable t) {
	    logger.warning("Exception " + t);
	    Utils.handleCriticalException(new OptimizationException(
		    "Dotfile to PS process failed"));
	}
    }

    /*
     * Displays the PNG image
     * @param filename	the PNG image
     */
    private static JFrame doDisplayGraph(String filename) {
	//Make sure we have nice window decorations.
	JFrame.setDefaultLookAndFeelDecorated(true);

	//Create and set up the window.
	JFrame frame = new JFrame(filename);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	ImageIcon i = new ImageIcon(filename);

	// Set up the scroll pane
	ScrollablePicture picture = new ScrollablePicture(i);
	JScrollPane pictureScrollPane = new JScrollPane(picture);

	int height = i.getIconHeight();
	if (i.getIconHeight() > LocalSettings.getGraphvizScreenHeight())
	    height = LocalSettings.getGraphvizScreenHeight();

	int width = i.getIconWidth() + 20;
	if (i.getIconWidth() > LocalSettings.getGraphvizScreenWidth())
	    width = LocalSettings.getGraphvizScreenWidth();

	pictureScrollPane.setPreferredSize(new Dimension(width, height));
	pictureScrollPane.setViewportBorder(BorderFactory
		.createLineBorder(Color.black));

	frame.add(pictureScrollPane);

	//Display the window.
	frame.pack();
	frame.setVisible(true);

	x = frame.getWidth() + frame.getX();
	return frame;
    }

    /*
     * Given a DOT file, converts it into a PNG image and displays it.
     * @param 	inputFile the DOT file 
     */
    public static JFrame displayGraph(String inputFullPath) {

	if (Settings.GENERAL_GENERATE_GRAPHS) {
	    final String pngFile = inputFullPath.replaceAll("dot", "png");
	    convertDOT2PNG(inputFullPath, pngFile);

	    final String PSFile = inputFullPath.replaceAll("dot", "ps");
	    convertDOT2PS(inputFullPath, PSFile);

	    if (Settings.DISPLAY_GRAPHS)
		return doDisplayGraph(pngFile);
	    else
		logger.finest("Graph " + inputFullPath
			+ " not displayed due to ini settiings.");
	    return null;

	} else
	    logger.finest("Graph " + inputFullPath
		    + " not generated due to ini settiings.");
	return null;
    }

    public static void main(String args[]) {
	Settings.initialize(new Options(args));
	displayGraph("test.dot");
    }
}
