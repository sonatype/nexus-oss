package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = RepositoryURLBuilder.class, hint = "RestletRepositoryUrlBuilder" )
public class RestletRepositoryURLBuilder
    implements RepositoryURLBuilder
{
    @Requirement
    private Logger logger;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private GlobalRestApiSettings globalRestApiSettings;

    @Override
    public String getRepositoryContentUrl( String repositoryId )
        throws NoSuchRepositoryException
    {
        return getRepositoryContentUrl( repositoryRegistry.getRepository( repositoryId ) );
    }

    @Override
    public String getRepositoryContentUrl( Repository repository )
    {
        if ( !repository.isExposed() )
        {
            return null;
        }

        final boolean forceBaseURL =
            globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
                && StringUtils.isNotBlank( globalRestApiSettings.getBaseUrl() );

        String baseURL = null;

        // if force, always use force
        if ( forceBaseURL )
        {
            baseURL = globalRestApiSettings.getBaseUrl();
        }
        // next check if this thread has a restlet request
        else if ( Request.getCurrent() != null )
        {
            baseURL = Request.getCurrent().getRootRef().toString();
        }
        // as last resort, try to use the baseURL if set
        else
        {
            baseURL = globalRestApiSettings.getBaseUrl();
        }

        // if all else fails?
        if ( StringUtils.isBlank( baseURL ) )
        {
            return null;
        }

        StringBuffer url = new StringBuffer( baseURL );
        
        if ( !baseURL.endsWith( "/" ) )
        {
            url.append( "/" );
        }

        final RepositoryTypeDescriptor rtd =
            repositoryTypeRegistry.getRepositoryTypeDescriptor( repository.getProviderRole(),
                repository.getProviderHint() );

        url.append( "content/" ).append( rtd.getPrefix() ).append( "/" ).append( repository.getPathPrefix() );

        return url.toString();
    }

}
