package org.sonatype.security.sample.web;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A Resource that simple returns "Hello".
 */
@Component( role = PlexusResource.class, hint = "sample-resource" )
public class SimplePlexusResource
    extends AbstractPlexusResource
{

    public Object getPayloadInstance()
    {
        // do nothing, this is a read only resource.
        return null;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        // any users with the permission 'sample:priv-name:read' can access this resource, NOTE: the 'read' part is
        // because we are doing a GET.
        return new PathProtectionDescriptor( this.getResourceUri(), "authcBasic,perms[sample:priv-name]" );
    }

    public String getResourceUri()
    {
        // we need to say where we are mounting this resource.
        return "/test";
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return "Hello";
    }

}
