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
package org.sonatype.nexus.rest.mirrors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.MirrorResource;

public abstract class AbstractRepositoryMirrorPlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /** Key to store Mirror with which we work against. */
    public static final String MIRROR_ID_KEY = "mirrorId";

    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    protected String getMirrorId( Request request )
    {
        return request.getAttributes().get( MIRROR_ID_KEY ).toString();
    }

    protected List<MirrorResource> nexusToRestModel( List<Mirror> mirrors )
    {
        List<MirrorResource> sortedList = new ArrayList<MirrorResource>();

        for ( Mirror mirror : mirrors )
        {
            sortedList.add( nexusToRestModel( mirror ) );
        }

        return sortedList;
    }

    protected MirrorResource nexusToRestModel( Mirror mirror )
    {
        MirrorResource resource = new MirrorResource();

        resource.setId( mirror.getId() );
        resource.setUrl( mirror.getUrl() );

        return resource;
    }

    protected List<Mirror> restToNexusModel( List<MirrorResource> resources )
    {
        List<Mirror> sortedList = new ArrayList<Mirror>();

        for ( MirrorResource resource : resources )
        {
            sortedList.add( restToNexusModel( resource ) );
        }

        return sortedList;
    }

    protected Mirror restToNexusModel( MirrorResource resource )
    {
        Mirror mirror = new Mirror( resource.getId(), resource.getUrl() );

        return mirror;
    }

    protected List<Mirror> getMirrors( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return repository.adaptToFacet( ProxyRepository.class ).getDownloadMirrors().getMirrors();
        }
        else
        {
            return repository.getPublishedMirrors().getMirrors();
        }
    }

    protected void setMirrors( Repository repository, List<Mirror> mirrors )
        throws IOException
    {
        //populate ids if not set
        for ( Mirror mirror : mirrors )
        {
            if ( StringUtils.isEmpty( mirror.getId() ) )
            {
                mirror.setId( mirror.getUrl() );
            }
        }
        
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            repository.adaptToFacet( ProxyRepository.class ).getDownloadMirrors().setMirrors( mirrors );

            getNexusConfiguration().saveConfiguration();
        }
        else
        {
            repository.getPublishedMirrors().setMirrors( mirrors );

            getNexusConfiguration().saveConfiguration();
        }
    }
}
