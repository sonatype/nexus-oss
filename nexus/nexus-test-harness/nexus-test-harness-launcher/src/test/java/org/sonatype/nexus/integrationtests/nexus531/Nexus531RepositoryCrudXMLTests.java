package org.sonatype.nexus.integrationtests.nexus531;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus531RepositoryCrudXMLTests
    extends Nexus531RepositoryCrudJsonTests
{
    public Nexus531RepositoryCrudXMLTests()
    {
        this.messageUtil =
            new RepositoryMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
    }
}
