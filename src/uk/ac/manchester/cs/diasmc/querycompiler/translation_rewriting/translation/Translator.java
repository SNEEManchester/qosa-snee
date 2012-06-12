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
package uk.ac.manchester.cs.diasmc.querycompiler.translation_rewriting.translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import uk.ac.manchester.cs.diasmc.common.Utils;
import uk.ac.manchester.cs.diasmc.querycompiler.Constants;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.AttributeType;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.LookupException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SourceDoesNotExistException;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.SourceMetadata;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.schema.Types;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.Units;
import uk.ac.manchester.cs.diasmc.querycompiler.metadata.units.UnrecognizedUnitException;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SNEEqlParser;
import uk.ac.manchester.cs.diasmc.querycompiler.parsing_typeChecking.SNEEqlParserTokenTypes;
import uk.ac.manchester.cs.diasmc.querycompiler.qos.QoSSpec;
import antlr.collections.AST;

/**
 * Handles translation from a type checked abstract syntax tree
 * into suitable input for the logical optimiser. 
 * @author Christian Brenninkmeijer and Steven Lynden
 */
public class Translator {
    private final Logger logger = Logger.getLogger(Translator.class.getName());

    private SchemaMetadata metaData;

    private final HashSet<OldAttribute> groupByList = new HashSet<OldAttribute>();

    private final HashSet<OldAttribute> attributes = new HashSet<OldAttribute>();

    private final HashSet<Aggregate> aggregates = new HashSet<Aggregate>();

    private String subQueryName;

    private SourceMetadata queryMetaData;

    private Constants.XSTREAM reportType;

    /*
     * Sub-queries, indexed by variable name.
     */
    private final Hashtable<String, SubQuery> subQueries = new Hashtable<String, SubQuery>();

    /*
     * A list of fields comprising the result tuple, or SELECT list.
     * Index by varName;
     */
    private Hashtable<String, Variable> resultTuple = new Hashtable<String, Variable>();

    /*
     * Relations in the FROM clause, indexed by variable name.
     * Used by the logical optimiser to build the scans and such
     * Could be a Relation or a SubQuery
     */
    private Hashtable<String, Source> sources;

    //Could be a rowWindow or time window
    private final Hashtable<String, Element> windows = new Hashtable<String, Element>();

    /*
     * A list of predicates from the WHERE clause.
     */
    private final ArrayList<Predicate> predicates = new ArrayList<Predicate>();

    /**/

    //Tree is expected to be a query
    public Translator(AST tree, final SchemaMetadata schemaMetadata,
	    long acquisitionInterval) {
	this.metaData = schemaMetadata;
	if (tree.getType() == SNEEqlParserTokenTypes.QUERY) {
	    tree = tree.getFirstChild();
	}
	this.queryMetaData = new SourceMetadata();
	try {
	    this.buildQuery(tree, acquisitionInterval);
	} catch (final TranslationException e) {
	    Utils.handleCriticalException(e);
	}
    }

    //Tree is expected to be a select
    private Translator(final AST tree, final String subQueryName,
	    final SchemaMetadata metaData, final long acquisitionInterval)
	    throws TranslationException {
	this.subQueryName = subQueryName;
	this.metaData = metaData;
	this.queryMetaData = new SourceMetadata(subQueryName);
	this.buildQuery(tree, acquisitionInterval);
    }

