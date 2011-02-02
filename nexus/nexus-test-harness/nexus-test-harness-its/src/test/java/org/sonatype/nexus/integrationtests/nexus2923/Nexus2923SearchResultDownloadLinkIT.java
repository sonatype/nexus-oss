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
package org.sonatype.nexus.integrationtests.nexus2923;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.util.StringUtils;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the 'pom' and 'artifact' download link in the search result panel
 * 
 * @author juven
 */
public class Nexus2923SearchResultDownloadLinkIT
    extends AbstractNexusIntegrationTest
{
     public Nexus2923SearchResultDownloadLinkIT()
    {
        super( "nexus2923" );
    }

    @Override
    public void runOnce()
        throws Exception
    {
        File testRepo = new File( nexusWorkDir, "storage/" + this.getTestRepositoryId() );
        File testFiles = getTestFile( "repo" );
        FileUtils.copyDirectory( testFiles, testRepo );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( this.getTestRepositoryId() );

        TaskScheduleUtil.runTask( UpdateIndexTaskDescriptor.ID, prop );
    }

    @Test
    public void testDownnloadLinks()
        throws Exception
    {
        List<NexusArtifact> artifacts = getSearchMessageUtil().searchFor( "xbean-server" );
        Assert.assertEquals( artifacts.size(), 3, "The artifact should be indexed" );

        for ( NexusArtifact artifact : artifacts )
        {
            if ( StringUtils.isNotEmpty( artifact.getPomLink() ) )
            {
                assertLinkAvailable( artifact.getPomLink() );
            }

            if ( StringUtils.isNotEmpty( artifact.getArtifactLink() ) )
            {
                assertLinkAvailable( artifact.getArtifactLink() );
            }
        }
    }

    private void assertLinkAvailable( String link )
        throws Exception
    {
        Response response = RequestFacade.sendMessage( new URL( link ), Method.GET, null );

        Assert.assertEquals(
            response.getStatus().getCode(),
            301,
            "Invalid link: '" + link + "' response code is '" + response.getStatus().getCode() + "'" );
    }
}
