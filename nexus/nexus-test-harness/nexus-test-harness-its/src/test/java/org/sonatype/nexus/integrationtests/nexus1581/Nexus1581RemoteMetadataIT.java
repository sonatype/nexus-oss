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
package org.sonatype.nexus.integrationtests.nexus1581;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1581RemoteMetadataIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus1581RemoteMetadataIT()
    {
        super( "with-mirror-proxy-repo" );
    }
    
    @Test
    public void testGetRemoteMirrorList() throws IOException
    {
        MirrorMessageUtils mirrorUtils = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        MirrorResourceListResponse response = mirrorUtils.getPredefinedMirrors( this.getTestRepositoryId() );
        
        List<MirrorResource> mirrorResources = response.getData();
        
        HashMap<String, String> mirrorIdMap = new HashMap<String, String>();
        
        for ( MirrorResource mirrorResource : mirrorResources )
        {
            mirrorIdMap.put( mirrorResource.getId(), mirrorResource.getUrl() );
        }
        
        Assert.assertTrue( mirrorIdMap.containsKey( "mirror1" ) );
        Assert.assertEquals( TestProperties.getString( "proxy.repo.base.url" )+"/mirror-repo", mirrorIdMap.get( "mirror1" ) );
        
        Assert.assertTrue( mirrorIdMap.containsKey( "mirror2" ) );
        Assert.assertEquals( TestProperties.getString( "proxy.repo.base.url" )+"/void", mirrorIdMap.get( "mirror2" ) );
        
        
        Assert.assertEquals( 2, mirrorResources.size() );
    }
    


}
