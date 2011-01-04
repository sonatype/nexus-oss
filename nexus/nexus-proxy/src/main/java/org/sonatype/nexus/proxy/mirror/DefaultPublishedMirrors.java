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
