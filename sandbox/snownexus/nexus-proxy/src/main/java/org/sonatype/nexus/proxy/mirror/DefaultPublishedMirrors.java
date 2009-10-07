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
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.repository.Mirror;

public class DefaultPublishedMirrors
    implements PublishedMirrors
{
    private final CRepositoryCoreConfiguration configuration;

    public DefaultPublishedMirrors( CRepositoryCoreConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public void setMirrors( List<Mirror> mirrors )
    {
        if ( mirrors == null || mirrors.isEmpty() )
        {
            getConfiguration( true ).getMirrors().clear();
        }
        else
        {
            ArrayList<CMirror> modelMirrors = new ArrayList<CMirror>( mirrors.size() );

            for ( Mirror mirror : mirrors )
            {
                CMirror model = new CMirror();

                model.setId( mirror.getId() );

                model.setUrl( mirror.getUrl() );

                modelMirrors.add( model );
            }

            getConfiguration( true ).setMirrors( modelMirrors );
        }
    }

    public List<Mirror> getMirrors()
    {
        List<CMirror> modelMirrors = getConfiguration( false ).getMirrors();

        ArrayList<Mirror> mirrors = new ArrayList<Mirror>( modelMirrors.size() );

        for ( CMirror model : modelMirrors )
        {
            Mirror mirror = new Mirror( model.getId(), model.getUrl() );

            mirrors.add( mirror );
        }

        return Collections.unmodifiableList( mirrors );
    }

    // ==

    protected CRepository getConfiguration( boolean forWrite )
    {
        return (CRepository) configuration.getConfiguration( forWrite );
    }
}
