/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.schedules;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class ScheduledServiceResponseTest
    extends TestCase
{

    protected XStream xstream;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // create and configure XStream for JSON
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testNoScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"none\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "none" );
    }

    public void testOnceScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"once\",\"startDate\":\"1210651200000\",\"startTime\":\"12:30\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "once" );
        assert ( (ScheduledServiceOnceResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceOnceResource) response.getData() ).getStartTime().equals( "12:30" );
    }

    public void testDailyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"daily\",\"startDate\":\"1210651200000\",\"startTime\":\"12:30\",\"recurringTime\":\"12:30\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "daily" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getStartTime().equals( "12:30" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getRecurringTime().equals( "12:30" );
        
        representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        representation.setPayload( response );
        response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "daily" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getStartTime().equals( "12:30" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testWeeklyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"weekly\",\"startDate\":\"1210651200000\",\"startTime\":\"12:30\",\"recurringTime\":\"12:30\",\"recurringDay\":[\"Monday\",\"Wednesday\"]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "weekly" );
        assert ( (ScheduledServiceWeeklyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceWeeklyResource) response.getData() ).getStartTime().equals( "12:30" );
        assert ( (ScheduledServiceWeeklyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testMonthlyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"monthly\",\"startDate\":\"1210651200000\",\"startTime\":\"12:30\",\"recurringTime\":\"12:30\",\"recurringDay\":[\"1\",\"2\"]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "monthly" );
        assert ( (ScheduledServiceMonthlyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceMonthlyResource) response.getData() ).getStartTime().equals( "12:30" );
        assert ( (ScheduledServiceMonthlyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testAdvancedScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"serviceType\":\"Synchronize Repositories\",\"serviceSchedule\":\"advanced\",\"cronCommand\":\"somecroncommand\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getServiceType().equals( "Synchronize Repositories" );
        assert response.getData().getServiceSchedule().equals( "advanced" );
        assert ( (ScheduledServiceAdvancedResource) response.getData() ).getCronCommand().equals( "somecroncommand" );
    }
}
