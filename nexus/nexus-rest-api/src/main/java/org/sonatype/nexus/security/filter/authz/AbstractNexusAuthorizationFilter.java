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
package org.sonatype.nexus.security.filter.authz;

import java.util.regex.Pattern;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

public class AbstractNexusAuthorizationFilter
    extends FailureLoggingHttpMethodPermissionFilter
{
    private Pattern pathPrefixPattern;

    private String pathPrefix;

    private RepositoryRouter repositoryRouter;

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public void setPathPrefix( String pathPrefix )
    {
        this.pathPrefix = pathPrefix;

        if ( pathPrefix != null )
        {
            setPathPrefixPattern( Pattern.compile( pathPrefix ) );
        }
    }

    protected RepositoryRouter getRepositoryRouter()
    {
        if ( this.repositoryRouter == null )
        {
            PlexusContainer plexus = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );

            try
            {
                this.repositoryRouter = plexus.lookup( RepositoryRouter.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot lookup NexusArtifactAuthorizer!", e );
            }
        }
        return this.repositoryRouter;
    }

    protected void setPathPrefixPattern( Pattern pathPrefixPattern )
    {
        this.pathPrefixPattern = pathPrefixPattern;
    }

    protected Pattern getPathPrefixPattern()
    {
        return pathPrefixPattern;
    }

    protected Object getAttribute( String key )
    {
        return this.getFilterConfig().getServletContext().getAttribute( key );
    }
}
