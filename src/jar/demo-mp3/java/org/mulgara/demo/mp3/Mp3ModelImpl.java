/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.demo.mp3;

// Java 2 standard packages
import java.net.URI;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;
import org.jrdf.vocabulary.RDF;
import org.mulgara.demo.mp3.id3.Id3Tag;
import org.mulgara.query.QueryException;

/**
 * Implementation of Mp3Model using iTQL. init() must be called after
 * FileSystemModel is set.
 * 
 * @created 2004-12-03
 * 
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner </a>
 * 
 * @version $Revision: 1.4 $
 * 
 * @modified $Date: 2005/04/20 19:02:50 $
 * 
 * @maintenanceAuthor $Author: newmana $
 * 
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software </A>
 * 
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *            Software Pty Ltd </a>
 * 
 * @licence <a href=" {@docRoot}/../../LICENCE">Mozilla Public License v1.1
 *          </a>
 */
public class Mp3ModelImpl extends AbstractModel implements Mp3Model {

    /**
     * Logger. This is named after the class.
     */
    private final static Logger log = Logger.getLogger(Mp3ModelImpl.class
            .getName());

    /** Listener used to notify caller of events */
    private Mp3ModelListener listener = null;

    /** Graph where Schema information is stored */
    private SchemaModel schemaModel = null;

    /**
     * init
     * 
     * @param context
     *            ModelContext
     * @throws Exception
     */
    public void init(ModelContext context) throws Exception {
        super.init(context);
        if (schemaModel == null) {
            throw new IllegalStateException("SchemaModel must be set before "
                    + "calling init()");
        }
    }

    /**
     * Discover mp3's from the FileSystemModel and loads them.
     * 
     * @param fsModel
     *            FileSystemModel
     * @throws IllegalArgumentException
     * @throws QueryException
     */
    public void loadMp3s(FileSystemModel fsModel)
            throws IllegalArgumentException, QueryException {
        if (fsModel == null) {
            throw new IllegalArgumentException("FileSystemModel is null");
        }
        try {
            checkInitialized();
            //get all files from the filesystem with an extension of mp3
            ClosableIterator<Triple> files = fsModel.findFiles(
                    FileSystemModel.FILE_EXT, Mp3Model.MP3_EXT);
            Mp3Iterator mp3s = new Mp3IteratorImpl(files, newMp3Context());
            loadMp3s(mp3s);
        } catch (Exception exception) {
            throw new QueryException("Failed to retrieve Mp3Files.", exception);
        }
    }

    /**
     * Sets the Graph used to store Schema Information.
     * 
     * @param schemaModel
     *            SchemaModel
     * @throws IllegalArgumentException
     */
    public void setSchemaModel(SchemaModel schemaModel)
            throws IllegalArgumentException {
        if (schemaModel == null) {
            throw new IllegalArgumentException("SchemaModel is null");
        }
        this.schemaModel = schemaModel;
    }

    /**
     * Returns the Graph used to store Schema Information.
     * 
     * @return SchemaModel
     */
    public SchemaModel getSchemaModel() {
        checkInitialized();
        return schemaModel;
    }

    /**
     * Returns all Mp3Files from the filesystem (FileSystemModel)
     * 
     * @return Mp3Iterator
     * @throws QueryException
     */
    public Mp3Iterator getMp3s() throws QueryException {
        try {
            checkInitialized();
            String query = getMp3sQuery();
            ClosableIterator<Triple> mp3s = query(query);
            return new Mp3IteratorImpl(mp3s, newMp3Context());
        } catch (Exception exception) {
            throw new QueryException("Failed to retrieve Mp3Files.", exception);
        }
    }

    /**
     * Returns an Iterator (of Triples) that match any of the properties for the
     * specified mp3.
     * 
     * @param mp3
     *            URIReference
     * @param properties
     *            Iterator
     * @return ClosableIterator
     * @throws QueryException
     */
    public ClosableIterator<Triple> getPropertyStatements(URIReference mp3,
            Iterator<Triple> properties) throws QueryException {
        try {
            checkInitialized();
            String query = getPropertiesQuery(mp3, properties);
            return query(query);
        } catch (Exception exception) {
            throw new QueryException("Failed to find property statements for: "
                    + mp3, exception);
        }
    }

