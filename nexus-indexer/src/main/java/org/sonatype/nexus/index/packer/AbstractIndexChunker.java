package org.sonatype.nexus.index.packer;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public abstract class AbstractIndexChunker
    implements IndexChunker
{
    public String getChunkId( Date d )
    {
        if ( d == null )
        {
            return null;
        }

        DateFormat df = getDateFormat();

        // we are "truncating" the date
        return df.format( d );
    }

    public Date getChunkDate( String id )
    {
        if ( id == null )
        {
            return null;
        }

        try
        {
            return getDateFormat().parse( id );
        }
        catch ( ParseException e )
        {
            // should not happen
            return null;
        }
    }

    protected abstract DateFormat getDateFormat();
}
