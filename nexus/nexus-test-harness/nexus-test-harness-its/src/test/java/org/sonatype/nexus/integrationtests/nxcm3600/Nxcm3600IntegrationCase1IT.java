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

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.hamcrest.MatcherAssert;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.annotations.Test;

/**
 * Case1 of NXCM-3600: anon access disabled.
 * 
 * @author cstamas
 */
public class Nxcm3600IntegrationCase1IT
    extends AbstractNxcm3600IntegrationTest
{
    /**
     * In Case1, we simply disable anonymous access totally, and asserts the expectations.
     * 
     * @throws IOException
     */
    @Test
    public void testCase1()
        throws IOException
    {
        // disable anonymous access
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        settings.setSecurityEnabled( true );
        SettingsMessageUtil.save( settings );

        Status responseStatus;

        // verify assumptions, we have the stuff deployed and present
        // try and verify authenticate /content GET access gives 200
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 200 ) );
        // try and verify authenticated /service/local GET access gives 200
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 200 ) );
        // try and verify anon /content GET access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );
        // try and verify anon /service/local GET access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );

        // now put repository into exposed=false mode
        setExposed( false );

        // READ access
        // try and verify authenticated /content GET access gives 404
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 404 ) );
        // try and verify authenticated /service/local GET access gives 200
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 200 ) );
        // try and verify anon /content GET access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );
        // try and verify anon /service/local GET access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.GET );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );

        // DELETE access
        // try and verify authenticated /content DELETE access gives 404
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.DELETE );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 404 ) );
        // try and verify anon /content DELETE access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "content/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.DELETE );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );
        // try and verify anon /service/local DELETE access gives 401
        responseStatus =
            sendMessage(
                false,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.DELETE );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 401 ) );
        // try and verify authenticated /service/local DELETE access gives 204
        responseStatus =
            sendMessage(
                true,
                RequestFacade.toNexusURL( "service/local/repositories/" + REPO_TEST_HARNESS_RELEASE_REPO
                    + "/content/nxcm3600/artifact/1.0.0/artifact-1.0.0.jar" ), Method.DELETE );
        MatcherAssert.assertThat( responseStatus.getCode(), equalTo( 204 ) );
    }
}
