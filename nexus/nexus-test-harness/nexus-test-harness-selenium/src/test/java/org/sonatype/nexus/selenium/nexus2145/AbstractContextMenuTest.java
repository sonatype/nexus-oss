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
package org.sonatype.nexus.selenium.nexus2145;

import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Requirement;
import org.hamcrest.CoreMatchers;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractContextMenuTest
    extends SeleniumTest
{

    protected static final String INCREMENTAL_REINDEX_URI = "/data_incremental_index/{domain}/{target}/content";

    protected static final String REBUILD_URI = "/metadata/{domain}/{target}/content";

    protected static final String REINDEX_URI = "/data_index/{domain}/{target}/content";

    protected static final String EXPIRE_CACHE_URI = "/data_cache/{domain}/{target}/content";

    @Requirement
    private Nexus nexus;

    @Requirement
    private RepositoryRegistry config;

    protected M2Repository proxyRepo;

    protected M2Repository hostedRepo;

    public AbstractContextMenuTest()
    {
        super();
    }

    @BeforeClass
    public void createRepo()
        throws Exception
    {
        Maven2ProxyRepositoryTemplate template =
            (Maven2ProxyRepositoryTemplate) nexus.getRepositoryTemplates().getTemplates(
                                                                                         Maven2ProxyRepositoryTemplate.class,
                                                                                         RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setId( getProxyRepositoryId() );
        template.getConfigurableRepository().setName( getProxyRepositoryId() );

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

        hostedTemplate.getConfigurableRepository().setId( getHostedRepositoryId() );
        hostedTemplate.getConfigurableRepository().setName( getHostedRepositoryId() );

        hostedTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        hostedTemplate.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        hostedTemplate.getConfigurableRepository().setExposed( true );
        hostedTemplate.getConfigurableRepository().setUserManaged( true );
        hostedTemplate.getConfigurableRepository().setIndexable( true );
        hostedTemplate.getConfigurableRepository().setBrowseable( true );

        hostedRepo = (M2Repository) hostedTemplate.create();
    }

    private String getHostedRepositoryId()
    {
        return getClass().getSimpleName() + "-hosted";
    }

    private String getProxyRepositoryId()
    {
        return getClass().getSimpleName() + "-proxy";
    }

    @AfterClass
    public void deleteRepo()
        throws Exception
    {
        nexus.deleteRepository( proxyRepo.getId() );
        nexus.deleteRepository( hostedRepo.getId() );
    }

    protected RepositoriesTab startContextMenuTest()
        throws NoSuchRepositoryException
    {
        assertThat( config.getRepository( hostedRepo.getId() ), CoreMatchers.notNullValue() );
        assertThat( config.getRepository( proxyRepo.getId() ), CoreMatchers.notNullValue() );

        doLogin();

        RepositoriesTab repositories = main.openRepositories();
        return repositories;
    }

}