package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;

import com.thoughtworks.xstream.XStream;

public class Nexus156RolesCrudXmlTests
    extends Nexus156RolesCrudJsonTests
{

    public Nexus156RolesCrudXmlTests()
    {
        xstream = XStreamInitializer.initialize( new XStream( ) );
        this.mediaType = MediaType.APPLICATION_XML;
    }
    
    @Test
    public void readTest()
        throws IOException
    {
        super.readTest();
    }
    
}
