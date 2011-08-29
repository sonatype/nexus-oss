package org.sonatype.nexus.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        // TODO: what about repositories not being exposed at all?
        // repository.isExposed()
        
        boolean forceBaseURL =
            globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
                && StringUtils.isNotEmpty( globalRestApiSettings.getBaseUrl() );
        String baseURL = null;

        // if force, always use force
        if ( forceBaseURL )
        {
            baseURL = globalRestApiSettings.getBaseUrl();
        }
        // next check if this thread has a request
        else if ( Request.getCurrent() != null )
        {
            baseURL = Request.getCurrent().getRootRef().toString();
        }
        // try to use the baseURL
        else
        {
            baseURL = globalRestApiSettings.getBaseUrl();
        }

        // if still null try to figure out the URL from the system properties (only works for the bundle)
        // TODO: this could be problematic, consider removing this
        if ( StringUtils.isEmpty( baseURL ) )
        {
            logger.info( "Base URL not set, this can be set in Administration -> Server -> Application Server Settings" );
            try
            {
                InetAddress local = InetAddress.getLocalHost();
                String hostname = local.getHostName();

                Integer port = Integer.getInteger( "plexus.application-port" );
                String contextPath = System.getProperty( "plexus.webapp-context-path" );

                // assume http?
                if ( port != null && contextPath != null )
                {
                    baseURL =
                        new StringBuffer( "http://" ).append( hostname ).append( ":" ).append( port ).append(
                            contextPath ).toString();
                }
            }
            catch ( UnknownHostException e )
            {
                logger.debug( "Failed to find name", e );
            }
        }

        // if all else fails?
        if ( StringUtils.isEmpty( baseURL ) )
        {
            baseURL = "http://base-url-not-set/"; // TODO: what should we do here ?
        }

        StringBuffer url = new StringBuffer( baseURL );
        if ( !baseURL.endsWith( "/" ) )
        {
            url.append( "/" );
        }

        String descriptiveURLPart = "repositories";
        for ( RepositoryTypeDescriptor desc : repositoryTypeRegistry.getRegisteredRepositoryTypeDescriptors() )
        {
            if ( repository.getProviderRole().equals( desc.getRole().getName() ) )
            {
                descriptiveURLPart = desc.getPrefix();
                break;
            }
        }

        url.append( "content/" ).append( descriptiveURLPart ).append( "/" ).append( repository.getId() );

        return url.toString();
    }

}