    /**
     * Returns an Iterator containing all the Mp3s (as URIReferenece's) that
     * have the specified metadata properties and values.
     * 
     * @param properties
     *            Iterator metadata predicates (eg. "mulgara:title")
     * @param values
     *            Iterator metadata objects (eg. "Vertigo")
     * @return Mp3Iterator
     * @throws QueryException
     */
    public Mp3Iterator findMp3s(Iterator<PredicateNode> properties, Iterator<ObjectNode> values)
            throws QueryException {
        try {
            checkInitialized();
            //query for mp3s with the specified property
            String query = getFindMp3Query(properties, values);
            ClosableIterator<Triple> mp3s = query(query);
            return new Mp3IteratorImpl(mp3s, newMp3Context());
        } catch (Exception exception) {
            throw new QueryException("Failed to retrieve Mp3Files.", exception);
        }
    }

    /**
     * hasInitialized
     * 
     * @return boolean
     */
    public boolean hasInitialized() {
        return (super.hasInitialized()) && (schemaModel != null);
    }

    /**
     * Returns a query in the following format:
     * <p>
     * 
     * <pre>
     * 
     *  
     *   
     *      select $s $p $o
     *      from &lt;getResource()&gt;
     *      where $s $p $o
     *      and $s &lt;rdf:type&gt; &lt;mulgara:mp3&gt;;
     *    
     *   
     *  
     * </pre>
     * 
     * @return String
     */
    private String getMp3sQuery() {
        StringBuffer query = new StringBuffer();
        query
                .append("select $s <" + RDF.TYPE + ">" + Mp3File.MULGARA_MP3
                        + NEWLINE);
        query.append("from <" + getResource() + "> " + NEWLINE);
        query.append("where $x" + Id3Tag.ID3_URI + " $s" + NEWLINE);
        query.append("and $x $p $o " + NEWLINE);
        query.append("order by $s ; ");
        return query.toString();
    }

    /**
     * Returns a query in the following format:
     * <p>
     * 
     * <pre>
     * 
     *  
     *   
     *      select $s $p $o
     *      from &lt;getResource()&gt;
     *      where $s $p $o
     *      and $s &lt;property-1&gt; &lt;value-1&gt;
     *      and $s &lt;property-2&gt; &lt;value-2&gt;
     *                 ...
     *      and $s &lt;property-n&gt; &lt;value-n&gt; ;
     *    
     *   
     *  
     * </pre>
     * 
     * Where n is the number of statements in the shortest iterator.
     * 
     * @param properties
     *            Iterator containing PredicateNodes
     * @param values
     *            Iterator containing ObjectNodes
     * @return String
     */
    private String getFindMp3Query(Iterator<PredicateNode> properties, Iterator<ObjectNode> values) {

        //if there are no constraints, return all.
        if ((properties == null) || (values == null) || (!properties.hasNext())
                || (!values.hasNext())) {
            return getMp3sQuery();
        }
        String currentProperty = null;
        String currentValue = null;
        StringBuffer query = new StringBuffer();
        query
                .append("select $s <" + RDF.TYPE + ">" + Mp3File.MULGARA_MP3
                        + NEWLINE);
        query.append("from <" + getResource() + "> " + NEWLINE);
        query.append("where $x" + Id3Tag.ID3_URI + " $s" + NEWLINE);
        while (properties.hasNext() && values.hasNext()) {
            currentProperty = asString(" $p", properties.next());
            currentValue = asString(" $o", values.next());
            query.append("and $x" + currentProperty + currentValue + NEWLINE);
        }
        query.append("order by $s ;");
        return query.toString();
    }

