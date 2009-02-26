/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.sonatype.nexus.index.ArtifactInfo;

/**
 * A Nexus specific <a href="http://lucene.apache.org/java/2_4_0/api/core/org/apache/lucene/analysis/Analyzer.html">Lucene Analyzer</a>
 * used to parse indexed fields
 * 
 * @author Eugene Kuleshov
 */
public class NexusAnalyzer
    extends Analyzer
{
    @Override
    public TokenStream tokenStream( String field, final Reader reader )
    {
        if ( field.equals( ArtifactInfo.NAMES ) )
        {
            return new CharTokenizer( reader ) {
                @Override
                protected boolean isTokenChar( char c )
                {
                    return c != '\n';
                }
                
                @Override
                protected char normalize( char c )
                {
                    return Character.toLowerCase( c );
                }
            };
        }
        else
        {
            return new CharTokenizer( reader ) {
                @Override
                protected boolean isTokenChar( char c )
                {
                    return true;
                }
                
                @Override
                protected char normalize( char c )
                {
                    return Character.toLowerCase( c );
                }
            };
        }
    }
    
}
