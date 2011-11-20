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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1719;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.integrationtests.RequestFacade.doGetRequest;
import static org.sonatype.nexus.test.utils.TaskScheduleUtil.waitForAllTasksToStop;

import java.io.File;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.Test;

public class NXCM1719UpdateSiteProxyIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1719UpdateSiteProxyIT()
    {
        super( "nxcm1719" );
    }

    @Test
    public void test()
        throws Exception
    {
        {
            final Response response = doGetRequest( "content/repositories/" + getTestRepositoryId() + "/features/" );
            assertThat( response.getStatus().isSuccess(), is( false ) );
        }

        final RepositoryMessageUtil repoUtil = new RepositoryMessageUtil(
            this, getXMLXStream(), MediaType.APPLICATION_XML
        );
        final RepositoryProxyResource repo = (RepositoryProxyResource) repoUtil.getRepository( getTestRepositoryId() );
        repo.getRemoteStorage().setRemoteStorageUrl( TestProperties.getString( "proxy-repo-base-url" ) + "nxcm1719/" );
        repoUtil.updateRepo( repo );

        waitForAllTasksToStop();

        {
            final Response response = doGetRequest( "content/repositories/" + getTestRepositoryId() + "/features/" );
            assertThat( response.getStatus().isSuccess(), is( true ) );
        }

        installAndVerifyP2Feature();
    }

}
