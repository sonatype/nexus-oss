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
        this.messageUtil =
            new TargetMessageUtil( XStreamInitializer.initialize( new XStream() ),
                                 MediaType.APPLICATION_XML, this.getBaseNexusUrl() );
    }
    
    
    
    @Test
    public void readTest()
        throws IOException
    {
        super.readTest();
    }
    
}
