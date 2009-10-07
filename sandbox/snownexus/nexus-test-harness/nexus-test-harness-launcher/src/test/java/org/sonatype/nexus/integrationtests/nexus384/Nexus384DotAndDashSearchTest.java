/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus384;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

/**
 * Searches for artifact that has a '.' and a '-' in the artifact name.
 */
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

        // if( printKnownErrorButDoNotFail( Nexus384DotAndDashSearchTest.class, "searchDash", "searchDot",
        // "searchDashAndDot",
        // "searchGroupDashed", "searchGroupDoted", "searchGroupdDashedAndDoted",
        // "searchMixed", "searchMixedNexus83" ))
        // {
        // return;
        // }

    }

    @Test
    public void searchAll()
        throws Exception
    {
        // groupId
        List<NexusArtifact> results = messageUtil.searchFor( "nexus384" );
        Assert.assertEquals( 9, results.size() );
    }

    // look on artifactId and groupId
    @Test
    public void searchDash()
        throws Exception
    { // with dash
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchDash" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "dash" );
        Assert.assertEquals( 5, results.size() );
    }

    @Test
    public void searchDot()
        throws Exception
    { // with dot
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchDot" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "dot" );
        Assert.assertEquals( 5, results.size() );
    }

    @Test
    public void searchDashAndDot()
        throws Exception
    { // with both
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchDashAndDot" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "dot dash" );
        Assert.assertEquals( 3, results.size() );
    } // look on groupId

    @Test
    public void searchGroudDashed()
        throws Exception
    { // dashed
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchGroudDashed" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "dashed" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void searchGroudDoted()
        throws Exception
    { // doted
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchGroudDoted" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "doted" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void searchGroudDashedAndDoted()
        throws Exception
    { // both
        
        List<NexusArtifact> results = messageUtil.searchFor( "dashed.doted" );
        Assert.assertEquals( 1, results.size() );
    }

    @Test
    public void searchMixed()
        throws Exception
    { // mixed
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchMixed" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "mixed" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void searchMixedNexus83()
        throws Exception
    { // based on nexus-83
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchMixedNexus83" ))
        {
            return;
        }
        
        List<NexusArtifact> results = messageUtil.searchFor( "mixed-" );
        Assert.assertEquals( 2, results.size() );
    }

}
