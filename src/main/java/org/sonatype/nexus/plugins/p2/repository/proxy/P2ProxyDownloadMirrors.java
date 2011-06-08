/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.proxy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.mirror.DefaultDownloadMirrors;
import org.sonatype.nexus.proxy.mirror.DownloadMirrorSelector;
import org.sonatype.nexus.proxy.repository.Mirror;

class P2ProxyDownloadMirrors
    extends DefaultDownloadMirrors
{

    public P2ProxyDownloadMirrors()
    {
        super( null );
    }

    private Map<String, Mirror> mirrorMap = new LinkedHashMap<String, Mirror>();

    @Override
    public DownloadMirrorSelector openSelector( String mirrorOfUrl )
    {
        return new P2DownloadMirrorSelector( this, mirrorOfUrl );
    }

    @Override
    public List<Mirror> getMirrors()
    {
        return new ArrayList<Mirror>( mirrorMap.values() );
    }

    @Override
    public void setMirrors( List<Mirror> mirrors )
    {
        this.mirrorMap.clear();
        for ( Mirror mirror : mirrors )
        {
            this.mirrorMap.put( mirror.getId(), mirror );
        }
        this.setMaxMirrors( mirrors.size() );
    }

    public void addMirror( Mirror mirror )
    {
        if ( mirror.getId() == "default" )
        {
            // This is a mirror added by nexus by default and
            // it "points" to the remote URL for the current p2 proxy repository.
            // We do not want this mirror... so, ignore it.
            return;
        }
        mirrorMap.put( mirror.getId(), mirror );
        this.setMaxMirrors( mirrorMap.size() );
    }

    @Override
    protected boolean existsMirrorWithId( boolean forWrite, String id )
    {
        return this.mirrorMap.containsKey( id );
    }
}