    private void buildQuery(AST tree, final long acquisitionInterval)
	    throws TranslationException {
	//		symbolTable = new SymbolTable();
	this.sources = new Hashtable<String, Source>();
	AST selectAST = null;
	AST fromAST = null;
	AST whereAST = null;
	AST groupbyAST = null;
	AST havingAST = null;
	while (tree != null) {
	    final int type = tree.getType();
	    switch (type) {
	    case SNEEqlParserTokenTypes.SELECT:
		if (selectAST != null) {
		    throw new TranslationException(
			    "More than one SELECT Clause found");
		}
		selectAST = tree;
		break;
	    case SNEEqlParserTokenTypes.FROM:
		if (fromAST != null) {
		    throw new TranslationException(
			    "More than one FROM Clause found");
		}
		fromAST = tree;
		break;
	    case SNEEqlParserTokenTypes.WHERE:
		if (whereAST != null) {
		    throw new TranslationException(
			    "More than one WHERE Clause found");
		}
		whereAST = tree;
		break;
	    case SNEEqlParserTokenTypes.GROUPBY:
		if (groupbyAST != null) {
		    throw new TranslationException(
			    "More than one GROUP BY Clause found");
		}
		groupbyAST = tree;
		break;
	    case SNEEqlParserTokenTypes.HAVING:
		if (havingAST != null) {
		    throw new TranslationException(
			    "More than one HAVING Clause found");
		}
		havingAST = tree;
		break;
	    case SNEEqlParserTokenTypes.MATERIALISE:
		System.out.println("Hi Farhana");
		break;
	    default:
		throw new TranslationException(
			"Unexpected token in main clause " + tree);
	    }
	    tree = tree.getNextSibling();
	}
	this.translateFrom(fromAST, acquisitionInterval);
	this.translateSelect(selectAST, acquisitionInterval);
	if (whereAST == null) {
	    this.logger.finest("No Where clause found");
	} else {
	    this.translateWhere(whereAST, false, acquisitionInterval);
	}
	if (groupbyAST == null) {
	    this.logger.finest("No GROUP BY clause found");
	} else {
	    this.translateGroupby(groupbyAST);
	}
	if (havingAST == null) {
	    this.logger.finest("No HAVING clause found");
	} else {
	    this.translateWhere(whereAST, true, acquisitionInterval);
	}
	//check that groupby
	this.logger.finest("aggregate size = " + this.aggregates.size());
	if (this.aggregates.size() > 0) {
	    this.logger.finest("Groupby list = " + this.groupByList);
	    this.logger.finest("Attributes = " + this.attributes);
	    final Iterator<OldAttribute> atts = this.attributes.iterator();
	    while (atts.hasNext()) {
		final Iterator<OldAttribute> groups = this.groupByList.iterator();
		final OldAttribute attTemp = atts.next();
		if (!(attTemp instanceof QueryEvaluationTime)) {
		    boolean missing = true;
		    while (groups.hasNext()) {
			final OldAttribute groupTemp = groups.next();
			if (attTemp.same(groupTemp)) {
			    missing = false;
			}
		    }
		    if (missing) {
			throw new TranslationException(
				"Query includes aggeragates so non aggregated attribute ["
					+ attTemp
					+ "] must be in group by list");
		    }
		}
	    }
	}
    }

    private Element translateWindow(final AST node, final long acquisitionInterval)
	    throws TranslationException {
	if (node == null) {
	    return null;
	}
	Element window;
	if (node.getType() == SNEEqlParserTokenTypes.NEW) {
	    window = new TimeWindow(-(int) acquisitionInterval, 0, 1);
	} else if (node.getType() == SNEEqlParserTokenTypes.NOW) {
	    window = new TimeWindow(0, 0, 1);
	} else if (node.getType() == SNEEqlParserTokenTypes.UNBOUNDED) {
	    window = new TimeWindow(Integer.MIN_VALUE, 0, 1);
	//else if (node.getType() == SNEEqlParserTokenTypes.RANGE)
	//{
	//	window = convertTimeWindow(node);
	//}
	} else if (node.getType() == SNEEqlParserTokenTypes.AT) {
	    window = this.translateAtWindow(node);
	} else if (node.getType() == SNEEqlParserTokenTypes.FROM) {
	    window = this.translateFromWindow(node);
	} else if (node.getType() == SNEEqlParserTokenTypes.RANGE) {
	    window = this.translateRangeWindow(node);
	} else {
	    return null;
	}
	return window;
    }

