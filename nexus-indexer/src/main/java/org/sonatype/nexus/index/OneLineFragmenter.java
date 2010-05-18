package org.sonatype.nexus.index;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.highlight.Fragmenter;

public class OneLineFragmenter
    implements Fragmenter
{
    private String text;

    public void start( String originalText )
    {
        setText( originalText );
    }

    public boolean isNewFragment( Token nextToken )
    {
        char c1 = getText().charAt( nextToken.startOffset() - 1 );
        char c2 = getText().charAt( nextToken.startOffset() - 2 );
        char c3 = getText().charAt( nextToken.startOffset() - 3 );

        return c1 == '\n' || c2 == '\n' || c3 == '\n';
    }

    public String getText()
    {
        return text;
    }

    public void setText( String text )
    {
        this.text = text;
    }
}
