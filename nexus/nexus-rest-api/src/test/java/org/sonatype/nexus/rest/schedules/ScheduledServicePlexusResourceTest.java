/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.schedules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.scheduling.DefaultScheduledTask;
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

    @SuppressWarnings( "rawtypes" )
    private static class MockDefaultScheduledTask
        extends DefaultScheduledTask
    {

        public MockDefaultScheduledTask()
        {
            super( "id", "name", "type", null, null, null );
        }

        @Override
        public void setLastStatus( TaskState lastStatus )
        {
            super.setLastStatus( lastStatus );
        }

        @Override
        public void setDuration( long duration )
        {
            super.setDuration( duration );
        }
    }

    @Test
    public void testGetLastRunResult()
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

        MockDefaultScheduledTask task = new MockDefaultScheduledTask();
        task.setLastStatus( TaskState.FINISHED );

        task.setDuration( 58 * 1000 );
        assertThat( service.getLastRunResult( task ), equalTo( "Ok [58s]" ) );

        task.setDuration( 7 * 60 * 1000 );
        assertThat( service.getLastRunResult( task ), equalTo( "Ok [7m0s]" ) );

        task.setDuration( 3 * 60 * 60 * 1000 );
        assertThat( service.getLastRunResult( task ), equalTo( "Ok [3h0m0s]" ) );

        task.setDuration( 2 * 24 * 60 * 60 * 1000 + 5 * 60 * 60 * 1000 + 13 * 60 * 1000 + 22 * 1000 );
        assertThat( service.getLastRunResult( task ), equalTo( "Ok [53h13m22s]" ) );
    }

}