    private Element translateAtWindow(final AST atAST)
	    throws TranslationException {
	final WindowValue at = new WindowValue(atAST);
	final AST slideAST = atAST.getNextSibling();
	WindowValue slide;
	if (slideAST == null) {
	    slide = new WindowValue(1);
	} else {
	    slide = new WindowValue(slideAST);
	    at.setUndefinedUnit(slide);
	}
	if (at.isRowValue()) {
	    if (slide.isRowValue()) {
		return new RowWindow(at.getValue(), at.getValue(), true, slide
			.getValue());
	    } else {
		return new RowWindow(at.getValue(), at.getValue(), false, slide
			.getValue());
	    }
	} else if (slide.isRowValue()) {
	    throw new TranslationException(
		    "Can not yet mix time RANGE with row SLIDE in RANGE X SLIDE z window.");
	} else {
	    return new TimeWindow(at.getValue(), at.getValue(), slide
		    .getValue());
	}
    }

    private Element translateFromWindow(final AST fromAST)
	    throws TranslationException {
	final WindowValue from = new WindowValue(fromAST);
	final AST toAST = fromAST.getNextSibling();
	if (toAST == null) {
	    throw new TranslationException(
		    "To part missing in FROM X to Y window. Found null");
	}
	if (toAST.getType() != SNEEqlParserTokenTypes.TO) {
	    throw new TranslationException(
		    "To part missing in FROM X to Y window. Found "
			    + toAST.getText());
	}
	final WindowValue to = new WindowValue(toAST);
	from.setUndefinedUnit(to);
	final AST slideAST = toAST.getNextSibling();
	WindowValue slide;
	if (slideAST == null) {
	    slide = new WindowValue(1);
	} else {
	    slide = new WindowValue(slideAST);
	    from.setUndefinedUnit(slide);
	    to.setUndefinedUnit(slide);
	}
	if (from.isRowValue()) {
	    if (to.isRowValue()) {
		if (slide.isRowValue()) {
		    return new RowWindow(from.getValue(), to.getValue(), true,
			    slide.getValue());
		} else {
		    return new RowWindow(from.getValue(), to.getValue(), false,
			    slide.getValue());
		}
	    } else {
		throw new TranslationException(
			"Can not yet mix row FROM with time TO in FROM X TO Y window.");
	    }
	} else if (to.isRowValue()) {
	    throw new TranslationException(
		    "Can not yet mix time FROM with row TO in FROM X TO Y window.");
	} else if (slide.isRowValue()) {
	    throw new TranslationException(
		    "Can not yet mix time FROM with row SLIDE in FROM X TO Y SLIDE z window.");
	} else {
	    return new TimeWindow(from.getValue(), to.getValue(), slide
		    .getValue());
	}
    }

    private Element translateRangeWindow(final AST rangeAST)
	    throws TranslationException {
	final WindowValue range = new WindowValue(rangeAST);
	final AST slideAST = rangeAST.getNextSibling();
	WindowValue slide;
	if (slideAST == null) {
	    slide = new WindowValue(1);
	} else {
	    slide = new WindowValue(slideAST);
	    range.setUndefinedUnit(slide);
	}
	if (range.isRowValue()) {
	    if (slide.isRowValue()) {
		return new RowWindow(-range.getValue() + 1, 0, true, slide
			.getValue());
	    } else {
		return new RowWindow(-range.getValue() + 1, 0, false, slide
			.getValue());
	    }
	} else if (slide.isRowValue()) {
	    throw new TranslationException(
		    "Can not yet mix time RANGE with row SLIDE in RANGE X SLIDE z window.");
	} else {
	    return new TimeWindow(-range.getValue() + 1, 0, slide.getValue());
	}
    }

