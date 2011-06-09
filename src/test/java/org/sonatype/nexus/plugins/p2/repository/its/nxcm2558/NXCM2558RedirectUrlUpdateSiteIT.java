/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2558;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTask;
import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;


public class NXCM2558RedirectUrlUpdateSiteIT
    extends AbstractNexusProxyP2IntegrationIT
{

    public NXCM2558RedirectUrlUpdateSiteIT()
    {
        super( "updatesiteproxy" );
    }

    @Test
    public void updatesiteproxy()
        throws Exception
    {
        String nexusTestRepoUrl = getNexusTestRepoUrl();

        File installDir = new File( "target/eclipse/nxcm2558" );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( UpdateSiteMirrorTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        repo.setValue( "updatesiteproxy" );
        TaskScheduleUtil.runTask( UpdateSiteMirrorTask.ROLE_HINT, repo );
        // wait for the tasks
        TaskScheduleUtil.waitForAllTasksToStop();

        Response response =
            RequestFacade.doGetRequest( "content/repositories/" + this.getTestRepositoryId() + "/features/" );
        Assert.assertTrue( "expected success: " + response.getStatus(), response.getStatus().isSuccess() );

        installUsingP2( nexusTestRepoUrl, "com.sonatype.nexus.p2.its.feature.feature.group",
            installDir.getCanonicalPath() );

        File feature = new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertTrue( feature.exists() && feature.isDirectory() );

        File bundle = new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertTrue( bundle.canRead() );
    }

}
