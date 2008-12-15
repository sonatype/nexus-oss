package org.sonatype.nexus.index.packer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Weekly chunker. This Chunker will cut the index into weeks, based on LAST_MODIFIED, the timestamp when the file get
 * to index (not the filetimestamp!).
 * 
 * @author Tamas Cservenak
 * @plexus.component role-hint="week"
 */
public class WeeklyIndexChunker
    extends AbstractIndexChunker
{
    private static final String ID = "week";

    private static final String INDEX_TIME_DAY_FORMAT = "yyyyMMW";

    private final SimpleDateFormat df;

    public WeeklyIndexChunker()
    {
        this.df = new SimpleDateFormat( INDEX_TIME_DAY_FORMAT );
        this.df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    public String getId()
    {
        return ID;
    }

    @Override
    protected DateFormat getDateFormat()
    {
        return df;
    }
}