    /*
     * Adds each source to the symbolTable
     */
    private void translateFrom(final AST fromAST, 
    	final long acquisitionInterval)
	    throws TranslationException {
	SourceMetadata fromMetaData;
	//String varSource;
	String varName = "";
	Source source = null;
	if (fromAST == null) {
	    throw new TranslationException("Missing FROM statement");
	}
	AST treeNode = fromAST.getFirstChild();
	if (treeNode == null) {
	    throw new TranslationException("Empty FROM statement");
	}
	while (treeNode != null) {
	    AST sourceNode = null;
	    AST subQueryNode = null;
	    String rename = null;
	    try {
		if (treeNode.getType() == SNEEqlParserTokenTypes.SOURCE) {
		    sourceNode = treeNode;
		} else if (treeNode.getType() == SNEEqlParserTokenTypes.QUERY) {
		    subQueryNode = treeNode;
		} else {
		    throw new TranslationException("Unexpected type "
			    + SNEEqlParser._tokenNames[treeNode.getType()]
			    + " found in from statement: " + treeNode);
		}
		//String varName = subQueryPrefix + varSource;
		final Element window = this.translateWindow(treeNode
			.getFirstChild(), acquisitionInterval);
		treeNode = treeNode.getNextSibling();
		if (treeNode != null) {
		    if (treeNode.getType() == SNEEqlParserTokenTypes.SOURCE_RENAME) {
			rename = treeNode.getText();
			treeNode = treeNode.getNextSibling();
			//ystem.out.println(r.getClass());
		    }
		}
		if (sourceNode != null) {
		    fromMetaData = this.metaData.getSourceMetaData(sourceNode
			    .getText());
		    if (rename == null) {
			varName = sourceNode.getText();
		    } else {
			varName = rename;
		    }
		    try {
			if (fromMetaData.isStream()) {
			    source = new Stream(sourceNode.getText(),
				    fromMetaData, varName);
			} else {
			    source = new Relation(sourceNode.getText(),
				    fromMetaData, varName);
			}
		    } catch (final LookupException e) {
			if (source == null) {
			    throw new TranslationException(
				    "Unable to get type for a source ", e);
			} else {
			    throw new TranslationException(
				    "Unable to get type for source "
					    + source.getName(), e);
			}
		    }
		} else if (subQueryNode != null) {
		    if (rename == null) {
			varName = this.getSubQueryName();
		    } else {
			varName = rename;
		    }
		    final Translator translator = new Translator(subQueryNode
			    .getFirstChild(), varName, this.metaData, acquisitionInterval);
		    source = new SubQuery(translator, varName, translator
			    .getJavaType());
		    fromMetaData = translator.queryMetaData;
		    this.subQueries.put(varName, (SubQuery) source);
		    //set name in metadata
		} else {
		    throw new TranslationException("CODE BUG" + treeNode);
		}
		this.sources.put(varName, source);
		this.logger.finest("Adding source " + varName + " = " + source);
		if (window != null) {
		    this.windows.put(varName, window);
		}
	    } catch (final SourceDoesNotExistException e) {
		e.printStackTrace();
		throw new TranslationException("Source " + sourceNode.getText()
			+ " does not exist.", e);
	    }
	}
    }

    private void translateSelect(final AST tree, final long acquisitionInterval)
	    throws TranslationException {
	if (tree.getText().equalsIgnoreCase("DSTREAM")) {
	    this.reportType = Constants.XSTREAM.DSTREAM;
	} else if (tree.getText().equalsIgnoreCase("ISTREAM")) {
	    this.reportType = Constants.XSTREAM.ISTREAM;
	} else if (tree.getText().equalsIgnoreCase("RSTREAM")) {
	    this.reportType = Constants.XSTREAM.RSTREAM;
	} else if (tree.getText().equalsIgnoreCase("SELECT")) {
	    this.reportType = Constants.XSTREAM.NOSTREAM;
	} else {
	    throw new TranslationException("Unexpected select type.");
	}
	AST treeNode = tree.getFirstChild();
	this.resultTuple = new Hashtable<String, Variable>();
	if (treeNode.getType() == SNEEqlParserTokenTypes.Star) {
	    return;
	}
	Variable result;
	while (treeNode != null) {
	    final AST variable = treeNode;
	    treeNode = treeNode.getNextSibling();
	    String varName = null;
	    if ((treeNode != null)
		    && (treeNode.getType() == SNEEqlParserTokenTypes.SELECT_RENAME)) {
		varName = treeNode.getText();
		//result.rename(varName);
		treeNode = treeNode.getNextSibling();
	    }
	    result = this.translateVariable(variable, varName, true, acquisitionInterval);
	    this.resultTuple.put(result.getName(), result);
	    this.logger.fine("Result " + result + " added.");
	    if (this.subQueryName != null) {
		try {
		    this.logger.finest("Adding attribute " + result.getName()
			    + ":" + varName);
		    this.queryMetaData.addAttribute(result.getName(), result
			    .getType());
		    this.logger.finest("SubQuery Identifer " + result.getName()
			    + " added to MetaData");
		} catch (final LookupException e) {
		    e.printStackTrace();
		    throw new TranslationException(
			    "Error processing select clause", e);
		}
	    }
	}
    }

