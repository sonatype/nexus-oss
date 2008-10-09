package org.sonatype.nexus.rest.contentclasses;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.rest.component.AbstractComponentListPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ContentClassComponentListPlexusResource" )
public class ContentClassComponentListPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/repo_content_classes";
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentscontentclasses]" );
    }

    @Override
    protected String getRole( Request request )
    {
        return ContentClass.class.getName();
    }

}
