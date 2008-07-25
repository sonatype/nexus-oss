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
package org.sonatype.nexus.rest.privileges;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class PrivilegeTest
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

    public void testTargetRequest()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"name\":\"Test Priv\",\"type\":\"repositoryTarget\",\"method\":[\"read\",\"create\"]," +
            "\"repositoryTargetId\":\"targetId\",\"repositoryId\":\"repoId\",\"repositoryGroupId\":\"groupId\"}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );
        
        PrivilegeResourceRequest request = ( PrivilegeResourceRequest ) representation.getPayload( new PrivilegeResourceRequest() );

        assert request.getData().getName().equals( "Test Priv" );
        assert request.getData().getType().equals( AbstractPrivilegeResourceHandler.TYPE_REPO_TARGET );
        assert request.getData().getMethod().size() == 2;
        assert request.getData().getMethod().contains( "read" );
        assert request.getData().getMethod().contains( "create" );
        assert ( ( PrivilegeTargetResource ) request.getData() ).getRepositoryTargetId().equals( "targetId" );
        assert ( ( PrivilegeTargetResource ) request.getData() ).getRepositoryId().equals( "repoId" );
        assert ( ( PrivilegeTargetResource ) request.getData() ).getRepositoryGroupId().equals( "groupId" );
    }
}
