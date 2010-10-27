package org.sonatype.nexus.rest;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

public class Nexus2302Test
    extends org.sonatype.nexus.AbstractNexusTestCase
{
    private ContentPlexusResource contentResource;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        contentResource = (ContentPlexusResource) lookup( ManagedPlexusResource.class, "content" );
    }

    public void doTestPathEncoding( final String uri )
    {
        // this is how it would come from some servlet container (encoded)
        final String encodedUri = Reference.encode( uri );

        Reference baseRef = new Reference( "http://localhost:8081/nexus/" );

        Request request = new Request( Method.GET, new Reference( baseRef, baseRef.toString() + encodedUri ) );

        // check is the method getResourceStorePath() handles encoded paths
        final String resourceStorePath = contentResource.getResourceStorePath( request );

        assertEquals( uri, resourceStorePath );
    }

    public void testSimplePath()
    {
        // FIXME: skipped
        // doTestPathEncoding( "content/repositories/central/org/log4j/log4j/1.2.13/log4j-1.2.13.jar" );
    }

    public void testProblematicPath()
    {
        // doTestPathEncoding(
        // "content/repositories/central/nexus-2302/utilities/0.4.0-SNAPSHOT/utilities-0.4.0-SNAPSHOT-i386-Linux-g++-static.nar"
        // );
    }

}
