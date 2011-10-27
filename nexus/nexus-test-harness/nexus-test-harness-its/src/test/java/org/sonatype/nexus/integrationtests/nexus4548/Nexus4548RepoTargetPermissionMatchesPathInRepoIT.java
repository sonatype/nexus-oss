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
package org.sonatype.nexus.integrationtests.nexus4548;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.maven.index.artifact.Gav;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * IT testing the pattern matching of repository targets for security permissions.
 * A pattern should fully match paths inside the repo, e.g.
 * {@code ^/g/a/v/.*} should match the path inside the repository and not need to take
 * the URL path (/repositories/id/g/a/v/) into account.
 * <p/>
 * The nexus config is set up to allow the user test/test access to ^/g/a/.* via a repo target permission.
 */
public class Nexus4548RepoTargetPermissionMatchesPathInRepoIT
    extends AbstractNexusIntegrationTest
{

    public Nexus4548RepoTargetPermissionMatchesPathInRepoIT()
    {
        super( "releases" );
    }

    @BeforeClass( alwaysRun = true )
    @BeforeMethod( alwaysRun = true )
    public void setSecureTest()
        throws IOException
    {
        TestContext ctx = TestContainer.getInstance().getTestContext();
        ctx.setSecureTest( true );
        ctx.setUsername( "test" );
        ctx.setPassword( "test" );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        // disable anonymous access
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        settings.setSecurityEnabled( true );
        SettingsMessageUtil.save( settings );
    }

    private void get( final String gavPath, final int code )
        throws IOException
    {
        // could not make the test work with RequestFacade - it would always give access, looks like was using admin user although test context says otherwise?
        // RequestFacade.doGet( AbstractNexusIntegrationTest.REPOSITORY_RELATIVE_URL + "releases/" + gavPath, respondsWithStatusCode( code ) );
        HttpMethod httpMethod = null;
        try
        {
            HttpClient client = new HttpClient();
            client.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( "test", "test" ) );
            httpMethod = new GetMethod( getNexusTestRepoUrl() + gavPath );
            assertThat( client.executeMethod( httpMethod ), Matchers.is( code ) );
        }
        finally
        {
            if ( httpMethod != null )
            {
                httpMethod.releaseConnection();
            }
        }
    }

    private void put( final String gavPath, final int code )
        throws Exception
    {
        PutMethod putMethod = null;
        try
        {
            putMethod = new PutMethod( getNexusTestRepoUrl() + gavPath );
            HttpClient client = new HttpClient();
            client.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( "test", "test" ) );
            putMethod.setRequestEntity( new FileRequestEntity( getTestFile( "pom-a.pom" ), "text/xml" ) );

            assertThat( client.executeMethod( putMethod ), Matchers.is( code ) );
        }
        finally
        {
            if ( putMethod != null )
            {
                putMethod.releaseConnection();
            }
        }

    }

    private void putRest( final String artifactId, final int code )
        throws IOException
    {
        HttpMethod httpMethod = null;
        try
        {
            File testFile = getTestFile( String.format( "pom-%s.pom", artifactId ) );
            httpMethod =
                getDeployUtils().deployPomWithRest( "releases",
                                                    testFile );
            assertThat( httpMethod.getStatusCode(), Matchers.is( code ) );
        }
        finally
        {
            if ( httpMethod != null )
            {
                httpMethod.releaseConnection();
            }
        }
    }

    @Test
    public void testAccessGranted()
        throws Exception
    {
        get( "g/a/v/a-v.pom", 200 );
        put( "g/a/v/a-v-deploy.pom", 201 );
        putRest( "a", 201 );
    }

    @Test
    public void testAccessDenied()
        throws Exception
    {
        get( "g/b/v/b-v.pom", 403 );
        put( "g/b/v/b-v-deploy.pom", 403 );
        putRest( "b", 403 );
    }
}
