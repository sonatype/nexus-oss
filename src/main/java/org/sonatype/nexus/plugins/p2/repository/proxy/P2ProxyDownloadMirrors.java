/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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

    private final Map<String, Mirror> mirrorMap = new LinkedHashMap<String, Mirror>();

    @Override
    public DownloadMirrorSelector openSelector( final String mirrorOfUrl )
    {
        return new P2DownloadMirrorSelector( this, mirrorOfUrl );
    }

    @Override
    public List<Mirror> getMirrors()
    {
        return new ArrayList<Mirror>( mirrorMap.values() );
    }

    @Override
    public void setMirrors( final List<Mirror> mirrors )
    {
        mirrorMap.clear();
        for ( final Mirror mirror : mirrors )
        {
            mirrorMap.put( mirror.getId(), mirror );
        }
        setMaxMirrors( mirrors.size() );
    }

    public void addMirror( final Mirror mirror )
    {
        if ( mirror.getId() == "default" )
        {
            // This is a mirror added by nexus by default and
            // it "points" to the remote URL for the current p2 proxy repository.
            // We do not want this mirror... so, ignore it.
            return;
        }
        mirrorMap.put( mirror.getId(), mirror );
        setMaxMirrors( mirrorMap.size() );
    }

    @Override
    protected boolean existsMirrorWithId( final boolean forWrite, final String id )
    {
        return mirrorMap.containsKey( id );
    }
}
