package org.sonatype.nexus.integrationtests.nexus384;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

public class Nexus384DotAndDashSearchTest
    extends AbstractNexusIntegrationTest
{
    @BeforeClass
    public static void cleanWorkFolder()
        throws Exception
    {
        cleanWorkDir();
    }

    protected SearchMessageUtil messageUtil;

    public Nexus384DotAndDashSearchTest()
    {
        this.messageUtil = new SearchMessageUtil();
        
        if( printKnownErrorButDoNotFail( Nexus384DotAndDashSearchTest.class, "searchDash", "searchDot", "searchDashAndDot",
                                     "searchGroupDashed", "searchGroupDoted", "searchGroupdDashedAndDoted",
                                     "searchMixed", "searchMixedNexus83" ))
        {
            return;
        }
        
    }

    @Test
    public void searchAll()
        throws Exception
    {
        // groupId
        List<NexusArtifact> results = messageUtil.searchFor( "nexus384" );
        Assert.assertEquals( 9, results.size() );
    }

    /*
     * // look on artifactId and groupId
     * @Test public void searchDash() throws Exception { // with dash List<NexusArtifact> results =
     * messageUtil.searchFor( "dash" ); Assert.assertEquals( 5, results.size() ); }
     * @Test public void searchDot() throws Exception { // with dot List<NexusArtifact> results = messageUtil.searchFor(
     * "dot" ); Assert.assertEquals( 5, results.size() ); }
     * @Test public void searchDashAndDot() throws Exception { // with both List<NexusArtifact> results =
     * messageUtil.searchFor( "dot dash" ); Assert.assertEquals( 3, results.size() ); } // look on groupId
     * @Test public void searchGroudDashed() throws Exception { // dashed List<NexusArtifact> results =
     * messageUtil.searchFor( "dashed" ); Assert.assertEquals( 2, results.size() ); }
     * @Test public void searchGroudDoted() throws Exception { // doted List<NexusArtifact> results =
     * messageUtil.searchFor( "doted" ); Assert.assertEquals( 2, results.size() ); }
     * @Test public void searchGroudDashedAndDoted() throws Exception { // both List<NexusArtifact> results =
     * messageUtil.searchFor( "dashed doted" ); Assert.assertEquals( 1, results.size() ); }
     * @Test public void searchMixed() throws Exception { // mixed List<NexusArtifact> results = messageUtil.searchFor(
     * "mixed" ); Assert.assertEquals( 2, results.size() ); }
     * @Test public void searchMixedNexus83() throws Exception { // based on nexus-83 List<NexusArtifact> results =
     * messageUtil.searchFor( "mixed-" ); Assert.assertEquals( 2, results.size() ); }
     */
}
