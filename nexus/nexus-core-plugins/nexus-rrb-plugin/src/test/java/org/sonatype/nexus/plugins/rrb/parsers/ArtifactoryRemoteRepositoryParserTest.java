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
    String localPrefix = "http://localhost:8081/nexus/service/local/repositories/ArtyJavaNet/remotebrowser/";

    @Before
    public void setUp()
        throws Exception
    {
        String remoteUrl = "http://www.remote.com"; // The exact name doesn't matter
        //However the format of the localUrl is important for the outcome
        String localUrl = localPrefix + "http://repo.jfrog.org/artifactory/java.net";
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
        	//One repo is a leaf, "archetype-catalog.xml", the rest are not leafs
        	assertEquals(repo.getText().equals("archetype-catalog.xml"), repo.isLeaf());
        	assertTrue(repo.getResourceURI().matches(localPrefix + "http.*/" + repo.getText() + (repo.isLeaf() ? "" : "/")));
		}
    }
    
    @Test
    public void testExcludeDottedLinks() {
    	//A test of excluding dotted links
    	ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();
    	result = parser.extractLinks( new StringBuilder( dottedLinkExample() ) );
        assertEquals( 1, result.size() );
    }
    
    /**
     * An example with one real and one dotted link
     * @return
     */
    private String dottedLinkExample() 
    {
    	return 
    	"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
    	"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:wicket=\"http://wicket.sourceforge.net/\">" +
    	"<head>" +
    	"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
    	"<link rel=\"icon\" type=\"image/x-icon\" href=\"../../../../favicon.ico\"/>" +
    	"<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"../../../../favicon.ico\"/>" +
    	"<title>Artifactory@repo.jfrog.org :: Repository Browser</title>" +
    	"<meta content=\"10040\" name=\"revision\"/>" +
    	"</head>" + 
     	"<body class=\"tundra\">" +
    	"<table cellpadding=\"0\" cellspacing=\"0\" class=\"page\">" +
    	"<tr>" +
    	"<a title=\"Artifactory\" class=\" artifactory-logo\" href=\"../../../../webapp/home.html\">" +
    	"<span class=\"none\">Artifactory</span>" +
    	"<div class=\"local-repos-list\">" +
    	"<div>" +
    	"<a class=\"icon-link folder\" href=\"http://repo.jfrog.org/artifactory/java.net-cache/commons-httpclient/commons-httpclient/\">..</a>" +
    	"<a class=\"icon-link jar\" href=\"http://repo.jfrog.org/artifactory/java.net-cache/commons-httpclient/commons-httpclient/3.1-rc1/commons-httpclient-3.1-rc1-sources.jar\">commons-httpclient-3.1-rc1-sources.jar</a>" +
    	"</div>" +
    	"</div>" +
    	"</body>" +
    	"</html>";
    }

}
