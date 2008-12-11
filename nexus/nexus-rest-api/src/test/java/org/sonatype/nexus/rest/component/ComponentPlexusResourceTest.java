/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.component;

import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;

public class ComponentPlexusResourceTest
    extends PlexusTestCase
{

    private AbstractComponentListPlexusResource getComponentPlexusResource()
        throws Exception
    {
        return (AbstractComponentListPlexusResource) this.lookup( PlexusResource.class, "ComponentPlexusResource" );
    }

    private PlexusComponentListResourceResponse runGetForRole( String role )
        throws Exception
    {
        AbstractComponentListPlexusResource componentPlexusResource = this.getComponentPlexusResource();

        Request request = new Request();
        request.getAttributes().put( AbstractComponentListPlexusResource.ROLE_ID, role );

        return (PlexusComponentListResourceResponse) componentPlexusResource.get( null, request, null, null );
    }

    public void testInvalidRole()
        throws Exception
    {
        try
        {
            runGetForRole( "JUNK-FOO_BAR-JUNK" );
            Assert.fail( "expected error thrown" );
        }
        catch ( ResourceException e )
        {
            Assert.assertEquals( "Expected a 404 error message.", 404, e.getStatus().getCode() );
        }
    }

    public void testValidRoleMultipleResults()
        throws Exception
    {
        PlexusComponentListResourceResponse result = runGetForRole( PlexusResource.class.getName() );

        Assert.assertTrue( result.getData().size() > 1 ); // expected a bunch of these thing, with new ones being
        // added all the time.

        // now for a more controled test
        result = runGetForRole( "MULTI-TEST" );
        Assert.assertEquals( 2, result.getData().size() );

        // the order is undefined
        PlexusComponentListResource resource1 = null;
        PlexusComponentListResource resource2 = null;

        for ( PlexusComponentListResource resource : (List<PlexusComponentListResource>) result.getData() )
        {
            if ( resource.getRoleHint().endsWith( "1" ) )
            {
                resource1 = (PlexusComponentListResource) result.getData().get( 0 );
            }
            else
            {
                resource2 = (PlexusComponentListResource) result.getData().get( 1 );
            }
        }

        // make sure we found both
        Assert.assertNotNull( resource1 );
        Assert.assertNotNull( resource2 );

        Assert.assertEquals( "Description-1", resource1.getDescription() );
        Assert.assertEquals( "hint-1", resource1.getRoleHint() );

        Assert.assertEquals( "Description-2", resource2.getDescription() );
        Assert.assertEquals( "hint-2", resource2.getRoleHint() );

    }

    public void testValidRoleSingleResult()
        throws Exception
    {
        PlexusComponentListResourceResponse result = runGetForRole( "TEST-ROLE" );

        Assert.assertTrue( result.getData().size() == 1 );

        PlexusComponentListResource resource = (PlexusComponentListResource) result.getData().get( 0 );

        Assert.assertEquals( "Test Description.", resource.getDescription() );
        Assert.assertEquals( "test-hint", resource.getRoleHint() );
    }

    public void testNullDescriptionAndHint()
        throws Exception
    {
        PlexusComponentListResourceResponse result = runGetForRole( "TEST-null" );

        Assert.assertTrue( result.getData().size() == 1 );

        PlexusComponentListResource resource = (PlexusComponentListResource) result.getData().get( 0 );

        Assert.assertEquals( "default", resource.getDescription() );
        Assert.assertEquals( "default", resource.getRoleHint() );
    }

    public void testEmptyDescriptionAndHint()
        throws Exception
    {
        PlexusComponentListResourceResponse result = runGetForRole( "TEST-empty" );

        Assert.assertTrue( result.getData().size() == 1 );

        PlexusComponentListResource resource = (PlexusComponentListResource) result.getData().get( 0 );

        Assert.assertEquals( "default", resource.getDescription() );
        Assert.assertEquals( "default", resource.getRoleHint() );
    }
}
