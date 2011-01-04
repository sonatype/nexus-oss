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
package org.sonatype.nexus.rest.privileges;

import org.restlet.data.MediaType;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class PrivilegeTest
    extends AbstractRestTestCase
{

    public void testTargetRequest()
        throws Exception
    {
        String jsonString = "{\"data\":{\"name\":\"Test Priv\",\"type\":\"target\",\"method\":[\"read\",\"create\"],"
            + "\"repositoryTargetId\":\"targetId\",\"repositoryId\":\"repoId\",\"repositoryGroupId\":\"groupId\"}}";
        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        PrivilegeResourceRequest request = (PrivilegeResourceRequest) representation
            .getPayload( new PrivilegeResourceRequest() );

        assert request.getData().getName().equals( "Test Priv" );
        assert request.getData().getType().equals( TargetPrivilegeDescriptor.TYPE );
        assert request.getData().getMethod().size() == 2;
        assert request.getData().getMethod().contains( "read" );
        assert request.getData().getMethod().contains( "create" );
        assert request.getData().getRepositoryTargetId().equals( "targetId" );
        assert request.getData().getRepositoryId().equals( "repoId" );
        assert request.getData().getRepositoryGroupId().equals( "groupId" );
    }
}
