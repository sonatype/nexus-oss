package org.sonatype.nexus.rest.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource list for Repository list.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "AllRepositoryListPlexusResource" )
public class AllRepositoryListPlexusResource
    extends AbstractRepositoryPlexusResource
{
    public AllRepositoryListPlexusResource()
    {
        this.setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/all_repositories";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repositories]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return listRepositories( request, true );
    }

}
