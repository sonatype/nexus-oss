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
