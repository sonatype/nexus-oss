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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2558;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.test.utils.TaskScheduleUtil.runTask;
import static org.sonatype.nexus.test.utils.TaskScheduleUtil.waitForAllTasksToStop;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.isDirectory;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.readable;

import java.io.File;

import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTask;
import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class NXCM2558RedirectUrlUpdateSiteIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2558RedirectUrlUpdateSiteIT()
    {
        super( "nxcm2558" );
    }

    @Test
    public void test()
        throws Exception
    {
        final String nexusTestRepoUrl = getNexusTestRepoUrl();

        final File installDir = new File( "target/eclipse/nxcm2558" );

        final ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( UpdateSiteMirrorTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        repo.setValue( getTestRepositoryId() );

        runTask( UpdateSiteMirrorTask.ROLE_HINT, repo );
        waitForAllTasksToStop();

        final Response response = RequestFacade.doGetRequest(
            "content/repositories/" + getTestRepositoryId() + "/features/"
        );
        assertThat( response.getStatus().isSuccess(), is( true ) );

        installAndVerifyP2Feature();
    }

}
