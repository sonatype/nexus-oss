package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;

import com.thoughtworks.xstream.XStream;

public class Nexus133TargetCrudXmlTests
    extends Nexus133TargetCrudJsonTests
{

    public Nexus133TargetCrudXmlTests()
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
