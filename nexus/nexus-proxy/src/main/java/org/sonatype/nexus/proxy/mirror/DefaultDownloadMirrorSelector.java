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
            
            if ( urls.size() >= mirrors.getMaxMirrors() )
            {
                break;
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

        failedMirrors.remove( url );

        this.success = true;
    }

    public void feedbackFailure( String url )
    {
        // XXX validate URL

        failedMirrors.add( url );
    }
}
