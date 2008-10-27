package org.sonatype.nexus.integrationtests.nexus531;

import org.restlet.data.MediaType;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus531RepositoryCrudXMLTests
    extends Nexus531RepositoryCrudJsonTests
{
    public Nexus531RepositoryCrudXMLTests()
    {
        this.messageUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }
}
