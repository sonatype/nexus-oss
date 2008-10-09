package org.sonatype.nexus.rest.schedules;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.rest.component.AbstractComponentListPlexusResource;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ScheduledTaskTypeComonentListPlexusResource" )
public class ScheduledTaskTypeComonentListPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/schedule_types";
    }

    @Override
    protected String getRole( Request request )
    {
        return ScheduledTaskDescriptor.class.getName();
    }
}
