/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Collection;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

/**
 * A component the creates Lucene Queries from "human written" queires, but also helps client applications to assemble
 * proper queries for fields they want to search.
 * 
 * @author Tamas Cservenak
 */
public interface QueryCreator
{
    String ROLE = QueryCreator.class.getName();

    /**
     * Performs a selection of the appropriate IndexerField belonging to proper Field.
     * 
     * @param field
     * @param type
     * @return
     */
    IndexerField selectIndexerField( final Field field, final SearchType type );

    /**
     * Constructs query by parsing the query string, using field as default field. This method should be use to
     * construct queries (single term or phrase queries) against <b>single field</b>.
     * 
     * @param field
     * @param query
     * @param type
     * @return
     * @throws ParseException if query parsing is unsuccesful.
     */
    Query constructQuery( Field field, String query, SearchType type );

    /**
     * Deprecated. Avoid it's use! Constructs query against <b>single</b> field, using it's "best effort" approach to
     * perform parsing, but letting caller to apply it's (usually wrong) knowledge about how field is indexed.
     * 
     * @param field
     * @param query
     * @return query if successfully parsed, or null.
     * @deprecated Use {@link #constructQuery(Collection, String)} or
     *             {@link QueryCreator#constructQuery(IndexerField, String, SearchType)} methods instead!
     */
    Query constructQuery( String field, String query );

}
