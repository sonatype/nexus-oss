package org.sonatype.nexus.rest.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Resource handler for Repository content resource.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryContentPlexusResource" )
public class RepositoryContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
{

    public RepositoryContentPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/content/**", "authcBasic,trperms" );
    }

    public boolean acceptsUpload()
    {
        return true;
    }

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            NoSuchRepositoryRouterException,
            ResourceException
    {
        return getNexus().getRepository(
            request.getAttributes().get( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString() );
    }

}
