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
