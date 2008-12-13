package org.sonatype.nexus.index.packer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Daily chunker. This Chunker will cut the index into days, based on LAST_MODIFIED, the timestamp when the file get to
 * index (not the filetimestamp!).
 * 
 * @author Tamas Cservenak
 * @plexus.component role-hint="day"
 */
public class DailyIndexChunker
    extends AbstractIndexChunker
{
    private static final String ID = "day";

    private static final String INDEX_TIME_DAY_FORMAT = "yyyyMMdd";

    private SimpleDateFormat df = new SimpleDateFormat( INDEX_TIME_DAY_FORMAT );

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
