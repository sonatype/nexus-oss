package org.sonatype.nexus.plugins.rrb.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class ArtifactoryRemoteRepositoryParserTest extends
		RemoteRepositoryParserTestAbstract {
    ArtifactoryRemoteRepositoryParser parser;
    StringBuilder indata;


    @Before
    public void setUp()
        throws Exception
    {
        String remoteUrl = "http://www.remote.com"; // The exact name doesn't matter
        String localUrl = "http://localhost:8081/nexus/service/local/repositories/ArtyJavaNet/remotebrowser/http://repo.jfrog.org/artifactory/java.net"; // doesn't matter in the tests
        parser = new ArtifactoryRemoteRepositoryParser( remoteUrl, localUrl, "test", "http://www.base.com/" );

        // Artifactory.java.net.htm is a file extracted from an Artifactory repo
        indata = new StringBuilder( getExampleFileContent( "/Artifactory.java.net.htm" ) );
        
    }
    
    @Test
    public void testExtractArtifactoryLinks() {
    	ArrayList<String> result = new ArrayList<String>();
    	result = parser.extractArtifactoryLinks( indata );
    	assertEquals( 30, result.size() );
    }

    @Test
    public void testExtractLinks()
        throws Exception
    {   ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();
    	result = parser.extractLinks( indata );
        assertEquals( 30, result.size() );
        for (RepositoryDirectory repo : result) {
        	assertEquals(repo.getText().equals("archetype-catalog.xml"), repo.isLeaf());
        	//System.out.println(repo);
		}
        //TODO more assertions
    }
    
    @Test
    public void testExcludeDottedLinks() {
    	//TODO a test of excluding the links
    	assertTrue(true);
    }

}
