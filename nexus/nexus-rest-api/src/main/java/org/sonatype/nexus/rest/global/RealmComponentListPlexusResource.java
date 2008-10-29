package org.sonatype.nexus.rest.global;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.realm.Realm;
import org.restlet.data.Request;
import org.sonatype.nexus.rest.component.AbstractComponentListPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RealmComponentListPlexusResource" )
public class RealmComponentListPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/realm_types";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentrealmtypes]" );
    }

    @Override
    protected String getRole( Request request )
    {
        return Realm.class.getName();
    }
}
