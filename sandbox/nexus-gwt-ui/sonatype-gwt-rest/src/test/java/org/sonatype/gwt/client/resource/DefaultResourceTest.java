/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
