package org.sonatype.nexus.index.context;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.sonatype.nexus.index.IndexerField;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;

public class NexusAnalyzerTest
    extends TestCase
{
    protected NexusAnalyzer nexusAnalyzer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusAnalyzer = new NexusAnalyzer();
    }

    public void testGroupIdTokenization()
        throws IOException
    {
        runAndCompare( MinimalArtifactInfoIndexCreator.FLD_GROUP_ID, "org.slf4j", new String[] { "org", "slf4j" } );

        runAndCompare( MinimalArtifactInfoIndexCreator.FLD_GROUP_ID_KW, "org.slf4j", new String[] { "org.slf4j" } );
    }

    protected void runAndCompare( IndexerField indexerField, String text, String[] expected )
        throws IOException
    {
        TokenStream ts = nexusAnalyzer.reusableTokenStream( indexerField.getKey(), new StringReader( text ) );

        ArrayList<String> tokenList = new ArrayList<String>();

        if ( !indexerField.isKeyword() )
        {
            final Token reusableToken = new Token();

            Token token = ts.next( reusableToken );

            while ( token != null )
            {
                tokenList.add( token.term() );

                token = ts.next( reusableToken );
            }
        }
        else
        {
            tokenList.add( text );
        }

        assertEquals( "The result does not meet the expectations.", Arrays.asList( expected ), tokenList );
    }

}