    private void checkPredicate(final String op, final AttributeType type1,
	    final AttributeType type2) throws TranslationException {
	if (op.equalsIgnoreCase("=")) {
	    if (type1.canEqual(type2)) {
		return;
	    }
	}
	if (type1.canCompare(type2)) {
	    return;
	}
	throw new TranslationException("Predicate " + op
		+ "not confirmed to work with types " + type1 + " and " + type2);
    }

    /*
     * Translate the where clause, which consists
     * of a list of predicates
     */
    private void translateWhere(AST node, final boolean having,
	    final long acquisitionInterval) throws TranslationException {
	this.logger.finest("Entering translateWhere");
	node = node.getFirstChild();
	while (node != null) {
	    final AST arg1 = node.getFirstChild();
	    final AST arg2 = arg1.getNextSibling();
	    //         Predicate predicate = null; //the predicate to be added
	    String operator = node.getText();
	    this.logger.finest("elem1 = " + arg1.getText());
	    this.logger.finest("operator = " + operator);
	    final Variable elem1 = this.translateVariable(arg1, null, having,
	    		acquisitionInterval);
	    final Variable elem2 = this.translateVariable(arg2, null, having,
	    		acquisitionInterval);
	    if (operator.equalsIgnoreCase("in")) {
		if (having) {
		    throw new TranslationException(
			    "In found in HAVING or SELECT clause");
		} else {
		    operator = "=";
		}
	    }
	    this.checkPredicate(operator, elem1.getType(), elem2.getType());
	    final Predicate predicate = new Predicate(elem1, elem2, operator);
	    this.logger.finest("New Predicate added " + elem1 + operator
		    + elem2);
	    this.predicates.add(predicate);
	    node = node.getNextSibling();
	}
    }

    private AttributeType getJavaType() throws TranslationException {
	//if (name == null)
	//	throw new TranslationException ("No java type available for main query");
	try {
	    return this.queryMetaData.getJavaType();
	} catch (final LookupException e) {
	    throw new TranslationException("Unable to get JavaType.", e);
	}
    }

    private String getSubQueryName() {
	int i = 1;
	while (true) {
	    final String name = "subquery"
		    + Integer.toString(this.subQueries.size() + i);
	    if (this.sources.get(name) == null) {
		return name;
	    }
	    i++;
	}
    }

    private Variable translateVariable(AST node, String name, boolean having,
	    final long acquisitionInterval) throws TranslationException {
	final int type = node.getType();
	String aggregateName = null;
	try {
	    switch (type) {
	    case SNEEqlParserTokenTypes.Int: {
		final Literal temp = new Literal(name, node.getText(), Types
			.getType("integer"));
		this.logger.finest("Created new literal:" + temp);
		return temp;
	    }
	    case SNEEqlParserTokenTypes.Float: {
		final Literal temp = new Literal(name, node.getText(), Types
			.getType("float"));
		this.logger.finest("Created new literal:" + temp);
		return temp;
	    }
	    case SNEEqlParserTokenTypes.TRUE: {
		final Literal temp = new Literal(name, node.getText(), Types
			.getType("boolean"));
		this.logger.finest("Created new literal:" + temp);
		return temp;
	    }
	    case SNEEqlParserTokenTypes.FALSE: {
		final Literal temp = new Literal(name, node.getText(), Types
			.getType("boolean"));
		this.logger.finest("Created new literal:" + temp);
		return temp;
	    }
	    case SNEEqlParserTokenTypes.SELECT: {
		if (name == null) {
		    name = this.getSubQueryName();
		}
		if (having) {
		    throw new TranslationException(
			    "SubQueries found outside of WHERE or FROM clauses");
		}
		final Translator translator = new Translator(node, name,
			this.metaData, acquisitionInterval);
		//set name in MetaData
		final SubQuery q = new SubQuery(translator, name, translator
			.getJavaType());
		this.sources.put(name, q);
		this.subQueries.put(name, q);
		this.logger.finest("created new SubQuery " + q);
		return translator.getResultVariable();
	    }
	    case SNEEqlParserTokenTypes.AGGREGATE: {
		if (!having) {
		    throw new TranslationException(
			    "Aggregate found outside of HAVING or SELECT clauses");
		}
		aggregateName = node.getText();
		node = node.getFirstChild();
	    }
	    }
	} catch (final SchemaMetadataException e) {
	    throw new TranslationException("Required type for "
		    + node.getText() + " not defined", e);
	}
	final OldAttribute attribute = this.translateAttribute(node, name);
	if (aggregateName == null) {
	    if (having) {
		this.attributes.add(attribute);
	    }
	    this.logger.finest("created new Attribute " + attribute);
	    return attribute;
	} else {
	    if (name == null) {
		name = aggregateName;
	    }
	    if (this.subQueryName != null) {
		name = this.subQueryName + '.' + name;
	    }
	    final Aggregate temp = new Aggregate(aggregateName, attribute, name);
	    this.aggregates.add(temp);
	    this.logger.finest("Created new Aggregate " + temp);
	    return temp;
	}
    }

