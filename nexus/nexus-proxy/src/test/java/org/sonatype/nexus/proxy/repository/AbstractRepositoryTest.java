package org.sonatype.nexus.proxy.repository;

import org.junit.Test;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link AbstractRepository}
 *
 * @since 2.2
 */
public class AbstractRepositoryTest
    extends TestSupport
{

    private AbstractRepository absRepo = new AbstractRepository()
    {
        @Override
        protected Configurator getConfigurator()
        {
            return null;
        }

        @Override
        protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
        {
            return null;
        }

        @Override
        public RepositoryKind getRepositoryKind()
        {
            return null;
        }

        @Override
        public ContentClass getRepositoryContentClass()
        {
            return null;
        }
    };

    @Test
    public void getExternalConfigurationReturnsNullWhenNotConfigured()
    {
        absRepo.getExternalConfiguration( false );
        absRepo.getExternalConfiguration( true );
    }
}
