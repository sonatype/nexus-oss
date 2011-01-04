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
package org.sonatype.nexus.repositories;

import java.util.List;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;

public class IndexableRepositoryTest
    extends AbstractNexusTestCase
{

    private Nexus nexus;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.nexus = this.lookup( Nexus.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.nexus = null;

        super.tearDown();
    }

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testCreateIndexableM1()
        throws Exception
    {
        String repoId = "indexableM1";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven1HostedRepositoryTemplate.class,
                RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        repoTemplate.getConfigurableRepository().setIndexable( true );

        // will not fail, just create a warning and silently override it
        Repository repository = repoTemplate.create();

        assertFalse( "The repository should be non-indexable!", repository.isIndexable() );
    }

    public void testCreateIndexableM2()
        throws Exception
    {
        String repoId = "indexableM2";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven2HostedRepositoryTemplate.class )
                .getTemplates( RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        repoTemplate.getConfigurableRepository().setIndexable( true );

        repoTemplate.create();
    }

    public void testCreateNonIndexableM2()
        throws Exception
    {
        String repoId = "nonIndexableM2";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven2HostedRepositoryTemplate.class )
                .getTemplates( RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        repoTemplate.getConfigurableRepository().setIndexable( false );

        repoTemplate.create();
    }

}
