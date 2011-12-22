/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nxcm3600;

import java.io.IOException;
import java.net.URL;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

/**
 * See NXCM-3600 issue for test description.
 * 
 * @author cstamas
 */
public class AbstractNxcm3600IntegrationTest
    extends AbstractPrivilegeTest
{
    private final RepositoryMessageUtil repositoryMessageUtil;

    public AbstractNxcm3600IntegrationTest()
    {
        super( REPO_TEST_HARNESS_RELEASE_REPO );
        this.repositoryMessageUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    protected RepositoryMessageUtil getRepositoryMessageUtil()
    {
        return repositoryMessageUtil;
    }

    /**
     * Sets the exposed flag of repository.
     * 
     * @param exposed
     * @throws IOException
     */
    protected void setExposed( final boolean exposed )
        throws IOException
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        final RepositoryBaseResource releasesRepository =
            getRepositoryMessageUtil().getRepository( REPO_TEST_HARNESS_RELEASE_REPO );
        releasesRepository.setExposed( exposed );
        getRepositoryMessageUtil().updateRepo( releasesRepository );
    }

    protected Status sendMessage( final boolean authenticated, final URL url, Method method )
        throws IOException
    {
        Response response = null;

        final boolean wasSecureTest = TestContainer.getInstance().getTestContext().isSecureTest();

        try
        {
            TestContainer.getInstance().getTestContext().setSecureTest( authenticated );

            response = RequestFacade.sendMessage( url, method, null );

            return response.getStatus();
        }
        finally
        {
            if ( response != null )
            {
                RequestFacade.releaseResponse( response );
            }

            TestContainer.getInstance().getTestContext().setSecureTest( wasSecureTest );
        }
    }
}
