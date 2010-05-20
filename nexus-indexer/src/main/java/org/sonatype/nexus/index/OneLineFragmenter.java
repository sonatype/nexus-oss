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
        // text: /org/sonatype/...
        // tokens: org sonatype
        boolean result =
            isNewline( getChar( nextToken.startOffset() - 1 ) ) || isNewline( getChar( nextToken.startOffset() - 2 ) );

        return result;
    }

    protected boolean isNewline( char c )
    {
        return c == '\n';
    }

    protected char getChar( int pos )
    {
        if ( ( pos < 0 ) || ( pos > ( getText().length() - 1 ) ) )
        {
            // return no newline ;)
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
