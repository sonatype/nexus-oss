package org.sonatype.nexus.integrationtests.nexus383;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;

public class Nexus383Search
    extends AbstractNexusIntegrationTest
{

    protected SearchMessageUtil messageUtil;

    public Nexus383Search()
    {
        this.messageUtil = new SearchMessageUtil( this.getBaseNexusUrl() );
    }

    @Test
    public void searchForGroupId()
        throws Exception
    {
        List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
        Assert.assertEquals( 3, results.size() );

        results = messageUtil.searchFor( "nexus-383" );
        Assert.assertTrue( results.isEmpty() );
    }

    @Test
    public void searchForArtifactId()
        throws Exception
    {
        List<NexusArtifact> results = messageUtil.searchFor( "know-artifact-1" );
        Assert.assertEquals( 2, results.size() );

        results = messageUtil.searchFor( "know-artifact-2" );
        Assert.assertEquals( 1, results.size() );

        results = messageUtil.searchFor( "know-artifact" );
        Assert.assertEquals( 3, results.size() );

        results = messageUtil.searchFor( "unknow-artifacts" );
        Assert.assertTrue( results.isEmpty() );
    }

    @Test
    public void searchForSHA1()
        throws Exception
    {
        // know-artifact-1
        NexusArtifact result = messageUtil.searchForSHA1( "2e4213cd44e95dd306a74ba002ed1fa1282f0a51" );
        Assert.assertNotNull( result );

        // know-artifact-2
        result = messageUtil.searchForSHA1( "807f665cd73a2e62e169453e5af4cd5241b9a232" );
        Assert.assertNotNull( result );

        // velo's picture
        result = messageUtil.searchForSHA1( "612c17de73fdc8b9e3f6a063154d89946eb7c6f2" );
        Assert.assertNull( result );
    }

}