    /**/
    private TimeWindow convertTimeWindow(final AST range)
	    throws TranslationException {
	AST nextAST;
	AST timeName = range.getFirstChild();
	AST amount = timeName.getFirstChild();
	final int begin = -this.convertTime(timeName.getText(), amount
		.getText()) - 1;
	if (begin > 0) {
	    new TranslationException(
		    "Unexpected negative RANGE value in window found by Translator. "
			    + "Negative range values are not allowed as they cause windows to block");
	}
	int until = 0;
	int slide = 1;
	nextAST = timeName.getNextSibling();
	while (nextAST != null) {
	    timeName = nextAST.getFirstChild();
	    amount = timeName.getFirstChild();
	    if (nextAST.getType() == SNEEqlParserTokenTypes.UNTIL) {
		until = -this.convertTime(timeName.getText(), amount.getText());
		if (until > 1) {
		    new TranslationException(
			    "Unexpected negative Until value in window found by Translator. "
				    + "Negative until values are not allowed as they cause windows to block");
		}
		if (until < begin) {
		    new TranslationException(
			    "Unexpected Until value less than Range in window found by Translator. "
				    + "The RANGE value includes the UNTIL so must be greater");
		}
	    } else if (nextAST.getType() == SNEEqlParserTokenTypes.SLIDE) {
		slide = this.convertTime(timeName.getText(), amount.getText());
		if (slide < 1) {
		    new TranslationException(
			    "Unexpected negative or zero slide value in window found by Translator.");
		}
	    } else {
		throw new TranslationException("Unexpected window element:"
			+ nextAST.getText() + " found by Translator");
	    }
	    nextAST = nextAST.getNextSibling();
	}
	return new TimeWindow(begin, until, slide);
    }

    private RowWindow convertRowWindow(final AST row)
	    throws TranslationException {
	final AST amount = row.getFirstChild();
	final int begin = Integer.parseInt(amount.getText());
	if (begin < 0) {
	    new TranslationException(
		    "Unexpected negative ROW value in window found by Translator. "
			    + "Negative range values are not allowed as they cause windows to block");
	}
	int until = 0;
	float slide = 1;
	boolean rowSlide = false;
	AST nextAST = amount.getNextSibling();
	while (nextAST != null) {
	    if (nextAST.getType() == SNEEqlParserTokenTypes.UNTIL) {
		until = Integer.parseInt(nextAST.getFirstChild().getText());
		if (until < 0) {
		    new TranslationException(
			    "Unexpected negative Until value in window found by Translator. "
				    + "Negative until values are not allowed as they cause windows to block");
		}
		if (until < begin) {
		    new TranslationException(
			    "Unexpected Until value less than ROW in window found by Translator. "
				    + "The ROW value includes the UNTIL so must be greater");
		}
	    } else if (nextAST.getType() == SNEEqlParserTokenTypes.SLIDE) {
		final AST child = nextAST.getFirstChild();
		if (child.getType() == SNEEqlParserTokenTypes.Int) {
		    slide = Float.parseFloat(child.getText());
		    rowSlide = true;
		} else {
		    slide = this.convertTime(child.getText(), child
			    .getFirstChild().getText());
		    rowSlide = false;
		}
		if (slide < 1) {
		    new TranslationException(
			    "Unexpected negative or zero slide value in window found by Translator.");
		}
	    } else {
		throw new TranslationException("Unexpected window element:"
			+ nextAST.getText() + " found by Translator");
	    }
	    nextAST = nextAST.getNextSibling();
	}
	return new RowWindow(begin, until, rowSlide, slide);
    }

