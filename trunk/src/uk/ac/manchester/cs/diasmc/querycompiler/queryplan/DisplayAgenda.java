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
package uk.ac.manchester.cs.diasmc.querycompiler.queryplan;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.graph.ScrollablePicture;
import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;
import uk.ac.manchester.cs.diasmc.querycompiler.QueryCompiler;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.whenScheduling.vanilla.VWhenScheduling;

public class DisplayAgenda {

    private static final Logger logger = Logger.getLogger(VWhenScheduling.class
	    .getName());;

    private static final long serialVersionUID = 1L;

    private static final int CELL_WIDTH = 100;

    private static final int CELL_HEIGHT = 20;

    DAF daf;

    Agenda agenda;

    private boolean useMilliSeconds;
    
    private HashSet<Long> timesToSkip = new HashSet<Long>();
    
    public DisplayAgenda(final Agenda agenda, final DAF daf, final boolean useMilliSeconds) {
	this.agenda = agenda;
	this.daf = daf;
	this.useMilliSeconds = useMilliSeconds;
	this.identifyTimesToSkip();
    }

    
    private void identifyTimesToSkip() {
    	//TODO: Need to add option to hide/show certain types of tasks.
    	Iterator<Long> startTimeIter = this.agenda.startTimeIterator();
    	while (startTimeIter.hasNext()) {
    		long startTime = startTimeIter.next();
    		Iterator<Task> taskIter = this.agenda.taskIterator(startTime);
    		boolean radioOnOffTasksOnly = true;
    		while (taskIter.hasNext()) {
    			Task task = taskIter.next();
    			if ((task instanceof FragmentTask) || 
    					(task instanceof CommunicationTask)) {
    				radioOnOffTasksOnly = false;
    				break;
    			}
    		}
    		if (radioOnOffTasksOnly) {
    			this.timesToSkip.add(startTime);
    		}
    	}
    }
    
    // computes the width of the schedule image
    private int computeWidth() {
    	
    	Iterator<Site> sitesIterator = this.agenda.siteIterator();
    	int sitesCount = 0;
    	while (sitesIterator.hasNext()) {
    		sitesIterator.next();
    		sitesCount++;
    	}
	return (sitesCount + 1) * CELL_WIDTH;
    }

    // computes the height of the schedule image
    private int computeHeight() {
	return (this.agenda.getStartTimes().size() - 
			this.timesToSkip.size() + 3) * CELL_HEIGHT;
    }

