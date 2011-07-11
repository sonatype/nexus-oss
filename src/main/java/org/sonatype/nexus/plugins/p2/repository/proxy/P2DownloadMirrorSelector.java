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
import java.util.LinkedHashSet;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.mirror.DefaultDownloadMirrors;
import org.sonatype.nexus.proxy.mirror.DownloadMirrorSelector;
import org.sonatype.nexus.proxy.repository.Mirror;

public class P2DownloadMirrorSelector
    implements DownloadMirrorSelector
{

    private final DefaultDownloadMirrors dMirrors;

    private final LinkedHashSet<Mirror> mirrors = new LinkedHashSet<Mirror>();

    private final LinkedHashSet<Mirror> failedMirrors = new LinkedHashSet<Mirror>();

    private boolean success;

    public P2DownloadMirrorSelector( final DefaultDownloadMirrors dMirrors, final String mirrorOfUrl )
    {
        this.dMirrors = dMirrors;

        for ( final Mirror mirror : dMirrors.getMirrors() )
        {
            if ( !dMirrors.isBlacklisted( mirror ) && StringUtils.equals( mirrorOfUrl, mirror.getMirrorOfUrl() ) )
            {
                mirrors.add( mirror );
            }

            // if ( mirrors.size() >= dMirrors.getMaxMirrors() )
            // {
            // break;
            // }
        }
    }

    @Override
    public List<Mirror> getMirrors()
    {
        return new ArrayList<Mirror>( mirrors );
    }

    @Override
    public void close()
    {
        if ( success )
        {
            dMirrors.blacklist( failedMirrors );
        }
    }

    @Override
    public void feedbackSuccess( final Mirror mirror )
    {
        // XXX validate URL

        failedMirrors.remove( mirror );

        success = true;
    }

    @Override
    public void feedbackFailure( final Mirror mirror )
    {
        // XXX validate URL

        failedMirrors.add( mirror );
    }

}
