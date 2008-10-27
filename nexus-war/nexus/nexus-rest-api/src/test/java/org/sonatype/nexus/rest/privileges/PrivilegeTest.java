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

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class PrivilegeTest
    extends AbstractRestTestCase
{

    public void testTargetRequest()
        throws Exception
    {
        String jsonString = "{\"data\":{\"name\":\"Test Priv\",\"type\":\"repositoryTarget\",\"method\":[\"read\",\"create\"],"
            + "\"repositoryTargetId\":\"targetId\",\"repositoryId\":\"repoId\",\"repositoryGroupId\":\"groupId\"}}";
        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        PrivilegeResourceRequest request = (PrivilegeResourceRequest) representation
            .getPayload( new PrivilegeResourceRequest() );

        assert request.getData().getName().equals( "Test Priv" );
        assert request.getData().getType().equals( AbstractPrivilegePlexusResource.TYPE_REPO_TARGET );
        assert request.getData().getMethod().size() == 2;
        assert request.getData().getMethod().contains( "read" );
        assert request.getData().getMethod().contains( "create" );
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryTargetId().equals( "targetId" );
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryId().equals( "repoId" );
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryGroupId().equals( "groupId" );
    }
}
