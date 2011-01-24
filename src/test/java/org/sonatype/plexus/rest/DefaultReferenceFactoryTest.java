package org.sonatype.plexus.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;

public class DefaultReferenceFactoryTest
{
    private DefaultReferenceFactory f;

    @Before
    public void create()
    {
        f = new DefaultReferenceFactory();
    }

    @Test
    public void testCreateChildReference()
    {
        Reference root = new Reference( "http://localhost:8080/" );
        Request req = new Request( Method.GET, root );
        req.setRootRef( root );
        Reference ref = f.createChildReference( req, "test" );
        assertThat( ref.toString(), equalTo( "http://localhost:8080/test" ) );

        req = new Request( Method.GET, ref );
        req.setRootRef( root );
        ref = f.createChildReference( req, "test2" );
        assertThat( ref.toString(), equalTo( "http://localhost:8080/test/test2" ) );
    }

}