    /**
     * Returns a queryString in the following format:
     * 
     * <p>
     * 
     * <pre>
     * 
     *  
     *   
     *     select $s $p $o
     *     from &lt;getResource()&gt;
     *     where $s $p $o
     *     and $s &lt;mulgara:is&gt; &lt;mp3&gt;
     *     and ($p &lt;mulgara:is&gt; &lt;schemaProperty-1&gt;
     *       or $p &lt;mulgara:is&gt; &lt;schemaProperty-2&gt;
     *                 ...
     *       or $p &lt;mulgara:is&gt; &lt;schemaProperty-n&gt;) ;
     *     
     *   
     *  
     * </pre>
     * 
     * where <schemaProperty>is a property of the ID3Tags schema.
     * 
     * @return String
     * @param mp3
     *            URIReference
     * @param properties
     *            Iterator of Triples where the SubjectNode's represent the
     *            schemaProperties.
     */
    private String getPropertiesQuery(URIReference mp3, Iterator<Triple> properties) {

        //if there are no properties, there should be no statements.
        if ((properties == null) || !(properties.hasNext())) {
            return ";";
        }

        Node currentProperty = properties.next().getPredicate();
        StringBuffer query = new StringBuffer("select $s $p $o" + NEWLINE);
        query.append("from <" + getResource().getURI() + ">" + NEWLINE);
        query.append("where $s $p $o" + NEWLINE);
        query.append("and " + getMulgaraIsConstraint("$s", mp3));
        query.append("and ( "
                + getMulgaraIsConstraint("$p", (URIReference) currentProperty));
        while (properties.hasNext()) {
            currentProperty = properties.next().getSubject();
            query.append("  or "
                    + getMulgaraIsConstraint("$p", (URIReference) currentProperty));
        }
        //results must be ordered
        query.append(") order by $s $p $o ; ");
        return query.toString();
    }

    /**
     * Returns an iTQL constraint in the form of: $var <mulgara:is><value>
     * 
     * @param var
     *            String
     * @param value
     *            Node
     * @return String
     */
    private String getMulgaraIsConstraint(String var, Node value) {

        if (var == null) {
            throw new IllegalArgumentException("'var' is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("'value' is null");
        }
        return var + MULGARA_IS + asString(value) + " " + NEWLINE;
    }

    /**
     * Loads all the mp3s Files in the FileSystemModel.
     * 
     * @throws Exception
     * @param mp3s
     *            Mp3Iterator
     */
    private void loadMp3s(Mp3Iterator mp3s) throws Exception {
        checkInitialized();
        if (mp3s == null) {
            throw new IllegalArgumentException("Mp3Iterator is null");
        }
        //load each Mp3 individually
        Mp3File mp3 = null;
        while (mp3s.hasNext()) {
            mp3 = mp3s.nextMp3();
            try {
                loadMp3(mp3);
            } catch (Exception e) {
                getListener().loadExceptionOccurred(mp3, e);
            }
        }
    }

    /**
     * Loads the Mp3.
     * 
     * @param mp3
     *            Mp3File
     * @throws Exception
     */
    private void loadMp3(Mp3File mp3) throws Exception {
        if (mp3 == null) {
            throw new IllegalArgumentException("Mp3File is null.");
        }
        checkInitialized();
        //construct insert query
        URI mp3Uri = mp3.getResource().getURI();
        StringBuffer query = new StringBuffer();
        query.append("load <" + mp3Uri + "> " + NEWLINE);
        query.append("into <" + getResource().getURI() + "> ;");
        //execute
        if (log.isDebugEnabled()) {
            log.debug("Loading: " + mp3Uri + " into " + getResource().getURI());
        }
        getBean().executeUpdate(query.toString());
    }

    /**
     * Creates a new Context for creating Mp3Files.
     * 
     * @return Mp3Context
     */
    private Mp3Context newMp3Context() {
        Mp3ContextImpl context = new Mp3ContextImpl();
        context.setBean(getBean());
        context.setMp3Model(this);
        context.setSchemaModel(schemaModel);
        return context;
    }

    /**
     * Returns the registered Mp3ModelListener or a default (no-op) listener if
     * one has not been set.
     * 
     * @return
     */
    private Mp3ModelListener getListener() {
        if (listener == null) {
            listener = new Mp3ModelListener() {
                public void loadExceptionOccurred(Mp3File file, Exception e) {
                    //no-op
                }
            };
        }
        return listener;
    }

    /**
     * Sets the listener that is notified of Mp3Model events.
     * 
     * @param listener
     */
    public void setMp3ModelListener(Mp3ModelListener listener) {
        this.listener = listener;
    }

}
