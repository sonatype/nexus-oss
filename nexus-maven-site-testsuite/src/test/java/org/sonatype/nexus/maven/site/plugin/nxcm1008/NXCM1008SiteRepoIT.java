/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.site.plugin.nxcm1008;

import java.util.List;

import org.junit.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NXCM1008SiteRepoIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

    @Test
    public void testRepoTypeResource()
        throws Exception
    {
        // need to make sure the site plugin is in the repoType resource

        String serviceURI = RequestFacade.SERVICE_LOCAL + "components/repo_types";
        Response response = null;
        try
        {
            response = RequestFacade.doGetRequest( serviceURI );

            String responseText = response.getEntity().getText();

            Assert.assertTrue( "Could not get repoTypes: " + response.getStatus() + ":\n" + responseText, response
                .getStatus().isSuccess() );

            XStreamRepresentation representation = new XStreamRepresentation(
                this.getXMLXStream(),
                responseText,
                MediaType.APPLICATION_XML );

            NexusRepositoryTypeListResourceResponse resourceResponse = (NexusRepositoryTypeListResourceResponse) representation
                .getPayload( new NexusRepositoryTypeListResourceResponse() );

            Assert.assertNotNull( "Resource Response shouldn't be null", resourceResponse );
            List<NexusRepositoryTypeListResource> repoTypes = resourceResponse.getData();

            boolean found = false;
            // now make sure the site is in here
            for ( NexusRepositoryTypeListResource repoType : repoTypes )
            {
                if ( repoType.getFormat().equals( "maven-site" ) )
                {
                    // found it
                    found = true;
                    break;
                }
            }
            Assert.assertTrue( "maven-site not found in list", found );
        }
        finally
        {
            RequestFacade.releaseResponse( response );
        }

    }

    @Test
    public void createSiteRepo()
        throws Exception
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "createSiteRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "createSiteRepo-Name" );
        resource.setProviderRole( WebSiteRepository.class.getName() );
        resource.setProvider( "maven-site" );
        // resource.setFormat( "maven2" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() ); // TODO: this shouldn't be required

        RepositoryMessageUtil messageUtil = new RepositoryMessageUtil(
            this,
            this.getJsonXStream(),
            MediaType.APPLICATION_JSON );
        // this also validates
        // TODO: No, it's not: it uses RepositoryTypeRegistry from _this_ plexus container,
        // while Nexus (where the new repo type is registered) runs in Guice!
        RepositoryResource result = (RepositoryResource) messageUtil.createRepository( resource, false );

        Assert.assertEquals( "maven-site", result.getProvider() );
        Assert.assertEquals( WebSiteRepository.class.getName(), result.getProviderRole() ); // not passed back
    }

}
