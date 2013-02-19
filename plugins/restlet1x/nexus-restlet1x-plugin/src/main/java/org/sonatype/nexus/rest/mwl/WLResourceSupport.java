/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.mwl;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * WL REST resource support.
 * 
 * @author cstamas
 * @since 2.4
 */
public abstract class WLResourceSupport
    extends AbstractNexusPlexusResource
{
    protected static final String REPOSITORY_ID_KEY = "repositoryId";

    @Requirement
    private WLManager wlManager;

    /**
     * Constructor needed to set resource modifiable.
     */
    public WLResourceSupport()
    {
        setModifiable( true );
    }

    protected WLManager getWLManager()
    {
        return wlManager;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*", "authcBasic,perms[nexus:repositories]" );
    }

    /**
     * Returns properly adapted {@link MavenRepository} instance, or handles cases like not exists or not having
     * required type (kind in Nx lingo).
     * 
     * @param request
     * @param clazz
     * @return
     * @throws ResourceException
     */
    protected <T extends MavenRepository> T getMavenRepository( final Request request, Class<T> clazz )
        throws ResourceException
    {
        final String repositoryId = request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
        try
        {
            final Repository repository = getRepositoryRegistry().getRepository( repositoryId );
            final T mavenRepository = repository.adaptToFacet( clazz );
            if ( mavenRepository != null )
            {
                if ( !Maven2ContentClass.ID.equals( mavenRepository.getRepositoryContentClass().getId() ) )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID=\""
                        + repositoryId + "\" does not have the required format (maven2)." );
                }
                if ( mavenRepository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID=\""
                        + repositoryId + "\" is not a required type (hosted, proxy or group)." );
                }
                return mavenRepository;
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID=\"" + repositoryId
                    + "\" is not a required type of " + clazz.getSimpleName() + "." );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No repository with ID=\"" + repositoryId
                + "\" found.", e );
        }
    }
}
