package org.sonatype.nexus.integrationtests.nexus570;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

public class Nexus570IndexArchetypeTest extends AbstractNexusIntegrationTest
{

    @Test
    public void searchForArchetype() throws Exception
    {
        
        SearchMessageUtil searchUtil = new SearchMessageUtil();
        Map<String, String> args = new HashMap<String, String>();
        args.put( "a", "simple-archetype" );
        args.put( "g", "nexus570" );
        
        List<NexusArtifact> results = searchUtil.searchFor( args );
        
        Assert.assertEquals( 1, results.size() );
        Assert.assertEquals("Expected maven-archetype packaging: "+ results.get( 0 ).getPackaging(), "maven-archetype", results.get( 0 ).getPackaging() );
        
    }
    
    @Test
    public void searchForjar() throws Exception
    {
        
        SearchMessageUtil searchUtil = new SearchMessageUtil();
        Map<String, String> args = new HashMap<String, String>();
        args.put( "a", "normal" );
        args.put( "g", "nexus570" );
        
        List<NexusArtifact> results = searchUtil.searchFor( args );
        
        Assert.assertEquals( 1, results.size() );
        Assert.assertEquals("Expected jar packaging: "+ results.get( 0 ).getPackaging(), "jar", results.get( 0 ).getPackaging() );
        
        
    }
    
}
