/**
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
