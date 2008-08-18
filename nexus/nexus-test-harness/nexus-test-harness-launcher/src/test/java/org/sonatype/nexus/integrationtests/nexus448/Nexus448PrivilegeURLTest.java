package org.sonatype.nexus.integrationtests.nexus448;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;

import com.thoughtworks.xstream.XStream;

/**
 * GETS for application privileges where returning an error, so this is a really simple test to make sure the GET will work.
 *
 */
public class Nexus448PrivilegeURLTest extends AbstractNexusIntegrationTest
{

    private PrivilegesMessageUtil messageUtil;

    public Nexus448PrivilegeURLTest()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void testUrls() throws IOException
    {
        
        PrivilegeBaseStatusResource resource = this.messageUtil.getPrivilegeResource( "T2" );
        Assert.assertEquals( "Type", "repositoryTarget", resource.getType() );
        
        resource = this.messageUtil.getPrivilegeResource( "1" );
        Assert.assertEquals( "Type", "application", resource.getType() );
        
    }
    
    
}
