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
package uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operator_rewriting;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;

import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.LocalSettings;
import uk.ac.manchester.cs.diasmc.querycompiler.Settings;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.operators.Operator;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorLexer;
import uk.ac.manchester.cs.diasmc.querycompiler.
	translation_rewriting.operatorTranslator.SNEEqlOperatorParser;

/**
 * Suuport class to use Haskell to generate the operatos.
 * Calls the Haskell parser and translator.
 * Reads the Haskell output file and builds operators
 * @author Christian
 *
 */
public final class HaskellOperatorRewriter {

	/** Hides default constructor. */
	private HaskellOperatorRewriter() {	
	}
	
    /**
     * Logger for this class.
     */
	private static Logger logger = 
		Logger.getLogger(HaskellOperatorRewriter.class.getName());

    /**
     * Invokes the parser. 
     * @param inputStream Pointer to file holding the Haskell output. 
     * @return Operators in ANTRL.AST format.
     */
    private static AST parse(final InputStream inputStream) {
    	try	{
    		final SNEEqlOperatorLexer l = new SNEEqlOperatorLexer(inputStream);
    		final SNEEqlOperatorParser p = new SNEEqlOperatorParser(l);
    		//set AST classes for various node types
    		p.operator(); 
    		return p.getAST(); 
    	} catch (final Exception e) {
    		Utils.handleCriticalException(e);
    		return null;
    	}
	}

    /**
     * Runs the Haskell parser and translator.
     * @param inputFullPath file holding the query.
     * @param outputFullPath file to write the operatos to.
     */
    private static void runHaskell(final String inputFullPath,
    	    final String outputFullPath) {
    	ArrayList<String> args = new ArrayList<String>();
    	args.add(LocalSettings.getRunHugsExe()); 
    	args.add(Settings.getHaskellMain()); 
    	args.add(inputFullPath);
    	args.add(Settings.INPUTS_SCHEMA_FILE);
    	//args.add(Settings.getHaskellDeclFile());
    	args.add(outputFullPath);
    	args.add("True");
    	Utils.runExternalProcess(args);
    }

    /**
     * Main funtions for using Haskell to generate operators. 
     * @param fullQueryPath file holding the query.
     * @param outputDirectory directory to write the operatos to.
     * @return The Operators that this query represents.
     */
    public static Operator generateOperators(final String fullQueryPath, 
    		final String outputDirectory) {
    	try {
    		String fullOperatorPath = outputDirectory + "operators.txt";
    		runHaskell(fullQueryPath, fullOperatorPath);
    		// Parse query and produce Abstract Syntax Tree 
    		final InputStream in = new FileInputStream(fullOperatorPath);
    		final AST ast = parse(in);
    		logger.info(ast.toStringList());
    		if (Settings.DISPLAY_AST && Settings.DISPLAY_GRAPHS) {
    			final JFrame astframe = new ASTFrame("Syntax Tree", ast);
    			astframe.setVisible(true);
    		}	
    		return OperatorFactory.convertAST(ast);
        } catch (final Exception e) {
        	Utils.handleQueryException(e);
        }
        return null;
    }
}
