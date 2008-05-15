package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.nexus.gwt.client.Nexus;

public class AbstractNexusService
    extends DefaultResource
{
    private Nexus nexus;

    public AbstractNexusService( Nexus nexus, String path )
    {
        super( path, nexus.getRestRequestBuilder() );

        this.nexus = nexus;
    }

    public Nexus getNexus()
    {
        return nexus;
    }
}
