package org.sonatype.nexus.rest;

import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

import junit.framework.TestCase;

public abstract class AbstractRestTestCase
    extends TestCase
{
    protected XStream xstream;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // create and configure XStream for JSON
        xstream = new XStream( new JsonOrgHierarchicalStreamDriver() );

        configureXStream( xstream );
    }

    protected void configureXStream( XStream xstream )
    {
        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xstream );
    }
}
