/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index;

import java.util.ArrayList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * The default query creator.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultQueryCreator
    extends AbstractLogEnabled
    implements QueryCreator
{
    public Query constructQuery( String field, String query )
    {
        if ( query == null || query.length() == 0 )
        {
            getLogger().info( "No/null query for field:" + field );

            return null;
        }

        ArrayList<Query> queries = new ArrayList<Query>();

        String[] phrases = query.toLowerCase().replaceAll( "\\*", "X" ).split( "[:, ]" );

        for ( String phrase : phrases )
        {
            String[] terms = null;

            if ( ArtifactInfo.GROUP_ID.equals( field ) )
            {
                terms = phrase.split( "[ -/\\\\&&[^\\.]]" );
            }
            else
            {
                terms = phrase.split( "[\\. -/\\\\]" );
            }

            int len = terms.length;

            if ( len > 1 )
            {
                if ( phrase.indexOf( 'X' ) > -1 )
                {
                    BooleanQuery bq = new BooleanQuery();

                    for ( int i = 0; i < len; i++ )
                    {
                        if ( terms[i].indexOf( 'X' ) > -1 )
                        {
                            Query q1 = new WildcardQuery( new Term( field, terms[i].replaceAll( "X", "*" ) ) );
                            
                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                        else if ( i < len - 1 )
                        {
                            Query q1 = new TermQuery( new Term( field, terms[i] ) );
                            
                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                        else
                        {
                            Query q1 = new PrefixQuery( new Term( field, terms[i] ) );
                            
                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                    }

                    queries.add( bq );
                }
                else
                {
                    PhraseQuery pq = new PhraseQuery();

                    for ( int i = 0; i < len; i++ )
                    {
                        pq.add( new Term( field, terms[i] ) );
                    }

                    queries.add( pq );
                }
            }
            else
            {
                if ( phrase.indexOf( 'X' ) > -1 )
                {
                    queries.add( new WildcardQuery( new Term( field, terms[0].replaceAll( "X", "*" ) ) ) );
                }
                else
                {
                    queries.add( new PrefixQuery( new Term( field, terms[0] ) ) );
                }
            }
        }

        Query result = null;

        if ( queries.size() > 1 )
        {
            BooleanQuery bq = new BooleanQuery();

            for ( Query q : queries )
            {
                bq.add( q, BooleanClause.Occur.SHOULD );
            }

            result = bq;
        }
        else
        {
            result = queries.get( 0 );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Constructed query: " + result.toString() );
        }

        return result;
    }

}
