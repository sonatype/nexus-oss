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

import org.sonatype.nexus.proxy.repository.Mirror;

public class DefaultDownloadMirrorSelector
    implements DownloadMirrorSelector
{
    private final DefaultDownloadMirrors dMirrors;

    private final LinkedHashSet<Mirror> mirrors = new LinkedHashSet<Mirror>();

    private final LinkedHashSet<Mirror> failedMirrors = new LinkedHashSet<Mirror>();

    private boolean success;

    public DefaultDownloadMirrorSelector( DefaultDownloadMirrors dMirrors )
    {
        this.dMirrors = dMirrors;

        for ( Mirror mirror : dMirrors.getMirrors() )
        {
            if ( !dMirrors.isBlacklisted( mirror ) )
            {
                mirrors.add( mirror );
            }
            
            if ( mirrors.size() >= dMirrors.getMaxMirrors() )
            {
                break;
            }
        }
    }

    public List<Mirror> getMirrors()
    {
        return new ArrayList<Mirror>( mirrors );
    }

    public void close()
    {
        if ( success )
        {
            dMirrors.blacklist( failedMirrors );
        }
    }

    public void feedbackSuccess( Mirror mirror )
    {
        // XXX validate URL

        failedMirrors.remove( mirror );

        this.success = true;
    }

    public void feedbackFailure( Mirror mirror )
    {
        // XXX validate URL

        failedMirrors.add( mirror );
    }
}
