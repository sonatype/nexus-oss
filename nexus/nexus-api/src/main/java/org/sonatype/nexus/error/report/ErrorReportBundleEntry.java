package org.sonatype.nexus.error.report;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;

public class ErrorReportBundleEntry
{

    private InputStream content;

    private String entryName;

    public ErrorReportBundleEntry( String entryName, InputStream content )
    {
        super();
        this.entryName = entryName;
        this.content = content;
    }

    public InputStream getContent()
    {
        return content;
    }

    public String getEntryName()
    {
        return entryName;
    }

    public void releaseEntry()
        throws IOException
    {
        IOUtil.close( content );
        content = null;
    }

}
