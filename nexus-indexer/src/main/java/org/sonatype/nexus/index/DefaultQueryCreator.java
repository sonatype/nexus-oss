/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamas Cservenak (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
                terms = phrase.split( "[ _\\-/\\\\&&[^\\.]]" );
            }
            else
            {
                terms = phrase.split( "[\\. _\\-/\\\\]" );
            }

            int len = terms.length;

            if ( len > 1 )
            {
                if ( phrase.indexOf( 'X' ) > -1 )
                {
                    // we have a joker in it

                    BooleanQuery bq = new BooleanQuery();

                    for ( int i = 0; i < len; i++ )
                    {
                        if ( terms[i].indexOf( 'X' ) > -1 )
                        {
                            // any that has a joker

                            Query q1 = new WildcardQuery( new Term( field, terms[i].replaceAll( "X", "*" ) ) );

                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                        else if ( i < ( len - 1 ) )
                        {
                            // any "in the middle"

                            Query q1 = new TermQuery( new Term( field, terms[i] ) );

                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                        else
                        {
                            // the last one

                            Query q1 = new PrefixQuery( new Term( field, terms[i] ) );

                            bq.add( q1, BooleanClause.Occur.MUST );
                        }
                    }

                    queries.add( bq );
                }
                else if ( ( phrase.startsWith( "\"" ) && phrase.endsWith( "\"" ) )
                    || ArtifactInfo.VERSION.equals( field ) )
                {
                    // we have quotes around, make it a phrase query

                    PhraseQuery pq = new PhraseQuery();

                    if ( len == 1 && terms[0].startsWith( "\"" ) && terms[0].endsWith( "\"" ) )
                    {
                        // shave
                        pq.add( new Term( field, terms[0].substring( 1, terms.length - 1 ) ) );
                    }
                    else if ( len == 1 )
                    {
                        pq.add( new Term( field, terms[0] ) );
                    }
                    else
                    {
                        for ( int i = 0; i < len; i++ )
                        {
                            if ( i == 0 && terms[i].startsWith( "\"" ) )
                            {
                                // first: "shave" those quotes
                                pq.add( new Term( field, terms[i].substring( 1 ) ) );
                            }
                            else if ( i == len - 1 && terms[i].endsWith( "\"" ) )
                            {
                                // last: "shave" those quotes
                                pq.add( new Term( field, terms[i].substring( 0, terms[i].length() - 1 ) ) );
                            }
                            else
                            {
                                // in the middle
                                pq.add( new Term( field, terms[i] ) );
                            }
                        }
                    }

                    queries.add( pq );
                }
                else
                {
                    // we have a "free" phrase, combine it into query from multiple Term + last as Wildcard query

                    BooleanQuery bq = new BooleanQuery();

                    Query q1 = null;

                    for ( int i = 0; i < len; i++ )
                    {
                        if ( i < len - 1 )
                        {
                            // all in the middle
                            q1 = new TermQuery( new Term( field, terms[i] ) );
                        }
                        else
                        {
                            // the last term
                            q1 = new PrefixQuery( new Term( field, terms[i] ) );
                        }

                        bq.add( q1, BooleanClause.Occur.MUST );
                    }

                    queries.add( bq );
                }
            }
            else
            {
                if ( phrase.indexOf( 'X' ) > -1 )
                {
                    queries.add( new WildcardQuery( new Term( field, phrase.replaceAll( "X", "*" ) ) ) );
                }
                else if ( phrase.startsWith( "\"" ) && phrase.endsWith( "\"" ) )
                {
                    PhraseQuery pq = new PhraseQuery();

                    pq.add( new Term( field, phrase.substring( 1, phrase.length() - 1 ) ) );

                    queries.add( pq );
                }
                else
                {
                    queries.add( new PrefixQuery( new Term( field, phrase ) ) );
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
