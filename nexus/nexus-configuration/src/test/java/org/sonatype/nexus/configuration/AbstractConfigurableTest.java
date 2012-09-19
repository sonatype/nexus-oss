package org.sonatype.nexus.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link AbstractConfigurable}
 *
 * @since 2.2
 */
public class AbstractConfigurableTest
    extends TestSupport
{

    private AbstractConfigurable configurable = new AbstractConfigurable()
    {
        @Override
        protected ApplicationConfiguration getApplicationConfiguration()
        {
            return null;
        }

        @Override
        protected Configurator getConfigurator()
        {
            return null;
        }

        @Override
        protected Object getCurrentConfiguration( final boolean forWrite )
        {
            return null;
        }

        @Override
        protected CoreConfiguration wrapConfiguration( final Object configuration )
            throws ConfigurationException
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }
    };

    @Test
    public void isDirtyNullConfigShouldReturnFalse()
    {
        assertThat( configurable.getCurrentCoreConfiguration(), nullValue() );
        assertThat( configurable.isDirty(), is( false ) );
    }
}
