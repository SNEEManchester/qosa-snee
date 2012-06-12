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

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;

/*
 * @author Christian Brenninkmeijer 
 */
public final class TreeDisplayer extends JPanel implements TreeSelectionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JTree tree;

    private static final boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static final boolean playWithLineStyle = false;

    private static final String lineStyle = "Horizontal";

    //Optionally set the look and feel.
    private static final boolean useSystemLookAndFeel = false;

    private TreeDisplayer(final Operator op) {
	super(new GridLayout(1, 0));

	//Create the nodes.
	final DefaultMutableTreeNode top = new DefaultMutableTreeNode(op
		.getText());

	this.addNodes(top, op);

	//Create a tree that allows one selection at a time.
	this.tree = new JTree(top);
	this.tree.getSelectionModel().setSelectionMode(
		TreeSelectionModel.SINGLE_TREE_SELECTION);

	//Listen for when the selection changes.
	this.tree.addTreeSelectionListener(this);

	if (playWithLineStyle) {
	    System.out.println("line style = " + lineStyle);
	    this.tree.putClientProperty("JTree.lineStyle", lineStyle);
	}

	//Create the scroll pane and add the tree to it. 
	final JScrollPane treeView = new JScrollPane(this.tree);

	final Dimension minimumSize = new Dimension(100, 50);
	treeView.setMinimumSize(minimumSize);
	this.add(treeView);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(final TreeSelectionEvent e) {
	final DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.tree
		.getLastSelectedPathComponent();

	if (node == null) {
	    return;
	}

	final Object nodeInfo = node.getUserObject();
	if (DEBUG) {
	    System.out.println("In TreeDisplayer"+nodeInfo.toString());
	}
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void show(final Operator op) {
	if (useSystemLookAndFeel) {
	    try {
		UIManager.setLookAndFeel(UIManager
			.getSystemLookAndFeelClassName());
	    } catch (final Exception e) {
		System.err.println("Couldn't use system look and feel.");
	    }
	}

	//Create and set up the window.
	final JFrame frame = new JFrame("Tree Display");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	//Create and set up the content pane.
	final TreeDisplayer newContentPane = new TreeDisplayer(op);
	newContentPane.setOpaque(true); //content panes must be opaque
	frame.setContentPane(newContentPane);

	//Display the window.
	frame.pack();
	frame.setVisible(true);
    }

    private void addNodes(final DefaultMutableTreeNode hereNode,
	    final Operator hereOperator) {
	final Operator[] ops = (Operator[]) hereOperator.getInputs();
	for (Operator element : ops) {
	    final DefaultMutableTreeNode child = new DefaultMutableTreeNode(
		    element.getText());
	    hereNode.add(child);
	    this.addNodes(child, element);
	}
    }

}
