package org.sonatype.nexus.index;

import org.apache.lucene.search.highlight.Encoder;

public class CleaningEncoder
    implements Encoder
{
    public String encodeText( String originalText )
    {
        return originalText.replace( "\n", "" );
    }
}
