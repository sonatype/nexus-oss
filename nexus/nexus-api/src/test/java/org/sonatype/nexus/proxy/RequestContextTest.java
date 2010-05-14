package org.sonatype.nexus.proxy;

import junit.framework.TestCase;

public class RequestContextTest
    extends TestCase
{
    public void testNullParent()
    {
        RequestContext requestContext = new RequestContext( null );
        assertNull( requestContext.getParentContext() );

        requestContext.setParentContext( null );
        assertNull( requestContext.getParentContext() );
    }

    public void testValidParent()
    {
        RequestContext parentContext = new RequestContext( null );
        RequestContext requestContext = new RequestContext( parentContext );
        assertEquals( parentContext, requestContext.getParentContext() );

        requestContext.setParentContext( null );
        assertNull( requestContext.getParentContext() );

        requestContext = new RequestContext();
        assertNull( requestContext.getParentContext() );
        requestContext.setParentContext( parentContext );
        assertEquals( parentContext, requestContext.getParentContext() );
    }

    public void testSelfParent()
    {
        RequestContext requestContext = new RequestContext();
        assertNull( requestContext.getParentContext() );

        try
        {
            requestContext.setParentContext( requestContext );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException expected )
        {
        }

        assertNull( requestContext.getParentContext() );
    }

    // 3-->2-->1-->3
    public void testSelfAncestor()
    {
        RequestContext requestContext1 = new RequestContext();
        assertNull( requestContext1.getParentContext() );
        RequestContext requestContext2 = new RequestContext( requestContext1 );
        assertEquals( requestContext1, requestContext2.getParentContext() );
        RequestContext requestContext3 = new RequestContext( requestContext2 );
        assertEquals( requestContext2, requestContext3.getParentContext() );

        try
        {
            requestContext1.setParentContext( requestContext3 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException expected )
        {
        }

        assertNull( requestContext1.getParentContext() );
    }
}
