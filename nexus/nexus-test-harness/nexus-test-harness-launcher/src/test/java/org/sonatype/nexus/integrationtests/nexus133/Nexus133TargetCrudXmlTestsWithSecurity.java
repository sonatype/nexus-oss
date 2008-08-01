package org.sonatype.nexus.integrationtests.nexus133;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.SecurityTest;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.TargetMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus133TargetCrudXmlTestsWithSecurity extends Nexus133TargetCrudXmlTests implements SecurityTest
{

    public Nexus133TargetCrudXmlTestsWithSecurity()
    {
        this.messageUtil =
            new TargetMessageUtil( XStreamInitializer.initialize( new XStream() ),
                                 MediaType.APPLICATION_XML, this.getBaseNexusUrl() );
    }
    
}
