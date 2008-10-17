package org.sonatype.nexus.integrationtests.nexus950;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus950CorruptPomTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void uploadCorruptPomTest() throws HttpException, IOException
    {
        
        File jarFile = this.getTestFile( "bad-pom.jar" );
        File badPomFile = this.getTestFile( "pom.xml" );
        
        HttpMethod resultMethod = DeployUtils.deployUsingPomWithRestReturnResult( this.getTestRepositoryId(), jarFile, badPomFile, "", "jar" );
        
        Assert.assertEquals( "", resultMethod.getStatusCode(), 500 );        
    }
    
}
