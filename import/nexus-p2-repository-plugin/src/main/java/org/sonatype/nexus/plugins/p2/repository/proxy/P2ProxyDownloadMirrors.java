/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
