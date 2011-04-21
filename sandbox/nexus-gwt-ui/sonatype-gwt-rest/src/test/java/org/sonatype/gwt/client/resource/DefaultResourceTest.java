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
package org.sonatype.gwt.client.resource;

import org.sonatype.gwt.client.request.DefaultRESTRequestBuilder;

public class DefaultResourceTest
{

    @Test
    public void testParentsAndChilds()
        throws Exception
    {
        DefaultResource defRes = new DefaultResource( "/some/path/to/somewhere", new DefaultRESTRequestBuilder() );

        Assert.assertEquals( "/some/path/to/somewhere", defRes.getPath() );

        Resource r1 = defRes.getParent();
        Assert.assertEquals( "/some/path/to", r1.getPath() );

        r1 = defRes.getParent().getParent();
        Assert.assertEquals( "/some/path", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent();
        Assert.assertEquals( "/some", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent();
        Assert.assertEquals( "/", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent().getParent();
        Assert.assertEquals( "/", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent().getParent().getParent();
        Assert.assertEquals( "/", r1.getPath() );

        r1 = defRes.getChild( "someId" );
        Assert.assertEquals( "/some/path/to/somewhere/someId", r1.getPath() );

        r1 = defRes.getResource( "some/rel/path" );
        Assert.assertEquals( "/some/path/to/somewhere/some/rel/path", r1.getPath() );

        r1 = defRes.getResource( "/some/abs/path" );
        Assert.assertEquals( "/some/abs/path", r1.getPath() );

        r1 = defRes.getResource( "some/../tricky/../path" );
        Assert.assertEquals( "/some/path/to/somewhere/path", r1.getPath() );

        r1 = defRes.getResource( "/some/../tricky/../abs/../path" );
        Assert.assertEquals( "/path", r1.getPath() );

    }

    @Test
    public void testCreateFromUrl()
        throws Exception
    {
        DefaultResource defRes;

        defRes = new DefaultResource( "http://www.sonatype.com/some/path/to/somewhere" );
        Assert.assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        Assert.assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        Assert.assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        Assert.assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "https://www.sonatype.com/some/path/to/somewhere" );
        Assert.assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        Assert.assertEquals( "https", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        Assert.assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        Assert.assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "http://www.sonatype.com:443/some/path/to/somewhere" );
        Assert.assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        Assert.assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        Assert.assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        Assert.assertEquals( "443", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "http://www.sonatype.com" );
        Assert.assertEquals( "/", defRes.getPath() );
        Assert.assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        Assert.assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        Assert.assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        try
        {
            defRes = new DefaultResource( "/some/path/to/somewhere" );
            fail( "We should not eat non-URLs!" );
        }
        catch ( IllegalArgumentException e )
        {
            // good
        }

    }

}
