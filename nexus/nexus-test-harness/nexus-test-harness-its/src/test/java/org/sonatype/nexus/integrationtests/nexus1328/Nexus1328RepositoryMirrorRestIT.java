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
package org.sonatype.nexus.integrationtests.nexus1328;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1328RepositoryMirrorRestIT
    extends AbstractNexusIntegrationTest
{
    protected MirrorMessageUtils messageUtil;

    private String repositoryId = "release-proxy-repo-1";

    public Nexus1328RepositoryMirrorRestIT()
    {
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void setMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();

        MirrorResource resource = new MirrorResource();
        resource.setUrl( "http://setMirrorTest1" );
        request.addData( resource );

        resource = new MirrorResource();
        resource.setUrl( "http://setMirrorTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );
    }

    @Test
    public void updateMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();

        MirrorResource resource = new MirrorResource();
        resource.setUrl( "http://updateMirrorTest1" );
        request.addData( resource );

        resource = new MirrorResource();
        resource.setUrl( "http://updateMirrorTest2" );
        request.addData( resource );

        MirrorResourceListResponse response = this.messageUtil.setMirrors( repositoryId, request );

        request.setData( response.getData() );

        resource = new MirrorResource();
        resource.setUrl( "http://updateMirrorTest3" );
        request.addData( resource );

        ( request.getData().iterator().next() ).setUrl( "http://updateMirrorTest4" );

        response = this.messageUtil.setMirrors( repositoryId, request );

        MirrorResource one = response.getData().get( 0 );
        MirrorResource two = response.getData().get( 1 );
        MirrorResource three = response.getData().get( 2 );

        Assert.assertEquals( "http://updateMirrorTest4", one.getUrl() );
        Assert.assertEquals( "http://updateMirrorTest2", two.getUrl() );
        Assert.assertEquals( "http://updateMirrorTest3", three.getUrl() );
    }

    @Test
    public void getMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();

        MirrorResource resource = new MirrorResource();
        resource.setUrl( "http://getMirrorTest1" );
        request.addData( resource );

        resource = new MirrorResource();
        resource.setUrl( "http://getMirrorTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );

        MirrorResourceListResponse response = this.messageUtil.getMirrors( repositoryId );

        MirrorResource one = response.getData().get( 0 );
        MirrorResource two = response.getData().get( 1 );

        Assert.assertEquals( "http://getMirrorTest1", one.getUrl() );
        Assert.assertEquals( "http://getMirrorTest2", two.getUrl() );
    }

    @Test
    public void mirrorStatusTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();

        MirrorResource resource = new MirrorResource();
        resource.setUrl( "http://mirrorStatusTest1" );
        request.addData( resource );

        resource = new MirrorResource();
        resource.setUrl( "http://mirrorStatusTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );

        MirrorStatusResourceListResponse response = this.messageUtil.getMirrorsStatus( repositoryId );

        MirrorStatusResource one = response.getData().get( 0 );
        MirrorStatusResource two = response.getData().get( 1 );

        Assert.assertEquals( "http://mirrorStatusTest1", one.getUrl() );
        Assert.assertEquals( "http://mirrorStatusTest2", two.getUrl() );
        Assert.assertEquals( "Available", one.getStatus() );
        Assert.assertEquals( "Available", two.getStatus() );
    }

    @Test
    public void predefinedMirrorTest()
        throws IOException
    {
        this.messageUtil.getPredefinedMirrors( repositoryId );
    }
}
