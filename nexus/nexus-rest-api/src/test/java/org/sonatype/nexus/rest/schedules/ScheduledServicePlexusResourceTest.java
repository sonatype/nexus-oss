package org.sonatype.nexus.rest.schedules;

import org.junit.Test;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.scheduling.TaskState;

public class ScheduledServicePlexusResourceTest
{

    @Test
    public void testGetReadableState()
    {
        AbstractScheduledServicePlexusResource service = new AbstractScheduledServicePlexusResource()
        {

            @Override
            public String getResourceUri()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PathProtectionDescriptor getResourceProtection()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getPayloadInstance()
            {
                // TODO Auto-generated method stub
                return null;
            }
        };

        TaskState[] states = TaskState.values();
        for ( TaskState state : states )
        {
            service.getReadableState( state );
        }
    }

}
