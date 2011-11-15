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
package org.sonatype.nexus.integrationtests.nexus3709;

import java.net.URL;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3709FileTypeValidationIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus3709FileTypeValidationIT()
    {
        super( "nexus3709" );
    }

    @Test
    public void testGoodZip()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodzip", "1.0.0", null, "zip", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 200, response.getStatus().getCode() );
    }

    @Test
    public void testBadZip()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badzip", "1.0.0", null, "zip", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 404, response.getStatus().getCode() );
    }

    @Test
    public void testGoodJar()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodjar", "1.0.0", null, "jar", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 200, response.getStatus().getCode() );
    }

    @Test
    public void testBadJar()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badjar", "1.0.0", null, "jar", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 404, response.getStatus().getCode() );
    }

    @Test
    public void testGoodPom()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodpom", "1.0.0", null, "pom", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 200, response.getStatus().getCode() );
    }

    @Test
    public void testBadPom()
        throws Exception
    {
        String relativePath =
            this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badpom", "1.0.0", null, "pom", null, null,
                null, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        Assert.assertEquals( 404, response.getStatus().getCode() );
    }

}
