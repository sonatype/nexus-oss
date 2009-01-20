/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.sonatype.nexus.index.ArtifactInfo;

/** @author Jason van Zyl */
public class NexusAnalyzer
    extends Analyzer
{

    private static Analyzer DEFAULT_ANALYZER = new StandardAnalyzer();

    @Override
    public TokenStream tokenStream( String field, final Reader reader )
    {
        if ( !isTextField( field ) )
        {
            return new CharTokenizer( reader )
            {
                @Override
                protected boolean isTokenChar( char c )
                {
                    return Character.isLetterOrDigit( c );
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
            return DEFAULT_ANALYZER.tokenStream( field, reader );
        }
    }

    @Override
    public TokenStream reusableTokenStream( String field, Reader reader )
        throws IOException
    {
        if ( !isTextField( field ) )
        {
            return new CharTokenizer( reader )
            {
                @Override
                protected boolean isTokenChar( char c )
                {
                    return Character.isLetterOrDigit( c );
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
            return DEFAULT_ANALYZER.reusableTokenStream( field, reader );
        }
    }

    protected boolean isTextField( String field )
    {
        return ArtifactInfo.NAME.equals( field ) || ArtifactInfo.DESCRIPTION.equals( field )
            || ArtifactInfo.NAMES.equals( field );

    }

}
