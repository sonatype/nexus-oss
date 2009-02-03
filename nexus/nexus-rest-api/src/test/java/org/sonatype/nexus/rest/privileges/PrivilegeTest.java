/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.privileges;

import org.restlet.data.MediaType;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
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
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryTargetId().equals( "targetId" );
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryId().equals( "repoId" );
        assert ( (PrivilegeTargetResource) request.getData() ).getRepositoryGroupId().equals( "groupId" );
    }
}
