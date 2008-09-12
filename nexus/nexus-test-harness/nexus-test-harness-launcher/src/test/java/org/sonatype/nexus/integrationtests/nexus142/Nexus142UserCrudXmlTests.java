package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

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
