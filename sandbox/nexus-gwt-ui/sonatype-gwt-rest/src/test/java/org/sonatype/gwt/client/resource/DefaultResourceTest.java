package org.sonatype.gwt.client.resource;

import org.sonatype.gwt.client.request.DefaultRESTRequestBuilder;

import junit.framework.TestCase;

public class DefaultResourceTest
    extends TestCase
{

    public void testParentsAndChilds()
        throws Exception
    {
        DefaultResource defRes = new DefaultResource( "/some/path/to/somewhere", new DefaultRESTRequestBuilder() );

        assertEquals( "/some/path/to/somewhere", defRes.getPath() );

        Resource r1 = defRes.getParent();
        assertEquals( "/some/path/to", r1.getPath() );

        r1 = defRes.getParent().getParent();
        assertEquals( "/some/path", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent();
        assertEquals( "/some", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent();
        assertEquals( "/", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent().getParent();
        assertEquals( "/", r1.getPath() );

        r1 = defRes.getParent().getParent().getParent().getParent().getParent().getParent();
        assertEquals( "/", r1.getPath() );

        r1 = defRes.getChild( "someId" );
        assertEquals( "/some/path/to/somewhere/someId", r1.getPath() );

        r1 = defRes.getResource( "some/rel/path" );
        assertEquals( "/some/path/to/somewhere/some/rel/path", r1.getPath() );

        r1 = defRes.getResource( "/some/abs/path" );
        assertEquals( "/some/abs/path", r1.getPath() );

        r1 = defRes.getResource( "some/../tricky/../path" );
        assertEquals( "/some/path/to/somewhere/path", r1.getPath() );

        r1 = defRes.getResource( "/some/../tricky/../abs/../path" );
        assertEquals( "/path", r1.getPath() );

    }

    public void testCreateFromUrl()
        throws Exception
    {
        DefaultResource defRes;

        defRes = new DefaultResource( "http://www.sonatype.com/some/path/to/somewhere" );
        assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "https://www.sonatype.com/some/path/to/somewhere" );
        assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        assertEquals( "https", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "http://www.sonatype.com:443/some/path/to/somewhere" );
        assertEquals( "/some/path/to/somewhere", defRes.getPath() );
        assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        assertEquals( "443", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

        defRes = new DefaultResource( "http://www.sonatype.com" );
        assertEquals( "/", defRes.getPath() );
        assertEquals( "http", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getScheme() );
        assertEquals( "www.sonatype.com", ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getHostname() );
        assertEquals( null, ( (DefaultRESTRequestBuilder) defRes.getRestRequestBuilder() ).getPort() );

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
