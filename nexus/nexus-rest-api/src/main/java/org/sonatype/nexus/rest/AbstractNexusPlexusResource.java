package org.sonatype.nexus.rest;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.Nexus;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;

public abstract class AbstractNexusPlexusResource
    extends AbstractPlexusResource
    implements PlexusResource
{
    public static final String NEXUS_INSTANCE_KEY = "instanceName";

    public static final String NEXUS_INSTANCE_LOCAL = "local";

    /**
     * @plexus.requirement
     */
    private Nexus nexus;

    protected Nexus getNexusInstance( Request request )
        throws ResourceException
    {
        if ( NEXUS_INSTANCE_LOCAL.equals( request.getAttributes().get( NEXUS_INSTANCE_KEY ) ) )
        {
            return nexus;
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Nexus instance named '"
                + request.getAttributes().get( NEXUS_INSTANCE_KEY ) + "' not found!" );
        }
    }

    protected Reference createChildReference( Request request, String childPath )
    {
        return new Reference( request.getResourceRef() ).addSegment( childPath ).getTargetRef();
    }

}
