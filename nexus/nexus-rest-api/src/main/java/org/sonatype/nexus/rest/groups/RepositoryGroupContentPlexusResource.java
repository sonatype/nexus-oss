package org.sonatype.nexus.rest.groups;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryGroupContentPlexusResource" )
public class RepositoryGroupContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
{

    public static final String GROUP_ID_KEY = "groupId";

    /**
     * TODO: THIS IS BAD! Dynamic router needed!
     */
    @Requirement( role = RepositoryRouter.class, hint = "groups" )
    private ResourceStore resourceStore;

    @Override
    public Object getPayloadInstance()
    {
        // group content is read only
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*/content**", "authcBasic,tgperms" );
    }

    @Override
    protected String getResourceStorePath( Request request )
    {
        String groupId = request.getAttributes().get( GROUP_ID_KEY ).toString();
        return "/" + groupId + super.getResourceStorePath( request );
    }

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            NoSuchRepositoryRouterException,
            ResourceException
    {

        return resourceStore;
    }

}
