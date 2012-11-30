/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.roles;

import junit.framework.Assert;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;
import org.sonatype.security.rest.users.AbstractSecurityRestTest;

public class RolesPRTest
    extends AbstractSecurityRestTest
{

    public void testPostWithSpace()
        throws Exception
    {
        PlexusResource resource = this.lookup( PlexusResource.class, "RoleListPlexusResource" );

        RoleResourceRequest roleRequest = new RoleResourceRequest();
        roleRequest.setData( new RoleResource() );

        roleRequest.getData().setId( "with spaces" );
        roleRequest.getData().setDescription( "foo" );
        roleRequest.getData().setName( "Foo Bar" );
        roleRequest.getData().setSessionTimeout( 60 );
        roleRequest.getData().addPrivilege( "1001" );

        Request request = new Request();
        Reference ref = new Reference( "http://localhost:12345/" );
        request.setRootRef( ref );
        request.setResourceRef( new Reference( ref, "roles" ) );

        Response response = new Response( request );

        RoleResourceResponse roleResponse = (RoleResourceResponse) resource.post( null, request, response, roleRequest );

        Assert.assertEquals( "with spaces", roleResponse.getData().getId() );

        // ok now we try the gets
        resource = this.lookup( PlexusResource.class, "RolePlexusResource" );

        // first with +
        request.getAttributes().put( "roleId", "with+spaces" );
        roleResponse = (RoleResourceResponse) resource.get( null, request, response, null );
        Assert.assertEquals( "with spaces", roleResponse.getData().getId() );

        // then with %20
        request.getAttributes().put( "roleId", "with%20spaces" );
        roleResponse = (RoleResourceResponse) resource.get( null, request, response, null );
        Assert.assertEquals( "with spaces", roleResponse.getData().getId() );

    }

}
