package org.sonatype.nexus.integrationtests.nexus1328;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;

public class Nexus1328RepositoryMirrorCRUDTest
    extends AbstractNexusIntegrationTest
{   
    @BeforeClass
    public static void clean()
    {
        try
        {
            cleanWorkDir();
        }
        catch ( IOException e )
        {
            // NVM
        }
    }
    
    protected MirrorMessageUtils messageUtil;
    
    private String repositoryId = "release-proxy-repo-1";
    
    public Nexus1328RepositoryMirrorCRUDTest()
    {
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }
    
    @Test
    public void setMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://setMirrorTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://setMirrorTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );
    }
    
    public void mirrorStatusTest()
        throws IOException
    {
    }
}
