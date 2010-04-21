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
import org.apache.lucene.analysis.Tokenizer;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;

/**
 * A Nexus specific analyzer. Only difference from Lucene's SimpleAnalyzer is that we use LetterOrDigitTokenizer instead
 * of LowerCaseTokenizer. LetterOrDigitTokenizer does pretty much the same as LowerCaseTokenizer, it normalizes to lower
 * case letter, but it takes letters and numbers too (as opposed to LowerCaseTokenizer) as token chars.
 * 
 * @author Eugene Kuleshov
 * @author cstamas
 */
public class NexusAnalyzer
    extends Analyzer
{
    public TokenStream tokenStream( String fieldName, Reader reader )
    {
        return getTokenizer( fieldName, reader );
    }

    protected Tokenizer getTokenizer( String fieldName, Reader reader )
    {
        if ( JarFileContentsIndexCreator.FLD_CLASSNAMES_KW.getKey().equals( fieldName ) )
        {
            // To keep "backward" compatibility, we have to use old flawed tokenizer.
            return new CharTokenizer( reader )
            {
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
            return new LetterOrDigitTokenizer( reader );
        }
    }

    // ==

    public static class LetterOrDigitTokenizer
        extends CharTokenizer
    {
        public LetterOrDigitTokenizer( Reader in )
        {
            super( in );
        }

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
    }

}