    private int convertTime(final String timeUnitName, final String amount)
	    throws TranslationException {
	final Units units = Units.getInstance();
	try {
	    final long scalingFactor = units.getTimeScalingFactor(timeUnitName);
	    final float value = Float.parseFloat(amount) * scalingFactor;
	    final int result = (int) value;
	    if (result == value) {
		return result;
	    } else {
		throw new TranslationException("Unable to convert " + value
			+ " " + timeUnitName + " to an integer value");
	    }

	} catch (final UnrecognizedUnitException e) {
	    throw new TranslationException(e.getMessage());
	}
    }

    /**/

    private Source findSource(final String identifier)
	    throws TranslationException {
	Source foundSource = null;
	for (final Enumeration<Source> e = this.sources.elements(); e
		.hasMoreElements();) {
	    //System.out.println(attribute);
	    final Source source = e.nextElement();
	    //System.out.println(source);
	    if (source.hasAttribute(identifier)) {
		if (foundSource == null) {
		    foundSource = source;
		} else {
		    throw new TranslationException("Identifier " + identifier
			    + " exists in two or more sources.");
		}
	    }
	}
	//System.out.println(found);
	if (foundSource == null) {
	    throw new TranslationException("Identifier " + identifier
		    + " does not exist in any source.");
	}
	return foundSource;
    }

    private OldAttribute translateAttribute(final AST node, String name)
	    throws TranslationException {
	this.logger.finest("Entering translateAttribute");
	if (node == null) {
	    throw new TranslationException(
		    "Found null where attribute expected");
	}
	Source source;
	String sourceName;
	String identifier;
	if (node.getType() == SNEEqlParserTokenTypes.Attribute) {
	    final String whole = node.getText();
	    sourceName = whole.substring(0, whole.indexOf('.'));
	    source = this.sources.get(sourceName);
	    if (source == null) {
		throw new TranslationException("Source " + source
			+ " not found");
	    }
	    identifier = whole
		    .substring(whole.indexOf('.') + 1, whole.length());
	} else if (node.getType() == SNEEqlParserTokenTypes.Identifier) {
	    identifier = node.getText();
	    if (identifier.equalsIgnoreCase("time")) {
		if (this.windows.size() > 0) {
		    if (this.subQueryName == null) {
			name = "time";
		    } else {
			name = this.subQueryName + '.' + "time";
		    }
		    try {
			return new QueryEvaluationTime(name);
		    } catch (final SchemaMetadataException e) {
			throw new TranslationException(
				"Type for QueryEvaluationTime not defined", e);
		    }
		}
	    }
	    source = this.findSource(identifier);
	    if (source == null) {
		throw new TranslationException("Source not found for "
			+ identifier);
	    }
	    sourceName = source.getName();
	} else {
	    this.logger.warning("type = " + node.getType());
	    throw new TranslationException(
		    "Unexpected node type found where attribute expected");
	}
	this.logger.finest("Still in translateAttribute");
	if (source.hasAttribute(identifier)) {
	    try {
		final AttributeType type = source.getAttributeType(identifier);
		if (name == null) {
		    if (this.subQueryName == null) {
			name = sourceName + '.' + identifier;
		    } else {
			name = this.subQueryName + '.' + identifier;
		    }
		} else if (this.subQueryName != null) {
		    name = this.subQueryName + '.' + identifier;
		}
		this.logger.finest("making attribute " + identifier);
		this.logger.finest("using source " + source);
		final OldAttribute attribute = new OldAttribute(source, identifier,
			name, type);
		return attribute;
	    } catch (final LookupException e) {
		throw new TranslationException("Unable to handle an attribute",
			e);
	    }
	} else {
	    throw new TranslationException("Source " + source
		    + " does not have identifier " + identifier);
	}
    }

