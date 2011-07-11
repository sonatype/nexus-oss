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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1916;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class NXCM1916CRUDP2RepositoryIT
    extends AbstractNexusProxyP2IT
{

    private final RepositoryMessageUtil messageUtil;

    public NXCM1916CRUDP2RepositoryIT()
        throws ComponentLookupException
    {
        super( "nxcm1916" );
        messageUtil = new RepositoryMessageUtil( this, getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void createRepositoryTest()
        throws IOException
    {

        final RepositoryResource resource = new RepositoryResource();

        resource.setId( "createTestRepo" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "p2" );
        resource.setFormat( "p2" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() );

        messageUtil.createRepository( resource );
    }

    @Test
    public void readTest()
        throws IOException
    {

        final RepositoryResource resource = new RepositoryResource();

        resource.setId( "readTestRepo" );
        resource.setRepoType( "hosted" );
        resource.setName( "Read Test Repo" );
        resource.setProvider( "p2" );
        resource.setFormat( "p2" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() );

        messageUtil.createRepository( resource );

        final RepositoryResource responseRepo = (RepositoryResource) messageUtil.getRepository( resource.getId() );

        messageUtil.validateResourceResponse( resource, responseRepo );

    }

    @Test
    public void updateTest()
        throws IOException
    {

        RepositoryResource resource = new RepositoryResource();

        resource.setId( "updateTestRepo" );
        resource.setRepoType( "hosted" );
        resource.setName( "Update Test Repo" );
        resource.setProvider( "p2" );
        resource.setFormat( "p2" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() );

        resource = (RepositoryResource) messageUtil.createRepository( resource );

        resource.setName( "updated repo" );

        messageUtil.updateRepo( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "deleteTestRepo" );
        resource.setRepoType( "hosted" );
        resource.setName( "Delete Test Repo" );
        resource.setProvider( "p2" );
        resource.setFormat( "p2" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() );

        resource = (RepositoryResource) messageUtil.createRepository( resource );

        final Response response = messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository: " + response.getStatus() );
        }
        Assert.assertNull( getNexusConfigUtil().getRepo( resource.getId() ) );
    }

}
