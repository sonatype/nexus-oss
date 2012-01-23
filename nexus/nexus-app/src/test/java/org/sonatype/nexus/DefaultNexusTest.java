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
package org.sonatype.nexus;

import java.util.Map;

import org.junit.Test;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

public class DefaultNexusTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    private RepositoryTypeRegistry repositoryTypeRegistry;

    private RepositoryRegistry repositoryRegistry;

    public DefaultNexus getDefaultNexus()
    {
        return defaultNexus;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.class );

        repositoryTypeRegistry = lookup( RepositoryTypeRegistry.class );

        repositoryRegistry = lookup( RepositoryRegistry.class );
    }

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    @Test
    public void testRepositoryTemplates()
        throws Exception
    {
        TemplateSet repoTemplates = getDefaultNexus().getRepositoryTemplates();

        assertNotNull( "template list is null", repoTemplates );
        assertEquals( "there should be 12 templates", 12, repoTemplates.size() );

        RepositoryTemplate template =
            (AbstractRepositoryTemplate) getDefaultNexus().getRepositoryTemplateById( "default_hosted_release" );

        assertNotNull( "template should exist", template );
        assertTrue( "it should be RepositoryTemplate", template instanceof AbstractRepositoryTemplate );

        // just adjust some params on template
        {
            // FIXME: how to handle this gracefully and in general way?
            AbstractRepositoryTemplate repoTemplate = (AbstractRepositoryTemplate) template;

            repoTemplate.getConfigurableRepository().setId( "created-from-template" );
            repoTemplate.getConfigurableRepository().setName( "Repository created from template" );
        }

        Repository repository = template.create();

        // this call will throw NoSuchRepositoryException if repo is not registered with registry
        assertTrue( "it should be registered with registry",
                    repositoryRegistry.getRepository( "created-from-template" ) != null );

        assertTrue( "it should be plain hosted release", repository.getRepositoryKind()
            .isFacetAvailable( HostedRepository.class ) );
        assertFalse( "it should be plain hosted release", repository.getRepositoryKind()
            .isFacetAvailable( ProxyRepository.class ) );

        // assertNotNull( getDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_HOSTED_RELEASE ) );
        // assertNotNull( getDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_HOSTED_SNAPSHOT ) );
        // assertNotNull( getDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_PROXY_RELEASE ) );
        // assertNotNull( getDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_PROXY_SNAPSHOT ) );
        // FIXME tamas here you go
        // assertNotNull( getDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_VIRTUAL ) );
    }

    @Test
    public void testListRepositoryContentClasses()
        throws Exception
    {
        Map<String, ContentClass> plexusContentClasses = getContainer().lookupMap( ContentClass.class );

        Map<String, ContentClass> contentClasses = repositoryTypeRegistry.getContentClasses();

        assertEquals( plexusContentClasses.size(), contentClasses.size() );

        assertTrue( plexusContentClasses.values().containsAll( contentClasses.values() ) );
    }

    @Test
    public void testBounceNexus()
        throws Exception
    {
        getDefaultNexus().stop();

        getDefaultNexus().start();
    }
}
