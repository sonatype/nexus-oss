package org.sonatype.nexus.plugins.filter;

import org.sonatype.nexus.proxy.item.ContentLocator;

public class FileInfo
{
    private final String fingerprint;

    private final String name;

    private final ContentLocator contentLocator;

    public FileInfo( String fingerprint, String name, ContentLocator contentLocator )
    {
        this.fingerprint = fingerprint;

        this.name = name;

        this.contentLocator = contentLocator;
    }

    protected String getFingerprint()
    {
        return fingerprint;
    }

    protected String getName()
    {
        return name;
    }

    protected ContentLocator getContentLocator()
    {
        return contentLocator;
    }
}
