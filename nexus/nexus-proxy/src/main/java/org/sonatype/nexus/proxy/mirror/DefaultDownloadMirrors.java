package org.sonatype.nexus.proxy.mirror;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = DownloadMirrors.class, instantiationStrategy = "per-lookup" )
public class DefaultDownloadMirrors
    implements DownloadMirrors
{
    private static final long NO_EXPIRATION = -1;

    private LinkedHashSet<String> urls = new LinkedHashSet<String>();

    private Map<String, BlaclistEntry> blacklist = new HashMap<String, BlaclistEntry>();

    private long blacklistExpiration = NO_EXPIRATION;

    private static class BlaclistEntry
    {
        private String url;

        private long timestamp;

        public BlaclistEntry( String url, long timestamp )
        {
            this.url = url;

            this.timestamp = timestamp;
        }

        public String getUrl()
        {
            return url;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }

    public DownloadMirrorSelector openSelector()
    {
        return new DefaultDownloadMirrorSelector( this );
    }

    public void setUrls( List<String> urls )
    {
        if ( urls == null || urls.isEmpty() )
        {
            this.urls.clear();
            this.blacklist.clear();
        }
        else
        {
            this.urls = new LinkedHashSet<String>( urls );

            Iterator<Entry<String, BlaclistEntry>> i = blacklist.entrySet().iterator();

            while ( i.hasNext() )
            {
                String url = i.next().getKey();
                if ( !this.urls.contains( url ) )
                {
                    i.remove();
                }
            }
        }
    }

    public List<String> getUrls()
    {
        return new ArrayList<String>( urls );
    }

    public boolean isBlacklisted( String url )
    {
        BlaclistEntry entry = blacklist.get( url );

        if ( entry == null )
        {
            return false;
        }

        if ( blacklistExpiration == NO_EXPIRATION )
        {
            return true;
        }

        if ( entry.getTimestamp() + blacklistExpiration > System.currentTimeMillis() )
        {
            return true;
        }

        blacklist.remove( url );

        return false;
    }

    /**
     * Adds specified mirror URLs to the black list.
     */
    public void blacklist( Set<String> urls )
    {
        for ( String url : urls )
        {
            blacklist.remove( url );

            if ( this.urls.contains( url ) )
            {
                blacklist.put( url, new BlaclistEntry( url, System.currentTimeMillis() ) );
            }
        }
    }

    public void setBlacklistExpiration( long blacklistExpiration )
    {
        this.blacklistExpiration = blacklistExpiration;
    }

    public long getBlacklistExpiration()
    {
        return blacklistExpiration;
    }

}
