package org.sonatype.nexus.rest.mirrors;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryPredefinedMirrorListPlexusResource" )
public class RepositoryPredefinedMirrorListPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    public RepositoryPredefinedMirrorListPlexusResource()
    {
        setModifiable( false );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repository_predefined_mirrors/*", "authcBasic,perms[nexus:repositorypredefinedmirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_predefined_mirrors/{" + REPOSITORY_ID_KEY + "}";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorResourceListResponse dto = new MirrorResourceListResponse();
        
        //TODO: retrieve a list of mirrors from somewhere
        
        MirrorResource resource = new MirrorResource();
        resource.setUrl( "http://someurl" );
        resource.setId( StringDigester.getSha1Digest( resource.getUrl() ) );
        dto.addData( resource );
        
        resource = new MirrorResource();
        resource.setUrl( "http://someurl2" );
        resource.setId( StringDigester.getSha1Digest( resource.getUrl() ) );
        dto.addData( resource );
        
        resource = new MirrorResource();
        resource.setUrl( "http://someurl3" );
        resource.setId( StringDigester.getSha1Digest( resource.getUrl() ) );
        dto.addData( resource );
        
        return dto;
    }

}
