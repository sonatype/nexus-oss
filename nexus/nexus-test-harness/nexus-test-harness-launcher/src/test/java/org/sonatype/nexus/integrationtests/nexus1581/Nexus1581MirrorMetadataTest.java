package org.sonatype.nexus.integrationtests.nexus1581;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Reader;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Writer;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;

public class Nexus1581MirrorMetadataTest
    extends AbstractNexusIntegrationTest
{    
    private MirrorMessageUtils mirrorUtils;
    
    public Nexus1581MirrorMetadataTest()
    {
        mirrorUtils = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }
    @Test
    public void testMetadata()
        throws Exception
    {
        File metadata = new File( AbstractNexusIntegrationTest.nexusWorkDir + "/storage/nexus-test-harness-repo/.meta/nexus-repository-metadata.xml" );
        
        RepositoryMetadataXpp3Reader reader = new RepositoryMetadataXpp3Reader();
        RepositoryMetadataXpp3Writer writer = new RepositoryMetadataXpp3Writer();
        
        FileInputStream fis = new FileInputStream( metadata );
        FileWriter fw = new FileWriter( metadata );
        
        RepositoryMetadata rm = null;
        
        try
        {
            rm = reader.read( fis );
        }
        catch ( Exception e )
        {
            rm = new RepositoryMetadata();
        }
        finally
        {
            fis.close();
        }
        
        rm.getMirrors().clear();
        
        RepositoryMirrorMetadata rmm = new RepositoryMirrorMetadata();
        rmm.setId( "id1" );
        rmm.setUrl( "http://localhost:8081/somemirror" );
        
        rm.addMirror( rmm );
        
        rmm = new RepositoryMirrorMetadata();
        rmm.setId( "id2" );
        rmm.setUrl( "http://localhost:8086/somemirror2" );
        
        rm.addMirror( rmm );
        
        rmm = new RepositoryMirrorMetadata();
        rmm.setId( "id3" );
        rmm.setUrl( "http://localhost:8086/somemirror3" );
        
        rm.addMirror( rmm );
        
        try
        {
            writer.write( fw, rm );
        }
        finally
        {
            fw.close();
        }
        
        MirrorResourceListResponse response = mirrorUtils.getPredefinedMirrors( "nexus-test-harness-repo" );
        
        Assert.assertEquals( 3, response.getData().size() );
        Assert.assertEquals( "id1", ( ( MirrorResource ) response.getData().get(0) ).getId() );
        Assert.assertEquals( "http://localhost:8081/somemirror", ( ( MirrorResource ) response.getData().get(0) ).getUrl() );
        Assert.assertEquals( "id2", ( ( MirrorResource ) response.getData().get(1) ).getId() );
        Assert.assertEquals( "http://localhost:8086/somemirror2", ( ( MirrorResource ) response.getData().get(1) ).getUrl() );
        Assert.assertEquals( "id3", ( ( MirrorResource ) response.getData().get(2) ).getId() );
        Assert.assertEquals( "http://localhost:8086/somemirror3", ( ( MirrorResource ) response.getData().get(2) ).getUrl() );
    }
}
