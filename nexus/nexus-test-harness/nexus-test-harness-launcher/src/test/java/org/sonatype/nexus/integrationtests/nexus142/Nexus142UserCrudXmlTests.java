package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;

import com.thoughtworks.xstream.XStream;

public class Nexus142UserCrudXmlTests
    extends Nexus142UserCrudJsonTests
{

    public Nexus142UserCrudXmlTests()
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
