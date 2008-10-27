package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.test.utils.UserMessageUtil;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus142UserCrudXmlTests
    extends Nexus142UserCrudJsonTests
{

    public Nexus142UserCrudXmlTests()
    {
        this.messageUtil = new UserMessageUtil(this.getXMLXStream(), MediaType.APPLICATION_XML );
    }
    
    
    
    @Test
    public void readTest()
        throws IOException
    {
        super.readTest();
    }
    
}
