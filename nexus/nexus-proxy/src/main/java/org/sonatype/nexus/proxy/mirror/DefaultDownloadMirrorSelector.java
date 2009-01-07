package org.sonatype.nexus.proxy.mirror;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DefaultDownloadMirrorSelector
    implements DownloadMirrorSelector
{
    private final DefaultDownloadMirrors mirrors;

    private final LinkedHashSet<String> urls = new LinkedHashSet<String>();

    private final LinkedHashSet<String> failedMirrors = new LinkedHashSet<String>();

    private boolean success;

    public DefaultDownloadMirrorSelector( DefaultDownloadMirrors mirrors )
    {
        this.mirrors = mirrors;

        for ( String url : mirrors.getUrls() )
        {
            if ( !mirrors.isBlacklisted( url ) )
            {
                urls.add( url );
            }
        }
    }

    public List<String> getUrls()
    {
        return new ArrayList<String>( urls );
    }

    public void close()
    {
        if ( success )
        {
            mirrors.blacklist( failedMirrors );
        }
    }

    public void feedbackSuccess( String url )
    {
        // XXX validate URL

        this.success = true;
    }

    public void feedbackFailure( String url )
    {
        // XXX validate URL

        failedMirrors.add( url );
    }
}