    private void generatePNGImage(final String outputDirName,
	    final String outputFileName) {

	final BufferedImage offImage = new BufferedImage(this.computeWidth(),
		this.computeHeight(), BufferedImage.TYPE_INT_RGB);
	final Graphics2D g2 = offImage.createGraphics();

	g2.setColor(Color.WHITE);
	g2.fill(new Rectangle(0, 0, this.computeWidth(), this.computeHeight()));

	int xpos = 10;
	int ypos = 20;

	g2.setColor(Color.BLACK);
	Iterator<Long> startTimeIter = this.agenda.startTimeIterator();
	while (startTimeIter.hasNext()) {
	    final Long startTime = startTimeIter.next();
	    
	    if (this.timesToSkip.contains(startTime)) {
	    	continue;
	    }
	    
	    g2.setFont(new Font("Arial", Font.BOLD, 12));
	    
	    if (this.useMilliSeconds) {
	    	g2.drawString(new Long(Agenda.bmsToMs(startTime)).toString(), xpos, ypos + 12);
	    } else {
	    	g2.drawString(startTime.toString(), xpos, ypos + 12);	
	    }
	    g2.setFont(new Font("Arial", Font.PLAIN, 12));
	    ypos += CELL_HEIGHT;
	}

	xpos = 50;

	final Iterator<Site> siteIter = this.agenda.getDAF().getRoutingTree()
		.siteIterator(TraversalOrder.POST_ORDER);
	while (siteIter.hasNext()) {
	    final Site site = siteIter.next();

	    ypos = 20;
	    g2.setColor(Color.BLACK);
	    g2.setFont(new Font("Arial", Font.BOLD, 12));
	    g2.drawString("Node " + site.getID(), xpos + 15, ypos - 5);

	    startTimeIter = this.agenda.startTimeIterator();

	    g2.setFont(new Font("Arial", Font.PLAIN, 12));

	    if (this.agenda.hasTasks(site)) {
		final Iterator<Task> taskIter = this.agenda.taskIterator(site);
		while (taskIter.hasNext()) {
		    final Task task = taskIter.next();

		    if (this.timesToSkip.contains(task.getStartTime())) {
		    	continue;
		    }
		    
		    Long sTime;
		    do {
			sTime = startTimeIter.next();

			if (this.timesToSkip.contains(sTime)) {
		    	continue;
		    }
			
			ypos += CELL_HEIGHT;
		    } while (sTime.intValue() != task.getStartTime());

		    if (task instanceof CommunicationTask || task instanceof RadioOnTask ||
		    		task instanceof RadioOffTask) {
			g2.setColor(Color.YELLOW);
		    } else {
			g2.setColor(Color.WHITE);
		    }
		    
		    if (!(task instanceof RadioOffTask)) {
			    g2.fill(new Rectangle(xpos, ypos - CELL_HEIGHT, CELL_WIDTH,
				    CELL_HEIGHT));
			    g2.setColor(Color.BLUE);
			    g2.draw(new Rectangle(xpos, ypos - CELL_HEIGHT, CELL_WIDTH,
				    CELL_HEIGHT));
			    g2.drawString(task.toString(), xpos + 12, ypos + 12
				    - CELL_HEIGHT);
		    }
		}

		xpos += CELL_WIDTH;
	    }
	}
    g2.drawString(agenda.getProvenanceString(), 25, ypos + CELL_HEIGHT);
    
	try {
	    boolean status;

	    final File outputfile = new File(outputDirName, outputFileName);
	    status = ImageIO.write(offImage, "png", outputfile);

	    if (status == false) {
		logger.severe("No png writer found for schedule image type");
	    }

	} catch (final IOException e) {
	}

    }

