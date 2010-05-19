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
        char c1 = getChar( nextToken.startOffset() - 1 );
        char c2 = getChar( nextToken.startOffset() - 2 );
        char c3 = getChar( nextToken.startOffset() - 3 );

        return c1 == '\n' || c2 == '\n' || c3 == '\n';
    }

    protected char getChar( int pos )
    {
        if ( ( pos < 0 ) || ( pos > ( getText().length() - 1 ) ) )
        {
            // return anything but newline ;)
            return ' ';
        }
        else
        {
            return getText().charAt( pos );
        }
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
