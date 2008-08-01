package org.sonatype.nexus.integrationtests.nexus142;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.SecurityTest;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;

import com.thoughtworks.xstream.XStream;

public class Nexus142UserCrudXmlTestsWithSecurity
    extends Nexus142UserCrudXmlTests
    implements SecurityTest
{

    public Nexus142UserCrudXmlTestsWithSecurity()
    {
        this.messageUtil =
            new UserMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML,
                                 this.getBaseNexusUrl() );
    }

}
