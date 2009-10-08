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
package org.sonatype.nexus.integrationtests.nexus570;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

public class Nexus570IndexArchetypeIT extends AbstractNexusIntegrationTest
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
