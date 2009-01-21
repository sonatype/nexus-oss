/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
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
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.Mirror;

@Component( role = DownloadMirrors.class, instantiationStrategy = "per-lookup" )
public class DefaultDownloadMirrors
    implements DownloadMirrors
{
    private static final long NO_EXPIRATION = -1;
    
    private static final long DEFAULT_EXPIRATION = 30 * 60 * 1000L; // 30 minutes

    private LinkedHashSet<Mirror> mirrors = new LinkedHashSet<Mirror>();

    private Map<String, BlaclistEntry> blacklist = new HashMap<String, BlaclistEntry>();

    private long blacklistExpiration = DEFAULT_EXPIRATION;

    /**
     * Maximum number of mirror url to consider before operation falls back to
     * canonical url.
     * 
     * @see AbstractProxyRepository#doRetrieveRemoteItem
     */
    private int maxMirrors = 1;

    private static class BlaclistEntry
    {
        private String id;

        private long timestamp;

        public BlaclistEntry( String id, long timestamp )
        {
            this.id = id;

            this.timestamp = timestamp;
        }

        public String getId()
        {
            return id;
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

    public void setMirrors( List<Mirror> mirrors )
    {
        if ( mirrors == null || mirrors.isEmpty() )
        {
            this.mirrors.clear();
            this.blacklist.clear();
        }
        else
        {
            this.mirrors = new LinkedHashSet<Mirror>( mirrors );

            Iterator<Entry<String, BlaclistEntry>> i = blacklist.entrySet().iterator();

            while ( i.hasNext() )
            {
                String id = i.next().getKey();
                
                if ( getMirror( id ) == null )
                {
                    i.remove();
                }
            }
        }
    }

    public List<Mirror> getMirrors()
    {
        return new ArrayList<Mirror>( mirrors );
    }

    public boolean isBlacklisted( Mirror mirror )
    {
        BlaclistEntry entry = blacklist.get( mirror.getId() );

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

        blacklist.remove( mirror.getId() );

        return false;
    }

    /**
     * Adds specified mirror URLs to the black list.
     */
    public void blacklist( Set<Mirror> mirrors )
    {
        for ( Mirror mirror : mirrors )
        {
            blacklist.remove( mirror.getId() );

            if ( this.mirrors.contains( mirror ) )
            {
                blacklist.put( mirror.getId(), new BlaclistEntry( mirror.getId(), System.currentTimeMillis() ) );
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

    public int getMaxMirrors()
    {
        return maxMirrors;
    }
    
    public void setMaxMirrors( int maxMirrors )
    {
        this.maxMirrors = maxMirrors;
    }
    
    private Mirror getMirror( String id )
    {
        if ( this.mirrors != null )
        {
            for ( Mirror mirror : this.mirrors )
            {
                if ( mirror.getId().equals( id ) )
                {
                    return mirror;
                }
            }
        }
        
        return null;
    }

}