    public final void exportAsLatex(final String outputFileName) {
    	try {
    	    final PrintWriter out = new PrintWriter(new BufferedWriter(
    		    new FileWriter(outputFileName)));

    	    out.println("%This latex has been generated by the SQNC optimizer");
    	    out.println("\\scriptsize");

    	    int siteCount = 0;
    	    final StringBuffer siteStringBuff = new StringBuffer();

    		Iterator<Site> siteIter = this.agenda.getDAF().getRoutingTree()
    		.siteIterator(TraversalOrder.POST_ORDER);
    	    while (siteIter.hasNext()) {
    		final Site site = siteIter.next();
    		siteStringBuff.append("& " + site.getID());
    		siteCount++;
    	    }

    	    out.println("\\begin{tabular}{|l|" + Utils.pad("c", siteCount)
    		    + "|}");
    	    //out.println("\\begin{tabular*}{6.4cm}{|p{0.8cm}|"+Utils.pad("p{0.4cm}",siteCount)+"|}");
    	    //out.println("\\begin{tabular}{|p{0.5cm}|"+Utils.pad("p{0.14cm}",siteCount)+"|}");
    	    out.println("\\hline");

    	    //temporary for ICDE'08 paper
    	    //out.println("\\multirow{2}{*}{\\textbf{Time}} & \\multicolumn{"+siteCount+"}{|c|}{\\textbf{Sites}} \\\\");
    	    out.println("\\multirow{2}{*}{\\mytextsf{Time}} & \\multicolumn{"
    		    + siteCount + "}{|c|}{\\mytextsf{Sites}} \\\\");
    	    out.println(siteStringBuff.toString() + " \\\\");
    	    out.println("\\hline");

    	    final Iterator<Long> startTimeIter = this.agenda.startTimeIterator();
    	    while (startTimeIter.hasNext()) {
    		final long startTime = startTimeIter.next().longValue();
    		
    		if (this.timesToSkip.contains(startTime)) {
    			continue;
    		}
    		
    		out.print("\\texttt{" + Agenda.bmsToMs(startTime) + "} ");

    		siteIter = this.agenda.getDAF().getRoutingTree().siteIterator(TraversalOrder.POST_ORDER);
    		while (siteIter.hasNext()) {
    		    final Site site = siteIter.next();

    		    final Task t = this.agenda.getTask(startTime, site);
    		    if (t == null) {
    			out.print("& ");
    		    } else if (t instanceof SleepTask) {
    			out.print("& \\multicolumn{" + siteCount
    				+ "}{|c|}{sleeping}");
    			break;
    		    } else if (t instanceof CommunicationTask) {
    			final CommunicationTask ct = (CommunicationTask) t;
    			if (ct.getMode() == CommunicationTask.RECEIVE) {
    			    out
    				    .print("& \\textit{rx" + ct.getSourceID()
    					    + "} ");
    			} else {
    			    out.print("& \\textit{tx" + ct.getDestID() + "} ");
    			}
    		    } else if (t instanceof FragmentTask) {
    			final FragmentTask ft = (FragmentTask) t;
    			if (ft.getFragment().isLeaf()) {
    			    out.print("& \\texttt{F" + ft.getFragment().getID()
    				    + "$_{" + ft.getOccurrence() + "}$} ");
    			} else {
    			    out.print("& \\texttt{F" + ft.getFragment().getID()
    				    + "} ");
    			} 
    			} else {
    				out.print("& ");
    		    }
    		}
    		out.println(" \\\\");
    	    }

    	    out.println("\\hline");
    	    out.println("\\end{tabular}");
    	    out.println("%End of generated Latex");
    	    out.close();
    	} catch (final IOException e) {
    	    Utils.handleCriticalException(e);
    	}
        }

        public final void exportAsLatex(final String outputDirName,
    	    final String outputFileName) {
    	this.exportAsLatex(outputDirName + outputFileName);
        }

    
    public final JFrame display(final String outputDirName,
	    final String outputFileName) {

	//Graphics2D g2 = (Graphics2D) g;

	this.exportAsLatex(outputDirName, outputFileName + ".tex");
	this.generatePNGImage(outputDirName, outputFileName + ".png");

	if (Settings.DISPLAY_GRAPHS) {
	    //Make sure we have nice window decorations.
	    JFrame.setDefaultLookAndFeelDecorated(true);

	    //Create and set up the window.
	    final JFrame frame = new JFrame(outputFileName + " task schedule");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    final ImageIcon i = new ImageIcon(QueryCompiler.queryPlanOutputDir
		    + outputFileName + ".png");

	    // Set up the scroll pane
	    final ScrollablePicture picture = new ScrollablePicture(i);
	    final JScrollPane pictureScrollPane = new JScrollPane(picture);

	    int height = i.getIconHeight();
	    if (i.getIconHeight() > LocalSettings.getGraphvizScreenHeight()) {
		height = LocalSettings.getGraphvizScreenHeight();
	    }

	    int width = i.getIconWidth() + 20;
	    if (i.getIconWidth() > LocalSettings.getGraphvizScreenWidth()) {
		width = LocalSettings.getGraphvizScreenWidth();
	    }

	    pictureScrollPane.setPreferredSize(new Dimension(width, height));
	    pictureScrollPane.setViewportBorder(BorderFactory
		    .createLineBorder(Color.black));

	    frame.add(pictureScrollPane);

	    //Display the window.
	    frame.pack();
	    frame.setVisible(true);

	    return frame;
	} else {
	    return null;
	}
    }
}
