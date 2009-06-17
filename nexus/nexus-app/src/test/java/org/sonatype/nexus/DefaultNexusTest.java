/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.Template;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

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

    public void testRepositoryTemplates()
        throws Exception
    {
        List<Template<Repository>> repoTemplates = getDefaultNexus().getRepositoryTemplates();

        assertNotNull( "template list is null", repoTemplates );
        assertEquals( "there should be 6 templates", 6, repoTemplates.size() );

        Template<Repository> template =
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

    public void testListRepositoryContentClasses()
        throws Exception
    {
        Map<String, ContentClass> plexusContentClasses = getContainer().lookupMap( ContentClass.class );

        Collection<ContentClass> contentClasses = repositoryTypeRegistry.getContentClasses();

        assertEquals( plexusContentClasses.size(), contentClasses.size() );

        for ( ContentClass cc : plexusContentClasses.values() )
        {
            assertTrue( contentClasses.contains( cc ) );
        }
    }

    public void testBounceNexus()
        throws Exception
    {
        getDefaultNexus().stop();

        getDefaultNexus().start();
    }
}
