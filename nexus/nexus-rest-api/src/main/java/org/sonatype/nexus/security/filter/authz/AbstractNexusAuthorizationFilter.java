package org.sonatype.nexus.security.filter.authz;

import java.util.regex.Pattern;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

public class AbstractNexusAuthorizationFilter
    extends HttpVerbMappingAuthorizationFilter
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
}
