/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus384;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Searches for artifact that has a '.' and a '-' in the artifact name.
 */
public class Nexus384DotAndDashSearchIT
    extends AbstractNexusIntegrationTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @Test
    public void searchAll()
        throws Exception
    {
        // groupId
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "nexus384" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "dash" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "dot" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "dot dash" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "dashed" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "doted" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void searchGroudDashedAndDoted()
        throws Exception
    { // both
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "dashed.doted" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void searchMixed()
        throws Exception
    { // mixed
        
        if( printKnownErrorButDoNotFail( this.getClass(), "searchMixed" ))
        {
            return;
        }
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "mixed" );
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
        
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "mixed-" );
        Assert.assertEquals( 2, results.size() );
    }

}
