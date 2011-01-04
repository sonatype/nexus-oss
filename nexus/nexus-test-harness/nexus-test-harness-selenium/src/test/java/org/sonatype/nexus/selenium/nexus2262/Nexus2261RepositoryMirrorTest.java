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
package org.sonatype.nexus.selenium.nexus2262;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoryMirror;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Component( role = Nexus2261RepositoryMirrorTest.class )
public class Nexus2261RepositoryMirrorTest
    extends SeleniumTest
{

    private M2Repository proxyRepo;

    private Nexus nexus;

    private M2Repository hostedRepo;

    @BeforeClass
    public void createRepo()
        throws Exception
    {
        nexus = lookup( Nexus.class );

        Maven2ProxyRepositoryTemplate template =
            (Maven2ProxyRepositoryTemplate) nexus.getRepositoryTemplates().getTemplates(
                                                                                         Maven2ProxyRepositoryTemplate.class,
                                                                                         RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setId( "nexus2261" );
        template.getConfigurableRepository().setName( "nexus2261" );

        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        template.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setUserManaged( true );
        template.getConfigurableRepository().setIndexable( true );
        template.getConfigurableRepository().setBrowseable( true );

        proxyRepo = (M2Repository) template.create();

        Maven2HostedRepositoryTemplate hostedTemplate =
            (Maven2HostedRepositoryTemplate) nexus.getRepositoryTemplates().getTemplates(
                                                                                          Maven2HostedRepositoryTemplate.class,
                                                                                          RepositoryPolicy.RELEASE ).pick();

        hostedTemplate.getConfigurableRepository().setId( "hosted-nexus2261" );
        hostedTemplate.getConfigurableRepository().setName( "hosted-nexus2261" );

        hostedTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        hostedTemplate.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        hostedTemplate.getConfigurableRepository().setExposed( true );
        hostedTemplate.getConfigurableRepository().setUserManaged( true );
        hostedTemplate.getConfigurableRepository().setIndexable( true );
        hostedTemplate.getConfigurableRepository().setBrowseable( true );

        hostedRepo = (M2Repository) hostedTemplate.create();
    }

    @AfterClass
    public void deleteRepo()
        throws Exception
    {
        nexus.deleteRepository( proxyRepo.getId() );
        nexus.deleteRepository( hostedRepo.getId() );
    }

    @Test
    public void fieldValidationProxy()
        throws Exception
    {
        doLogin();

        RepositoryMirror mirror = main.openRepositories().select( proxyRepo.getId(), RepoKind.PROXY ).selectMirror();

        // invalid mirror format
        mirror.addMirror( "mock-mirror" );
        NxAssert.hasErrorText( mirror.getMirrorUrl(), "Protocol must be http:// or https://" );
        mirror.addMirror( "http://www.sonatype.org" );
        NxAssert.noErrorText( mirror.getMirrorUrl() );

        // duplicated validation
        mirror.addMirror( "http://www.sonatype.org" );
        NxAssert.hasErrorText( mirror.getMirrorUrl(), "This URL already exists" );

        mirror.addMirror( "https://www.sonatype.org" );
        NxAssert.noErrorText( mirror.getMirrorUrl() );

        mirror.cancel();
    }

    @Test
    public void fieldValidationHosted()
        throws Exception
    {
        doLogin();

        RepositoryMirror mirror = main.openRepositories().select( hostedRepo.getId(), RepoKind.HOSTED ).selectMirror();

        // invalid mirror format
        mirror.addMirror( "mock-mirror" );
        NxAssert.hasErrorText( mirror.getMirrorUrl(), "Protocol must be http:// or https://" );
        mirror.addMirror( "http://www.sonatype.org" );
        NxAssert.noErrorText( mirror.getMirrorUrl() );

        // duplicated validation
        mirror.addMirror( "http://www.sonatype.org" );
        NxAssert.hasErrorText( mirror.getMirrorUrl(), "This URL already exists" );

        mirror.addMirror( "https://www.sonatype.org" );
        NxAssert.noErrorText( mirror.getMirrorUrl() );

        mirror.cancel();
    }

    @Test
    public void mirrorProxy()
        throws Exception
    {
        doLogin();

        MirrorResourceListResponse mirrors = new MirrorResourceListResponse();
        MirrorResource m = new MirrorResource();
        m.setId( "mock-mirror" );
        m.setUrl( "http://mock-remove-mirror-url/repo" );
        mirrors.addData( m );

        MockHelper.expect( "/repository_predefined_mirrors/{repositoryId}", new MockResponse( Status.SUCCESS_OK,
                                                                                              mirrors ) );
        RepositoryMirror mirror = main.openRepositories().select( proxyRepo.getId(), RepoKind.PROXY ).selectMirror();

        mirror.addMirror( "mock-mirror" ).addMirror( "http://www.sonatype.org" ).save();

        Assert.assertNotNull( proxyRepo.getDownloadMirrors() );
        Assert.assertNotNull( proxyRepo.getDownloadMirrors().getMirrors() );
        Assert.assertEquals( 2, proxyRepo.getDownloadMirrors().getMirrors().size() );

        mirror.removeMirror( "http://www.sonatype.org" ).save();
        Assert.assertEquals( 1, proxyRepo.getDownloadMirrors().getMirrors().size() );

        mirror.removeAllMirrors().save();
        Assert.assertEquals( 0, proxyRepo.getDownloadMirrors().getMirrors().size() );
    }

    @Test
    public void mirrorHosted()
        throws Exception
    {
        doLogin();

        RepositoryMirror mirror = main.openRepositories().select( hostedRepo.getId(), RepoKind.HOSTED ).selectMirror();

        mirror.addMirror( "http://www.sonatype.org" ).save();
        Assert.assertNotNull( hostedRepo.getPublishedMirrors() );
        Assert.assertNotNull( hostedRepo.getPublishedMirrors().getMirrors() );
        Assert.assertEquals( 1, hostedRepo.getPublishedMirrors().getMirrors().size() );

        mirror.removeAllMirrors().save();
        Assert.assertEquals( 0, hostedRepo.getPublishedMirrors().getMirrors().size() );

    }

}
