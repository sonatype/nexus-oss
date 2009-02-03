/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * The default query creator.
 * <p>
 * Constructs Lucene query for provided query text. 
 * 
 * By default it creates wildcard such as query text matches beginning of the field 
 * value or beginning of the class/package name segment for {@link ArtifactInfo#NAMES NAMES} field.  
 * But it can be controlled by using the following wildcards:
 * 
 * <ul>
 * <li>* - any character</li>
 * <li>'^' - beginning of the text</li> 
 * <li>'$' or '&lt;' or ' ' end of the text</li>
 * </ul>
 * 
 * For example:
 * 
 * <ul>
 * <li>junit - matches junit and junit-foo, but not foo-junit</li>
 * <li>*junit - matches junit, junit-foo and foo-junit</li>
 * <li>^junit$ - matches junit, but not junit-foo, nor foo-junit</li>
 * </ul>
 * 
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
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
            getLogger().info( "Empty or null query for field:" + field );

            return null;
        }
        
        String q = query.toLowerCase();
        
        char h = query.charAt( 0 );
        
        if( field.equals( ArtifactInfo.NAMES  ))
        {
            q = q.replaceAll( "\\.", "/" );
            
            if( h == '^' )
            {
                q = q.substring( 1 );
                if ( q.charAt( 0 ) != '/' )
                {
                    q = '/' + q;
                }
            }
            else if( h != '*' )
            {
                q = "*/" + q;
            }
        }
        else
        {
            if( h == '^' )
            {
                q = q.substring( 1 );
            }
            else if( h != '*' )
            {
                q = "*" + q;
            }
        }
        
        int l = q.length() - 1;
        char c = q.charAt( l );
        if( c == ' ' || c == '<' || c == '$' )
        {
            q = q.substring( 0, q.length() - 1 );
        }
        else if ( c != '*' )
        {
            q += "*";
        }

        int n = q.indexOf( '*' );
        if( n == -1 )
        {
            return new TermQuery( new Term( field, q ) );
        }
        else if( n > 0 && n == q.length() -1 )
        {
            return new PrefixQuery( new Term( field, q.substring( 0, q.length() - 2 ) ) );
        }
        
        return new WildcardQuery( new Term( field, q ) );
    }
}
