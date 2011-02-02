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
package org.sonatype.nexus.integrationtests.nexus969;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus969CacheEvictInteractionIT
    extends AbstractNexusIntegrationTest
{

    private static final String CACHE_EVICT = "cache-evict";

    @Test
    public void testCacheAndEvict()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "Can't be kept active, is breaking all other tests" );
            return;
        }
        String id1 = createEvictTask( CACHE_EVICT ).getId();
        String id2 = createEvictTask( CACHE_EVICT + "2" ).getId();
        Assert.assertFalse( id1.equals( id2 ) );
        restartNexus();
        String id3 = createEvictTask( CACHE_EVICT + "3" ).getId();
        Assert.assertFalse( id1.equals( id3 ), "The new task ID should be different both are : " + id3 );
        Assert.assertFalse( id2.equals( id3 ), "The new task ID should be different both are: " + id3 );
    }

    private ScheduledServiceListResource createEvictTask( String taskName )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "all_repo" );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setKey( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 0 ) );
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( taskName );
        scheduledTask.setTypeId( EvictUnusedItemsTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( age );
        scheduledTask.addProperty( repo );

        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertTrue( status.isSuccess(), "Unable to create task: " + status.getDescription() );

        return TaskScheduleUtil.getTask( taskName );
    }
}
