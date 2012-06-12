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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.StreamHandler;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.common.options.Options;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;

public class LoggerSetup {

    private static Logger logger;

    public static void setupLoggers() {
	Logger root = Logger.getLogger("");
	Handler[] handlers = root.getHandlers();
	for (int i = 0; i < handlers.length; i++) {
	    root.removeHandler(handlers[i]);
	    //handlers[i].setFormatter(new MessageFirstFormatter());
	    //handlers[i].setLevel(Level.INFO);
	}
	Handler stream = new StreamHandler(System.out,
		new MessageFirstFormatter());
	stream.setLevel(Level.ALL);
	root.addHandler(stream);
	handlers = root.getHandlers();
	//System.out.println(handlers.length);
	logger = Logger.getLogger(LoggerSetup.class.getName());
    }

    public static void flush() {
	Logger root = Logger.getLogger("");
	Handler[] handlers = root.getHandlers();
	for (int i = 0; i < handlers.length; i++) {
	    handlers[i].flush();
	}
    }

    public static void setMainLogFile(String file) {
	Logger log = Logger.getLogger("");
	if (file != null)
	    try {
		Handler handle = new FileHandler(file);
		handle.setFormatter(new DiasFormatter());
		handle.setLevel(Level.ALL);
		log.addHandler(handle);
	    } catch (IOException e) {
		Utils.handleCriticalException(e);
	    }
    }

    public static void addMemoryHandler(String file) {
	Logger log = Logger.getLogger("");
	if (file != null)
	    try {
		FileHandler dump = new FileHandler(file);
		dump.setLevel(Level.ALL);
		dump.setFormatter(new DiasFormatter());
		Handler memory = new MemoryHandler(dump, 1000, Level.SEVERE);
		memory.setLevel(Level.ALL);
		memory.setFormatter(new DiasFormatter());
		log.addHandler(memory);
	    } catch (IOException e) {
		Utils.handleCriticalException(e);
	    }
    }

    /** 
     * Set the maximun level that will be passed to the console
     * @param level
     */
    public static void setConsoleLevel(Level level) {
	Logger log = Logger.getLogger("");
	Handler[] handlers = log.getHandlers();
	//System.out.println(handlers.length+"|"+logPath+"|");
	for (int i = 0; i < handlers.length; i++) {
	    if ((handlers[i] instanceof ConsoleHandler)) {
		handlers[i].setLevel(level);
		logger.config("Max Console log level set to:"
			+ level.toString());
	    }
	    if ((handlers[i] instanceof StreamHandler)) {
		handlers[i].setLevel(level);
		logger
			.config("Max Stream log level set to:"
				+ level.toString());
	    }

	}
    }

    /** 
     * Set the maximun level that will be passed to the file
     * @param level
     */
    public static void setFileLevel(Level level, String logPath) {
	Logger log = Logger.getLogger(logPath);
	Handler[] handlers = log.getHandlers();
	for (int i = 0; i < handlers.length; i++) {
	    if ((handlers[i] instanceof FileHandler)) {
		handlers[i].setLevel(level);
	    }
	}
	logger.config("Max File log level set to:" + level.toString());
    }

    public static Level toLevel(String level) {
	if (level == null)
	    return null;
	if (level.equalsIgnoreCase("all"))
	    return Level.ALL;
	if (level.equalsIgnoreCase("severe"))
	    return (Level.SEVERE);
	if (level.equalsIgnoreCase("warning"))
	    return (Level.WARNING);
	if (level.equalsIgnoreCase("info"))
	    return (Level.INFO);
	if (level.equalsIgnoreCase("config"))
	    return (Level.CONFIG);
	if (level.equalsIgnoreCase("fine"))
	    return (Level.FINE);
	if (level.equalsIgnoreCase("finer"))
	    return (Level.FINER);
	if (level.equalsIgnoreCase("finest"))
	    return (Level.FINEST);
	if (level.equalsIgnoreCase("off"))
	    return (Level.OFF);
	return null;
    }

    public static void setConsoleLevel(String level) {
	setConsoleLevel(toLevel(level));
	//logger.config("Console level set to "+level+toLevel(level));
    }

    public static void setFileLevel(String level) {
	setFileLevel(toLevel(level), "");
	logger.config("File level set to " + level);
    }

    //public static void setFileLevel (String level, String logPath)
    //{
    //	setFileLevel(toLevel(level),logPath); 
    //}

    public static void setLogLevel(String levelName, String logPath) {
	Logger log = Logger.getLogger(logPath);
	Level level = toLevel(levelName);
	if (level == null)
	    logger.warning("Unable to create level \"" + levelName
		    + "\" for \"" + logPath + "\"");
	log.setLevel(level);
	if (logPath.equals(""))
	    logger.config("Log level " + level + " set for root");
	else
	    logger.config("Log level " + level + " set for " + logPath);
    }

    public static void main(String[] args) {
	//System.out.println("how?");
	Settings.initialize(new Options(args));
	//setupLoggers();
	//System.out.println("how?");
	//root.setLevel(Level.INFO);
	//logger.setLevel(Level.CONFIG);
	//System.out.println("My level ="+logger.getLevel());
	//Logger parent = logger.getParent();
	//System.out.println("Parent level ="+parent.getName());
	//System.out.println("Parent level ="+parent.getLevel());
	logger.severe("Major disaster1");
	logger.warning("Potential problem2");
	logger.info("Standard output3");
	logger.config("Some config notes");
	logger.fine("Fine detail");
	logger.finer("Finer detail");
	logger.finest("Finest detail");
	/**/
	Utils.handleCriticalException(new RuntimeException("oh oh"));
	/*
	 Logger base = Logger.getLogger("org.jdom");
	 Logger elt =  Logger.getLogger("org.jdom.Element");
	 Logger attr = Logger.getLogger("org.jdom.Attribute");
	 //	 base == elt.getParent()

	 //Handler[] handlers = base.getHandlers(); 
	 //for (int i = 0; i < handlers.length; i++) 
	 //{
	 //	System.out.println (handlers[i]);
	 //   handlers[i].setFormatter(new DiasFormatter());
	 //}

	 
	 elt.info("Displayed 1");
	 attr.info("Displayed 2");
	 base.setLevel(Level.SEVERE);
	 elt.info("Hidden 1");
	 attr.info("Hidden 2");
	 elt.setLevel(Level.INFO);
	 elt.info("Displayed 3");
	 attr.info("Hidden 3");
	 elt.setLevel(null);
	 elt.info("Hidden 4");

	 /**/
    }
}