    public Collection<SubQuery> getSubQueries() {
	return this.subQueries.values();
    }

    public Collection<Variable> getResultTuple() {
	this.logger.finest("" + this.resultTuple.values());

	return this.resultTuple.values();
    }

    public Variable getResultVariable() throws TranslationException {
	final Collection<Variable> results = this.resultTuple.values();
	if (results.size() == 1) {
	    final Variable temp = results.iterator().next();
	    temp.setSourceReference(this.subQueryName);
	    return temp;
	}
	throw new TranslationException(
		"SubQueries found in WHERE statement must return exactly one column");
    }

    public ArrayList<Predicate> getPredicates() {
	return this.predicates;
    }

    /**
     * Returns a String representation of the translated Query
     * for debugging purposes
     */
    public String toString() {
	final StringBuffer b = new StringBuffer();
	b.append("[Type: ");
	b.append(this.reportType.toString());
	//switch(reportType){
	//	case Constants.XSTREAM.NOSTREAM : b.append("Normal ");break;
	//	case Constants.XSTREAM.DSTREAM : b.append("DSTREAM ");break;
	//	case Constants.XSTREAM.ISTREAM : b.append("ISTREAM ");break;
	//	case Constants.XSTREAM.RSTREAM : b.append("RSTREAM ");break;
	//	default : b.append("ReportTyye Undefined ");
	//}
	b.append("Result tuple: ");
	b.append(this.getResultTuple().toString());
	if (this.aggregates.size() > 0) {
	    b.append(" -- Aggregate: ");
	    b.append(this.aggregates);
	}
	//relations        
	b.append(" -- From list: ");
	b.append(this.sources);
	//Windows
	if (this.windows.size() > 0) {
	    b.append(" -- Windows: ");
	    b.append(this.windows.toString());
	}

	//predicates
	if (this.predicates.size() > 0) {
	    b.append(" -- Predicates: ");
	    b.append(this.predicates.toString());
	}
	//groupby
	if (this.groupByList.size() > 0) {
	    b.append(" -- Group By: ");
	    b.append(this.groupByList.toString());
	}
	//sub queries
	if (this.getSubQueries().size() > 0) {
	    b.append(Constants.NEWLINE);
	    b.append(" -- Sub queries:: ");
	    //b.append(getSubQueries().toString());
	    final Iterator<SubQuery> it = this.getSubQueries().iterator();
	    while (it.hasNext()) {
		final SubQuery q = it.next();
		b.append(q.getName() + " = ");
		b.append(q.getQuery().toString());
	    }
	}
	b.append("]");
	b.append(Constants.NEWLINE);
	return b.toString();
    }

    public HashSet<Aggregate> getAggregates() {
	return this.aggregates;
    }

    public HashSet<OldAttribute> getGroupByList() {
	return this.groupByList;
    }

    public Collection<Source> getSources() {
	return this.sources.values();
    }

    private void translateGroupby(final AST groupbyAST)
	    throws TranslationException {
	this.logger.finest("Entering translateGroupBY");
	AST node = groupbyAST.getFirstChild();
	while (node != null) {
	    final OldAttribute temp = this.translateAttribute(node, null);
	    this.logger
		    .finest("Adding Attribute " + temp + " to group by List");
	    this.logger.finest("Source: " + temp.getSource());
	    this.groupByList.add(temp);
	    node = node.getNextSibling();
	}
    }

    public Hashtable<String, Element> getWindows() {
	return this.windows;
    }

    public Constants.XSTREAM getType() {
	return this.reportType;
    }

    protected SourceMetadata getMetaData() {
	return this.queryMetaData;
    }

    public int lookupTableCardinality(final String name) throws LookupException {
	final Source source = this.sources.get(name);
	if (source == null) {
	    throw new LookupException("Relation " + name
		    + " not found in the sources");
	}
	return source.getCardinality();
    }
}
