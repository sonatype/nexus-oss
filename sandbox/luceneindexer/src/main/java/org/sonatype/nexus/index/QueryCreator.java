/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

/**
 * A component that creates Lucene 
 * <a href="http://lucene.apache.org/java/2_4_0/api/core/org/apache/lucene/search/Query.html">Query</a>
 * instances for provided query text. Created queries can be also combined using
 * <a href="http://lucene.apache.org/java/2_4_0/api/core/org/apache/lucene/search/BooleanQuery.html">BooleanQuery</a>.
 * 
 * @author Tamas Cservenak
 */
public interface QueryCreator
{
    String ROLE = QueryCreator.class.getName();

    Query constructQuery( String field, String query );
}
